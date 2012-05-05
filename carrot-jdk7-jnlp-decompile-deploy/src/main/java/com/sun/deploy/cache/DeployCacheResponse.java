package com.sun.deploy.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.CacheResponse;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DeployCacheResponse extends CacheResponse
{
  protected InputStream is;
  protected Map headers;

  DeployCacheResponse(InputStream paramInputStream, Map paramMap)
  {
    this.is = paramInputStream;
    this.headers = paramMap;
  }

  public InputStream getBody()
    throws IOException
  {
    return this.is;
  }

  public Map getHeaders()
    throws IOException
  {
    return this.headers;
  }

  private void dumpHeaders(PrintStream paramPrintStream)
  {
    Iterator localIterator1 = this.headers.keySet().iterator();
    while (localIterator1.hasNext())
    {
      String str1 = (String)localIterator1.next();
      if (str1 != null)
      {
        List localList = (List)this.headers.get(str1);
        Iterator localIterator2 = localList.iterator();
        while (localIterator2.hasNext())
        {
          String str2 = (String)localIterator2.next();
          if (str2 != null)
            paramPrintStream.println("key=" + str1 + ", value=" + str2);
        }
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.DeployCacheResponse
 * JD-Core Version:    0.6.0
 */