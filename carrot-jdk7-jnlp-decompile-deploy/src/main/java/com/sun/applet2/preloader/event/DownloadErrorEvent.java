package com.sun.applet2.preloader.event;

import java.net.URL;

public class DownloadErrorEvent extends ErrorEvent
{
  private String version;

  public DownloadErrorEvent(URL paramURL, String paramString)
  {
    this(paramURL, paramString, null);
  }

  public DownloadErrorEvent(URL paramURL, String paramString, Throwable paramThrowable)
  {
    super(paramURL, "Unable to load resource", paramThrowable);
    this.version = paramString;
  }

  public String getVersion()
  {
    return this.version;
  }

  public String toString()
  {
    return "DownloadErrorEvent[url=" + getLocation() + " version=" + this.version + " label=" + getValue() + " cause=" + (getException() == null ? "null" : getException().getMessage());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.preloader.event.DownloadErrorEvent
 * JD-Core Version:    0.6.0
 */