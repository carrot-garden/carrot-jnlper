package com.sun.javaws;

import com.sun.deploy.Environment;
import com.sun.deploy.trace.Trace;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

public class Globals
{
  private static final String JAVAWS_NAME = "javaws-10.0.0.20";
  public static final String JAVAWS_VERSION = "10.0.0.20";
  static final String JNLP_VERSION = "6.0_18";
  private static final String WIN_ID = "Windows";
  private static boolean _isSilentMode = false;
  private static boolean _isQuietMode = false;
  private static boolean _isSecureMode = false;
  private static boolean _isReverseMode = false;
  private static String[] _applicationArgs = null;
  private static boolean _createShortcut = false;
  private static boolean _createAssoc = false;
  private static boolean _showPrompts = false;
  private static boolean _isIconImageUpdated = false;
  private static final String DEFAULT_LOGHOST = "localhost:8205";
  public static String BootClassPath = "NONE";
  public static String JCOV = "NONE";
  public static boolean TraceDefault = true;
  public static boolean TraceBasic = false;
  public static boolean TraceNetwork = false;
  public static boolean TraceSecurity = false;
  public static boolean TraceCache = false;
  public static boolean TraceExtensions = false;
  public static boolean TraceTemp = false;
  public static String LogToHost = null;
  public static boolean SupportJREinstallation = true;
  public static boolean OverrideSystemClassLoader = true;
  public static boolean TCKHarnessRun = false;
  public static boolean TCKResponse = false;
  public static final String JAVA_STARTED = "Java Started";
  public static final String JNLP_LAUNCHING = "JNLP Launching";
  public static final String NEW_VM_STARTING = "JVM Starting";
  public static final String JAVA_SHUTDOWN = "JVM Shutdown";
  public static final String CACHE_CLEAR_OK = "Cache Clear Success";
  public static final String CACHE_CLEAR_FAILED = "Cache Clear Failed";
  private static final Locale defaultLocale = Locale.getDefault();
  private static final String defaultLocaleString = getDefaultLocale().toString();

  public static String getDefaultLocaleString()
  {
    return defaultLocaleString;
  }

  public static Locale getDefaultLocale()
  {
    return defaultLocale;
  }

  public static boolean isShortcutMode()
  {
    return _createShortcut;
  }

  public static boolean createShortcut()
  {
    return _createShortcut;
  }

  public static boolean createAssoc()
  {
    return _createAssoc;
  }

  public static boolean showPrompts()
  {
    return _showPrompts;
  }

  public static boolean isSilentMode()
  {
    return (_isQuietMode) || ((_isSilentMode) && ((Environment.isImportMode()) || (Environment.isInstallMode())));
  }

  public static boolean isQuietMode()
  {
    return _isQuietMode;
  }

  public static boolean isReverseMode()
  {
    return (_isReverseMode) && (Environment.isImportMode());
  }

  public static boolean isIconImageUpdated()
  {
    return _isIconImageUpdated;
  }

  public static void setIconImageUpdated(boolean paramBoolean)
  {
    _isIconImageUpdated = paramBoolean;
  }

  public static boolean isSecureMode()
  {
    return _isSecureMode;
  }

  public static String[] getApplicationArgs()
  {
    return _applicationArgs;
  }

  public static void setCreateShortcut(boolean paramBoolean)
  {
    _createShortcut = paramBoolean;
  }

  public static void setCreateAssoc(boolean paramBoolean)
  {
    _createAssoc = paramBoolean;
  }

  public static void setShowPrompts(boolean paramBoolean)
  {
    _showPrompts = paramBoolean;
  }

  public static void setSilentMode(boolean paramBoolean)
  {
    _isSilentMode = paramBoolean;
  }

  public static void setQuietMode(boolean paramBoolean)
  {
    _isQuietMode = paramBoolean;
  }

  public static void setReverseMode(boolean paramBoolean)
  {
    _isReverseMode = paramBoolean;
  }

  public static void setSecureMode(boolean paramBoolean)
  {
    _isSecureMode = paramBoolean;
  }

  public static void setApplicationArgs(String[] paramArrayOfString)
  {
    _applicationArgs = paramArrayOfString;
  }

  public static String getBuildID()
  {
    String str = null;
    InputStream localInputStream = Globals.class.getResourceAsStream("/build.id");
    if (localInputStream != null)
    {
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream));
      try
      {
        str = localBufferedReader.readLine();
      }
      catch (IOException localIOException)
      {
      }
    }
    return (str == null) || (str.length() == 0) ? "<internal>" : str;
  }

  public static String getComponentName()
  {
    return "javaws-10.0.0.20";
  }

  public static String getUserAgent()
  {
    return "JNLP/6.0_18 javaws/10.0.0.20 (" + getBuildID() + ")" + " Java/" + System.getProperty("java.version");
  }

  public static String[] parseOptions(String[] paramArrayOfString)
  {
    readOptionFile();
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    int j = 0;
    while (i < paramArrayOfString.length)
    {
      localObject = paramArrayOfString[(i++)];
      if ((((String)localObject).startsWith("-XX:")) && (j == 0))
        parseOption(((String)localObject).substring(4), false);
      else
        localArrayList.add(localObject);
      if (!((String)localObject).startsWith("-"))
        j = 1;
    }
    setTCKOptions();
    Object localObject = new String[localArrayList.size()];
    return (String)(String[])(String[])localArrayList.toArray(localObject);
  }

  public static void getDebugOptionsFromProperties(Properties paramProperties)
  {
    for (int i = 0; ; i++)
    {
      String str = paramProperties.getProperty("javaws.debug." + i);
      if (str == null)
        return;
      parseOption(str, true);
    }
  }

  private static void setTCKOptions()
  {
    if ((TCKHarnessRun == true) && (LogToHost == null))
      Trace.println("Warning: LogHost = null");
  }

  private static void parseOption(String paramString, boolean paramBoolean)
  {
    String str1 = null;
    String str2 = null;
    int i = paramString.indexOf('=');
    if (i == -1)
    {
      str1 = paramString;
      str2 = null;
    }
    else
    {
      str1 = paramString.substring(0, i);
      str2 = paramString.substring(i + 1);
    }
    if ((str1.length() > 0) && ((str1.startsWith("-")) || (str1.startsWith("+"))))
    {
      str1 = str1.substring(1);
      str2 = paramString.startsWith("+") ? "true" : "false";
    }
    if ((paramBoolean) && (!str1.startsWith("x")) && (!str1.startsWith("Trace")))
      str1 = null;
    if (str1 != null)
      setOption(str1, str2);
  }

  private static boolean setOption(String paramString1, String paramString2)
  {
    Class localClass1 = new String().getClass();
    int i = 1;
    try
    {
      Field localField = new Globals().getClass().getDeclaredField(paramString1);
      if ((localField.getModifiers() & 0x8) == 0)
        return false;
      Class localClass2 = localField.getType();
      if (localClass2 == localClass1)
        localField.set(null, paramString2);
      else if (localClass2 == Boolean.TYPE)
        localField.setBoolean(null, Boolean.valueOf(paramString2).booleanValue());
      else if (localClass2 == Integer.TYPE)
        localField.setInt(null, Integer.parseInt(paramString2));
      else if (localClass2 == Float.TYPE)
        localField.setFloat(null, Float.parseFloat(paramString2));
      else if (localClass2 == Double.TYPE)
        localField.setDouble(null, Double.parseDouble(paramString2));
      else if (localClass2 == Long.TYPE)
        localField.setLong(null, Long.parseLong(paramString2));
      else
        return false;
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      return false;
    }
    catch (NoSuchFieldException localNoSuchFieldException)
    {
      return false;
    }
    return i;
  }

  private static void readOptionFile()
  {
    FileInputStream localFileInputStream = null;
    try
    {
      localFileInputStream = new FileInputStream(System.getProperty("user.home") + File.separator + ".javawsrc");
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      return;
    }
    try
    {
      Properties localProperties = new Properties();
      localProperties.load(localFileInputStream);
      Enumeration localEnumeration = localProperties.propertyNames();
      while (localEnumeration.hasMoreElements())
      {
        String str1 = (String)localEnumeration.nextElement();
        String str2 = localProperties.getProperty(str1);
        parseOption(str1 + "=" + str2, false);
      }
    }
    catch (IOException localIOException)
    {
    }
  }

  public static String getJavawsVersion()
  {
    int i = "10.0.0.20".indexOf("-");
    if (i > 0)
      return "10.0.0.20".substring(0, i);
    return "10.0.0.20";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.Globals
 * JD-Core Version:    0.6.0
 */