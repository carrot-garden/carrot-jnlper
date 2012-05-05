package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import java.io.File;

public final class MNetscape4ProxyConfig
  implements BrowserProxyConfig
{
  public BrowserProxyInfo getBrowserProxyInfo()
  {
    Trace.msgNetPrintln("net.proxy.loading.ns");
    BrowserProxyInfo localBrowserProxyInfo = new BrowserProxyInfo();
    String str = null;
    str = System.getProperty("user.home");
    File localFile = new File(str + "/.netscape/preferences.js");
    NSPreferences.parseFile(localFile, localBrowserProxyInfo, 4.0F, true);
    Trace.msgNetPrintln("net.proxy.loading.done");
    return localBrowserProxyInfo;
  }

  public void getSystemProxy(BrowserProxyInfo paramBrowserProxyInfo)
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.MNetscape4ProxyConfig
 * JD-Core Version:    0.6.0
 */