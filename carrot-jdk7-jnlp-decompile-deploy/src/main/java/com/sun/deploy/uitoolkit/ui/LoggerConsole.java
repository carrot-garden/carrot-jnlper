package com.sun.deploy.uitoolkit.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggerConsole
  implements ConsoleWindow
{
  private static final Logger LOGGER = Logger.getLogger("DeployConsole");
  private final ConsoleController controller;
  private volatile boolean visible = true;

  public static LoggerConsole create(ConsoleController paramConsoleController)
  {
    return new LoggerConsole(paramConsoleController);
  }

  public LoggerConsole(ConsoleController paramConsoleController)
  {
    this.controller = paramConsoleController;
  }

  public void clear()
  {
    LOGGER.log(Level.INFO, "CLEAR");
    ConsoleHelper.displayVersion(this.controller, this);
    append("\n");
    ConsoleHelper.displayHelp(this.controller, this);
  }

  public void append(String paramString)
  {
    LOGGER.log(Level.FINE, paramString);
  }

  public void setVisible(boolean paramBoolean)
  {
    if (this.controller.isIconifiedOnClose())
      LOGGER.log(Level.INFO, paramBoolean ? "Visible" : "Iconified");
    else
      LOGGER.log(Level.INFO, paramBoolean ? "Visible" : "Invisible");
    this.visible = paramBoolean;
  }

  public boolean isVisible()
  {
    return this.visible;
  }

  public void dispose()
  {
    LOGGER.log(Level.INFO, "Disposed");
  }

  public void setTitle(String paramString)
  {
    LOGGER.log(Level.CONFIG, "Console Title : {0}", paramString);
  }

  public String getRecentLog()
  {
    return "";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.ui.LoggerConsole
 * JD-Core Version:    0.6.0
 */