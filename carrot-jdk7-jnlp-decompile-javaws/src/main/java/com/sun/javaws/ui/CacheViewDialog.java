package com.sun.javaws.ui;

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
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class CacheViewDialog extends WindowAdapter
  implements ActionListener
{
  private ProgressDialog progressDialog = null;
  private int answer = -1;
  private boolean isVisible = false;
  private boolean allowVisible = false;
  private AppInfo ainfo = null;
  private int percentComplete = 0;
  private URL currentUrl = null;
  private boolean isCanceled = false;
  private boolean exitOnCancel = false;
  private boolean includeOk = false;
  private String statusString = null;
  private static final int TIMER_UPDATE_RATE = 1500;
  private static final int TIMER_INITIAL_DELAY = 8000;
  private static final int TIMER_RECENT_SIZE = 10;
  Timer timerObject = null;
  private int[] timerDownloadPercents = new int[10];
  private int timerCount = 0;
  private int timerLastPercent = 0;
  private boolean timerOn = false;
  private String pendingHeading;

  public void initialize(Object paramObject, AppInfo paramAppInfo, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    this.isCanceled = false;
    this.exitOnCancel = paramBoolean1;
    this.includeOk = paramBoolean2;
    if (this.ainfo != null)
    {
      this.ainfo = paramAppInfo;
    }
    else
    {
      this.ainfo = new AppInfo();
      this.ainfo.setTitle(" ");
      if (paramBoolean3)
        this.ainfo.setVendor(" ");
    }
    String str = ResourceManager.getString("product.javaws.name", "");
    if (this.progressDialog == null)
      this.progressDialog = UIFactory.createProgressDialog(this.ainfo, (Component)paramObject, str, null, paramBoolean2);
    else
      this.progressDialog.reset(this.ainfo, str, paramBoolean2);
    this.progressDialog.addWindowListener(this);
  }

  public void initialize(Object paramObject, boolean paramBoolean)
  {
    initialize(paramObject, null, false, false, paramBoolean);
  }

  public Component getOwner()
  {
    if (this.progressDialog == null)
      return null;
    return this.progressDialog.getDialog();
  }

  public void showLoadingProgressScreen()
  {
    setHeading(ResourceManager.getString("progress.downloading"), true);
    this.timerObject = new Timer(1500, this);
    this.timerCount = 0;
    this.timerObject.start();
  }

  public void setStatus(String paramString)
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
    1 local1 = new Runnable(paramString)
    {
      private final String val$text;

      public void run()
      {
        if (CacheViewDialog.this.progressDialog != null)
          CacheViewDialog.this.progressDialog.setProgressStatusText(this.val$text);
      }
    };
    invokeLater(local1);
  }

  public void setHeading(String paramString, boolean paramBoolean)
  {
    2 local2 = new Runnable(paramString, paramBoolean)
    {
      private final String val$text;
      private final boolean val$singleLine;

      public void run()
      {
        if (CacheViewDialog.this.progressDialog != null)
          CacheViewDialog.this.progressDialog.setMasthead(this.val$text == null ? " " : this.val$text, this.val$singleLine);
      }
    };
    this.pendingHeading = null;
    invokeLater(local2);
  }

  public void setHeadingLater(String paramString)
  {
    if ((this.timerObject != null) && (this.timerObject.isRunning()))
      this.pendingHeading = paramString;
    else
      setHeading(paramString, true);
  }

  public void setProgressBarVisible(boolean paramBoolean)
  {
    invokeLater(new Runnable(paramBoolean)
    {
      private final boolean val$isVisible;

      public void run()
      {
        if (CacheViewDialog.this.progressDialog != null)
        {
          int i = this.val$isVisible ? 0 : 9999;
          CacheViewDialog.this.progressDialog.showProgress(i);
        }
      }
    });
  }

  public void setProgressBarValue(int paramInt)
  {
    invokeLater(new Runnable(paramInt)
    {
      private final int val$value;

      public void run()
      {
        if (CacheViewDialog.this.progressDialog != null)
          CacheViewDialog.this.progressDialog.showProgress(this.val$value);
      }
    });
  }

  public void setIndeterminate(boolean paramBoolean)
  {
    invokeLater(new Runnable(paramBoolean)
    {
      private final boolean val$value;

      public void run()
      {
        if (CacheViewDialog.this.progressDialog != null)
          CacheViewDialog.this.progressDialog.setIndeterminate(this.val$value);
      }
    });
  }

  public void showLaunchingApplication(String paramString)
  {
    ProgressDialog localProgressDialog = this.progressDialog;
    if (localProgressDialog != null)
      invokeLater(new Runnable(localProgressDialog, paramString)
      {
        private final ProgressDialog val$pd;
        private final String val$title;

        public void run()
        {
          this.val$pd.setTitle(this.val$title);
          this.val$pd.setMasthead(ResourceManager.getString("progress.launching"), true);
          this.val$pd.showProgress(100);
        }
      });
  }

  public void clearWindow()
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
            CacheViewDialog.this.clearWindowHelper();
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

  public void disposeWindow()
  {
    if (this.progressDialog != null)
    {
      clearWindow();
      this.exitOnCancel = false;
      this.progressDialog.removeWindowListener(this);
      setVisible(false);
      this.progressDialog = null;
    }
  }

  public void reset()
  {
    ProgressDialog localProgressDialog = this.progressDialog;
    if (localProgressDialog != null)
    {
      stopTimer();
      invokeLater(new Runnable(localProgressDialog)
      {
        private final ProgressDialog val$d;

        public void run()
        {
          this.val$d.setMasthead("", true);
          this.val$d.showProgress(9999);
          this.val$d.setProgressStatusText(null);
        }
      });
    }
  }

  public void setTitle(String paramString)
  {
    invokeLater(new Runnable(paramString)
    {
      private final String val$title;

      public void run()
      {
        if (CacheViewDialog.this.progressDialog != null)
          CacheViewDialog.this.progressDialog.setTitle(this.val$title);
      }
    });
  }

  private void showDownloadWindow()
  {
    if ((this.allowVisible == true) && (!isVisible()))
      setVisible(true);
  }

  public void progress(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
  {
    showDownloadWindow();
    this.timerOn = true;
    this.percentComplete = paramInt;
    if ((paramURL != this.currentUrl) && (paramURL != null))
    {
      setHeadingLater(ResourceManager.getString("progress.downloading"));
      this.currentUrl = paramURL;
    }
    this.percentComplete = paramInt;
    if (this.progressDialog != null)
      if (paramLong2 == -1L)
        this.progressDialog.showProgress(9999);
      else
        this.progressDialog.showProgress(paramInt);
  }

  public void upgradingArchive(URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    showDownloadWindow();
    if ((this.currentUrl != paramURL) || (paramInt1 == 0))
    {
      if (this.pendingHeading == null)
        setHeadingLater(ResourceManager.getString("progress.patching"));
      this.currentUrl = paramURL;
    }
  }

  public void validating(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
  {
    showDownloadWindow();
    if ((this.currentUrl != paramURL) || (paramLong1 == 0L))
    {
      if (this.pendingHeading == null)
        setHeadingLater(ResourceManager.getString("progress.verifying"));
      this.currentUrl = paramURL;
    }
  }

  public void downloadFailed(URL paramURL, String paramString)
  {
    stopTimer();
    setHeading(ResourceManager.getString("progress.download.failed"), true);
    if (this.progressDialog != null)
      this.progressDialog.showProgress(9999);
  }

  public void extensionDownload(String paramString, int paramInt)
  {
  }

  public void jreDownload(String paramString, URL paramURL)
  {
    setHeading(ResourceManager.getString("progress.download.jre", paramString), true);
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
      this.timerObject.stop();
    if (this.pendingHeading != null)
      setHeading(this.pendingHeading, true);
  }

  public void windowClosing(WindowEvent paramWindowEvent)
  {
    cancelAction();
    resetCancled();
  }

  private void cancelAction()
  {
    this.isVisible = false;
    if (this.exitOnCancel)
      AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          try
          {
            CacheViewDialog.access$200(-1);
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

  public boolean isCanceled()
  {
    return this.isCanceled;
  }

  public void resetCancled()
  {
    this.isCanceled = false;
  }

  public void setAllowVisible(boolean paramBoolean)
  {
    this.allowVisible = paramBoolean;
  }

  public void setVisible(boolean paramBoolean)
  {
    ProgressDialog localProgressDialog = this.progressDialog;
    if (!paramBoolean)
    {
      this.exitOnCancel = false;
      stopTimer();
    }
    if ((localProgressDialog != null) && (paramBoolean != this.isVisible))
    {
      this.isVisible = paramBoolean;
      invokeLater(new Runnable(paramBoolean, localProgressDialog)
      {
        private final boolean val$show;
        private final ProgressDialog val$d;

        public void run()
        {
          if (this.val$show)
          {
            UIFactory.showProgressDialog(this.val$d);
            CacheViewDialog.access$302(CacheViewDialog.this, this.val$d.getUserAnswer());
            if (CacheViewDialog.this.answer == 1)
              CacheViewDialog.this.cancelAction();
          }
          else
          {
            UIFactory.hideProgressDialog(this.val$d);
          }
        }
      });
    }
    else if ((localProgressDialog != null) && (!paramBoolean))
    {
      UIFactory.hideProgressDialog(localProgressDialog);
    }
  }

  public boolean isVisible()
  {
    return (this.progressDialog != null) && (this.isVisible);
  }

  public void setApplication(String paramString1, String paramString2, String paramString3)
  {
    this.progressDialog.setApplication(paramString1, paramString2, paramString3);
  }

  public int showConfirmDialog(AppInfo paramAppInfo, String paramString1, String paramString2)
  {
    initialize(null, paramAppInfo, false, true, true);
    this.progressDialog.setMasthead(paramString1, false);
    setVisible(true);
    do
      try
      {
        Thread.sleep(1000L);
      }
      catch (InterruptedException localInterruptedException1)
      {
      }
    while (!isVisible());
    do
      try
      {
        Thread.sleep(1000L);
      }
      catch (InterruptedException localInterruptedException2)
      {
      }
    while ((this.progressDialog.getUserAnswer() == -1) && (isVisible()));
    if (isVisible())
      initialize(null, paramAppInfo, true, false, true);
    return this.progressDialog.getUserAnswer();
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

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.CacheViewDialog
 * JD-Core Version:    0.6.0
 */