package com.sun.jnlp;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import javax.jnlp.DownloadService2;
import javax.jnlp.DownloadService2.ResourceSpec;

public class DownloadService2Impl
  implements DownloadService2
{
  private static DownloadService2Impl instance;
  private static ResourceSpecAccess resourceSpecAccess;

  public static synchronized DownloadService2 getInstance()
  {
    if (instance == null)
      instance = new DownloadService2Impl();
    return instance;
  }

  public static void setResourceSpecAccess(ResourceSpecAccess paramResourceSpecAccess)
  {
    resourceSpecAccess = paramResourceSpecAccess;
  }

  public DownloadService2.ResourceSpec[] getCachedResources(DownloadService2.ResourceSpec paramResourceSpec)
  {
    validateResourceSpec(paramResourceSpec);
    ArrayList localArrayList = new ArrayList();
    AccessController.doPrivileged(new PrivilegedAction(paramResourceSpec, localArrayList)
    {
      private final DownloadService2.ResourceSpec val$spec;
      private final ArrayList val$matchingResources;

      public Object run()
      {
        DownloadService2Impl.this.getCachedResourcesImpl(this.val$spec, this.val$matchingResources);
        return null;
      }
    });
    return (DownloadService2.ResourceSpec[])(DownloadService2.ResourceSpec[])localArrayList.toArray(new DownloadService2.ResourceSpec[localArrayList.size()]);
  }

  private void getCachedResourcesImpl(DownloadService2.ResourceSpec paramResourceSpec, ArrayList paramArrayList)
  {
    File[] arrayOfFile = Cache.getCacheEntries(false);
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      CacheEntry localCacheEntry = Cache.getCacheEntryFromFile(arrayOfFile[i]);
      if (!matches(localCacheEntry, paramResourceSpec))
        continue;
      paramArrayList.add(toResourceSpec(localCacheEntry));
    }
  }

  private boolean matches(CacheEntry paramCacheEntry, DownloadService2.ResourceSpec paramResourceSpec)
  {
    int i = 0;
    String str1 = paramResourceSpec.getUrl();
    if ((str1 != null) && (paramCacheEntry != null))
    {
      String str2 = paramResourceSpec.getVersion();
      String str3 = paramCacheEntry.getVersion();
      int j = paramResourceSpec.getType();
      int k = (j == 0) || (j == getResourceType(paramCacheEntry)) ? 1 : 0;
      int m = ((str2 == null) && (str3 == null)) || ((str2 != null) && (str3 != null) && (stringMatch(str3, str2))) ? 1 : 0;
      i = (stringMatch(paramCacheEntry.getURL(), str1)) && (m != 0) && (k != 0) ? 1 : 0;
    }
    return i;
  }

  private DownloadService2.ResourceSpec toResourceSpec(CacheEntry paramCacheEntry)
  {
    DownloadService2.ResourceSpec localResourceSpec = new DownloadService2.ResourceSpec(paramCacheEntry.getURL(), paramCacheEntry.getVersion(), getResourceType(paramCacheEntry));
    resourceSpecAccess.setSize(localResourceSpec, paramCacheEntry.getSize());
    resourceSpecAccess.setLastModified(localResourceSpec, paramCacheEntry.getLastModified());
    resourceSpecAccess.setExpirationDate(localResourceSpec, paramCacheEntry.getExpirationDate());
    return localResourceSpec;
  }

  private int getResourceType(CacheEntry paramCacheEntry)
  {
    String str1 = paramCacheEntry.getURL();
    String str2 = getLowerNameExtension(str1);
    int i = 0;
    if (paramCacheEntry.isJNLPFile())
      try
      {
        URL localURL = new URL(str1);
        LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(paramCacheEntry.getDataFile(), URLUtil.getBase(localURL), null, localURL);
        if (localLaunchDesc.isApplication())
          i = 1;
        else if (localLaunchDesc.isApplet())
          i = 2;
        else if ((localLaunchDesc.isInstaller()) || (localLaunchDesc.isLibrary()))
          i = 3;
      }
      catch (Exception localException)
      {
      }
    else if (paramCacheEntry.isJarFile())
      i = 4;
    else if ((str2.endsWith("png")) || (str2.endsWith("gif")) || (str2.endsWith("jpeg")) || (str2.endsWith("jpg")) || (str2.endsWith("ico")))
      i = 5;
    else if (str2.endsWith("class"))
      i = 6;
    return i;
  }

  private String getLowerNameExtension(String paramString)
  {
    if (paramString.indexOf('?') != -1);
    for (paramString = paramString.substring(0, paramString.indexOf('?')); paramString.charAt(paramString.length() - 1) == '/'; paramString = paramString.substring(0, paramString.length() - 1));
    paramString = paramString.toLowerCase();
    return paramString;
  }

  public DownloadService2.ResourceSpec[] getUpdateAvailableResources(DownloadService2.ResourceSpec paramResourceSpec)
  {
    validateResourceSpec(paramResourceSpec);
    DownloadService2.ResourceSpec[] arrayOfResourceSpec1 = getCachedResources(paramResourceSpec);
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < arrayOfResourceSpec1.length; i++)
    {
      DownloadService2.ResourceSpec localResourceSpec = arrayOfResourceSpec1[i];
      URL localURL;
      try
      {
        localURL = new URL(localResourceSpec.getUrl());
      }
      catch (MalformedURLException localMalformedURLException)
      {
        InternalError localInternalError = new InternalError();
        localInternalError.initCause(localMalformedURLException);
        throw localInternalError;
      }
      try
      {
        if (localResourceSpec.getVersion() == null)
        {
          if (DownloadEngine.isUpdateAvailable(localURL, null))
            localArrayList.add(localResourceSpec);
        }
        else
        {
          String str = DownloadEngine.getAvailableVersion(localURL, "0+", false, null);
          if (!DownloadEngine.isResourceCached(localURL, null, str))
            localArrayList.add(localResourceSpec);
        }
      }
      catch (IOException localIOException)
      {
      }
    }
    DownloadService2.ResourceSpec[] arrayOfResourceSpec2 = new DownloadService2.ResourceSpec[localArrayList.size()];
    for (int j = 0; j < localArrayList.size(); j++)
      arrayOfResourceSpec2[j] = ((DownloadService2.ResourceSpec)localArrayList.get(j));
    return arrayOfResourceSpec2;
  }

  private void validateResourceSpec(DownloadService2.ResourceSpec paramResourceSpec)
  {
    String str = paramResourceSpec.getUrl();
    int i = paramResourceSpec.getType();
    if (str == null)
      throw new IllegalArgumentException("ResourceSpec has null url");
    if (str == "")
      throw new IllegalArgumentException("ResourceSpec has empty url");
    if ((i < 0) || (i > 6))
      throw new IllegalArgumentException("ResourceSpec has invalue type");
  }

  private boolean stringMatch(String paramString1, String paramString2)
  {
    if (Config.isJavaVersionAtLeast14())
      return paramString1.matches(paramString2);
    return paramString1.equals(paramString2);
  }

  public static abstract interface ResourceSpecAccess
  {
    public abstract void setSize(DownloadService2.ResourceSpec paramResourceSpec, long paramLong);

    public abstract void setLastModified(DownloadService2.ResourceSpec paramResourceSpec, long paramLong);

    public abstract void setExpirationDate(DownloadService2.ResourceSpec paramResourceSpec, long paramLong);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.DownloadService2Impl
 * JD-Core Version:    0.6.0
 */