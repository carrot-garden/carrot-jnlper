package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class DeployProxySelector extends ProxySelector
{
  public static void reset()
  {
    DynamicProxyManager.reset();
    ProxySelector.setDefault(new DeployProxySelector());
  }

  protected URL getURLFromURI(URI paramURI, boolean paramBoolean)
  {
    if (paramURI == null)
      return null;
    int i = paramURI.getPort();
    Object localObject1 = paramURI.getHost();
    if (localObject1 == null)
    {
      localObject2 = paramURI.getAuthority();
      if (localObject2 != null)
      {
        int j = ((String)localObject2).indexOf('@');
        if (j >= 0)
          localObject2 = ((String)localObject2).substring(j + 1);
        j = ((String)localObject2).lastIndexOf(':');
        if (j >= 0)
        {
          try
          {
            i = Integer.parseInt(((String)localObject2).substring(j + 1));
          }
          catch (NumberFormatException localNumberFormatException)
          {
            i = -1;
          }
          localObject2 = ((String)localObject2).substring(0, j);
        }
        localObject1 = localObject2;
      }
    }
    Object localObject2 = null;
    try
    {
      String str = paramURI.getScheme();
      if (paramBoolean)
      {
        if (i == -1)
          localObject2 = new URL("http://" + (String)localObject1 + "/");
        else
          localObject2 = new URL("http://" + (String)localObject1 + ":" + i + "/");
      }
      else
        localObject2 = paramURI.toURL();
    }
    catch (MalformedURLException localMalformedURLException)
    {
      localMalformedURLException.printStackTrace();
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      localIllegalArgumentException.printStackTrace();
    }
    return (URL)(URL)localObject2;
  }

  public List select(URI paramURI)
  {
    if (paramURI == null)
      throw new IllegalArgumentException();
    String str = paramURI.getScheme();
    boolean bool = (str.equalsIgnoreCase("socket")) || (str.equalsIgnoreCase("serversocket"));
    URL localURL = getURLFromURI(paramURI, bool);
    List localList = null;
    try
    {
      localList = DynamicProxyManager.getProxyList(localURL, bool);
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
    if (Trace.isEnabled(TraceLevel.NETWORK))
      Trace.msgNetPrintln("net.proxy.connect", new Object[] { paramURI, localList.get(0) });
    return localList;
  }

  public void connectFailed(URI paramURI, SocketAddress paramSocketAddress, IOException paramIOException)
  {
    if ((paramURI == null) || (paramSocketAddress == null) || (paramIOException == null))
      throw new IllegalArgumentException("Arguments can't be null.");
    if (Trace.isEnabled(TraceLevel.NETWORK))
      Trace.msgNetPrintln("net.proxy.connectionFailure", new Object[] { paramURI.toString() + ", " + paramSocketAddress.toString() + paramIOException.toString() });
    try
    {
      DynamicProxyManager.removeProxyFromCache(paramURI.toURL(), paramSocketAddress.toString());
    }
    catch (Exception localException)
    {
      Trace.securityPrintException(localException);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.DeployProxySelector
 * JD-Core Version:    0.6.0
 */