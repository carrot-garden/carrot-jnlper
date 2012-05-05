package com.sun.deploy.net.offline;

import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import java.net.URL;

public class DeployOfflineManager
{
  private static OfflineHandler handler = new NeverOfflineHandler();
  private static boolean forcedOffline = false;

  public static void setForcedOffline(boolean paramBoolean)
  {
    forcedOffline = paramBoolean;
  }

  public static boolean promptUserGoOnline(URL paramURL)
  {
    if ((isGlobalOffline()) && (!isForcedOffline()) && (!askUserGoOnline(paramURL)))
    {
      setForcedOffline(true);
      return false;
    }
    return true;
  }

  public static boolean isForcedOffline()
  {
    return forcedOffline;
  }

  public static void reset()
  {
    Service localService = ServiceManager.getService();
    handler = localService.getOfflineHandler();
    if (handler == null)
      handler = new NeverOfflineHandler();
  }

  public static boolean isGlobalOffline()
  {
    if (forcedOffline)
      return true;
    return handler.isGlobalOffline();
  }

  public static boolean setGlobalOffline(boolean paramBoolean)
  {
    return handler.setGlobalOffline(paramBoolean);
  }

  public static boolean askUserGoOnline(URL paramURL)
  {
    if (forcedOffline)
      return false;
    return handler.askUserGoOnline(paramURL);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.offline.DeployOfflineManager
 * JD-Core Version:    0.6.0
 */