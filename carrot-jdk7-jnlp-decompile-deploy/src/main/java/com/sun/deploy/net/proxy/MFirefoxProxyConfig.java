package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.AccessController;
import sun.security.action.GetPropertyAction;

public final class MFirefoxProxyConfig
  implements BrowserProxyConfig
{
  public BrowserProxyInfo getBrowserProxyInfo()
  {
    Trace.msgNetPrintln("net.proxy.loading.ns");
    File localFile1 = null;
    BrowserProxyInfo localBrowserProxyInfo = new BrowserProxyInfo();
    localBrowserProxyInfo.setType(-1);
    try
    {
      String str1 = System.getProperty("user.home");
      File localFile2 = new File(str1 + "/.mozilla/firefox/profiles.ini");
      if (localFile2.exists())
      {
        FileInputStream localFileInputStream = new FileInputStream(localFile2);
        InputStreamReader localInputStreamReader = new InputStreamReader(localFileInputStream, "ISO-8859-1");
        BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader);
        int i = 0;
        int j = 1;
        String str2;
        while ((str2 = localBufferedReader.readLine()) != null)
        {
          if (str2.trim().equals("[Profile0]"))
          {
            i = 1;
            continue;
          }
          if ((i != 0) && (str2.startsWith("isRelative=")))
          {
            try
            {
              int k = Integer.parseInt(str2.substring("isRelative=".length()));
              j = k != 0 ? 1 : 0;
            }
            catch (NumberFormatException localNumberFormatException)
            {
              j = 1;
            }
            continue;
          }
          if ((i == 0) || (!str2.startsWith("Path=")))
            continue;
          if (j != 0)
            localFile1 = new File(str1 + "/.mozilla/firefox/" + str2.substring("Path=".length()) + "/prefs.js");
          else
            localFile1 = new File(str2.substring("Path=".length()) + "/prefs.js");
        }
        localBufferedReader.close();
        if (localFile1.exists())
        {
          NSPreferences.parseFile(localFile1, localBrowserProxyInfo, 6.0F, false);
          if (localBrowserProxyInfo.isAutoProxyDetectionEnabled())
            localBrowserProxyInfo.setAutoConfigURL(WebProxyAutoDetection.getWPADURL());
        }
      }
    }
    catch (IOException localIOException)
    {
      localBrowserProxyInfo.setType(-1);
    }
    catch (SecurityException localSecurityException)
    {
      Trace.netPrintException(localSecurityException);
      localBrowserProxyInfo.setType(-1);
    }
    if (AccessController.doPrivileged(new GetPropertyAction("javaplugin.version")) != null)
      localBrowserProxyInfo.setType(3);
    Trace.msgNetPrintln("net.proxy.loading.done");
    return localBrowserProxyInfo;
  }

  public void getSystemProxy(BrowserProxyInfo paramBrowserProxyInfo)
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.MFirefoxProxyConfig
 * JD-Core Version:    0.6.0
 */