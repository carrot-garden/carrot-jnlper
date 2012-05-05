package com.sun.deploy.cache;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.FileTraceListener;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.SystemUtils;
import com.sun.deploy.util.VersionString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;

abstract class CacheUpgrader
{
  private static CacheUpgrader instance;
  private static CacheUpgrader systemInstance;
  private static boolean backgroundRunning;
  private final String name;
  protected final File oldCacheDir;
  protected final File newCacheDir;
  protected UpgradeStatus old;
  protected UpgradeStatus current;
  private static final UpgradeStatus NOOP_STATUS = UpgradeStatus.getUpgradeStatus("noopUpgradeBegin", 9223372036854775807L, "noopUpgradeCount", 2147483647);
  private static final String SEP = File.separator;
  private static File oldDefaultCacheDir;
  private static File oldDefaultSystemCacheDir;

  protected CacheUpgrader(String paramString, File paramFile1, File paramFile2)
  {
    this.name = paramString;
    this.oldCacheDir = paramFile1;
    this.newCacheDir = paramFile2;
  }

  public String getName()
  {
    return this.name;
  }

  public String toString()
  {
    return this.name;
  }

  static synchronized CacheUpgrader getInstance()
  {
    if ((instance == null) && (!Config.get().isCacheUpgradeSkipped()) && (UpgradeStatus.beenInitialized("Cache6Upgrader")))
    {
      File localFile1 = getOldDefaultCacheDir();
      if (localFile1 != null)
      {
        File localFile2 = new File(Config.get().getDefaultCacheVersionDirectory());
        File localFile3 = Cache.getCacheDir();
        boolean bool = localFile2.equals(localFile3);
        int i = !localFile1.equals(localFile3) ? 1 : 0;
        if ((bool) && (i != 0))
        {
          Cache6Upgrader localCache6Upgrader = Cache6Upgrader.createInstance(false, localFile1, localFile3);
          if (!localCache6Upgrader.isUpgradeDone())
          {
            instance = localCache6Upgrader;
            startBackgroundUpgradeIfNeeded();
          }
        }
      }
    }
    if (instance == null)
      instance = new NoopUpgrader();
    return instance;
  }

  static synchronized CacheUpgrader getSystemInstance()
  {
    if ((systemInstance == null) && (!Config.get().isCacheUpgradeSkipped()) && (UpgradeStatus.beenInitialized("SystemCache6Upgrader")))
    {
      File localFile1 = getOldDefaultSystemCacheDir();
      File localFile2 = Cache.getSystemCacheDir();
      if ((localFile1 != null) && (localFile2 != null))
      {
        String str = Config.get().getDefaultSystemCacheVersionDirectory();
        Object localObject = str != null ? new File(str) : null;
        boolean bool = localFile2.equals(localObject);
        int i = !localFile1.equals(localFile2) ? 1 : 0;
        if ((bool) && (i != 0))
        {
          Cache6Upgrader localCache6Upgrader = Cache6Upgrader.createInstance(false, localFile1, localFile2);
          if (!localCache6Upgrader.isUpgradeDone())
          {
            systemInstance = localCache6Upgrader;
            startBackgroundUpgradeIfNeeded();
          }
        }
      }
    }
    if (systemInstance == null)
      systemInstance = new NoopUpgrader();
    return systemInstance;
  }

  static void setInstance(CacheUpgrader paramCacheUpgrader)
  {
    instance = paramCacheUpgrader;
  }

  static void setSystemInstance(CacheUpgrader paramCacheUpgrader)
  {
    systemInstance = paramCacheUpgrader;
  }

  public void upgrade()
    throws IOException
  {
    if (this.oldCacheDir == null)
    {
      if (isTracing())
        trace(this + ": No old cache, no upgrade");
      return;
    }
    if (!incrementUpgradeAttempts())
      return;
    upgradeImpl();
    setUpgradeCompleted();
    if (isTracing())
      trace(this + ": End upgrade");
  }

  public CacheEntry upgradeItem(URL paramURL, String paramString, int paramInt)
  {
    if (this.oldCacheDir == null)
      return null;
    if (this.oldCacheDir.equals(this.newCacheDir))
      return null;
    return upgradeItemImpl(paramURL, paramString, paramInt);
  }

  protected abstract void upgradeImpl();

  protected abstract CacheEntry upgradeItemImpl(URL paramURL, String paramString, int paramInt);

  protected UpgradeStatus getStatus()
  {
    this.old = this.current;
    this.current = UpgradeStatus.getUpgradeStatus(getName());
    return this.current;
  }

  protected boolean incrementUpgradeAttempts()
  {
    getStatus();
    int i = this.old == null ? 0 : this.old.getUpgradeAttempts();
    return this.current.incrementUpgradeAttempt(i);
  }

  protected void setUpgradeCompleted()
  {
    getStatus().setCompleted();
  }

  protected static File getOldDefaultCacheDir()
  {
    if (oldDefaultCacheDir == null)
      oldDefaultCacheDir = getOldDefaultCache6Dir();
    if ((oldDefaultCacheDir != null) && (SystemUtils.priviledgedIsDirectory(oldDefaultCacheDir)))
      return oldDefaultCacheDir;
    return null;
  }

  static void setOldDefaultCacheDir(File paramFile)
  {
    oldDefaultCacheDir = paramFile;
  }

  protected static File getOldDefaultSystemCacheDir()
  {
    if (oldDefaultSystemCacheDir == null)
      oldDefaultSystemCacheDir = getOldDefaultSystemCache6Dir();
    if ((oldDefaultSystemCacheDir != null) && (SystemUtils.priviledgedIsDirectory(oldDefaultSystemCacheDir)))
      return oldDefaultSystemCacheDir;
    return null;
  }

  static void setOldDefaultSystemCacheDir(File paramFile)
  {
    oldDefaultSystemCacheDir = paramFile;
  }

  static File getOldDefaultCache6Dir()
  {
    return new File(Config.getUserHome() + SEP + "cache" + SEP + "6.0");
  }

  static File getOldDefaultSystemCache6Dir()
  {
    return new File(Config.getUserHome() + SEP + "SystemCache" + SEP + "6.0");
  }

  protected CacheEntry getCacheEntryFromIndexFile(File paramFile)
  {
    CacheEntry localCacheEntry = new CacheEntry(paramFile);
    if ((localCacheEntry != null) && (localCacheEntry.isValidEntry()))
      return localCacheEntry;
    return null;
  }

  protected CacheEntry getOldCacheEntry(URL paramURL, String paramString, int paramInt)
  {
    if (this.oldCacheDir == null)
      throw new IllegalStateException("Should never called w/o old cache");
    CacheEntry localCacheEntry1 = null;
    File[] arrayOfFile = Cache.getMatchingIndexFiles(this.oldCacheDir, paramURL);
    if (arrayOfFile != null)
    {
      for (int i = 0; i < arrayOfFile.length; i++)
      {
        CacheEntry localCacheEntry2 = new CacheEntry(arrayOfFile[i]);
        if ((!localCacheEntry2.isValidEntry()) || (!localCacheEntry2.getURL().equals(paramURL.toString())))
          continue;
        localCacheEntry1 = getBetterMatch(paramString, localCacheEntry2, localCacheEntry1);
      }
      if ((localCacheEntry1 != null) && (DownloadEngine.isNativeContentType(paramInt)))
      {
        File localFile = new File(localCacheEntry1.getNativeLibPath());
        if (!localFile.isDirectory())
          localCacheEntry1 = null;
      }
    }
    return localCacheEntry1;
  }

  protected boolean resourcePresentInNewCache(URL paramURL)
  {
    if (this.newCacheDir == null)
      throw new IllegalStateException("Should never called w/o new cache");
    File[] arrayOfFile = Cache.getMatchingIndexFiles(this.newCacheDir, paramURL);
    return (arrayOfFile != null) && (arrayOfFile.length > 0);
  }

  static CacheEntry getBetterMatch(String paramString, CacheEntry paramCacheEntry1, CacheEntry paramCacheEntry2)
  {
    if (paramString == null)
    {
      if (paramCacheEntry1.getVersion() == null)
        paramCacheEntry2 = paramCacheEntry1;
      else if ((paramCacheEntry2 != null) && (paramCacheEntry2.getVersion() != null) && (paramCacheEntry1.getVersion().compareTo(paramCacheEntry2.getVersion()) > 0))
        paramCacheEntry2 = paramCacheEntry1;
    }
    else if (new VersionString(paramString).contains(paramCacheEntry1.getVersion()))
      if (paramCacheEntry2 == null)
        paramCacheEntry2 = paramCacheEntry1;
      else if ((paramCacheEntry1.getVersion() != null) && (paramCacheEntry1.getVersion().compareTo(paramCacheEntry2.getVersion()) > 0))
        paramCacheEntry2 = paramCacheEntry1;
    return paramCacheEntry2;
  }

  protected static void copyDirIgnoresErrors(File paramFile1, File paramFile2)
  {
    copyDirIgnoresErrors(paramFile1, paramFile2, null);
  }

  protected static void copyDirIgnoresErrors(File paramFile1, File paramFile2, FilenameFilter paramFilenameFilter)
  {
    if (paramFile1.isDirectory())
    {
      paramFile2.mkdirs();
      File[] arrayOfFile = paramFile1.listFiles(paramFilenameFilter);
      for (int i = 0; i < arrayOfFile.length; i++)
      {
        File localFile1 = arrayOfFile[i];
        File localFile2 = new File(paramFile2, localFile1.getName());
        if (arrayOfFile[i].isDirectory())
        {
          copyDirIgnoresErrors(localFile1, localFile2, paramFilenameFilter);
        }
        else
        {
          if (localFile2.exists())
            continue;
          try
          {
            Cache.copyFile(localFile1, localFile2);
          }
          catch (IOException localIOException)
          {
            Trace.ignored(localIOException);
          }
        }
      }
    }
  }

  protected CacheEntry copyToNewCache(CacheEntry paramCacheEntry)
  {
    try
    {
      return (CacheEntry)AccessController.doPrivileged(new PrivilegedExceptionAction(paramCacheEntry)
      {
        private final CacheEntry val$oldEntry;

        public Object run()
          throws IOException
        {
          return CacheUpgrader.this._copyToNewCache(this.val$oldEntry);
        }
      });
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    return null;
  }

  private CacheEntry _copyToNewCache(CacheEntry paramCacheEntry)
  {
    if (isTracing())
      trace(this + " start copying entry: " + paramCacheEntry.getDataFile());
    CacheEntry localCacheEntry = null;
    try
    {
      URL localURL = new URL(paramCacheEntry.getURL());
      String str2 = paramCacheEntry.getVersion();
      String str3 = Cache.generateCacheFileName(localURL, str2);
      File localFile1 = new File(this.newCacheDir, str3 + ".idx");
      localCacheEntry = new CacheEntry(localFile1);
      File localFile2 = new File(paramCacheEntry.getResourceFilename());
      File localFile3 = new File(this.newCacheDir, str3);
      localFile3.getParentFile().mkdirs();
      Cache.copyFile(localFile2, localFile3);
      File localFile4 = new File(paramCacheEntry.getNativeLibPath());
      if (localFile4.isDirectory())
        copyDirIgnoresErrors(localFile4, new File(localCacheEntry.getNativeLibPath()));
      localCacheEntry.setURL(localURL.toString());
      localCacheEntry.setContentLength(paramCacheEntry.getContentLength());
      localCacheEntry.setLastModified(paramCacheEntry.getLastModified());
      localCacheEntry.setExpirationDate(paramCacheEntry.getExpirationDate());
      if (str2 != null)
        localCacheEntry.setVersion(str2);
      localCacheEntry.setHeaders(paramCacheEntry.cloneHeaders());
      localCacheEntry.setBusy(CacheEntry.BUSY_FALSE);
      localCacheEntry.setIncomplete(CacheEntry.INCOMPLETE_FALSE);
      localCacheEntry.writeFileToDisk();
      Cache.recordLastAccessed();
      if (localCacheEntry.isJNLPFile())
        upgradeLocalAppProperties(localCacheEntry, paramCacheEntry);
    }
    catch (Exception localException)
    {
      if (localCacheEntry != null)
        Cache.removeCacheEntry(localCacheEntry);
      Trace.ignoredException(localException);
      localCacheEntry = null;
    }
    if (isTracing())
    {
      String str1 = localCacheEntry.getDataFile() + " done.";
      System.out.println(this + " copy entry: " + str1);
    }
    return localCacheEntry;
  }

  protected void upgradeLocalAppProperties(CacheEntry paramCacheEntry1, CacheEntry paramCacheEntry2)
  {
    CacheUpgradeHelper.upgradeLocalAppProperties(paramCacheEntry1, paramCacheEntry2);
  }

  boolean isUpgradeDone()
  {
    return getStatus().isDone();
  }

  private static String getClassPath()
  {
    String str1 = SystemUtils.getJarPath(Object.class);
    File localFile = new File(str1, "jsse.jar");
    String str2 = SystemUtils.getJarPath(CacheUpgrader.class);
    return CacheUpgradeHelper.getHelperJarPaths() + ';' + str2 + ';' + str1 + ';' + localFile.getPath();
  }

  private static synchronized void startBackgroundUpgradeIfNeeded()
  {
    if (backgroundRunning)
      return;
    backgroundRunning = true;
    try
    {
      if (noBackgroundUpgradeStartedRecenty())
      {
        String[] arrayOfString = new String[3];
        arrayOfString[0] = Environment.getJavawCommand();
        arrayOfString[1] = ("-Xbootclasspath:" + getClassPath());
        arrayOfString[2] = CacheUpgrader.class.getName();
        if (isTracing())
        {
          String str = Arrays.asList(arrayOfString).toString();
          trace("Starting background upgrade: " + str);
        }
        AccessController.doPrivileged(new PrivilegedExceptionAction(arrayOfString)
        {
          private final String[] val$cmd;

          public Object run()
            throws IOException
          {
            Process localProcess = Runtime.getRuntime().exec(this.val$cmd);
            Thread localThread1 = new Thread(new CacheUpgrader.ProcesOutputHandler(localProcess.getInputStream()));
            localThread1.setDaemon(true);
            localThread1.start();
            Thread localThread2 = new Thread(new CacheUpgrader.ProcesOutputHandler(localProcess.getErrorStream()));
            localThread2.setDaemon(true);
            localThread2.start();
            return null;
          }
        });
      }
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
  }

  private static boolean noBackgroundUpgradeStartedRecenty()
    throws IOException
  {
    return (!getInstance().isBackgroundUpgradeStartedRecenty()) && (!getSystemInstance().isBackgroundUpgradeStartedRecenty());
  }

  boolean isBackgroundUpgradeStartedRecenty()
  {
    return getStatus().wasStartedRecently();
  }

  public static void setupServices()
  {
    String str = System.getProperty("os.name");
    if (str.startsWith("Win"))
      ServiceManager.setService(33024);
    else if (str.startsWith("Mac"))
      ServiceManager.setService(40960);
    else
      ServiceManager.setService(36864);
  }

  private static void initTrace()
  {
    if (Config.getBooleanProperty("deployment.trace"))
    {
      File localFile = new File(Config.getStringProperty("deployment.user.logdir"));
      if ((localFile.exists()) && (localFile.isDirectory()))
      {
        FileTraceListener localFileTraceListener = new FileTraceListener(new File(localFile, "cacheUpgrade.trace"), false);
        Trace.addTraceListener(localFileTraceListener);
      }
      Trace.redirectStdioStderr();
      Trace.setEnabled(TraceLevel.CACHE, true);
      Trace.setEnabled(TraceLevel.BASIC, true);
      Trace.setEnabled(TraceLevel.TEMP, true);
    }
  }

  protected static boolean isTracing()
  {
    return Trace.isEnabled(TraceLevel.CACHE);
  }

  protected static void trace(String paramString)
  {
    Trace.println(paramString, TraceLevel.CACHE);
  }

  protected static void trace(Exception paramException)
  {
    Trace.ignoredException(paramException);
  }

  static void initializeUpgraderKeys()
  {
    if (!UpgradeStatus.beenInitialized("Cache6Upgrader"))
      UpgradeStatus.initialized("Cache6Upgrader");
    if (!UpgradeStatus.beenInitialized("SystemCache6Upgrader"))
      UpgradeStatus.initialized("SystemCache6Upgrader");
  }

  public static void main(String[] paramArrayOfString)
  {
    backgroundRunning = true;
    initTrace();
    trace("Start upgraders");
    try
    {
      setupServices();
      getSystemInstance().upgrade();
      getInstance().upgrade();
    }
    catch (IOException localIOException)
    {
      trace(localIOException);
    }
    trace("End upgraders");
  }

  private static class NoopUpgrader extends CacheUpgrader
  {
    public NoopUpgrader()
    {
      super(null, null);
    }

    protected void upgradeImpl()
    {
    }

    protected CacheEntry upgradeItemImpl(URL paramURL, String paramString, int paramInt)
    {
      return null;
    }

    protected UpgradeStatus getStatus()
    {
      return CacheUpgrader.NOOP_STATUS;
    }

    protected boolean incrementUpgradeAttempts()
    {
      return true;
    }

    protected void setUpgradeCompleted()
    {
    }

    boolean isBackgroundUpgradeStartedRecenty()
    {
      return false;
    }

    boolean isUpgradeDone()
    {
      return true;
    }
  }

  static class ProcesOutputHandler
    implements Runnable
  {
    private BufferedReader reader;

    public ProcesOutputHandler(InputStream paramInputStream)
    {
      this.reader = new BufferedReader(new InputStreamReader(paramInputStream));
    }

    public void run()
    {
      try
      {
        String str = null;
        while ((str = this.reader.readLine()) != null)
          Trace.print(str, TraceLevel.CACHE);
      }
      catch (IOException localIOException)
      {
        Trace.ignored(localIOException);
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.CacheUpgrader
 * JD-Core Version:    0.6.0
 */