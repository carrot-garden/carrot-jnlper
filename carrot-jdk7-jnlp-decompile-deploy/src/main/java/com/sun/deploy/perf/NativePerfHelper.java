package com.sun.deploy.perf;

import com.sun.deploy.config.Platform;

public class NativePerfHelper
  implements PerfHelper
{
  public void setInitTime(long paramLong)
  {
  }

  public void setInitTime1(long paramLong)
  {
  }

  public long getInitTime0()
  {
    return 0L;
  }

  public long getInitTime1()
  {
    return 0L;
  }

  public void clear()
  {
  }

  public long put(long paramLong, String paramString)
  {
    put(paramString);
    return 0L;
  }

  public native void put(String paramString);

  public native PerfLabel[] toArray();

  static
  {
    Platform.get();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.perf.NativePerfHelper
 * JD-Core Version:    0.6.0
 */