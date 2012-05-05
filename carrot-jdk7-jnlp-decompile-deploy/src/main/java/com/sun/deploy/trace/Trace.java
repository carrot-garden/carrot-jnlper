package com.sun.deploy.trace;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class Trace
{
  private static boolean automation = false;
  private static final List queue = new ArrayList();
  private static final Set traceListeners = new HashSet();
  private static final Set enabledTraceLevels = Collections.synchronizedSet(new HashSet());
  private static final PrintStream traceStdout = new PrintStream(new TraceStream());
  private static final PrintStream traceStderr = new PrintStream(new TraceStream());
  private static PrintStream realStdout = null;
  private static PrintStream realStderr = null;
  private static final TraceMsgQueueChecker queueChecker = new TraceMsgQueueChecker(null);

  public static void ensureMessageQueueProcessingStarted()
  {
    synchronized (queueChecker)
    {
      queueChecker.notifyAll();
    }
  }

  public static synchronized void redirectStdioStderr()
  {
    if ((null == realStdout) && (realStdout != traceStdout))
      realStdout = System.out;
    if ((null == realStderr) && (realStderr != traceStderr))
      realStderr = System.err;
    System.setOut(traceStdout);
    System.setErr(traceStderr);
  }

  public static synchronized void restoreStdioStdErr()
  {
    if ((null != realStdout) && (System.out == traceStdout))
      System.setOut(realStdout);
    if ((null != realStderr) && (System.err == traceStderr))
      System.setErr(realStderr);
    realStdout = null;
    realStderr = null;
  }

  public static synchronized PrintStream getRealStdout()
  {
    if ((null != realStdout) && (System.out == traceStdout))
      return realStdout;
    return System.out;
  }

  public static synchronized PrintStream getRealStderr()
  {
    if ((null != realStderr) && (System.err == traceStderr))
      return realStderr;
    return System.err;
  }

  public static void flush()
  {
    while (true)
      synchronized (queue)
      {
        if (queue.isEmpty())
          break;
        queue.notifyAll();
        try
        {
          queue.wait(200L);
        }
        catch (InterruptedException localInterruptedException)
        {
          return;
        }
      }
    fireFlushEvent();
  }

  public static boolean isEnabled()
  {
    return enabledTraceLevels.size() > 1;
  }

  public static void setInitialTraceLevel()
  {
    String str = Config.getStringProperty("deployment.trace.level");
    setInitialTraceLevel(str);
  }

  public static void setInitialTraceLevel(String paramString)
  {
    if ((paramString != null) && (!paramString.equals("")))
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "|");
      while (localStringTokenizer.hasMoreTokens())
      {
        String str = localStringTokenizer.nextToken();
        if (str.equalsIgnoreCase("all"))
        {
          setEnabled(TraceLevel.BASIC, true);
          setEnabled(TraceLevel.CACHE, true);
          setEnabled(TraceLevel.NETWORK, true);
          setEnabled(TraceLevel.TEMP, true);
          setEnabled(TraceLevel.SECURITY, true);
          setEnabled(TraceLevel.EXTENSIONS, true);
          setEnabled(TraceLevel.LIVECONNECT, true);
          setEnabled(TraceLevel.UI, true);
          setEnabled(TraceLevel.PRELOADER, true);
          break;
        }
        if (str.equalsIgnoreCase("basic"))
          setEnabled(TraceLevel.BASIC, true);
        else if (str.equalsIgnoreCase("cache"))
          setEnabled(TraceLevel.CACHE, true);
        else if (str.equalsIgnoreCase("net"))
          setEnabled(TraceLevel.NETWORK, true);
        else if (str.equalsIgnoreCase("temp"))
          setEnabled(TraceLevel.TEMP, true);
        else if (str.equalsIgnoreCase("security"))
          setEnabled(TraceLevel.SECURITY, true);
        else if (str.equalsIgnoreCase("ext"))
          setEnabled(TraceLevel.EXTENSIONS, true);
        else if (str.equalsIgnoreCase("liveconnect"))
          setEnabled(TraceLevel.LIVECONNECT, true);
        else if (str.equalsIgnoreCase("ui"))
          setEnabled(TraceLevel.UI, true);
        else if (str.equalsIgnoreCase("preloader"))
          setEnabled(TraceLevel.PRELOADER, true);
      }
    }
  }

  public static boolean isEnabled(TraceLevel paramTraceLevel)
  {
    return enabledTraceLevels.contains(paramTraceLevel);
  }

  public static void setEnabled(TraceLevel paramTraceLevel, boolean paramBoolean)
  {
    if (paramBoolean)
      enabledTraceLevels.add(paramTraceLevel);
    else
      enabledTraceLevels.remove(paramTraceLevel);
  }

  public static boolean isAutomationEnabled()
  {
    return automation;
  }

  public static void enableAutomation(boolean paramBoolean)
  {
    automation = paramBoolean;
  }

  private static void enQueue(TraceMessage paramTraceMessage)
  {
    synchronized (queue)
    {
      queue.add(paramTraceMessage);
      queue.notifyAll();
    }
  }

  public static void print(String paramString, TraceLevel paramTraceLevel)
  {
    if (enabledTraceLevels.contains(paramTraceLevel))
    {
      TraceMessage localTraceMessage = new TraceMessage(paramTraceLevel, paramString);
      enQueue(localTraceMessage);
    }
  }

  public static void println(String paramString, TraceLevel paramTraceLevel)
  {
    if (enabledTraceLevels.contains(paramTraceLevel))
    {
      TraceMessage localTraceMessage = new TraceMessage(paramTraceLevel, paramString, true);
      enQueue(localTraceMessage);
    }
  }

  public static void println(String paramString)
  {
    println(paramString, TraceLevel.DEFAULT);
  }

  public static void print(String paramString)
  {
    print(paramString, TraceLevel.DEFAULT);
  }

  public static void msgPrintln(String paramString, Object[] paramArrayOfObject, TraceLevel paramTraceLevel)
  {
    if (enabledTraceLevels.contains(paramTraceLevel))
    {
      String str1 = ResourceManager.getMessage(paramString);
      String str2 = MessageFormat.format(str1, paramArrayOfObject);
      TraceMessage localTraceMessage = new TraceMessage(paramTraceLevel, str2, true);
      enQueue(localTraceMessage);
    }
  }

  public static void msgPrintln(String paramString, Object[] paramArrayOfObject)
  {
    msgPrintln(paramString, paramArrayOfObject, TraceLevel.DEFAULT);
  }

  public static void msgPrintln(String paramString)
  {
    msgPrintln(paramString, null, TraceLevel.BASIC);
  }

  public static void addTraceListener(TraceListener paramTraceListener)
  {
    synchronized (traceListeners)
    {
      traceListeners.add(paramTraceListener);
    }
  }

  public static void removeTraceListener(TraceListener paramTraceListener)
  {
    synchronized (traceListeners)
    {
      traceListeners.remove(paramTraceListener);
    }
  }

  public static void clearTraceListeners()
  {
    synchronized (traceListeners)
    {
      traceListeners.clear();
    }
  }

  public static void resetTraceLevel()
  {
    enabledTraceLevels.clear();
    enabledTraceLevels.add(TraceLevel.DEFAULT);
  }

  private static void firePrintlnEvent(TraceMessage paramTraceMessage)
  {
    TraceListener[] arrayOfTraceListener;
    synchronized (traceListeners)
    {
      arrayOfTraceListener = (TraceListener[])(TraceListener[])traceListeners.toArray(new TraceListener[traceListeners.size()]);
    }
    for (int i = 0; i < arrayOfTraceListener.length; i++)
      try
      {
        arrayOfTraceListener[i].print(paramTraceMessage.getMessage());
      }
      catch (Throwable localThrowable)
      {
      }
  }

  private static void fireFlushEvent()
  {
    TraceListener[] arrayOfTraceListener;
    synchronized (traceListeners)
    {
      arrayOfTraceListener = (TraceListener[])(TraceListener[])traceListeners.toArray(new TraceListener[traceListeners.size()]);
    }
    for (int i = 0; i < arrayOfTraceListener.length; i++)
      try
      {
        arrayOfTraceListener[i].flush();
      }
      catch (Throwable localThrowable)
      {
      }
  }

  public static void ignoredException(Exception paramException)
  {
    ignored(paramException);
  }

  public static void ignored(Throwable paramThrowable)
  {
    if (enabledTraceLevels.size() > 1)
      paramThrowable.printStackTrace();
  }

  public static void ignored(Throwable paramThrowable, boolean paramBoolean)
  {
    Throwable localThrowable = paramThrowable;
    if ((paramBoolean) && (paramThrowable.getCause() != null))
      localThrowable = paramThrowable.getCause();
    ignored(localThrowable);
  }

  public static void printException(Throwable paramThrowable)
  {
    printException(null, paramThrowable);
  }

  public static void printException(Component paramComponent, Throwable paramThrowable)
  {
    printException(paramComponent, paramThrowable, null, null);
  }

  public static void printException(Throwable paramThrowable, String paramString1, String paramString2)
  {
    printException(null, paramThrowable, paramString1, paramString2);
  }

  public static void printException(Component paramComponent, Throwable paramThrowable, String paramString1, String paramString2)
  {
    printException(paramComponent, paramThrowable, paramString1, paramString2, true);
  }

  public static void printException(Component paramComponent, Throwable paramThrowable, String paramString1, String paramString2, boolean paramBoolean)
  {
    println("Ignored exception: " + paramThrowable);
    if ((paramBoolean) && (!isAutomationEnabled()))
    {
      if (paramString1 == null)
        paramString1 = ResourceManager.getMessage("dialogfactory.general_error");
      ToolkitStore.getUI().showExceptionDialog(paramComponent, null, paramThrowable, paramString2, paramString1, null, null);
    }
  }

  public static void netPrintln(String paramString)
  {
    println(paramString, TraceLevel.NETWORK);
  }

  public static void msgNetPrintln(String paramString)
  {
    msgPrintln(paramString, null, TraceLevel.NETWORK);
  }

  public static void msgNetPrintln(String paramString, Object[] paramArrayOfObject)
  {
    msgPrintln(paramString, paramArrayOfObject, TraceLevel.NETWORK);
  }

  public static void netPrintException(Throwable paramThrowable)
  {
    printException(null, paramThrowable, ResourceManager.getMessage("dialogfactory.net_error"), null, false);
  }

  public static void netPrintException(Throwable paramThrowable, String paramString1, String paramString2)
  {
    printException(null, paramThrowable, paramString1, paramString2, false);
  }

  public static void securityPrintln(String paramString)
  {
    println(paramString, TraceLevel.SECURITY);
  }

  public static void msgSecurityPrintln(String paramString)
  {
    msgPrintln(paramString, null, TraceLevel.SECURITY);
  }

  public static void msgSecurityPrintln(String paramString, Object[] paramArrayOfObject)
  {
    msgPrintln(paramString, paramArrayOfObject, TraceLevel.SECURITY);
  }

  public static void securityPrintException(Throwable paramThrowable)
  {
    printException(null, paramThrowable, ResourceManager.getMessage("dialogfactory.security_error"), null, true);
  }

  public static void securityPrintException(Throwable paramThrowable, String paramString1, String paramString2)
  {
    printException(null, paramThrowable, paramString1, paramString2, true);
  }

  public static void extPrintln(String paramString)
  {
    println(paramString, TraceLevel.EXTENSIONS);
  }

  public static void msgExtPrintln(String paramString)
  {
    msgPrintln(paramString, null, TraceLevel.EXTENSIONS);
  }

  public static void msgExtPrintln(String paramString, Object[] paramArrayOfObject)
  {
    msgPrintln(paramString, paramArrayOfObject, TraceLevel.EXTENSIONS);
  }

  public static void extPrintException(Throwable paramThrowable)
  {
    printException(null, paramThrowable, ResourceManager.getMessage("dialogfactory.ext_error"), null, true);
  }

  public static void extPrintException(Throwable paramThrowable, String paramString1, String paramString2)
  {
    printException(null, paramThrowable, paramString1, paramString2, true);
  }

  public static void liveConnectPrintln(String paramString)
  {
    println(paramString, TraceLevel.LIVECONNECT);
  }

  public static void msgLiveConnectPrintln(String paramString)
  {
    msgPrintln(paramString, null, TraceLevel.LIVECONNECT);
  }

  public static void msgLiveConnectPrintln(String paramString, Object[] paramArrayOfObject)
  {
    msgPrintln(paramString, paramArrayOfObject, TraceLevel.LIVECONNECT);
  }

  public static void liveConnectPrintException(Throwable paramThrowable)
  {
    printException(null, paramThrowable, null, null, false);
  }

  public static File createTempFile(String paramString1, String paramString2, File paramFile)
  {
    try
    {
      File[] arrayOfFile = paramFile.listFiles(new FileFilter(paramString1, paramString2)
      {
        private final String val$prefix;
        private final String val$suffix;

        public boolean accept(File paramFile)
        {
          String str = paramFile.getName();
          return (str.startsWith(this.val$prefix)) && (str.endsWith(this.val$suffix));
        }
      });
      int i = Config.getIntProperty("deployment.max.output.files");
      if ((i > 0) && (arrayOfFile.length >= i))
      {
        int j = arrayOfFile.length;
        long[] arrayOfLong = new long[j];
        for (int k = 0; k < j; k++)
          arrayOfLong[k] = arrayOfFile[k].lastModified();
        Arrays.sort(arrayOfLong);
        for (k = 0; k < arrayOfLong.length - i + 1; k++)
          arrayOfFile[k].delete();
      }
      return File.createTempFile(paramString1, paramString2, paramFile);
    }
    catch (Exception localException)
    {
      ignored(localException);
    }
    return null;
  }

  static
  {
    resetTraceLevel();
    Thread localThread = new Thread(queueChecker, "traceMsgQueueThread");
    localThread.setDaemon(true);
    localThread.start();
  }

  private static class TraceMsgQueueChecker
    implements Runnable
  {
    private TraceMsgQueueChecker()
    {
    }

    public void run()
    {
      try
      {
        synchronized (this)
        {
          wait(2000L);
        }
      }
      catch (InterruptedException localInterruptedException1)
      {
      }
      while (true)
      {
        TraceMessage localTraceMessage = null;
        synchronized (Trace.queue)
        {
          if (Trace.queue.isEmpty())
          {
            try
            {
              Trace.queue.wait();
            }
            catch (InterruptedException localInterruptedException2)
            {
              Thread.interrupted();
            }
            continue;
          }
          try
          {
            localTraceMessage = (TraceMessage)Trace.queue.remove(0);
          }
          catch (IndexOutOfBoundsException localIndexOutOfBoundsException)
          {
          }
          Trace.queue.notifyAll();
        }
        if (localTraceMessage != null)
          Trace.access$200(localTraceMessage);
      }
    }

    TraceMsgQueueChecker(Trace.1 param1)
    {
      this();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.trace.Trace
 * JD-Core Version:    0.6.0
 */