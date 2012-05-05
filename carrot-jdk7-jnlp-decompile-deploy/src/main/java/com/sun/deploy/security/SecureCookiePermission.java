package com.sun.deploy.security;

import java.net.URI;
import java.net.URL;
import java.security.BasicPermission;
import java.security.PermissionCollection;

public final class SecureCookiePermission extends BasicPermission
{
  static String ORIGIN_PREFIX = "origin.";

  public SecureCookiePermission(String paramString)
  {
    super(paramString);
  }

  public SecureCookiePermission(URI paramURI)
  {
    super(getURIOriginString(paramURI));
  }

  public SecureCookiePermission(String paramString1, String paramString2)
  {
    super(paramString1, paramString2);
  }

  public static String getURIOriginString(URI paramURI)
  {
    if (paramURI == null)
      return null;
    StringBuffer localStringBuffer = new StringBuffer(ORIGIN_PREFIX);
    localStringBuffer.append(paramURI.getScheme());
    localStringBuffer.append("://");
    localStringBuffer.append(paramURI.getHost());
    localStringBuffer.append(":");
    int i = paramURI.getPort();
    if (i == -1)
      try
      {
        i = paramURI.toURL().getDefaultPort();
      }
      catch (Exception localException)
      {
      }
    localStringBuffer.append(i);
    return localStringBuffer.toString();
  }

  public static String getURLOriginString(URL paramURL)
  {
    if (paramURL == null)
      return null;
    StringBuffer localStringBuffer = new StringBuffer(ORIGIN_PREFIX);
    localStringBuffer.append(paramURL.getProtocol());
    localStringBuffer.append("://");
    localStringBuffer.append(paramURL.getHost());
    localStringBuffer.append(":");
    int i = paramURL.getPort();
    if (i == -1)
      i = paramURL.getDefaultPort();
    localStringBuffer.append(i);
    return localStringBuffer.toString();
  }

  public PermissionCollection newPermissionCollection()
  {
    return new BasicPermissionCollection();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.SecureCookiePermission
 * JD-Core Version:    0.6.0
 */