package com.sun.deploy.cache;

import com.sun.deploy.Environment;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.net.CanceledDownloadException;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.HttpUtils;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

class DeployFileOutputStream extends FileOutputStream
{
  private final URL _url;
  private final URLConnection _conn;
  private File _file;
  private boolean _isPack = false;
  private boolean closed = false;
  private boolean finalized = false;
  private boolean aborted = false;

  DeployFileOutputStream(File paramFile, URL paramURL, URLConnection paramURLConnection, boolean paramBoolean)
    throws FileNotFoundException
  {
    super(paramFile);
    this._url = paramURL;
    this._conn = paramURLConnection;
    this._file = paramFile;
    this._isPack = paramBoolean;
  }

  void setAbort(boolean paramBoolean)
  {
    this.aborted = paramBoolean;
  }

  protected void finalize()
    throws IOException
  {
    this.finalized = true;
    super.finalize();
  }

  public void close()
    throws IOException
  {
    super.close();
    if ((this.closed) || (this.finalized) || (this.aborted))
      return;
    this.closed = true;
    URL localURL = HttpUtils.removeQueryStringFromURL(this._url);
    String str1 = (String)ToolkitStore.get().getAppContext().get("deploy-" + localURL.toString());
    boolean bool1 = false;
    String str2 = this._conn.getContentType();
    if ((str2 != null) && (str2.equalsIgnoreCase("application/x-java-jnlp-error")))
      throw new IOException("version requested not returned");
    String str3 = this._conn.getHeaderField("x-java-jnlp-version-id");
    if ((str3 == null) && (Environment.isJavaPlugin()))
      str3 = str1;
    bool1 = (str2 != null) && (str2.equalsIgnoreCase("application/x-java-archive-diff"));
    InputStream localInputStream = null;
    try
    {
      localInputStream = (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run()
          throws IOException
        {
          return new BufferedInputStream(new FileInputStream(DeployFileOutputStream.this._file));
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException1)
    {
      Trace.ignoredException(localPrivilegedActionException1);
    }
    String str4 = this._conn.getRequestProperty("content-type");
    try
    {
      int i = 0;
      if (this._isPack)
        i = 4352;
      else if ((str4 != null) && (str4.equals("application/x-java-archive")))
        i = 256;
      else
        i = 1;
      boolean bool2 = DownloadEngine.isInternalUse();
      CacheEntry localCacheEntry1 = Cache.downloadResourceToCache(localURL, str3, this._conn, this._url, bool1, i, localInputStream, bool2);
      if (localCacheEntry1 != null)
      {
        CacheEntry localCacheEntry2 = (CacheEntry)(CacheEntry)MemoryCache.addLoadedResource(localURL.toString(), localCacheEntry1);
        if ((localCacheEntry2 != null) && (localCacheEntry2.getVersion() == null) && (localCacheEntry1.getVersion() == null))
          Cache.markResourceIncomplete(localCacheEntry2);
      }
    }
    catch (CanceledDownloadException localCanceledDownloadException)
    {
      throw new IOException(localCanceledDownloadException.getMessage());
    }
    finally
    {
      super.close();
      try
      {
        AccessController.doPrivileged(new PrivilegedExceptionAction()
        {
          public Object run()
            throws IOException
          {
            if (DeployFileOutputStream.this._file != null)
              DeployFileOutputStream.this._file.delete();
            return null;
          }
        });
      }
      catch (PrivilegedActionException localPrivilegedActionException3)
      {
        Trace.ignoredException(localPrivilegedActionException3);
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.DeployFileOutputStream
 * JD-Core Version:    0.6.0
 */