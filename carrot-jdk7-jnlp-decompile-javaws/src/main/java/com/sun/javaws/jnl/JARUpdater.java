package com.sun.javaws.jnl;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.exceptions.FailedDownloadingResourceException;
import com.sun.javaws.exceptions.JNLPException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class JARUpdater
{
  private JARDesc _jar = null;
  private CacheEntry _ce = null;
  private boolean _updateChecked = false;
  private boolean _updateAvailable = false;
  private boolean _updateDownloaded = false;

  public JARUpdater(JARDesc paramJARDesc)
  {
    this._jar = paramJARDesc;
  }

  public synchronized boolean isUpdateAvailable()
    throws Exception
  {
    if (!this._updateChecked)
    {
      Trace.println("JARUpdater: update check for " + this._jar.getLocation().toString(), TraceLevel.NETWORK);
      try
      {
        this._updateAvailable = updateCheck();
        this._updateChecked = true;
        this._updateDownloaded = (!this._updateAvailable);
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
        throw localException;
      }
    }
    return this._updateAvailable;
  }

  public boolean isUpdateDownloaded()
  {
    return this._updateDownloaded;
  }

  public void downloadUpdate()
    throws Exception
  {
    if (isUpdateAvailable())
      download();
    synchronized (this)
    {
      this._updateAvailable = false;
      this._updateDownloaded = true;
    }
  }

  private CacheEntry getCacheEntry()
  {
    return this._ce;
  }

  public void updateCache()
    throws IOException
  {
    URL localURL = this._jar.getLocation();
    CacheEntry localCacheEntry1 = getCacheEntry();
    CacheEntry localCacheEntry2 = Cache.getCacheEntry(localURL, null, this._jar.getVersion(), getContentType());
    if (localCacheEntry1 != null)
      Cache.processNewCacheEntry(localURL, true, localCacheEntry1, localCacheEntry2);
    Trace.println("Background Update Thread: update cache: " + localURL, TraceLevel.NETWORK);
  }

  private boolean updateCheck()
    throws JNLPException
  {
    URL localURL = this._jar.getLocation();
    String str = this._jar.getVersion();
    boolean bool = false;
    if (str != null)
      return false;
    try
    {
      bool = DownloadEngine.isUpdateAvailable(localURL, str, this._jar.isPack200Enabled());
    }
    catch (IOException localIOException)
    {
      ResourcesDesc localResourcesDesc = this._jar.getParent();
      LaunchDesc localLaunchDesc = localResourcesDesc == null ? null : localResourcesDesc.getParent();
      throw new FailedDownloadingResourceException(localLaunchDesc, localURL, null, localIOException);
    }
    return bool;
  }

  private void download()
    throws JNLPException
  {
    int i = getContentType();
    URL localURL = this._jar.getLocation();
    String str = this._jar.getVersion();
    CacheEntry localCacheEntry = null;
    try
    {
      localCacheEntry = DownloadEngine.getResourceTempCacheEntry(localURL, str, i);
      Trace.println("Downloaded " + localURL + " to " + URLUtil.fileToURL(new File(localCacheEntry.getResourceFilename())) + "\n\t Cache Entry disabled", TraceLevel.NETWORK);
    }
    catch (IOException localIOException)
    {
      throw new FailedDownloadingResourceException(localURL, str, localIOException);
    }
    if ((Cache.isCacheEnabled()) && (localCacheEntry == null))
      throw new FailedDownloadingResourceException(localURL, str, null);
    this._ce = localCacheEntry;
  }

  private int getContentType()
  {
    int i = 256;
    if (this._jar.isNativeLib())
      i |= 16;
    if (this._jar.isPack200Enabled())
      i |= 4096;
    return i;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.JARUpdater
 * JD-Core Version:    0.6.0
 */