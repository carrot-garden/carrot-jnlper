package com.sun.deploy.net.protocol.about;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public final class AboutURLConnection extends URLConnection
{
  URL url = null;

  public AboutURLConnection(URL paramURL)
    throws IOException
  {
    super(paramURL);
    this.url = paramURL;
  }

  public void connect()
    throws IOException
  {
    throw new IOException("Cannot connect to " + this.url);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.protocol.about.AboutURLConnection
 * JD-Core Version:    0.6.0
 */