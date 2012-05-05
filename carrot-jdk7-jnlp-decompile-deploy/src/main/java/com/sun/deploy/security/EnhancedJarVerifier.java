package com.sun.deploy.security;

import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine.DownloadDelegate;
import com.sun.deploy.net.JARSigningException;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.BlackList;
import com.sun.deploy.util.TrustedLibraries;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.misc.JavaUtilJarAccess;
import sun.misc.SharedSecrets;

public class EnhancedJarVerifier extends JarVerifier
{
  public static JarVerifier create(URL paramURL, String paramString, File paramFile1, File paramFile2)
  {
    if (CacheEntry.hasEnhancedJarAccess())
      return new EnhancedJarVerifier(paramURL, paramString, paramFile1, paramFile2);
    return null;
  }

  EnhancedJarVerifier(URL paramURL, String paramString, File paramFile1, File paramFile2)
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
      Enumeration localEnumeration = null;
      int i = 0;
      int j = localJarFile.size();
      CodeSource[] arrayOfCodeSource = null;
      JavaUtilJarAccess localJavaUtilJarAccess1 = null;
      if (paramDownloadDelegate != null)
        paramDownloadDelegate.validating(this.jarLocation, 0, j);
      localJavaUtilJarAccess1 = SharedSecrets.javaUtilJarAccess();
      JavaUtilJarAccess localJavaUtilJarAccess2 = (JavaUtilJarAccess)localJavaUtilJarAccess1;
      arrayOfCodeSource = localJavaUtilJarAccess2.getCodeSources(localJarFile, this.jarLocation);
      if (BlackList.getInstance().checkJarFile(localJarFile))
        throw new JARSigningException(this.jarLocation, this.jarVersion, 5);
      Object localObject1;
      if (TrustedLibraries.checkJarFile(localJarFile))
      {
        localObject1 = this.manifest.getMainAttributes();
        ((Attributes)localObject1).putValue("Trusted-Library", Boolean.TRUE.toString());
      }
      int k;
      if ((arrayOfCodeSource != null) && (arrayOfCodeSource.length > 0))
      {
        localObject1 = new ArrayList();
        for (k = 0; k < arrayOfCodeSource.length; k++)
        {
          if (arrayOfCodeSource[k].getCertificates() == null)
            continue;
          ((List)localObject1).add(arrayOfCodeSource[k]);
        }
        if (((List)localObject1).size() != arrayOfCodeSource.length)
          arrayOfCodeSource = (CodeSource[])(CodeSource[])((List)localObject1).toArray(new CodeSource[((List)localObject1).size()]);
        else
          this.hasOnlySignedEntries = true;
      }
      if ((arrayOfCodeSource != null) && (arrayOfCodeSource.length > 0))
        localEnumeration = localJavaUtilJarAccess2.entryNames(localJarFile, arrayOfCodeSource);
      else
        localEnumeration = localJarFile.entries();
      Object localObject2;
      if ((arrayOfCodeSource != null) && (arrayOfCodeSource.length == 1))
      {
        localObject1 = arrayOfCodeSource[0].getCertificates();
        this.singleSignerIndicesCert = new int[localObject1.length];
        for (k = 0; k < localObject1.length; k++)
        {
          this.signerCerts.add(localObject1[k]);
          this.singleSignerIndicesCert[k] = k;
        }
        this.codeSourceCertCache.put(this.singleSignerIndicesCert, arrayOfCodeSource[0]);
        if (Config.isJavaVersionAtLeast15())
        {
          localObject2 = arrayOfCodeSource[0].getCodeSigners();
          this.singleSignerIndicesCS = new int[localObject2.length];
          for (int m = 0; m < localObject2.length; m++)
          {
            this.signersCS.add(localObject2[m]);
            this.singleSignerIndicesCS[m] = m;
          }
          this.codeSourceCache.put(this.singleSignerIndicesCS, arrayOfCodeSource[0]);
        }
        this.hasSingleCodeSource = true;
      }
      while (localEnumeration.hasMoreElements())
      {
        i++;
        localObject1 = null;
        localObject2 = null;
        if ((arrayOfCodeSource != null) && (arrayOfCodeSource.length > 0))
        {
          localObject1 = (String)localEnumeration.nextElement();
          localObject2 = localJarFile.getJarEntry((String)localObject1);
        }
        else
        {
          localObject2 = (JarEntry)localEnumeration.nextElement();
          localObject1 = ((JarEntry)localObject2).getName();
        }
        if (localObject2 == null)
        {
          this.hasMissingSignedEntries = true;
          Trace.println("signed entry \"" + (String)localObject1 + "\" missing from jar " + this.jarLocation, TraceLevel.CACHE);
        }
        authenticateJarEntry(localJarFile, (JarEntry)localObject2);
        processCertificates(localJarFile, (JarEntry)localObject2, (String)localObject1);
        processSigners(localJarFile, (JarEntry)localObject2, (String)localObject1);
        if ((paramDownloadDelegate != null) && ((i % 10 == 0) || (i >= j)))
          paramDownloadDelegate.validating(this.jarLocation, i, j);
      }
      if (paramDownloadDelegate != null)
        paramDownloadDelegate.validating(this.jarLocation, j, j);
    }
    finally
    {
      localJarFile.close();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.EnhancedJarVerifier
 * JD-Core Version:    0.6.0
 */