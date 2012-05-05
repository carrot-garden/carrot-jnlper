package com.sun.javaws;

import com.sun.applet2.preloader.CancelException;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.ConfigEvent;
import com.sun.applet2.preloader.event.InitEvent;
import com.sun.deploy.Environment;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.config.JfxRuntime;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.JVMParameters;
import com.sun.deploy.util.Property;
import com.sun.deploy.util.StringQuoteUtil;
import com.sun.deploy.util.VersionID;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.exceptions.LaunchDescException;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.MatchJREIf;
import com.sun.javaws.util.GeneralUtil;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class JnlpxArgs
{
  public static final String ARG_JVM = "jnlpx.jvm";
  public static final String ARG_SPLASHPORT = "jnlpx.splashport";
  public static final String ARG_REMOVE = "jnlpx.remove";
  public static final String ARG_OFFLINE = "jnlpx.offline";
  public static final String ARG_HEAPSIZE = "jnlpx.heapsize";
  public static final String ARG_VMARGS = "jnlpx.vmargs";
  public static final String ARG_HOME = "jnlpx.home";
  public static final String ARG_RELAUNCH = "jnlpx.relaunch";
  private static File _currentJVMCommand = null;
  private static final String JAVAWS_JAR = "javaws.jar";
  private static final String DEPLOY_JAR = "deploy.jar";
  private static final String PLUGIN_JAR = "plugin.jar";
  private static final Vector fileReadWriteList = new Vector();
  public static boolean _verbose = false;

  public static int getSplashPort()
  {
    try
    {
      return Integer.parseInt(System.getProperty("jnlpx.splashport", "-1"));
    }
    catch (NumberFormatException localNumberFormatException)
    {
    }
    return -1;
  }

  public static String getVMArgs()
  {
    return StringQuoteUtil.unquoteIfNeeded(System.getProperty("jnlpx.vmargs"));
  }

  public static boolean getIsRelaunch()
  {
    return getBooleanProperty("jnlpx.relaunch");
  }

  public static File getJVMCommand()
  {
    if (_currentJVMCommand == null)
    {
      String str = System.getProperty("jnlpx.jvm", "").trim();
      if (str.startsWith("X"))
        str = JREInfo.getDefaultJavaPath();
      if (str.startsWith("\""))
        str = str.substring(1);
      if (str.endsWith("\""))
        str = str.substring(0, str.length() - 1);
      _currentJVMCommand = new File(str);
    }
    return _currentJVMCommand;
  }

  public static boolean shouldRemoveArgumentFile()
  {
    return getBooleanProperty("jnlpx.remove");
  }

  public static void setShouldRemoveArgumentFile(String paramString)
  {
    System.setProperty("jnlpx.remove", paramString);
  }

  public static boolean isOffline()
  {
    return getBooleanProperty("jnlpx.offline");
  }

  public static void SetIsOffline()
  {
    System.setProperty("jnlpx.offline", "true");
  }

  public static String getHeapSize()
  {
    return System.getProperty("jnlpx.heapsize");
  }

  public static void setVerbose(boolean paramBoolean)
  {
    _verbose = paramBoolean;
  }

  public static long getInitialHeapSize()
  {
    String str1 = getHeapSize();
    if (str1 == null)
      return -1L;
    String str2 = str1.substring(str1.lastIndexOf('=') + 1);
    String str3 = str2.substring(0, str2.lastIndexOf(','));
    return GeneralUtil.heapValToLong(str3);
  }

  public static long getMaxHeapSize()
  {
    String str1 = getHeapSize();
    if (str1 == null)
      return -1L;
    String str2 = str1.substring(str1.lastIndexOf('=') + 1);
    String str3 = str2.substring(str2.lastIndexOf(',') + 1, str2.length());
    return GeneralUtil.heapValToLong(str3);
  }

  private static boolean heapSizesValid(long paramLong1, long paramLong2)
  {
    return (paramLong1 != -1L) || (paramLong2 != -1L);
  }

  static List getArgumentList(String paramString, long paramLong1, long paramLong2, JVMParameters paramJVMParameters, boolean paramBoolean1, JfxRuntime paramJfxRuntime, boolean paramBoolean2, int paramInt)
  {
    String str1 = "-Djnlpx.heapsize=NULL,NULL";
    if (heapSizesValid(paramLong1, paramLong2))
      str1 = "-Djnlpx.heapsize=" + paramLong1 + "," + paramLong2;
    String str2 = getVMArgs();
    Object localObject1 = new ArrayList();
    if (str2 != null)
    {
      localObject1 = StringQuoteUtil.parseCommandLine(str2);
      paramJVMParameters = paramJVMParameters.copy();
      paramJVMParameters.parse(str2);
    }
    String str4 = Environment.getForcedBootClassPath();
    String str3;
    if (str4 == null)
      str3 = "-Xbootclasspath/a:" + Environment.getJavaHome() + File.separator + "lib" + File.separator + "javaws.jar" + File.pathSeparator + Environment.getJavaHome() + File.separator + "lib" + File.separator + "deploy.jar" + File.pathSeparator + Environment.getJavaHome() + File.separator + "lib" + File.separator + "plugin.jar";
    else
      str3 = "-Xbootclasspath/p:" + str4;
    String str5 = "";
    String str6 = "";
    if (paramJfxRuntime != null)
    {
      str5 = "-Djnlp.fx=" + paramJfxRuntime.getProductVersion().toString();
      if (!paramBoolean2)
        str6 = "-Djnlp.tk=awt";
    }
    String[] arrayOfString = { str3, str5, str6, "-classpath", Environment.getJavaHome() + File.separator + "lib" + File.separator + "deploy.jar", null, "-Djnlpx.jvm=" + paramString, "-Djnlpx.splashport=" + getSplashPort(), "-Djnlpx.home=" + Environment.getJavaHome() + File.separator + "bin", "-Djnlpx.remove=" + (shouldRemoveArgumentFile() ? "true" : "false"), "-Djnlpx.offline=" + (isOffline() ? "true" : "false"), "-Djnlpx.relaunch=true", str1, "-Djava.security.policy=" + getPolicyURLString(), "-DtrustProxy=true", "-Xverify:remote", useJCOV(), useBootClassPath(), useJpiProfile(), useDebugMode(), useDebugVMMode(), paramBoolean2 ? "" : "-Dsun.awt.warmup=true", "com.sun.javaws.Main", paramBoolean1 ? "-secure" : "", _verbose ? "-verbose" : "", setTCKHarnessOption(), useLogToHost() };
    for (int i = 0; i < arrayOfString.length; i++)
    {
      if (arrayOfString[i] == null)
        continue;
      paramInt -= arrayOfString[i].length() + 1;
    }
    List localList = paramJVMParameters.getCommandLineArguments(false, false, true, paramBoolean1, paramInt / 2 - 20);
    if (Environment.isJavaWebStart())
      for (int j = 0; j < ((List)localObject1).size(); j++)
      {
        localObject2 = (String)((List)localObject1).get(j);
        if (localList.contains(localObject2))
          continue;
        localList.add(localObject2);
      }
    String str7 = StringQuoteUtil.getStringByCommandList(localList);
    paramInt -= str7.length();
    Object localObject2 = new Property("jnlpx.vmargs", str7);
    String str8 = ((Property)localObject2).toString(true);
    if (paramInt < str8.length())
    {
      str8 = null;
      Trace.println("JnlpxArgs.getArgumentList: Internal Error:  remaining custArgsMaxLen: " + paramInt + " < vmArgsPropertyStr.length: " + str8.length() + " dropping vmArgsPropertyStr");
    }
    for (int k = 0; k < arrayOfString.length; k++)
      if (arrayOfString[k] == null)
      {
        if (str8 == null)
          continue;
        localList.add(str8);
        str8 = null;
      }
      else
      {
        if (arrayOfString[k].length() <= 0)
          continue;
        localList.add(arrayOfString[k]);
      }
    return (List)(List)localList;
  }

  static String getPolicyURLString()
  {
    String str1 = Environment.getJavaHome() + File.separator + "lib" + File.separator + "security" + File.separator + "javaws.policy";
    String str2 = str1;
    try
    {
      URL localURL = new URL("file", "", str1);
      str2 = localURL.toString();
    }
    catch (Exception localException)
    {
    }
    return str2;
  }

  public static String useLogToHost()
  {
    if (Globals.LogToHost != null)
      return "-XX:LogToHost=" + Globals.LogToHost;
    return "";
  }

  public static String setTCKHarnessOption()
  {
    if (Globals.TCKHarnessRun == true)
      return "-XX:TCKHarnessRun=true";
    return "";
  }

  public static String useBootClassPath()
  {
    if (Globals.BootClassPath.equals("NONE"))
      return "";
    return "-Xbootclasspath" + Globals.BootClassPath;
  }

  public static String useJpiProfile()
  {
    String str = System.getProperty("javaplugin.user.profile");
    if (str != null)
      return "-Djavaplugin.user.profile=" + str;
    return "";
  }

  public static String useJCOV()
  {
    if (Globals.JCOV.equals("NONE"))
      return "";
    return "-Xrunjcov:file=" + Globals.JCOV;
  }

  public static String useDebugMode()
  {
    if (Config.isDebugMode())
      return "-Ddeploy.debugMode=true";
    return "";
  }

  public static String useDebugVMMode()
  {
    if (Config.isDebugVMMode())
      return "-Ddeploy.useDebugJavaVM=true";
    return "";
  }

  public static void removeArgumentFile(String paramString)
  {
    if ((shouldRemoveArgumentFile()) && (paramString != null))
      new File(paramString).delete();
  }

  public static void verify()
  {
    if (Trace.isEnabled(TraceLevel.BASIC))
    {
      Trace.println("Java part started", TraceLevel.BASIC);
      Trace.println("jnlpx.jvm: " + getJVMCommand(), TraceLevel.BASIC);
      Trace.println("jnlpx.splashport: " + getSplashPort(), TraceLevel.BASIC);
      Trace.println("jnlpx.remove: " + shouldRemoveArgumentFile(), TraceLevel.BASIC);
      Trace.println("jnlpx.heapsize: " + getHeapSize(), TraceLevel.BASIC);
    }
  }

  private static boolean getBooleanProperty(String paramString)
  {
    String str = System.getProperty(paramString, "false");
    return (str != null) && (str.equals("true"));
  }

  public static Vector getFileReadWriteList()
  {
    return fileReadWriteList;
  }

  protected static Process execProgram(JREInfo paramJREInfo, String[] paramArrayOfString, long paramLong1, long paramLong2, JVMParameters paramJVMParameters, boolean paramBoolean)
    throws IOException
  {
    return execProgram(paramJREInfo, paramArrayOfString, paramLong1, paramLong2, paramJVMParameters, paramBoolean, null, false);
  }

  protected static Process execProgram(JREInfo paramJREInfo, String[] paramArrayOfString, long paramLong1, long paramLong2, JVMParameters paramJVMParameters, boolean paramBoolean1, JfxRuntime paramJfxRuntime, boolean paramBoolean2)
    throws IOException
  {
    String str1 = null;
    String str2 = null;
    str2 = paramJREInfo.getPath();
    if ((Config.isDebugMode()) && (Config.isDebugVMMode()))
      str1 = paramJREInfo.getDebugJavaPath();
    else
      str1 = paramJREInfo.getPath();
    if ((str1.length() == 0) || (str2.length() == 0))
      throw new IllegalArgumentException("must exist");
    int i = Config.getMaxCommandLineLength();
    for (int j = 0; j < paramArrayOfString.length; j++)
    {
      if (paramArrayOfString[j] == null)
        continue;
      i -= paramArrayOfString[j].length() + 1;
    }
    i -= str1.length() + 1;
    List localList = getArgumentList(str2, paramLong1, paramLong2, paramJVMParameters, paramBoolean1, paramJfxRuntime, paramBoolean2, i);
    int k = 1 + localList.size() + paramArrayOfString.length;
    String[] arrayOfString = new String[k];
    int m = 0;
    arrayOfString[(m++)] = str1;
    for (int n = 0; n < localList.size(); n++)
      arrayOfString[(m++)] = ((String)localList.get(n));
    for (n = 0; n < paramArrayOfString.length; n++)
      arrayOfString[(m++)] = paramArrayOfString[n];
    if (Trace.isEnabled(TraceLevel.BASIC))
    {
      Trace.println("Launching new JRE version: " + paramJREInfo, TraceLevel.BASIC);
      Trace.println("\t jvmParams: " + paramJVMParameters, TraceLevel.BASIC);
      for (n = 0; n < arrayOfString.length; n++)
        Trace.println("cmd " + n + " : " + arrayOfString[n], TraceLevel.BASIC);
    }
    if (Globals.TCKHarnessRun)
      Main.tckprintln("JVM Starting");
    Trace.flush();
    return Runtime.getRuntime().exec(arrayOfString);
  }

  public static void executeInstallers(ArrayList paramArrayList, Preloader paramPreloader)
    throws ExitException
  {
    if (paramPreloader.getOwner() != null)
      try
      {
        paramPreloader.handleEvent(new InitEvent(2));
      }
      catch (CancelException localCancelException1)
      {
      }
    for (int i = 0; i < paramArrayList.size(); i++)
    {
      File localFile = (File)paramArrayList.get(i);
      try
      {
        LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile, null, null, null);
        LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(localFile.getPath());
        localLocalApplicationProperties.setExtensionInstalled(false);
        localLocalApplicationProperties.store();
        Trace.println("Installing extension: " + localFile, TraceLevel.EXTENSIONS);
        String[] arrayOfString = { "-installer", localFile.getAbsolutePath() };
        JREInfo localJREInfo = localLaunchDesc.selectJRE();
        if (localJREInfo == null)
        {
          paramPreloader.handleEvent(new ConfigEvent(6));
          LaunchDescException localLaunchDescException = new LaunchDescException(localLaunchDesc, ResourceManager.getString("launch.error.missingjreversion"), null);
          throw new ExitException(localLaunchDescException, 3);
        }
        boolean bool = shouldRemoveArgumentFile();
        setShouldRemoveArgumentFile("false");
        MatchJREIf localMatchJREIf = localLaunchDesc.getJREMatcher();
        JVMParameters localJVMParameters = localMatchJREIf.getSelectedJVMParameters();
        JREDesc localJREDesc = localMatchJREIf.getSelectedJREDesc();
        long l1 = localJREDesc.getMinHeap();
        long l2 = localJREDesc.getMaxHeap();
        Process localProcess = execProgram(localJREInfo, arrayOfString, l1, l2, localJVMParameters, false);
        paramPreloader.handleEvent(new ConfigEvent(6));
        EatInput.access$000(localProcess.getErrorStream());
        EatInput.access$000(localProcess.getInputStream());
        localProcess.waitFor();
        setShouldRemoveArgumentFile(String.valueOf(bool));
        localLocalApplicationProperties.refresh();
        if (localLocalApplicationProperties.isRebootNeeded())
        {
          int j = 0;
          ExtensionInstallHandler localExtensionInstallHandler = ExtensionInstallHandler.getInstance();
          if ((localExtensionInstallHandler != null) && (localExtensionInstallHandler.doPreRebootActions((Component)paramPreloader.getOwner())))
            j = 1;
          localLocalApplicationProperties.setExtensionInstalled(true);
          localLocalApplicationProperties.setRebootNeeded(false);
          localLocalApplicationProperties.store();
          if ((j != 0) && (localExtensionInstallHandler.doReboot()))
            throw new ExitException(null, 1);
        }
        if (!localLocalApplicationProperties.isExtensionInstalled())
        {
          paramPreloader.handleEvent(new ConfigEvent(6));
          throw new ExitException(new LaunchDescException(localLaunchDesc, ResourceManager.getString("Launch.error.installfailed"), null), 3);
        }
      }
      catch (JNLPException localJNLPException)
      {
        try
        {
          paramPreloader.handleEvent(new ConfigEvent(6));
        }
        catch (CancelException localCancelException2)
        {
        }
        throw new ExitException(localJNLPException, 3);
      }
      catch (IOException localIOException)
      {
        try
        {
          paramPreloader.handleEvent(new ConfigEvent(6));
        }
        catch (CancelException localCancelException3)
        {
        }
        throw new ExitException(localIOException, 3);
      }
      catch (InterruptedException localInterruptedException)
      {
        try
        {
          paramPreloader.handleEvent(new ConfigEvent(6));
        }
        catch (CancelException localCancelException4)
        {
        }
        throw new ExitException(localInterruptedException, 3);
      }
    }
  }

  public static void executeUninstallers(ArrayList paramArrayList)
    throws ExitException
  {
    for (int i = 0; i < paramArrayList.size(); i++)
    {
      File localFile = (File)paramArrayList.get(i);
      try
      {
        LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile, null, null, null);
        LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(localFile.getPath());
        Trace.println("uninstalling extension: " + localFile, TraceLevel.EXTENSIONS);
        if (!localLaunchDesc.isInstaller())
          throw new ExitException(null, 3);
        String[] arrayOfString = { "-silent", "-installer", localFile.getAbsolutePath() };
        JREInfo localJREInfo = localLaunchDesc.selectJRE();
        if (localJREInfo == null)
        {
          localObject = new LaunchDescException(localLaunchDesc, ResourceManager.getString("launch.error.missingjreversion"), null);
          throw new ExitException((Throwable)localObject, 3);
        }
        Object localObject = localLaunchDesc.getJREMatcher();
        JVMParameters localJVMParameters = ((MatchJREIf)localObject).getSelectedJVMParameters();
        JREDesc localJREDesc = ((MatchJREIf)localObject).getSelectedJREDesc();
        long l1 = localJREDesc.getMinHeap();
        long l2 = localJREDesc.getMaxHeap();
        Process localProcess = execProgram(localJREInfo, arrayOfString, l1, l2, localJVMParameters, false);
        EatInput.access$000(localProcess.getErrorStream());
        EatInput.access$000(localProcess.getInputStream());
        localProcess.waitFor();
        localLocalApplicationProperties.refresh();
        if (localLocalApplicationProperties.isRebootNeeded())
        {
          int j = 0;
          ExtensionInstallHandler localExtensionInstallHandler = ExtensionInstallHandler.getInstance();
          if ((localExtensionInstallHandler != null) && (localExtensionInstallHandler.doPreRebootActions(null)))
            j = 1;
          localLocalApplicationProperties.setRebootNeeded(false);
          localLocalApplicationProperties.setExtensionInstalled(false);
          localLocalApplicationProperties.store();
          if ((j != 0) && (localExtensionInstallHandler.doReboot()))
            throw new ExitException(null, 1);
        }
      }
      catch (JNLPException localJNLPException)
      {
        throw new ExitException(localJNLPException, 3);
      }
      catch (IOException localIOException)
      {
        throw new ExitException(localIOException, 3);
      }
      catch (InterruptedException localInterruptedException)
      {
        throw new ExitException(localInterruptedException, 3);
      }
    }
  }

  private static String sizeString(long paramLong)
  {
    if (paramLong > 1048576L)
      return "" + paramLong / 1048576L + "Mb";
    return "" + paramLong + "bytes";
  }

  private static class EatInput
    implements Runnable
  {
    private InputStream _is;

    EatInput(InputStream paramInputStream)
    {
      this._is = paramInputStream;
    }

    public void run()
    {
      byte[] arrayOfByte = new byte[1024];
      try
      {
        for (int i = 0; i != -1; i = this._is.read(arrayOfByte));
      }
      catch (IOException localIOException)
      {
      }
    }

    private static void eatInput(InputStream paramInputStream)
    {
      EatInput localEatInput = new EatInput(paramInputStream);
      new Thread(localEatInput).start();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.JnlpxArgs
 * JD-Core Version:    0.6.0
 */