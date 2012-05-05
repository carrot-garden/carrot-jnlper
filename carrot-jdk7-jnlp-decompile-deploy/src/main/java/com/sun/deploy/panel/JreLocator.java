package com.sun.deploy.panel;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class JreLocator
{
  public static final int DEFAULT_TIMEOUT = 15000;
  private static final String PRODUCT_ID = "productVersion=";
  private static final String PLATFORM_ID = "platformVersion=";

  public static JREInfo getVersion(File paramFile)
  {
    return getVersion(paramFile, 15000);
  }

  public static JREInfo getVersion(File paramFile, int paramInt)
  {
    String str = Environment.getDeploymentHomePath() + File.separator + "lib" + File.separator + "deploy.jar";
    String[] arrayOfString = execute(new String[] { paramFile.getPath(), "-classpath", str, JreLocator.class.getName() }, paramInt);
    JREInfo localJREInfo = null;
    if (arrayOfString != null)
    {
      localJREInfo = extractVersion(paramFile.getPath(), arrayOfString[0]);
      if (localJREInfo == null)
        localJREInfo = extractVersion(paramFile.getPath(), arrayOfString[1]);
      if ((localJREInfo != null) && (localJREInfo.getPlatform().equals("1.2")))
      {
        arrayOfString = execute(new String[] { paramFile.getPath(), "-fullversion" }, paramInt);
        if (arrayOfString != null)
        {
          localJREInfo = extractVersionFor12(paramFile.getPath(), arrayOfString[0]);
          if (localJREInfo == null)
            localJREInfo = extractVersionFor12(paramFile.getPath(), arrayOfString[1]);
        }
      }
    }
    if (Trace.isEnabled(TraceLevel.BASIC))
      Trace.println("\tjre search returning: " + localJREInfo, TraceLevel.BASIC);
    return localJREInfo;
  }

  private static String[] execute(String[] paramArrayOfString, int paramInt)
  {
    Process localProcess = null;
    int i = 0;
    Trace.println("jre search executing", TraceLevel.BASIC);
    for (int j = 0; j < paramArrayOfString.length; j++)
      Trace.println(j + ": " + paramArrayOfString[j], TraceLevel.BASIC);
    try
    {
      localProcess = Runtime.getRuntime().exec(paramArrayOfString);
    }
    catch (IOException localIOException)
    {
      i = 1;
    }
    int k = -1;
    int m = paramInt / 100;
    while (i == 0)
    {
      try
      {
        Thread.sleep(100L);
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      try
      {
        k = localProcess.exitValue();
        i = 1;
        Trace.println("\tfinished executing " + k, TraceLevel.BASIC);
      }
      catch (IllegalThreadStateException localIllegalThreadStateException)
      {
        m--;
        if (m == 0)
        {
          i = 1;
          Trace.println("\tfailed " + k, TraceLevel.BASIC);
          localProcess.destroy();
        }
      }
    }
    if ((i != 0) && (k == 0))
    {
      String[] arrayOfString = new String[2];
      arrayOfString[0] = readFromStream(localProcess.getErrorStream());
      arrayOfString[1] = readFromStream(localProcess.getInputStream());
      Trace.println("result: " + arrayOfString[0], TraceLevel.BASIC);
      Trace.println("result: " + arrayOfString[1], TraceLevel.BASIC);
      return arrayOfString;
    }
    return null;
  }

  private static String readFromStream(InputStream paramInputStream)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    try
    {
      byte[] arrayOfByte = new byte[80];
      int i = 0;
      while (i == 0)
      {
        int j = paramInputStream.read(arrayOfByte, 0, 80);
        if (j == -1)
          i = 1;
        else if (j > 0)
          localStringBuffer.append(new String(arrayOfByte, 0, j));
      }
    }
    catch (IOException localIOException1)
    {
    }
    try
    {
      paramInputStream.close();
    }
    catch (IOException localIOException2)
    {
    }
    return localStringBuffer.toString();
  }

  private static JREInfo extractVersion(String paramString1, String paramString2)
  {
    String str1 = extractString("platformVersion=", paramString2);
    String str2 = extractString("productVersion=", paramString2);
    if ((str1 != null) && (str2 != null))
      return new JREInfo(str1, str2, null, paramString1, null, Config.getOSName(), Config.getOSArch(), true, false);
    return null;
  }

  private static String extractString(String paramString1, String paramString2)
  {
    int i = paramString2.indexOf(paramString1);
    if (i != -1)
    {
      int j = paramString2.indexOf('\n', i);
      String str;
      if (j != -1)
        str = paramString2.substring(i + paramString1.length(), j);
      else
        str = paramString2.substring(i + paramString1.length());
      if ((str.length() > 0) && (str.charAt(str.length() - 1) == '\r'))
        str = str.substring(0, str.length() - 1);
      return str;
    }
    return null;
  }

  private static JREInfo extractVersionFor12(String paramString1, String paramString2)
  {
    int i = paramString2.indexOf("1.2");
    int j = paramString2.length();
    if ((i != -1) && (i < j - 1))
    {
      int k = paramString2.indexOf('"', i);
      if (k != -1)
      {
        String str = paramString2.substring(i, k);
        return new JREInfo("1.2", str, null, paramString1, null, Config.getOSName(), Config.getOSArch(), true, false);
      }
    }
    return null;
  }

  private static String getClassPath(File paramFile)
  {
    File localFile = paramFile;
    localFile = localFile.getParentFile();
    if (localFile != null)
    {
      localFile = localFile.getParentFile();
      if (localFile != null)
      {
        localFile = new File(localFile, "lib");
        if ((localFile != null) && (localFile.exists()))
        {
          localFile = new File(localFile, "classes.zip");
          if ((localFile != null) && (localFile.exists()))
            return getThisPath() + File.pathSeparator + localFile.getPath();
        }
      }
    }
    return getThisPath();
  }

  public static void main(String[] paramArrayOfString)
  {
    write("platformVersion=", System.getProperty("java.specification.version"));
    write("productVersion=", System.getProperty("java.version"));
  }

  private static void write(String paramString1, String paramString2)
  {
    if (paramString2 != null)
      System.out.println(paramString1 + paramString2);
  }

  private static String getThisPath()
  {
    return Environment.getDeploymentHomePath() + File.separator + "lib" + File.separator + "javaws.jar";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.JreLocator
 * JD-Core Version:    0.6.0
 */