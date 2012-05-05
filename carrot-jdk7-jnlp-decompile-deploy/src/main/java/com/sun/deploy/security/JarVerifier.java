package com.sun.deploy.security;

import com.sun.deploy.cache.SignedAsBlobJarFile;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.DownloadEngine.DownloadDelegate;
import com.sun.deploy.net.JARSigningException;
import com.sun.deploy.util.BlackList;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipException;
import sun.misc.JavaUtilJarAccess;
import sun.misc.SharedSecrets;

public abstract class JarVerifier
{
  protected File jarFile;
  protected File nativePath;
  protected URL jarLocation;
  protected String jarVersion;
  protected boolean hasSingleCodeSource = false;
  protected boolean hasOnlySignedEntries = false;
  protected boolean hasMissingSignedEntries = false;
  protected Manifest manifest;
  protected Map signerMapCert = new HashMap();
  protected Map codeSourceCertCache = new HashMap();
  protected int[] singleSignerIndicesCert;
  protected List signerCerts = new ArrayList();
  protected Map signerMap = new HashMap();
  protected Map codeSourceCache = new HashMap();
  protected List signersCS = new ArrayList();
  protected int[] singleSignerIndicesCS;

  protected JarVerifier(URL paramURL, String paramString, File paramFile1, File paramFile2)
  {
    this.jarFile = paramFile1;
    this.nativePath = paramFile2;
    this.jarLocation = paramURL;
    this.jarVersion = paramString;
  }

  public URL getJarLocation()
  {
    return this.jarLocation;
  }

  public abstract void validate(DownloadEngine.DownloadDelegate paramDownloadDelegate)
    throws IOException, JARSigningException;

  public Manifest getManifest()
  {
    return this.manifest;
  }

  public Map getSignerMapCert()
  {
    return this.signerMapCert;
  }

  public Map getCodeSourceCertCache()
  {
    return this.codeSourceCertCache;
  }

  public Map getSignerMap()
  {
    return this.signerMap;
  }

  public Map getCodeSourceCache()
  {
    return this.codeSourceCache;
  }

  public List getSignerCerts()
  {
    return this.signerCerts;
  }

  public List getSignersCS()
  {
    return this.signersCS;
  }

  public int[] getSingleSignerIndicesCert()
  {
    return this.singleSignerIndicesCert;
  }

  public int[] getSingleSignerIndicesCS()
  {
    return this.singleSignerIndicesCS;
  }

  public boolean hasOnlySignedEntries()
  {
    return this.hasOnlySignedEntries;
  }

  public boolean hasSingleCodeSource()
  {
    return this.hasSingleCodeSource;
  }

  public boolean hasMissingSignedEntries()
  {
    return this.hasMissingSignedEntries;
  }

  public static JarVerifier create(URL paramURL, String paramString, File paramFile1, File paramFile2)
  {
    JarVerifier localJarVerifier = JarAsBLOBVerifier.create(paramURL, paramString, paramFile1, paramFile2);
    if (localJarVerifier == null)
    {
      localJarVerifier = EnhancedJarVerifier.create(paramURL, paramString, paramFile1, paramFile2);
      if (localJarVerifier == null)
        localJarVerifier = SimpleJarVerifier.create(paramURL, paramString, paramFile1, paramFile2);
    }
    return localJarVerifier;
  }

  static void readAndMaybeSaveStreamTo(InputStream paramInputStream, boolean paramBoolean, String paramString, File paramFile)
    throws IOException
  {
    BufferedOutputStream localBufferedOutputStream = null;
    byte[] arrayOfByte = new byte[16384];
    try
    {
      if ((paramBoolean) && (paramFile != null))
      {
        File localFile = new File(paramFile, paramString).getCanonicalFile();
        if (localFile.getParentFile().equals(paramFile))
        {
          localFile.getParentFile().mkdirs();
          localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile));
        }
      }
      int i;
      while ((i = paramInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1)
      {
        if (localBufferedOutputStream == null)
          continue;
        localBufferedOutputStream.write(arrayOfByte, 0, i);
      }
    }
    finally
    {
      if (localBufferedOutputStream != null)
      {
        localBufferedOutputStream.close();
        localBufferedOutputStream = null;
      }
      if (paramInputStream != null)
        paramInputStream.close();
    }
  }

  protected void processCertificates(JarFile paramJarFile, JarEntry paramJarEntry, String paramString)
    throws MalformedURLException
  {
    HashMap localHashMap = new HashMap();
    Certificate[] arrayOfCertificate = null;
    if (this.hasSingleCodeSource)
    {
      this.signerMapCert.put(paramString, this.singleSignerIndicesCert);
    }
    else
    {
      Object localObject;
      if (paramJarEntry != null)
      {
        arrayOfCertificate = paramJarEntry.getCertificates();
      }
      else
      {
        localObject = null;
        JavaUtilJarAccess localJavaUtilJarAccess = SharedSecrets.javaUtilJarAccess();
        localObject = localJavaUtilJarAccess.getCodeSource(paramJarFile, this.jarLocation, paramString);
        arrayOfCertificate = localObject != null ? ((CodeSource)localObject).getCertificates() : null;
      }
      if ((arrayOfCertificate != null) && (arrayOfCertificate.length > 0))
      {
        localObject = new int[arrayOfCertificate.length];
        for (int i = 0; i < arrayOfCertificate.length; i++)
        {
          j = this.signerCerts.indexOf(arrayOfCertificate[i]);
          if (j == -1)
          {
            j = this.signerCerts.size();
            this.signerCerts.add(arrayOfCertificate[i]);
          }
          localObject[i] = j;
        }
        String str = String.valueOf(localObject.length);
        for (int j = 0; j < localObject.length; j++)
          str = str + " " + localObject[j];
        int[] arrayOfInt = (int[])(int[])localHashMap.get(str);
        if (arrayOfInt == null)
        {
          localHashMap.put(str, localObject);
          CodeSource localCodeSource = new CodeSource(this.jarLocation, arrayOfCertificate);
          this.codeSourceCertCache.put(localObject, localCodeSource);
        }
        else
        {
          localObject = arrayOfInt;
        }
        this.signerMapCert.put(paramString, localObject);
      }
    }
  }

  protected void processSigners(JarFile paramJarFile, JarEntry paramJarEntry, String paramString)
    throws MalformedURLException
  {
    if (!Config.isJavaVersionAtLeast15())
      return;
    CodeSigner[] arrayOfCodeSigner = null;
    HashMap localHashMap = new HashMap();
    if (this.hasSingleCodeSource)
    {
      this.signerMap.put(paramString, this.singleSignerIndicesCS);
    }
    else
    {
      Object localObject;
      if (paramJarEntry != null)
      {
        arrayOfCodeSigner = paramJarEntry.getCodeSigners();
      }
      else
      {
        localObject = SharedSecrets.javaUtilJarAccess();
        CodeSource localCodeSource1 = ((JavaUtilJarAccess)localObject).getCodeSource(paramJarFile, this.jarLocation, paramString);
        arrayOfCodeSigner = localCodeSource1 != null ? localCodeSource1.getCodeSigners() : null;
      }
      if ((arrayOfCodeSigner != null) && (arrayOfCodeSigner.length > 0))
      {
        localObject = new int[arrayOfCodeSigner.length];
        for (int i = 0; i < arrayOfCodeSigner.length; i++)
        {
          j = this.signersCS.indexOf(arrayOfCodeSigner[i]);
          if (j == -1)
          {
            j = this.signersCS.size();
            this.signersCS.add(arrayOfCodeSigner[i]);
          }
          localObject[i] = j;
        }
        String str = String.valueOf(localObject.length);
        for (int j = 0; j < localObject.length; j++)
          str = str + " " + localObject[j];
        int[] arrayOfInt = (int[])(int[])localHashMap.get(str);
        if (arrayOfInt == null)
        {
          localHashMap.put(str, localObject);
          CodeSource localCodeSource2 = new CodeSource(this.jarLocation, arrayOfCodeSigner);
          this.codeSourceCache.put(localObject, localCodeSource2);
        }
        else
        {
          localObject = arrayOfInt;
        }
        this.signerMap.put(paramString, localObject);
      }
    }
  }

  protected void authenticateJarEntry(JarFile paramJarFile, JarEntry paramJarEntry)
    throws IOException, JARSigningException
  {
    if (paramJarEntry == null)
      return;
    String str = paramJarEntry.getName();
    boolean bool = (this.nativePath != null) && (str.indexOf("/") == -1) && (str.indexOf("\\") == -1);
    try
    {
      readAndMaybeSaveStreamTo(paramJarFile.getInputStream(paramJarEntry), bool, str, this.nativePath);
    }
    catch (SecurityException localSecurityException)
    {
      throw new JARSigningException(this.jarLocation, this.jarVersion, 2, localSecurityException);
    }
  }

  public static JarFile getValidatedJarFile(File paramFile, URL paramURL1, URL paramURL2, String paramString, DownloadEngine.DownloadDelegate paramDownloadDelegate)
    throws IOException
  {
    int i = 0;
    JarVerifier localJarVerifier = create(paramURL2, paramString, paramFile, null);
    Object localObject1 = new JarFile(paramFile);
    if (paramDownloadDelegate != null)
      paramDownloadDelegate.validating(paramURL1, 0, ((JarFile)localObject1).size());
    try
    {
      localJarVerifier.validate(paramDownloadDelegate);
      if ((localJarVerifier instanceof JarAsBLOBVerifier))
        localObject1 = new SignedAsBlobJarFile(paramFile, (JarAsBLOBVerifier)localJarVerifier);
      if (BlackList.getInstance().checkJarFile((JarFile)localObject1))
      {
        Object localObject2 = null;
        return localObject2;
      }
      i = 1;
    }
    catch (ZipException localZipException)
    {
      throw new JARSigningException(paramURL1, paramString, 2, localZipException);
    }
    catch (SecurityException localSecurityException)
    {
      throw new JARSigningException(paramURL1, paramString, 2, localSecurityException);
    }
    finally
    {
      if (i == 0)
      {
        ((JarFile)localObject1).close();
        DownloadEngine.clearTemporaryResourceMaps(paramURL1);
      }
    }
    return i != 0 ? localObject1 : (JarFile)null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.JarVerifier
 * JD-Core Version:    0.6.0
 */