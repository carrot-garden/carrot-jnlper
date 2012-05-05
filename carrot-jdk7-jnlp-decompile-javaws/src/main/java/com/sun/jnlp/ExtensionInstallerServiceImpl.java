package com.sun.jnlp;

import com.sun.applet2.preloader.CancelException;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.ConfigEvent;
import com.sun.applet2.preloader.event.DownloadEvent;
import com.sun.applet2.preloader.event.InitEvent;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.javaws.Main;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.jnl.DefaultMatchJRE;
import com.sun.javaws.jnl.LaunchSelection;
import com.sun.javaws.progress.PreloaderDelegate;
import com.sun.javaws.ui.LaunchErrorDialog;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Date;
import javax.jnlp.ExtensionInstallerService;

public final class ExtensionInstallerServiceImpl
  implements ExtensionInstallerService
{
  private LocalApplicationProperties _lap;
  private Preloader _progress;
  private String _target;
  private String _installPath;
  private boolean _failedJREInstall = false;
  static ExtensionInstallerServiceImpl _sharedInstance = null;

  private ExtensionInstallerServiceImpl(String paramString, LocalApplicationProperties paramLocalApplicationProperties, Preloader paramPreloader)
  {
    this._lap = paramLocalApplicationProperties;
    this._progress = paramPreloader;
    this._installPath = paramString;
  }

  public static synchronized ExtensionInstallerServiceImpl getInstance()
  {
    return _sharedInstance;
  }

  public static synchronized void initialize(String paramString, LocalApplicationProperties paramLocalApplicationProperties, Preloader paramPreloader)
  {
    if (_sharedInstance == null)
      _sharedInstance = new ExtensionInstallerServiceImpl(paramString, paramLocalApplicationProperties, paramPreloader);
    if ((paramPreloader instanceof PreloaderDelegate))
      ((PreloaderDelegate)paramPreloader).keepPreloaderAlive(true);
  }

  public String getInstallPath()
  {
    return this._installPath;
  }

  public String getExtensionVersion()
  {
    return this._lap.getVersionId();
  }

  public URL getExtensionLocation()
  {
    return this._lap.getLocation();
  }

  public String getInstalledJRE(URL paramURL, String paramString)
  {
    JREInfo localJREInfo = LaunchSelection.selectJRE(paramURL, paramString, new DefaultMatchJRE());
    return localJREInfo != null ? localJREInfo.getPath() : null;
  }

  public void setHeading(String paramString)
  {
    try
    {
      this._progress.handleEvent(new ConfigEvent(2, paramString));
    }
    catch (CancelException localCancelException)
    {
      throw new RuntimeException(localCancelException);
    }
  }

  public void setStatus(String paramString)
  {
    try
    {
      this._progress.handleEvent(new ConfigEvent(1, paramString));
    }
    catch (CancelException localCancelException)
    {
      throw new RuntimeException(localCancelException);
    }
  }

  public void updateProgress(int paramInt)
  {
    try
    {
      DownloadEvent localDownloadEvent = new DownloadEvent(0, null, null, null, 100L, 100L, paramInt);
      localDownloadEvent.sendExplicitEvent(true);
      this._progress.handleEvent(localDownloadEvent);
    }
    catch (CancelException localCancelException)
    {
      localCancelException.printStackTrace();
      throw new RuntimeException(localCancelException);
    }
  }

  public void hideProgressBar()
  {
    try
    {
      this._progress.handleEvent(new ConfigEvent(5));
    }
    catch (CancelException localCancelException)
    {
      throw new RuntimeException(localCancelException);
    }
  }

  public void hideStatusWindow()
  {
    try
    {
      this._progress.handleEvent(new ConfigEvent(6));
    }
    catch (CancelException localCancelException)
    {
      throw new RuntimeException(localCancelException);
    }
  }

  public void setJREInfo(String paramString1, String paramString2)
  {
    int i = JNLPClassLoaderUtil.getInstance().getDefaultSecurityModel();
    if ((i != 1) && (i != 2))
      throw new SecurityException("Unsigned extension installer attempting to call setJREInfo.");
    Trace.println("setJREInfo: " + paramString2, TraceLevel.EXTENSIONS);
    if ((paramString2 != null) && (new File(paramString2).exists()))
    {
      JREInfo.removeJREsIn(paramString2);
      JREInfo.addJRE(new JREInfo(paramString1, getExtensionVersion(), getExtensionLocation().toString(), paramString2, null, Config.getOSName(), Config.getOSArch(), true, false));
    }
    else
    {
      Trace.println("jre install failed: jrePath invalid", TraceLevel.EXTENSIONS);
      this._failedJREInstall = true;
    }
  }

  public void setNativeLibraryInfo(String paramString)
  {
    Trace.println("setNativeLibInfo: " + paramString, TraceLevel.EXTENSIONS);
    this._lap.setNativeLibDirectory(paramString);
  }

  public void installFailed()
  {
    Trace.println("installFailed", TraceLevel.EXTENSIONS);
    if ((this._progress instanceof PreloaderDelegate))
      ((PreloaderDelegate)this._progress).keepPreloaderAlive(false);
    try
    {
      Main.systemExit(1);
    }
    catch (ExitException localExitException)
    {
      Trace.println("systemExit: " + localExitException, TraceLevel.BASIC);
      Trace.ignoredException(localExitException);
    }
  }

  public void installSucceeded(boolean paramBoolean)
  {
    if ((this._progress instanceof PreloaderDelegate))
      ((PreloaderDelegate)this._progress).keepPreloaderAlive(false);
    if (this._failedJREInstall)
      return;
    Trace.println("installSucceded", TraceLevel.EXTENSIONS);
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        Config.get().storeIfNeeded();
        return null;
      }
    });
    this._lap.setInstallDirectory(this._installPath);
    this._lap.setLastAccessed(new Date());
    if (paramBoolean)
      this._lap.setRebootNeeded(true);
    else
      this._lap.setExtensionInstalled(true);
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run()
          throws IOException
        {
          ExtensionInstallerServiceImpl.this._lap.store();
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      if ((localPrivilegedActionException.getException() instanceof IOException))
        LaunchErrorDialog.show(this._progress.getOwner(), (IOException)localPrivilegedActionException.getException(), false);
      else
        Trace.ignoredException(localPrivilegedActionException.getException());
    }
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        try
        {
          ExtensionInstallerServiceImpl.this._progress.handleEvent(new InitEvent(3));
          Main.systemExit(0);
        }
        catch (CancelException localCancelException)
        {
          throw new RuntimeException(localCancelException);
        }
        catch (ExitException localExitException)
        {
          Trace.println("systemExit: " + localExitException, TraceLevel.BASIC);
          Trace.ignoredException(localExitException);
        }
        return null;
      }
    });
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.ExtensionInstallerServiceImpl
 * JD-Core Version:    0.6.0
 */