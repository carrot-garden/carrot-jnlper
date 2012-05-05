package com.sun.deploy.cache;

import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.UpdateTracker;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarFile;

public class MemoryCache
{
  private static final Map loadedResource;
  private static ReferenceQueue refQueue = new ReferenceQueue();
  private static Thread cleanupThread;

  static synchronized void reset()
  {
    loadedResource.clear();
  }

  public static synchronized Object addLoadedResource(String paramString, Object paramObject)
  {
    LoadedResourceReference localLoadedResourceReference1 = (LoadedResourceReference)loadedResource.get(paramString);
    if ((localLoadedResourceReference1 != null) && (localLoadedResourceReference1.get() != null))
    {
      if (paramObject == localLoadedResourceReference1.get())
        return null;
      if ((localLoadedResourceReference1.getReferenceCount() > 0) && (DownloadEngine.isBackgroundUpdateRequest()))
        return null;
      Trace.println("Replacing MemoryCache entry (cnt=" + localLoadedResourceReference1.refcnt + ") for " + paramString + "was=" + localLoadedResourceReference1.get().getClass().getName() + " (" + localLoadedResourceReference1.get().hashCode() + ")" + " now=" + paramObject.getClass().getName() + " (" + paramObject.hashCode() + ")", TraceLevel.CACHE);
    }
    LoadedResourceReference localLoadedResourceReference2 = new LoadedResourceReference(paramObject);
    if (!(paramObject instanceof CacheEntry))
      localLoadedResourceReference2.registerReference(new CachedResourceReference(paramObject, refQueue, paramString));
    LoadedResourceReference localLoadedResourceReference3 = (LoadedResourceReference)loadedResource.put(paramString, localLoadedResourceReference2);
    if (localLoadedResourceReference3 != null)
      return localLoadedResourceReference3.get();
    return null;
  }

  public static synchronized Object getLoadedResource(String paramString)
  {
    LoadedResourceReference localLoadedResourceReference = (LoadedResourceReference)loadedResource.get(paramString);
    if (localLoadedResourceReference == null)
      return null;
    Object localObject = localLoadedResourceReference.get();
    if (validateResource(localObject, paramString))
    {
      addResourceReference(localObject, paramString);
      return localObject;
    }
    return null;
  }

  public static synchronized Object removeLoadedResource(String paramString)
  {
    LoadedResourceReference localLoadedResourceReference = (LoadedResourceReference)loadedResource.remove(paramString);
    if (localLoadedResourceReference != null)
    {
      Trace.println("MemoryCache: removed entry " + paramString, TraceLevel.CACHE);
      return localLoadedResourceReference.get();
    }
    return null;
  }

  public static synchronized void clearLoadedResources()
  {
    loadedResource.clear();
    UpdateTracker.clear();
    DownloadEngine.clearNoCacheJarFileList();
  }

  public static synchronized void shutdown()
  {
    closeJars();
    clearLoadedResources();
  }

  private static synchronized void closeJars()
  {
    Iterator localIterator = loadedResource.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      LoadedResourceReference localLoadedResourceReference = (LoadedResourceReference)localEntry.getValue();
      JarFile localJarFile = null;
      if ((localLoadedResourceReference.get() instanceof JarFile))
        localJarFile = (JarFile)localLoadedResourceReference.get();
      if (localJarFile != null)
        try
        {
          localJarFile.close();
        }
        catch (Exception localException)
        {
          Trace.ignored(localException);
        }
    }
    reset();
  }

  static synchronized void addResourceReference(Object paramObject, String paramString)
  {
    LoadedResourceReference localLoadedResourceReference = (LoadedResourceReference)loadedResource.get(paramString);
    if (localLoadedResourceReference != null)
      localLoadedResourceReference.registerReference(new CachedResourceReference(paramObject, refQueue, paramString));
  }

  static synchronized boolean contains(String paramString)
  {
    return loadedResource.containsKey(paramString);
  }

  private static boolean validateResource(Object paramObject, String paramString)
  {
    if (paramObject == null)
      return false;
    if ((paramObject instanceof CacheEntry))
    {
      CacheEntry localCacheEntry = (CacheEntry)paramObject;
      if (Cache.hasIncompatibleCompressEncoding(localCacheEntry))
      {
        removeLoadedResource(paramString);
        return false;
      }
    }
    return true;
  }

  public static synchronized boolean isCacheEntryLoaded(String paramString1, String paramString2)
  {
    Object localObject = getLoadedResource(paramString1);
    if ((localObject instanceof CacheEntry))
    {
      CacheEntry localCacheEntry = (CacheEntry)localObject;
      if (((localCacheEntry.getVersion() == null) && (paramString2 == null)) || ((paramString2 != null) && (paramString2.equals(localCacheEntry.getVersion()))))
        return true;
    }
    return false;
  }

  static int getReferenceCount(String paramString)
  {
    LoadedResourceReference localLoadedResourceReference = (LoadedResourceReference)loadedResource.get(paramString);
    return localLoadedResourceReference == null ? 0 : localLoadedResourceReference.getReferenceCount();
  }

  static
  {
    loadedResource = new HashMap();
    cleanupThread = new LoadedResourceCleanupThread("CacheMemoryCleanUpThread");
    cleanupThread.setDaemon(true);
    cleanupThread.start();
  }

  private static class CachedResourceReference extends WeakReference
  {
    String url;

    public CachedResourceReference(Object paramObject, ReferenceQueue paramReferenceQueue, String paramString)
    {
      super(paramReferenceQueue);
      this.url = paramString;
    }

    public String getURL()
    {
      return this.url;
    }

    public int hashCode()
    {
      return this.url.hashCode();
    }

    public boolean equals(Object paramObject)
    {
      CachedResourceReference localCachedResourceReference = (CachedResourceReference)paramObject;
      return (localCachedResourceReference != null) && (get() == localCachedResourceReference.get()) && (this.url.equals(localCachedResourceReference.getURL()));
    }
  }

  static class LoadedResourceCleanupThread extends Thread
  {
    LoadedResourceCleanupThread(String paramString)
    {
      super();
    }

    public void run()
    {
      while (true)
        try
        {
          MemoryCache.CachedResourceReference localCachedResourceReference = (MemoryCache.CachedResourceReference)MemoryCache.refQueue.remove();
          synchronized (MemoryCache.loadedResource)
          {
            String str = localCachedResourceReference.getURL();
            MemoryCache.LoadedResourceReference localLoadedResourceReference = (MemoryCache.LoadedResourceReference)MemoryCache.loadedResource.get(str);
            if ((localLoadedResourceReference == null) || (!localLoadedResourceReference.deregisterReference(localCachedResourceReference)))
              continue;
            MemoryCache.removeLoadedResource(str);
          }
          continue;
        }
        catch (InterruptedException localInterruptedException)
        {
        }
    }
  }

  private static class LoadedResourceReference
  {
    private Set resourceRefs = new HashSet();
    private int refcnt = 0;
    Object o;

    LoadedResourceReference(Object paramObject)
    {
      this.o = paramObject;
    }

    Object get()
    {
      return this.o;
    }

    synchronized void registerReference(Reference paramReference)
    {
      if (this.resourceRefs.add(paramReference))
        this.refcnt += 1;
    }

    synchronized boolean deregisterReference(Reference paramReference)
    {
      this.refcnt -= 1;
      this.resourceRefs.remove(paramReference);
      if ((this.o instanceof CacheEntry))
        return this.refcnt <= 1;
      return this.refcnt == 0;
    }

    synchronized int getReferenceCount()
    {
      return this.refcnt;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.MemoryCache
 * JD-Core Version:    0.6.0
 */