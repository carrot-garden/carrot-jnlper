package com.sun.deploy.cache;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class CachedJarFile14 extends JarFile
{
  private Reference certificatesRef;
  private Reference signerMapCertRef = null;
  private boolean hasStrictSingleSigning;
  private Reference manifestRef = null;
  private Reference codeSourcesRef;
  private Reference codeSourceCertCacheRef = null;
  private String resourceURL;
  private File indexFile;
  private int[] emptySignerIndices = new int[0];
  private static Enumeration emptyEnumeration = new Enumeration()
  {
    public boolean hasMoreElements()
    {
      return false;
    }

    public Object nextElement()
    {
      throw new NoSuchElementException();
    }
  };
  private static Iterator emptyIterator = Collections.EMPTY_MAP.keySet().iterator();

  public String getName()
  {
    String str = super.getName();
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager == null)
      return str;
    try
    {
      localSecurityManager.checkPermission(new RuntimePermission("accessDeploymentCache"));
      return str;
    }
    catch (SecurityException localSecurityException)
    {
    }
    return "";
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    try
    {
      return new CachedJarFile14(new File(super.getName()), this.certificatesRef, this.signerMapCertRef, this.hasStrictSingleSigning, this.manifestRef, this.codeSourcesRef, this.codeSourceCertCacheRef, this.resourceURL, this.indexFile);
    }
    catch (IOException localIOException)
    {
    }
    throw new CloneNotSupportedException();
  }

  private CachedJarFile14(File paramFile1, Reference paramReference1, Reference paramReference2, boolean paramBoolean, Reference paramReference3, Reference paramReference4, Reference paramReference5, String paramString, File paramFile2)
    throws IOException
  {
    super(paramFile1, false);
    this.certificatesRef = paramReference1;
    this.signerMapCertRef = paramReference2;
    this.hasStrictSingleSigning = paramBoolean;
    this.manifestRef = paramReference3;
    this.codeSourcesRef = paramReference4;
    this.codeSourceCertCacheRef = paramReference5;
    this.resourceURL = paramString;
    this.indexFile = paramFile2;
    MemoryCache.addResourceReference(this, paramString);
  }

  protected CachedJarFile14(CacheEntry paramCacheEntry)
    throws IOException
  {
    super(new File(paramCacheEntry.getResourceFilename()), false);
    this.resourceURL = paramCacheEntry.getURL();
    this.certificatesRef = new SoftReference(null);
    this.signerMapCertRef = new SoftReference(null);
    this.hasStrictSingleSigning = false;
    this.manifestRef = new SoftReference(null);
    this.codeSourcesRef = new SoftReference(null);
    this.codeSourceCertCacheRef = new SoftReference(null);
    this.indexFile = paramCacheEntry.getIndexFile();
    MemoryCache.addResourceReference(this, this.resourceURL);
  }

  public ZipEntry getEntry(String paramString)
  {
    ZipEntry localZipEntry = super.getEntry(paramString);
    if (localZipEntry != null)
      return new JarFileEntry(localZipEntry);
    return null;
  }

  public Enumeration entries()
  {
    Enumeration localEnumeration = super.entries();
    return new Enumeration(localEnumeration)
    {
      private final Enumeration val$enum14;

      public boolean hasMoreElements()
      {
        return this.val$enum14.hasMoreElements();
      }

      public Object nextElement()
      {
        try
        {
          ZipEntry localZipEntry = (ZipEntry)this.val$enum14.nextElement();
          return new CachedJarFile14.JarFileEntry(CachedJarFile14.this, localZipEntry);
        }
        catch (InternalError localInternalError)
        {
        }
        throw new InternalError("Error in CachedJarFile entries");
      }
    };
  }

  private synchronized CacheEntry getCacheEntry()
  {
    if (this.resourceURL == null)
      return null;
    CacheEntry localCacheEntry = (CacheEntry)MemoryCache.getLoadedResource(this.resourceURL);
    if ((localCacheEntry == null) || (!this.indexFile.equals(localCacheEntry.getIndexFile())))
    {
      String str = "CachedJarFile.getCacheEntry: " + this.indexFile + " != " + localCacheEntry.getIndexFile() + " for " + this.resourceURL;
      Trace.println(str, TraceLevel.CACHE);
      localCacheEntry = CachedJarFile.recoverCacheEntry(this.indexFile, this.resourceURL);
      if (localCacheEntry != null)
        clearReferences();
    }
    return localCacheEntry;
  }

  public synchronized Manifest getManifest()
    throws IOException
  {
    if (this.manifestRef == null)
      return null;
    Manifest localManifest = (Manifest)this.manifestRef.get();
    if (localManifest == null)
    {
      CacheEntry localCacheEntry = getCacheEntry();
      if (localCacheEntry != null)
        localManifest = localCacheEntry.getManifest();
      else
        Trace.println("Warning: NULL cache entry for loaded resource!", TraceLevel.CACHE);
      if (localManifest != null)
        this.manifestRef = new SoftReference(localManifest);
      else
        this.manifestRef = null;
    }
    return localManifest;
  }

  private synchronized Map getCertificateMap()
  {
    if (this.signerMapCertRef == null)
      return null;
    Map localMap = (Map)this.signerMapCertRef.get();
    if (localMap == null)
    {
      CacheEntry localCacheEntry = getCacheEntry();
      if (localCacheEntry != null)
      {
        localMap = localCacheEntry.getCertificateMap();
        if (localMap != null)
        {
          this.signerMapCertRef = new SoftReference(localMap);
          if (!localMap.isEmpty())
            this.hasStrictSingleSigning = localCacheEntry.hasStrictSingleSigning();
        }
        else
        {
          this.signerMapCertRef = null;
        }
      }
      else
      {
        Trace.println("Missing CacheEntry for " + this.resourceURL + "\n" + localCacheEntry, TraceLevel.CACHE);
      }
    }
    return localMap;
  }

  private synchronized Certificate[] getCertificates()
  {
    if (this.certificatesRef == null)
      return null;
    Certificate[] arrayOfCertificate = (Certificate[])(Certificate[])this.certificatesRef.get();
    if (arrayOfCertificate == null)
    {
      CacheEntry localCacheEntry = getCacheEntry();
      if (localCacheEntry != null)
      {
        arrayOfCertificate = localCacheEntry.getCertificates();
        if (arrayOfCertificate != null)
          this.certificatesRef = new SoftReference(arrayOfCertificate);
        else
          this.certificatesRef = null;
      }
      else
      {
        Trace.println("Missing CacheEntry for " + this.resourceURL + "\n" + localCacheEntry, TraceLevel.CACHE);
      }
    }
    return arrayOfCertificate;
  }

  private synchronized void clearReferences()
  {
    CachedJarFile.clear(new Reference[] { this.certificatesRef, this.signerMapCertRef, this.manifestRef, this.codeSourcesRef, this.codeSourceCertCacheRef });
  }

  private int[] findMatchingSignerIndices(CodeSource paramCodeSource)
  {
    Map localMap = getCodeSourceCertCache();
    if (localMap == null)
      return this.emptySignerIndices;
    Iterator localIterator = localMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      if (localEntry.getValue().equals(paramCodeSource))
        return (int[])(int[])localEntry.getKey();
    }
    if (paramCodeSource.getCertificates() == null)
      return this.emptySignerIndices;
    return null;
  }

  Enumeration entryNames(CodeSource[] paramArrayOfCodeSource)
  {
    int i = 0;
    ArrayList localArrayList = new ArrayList(paramArrayOfCodeSource.length);
    for (int j = 0; j < paramArrayOfCodeSource.length; j++)
    {
      localObject = findMatchingSignerIndices(paramArrayOfCodeSource[j]);
      if (localObject == null)
        continue;
      if (localObject.length > 0)
        localArrayList.add(localObject);
      else
        i = 1;
    }
    Map localMap1 = getCertificateMap();
    if ((localMap1 != null) && (!localMap1.isEmpty()) && (this.hasStrictSingleSigning) && (!localArrayList.isEmpty()))
    {
      localMap1 = null;
      i = 1;
      localArrayList.clear();
    }
    Object localObject = localArrayList;
    Map localMap2 = localMap1;
    Iterator localIterator = localMap2 != null ? localMap2.keySet().iterator() : emptyIterator;
    Enumeration localEnumeration = i != 0 ? unsignedEntryNames(localMap2) : emptyEnumeration;
    return (Enumeration)new Enumeration(localIterator, (List)localObject, localMap2, localEnumeration)
    {
      String name;
      private final Iterator val$signerKeys;
      private final List val$signersReq;
      private final Map val$signerMap;
      private final Enumeration val$enum2;

      public boolean hasMoreElements()
      {
        if (this.name != null)
          return true;
        while (this.val$signerKeys.hasNext())
        {
          String str = (String)this.val$signerKeys.next();
          if (this.val$signersReq.contains((int[])(int[])this.val$signerMap.get(str)))
          {
            this.name = str;
            return true;
          }
          Trace.println("entryNames checking signer failed for " + str, TraceLevel.CACHE);
        }
        if (this.val$enum2.hasMoreElements())
        {
          this.name = ((String)this.val$enum2.nextElement());
          return true;
        }
        return false;
      }

      public Object nextElement()
      {
        if (hasMoreElements())
        {
          String str = this.name;
          this.name = null;
          return str;
        }
        throw new NoSuchElementException();
      }
    };
  }

  private Enumeration unsignedEntryNames(Map paramMap)
  {
    Enumeration localEnumeration = entries();
    return new Enumeration(localEnumeration, paramMap)
    {
      String name;
      private final Enumeration val$entries;
      private final Map val$signerMap;

      public boolean hasMoreElements()
      {
        if (this.name != null)
          return true;
        while (this.val$entries.hasMoreElements())
        {
          ZipEntry localZipEntry = (ZipEntry)this.val$entries.nextElement();
          String str = localZipEntry.getName();
          if ((localZipEntry.isDirectory()) || (CacheEntry.isSigningRelated(str)))
            continue;
          if ((this.val$signerMap == null) || (this.val$signerMap.get(str) == null))
          {
            this.name = str;
            return true;
          }
        }
        return false;
      }

      public Object nextElement()
      {
        if (hasMoreElements())
        {
          String str = this.name;
          this.name = null;
          return str;
        }
        throw new NoSuchElementException();
      }
    };
  }

  synchronized CodeSource[] getCodeSources(URL paramURL)
  {
    if (this.codeSourcesRef == null)
      return null;
    CodeSource[] arrayOfCodeSource = (CodeSource[])(CodeSource[])this.codeSourcesRef.get();
    if (arrayOfCodeSource == null)
    {
      CacheEntry localCacheEntry = getCacheEntry();
      if (localCacheEntry != null)
      {
        arrayOfCodeSource = localCacheEntry.getCodeSources(paramURL);
        if (arrayOfCodeSource != null)
          this.codeSourcesRef = new SoftReference(arrayOfCodeSource);
        else
          this.codeSourcesRef = null;
      }
      else
      {
        Trace.println("Missing CacheEntry for " + this.resourceURL + "\n" + localCacheEntry, TraceLevel.CACHE);
      }
    }
    return arrayOfCodeSource;
  }

  synchronized Map getCodeSourceCertCache()
  {
    if (this.codeSourceCertCacheRef == null)
      return null;
    Map localMap = (Map)this.codeSourceCertCacheRef.get();
    if (localMap == null)
    {
      CacheEntry localCacheEntry = getCacheEntry();
      if (localCacheEntry != null)
      {
        localMap = localCacheEntry.getCodeSourceCertCache();
        if (localMap != null)
          this.codeSourceCertCacheRef = new SoftReference(localMap);
        else
          this.codeSourceCertCacheRef = null;
      }
      else
      {
        Trace.println("Missing CacheEntry for " + this.resourceURL + "\n" + localCacheEntry, TraceLevel.CACHE);
      }
    }
    return localMap;
  }

  synchronized CodeSource getCodeSource(URL paramURL, String paramString)
  {
    Map localMap1 = getCertificateMap();
    int[] arrayOfInt = null;
    if ((localMap1 == null) || (localMap1.isEmpty()))
      arrayOfInt = null;
    else if (this.hasStrictSingleSigning)
    {
      if ((!CacheEntry.isSigningRelated(paramString)) && (!paramString.endsWith("/")))
        arrayOfInt = (int[])(int[])localMap1.get(null);
      else
        arrayOfInt = null;
    }
    else
      arrayOfInt = (int[])(int[])localMap1.get(paramString);
    if (arrayOfInt != null)
    {
      Map localMap2 = getCodeSourceCertCache();
      if (localMap2 != null)
        return (CodeSource)localMap2.get(arrayOfInt);
    }
    return CacheEntry.getUnsignedCS(paramURL);
  }

  private class JarFileEntry extends JarEntry
  {
    JarFileEntry(ZipEntry arg2)
    {
      super();
    }

    public Attributes getAttributes()
      throws IOException
    {
      Manifest localManifest = CachedJarFile14.this.getManifest();
      if (localManifest != null)
        return localManifest.getAttributes(getName());
      return null;
    }

    public Certificate[] getCertificates()
    {
      Certificate[] arrayOfCertificate1 = null;
      int[] arrayOfInt = getCertIndices();
      Certificate[] arrayOfCertificate2 = CachedJarFile14.this.getCertificates();
      if ((arrayOfCertificate2 != null) && (arrayOfInt != null))
      {
        arrayOfCertificate1 = new Certificate[arrayOfCertificate2.length];
        for (int i = 0; i < arrayOfCertificate2.length; i++)
          arrayOfCertificate1[i] = arrayOfCertificate2[arrayOfInt[i]];
      }
      return arrayOfCertificate1;
    }

    private int[] getCertIndices()
    {
      Map localMap = CachedJarFile14.this.getCertificateMap();
      String str = getName();
      if ((localMap == null) || (localMap.isEmpty()))
        return null;
      if (CachedJarFile14.this.hasStrictSingleSigning)
      {
        if ((!CacheEntry.isSigningRelated(str)) && (!str.endsWith("/")))
          return (int[])(int[])localMap.get(null);
        return null;
      }
      return (int[])(int[])localMap.get(str);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.CachedJarFile14
 * JD-Core Version:    0.6.0
 */