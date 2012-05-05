package com.sun.deploy.perf;

import com.sun.deploy.util.SystemUtils;
import java.io.PrintStream;
import java.util.ArrayList;

public class DefaultPerfHelper
  implements PerfHelper
{
  private long initTime0 = 0L;
  private long lastTime0 = 0L;
  private long initTime1 = -1L;
  private long lastTime1 = -1L;
  private static ArrayList labelList = new ArrayList();

  public DefaultPerfHelper()
  {
    setInitTime(SystemUtils.microTime());
  }

  public DefaultPerfHelper(long paramLong)
  {
    setInitTime(paramLong);
  }

  public DefaultPerfHelper(long paramLong1, long paramLong2)
  {
    this(paramLong1);
    setInitTime1(paramLong2);
  }

  public void setInitTime(long paramLong)
  {
    this.initTime0 = paramLong;
    this.lastTime0 = 0L;
  }

  public void setInitTime1(long paramLong)
  {
    this.initTime1 = paramLong;
    this.lastTime1 = 0L;
  }

  public long getInitTime0()
  {
    return this.initTime0;
  }

  public long getInitTime1()
  {
    return this.initTime1;
  }

  public void clear()
  {
    labelList.clear();
  }

  private PerfLabel tagLabel(String paramString)
  {
    long l1 = SystemUtils.microTime();
    long l2 = l1 - this.initTime0;
    PerfLabel localPerfLabel;
    if (this.initTime1 >= 0L)
    {
      long l3 = l1 - this.initTime1;
      localPerfLabel = new PerfLabel(l2, l3, paramString);
    }
    else
    {
      localPerfLabel = new PerfLabel(l2, paramString);
    }
    labelList.add(localPerfLabel);
    return localPerfLabel;
  }

  public void put(String paramString)
  {
    put(-1L, paramString);
  }

  public long put(long paramLong, String paramString)
  {
    PerfLabel localPerfLabel = tagLabel(paramString);
    long l1 = localPerfLabel.getTime();
    StringBuffer localStringBuffer = new StringBuffer(256);
    localStringBuffer.append("PERF: t0 ");
    localStringBuffer.append(Long.toString(l1));
    if (this.initTime1 >= 0L)
    {
      long l2 = localPerfLabel.getTime1();
      localStringBuffer.append(" us, t1 ");
      localStringBuffer.append(Long.toString(l2));
      this.lastTime1 = l2;
    }
    localStringBuffer.append(" us, dt ");
    localStringBuffer.append(Long.toString(l1 - this.lastTime0));
    this.lastTime0 = l1;
    if (paramLong >= 0L)
    {
      localStringBuffer.append(" us, user dt ");
      localStringBuffer.append(Long.toString(l1 - paramLong));
    }
    localStringBuffer.append(" us :");
    localStringBuffer.append(paramString);
    System.out.println(localStringBuffer.toString());
    return this.lastTime0;
  }

  public PerfLabel[] toArray()
  {
    if (labelList.size() == 0)
      return null;
    PerfLabel[] arrayOfPerfLabel = new PerfLabel[labelList.size()];
    for (int i = 0; i < labelList.size(); i++)
      arrayOfPerfLabel[i] = ((PerfLabel)labelList.get(i));
    return arrayOfPerfLabel;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.perf.DefaultPerfHelper
 * JD-Core Version:    0.6.0
 */