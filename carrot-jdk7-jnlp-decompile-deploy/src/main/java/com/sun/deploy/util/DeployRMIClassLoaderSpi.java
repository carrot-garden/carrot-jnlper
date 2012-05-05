package com.sun.deploy.util;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.security.AccessController;
import java.security.PrivilegedAction;

public abstract class DeployRMIClassLoaderSpi extends RMIClassLoaderSpi
{
  protected static RMIClassLoaderSpi getDefaultProviderInstance()
  {
    return (RMIClassLoaderSpi)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return RMIClassLoader.getDefaultProviderInstance();
      }
    });
  }

  public Class loadProxyClass(String paramString, String[] paramArrayOfString, ClassLoader paramClassLoader)
    throws MalformedURLException, ClassNotFoundException
  {
    return getDefaultProviderInstance().loadProxyClass(paramString, paramArrayOfString, paramClassLoader);
  }

  public Class loadClass(String paramString1, String paramString2, ClassLoader paramClassLoader)
    throws MalformedURLException, ClassNotFoundException
  {
    return getDefaultProviderInstance().loadClass(paramString1, paramString2, paramClassLoader);
  }

  public ClassLoader getClassLoader(String paramString)
    throws MalformedURLException
  {
    return getDefaultProviderInstance().getClassLoader(paramString);
  }

  public String getClassAnnotation(Class paramClass)
  {
    if (useRMIServerCodebaseForClass(paramClass))
      return (String)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          return System.getProperty("java.rmi.server.codebase");
        }
      });
    return getDefaultProviderInstance().getClassAnnotation(paramClass);
  }

  protected abstract boolean useRMIServerCodebaseForClass(Class paramClass);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.DeployRMIClassLoaderSpi
 * JD-Core Version:    0.6.0
 */