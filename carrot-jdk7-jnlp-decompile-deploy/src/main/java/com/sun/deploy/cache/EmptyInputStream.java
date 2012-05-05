package com.sun.deploy.cache;

import java.io.IOException;
import java.io.InputStream;

class EmptyInputStream extends InputStream
{
  public int read()
    throws IOException
  {
    return -1;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.EmptyInputStream
 * JD-Core Version:    0.6.0
 */