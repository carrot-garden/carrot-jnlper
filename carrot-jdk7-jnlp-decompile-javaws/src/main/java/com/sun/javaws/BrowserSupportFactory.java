package com.sun.javaws;

public class BrowserSupportFactory
{
  public static BrowserSupport newInstance()
  {
    return new UnixBrowserSupport();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.BrowserSupportFactory
 * JD-Core Version:    0.6.0
 */