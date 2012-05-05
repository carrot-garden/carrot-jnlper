package com.sun.deploy.net.proxy;

public class MDefaultBrowserProxyConfig
  implements BrowserProxyConfig
{
  private static String PROXY_PRO_HTTP = "http";
  private static String PROXY_PRO_FTP = "ftp";
  private static String PROXY_PRO_SOCKS = "socks";
  private static String PROXY_PRO_HTTPS = "https";
  private static String PROXY_EMPTY_URL = "";

  public BrowserProxyInfo getBrowserProxyInfo()
  {
    BrowserProxyInfo localBrowserProxyInfo = null;
    MFirefoxProxyConfig localMFirefoxProxyConfig = new MFirefoxProxyConfig();
    localBrowserProxyInfo = localMFirefoxProxyConfig.getBrowserProxyInfo();
    Object localObject;
    if (localBrowserProxyInfo.getType() == -1)
    {
      localObject = new MNetscape6ProxyConfig();
      localBrowserProxyInfo = ((MNetscape6ProxyConfig)localObject).getBrowserProxyInfo();
    }
    if (localBrowserProxyInfo.getType() == -1)
    {
      localObject = new MNetscape4ProxyConfig();
      localBrowserProxyInfo = ((MNetscape4ProxyConfig)localObject).getBrowserProxyInfo();
    }
    return (BrowserProxyInfo)localBrowserProxyInfo;
  }

  public void getSystemProxy(BrowserProxyInfo paramBrowserProxyInfo)
  {
    MSystemProxyHandler localMSystemProxyHandler = new MSystemProxyHandler();
    if (MSystemProxyHandler.hasSystemProxies)
    {
      String str = localMSystemProxyHandler.getSystemProxy(PROXY_PRO_HTTP, PROXY_EMPTY_URL);
      if (str != null)
      {
        paramBrowserProxyInfo.setHttpHost(getHost(str));
        paramBrowserProxyInfo.setHttpPort(getPort(str));
      }
      str = localMSystemProxyHandler.getSystemProxy(PROXY_PRO_FTP, PROXY_EMPTY_URL);
      if (str != null)
      {
        paramBrowserProxyInfo.setFtpHost(getHost(str));
        paramBrowserProxyInfo.setFtpPort(getPort(str));
      }
      str = localMSystemProxyHandler.getSystemProxy(PROXY_PRO_HTTPS, PROXY_EMPTY_URL);
      if (str != null)
      {
        paramBrowserProxyInfo.setHttpsHost(getHost(str));
        paramBrowserProxyInfo.setHttpsPort(getPort(str));
      }
      str = localMSystemProxyHandler.getSystemProxy(PROXY_PRO_SOCKS, PROXY_EMPTY_URL);
      if (str != null)
      {
        paramBrowserProxyInfo.setSocksHost(getHost(str));
        paramBrowserProxyInfo.setSocksPort(getPort(str));
      }
    }
  }

  private String getHost(String paramString)
  {
    return paramString.substring(0, paramString.indexOf(':'));
  }

  private int getPort(String paramString)
  {
    String str = paramString.substring(paramString.lastIndexOf(':') + 1);
    return Integer.parseInt(str);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.MDefaultBrowserProxyConfig
 * JD-Core Version:    0.6.0
 */