package com.sun.applet2.preloader.event;

import java.net.URL;

public class ErrorEvent extends PreloaderEvent
{
  private URL location;
  private String value;
  private Throwable t;

  public ErrorEvent(URL paramURL, String paramString)
  {
    this(paramURL, paramString, null);
  }

  public ErrorEvent(URL paramURL, Throwable paramThrowable)
  {
    this(paramURL, null == paramThrowable ? null : paramThrowable.getMessage(), paramThrowable);
  }

  public ErrorEvent(URL paramURL, String paramString, Throwable paramThrowable)
  {
    super(6);
    this.location = paramURL;
    this.value = paramString;
    this.t = paramThrowable;
  }

  public String getValue()
  {
    return this.value;
  }

  public URL getLocation()
  {
    return this.location;
  }

  public Throwable getException()
  {
    return this.t;
  }

  public String toString()
  {
    return "ErrorEvent[url=" + this.location + " label=" + this.value + " cause=" + (this.t == null ? "null" : this.t.getMessage());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.preloader.event.ErrorEvent
 * JD-Core Version:    0.6.0
 */