package com.sun.deploy.config;

import com.sun.deploy.Environment;
import com.sun.deploy.net.proxy.NSPreferences;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.SearchPath;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import sun.security.action.GetPropertyAction;

public class UnixPlatform extends Platform
{
  private String _userHome;
  private String _systemHome;
  private String _osHome;
  private static final int ARGS_LIST_CAPACITY = 32;
  private static final int ARG_CAPACITY = 64;
  private static final char BACKSLASH = '\\';
  private static final char SINGLE_QUOTE = '\'';
  private static final char DOUBLE_QUOTE = '"';
  private static final char TAB = '\t';
  private static final char LINEFEED = '\n';
  private static final char RETURN = '\r';
  private static final char SPACE = ' ';

  public UnixPlatform()
  {
    loadDeployNativeLib();
  }

  public void loadDeployNativeLib()
  {
    try
    {
      String str = System.getProperty("os.arch");
      if (str.equals("x86"))
        str = "i386";
      System.load(Environment.getJavaHome() + File.separator + "lib" + File.separator + str + File.separator + "libdeploy.so");
    }
    catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
    {
    }
  }

  public String escapeBackslashAndQuoteString(String paramString)
  {
    return paramString;
  }

  public String getUserHome()
  {
    if (this._userHome == null)
    {
      String str = (String)AccessController.doPrivileged(new GetPropertyAction("javaplugin.user.profile"));
      if ((str == null) || (str.trim().equals("")))
        str = (String)AccessController.doPrivileged(new GetPropertyAction("user.home"));
      this._userHome = (str + (str.endsWith(File.separator) ? "" : File.separator) + ".java" + File.separator + "deployment");
    }
    return this._userHome;
  }

  public String getSystemHome()
  {
    if (this._systemHome == null)
      this._systemHome = (File.separator + "etc" + File.separator + ".java" + File.separator + "deployment");
    return this._systemHome;
  }

  public String getOSHome()
  {
    if (this._osHome == null)
      this._osHome = (File.separator + "etc");
    return this._osHome;
  }

  public void sendJFXPing(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, int paramInt, String paramString6)
  {
  }

  public boolean isBrowserFireFox()
  {
    return false;
  }

  public boolean isNativeModalDialogUp()
  {
    return false;
  }

  public boolean isPlatformWindowsVista()
  {
    return false;
  }

  public String getPlatformExtension()
  {
    return "";
  }

  public String getLibraryPrefix()
  {
    return "lib";
  }

  public String getLibrarySufix()
  {
    return ".so";
  }

  public int installShortcut(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7)
  {
    return 0;
  }

  public boolean useAltFileSystemView()
  {
    return false;
  }

  public boolean isLocalInstallSupported()
  {
    return getEnv("GNOME_DESKTOP_SESSION_ID") != null;
  }

  public boolean systemLookAndFeelDefault()
  {
    return (getEnv("GNOME_DESKTOP_SESSION_ID") != null) && (Config.isJavaVersionAtLeast15());
  }

  public String getSessionSpecificString()
  {
    String str = getEnv("DISPLAY");
    if (str != null)
      return str;
    return "";
  }

  public String getPlatformSpecificJavaName()
  {
    return "java";
  }

  public native String getEnv(String paramString);

  public String getBrowserPath()
  {
    String str = Config.getStringProperty("deployment.browser.path");
    File localFile;
    if ((str != null) && (str.length() > 0))
    {
      localFile = new File(str);
      if (!localFile.exists())
        str = null;
    }
    if ((str == null) || (str.length() == 0))
    {
      localFile = SearchPath.findOne(getEnv("PATH"));
      if (localFile != null)
        str = localFile.getAbsolutePath();
    }
    return str;
  }

  public String getFireFoxUserProfileDirectory()
  {
    return getMozillaUserProfileDirectory();
  }

  public String getMozillaUserProfileDirectory()
  {
    String str1 = null;
    try
    {
      String str2 = System.getProperty("user.home");
      File localFile = new File(str2 + "/.mozilla/appreg");
      if (localFile.exists())
        str1 = NSPreferences.getNS6UserProfileDirectory(localFile);
    }
    catch (IOException localIOException)
    {
    }
    return str1;
  }

  public boolean showDocument(String paramString)
  {
    String str = getBrowserPath();
    int i = 0;
    if ((str != null) && (!str.equals("")))
    {
      File localFile = new File(str);
      if (localFile.exists())
      {
        String[] arrayOfString = null;
        Process localProcess = null;
        arrayOfString = getExtendedBrowserArgs(localFile, paramString);
        if (arrayOfString != null)
          try
          {
            Trace.println("Invoking browser with: \n     " + argsFromArray(arrayOfString), TraceLevel.BASIC);
            localProcess = Runtime.getRuntime().exec(arrayOfString);
            int j = localProcess.waitFor();
            if (j == 0)
              i = 1;
          }
          catch (IOException localIOException1)
          {
            Trace.ignoredException(localIOException1);
          }
          catch (InterruptedException localInterruptedException)
          {
            Trace.ignoredException(localInterruptedException);
          }
        if (i == 0)
          try
          {
            arrayOfString = getBasicBrowserArgs(localFile, paramString);
            Trace.println("Invoking browser with: \n     " + argsFromArray(arrayOfString), TraceLevel.BASIC);
            localProcess = Runtime.getRuntime().exec(arrayOfString);
            i = localProcess != null ? 1 : 0;
          }
          catch (IOException localIOException2)
          {
            Trace.ignoredException(localIOException2);
          }
      }
    }
    return i;
  }

  public String argsFromArray(String[] paramArrayOfString)
  {
    StringBuffer localStringBuffer = new StringBuffer(2048);
    for (int i = 0; i < paramArrayOfString.length; i++)
      localStringBuffer.append(paramArrayOfString[i]).append(' ');
    return localStringBuffer.toString().trim();
  }

  public String[] argsFromString(String paramString)
  {
    ArrayList localArrayList = new ArrayList(32);
    StringBuffer localStringBuffer = new StringBuffer(64);
    int i = 0;
    int j = 0;
    int k = 0;
    String[] arrayOfString = new String[0];
    if (paramString != null)
      k = paramString.length();
    for (int m = 0; m < k; m++)
    {
      char c = paramString.charAt(m);
      switch (c)
      {
      case '\\':
        m++;
        if (m >= k)
          continue;
        localStringBuffer.append(paramString.charAt(m));
        break;
      case '\'':
        if (i != 0)
          localStringBuffer.append(c);
        j = j == 0 ? 1 : 0;
        break;
      case '"':
        if (j != 0)
          localStringBuffer.append(c);
        i = i == 0 ? 1 : 0;
        break;
      case '\t':
      case '\n':
      case '\r':
      case ' ':
        if ((j != 0) || (i != 0))
        {
          localStringBuffer.append(c);
        }
        else
        {
          if (localStringBuffer.length() <= 0)
            continue;
          localArrayList.add(localStringBuffer.toString());
          localStringBuffer.delete(0, localStringBuffer.length());
        }
        break;
      default:
        localStringBuffer.append(c);
      }
    }
    if (localStringBuffer.length() > 0)
      localArrayList.add(localStringBuffer.toString());
    return (String[])(String[])localArrayList.toArray(arrayOfString);
  }

  private String[] getBasicBrowserArgs(File paramFile, String paramString)
  {
    String[] arrayOfString = { paramFile.getAbsolutePath(), paramString };
    return arrayOfString;
  }

  private String[] getExtendedBrowserArgs(File paramFile, String paramString)
  {
    int i = 0;
    String[] arrayOfString1 = argsFromString(Config.getStringProperty("deployment.browser.args"));
    String[] arrayOfString2 = null;
    for (int j = 0; j < arrayOfString1.length; j++)
    {
      int k = 0;
      int m = -1;
      m = arrayOfString1[j].indexOf("%u", k);
      while (m >= 0)
      {
        arrayOfString1[j] = (arrayOfString1[j].substring(k, m) + paramString + arrayOfString1[j].substring(m + 2));
        k = m + 2;
        m = arrayOfString1[j].indexOf("%u", k);
        i = 1;
      }
    }
    if (i != 0)
    {
      arrayOfString2 = new String[arrayOfString1.length + 1];
      System.arraycopy(arrayOfString1, 0, arrayOfString2, 1, arrayOfString1.length);
      arrayOfString2[0] = paramFile.getAbsolutePath();
    }
    return arrayOfString2;
  }

  public String getDebugJavaPath(String paramString)
  {
    return paramString;
  }

  public boolean isPlatformIconType(String paramString)
  {
    return (isLocalInstallSupported()) && ((paramString.toLowerCase().endsWith(".png")) || (paramString.toLowerCase().endsWith(".ico")));
  }

  public Vector getInstalledJREList()
  {
    return getPublicJres();
  }

  public String getBrowserHomePath()
  {
    return System.getenv("MOZILLA_HOME");
  }

  public String getUserHomeOverride()
  {
    return null;
  }

  public void setUserHomeOverride(String paramString)
  {
  }

  public void init()
  {
  }

  public void onSave(Object paramObject)
  {
  }

  public void onLoad(Object paramObject)
  {
  }

  public void resetJavaHome()
  {
  }

  public Vector getPublicJres()
  {
    String str1 = System.getProperty("user.home");
    Object localObject = System.getProperty("java.class.path");
    String str2 = System.getProperty("sun.boot.class.path");
    if (localObject == null)
      localObject = str2;
    else
      localObject = (String)localObject + File.pathSeparator + str2;
    StringTokenizer localStringTokenizer = new StringTokenizer((String)localObject, File.pathSeparator);
    String str3 = "";
    while (localStringTokenizer.hasMoreElements())
    {
      str3 = (String)localStringTokenizer.nextElement();
      if (!str3.endsWith("rt.jar"))
        continue;
    }
    int i = str3.lastIndexOf(File.separatorChar);
    str3 = str3.substring(0, i);
    i = str3.lastIndexOf(File.separatorChar);
    str3 = str3.substring(0, i);
    String str4 = System.getProperty("java.version");
    int j = str4.lastIndexOf("-");
    Vector localVector = new Vector();
    if (j != -1)
      str4 = str4.substring(0, j);
    localVector.addElement(str4);
    localVector.addElement(str3);
    return (Vector)localVector;
  }

  public Vector getPublicJdks()
  {
    return new Vector();
  }

  public int applyBrowserSettings()
  {
    return 0;
  }

  public void initBrowserSettings()
  {
  }

  public boolean getJqsSettings()
  {
    return false;
  }

  public void setJqsSettings(boolean paramBoolean)
  {
  }

  public boolean getJavaPluginSettings()
  {
    return false;
  }

  public int setJavaPluginSettings(boolean paramBoolean)
  {
    return 2;
  }

  public String getLongPathName(String paramString)
  {
    return paramString;
  }

  public boolean samePaths(String paramString1, String paramString2)
  {
    try
    {
      return new File(paramString1).getCanonicalPath().equals(new File(paramString2).getCanonicalPath());
    }
    catch (Throwable localThrowable)
    {
    }
    return false;
  }

  public boolean canBecomeAdmin()
  {
    return hasAdminPrivileges();
  }

  public boolean hasAdminPrivileges()
  {
    return false;
  }

  public void handleUserResponse(int paramInt)
  {
  }

  public boolean shouldPromptForAutoCheck()
  {
    return false;
  }

  public String toExecArg(String paramString)
  {
    return paramString;
  }

  public boolean showURL(String paramString)
  {
    return false;
  }

  public int getSystemShortcutIconSize(boolean paramBoolean)
  {
    return paramBoolean ? 32 : 16;
  }

  public String getSystemJavawsPath()
  {
    return Environment.getJavawsCommand();
  }

  public long getNativePID()
  {
    return getPlatformPID();
  }

  public String getDefaultSystemCache()
  {
    return null;
  }

  public boolean canAutoDownloadJRE()
  {
    return true;
  }

  public void addRemoveProgramsAdd(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, boolean paramBoolean)
  {
  }

  public void addRemoveProgramsRemove(String paramString, boolean paramBoolean)
  {
  }

  public void notifyJREInstalled(String paramString)
  {
  }

  public void cacheSecurityBaseline(String paramString1, String paramString2)
  {
  }

  public native int getPlatformMaxCommandLineLength();

  protected native long getPlatformPID();

  public String getLoadedNativeLibPath(String paramString)
  {
    throw new UnsupportedOperationException("Not yet supported on Unix platform.");
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.UnixPlatform
 * JD-Core Version:    0.6.0
 */