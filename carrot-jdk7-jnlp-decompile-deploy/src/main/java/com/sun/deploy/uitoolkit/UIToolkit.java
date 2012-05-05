package com.sun.deploy.uitoolkit;

import com.sun.applet2.Applet2Context;
import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.uitoolkit.ui.UIFactory;

public abstract class UIToolkit
{
  public abstract void init()
    throws Exception;

  public abstract void dispose()
    throws Exception;

  public abstract UIFactory getUIFactory();

  public abstract WindowFactory getWindowFactory();

  public abstract Preloader getDefaultPreloader();

  public abstract boolean isHeadless();

  public abstract void setContextClassLoader(ClassLoader paramClassLoader);

  public abstract void warmup();

  public abstract UIToolkit changeMode(int paramInt);

  public abstract Applet2Adapter getApplet2Adapter(Applet2Context paramApplet2Context);

  public abstract AppContext getAppContext();

  public abstract AppContext createAppContext();

  public abstract SecurityManager getSecurityManager();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.UIToolkit
 * JD-Core Version:    0.6.0
 */