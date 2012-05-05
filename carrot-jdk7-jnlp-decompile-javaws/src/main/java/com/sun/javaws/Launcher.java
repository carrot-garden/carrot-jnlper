package com.sun.javaws;

import com.sun.applet2.preloader.CancelException;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.AppInitEvent;
import com.sun.applet2.preloader.event.ConfigEvent;
import com.sun.applet2.preloader.event.DownloadEvent;
import com.sun.applet2.preloader.event.InitEvent;
import com.sun.deploy.Environment;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.AutoUpdater;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.config.JfxRuntime;
import com.sun.deploy.config.Platform;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.DownloadException;
import com.sun.deploy.net.FailedDownloadException;
import com.sun.deploy.net.offline.DeployOfflineManager;
import com.sun.deploy.pings.Pings;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.si.SingleInstanceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.deploy.uitoolkit.ui.ComponentRef;
import com.sun.deploy.util.ArrayUtil;
import com.sun.deploy.util.DeploymentHooks;
import com.sun.deploy.util.JVMParameters;
import com.sun.deploy.util.PerfLogger;
import com.sun.deploy.util.SecurityBaseline;
import com.sun.deploy.util.SystemUtils;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.exceptions.JreExecException;
import com.sun.javaws.exceptions.LaunchDescException;
import com.sun.javaws.exceptions.MissingFieldException;
import com.sun.javaws.exceptions.NoLocalJREException;
import com.sun.javaws.exceptions.OfflineLaunchException;
import com.sun.javaws.jnl.AppletDesc;
import com.sun.javaws.jnl.ApplicationDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.LDUpdater;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.MatchJREIf;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.jnl.SecureMatchJRE;
import com.sun.javaws.jnl.UpdateDesc;
import com.sun.javaws.progress.PreloaderDelegate;
import com.sun.javaws.progress.Progress;
import com.sun.javaws.security.AppPolicy;
import com.sun.javaws.security.JNLPSignedResourcesHelper;
import com.sun.javaws.security.JavaWebStartSecurity;
import com.sun.javaws.ui.LaunchErrorDialog;
import com.sun.javaws.ui.SecureStaticVersioning;
import com.sun.javaws.ui.SplashScreen;
import com.sun.javaws.util.JavawsConsoleController;
import com.sun.javaws.util.JfxHelper;
import com.sun.jnlp.AppletContainer;
import com.sun.jnlp.AppletContainerCallback;
import com.sun.jnlp.BasicServiceImpl;
import com.sun.jnlp.ExtensionInstallerServiceImpl;
import com.sun.jnlp.JNLPClassLoader;
import com.sun.jnlp.PreverificationClassLoader;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import sun.awt.AppContext;

public class Launcher
  implements Runnable
{
  private LaunchDesc _initialLaunchDesc;
  protected LaunchDesc _launchDesc;
  protected String[] _args;
  private boolean _exit = true;
  private JAuthenticator _ja;
  private LocalApplicationProperties _lap = null;
  private JNLPClassLoader _jnlpClassLoader = null;
  private JREInfo _jreInfo = null;
  private boolean _isRelaunch = false;
  private boolean _isCached = false;
  private boolean _jreInstalled = false;
  boolean securityManagerDiabledForTests = false;
  private boolean _shownDownloadWindow = false;

  public Launcher(LaunchDesc paramLaunchDesc)
  {
    this._initialLaunchDesc = paramLaunchDesc;
    Trace.println("new Launcher: " + paramLaunchDesc.toString(), TraceLevel.BASIC);
  }

  public void launch(String[] paramArrayOfString, boolean paramBoolean)
  {
    this._args = paramArrayOfString;
    this._exit = paramBoolean;
    if ((!Environment.isImportMode()) && (SingleInstanceManager.isServerRunning(this._initialLaunchDesc.getCanonicalHome().toString())))
    {
      String[] arrayOfString = Globals.getApplicationArgs();
      if ((arrayOfString != null) && (this._initialLaunchDesc.getApplicationDescriptor() != null))
        this._initialLaunchDesc.getApplicationDescriptor().setArguments(arrayOfString);
      if (SingleInstanceManager.connectToServer(this._initialLaunchDesc.toString()))
      {
        Trace.println("Exiting (launched in the other instance)", TraceLevel.BASIC);
        try
        {
          ToolkitStore.get().dispose();
        }
        catch (Exception localException)
        {
          Trace.ignoredException(localException);
        }
        return;
      }
    }
    if (!Environment.isImportMode())
      SecurityBaseline.checkUpdate();
    if (prepareToLaunch())
    {
      Platform.get().getAutoUpdater().checkForUpdate(null);
      if (useAppletLifecycle())
        return;
      new Thread(Main.getLaunchThreadGroup(), this, "javawsApplicationMain").start();
    }
    else if (!Environment.isImportMode())
    {
      LaunchErrorDialog.show(Progress.get(null).getOwner(), new Exception(ResourceManager.getString("launch.error.category.unexpected")), true);
    }
  }

  public void run()
  {
    try
    {
      doLaunchApp();
    }
    catch (Throwable localThrowable)
    {
      ExitException localExitException1 = (localThrowable instanceof ExitException) ? (ExitException)localThrowable : new ExitException(localThrowable, 3);
      int i = localExitException1.getReason() == 0 ? 0 : -1;
      if ((i != 0) && (this._launchDesc != null) && (this._launchDesc.isApplication()))
      {
        URL localURL = this._launchDesc.getCanonicalHome();
        SecureStaticVersioning.resetAcceptedVersion(localURL);
      }
      if (localExitException1.getReason() == 3)
      {
        if ((this._launchDesc != null) && (this._launchDesc.getUpdater().isBackgroundUpdateRunning()) && (this._lap != null))
        {
          this._lap.setForceUpdateCheck(true);
          try
          {
            this._lap.store();
          }
          catch (Exception localException)
          {
          }
        }
        LaunchErrorDialog.show(Progress.get(null).getOwner(), localExitException1.getException(), this._exit);
      }
      if (this._exit)
        try
        {
          Main.systemExit(i);
        }
        catch (ExitException localExitException2)
        {
          Trace.println("systemExit: " + localExitException2, TraceLevel.BASIC);
          Trace.ignoredException(localExitException2);
        }
    }
  }

  private boolean isImport()
  {
    return (Environment.isImportMode()) || ((this._launchDesc != null) && (this._launchDesc.isLibrary()));
  }

  private boolean prepareToLaunch()
  {
    PerfLogger.setTime("Start: prepareToLaunch()");
    PerfLogger.setTime("setting tryOffline");
    boolean bool1 = (Cache.isCacheEnabled()) && ((this._initialLaunchDesc.getUpdate().isBackgroundCheck()) || (DeployOfflineManager.isForcedOffline()) || (JnlpxArgs.getIsRelaunch()));
    PerfLogger.setTime(" tryOffline = " + bool1);
    try
    {
      if ((!bool1) && (DeployOfflineManager.isForcedOffline()))
        throw new CacheUpdateRequiredException("Forced offline mode!");
      boolean bool2 = prepareToLaunch(bool1);
      PerfLogger.setTime("End: prepareToLaunch()");
      return bool2;
    }
    catch (CacheUpdateRequiredException localCacheUpdateRequiredException1)
    {
      Trace.println("Could not launch from cache. Will try online mode. [" + localCacheUpdateRequiredException1.getMessage() + "]");
      if ((bool1) && (DeployOfflineManager.isForcedOffline()))
      {
        DeployOfflineManager.setForcedOffline(false);
        if (!DeployOfflineManager.askUserGoOnline(this._initialLaunchDesc.getLocation()))
        {
          DeployOfflineManager.setForcedOffline(true);
          Trace.println("User chose not to go online and we can not not start in offline mode");
          LaunchErrorDialog.show(Progress.get(null).getOwner(), new OfflineLaunchException(0), this._exit);
          return false;
        }
      }
      try
      {
        PerfLogger.setTime("End: prepareToLaunch()");
        return prepareToLaunch(false);
      }
      catch (CacheUpdateRequiredException localCacheUpdateRequiredException2)
      {
        Trace.println("Unexpected exception: " + localCacheUpdateRequiredException2);
      }
    }
    return false;
  }

  private boolean canRelaunch()
  {
    if (this._isRelaunch)
    {
      Trace.println("JAVAWS: Relaunch ignored: relaunched already", TraceLevel.BASIC);
      return false;
    }
    return true;
  }

  private boolean prepareToLaunch(boolean paramBoolean)
    throws Launcher.CacheUpdateRequiredException
  {
    PerfLogger.setTime("Start: prepareToLaunch1()");
    try
    {
      Trace.println("prepareToLaunch: offlineOnly=" + paramBoolean, TraceLevel.NETWORK);
      PerfLogger.setTime("Begin updateFinalLaunchDesc");
      boolean bool1 = updateFinalLaunchDesc(this._initialLaunchDesc, 0, paramBoolean);
      removeTempJnlpFile(this._launchDesc);
      if (bool1)
      {
        File localFile = null;
        if (this._launchDesc.isApplicationDescriptor())
        {
          try
          {
            localFile = DownloadEngine.getCachedFile(this._launchDesc.getCanonicalHome());
          }
          catch (IOException localIOException1)
          {
            Trace.ignoredException(localIOException1);
          }
          if (this._args != null)
            this._args[0] = localFile.getPath();
        }
      }
      PerfLogger.setTime("End updateFinalLaunchDesc");
      boolean bool2 = this._launchDesc.isInstaller();
      this._isRelaunch = JnlpxArgs.getIsRelaunch();
      URL localURL1 = this._launchDesc.getCanonicalHome();
      if ((!bool2) && (!this._launchDesc.isLibrary()))
      {
        this._lap = Cache.getLocalApplicationProperties(localURL1);
        if ((paramBoolean) && (this._lap != null) && (this._lap.forceUpdateCheck()))
          throw new CacheUpdateRequiredException("Need to update: force update set in LAP");
      }
      if ((bool1) && (this._lap != null) && (Cache.isCacheEnabled()) && (this._lap.isShortcutInstalled()) && (LocalInstallHandler.getInstance() != null) && (LocalInstallHandler.getInstance().isShortcutExists(this._lap)))
        notifyLocalInstallHandler(this._launchDesc, this._lap, Globals.isSilentMode(), bool1, Progress.get(null).getOwnerRef());
      URL localURL2 = this._launchDesc.getLocation();
      if (localURL2 != null)
        Cache.removeRemovedApp(localURL2.toString(), this._launchDesc.getInformation().getTitle());
      Trace.println("isUpdated: " + bool1, TraceLevel.NETWORK);
      if (this._launchDesc.getResources() != null)
        Globals.getDebugOptionsFromProperties(this._launchDesc.getResources().getResourceProperties());
      if (Config.getBooleanProperty("deployment.security.authenticator"))
      {
        this._ja = JAuthenticator.getInstance(Progress.get(null).getOwnerRef());
        Authenticator.setDefault(this._ja);
      }
      if (this._lap != null)
      {
        URL localURL3 = LaunchDescFactory.getDerivedCodebase();
        if (localURL3 != null)
          this._lap.setCodebase(localURL3.toString());
        try
        {
          this._lap.store();
        }
        catch (IOException localIOException2)
        {
          Trace.ignoredException(localIOException2);
        }
      }
      if ((useAppletLifecycle()) && (!isImport()))
      {
        launchAppUsingAppletLifecycle(this._launchDesc);
        int k = 1;
        return k;
      }
      prepareAllResources(this._launchDesc, this._args, bool1, paramBoolean);
    }
    catch (CacheUpdateRequiredException localCacheUpdateRequiredException)
    {
      throw localCacheUpdateRequiredException;
    }
    catch (Throwable localThrowable)
    {
      ExitException localExitException1 = (localThrowable instanceof ExitException) ? (ExitException)localThrowable : new ExitException(localThrowable, 3);
      int i = localExitException1.getReason() == 0 ? 0 : -1;
      if (localExitException1.getReason() == 3)
        LaunchErrorDialog.show(Progress.get(null).getOwner(), localExitException1.getException(), this._exit);
      else if (localExitException1.getReason() == 6)
        LaunchErrorDialog.show(Progress.get(null).getOwner(), localExitException1, this._exit);
      if (i == 0)
        Trace.println("Exiting", TraceLevel.BASIC);
      else
        Trace.ignoredException(localExitException1);
      if (this._exit)
        try
        {
          Main.systemExit(i);
        }
        catch (ExitException localExitException2)
        {
          Trace.println("systemExit: " + localExitException2, TraceLevel.BASIC);
          Trace.ignoredException(localExitException2);
        }
      PerfLogger.setTime("End: prepareToLaunch1()");
      int j = 0;
      return j;
    }
    finally
    {
      removeLaunchPropFile();
    }
    PerfLogger.setTime("End: prepareToLaunch1()");
    return true;
  }

  private boolean useAppletLifecycle()
  {
    if (this._launchDesc == null)
      return false;
    return (this._launchDesc.getLaunchType() == 6) || (this._launchDesc.getLaunchType() == 2);
  }

  private void launchAppUsingAppletLifecycle(LaunchDesc paramLaunchDesc)
    throws ExitException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
  {
    MatchJREIf localMatchJREIf = paramLaunchDesc.getJREMatcher();
    this._jreInfo = paramLaunchDesc.selectJRE(localMatchJREIf);
    if ((this._jreInfo == null) || (!SecureStaticVersioning.canUse(paramLaunchDesc, this._jreInfo.getProduct())))
      this._jreInfo = paramLaunchDesc.selectJRE(new SecureMatchJRE());
    int i = 0;
    JfxRuntime localJfxRuntime = null;
    if (paramLaunchDesc.needFX())
    {
      localJfxRuntime = JfxHelper.getBestJfxInstalled(paramLaunchDesc);
      if (null == localJfxRuntime)
      {
        i = 1;
        try
        {
          localJfxRuntime = JfxHelper.installJfxRuntime(paramLaunchDesc, Progress.get(null));
        }
        catch (DownloadException localDownloadException)
        {
          throw new ExitException(ResourceManager.getMessage("launch.error.jfx.download"), localDownloadException);
        }
        catch (Throwable localThrowable)
        {
          throw new ExitException(localThrowable, 3);
        }
      }
      if (!isJfxSupportSatisfied(paramLaunchDesc))
        i = 1;
    }
    localMatchJREIf = paramLaunchDesc.getJREMatcher();
    if (i == 0)
      i = !localMatchJREIf.isRunningJVMSatisfying(true) ? 1 : 0;
    if ((i != 0) && (canRelaunch()))
    {
      localObject1 = localMatchJREIf.getSelectedJVMParameters();
      this._jreInfo = localMatchJREIf.getSelectedJREInfo();
      localObject2 = localMatchJREIf.getSelectedJREDesc();
      relaunch((JREDesc)localObject2, this._args, (JVMParameters)localObject1, false, localJfxRuntime, true);
      return;
    }
    Object localObject1 = paramLaunchDesc.getCodebase().toString();
    Object localObject2 = paramLaunchDesc.getAppletDescriptor();
    Object localObject3 = (localObject2 != null) && (((AppletDesc)localObject2).getDocumentBase() != null) ? ((AppletDesc)localObject2).getDocumentBase().toString() : localObject1;
    executeApplet(paramLaunchDesc, null, Progress.get(null), this._lap, (String)localObject1, localObject3, true);
  }

  protected boolean updateFinalLaunchDesc(LaunchDesc paramLaunchDesc, int paramInt, boolean paramBoolean)
    throws ExitException, Launcher.CacheUpdateRequiredException
  {
    int i = DownloadEngine.incrementInternalUse();
    try
    {
      URL localURL1 = paramLaunchDesc.getLocation();
      if (localURL1 == null)
      {
        this._launchDesc = paramLaunchDesc;
        int j = LaunchDownload.updateLaunchDescInCache(paramLaunchDesc) != paramLaunchDesc ? 1 : 0;
        return j;
      }
      URL localURL2 = deriveCodebase(localURL1);
      boolean bool1 = SystemUtils.isPathFromCache(this._args[0]);
      File localFile = DownloadEngine.getCachedFile(localURL1);
      URL localURL3;
      boolean bool3;
      if ((!paramBoolean) && (bool1))
      {
        LaunchDesc localLaunchDesc = LaunchDownload.getUpdatedLaunchDesc(localURL1, null);
        if (localLaunchDesc == null)
        {
          this._launchDesc = paramLaunchDesc;
          int k = 0;
          return k;
        }
        localURL3 = localLaunchDesc.getLocation();
        if ((localURL3 == null) || ((!localURL3.toString().equals(localURL1.toString())) && (paramInt == 0)))
        {
          Cache.removeCacheEntry(localURL1, null, null);
          paramInt++;
          bool3 = updateFinalLaunchDesc(localLaunchDesc, paramInt, false);
          return bool3;
        }
        this._launchDesc = localLaunchDesc;
        bool3 = true;
        return bool3;
      }
      if (localFile != null)
      {
        try
        {
          this._launchDesc = LaunchDescFactory.buildDescriptor(localFile, localURL2, null, localURL1);
        }
        catch (LaunchDescException localLaunchDescException)
        {
          this._launchDesc = LaunchDescFactory.buildDescriptor(localFile);
          if (this._launchDesc == null)
            throw localLaunchDescException;
        }
        localObject1 = null;
        if (!paramLaunchDesc.hasIdenticalContent(this._launchDesc))
        {
          if ((paramInt == 0) && (paramBoolean))
            throw new CacheUpdateRequiredException("Given JNLP is newer than cached copy!");
          localObject1 = LaunchDownload.getUpdatedLaunchDesc(localURL1, null);
        }
        if (localObject1 != null)
        {
          this._launchDesc = ((LaunchDesc)localObject1);
          localURL3 = this._launchDesc.getLocation();
          if ((localURL3 == null) || ((!localURL3.toString().equals(localURL1.toString())) && (paramInt == 0)))
          {
            Cache.removeCacheEntry(localURL1, null, null);
            paramInt++;
            bool3 = updateFinalLaunchDesc(this._launchDesc, paramInt, paramBoolean);
            return bool3;
          }
          bool3 = true;
          return bool3;
        }
        Cache.removeRemovedApp(localURL1.toString(), this._launchDesc.getInformation().getTitle());
        localURL3 = this._launchDesc.getLocation();
        if ((localURL3 == null) || ((!localURL3.toString().equals(localURL1.toString())) && (paramInt == 0)))
        {
          Cache.removeCacheEntry(localURL1, null, null);
          paramInt++;
          bool3 = updateFinalLaunchDesc(this._launchDesc, paramInt, paramBoolean);
          return bool3;
        }
        this._launchDesc = paramLaunchDesc;
        bool3 = false;
        return bool3;
      }
      if (paramBoolean)
        throw new CacheUpdateRequiredException("Missing from the cache: " + localURL1);
      if (Cache.isCacheEnabled())
      {
        DownloadEngine.getResource(localURL1, null, null, null, true);
        localFile = DownloadEngine.getCachedFile(localURL1);
        if (localFile != null)
        {
          this._launchDesc = LaunchDescFactory.buildDescriptor(localFile, localURL2, null, localURL1);
          localObject1 = this._launchDesc.getLocation();
          if ((localObject1 == null) || ((!((URL)localObject1).toString().equals(localURL1.toString())) && (paramInt == 0)))
          {
            Cache.removeCacheEntry(localURL1, null, null);
            paramInt++;
            bool2 = updateFinalLaunchDesc(this._launchDesc, paramInt, paramBoolean);
            return bool2;
          }
          bool2 = true;
          return bool2;
        }
        throw new Exception("cache failed for" + localURL1);
      }
      this._launchDesc = LaunchDescFactory.buildDescriptor(localURL1, localURL2);
      Object localObject1 = this._launchDesc.getLocation();
      if ((localObject1 != null) && (!((URL)localObject1).toString().equals(localURL1.toString())) && (paramInt == 0))
      {
        paramInt++;
        bool2 = updateFinalLaunchDesc(this._launchDesc, paramInt, paramBoolean);
        return bool2;
      }
      boolean bool2 = false;
      return bool2;
    }
    catch (CacheUpdateRequiredException localCacheUpdateRequiredException)
    {
      throw localCacheUpdateRequiredException;
    }
    catch (Exception localException)
    {
      throw new ExitException(localException, 3);
    }
    finally
    {
      DownloadEngine.decrementInternalUse(i);
    }
    throw localObject2;
  }

  private URL deriveCodebase(URL paramURL)
  {
    try
    {
      return new URL(paramURL.toString().substring(0, paramURL.toString().lastIndexOf("/") + 1));
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignoredException(localMalformedURLException);
    }
    return null;
  }

  private void removeTempJnlpFile(LaunchDesc paramLaunchDesc)
  {
    File localFile = null;
    if (paramLaunchDesc.isApplicationDescriptor())
      try
      {
        localFile = DownloadEngine.getCachedFile(paramLaunchDesc.getCanonicalHome());
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    if (localFile == null)
      return;
    if ((this._args != null) && (localFile != null) && (JnlpxArgs.shouldRemoveArgumentFile()))
    {
      new File(this._args[0]).delete();
      JnlpxArgs.setShouldRemoveArgumentFile(String.valueOf(false));
      this._args[0] = localFile.getPath();
    }
  }

  private static void removeLaunchPropFile()
  {
    String str = Environment.getLaunchPropFile();
    if (str != null)
      try
      {
        Trace.println("Remove LaunchPropFile: " + str, TraceLevel.BASIC);
        new File(str).delete();
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
  }

  static String getCurrentJavaFXVersion()
  {
    URL localURL = null;
    try
    {
      localURL = new URL("http://dl.javafx.com/javafx-rt.jnlp");
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    CacheEntry localCacheEntry = Cache.getLatestCacheEntry(localURL, null);
    LaunchDesc localLaunchDesc = null;
    String str = "XX";
    if (localCacheEntry != null)
    {
      try
      {
        localLaunchDesc = LaunchDescFactory.buildDescriptor(localCacheEntry.getDataFile(), null, null, localURL);
      }
      catch (Exception localException)
      {
      }
      str = localLaunchDesc.getVersion();
    }
    return str;
  }

  static String getRequestedJavaFXVersion(LaunchDesc paramLaunchDesc)
  {
    String str = "XX";
    if (paramLaunchDesc != null)
      str = paramLaunchDesc.getVersion();
    return str;
  }

  void prepareAllResources(LaunchDesc paramLaunchDesc, String[] paramArrayOfString, boolean paramBoolean1, boolean paramBoolean2)
    throws ExitException, Launcher.CacheUpdateRequiredException
  {
    PerfLogger.setTime("Start: prepareAllResources()");
    ArrayList localArrayList = new ArrayList();
    boolean bool1 = prepareLaunchFile(paramLaunchDesc, paramArrayOfString, paramBoolean2, localArrayList);
    prepareSecurity(paramLaunchDesc);
    PreloaderDelegate localPreloaderDelegate = Progress.get(null);
    JNLPSignedResourcesHelper localJNLPSignedResourcesHelper = new JNLPSignedResourcesHelper(paramLaunchDesc);
    if (!paramBoolean2)
    {
      prepareEnvironment(paramLaunchDesc);
      if ((!Globals.isSilentMode()) && (!paramBoolean2))
      {
        int i = (!paramLaunchDesc.getUpdate().isBackgroundCheck()) && (!paramLaunchDesc.getUpdate().isPromptPolicy()) && (!this._isRelaunch) ? 1 : 0;
        boolean bool2 = (!this._isCached) || (i != 0);
        1 local1 = new Runnable(localJNLPSignedResourcesHelper, localPreloaderDelegate)
        {
          private final JNLPSignedResourcesHelper val$signingHelper;
          private final PreloaderDelegate val$preloader;

          public void run()
          {
            SplashScreen.hide();
            try
            {
              boolean bool = this.val$signingHelper.checkSignedResources(Progress.get(null), true);
              Trace.println("Security check for progress jars: allSigned=" + bool, TraceLevel.SECURITY);
            }
            catch (Exception localException)
            {
              throw new RuntimeException(localException);
            }
            this.val$preloader.initPreloader(Launcher.this._jnlpClassLoader, Main.getLaunchThreadGroup());
          }
        };
        2 local2 = new Runnable(localPreloaderDelegate)
        {
          private final PreloaderDelegate val$preloader;

          public void run()
          {
            this.val$preloader.setPreloaderClass(null);
            SplashScreen.hide();
            this.val$preloader.initPreloader(Launcher.this._jnlpClassLoader, Main.getLaunchThreadGroup());
          }
        };
        LaunchDownload.prepareCustomProgress(paramLaunchDesc, localPreloaderDelegate, localJNLPSignedResourcesHelper, local1, local2, bool2);
      }
    }
    prepareResources(paramLaunchDesc, paramArrayOfString, paramBoolean1, paramBoolean2, bool1, localArrayList, localJNLPSignedResourcesHelper);
    if (paramBoolean2)
    {
      prepareEnvironment(paramLaunchDesc);
      localPreloaderDelegate.setPreloaderClass(paramLaunchDesc.getProgressClassName());
      localPreloaderDelegate.initPreloader(this._jnlpClassLoader, Main.getLaunchThreadGroup());
    }
    PerfLogger.setTime("End: prepareAllResources()");
  }

  private boolean prepareLaunchFile(LaunchDesc paramLaunchDesc, String[] paramArrayOfString, boolean paramBoolean, ArrayList paramArrayList)
    throws ExitException, Launcher.CacheUpdateRequiredException
  {
    boolean bool1 = false;
    if (paramLaunchDesc.getResources() == null)
      handleJnlpFileException(paramLaunchDesc, new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.noappresources", paramLaunchDesc.getSpecVersion()), null));
    if ((!isImport()) && (paramLaunchDesc.isLibrary()))
    {
      LaunchDescException localLaunchDescException1 = new LaunchDescException(paramLaunchDesc, "Internal Error: !isImport() && ld.isLibrary()", null);
      handleJnlpFileException(paramLaunchDesc, localLaunchDescException1);
    }
    boolean bool2 = paramLaunchDesc.isInstaller();
    JNLPException.setDefaultLaunchDesc(paramLaunchDesc);
    JREInfo localJREInfo = JREInfo.getHomeJRE();
    Trace.println("Launcher: isInstaller: " + bool2 + ", isRelaunch: " + this._isRelaunch + ", isImport(): " + isImport() + ", java.home:" + Environment.getJavaHome() + ", Running JRE: " + localJREInfo, TraceLevel.BASIC);
    Trace.println("JREInfos", TraceLevel.BASIC);
    JREInfo.traceJREs();
    if (localJREInfo == null)
    {
      LaunchDescException localLaunchDescException2 = new LaunchDescException(paramLaunchDesc, "Internal Error: no running JRE", null);
      handleJnlpFileException(paramLaunchDesc, localLaunchDescException2);
    }
    if ((!paramLaunchDesc.getInformation().supportsOfflineOperation()) && (DeployOfflineManager.isGlobalOffline() == true))
      throw new ExitException(new OfflineLaunchException(1), 3);
    PerfLogger.setTime("  - Start: LaunchDownload.isInCache(ld)");
    if (((paramLaunchDesc.getUpdate().isBackgroundCheck()) || (paramLaunchDesc.getUpdate().isPromptPolicy())) && ((this._lap == null) || (!this._lap.forceUpdateCheck())))
      this._isCached = LaunchDownload.isInCache(paramLaunchDesc);
    PerfLogger.setTime("  - End: LaunchDownload.isInCache(ld)");
    if ((!this._isCached) && (!paramBoolean))
      try
      {
        LaunchDownload.downloadExtensions(paramLaunchDesc, null, 0, paramArrayList);
        bool1 = true;
      }
      catch (Exception localException)
      {
        if ((!paramLaunchDesc.getInformation().supportsOfflineOperation()) || (!LaunchDownload.isInCache(paramLaunchDesc)))
          throw new ExitException(localException, 3);
        Trace.ignoredException(localException);
      }
    else
      bool1 = this._isCached;
    if (!bool1)
    {
      if ((!paramLaunchDesc.getInformation().supportsOfflineOperation()) || (!LaunchDownload.isInCache(paramLaunchDesc)))
        throw new CacheUpdateRequiredException("Some of required resources are not cached.");
      bool1 = true;
    }
    MatchJREIf localMatchJREIf = paramLaunchDesc.getJREMatcher();
    JVMParameters localJVMParameters = localMatchJREIf.getSelectedJVMParameters();
    this._jreInfo = localMatchJREIf.getSelectedJREInfo();
    JREDesc localJREDesc = localMatchJREIf.getSelectedJREDesc();
    LaunchDescException localLaunchDescException3;
    if (((this._jreInfo == null) && (localJREDesc == null)) || (null == localJVMParameters))
    {
      Trace.println(localMatchJREIf.toString());
      localLaunchDescException3 = new LaunchDescException(paramLaunchDesc, "Internal Error: Internal error, jreMatcher uninitialized", null);
      handleJnlpFileException(paramLaunchDesc, localLaunchDescException3);
    }
    if (!paramLaunchDesc.isJRESpecified())
    {
      localLaunchDescException3 = new LaunchDescException(paramLaunchDesc, "Internal Error: !isJRESpecified()", null);
      handleJnlpFileException(paramLaunchDesc, localLaunchDescException3);
    }
    if (paramLaunchDesc.needFX())
      try
      {
        if (this._jreInfo != null)
          JfxHelper.validateJfxRequest(paramLaunchDesc, this._jreInfo);
        else
          JfxHelper.validateJfxRequest(paramLaunchDesc, localJREDesc);
      }
      catch (LaunchDescException localLaunchDescException4)
      {
        throw new ExitException(localLaunchDescException4, 3);
      }
    URL localURL = paramLaunchDesc.getCanonicalHome();
    if (localURL == null)
    {
      LaunchDescException localLaunchDescException5 = new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.nomainjar"), null);
      throw new ExitException(localLaunchDescException5, 3);
    }
    if (bool2)
    {
      this._lap = Cache.getLocalApplicationProperties(paramArrayOfString[0]);
      if ((this._lap == null) || (!Environment.isInstallMode()))
        handleJnlpFileException(paramLaunchDesc, new MissingFieldException(paramLaunchDesc.getSource(), "<application-desc>|<applet-desc>"));
      localURL = this._lap.getLocation();
    }
    else if (!paramLaunchDesc.isLibrary())
    {
      this._lap = Cache.getLocalApplicationProperties(localURL);
    }
    Trace.println("LaunchDesc location: " + localURL, TraceLevel.BASIC);
    boolean bool3 = (this._isCached) && (DeployOfflineManager.isGlobalOffline());
    if (!isImport())
      if (this._jreInfo == null)
      {
        if (!SecureStaticVersioning.canAutoDownload(paramLaunchDesc, localJREDesc, bool3))
          this._jreInfo = paramLaunchDesc.selectJRE(new SecureMatchJRE());
      }
      else if (!SecureStaticVersioning.canUse(paramLaunchDesc, this._jreInfo.getProduct()))
        this._jreInfo = paramLaunchDesc.selectJRE(new SecureMatchJRE());
    return bool1;
  }

  private void prepareEnvironment(LaunchDesc paramLaunchDesc)
    throws ExitException
  {
    if ((isImport()) && (!paramLaunchDesc.isInstaller()))
      return;
    AppPolicy localAppPolicy = AppPolicy.createInstance(paramLaunchDesc.getCanonicalHome().getHost());
    this._jnlpClassLoader = JNLPClassLoader.createClassLoader(paramLaunchDesc, localAppPolicy);
    try
    {
      String str1 = "http";
      URL localURL = paramLaunchDesc.getCanonicalHome();
      if ((localURL.getProtocol().equalsIgnoreCase("file")) && (localURL.getHost().equals("")))
        str1 = "file";
      BasicServiceImpl.initialize(paramLaunchDesc.getCodebase(), BrowserSupport.isWebBrowserSupported(), str1);
      if (paramLaunchDesc.getLaunchType() == 4)
      {
        String str2 = this._lap.getInstallDirectory();
        if (str2 == null)
        {
          str2 = Cache.getNewExtensionInstallDirectory();
          this._lap.setInstallDirectory(str2);
        }
        ExtensionInstallerServiceImpl.initialize(paramLaunchDesc.isSecure() ? null : str2, this._lap, Progress.get(null));
      }
    }
    catch (Throwable localThrowable)
    {
      throw new ExitException(localThrowable, 3);
    }
    if (!this.securityManagerDiabledForTests)
      System.setSecurityManager(new JavaWebStartSecurity());
  }

  void disableSecurityManagerForTests()
  {
    this.securityManagerDiabledForTests = true;
  }

  private boolean isJfxSupportSatisfied(LaunchDesc paramLaunchDesc)
  {
    if (!paramLaunchDesc.needFX())
      return true;
    if ((paramLaunchDesc.isFXApp()) && (!ToolkitStore.isUsingPreferredToolkit(11, 0)))
      return false;
    return JfxHelper.isJfxSupportSatisfied(null, paramLaunchDesc);
  }

  private void relaunch(JREDesc paramJREDesc, String[] paramArrayOfString, JVMParameters paramJVMParameters, boolean paramBoolean1, JfxRuntime paramJfxRuntime, boolean paramBoolean2)
    throws ExitException
  {
    if (!canRelaunch())
      return;
    long l1 = paramJREDesc.getMinHeap();
    long l2 = paramJREDesc.getMaxHeap();
    String str = Environment.getLaunchPropFile();
    if (str != null)
    {
      paramArrayOfString = new String[2];
      paramArrayOfString[0] = "-nocodebase";
      paramArrayOfString[1] = str;
    }
    try
    {
      paramArrayOfString = insertApplicationArgs(paramArrayOfString);
      JnlpxArgs.execProgram(this._jreInfo, paramArrayOfString, l1, l2, paramJVMParameters, paramBoolean1, paramJfxRuntime, paramBoolean2);
    }
    catch (IOException localIOException)
    {
      throw new ExitException(new JreExecException(this._jreInfo.getPath(), localIOException), 3);
    }
    if (JnlpxArgs.shouldRemoveArgumentFile())
      JnlpxArgs.setShouldRemoveArgumentFile(String.valueOf(false));
    throw new ExitException(null, 0);
  }

  private void prepareResources(LaunchDesc paramLaunchDesc, String[] paramArrayOfString, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, ArrayList paramArrayList, JNLPSignedResourcesHelper paramJNLPSignedResourcesHelper)
    throws ExitException, Launcher.CacheUpdateRequiredException
  {
    PerfLogger.setTime("  - Start: prepareResources");
    boolean bool1 = paramLaunchDesc.isInstaller();
    boolean bool2 = (this._isCached) && (DeployOfflineManager.isGlobalOffline());
    boolean bool3 = ((!isImport()) && (this._jreInfo == null)) || (bool1);
    boolean bool4 = (!this._isCached) || (bool3);
    if ((bool4) && (bool2))
      throw new ExitException(new OfflineLaunchException(0), 3);
    boolean bool5 = bool4;
    if (!bool2)
      if ((this._lap != null) && (this._lap.forceUpdateCheck()))
      {
        Trace.println("Forced update check in LAP, do full update", TraceLevel.BASIC);
        bool5 = true;
      }
      else if ((!paramBoolean2) && (!paramLaunchDesc.getUpdate().isBackgroundCheck()) && (paramLaunchDesc.getUpdate().getPolicy() == 0))
      {
        bool5 = true;
      }
      else if (!paramBoolean2)
      {
        try
        {
          bool5 = paramLaunchDesc.getUpdater().isUpdateAvailable();
        }
        catch (Exception localException)
        {
          throw new ExitException(localException, 3);
        }
        if ((bool5) && (paramLaunchDesc.getUpdate().getPolicy() != 0))
        {
          this._isCached = LaunchDownload.isInCache(paramLaunchDesc);
          if (this._isCached)
            bool4 = bool3;
        }
        if (paramLaunchDesc.getUpdater().isCheckAborted())
          throw new ExitException(new LaunchDescException(paramLaunchDesc, "User rejected cert - aborted", null), 4);
      }
    Trace.println("Offline mode: " + bool2 + "\nIsInCache: " + this._isCached + "\nforceUpdate: " + bool4 + "\nneedUpdate: " + bool5 + "\nIsInstaller: " + bool1, TraceLevel.BASIC);
    if ((bool5) && (!bool4))
      bool4 = paramLaunchDesc.getUpdater().needUpdatePerPolicy(Progress.get(null));
    if ((bool4) && (bool2))
      throw new ExitException(new OfflineLaunchException(0), 3);
    if (bool4)
    {
      PerfLogger.setTime("    - Start: Update Check");
      if ((!isImport()) && (this._jreInfo == null))
      {
        downloadJREResource(paramLaunchDesc, paramArrayList);
        if (!paramArrayList.isEmpty())
        {
          JnlpxArgs.executeInstallers(paramArrayList, Progress.get(null));
          if (!paramLaunchDesc.isValidSpecificationVersion())
            Platform.get().resetJavaHome();
        }
        String str1 = Environment.getJavaHome() + File.separator + "bin" + File.separator;
        Platform.get().notifyJREInstalled(str1);
        this._jreInstalled = true;
      }
      else
      {
        try
        {
          downloadResources(paramLaunchDesc, paramArrayList, paramBoolean3, bool1);
        }
        catch (ExitException localExitException)
        {
          if (Environment.isJavaFXInstallInitiated())
          {
            Throwable localThrowable1 = localExitException.getException();
            if ((localThrowable1 instanceof FailedDownloadException))
            {
              localObject = ((FailedDownloadException)localThrowable1).getLocation().toString();
              Pings.sendJFXPing("jfxic", getCurrentJavaFXVersion(), getRequestedJavaFXVersion(paramLaunchDesc), 3, (String)localObject);
            }
            else
            {
              Pings.sendJFXPing("jfxic", getCurrentJavaFXVersion(), getRequestedJavaFXVersion(paramLaunchDesc), 2, null);
            }
          }
          throw localExitException;
        }
      }
      if ((this._lap != null) && (this._lap.forceUpdateCheck()))
      {
        this._lap.setForceUpdateCheck(false);
        try
        {
          this._lap.store();
        }
        catch (IOException localIOException1)
        {
          Trace.ignoredException(localIOException1);
        }
      }
      PerfLogger.setTime("    - End: Update Check");
    }
    PreloaderDelegate localPreloaderDelegate = Progress.get(null);
    try
    {
      localPreloaderDelegate.handleEvent(new DownloadEvent(1, paramLaunchDesc.getLocation(), null, null, 1L, 1L, 100));
    }
    catch (CancelException localCancelException1)
    {
      throw new ExitException(localCancelException1, 3);
    }
    try
    {
      localPreloaderDelegate.waitTillLoaded();
    }
    catch (JNLPException localJNLPException1)
    {
      throw new ExitException(localJNLPException1, 3);
    }
    catch (IOException localIOException2)
    {
      if ((paramLaunchDesc.getInformation().supportsOfflineOperation()) && (LaunchDownload.isInCache(paramLaunchDesc, paramBoolean3)))
        Trace.ignoredException(localIOException2);
      else
        throw new ExitException(localIOException2, 3);
    }
    if ((this._jreInfo != null) || (isImport()))
    {
      SplashScreen.generateCustomSplash(paramLaunchDesc, paramBoolean1);
      if ((!isImport()) && (!paramArrayList.isEmpty()))
      {
        if (bool1);
        JnlpxArgs.executeInstallers(paramArrayList, Progress.get(null));
      }
      if ((!Globals.isSilentMode()) && (paramLaunchDesc.isInstaller()))
        try
        {
          Progress.get(null).handleEvent(new ConfigEvent(3, paramLaunchDesc.getAppInfo()));
          Progress.get(null).handleEvent(new InitEvent(2));
        }
        catch (CancelException localCancelException2)
        {
          throw new ExitException(localCancelException2, 3);
        }
    }
    MatchJREIf localMatchJREIf = paramLaunchDesc.getJREMatcher();
    Object localObject = localMatchJREIf.getSelectedJVMParameters();
    this._jreInfo = localMatchJREIf.getSelectedJREInfo();
    JREDesc localJREDesc = localMatchJREIf.getSelectedJREDesc();
    JfxRuntime localJfxRuntime = null;
    int i = !isJfxSupportSatisfied(paramLaunchDesc) ? 1 : 0;
    if (i != 0)
    {
      localJfxRuntime = JfxHelper.getBestJfxInstalled(paramLaunchDesc);
      if (null == localJfxRuntime)
      {
        if (bool2)
          throw new ExitException(new OfflineLaunchException(0), 3);
        try
        {
          localJfxRuntime = JfxHelper.installJfxRuntime(paramLaunchDesc, Progress.get(null));
        }
        catch (DownloadException localDownloadException)
        {
          throw new ExitException(ResourceManager.getMessage("launch.error.jfx.download"), localDownloadException);
        }
        catch (Throwable localThrowable2)
        {
          throw new ExitException(localThrowable2, 3);
        }
      }
    }
    if (!isImport())
    {
      if (!paramLaunchDesc.isValidSpecificationVersion())
      {
        JNLPException.setDefaultLaunchDesc(paramLaunchDesc);
        handleJnlpFileException(paramLaunchDesc, new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.badjnlversion", paramLaunchDesc.getSpecVersion()), null));
      }
      if (this._jreInfo == null)
      {
        Config.get().refreshIfNeeded();
        this._jreInfo = paramLaunchDesc.selectJRE();
        if (this._jreInfo == null)
        {
          Trace.println("No JREInfo(1): " + paramLaunchDesc.getJREMatcher());
          LaunchDescException localLaunchDescException = new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.missingjreversion"), null);
          throw new ExitException(localLaunchDescException, 3);
        }
        localMatchJREIf = paramLaunchDesc.getJREMatcher();
        localJREDesc = localMatchJREIf.getSelectedJREDesc();
        localObject = localMatchJREIf.getSelectedJVMParameters();
      }
      boolean bool6 = localMatchJREIf.isRunningJVMSatisfying(true);
      if (Trace.isEnabled(TraceLevel.BASIC))
      {
        Trace.println("_jreInstalled:    " + this._jreInstalled, TraceLevel.BASIC);
        Trace.println(localMatchJREIf.toString(), TraceLevel.BASIC);
      }
      if ((this._jreInstalled) && (bool6))
        throw new ExitException(new Exception("Internal Error: jreInstalled, but homeJVM matches"), 3);
      if ((paramLaunchDesc.isSecureJVMArgs()) && (((!bool6) && (this._jreInfo != null)) || (i != 0)))
        relaunch(localJREDesc, paramArrayOfString, (JVMParameters)localObject, false, localJfxRuntime, paramLaunchDesc.isFXApp());
    }
    if (isImport())
    {
      try
      {
        Progress.get(null).handleEvent(new InitEvent(5));
      }
      catch (CancelException localCancelException3)
      {
        throw new ExitException(localCancelException3, 3);
      }
      boolean bool7 = (LaunchDownload.isJnlpCached(paramLaunchDesc)) && (paramBoolean1);
      notifyLocalInstallHandler(paramLaunchDesc, this._lap, Globals.isSilentMode(), bool7, null);
      if (Environment.isJavaFXInstallInitiated())
        Pings.sendJFXPing("jfxic", getCurrentJavaFXVersion(), getRequestedJavaFXVersion(paramLaunchDesc), 0, null);
      preverifyImportedJARs(paramJNLPSignedResourcesHelper, paramLaunchDesc);
      Trace.println("Exiting after import", TraceLevel.BASIC);
      throw new ExitException(null, 0);
    }
    String str2 = " - " + paramLaunchDesc.getInformation().getTitle();
    JavawsConsoleController.getInstance().setTitle("console.caption", str2);
    JavawsConsoleController.getInstance().showConsoleIfEnabled();
    boolean bool8 = false;
    try
    {
      PerfLogger.setTime("    - Start: checkSignedLaunchDesc");
      paramJNLPSignedResourcesHelper.checkSignedLaunchDesc();
      PerfLogger.setTime("    - End: checkSignedLaunchDesc");
      PerfLogger.setTime("    - Start: checkSignedResources");
      bool8 = paramJNLPSignedResourcesHelper.checkSignedResources(Progress.get(null), false);
      PerfLogger.setTime("    - End: checkSignedResources");
      bool8 = (bool8) && (paramLaunchDesc.isSigned());
    }
    catch (JNLPException localJNLPException2)
    {
      throw new ExitException(localJNLPException2, 3);
    }
    catch (IOException localIOException3)
    {
      throw new ExitException(localIOException3, 3);
    }
    if (Trace.isEnabled(TraceLevel.BASIC))
    {
      Trace.println("passing security checks; secureArgs:" + paramLaunchDesc.isSecureJVMArgs() + ", allSigned:" + bool8, TraceLevel.BASIC);
      Trace.println("trusted app: " + (!paramLaunchDesc.isSecure()) + ", -secure=" + Globals.isSecureMode(), TraceLevel.BASIC);
      Trace.println(localMatchJREIf.toString(), TraceLevel.BASIC);
    }
    boolean bool9 = localMatchJREIf.isRunningJVMSatisfying(bool8);
    if ((!bool9) || (i != 0))
      relaunch(localJREDesc, paramArrayOfString, (JVMParameters)localObject, bool8, localJfxRuntime, paramLaunchDesc.isFXApp());
    JnlpxArgs.removeArgumentFile(paramArrayOfString[0]);
    if ((Cache.isCacheEnabled()) && (((LocalInstallHandler.getInstance() != null) && (!LocalInstallHandler.getInstance().isShortcutExists(this._lap))) || (Globals.isIconImageUpdated())))
    {
      notifyLocalInstallHandler(paramLaunchDesc, this._lap, Globals.isSilentMode(), (paramBoolean1) || (Globals.isIconImageUpdated()), Progress.get(null).getOwnerRef());
      if (Globals.isIconImageUpdated())
        Globals.setIconImageUpdated(false);
    }
    PerfLogger.setTime("  - End prepareResources()");
    Trace.println("continuing launch in this VM", TraceLevel.BASIC);
  }

  private void preverifyImportedJARs(JNLPSignedResourcesHelper paramJNLPSignedResourcesHelper, LaunchDesc paramLaunchDesc)
  {
    if ((Environment.getJavaFxInstallMode() == 1) || (Environment.getJavaFxInstallMode() == 2))
    {
      try
      {
        paramJNLPSignedResourcesHelper.checkSignedResources(Progress.get(null), false);
      }
      catch (JNLPException localJNLPException)
      {
        Trace.ignoredException(localJNLPException);
      }
      catch (ExitException localExitException)
      {
        Trace.ignoredException(localExitException);
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
      PreverificationClassLoader localPreverificationClassLoader = new PreverificationClassLoader(ClassLoader.getSystemClassLoader());
      localPreverificationClassLoader.initialize(paramLaunchDesc);
      localPreverificationClassLoader.preverifyJARs();
    }
  }

  private String[] insertApplicationArgs(String[] paramArrayOfString)
  {
    String[] arrayOfString1 = Globals.getApplicationArgs();
    if (arrayOfString1 == null)
      return paramArrayOfString;
    String[] arrayOfString2 = new String[arrayOfString1.length + paramArrayOfString.length];
    for (int i = 0; i < arrayOfString1.length; i++)
      arrayOfString2[i] = arrayOfString1[i];
    for (int j = 0; j < paramArrayOfString.length; j++)
      arrayOfString2[(i++)] = paramArrayOfString[j];
    return arrayOfString2;
  }

  private void doLaunchApp()
    throws ExitException
  {
    JNLPClassLoader localJNLPClassLoader = this._jnlpClassLoader;
    Thread.currentThread().setContextClassLoader(localJNLPClassLoader);
    ToolkitStore.get().setContextClassLoader(localJNLPClassLoader);
    String str1 = this._launchDesc.getInformation().getTitle();
    AppContext.getAppContext().put("deploy.trust.decider.app.name", str1);
    String str2 = null;
    Class localClass = null;
    Object localObject;
    try
    {
      str2 = LaunchDownload.getMainClassName(this._launchDesc, true);
      Trace.println("Main-class: " + str2, TraceLevel.BASIC);
      if (str2 == null)
        throw new ClassNotFoundException(str2);
      Progress.get(null).handleEvent(new AppInitEvent(1));
      localClass = localJNLPClassLoader.loadClass(str2);
      if (getClass().getPackage().equals(localClass.getPackage()))
        throw new ClassNotFoundException(str2);
      ClassLoader localClassLoader = localClass.getClassLoader();
      if ((localClassLoader != localJNLPClassLoader) && (localClassLoader != localJNLPClassLoader.getParent()) && (localClassLoader != ((JNLPClassLoader)localJNLPClassLoader).getJNLPPreverifyClassLoader()))
      {
        localObject = new SecurityException("Bad main-class name");
        throw new ExitException((Throwable)localObject, 3);
      }
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new ExitException(localClassNotFoundException, 3);
    }
    catch (IOException localIOException)
    {
      throw new ExitException(localIOException, 3);
    }
    catch (JNLPException localJNLPException)
    {
      throw new ExitException(localJNLPException, 3);
    }
    catch (Exception localException)
    {
      throw new ExitException(localException, 3);
    }
    catch (Throwable localThrowable1)
    {
      throw new ExitException(localThrowable1, 3);
    }
    try
    {
      if (Globals.TCKHarnessRun)
        Main.tckprintln("JNLP Launching");
      PerfLogger.setTime("calling executeMainClass ...");
      executeMainClass(this._launchDesc, this._lap, localClass, Progress.get(null));
    }
    catch (SecurityException localSecurityException)
    {
      throw new ExitException(localSecurityException, 3);
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      Trace.ignored(localInvocationTargetException);
      for (localObject = localInvocationTargetException; ((Throwable)localObject).getCause() != null; localObject = ((Throwable)localObject).getCause());
      throw new ExitException((Throwable)localObject, 3);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new ExitException(localNoSuchMethodException, 3);
    }
    catch (Throwable localThrowable2)
    {
      throw new ExitException(localThrowable2, 3);
    }
    if (this._launchDesc.getLaunchType() == 4)
      throw new ExitException(null, 0);
  }

  private void downloadJREResource(LaunchDesc paramLaunchDesc, ArrayList paramArrayList)
    throws ExitException
  {
    if ((!this._shownDownloadWindow) && (!Globals.isSilentMode()))
    {
      this._shownDownloadWindow = true;
      try
      {
        Progress.get(null).handleEvent(new ConfigEvent(3, paramLaunchDesc.getAppInfo()));
        Progress.get(null).handleEvent(new InitEvent(0));
      }
      catch (CancelException localCancelException)
      {
        throw new ExitException(localCancelException, 3);
      }
    }
    try
    {
      if (Cache.isCacheEnabled())
        LaunchDownload.downloadJRE(paramLaunchDesc, Progress.get(null), paramArrayList);
      else
        throw new IOException("Cache disabled, cannot download JRE");
    }
    catch (SecurityException localSecurityException)
    {
      throw new ExitException(localSecurityException, 3);
    }
    catch (JNLPException localJNLPException)
    {
      throw new ExitException(localJNLPException, 3);
    }
    catch (IOException localIOException)
    {
      Trace.ignored(localIOException);
      throw new ExitException(new NoLocalJREException(paramLaunchDesc, paramLaunchDesc.getResources().getSelectedJRE().getVersion(), false), 3);
    }
  }

  private void downloadResources(LaunchDesc paramLaunchDesc, ArrayList paramArrayList, boolean paramBoolean1, boolean paramBoolean2)
    throws ExitException
  {
    try
    {
      if ((!this._shownDownloadWindow) && (!Globals.isSilentMode()))
      {
        this._shownDownloadWindow = true;
        PreloaderDelegate localPreloaderDelegate = Progress.get(null);
        localPreloaderDelegate.handleEvent(new ConfigEvent(3, paramLaunchDesc.getAppInfo()));
        if (!paramBoolean2)
          localPreloaderDelegate.handleEvent(new InitEvent(4));
      }
      if (!paramBoolean1)
        LaunchDownload.downloadExtensions(paramLaunchDesc, Progress.get(null), 0, paramArrayList);
      LaunchDownload.checkJNLPSecurity(paramLaunchDesc);
      LaunchDownload.downloadEagerorAll(paramLaunchDesc, false, Progress.get(null), false);
    }
    catch (SecurityException localSecurityException)
    {
      throw new ExitException(localSecurityException, 3);
    }
    catch (JNLPException localJNLPException)
    {
      throw new ExitException(localJNLPException, 3);
    }
    catch (Exception localException)
    {
      if ((paramLaunchDesc.getInformation().supportsOfflineOperation()) && (LaunchDownload.isInCache(paramLaunchDesc, paramBoolean1)))
        Trace.ignoredException(localException);
      else
        throw new ExitException(localException, 3);
    }
  }

  public void prepareSecurity(LaunchDesc paramLaunchDesc)
    throws ExitException
  {
    try
    {
      LaunchDownload.checkJNLPSecurity(paramLaunchDesc);
    }
    catch (SecurityException localSecurityException)
    {
      throw new ExitException(localSecurityException, 3);
    }
    catch (JNLPException localJNLPException)
    {
      throw new ExitException(localJNLPException, 3);
    }
  }

  public static void notifyLocalInstallHandler(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean1, boolean paramBoolean2, ComponentRef paramComponentRef)
  {
    if (paramLocalApplicationProperties == null)
      return;
    URL localURL = LaunchDescFactory.getDerivedCodebase();
    if (localURL != null)
      paramLocalApplicationProperties.setCodebase(localURL.toString());
    paramLocalApplicationProperties.setLastAccessed(new Date());
    if ((!Environment.isImportMode()) && (!paramLaunchDesc.isLibrary()))
      paramLocalApplicationProperties.incrementLaunchCount();
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    if (localLocalInstallHandler != null)
      localLocalInstallHandler.install(paramLaunchDesc, paramLocalApplicationProperties, paramBoolean2, paramBoolean1, paramComponentRef);
    try
    {
      paramLocalApplicationProperties.store();
    }
    catch (IOException localIOException)
    {
      Trace.println("Couldn't save LAP: " + localIOException, TraceLevel.BASIC);
    }
  }

  private void executeMainClass(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, Class paramClass, PreloaderDelegate paramPreloaderDelegate)
    throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException
  {
    if (paramLaunchDesc.getLaunchType() == 2)
    {
      String str1 = null;
      String str2 = null;
      boolean bool1 = false;
      if (paramLocalApplicationProperties != null)
      {
        str1 = paramLocalApplicationProperties.getCodebase();
        str2 = paramLocalApplicationProperties.getDocumentBase();
        bool1 = paramLocalApplicationProperties.isDraggedApplet();
      }
      boolean bool2 = (!bool1) && ((str1 == null) || (str2 == null));
      if (bool2)
      {
        AppletDesc localAppletDesc = paramLaunchDesc.getAppletDescriptor();
        URL localURL1 = BasicServiceImpl.getInstance().getCodeBase();
        URL localURL2 = localAppletDesc.getDocumentBase();
        if (localURL2 == null)
          localURL2 = localURL1;
        str1 = localURL1 != null ? localURL1.toString() : null;
        str2 = localURL2 != null ? localURL2.toString() : null;
        if (str2 == null)
          str2 = paramLaunchDesc.getCanonicalHome().toString();
      }
      executeApplet(paramLaunchDesc, paramClass, paramPreloaderDelegate, paramLocalApplicationProperties, str1, str2, bool2);
    }
    else
    {
      paramLaunchDesc.getUpdater().startBackgroundUpdateOpt();
      executeApplication(paramLaunchDesc, paramLocalApplicationProperties, paramClass, paramPreloaderDelegate);
    }
  }

  private void executeApplication(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, Class paramClass, PreloaderDelegate paramPreloaderDelegate)
    throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
  {
    PerfLogger.setTime("Begin executeApplication");
    String[] arrayOfString = null;
    if (paramLaunchDesc.getLaunchType() == 4)
    {
      arrayOfString = new String[1];
      arrayOfString[0] = (paramLocalApplicationProperties.isExtensionInstalled() ? "uninstall" : "install");
      paramLocalApplicationProperties.setExtensionInstalled(false);
      paramLocalApplicationProperties.setRebootNeeded(false);
      try
      {
        paramLocalApplicationProperties.store();
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
    else
    {
      SplashScreen.hide();
      if (Globals.getApplicationArgs() != null)
        arrayOfString = Globals.getApplicationArgs();
      else if (!paramLaunchDesc.isFXApp())
        arrayOfString = paramLaunchDesc.getApplicationDescriptor().getArguments();
    }
    Object[] arrayOfObject = { arrayOfString };
    Class[] arrayOfClass = { new String[0].getClass() };
    Method localMethod = paramClass.getMethod("main", arrayOfClass);
    if (!Modifier.isStatic(localMethod.getModifiers()))
      throw new NoSuchMethodException(ResourceManager.getString("launch.error.nonstaticmainmethod"));
    localMethod.setAccessible(true);
    PerfLogger.setTime("End executeApplication (invoking App main)");
    PerfLogger.outputLog();
    Config.getHooks().preLaunch("javaws application", paramLaunchDesc.getCodebase().toString() + ": " + paramClass.getName() + " " + ArrayUtil.arrayToString(arrayOfString));
    try
    {
      Progress.get(null).handleEvent(new AppInitEvent(2));
    }
    catch (CancelException localCancelException)
    {
    }
    localMethod.invoke(null, arrayOfObject);
  }

  private void executeApplet(LaunchDesc paramLaunchDesc, Class paramClass, PreloaderDelegate paramPreloaderDelegate, LocalApplicationProperties paramLocalApplicationProperties, String paramString1, String paramString2, boolean paramBoolean)
    throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
  {
    try
    {
      Class localClass = null;
      localClass = Class.forName("sun.plugin2.applet.viewer.JNLP2Viewer");
      SplashScreen.hide();
      localObject1 = new Class[] { new String[0].getClass() };
      Method localMethod = localClass.getMethod("main", localObject1);
      localMethod.setAccessible(true);
      URL localURL1 = paramLaunchDesc.getCanonicalHome();
      localObject2 = null;
      try
      {
        String str = DownloadEngine.getCachedResourceFilePath(localURL1);
        localObject2 = new File(str);
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
      if (localObject2 != null)
      {
        localObject3 = null;
        if (!paramBoolean)
        {
          localObject4 = SingleInstanceManager.getActionName();
          if ((localObject4 != null) && ((((String)localObject4).equals("-open")) || (((String)localObject4).equals("-print"))))
          {
            localObject3 = new String[3];
            localObject3[0] = localObject4;
            localObject3[1] = SingleInstanceManager.getOpenPrintFilePath();
            localObject3[2] = ((File)localObject2).toString();
          }
          else if (paramLocalApplicationProperties.isDraggedApplet())
          {
            localObject3 = new String[2];
            localObject3[0] = "-draggedApplet";
            localObject3[1] = ((File)localObject2).toString();
          }
          else
          {
            localObject3 = new String[1];
            localObject3[0] = ((File)localObject2).toString();
          }
        }
        else
        {
          localObject3 = new String[5];
          localObject3[0] = "-codebase";
          localObject3[1] = paramString1;
          localObject3[2] = "-documentbase";
          localObject3[3] = paramString2;
          localObject3[4] = ((File)localObject2).toString();
        }
        Object localObject4 = { localObject3 };
        DeployOfflineManager.setForcedOffline(false);
        localObject5 = new Throwable[1];
        localObject5[0] = null;
        try
        {
          Thread localThread = new Thread(Main.getMainThreadGroup(), new Runnable(localMethod, localObject4, localObject5)
          {
            private final Method val$mainMethod;
            private final Object[] val$wrappedArgs;
            private final Throwable[] val$e;

            public void run()
            {
              try
              {
                this.val$mainMethod.invoke(null, this.val$wrappedArgs);
              }
              catch (Throwable localThrowable)
              {
                this.val$e[0] = localThrowable;
              }
            }
          });
          localThread.start();
          localThread.join();
        }
        catch (InterruptedException localInterruptedException)
        {
          localInterruptedException.printStackTrace();
        }
        finally
        {
          if (localObject5[0] != null)
            throw new RuntimeException(localObject5[0]);
        }
      }
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      paramLaunchDesc.getUpdater().startBackgroundUpdateOpt();
      Object localObject1 = paramLaunchDesc.getAppletDescriptor();
      int i = ((AppletDesc)localObject1).getWidth();
      int j = ((AppletDesc)localObject1).getHeight();
      Object localObject2 = null;
      localObject2 = (Applet)paramClass.newInstance();
      SplashScreen.hide();
      Object localObject3 = new JFrame();
      boolean bool = BrowserSupport.isWebBrowserSupported();
      Object localObject5 = new AppletContainerCallback((JFrame)localObject3)
      {
        private final JFrame val$mainFrame;

        public void showDocument(URL paramURL)
        {
          BrowserSupport.showDocument(paramURL);
        }

        public void relativeResize(Dimension paramDimension)
        {
          Dimension localDimension = this.val$mainFrame.getSize();
          localDimension.width += paramDimension.width;
          localDimension.height += paramDimension.height;
          this.val$mainFrame.setSize(localDimension);
        }
      };
      URL localURL2 = BasicServiceImpl.getInstance().getCodeBase();
      URL localURL3 = ((AppletDesc)localObject1).getDocumentBase();
      if (localURL3 == null)
        localURL3 = localURL2;
      AppletContainer localAppletContainer = new AppletContainer((AppletContainerCallback)localObject5, (Applet)localObject2, ((AppletDesc)localObject1).getName(), localURL3, localURL2, i, j, ((AppletDesc)localObject1).getParameters());
      ((JFrame)localObject3).addWindowListener(new WindowAdapter(localAppletContainer)
      {
        private final AppletContainer val$ac;

        public void windowClosing(WindowEvent paramWindowEvent)
        {
          this.val$ac.stopApplet();
        }
      });
      ((JFrame)localObject3).setTitle(paramLaunchDesc.getInformation().getTitle());
      Container localContainer = ((JFrame)localObject3).getContentPane();
      localContainer.setLayout(new BorderLayout());
      localContainer.add("Center", localAppletContainer);
      ((JFrame)localObject3).pack();
      Dimension localDimension = localAppletContainer.getPreferredFrameSize((Frame)localObject3);
      ((JFrame)localObject3).setSize(localDimension);
      ((JFrame)localObject3).getRootPane().revalidate();
      ((JFrame)localObject3).getRootPane().repaint();
      ((JFrame)localObject3).setResizable(false);
      if (!((JFrame)localObject3).isVisible())
        SwingUtilities.invokeLater(new Runnable((JFrame)localObject3)
        {
          private final JFrame val$mainFrame;

          public void run()
          {
            this.val$mainFrame.setVisible(true);
          }
        });
      Config.getHooks().preLaunch("javaws applet", ((AppletDesc)localObject1).getDocumentBase() + ": " + ArrayUtil.propertiesToString(((AppletDesc)localObject1).getParameters()));
      localAppletContainer.startApplet();
    }
  }

  private void handleJnlpFileException(LaunchDesc paramLaunchDesc, Exception paramException)
    throws ExitException
  {
    DownloadEngine.removeCachedResource(paramLaunchDesc.getCanonicalHome(), null, null);
    throw new ExitException(paramException, 3);
  }

  class CacheUpdateRequiredException extends Exception
  {
    public CacheUpdateRequiredException(String arg2)
    {
      super();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.Launcher
 * JD-Core Version:    0.6.0
 */