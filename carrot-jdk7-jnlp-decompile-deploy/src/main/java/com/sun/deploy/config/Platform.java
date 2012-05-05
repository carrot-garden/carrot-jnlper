package com.sun.deploy.config;

import com.sun.deploy.Environment;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.VersionID;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;

public abstract class Platform
{
  private static final boolean DEBUG = (Config.getDeployDebug()) || (Config.getPluginDebug());
  private static Platform _platform;
  private Collection fx_runtimes = null;
  protected static final String UPGRADE_FILENAME = "CacheUpgrade.properties";
  protected AutoUpdater auInstance;

  public static void setInstance(Platform paramPlatform)
  {
    _platform = paramPlatform;
  }

  public static Platform get()
  {
    if (_platform == null)
      setInstance(PlatformFactory.newInstance());
    return _platform;
  }

  public abstract void addRemoveProgramsAdd(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, boolean paramBoolean);

  public abstract void cacheSecurityBaseline(String paramString1, String paramString2);

  public abstract void addRemoveProgramsRemove(String paramString, boolean paramBoolean);

  public abstract boolean canAutoDownloadJRE();

  public abstract String getBrowserPath();

  public abstract String getBrowserHomePath();

  public abstract String getDefaultSystemCache();

  public abstract String getFireFoxUserProfileDirectory();

  public abstract boolean canBecomeAdmin();

  public abstract boolean hasAdminPrivileges();

  public abstract Vector getInstalledJREList();

  public abstract void notifyJREInstalled(String paramString);

  public abstract String getLibrarySufix();

  public abstract String getLibraryPrefix();

  public abstract String getMozillaUserProfileDirectory();

  public abstract long getNativePID();

  public abstract String getSessionSpecificString();

  public abstract String getSystemJavawsPath();

  public abstract int getSystemShortcutIconSize(boolean paramBoolean);

  public abstract int installShortcut(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7);

  public abstract boolean isBrowserFireFox();

  public abstract boolean isLocalInstallSupported();

  public abstract boolean isNativeModalDialogUp();

  public abstract boolean isPlatformIconType(String paramString);

  public abstract boolean isPlatformWindowsVista();

  public abstract void loadDeployNativeLib();

  public abstract void sendJFXPing(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, int paramInt, String paramString6);

  public abstract void setUserHomeOverride(String paramString);

  public abstract boolean showDocument(String paramString);

  public abstract boolean getJqsSettings();

  public abstract void setJqsSettings(boolean paramBoolean);

  public abstract boolean getJavaPluginSettings();

  public abstract int setJavaPluginSettings(boolean paramBoolean);

  public abstract void initBrowserSettings();

  public abstract int applyBrowserSettings();

  public abstract boolean systemLookAndFeelDefault();

  public abstract String getUserHome();

  public String getLocalStorageDir()
  {
    return getUserHome();
  }

  public abstract String getUserHomeOverride();

  public abstract String getOSHome();

  public abstract String getSystemHome();

  public abstract String getPlatformSpecificJavaName();

  public abstract String getPlatformExtension();

  public abstract String getDebugJavaPath(String paramString);

  public abstract String getLongPathName(String paramString);

  public abstract boolean samePaths(String paramString1, String paramString2);

  public abstract int getPlatformMaxCommandLineLength();

  public abstract boolean useAltFileSystemView();

  public abstract String toExecArg(String paramString);

  public abstract void onLoad(Object paramObject);

  public abstract void onSave(Object paramObject);

  public abstract boolean shouldPromptForAutoCheck();

  public abstract void handleUserResponse(int paramInt);

  public abstract void resetJavaHome();

  public String getJucheckPath()
  {
    return null;
  }

  protected void addJfxRuntimeIfValid(Collection paramCollection, String paramString)
  {
    if (paramString == null)
      return;
    JfxRuntime localJfxRuntime = JfxRuntime.runtimeForPath(paramString);
    if (localJfxRuntime != null)
      paramCollection.add(localJfxRuntime);
  }

  protected Collection detectInstalledJfxRuntimes()
  {
    LinkedList localLinkedList = new LinkedList();
    addJfxRuntimeIfValid(localLinkedList, Environment.getenv("FORCED_FX_ROOT"));
    addJfxRuntimeIfValid(localLinkedList, System.getProperty("java.home"));
    return localLinkedList;
  }

  public Collection getInstalledJfxRuntimes()
  {
    return getInstalledJfxRuntimes(false);
  }

  public Collection getInstalledJfxRuntimes(boolean paramBoolean)
  {
    if ((paramBoolean) || (null == this.fx_runtimes))
    {
      this.fx_runtimes = Collections.unmodifiableCollection(detectInstalledJfxRuntimes());
      if (DEBUG)
      {
        System.out.println(this.fx_runtimes.size() + " JavaFX runtime found.");
        Iterator localIterator = this.fx_runtimes.iterator();
        int i = 0;
        while (localIterator.hasNext())
        {
          i++;
          JfxRuntime localJfxRuntime = (JfxRuntime)localIterator.next();
          System.out.println("  " + i + ") " + localJfxRuntime.toString());
        }
      }
    }
    return this.fx_runtimes;
  }

  public String getPlatformNativeEncoding()
  {
    return "UTF-8";
  }

  public JfxRuntime getBestJfxRuntime(VersionID paramVersionID)
  {
    if (DEBUG)
      System.out.println("Looking for best JavaFX runtime for " + paramVersionID + " ...");
    String str = Environment.getenv("FORCED_FX_ROOT");
    if (str != null)
      return JfxRuntime.runtimeForPath(str);
    if (null == paramVersionID)
      paramVersionID = new VersionID("2.0+");
    Object localObject = null;
    Iterator localIterator = getInstalledJfxRuntimes().iterator();
    while (localIterator.hasNext())
    {
      JfxRuntime localJfxRuntime = (JfxRuntime)localIterator.next();
      VersionID localVersionID = localJfxRuntime.getProductVersion();
      if (DEBUG)
        System.out.println("Test " + localJfxRuntime);
      if (paramVersionID.equals(localVersionID))
      {
        if (DEBUG)
          System.out.println("Perfect match: " + localJfxRuntime);
        return localJfxRuntime;
      }
      if (paramVersionID.match(localVersionID))
      {
        if (DEBUG)
          System.out.println("A match: " + localJfxRuntime);
        if ((localObject == null) || (localJfxRuntime.getProductVersion().isGreaterThan(localObject.getProductVersion())))
        {
          localObject = localJfxRuntime;
          if (DEBUG)
            System.out.println("A better match: " + localObject);
        }
      }
    }
    return localObject;
  }

  public void storeCacheUpgradeInfo(Properties paramProperties)
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramProperties)
      {
        private final Properties val$info;

        public Object run()
          throws IOException
        {
          if (this.val$info == null)
          {
            localObject1 = new File(Platform.this.getUserHome(), "CacheUpgrade.properties");
            if (((File)localObject1).exists())
              ((File)localObject1).delete();
            return null;
          }
          Object localObject1 = null;
          try
          {
            Properties localProperties = Platform.this.getCacheUpgradeProperties();
            localProperties.putAll(this.val$info);
            File localFile = new File(Platform.this.getUserHome(), "CacheUpgrade.properties");
            if (!localFile.exists())
              localFile.createNewFile();
            localObject1 = new FileOutputStream(localFile);
            localProperties.store((OutputStream)localObject1, "");
          }
          finally
          {
            if (localObject1 != null)
              ((OutputStream)localObject1).close();
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Trace.ignored(localPrivilegedActionException, true);
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
  }

  public Properties getCacheUpgradeInfo(String[] paramArrayOfString)
  {
    Properties localProperties1 = getCacheUpgradeProperties();
    Properties localProperties2 = new Properties();
    for (int i = 0; i < paramArrayOfString.length; i++)
    {
      String str = localProperties1.getProperty(paramArrayOfString[i]);
      if (str == null)
        continue;
      localProperties2.put(paramArrayOfString[i], str);
    }
    return localProperties2;
  }

  protected Properties getCacheUpgradeProperties()
  {
    Properties localProperties = new Properties();
    InputStream localInputStream = null;
    try
    {
      File localFile = new File(getUserHome(), "CacheUpgrade.properties");
      if (localFile.isFile())
      {
        localInputStream = (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(localFile)
        {
          private final File val$statsFile;

          public Object run()
            throws IOException
          {
            return new FileInputStream(this.val$statsFile);
          }
        });
        localProperties.load(localInputStream);
      }
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Trace.ignored(localPrivilegedActionException, true);
    }
    catch (Exception localException1)
    {
      Trace.ignored(localException1);
    }
    finally
    {
      if (localInputStream != null)
        try
        {
          localInputStream.close();
        }
        catch (Exception localException2)
        {
          Trace.ignored(localException2);
        }
    }
    return localProperties;
  }

  public abstract String getLoadedNativeLibPath(String paramString);

  public synchronized AutoUpdater getAutoUpdater()
  {
    if (this.auInstance == null)
      this.auInstance = new AutoUpdater();
    return this.auInstance;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.Platform
 * JD-Core Version:    0.6.0
 */