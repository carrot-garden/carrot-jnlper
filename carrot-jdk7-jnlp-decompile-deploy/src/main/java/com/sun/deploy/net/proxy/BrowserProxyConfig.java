package com.sun.deploy.net.proxy;

public abstract interface BrowserProxyConfig
{
  public abstract BrowserProxyInfo getBrowserProxyInfo();

  public abstract void getSystemProxy(BrowserProxyInfo paramBrowserProxyInfo);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.BrowserProxyConfig
 * JD-Core Version:    0.6.0
 */