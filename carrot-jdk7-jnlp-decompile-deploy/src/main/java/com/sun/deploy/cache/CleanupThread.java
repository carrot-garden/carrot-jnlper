package com.sun.deploy.cache;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.SystemUtils;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

class CleanupThread extends Thread
{
  private ArrayList nonJarItemRemovalList = new ArrayList();
  private ArrayList jarItemRemovalList = new ArrayList();
  private ArrayList loadedResourceList = new ArrayList();
  private static final double CACHE_THRESHOLD_PERCENT = 0.98D;
  private final Object syncObject;
  private final long currentCacheMaxSize = Config.getCacheSizeMax();
  private volatile long currentCacheSize = 0L;
  private boolean initCacheSize = true;
  private static final long BUSY_STALE_LIMIT = 3600000L;

  CleanupThread(String paramString, Object paramObject)
  {
    super(paramString);
    setDaemon(true);
    this.syncObject = paramObject;
  }

  synchronized void addToLoadedResourceList(String paramString)
  {
    this.loadedResourceList.add(paramString);
  }

  void startCleanup()
  {
    synchronized (this)
    {
      notify();
    }
  }

  private long getCurrentCacheSize()
  {
    long l = 0L;
    File[] arrayOfFile = getCacheResourceFiles();
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      String str1 = arrayOfFile[i].getPath();
      File localFile = null;
      if (arrayOfFile[i].isDirectory())
      {
        if (!arrayOfFile[i].getName().toLowerCase().endsWith("-n"))
        {
          deleteFileOrDirectory(arrayOfFile[i]);
        }
        else
        {
          localFile = new File(str1.substring(0, str1.length() - 2) + ".idx");
          if (localFile.exists())
            continue;
          deleteFileOrDirectory(arrayOfFile[i]);
        }
      }
      else
      {
        localFile = new File(str1 + ".idx");
        if (localFile.exists())
        {
          CacheEntry localCacheEntry = Cache.getCacheEntryFromFile(localFile);
          if (localCacheEntry != null)
          {
            l += localFile.length();
            l += arrayOfFile[i].length();
            l += getFileOrDirectorySize(new File(localCacheEntry.getNativeLibPath()));
            int j = 1;
            String str2 = localCacheEntry.getURL().toLowerCase();
            synchronized (this)
            {
              if ((str2.endsWith(".jnlp")) || (str2.endsWith(".jarjnlp")) || (localCacheEntry.getIsShortcutImage() == 1) || (MemoryCache.contains(localCacheEntry.getURL())) || (this.loadedResourceList.contains(localCacheEntry.getURL())) || (localCacheEntry.getBusy() == CacheEntry.BUSY_TRUE))
                j = 0;
            }
            if (j != 0)
              if ((str2.endsWith(".jar")) || (str2.endsWith(".jarjar")) || (str2.endsWith(".zip")))
              {
                if (!this.jarItemRemovalList.contains(arrayOfFile[i].getPath()))
                  this.jarItemRemovalList.add(arrayOfFile[i].getPath());
              }
              else if (!this.nonJarItemRemovalList.contains(arrayOfFile[i].getPath()))
                this.nonJarItemRemovalList.add(arrayOfFile[i].getPath());
          }
          else
          {
            localCacheEntry = new CacheEntry(localFile);
            if (checkBusy(localCacheEntry))
              continue;
            if (!localFile.delete())
              localFile.deleteOnExit();
            if (!arrayOfFile[i].delete())
              arrayOfFile[i].deleteOnExit();
            deleteFileOrDirectory(new File(str1 + "-n"));
          }
        }
        else
        {
          if (arrayOfFile[i].delete())
            continue;
          arrayOfFile[i].deleteOnExit();
        }
      }
    }
    return l;
  }

  private static File[] getCacheResourceFiles()
  {
    File localFile1 = Cache.getCacheDir();
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < 64; i++)
    {
      File localFile2 = new File(localFile1.getPath() + File.separator + i);
      if ((!localFile2.exists()) || (!localFile2.isDirectory()))
        continue;
      File[] arrayOfFile = localFile2.listFiles(new FileFilter()
      {
        public boolean accept(File paramFile)
        {
          String str = paramFile.getName().toLowerCase();
          return (!str.endsWith(".idx")) && (!str.endsWith(".lap")) && (!str.endsWith(".ico")) && (!str.endsWith("-temp"));
        }
      });
      for (int j = 0; j < arrayOfFile.length; j++)
        localArrayList.add(arrayOfFile[j]);
    }
    return (File[])(File[])localArrayList.toArray(new File[localArrayList.size()]);
  }

  private static long getFileOrDirectorySize(File paramFile)
  {
    long l = 0L;
    if (paramFile.exists())
      if (paramFile.isDirectory())
      {
        File[] arrayOfFile = paramFile.listFiles();
        for (int i = 0; i < arrayOfFile.length; i++)
          l += getFileOrDirectorySize(arrayOfFile[i]);
      }
      else
      {
        l += paramFile.length();
      }
    return l;
  }

  private static void deleteFileOrDirectory(File paramFile)
  {
    if (paramFile.exists())
    {
      if (paramFile.isDirectory())
      {
        File[] arrayOfFile = paramFile.listFiles();
        for (int i = 0; i < arrayOfFile.length; i++)
          deleteFileOrDirectory(arrayOfFile[i]);
      }
      paramFile.delete();
    }
  }

  private Object[] prepareRemovalList(ArrayList paramArrayList)
  {
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = paramArrayList.iterator();
    while (localIterator.hasNext())
    {
      localObject = (String)localIterator.next();
      File localFile = new File((String)localObject + ".idx");
      if (localFile.exists())
      {
        CacheEntry localCacheEntry = Cache.getCacheEntryFromFile(localFile);
        localArrayList.add(localCacheEntry);
      }
    }
    Object localObject = localArrayList.toArray();
    Arrays.sort(localObject, new Comparator()
    {
      public int compare(Object paramObject1, Object paramObject2)
      {
        CacheEntry localCacheEntry1 = (CacheEntry)paramObject1;
        CacheEntry localCacheEntry2 = (CacheEntry)paramObject2;
        if (localCacheEntry1.removeBefore(localCacheEntry2))
          return -1;
        if (localCacheEntry2.removeBefore(localCacheEntry1))
          return 1;
        return 0;
      }
    });
    return (Object)localObject;
  }

  private void removeResourceFromList(Object[] paramArrayOfObject)
  {
    long l1 = ()(this.currentCacheMaxSize * 0.98D);
    for (int i = 0; (i < paramArrayOfObject.length) && (this.currentCacheSize >= l1); i++)
    {
      CacheEntry localCacheEntry = (CacheEntry)paramArrayOfObject[i];
      long l2 = localCacheEntry.getIndexFile().length() + new File(localCacheEntry.getResourceFilename()).length() + getFileOrDirectorySize(new File(localCacheEntry.getNativeLibPath()));
      Cache.removeCacheEntry(localCacheEntry);
      this.currentCacheSize -= l2;
    }
  }

  public void run()
  {
    while (true)
      try
      {
        synchronized (this)
        {
          wait();
        }
        long l1 = SystemUtils.microTime();
        synchronized (this.syncObject)
        {
          if (!this.initCacheSize)
            continue;
          this.currentCacheSize = getCurrentCacheSize();
          this.initCacheSize = false;
          if ((this.currentCacheMaxSize == -1L) || (this.currentCacheSize < this.currentCacheMaxSize))
            continue;
          Object[] arrayOfObject = prepareRemovalList(this.nonJarItemRemovalList);
          removeResourceFromList(arrayOfObject);
          arrayOfObject = prepareRemovalList(this.jarItemRemovalList);
          removeResourceFromList(arrayOfObject);
        }
        long l2 = SystemUtils.microTime() - l1;
        Trace.println("CleanupThread used " + l2 + " us", TraceLevel.NETWORK);
        continue;
      }
      catch (InterruptedException localInterruptedException)
      {
      }
  }

  private static boolean checkBusy(CacheEntry paramCacheEntry)
  {
    Object localObject = MemoryCache.getLoadedResource(paramCacheEntry.getURL());
    CacheEntry localCacheEntry = (localObject instanceof CacheEntry) ? (CacheEntry)localObject : null;
    if (paramCacheEntry.isSameEntry(localCacheEntry))
      return true;
    if (paramCacheEntry.getBusy() == CacheEntry.BUSY_TRUE)
    {
      long l = paramCacheEntry.getIndexFile().lastModified();
      if (System.currentTimeMillis() - l < 3600000L)
        return true;
    }
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.CleanupThread
 * JD-Core Version:    0.6.0
 */