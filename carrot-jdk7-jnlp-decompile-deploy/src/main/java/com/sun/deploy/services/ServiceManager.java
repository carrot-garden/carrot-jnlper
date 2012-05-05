package com.sun.deploy.services;

import com.sun.deploy.trace.Trace;

public class ServiceManager
{
  private static Service service = new DefaultService();

  public static synchronized Service getService()
  {
    return service;
  }

  public static synchronized void setService(int paramInt)
  {
    Class localClass = DefaultService.class;
    try
    {
      if (paramInt == 16640)
        localClass = Class.forName("com.sun.deploy.services.WPlatformService14");
      if (paramInt == 20480)
        localClass = Class.forName("com.sun.deploy.services.MPlatformService14");
      if (paramInt == 33024)
        localClass = Class.forName("com.sun.deploy.services.WPlatformService");
      if (paramInt == 36864)
        localClass = Class.forName("com.sun.deploy.services.MPlatformService");
      if (paramInt == 40960)
        localClass = Class.forName("com.sun.deploy.services.MacOSXPlatformService");
      if (paramInt == 257)
        localClass = Class.forName("sun.plugin.services.WIExplorerBrowserService");
      else if (paramInt == 258)
        localClass = Class.forName("sun.plugin.services.WNetscape4BrowserService");
      else if (paramInt == 259)
        localClass = Class.forName("sun.plugin.services.WNetscape6BrowserService");
      else if (paramInt == 4098)
        localClass = Class.forName("sun.plugin.services.MNetscape4BrowserService");
      else if (paramInt == 4099)
        localClass = Class.forName("sun.plugin.services.MNetscape6BrowserService");
      else if (paramInt == 5)
        localClass = Class.forName("sun.plugin.services.AxBridgeBrowserService");
      service = (Service)localClass.newInstance();
    }
    catch (Throwable localThrowable)
    {
      Trace.printException(localThrowable);
    }
  }

  public static synchronized void setService(Service paramService)
  {
    service = paramService;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.services.ServiceManager
 * JD-Core Version:    0.6.0
 */