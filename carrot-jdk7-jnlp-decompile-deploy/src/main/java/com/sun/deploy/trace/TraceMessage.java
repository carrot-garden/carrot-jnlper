package com.sun.deploy.trace;

import com.sun.deploy.util.PerfLogger;

final class TraceMessage
{
  private final TraceLevel level;
  private final String msg;
  private final boolean newline;
  private final long ts = System.currentTimeMillis();

  TraceMessage(TraceLevel paramTraceLevel, String paramString)
  {
    this(paramTraceLevel, paramString, false);
  }

  TraceMessage(TraceLevel paramTraceLevel, String paramString, boolean paramBoolean)
  {
    this.level = paramTraceLevel;
    this.msg = paramString;
    this.newline = paramBoolean;
  }

  public String toString()
  {
    if (PerfLogger.perfLogEnabled())
      return "ts: " + this.ts + ' ' + this.level + ": " + this.msg;
    return this.level + ": " + this.msg;
  }

  public TraceLevel getLevel()
  {
    return this.level;
  }

  public String getMessage()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    if (PerfLogger.perfLogEnabled())
      localStringBuffer.append("ts: ").append(this.ts).append(' ');
    if (this.level.equals(TraceLevel.DEFAULT))
      localStringBuffer.append(this.msg);
    else
      localStringBuffer.append(this.level).append(": ").append(this.msg);
    if (this.newline)
      localStringBuffer.append('\n');
    return localStringBuffer.toString();
  }

  public String getMsg()
  {
    return this.msg;
  }

  public long getTimestamp()
  {
    return this.ts;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.trace.TraceMessage
 * JD-Core Version:    0.6.0
 */