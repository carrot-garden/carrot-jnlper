package com.sun.deploy.net.proxy;

import com.sun.deploy.net.protocol.rmi.DeployRMISocketFactory;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.rmi.server.RMISocketFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

public class DynamicProxyManager
{
  private static HashMap proxyCache = new HashMap();
  private static ProxyHandler handler = null;

  public static synchronized List getProxyList(URL paramURL, boolean paramBoolean)
  {
    String str1 = paramURL.getProtocol();
    String str2 = paramURL.getHost();
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append(str1);
    localStringBuffer.append(str2);
    localStringBuffer.append(paramURL.getPort());
    String str3 = localStringBuffer.toString();
    Object localObject = null;
    if (handler.isProxyCacheSupported())
      localObject = (List)proxyCache.get(str3);
    if (localObject == null)
      try
      {
        localObject = new ArrayList();
        if ((!str2.equals("127.0.0.1")) && (!str2.equals("localhost")))
        {
          ProxyInfo[] arrayOfProxyInfo = handler.getProxyInfo(paramURL);
          for (int i = 0; i < arrayOfProxyInfo.length; i++)
            if (arrayOfProxyInfo[i].isProxyUsed())
              ((List)localObject).add(getProxy(arrayOfProxyInfo[i], paramBoolean));
            else
              ((List)localObject).add(Proxy.NO_PROXY);
        }
        else
        {
          ((List)localObject).add(Proxy.NO_PROXY);
        }
        if (handler.isProxyCacheSupported())
          proxyCache.put(str3.toString(), localObject);
      }
      catch (ProxyUnavailableException localProxyUnavailableException)
      {
        Trace.msgNetPrintln("net.proxy.service.not_available", new Object[] { paramURL });
        ((List)localObject).add(Proxy.NO_PROXY);
      }
    return (List)(List)((ArrayList)localObject).clone();
  }

  private static Proxy getProxy(ProxyInfo paramProxyInfo, boolean paramBoolean)
  {
    Proxy localProxy = null;
    try
    {
      localProxy = (Proxy)AccessController.doPrivileged(new PrivilegedExceptionAction(paramBoolean, paramProxyInfo)
      {
        private final boolean val$isSocketURL;
        private final ProxyInfo val$pi;

        public Object run()
          throws IOException
        {
          if (this.val$isSocketURL)
          {
            if (this.val$pi.isSocksUsed())
              return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(this.val$pi.getSocksProxy(), this.val$pi.getSocksPort()));
            return Proxy.NO_PROXY;
          }
          if ((this.val$pi.getProxy() == null) && (this.val$pi.isSocksUsed()))
            return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(this.val$pi.getSocksProxy(), this.val$pi.getSocksPort()));
          return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.val$pi.getProxy(), this.val$pi.getPort()));
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
    }
    return localProxy;
  }

  public static synchronized void setNoProxy(URL paramURL)
  {
    String str = buildProxyKey(paramURL);
    if (handler.isProxyCacheSupported())
    {
      ArrayList localArrayList = new ArrayList();
      localArrayList.add(Proxy.NO_PROXY);
      proxyCache.put(str, localArrayList);
    }
  }

  private static String buildProxyKey(URL paramURL)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append(paramURL.getProtocol());
    localStringBuffer.append(paramURL.getHost());
    localStringBuffer.append(paramURL.getPort());
    return localStringBuffer.toString();
  }

  protected static synchronized void removeProxyFromCache(URL paramURL, String paramString)
  {
    String str = buildProxyKey(paramURL);
    if (proxyCache.containsKey(str))
    {
      List localList = (List)proxyCache.get(str);
      ListIterator localListIterator = localList.listIterator();
      ArrayList localArrayList = new ArrayList();
      while (localListIterator.hasNext())
      {
        Proxy localProxy = (Proxy)localListIterator.next();
        InetSocketAddress localInetSocketAddress = (InetSocketAddress)localProxy.address();
        if ((localInetSocketAddress != null) && (paramString.contains(localInetSocketAddress.getHostName())))
          localArrayList.add(localProxy);
      }
      localListIterator = localArrayList.listIterator();
      while (localListIterator.hasNext())
        localList.remove(localListIterator.next());
      if (localList.size() == 0)
        proxyCache.remove(str);
    }
  }

  public static synchronized void reset()
  {
    try
    {
      proxyCache.clear();
      UserDefinedProxyConfig localUserDefinedProxyConfig = new UserDefinedProxyConfig();
      BrowserProxyInfo localBrowserProxyInfo1 = localUserDefinedProxyConfig.getBrowserProxyInfo();
      Service localService = ServiceManager.getService();
      Object localObject;
      if (localBrowserProxyInfo1.getType() == 3)
      {
        localObject = localService.getProxyConfig();
        localBrowserProxyInfo1 = ((BrowserProxyConfig)localObject).getBrowserProxyInfo();
      }
      switch (localBrowserProxyInfo1.getType())
      {
      case 4:
        try
        {
          localObject = localService.getSystemProxyHandler();
          if (localObject == null)
            throw new ProxyConfigException("Unable to obtain system proxy handler.");
          handler = (ProxyHandler)localObject;
          handler.init(localBrowserProxyInfo1);
        }
        catch (ProxyConfigException localProxyConfigException1)
        {
          Trace.ignoredException(localProxyConfigException1);
        }
      case 3:
        try
        {
          ProxyHandler localProxyHandler1 = localService.getBrowserProxyHandler();
          if (localProxyHandler1 == null)
            throw new ProxyConfigException("Unable to obtain browser proxy handler.");
          handler = localProxyHandler1;
          handler.init(localBrowserProxyInfo1);
        }
        catch (ProxyConfigException localProxyConfigException2)
        {
          Trace.ignoredException(localProxyConfigException2);
        }
      case 0:
        try
        {
          handler = new DirectProxyHandler();
          handler.init(localBrowserProxyInfo1);
        }
        catch (ProxyConfigException localProxyConfigException3)
        {
          Trace.ignoredException(localProxyConfigException3);
        }
      case 1:
        try
        {
          handler = new ManualProxyHandler();
          handler.init(localBrowserProxyInfo1);
        }
        catch (ProxyConfigException localProxyConfigException4)
        {
          Trace.msgNetPrintln("net.proxy.loading.manual.error");
          try
          {
            localBrowserProxyInfo1.setType(0);
            handler = new DirectProxyHandler();
            handler.init(localBrowserProxyInfo1);
          }
          catch (ProxyConfigException localProxyConfigException6)
          {
            Trace.ignoredException(localProxyConfigException6);
          }
        }
      case 2:
        try
        {
          ProxyHandler localProxyHandler2 = localService.getAutoProxyHandler();
          if (localProxyHandler2 == null)
            throw new ProxyConfigException("Unable to obtain auto proxy handler.");
          handler = localProxyHandler2;
          handler.init(localBrowserProxyInfo1);
        }
        catch (ProxyConfigException localProxyConfigException5)
        {
          Trace.msgNetPrintln("net.proxy.loading.auto.error");
          try
          {
            localBrowserProxyInfo1.setType(1);
            handler = new ManualProxyHandler();
            handler.init(localBrowserProxyInfo1);
          }
          catch (ProxyConfigException localProxyConfigException7)
          {
            Trace.msgNetPrintln("net.proxy.loading.manual.error");
            try
            {
              localBrowserProxyInfo1.setType(0);
              handler = new DirectProxyHandler();
              handler.init(localBrowserProxyInfo1);
            }
            catch (ProxyConfigException localProxyConfigException8)
            {
              Trace.ignoredException(localProxyConfigException8);
            }
          }
        }
      default:
        throw new IllegalStateException("DynamicProxyManager: Invalid Proxy Type");
      }
      try
      {
        String str = System.getProperty("jnlp.cfg.normifactory");
        int i = !"true".equals(str) ? 1 : 0;
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
          DynamicProxyManager.access$000(this.val$info);
          return null;
        }
      });
      if (Trace.isEnabled(TraceLevel.NETWORK))
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
    int i = paramBrowserProxyInfo.getType();
    StringBuffer localStringBuffer = new StringBuffer();
    if (paramBrowserProxyInfo.getHttpHost() != null)
    {
      localStringBuffer.append("http=" + paramBrowserProxyInfo.getHttpHost());
      if (paramBrowserProxyInfo.getHttpPort() != -1)
        localStringBuffer.append(":" + paramBrowserProxyInfo.getHttpPort());
    }
    if (paramBrowserProxyInfo.getHttpsHost() != null)
    {
      localStringBuffer.append(",https=" + paramBrowserProxyInfo.getHttpsHost());
      if (paramBrowserProxyInfo.getHttpsPort() != -1)
        localStringBuffer.append(":" + paramBrowserProxyInfo.getHttpsPort());
    }
    if (paramBrowserProxyInfo.getFtpHost() != null)
    {
      localStringBuffer.append(",ftp=" + paramBrowserProxyInfo.getFtpHost());
      if (paramBrowserProxyInfo.getFtpPort() != -1)
        localStringBuffer.append(":" + paramBrowserProxyInfo.getFtpPort());
    }
    if (paramBrowserProxyInfo.getGopherHost() != null)
    {
      localStringBuffer.append(",gopher=" + paramBrowserProxyInfo.getGopherHost());
      if (paramBrowserProxyInfo.getGopherPort() != -1)
        localStringBuffer.append(":" + paramBrowserProxyInfo.getGopherPort());
    }
    if (paramBrowserProxyInfo.getSocksHost() != null)
    {
      localStringBuffer.append(",socks=" + paramBrowserProxyInfo.getSocksHost());
      if (paramBrowserProxyInfo.getSocksPort() != -1)
        localStringBuffer.append(":" + paramBrowserProxyInfo.getSocksPort());
    }
    String str1 = localStringBuffer.toString();
    String[] arrayOfString = paramBrowserProxyInfo.getOverrides();
    String str2 = null;
    if (arrayOfString != null)
    {
      localStringBuffer = new StringBuffer();
      int j = 1;
      for (int k = 0; k < arrayOfString.length; k++)
      {
        if (k != 0)
          localStringBuffer.append(",");
        localStringBuffer.append(arrayOfString[k]);
      }
      str2 = localStringBuffer.toString();
    }
    localProperties.remove("javaplugin.proxy.config.type");
    localProperties.remove("javaplugin.proxy.config.list");
    localProperties.remove("javaplugin.proxy.config.bypass");
    if (i == 0)
    {
      localProperties.put("javaplugin.proxy.config.type", "direct");
    }
    else if (i == 1)
    {
      localProperties.put("javaplugin.proxy.config.type", "manual");
      if (str1 != null)
        localProperties.put("javaplugin.proxy.config.list", str1);
      if (str2 != null)
        localProperties.put("javaplugin.proxy.config.bypass", str2);
    }
    else if (i == 2)
    {
      localProperties.put("javaplugin.proxy.config.type", "auto");
    }
    else if (i == 3)
    {
      localProperties.put("javaplugin.proxy.config.type", "browser");
      if (str1 != null)
        localProperties.put("javaplugin.proxy.config.list", str1);
      if (str2 != null)
        localProperties.put("javaplugin.proxy.config.bypass", str2);
    }
    else
    {
      localProperties.put("javaplugin.proxy.config.type", "unknown");
    }
    System.setProperties(localProperties);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.DynamicProxyManager
 * JD-Core Version:    0.6.0
 */