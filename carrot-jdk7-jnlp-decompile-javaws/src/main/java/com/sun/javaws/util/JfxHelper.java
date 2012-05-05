package com.sun.javaws.util;

import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.Environment;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.config.JfxRuntime;
import com.sun.deploy.config.Platform;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.VersionID;
import com.sun.deploy.util.VersionString;
import com.sun.javaws.JnlpxArgs;
import com.sun.javaws.exceptions.CanceledDownloadException;
import com.sun.javaws.exceptions.LaunchDescException;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.JavaFXRuntimeDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JfxHelper
{
  public static final VersionID JRE_MINIMUM_VER = new VersionID("1.6.0_10");
  public static final String[] SUPPORTTED_OS = { "Windows", "Linux", "Mac OS X" };
  private static VersionID currentVersion = null;
  private static boolean detectionCompleted = false;

  public static boolean isJfxSupportSatisfied(ClassLoader paramClassLoader, LaunchDesc paramLaunchDesc)
  {
    if (Environment.getenv("FORCED_FX_ROOT") != null)
      return true;
    JavaFXRuntimeDesc localJavaFXRuntimeDesc = paramLaunchDesc.getJavaFXRuntimeDescriptor();
    if ((null == localJavaFXRuntimeDesc) && (!paramLaunchDesc.isFXApp()))
      return true;
    String str = null == localJavaFXRuntimeDesc ? "2.0+" : localJavaFXRuntimeDesc.getVersion();
    return isJfxSupportSatisfied(paramClassLoader, str);
  }

  public static boolean isJfxSupportSatisfied(ClassLoader paramClassLoader, String paramString)
  {
    if (Environment.getenv("FORCED_FX_ROOT") != null)
      return true;
    if ((null == paramString) || (paramString.length() == 0))
      paramString = "2.0+";
    VersionString localVersionString = new VersionString(paramString);
    VersionID localVersionID = getCurrentJfxVersion(paramClassLoader);
    return null == localVersionID ? false : localVersionString.contains(localVersionID);
  }

  public static JfxRuntime getBestJfxInstalled(LaunchDesc paramLaunchDesc)
  {
    JavaFXRuntimeDesc localJavaFXRuntimeDesc = paramLaunchDesc.getJavaFXRuntimeDescriptor();
    String str = null == localJavaFXRuntimeDesc ? "2.0+" : localJavaFXRuntimeDesc.getVersion();
    VersionString localVersionString = new VersionString(str);
    List localList = localVersionString.getAllVersionIDs();
    for (int i = 0; i < localList.size(); i++)
    {
      VersionID localVersionID = (VersionID)localList.get(i);
      JfxRuntime localJfxRuntime = Platform.get().getBestJfxRuntime(localVersionID);
      if (localJfxRuntime != null)
        return localJfxRuntime;
    }
    return null;
  }

  private static boolean isAllowedToInstall(URL paramURL, String paramString, Preloader paramPreloader)
  {
    return true;
  }

  public static JfxRuntime installJfxRuntime(LaunchDesc paramLaunchDesc, Preloader paramPreloader)
    throws Throwable
  {
    JavaFXRuntimeDesc localJavaFXRuntimeDesc = paramLaunchDesc.getJavaFXRuntimeDescriptor();
    if (null == localJavaFXRuntimeDesc)
      throw new IllegalArgumentException("Missing <javafx-runtime> element.");
    URL localURL = localJavaFXRuntimeDesc.getDownloadURL();
    if (localURL == null)
      throw new IllegalArgumentException("Missing \"href\" attribute in javafx-runtime element.");
    return installJfxRuntime(localURL, localJavaFXRuntimeDesc.getVersion(), paramPreloader);
  }

  public static JfxRuntime installJfxRuntime(URL paramURL, String paramString, Preloader paramPreloader)
    throws Throwable
  {
    Trace.println("Install JavaFX Runtime from " + paramURL, TraceLevel.EXTENSIONS);
    if (!isAllowedToInstall(paramURL, paramString, paramPreloader))
      throw new CanceledDownloadException(paramURL, paramString);
    File localFile = DownloadEngine.getUpdatedFile(paramURL, null);
    Trace.println("JavaFX installer JNLP downloaded at " + localFile.getAbsolutePath(), TraceLevel.EXTENSIONS);
    LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile);
    if (!localLaunchDesc.isInstaller())
      throw new IllegalArgumentException(paramURL + " is not pointing to a valid JNLP installer.");
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(localFile);
    Trace.println("JavaFX installer verified, launching ...", TraceLevel.EXTENSIONS);
    JnlpxArgs.executeInstallers(localArrayList, paramPreloader);
    Platform.get().getInstalledJfxRuntimes(true);
    VersionID localVersionID = new VersionID((null == paramString) || (paramString.length() == 0) ? "2.0+" : paramString);
    JfxRuntime localJfxRuntime = Platform.get().getBestJfxRuntime(localVersionID);
    if (null == localJfxRuntime)
      throw new IllegalStateException("Installed JavaFX runtime cannot be located.");
    return localJfxRuntime;
  }

  public static VersionID getCurrentJfxVersion(ClassLoader paramClassLoader)
  {
    if (!detectionCompleted)
      synchronized (JfxHelper.class)
      {
        if (!detectionCompleted)
        {
          try
          {
            if (null == paramClassLoader)
              paramClassLoader = Thread.currentThread().getContextClassLoader();
            Class localClass = paramClassLoader.loadClass("com.sun.javafx.runtime.VersionInfo");
            Method localMethod = localClass.getMethod("getVersion", null);
            String str2 = (String)localMethod.invoke(null, null);
            currentVersion = new VersionID(str2);
          }
          catch (Exception localException)
          {
            currentVersion = null;
          }
          if (null == currentVersion)
          {
            String str1 = System.getProperty("jnlp.fx");
            if (str1 != null)
            {
              str1 = str1.trim();
              currentVersion = new VersionID(str1);
            }
          }
          detectionCompleted = true;
        }
      }
    return currentVersion;
  }

  static void reset()
  {
    detectionCompleted = false;
  }

  public static boolean isSupportedJreVersion(VersionID paramVersionID)
  {
    return !JRE_MINIMUM_VER.isGreaterThan(paramVersionID);
  }

  public static boolean isSupportedOS(String paramString)
  {
    for (int i = 0; i < SUPPORTTED_OS.length; i++)
      if (SUPPORTTED_OS[i].equals(paramString))
        return true;
    return false;
  }

  public static void validateJfxRequest(LaunchDesc paramLaunchDesc, JREInfo paramJREInfo)
    throws LaunchDescException
  {
    if (!isSupportedOS(paramJREInfo.getOSName()))
    {
      localObject = ResourceManager.getString("launch.error.jfx.os", paramJREInfo.getOSName());
      throw new LaunchDescException(paramLaunchDesc, (String)localObject, null);
    }
    Object localObject = paramJREInfo.getProductVersion();
    if (!isSupportedJreVersion((VersionID)localObject))
    {
      String str = ResourceManager.getString("launch.error.jfx.jre", JRE_MINIMUM_VER.toString(), ((VersionID)localObject).toString());
      throw new LaunchDescException(paramLaunchDesc, str, null);
    }
  }

  public static void validateJfxRequest(LaunchDesc paramLaunchDesc, JREDesc paramJREDesc)
    throws LaunchDescException
  {
    VersionID localVersionID = new VersionID(paramJREDesc.getVersion());
    if (!isSupportedJreVersion(localVersionID))
    {
      String str = ResourceManager.getString("launch.error.jfx.jre", JRE_MINIMUM_VER.toString(), localVersionID.toString());
      throw new LaunchDescException(paramLaunchDesc, str, null);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.util.JfxHelper
 * JD-Core Version:    0.6.0
 */