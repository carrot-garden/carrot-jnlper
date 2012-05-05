package com.sun.javaws;

import java.awt.Component;

public abstract class ExtensionInstallHandler
{
  private static ExtensionInstallHandler _installHandler;

  public static synchronized ExtensionInstallHandler getInstance()
  {
    if (_installHandler == null)
      _installHandler = ExtensionInstallHandlerFactory.newInstance();
    return _installHandler;
  }

  public abstract boolean doPreRebootActions(Component paramComponent);

  public abstract boolean doReboot();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ExtensionInstallHandler
 * JD-Core Version:    0.6.0
 */