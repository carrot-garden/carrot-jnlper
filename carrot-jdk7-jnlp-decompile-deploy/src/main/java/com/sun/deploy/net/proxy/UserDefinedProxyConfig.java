package com.sun.deploy.net.proxy;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

public final class UserDefinedProxyConfig
  implements BrowserProxyConfig
{
  public BrowserProxyInfo getBrowserProxyInfo()
  {
    Trace.msgNetPrintln("net.proxy.loading.userdef");
    BrowserProxyInfo localBrowserProxyInfo = new BrowserProxyInfo();
    localBrowserProxyInfo.setType(3);
    Config.get().refreshIfNeeded();
    int i = Config.getProxyType();
    if (i == 0)
    {
      localBrowserProxyInfo.setType(0);
    }
    else if ((i == 1) || (i == 2))
    {
      localBrowserProxyInfo.setType(1);
      if (i == 2)
      {
        localObject1 = Config.getProxyAutoConfig();
        Trace.msgNetPrintln("net.proxy.browser.autoConfigURL", new Object[] { localObject1 });
        if ((localObject1 != null) && (!"".equals(((String)localObject1).trim())))
        {
          localBrowserProxyInfo.setType(2);
          localBrowserProxyInfo.setAutoConfigURL((String)localObject1);
        }
      }
      Object localObject1 = new StringBuffer();
      int j = 0;
      boolean bool = Config.isProxySame();
      String str1 = Config.getProxyHttpHost();
      if ((str1 != null) && (str1.trim().length() > 0))
      {
        j = 1;
        if (!bool)
          ((StringBuffer)localObject1).append("http=");
        ((StringBuffer)localObject1).append(str1);
        int k = Config.getProxyHttpPort();
        if (k > 0)
          ((StringBuffer)localObject1).append(":" + k);
      }
      Object localObject2;
      if (!bool)
      {
        str2 = Config.getProxyHttpsHost();
        if ((str2 != null) && (str2.trim().length() > 0))
        {
          if (j != 0)
            ((StringBuffer)localObject1).append(";");
          j = 1;
          ((StringBuffer)localObject1).append("https=");
          ((StringBuffer)localObject1).append(str2);
          int m = Config.getProxyHttpsPort();
          if (m > 0)
            ((StringBuffer)localObject1).append(":" + m);
        }
        str3 = Config.getProxyFtpHost();
        if ((str3 != null) && (str3.trim().length() > 0))
        {
          if (j != 0)
            ((StringBuffer)localObject1).append(";");
          j = 1;
          ((StringBuffer)localObject1).append("ftp=");
          ((StringBuffer)localObject1).append(str3);
          int n = Config.getProxyFtpPort();
          if (n > 0)
            ((StringBuffer)localObject1).append(":" + n);
        }
        localObject2 = Config.getProxySocksHost();
        if ((localObject2 != null) && (((String)localObject2).trim().length() > 0))
        {
          if (j != 0)
            ((StringBuffer)localObject1).append(";");
          j = 1;
          ((StringBuffer)localObject1).append("socks=");
          ((StringBuffer)localObject1).append((String)localObject2);
          int i1 = Config.getProxySocksPort();
          if (i1 > 0)
            ((StringBuffer)localObject1).append(":" + i1);
        }
      }
      String str2 = ((StringBuffer)localObject1).toString();
      Trace.msgNetPrintln("net.proxy.browser.proxyList", new Object[] { str2 });
      if ((str2 != null) && (!"".equals(str2.trim())))
        ProxyUtils.parseProxyServer(str2, localBrowserProxyInfo);
      String str3 = Config.getProxyBypass();
      if (Config.isProxyBypassLocal())
        if (str3 != null)
          str3 = str3 + ";<local>";
        else
          str3 = "<local>";
      Trace.msgNetPrintln("net.proxy.browser.proxyOverride", new Object[] { str3 });
      if ((str3 != null) && (!"".equals(str3.trim())))
      {
        localObject2 = new StringTokenizer(str3, ";");
        ArrayList localArrayList = new ArrayList();
        while (((StringTokenizer)localObject2).hasMoreTokens())
        {
          String str4 = ((StringTokenizer)localObject2).nextToken().toLowerCase(Locale.ENGLISH).trim();
          if (str4 != null)
            localArrayList.add(str4);
        }
        localBrowserProxyInfo.setOverrides(localArrayList);
      }
    }
    Trace.msgNetPrintln("net.proxy.loading.done");
    return (BrowserProxyInfo)(BrowserProxyInfo)localBrowserProxyInfo;
  }

  public void getSystemProxy(BrowserProxyInfo paramBrowserProxyInfo)
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.UserDefinedProxyConfig
 * JD-Core Version:    0.6.0
 */