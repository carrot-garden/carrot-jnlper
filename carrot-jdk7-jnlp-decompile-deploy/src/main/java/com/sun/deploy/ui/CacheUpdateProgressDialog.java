package com.sun.deploy.ui;

import com.sun.deploy.Environment;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import javax.swing.SwingUtilities;

public class CacheUpdateProgressDialog
{
  private static ProgressDialog dialog = null;
  private static boolean systemCache = false;
  private static final int TIMER_INITIAL_DELAY = 8000;
  private static String JAVA_VERSION = "6";
  private static int currentPercent = -1;
  private static long startTime = -1L;

  public static void dismiss()
  {
    if (dialog != null)
    {
      ProgressDialog localProgressDialog = dialog;
      SwingUtilities.invokeLater(new Runnable(localProgressDialog)
      {
        private final ProgressDialog val$pd;

        public void run()
        {
          this.val$pd.setVisible(false);
        }
      });
    }
    dialog = null;
  }

  public static void setSystemCache(boolean paramBoolean)
  {
    systemCache = paramBoolean;
  }

  public static void showProgress(int paramInt1, int paramInt2)
    throws CacheUpdateProgressDialog.CanceledException
  {
    String str1 = null;
    int i = 100 * paramInt1 / (paramInt2 > 0 ? paramInt2 : 1);
    if (currentPercent == -1)
    {
      currentPercent = i;
      startTime = System.currentTimeMillis();
    }
    else if (currentPercent != i)
    {
      currentPercent = i;
      long l1 = System.currentTimeMillis();
      long l2 = l1 - startTime;
      long l3 = l2 * 100L / i;
      int j = (int)((l3 - l2) / 1000L);
      int k = j / 60;
      int m = j - k * 60;
      String str5;
      if (k > 0)
      {
        if (k == 1)
        {
          if (m == 1)
            str5 = "progress.time.left.minute.second";
          else
            str5 = "progress.time.left.minute.seconds";
        }
        else if (m == 1)
          str5 = "progress.time.left.minutes.second";
        else
          str5 = "progress.time.left.minutes.seconds";
        str1 = ResourceManager.getString(str5, "" + k, "" + m);
      }
      else
      {
        if ((m == 1) || (m == 0))
          str5 = "progress.time.left.second";
        else
          str5 = "progress.time.left.seconds";
        str1 = ResourceManager.getString(str5, "" + m);
      }
    }
    if (dialog == null)
    {
      AppInfo localAppInfo = new AppInfo();
      String str2 = ResourceManager.getString(Environment.isJavaPlugin() ? "cache.upgrade.title.javapi" : "cache.upgrade.title.javaws");
      String str3 = ResourceManager.getString(Environment.isJavaPlugin() ? "cache.upgrade.masthead.javapi" : "cache.upgrade.masthead.javaws");
      String str4 = ResourceManager.getString(Environment.isJavaPlugin() ? "cache.upgrade.message.javapi" : "cache.upgrade.message.javaws");
      dialog = UIFactory.createProgressDialog(localAppInfo, null, str2, str4, false);
      dialog.setMasthead(str3, true);
      ProgressDialog localProgressDialog = dialog;
      try
      {
        SwingUtilities.invokeLater(new Runnable(localProgressDialog)
        {
          private final ProgressDialog val$pd;

          public void run()
          {
            UIFactory.showProgressDialog(this.val$pd);
          }
        });
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
    }
    else if (dialog.getUserAnswer() == 1)
    {
      throw new CanceledException();
    }
    if ((str1 != null) && (System.currentTimeMillis() >= startTime + 8000L))
      dialog.setProgressStatusText(str1);
    dialog.progress(i);
  }

  public static class CanceledException extends Exception
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.CacheUpdateProgressDialog
 * JD-Core Version:    0.6.0
 */