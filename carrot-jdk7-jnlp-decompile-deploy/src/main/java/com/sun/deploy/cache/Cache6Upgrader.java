package com.sun.deploy.cache;

import com.sun.deploy.trace.Trace;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

class Cache6Upgrader extends CacheUpgrader
{
  static final String CACHE_6_UPGRADER_NAME = "Cache6Upgrader";
  static final String SYSTEM_CACHE_6_UPGRADER_NAME = "SystemCache6Upgrader";

  private Cache6Upgrader(String paramString, File paramFile1, File paramFile2)
  {
    super(paramString, paramFile1, paramFile2);
  }

  static Cache6Upgrader createInstance(boolean paramBoolean, File paramFile1, File paramFile2)
  {
    String str = "Cache6Upgrader";
    if (paramBoolean)
      str = "SystemCache6Upgrader";
    return new Cache6Upgrader(str, paramFile1, paramFile2);
  }

  protected void upgradeImpl()
  {
    copySplashDir();
    copyMuffinDir();
    copyCacheEntries();
  }

  protected CacheEntry upgradeItemImpl(URL paramURL, String paramString, int paramInt)
  {
    CacheEntry localCacheEntry1 = getOldCacheEntry(paramURL, paramString, paramInt);
    if (localCacheEntry1 == null)
    {
      if (isTracing())
        trace(this + ": found no old cache for: " + paramURL.toString() + " version: " + paramString + " contentType: " + paramInt);
      return null;
    }
    CacheEntry localCacheEntry2 = copyToNewCache(localCacheEntry1);
    if (isTracing())
      if (localCacheEntry2 != null)
        trace(this + " copied " + paramURL.toString() + " to: " + localCacheEntry2.getDataFile());
      else
        trace(this + " failed to copy: " + paramURL.toString());
    return localCacheEntry2;
  }

  private void copySplashDir()
  {
    File localFile1 = new File(this.oldCacheDir, "splash");
    File localFile2 = new File(this.newCacheDir, "splash");
    copyDirIgnoresErrors(localFile1, localFile2);
  }

  private void copyMuffinDir()
  {
    File localFile1 = new File(this.oldCacheDir, "muffin");
    File localFile2 = new File(this.newCacheDir, "muffin");
    copyDirIgnoresErrors(localFile1, localFile2, new FilenameFilter()
    {
      public boolean accept(File paramFile, String paramString)
      {
        if (new File(paramFile, paramString).isDirectory())
          return true;
        return paramString.toLowerCase().endsWith(".muf");
      }
    });
  }

  private void copyCacheEntries()
  {
    File[] arrayOfFile = Cache.getIndexFiles(this.oldCacheDir);
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      File localFile = arrayOfFile[i];
      CacheEntry localCacheEntry = getCacheEntryFromIndexFile(localFile);
      if (localCacheEntry == null)
        continue;
      try
      {
        if (resourcePresentInNewCache(new URL(localCacheEntry.getURL())))
          continue;
        copyToNewCache(localCacheEntry);
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.Cache6Upgrader
 * JD-Core Version:    0.6.0
 */