package com.sun.deploy.cache;

import com.sun.deploy.trace.Trace;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

class DeployCacheRequest extends CacheRequest
{
  DeployFileOutputStream fos;
  File file;

  public DeployCacheRequest(URL paramURL, URLConnection paramURLConnection, boolean paramBoolean)
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL, paramURLConnection, paramBoolean)
      {
        private final URL val$url;
        private final URLConnection val$conn;
        private final boolean val$isPack;

        public Object run()
          throws Exception
        {
          try
          {
            DeployCacheRequest.this.file = File.createTempFile("jar_cache", null);
            DeployCacheRequest.this.file.deleteOnExit();
            DeployCacheRequest.this.fos = new DeployFileOutputStream(DeployCacheRequest.this.file, this.val$url, this.val$conn, this.val$isPack);
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Trace.ignoredException(localPrivilegedActionException);
    }
  }

  public OutputStream getBody()
    throws IOException
  {
    return new BufferedOutputStream(this.fos);
  }

  public void abort()
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run()
          throws Exception
        {
          try
          {
            if (DeployCacheRequest.this.fos != null)
            {
              DeployCacheRequest.this.fos.setAbort(true);
              DeployCacheRequest.this.fos.close();
            }
            if (DeployCacheRequest.this.file != null)
              DeployCacheRequest.this.file.delete();
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Trace.ignoredException(localPrivilegedActionException);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.DeployCacheRequest
 * JD-Core Version:    0.6.0
 */