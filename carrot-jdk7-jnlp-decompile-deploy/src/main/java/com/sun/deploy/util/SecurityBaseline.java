package com.sun.deploy.util;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.config.Platform;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class SecurityBaseline
{
  private static final String BASELINE_VERSION_131 = "1.3.1_21";
  private static final String BASELINE_VERSION_142 = "1.4.2_36";
  private static final String BASELINE_VERSION_150 = "1.5.0_34";
  private static final String BASELINE_VERSION_160 = "1.6.0_31";
  private static final String BASELINE_VERSION_170 = "1.7.0_03";
  private static final String BASELINE_VERSION_180 = "1.8.0";
  private static final String CURRENT_VERSION = "1.7.0_04";
  private static final String CURRENT_NODOT_VERSION = "170";
  private static final String DEPLOY_VERSION = "10.0.0.20";
  private static final String DEPLOY_NODOT_VERSION = "1000";
  private static String baseline_131 = "1.3.1_21";
  private static String baseline_142 = "1.4.2_36";
  private static String baseline_150 = "1.5.0_34";
  private static String baseline_160 = "1.6.0_31";
  private static String baseline_170 = "1.7.0_03";
  private static String baseline_180 = "1.8.0";
  private static boolean baselines_initialized = false;
  private static final boolean DEBUG = Config.getBooleanProperty("deployment.baseline.debug");
  private static final long UPDATE_INTERVAL = DEBUG ? 10000 : 604800000;
  private static final long THREAD_SLEEP_INTERVAL = DEBUG ? 1000 : 60000;
  private static final String BASELINE_FILENAME = "baseline.versions";
  private static final String BASELINE_TIMESTAMP = "baseline.timestamp";
  private static final String UPDATE_TIMESTAMP = "update.timestamp";

  private static File getBaselineFile()
  {
    String str = Config.getUserHome() + File.separator + "security" + File.separator + "baseline.versions";
    return new File(str);
  }

  private static File getTimeStampFile()
  {
    String str = Config.getUserHome() + File.separator + "security" + File.separator + "baseline.timestamp";
    return new File(str);
  }

  private static File getUpdateTimeStampFile()
  {
    String str = Config.getUserHome() + File.separator + "security" + File.separator + "update.timestamp";
    return new File(str);
  }

  private static void initialize_baselines()
  {
    if (!baselines_initialized)
    {
      File localFile = getBaselineFile();
      if (localFile.exists())
      {
        BufferedReader localBufferedReader = null;
        try
        {
          long l = System.currentTimeMillis();
          localBufferedReader = new BufferedReader(new FileReader(localFile));
          int i = 0;
          while (i == 0)
          {
            String str = localBufferedReader.readLine();
            if (str == null)
            {
              i = 1;
            }
            else if (str.startsWith("1.8"))
            {
              baseline_180 = str;
              Platform.get().cacheSecurityBaseline("1.8.0", baseline_180);
            }
            else if (str.startsWith("1.7"))
            {
              baseline_170 = str;
              Platform.get().cacheSecurityBaseline("1.7.0", baseline_170);
            }
            else if (str.startsWith("1.6"))
            {
              baseline_160 = str;
              Platform.get().cacheSecurityBaseline("1.6.0", baseline_160);
            }
            else if (str.startsWith("1.5"))
            {
              baseline_150 = str;
              Platform.get().cacheSecurityBaseline("1.5.0", baseline_150);
            }
            else if (str.startsWith("1.4.2"))
            {
              baseline_142 = str;
              Platform.get().cacheSecurityBaseline("1.4.2", baseline_142);
            }
            else if (str.startsWith("1.3.1"))
            {
              baseline_131 = str;
              Platform.get().cacheSecurityBaseline("1.3.1", baseline_131);
            }
          }
          if (DEBUG)
            Trace.println("It took " + (System.currentTimeMillis() - l) + " Ms. to read baseline file", TraceLevel.BASIC);
        }
        catch (Exception localException)
        {
          Trace.ignored(localException);
        }
      }
    }
    baselines_initialized = true;
  }

  private static String getBaselineVersion(String paramString)
  {
    if (!baselines_initialized)
      initialize_baselines();
    if (paramString.startsWith("1.8"))
      return baseline_180;
    if (paramString.startsWith("1.7"))
      return baseline_170;
    if (paramString.startsWith("1.6"))
      return baseline_160;
    if (paramString.startsWith("1.5"))
      return baseline_150;
    if (paramString.startsWith("1.4.2"))
      return baseline_142;
    if (paramString.startsWith("1.3.1"))
      return baseline_131;
    return "1.7.0_04";
  }

  public static boolean satisfiesSecurityBaseline(String paramString)
  {
    VersionID localVersionID = new VersionID(paramString);
    if (paramString.compareTo(getBaselineVersion(paramString)) >= 0)
      return true;
    return localVersionID.equals(JREInfo.getLatestVersion(false));
  }

  public static boolean satisfiesBaselineStrictly(String paramString)
  {
    VersionID localVersionID = new VersionID(paramString);
    return paramString.compareTo(getBaselineVersion(paramString)) >= 0;
  }

  public static String getDeployVersion()
  {
    return "10.0.0.20";
  }

  public static String getDeployNoDotVersion()
  {
    return "1000";
  }

  public static String getCurrentVersion()
  {
    return "1.7.0_04";
  }

  public static String getCurrentNoDotVersion()
  {
    return "170";
  }

  private static synchronized void checkForBaselineUpdates()
  {
    String str = Config.getStringProperty("deployment.baseline.url");
    if ((str != null) && (str.length() > 0))
    {
      File localFile = getBaselineFile();
      long l = 0L;
      if (localFile.exists())
        l = localFile.lastModified();
      InputStream localInputStream = null;
      FileOutputStream localFileOutputStream = null;
      try
      {
        if (DEBUG)
          Trace.println("Checking for baseline at: " + str, TraceLevel.NETWORK);
        URL localURL = new URL(str);
        URLConnection localURLConnection = localURL.openConnection();
        localURLConnection.setUseCaches(false);
        if (localURLConnection.getLastModified() > l)
        {
          Trace.println("Updating baseline file at: " + localFile + " from url: " + str, TraceLevel.NETWORK);
          localInputStream = localURLConnection.getInputStream();
          localFileOutputStream = new FileOutputStream(localFile);
          byte[] arrayOfByte = new byte[8192];
          int i = 0;
          while ((i = localInputStream.read(arrayOfByte)) != -1)
            localFileOutputStream.write(arrayOfByte, 0, i);
        }
      }
      catch (Exception localException1)
      {
        Trace.ignored(localException1);
      }
      finally
      {
        if (localFileOutputStream != null)
          try
          {
            localFileOutputStream.close();
          }
          catch (Exception localException2)
          {
          }
        if (localInputStream != null)
          try
          {
            localInputStream.close();
          }
          catch (Exception localException3)
          {
          }
      }
    }
  }

  private static long getLastChecked()
  {
    File localFile = getTimeStampFile();
    if (localFile.exists())
      return localFile.lastModified();
    return 0L;
  }

  private static void setLastChecked(long paramLong)
  {
    File localFile = getTimeStampFile();
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
      localFileOutputStream.write(46);
      localFileOutputStream.close();
    }
    catch (IOException localIOException)
    {
    }
  }

  private static long getLastUpdateNag()
  {
    File localFile = getUpdateTimeStampFile();
    if (localFile.exists())
      return localFile.lastModified();
    return 0L;
  }

  private static void setLastUpdateNag(long paramLong)
  {
    File localFile = getUpdateTimeStampFile();
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
      localFileOutputStream.write(46);
      localFileOutputStream.close();
    }
    catch (IOException localIOException)
    {
    }
  }

  public static void backgroundBaselineUpdate()
  {
    Thread localThread = new Thread(new Runnable()
    {
      public void run()
      {
        long l = new Date().getTime();
        if (l > SecurityBaseline.access$000() + SecurityBaseline.UPDATE_INTERVAL)
          try
          {
            Thread.sleep(SecurityBaseline.THREAD_SLEEP_INTERVAL);
            SecurityBaseline.access$300();
            SecurityBaseline.access$400(l);
          }
          catch (Exception localException)
          {
            Trace.ignored(localException);
          }
        if (SecurityBaseline.DEBUG)
          Trace.println("Baseline thread exiting time: " + (new Date().getTime() - l), TraceLevel.BASIC);
      }
    });
    localThread.setDaemon(true);
    localThread.start();
  }

  public static void forceBaselineUpdate()
  {
    checkForBaselineUpdates();
    initialize_baselines();
    setLastChecked(new Date().getTime());
  }

  public static void checkUpdate()
  {
    if ((!Environment.isSilentMode()) && (Config.getBooleanProperty("deployment.java.update.check") == true))
    {
      String str = JREInfo.getHomeJRE().getProduct();
      if (!satisfiesBaselineStrictly(str))
      {
        long l = new Date().getTime();
        if (l > getLastUpdateNag() + UPDATE_INTERVAL)
        {
          setLastUpdateNag(l);
          URL localURL = null;
          try
          {
            localURL = new URL("http://java.com/en/download/faq/whatis_java.xml");
          }
          catch (Exception localException)
          {
          }
          ToolkitStore.getUI();
          int i = ToolkitStore.getUI().showMessageDialog(null, new AppInfo(), 2, ResourceManager.getMessage("deployment.ssv2.update.title"), ResourceManager.getMessage("deployment.ssv2.update"), null, null, ResourceManager.getString("deployment.ssv2.update.ok"), ResourceManager.getString("deployment.ssv2.update.cancel"), null, localURL, ResourceManager.getMessage("deployment.ssv2.update.more"));
          ToolkitStore.getUI();
          if (i == 0)
            Platform.get().showDocument("http://java.com");
        }
      }
    }
  }

  static
  {
    backgroundBaselineUpdate();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.SecurityBaseline
 * JD-Core Version:    0.6.0
 */