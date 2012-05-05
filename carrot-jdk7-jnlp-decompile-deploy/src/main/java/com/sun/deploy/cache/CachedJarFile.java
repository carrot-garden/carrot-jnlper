package com.sun.deploy.cache;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.CertPath;
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

public class CachedJarFile extends JarFile
{
  private Reference signersRef;
  private Reference signerMapRef = null;
  private boolean hasStrictSingleSigning;
  private Reference manRef = null;
  private Reference codeSourcesRef;
  private Reference codeSourceCacheRef = null;
  private final String resourceURL;
  private final File indexFile;
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
      return new CachedJarFile(new File(super.getName()), this.signersRef, this.signerMapRef, this.hasStrictSingleSigning, this.manRef, this.codeSourcesRef, this.codeSourceCacheRef, this.resourceURL, this.indexFile);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    throw new CloneNotSupportedException();
  }

  private CachedJarFile(File paramFile1, Reference paramReference1, Reference paramReference2, boolean paramBoolean, Reference paramReference3, Reference paramReference4, Reference paramReference5, String paramString, File paramFile2)
    throws IOException
  {
    super(paramFile1, false);
    this.signersRef = paramReference1;
    this.signerMapRef = paramReference2;
    this.hasStrictSingleSigning = paramBoolean;
    this.manRef = paramReference3;
    this.codeSourcesRef = paramReference4;
    this.codeSourceCacheRef = paramReference5;
    this.resourceURL = paramString;
    this.indexFile = paramFile2;
    ensureAncestorKnowsAboutManifest(this);
    MemoryCache.addResourceReference(this, paramString);
  }

  private static void ensureAncestorKnowsAboutManifest(JarFile paramJarFile)
    throws IOException
  {
    if (!Config.isJavaVersionAtLeast16())
      try
      {
        AccessController.doPrivileged(new PrivilegedExceptionAction(paramJarFile)
        {
          private final JarFile val$jar;

          public Object run()
            throws IOException
          {
            try
            {
              Field localField = Manifest.class.getDeclaredField("manLoaded");
              if (localField != null)
              {
                localField.setAccessible(true);
                localField.setBoolean(this.val$jar, true);
                return null;
              }
            }
            catch (Exception localException)
            {
            }
            return null;
          }
        });
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        throw ((IOException)localPrivilegedActionException.getException());
      }
  }

  protected CachedJarFile(CacheEntry paramCacheEntry)
    throws IOException
  {
    super(new File(paramCacheEntry.getResourceFilename()), false);
    this.resourceURL = paramCacheEntry.getURL();
    this.signersRef = new SoftReference(null);
    this.signerMapRef = new SoftReference(null);
    this.hasStrictSingleSigning = false;
    this.manRef = new SoftReference(null);
    this.codeSourcesRef = new SoftReference(null);
    this.codeSourceCacheRef = new SoftReference(null);
    this.indexFile = paramCacheEntry.getIndexFile();
    ensureAncestorKnowsAboutManifest(this);
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
      private final Enumeration val$entryList;

      public boolean hasMoreElements()
      {
        return this.val$entryList.hasMoreElements();
      }

      public Object nextElement()
      {
        try
        {
          ZipEntry localZipEntry = (ZipEntry)this.val$entryList.nextElement();
          return new CachedJarFile.JarFileEntry(CachedJarFile.this, localZipEntry);
        }
        catch (InternalError localInternalError)
        {
        }
        throw new InternalError("Error in CachedJarFile entries");
      }
    };
  }

  public synchronized Manifest getManifest()
    throws IOException
  {
    if (this.manRef == null)
      return null;
    Manifest localManifest = (Manifest)this.manRef.get();
    if (localManifest == null)
    {
      CacheEntry localCacheEntry = getCacheEntry();
      if (localCacheEntry != null)
        localManifest = localCacheEntry.getManifest();
      else
        Trace.print("Warning: NULL cache entry for loaded resource!");
      if (localManifest != null)
        this.manRef = new SoftReference(localManifest);
      else
        this.manRef = null;
    }
    return localManifest;
  }

  public URL getResourceURL()
  {
    try
    {
      return new URL(this.resourceURL);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignored(localMalformedURLException);
    }
    return null;
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
      localCacheEntry = recoverCacheEntry(this.indexFile, this.resourceURL);
      if (localCacheEntry != null)
        clearReferences();
    }
    return localCacheEntry;
  }

  static CacheEntry recoverCacheEntry(File paramFile, String paramString)
  {
    CacheEntry localCacheEntry = recoverOldCacheEntry(paramFile, paramString);
    if (localCacheEntry != null)
    {
      MemoryCache.addLoadedResource(paramString, localCacheEntry);
      return localCacheEntry;
    }
    return recoverWithLatestVersionCacheEntry(paramString);
  }

  private static CacheEntry recoverOldCacheEntry(File paramFile, String paramString)
  {
    CacheEntry localCacheEntry = null;
    if ((paramFile != null) && (paramFile.isFile()))
    {
      try
      {
        localCacheEntry = Cache.getCacheEntryFromFile(paramFile, true);
        Trace.println("Recovered memory CacheEntry from: " + paramString, TraceLevel.CACHE);
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
      if (localCacheEntry == null)
        Trace.println("Failed to recover old CacheEntry for " + paramString + "from " + paramFile, TraceLevel.CACHE);
    }
    return localCacheEntry;
  }

  private static CacheEntry recoverWithLatestVersionCacheEntry(String paramString)
  {
    CacheEntry localCacheEntry = null;
    try
    {
      localCacheEntry = Cache.getCacheEntry(new URL(paramString), null, null);
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
    if (localCacheEntry != null)
      Trace.println("Recovered CacheEntry: " + localCacheEntry, TraceLevel.CACHE);
    else
      Trace.println("Failed to recover with latest CacheEntry", TraceLevel.CACHE);
    return localCacheEntry;
  }

  private synchronized Map getSignerMap()
  {
    if (this.signerMapRef == null)
      return null;
    Map localMap = (Map)this.signerMapRef.get();
    if (localMap == null)
    {
      CacheEntry localCacheEntry = getCacheEntry();
      if (localCacheEntry != null)
      {
        localMap = localCacheEntry.getSignerMap();
        if (localMap != null)
        {
          this.signerMapRef = new SoftReference(localMap);
          if (!localMap.isEmpty())
            this.hasStrictSingleSigning = localCacheEntry.hasStrictSingleSigning();
        }
        else
        {
          this.signerMapRef = null;
        }
      }
      else
      {
        Trace.println("getSignerMap failed to get CacheEntry for " + this.resourceURL, TraceLevel.CACHE);
      }
    }
    return localMap;
  }

  private synchronized CodeSigner[] getSigners()
  {
    if (this.signersRef == null)
      return null;
    CodeSigner[] arrayOfCodeSigner = (CodeSigner[])(CodeSigner[])this.signersRef.get();
    if (arrayOfCodeSigner == null)
    {
      CacheEntry localCacheEntry = getCacheEntry();
      if (localCacheEntry != null)
      {
        arrayOfCodeSigner = localCacheEntry.getCodeSigners();
        if (arrayOfCodeSigner != null)
          this.signersRef = new SoftReference(arrayOfCodeSigner);
        else
          this.signersRef = null;
      }
      else
      {
        Trace.println("getSigners failed to get CacheEntry for " + this.resourceURL, TraceLevel.CACHE);
      }
    }
    return arrayOfCodeSigner;
  }

  private static void replaceMapFieldWithImmutableMap(Class paramClass, Object paramObject, String paramString)
  {
    try
    {
      Field localField = paramClass.getDeclaredField(paramString);
      localField.setAccessible(true);
      Map localMap = (Map)localField.get(paramObject);
      if (localMap != null)
      {
        localMap = Collections.unmodifiableMap(localMap);
        localField.set(paramObject, localMap);
      }
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
  }

  private static Map makeAttributesImmutable(Attributes paramAttributes)
  {
    Iterator localIterator = paramAttributes.keySet().iterator();
    while (localIterator.hasNext())
    {
      Object localObject1 = localIterator.next();
      Object localObject2 = paramAttributes.get(localObject1);
      if ((localObject2 instanceof Attributes))
        makeAttributesImmutable((Attributes)localObject2);
    }
    replaceMapFieldWithImmutableMap(Attributes.class, paramAttributes, "map");
    return paramAttributes;
  }

  static void makeManifestImmutable(Manifest paramManifest)
  {
    Attributes localAttributes = paramManifest.getMainAttributes();
    makeAttributesImmutable(localAttributes);
    replaceMapFieldWithImmutableMap(Manifest.class, paramManifest, "entries");
  }

  public URL getURL()
  {
    try
    {
      return new URL(this.resourceURL);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignored(localMalformedURLException);
    }
    return null;
  }

  private synchronized void clearReferences()
  {
    clear(new Reference[] { this.codeSourceCacheRef, this.codeSourcesRef, this.manRef, this.signerMapRef, this.signersRef });
  }

  static void clear(Reference[] paramArrayOfReference)
  {
    if (paramArrayOfReference == null)
      return;
    for (int i = 0; i < paramArrayOfReference.length; i++)
    {
      if (paramArrayOfReference[i] == null)
        continue;
      paramArrayOfReference[i].clear();
    }
  }

  private int[] findMatchingSignerIndices(CodeSource paramCodeSource)
  {
    Map localMap = getCodeSourceCache();
    if (localMap == null)
      return this.emptySignerIndices;
    Iterator localIterator = localMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      if (localEntry.getValue().equals(paramCodeSource))
        return (int[])(int[])localEntry.getKey();
    }
    if (paramCodeSource.getCodeSigners() == null)
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
    Map localMap1 = getSignerMap();
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
        Trace.println("getCodeSources failed to get CacheEntry for " + this.resourceURL, TraceLevel.CACHE);
      }
    }
    return arrayOfCodeSource;
  }

  synchronized Map getCodeSourceCache()
  {
    if (this.codeSourceCacheRef == null)
      return null;
    Map localMap = (Map)this.codeSourceCacheRef.get();
    if (localMap == null)
    {
      CacheEntry localCacheEntry = getCacheEntry();
      if (localCacheEntry != null)
      {
        localMap = localCacheEntry.getCodeSourceCache();
        if (localMap != null)
          this.codeSourceCacheRef = new SoftReference(localMap);
        else
          this.codeSourceCacheRef = null;
      }
      else
      {
        Trace.println("getCodeSourceCache failed to get CacheEntry for " + this.resourceURL, TraceLevel.CACHE);
      }
    }
    return localMap;
  }

  synchronized CodeSource getCodeSource(URL paramURL, String paramString)
  {
    Map localMap1 = getSignerMap();
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
      Map localMap2 = getCodeSourceCache();
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
      Manifest localManifest = CachedJarFile.this.getManifest();
      if (localManifest != null)
      {
        Attributes localAttributes = localManifest.getAttributes(getName());
        return localAttributes;
      }
      return null;
    }

    public Certificate[] getCertificates()
    {
      Object localObject = null;
      int[] arrayOfInt = getSignerIndices();
      CodeSigner[] arrayOfCodeSigner = CachedJarFile.this.getSigners();
      if ((arrayOfCodeSigner != null) && (arrayOfInt != null))
      {
        ArrayList localArrayList = new ArrayList();
        for (int i = 0; i < arrayOfInt.length; i++)
          localArrayList.addAll(arrayOfCodeSigner[arrayOfInt[i]].getSignerCertPath().getCertificates());
        return (Certificate[])(Certificate[])localArrayList.toArray(new Certificate[localArrayList.size()]);
      }
      return localObject;
    }

    public CodeSigner[] getCodeSigners()
    {
      CodeSigner[] arrayOfCodeSigner1 = null;
      int[] arrayOfInt = getSignerIndices();
      CodeSigner[] arrayOfCodeSigner2 = CachedJarFile.this.getSigners();
      if ((arrayOfCodeSigner2 != null) && (arrayOfInt != null))
      {
        arrayOfCodeSigner1 = new CodeSigner[arrayOfInt.length];
        for (int i = 0; i < arrayOfInt.length; i++)
        {
          if (arrayOfCodeSigner2 == null)
            continue;
          arrayOfCodeSigner1[i] = arrayOfCodeSigner2[arrayOfInt[i]];
        }
      }
      return arrayOfCodeSigner1;
    }

    private int[] getSignerIndices()
    {
      Map localMap = CachedJarFile.this.getSignerMap();
      String str = getName();
      if ((localMap == null) || (localMap.isEmpty()))
        return null;
      if (CachedJarFile.this.hasStrictSingleSigning)
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
 * Qualified Name:     com.sun.deploy.cache.CachedJarFile
 * JD-Core Version:    0.6.0
 */