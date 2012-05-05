package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine.DownloadDelegate;
import com.sun.deploy.net.JARSigningException;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.BlackList;
import com.sun.deploy.util.TrustedLibraries;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarAsBLOBVerifier extends JarVerifier
{
  private JarSignature js = null;

  public static JarVerifier create(URL paramURL, String paramString, File paramFile1, File paramFile2)
  {
    JarAsBLOBVerifier localJarAsBLOBVerifier = new JarAsBLOBVerifier(paramURL, paramString, paramFile1, paramFile2);
    if (localJarAsBLOBVerifier.detectedSignature())
      return localJarAsBLOBVerifier;
    return null;
  }

  JarAsBLOBVerifier(URL paramURL, String paramString, File paramFile1, File paramFile2)
  {
    super(paramURL, paramString, paramFile1, paramFile2);
    init();
  }

  private void init()
  {
    ZipInputStream localZipInputStream = null;
    try
    {
      localZipInputStream = new ZipInputStream(new FileInputStream(this.jarFile));
      ZipEntry localZipEntry = localZipInputStream.getNextEntry();
      if ((localZipEntry != null) && ("META-INF/".equals(localZipEntry.getName())))
        localZipEntry = localZipInputStream.getNextEntry();
      if ((localZipEntry != null) && (localZipEntry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF")))
        localZipEntry = localZipInputStream.getNextEntry();
      if ((localZipEntry != null) && (JarSignature.BLOB_SIGNATURE.equals(localZipEntry.getName())))
      {
        byte[] arrayOfByte = getBytes(localZipInputStream);
        this.js = JarSignature.load(arrayOfByte);
      }
    }
    catch (Exception localIOException2)
    {
      if ((Config.getDeployDebug()) || (Config.getPluginDebug()))
        Trace.ignored(localException);
      this.js = null;
    }
    finally
    {
      if (localZipInputStream != null)
        try
        {
          localZipInputStream.close();
        }
        catch (IOException localIOException3)
        {
          Trace.ignored(localIOException3);
        }
    }
  }

  private boolean detectedSignature()
  {
    return this.js != null;
  }

  public void validate(DownloadEngine.DownloadDelegate paramDownloadDelegate)
    throws IOException, JARSigningException
  {
    JarFile localJarFile = new JarFile(this.jarFile);
    try
    {
      int i = localJarFile.size();
      int j = 0;
      this.manifest = localJarFile.getManifest();
      Enumeration localEnumeration = localJarFile.entries();
      if (paramDownloadDelegate != null)
        paramDownloadDelegate.validating(this.jarLocation, 0, i);
      if (BlackList.getInstance().checkJarFile(localJarFile))
        throw new JARSigningException(this.jarLocation, this.jarVersion, 5);
      if (TrustedLibraries.checkJarFile(localJarFile))
      {
        localObject1 = this.manifest.getMainAttributes();
        ((Attributes)localObject1).putValue("Trusted-Library", Boolean.TRUE.toString());
      }
      while (localEnumeration.hasMoreElements())
      {
        j++;
        localObject1 = null;
        localObject2 = null;
        localObject2 = (JarEntry)localEnumeration.nextElement();
        localObject1 = ((JarEntry)localObject2).getName();
        if ((JarSignature.BLOB_SIGNATURE.equals(localObject1)) || (((String)localObject1).endsWith("/")))
          continue;
        authenticateJarEntry(localJarFile, (JarEntry)localObject2);
      }
      if (!this.js.isValid())
        throw new JARSigningException(this.jarLocation, this.jarVersion, 2);
      this.hasOnlySignedEntries = true;
      this.hasSingleCodeSource = true;
      this.hasMissingSignedEntries = false;
      Object localObject1 = this.js.getCodeSigners();
      Object localObject2 = new CodeSource(this.jarLocation, localObject1);
      Certificate[] arrayOfCertificate = ((CodeSource)localObject2).getCertificates();
      this.singleSignerIndicesCert = new int[arrayOfCertificate.length];
      for (int k = 0; k < arrayOfCertificate.length; k++)
      {
        this.signerCerts.add(arrayOfCertificate[k]);
        this.singleSignerIndicesCert[k] = k;
      }
      this.codeSourceCertCache.put(this.singleSignerIndicesCert, localObject2);
      if (Config.isJavaVersionAtLeast15())
      {
        this.singleSignerIndicesCS = new int[localObject1.length];
        for (k = 0; k < localObject1.length; k++)
        {
          this.signersCS.add(localObject1[k]);
          this.singleSignerIndicesCS[k] = k;
        }
        this.codeSourceCache.put(this.singleSignerIndicesCS, localObject2);
      }
      this.signerMap.put(null, this.singleSignerIndicesCS);
      this.signerMapCert.put(null, this.singleSignerIndicesCert);
    }
    finally
    {
      localJarFile.close();
    }
  }

  protected void authenticateJarEntry(JarFile paramJarFile, JarEntry paramJarEntry)
    throws IOException, JARSigningException
  {
    String str = paramJarEntry.getName();
    boolean bool = (this.nativePath != null) && (str.indexOf("/") == -1) && (str.indexOf("\\") == -1);
    try
    {
      InputStream localInputStream = this.js.updateWithZipEntry(str, paramJarFile.getInputStream(paramJarEntry));
      readAndMaybeSaveStreamTo(localInputStream, bool, str, this.nativePath);
    }
    catch (SignatureException localSignatureException)
    {
      throw new JARSigningException(this.jarLocation, this.jarVersion, 2, localSignatureException);
    }
    catch (SecurityException localSecurityException)
    {
      throw new JARSigningException(this.jarLocation, this.jarVersion, 2, localSecurityException);
    }
  }

  private static byte[] getBytes(InputStream paramInputStream)
    throws IOException
  {
    byte[] arrayOfByte = new byte[8192];
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(2048);
    int i;
    while ((i = paramInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1)
      localByteArrayOutputStream.write(arrayOfByte, 0, i);
    return localByteArrayOutputStream.toByteArray();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.JarAsBLOBVerifier
 * JD-Core Version:    0.6.0
 */