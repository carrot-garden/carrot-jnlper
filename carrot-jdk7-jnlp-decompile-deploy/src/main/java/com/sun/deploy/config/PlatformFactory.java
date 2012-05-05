package com.sun.deploy.config;

public class PlatformFactory
{
  public static Platform newInstance()
  {
    return new UnixPlatform();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.PlatformFactory
 * JD-Core Version:    0.6.0
 */