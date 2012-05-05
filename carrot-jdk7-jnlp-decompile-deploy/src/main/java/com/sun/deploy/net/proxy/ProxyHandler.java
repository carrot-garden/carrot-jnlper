package com.sun.deploy.net.proxy;

import java.net.URL;

public abstract interface ProxyHandler
{
  public abstract boolean isSupported(int paramInt);

  public abstract boolean isProxyCacheSupported();

  public abstract void init(BrowserProxyInfo paramBrowserProxyInfo)
    throws ProxyConfigException;

  public abstract ProxyInfo[] getProxyInfo(URL paramURL)
    throws ProxyUnavailableException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.ProxyHandler
 * JD-Core Version:    0.6.0
 */