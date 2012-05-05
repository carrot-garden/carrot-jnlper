package com.sun.javaws.util;

import com.sun.deploy.uitoolkit.ui.ConsoleController14;
import java.security.Policy;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavawsConsoleController14 extends JavawsConsoleController
  implements ConsoleController14
{
  private static Logger logger = null;

  public void setLogger(Logger paramLogger)
  {
    if (logger == null)
      logger = paramLogger;
  }

  public Logger getLogger()
  {
    return logger;
  }

  public boolean isSecurityPolicyReloadSupported()
  {
    return true;
  }

  public void reloadSecurityPolicy()
  {
    Policy localPolicy = Policy.getPolicy();
    localPolicy.refresh();
  }

  public boolean isLoggingSupported()
  {
    return true;
  }

  public boolean toggleLogging()
  {
    if (logger != null)
    {
      Level localLevel = logger.getLevel();
      if (localLevel == Level.OFF)
        localLevel = Level.ALL;
      else
        localLevel = Level.OFF;
      logger.setLevel(localLevel);
      return localLevel == Level.ALL;
    }
    return false;
  }

  public boolean isDumpStackSupported()
  {
    return true;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.util.JavawsConsoleController14
 * JD-Core Version:    0.6.0
 */