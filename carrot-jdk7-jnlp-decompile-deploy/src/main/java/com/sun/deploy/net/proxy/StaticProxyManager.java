package com.sun.deploy.net.proxy;

import com.sun.deploy.config.Config;
import com.sun.deploy.net.protocol.rmi.DeployRMISocketFactory;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.rmi.server.RMISocketFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

public class StaticProxyManager
{
  public static void reset()
  {
    try
    {
      UserDefinedProxyConfig localUserDefinedProxyConfig = new UserDefinedProxyConfig();
      BrowserProxyInfo localBrowserProxyInfo1 = localUserDefinedProxyConfig.getBrowserProxyInfo();
      Service localService = ServiceManager.getService();
      Object localObject;
      if (localBrowserProxyInfo1.getType() == 3)
      {
        localObject = localService.getProxyConfig();
        localBrowserProxyInfo1 = ((BrowserProxyConfig)localObject).getBrowserProxyInfo();
      }
      else if (localBrowserProxyInfo1.getType() == 2)
      {
        localObject = new BrowserProxyConfigCanonicalizer(localUserDefinedProxyConfig, localService.getAutoProxyHandler());
        localBrowserProxyInfo1 = ((BrowserProxyConfigCanonicalizer)localObject).getBrowserProxyInfo();
      }
      try
      {
        localObject = System.getProperty("jnlp.cfg.normifactory");
        int i = !"true".equals(localObject) ? 1 : 0;
        if ((localBrowserProxyInfo1.getType() != 0) && (i != 0))
          RMISocketFactory.setSocketFactory(new DeployRMISocketFactory());
      }
      catch (Throwable localThrowable2)
      {
      }
      BrowserProxyInfo localBrowserProxyInfo2 = localBrowserProxyInfo1;
      AccessController.doPrivileged(new PrivilegedAction(localBrowserProxyInfo2)
      {
        private final BrowserProxyInfo val$info;

        public Object run()
        {
          StaticProxyManager.access$000(this.val$info);
          return null;
        }
      });
      Trace.msgNetPrintln(localBrowserProxyInfo1.toString());
    }
    catch (Throwable localThrowable1)
    {
      localThrowable1.printStackTrace();
      ToolkitStore.getUI().showExceptionDialog(null, null, localThrowable1, null, null, null, null);
    }
  }

  private static void setProperties(BrowserProxyInfo paramBrowserProxyInfo)
  {
    Properties localProperties = System.getProperties();
    int i = !Config.isJavaVersionAtLeast14() ? 1 : 0;
    switch (paramBrowserProxyInfo.getType())
    {
    case 0:
      localProperties.remove("trustProxy");
      if (i != 0)
      {
        localProperties.remove("proxyHost");
        localProperties.remove("proxyPort");
      }
      localProperties.remove("http.proxyHost");
      localProperties.remove("http.proxyPort");
      localProperties.remove("http.nonProxyHosts");
      localProperties.remove("https.proxyHost");
      localProperties.remove("https.proxyPort");
      localProperties.remove("https.nonProxyHosts");
      localProperties.remove("ftp.proxyHost");
      localProperties.remove("ftp.proxyPort");
      localProperties.remove("ftp.nonProxyHosts");
      localProperties.remove("gopherProxySet");
      localProperties.remove("gopherProxyHost");
      localProperties.remove("gopherProxyPort");
      localProperties.remove("socksProxyHost");
      localProperties.remove("socksProxyPort");
      break;
    case 1:
    case 2:
      localProperties.put("trustProxy", "true");
      if (paramBrowserProxyInfo.getHttpHost() != null)
      {
        if (i != 0)
        {
          localProperties.put("proxyHost", paramBrowserProxyInfo.getHttpHost());
          localProperties.put("proxyPort", String.valueOf(paramBrowserProxyInfo.getHttpPort()));
        }
        localProperties.put("http.proxyHost", paramBrowserProxyInfo.getHttpHost());
        localProperties.put("http.proxyPort", String.valueOf(paramBrowserProxyInfo.getHttpPort()));
        localProperties.put("http.nonProxyHosts", paramBrowserProxyInfo.getOverridesString());
      }
      if (paramBrowserProxyInfo.getHttpsHost() != null)
      {
        localProperties.put("https.proxyHost", paramBrowserProxyInfo.getHttpsHost());
        localProperties.put("https.proxyPort", String.valueOf(paramBrowserProxyInfo.getHttpsPort()));
        localProperties.put("https.nonProxyHosts", paramBrowserProxyInfo.getOverridesString());
      }
      if (paramBrowserProxyInfo.getFtpHost() != null)
      {
        localProperties.put("ftp.proxyHost", paramBrowserProxyInfo.getFtpHost());
        localProperties.put("ftp.proxyPort", String.valueOf(paramBrowserProxyInfo.getFtpPort()));
        localProperties.put("ftp.nonProxyHosts", paramBrowserProxyInfo.getOverridesString());
      }
      if (paramBrowserProxyInfo.getGopherHost() != null)
      {
        localProperties.put("gopherProxySet", "true");
        localProperties.put("gopherProxyHost", paramBrowserProxyInfo.getGopherHost());
        localProperties.put("gopherProxyPort", String.valueOf(paramBrowserProxyInfo.getGopherPort()));
      }
      if ((paramBrowserProxyInfo.getHttpHost() != null) || (paramBrowserProxyInfo.getSocksHost() == null))
        break;
      localProperties.put("socksProxyHost", paramBrowserProxyInfo.getSocksHost());
      localProperties.put("socksProxyPort", String.valueOf(paramBrowserProxyInfo.getSocksPort()));
      break;
    case 3:
      throw new IllegalStateException("StaticProxyManager:  ProxyType should not be BROWSER");
    }
    System.setProperties(localProperties);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.StaticProxyManager
 * JD-Core Version:    0.6.0
 */