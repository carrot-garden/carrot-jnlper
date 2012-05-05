package com.sun.jnlp;

import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.JfxRuntime;
import com.sun.deploy.config.Platform;
import com.sun.deploy.net.DeployClassLoader;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.JARSigningException;
import com.sun.deploy.security.CPCallbackClassLoaderIf;
import com.sun.deploy.security.CPCallbackHandler;
import com.sun.deploy.security.DeployURLClassPath;
import com.sun.deploy.security.SecureCookiePermission;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.LaunchDownload;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.jnl.ResourcesDesc.PackageInformation;
import com.sun.javaws.progress.Progress;
import com.sun.javaws.security.AppPolicy;
import com.sun.javaws.util.JNLPUtils;
import com.sun.javaws.util.JfxHelper;
import java.awt.AWTPermission;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
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
import sun.misc.URLClassPath;

public final class JNLPClassLoader extends URLClassLoader
  implements JNLPClassLoaderIf, CPCallbackClassLoaderIf
{
  private static JNLPClassLoader _instance = null;
  private static JNLPPreverifyClassLoader _preverifyCL = null;
  private LaunchDesc _launchDesc = null;
  private AppPolicy _appPolicy;
  private AccessControlContext _acc = null;
  private boolean _initialized = false;
  private Map _jarsInURLClassLoader = new HashMap();
  private ArrayList _jarsNotInURLClassLoader = new ArrayList();
  private static Field ucpField = getUCPField("ucp");
  private List addedURLs = new ArrayList();
  private JNLPClassLoader _jclParent;

  private JNLPClassLoader(ClassLoader paramClassLoader)
  {
    super(new URL[0], paramClassLoader);
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkCreateClassLoader();
    if ((paramClassLoader instanceof JNLPClassLoader))
      this._jclParent = ((JNLPClassLoader)paramClassLoader);
    setUCP(this, new DeployURLClassPath(new URL[0]));
  }

  private void initialize(LaunchDesc paramLaunchDesc, AppPolicy paramAppPolicy)
  {
    this._launchDesc = paramLaunchDesc;
    this._acc = AccessController.getContext();
    this._appPolicy = paramAppPolicy;
    this._initialized = true;
    if (this._jclParent != null)
    {
      this._jclParent.initialize(paramLaunchDesc, paramAppPolicy);
      drainPendingURLs();
      return;
    }
    if (_preverifyCL != null)
      _preverifyCL.initialize(paramLaunchDesc, paramAppPolicy);
    if (paramLaunchDesc.needFX())
    {
      localObject1 = null;
      for (localObject2 = getParent(); (localObject1 == null) && (localObject2 != null); localObject2 = ((ClassLoader)localObject2).getParent())
      {
        if (!(localObject2 instanceof DeployClassLoader))
          continue;
        localObject1 = (DeployClassLoader)localObject2;
      }
      if (localObject1 != null)
      {
        JfxRuntime localJfxRuntime = JfxHelper.getBestJfxInstalled(paramLaunchDesc);
        try
        {
          ((DeployClassLoader)localObject1).injectJfx(localJfxRuntime);
        }
        catch (ClassNotFoundException localClassNotFoundException)
        {
          Trace.ignored(localClassNotFoundException);
        }
        catch (IllegalStateException localIllegalStateException)
        {
          Trace.ignored(localIllegalStateException);
        }
      }
    }
    Object localObject1 = paramLaunchDesc.getResources();
    Object localObject2 = new ArrayList();
    if (localObject1 != null)
    {
      JNLPUtils.sortResourcesForClasspath((ResourcesDesc)localObject1, (List)localObject2, this._jarsNotInURLClassLoader);
      for (int i = 0; i < ((ArrayList)localObject2).size(); i++)
      {
        JARDesc localJARDesc = (JARDesc)((ArrayList)localObject2).get(i);
        this._jarsInURLClassLoader.put(URLUtil.toNormalizedString(localJARDesc.getLocation()), localJARDesc);
        if ((_preverifyCL != null) && (_preverifyCL.contains(localJARDesc.getLocation())))
          continue;
        addURL2(localJARDesc.getLocation());
      }
    }
  }

  public JNLPPreverifyClassLoader getJNLPPreverifyClassLoader()
  {
    return _preverifyCL;
  }

  public Preloader getPreloader()
  {
    return Progress.get(null);
  }

  public static JNLPClassLoader createClassLoader()
  {
    if (_instance == null)
    {
      ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
      _preverifyCL = new JNLPPreverifyClassLoader(localClassLoader);
      JNLPClassLoader localJNLPClassLoader1 = new JNLPClassLoader(_preverifyCL);
      if (Config.getMixcodeValue() != 3)
      {
        JNLPClassLoader localJNLPClassLoader2 = new JNLPClassLoader(localJNLPClassLoader1);
        if (!setDeployURLClassPathCallbacks(localJNLPClassLoader1, localJNLPClassLoader2))
          _instance = localJNLPClassLoader1;
        else
          _instance = localJNLPClassLoader2;
      }
      else
      {
        _instance = localJNLPClassLoader1;
      }
    }
    return _instance;
  }

  public static JNLPClassLoader createClassLoader(LaunchDesc paramLaunchDesc, AppPolicy paramAppPolicy)
  {
    JNLPClassLoader localJNLPClassLoader = createClassLoader();
    if (!localJNLPClassLoader._initialized)
      localJNLPClassLoader.initialize(paramLaunchDesc, paramAppPolicy);
    return localJNLPClassLoader;
  }

  public static JNLPClassLoaderIf getInstance()
  {
    return _instance;
  }

  public LaunchDesc getLaunchDesc()
  {
    return this._launchDesc;
  }

  public JARDesc getJarDescFromURL(URL paramURL)
  {
    if (this._jclParent != null)
      return this._jclParent.getJarDescFromURL(paramURL);
    String str1 = URLUtil.toNormalizedString(paramURL);
    JARDesc localJARDesc = (JARDesc)this._jarsInURLClassLoader.get(str1);
    if (localJARDesc != null)
      return localJARDesc;
    HashMap localHashMap = new HashMap();
    Iterator localIterator = this._jarsInURLClassLoader.keySet().iterator();
    while (localIterator.hasNext())
    {
      String str2 = (String)localIterator.next();
      localJARDesc = (JARDesc)this._jarsInURLClassLoader.get(str2);
      String str3 = URLUtil.toNormalizedString(DownloadEngine.getKnownRedirectFinalURL(localJARDesc.getLocation()));
      if (!this._jarsInURLClassLoader.containsKey(str3))
      {
        localHashMap.put(str3, localJARDesc);
        if (str1.equals(str3))
        {
          this._jarsInURLClassLoader.putAll(localHashMap);
          return localJARDesc;
        }
      }
    }
    this._jarsInURLClassLoader.putAll(localHashMap);
    return null;
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
          localURL = JNLPClassLoader.this.getResource(this.val$name);
        return localURL;
      }
    });
    return localURL;
  }

  protected String findLibrary(String paramString)
  {
    if (this._jclParent != null)
      return this._jclParent.findLibrary(paramString);
    if (!this._initialized)
      return super.findLibrary(paramString);
    paramString = Platform.get().getLibraryPrefix() + paramString + Platform.get().getLibrarySufix();
    Trace.println("Looking up native library: " + paramString, TraceLevel.NETWORK);
    ResourcesDesc localResourcesDesc = this._launchDesc.getResources();
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(true);
    for (int i = 0; i < arrayOfJARDesc.length; i++)
    {
      if (!arrayOfJARDesc[i].isNativeLib())
        continue;
      try
      {
        String str = DownloadEngine.getLibraryDirForJar(paramString, arrayOfJARDesc[i].getLocation(), arrayOfJARDesc[i].getVersion());
        if (str != null)
          return new File(str, paramString).getAbsolutePath();
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
    Trace.println("Native library " + paramString + " not found", TraceLevel.NETWORK);
    return super.findLibrary(paramString);
  }

  protected Class findClass(String paramString)
    throws ClassNotFoundException
  {
    if (!this._initialized)
      return super.findClass(paramString);
    try
    {
      return super.findClass(paramString);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      if ((!(localClassNotFoundException.getCause() instanceof JARSigningException)) && (checkPackageParts(paramString)))
        return super.findClass(paramString);
    }
    throw localClassNotFoundException;
  }

  public void quiescenceRequested(Thread paramThread, boolean paramBoolean)
  {
  }

  public void quiescenceCancelled(boolean paramBoolean)
  {
  }

  public URL findResource(String paramString)
  {
    URL localURL = super.findResource(paramString);
    if (!this._initialized)
      return localURL;
    if ((localURL == null) && (checkPackageParts(paramString)))
      localURL = super.findResource(paramString);
    return localURL;
  }

  private boolean checkPackageParts(String paramString)
  {
    if (this._jclParent != null)
      return drainPendingURLs();
    int i = 0;
    ResourcesDesc.PackageInformation localPackageInformation = this._launchDesc.getResources().getPackageInformation(paramString);
    if (localPackageInformation != null)
    {
      JARDesc[] arrayOfJARDesc = localPackageInformation.getLaunchDesc().getResources().getPart(localPackageInformation.getPart());
      for (int j = 0; j < arrayOfJARDesc.length; j++)
      {
        if (!this._jarsNotInURLClassLoader.contains(arrayOfJARDesc[j]))
          continue;
        this._jarsNotInURLClassLoader.remove(arrayOfJARDesc[j]);
        addLoadedJarsEntry(arrayOfJARDesc[j]);
        addURL2(arrayOfJARDesc[j].getLocation());
        i = 1;
      }
    }
    return i;
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
      try
      {
        File localFile = DownloadEngine.getCachedFile(localJARDesc.getLocation(), localJARDesc.getVersion());
        if (localFile != null)
        {
          String str = localFile.getPath();
          localPermissionCollection.add(new FilePermission(str, "read"));
        }
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    if (!localPermissionCollection.implies(new AWTPermission("accessClipboard")))
      ToolkitStore.get().getAppContext().put("UNTRUSTED_URLClassLoader", Boolean.TRUE);
    localPermissionCollection.add(new SecureCookiePermission(SecureCookiePermission.getURLOriginString(paramCodeSource.getLocation())));
    return localPermissionCollection;
  }

  public JarFile getJarFile(URL paramURL)
    throws IOException
  {
    JARDesc localJARDesc = getJarDescFromURL(paramURL);
    if (localJARDesc == null)
      return null;
    int i = LaunchDownload.getDownloadType(localJARDesc);
    try
    {
      return (JarFile)AccessController.doPrivileged(new PrivilegedExceptionAction(localJARDesc, i)
      {
        private final JARDesc val$jd;
        private final int val$contentType;

        public Object run()
          throws IOException
        {
          JarFile localJarFile = DownloadEngine.getCachedJarFile(this.val$jd.getLocation(), this.val$jd.getVersion());
          if (localJarFile != null)
            return localJarFile;
          return DownloadEngine.getUpdatedJarFile(this.val$jd.getLocation(), this.val$jd.getVersion(), this.val$contentType);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
    }
    throw ((IOException)localPrivilegedActionException.getException());
  }

  private void addLoadedJarsEntry(JARDesc paramJARDesc)
  {
    String str = paramJARDesc.getLocationString();
    if (!this._jarsInURLClassLoader.containsKey(str))
      this._jarsInURLClassLoader.put(str, paramJARDesc);
  }

  public void addResource(URL paramURL, String paramString1, String paramString2)
  {
    if (this._jclParent != null)
    {
      this._jclParent.addResource(paramURL, paramString1, paramString2);
      drainPendingURLs();
      return;
    }
    JARDesc localJARDesc = new JARDesc(paramURL, paramString1, true, false, false, null, 0, null);
    String str = localJARDesc.getLocationString();
    if (!this._jarsInURLClassLoader.containsKey(str))
    {
      this._launchDesc.getResources().addResource(localJARDesc);
      addLoadedJarsEntry(localJARDesc);
      addURL2(paramURL);
    }
  }

  static boolean setDeployURLClassPathCallbacks(JNLPClassLoader paramJNLPClassLoader1, JNLPClassLoader paramJNLPClassLoader2)
  {
    try
    {
      if (!CacheEntry.hasEnhancedJarAccess())
      {
        Trace.println("setDeployURLClassPathCallbacks: no enhanced access", TraceLevel.BASIC);
        return false;
      }
      CPCallbackHandler localCPCallbackHandler = new CPCallbackHandler(paramJNLPClassLoader1, paramJNLPClassLoader2);
      getDUCP(paramJNLPClassLoader1).setDeployURLClassPathCallback(localCPCallbackHandler.getParentCallback());
      getDUCP(paramJNLPClassLoader2).setDeployURLClassPathCallback(localCPCallbackHandler.getChildCallback());
    }
    catch (ThreadDeath localThreadDeath)
    {
      throw localThreadDeath;
    }
    catch (Exception localException)
    {
      return false;
    }
    catch (Error localError)
    {
      return false;
    }
    return true;
  }

  private static DeployURLClassPath getDUCP(JNLPClassLoader paramJNLPClassLoader)
  {
    return (DeployURLClassPath)getUCP(paramJNLPClassLoader);
  }

  private static URLClassPath getUCP(JNLPClassLoader paramJNLPClassLoader)
  {
    URLClassPath localURLClassPath = null;
    try
    {
      localURLClassPath = (URLClassPath)ucpField.get(paramJNLPClassLoader);
    }
    catch (Exception localException)
    {
    }
    return localURLClassPath;
  }

  private static void setUCP(JNLPClassLoader paramJNLPClassLoader, URLClassPath paramURLClassPath)
  {
    try
    {
      ucpField.set(paramJNLPClassLoader, paramURLClassPath);
    }
    catch (Exception localException)
    {
    }
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

  protected void addURL(URL paramURL)
  {
    if (this._jclParent != null)
      this._jclParent.addURL(paramURL);
    super.addURL(paramURL);
  }

  void addURL2(URL paramURL)
  {
    if (this._jclParent != null)
      drainPendingURLs();
    else
      putAddedURL(paramURL);
    super.addURL(paramURL);
  }

  boolean drainPendingURLs()
  {
    List localList = this._jclParent.grabAddedURLs();
    for (int i = 0; i < localList.size(); i++)
      super.addURL((URL)localList.get(i));
    return i != 0;
  }

  synchronized List grabAddedURLs()
  {
    List localList = this.addedURLs;
    this.addedURLs = new ArrayList();
    return localList;
  }

  synchronized void putAddedURL(URL paramURL)
  {
    this.addedURLs.add(paramURL);
  }

  public CodeSource[] getTrustedCodeSources(CodeSource[] paramArrayOfCodeSource)
  {
    ArrayList localArrayList = new ArrayList();
    Policy localPolicy = (Policy)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return Policy.getPolicy();
      }
    });
    for (int i = 0; i < paramArrayOfCodeSource.length; i++)
    {
      CodeSource localCodeSource = paramArrayOfCodeSource[i];
      boolean bool = false;
      PermissionCollection localPermissionCollection = localPolicy.getPermissions(localCodeSource);
      try
      {
        bool = this._appPolicy.addPermissions(getInstance(), localPermissionCollection, localCodeSource, true);
      }
      catch (ExitException localExitException)
      {
        Trace.println("_appPolicy.addPermissions: " + localExitException, TraceLevel.BASIC);
        Trace.ignoredException(localExitException);
      }
      if (!bool)
        bool = localPermissionCollection.implies(new AllPermission());
      if (!bool)
        bool = isTrustedByPolicy(localPolicy, localCodeSource);
      if (!bool)
        continue;
      localArrayList.add(localCodeSource);
    }
    return (CodeSource[])(CodeSource[])localArrayList.toArray(new CodeSource[localArrayList.size()]);
  }

  private boolean isTrustedByPolicy(Policy paramPolicy, CodeSource paramCodeSource)
  {
    PermissionCollection localPermissionCollection1 = paramPolicy.getPermissions(paramCodeSource);
    PermissionCollection localPermissionCollection2 = paramPolicy.getPermissions(new CodeSource(null, (Certificate[])null));
    Enumeration localEnumeration = localPermissionCollection1.elements();
    while (localEnumeration.hasMoreElements())
    {
      Permission localPermission = (Permission)localEnumeration.nextElement();
      if (!localPermissionCollection2.implies(localPermission))
        return true;
    }
    return false;
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
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.JNLPClassLoader
 * JD-Core Version:    0.6.0
 */