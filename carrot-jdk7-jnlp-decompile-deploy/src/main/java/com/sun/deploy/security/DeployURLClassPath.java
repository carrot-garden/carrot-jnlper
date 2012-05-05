package com.sun.deploy.security;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.JARSigningException;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.URLUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.misc.ExtensionDependency;
import sun.misc.FileURLMapper;
import sun.misc.JarIndex;
import sun.misc.JavaUtilJarAccess;
import sun.misc.Resource;
import sun.misc.SharedSecrets;
import sun.misc.URLClassPath;
import sun.net.www.ParseUtil;
import sun.security.action.GetPropertyAction;

public class DeployURLClassPath extends URLClassPath
{
  static final String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
  static final String JAVA_VERSION = (String)AccessController.doPrivileged(new GetPropertyAction("java.version"));
  private static final boolean DEBUG;
  private static boolean hasRealMetaIndex;
  private ArrayList path = new ArrayList();
  private Set knownUrls = new HashSet();
  final Stack urls = new Stack();
  ArrayList loaders = new ArrayList();
  Map lmap = new HashMap();
  private URLStreamHandler jarHandler;
  private DeployURLClassPathCallback cb;
  private boolean isShadowClassPath = false;

  private DeployURLClassPath(URL[] paramArrayOfURL, URLStreamHandlerFactory paramURLStreamHandlerFactory)
  {
    super(paramArrayOfURL);
    this.path.addAll(Arrays.asList(paramArrayOfURL));
    push(paramArrayOfURL);
    if (paramURLStreamHandlerFactory != null)
      this.jarHandler = paramURLStreamHandlerFactory.createURLStreamHandler("jar");
  }

  public DeployURLClassPath(URL[] paramArrayOfURL)
  {
    this(paramArrayOfURL, null);
  }

  public DeployURLClassPath(URLClassPath paramURLClassPath)
  {
    this(paramURLClassPath.getURLs());
  }

  public DeployURLClassPath(URL[] paramArrayOfURL, boolean paramBoolean)
  {
    this(paramArrayOfURL);
    this.isShadowClassPath = paramBoolean;
  }

  public void addURL(URL paramURL)
  {
    synchronized (this.urls)
    {
      String str = URLUtil.toNormalizedString(paramURL);
      if (this.knownUrls.contains(str))
        return;
      this.knownUrls.add(str);
      this.urls.add(0, paramURL);
      this.path.add(paramURL);
    }
  }

  public URL[] getURLs()
  {
    synchronized (this.urls)
    {
      return (URL[])(URL[])this.path.toArray(new URL[this.path.size()]);
    }
  }

  public void setDeployURLClassPathCallback(DeployURLClassPathCallback paramDeployURLClassPathCallback)
  {
    this.cb = paramDeployURLClassPathCallback;
  }

  public URL findResource(String paramString, boolean paramBoolean)
  {
    if (URLClassPathControl.isDisabledInCurrentThread())
      return null;
    PathIterator localPathIterator = new PathIterator();
    Loader localLoader;
    while ((localLoader = getLoader(localPathIterator)) != null)
    {
      URL localURL = localLoader.findResource(paramString, paramBoolean, localPathIterator);
      if (localURL != null)
        return localURL;
      localPathIterator.next();
    }
    return null;
  }

  public Resource getResource(String paramString, boolean paramBoolean)
  {
    if (URLClassPathControl.isDisabledInCurrentThread())
      return null;
    if (DEBUG)
      System.err.println("URLClassPath.getResource(\"" + paramString + "\")");
    PathIterator localPathIterator = new PathIterator();
    Loader localLoader;
    while ((localLoader = getLoader(localPathIterator)) != null)
    {
      Resource localResource = localLoader.getResource(paramString, paramBoolean, localPathIterator);
      if (localResource != null)
        return localResource;
      localPathIterator.next();
    }
    return null;
  }

  public Enumeration findResources(String paramString, boolean paramBoolean)
  {
    if (URLClassPathControl.isDisabledInCurrentThread())
      return Collections.enumeration(Collections.EMPTY_LIST);
    return new Enumeration(paramString, paramBoolean)
    {
      private DeployURLClassPath.PathIterator pi;
      private URL url;
      private final String val$name;
      private final boolean val$check;

      private boolean next()
      {
        if (this.url != null)
          return true;
        DeployURLClassPath.Loader localLoader;
        while ((localLoader = DeployURLClassPath.this.getLoader(this.pi)) != null)
        {
          this.url = localLoader.findResource(this.val$name, this.val$check, this.pi);
          this.pi.nextResource();
          if (this.url != null)
            return true;
        }
        return false;
      }

      public boolean hasMoreElements()
      {
        return next();
      }

      public Object nextElement()
      {
        if (!next())
          throw new NoSuchElementException();
        URL localURL = this.url;
        this.url = null;
        return localURL;
      }
    };
  }

  public Resource getResource(String paramString)
  {
    return getResource(paramString, true);
  }

  public Enumeration getResources(String paramString, boolean paramBoolean)
  {
    if (URLClassPathControl.isDisabledInCurrentThread())
      return Collections.enumeration(Collections.EMPTY_LIST);
    return new Enumeration(paramString, paramBoolean)
    {
      DeployURLClassPath.PathIterator pi;
      private Resource res;
      private final String val$name;
      private final boolean val$check;

      private boolean next()
      {
        if (this.res != null)
          return true;
        DeployURLClassPath.Loader localLoader;
        while ((localLoader = DeployURLClassPath.this.getLoader(this.pi)) != null)
        {
          this.res = localLoader.getResource(this.val$name, this.val$check, this.pi);
          this.pi.nextResource();
          if (this.res != null)
            return true;
        }
        return false;
      }

      public boolean hasMoreElements()
      {
        return next();
      }

      public Object nextElement()
      {
        if (!next())
          throw new NoSuchElementException();
        Resource localResource = this.res;
        this.res = null;
        return localResource;
      }
    };
  }

  public Enumeration getResources(String paramString)
  {
    return getResources(paramString, true);
  }

  private synchronized Loader getLoader(PathIterator paramPathIterator)
  {
    if (paramPathIterator.found())
      return null;
    while (this.loaders.size() < paramPathIterator.index() + 1)
    {
      URL localURL;
      synchronized (this.urls)
      {
        if (this.urls.empty())
          return null;
        localURL = (URL)this.urls.pop();
      }
      ??? = URLUtil.urlNoFragString(localURL);
      if (this.lmap.containsKey(???))
        continue;
      Object localObject2;
      try
      {
        localObject2 = getLoader(localURL);
        URL[] arrayOfURL = ((Loader)localObject2).getClassPath();
        if (arrayOfURL != null)
          push(arrayOfURL);
      }
      catch (JARSigningException localJARSigningException)
      {
        localObject2 = new InvalidLoader(localJARSigningException, localJARSigningException.getLocation());
      }
      catch (IOException localIOException)
      {
        Trace.ignored(localIOException);
      }
      continue;
      this.loaders.add(localObject2);
      this.lmap.put(???, localObject2);
    }
    return (Loader)(Loader)(Loader)this.loaders.get(paramPathIterator.index());
  }

  private Loader getLoader(URL paramURL)
    throws IOException
  {
    try
    {
      return (Loader)AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL)
      {
        private final URL val$url;

        public Object run()
          throws IOException
        {
          String str = this.val$url.getFile();
          if ((str != null) && (str.endsWith("/")))
          {
            if ("file".equals(this.val$url.getProtocol()))
              return new DeployURLClassPath.FileLoader(DeployURLClassPath.this, this.val$url);
            return new DeployURLClassPath.UrlLoader(DeployURLClassPath.this, this.val$url);
          }
          return new DeployURLClassPath.JarLoader(DeployURLClassPath.this, this.val$url, DeployURLClassPath.this.jarHandler, DeployURLClassPath.this.lmap);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
    }
    throw ((IOException)localPrivilegedActionException.getException());
  }

  private void push(URL[] paramArrayOfURL)
  {
    synchronized (this.urls)
    {
      for (int i = paramArrayOfURL.length - 1; i >= 0; i--)
        this.urls.push(paramArrayOfURL[i]);
    }
  }

  public URL checkURL(URL paramURL)
  {
    try
    {
      check(paramURL);
    }
    catch (Exception localException)
    {
      return null;
    }
    return paramURL;
  }

  static void check(URL paramURL)
    throws IOException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      URLConnection localURLConnection = paramURL.openConnection();
      Permission localPermission = localURLConnection.getPermission();
      if (localPermission != null)
        try
        {
          localSecurityManager.checkPermission(localPermission);
        }
        catch (SecurityException localSecurityException)
        {
          if (((localPermission instanceof FilePermission)) && (localPermission.getActions().indexOf("read") != -1))
          {
            localSecurityManager.checkRead(localPermission.getName());
          }
          else if (((localPermission instanceof SocketPermission)) && (localPermission.getActions().indexOf("connect") != -1))
          {
            URL localURL = paramURL;
            if ((localURLConnection instanceof JarURLConnection))
              localURL = ((JarURLConnection)localURLConnection).getJarFileURL();
            localSecurityManager.checkConnect(localURL.getHost(), localURL.getPort());
          }
          else
          {
            throw localSecurityException;
          }
        }
    }
  }

  public synchronized List closeLoaders()
  {
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = this.lmap.values().iterator();
    while (localIterator.hasNext())
      try
      {
        Loader localLoader = (Loader)localIterator.next();
        if (localLoader != null)
          localLoader.close();
      }
      catch (Exception localException)
      {
        localArrayList.add(localException);
      }
    this.lmap.clear();
    this.loaders.clear();
    return localArrayList;
  }

  private String parseUtilEncodePath(String paramString, boolean paramBoolean)
  {
    try
    {
      return ParseUtil.encodePath(paramString, paramBoolean);
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
    }
    return ParseUtil.encodePath(paramString);
  }

  private static URL createBaseJarURL(URL paramURL, URLStreamHandler paramURLStreamHandler)
    throws IOException
  {
    return new URL("jar", "", -1, paramURL + "!/", paramURLStreamHandler);
  }

  static
  {
    int i = AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.debug")) != null ? 1 : 0;
    DEBUG = (i != 0) || (Config.getPluginDebug()) || (Config.getDeployDebug());
    try
    {
      if (Class.forName("sun.misc.MetaIndex") != null)
        hasRealMetaIndex = true;
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
    }
  }

  private class FileLoader extends DeployURLClassPath.Loader
  {
    private File dir;

    FileLoader(URL arg2)
      throws IOException
    {
      super();
      if (!"file".equals(localURL.getProtocol()))
        throw new IllegalArgumentException("url");
      String str = localURL.getFile().replace('/', File.separatorChar);
      str = ParseUtil.decode(str);
      this.dir = new File(str).getCanonicalFile();
      if (DeployURLClassPath.this.cb != null)
      {
        this.cpe = DeployURLClassPath.this.cb.openClassPathElement(localURL);
        this.skip = this.cpe.skip();
        this.defer = this.cpe.defer();
      }
    }

    URL findResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator)
    {
      try
      {
        Resource localResource = getResource(paramString, paramBoolean, paramPathIterator);
        if (localResource != null)
          return localResource.getURL();
      }
      catch (Exception localException)
      {
      }
      return null;
    }

    Resource getResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator)
    {
      if (this.skip)
        return null;
      URL localURL1;
      File localFile;
      try
      {
        URL localURL2 = new URL(getBaseURL(), ".");
        localURL1 = new URL(getBaseURL(), DeployURLClassPath.this.parseUtilEncodePath(paramString, false));
        if (!URLUtil.checkTargetURL(getBaseURL(), localURL1))
          throw new SecurityException("Permission denied: " + localURL1);
        if (!localURL1.getFile().startsWith(localURL2.getFile()))
          return null;
        if (paramBoolean)
          DeployURLClassPath.check(localURL1);
        if (paramString.indexOf("..") != -1)
        {
          localFile = new File(this.dir, paramString.replace('/', File.separatorChar)).getCanonicalFile();
          if (!localFile.getPath().startsWith(this.dir.getPath()))
            return null;
        }
        else
        {
          localFile = new File(this.dir, paramString.replace('/', File.separatorChar));
        }
        if (!localFile.exists())
          return null;
      }
      catch (Exception localException)
      {
        return null;
      }
      if (DeployURLClassPath.this.cb != null)
      {
        if (this.defer)
        {
          paramPathIterator.found(true);
          return null;
        }
        try
        {
          this.cpe.checkResource(paramString);
        }
        catch (SecurityException localSecurityException)
        {
          Trace.println("resource name \"" + paramString + "\" in " + this.dir + " : " + localSecurityException, TraceLevel.SECURITY);
          throw localSecurityException;
        }
      }
      Object localObject = localFile;
      return new Resource(paramString, localURL1, localObject)
      {
        private final String val$name;
        private final URL val$url;
        private final File val$file;

        public String getName()
        {
          return this.val$name;
        }

        public URL getURL()
        {
          return this.val$url;
        }

        public URL getCodeSourceURL()
        {
          return DeployURLClassPath.FileLoader.this.getBaseURL();
        }

        public InputStream getInputStream()
          throws IOException
        {
          return new FileInputStream(this.val$file);
        }

        public int getContentLength()
          throws IOException
        {
          return (int)this.val$file.length();
        }
      };
    }

    void close()
      throws IOException
    {
    }
  }

  class JarLoader extends DeployURLClassPath.Loader
  {
    private JarFile jar;
    private URL csu;
    private URL redirectFinalBase;
    private JarIndex index;
    private DeployURLClassPath.MetaIndex metaIndex;
    private URLStreamHandler handler;
    private Map lmap;

    JarLoader(URL paramURLStreamHandler, URLStreamHandler paramMap, Map arg4)
      throws IOException
    {
      super();
      this.csu = paramURLStreamHandler;
      this.handler = paramMap;
      Object localObject;
      this.lmap = localObject;
      if (!isOptimizable(paramURLStreamHandler))
      {
        ensureOpen();
      }
      else
      {
        String str = paramURLStreamHandler.getFile();
        if (str != null)
        {
          str = ParseUtil.decode(str);
          File localFile = new File(str);
          this.metaIndex = DeployURLClassPath.MetaIndex.forJar(localFile);
          if ((this.metaIndex != null) && (!localFile.exists()))
            this.metaIndex = null;
        }
        if (this.metaIndex == null)
          ensureOpen();
      }
    }

    JarFile getJarFile()
    {
      return this.jar;
    }

    private boolean isOptimizable(URL paramURL)
    {
      return "file".equals(paramURL.getProtocol());
    }

    private void ensureOpen()
      throws IOException
    {
      if (this.jar == null)
      {
        int i = DownloadEngine.incrementInternalUse();
        try
        {
          AccessController.doPrivileged(new PrivilegedExceptionAction()
          {
            public Object run()
              throws IOException
            {
              if (DeployURLClassPath.DEBUG)
                System.err.println("Opening jar " + DeployURLClassPath.JarLoader.this.csu);
              DeployURLClassPath.JarLoader.access$902(DeployURLClassPath.JarLoader.this, DeployURLClassPath.JarLoader.this.getJarFile(DeployURLClassPath.JarLoader.this.csu));
              if (DeployURLClassPath.hasRealMetaIndex)
                DeployURLClassPath.JarLoader.access$1202(DeployURLClassPath.JarLoader.this, JarIndex.getJarIndex(DeployURLClassPath.JarLoader.this.jar, null));
              else
                DeployURLClassPath.JarLoader.access$1202(DeployURLClassPath.JarLoader.this, JarIndex.getJarIndex(DeployURLClassPath.JarLoader.this.jar));
              if (DeployURLClassPath.JarLoader.this.index != null)
              {
                String[] arrayOfString = DeployURLClassPath.JarLoader.this.index.getJarFiles();
                for (int i = 0; i < arrayOfString.length; i++)
                  try
                  {
                    URL localURL = new URL(DeployURLClassPath.JarLoader.this.csu, arrayOfString[i]);
                    if (!URLUtil.checkTargetURL(DeployURLClassPath.JarLoader.this.csu, localURL))
                      throw new SecurityException("Permission denied: " + localURL);
                    String str = URLUtil.urlNoFragString(localURL);
                    if (!DeployURLClassPath.JarLoader.this.lmap.containsKey(str))
                      DeployURLClassPath.JarLoader.this.lmap.put(str, null);
                  }
                  catch (MalformedURLException localMalformedURLException)
                  {
                  }
              }
              return null;
            }
          });
        }
        catch (PrivilegedActionException localPrivilegedActionException)
        {
          throw ((IOException)localPrivilegedActionException.getException());
        }
        finally
        {
          DownloadEngine.decrementInternalUse(i);
        }
      }
    }

    private JarFile getJarFile(URL paramURL)
      throws IOException
    {
      Object localObject;
      JarFile localJarFile;
      if (isOptimizable(paramURL))
      {
        localObject = new FileURLMapper(paramURL);
        if (!((FileURLMapper)localObject).exists())
          throw new FileNotFoundException(((FileURLMapper)localObject).getPath());
        localJarFile = JarVerifier.getValidatedJarFile(new File(((FileURLMapper)localObject).getPath()), paramURL, paramURL, null, null);
      }
      else
      {
        localObject = (JarURLConnection)getBaseURL().openConnection();
        ((JarURLConnection)localObject).setRequestProperty("UA-Java-Version", DeployURLClassPath.JAVA_VERSION);
        localJarFile = ((JarURLConnection)localObject).getJarFile();
        URL localURL = DownloadEngine.getKnownRedirectFinalURL(this.csu);
        if (!URLUtil.sameURLs(localURL, this.csu))
        {
          this.csu = localURL;
          this.redirectFinalBase = DeployURLClassPath.access$700(this.csu, this.handler);
        }
      }
      if (DeployURLClassPath.this.cb != null)
      {
        this.cpe = DeployURLClassPath.this.cb.openClassPathElement(localJarFile, this.csu);
        this.skip = this.cpe.skip();
        this.defer = this.cpe.defer();
      }
      return (JarFile)localJarFile;
    }

    URL getBaseURL()
    {
      if (this.redirectFinalBase != null)
        return this.redirectFinalBase;
      return super.getBaseURL();
    }

    JarIndex getIndex()
    {
      try
      {
        ensureOpen();
      }
      catch (IOException localIOException)
      {
        throw ((InternalError)new InternalError().initCause(localIOException));
      }
      return this.index;
    }

    Resource checkResource(String paramString, boolean paramBoolean, JarEntry paramJarEntry, JarFile paramJarFile, DeployURLClassPath.PathIterator paramPathIterator)
    {
      URL localURL;
      try
      {
        localURL = new URL(getBaseURL(), DeployURLClassPath.this.parseUtilEncodePath(paramString, false));
        if (!URLUtil.checkTargetURL(getBaseURL(), localURL))
          throw new SecurityException("Permission denied: " + localURL);
        if (paramBoolean)
          DeployURLClassPath.check(localURL);
      }
      catch (MalformedURLException localMalformedURLException)
      {
        return null;
      }
      catch (IOException localIOException)
      {
        return null;
      }
      catch (AccessControlException localAccessControlException)
      {
        return null;
      }
      if (DeployURLClassPath.this.cb != null)
      {
        if (this.defer)
        {
          paramPathIterator.found(true);
          return null;
        }
        try
        {
          this.cpe.checkResource(paramString);
        }
        catch (SecurityException localSecurityException)
        {
          Trace.println("resource name \"" + paramString + "\" in " + this.csu + " : " + localSecurityException, TraceLevel.SECURITY);
          throw localSecurityException;
        }
      }
      if (Config.isJavaVersionAtLeast15())
        return new Resource(paramString, localURL, paramJarFile, paramJarEntry)
        {
          private final String val$name;
          private final URL val$url;
          private final JarFile val$jar;
          private final JarEntry val$entry;

          public String getName()
          {
            return this.val$name;
          }

          public URL getURL()
          {
            return this.val$url;
          }

          public URL getCodeSourceURL()
          {
            return DeployURLClassPath.JarLoader.this.csu;
          }

          public InputStream getInputStream()
            throws IOException
          {
            return this.val$jar.getInputStream(this.val$entry);
          }

          public int getContentLength()
          {
            return (int)this.val$entry.getSize();
          }

          public Manifest getManifest()
            throws IOException
          {
            return this.val$jar.getManifest();
          }

          public Certificate[] getCertificates()
          {
            return this.val$entry.getCertificates();
          }

          public CodeSigner[] getCodeSigners()
          {
            return this.val$entry.getCodeSigners();
          }
        };
      return new Resource(paramString, localURL, paramJarFile, paramJarEntry)
      {
        private final String val$name;
        private final URL val$url;
        private final JarFile val$jar;
        private final JarEntry val$entry;

        public String getName()
        {
          return this.val$name;
        }

        public URL getURL()
        {
          return this.val$url;
        }

        public URL getCodeSourceURL()
        {
          return DeployURLClassPath.JarLoader.this.csu;
        }

        public InputStream getInputStream()
          throws IOException
        {
          return this.val$jar.getInputStream(this.val$entry);
        }

        public int getContentLength()
        {
          return (int)this.val$entry.getSize();
        }

        public Manifest getManifest()
          throws IOException
        {
          return this.val$jar.getManifest();
        }

        public Certificate[] getCertificates()
        {
          return this.val$entry.getCertificates();
        }
      };
    }

    URL findResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator)
    {
      try
      {
        Resource localResource = getResource(paramString, paramBoolean, paramPathIterator);
        if (localResource != null)
          return localResource.getURL();
      }
      catch (Exception localException)
      {
      }
      return null;
    }

    Resource getResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator)
    {
      if (this.skip)
        return null;
      if ((this.metaIndex != null) && (!this.metaIndex.mayContain(paramString)))
        return null;
      try
      {
        ensureOpen();
      }
      catch (IOException localIOException)
      {
        throw ((InternalError)new InternalError().initCause(localIOException));
      }
      JarEntry localJarEntry = this.jar.getJarEntry(paramString);
      if (localJarEntry != null)
        return checkResource(paramString, paramBoolean, localJarEntry, this.jar, paramPathIterator);
      if (this.index == null)
        return null;
      HashSet localHashSet = new HashSet();
      return getResource(paramString, paramBoolean, localHashSet, paramPathIterator);
    }

    Resource getResource(String paramString, boolean paramBoolean, Set paramSet, DeployURLClassPath.PathIterator paramPathIterator)
    {
      int i = 0;
      int j = 0;
      LinkedList localLinkedList;
      if ((localLinkedList = this.index.get(paramString)) == null)
        return null;
      do
      {
        Object[] arrayOfObject = localLinkedList.toArray();
        int k = localLinkedList.size();
        while (j < k)
        {
          String str1 = (String)arrayOfObject[(j++)];
          URL localURL;
          JarLoader localJarLoader;
          try
          {
            localURL = new URL(this.csu, str1);
            if (!URLUtil.checkTargetURL(this.csu, localURL))
              throw new SecurityException("Permission denied: " + localURL);
            String str2 = URLUtil.urlNoFragString(localURL);
            if ((localJarLoader = (JarLoader)this.lmap.get(str2)) == null)
            {
              localJarLoader = (JarLoader)AccessController.doPrivileged(new PrivilegedExceptionAction(localURL)
              {
                private final URL val$url;

                public Object run()
                  throws IOException
                {
                  return new DeployURLClassPath.JarLoader(DeployURLClassPath.this, this.val$url, DeployURLClassPath.JarLoader.this.handler, DeployURLClassPath.JarLoader.this.lmap);
                }
              });
              JarIndex localJarIndex = localJarLoader.getIndex();
              if (localJarIndex != null)
              {
                int n = str1.lastIndexOf("/");
                localJarIndex.merge(this.index, n == -1 ? null : str1.substring(0, n + 1));
              }
              this.lmap.put(str2, localJarLoader);
            }
          }
          catch (PrivilegedActionException localPrivilegedActionException)
          {
            continue;
          }
          catch (MalformedURLException localMalformedURLException)
          {
          }
          continue;
          int m = !paramSet.add(URLUtil.urlNoFragString(localURL)) ? 1 : 0;
          if (m == 0)
          {
            try
            {
              localJarLoader.ensureOpen();
            }
            catch (IOException localIOException)
            {
              throw ((InternalError)new InternalError().initCause(localIOException));
            }
            JarEntry localJarEntry = localJarLoader.jar.getJarEntry(paramString);
            if (localJarEntry != null)
              return localJarLoader.checkResource(paramString, paramBoolean, localJarEntry, localJarLoader.jar, paramPathIterator);
          }
          if ((m != 0) || (localJarLoader == this) || (localJarLoader.getIndex() == null))
            continue;
          Resource localResource;
          if ((localResource = localJarLoader.getResource(paramString, paramBoolean, paramSet, paramPathIterator)) != null)
            return localResource;
        }
        localLinkedList = this.index.get(paramString);
      }
      while (j < localLinkedList.size());
      return null;
    }

    URL[] getClassPath()
      throws IOException
    {
      if (this.index != null)
        return null;
      if (this.metaIndex != null)
        return null;
      ensureOpen();
      if (!DeployURLClassPath.this.isShadowClassPath)
        parseExtensionsDependencies();
      Manifest localManifest;
      Attributes localAttributes;
      String str;
      if (Config.checkClassName("sun.misc.SharedSecrets"))
      {
        if (SharedSecrets.javaUtilJarAccess().jarFileHasClassPathAttribute(this.jar))
        {
          localManifest = this.jar.getManifest();
          if (localManifest != null)
          {
            localAttributes = localManifest.getMainAttributes();
            if (localAttributes != null)
            {
              str = localAttributes.getValue(Attributes.Name.CLASS_PATH);
              if (str != null)
                return parseClassPath(this.csu, str);
            }
          }
        }
      }
      else
      {
        localManifest = this.jar.getManifest();
        if (localManifest != null)
        {
          localAttributes = localManifest.getMainAttributes();
          if (localAttributes != null)
          {
            str = localAttributes.getValue(Attributes.Name.CLASS_PATH);
            if (str != null)
              return parseClassPath(this.csu, str);
          }
        }
      }
      return null;
    }

    private void parseExtensionsDependencies()
      throws IOException
    {
      ExtensionDependency.checkExtensionsDependencies(this.jar);
    }

    private URL[] parseClassPath(URL paramURL, String paramString)
      throws MalformedURLException
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString);
      URL[] arrayOfURL = new URL[localStringTokenizer.countTokens()];
      for (int i = 0; localStringTokenizer.hasMoreTokens(); i++)
      {
        String str = localStringTokenizer.nextToken();
        arrayOfURL[i] = new URL(paramURL, str);
        if (URLUtil.checkTargetURL(paramURL, arrayOfURL[i]))
          continue;
        throw new SecurityException("Permission denied: " + arrayOfURL[i]);
      }
      return arrayOfURL;
    }

    void close()
      throws IOException
    {
      if (this.jar != null)
        this.jar.close();
      this.jar = null;
    }
  }

  static abstract class Loader
  {
    private final URL base;
    protected DeployURLClassPathCallback.Element cpe;
    protected boolean skip;
    protected boolean defer;

    Loader(URL paramURL)
    {
      this.base = paramURL;
    }

    URL getBaseURL()
    {
      return this.base;
    }

    URL[] getClassPath()
      throws IOException
    {
      return null;
    }

    abstract URL findResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator);

    abstract Resource getResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator);

    abstract void close()
      throws IOException;
  }

  private static class MetaIndex
  {
    static MetaIndex forJar(File paramFile)
    {
      return null;
    }

    boolean mayContain(String paramString)
    {
      return false;
    }
  }

  static class PathIterator
  {
    int index;
    boolean found;

    int index()
    {
      return this.index;
    }

    void next()
    {
      this.index += 1;
    }

    void nextResource()
    {
      this.index += 1;
      this.found = false;
    }

    boolean found()
    {
      return this.found;
    }

    void found(boolean paramBoolean)
    {
      this.found = paramBoolean;
    }
  }

  private class UrlLoader extends DeployURLClassPath.Loader
  {
    UrlLoader(URL arg2)
      throws IOException
    {
      super();
      if (DeployURLClassPath.this.cb != null)
      {
        this.cpe = DeployURLClassPath.this.cb.openClassPathElement(localURL);
        this.skip = this.cpe.skip();
        this.defer = this.cpe.defer();
      }
    }

    URL findResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator)
    {
      if (this.skip)
        return null;
      URL localURL;
      try
      {
        localURL = new URL(getBaseURL(), DeployURLClassPath.this.parseUtilEncodePath(paramString, false));
        if (!URLUtil.checkTargetURL(getBaseURL(), localURL))
          throw new SecurityException("Permission denied: " + localURL);
      }
      catch (MalformedURLException localMalformedURLException)
      {
        throw new IllegalArgumentException("name");
      }
      try
      {
        if (paramBoolean)
          DeployURLClassPath.check(localURL);
        URLConnection localURLConnection = localURL.openConnection();
        Object localObject;
        if ((localURLConnection instanceof HttpURLConnection))
        {
          localObject = (HttpURLConnection)localURLConnection;
          ((HttpURLConnection)localObject).setRequestMethod("HEAD");
          if (((HttpURLConnection)localObject).getResponseCode() >= 400)
            return null;
        }
        else
        {
          localObject = localURL.openStream();
          ((InputStream)localObject).close();
        }
        if (this.cpe != null)
        {
          if (this.defer)
          {
            paramPathIterator.found(true);
            return null;
          }
          this.cpe.checkResource(paramString);
        }
        return DownloadEngine.getKnownRedirectFinalURL(localURL);
      }
      catch (SecurityException localSecurityException)
      {
        Trace.println("resource name \"" + paramString + "\" in " + localURL + " : " + localSecurityException, TraceLevel.SECURITY);
        return null;
      }
      catch (Exception localException)
      {
      }
      return (URL)null;
    }

    Resource getResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator)
    {
      if (this.skip)
        return null;
      URL localURL1;
      try
      {
        localURL1 = new URL(getBaseURL(), DeployURLClassPath.this.parseUtilEncodePath(paramString, false));
        if (!URLUtil.checkTargetURL(getBaseURL(), localURL1))
          throw new SecurityException("Permission denied: " + localURL1);
      }
      catch (MalformedURLException localMalformedURLException)
      {
        throw new IllegalArgumentException("name");
      }
      URLConnection localURLConnection;
      try
      {
        if (paramBoolean)
          DeployURLClassPath.check(localURL1);
        localURLConnection = localURL1.openConnection();
        InputStream localInputStream = localURLConnection.getInputStream();
      }
      catch (Exception localException)
      {
        if (DeployURLClassPath.DEBUG)
          Trace.ignored(localException);
        return null;
      }
      if (DeployURLClassPath.this.cb != null)
      {
        if (this.defer)
        {
          paramPathIterator.found(true);
          return null;
        }
        try
        {
          this.cpe.checkResource(paramString);
        }
        catch (SecurityException localSecurityException)
        {
          Trace.println("resource name \"" + paramString + "\" in " + localURL1 + " : " + localSecurityException, TraceLevel.SECURITY);
          throw localSecurityException;
        }
      }
      boolean bool = !URLUtil.sameURLs(localURL1, localURLConnection.getURL());
      URL localURL2 = bool ? localURLConnection.getURL() : DownloadEngine.getKnownRedirectFinalURL(localURL1);
      return new Resource(paramString, localURL2, bool, localURLConnection, localURL1)
      {
        private InputStream in;
        private final String val$name;
        private final URL val$targetURL;
        private final boolean val$redirect;
        private final URLConnection val$uc;
        private final URL val$url;

        public String getName()
        {
          return this.val$name;
        }

        public URL getURL()
        {
          return this.val$targetURL;
        }

        public URL getCodeSourceURL()
        {
          return this.val$targetURL;
        }

        public synchronized InputStream getInputStream()
          throws IOException
        {
          if (this.in == null)
            if (this.val$redirect)
              this.in = new BufferedInputStream(this.val$uc.getInputStream())
              {
                boolean closeCalled;

                public void close()
                  throws IOException
                {
                  if (!this.closeCalled)
                  {
                    this.closeCalled = true;
                    super.close();
                    Cache.createRedirectEntry(DeployURLClassPath.UrlLoader.1.this.val$url, DeployURLClassPath.UrlLoader.1.this.val$uc.getURL(), null);
                  }
                }
              };
            else
              this.in = this.val$uc.getInputStream();
          return this.in;
        }

        public int getContentLength()
          throws IOException
        {
          return this.val$uc.getContentLength();
        }
      };
    }

    void close()
      throws IOException
    {
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.DeployURLClassPath
 * JD-Core Version:    0.6.0
 */