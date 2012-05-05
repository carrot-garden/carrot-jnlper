package com.sun.deploy;

import com.sun.deploy.net.cookie.DeployCookieSelector;
import com.sun.deploy.net.proxy.DeployProxySelector;
import com.sun.deploy.net.proxy.StaticProxyManager;
import com.sun.deploy.security.DeployAuthenticator;
import com.sun.deploy.services.ServiceManager;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Authenticator;
import java.util.Properties;

public class ClientContainer
{
  public static void main(String[] paramArrayOfString)
  {
    if (paramArrayOfString.length < 1)
    {
      System.err.println("Usage: ClientContainer class-name [args ...]");
      System.exit(1);
    }
    try
    {
      String str1 = System.getProperty("os.name");
      if (str1.indexOf("Windows") != -1)
        ServiceManager.setService(33024);
      else if (str1.startsWith("Mac"))
        ServiceManager.setService(40960);
      else
        ServiceManager.setService(36864);
      try
      {
        DeployProxySelector.reset();
        DeployCookieSelector.reset();
      }
      catch (Throwable localThrowable2)
      {
        StaticProxyManager.reset();
      }
      Properties localProperties = System.getProperties();
      String str2 = (String)localProperties.get("java.protocol.handler.pkgs");
      if (str2 != null)
        localProperties.put("java.protocol.handler.pkgs", str2 + "|com.sun.deploy.net.protocol");
      else
        localProperties.put("java.protocol.handler.pkgs", "com.sun.deploy.net.protocol");
      System.setProperties(localProperties);
      Authenticator.setDefault(new DeployAuthenticator());
      Class localClass = Class.forName(paramArrayOfString[0]);
      Class[] arrayOfClass = { new String[0].getClass() };
      Method localMethod = localClass.getMethod("main", arrayOfClass);
      if (!Modifier.isStatic(localMethod.getModifiers()))
        throw new NoSuchMethodException("Cannot find main-method.");
      localMethod.setAccessible(true);
      String[] arrayOfString = new String[paramArrayOfString.length - 1];
      for (int i = 0; i < arrayOfString.length; i++)
        arrayOfString[i] = paramArrayOfString[(i + 1)];
      Object[] arrayOfObject = { arrayOfString };
      localMethod.invoke(null, arrayOfObject);
    }
    catch (Throwable localThrowable1)
    {
      localThrowable1.printStackTrace();
      System.exit(2);
    }
    System.exit(0);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ClientContainer
 * JD-Core Version:    0.6.0
 */