package com.sun.javaws.security;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.cache.DeployCacheJarAccess;
import com.sun.deploy.cache.DeployCacheJarAccessImpl;
import com.sun.deploy.cache.SignedAsBlobJarFile;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.JARSigningException;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class SigningInfo
{
  private static DeployCacheJarAccess jarAccess = DeployCacheJarAccessImpl.getJarAccess();
  private CacheEntry ce = null;
  private URL location = null;
  private String version = null;
  private boolean fileInCache;
  private boolean jarIsEmpty;
  private String jarFilePath = null;
  private boolean wasChecked = false;

  public static Certificate[] toCertificateArray(List paramList)
  {
    Iterator localIterator = paramList.iterator();
    int i = 0;
    while (localIterator.hasNext())
    {
      localObject = localIterator.next();
      if ((localObject instanceof CertChain))
        i += ((CertChain)localObject).getLength();
      else
        return null;
    }
    Object localObject = new Certificate[i];
    localIterator = paramList.iterator();
    int j = 0;
    while (localIterator.hasNext())
    {
      Certificate[] arrayOfCertificate = ((CertChain)localIterator.next()).getCertificates();
      System.arraycopy(arrayOfCertificate, 0, localObject, j, arrayOfCertificate.length);
      j += arrayOfCertificate.length;
    }
    return (Certificate)localObject;
  }

  private static boolean setContains(List paramList, Object paramObject)
  {
    if ((paramList == null) || (paramObject == null))
      return false;
    Iterator localIterator = paramList.iterator();
    if (Config.isJavaVersionAtLeast15())
    {
      CertPath localCertPath1 = null;
      CertPath localCertPath2 = ((CodeSigner)paramObject).getSignerCertPath();
      while (localIterator.hasNext())
      {
        localCertPath1 = ((CodeSigner)(CodeSigner)localIterator.next()).getSignerCertPath();
        if (localCertPath2.equals(localCertPath1))
          return true;
      }
      return false;
    }
    while (localIterator.hasNext())
      if (paramObject.equals(localIterator.next()))
        return true;
    return false;
  }

  public static List overlapChainLists(List paramList1, List paramList2)
  {
    if ((paramList1 == null) || (paramList2 == null))
      return null;
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = paramList1.iterator();
    while (localIterator.hasNext())
    {
      Object localObject = localIterator.next();
      if (setContains(paramList2, localObject))
        localArrayList.add(localObject);
    }
    if (localArrayList.isEmpty())
      return null;
    return localArrayList;
  }

  public static List overlapSigners(List paramList, CodeSigner[] paramArrayOfCodeSigner)
  {
    ArrayList localArrayList = new ArrayList();
    if (paramArrayOfCodeSigner == null)
      return localArrayList;
    for (int i = 0; i < paramArrayOfCodeSigner.length; i++)
    {
      if ((paramList != null) && (!setContains(paramList, paramArrayOfCodeSigner[i])))
        continue;
      localArrayList.add(paramArrayOfCodeSigner[i]);
    }
    return localArrayList;
  }

  public static List overlapCertificateChains(List paramList, Certificate[] paramArrayOfCertificate)
  {
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    if (paramArrayOfCertificate == null)
      return localArrayList;
    CertChain localCertChain;
    while ((localCertChain = getAChain(paramArrayOfCertificate, i)) != null)
    {
      if ((paramList == null) || (paramList.contains(localCertChain)))
        localArrayList.add(localCertChain);
      i += localCertChain.getLength();
    }
    return localArrayList;
  }

  private static CertChain getAChain(Certificate[] paramArrayOfCertificate, int paramInt)
  {
    if (paramInt > paramArrayOfCertificate.length - 1)
      return null;
    int i = 0;
    for (i = paramInt; (i < paramArrayOfCertificate.length - 1) && (((X509Certificate)paramArrayOfCertificate[(i + 1)]).getSubjectDN().equals(((X509Certificate)paramArrayOfCertificate[i]).getIssuerDN())); i++);
    return new CertChain(paramArrayOfCertificate, paramInt, i);
  }

  public SigningInfo(URL paramURL, String paramString)
  {
    this.location = paramURL;
    this.version = paramString;
    int i = 0;
    try
    {
      this.jarFilePath = DownloadEngine.getCachedResourceFilePath(paramURL, paramString);
      this.fileInCache = true;
    }
    catch (IOException localIOException1)
    {
      if (paramString != null)
        try
        {
          this.jarFilePath = DownloadEngine.getCachedResourceFilePath(paramURL, null);
          i = 1;
        }
        catch (IOException localIOException2)
        {
          this.fileInCache = false;
        }
      else
        this.fileInCache = false;
    }
    if ((this.fileInCache) && (Cache.isCacheEnabled()))
      this.ce = Cache.getCacheEntry(paramURL, null, i != 0 ? null : paramString);
  }

  public List check()
    throws IOException, JARSigningException
  {
    List localList = null;
    JarFile localJarFile;
    if (this.ce != null)
    {
      localJarFile = this.ce.getJarFile();
      Trace.println("Validating cached jar url=" + this.ce.getURL() + " ffile=" + this.ce.getResourceFilename() + " " + localJarFile, TraceLevel.SECURITY);
      localList = getCommonCodeSignersForJar(localJarFile);
      if ((localList != null) && (localList.isEmpty()))
      {
        localList = null;
        throw new JARSigningException(this.location, this.version, 1);
      }
      this.wasChecked = true;
    }
    else
    {
      localJarFile = null;
      try
      {
        localJarFile = DownloadEngine.getCachedJarFile(this.location, this.version);
        if ((localJarFile instanceof SignedAsBlobJarFile))
          try
          {
            localJarFile = (JarFile)((SignedAsBlobJarFile)localJarFile).clone();
          }
          catch (CloneNotSupportedException localCloneNotSupportedException)
          {
            Trace.ignored(localCloneNotSupportedException);
          }
        else
          localJarFile = null;
        if (localJarFile == null)
          localJarFile = new JarFile(this.jarFilePath);
        localList = getCommonCodeSignersForJar(localJarFile);
        if ((localList != null) && (localList.isEmpty()))
        {
          localList = null;
          throw new JARSigningException(this.location, this.version, 1);
        }
      }
      finally
      {
        if (localJarFile != null)
          localJarFile.close();
      }
    }
    return localList;
  }

  public long getCachedVerificationTimestampt()
  {
    if (this.ce != null)
      return this.ce.getValidationTimestampt();
    return 0L;
  }

  public List getCertificates()
  {
    if (this.ce != null)
    {
      if (Config.isJavaVersionAtLeast15())
        return overlapSigners(null, this.ce.getCodeSigners());
      return overlapCertificateChains(null, this.ce.getCertificates());
    }
    return null;
  }

  public boolean isFileKnownToBeNotCached()
  {
    return !this.fileInCache;
  }

  public boolean isJarKnownToBeEmpty()
  {
    return this.jarIsEmpty;
  }

  public boolean isKnownToBeValidated()
  {
    return (this.ce != null) && (this.ce.getValidationTimestampt() != 0L);
  }

  public boolean isKnownToBeSigned()
  {
    if (this.ce != null)
      return this.ce.isKnownToBeSigned();
    return false;
  }

  public Map getTrustedEntries()
  {
    if (this.ce != null)
      return this.ce.getCachedTrustedEntries();
    return null;
  }

  public void updateCacheIfNeeded(boolean paramBoolean, Map paramMap, long paramLong1, long paramLong2)
  {
    if (!this.wasChecked)
      return;
    updateCache(paramBoolean, paramMap, paramLong1, paramLong2);
  }

  public void updateCache(boolean paramBoolean, Map paramMap, long paramLong1, long paramLong2)
  {
    if (this.ce != null)
      this.ce.updateValidationResults(paramBoolean, paramMap, paramLong1, paramLong2);
  }

  List getCommonCodeSignersForJar(JarFile paramJarFile)
    throws IOException
  {
    List localList = null;
    int i = 0;
    try
    {
      boolean bool = Config.isJavaVersionAtLeast15();
      Object localObject1;
      Enumeration localEnumeration;
      Object localObject2;
      Object localObject3;
      Object localObject4;
      if (jarAccess != null)
      {
        jarAccess.getCodeSource(paramJarFile, new URL("http:"), "/NOP");
        localObject1 = jarAccess.getCodeSources(paramJarFile, null);
        jarAccess.setEagerValidation(paramJarFile, true);
        localEnumeration = jarAccess.entryNames(paramJarFile, localObject1);
        while ((localEnumeration.hasMoreElements()) && ((localList == null) || (!localList.isEmpty())))
        {
          localObject2 = (String)localEnumeration.nextElement();
          localObject3 = jarAccess.getCodeSource(paramJarFile, null, (String)localObject2);
          i = 1;
          if (bool)
          {
            localObject4 = ((CodeSource)localObject3).getCodeSigners();
            if (localObject4 == null)
            {
              if (((String)localObject2).startsWith("META-INF/"))
                continue;
              Trace.println("Found unsigned entry: " + (String)localObject2, TraceLevel.SECURITY);
              throw new JARSigningException(this.location, this.version, 3);
            }
            localList = overlapSigners(localList, localObject4);
          }
          else
          {
            localObject4 = ((CodeSource)localObject3).getCertificates();
            if (localObject4 == null)
            {
              if (((String)localObject2).startsWith("META-INF/"))
                continue;
              Trace.println("Found unsigned entry: " + (String)localObject2, TraceLevel.SECURITY);
              throw new JARSigningException(this.location, this.version, 3);
            }
            localList = overlapCertificateChains(localList, localObject4);
          }
        }
      }
      else
      {
        localEnumeration = paramJarFile.entries();
        while ((localEnumeration.hasMoreElements()) && ((localList == null) || (!localList.isEmpty())))
        {
          localObject1 = new byte[8192];
          localObject2 = (JarEntry)localEnumeration.nextElement();
          localObject3 = ((JarEntry)localObject2).getName();
          if ((!CacheEntry.isSigningRelated((String)localObject3)) && (!((String)localObject3).endsWith("/")))
          {
            i = 1;
            localObject4 = paramJarFile.getInputStream((ZipEntry)localObject2);
            while (((InputStream)localObject4).read(localObject1, 0, localObject1.length) != -1);
            ((InputStream)localObject4).close();
            Object localObject5;
            if (bool)
            {
              localObject5 = ((JarEntry)localObject2).getCodeSigners();
              if (localObject5 == null)
              {
                if (((String)localObject3).startsWith("META-INF/"))
                  continue;
                Trace.println("Found unsigned entry: " + (String)localObject3, TraceLevel.SECURITY);
                throw new JARSigningException(this.location, this.version, 3);
              }
              localList = overlapSigners(localList, localObject5);
            }
            else
            {
              localObject5 = ((JarEntry)localObject2).getCertificates();
              if (localObject5 == null)
              {
                if (((String)localObject3).startsWith("META-INF/"))
                  continue;
                Trace.println("Found unsigned entry: " + (String)localObject3, TraceLevel.SECURITY);
                throw new JARSigningException(this.location, this.version, 3);
              }
              localList = overlapCertificateChains(localList, localObject5);
            }
          }
        }
      }
    }
    catch (JARSigningException localJARSigningException)
    {
      throw localJARSigningException;
    }
    catch (IOException localIOException)
    {
      throw new JARSigningException(this.location, this.version, 2, localIOException);
    }
    catch (SecurityException localSecurityException)
    {
      throw new JARSigningException(this.location, this.version, 2, localSecurityException);
    }
    this.jarIsEmpty = (i == 0);
    return (List)(List)(List)(List)(List)localList;
  }

  static class CertChain
  {
    Certificate[] certs;

    CertChain(Certificate[] paramArrayOfCertificate, int paramInt1, int paramInt2)
    {
      this.certs = new Certificate[paramInt2 - paramInt1 + 1];
      for (int i = 0; i < this.certs.length; i++)
        this.certs[i] = paramArrayOfCertificate[(paramInt1 + i)];
    }

    Certificate[] getCertificates()
    {
      return this.certs;
    }

    int getLength()
    {
      return this.certs.length;
    }

    public int hashCode()
    {
      if (this.certs.length == 0)
        return 0;
      return this.certs[0].hashCode();
    }

    public boolean equals(Object paramObject)
    {
      CertChain localCertChain = (CertChain)paramObject;
      if ((localCertChain == null) || (localCertChain.getLength() != getLength()))
        return false;
      for (int i = 0; i < this.certs.length; i++)
        if (!localCertChain.certs[i].equals(this.certs[i]))
          return false;
      return true;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.security.SigningInfo
 * JD-Core Version:    0.6.0
 */