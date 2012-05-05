package com.sun.deploy.cache;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.SystemUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class CacheUpgradeHelper
{
  private static final boolean DEBUG = (Config.getDeployDebug()) || (Config.getPluginDebug());
  private static List helpers = new ArrayList();

  protected synchronized void register()
  {
    if (!helpers.contains(this))
      helpers.add(this);
  }

  public static void upgradeLocalAppProperties(CacheEntry paramCacheEntry1, CacheEntry paramCacheEntry2)
  {
    for (int i = 0; i < helpers.size(); i++)
    {
      CacheUpgradeHelper localCacheUpgradeHelper = (CacheUpgradeHelper)helpers.get(i);
      if (!localCacheUpgradeHelper.canProcess(paramCacheEntry2.getCacheVersion()))
        continue;
      localCacheUpgradeHelper.processLocalAppProperties(paramCacheEntry1, paramCacheEntry2);
    }
  }

  public abstract void processLocalAppProperties(CacheEntry paramCacheEntry1, CacheEntry paramCacheEntry2);

  public boolean canProcess(int paramInt)
  {
    return true;
  }

  protected static Properties loadProperties(File paramFile)
  {
    try
    {
      Properties localProperties = new Properties();
      localProperties.load(new ByteArrayInputStream(Cache.getLapBytes(paramFile)));
      return localProperties;
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    return null;
  }

  protected static String getLapFileName(CacheEntry paramCacheEntry)
  {
    return paramCacheEntry.getResourceFilename() + "6.0" + Cache.getVersionTag(paramCacheEntry.getVersion()) + ".lap";
  }

  protected static Properties getProperties(CacheEntry paramCacheEntry)
  {
    File localFile = new File(getLapFileName(paramCacheEntry));
    if (localFile.isFile())
      return loadProperties(localFile);
    return null;
  }

  static String getHelperJarPaths()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < helpers.size(); i++)
    {
      String str = SystemUtils.getJarPath(helpers.get(i).getClass());
      if ((DEBUG) && (!new File(str).exists()))
        System.out.println("Wrong path: " + str);
      localStringBuffer.append(str);
      localStringBuffer.append(File.pathSeparator);
    }
    return localStringBuffer.toString();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.CacheUpgradeHelper
 * JD-Core Version:    0.6.0
 */