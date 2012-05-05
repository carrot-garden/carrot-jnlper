package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import java.net.URL;
import java.util.StringTokenizer;

public abstract class AbstractBrowserProxyHandler
  implements ProxyHandler
{
  public final boolean isSupported(int paramInt)
  {
    return paramInt == 3;
  }

  public final boolean isProxyCacheSupported()
  {
    return false;
  }

  public final void init(BrowserProxyInfo paramBrowserProxyInfo)
    throws ProxyConfigException
  {
    Trace.msgNetPrintln("net.proxy.loading.browser");
    if (!isSupported(paramBrowserProxyInfo.getType()))
      throw new ProxyConfigException("Unable to support proxy type: " + paramBrowserProxyInfo.getType());
    Trace.msgNetPrintln("net.proxy.loading.done");
  }

  public final ProxyInfo[] getProxyInfo(URL paramURL)
  {
    String str = findProxyForURL(paramURL.toString());
    return extractAutoProxySetting(str);
  }

  protected abstract String findProxyForURL(String paramString);

  private ProxyInfo[] extractAutoProxySetting(String paramString)
  {
    if (paramString != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ";", false);
      ProxyInfo[] arrayOfProxyInfo = new ProxyInfo[localStringTokenizer.countTokens()];
      int i = 0;
      while (localStringTokenizer.hasMoreTokens())
      {
        String str = localStringTokenizer.nextToken();
        int j = str.indexOf("PROXY");
        if (j != -1)
        {
          arrayOfProxyInfo[(i++)] = new ProxyInfo(str.substring(j + 6));
          continue;
        }
        j = str.indexOf("SOCKS");
        if (j != -1)
        {
          arrayOfProxyInfo[(i++)] = new ProxyInfo(null, str.substring(j + 6));
          continue;
        }
        arrayOfProxyInfo[(i++)] = new ProxyInfo(null, -1);
      }
      return arrayOfProxyInfo;
    }
    return new ProxyInfo[] { new ProxyInfo(null) };
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.AbstractBrowserProxyHandler
 * JD-Core Version:    0.6.0
 */