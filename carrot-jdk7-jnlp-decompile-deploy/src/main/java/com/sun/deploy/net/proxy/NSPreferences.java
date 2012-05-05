package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class NSPreferences
{
  private static String parseValue(String paramString)
  {
    int i = paramString.indexOf(",");
    if (i != -1)
    {
      int j = paramString.lastIndexOf(")");
      if ((j != -1) && (i + 1 < j))
        return paramString.substring(i + 1, j).trim();
    }
    return null;
  }

  private static String parseString(String paramString)
  {
    String str = parseValue(paramString);
    if ((str != null) && (str.length() > 1) && (str.startsWith("\"")) && (str.endsWith("\"")))
      return str.substring(1, str.length() - 1);
    return null;
  }

  private static int parseInt(String paramString)
  {
    String str = parseValue(paramString);
    if (str != null)
      try
      {
        return Integer.parseInt(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    return -1;
  }

  private static List parseList(String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(parseString(paramString), ", ");
    ArrayList localArrayList = new ArrayList();
    while (localStringTokenizer.hasMoreTokens())
      localArrayList.add("*" + localStringTokenizer.nextToken());
    return localArrayList;
  }

  private static boolean isKeyword(String paramString1, String paramString2)
  {
    int i = paramString1.indexOf("(");
    return (i != -1) && (paramString1.substring(i + 1, paramString1.length()).startsWith("\"" + paramString2 + "\""));
  }

  private static boolean getDeaultValue(File paramFile)
  {
    int i = 0;
    try
    {
      if (paramFile.exists())
      {
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(paramFile), "ISO-8859-1"));
        String str;
        while ((str = localBufferedReader.readLine()) != null)
        {
          if ((!str.startsWith("pref")) || (!isKeyword(str, "config.use_system_prefs")))
            continue;
          if (parseValue(str).equals("true"))
            i = 1;
          else
            i = 0;
        }
        localBufferedReader.close();
      }
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    return i;
  }

  private static boolean getDefaultValueOfUseSystemPrefs()
  {
    int i = 0;
    File localFile1 = new File("/usr/sfw/lib/mozilla/greprefs/all.js");
    File localFile2 = new File("/usr/lib/mozilla/greprefs/all.js");
    i = (getDeaultValue(localFile1)) || (getDeaultValue(localFile2)) ? 1 : 0;
    return i;
  }

  protected static void parseFile(File paramFile, BrowserProxyInfo paramBrowserProxyInfo, float paramFloat, boolean paramBoolean)
  {
    BufferedReader localBufferedReader = null;
    try
    {
      localBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(paramFile), "ISO-8859-1"));
      String str2 = null;
      String str3 = null;
      int i = -1;
      int j = -1;
      boolean bool = getDefaultValueOfUseSystemPrefs();
      String str1;
      while ((str1 = localBufferedReader.readLine()) != null)
      {
        if (!str1.startsWith("user_pref"))
          continue;
        if (isKeyword(str1, "network.proxy.type"))
        {
          i = parseInt(str1);
          if ((i == 3) && (System.getProperty("os.name").equals("SunOS")) && (paramFloat >= 4.0F))
          {
            paramBrowserProxyInfo.setType(0);
            i = 0;
          }
          else if (i == 5)
          {
            paramBrowserProxyInfo.setType(2);
          }
          else
          {
            paramBrowserProxyInfo.setType(i);
          }
          Trace.msgNetPrintln("net.proxy.browser.proxyEnable", new Object[] { new Integer(i) });
          continue;
        }
        if (isKeyword(str1, "network.proxy.http"))
        {
          str2 = parseString(str1);
          try
          {
            URL localURL1 = new URL(str2);
            str2 = localURL1.getHost();
          }
          catch (MalformedURLException localMalformedURLException1)
          {
          }
          Trace.netPrintln("    network.proxy.http=" + str2);
          paramBrowserProxyInfo.setHttpHost(str2);
          continue;
        }
        if (isKeyword(str1, "network.proxy.http_port"))
        {
          j = parseInt(str1);
          Trace.netPrintln("    network.proxy.http_port=" + j);
          paramBrowserProxyInfo.setHttpPort(j);
          continue;
        }
        if (isKeyword(str1, "network.proxy.ssl"))
        {
          String str4 = parseString(str1);
          try
          {
            URL localURL2 = new URL(str4);
            str4 = localURL2.getHost();
          }
          catch (MalformedURLException localMalformedURLException2)
          {
          }
          Trace.netPrintln("    network.proxy.ssl=" + str4);
          paramBrowserProxyInfo.setHttpsHost(str4);
          continue;
        }
        if (isKeyword(str1, "network.proxy.ssl_port"))
        {
          int k = parseInt(str1);
          Trace.netPrintln("    network.proxy.ssl_port=" + k);
          paramBrowserProxyInfo.setHttpsPort(k);
          continue;
        }
        if (isKeyword(str1, "network.proxy.ftp"))
        {
          String str5 = parseString(str1);
          try
          {
            URL localURL3 = new URL(str5);
            str5 = localURL3.getHost();
          }
          catch (MalformedURLException localMalformedURLException3)
          {
          }
          Trace.netPrintln("    network.proxy.ftp=" + str5);
          paramBrowserProxyInfo.setFtpHost(str5);
          continue;
        }
        if (isKeyword(str1, "network.proxy.ftp_port"))
        {
          int m = parseInt(str1);
          Trace.netPrintln("    network.proxy.ftp_port=" + m);
          paramBrowserProxyInfo.setFtpPort(m);
          continue;
        }
        if (isKeyword(str1, "network.proxy.gopher"))
        {
          String str6 = parseString(str1);
          try
          {
            URL localURL4 = new URL(str6);
            str6 = localURL4.getHost();
          }
          catch (MalformedURLException localMalformedURLException4)
          {
          }
          Trace.netPrintln("    network.proxy.gopher=" + str6);
          paramBrowserProxyInfo.setGopherHost(str6);
          continue;
        }
        if (isKeyword(str1, "network.proxy.gopher_port"))
        {
          int n = parseInt(str1);
          Trace.netPrintln("    network.proxy.gopher_port=" + n);
          paramBrowserProxyInfo.setGopherPort(n);
          continue;
        }
        if (isKeyword(str1, "network.proxy.socks"))
        {
          String str7 = parseString(str1);
          try
          {
            URL localURL5 = new URL(str7);
            str7 = localURL5.getHost();
          }
          catch (MalformedURLException localMalformedURLException5)
          {
          }
          Trace.netPrintln("    network.proxy.socks=" + str7);
          paramBrowserProxyInfo.setSocksHost(str7);
          continue;
        }
        if (isKeyword(str1, "network.proxy.socks_port"))
        {
          int i1 = parseInt(str1);
          Trace.netPrintln("    network.proxy.socks_port=" + i1);
          paramBrowserProxyInfo.setSocksPort(i1);
          continue;
        }
        if (isKeyword(str1, "network.proxy.no_proxies_on"))
        {
          Trace.msgNetPrintln("net.proxy.browser.proxyOverride", new Object[] { parseString(str1) });
          paramBrowserProxyInfo.setOverrides(parseList(str1));
          continue;
        }
        if (isKeyword(str1, "network.proxy.autoconfig_url"))
        {
          str3 = parseString(str1);
          Trace.msgNetPrintln("net.proxy.browser.autoConfigURL", new Object[] { str3 });
          paramBrowserProxyInfo.setAutoConfigURL(str3);
          continue;
        }
        if (!isKeyword(str1, "config.use_system_prefs"))
          continue;
        if (parseValue(str1).equals("true"))
        {
          bool = true;
          continue;
        }
        bool = false;
      }
      localBufferedReader.close();
      if ((paramBoolean) && (bool))
      {
        paramBrowserProxyInfo.setType(4);
        return;
      }
      if ((i == -1) && (System.getProperty("os.name").equals("SunOS")) && (paramFloat >= 6.0F))
      {
        paramBrowserProxyInfo.setType(2);
        return;
      }
      if ((i == -1) && (System.getProperty("os.name").equals("Linux")) && (paramFloat >= 4.0F))
      {
        paramBrowserProxyInfo.setType(0);
        return;
      }
      if ((i == -1) && (System.getProperty("os.name").indexOf("Windows") != -1) && (paramFloat >= 4.0F) && (paramFloat < 5.0F))
      {
        paramBrowserProxyInfo.setType(0);
        return;
      }
      if ((str2 != null) && (j == -1) && (System.getProperty("os.name").indexOf("Windows") != -1) && (paramFloat >= 6.0F))
      {
        j = 8080;
        paramBrowserProxyInfo.setHttpPort(8080);
      }
      if ((System.getProperty("os.name").indexOf("Windows") != -1) && (paramFloat >= 6.0F) && (i == -1))
      {
        i = 0;
        paramBrowserProxyInfo.setType(0);
      }
      if ((i == -1) && (str3 != null))
        paramBrowserProxyInfo.setType(2);
      if ((i == 4) && (paramFloat >= 6.0F))
      {
        paramBrowserProxyInfo.setType(2);
        paramBrowserProxyInfo.setAutoProxyDetectionEnabled(true);
      }
    }
    catch (IOException localIOException1)
    {
      if (localBufferedReader != null)
        try
        {
          localBufferedReader.close();
        }
        catch (IOException localIOException2)
        {
        }
    }
  }

  public static File getNS6PrefsFile(File paramFile)
    throws IOException
  {
    return new File(getNS6UserProfileDirectory(paramFile), "prefs.js");
  }

  public static String getNS6UserProfileDirectory(File paramFile)
    throws IOException
  {
    NSRegistry localNSRegistry = new NSRegistry().open(paramFile);
    String str1 = null;
    String str2 = null;
    if (localNSRegistry != null)
    {
      str2 = localNSRegistry.get("Common/Profiles/CurrentProfile");
      if (str2 != null)
        str1 = localNSRegistry.get("Common/Profiles/" + str2 + "/directory");
      localNSRegistry.close();
    }
    if (str1 == null)
      throw new IOException();
    return str1;
  }

  public static String getFireFoxUserProfileDirectory(File paramFile)
    throws IOException
  {
    String str1 = null;
    String str2 = null;
    String str3 = null;
    int i = 1;
    int j = 0;
    if (paramFile.exists())
    {
      BufferedReader localBufferedReader = new BufferedReader(new FileReader(paramFile));
      while ((str2 = localBufferedReader.readLine()) != null)
      {
        if (str2.trim().toLowerCase().equals("[profile0]"))
        {
          j = 1;
          continue;
        }
        if ((j != 0) && (str2.toLowerCase().startsWith("isrelative=")))
        {
          try
          {
            int m = Integer.parseInt(str2.substring("isrelative=".length()));
            i = m != 0 ? 1 : 0;
          }
          catch (NumberFormatException localNumberFormatException)
          {
            i = 1;
          }
          continue;
        }
        if ((j == 0) || (!str2.toLowerCase().startsWith("path=")))
          continue;
        str1 = str2.substring("path=".length());
      }
    }
    if (str1 != null)
      if (i != 0)
      {
        int k = paramFile.toString().length() - 12;
        str3 = paramFile.toString().substring(0, k) + str1;
      }
      else
      {
        str3 = str1;
      }
    if (str3 == null)
      throw new IOException();
    return str3;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.NSPreferences
 * JD-Core Version:    0.6.0
 */