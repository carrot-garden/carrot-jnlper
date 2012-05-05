package com.sun.deploy.services;

public final class PlatformType
{
  private static final int UNKNOWN = 0;
  private static final int BROWSER_WIN32 = 256;
  private static final int BROWSER_UNIX = 4096;
  private static final int BROWSER_MACOSX = 8192;
  private static final int INTERNET_EXPLORER = 1;
  private static final int NETSCAPE4 = 2;
  private static final int NETSCAPE6 = 3;
  private static final int NETSCAPE45 = 4;
  public static final int AXBRIDGE = 5;
  private static final int STANDALONE_MANTIS = 16384;
  private static final int STANDALONE_TIGER = 32768;
  public static final int INTERNET_EXPLORER_WIN32 = 257;
  public static final int NETSCAPE4_WIN32 = 258;
  public static final int NETSCAPE45_WIN32 = 260;
  public static final int NETSCAPE6_WIN32 = 259;
  public static final int NETSCAPE4_UNIX = 4098;
  public static final int NETSCAPE45_UNIX = 4100;
  public static final int NETSCAPE6_UNIX = 4099;
  public static final int STANDALONE_MANTIS_WIN32 = 16640;
  public static final int STANDALONE_MANTIS_UNIX = 20480;
  public static final int STANDALONE_TIGER_WIN32 = 33024;
  public static final int STANDALONE_TIGER_UNIX = 36864;
  public static final int STANDALONE_TIGER_MACOSX = 40960;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.services.PlatformType
 * JD-Core Version:    0.6.0
 */