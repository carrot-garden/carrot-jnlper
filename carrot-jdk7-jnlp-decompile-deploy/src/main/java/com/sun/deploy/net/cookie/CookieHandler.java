package com.sun.deploy.net.cookie;

import java.net.URL;

public abstract interface CookieHandler
{
  public abstract String getCookieInfo(URL paramURL)
    throws CookieUnavailableException;

  public abstract void setCookieInfo(URL paramURL, String paramString)
    throws CookieUnavailableException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.cookie.CookieHandler
 * JD-Core Version:    0.6.0
 */