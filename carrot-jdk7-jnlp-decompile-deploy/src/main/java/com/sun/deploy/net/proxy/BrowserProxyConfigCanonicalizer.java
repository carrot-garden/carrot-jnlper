package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import java.net.URL;

public class BrowserProxyConfigCanonicalizer
  implements BrowserProxyConfig
{
  private BrowserProxyInfo bpi = new BrowserProxyInfo();

  public BrowserProxyConfigCanonicalizer(BrowserProxyConfig paramBrowserProxyConfig, ProxyHandler paramProxyHandler)
  {
    this.bpi = paramBrowserProxyConfig.getBrowserProxyInfo();
    if (this.bpi.getType() == 4)
      paramBrowserProxyConfig.getSystemProxy(this.bpi);
    canonicalizeAutoConfigProxy(this.bpi, paramProxyHandler);
  }

  private void canonicalizeAutoConfigProxy(BrowserProxyInfo paramBrowserProxyInfo, ProxyHandler paramProxyHandler)
  {
    if (paramBrowserProxyInfo.getType() == 2)
    {
      ProxyInfo[] arrayOfProxyInfo = new ProxyInfo[0];
      try
      {
        paramProxyHandler.init(paramBrowserProxyInfo);
        arrayOfProxyInfo = paramProxyHandler.getProxyInfo(new URL("http://java.sun.com"));
      }
      catch (Throwable localThrowable)
      {
        Trace.msgNetPrintln("net.proxy.loading.auto.error");
      }
      ProxyInfo localProxyInfo = null;
      if (arrayOfProxyInfo.length > 0)
        localProxyInfo = arrayOfProxyInfo[0];
      paramBrowserProxyInfo.setHintOnly(true);
      if (localProxyInfo != null)
        if (localProxyInfo.isSocksUsed())
        {
          paramBrowserProxyInfo.setSocksHost(localProxyInfo.getSocksProxy());
          paramBrowserProxyInfo.setSocksPort(localProxyInfo.getSocksPort());
        }
        else
        {
          paramBrowserProxyInfo.setHttpHost(localProxyInfo.getProxy());
          paramBrowserProxyInfo.setHttpPort(localProxyInfo.getPort());
          paramBrowserProxyInfo.setHttpsHost(localProxyInfo.getProxy());
          paramBrowserProxyInfo.setHttpsPort(localProxyInfo.getPort());
          paramBrowserProxyInfo.setFtpHost(localProxyInfo.getProxy());
          paramBrowserProxyInfo.setFtpPort(localProxyInfo.getPort());
          paramBrowserProxyInfo.setGopherHost(localProxyInfo.getProxy());
          paramBrowserProxyInfo.setGopherPort(localProxyInfo.getPort());
        }
    }
  }

  public BrowserProxyInfo getBrowserProxyInfo()
  {
    return this.bpi;
  }

  public void getSystemProxy(BrowserProxyInfo paramBrowserProxyInfo)
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.BrowserProxyConfigCanonicalizer
 * JD-Core Version:    0.6.0
 */