package com.sun.deploy.uitoolkit.impl.awt.ui;

import com.sun.applet2.preloader.CancelException;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.AppInitEvent;
import com.sun.applet2.preloader.event.ConfigEvent;
import com.sun.applet2.preloader.event.DownloadEvent;
import com.sun.applet2.preloader.event.ErrorEvent;
import com.sun.applet2.preloader.event.InitEvent;
import com.sun.applet2.preloader.event.PreloaderEvent;
import com.sun.deploy.Environment;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.ui.ProgressDialog;
import com.sun.deploy.ui.UIFactory;
import com.sun.deploy.util.DeploySysAction;
import com.sun.deploy.util.DeploySysRun;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.TimerTask;
import javax.swing.SwingUtilities;

public class DownloadWindow extends Preloader
  implements ActionListener
{
  private ProgressDialog progressDialog = null;
  String titleMsg = null;
  String headingMsg = null;
  private int answer = -1;
  private boolean isVisible = false;
  private boolean isInstaller = false;
  private boolean allowVisible = false;
  private int percentComplete = 0;
  private URL currentUrl = null;
  private boolean isCanceled = false;
  private boolean exitOnCancel = false;
  private String statusString = null;
  private static final int TIMER_UPDATE_RATE = 1500;
  private static final int TIMER_INITIAL_DELAY = 8000;
  private static final int TIMER_RECENT_SIZE = 10;
  static final int DELAY_TO_MAKE_VISIBLE = 2000;
  private boolean showOnDelayEnabled = false;
  java.util.Timer delayTimer = null;
  javax.swing.Timer timerObject = null;
  private int[] timerDownloadPercents = new int[10];
  private int timerCount = 0;
  private int timerLastPercent = 0;
  private boolean timerOn = false;
  private String pendingHeading;
  WindowAdapter closeListener = new WindowAdapter()
  {
    public void windowClosing(WindowEvent paramWindowEvent)
    {
      DownloadWindow.this.cancelAction();
      DownloadWindow.this.resetCanceled();
    }
  };
  private boolean hideOnDownloadCompletion = false;
  private volatile boolean lazyInitialize = false;
  private Component owner;
  private AppInfo ainfo;
  private boolean includeOk;

  private void showOnDelay(boolean paramBoolean)
  {
    if (paramBoolean == this.showOnDelayEnabled)
      return;
    if (paramBoolean)
    {
      synchronized (this)
      {
        this.delayTimer = new java.util.Timer();
      }
      ??? = new TimerTask()
      {
        public void run()
        {
          DownloadWindow.this.cancelTimer();
          if (!UIFactory.hasVisibleDialogs())
          {
            DownloadWindow.this.setVisible(true);
          }
          else
          {
            DownloadWindow.this.showOnDelay(false);
            DownloadWindow.this.showOnDelay(true);
          }
        }
      };
      this.delayTimer.schedule((TimerTask)???, 2000L);
      this.showOnDelayEnabled = true;
    }
    else
    {
      cancelTimer();
      this.showOnDelayEnabled = false;
    }
  }

  public synchronized Object getOwner()
  {
    Object[] arrayOfObject = new Object[1];
    arrayOfObject[0] = null;
    invokeLater(new Runnable(arrayOfObject)
    {
      private final Object[] val$ret;

      public void run()
      {
        if (DownloadWindow.this.progressDialog != null)
          this.val$ret[0] = DownloadWindow.access$400(DownloadWindow.this).getDialog();
      }
    });
    return arrayOfObject[0];
  }

  public boolean handleEvent(PreloaderEvent paramPreloaderEvent)
    throws CancelException
  {
    Object localObject;
    switch (paramPreloaderEvent.getType())
    {
    case 2:
      ConfigEvent localConfigEvent = (ConfigEvent)paramPreloaderEvent;
      switch (localConfigEvent.getAction())
      {
      case 1:
        setStatus((String)localConfigEvent.getValue());
        break;
      case 2:
        setHeading((String)localConfigEvent.getValue(), true);
        break;
      case 3:
        localObject = (AppInfo)localConfigEvent.getValue();
        initialize(null, (AppInfo)localObject, true, false);
        break;
      case 4:
        setVisible(((Boolean)localConfigEvent.getValue()).booleanValue());
        break;
      case 6:
        setVisible(false);
        break;
      case 5:
        if (this.progressDialog == null)
          break;
        this.progressDialog.showProgress(9999);
        break;
      default:
        return false;
      }
      break;
    case 3:
      showOnDelay(false);
      if (isCanceled())
        throw new CancelException("Cancelled");
      localObject = (DownloadEvent)paramPreloaderEvent;
      switch (((DownloadEvent)localObject).getDownloadType())
      {
      case 0:
        if (((DownloadEvent)localObject).getResourceLabel() != null)
          setStatus(((DownloadEvent)localObject).getResourceLabel());
        setHeading(ResourceManager.getString("progress.downloading"), true);
        progress(this.currentUrl, ((DownloadEvent)localObject).getCompletedCount(), ((DownloadEvent)localObject).getTotalCount(), ((DownloadEvent)localObject).getOverallPercentage());
        break;
      case 2:
        setHeading(ResourceManager.getString("progress.patching"), true);
        upgradingArchive(this.currentUrl, null, (int)((DownloadEvent)localObject).getCompletedCount(), ((DownloadEvent)localObject).getOverallPercentage());
        break;
      case 1:
        setHeading(ResourceManager.getString("progress.verifying"), true);
        validating(this.currentUrl, null, ((DownloadEvent)localObject).getCompletedCount(), ((DownloadEvent)localObject).getTotalCount(), ((DownloadEvent)localObject).getOverallPercentage());
        break;
      default:
        return false;
      }
    case 1:
      InitEvent localInitEvent = (InitEvent)paramPreloaderEvent;
      this.hideOnDownloadCompletion = false;
      switch (localInitEvent.getInitType())
      {
      case 2:
        this.titleMsg = "progress.title.installer";
        this.headingMsg = "progress.launching";
        setAllowVisible(true);
        setVisible(true);
        this.isInstaller = true;
        break;
      case 0:
        this.titleMsg = "progress.download.jre";
        this.headingMsg = "progress.downloading";
        setAllowVisible(true);
        setVisible(true);
        this.isInstaller = true;
        break;
      case 1:
        this.hideOnDownloadCompletion = true;
        this.titleMsg = "progress.title.app";
        this.headingMsg = "progress.downloading";
        setAllowVisible(true);
        setVisible(true);
        startTimer();
        break;
      case 4:
        this.titleMsg = "progress.title.app";
        this.headingMsg = "progress.downloading";
        setAllowVisible(true);
        showOnDelay(true);
        break;
      case 5:
        disposeWindow();
        break;
      case 3:
      default:
        return false;
      }
    case 6:
      ErrorEvent localErrorEvent = (ErrorEvent)paramPreloaderEvent;
      showOnDelay(false);
      downloadFailed(localErrorEvent.getLocation(), localErrorEvent.getValue());
      return true;
    case 4:
      showOnDelay(false);
      AppInitEvent localAppInitEvent = (AppInitEvent)paramPreloaderEvent;
      switch (localAppInitEvent.getSubtype())
      {
      case 2:
        if (!this.isInstaller)
          setVisible(false);
        else
          this.isInstaller = false;
        break;
      default:
        Trace.println("AppInitEvent that is not handled explicitly", TraceLevel.PRELOADER);
        if (!this.isInstaller)
          setVisible(false);
        return false;
      }
    case 5:
    default:
      Trace.println("DownloadWindow can not handle " + paramPreloaderEvent, TraceLevel.PRELOADER);
      return false;
    }
    return true;
  }

  private synchronized void initialize(Component paramComponent, AppInfo paramAppInfo, boolean paramBoolean1, boolean paramBoolean2)
  {
    this.isCanceled = false;
    this.exitOnCancel = paramBoolean1;
    this.includeOk = paramBoolean2;
    this.owner = paramComponent;
    this.ainfo = paramAppInfo;
    this.lazyInitialize = true;
  }

  private void lazyInitializeIfNeeded()
  {
    if (!this.lazyInitialize)
      return;
    Trace.println("Performing actual init of DownloadWindow", TraceLevel.PRELOADER);
    this.lazyInitialize = false;
    String str = ResourceManager.getString("product.javaws.name", "");
    if (this.progressDialog == null)
      this.progressDialog = UIFactory.createProgressDialog(this.ainfo, this.owner, str, null, this.includeOk);
    else
      this.progressDialog.reset(this.ainfo, str, this.includeOk);
    if (this.progressDialog != null)
      this.progressDialog.addWindowListener(this.closeListener);
  }

  private void startTimer()
  {
    this.timerObject = new javax.swing.Timer(1500, this);
    this.timerCount = 0;
    this.timerObject.start();
  }

  private void setStatus(String paramString)
  {
    this.statusString = paramString;
    setStatusStringText(paramString);
  }

  private void setEstimatedTime(String paramString)
  {
    if ((this.statusString == null) || (this.statusString.length() == 0))
      setStatusStringText(paramString);
  }

  private void setStatusStringText(String paramString)
  {
    4 local4 = new Runnable(paramString)
    {
      private final String val$text;

      public void run()
      {
        if (DownloadWindow.this.progressDialog != null)
          DownloadWindow.this.progressDialog.setProgressStatusText(this.val$text);
      }
    };
    invokeLater(local4);
  }

  private void setHeading(String paramString, boolean paramBoolean)
  {
    5 local5 = new Runnable(paramString, paramBoolean)
    {
      private final String val$text;
      private final boolean val$singleLine;

      public void run()
      {
        if (DownloadWindow.this.progressDialog != null)
          DownloadWindow.this.progressDialog.setMasthead(this.val$text == null ? " " : this.val$text, this.val$singleLine);
      }
    };
    this.pendingHeading = null;
    invokeLater(local5);
  }

  private void setHeadingLater(String paramString)
  {
    if ((this.timerObject != null) && (this.timerObject.isRunning()))
      this.pendingHeading = paramString;
    else
      setHeading(paramString, true);
  }

  private void clearWindow()
  {
    if (SwingUtilities.isEventDispatchThread())
      clearWindowHelper();
    else
      try
      {
        SwingUtilities.invokeAndWait(new Runnable()
        {
          public void run()
          {
            DownloadWindow.this.clearWindowHelper();
          }
        });
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
  }

  private void clearWindowHelper()
  {
    reset();
  }

  void disposeWindow()
  {
    invokeLater(new Runnable()
    {
      public void run()
      {
        if (DownloadWindow.this.progressDialog != null)
        {
          DownloadWindow.this.clearWindow();
          DownloadWindow.access$702(DownloadWindow.this, false);
          DownloadWindow.this.progressDialog.removeWindowListener(DownloadWindow.this.closeListener);
          DownloadWindow.this.setVisible(false);
          DownloadWindow.access$402(DownloadWindow.this, null);
        }
      }
    });
  }

  private void reset()
  {
    stopTimer();
    invokeLater(new Runnable()
    {
      public void run()
      {
        if (DownloadWindow.this.progressDialog != null)
        {
          DownloadWindow.this.progressDialog.setMasthead("", true);
          DownloadWindow.this.progressDialog.showProgress(9999);
          DownloadWindow.this.progressDialog.setProgressStatusText(null);
        }
      }
    });
  }

  private void showDownloadWindow()
  {
    if ((this.allowVisible == true) && (!isVisible()))
      setVisible(true);
  }

  private void progress(URL paramURL, long paramLong1, long paramLong2, int paramInt)
  {
    if (paramInt != 100)
      showDownloadWindow();
    this.timerOn = true;
    this.percentComplete = paramInt;
    if ((paramURL != this.currentUrl) && (paramURL != null))
    {
      setHeadingLater(ResourceManager.getString("progress.downloading"));
      this.currentUrl = paramURL;
    }
    this.percentComplete = paramInt;
    invokeLater(new Runnable(paramLong2, paramInt)
    {
      private final long val$totalSize;
      private final int val$percent;

      public void run()
      {
        if (DownloadWindow.this.progressDialog != null)
          if (this.val$totalSize == -1L)
            DownloadWindow.this.progressDialog.showProgress(9999);
          else
            DownloadWindow.this.progressDialog.showProgress(this.val$percent);
      }
    });
  }

  private void upgradingArchive(URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    if (paramInt2 != 100)
      showDownloadWindow();
    if ((this.currentUrl != paramURL) || (paramInt1 == 0))
    {
      if (this.pendingHeading == null)
        setHeadingLater(ResourceManager.getString("progress.patching"));
      this.currentUrl = paramURL;
    }
  }

  private void validating(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
  {
    if (paramInt != 100)
      showDownloadWindow();
    else if (this.hideOnDownloadCompletion)
      setVisible(false);
    if ((this.currentUrl != paramURL) || (paramLong1 == 0L))
    {
      if (this.pendingHeading == null)
        setHeadingLater(ResourceManager.getString("progress.verifying"));
      this.currentUrl = paramURL;
    }
  }

  private void downloadFailed(URL paramURL, String paramString)
  {
    stopTimer();
    setHeading(ResourceManager.getString("progress.download.failed"), true);
    invokeLater(new Runnable()
    {
      public void run()
      {
        if (DownloadWindow.this.progressDialog != null)
          DownloadWindow.this.progressDialog.showProgress(9999);
      }
    });
    setVisible(false);
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    if (this.pendingHeading != null)
      setHeading(this.pendingHeading, true);
    if ((!this.timerOn) || (this.percentComplete <= 0))
      return;
    this.timerCount += 1;
    int i = this.timerCount * 1500;
    int j = this.percentComplete - this.timerLastPercent;
    this.timerLastPercent = this.percentComplete;
    this.timerDownloadPercents[(this.timerCount % 10)] = j;
    if (i > 8000)
    {
      int k = 0;
      int m = Math.min(10, this.timerCount);
      for (int n = 0; n < m; n++)
        k += this.timerDownloadPercents[n];
      if (this.percentComplete == 100)
      {
        setEstimatedTime("");
      }
      else if ((k != 0) && (this.percentComplete > 0))
      {
        n = i / 1000 * (100 - this.percentComplete) / this.percentComplete;
        int i1 = n / 60;
        int i2 = n - i1 * 60;
        String str1;
        String str2;
        if (i1 > 0)
        {
          if (i1 == 1)
          {
            if (i2 == 1)
              str1 = "progress.time.left.minute.second";
            else
              str1 = "progress.time.left.minute.seconds";
          }
          else if (i2 == 1)
            str1 = "progress.time.left.minutes.second";
          else
            str1 = "progress.time.left.minutes.seconds";
          str2 = ResourceManager.getString(str1, "" + i1, "" + i2);
        }
        else
        {
          if (i2 == 1)
            str1 = "progress.time.left.second";
          else
            str1 = "progress.time.left.seconds";
          str2 = ResourceManager.getString(str1, "" + i2);
        }
        setEstimatedTime(str2);
      }
    }
  }

  private void stopTimer()
  {
    this.timerOn = false;
    if (this.timerObject != null)
    {
      this.timerObject.stop();
      this.timerObject = null;
    }
    if (this.pendingHeading != null)
      setHeading(this.pendingHeading, true);
  }

  private synchronized void cancelTimer()
  {
    if (this.delayTimer != null)
    {
      this.delayTimer.cancel();
      this.delayTimer = null;
    }
  }

  synchronized void cancelAction()
  {
    setVisible(false);
    if (this.exitOnCancel)
      AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          try
          {
            DownloadWindow.access$800(-1);
          }
          catch (Exception localException)
          {
            Trace.println("systemExit: " + localException, TraceLevel.BASIC);
            Trace.ignoredException(localException);
          }
          return null;
        }
      });
    else
      this.isCanceled = true;
  }

  private static void systemExit(int paramInt)
    throws Exception
  {
    Trace.flush();
    if (Environment.isJavaPlugin())
    {
      RuntimeException localRuntimeException = new RuntimeException("exit(" + paramInt + ")");
      throw localRuntimeException;
    }
    System.exit(paramInt);
  }

  private boolean isCanceled()
  {
    return this.isCanceled;
  }

  private synchronized void resetCanceled()
  {
    this.isCanceled = false;
  }

  private void setAllowVisible(boolean paramBoolean)
  {
    this.allowVisible = paramBoolean;
  }

  private void setVisible(boolean paramBoolean)
  {
    if (!paramBoolean)
    {
      this.exitOnCancel = false;
      stopTimer();
    }
    showOnDelay(false);
    if (paramBoolean != this.isVisible)
    {
      this.isVisible = paramBoolean;
      invokeLater(new Runnable(paramBoolean)
      {
        private final boolean val$show;

        public void run()
        {
          if (this.val$show)
          {
            DownloadWindow.this.lazyInitializeIfNeeded();
            if (DownloadWindow.this.progressDialog != null)
            {
              DownloadWindow.this.progressDialog.setTitle(ResourceManager.getString(DownloadWindow.this.titleMsg));
              DownloadWindow.this.setHeading(ResourceManager.getString(DownloadWindow.this.headingMsg), true);
              UIFactory.showProgressDialog(DownloadWindow.this.progressDialog);
              DownloadWindow.access$1102(DownloadWindow.this, DownloadWindow.this.progressDialog.getUserAnswer());
              if (DownloadWindow.this.answer == 1)
                DownloadWindow.this.cancelAction();
            }
          }
          else if (DownloadWindow.this.progressDialog != null)
          {
            UIFactory.hideProgressDialog(DownloadWindow.this.progressDialog);
          }
        }
      });
    }
    else if (!paramBoolean)
    {
      invokeLater(new Runnable()
      {
        public void run()
        {
          if (DownloadWindow.this.progressDialog != null)
            UIFactory.hideProgressDialog(DownloadWindow.this.progressDialog);
        }
      });
      showOnDelay(false);
    }
  }

  boolean isVisible()
  {
    return this.isVisible;
  }

  private void invokeLater(Runnable paramRunnable)
  {
    if (paramRunnable == null)
      return;
    try
    {
      DeploySysRun.execute(new DeploySysAction(paramRunnable)
      {
        private final Runnable val$r;

        public Object execute()
        {
          SwingUtilities.invokeLater(this.val$r);
          return null;
        }
      });
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.awt.ui.DownloadWindow
 * JD-Core Version:    0.6.0
 */