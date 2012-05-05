package com.sun.deploy.perf;

import com.sun.deploy.config.Config;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class DeployPerfUtil
{
  private static final boolean deployFirstframePerfEnabled;
  private static volatile boolean getenvSupported = true;
  private static final String DEPLOY_PERF_ENABLED = "DEPLOY_PERF_ENABLED";
  private static final String DEPLOY_PERF_LOG = "DEPLOY_PERF_LOG";
  private static final String DEFAULT_LOGNAME = "deploy_perf.log";
  private static final String DEFAULT_LOGFILE = "deploy_perf.log";
  private static final String INVALID_DEPLOY_PERF_LOG = "The DEPLOY_PERF_LOG variable must point to a file path, not a directory!";
  private static final String MISSING_PARENTS = "Failed to create the parent directories for the file specified by DEPLOY_PERF_LOG!";
  private static final File perfLog;
  private static PerfHelper helper;

  private static String getenv(String paramString)
  {
    if (getenvSupported)
      try
      {
        return System.getenv(paramString);
      }
      catch (Error localError)
      {
        getenvSupported = false;
      }
    return null;
  }

  public static boolean isDeployFirstframePerfEnabled()
  {
    return deployFirstframePerfEnabled;
  }

  public static boolean isEnabled()
  {
    return perfLog != null;
  }

  public static void put(String paramString)
  {
    if ((isEnabled() == true) && (helper != null))
      helper.put(paramString);
  }

  public static long put(long paramLong, String paramString)
  {
    if ((isEnabled() == true) && (helper != null))
      return helper.put(paramLong, paramString);
    return 0L;
  }

  public static void setInitTime(long paramLong)
  {
    if ((isEnabled() == true) && (helper != null))
      helper.setInitTime(paramLong);
  }

  public static void clear()
  {
    if ((isEnabled() == true) && (helper != null))
      helper.clear();
  }

  public static void write()
    throws IOException
  {
    write(null);
  }

  public static synchronized void write(PerfRollup paramPerfRollup)
    throws IOException
  {
    if ((isEnabled() == true) && (helper != null))
    {
      if (perfLog.isDirectory() == true)
        throw new IllegalStateException("The DEPLOY_PERF_LOG variable must point to a file path, not a directory!");
      if ((!perfLog.getParentFile().exists()) && (!perfLog.getParentFile().mkdirs()))
        throw new IllegalStateException("Failed to create the parent directories for the file specified by DEPLOY_PERF_LOG!");
      PrintStream localPrintStream = null;
      try
      {
        localPrintStream = new PrintStream(new FileOutputStream(perfLog, true));
        localPrintStream.println("");
        localPrintStream.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        localPrintStream.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        localPrintStream.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        localPrintStream.println("");
        PerfLabel[] arrayOfPerfLabel = helper.toArray();
        if ((arrayOfPerfLabel != null) && (arrayOfPerfLabel.length > 0))
        {
          long l1 = 9223372036854775807L;
          long l2 = 0L;
          long l3 = 0L;
          long l4 = 0L;
          int i = helper.getInitTime1() >= 0L ? 1 : 0;
          for (int j = 0; j < arrayOfPerfLabel.length; j++)
          {
            int k = 0;
            StringBuffer localStringBuffer = new StringBuffer(256);
            localStringBuffer.append(pad(arrayOfPerfLabel[j].getLabel(), 65, true));
            l2 = arrayOfPerfLabel[j].getTime();
            if (l1 > l2)
            {
              k = 1;
              l1 = l2;
              l4 = 0L;
            }
            l3 = l2 - l1;
            l1 = l2;
            localStringBuffer.append(" , ").append(pad("t0", l2, 11, false));
            localStringBuffer.append(" , ").append(pad("delta", l3, 10, false));
            l4 += l3;
            localStringBuffer.append(" , ").append(pad("elapsed", l4, 11, false));
            if (i != 0)
              localStringBuffer.append(" , ").append(pad("t1", arrayOfPerfLabel[j].getTime1(), 11, false));
            if (k != 0)
              localStringBuffer.append(" ** reset **");
            localPrintStream.println(localStringBuffer.toString());
          }
        }
        else
        {
          localPrintStream.println("The perf label event array is empty.");
        }
        localPrintStream.flush();
        if (paramPerfRollup != null)
        {
          paramPerfRollup.doRollup(arrayOfPerfLabel, localPrintStream);
          localPrintStream.flush();
        }
      }
      catch (IOException localIOException)
      {
        throw localIOException;
      }
      finally
      {
        if (localPrintStream != null)
          localPrintStream.close();
      }
    }
  }

  public static synchronized void initialize(PerfHelper paramPerfHelper)
  {
    if (helper == null)
      helper = paramPerfHelper;
  }

  public static synchronized PerfHelper getPerfHelper()
  {
    return helper;
  }

  private static File getPerfLog()
  {
    File localFile = null;
    try
    {
      String str1 = System.getenv("DEPLOY_PERF_ENABLED");
      if ((str1 != null) && (!str1.equalsIgnoreCase("false")))
      {
        String str2 = System.getenv("DEPLOY_PERF_LOG");
        if (str2 != null)
          localFile = new File(str2);
        else
          localFile = new File(Config.getLogDirectory(), "deploy_perf.log");
      }
    }
    catch (Error localError)
    {
    }
    return localFile;
  }

  private static String pad(String paramString, long paramLong, int paramInt, boolean paramBoolean)
  {
    String str = Long.toString(paramLong);
    return pad(paramString, str, paramInt, paramBoolean);
  }

  private static String pad(String paramString, int paramInt, boolean paramBoolean)
  {
    return pad(null, paramString, paramInt, paramBoolean);
  }

  private static String pad(String paramString1, String paramString2, int paramInt, boolean paramBoolean)
  {
    int i = paramInt - paramString2.length();
    if (null != paramString1)
      i -= paramString1.length() + 1;
    StringBuffer localStringBuffer = new StringBuffer(paramInt);
    if (null != paramString1)
    {
      localStringBuffer.append(paramString1);
      localStringBuffer.append(' ');
    }
    int j;
    if (!paramBoolean)
      for (j = 0; j < i; j++)
        localStringBuffer.append(' ');
    localStringBuffer.append(paramString2);
    if (paramBoolean == true)
      for (j = 0; j < i; j++)
        localStringBuffer.append(' ');
    return localStringBuffer.toString();
  }

  static
  {
    deployFirstframePerfEnabled = getenv("DEPLOY_FIRSTFRAME_PERF_ENABLED") != null;
    perfLog = getPerfLog();
    helper = null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.perf.DeployPerfUtil
 * JD-Core Version:    0.6.0
 */