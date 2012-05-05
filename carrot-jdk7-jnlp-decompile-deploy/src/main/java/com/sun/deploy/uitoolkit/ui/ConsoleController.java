package com.sun.deploy.uitoolkit.ui;

public abstract interface ConsoleController
{
  public abstract boolean isIconifiedOnClose();

  public abstract boolean isDumpStackSupported();

  public abstract ThreadGroup getMainThreadGroup();

  public abstract boolean isSecurityPolicyReloadSupported();

  public abstract void reloadSecurityPolicy();

  public abstract boolean isProxyConfigReloadSupported();

  public abstract void reloadProxyConfig();

  public abstract boolean isDumpClassLoaderSupported();

  public abstract String dumpClassLoaders();

  public abstract boolean isClearClassLoaderSupported();

  public abstract void clearClassLoaders();

  public abstract boolean isLoggingSupported();

  public abstract boolean toggleLogging();

  public abstract boolean isJCovSupported();

  public abstract boolean dumpJCovData();

  public abstract String getProductName();

  public abstract void notifyConsoleClosed();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.ui.ConsoleController
 * JD-Core Version:    0.6.0
 */