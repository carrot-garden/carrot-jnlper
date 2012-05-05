package com.sun.javaws;

import com.sun.deploy.Environment;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.exceptions.BadFieldException;
import com.sun.javaws.jnl.ExtensionDesc;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.RContentDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.ui.SplashScreen;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class CacheUtil
{
  public static void remove(boolean paramBoolean)
  {
    ArrayList localArrayList1 = Cache.getJnlpCacheEntries(Environment.isSystemCacheMode());
    ArrayList localArrayList2;
    if (paramBoolean)
      localArrayList2 = new ArrayList();
    else
      localArrayList2 = getExcludedCacheEntries(localArrayList1.iterator());
    Iterator localIterator = localArrayList1.iterator();
    while (localIterator.hasNext())
    {
      localObject = (File)localIterator.next();
      LaunchDesc localLaunchDesc = null;
      try
      {
        localLaunchDesc = LaunchDescFactory.buildDescriptor((File)localObject, null, null, null);
      }
      catch (Exception localException2)
      {
        Trace.println("Cached jnlp file has no codebase", TraceLevel.CACHE);
        try
        {
          localLaunchDesc = LaunchDescFactory.buildDescriptor((File)localObject);
        }
        catch (Exception localException3)
        {
          Trace.ignoredException(localException2);
        }
      }
      if (localLaunchDesc != null)
        remove((File)localObject, localLaunchDesc, localArrayList2);
    }
    if (Globals.isShortcutMode())
      return;
    Object localObject = Cache.getCacheEntries(Environment.isSystemCacheMode());
    for (int i = 0; i < localObject.length; i++)
    {
      CacheEntry localCacheEntry = Cache.getCacheEntryFromFile(localObject[i]);
      if (localCacheEntry != null)
      {
        if (localArrayList2.contains(localCacheEntry.getIndexFile()))
          continue;
        Cache.removeCacheEntry(localCacheEntry);
      }
      else
      {
        localObject[i].delete();
      }
    }
    if (paramBoolean)
      try
      {
        Cache.removeAllMuffins();
      }
      catch (Exception localException1)
      {
        Trace.ignored(localException1);
      }
  }

  public static ArrayList getInstalledResources(boolean paramBoolean)
  {
    return getExcludedCacheEntries(Cache.getJnlpCacheEntries(paramBoolean).iterator());
  }

  public static ArrayList getExcludedCacheEntries(Iterator paramIterator)
  {
    1 local1 = new ArrayList()
    {
      public boolean add(Object paramObject)
      {
        if (!contains(paramObject))
          super.add(paramObject);
        return true;
      }
    };
    while (paramIterator.hasNext())
    {
      File localFile1 = (File)paramIterator.next();
      File localFile2 = new File(localFile1.getPath() + ".idx");
      LaunchDesc localLaunchDesc = null;
      if (localFile2.exists())
      {
        try
        {
          localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile1, null, null, null);
        }
        catch (BadFieldException localBadFieldException)
        {
          try
          {
            CacheEntry localCacheEntry2 = Cache.getCacheEntryFromFile(localFile2);
            localLaunchDesc = LaunchDescFactory.buildDescriptor(localCacheEntry2.getDataFile(), URLUtil.getBase(new URL(localCacheEntry2.getURL())), null, new URL(localCacheEntry2.getURL()));
          }
          catch (Exception localException2)
          {
            Trace.ignoredException(localException2);
          }
        }
        catch (Exception localException1)
        {
          Trace.ignoredException(localException1);
        }
        CacheEntry localCacheEntry1 = Cache.getCacheEntryFromFile(localFile2);
        if ((localLaunchDesc != null) && (localCacheEntry1 != null))
        {
          LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(localCacheEntry1);
          if ((localLocalApplicationProperties != null) && (localLocalApplicationProperties.isJnlpInstalled()))
            markResourcesInstalled(localFile2, localLaunchDesc, localCacheEntry1, local1);
        }
      }
    }
    return local1;
  }

  private static void markResourcesInstalled(File paramFile, LaunchDesc paramLaunchDesc, CacheEntry paramCacheEntry, ArrayList paramArrayList)
  {
    paramArrayList.add(paramFile);
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    Object localObject3;
    Object localObject4;
    Object localObject2;
    int j;
    if (localResourcesDesc != null)
    {
      localObject1 = localResourcesDesc.getLocalJarDescs();
      if (localObject1 != null)
        for (int i = localObject1.length - 1; i >= 0; i--)
        {
          URL localURL = localObject1[i].getLocation();
          localObject3 = localObject1[i].getVersion();
          localObject4 = Cache.getCacheEntry(localURL, null, (String)localObject3);
          if (localObject4 == null)
            continue;
          paramArrayList.add(((CacheEntry)localObject4).getIndexFile());
        }
      localObject2 = localResourcesDesc.getExtensionDescs();
      if (localObject2 != null)
        for (j = localObject2.length - 1; j >= 0; j--)
        {
          localObject3 = localObject2[j];
          localObject4 = Cache.getCacheEntry(((ExtensionDesc)localObject3).getLocation(), null, ((ExtensionDesc)localObject3).getVersion());
          if (localObject4 == null)
            continue;
          try
          {
            LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(((CacheEntry)localObject4).getDataFile(), null, null, null);
            if (localLaunchDesc != null)
              markResourcesInstalled(((CacheEntry)localObject4).getIndexFile(), localLaunchDesc, (CacheEntry)localObject4, paramArrayList);
          }
          catch (Exception localException)
          {
            Trace.ignored(localException);
          }
        }
    }
    Object localObject1 = paramLaunchDesc.getInformation();
    if (localObject1 != null)
    {
      localObject2 = ((InformationDesc)localObject1).getIcons();
      CacheEntry localCacheEntry;
      if (localObject2 != null)
        for (j = 0; j < localObject2.length; j++)
        {
          localObject3 = localObject2[j].getLocation();
          if (localObject3 == null)
            continue;
          localObject4 = localObject2[j].getVersion();
          localCacheEntry = Cache.getCacheEntry((URL)localObject3, null, (String)localObject4);
          if (localCacheEntry == null)
            continue;
          paramArrayList.add(localCacheEntry.getIndexFile());
        }
      RContentDesc[] arrayOfRContentDesc = ((InformationDesc)localObject1).getRelatedContent();
      if (arrayOfRContentDesc != null)
        for (int k = 0; k < arrayOfRContentDesc.length; k++)
        {
          localObject4 = arrayOfRContentDesc[k].getIcon();
          if (localObject4 == null)
            continue;
          localCacheEntry = Cache.getCacheEntry((URL)localObject4, null, null);
          if (localCacheEntry == null)
            continue;
          paramArrayList.add(localCacheEntry.getIndexFile());
        }
    }
  }

  public static void remove(CacheEntry paramCacheEntry)
  {
    try
    {
      LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(paramCacheEntry.getDataFile(), null, null, null);
      remove(paramCacheEntry, localLaunchDesc);
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
  }

  public static void remove(File paramFile, LaunchDesc paramLaunchDesc, ArrayList paramArrayList)
  {
    File localFile = new File(paramFile.getPath() + ".idx");
    CacheEntry localCacheEntry;
    if (localFile.exists())
    {
      localCacheEntry = Cache.getCacheEntryFromFile(localFile);
      if (localCacheEntry != null)
      {
        if (!paramArrayList.contains(localFile))
          remove(localCacheEntry, paramLaunchDesc);
      }
      else
      {
        localFile.delete();
        paramFile.delete();
      }
    }
    else if ((paramFile.exists()) && (Cache.getCacheDir().equals(paramFile.getParentFile())))
    {
      paramFile.delete();
    }
    else
    {
      localCacheEntry = Cache.getCacheEntry(paramLaunchDesc.getCanonicalHome(), null, null);
      if ((localCacheEntry != null) && (!paramArrayList.contains(localCacheEntry.getIndexFile())))
        remove(localCacheEntry, paramLaunchDesc);
    }
  }

  public static void remove(CacheEntry paramCacheEntry, LaunchDesc paramLaunchDesc)
  {
    LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(paramCacheEntry);
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    if ((localLocalInstallHandler != null) && (paramLaunchDesc.isApplicationDescriptor()))
      localLocalInstallHandler.uninstall(paramLaunchDesc, localLocalApplicationProperties, true);
    if (Globals.isShortcutMode())
      return;
    if ((paramLaunchDesc.isApplicationDescriptor()) && (paramLaunchDesc.getLocation() != null))
      Cache.saveRemovedApp(paramLaunchDesc.getLocation(), localInformationDesc.getTitle());
    localLocalApplicationProperties.refresh();
    if ((localLocalApplicationProperties.isExtensionInstalled()) && (paramLaunchDesc.isInstaller()))
    {
      localObject1 = new ArrayList();
      ((ArrayList)localObject1).add(paramCacheEntry.getDataFile());
      try
      {
        String str1 = localLocalApplicationProperties.getInstallDirectory();
        JnlpxArgs.executeUninstallers((ArrayList)localObject1);
        JREInfo.removeJREsIn(str1);
      }
      catch (Exception localException1)
      {
        Trace.ignoredException(localException1);
      }
    }
    SplashScreen.removeCustomSplash(paramLaunchDesc);
    Object localObject4;
    int j;
    if (localInformationDesc != null)
    {
      localObject1 = localInformationDesc.getIcons();
      if (localObject1 != null)
        for (int i = 0; i < localObject1.length; i++)
        {
          URL localURL1 = localObject1[i].getLocation();
          localObject4 = localObject1[i].getVersion();
          removeEntries(localURL1, (String)localObject4);
        }
      localObject2 = localInformationDesc.getRelatedContent();
      if (localObject2 != null)
        for (j = 0; j < localObject2.length; j++)
        {
          localObject4 = localObject2[j].getIcon();
          if (localObject4 == null)
            continue;
          removeEntries((URL)localObject4, null);
        }
    }
    Object localObject1 = paramLaunchDesc.getResources();
    if (localObject1 != null)
    {
      localObject2 = ((ResourcesDesc)localObject1).getLocalJarDescs();
      String str2;
      if (localObject2 != null)
        for (j = localObject2.length - 1; j >= 0; j--)
        {
          localObject4 = localObject2[j].getLocation();
          str2 = localObject2[j].getVersion();
          removeEntries((URL)localObject4, str2);
        }
      localObject3 = ((ResourcesDesc)localObject1).getExtensionDescs();
      if (localObject3 != null)
        for (int k = localObject3.length - 1; k >= 0; k--)
        {
          str2 = localObject3[k];
          CacheEntry localCacheEntry = Cache.getCacheEntry(str2.getLocation(), null, str2.getVersion());
          if (localCacheEntry == null)
            continue;
          try
          {
            LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localCacheEntry.getDataFile(), null, null, null);
            if ((localLaunchDesc != null) && (localLaunchDesc.isInstaller()))
              remove(localCacheEntry, localLaunchDesc);
          }
          catch (Exception localException2)
          {
            Trace.ignored(localException2);
          }
        }
    }
    Object localObject2 = paramCacheEntry.getURL();
    Object localObject3 = paramCacheEntry.getVersion();
    try
    {
      URL localURL2 = new URL((String)localObject2);
      if (localURL2 != null)
        removeEntries(localURL2, (String)localObject3);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignored(localMalformedURLException);
    }
    Cache.removeLoadedProperties((String)localObject2);
  }

  private static void removeEntries(URL paramURL, String paramString)
  {
    if (paramURL != null)
    {
      CacheEntry localCacheEntry = Cache.getCacheEntry(paramURL, null, paramString);
      Cache.removeAllCacheEntries(localCacheEntry);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.CacheUtil
 * JD-Core Version:    0.6.0
 */