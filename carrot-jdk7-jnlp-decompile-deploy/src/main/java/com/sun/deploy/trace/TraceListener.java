package com.sun.deploy.trace;

public abstract interface TraceListener
{
  public abstract void print(String paramString);

  public abstract void flush();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.trace.TraceListener
 * JD-Core Version:    0.6.0
 */