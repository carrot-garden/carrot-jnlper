package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import sun.security.action.GetPropertyAction;

public final class MNetscape6ProxyConfig
  implements BrowserProxyConfig
{
  public BrowserProxyInfo getBrowserProxyInfo()
  {
    Trace.msgNetPrintln("net.proxy.loading.ns");
    BrowserProxyInfo localBrowserProxyInfo = new BrowserProxyInfo();
    try
    {
      String str = System.getProperty("user.home");
      File localFile1 = new File(str + "/.mozilla/appreg");
      File localFile2 = null;
      try
      {
        localFile2 = NSPreferences.getNS6PrefsFile(localFile1);
        Trace.msgNetPrintln("net.proxy.browser.pref.read", new Object[] { localFile2.getPath() });
        NSPreferences.parseFile(localFile2, localBrowserProxyInfo, 6.0F, true);
        if (localBrowserProxyInfo.isAutoProxyDetectionEnabled())
          localBrowserProxyInfo.setAutoConfigURL(WebProxyAutoDetection.getWPADURL());
      }
      catch (IOException localIOException)
      {
        Trace.msgNetPrintln("net.proxy.ns6.regs.exception", new Object[] { localFile1.getPath() });
        localBrowserProxyInfo.setType(-1);
      }
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
 * Qualified Name:     com.sun.deploy.net.proxy.MNetscape6ProxyConfig
 * JD-Core Version:    0.6.0
 */