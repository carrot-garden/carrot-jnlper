package com.sun.jnlp;

import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.Environment;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.config.Platform;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.perf.DeployPerfUtil;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.deploy.util.NativeLibraryBundle;
import com.sun.javaws.LaunchDownload;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.progress.Progress;
import com.sun.javaws.security.AppPolicy;
import com.sun.javaws.util.JNLPUtils;
import java.awt.AWTPermission;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.jnlp.BasicService;
import javax.jnlp.ClipboardService;
import javax.jnlp.DownloadService;
import javax.jnlp.DownloadService2;
import javax.jnlp.ExtendedService;
import javax.jnlp.ExtensionInstallerService;
import javax.jnlp.FileOpenService;
import javax.jnlp.FileSaveService;
import javax.jnlp.IntegrationService;
import javax.jnlp.PersistenceService;
import javax.jnlp.PrintService;
import javax.jnlp.SingleInstanceService;
import sun.misc.Resource;
import sun.misc.URLClassPath;

public final class JNLPPreverifyClassLoader extends URLClassLoader
  implements JNLPClassLoaderIf
{
  private static Field ucpField = getUCPField("ucp");
  private static Method defineClassMethod = getDefineClassMethod("defineClass");
  private static JNLPPreverifyClassLoader _instance = null;
  private ClassLoader _delegatingClassLoader = null;
  private boolean quiescenceRequested;
  private Thread delegatingThread;
  private int pendingCalls;
  private ClassLoader parent;
  private LaunchDesc _launchDesc = null;
  private AppPolicy _appPolicy;
  private AccessControlContext _acc = null;
  private boolean _initialized = false;
  private ArrayList _jarsInURLClassLoader = new ArrayList();
  private ArrayList _jarsNotInURLClassLoader = new ArrayList();
  private NativeLibraryBundle nativeLibraries = null;
  private boolean processingException = false;

  public JNLPPreverifyClassLoader(ClassLoader paramClassLoader)
  {
    super(new URL[0], paramClassLoader);
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkCreateClassLoader();
    this.parent = paramClassLoader;
  }

  public Preloader getPreloader()
  {
    return Progress.get(null);
  }

  public void setDelegatingClassLoader(ClassLoader paramClassLoader)
  {
    this._delegatingClassLoader = paramClassLoader;
  }

  public void initialize(LaunchDesc paramLaunchDesc, AppPolicy paramAppPolicy)
  {
    this._launchDesc = paramLaunchDesc;
    this._acc = AccessController.getContext();
    this._appPolicy = paramAppPolicy;
    this._initialized = false;
    ArrayList localArrayList = new ArrayList();
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc != null)
    {
      JNLPUtils.sortResourcesForClasspath(localResourcesDesc, this._jarsInURLClassLoader, this._jarsNotInURLClassLoader);
      for (i = 0; i < this._jarsInURLClassLoader.size(); i++)
      {
        JARDesc localJARDesc = (JARDesc)this._jarsInURLClassLoader.get(i);
        if (!Cache.isCacheEnabled())
          continue;
        CacheEntry localCacheEntry = Cache.getSystemCacheEntry(localJARDesc.getLocation(), localJARDesc.getVersion());
        if ((localCacheEntry != null) && (localCacheEntry.getClassesVerificationStatus() == 1) && (localCacheEntry.isKnownToBeSigned()))
        {
          localArrayList.add(localJARDesc.getLocation());
          Trace.println("JNLPPreverifyClassLoader.initialize: addURL: " + localJARDesc.getLocation(), TraceLevel.CACHE);
        }
        else
        {
          if (!Environment.allowAltJavaFxRuntimeURL())
          {
            this._initialized = false;
            Trace.println("JNLPPreverifyClassLoader.initialize: FAILED: " + localJARDesc.getLocation(), TraceLevel.CACHE);
            return;
          }
          Trace.println("JNLPPreverifyClassLoader.initialize: skip " + localJARDesc.getLocation(), TraceLevel.CACHE);
        }
      }
    }
    for (int i = 0; i < localArrayList.size(); i++)
      addURL((URL)localArrayList.get(i));
    _instance = this;
    this._initialized = true;
  }

  public boolean contains(URL paramURL)
  {
    if (!this._initialized)
      return false;
    String str = paramURL.toString();
    URL[] arrayOfURL = getURLs();
    for (int i = 0; i < arrayOfURL.length; i++)
      if (str.equals(arrayOfURL[i].toString()))
        return true;
    return false;
  }

  public JARDesc getJarDescFromURL(URL paramURL)
  {
    for (int i = 0; i < this._jarsInURLClassLoader.size(); i++)
    {
      JARDesc localJARDesc = (JARDesc)this._jarsInURLClassLoader.get(i);
      if (localJARDesc.getLocation().toString().equals(paramURL.toString()))
        return localJARDesc;
    }
    return null;
  }

  public static JNLPClassLoaderIf getInstance()
  {
    return _instance;
  }

  public LaunchDesc getLaunchDesc()
  {
    return this._launchDesc;
  }

  public int getDefaultSecurityModel()
  {
    return this._launchDesc.getSecurityModel();
  }

  public URL getResource(String paramString)
  {
    URL localURL = (URL)AccessController.doPrivileged(new PrivilegedAction(paramString)
    {
      private final String val$name;

      public Object run()
        throws SecurityException
      {
        URL localURL = null;
        for (int i = 0; (localURL == null) && (i < 3); i++)
          localURL = JNLPPreverifyClassLoader.this.getResource(this.val$name);
        return localURL;
      }
    });
    return localURL;
  }

  protected String findLibrary(String paramString)
  {
    DeployPerfUtil.put("JNLPPreverifyClassLoader.findLibrary - start()");
    if (!this._initialized)
    {
      Trace.println("JNLPPreverifyClassLoader.findLibrary: " + paramString + ": not initialized -> super()", TraceLevel.BASIC);
      return super.findLibrary(paramString);
    }
    paramString = Platform.get().getLibraryPrefix() + paramString + Platform.get().getLibrarySufix();
    Trace.println("JNLPPreverifyClassLoader.findLibrary: Looking up native library: " + paramString, TraceLevel.BASIC);
    synchronized (this)
    {
      if (this.nativeLibraries != null)
      {
        localObject1 = this.nativeLibraries.get(paramString);
        if (localObject1 != null)
        {
          Trace.println("JNLPPreverifyClassLoader.findLibrary: native library found: " + (String)localObject1, TraceLevel.BASIC);
          DeployPerfUtil.put("JNLPPreverifyClassLoader.findLibrary - reusing library");
          return localObject1;
        }
      }
      else
      {
        this.nativeLibraries = new NativeLibraryBundle();
      }
    }
    ??? = this._launchDesc.getResources();
    Object localObject1 = ((ResourcesDesc)???).getEagerOrAllJarDescs(true);
    for (int i = 0; i < localObject1.length; i++)
    {
      if (!localObject1[i].isNativeLib())
        continue;
      try
      {
        String str1 = DownloadEngine.getLibraryDirForJar(paramString, localObject1[i].getLocation(), localObject1[i].getVersion());
        if (str1 != null)
        {
          CacheEntry localCacheEntry = Cache.getSystemCacheEntry(localObject1[i].getLocation(), localObject1[i].getVersion());
          if (localCacheEntry != null)
          {
            JarFile localJarFile = localCacheEntry.getJarFile();
            this.nativeLibraries.prepareLibrary(paramString, localJarFile, str1);
            String str2 = this.nativeLibraries.get(paramString);
            Trace.println("JNLPPreverifyClassLoader.findLibrary: native library found: " + str2, TraceLevel.BASIC);
            DeployPerfUtil.put("JNLPPreverifyClassLoader.findLibrary - found library");
            return str2;
          }
        }
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
    Trace.println("Native library " + paramString + " not found", TraceLevel.BASIC);
    DeployPerfUtil.put("JNLPPreverifyClassLoader.findLibrary - return super.findLibrary");
    return (String)(String)super.findLibrary(paramString);
  }

  private static Method getDefineClassMethod(String paramString)
  {
    return (Method)AccessController.doPrivileged(new PrivilegedAction(paramString)
    {
      private final String val$name;

      public Object run()
      {
        try
        {
          Method localMethod = JNLPPreverifyClassLoader.class$java$net$URLClassLoader.getDeclaredMethod(this.val$name, new Class[] { String.class, Resource.class, Boolean.TYPE });
          localMethod.setAccessible(true);
          return localMethod;
        }
        catch (Exception localException)
        {
        }
        return null;
      }
    });
  }

  private static Field getUCPField(String paramString)
  {
    return (Field)AccessController.doPrivileged(new PrivilegedAction(paramString)
    {
      private final String val$name;

      public Object run()
      {
        try
        {
          Field localField = URLClassLoader.class.getDeclaredField(this.val$name);
          localField.setAccessible(true);
          return localField;
        }
        catch (Exception localException)
        {
        }
        return null;
      }
    });
  }

  private Class defineClassHelper(String paramString, Resource paramResource)
    throws IOException
  {
    try
    {
      String str = paramResource.getURL().toString();
      localObject1 = str.substring(4, str.indexOf('!'));
      URL localURL = new URL((String)localObject1);
      JARDesc localJARDesc = getJarDescFromURL(localURL);
      Boolean localBoolean = Boolean.TRUE;
      if (Cache.isCacheEnabled())
      {
        localObject2 = Cache.getSystemCacheEntry(localJARDesc.getLocation(), localJARDesc.getVersion());
        if ((localObject2 != null) && (((CacheEntry)localObject2).getClassesVerificationStatus() == 1))
          localBoolean = Boolean.FALSE;
      }
      Object localObject2 = (Class)defineClassMethod.invoke(this, new Object[] { paramString, paramResource, localBoolean });
      return localObject2;
    }
    catch (Exception localException)
    {
      for (Object localObject1 = localException.getCause(); localObject1 != null; localObject1 = ((Throwable)localObject1).getCause())
      {
        if ((localObject1 instanceof LinkageError))
          throw ((LinkageError)localObject1);
        if ((localObject1 instanceof IOException))
          throw ((IOException)localObject1);
        if (!(localObject1 instanceof SecurityException))
          continue;
        throw ((SecurityException)localObject1);
      }
    }
    throw new RuntimeException(localException);
  }

  private Class findClassHelper(String paramString)
    throws ClassNotFoundException
  {
    if ((ucpField == null) || (defineClassMethod == null))
      return super.findClass(paramString);
    try
    {
      return (Class)AccessController.doPrivileged(new PrivilegedExceptionAction(paramString)
      {
        private final String val$name;

        public Object run()
          throws ClassNotFoundException
        {
          String str = this.val$name.replace('.', '/').concat(".class");
          URLClassPath localURLClassPath;
          try
          {
            localURLClassPath = (URLClassPath)JNLPPreverifyClassLoader.ucpField.get(JNLPPreverifyClassLoader.this);
          }
          catch (Exception localException)
          {
            throw new ClassNotFoundException(this.val$name, localException);
          }
          Resource localResource = localURLClassPath.getResource(str, false);
          if (localResource != null)
            try
            {
              return JNLPPreverifyClassLoader.this.defineClassHelper(this.val$name, new JNLPPreverifyClassLoader.PreverifiedResource(JNLPPreverifyClassLoader.this, localResource));
            }
            catch (IOException localIOException)
            {
              throw new ClassNotFoundException(this.val$name, localIOException);
            }
          throw new ClassNotFoundException(this.val$name);
        }
      }
      , this._acc);
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
    }
    throw ((ClassNotFoundException)localPrivilegedActionException.getException());
  }

  protected Class findClass(String paramString)
    throws ClassNotFoundException
  {
    return findClass(paramString, false);
  }

  protected Class findClass(String paramString, boolean paramBoolean)
    throws ClassNotFoundException
  {
    if (!this._initialized)
      return super.findClass(paramString);
    try
    {
      return findClassHelper(paramString);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      synchronized (this)
      {
        if ((!paramBoolean) && (!this.processingException) && (this._delegatingClassLoader != null) && (needToApplyWorkaround()))
        {
          this.processingException = true;
          try
          {
            int i = 0;
            Object localObject1 = new DelegatingThread(this._delegatingClassLoader, this);
            ((DelegatingThread)localObject1).start();
            while (!((DelegatingThread)localObject1).done())
              try
              {
                wait();
              }
              catch (InterruptedException localInterruptedException1)
              {
                i = 1;
              }
            if (i == 0)
            {
              Class localClass = this._delegatingClassLoader.loadClass(paramString);
              int j = 0;
              UndelegatingThread localUndelegatingThread1 = new UndelegatingThread(this._delegatingClassLoader, this);
              localUndelegatingThread1.start();
              while (!localUndelegatingThread1.done())
                try
                {
                  wait();
                }
                catch (InterruptedException localInterruptedException3)
                {
                  j = 1;
                }
              if (j == 0)
                this.processingException = false;
              return localClass;
            }
            i = 0;
            localObject1 = new UndelegatingThread(this._delegatingClassLoader, this);
            ((UndelegatingThread)localObject1).start();
            while (!((UndelegatingThread)localObject1).done())
              try
              {
                wait();
              }
              catch (InterruptedException localInterruptedException2)
              {
                i = 1;
              }
            if (i == 0)
              this.processingException = false;
          }
          finally
          {
            int k = 0;
            UndelegatingThread localUndelegatingThread2 = new UndelegatingThread(this._delegatingClassLoader, this);
            localUndelegatingThread2.start();
            while (!localUndelegatingThread2.done())
              try
              {
                wait();
              }
              catch (InterruptedException localInterruptedException4)
              {
                k = 1;
              }
            if (k == 0)
              this.processingException = false;
          }
        }
      }
    }
    throw localClassNotFoundException;
  }

  protected synchronized Class loadClass(String paramString, boolean paramBoolean)
    throws ClassNotFoundException
  {
    return loadClass(paramString, paramBoolean, false);
  }

  public synchronized Class loadClass(String paramString, boolean paramBoolean1, boolean paramBoolean2)
    throws ClassNotFoundException
  {
    while ((this.quiescenceRequested) && (this.pendingCalls == 0) && (!Thread.currentThread().equals(this.delegatingThread)))
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException)
      {
        throw new ClassNotFoundException("Quiescence interrupted");
      }
    try
    {
      this.pendingCalls += 1;
      Class localClass = loadClass0(paramString, paramBoolean1, paramBoolean2);
      return localClass;
    }
    finally
    {
      this.pendingCalls -= 1;
    }
    throw localObject;
  }

  public synchronized void quiescenceRequested(Thread paramThread, boolean paramBoolean)
  {
    if (paramBoolean)
      this.pendingCalls -= 1;
    this.delegatingThread = paramThread;
    this.quiescenceRequested = true;
  }

  public synchronized void quiescenceCancelled(boolean paramBoolean)
  {
    if (this.quiescenceRequested)
    {
      if (paramBoolean)
        this.pendingCalls += 1;
      this.delegatingThread = null;
      this.quiescenceRequested = false;
      if (!paramBoolean)
        notifyAll();
    }
  }

  private Class loadClass0(String paramString, boolean paramBoolean1, boolean paramBoolean2)
    throws ClassNotFoundException
  {
    Class localClass = findLoadedClass(paramString);
    if ((localClass == null) && (this.parent != null))
      try
      {
        localClass = this.parent.loadClass(paramString);
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
      }
    if (localClass == null)
      localClass = findClass(paramString, paramBoolean2);
    if (paramBoolean1)
      resolveClass(localClass);
    return localClass;
  }

  protected boolean needToApplyWorkaround()
  {
    StackTraceElement[] arrayOfStackTraceElement = null;
    try
    {
      arrayOfStackTraceElement = new Throwable().getStackTrace();
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
    }
    catch (NoClassDefFoundError localNoClassDefFoundError)
    {
    }
    if (arrayOfStackTraceElement == null)
      return false;
    String str;
    for (int i = 0; i < arrayOfStackTraceElement.length; i++)
    {
      str = arrayOfStackTraceElement[i].getClassName();
      if ((str.equals(JNLPPreverifyClassLoader.class.getName())) || (str.equals(Class.class.getName())))
        continue;
      if (!str.equals(ClassLoader.class.getName()))
        break;
    }
    if ((i > 0) && (i < arrayOfStackTraceElement.length) && (arrayOfStackTraceElement[(i - 1)].getClassName().equals(Class.class.getName())) && (arrayOfStackTraceElement[(i - 1)].getMethodName().equals("forName")))
    {
      str = arrayOfStackTraceElement[i].getClassName();
      if (str.equals("com.sun.javafx.runtime.adapter.AppletStartupRoutine"))
        return true;
    }
    return false;
  }

  public URL findResource(String paramString)
  {
    return super.findResource(paramString);
  }

  protected PermissionCollection getPermissions(CodeSource paramCodeSource)
  {
    PermissionCollection localPermissionCollection = super.getPermissions(paramCodeSource);
    try
    {
      this._appPolicy.addPermissions(getInstance(), localPermissionCollection, paramCodeSource, true);
    }
    catch (ExitException localExitException)
    {
      Trace.println("_appPolicy.addPermissions: " + localExitException, TraceLevel.BASIC);
      Trace.ignoredException(localExitException);
    }
    URL localURL = paramCodeSource.getLocation();
    JARDesc localJARDesc = getJarDescFromURL(localURL);
    if (localJARDesc != null)
    {
      CacheEntry localCacheEntry = Cache.getSystemCacheEntry(localJARDesc.getLocation(), localJARDesc.getVersion());
      if (localCacheEntry != null)
      {
        File localFile = localCacheEntry.getDataFile();
        if (localFile != null)
        {
          String str = localFile.getPath();
          localPermissionCollection.add(new FilePermission(str, "read"));
        }
      }
    }
    if (!localPermissionCollection.implies(new AWTPermission("accessClipboard")))
      ToolkitStore.get().getAppContext().put("UNTRUSTED_URLClassLoader", Boolean.TRUE);
    return localPermissionCollection;
  }

  public JarFile getJarFile(URL paramURL)
    throws IOException
  {
    JARDesc localJARDesc = getJarDescFromURL(paramURL);
    JarFile localJarFile = null;
    if (localJARDesc != null)
    {
      int i = LaunchDownload.getDownloadType(localJARDesc);
      localJarFile = (JarFile)AccessController.doPrivileged(new PrivilegedAction(localJARDesc, i)
      {
        private final JARDesc val$jd;
        private final int val$contentType;

        public Object run()
          throws SecurityException
        {
          try
          {
            CacheEntry localCacheEntry = Cache.getSystemCacheEntry(this.val$jd.getLocation(), this.val$jd.getVersion());
            if (localCacheEntry == null)
              return null;
            JarFile localJarFile = localCacheEntry.getJarFile();
            if (localJarFile != null)
              return localJarFile;
            return DownloadEngine.getUpdatedJarFile(this.val$jd.getLocation(), this.val$jd.getVersion(), this.val$contentType);
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
          return null;
        }
      });
      if (localJarFile == null)
        throw new IOException("Resource not found: " + localJARDesc.getLocation() + ":" + localJARDesc.getVersion());
      return localJarFile;
    }
    return null;
  }

  private void addLoadedJarsEntry(JARDesc paramJARDesc)
  {
    if ((!this._jarsInURLClassLoader.contains(paramJARDesc)) && (!paramJARDesc.isNativeLib()) && ((Environment.allowAltJavaFxRuntimeURL()) || (paramJARDesc.getLocation().getHost().equals("dl.javafx.com"))))
      this._jarsInURLClassLoader.add(paramJARDesc);
  }

  public void addResource(URL paramURL, String paramString1, String paramString2)
  {
  }

  public BasicService getBasicService()
  {
    return BasicServiceImpl.getInstance();
  }

  public FileOpenService getFileOpenService()
  {
    return FileOpenServiceImpl.getInstance();
  }

  public FileSaveService getFileSaveService()
  {
    return FileSaveServiceImpl.getInstance();
  }

  public ExtensionInstallerService getExtensionInstallerService()
  {
    return ExtensionInstallerServiceImpl.getInstance();
  }

  public DownloadService getDownloadService()
  {
    return DownloadServiceImpl.getInstance();
  }

  public ClipboardService getClipboardService()
  {
    return ClipboardServiceImpl.getInstance();
  }

  public PrintService getPrintService()
  {
    return PrintServiceImpl.getInstance();
  }

  public PersistenceService getPersistenceService()
  {
    return PersistenceServiceImpl.getInstance();
  }

  public ExtendedService getExtendedService()
  {
    return ExtendedServiceImpl.getInstance();
  }

  public SingleInstanceService getSingleInstanceService()
  {
    return SingleInstanceServiceImpl.getInstance();
  }

  public IntegrationService getIntegrationService()
  {
    return new IntegrationServiceImpl(this);
  }

  public DownloadService2 getDownloadService2()
  {
    return DownloadService2Impl.getInstance();
  }

  public static class DelegatingThread extends Thread
  {
    protected ClassLoader cl;
    protected final ClassLoader waiter;
    protected Thread thread;

    public DelegatingThread(ClassLoader paramClassLoader1, ClassLoader paramClassLoader2)
    {
      super();
      setDaemon(true);
      this.cl = paramClassLoader1;
      this.waiter = paramClassLoader2;
      this.thread = Thread.currentThread();
    }

    public void run()
    {
      if ((isAncestor(this.cl, this.waiter)) || (this.cl.equals(this.waiter)))
        quiesce(this.cl, this.waiter);
      synchronized (this.waiter)
      {
        this.thread = null;
        this.waiter.notifyAll();
      }
    }

    public boolean done()
    {
      synchronized (this.waiter)
      {
        return this.thread == null;
      }
    }

    protected boolean isAncestor(ClassLoader paramClassLoader1, ClassLoader paramClassLoader2)
    {
      do
      {
        paramClassLoader1 = paramClassLoader1.getParent();
        if (paramClassLoader1 == paramClassLoader2)
          return true;
      }
      while (paramClassLoader1 != null);
      return false;
    }

    protected void quiesce(ClassLoader paramClassLoader1, ClassLoader paramClassLoader2)
    {
      boolean bool = this.waiter.equals(paramClassLoader1);
      if ((paramClassLoader1 instanceof JNLPClassLoaderIf))
        ((JNLPClassLoaderIf)paramClassLoader1).quiescenceRequested(this.thread, bool);
      if (bool)
        return;
      quiesce(paramClassLoader1.getParent(), paramClassLoader2);
    }
  }

  private class PreverifiedResource extends Resource
  {
    private Resource res = null;
    private byte[] cbytes;

    public PreverifiedResource(Resource arg2)
    {
      Object localObject;
      this.res = localObject;
    }

    public String getName()
    {
      return this.res.getName();
    }

    public URL getURL()
    {
      return this.res.getURL();
    }

    public URL getCodeSourceURL()
    {
      return this.res.getCodeSourceURL();
    }

    public InputStream getInputStream()
      throws IOException
    {
      return this.res.getInputStream();
    }

    public int getContentLength()
      throws IOException
    {
      return this.res.getContentLength();
    }

    public byte[] getBytes()
      throws IOException
    {
      if (this.cbytes != null)
        return this.cbytes;
      return this.cbytes = super.getBytes();
    }

    public ByteBuffer getByteBuffer()
      throws IOException
    {
      return this.res.getByteBuffer();
    }

    public Manifest getManifest()
      throws IOException
    {
      return this.res.getManifest();
    }

    public Certificate[] getCertificates()
    {
      return null;
    }

    public CodeSigner[] getCodeSigners()
    {
      return null;
    }
  }

  public static class UndelegatingThread extends JNLPPreverifyClassLoader.DelegatingThread
  {
    public UndelegatingThread(ClassLoader paramClassLoader1, ClassLoader paramClassLoader2)
    {
      super(paramClassLoader2);
    }

    public void run()
    {
      if ((isAncestor(this.cl, this.waiter)) || (this.cl.equals(this.waiter)))
        unquiesce(this.cl, this.waiter);
      synchronized (this.waiter)
      {
        this.thread = null;
        this.waiter.notifyAll();
      }
    }

    public boolean done()
    {
      synchronized (this.waiter)
      {
        return this.thread == null;
      }
    }

    protected void unquiesce(ClassLoader paramClassLoader1, ClassLoader paramClassLoader2)
    {
      boolean bool = (paramClassLoader2 != null) && (paramClassLoader2.equals(paramClassLoader1));
      if (!bool)
        unquiesce(paramClassLoader1.getParent(), paramClassLoader2);
      if ((paramClassLoader1 instanceof JNLPClassLoaderIf))
        ((JNLPClassLoaderIf)paramClassLoader1).quiescenceCancelled(bool);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.JNLPPreverifyClassLoader
 * JD-Core Version:    0.6.0
 */