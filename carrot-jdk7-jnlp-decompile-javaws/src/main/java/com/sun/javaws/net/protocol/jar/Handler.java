package com.sun.javaws.net.protocol.jar;

import com.sun.jnlp.JNLPCachedJarURLConnection;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Handler extends sun.net.www.protocol.jar.Handler
{
  protected URLConnection openConnection(URL paramURL)
    throws IOException
  {
    return new JNLPCachedJarURLConnection(paramURL, this);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.net.protocol.jar.Handler
 * JD-Core Version:    0.6.0
 */