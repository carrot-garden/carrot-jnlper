package com.sun.javaws.util;

import com.sun.deploy.config.Config;
import com.sun.deploy.net.proxy.DynamicProxyManager;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.uitoolkit.ui.ConsoleController;
import com.sun.deploy.uitoolkit.ui.ConsoleWindow;
import com.sun.javaws.Main;
import java.util.logging.Logger;

public class JavawsConsoleController
  implements ConsoleController
{
  private ConsoleWindow console = null;
  private static JavawsConsoleController jcc = null;

  public static synchronized JavawsConsoleController getInstance()
  {
    if (jcc == null)
      if (Config.isJavaVersionAtLeast14())
        jcc = new JavawsConsoleController14();
      else
        jcc = new JavawsConsoleController();
    return jcc;
  }

  public void setLogger(Logger paramLogger)
  {
  }

  public void setConsole(ConsoleWindow paramConsoleWindow)
  {
    if (this.console == null)
      this.console = paramConsoleWindow;
  }

  public ConsoleWindow getConsole()
  {
    return this.console;
  }

  public boolean isIconifiedOnClose()
  {
    return false;
  }

  public boolean isDumpStackSupported()
  {
    return false;
  }

  public ThreadGroup getMainThreadGroup()
  {
    return Main.getSecurityThreadGroup().getParent();
  }

  public boolean isSecurityPolicyReloadSupported()
  {
    return false;
  }

  public void reloadSecurityPolicy()
  {
  }

  public boolean isProxyConfigReloadSupported()
  {
    return true;
  }

  public void reloadProxyConfig()
  {
    DynamicProxyManager.reset();
  }

  public boolean isDumpClassLoaderSupported()
  {
    return false;
  }

  public String dumpClassLoaders()
  {
    return "Dump class loaders is unavailable.\n";
  }

  public boolean isClearClassLoaderSupported()
  {
    return false;
  }

  public void clearClassLoaders()
  {
  }

  public boolean isLoggingSupported()
  {
    return false;
  }

  public boolean toggleLogging()
  {
    return false;
  }

  public boolean isJCovSupported()
  {
    return false;
  }

  public boolean dumpJCovData()
  {
    return false;
  }

  public String getProductName()
  {
    return ResourceManager.getString("product.javaws.name", "10.0.0.20");
  }

  public void notifyConsoleClosed()
  {
  }

  public void showConsoleIfEnabled()
  {
    if ((Config.getStringProperty("deployment.console.startup.mode").equals("SHOW")) && (this.console != null))
      this.console.setVisible(true);
  }

  public void setTitle(String paramString1, String paramString2)
  {
    if (this.console != null)
      this.console.setTitle(ResourceManager.getMessage(paramString1) + paramString2);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.util.JavawsConsoleController
 * JD-Core Version:    0.6.0
 */