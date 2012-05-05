package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import java.net.URL;

final class DirectProxyHandler
  implements ProxyHandler
{
  public boolean isSupported(int paramInt)
  {
    return paramInt == 0;
  }

  public boolean isProxyCacheSupported()
  {
    return true;
  }

  public void init(BrowserProxyInfo paramBrowserProxyInfo)
    throws ProxyConfigException
  {
    Trace.msgNetPrintln("net.proxy.loading.direct");
    if (!isSupported(paramBrowserProxyInfo.getType()))
      throw new ProxyConfigException("Unable to support proxy type: " + paramBrowserProxyInfo.getType());
    Trace.msgNetPrintln("net.proxy.loading.done");
  }

  public ProxyInfo[] getProxyInfo(URL paramURL)
  {
    return new ProxyInfo[] { new ProxyInfo(null) };
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.DirectProxyHandler
 * JD-Core Version:    0.6.0
 */