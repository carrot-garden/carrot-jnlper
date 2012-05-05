package com.sun.deploy.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.OSType;
import java.lang.reflect.Constructor;

public abstract class JavaTrayIcon
{
  private static JavaTrayIcon soleInstance;
  protected JavaTrayIconController controller;

  public static void install(JavaTrayIconController paramJavaTrayIconController)
  {
    if (isSupported())
    {
      if (soleInstance != null)
        throw new IllegalStateException("Already installed");
      JavaTrayIcon localJavaTrayIcon = null;
      String str = OSType.isWin() ? "com.sun.deploy.ui.WindowsJavaTrayIcon" : "com.sun.deploy.ui.MacJavaTrayIcon";
      try
      {
        Class localClass = Class.forName(str, false, null);
        if (localClass != null)
        {
          Constructor localConstructor = localClass.getDeclaredConstructor(new Class[] { JavaTrayIconController.class });
          localJavaTrayIcon = (JavaTrayIcon)localConstructor.newInstance(new Object[] { paramJavaTrayIconController });
        }
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
      if ((localJavaTrayIcon != null) && (isEnabled()))
        localJavaTrayIcon.installImpl();
      soleInstance = localJavaTrayIcon;
    }
  }

  public static void notifyConsoleClosed()
  {
    if (soleInstance != null)
      soleInstance.notifyConsoleClosedImpl();
  }

  protected JavaTrayIcon(JavaTrayIconController paramJavaTrayIconController)
  {
    this.controller = paramJavaTrayIconController;
  }

  protected static boolean isEnabled()
  {
    return Config.getBooleanProperty("deployment.system.tray.icon");
  }

  protected abstract void installImpl();

  protected abstract void notifyConsoleClosedImpl();

  private static boolean isSupported()
  {
    return (OSType.isWin()) || (OSType.isMac());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.JavaTrayIcon
 * JD-Core Version:    0.6.0
 */