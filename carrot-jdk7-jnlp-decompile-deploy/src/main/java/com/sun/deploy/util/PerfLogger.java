package com.sun.deploy.util;

import sun.misc.PerformanceLogger;

public final class PerfLogger
{
  private static boolean perfLogOn = false;
  private static boolean useNano = false;
  private static String perfLogFilePath = null;
  private static long baseTime = 0L;
  private static long endTime = 0L;

  private static boolean isJavaVersionAtLeast15()
  {
    String str = System.getProperty("java.version");
    return (!str.startsWith("1.3")) && (!str.startsWith("1.4"));
  }

  public static void setBaseTimeString(String paramString)
  {
    if ((perfLogOn) && (baseTime == 0L) && (paramString != null))
      try
      {
        long l = new Long(paramString).longValue();
        if (l > 0L)
          if (useNano)
          {
            baseTime = l * 1000000L;
            PerformanceLogger.setStartTime("Native Start Time (in Nano)", baseTime);
          }
          else
          {
            baseTime = l;
            PerformanceLogger.setStartTime("Native Start Time", baseTime);
          }
      }
      catch (Throwable localThrowable)
      {
      }
  }

  public static boolean perfLogEnabled()
  {
    return perfLogOn;
  }

  public static void setStartTime(String paramString)
  {
    if ((perfLogOn) && (baseTime == 0L))
    {
      if (useNano)
        baseTime = System.nanoTime();
      else
        baseTime = System.currentTimeMillis();
      PerformanceLogger.setStartTime(paramString, baseTime);
    }
  }

  public static void setTime(String paramString)
  {
    if (perfLogOn)
      PerformanceLogger.setTime(paramString);
  }

  public static void setEndTime(String paramString)
  {
    if (perfLogOn)
    {
      PerformanceLogger.setTime(paramString);
      if (useNano)
        endTime = System.nanoTime();
      else
        endTime = System.currentTimeMillis();
    }
  }

  public static void outputLog()
  {
    if (perfLogOn)
    {
      try
      {
        PerformanceLogger.setBaseTime(baseTime);
      }
      catch (NoSuchMethodError localNoSuchMethodError)
      {
      }
      if (endTime != 0L)
        PerformanceLogger.setTime("Deployment Java Startup time " + (endTime - PerformanceLogger.getStartTime()));
      PerformanceLogger.outputLog();
    }
  }

  static
  {
    perfLogFilePath = System.getProperty("sun.perflog");
    if ((perfLogFilePath != null) && (isJavaVersionAtLeast15()))
    {
      perfLogOn = true;
      useNano = System.getProperty("sun.perflog.nano") != null;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.PerfLogger
 * JD-Core Version:    0.6.0
 */