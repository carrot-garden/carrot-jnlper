package com.sun.deploy.net.protocol.javascript;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public final class JavaScriptURLConnection extends URLConnection
{
  URL url = null;

  public JavaScriptURLConnection(URL paramURL)
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
 * Qualified Name:     com.sun.deploy.net.protocol.javascript.JavaScriptURLConnection
 * JD-Core Version:    0.6.0
 */