package com.sun.javaws;

import com.sun.deploy.Environment;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.cache.DeployCacheHandler;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.config.Platform;
import com.sun.deploy.config.WebStartConfig;
import com.sun.deploy.net.DeployClassLoader;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.cookie.DeployCookieSelector;
import com.sun.deploy.net.offline.DeployOfflineManager;
import com.sun.deploy.net.proxy.DeployProxySelector;
import com.sun.deploy.net.proxy.StaticProxyManager;
import com.sun.deploy.panel.ControlPanel;
import com.sun.deploy.pings.Pings;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.si.SingleInstanceManager;
import com.sun.deploy.trace.FileTraceListener;
import com.sun.deploy.trace.LoggerTraceListener;
import com.sun.deploy.trace.SocketTraceListener;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.trace.TraceListener;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.deploy.uitoolkit.ui.ComponentRef;
import com.sun.deploy.uitoolkit.ui.ConsoleController;
import com.sun.deploy.uitoolkit.ui.ConsoleTraceListener;
import com.sun.deploy.uitoolkit.ui.ConsoleWindow;
import com.sun.deploy.util.DeploySysRun;
import com.sun.deploy.util.DeployUIManager;
import com.sun.deploy.util.JVMParameters;
import com.sun.deploy.util.PerfLogger;
import com.sun.deploy.util.SecurityBaseline;
import com.sun.javaws.exceptions.CacheAccessException;
import com.sun.javaws.exceptions.CouldNotLoadArgumentException;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.exceptions.FailedDownloadingResourceException;
import com.sun.javaws.exceptions.InvalidArgumentException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.exceptions.JNLParseException;
import com.sun.javaws.exceptions.LaunchDescException;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.security.AppContextUtil;
import com.sun.javaws.ui.JavawsSysRun;
import com.sun.javaws.ui.LaunchErrorDialog;
import com.sun.javaws.ui.SplashScreen;
import com.sun.javaws.util.JavawsConsoleController;
import com.sun.javaws.util.JavawsDialogListener;
import com.sun.jnlp.JNLPClassLoader;
import com.sun.jnlp.JnlpLookupStub;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLException;
import sun.misc.BASE64Decoder;

public class Main
{
  private static boolean _isViewer = false;
  private static boolean _silent = false;
  private static boolean _environmentInitialized = false;
  private static boolean _baselineUpdate = false;
  private static ThreadGroup _mainTG = null;
  private static ThreadGroup _systemTG;
  private static ThreadGroup _securityTG;
  private static ClassLoader _secureContextClassLoader;
  private static ThreadGroup _launchTG;
  private static String _tempfile = null;
  private static DataInputStream _tckStream = null;
  private static boolean _verbose = false;
  private static String _embedded = null;
  private static boolean uninstall = false;
  private static boolean includeInstalled = false;
  private static final String DOCBASE_KEY = "docbase";
  private static final String JNLPHREF_KEY = "jnlphref";
  private static final String EMBEDDED_KEY = "embedded";

  public static void main(String[] paramArrayOfString)
  {
    try
    {
      PerfLogger.setBaseTimeString(System.getProperty("jnlp.start.time"));
      PerfLogger.setStartTime("Java Web Start started");
      PerfLogger.setTime("Starting Main");
      Environment.setEnvironmentType(1);
      Platform.get().loadDeployNativeLib();
      PerfLogger.setTime("  - returned from Platform.get();");
      Config.get();
      PerfLogger.setTime("  - returned from Config.get();");
      Config.setInstance(new WebStartConfig());
      PerfLogger.setTime("  - back from Config.setInstance(new WebStartConfig());");
      URL.setURLStreamHandlerFactory(null);
      Thread.currentThread().setContextClassLoader(new DeployClassLoader());
      _secureContextClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(JNLPClassLoader.createClassLoader());
      String str1 = System.getProperty("java.protocol.handler.pkgs");
      String str2 = "com.sun.javaws.net.protocol";
      if (Config.isJavaVersionAtLeast14())
        str2 = str2 + "|com.sun.deploy.net.protocol";
      if (str1 != null)
        System.setProperty("java.protocol.handler.pkgs", str1 + "|" + str2);
      else
        System.setProperty("java.protocol.handler.pkgs", str2);
      PerfLogger.setTime("Start Toolkit init");
      ToolkitStore.get().warmup();
      PerfLogger.setTime("End Toolkit init");
      initializeThreadGroups();
      try
      {
        DeployCacheHandler.reset();
      }
      catch (Throwable localThrowable2)
      {
      }
      new Thread(_securityTG, new Runnable(paramArrayOfString)
      {
        private final String[] val$args;

        public void run()
        {
          AppContextUtil.createSecurityAppContext();
          Thread.currentThread().setContextClassLoader(Main.getSecureContextClassLoader());
          try
          {
            Main.continueInSecureThread(this.val$args);
          }
          catch (Throwable localThrowable)
          {
            localThrowable.printStackTrace();
          }
          Trace.flush();
        }
      }
      , "Java Web Start Main Thread").start();
    }
    catch (Throwable localThrowable1)
    {
      LaunchErrorDialog.show(null, localThrowable1, true);
    }
  }

  public static void continueInSecureThread(String[] paramArrayOfString)
  {
    String[] arrayOfString1 = Globals.parseOptions(paramArrayOfString);
    arrayOfString1 = parseArgs(arrayOfString1);
    if ((!Globals.isSilentMode()) || (Globals.isQuietMode()))
      DeployUIManager.setLookAndFeel();
    if (arrayOfString1.length > 0)
      _tempfile = arrayOfString1[(arrayOfString1.length - 1)];
    else if ((!uninstall) && (!_isViewer) && (!_baselineUpdate) && (!Environment.isSystemCacheMode()))
      LaunchErrorDialog.show(null, new InvalidArgumentException(paramArrayOfString), true);
    if (!_isViewer)
    {
      PerfLogger.setTime("Start initTrace");
      initTrace();
      PerfLogger.setTime("End initTrace");
    }
    if ((Environment.isSystemCacheMode()) && ((Config.getSystemCacheDirectory() == null) || (!Cache.canWrite())))
      if ((Environment.isImportMode()) && (Globals.isSilentMode()))
        Environment.setSystemCacheMode(false);
      else
        LaunchErrorDialog.show(null, new CacheAccessException(true), true);
    if ((Environment.isImportMode()) && (!Cache.isCacheEnabled()))
      if (Globals.isSilentMode())
        try
        {
          systemExit(-1);
        }
        catch (ExitException localExitException)
        {
          Trace.println("systemExit: " + localExitException, TraceLevel.BASIC);
          Trace.ignoredException(localExitException);
        }
      else
        LaunchErrorDialog.show(null, new CacheAccessException(Environment.isSystemCacheMode(), true), true);
    Config.validateSystemCacheDirectory();
    if ((Cache.canWrite()) || (_isViewer))
    {
      setupBrowser();
      PerfLogger.setTime("Start JnlpxArgs.verify");
      JnlpxArgs.verify();
      PerfLogger.setTime("End JnlpxArgs.verify");
      initializeExecutionEnvironment();
      PerfLogger.setTime("End InitializeExecutionEnv");
      if (uninstall)
      {
        uninstallCache(includeInstalled, arrayOfString1.length > 0 ? arrayOfString1[0] : null);
      }
      else
      {
        Cache6UpgradeHelper.getInstance();
        if (DeployOfflineManager.isGlobalOffline())
        {
          JnlpxArgs.SetIsOffline();
          DeployOfflineManager.setForcedOffline(true);
        }
        if (Environment.isSystemCacheMode())
        {
          CacheUpdateHelper.systemUpdateCheck();
        }
        else if ((Config.getBooleanProperty("deployment.javaws.cache.update")) && (CacheUpdateHelper.updateCache()))
        {
          Config.setBooleanProperty("deployment.javaws.cache.update", false);
          Config.get().storeIfNeeded();
        }
        if (_baselineUpdate)
        {
          SecurityBaseline.forceBaselineUpdate();
        }
        else if (!_isViewer)
        {
          if (Globals.TCKHarnessRun)
            tckprintln("Java Started");
          if (arrayOfString1.length > 0)
            if (Environment.isImportMode())
            {
              for (int i = 0; i < arrayOfString1.length; i++)
              {
                int j;
                if (i == 0)
                  j = arrayOfString1.length - 1;
                else
                  j = i - 1;
                boolean bool = i == arrayOfString1.length - 1;
                PerfLogger.setTime("calling launchApp for import ...");
                launchApp(arrayOfString1[j], bool);
                Environment.setJavaFxInstallMode(0);
              }
            }
            else
            {
              PerfLogger.setTime("calling launchApp ...");
              if (arrayOfString1.length != 1)
              {
                JnlpxArgs.removeArgumentFile(arrayOfString1[(arrayOfString1.length - 1)]);
                LaunchErrorDialog.show(null, new InvalidArgumentException(arrayOfString1), true);
                return;
              }
              launchApp(arrayOfString1[0], true);
            }
        }
        else
        {
          if (arrayOfString1.length > 0)
            JnlpxArgs.removeArgumentFile(arrayOfString1[0]);
          PerfLogger.setEndTime("Calling Application viewer");
          PerfLogger.outputLog();
          try
          {
            String str = _silent ? "-store" : "-viewer";
            String[] arrayOfString2 = new String[1];
            arrayOfString2[0] = str;
            launchJavaControlPanel(arrayOfString2);
          }
          catch (Exception localException)
          {
            LaunchErrorDialog.show(null, localException, true);
          }
        }
      }
    }
    else
    {
      LaunchErrorDialog.show(null, new CacheAccessException(Environment.isSystemCacheMode()), true);
    }
    Trace.flush();
    SplashScreen.hide();
  }

  private static void validateJavaFxUrl(String paramString, boolean paramBoolean)
  {
    if ((!Environment.allowAltJavaFxRuntimeURL()) && (!"http://dl.javafx.com/javafx-cache.jnlp".equals(paramString)))
    {
      LaunchErrorDialog.show(null, new Exception("Incorrect URL for JavaFX preload or auto-update"), paramBoolean);
      return;
    }
  }

  public static void launchApp(String paramString, boolean paramBoolean)
  {
    if ((Environment.isImportMode()) && (Environment.getJavaFxInstallMode() != 0))
      validateJavaFxUrl(paramString, paramBoolean);
    LaunchDesc localLaunchDesc = null;
    try
    {
      JREInfo localJREInfo = JREInfo.getHomeJRE();
      if (localJREInfo == null)
        throw new ExitException(new Exception("Internal Error: no running JRE"), 3);
      localObject2 = System.getProperty("jnlp.application.href");
      if (localObject2 != null)
      {
        localObject3 = new File(paramString);
        if (!((File)localObject3).exists())
          paramString = (String)localObject2;
      }
      Object localObject3 = LaunchDescFactory.getDocBase();
      localLaunchDesc = fromEmbedded(paramString, (URL)localObject3, _embedded);
      if (localLaunchDesc == null)
        if (localObject3 != null)
          localLaunchDesc = LaunchDescFactory.buildDescriptor(paramString, (URL)null, (URL)localObject3, true);
        else
          localLaunchDesc = LaunchDescFactory.buildDescriptor(paramString);
      if (localLaunchDesc.getLocation() == null)
        try
        {
          URL localURL = new URL(paramString);
          CacheEntry localCacheEntry = Cache.getCacheEntry(localURL, null, null);
          if (localCacheEntry != null)
            Cache.removeCacheEntry(localCacheEntry);
        }
        catch (MalformedURLException localMalformedURLException2)
        {
        }
    }
    catch (IOException localIOException)
    {
      Object localObject2 = null;
      JnlpxArgs.removeArgumentFile(paramString);
      localObject2 = new CouldNotLoadArgumentException(paramString, localIOException);
      if ((Config.isJavaVersionAtLeast14()) && (((localIOException instanceof SSLException)) || ((localIOException.getMessage() != null) && (localIOException.getMessage().toLowerCase().indexOf("https") != -1))))
        try
        {
          localObject2 = new FailedDownloadingResourceException(new URL(paramString), null, localIOException);
        }
        catch (MalformedURLException localMalformedURLException1)
        {
          Trace.ignoredException(localMalformedURLException1);
        }
      if ((Environment.isImportMode()) && (Environment.getJavaFxInstallMode() != 0))
        Pings.sendJFXPing("jfxic", Launcher.getCurrentJavaFXVersion(), "XX", 3, paramString);
      LaunchErrorDialog.show(null, (Throwable)localObject2, paramBoolean);
      return;
    }
    catch (JNLParseException localJNLParseException)
    {
      JnlpxArgs.removeArgumentFile(paramString);
      LaunchErrorDialog.show(null, localJNLParseException, paramBoolean);
      return;
    }
    catch (LaunchDescException localLaunchDescException)
    {
      Trace.println("Error parsing " + paramString + ". Try to parse again with codebase from LAP", TraceLevel.BASIC);
      try
      {
        localLaunchDesc = LaunchDescFactory.buildDescriptor(new File(paramString));
        if (localLaunchDesc == null)
          throw localLaunchDescException;
      }
      catch (Exception localException2)
      {
        JnlpxArgs.removeArgumentFile(paramString);
        LaunchErrorDialog.show(null, localLaunchDescException, paramBoolean);
        return;
      }
    }
    catch (Exception localException1)
    {
      Trace.ignoredException(localException1);
      JnlpxArgs.removeArgumentFile(paramString);
      LaunchErrorDialog.show(null, localException1, paramBoolean);
      return;
    }
    Environment.setImportModeCodebase(localLaunchDesc.getCodebase());
    Object localObject1;
    if (localLaunchDesc.getLaunchType() == 5)
    {
      JnlpxArgs.removeArgumentFile(paramString);
      localObject1 = localLaunchDesc.getInternalCommand();
      String[] arrayOfString;
      if ((localObject1 != null) && (!((String)localObject1).equals("player")) && (!((String)localObject1).equals("viewer")))
      {
        arrayOfString = new String[2];
        arrayOfString[0] = "-tab";
        arrayOfString[1] = localLaunchDesc.getInternalCommand();
      }
      else
      {
        arrayOfString = new String[1];
        arrayOfString[0] = "-viewer";
      }
      launchJavaControlPanel(arrayOfString);
    }
    else if (Config.get().isValid())
    {
      localObject1 = new String[1];
      localObject1[0] = paramString;
      new Launcher(localLaunchDesc).launch(localObject1, paramBoolean);
    }
    else
    {
      LaunchErrorDialog.show(null, new LaunchDescException(localLaunchDesc, ResourceManager.getString("enterprize.cfg.mandatory", Config.get().getEnterpriseString()), null), paramBoolean);
    }
  }

  private static LaunchDesc fromEmbedded(String paramString1, URL paramURL, String paramString2)
  {
    if (paramString2 != null)
      try
      {
        URL localURL = null;
        try
        {
          localURL = new URL(paramString1);
        }
        catch (MalformedURLException localMalformedURLException)
        {
        }
        BASE64Decoder localBASE64Decoder = new BASE64Decoder();
        byte[] arrayOfByte = localBASE64Decoder.decodeBuffer(_embedded);
        return LaunchDownload.updateLaunchDescInCache(LaunchDescFactory.buildDescriptor(arrayOfByte, paramURL, paramURL, localURL), paramURL, paramURL);
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
    return null;
  }

  static void importApp(String paramString)
  {
    boolean bool1 = Environment.isImportMode();
    Environment.setImportMode(true);
    boolean bool2 = Globals.isSilentMode();
    Globals.setSilentMode(true);
    boolean bool3 = Globals.isShortcutMode();
    Globals.setCreateShortcut(true);
    launchApp(paramString, false);
    Environment.setImportMode(bool1);
    Globals.setSilentMode(bool2);
    Globals.setCreateShortcut(bool3);
  }

  private static void launchJavaControlPanel(String[] paramArrayOfString)
  {
    SplashScreen.hide();
    ControlPanel.main(paramArrayOfString);
  }

  private static void uninstallCache(boolean paramBoolean, String paramString)
  {
    int i = -1;
    try
    {
      i = uninstall(paramBoolean, paramString);
    }
    catch (Exception localException)
    {
      LaunchErrorDialog.show(null, localException, (!Globals.isSilentMode()) || (Globals.isQuietMode()));
    }
    _tempfile = null;
    try
    {
      systemExit(i);
    }
    catch (ExitException localExitException)
    {
      Trace.println("systemExit: " + localExitException, TraceLevel.BASIC);
      Trace.ignoredException(localExitException);
    }
  }

  private static Date parseDate(String paramString)
  {
    Date localDate = null;
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
    try
    {
      localDate = localSimpleDateFormat.parse(paramString);
    }
    catch (ParseException localParseException1)
    {
      ParseException localParseException2 = new ParseException(localParseException1.getMessage() + " " + ResourceManager.getString("launch.error.dateformat"), localParseException1.getErrorOffset());
      LaunchErrorDialog.show(null, localParseException2, true);
    }
    return localDate;
  }

  private static Properties loadPropertiesFromFile(String paramString1, String paramString2)
  {
    FileInputStream localFileInputStream = null;
    InputStreamReader localInputStreamReader = null;
    try
    {
      localFileInputStream = new FileInputStream(paramString1);
      localInputStreamReader = new InputStreamReader(localFileInputStream, paramString2);
      Properties localProperties1 = new Properties();
      localProperties1.load(localInputStreamReader);
      localProperties2 = localProperties1;
      return localProperties2;
    }
    catch (Exception localException)
    {
      Properties localProperties2 = new Properties();
      return localProperties2;
    }
    finally
    {
      if (localInputStreamReader != null)
        try
        {
          localInputStreamReader.close();
        }
        catch (IOException localIOException5)
        {
          Trace.ignoredException(localIOException5);
        }
      else if (localFileInputStream != null)
        try
        {
          localFileInputStream.close();
        }
        catch (IOException localIOException6)
        {
          Trace.ignoredException(localIOException6);
        }
    }
    throw localObject;
  }

  private static String processLaunchPropFile(String paramString)
  {
    String str1 = null;
    try
    {
      Properties localProperties = loadPropertiesFromFile(paramString, Platform.get().getPlatformNativeEncoding());
      if (localProperties.getProperty("docbase") == null)
        localProperties = loadPropertiesFromFile(paramString, "UTF-8");
      String str2 = localProperties.getProperty("docbase");
      String str3 = localProperties.getProperty("jnlphref");
      String str4 = localProperties.getProperty("embedded");
      URL localURL1;
      if ((str2 != null) && (str2.length() > 0))
      {
        localURL1 = new URL(str2);
        LaunchDescFactory.setDocBase(localURL1);
        Trace.println("docbase from launch file: " + localURL1.toString(), TraceLevel.BASIC);
      }
      if ((str3 != null) && (str3.length() > 0))
      {
        localURL1 = null;
        try
        {
          localURL1 = new URL(str3);
        }
        catch (MalformedURLException localMalformedURLException)
        {
          URL localURL2 = LaunchDescFactory.getDerivedCodebase();
          if (localURL2 != null)
            localURL1 = new URL(localURL2 + str3);
        }
        if (localURL1 != null)
        {
          str1 = localURL1.toString();
          Trace.println("jnlphref from launch property file: " + str1, TraceLevel.BASIC);
        }
      }
      if ((str4 != null) && (str4.length() > 0))
        _embedded = str4;
    }
    catch (Exception localException)
    {
      LaunchErrorDialog.show(null, new Throwable("Launch file error", localException), true);
    }
    finally
    {
      if (paramString != null)
        Environment.setLaunchPropFile(paramString);
    }
    return str1;
  }

  private static String[] parseArgs(String[] paramArrayOfString)
  {
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < paramArrayOfString.length; i++)
      if (!paramArrayOfString[i].startsWith("-"))
      {
        localArrayList.add(paramArrayOfString[i]);
      }
      else if (paramArrayOfString[i].equals("-offline"))
      {
        JnlpxArgs.SetIsOffline();
        DeployOfflineManager.setForcedOffline(true);
      }
      else
      {
        if ((paramArrayOfString[i].equals("-online")) || (paramArrayOfString[i].equals("-Xnosplash")))
          continue;
        if (paramArrayOfString[i].equals("-installer"))
        {
          Environment.setInstallMode(true);
        }
        else if (paramArrayOfString[i].equals("-uninstall"))
        {
          uninstall = true;
          includeInstalled = true;
          Environment.setInstallMode(true);
          Environment.setImportMode(true);
        }
        else if (paramArrayOfString[i].equals("-clearcache"))
        {
          uninstall = true;
          includeInstalled = false;
          Environment.setInstallMode(true);
          Environment.setImportMode(true);
        }
        else if (paramArrayOfString[i].equals("-import"))
        {
          Environment.setImportMode(true);
        }
        else if (paramArrayOfString[i].equals("-quiet"))
        {
          Globals.setQuietMode(true);
        }
        else if (paramArrayOfString[i].equals("-silent"))
        {
          _silent = true;
          Globals.setSilentMode(true);
        }
        else if (paramArrayOfString[i].equals("-reverse"))
        {
          Globals.setReverseMode(true);
        }
        else if (paramArrayOfString[i].equals("-javafx"))
        {
          Environment.setJavaFxInstallMode(1);
        }
        else if (paramArrayOfString[i].equals("-javafxau"))
        {
          Environment.setJavaFxInstallMode(2);
        }
        else if (paramArrayOfString[i].equals("-shortcut"))
        {
          Globals.setCreateShortcut(true);
        }
        else if (paramArrayOfString[i].equals("-association"))
        {
          Globals.setCreateAssoc(true);
        }
        else if (paramArrayOfString[i].equals("-prompt"))
        {
          Globals.setShowPrompts(true);
        }
        else if (paramArrayOfString[i].equals("-nocodebase"))
        {
          if (i + 1 >= paramArrayOfString.length)
            continue;
          i++;
          localObject = paramArrayOfString[i];
          String str = processLaunchPropFile((String)localObject);
          if (str != null)
            localArrayList.add(str);
        }
        else if (paramArrayOfString[i].equals("-docbase"))
        {
          if (i + 1 >= paramArrayOfString.length)
            continue;
          i++;
          localObject = paramArrayOfString[i];
          try
          {
            LaunchDescFactory.setDocBase(new URL((String)localObject));
          }
          catch (MalformedURLException localMalformedURLException1)
          {
            Trace.ignoredException(localMalformedURLException1);
          }
        }
        else if (paramArrayOfString[i].equals("-codebase"))
        {
          if (i + 1 >= paramArrayOfString.length)
            continue;
          i++;
          localObject = paramArrayOfString[i];
          try
          {
            new URL((String)localObject);
          }
          catch (MalformedURLException localMalformedURLException2)
          {
            LaunchErrorDialog.show(null, localMalformedURLException2, true);
          }
          Environment.setImportModeCodebaseOverride((String)localObject);
        }
        else
        {
          Date localDate;
          if (paramArrayOfString[i].equals("-timestamp"))
          {
            if (i + 1 >= paramArrayOfString.length)
              continue;
            i++;
            localObject = paramArrayOfString[i];
            localDate = parseDate((String)localObject);
            if (localDate != null)
              Environment.setImportModeTimestamp(localDate);
          }
          else if (paramArrayOfString[i].equals("-expiration"))
          {
            if (i + 1 >= paramArrayOfString.length)
              continue;
            i++;
            localObject = paramArrayOfString[i];
            localDate = parseDate((String)localObject);
            if (localDate != null)
              Environment.setImportModeExpiration(localDate);
          }
          else if (paramArrayOfString[i].equals("-system"))
          {
            Environment.setSystemCacheMode(true);
          }
          else if (paramArrayOfString[i].equals("-secure"))
          {
            Globals.setSecureMode(true);
          }
          else if ((paramArrayOfString[i].equals("-open")) || (paramArrayOfString[i].equals("-print")))
          {
            if (i + 1 >= paramArrayOfString.length)
              continue;
            localObject = new String[2];
            localObject[0] = paramArrayOfString[(i++)];
            localObject[1] = paramArrayOfString[i];
            Globals.setApplicationArgs(localObject);
            SingleInstanceManager.setActionName(localObject[0]);
            SingleInstanceManager.setOpenPrintFilePath(localObject[1]);
          }
          else if (paramArrayOfString[i].equals("-viewer"))
          {
            _isViewer = true;
          }
          else if (paramArrayOfString[i].equals("-verbose"))
          {
            _verbose = true;
          }
          else if (paramArrayOfString[i].equals("-SSVBaselineUpdate"))
          {
            _baselineUpdate = true;
          }
          else
          {
            Trace.println("unsupported option: " + paramArrayOfString[i], TraceLevel.BASIC);
          }
        }
      }
    Object localObject = new String[localArrayList.size()];
    for (int j = 0; j < localObject.length; j++)
      localObject[j] = ((String)localArrayList.get(j));
    return (String)localObject;
  }

  private static void initTrace()
  {
    Trace.redirectStdioStderr();
    Trace.resetTraceLevel();
    Trace.clearTraceListeners();
    Trace.setInitialTraceLevel();
    if ((_verbose) || (Globals.TraceBasic))
      Trace.setEnabled(TraceLevel.BASIC, true);
    if ((_verbose) || (Globals.TraceNetwork))
      Trace.setEnabled(TraceLevel.NETWORK, true);
    if ((_verbose) || (Globals.TraceCache))
      Trace.setEnabled(TraceLevel.CACHE, true);
    if ((_verbose) || (Globals.TraceSecurity))
      Trace.setEnabled(TraceLevel.SECURITY, true);
    if ((_verbose) || (Globals.TraceExtensions))
      Trace.setEnabled(TraceLevel.EXTENSIONS, true);
    if ((_verbose) || (Globals.TraceTemp))
      Trace.setEnabled(TraceLevel.TEMP, true);
    PerfLogger.setTime("Start setup Console");
    if ((Config.getStringProperty("deployment.console.startup.mode").equals("SHOW")) && (!ToolkitStore.get().isHeadless()) && (!Globals.isQuietMode()))
    {
      localObject1 = JavawsConsoleController.getInstance();
      localObject2 = new ConsoleTraceListener();
      localObject3 = ToolkitStore.getUI().getConsole((ConsoleController)localObject1);
      ((JavawsConsoleController)localObject1).setConsole((ConsoleWindow)localObject3);
      ((ConsoleTraceListener)localObject2).setConsole((ConsoleWindow)localObject3);
      Trace.addTraceListener((TraceListener)localObject2);
      ((ConsoleWindow)localObject3).clear();
    }
    PerfLogger.setTime("End setup Console");
    Object localObject1 = initSocketTrace();
    if (localObject1 != null)
      Trace.addTraceListener((TraceListener)localObject1);
    Object localObject2 = initFileTrace();
    if (localObject2 != null)
      Trace.addTraceListener((TraceListener)localObject2);
    Object localObject3 = JavawsConsoleController.getInstance();
    if ((Config.isJavaVersionAtLeast14()) && (Config.getBooleanProperty("deployment.log")))
    {
      File localFile1 = null;
      try
      {
        boolean bool = false;
        String str = Config.getStringProperty("deployment.javaws.logFileName");
        File localFile2 = new File(Config.getLogDirectory());
        if ((str != null) && (!"".equals(str)))
        {
          localFile1 = new File(str);
          if (localFile1.isDirectory())
            localFile1 = null;
          else
            bool = true;
        }
        LoggerTraceListener localLoggerTraceListener = LoggerTraceListener.getOrCreateSharedInstance("com.sun.deploy", localFile1, localFile2, "javaws", ".log", bool);
        if (localLoggerTraceListener != null)
        {
          localLoggerTraceListener.getLogger().setLevel(Level.ALL);
          ((JavawsConsoleController)localObject3).setLogger(localLoggerTraceListener.getLogger());
          Trace.addTraceListener(localLoggerTraceListener);
        }
      }
      catch (Exception localException)
      {
        Trace.println("can not create log file in directory: " + Config.getLogDirectory(), TraceLevel.BASIC);
      }
    }
  }

  private static FileTraceListener initFileTrace()
  {
    if (Config.getBooleanProperty("deployment.trace"))
    {
      File localFile = null;
      String str1 = Config.getLogDirectory();
      String str2 = Config.getStringProperty("deployment.javaws.traceFileName");
      boolean bool = false;
      try
      {
        if ((str2 != null) && (!"".equals(str2)) && (str2.compareToIgnoreCase("TEMP") != 0))
        {
          localFile = new File(str2);
          if (!localFile.isDirectory())
          {
            int i = str2.lastIndexOf(File.separator);
            if (i != -1)
              str1 = str2.substring(0, i);
            bool = true;
          }
          else
          {
            localFile = null;
          }
        }
        return FileTraceListener.getOrCreateSharedInstance(localFile, new File(str1), "javaws", ".trace", true, bool);
      }
      catch (Exception localException)
      {
        Trace.println("cannot create trace file in Directory: " + str1, TraceLevel.BASIC);
      }
    }
    return null;
  }

  private static SocketTraceListener initSocketTrace()
  {
    if (Globals.LogToHost != null)
    {
      String str1 = Globals.LogToHost;
      String str2 = null;
      int i = -1;
      int j = 0;
      int k = 0;
      if ((str1.charAt(0) == '[') && ((k = str1.indexOf(1, 93)) != -1))
        j = 1;
      else
        k = str1.indexOf(":");
      str2 = str1.substring(j, k);
      if (str2 == null)
        return null;
      try
      {
        String str3 = str1.substring(str1.lastIndexOf(':') + 1);
        i = Integer.parseInt(str3);
      }
      catch (NumberFormatException localNumberFormatException)
      {
        i = -1;
      }
      if (i < 0)
        return null;
      SocketTraceListener localSocketTraceListener = new SocketTraceListener(str2, i);
      if (localSocketTraceListener != null)
      {
        Socket localSocket = localSocketTraceListener.getSocket();
        if ((Globals.TCKResponse) && (localSocket != null))
          try
          {
            _tckStream = new DataInputStream(localSocket.getInputStream());
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
      }
      return localSocketTraceListener;
    }
    return null;
  }

  private static int uninstall(boolean paramBoolean, String paramString)
  {
    if (paramString == null)
    {
      Trace.println("Uninstall all!", TraceLevel.BASIC);
      CacheUtil.remove(paramBoolean);
      if (Globals.TCKHarnessRun)
        tckprintln("Cache Clear Success");
    }
    else
    {
      Trace.println("Uninstall: " + paramString, TraceLevel.BASIC);
      LaunchDesc localLaunchDesc = null;
      try
      {
        localLaunchDesc = LaunchDescFactory.buildDescriptor(paramString);
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
      catch (JNLPException localJNLPException)
      {
        Trace.ignoredException(localJNLPException);
      }
      Object localObject1;
      Object localObject2;
      String str2;
      if (localLaunchDesc != null)
      {
        Object localObject3;
        if (Globals.showPrompts())
        {
          localObject1 = localLaunchDesc.getInformation().getTitle();
          localObject2 = ResourceManager.getString("uninstall.app.prompt.title");
          localObject3 = ResourceManager.getString("uninstall.app.prompt.message", (String)localObject1);
          str2 = ResourceManager.getString("common.ok_btn");
          String str3 = ResourceManager.getString("common.cancel_btn");
          ToolkitStore.getUI();
          ToolkitStore.getUI();
          if (ToolkitStore.getUI().showMessageDialog(null, localLaunchDesc.getAppInfo(), 3, (String)localObject2, null, (String)localObject3, null, str2, str3, null) != 0)
          {
            Trace.println("Uninstall canceled by user.", TraceLevel.BASIC);
            return 0;
          }
        }
        localObject1 = null;
        if ((localLaunchDesc.isInstaller()) || (localLaunchDesc.isLibrary()))
          localObject1 = Cache.getLocalApplicationProperties(paramString);
        else
          localObject1 = Cache.getLocalApplicationProperties(localLaunchDesc.getCanonicalHome());
        if (localObject1 != null)
        {
          localObject2 = null;
          try
          {
            localObject2 = new File(paramString);
            if (!((File)localObject2).exists())
            {
              localObject3 = new URL(paramString);
              localObject2 = DownloadEngine.getCachedFile((URL)localObject3);
            }
          }
          catch (Exception localException)
          {
            Trace.ignored(localException);
          }
          if ((localObject2 != null) && (((File)localObject2).exists()))
            CacheUtil.remove((File)localObject2, localLaunchDesc, new ArrayList());
          if (Globals.TCKHarnessRun)
            tckprintln("Cache Clear Success");
          return 0;
        }
      }
      Trace.println("Error uninstalling!", TraceLevel.BASIC);
      if (Globals.TCKHarnessRun)
        tckprintln("Cache Clear Failed");
      if ((!Globals.isSilentMode()) || (Globals.isQuietMode()))
      {
        SplashScreen.hide();
        localObject1 = ResourceManager.getMessage("uninstall.failedMessageTitle");
        localObject2 = ResourceManager.getMessage("uninstall.failedMessage");
        String str1 = ResourceManager.getString("common.ok_btn");
        str2 = ResourceManager.getString("common.detail.button");
        ToolkitStore.getUI();
        ToolkitStore.getUI().showMessageDialog(null, null, 0, (String)localObject1, null, (String)localObject2, null, str1, str2, null);
      }
    }
    return 0;
  }

  private static void setupBrowser()
  {
    if (Config.getBooleanProperty("deployment.capture.mime.types"))
    {
      setupNS6();
      setupOpera();
      Config.setBooleanProperty("deployment.capture.mime.types", false);
    }
  }

  private static void setupOpera()
  {
    OperaSupport localOperaSupport = BrowserSupport.getInstance().getOperaSupport();
    if ((localOperaSupport != null) && (localOperaSupport.isInstalled()))
      localOperaSupport.enableJnlp(new File(Platform.get().getSystemJavawsPath()), Config.getBooleanProperty("deployment.update.mime.types"));
  }

  private static void setupNS6()
  {
    String str1 = null;
    str1 = BrowserSupport.getInstance().getNS6MailCapInfo();
    String str2 = "user_pref(\"browser.helperApps.neverAsk.openFile\", \"application%2Fx-java-jnlp-file\");\n";
    File localFile = null;
    String str3 = Platform.get().getMozillaUserProfileDirectory();
    if (str3 != null)
      localFile = new File(str3 + File.separator + "prefs.js");
    if (localFile == null)
      return;
    FileInputStream localFileInputStream = null;
    try
    {
      String str4 = null;
      localFileInputStream = new FileInputStream(localFile);
      localObject1 = new BufferedReader(new InputStreamReader(localFileInputStream));
      localObject2 = "";
      int i = 1;
      int j;
      if (str1 == null)
        j = 0;
      else
        j = 1;
      while (true)
        try
        {
          str4 = ((BufferedReader)localObject1).readLine();
          if (str4 != null)
            continue;
          localFileInputStream.close();
          break;
          localObject2 = (String)localObject2 + str4 + "\n";
          if (str4.indexOf("x-java-jnlp-file") == -1)
            continue;
          i = 0;
          if ((str1 == null) || (str4.indexOf(".mime.types") == -1))
            continue;
          j = 0;
          continue;
        }
        catch (IOException localIOException2)
        {
          Trace.ignoredException(localIOException2);
        }
      if ((i == 0) && (j == 0))
        return;
      if (i != 0)
        localObject2 = (String)localObject2 + str2;
      if ((str1 != null) && (j != 0))
        localObject2 = (String)localObject2 + str1;
      FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
      try
      {
        localFileOutputStream.write(((String)localObject2).getBytes());
        localFileOutputStream.close();
      }
      catch (IOException localIOException3)
      {
        Trace.ignoredException(localIOException3);
      }
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      Object localObject2;
      Trace.ignoredException(localFileNotFoundException);
      Object localObject1 = "";
      if (str1 != null)
        localObject1 = (String)localObject1 + str1;
      localObject1 = (String)localObject1 + str2;
      try
      {
        localObject2 = new FileOutputStream(localFile);
        ((FileOutputStream)localObject2).write(((String)localObject1).getBytes());
        ((FileOutputStream)localObject2).close();
      }
      catch (IOException localIOException1)
      {
        Trace.ignoredException(localIOException1);
      }
    }
  }

  public static void initializeExecutionEnvironment()
  {
    if (_environmentInitialized)
      return;
    _environmentInitialized = true;
    int i = Config.getOSName().indexOf("Windows") != -1 ? 1 : 0;
    int j = Config.getOSName().indexOf("Mac") != -1 ? 1 : 0;
    boolean bool = Config.isJavaVersionAtLeast15();
    Environment.setUserAgent(Globals.getUserAgent());
    if (i != 0)
    {
      if (bool)
        com.sun.deploy.services.ServiceManager.setService(33024);
      else
        com.sun.deploy.services.ServiceManager.setService(16640);
    }
    else if (j != 0)
      com.sun.deploy.services.ServiceManager.setService(40960);
    else if (bool)
      com.sun.deploy.services.ServiceManager.setService(36864);
    else
      com.sun.deploy.services.ServiceManager.setService(20480);
    Properties localProperties = System.getProperties();
    localProperties.put("http.auth.serializeRequests", "true");
    if (Config.isJavaVersionAtLeast14())
    {
      if (Config.installDeployRMIClassLoaderSpi())
        localProperties.put("java.rmi.server.RMIClassLoaderSpi", "com.sun.jnlp.JNLPRMIClassLoaderSpi");
      String str = (String)localProperties.get("java.protocol.handler.pkgs");
      if (str != null)
        localProperties.put("java.protocol.handler.pkgs", str + "|com.sun.deploy.net.protocol");
      else
        localProperties.put("java.protocol.handler.pkgs", "com.sun.deploy.net.protocol");
    }
    localProperties.setProperty("javawebstart.version", Globals.getComponentName());
    try
    {
      PerfLogger.setTime("Start DeployProxySelector.reset");
      DeployProxySelector.reset();
      PerfLogger.setTime("End DeployProxySelector.reset");
      DeployCookieSelector.reset();
    }
    catch (Throwable localThrowable)
    {
      StaticProxyManager.reset();
    }
    DeployOfflineManager.reset();
    if (Config.getBooleanProperty("deployment.security.authenticator"))
    {
      localObject = JAuthenticator.getInstance((ComponentRef)null);
      Authenticator.setDefault((Authenticator)localObject);
    }
    javax.jnlp.ServiceManager.setServiceManagerStub(new JnlpLookupStub());
    Config.setupPackageAccessRestriction();
    com.sun.deploy.ui.UIFactory.setDialogListener(new JavawsDialogListener());
    if (localProperties.get("https.protocols") == null)
    {
      localObject = new StringBuffer();
      if (Config.getBooleanProperty("deployment.security.TLSv1.2"))
        ((StringBuffer)localObject).append("TLSv1.2");
      if (Config.getBooleanProperty("deployment.security.TLSv1.1"))
      {
        if (((StringBuffer)localObject).length() != 0)
          ((StringBuffer)localObject).append(",");
        ((StringBuffer)localObject).append("TLSv1.1");
      }
      if (Config.getBooleanProperty("deployment.security.TLSv1"))
      {
        if (((StringBuffer)localObject).length() != 0)
          ((StringBuffer)localObject).append(",");
        ((StringBuffer)localObject).append("TLSv1");
      }
      if (Config.getBooleanProperty("deployment.security.SSLv3"))
      {
        if (((StringBuffer)localObject).length() != 0)
          ((StringBuffer)localObject).append(",");
        ((StringBuffer)localObject).append("SSLv3");
      }
      if (Config.getBooleanProperty("deployment.security.SSLv2Hello"))
      {
        if (((StringBuffer)localObject).length() != 0)
          ((StringBuffer)localObject).append(",");
        ((StringBuffer)localObject).append("SSLv2Hello");
      }
      localProperties.put("https.protocols", ((StringBuffer)localObject).toString());
    }
    Object localObject = new JVMParameters();
    long l = JnlpxArgs.getMaxHeapSize();
    if (l <= 0L)
      l = JVMParameters.getDefaultHeapSize();
    ((JVMParameters)localObject).setMaxHeapSize(l);
    l = JnlpxArgs.getInitialHeapSize();
    if ((l > 0L) && (l != JVMParameters.getDefaultHeapSize()))
      ((JVMParameters)localObject).parse("-Xms" + JVMParameters.unparseMemorySpec(l));
    ((JVMParameters)localObject).parseTrustedOptions(JnlpxArgs.getVMArgs());
    ((JVMParameters)localObject).setDefault(true);
    JVMParameters.setRunningJVMParameters((JVMParameters)localObject);
    if (Trace.isEnabled(TraceLevel.BASIC))
      Trace.println("Running JVMParams: " + localObject + "\n\t-> " + JVMParameters.getRunningJVMParameters(), TraceLevel.BASIC);
  }

  public static void systemExit(int paramInt)
    throws ExitException
  {
    try
    {
      JnlpxArgs.removeArgumentFile(_tempfile);
      SplashScreen.hide();
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
    Trace.flush();
    if (Environment.isJavaPlugin())
    {
      ExitException localExitException = new ExitException(new RuntimeException("exit(" + paramInt + ")"), 4);
      throw localExitException;
    }
    System.exit(paramInt);
  }

  public static boolean isViewer()
  {
    return _isViewer;
  }

  public static final ThreadGroup getLaunchThreadGroup()
  {
    return _launchTG;
  }

  public static final ThreadGroup getSecurityThreadGroup()
  {
    return _securityTG;
  }

  public static final ClassLoader getSecureContextClassLoader()
  {
    return _secureContextClassLoader;
  }

  static final ThreadGroup getMainThreadGroup()
  {
    return _mainTG;
  }

  private static void initializeThreadGroups()
  {
    if (_mainTG == null)
      _mainTG = Thread.currentThread().getThreadGroup();
    if (_securityTG == null)
    {
      _systemTG = Thread.currentThread().getThreadGroup();
      while (_systemTG.getParent() != null)
        _systemTG = _systemTG.getParent();
      _securityTG = new ThreadGroup(_systemTG, "javawsSecurityThreadGroup");
      DeploySysRun.setOverride(new JavawsSysRun());
      _launchTG = new ThreadGroup(_systemTG, "javawsApplicationThreadGroup");
    }
  }

  public static synchronized void tckprintln(String paramString)
  {
    long l = System.currentTimeMillis();
    Trace.println("##TCKHarnesRun##:" + l + ":" + Runtime.getRuntime().hashCode() + ":" + Thread.currentThread() + ":" + paramString);
    if (_tckStream != null)
      try
      {
        while (_tckStream.readLong() < l);
      }
      catch (IOException localIOException)
      {
        System.err.println("Warning:Exceptions occurred, while logging to logSocket");
        localIOException.printStackTrace(System.err);
      }
    Trace.flush();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.Main
 * JD-Core Version:    0.6.0
 */