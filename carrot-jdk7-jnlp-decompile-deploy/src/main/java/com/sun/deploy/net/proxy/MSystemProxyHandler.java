package com.sun.deploy.net.proxy;

import com.sun.deploy.config.Platform;
import com.sun.deploy.trace.Trace;
import java.net.URL;

public class MSystemProxyHandler
  implements ProxyHandler
{
  protected static boolean hasSystemProxies;

  public final boolean isSupported(int paramInt)
  {
    return paramInt == 4;
  }

  public final boolean isProxyCacheSupported()
  {
    return false;
  }

  public final void init(BrowserProxyInfo paramBrowserProxyInfo)
    throws ProxyConfigException
  {
    Trace.msgNetPrintln("net.proxy.loading.system");
    if (!isSupported(paramBrowserProxyInfo.getType()))
      throw new ProxyConfigException("Unable to support proxy type: " + paramBrowserProxyInfo.getType());
    Trace.msgNetPrintln("net.proxy.loading.done");
  }

  public final ProxyInfo[] getProxyInfo(URL paramURL)
    throws ProxyUnavailableException
  {
    ProxyInfo[] arrayOfProxyInfo = new ProxyInfo[1];
    String str1 = paramURL.getProtocol();
    String str2 = paramURL.getHost();
    if (hasSystemProxies)
    {
      String str3 = getSystemProxy(str1, str2);
      if (str3 == null)
        return new ProxyInfo[] { new ProxyInfo(null) };
      arrayOfProxyInfo[0] = new ProxyInfo(str3);
    }
    else
    {
      return new ProxyInfo[] { new ProxyInfo(null) };
    }
    return arrayOfProxyInfo;
  }

  protected static native boolean init();

  protected native String getSystemProxy(String paramString1, String paramString2);

  static
  {
    Platform.get().loadDeployNativeLib();
    hasSystemProxies = false;
    hasSystemProxies = init();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.MSystemProxyHandler
 * JD-Core Version:    0.6.0
 */