package com.sun.deploy.net.protocol.chrome;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public final class ChromeURLConnection extends URLConnection
{
  URL url = null;

  public ChromeURLConnection(URL paramURL)
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
 * Qualified Name:     com.sun.deploy.net.protocol.chrome.ChromeURLConnection
 * JD-Core Version:    0.6.0
 */