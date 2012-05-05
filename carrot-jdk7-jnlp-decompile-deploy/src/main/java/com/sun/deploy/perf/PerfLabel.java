package com.sun.deploy.perf;

public class PerfLabel
{
  private long time0;
  private long time1;
  private String label;

  public long getTime()
  {
    return this.time0;
  }

  public long getTime1()
  {
    return this.time1;
  }

  public String getLabel()
  {
    return this.label;
  }

  public PerfLabel()
  {
    this.time0 = 0L;
    this.time1 = -1L;
    this.label = "";
  }

  public PerfLabel(long paramLong, String paramString)
  {
    this.time0 = paramLong;
    this.time1 = -1L;
    this.label = paramString;
  }

  public PerfLabel(long paramLong1, long paramLong2, String paramString)
  {
    this.time0 = paramLong1;
    this.time1 = paramLong2;
    this.label = paramString;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.perf.PerfLabel
 * JD-Core Version:    0.6.0
 */