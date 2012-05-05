package com.sun.deploy.net.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

public final class ProxyUtils
{
  private static void parseProxyAddress(int paramInt, String paramString, BrowserProxyInfo paramBrowserProxyInfo)
  {
    String str1 = null;
    try
    {
      URL localURL = new URL(paramString);
      str1 = new String(localURL.getHost() + ":" + localURL.getPort());
    }
    catch (MalformedURLException localMalformedURLException)
    {
      str1 = new String(paramString);
    }
    StringTokenizer localStringTokenizer = new StringTokenizer(str1, ":");
    if (!localStringTokenizer.hasMoreTokens())
      return;
    String str2 = localStringTokenizer.nextToken();
    int i = -1;
    if (localStringTokenizer.hasMoreTokens())
      try
      {
        i = Integer.parseInt(localStringTokenizer.nextToken());
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    switch (paramInt)
    {
    case 1:
      paramBrowserProxyInfo.setHttpHost(str2);
      if (i == -1)
        i = 80;
      paramBrowserProxyInfo.setHttpPort(i);
      break;
    case 2:
      paramBrowserProxyInfo.setHttpsHost(str2);
      if (i == -1)
        i = 80;
      paramBrowserProxyInfo.setHttpsPort(i);
      break;
    case 4:
      paramBrowserProxyInfo.setFtpHost(str2);
      if (i == -1)
        i = 80;
      paramBrowserProxyInfo.setFtpPort(i);
      break;
    case 8:
      paramBrowserProxyInfo.setGopherHost(str2);
      if (i == -1)
        i = 80;
      paramBrowserProxyInfo.setGopherPort(i);
      break;
    case 16:
      paramBrowserProxyInfo.setSocksHost(str2);
      if (i == -1)
        i = 1080;
      paramBrowserProxyInfo.setSocksPort(i);
      break;
    default:
      throw new IllegalStateException("ProxyUtils: ProtocolType not valid");
    }
  }

  public static void parseProxyServer(String paramString, BrowserProxyInfo paramBrowserProxyInfo)
  {
    if (paramString.indexOf("=") != -1)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ";");
      while (localStringTokenizer.hasMoreTokens())
      {
        String str = localStringTokenizer.nextToken();
        if (str.startsWith("http="))
          parseProxyAddress(1, str.substring(5, str.length()), paramBrowserProxyInfo);
        else if (str.startsWith("https="))
          parseProxyAddress(2, str.substring(6, str.length()), paramBrowserProxyInfo);
        else if (str.startsWith("ftp="))
          parseProxyAddress(4, str.substring(4, str.length()), paramBrowserProxyInfo);
        else if (str.startsWith("gopher="))
          parseProxyAddress(8, str.substring(7, str.length()), paramBrowserProxyInfo);
        else if (str.startsWith("socks="))
          parseProxyAddress(16, str.substring(6, str.length()), paramBrowserProxyInfo);
      }
    }
    else
    {
      parseProxyAddress(1, paramString, paramBrowserProxyInfo);
      parseProxyAddress(2, paramString, paramBrowserProxyInfo);
      parseProxyAddress(4, paramString, paramBrowserProxyInfo);
      parseProxyAddress(8, paramString, paramBrowserProxyInfo);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.ProxyUtils
 * JD-Core Version:    0.6.0
 */