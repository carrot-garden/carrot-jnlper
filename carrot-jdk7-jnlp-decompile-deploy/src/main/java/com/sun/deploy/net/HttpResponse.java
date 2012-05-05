package com.sun.deploy.net;

import java.io.BufferedInputStream;
import java.net.URL;

public abstract interface HttpResponse
{
  public abstract URL getRequest();

  public abstract int getStatusCode();

  public abstract int getContentLength();

  public abstract long getExpiration();

  public abstract long getLastModified();

  public abstract String getContentType();

  public abstract String getResponseHeader(String paramString);

  public abstract BufferedInputStream getInputStream();

  public abstract void disconnect();

  public abstract String getContentEncoding();

  public abstract MessageHeader getHeaders();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.HttpResponse
 * JD-Core Version:    0.6.0
 */