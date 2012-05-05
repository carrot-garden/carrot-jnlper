package com.sun.deploy.security;

import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.net.DownloadEngine.DownloadDelegate;
import com.sun.deploy.net.JARSigningException;
import com.sun.deploy.util.BlackList;
import com.sun.deploy.util.TrustedLibraries;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class SimpleJarVerifier extends JarVerifier
{
  public static JarVerifier create(URL paramURL, String paramString, File paramFile1, File paramFile2)
  {
    return new SimpleJarVerifier(paramURL, paramString, paramFile1, paramFile2);
  }

  private SimpleJarVerifier(URL paramURL, String paramString, File paramFile1, File paramFile2)
  {
    super(paramURL, paramString, paramFile1, paramFile2);
  }

  public void validate(DownloadEngine.DownloadDelegate paramDownloadDelegate)
    throws IOException, JARSigningException
  {
    JarFile localJarFile = new JarFile(this.jarFile);
    this.manifest = localJarFile.getManifest();
    try
    {
      int i = localJarFile.size();
      int j = 0;
      boolean bool1 = false;
      boolean bool2 = false;
      Enumeration localEnumeration = localJarFile.entries();
      if (paramDownloadDelegate != null)
        paramDownloadDelegate.validating(this.jarLocation, 0, i);
      while (localEnumeration.hasMoreElements())
      {
        j++;
        String str = null;
        JarEntry localJarEntry = null;
        localJarEntry = (JarEntry)localEnumeration.nextElement();
        str = localJarEntry.getName();
        if (!bool1)
          try
          {
            bool1 = BlackList.getInstance().checkJarEntry(localJarFile, localJarEntry);
          }
          catch (GeneralSecurityException localGeneralSecurityException1)
          {
            throw new JARSigningException(this.jarLocation, this.jarVersion, 5, localGeneralSecurityException1);
          }
        if (!bool2)
          try
          {
            bool2 = TrustedLibraries.checkJarEntry(localJarFile, localJarEntry);
          }
          catch (GeneralSecurityException localGeneralSecurityException2)
          {
            Attributes localAttributes = this.manifest.getMainAttributes();
            localAttributes.putValue("Trusted-Library", Boolean.TRUE.toString());
          }
        if ((CacheEntry.isSigningRelated(str)) || (str.endsWith("/")))
          continue;
        authenticateJarEntry(localJarFile, localJarEntry);
        processCertificates(localJarFile, localJarEntry, str);
        processSigners(localJarFile, localJarEntry, str);
        if ((paramDownloadDelegate != null) && ((j % 10 == 0) || (j >= i)))
          paramDownloadDelegate.validating(this.jarLocation, j, i);
      }
      if (paramDownloadDelegate != null)
        paramDownloadDelegate.validating(this.jarLocation, i, i);
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
      readAndMaybeSaveStreamTo(paramJarFile.getInputStream(paramJarEntry), bool, str, this.nativePath);
    }
    catch (SecurityException localSecurityException)
    {
      throw new JARSigningException(this.jarLocation, this.jarVersion, 2, localSecurityException);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.SimpleJarVerifier
 * JD-Core Version:    0.6.0
 */