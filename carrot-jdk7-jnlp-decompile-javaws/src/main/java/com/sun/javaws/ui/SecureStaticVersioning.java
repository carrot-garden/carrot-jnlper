package com.sun.javaws.ui;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.deploy.util.SecurityBaseline;
import com.sun.deploy.util.VersionID;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.LaunchDesc;
import java.awt.Component;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class SecureStaticVersioning
{
  private static final String SSV2VERSION_KEY = "ssv.version.allowed";
  private static final String SSV2LATEST_VALUE = "ssv.latest.allowed";
  private static HashMap session = new HashMap();

  public static boolean promptDownload(Object paramObject, LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, String paramString1, String paramString2)
  {
    String str1 = ResourceManager.getString("javaws.ssv.title");
    String str2 = ResourceManager.getString("javaws.ssv.download.masthead");
    String str3 = ResourceManager.getString("javaws.ssv.download.bullet", paramString1, paramString2);
    String str4 = ResourceManager.getString("javaws.ssv.download.button");
    String str5 = ResourceManager.getString("common.cancel_btn");
    ToolkitStore.getUI();
    int i = ToolkitStore.getUI().showMessageDialog(paramObject, paramLaunchDesc.getAppInfo(), 3, str1, null, str2, str3, str4, str5, null);
    ToolkitStore.getUI();
    if (i == 0)
    {
      if (paramLocalApplicationProperties != null)
      {
        paramLocalApplicationProperties.put("ssv.version.allowed", paramString1);
        try
        {
          paramLocalApplicationProperties.store();
        }
        catch (IOException localIOException)
        {
          Trace.ignoredException(localIOException);
        }
      }
      return true;
    }
    return false;
  }

  public static boolean promptUse(Component paramComponent, AppInfo paramAppInfo, LocalApplicationProperties paramLocalApplicationProperties, String paramString)
    throws ExitException
  {
    if (Config.getStringProperty("deployment.insecure.jres").equals("NEVER"))
      return false;
    if ((paramLocalApplicationProperties != null) && ("ssv.latest.allowed".equals(paramLocalApplicationProperties.get("ssv.version.allowed"))))
      return false;
    if (showSSV2Dialog(paramComponent, paramAppInfo, paramString))
    {
      if (paramLocalApplicationProperties != null)
      {
        paramLocalApplicationProperties.put("ssv.version.allowed", paramString);
        try
        {
          paramLocalApplicationProperties.store();
        }
        catch (IOException localIOException1)
        {
          Trace.ignoredException(localIOException1);
        }
      }
      return true;
    }
    if (paramLocalApplicationProperties != null)
    {
      paramLocalApplicationProperties.put("ssv.version.allowed", "ssv.latest.allowed");
      try
      {
        paramLocalApplicationProperties.store();
      }
      catch (IOException localIOException2)
      {
        Trace.ignoredException(localIOException2);
      }
    }
    return false;
  }

  public static boolean promptRequired(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean, String paramString)
  {
    if ((paramLaunchDesc.getSecurityModel() != 0) || (!isOlderVersion(paramString)) || (Config.getStringProperty("deployment.insecure.jres").equals("ALWAYS")) || (SecurityBaseline.satisfiesSecurityBaseline(paramString)))
      return false;
    if (paramBoolean)
      return true;
    return (paramLocalApplicationProperties == null) || (!paramString.equals(paramLocalApplicationProperties.get("ssv.version.allowed")));
  }

  public static boolean canAutoDownload(LaunchDesc paramLaunchDesc, JREDesc paramJREDesc, boolean paramBoolean)
    throws ExitException
  {
    LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(paramLaunchDesc.getCanonicalHome());
    String str1 = Config.getStringProperty("deployment.javaws.autodownload");
    if ((paramBoolean) || ((str1 != null) && (str1.equalsIgnoreCase("NEVER"))))
    {
      showCannotDownloadDialog(null, paramLaunchDesc, localLocalApplicationProperties, paramJREDesc.getVersion());
      return false;
    }
    if ((paramLaunchDesc.getSecurityModel() == 0) && ("NEVER".equals(Config.getStringProperty("deployment.insecure.jres"))))
      return false;
    String str2 = paramJREDesc.getSource();
    URL localURL = paramJREDesc.getHref();
    boolean bool = localURL == null;
    if (bool)
    {
      str3 = Config.getStringProperty("deployment.javaws.installURL");
      try
      {
        localURL = new URL(str3);
      }
      catch (MalformedURLException localMalformedURLException)
      {
        throw new ExitException(localMalformedURLException, 3);
      }
    }
    String str3 = DownloadEngine.getAvailableVersion(localURL, paramJREDesc.getVersion(), bool, JREInfo.getKnownPlatforms());
    if ((str3 != null) && (paramLaunchDesc.getSecurityModel() == 0) && (!SecurityBaseline.satisfiesSecurityBaseline(str3)))
      str3 = null;
    if (str3 == null)
    {
      showCannotDownloadDialog(null, paramLaunchDesc, localLocalApplicationProperties, paramJREDesc.getVersion());
      return false;
    }
    if (promptRequired(paramLaunchDesc, localLocalApplicationProperties, true, str3))
    {
      if (!promptDownload(null, paramLaunchDesc, localLocalApplicationProperties, str3, str2))
        return false;
    }
    else if ((str1 != null) && (str1.equalsIgnoreCase("PROMPT")) && (!AutoDownloadPrompt.prompt(null, paramLaunchDesc)))
    {
      showCannotDownloadDialog(null, paramLaunchDesc, localLocalApplicationProperties, paramJREDesc.getVersion());
      return false;
    }
    return true;
  }

  public static boolean canUse(LaunchDesc paramLaunchDesc, String paramString)
    throws ExitException
  {
    LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(paramLaunchDesc.getCanonicalHome());
    if (promptRequired(paramLaunchDesc, localLocalApplicationProperties, false, paramString))
      return promptUse(null, paramLaunchDesc.getAppInfo(), localLocalApplicationProperties, paramString);
    return true;
  }

  public static boolean canUse(AppInfo paramAppInfo, String paramString)
    throws ExitException
  {
    if ((!isOlderVersion(paramString)) || (Config.getStringProperty("deployment.insecure.jres").equals("ALWAYS")) || (SecurityBaseline.satisfiesSecurityBaseline(paramString)))
      return true;
    String str1 = paramAppInfo.getFrom().toString() + "/" + paramAppInfo.getTitle();
    synchronized (session)
    {
      String str2 = (String)session.get(str1);
      if ((paramString != null) && (paramString.equals(str2)))
        return true;
      if ("ssv.latest.allowed".equals(str2))
        return false;
    }
    boolean bool = promptUse(null, paramAppInfo, null, paramString);
    synchronized (session)
    {
      session.put(str1, bool ? paramString : "ssv.latest.allowed");
    }
    return bool;
  }

  public static void resetAcceptedVersion(URL paramURL)
  {
    LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(paramURL);
    if (localLocalApplicationProperties != null)
    {
      localLocalApplicationProperties.put("ssv.version.allowed", null);
      try
      {
        localLocalApplicationProperties.store();
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
    synchronized (session)
    {
      if (paramURL != null)
        session.remove(paramURL.toString());
    }
  }

  private static boolean isOlderVersion(String paramString)
  {
    VersionID localVersionID1 = new VersionID(SecurityBaseline.getCurrentVersion() + "+");
    VersionID localVersionID2 = new VersionID(paramString);
    return !localVersionID1.match(localVersionID2);
  }

  private static boolean showSSV2Dialog(Component paramComponent, AppInfo paramAppInfo, String paramString)
    throws ExitException
  {
    String str1 = ResourceManager.getString("deployment.ssv2.title");
    String str2 = ResourceManager.getString("deployment.ssv2.masthead");
    String str3 = ResourceManager.getString("deployment.ssv2.risk");
    String str4 = ResourceManager.getString("deployment.ssv2.moreText");
    URL localURL = null;
    try
    {
      localURL = new URL("http://java.com/access_old_java");
    }
    catch (Exception localException)
    {
    }
    String str5 = ResourceManager.getString("deployment.ssv2.choice");
    String str6 = ResourceManager.getString("deployment.ssv2.choice1");
    String str7 = ResourceManager.getString("deployment.ssv2.choice2", paramString);
    String str8 = ResourceManager.getString("deployment.ssv2.run.button");
    String str9 = ResourceManager.getString("common.cancel_btn");
    int i = ToolkitStore.getUI().showSSVDialog(paramComponent, paramAppInfo, str1, str2, str3, str4, localURL, str5, str6, str7, str8, str9);
    ToolkitStore.getUI();
    if (i == 0)
      return true;
    ToolkitStore.getUI();
    if (i == 2)
      return false;
    throw new ExitException(null, 0);
  }

  private static void showCannotDownloadDialog(Component paramComponent, LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, String paramString)
    throws ExitException
  {
    if ((paramLocalApplicationProperties != null) && ("ssv.latest.allowed".equals(paramLocalApplicationProperties.get("ssv.version.allowed"))))
      return;
    String str1 = ResourceManager.getString("deployment.ssv2.nodl.title");
    String str2 = ResourceManager.getString("deployment.ssv2.nodl.masthead", paramString);
    String str3 = ResourceManager.getString("deployment.ssv2.nodl.button");
    String str4 = ResourceManager.getString("common.cancel_btn");
    String str5 = ResourceManager.getString("deployment.ssv2.moreText");
    URL localURL = null;
    try
    {
      localURL = new URL("http://java.com/access_old_java");
    }
    catch (Exception localException)
    {
    }
    ToolkitStore.getUI();
    int i = ToolkitStore.getUI().showMessageDialog(paramComponent, paramLaunchDesc.getAppInfo(), 2, str1, str2, null, null, str3, str4, null, localURL, str5);
    ToolkitStore.getUI();
    if (i != 0)
      throw new ExitException(null, 0);
    if (paramLocalApplicationProperties != null)
    {
      paramLocalApplicationProperties.put("ssv.version.allowed", "ssv.latest.allowed");
      try
      {
        paramLocalApplicationProperties.store();
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.SecureStaticVersioning
 * JD-Core Version:    0.6.0
 */