package com.sun.deploy.trace;

public class TraceLevel
{
  public static final TraceLevel DEFAULT = new TraceLevel("default");
  public static final TraceLevel BASIC = new TraceLevel("basic");
  public static final TraceLevel NETWORK = new TraceLevel("network");
  public static final TraceLevel SECURITY = new TraceLevel("security");
  public static final TraceLevel CACHE = new TraceLevel("cache");
  public static final TraceLevel EXTENSIONS = new TraceLevel("extensions");
  public static final TraceLevel LIVECONNECT = new TraceLevel("liveconnect");
  public static final TraceLevel UI = new TraceLevel("ui");
  public static final TraceLevel PRELOADER = new TraceLevel("preloader");
  public static final TraceLevel TEMP = new TraceLevel("temp");
  private final String level;

  private TraceLevel(String paramString)
  {
    this.level = paramString;
  }

  public String toString()
  {
    return this.level;
  }

  public static TraceLevel[] values()
  {
    return new TraceLevel[] { DEFAULT, BASIC, NETWORK, SECURITY, CACHE, EXTENSIONS, LIVECONNECT, UI, PRELOADER, TEMP };
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.trace.TraceLevel
 * JD-Core Version:    0.6.0
 */