package com.sun.deploy.cache;

import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.JarFile;
import sun.misc.JavaUtilJarAccess;
import sun.misc.SharedSecrets;

public class DeployCacheJarAccessImpl
  implements DeployCacheJarAccess
{
  private JavaUtilJarAccess access = SharedSecrets.javaUtilJarAccess();
  private static boolean enhancedJarAccess;

  public static DeployCacheJarAccess getJarAccess()
  {
    if (enhancedJarAccess)
      return new DeployCacheJarAccessImpl();
    return null;
  }

  public Enumeration entryNames(JarFile paramJarFile, CodeSource[] paramArrayOfCodeSource)
  {
    if ((paramJarFile instanceof CachedJarFile))
      return ((CachedJarFile)paramJarFile).entryNames(paramArrayOfCodeSource);
    if ((paramJarFile instanceof CachedJarFile14))
      return ((CachedJarFile14)paramJarFile).entryNames(paramArrayOfCodeSource);
    if ((paramJarFile instanceof SignedAsBlobJarFile))
      return ((SignedAsBlobJarFile)paramJarFile).entryNames(paramArrayOfCodeSource);
    return this.access.entryNames(paramJarFile, paramArrayOfCodeSource);
  }

  public CodeSource[] getCodeSources(JarFile paramJarFile, URL paramURL)
  {
    if ((paramJarFile instanceof CachedJarFile))
      return ((CachedJarFile)paramJarFile).getCodeSources(paramURL);
    if ((paramJarFile instanceof CachedJarFile14))
      return ((CachedJarFile14)paramJarFile).getCodeSources(paramURL);
    if ((paramJarFile instanceof SignedAsBlobJarFile))
      return ((SignedAsBlobJarFile)paramJarFile).getCodeSources(paramURL);
    return this.access.getCodeSources(paramJarFile, paramURL);
  }

  public CodeSource getCodeSource(JarFile paramJarFile, URL paramURL, String paramString)
  {
    if ((paramJarFile instanceof CachedJarFile))
      return ((CachedJarFile)paramJarFile).getCodeSource(paramURL, paramString);
    if ((paramJarFile instanceof CachedJarFile14))
      return ((CachedJarFile14)paramJarFile).getCodeSource(paramURL, paramString);
    if ((paramJarFile instanceof SignedAsBlobJarFile))
      return ((SignedAsBlobJarFile)paramJarFile).getCodeSource(paramURL, paramString);
    return this.access.getCodeSource(paramJarFile, paramURL, paramString);
  }

  public void setEagerValidation(JarFile paramJarFile, boolean paramBoolean)
  {
    if ((!(paramJarFile instanceof CachedJarFile)) && (!(paramJarFile instanceof CachedJarFile14)))
      this.access.setEagerValidation(paramJarFile, paramBoolean);
  }

  static
  {
    try
    {
      JavaUtilJarAccess localJavaUtilJarAccess = SharedSecrets.javaUtilJarAccess();
      localJavaUtilJarAccess.setEagerValidation((JarFile)null, false);
      enhancedJarAccess = true;
    }
    catch (NoClassDefFoundError localNoClassDefFoundError)
    {
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
    }
    catch (NullPointerException localNullPointerException)
    {
      enhancedJarAccess = true;
    }
    catch (Exception localException)
    {
    }
    catch (Error localError)
    {
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.DeployCacheJarAccessImpl
 * JD-Core Version:    0.6.0
 */