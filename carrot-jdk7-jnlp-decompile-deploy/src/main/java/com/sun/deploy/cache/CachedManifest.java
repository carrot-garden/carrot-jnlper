package com.sun.deploy.cache;

import com.sun.deploy.Environment;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class CachedManifest extends Manifest
{
  String resourceURL;
  boolean isReduced = false;
  boolean postponePostprocessing = false;
  static Field fEntries = null;
  static Field fAttributes = null;

  CachedManifest(String paramString, byte[] paramArrayOfByte, boolean paramBoolean)
    throws IOException
  {
    this.resourceURL = paramString;
    readFromBytes(paramArrayOfByte);
    this.isReduced = paramBoolean;
  }

  CachedManifest(Manifest paramManifest)
    throws IOException
  {
    try
    {
      if ((fAttributes != null) && (fEntries != null))
      {
        fAttributes.set(this, fAttributes.get(paramManifest));
        fEntries.set(this, fEntries.get(paramManifest));
      }
    }
    catch (Exception localException)
    {
      fAttributes = null;
      fEntries = null;
    }
    this.isReduced = false;
  }

  public synchronized Map getEntries()
  {
    loadFullManifest();
    return super.getEntries();
  }

  private Map getEntriesLocal()
  {
    if (this.isReduced)
      try
      {
        if (fEntries != null)
          return (Map)fEntries.get(this);
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    return getEntries();
  }

  public synchronized Attributes getAttributes(String paramString)
  {
    if (!belongsToReducedManifest(paramString))
      loadFullManifest();
    Map localMap = getEntriesLocal();
    if (localMap != null)
      return (Attributes)localMap.get(paramString);
    return null;
  }

  public synchronized void clear()
  {
    super.clear();
    this.isReduced = false;
  }

  public int hashCode()
  {
    if (this.resourceURL != null)
      return this.resourceURL.hashCode();
    return 0;
  }

  public boolean equals(Object paramObject)
  {
    if ((paramObject instanceof CachedManifest))
    {
      CachedManifest localCachedManifest = (CachedManifest)paramObject;
      if ((this.resourceURL != null) && (this.resourceURL.equals(localCachedManifest.resourceURL)))
        return true;
    }
    return false;
  }

  public Object clone()
  {
    CachedManifest localCachedManifest = (CachedManifest)super.clone();
    localCachedManifest.resourceURL = this.resourceURL;
    localCachedManifest.isReduced = this.isReduced;
    return localCachedManifest;
  }

  synchronized void postprocess()
  {
    if (!Environment.isJavaPlugin())
      getMainAttributes().remove(Attributes.Name.CLASS_PATH);
    makeManifestImmutable(this);
  }

  private CacheEntry getCacheEntry()
  {
    CacheEntry localCacheEntry = (CacheEntry)MemoryCache.getLoadedResource(this.resourceURL);
    if (localCacheEntry == null)
      Trace.println("Missing CacheEntry for " + this.resourceURL + "\n" + localCacheEntry, TraceLevel.CACHE);
    return localCacheEntry;
  }

  private void loadFullManifest()
  {
    if (!this.isReduced)
      return;
    Trace.print("Loading full manifest for " + this.resourceURL, TraceLevel.CACHE);
    CacheEntry localCacheEntry = getCacheEntry();
    if (localCacheEntry != null)
      try
      {
        byte[] arrayOfByte = localCacheEntry.getFullManifestBytes();
        if (arrayOfByte != null)
          try
          {
            Field localField1 = Manifest.class.getDeclaredField("attr");
            localField1.setAccessible(true);
            Field localField2 = Manifest.class.getDeclaredField("entries");
            localField2.setAccessible(true);
            localField1.set(this, new Attributes());
            localField2.set(this, new HashMap());
            this.isReduced = false;
            readFromBytes(arrayOfByte);
          }
          catch (Exception localException)
          {
          }
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
  }

  private int writeCompressed(Manifest paramManifest, OutputStream paramOutputStream)
    throws IOException
  {
    if (paramManifest == null)
      return 0;
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    GZIPOutputStream localGZIPOutputStream = new GZIPOutputStream(localByteArrayOutputStream);
    paramManifest.write(localGZIPOutputStream);
    localGZIPOutputStream.flush();
    localGZIPOutputStream.close();
    localByteArrayOutputStream.close();
    byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
    paramOutputStream.write(arrayOfByte);
    return arrayOfByte.length;
  }

  int writeReduced(OutputStream paramOutputStream)
    throws IOException
  {
    Manifest localManifest = reduce();
    return writeCompressed(localManifest, paramOutputStream);
  }

  int writeFull(OutputStream paramOutputStream)
    throws IOException
  {
    loadFullManifest();
    return writeCompressed(this, paramOutputStream);
  }

  private void readFromBytes(byte[] paramArrayOfByte)
    throws IOException
  {
    ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramArrayOfByte);
    GZIPInputStream localGZIPInputStream = new GZIPInputStream(localByteArrayInputStream);
    read(localGZIPInputStream);
    localGZIPInputStream.close();
    localByteArrayInputStream.close();
  }

  public void read(InputStream paramInputStream)
    throws IOException
  {
    super.read(paramInputStream);
    if (!this.postponePostprocessing)
      postprocess();
  }

  private static boolean belongsToReducedManifest(String paramString)
  {
    return paramString.endsWith("/");
  }

  private Manifest reduce()
  {
    if (this.isReduced)
      return this;
    if (getEntries().size() < 25)
      return null;
    Manifest localManifest = new Manifest();
    int i = 0;
    Attributes localAttributes1 = localManifest.getMainAttributes();
    localAttributes1.putAll(getMainAttributes());
    Map localMap = localManifest.getEntries();
    Iterator localIterator = getEntries().keySet().iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      if (belongsToReducedManifest(str))
      {
        Attributes localAttributes2 = getAttributes(str);
        localMap.put(str, localAttributes2);
      }
      else
      {
        i++;
      }
    }
    if (i < 25)
      return null;
    return localManifest;
  }

  private void replaceAttributesMapWithImmutableMap(Attributes paramAttributes)
  {
    try
    {
      Field localField = Attributes.class.getDeclaredField("map");
      localField.setAccessible(true);
      Map localMap = (Map)localField.get(paramAttributes);
      if (localMap != null)
      {
        localMap = Collections.unmodifiableMap(localMap);
        localField.set(paramAttributes, localMap);
      }
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
  }

  private void replaceEntriesMapWithImmutableMap(Manifest paramManifest)
  {
    try
    {
      Field localField = Manifest.class.getDeclaredField("entries");
      localField.setAccessible(true);
      Map localMap = (Map)localField.get(paramManifest);
      if (localMap != null)
      {
        makeEntriesAttributesImmutable(localMap);
        localMap = Collections.unmodifiableMap(localMap);
        localField.set(paramManifest, localMap);
      }
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
  }

  private void makeEntriesAttributesImmutable(Map paramMap)
  {
    Iterator localIterator = paramMap.keySet().iterator();
    while (localIterator.hasNext())
    {
      Object localObject1 = localIterator.next();
      Object localObject2 = paramMap.get(localObject1);
      if ((localObject2 instanceof Attributes))
        replaceAttributesMapWithImmutableMap((Attributes)localObject2);
    }
  }

  private void makeManifestImmutable(Manifest paramManifest)
  {
    Attributes localAttributes = paramManifest.getMainAttributes();
    replaceAttributesMapWithImmutableMap(localAttributes);
    replaceEntriesMapWithImmutableMap(paramManifest);
  }

  static
  {
    try
    {
      fAttributes = Manifest.class.getDeclaredField("attr");
      fAttributes.setAccessible(true);
      fEntries = Manifest.class.getDeclaredField("entries");
      fEntries.setAccessible(true);
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.CachedManifest
 * JD-Core Version:    0.6.0
 */