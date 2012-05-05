package com.sun.javaws.ui;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadException;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.ConsoleController;
import com.sun.deploy.uitoolkit.ui.ConsoleWindow;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.deploy.util.DeploySysAction;
import com.sun.deploy.util.DeploySysRun;
import com.sun.deploy.util.PerfLogger;
import com.sun.javaws.Globals;
import com.sun.javaws.Main;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.util.JavawsConsoleController;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.GeneralSecurityException;

public class LaunchErrorDialog
{
  private static String getMoreInfo(Throwable paramThrowable)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    String str1 = "<split>";
    JNLPException localJNLPException = null;
    DownloadException localDownloadException = null;
    Throwable localThrowable = null;
    if ((paramThrowable instanceof JNLPException))
    {
      localJNLPException = (JNLPException)paramThrowable;
      localThrowable = localJNLPException.getWrappedException();
    }
    else if ((paramThrowable instanceof DownloadException))
    {
      localDownloadException = (DownloadException)paramThrowable;
      localThrowable = localDownloadException.getWrappedException();
    }
    else if ((paramThrowable instanceof ExitException))
    {
      localThrowable = ((ExitException)paramThrowable).getException();
    }
    localStringBuffer.append(getErrorDescription(paramThrowable));
    String str2 = null;
    String str3 = null;
    if (localJNLPException != null)
    {
      str2 = localJNLPException.getLaunchDescSource();
      if (str2 == null)
      {
        localObject1 = JNLPException.getDefaultLaunchDesc();
        if (localObject1 != null)
          str2 = ((LaunchDesc)localObject1).getSource();
      }
    }
    else if (JNLPException.getDefaultLaunchDesc() != null)
    {
      str2 = JNLPException.getDefaultLaunchDesc().getSource();
    }
    if (JNLPException.getDefaultLaunchDesc() != null)
      str3 = JNLPException.getDefaultLaunchDesc().getSource();
    if ((str3 != null) && (str3.equals(str2)))
      str3 = null;
    if (str2 != null)
    {
      localStringBuffer.append(str1);
      localStringBuffer.append(ResourceManager.getString("launcherrordialog.jnlpTab"));
      localStringBuffer.append(str1);
      localStringBuffer.append(filter(str2));
    }
    if (str3 != null)
    {
      localStringBuffer.append(str1);
      localStringBuffer.append(ResourceManager.getString("launcherrordialog.jnlpMainTab"));
      localStringBuffer.append(str1);
      localStringBuffer.append(filter(str3));
    }
    if (paramThrowable != null)
    {
      localObject1 = new StringWriter();
      localObject2 = new PrintWriter((Writer)localObject1);
      paramThrowable.printStackTrace((PrintWriter)localObject2);
      localStringBuffer.append(str1);
      localStringBuffer.append(ResourceManager.getString("launcherrordialog.exceptionTab"));
      localStringBuffer.append(str1);
      localStringBuffer.append(((StringWriter)localObject1).toString());
    }
    if (localThrowable != null)
    {
      localObject1 = new StringWriter();
      localObject2 = new PrintWriter((Writer)localObject1);
      localThrowable.printStackTrace((PrintWriter)localObject2);
      localStringBuffer.append(str1);
      localStringBuffer.append(ResourceManager.getString("launcherrordialog.wrappedExceptionTab"));
      localStringBuffer.append(str1);
      localStringBuffer.append(((StringWriter)localObject1).toString());
    }
    Object localObject1 = JavawsConsoleController.getInstance();
    Object localObject2 = ToolkitStore.getUI().getConsole((ConsoleController)localObject1);
    ((JavawsConsoleController)localObject1).setConsole((ConsoleWindow)localObject2);
    if (localObject2 != null)
    {
      localStringBuffer.append(str1);
      localStringBuffer.append(ResourceManager.getString("launcherrordialog.consoleTab"));
      try
      {
        localStringBuffer.append(str1);
        localStringBuffer.append(((ConsoleWindow)localObject2).getRecentLog());
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
    }
    return (String)(String)localStringBuffer.toString();
  }

  private static String filter(String paramString)
  {
    if (paramString.length() > 10240)
      return paramString.substring(0, 10239) + "\njnlp file truncated after 10K\n";
    return paramString;
  }

  public static void show(Object paramObject, Throwable paramThrowable, boolean paramBoolean)
  {
    SplashScreen.hide();
    System.err.println("#### Java Web Start Error:");
    System.err.println("#### " + getMessage(paramThrowable));
    if (Config.getDeployDebug())
      paramThrowable.printStackTrace();
    int i = (!Globals.TCKHarnessRun) && (!Globals.isSilentMode()) ? 1 : 0;
    if (((paramThrowable instanceof ExitException)) && (((ExitException)paramThrowable).getReason() == 0))
      i = 0;
    if (i != 0)
    {
      1 local1 = new DeploySysAction(paramThrowable, paramObject)
      {
        private final Throwable val$throwable;
        private final Object val$owner;

        public Object execute()
        {
          Throwable localThrowable = this.val$throwable;
          try
          {
            String str1 = null;
            String str2 = LaunchErrorDialog.access$000(this.val$throwable);
            PerfLogger.setTime("showing error dialog");
            PerfLogger.outputLog();
            if ((this.val$throwable instanceof JNLPException))
            {
              str1 = ((JNLPException)this.val$throwable).getBriefMessage();
            }
            else if ((this.val$throwable instanceof ExitException))
            {
              localObject = (ExitException)this.val$throwable;
              if (((ExitException)localObject).getReason() == 6)
              {
                str1 = ((ExitException)localObject).getMessage();
                localThrowable = ((ExitException)localObject).getException();
              }
            }
            if (str1 == null)
              if (Environment.isImportMode())
              {
                if (Environment.isInstallMode())
                  str1 = ResourceManager.getString("launcherrordialog.uninstall.brief.message");
                else
                  str1 = ResourceManager.getString("launcherrordialog.import.brief.message");
              }
              else
                str1 = ResourceManager.getString("launcherrordialog.brief.message");
            Object localObject = LaunchErrorDialog.access$100() == null ? new AppInfo() : LaunchErrorDialog.access$100().getAppInfo();
            String str3 = ResourceManager.getString("launcherrordialog.brief.ok");
            String str4 = ResourceManager.getString("launcherrordialog.brief.details");
            String str5 = ResourceManager.getString("error.default.title", str2);
            ToolkitStore.getUI();
            ToolkitStore.getUI().showMessageDialog(this.val$owner, (AppInfo)localObject, 0, str5, null, str1, LaunchErrorDialog.access$200(localThrowable), str3, str4, null);
          }
          catch (Exception localException)
          {
            Trace.ignored(localException);
          }
          return null;
        }
      };
      DeploySysRun.execute(local1, null);
    }
    if (paramBoolean)
      try
      {
        Main.systemExit(-1);
      }
      catch (ExitException localExitException)
      {
        Trace.println("systemExit: " + localExitException, TraceLevel.BASIC);
        Trace.ignoredException(localExitException);
      }
  }

  private static String getErrorCategory(Throwable paramThrowable)
  {
    String str = ResourceManager.getString("launch.error.category.unexpected");
    if ((paramThrowable instanceof JNLPException))
    {
      JNLPException localJNLPException = (JNLPException)paramThrowable;
      str = localJNLPException.getCategory();
    }
    else if (((paramThrowable instanceof SecurityException)) || ((paramThrowable instanceof GeneralSecurityException)))
    {
      str = ResourceManager.getString("launch.error.category.security");
    }
    else if ((paramThrowable instanceof OutOfMemoryError))
    {
      str = ResourceManager.getString("launch.error.category.memory");
    }
    else if ((paramThrowable instanceof DownloadException))
    {
      str = ResourceManager.getString("launch.error.category.download");
    }
    return str;
  }

  private static String getErrorDescription(Throwable paramThrowable)
  {
    String str = getMessage(paramThrowable);
    if (str == null)
      str = ResourceManager.getString("launcherrordialog.genericerror", paramThrowable.getClass().getName());
    return str;
  }

  private static String getMessage(Throwable paramThrowable)
  {
    if ((paramThrowable instanceof Exception))
      return paramThrowable.getMessage();
    return paramThrowable.getClass().getName() + ": " + paramThrowable.getMessage();
  }

  private static LaunchDesc getLaunchDesc()
  {
    return JNLPException.getDefaultLaunchDesc();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.LaunchErrorDialog
 * JD-Core Version:    0.6.0
 */