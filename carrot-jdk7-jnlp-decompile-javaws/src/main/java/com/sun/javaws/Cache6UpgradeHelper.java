package com.sun.javaws;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.cache.CacheUpgradeHelper;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Platform;
import com.sun.deploy.trace.Trace;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

public class Cache6UpgradeHelper extends CacheUpgradeHelper
{
  private static DateFormat _df = null;
  static final Cache6UpgradeHelper instance = new Cache6UpgradeHelper();

  public static Cache6UpgradeHelper getInstance()
  {
    return instance;
  }

  private Cache6UpgradeHelper()
  {
    register();
  }

  public void processLocalAppProperties(CacheEntry paramCacheEntry1, CacheEntry paramCacheEntry2)
  {
    if ((paramCacheEntry2 == null) || (!paramCacheEntry2.isJNLPFile()) || (paramCacheEntry1 == null))
      return;
    LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(paramCacheEntry1);
    Properties localProperties = getProperties(paramCacheEntry2);
    if (localProperties == null)
      return;
    URL localURL = localLocalApplicationProperties.getLocation();
    File localFile = paramCacheEntry1.getDataFile();
    String str1 = localProperties.getProperty("_default.lastAccessed");
    Date localDate = new Date();
    if (str1 != null)
      try
      {
        synchronized (this)
        {
          if (_df == null)
            _df = DateFormat.getDateTimeInstance();
        }
        localDate = _df.parse(str1);
      }
      catch (Exception localException1)
      {
      }
    localLocalApplicationProperties.setLastAccessed(localDate);
    String str2 = localProperties.getProperty("_default.launchCount");
    if ((str2 != null) && (Integer.parseInt(str2) != 0))
      localLocalApplicationProperties.incrementLaunchCount();
    localLocalApplicationProperties.setAskedForInstall(true);
    String str3 = localProperties.getProperty("_default.locallyInstalled");
    String str4 = localProperties.getProperty("_default.title");
    if (str4 != null)
      Platform.get().addRemoveProgramsRemove(str4, false);
    String str5 = localProperties.getProperty("_default.mime.types.");
    String str6 = localProperties.getProperty("_default.extensions.");
    try
    {
      LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
      LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile, null, null, localURL);
      if ((str3 != null) && (str3.equalsIgnoreCase("true")))
      {
        boolean bool1 = false;
        boolean bool2 = removeShortCut(localProperties, localLocalInstallHandler, "windows.installedDesktopShortcut");
        bool1 = removeShortCut(localProperties, localLocalInstallHandler, "windows.installedStartMenuShortcut");
        removeShortCut(localProperties, localLocalInstallHandler, "windows.uninstalledStartMenuShortcut");
        removeShortCut(localProperties, localLocalInstallHandler, "windows.RContent.shortcuts");
        removeShortCut(localProperties, localLocalInstallHandler, "unix.installedDesktopShortcut");
        removeShortCut(localProperties, localLocalInstallHandler, "unix.installedDirectoryFile");
        if (!bool1)
          bool1 = removeShortCut(localProperties, localLocalInstallHandler, "unix.gnome.installedStartMenuShortcut");
        removeShortCut(localProperties, localLocalInstallHandler, "unix.gnome.installedUninstallShortcut");
        removeShortCut(localProperties, localLocalInstallHandler, "unix.gnome.installedRCShortcut");
        if ((bool2) || (bool1))
          localLocalInstallHandler.reinstallShortcuts(localLaunchDesc, localLocalApplicationProperties, bool2, bool1);
      }
      if ((str5 != null) || (str6 != null))
      {
        localLocalInstallHandler.removeAssociations(str5, str6);
        localLocalInstallHandler.reinstallAssociations(localLaunchDesc, localLocalApplicationProperties);
      }
      localLocalInstallHandler.removeFromInstallPanel(localLaunchDesc, localLocalApplicationProperties, false);
      localLocalInstallHandler.registerWithInstallPanel(localLaunchDesc, localLocalApplicationProperties);
    }
    catch (Exception localIOException2)
    {
      Trace.ignored(localException2);
    }
    finally
    {
      try
      {
        localLocalApplicationProperties.store();
      }
      catch (IOException localIOException3)
      {
        Trace.ignoredException(localIOException3);
      }
    }
  }

  private static boolean removeShortCut(Properties paramProperties, LocalInstallHandler paramLocalInstallHandler, String paramString)
  {
    String str = paramProperties.getProperty("windows.installedDesktopShortcut");
    if (str != null)
      return paramLocalInstallHandler.removeShortcuts(str);
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.Cache6UpgradeHelper
 * JD-Core Version:    0.6.0
 */