package com.sun.deploy.uitoolkit.impl.text;

import com.sun.applet2.Applet2Context;
import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.Applet2Adapter;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.deploy.uitoolkit.WindowFactory;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.deploy.util.ReflectionUtil;

public class TextUIToolkit extends UIToolkit
{
  UIFactory textbaseuifactory = new TextUIFactory();
  Preloader textbasepreloader = new TextPreloader();

  public AppContext getAppContext()
  {
    return FXAppContext.getInstance();
  }

  public AppContext createAppContext()
  {
    return FXAppContext.getInstance();
  }

  public UIToolkit changeMode(int paramInt)
  {
    return this;
  }

  public void init()
    throws Exception
  {
    Trace.println("TextUIToolkit init()");
  }

  public void dispose()
    throws Exception
  {
    Trace.println("TextUIToolkit dispose()");
  }

  public UIFactory getUIFactory()
  {
    Trace.println("TextUIToolkit getUIFactory()");
    return this.textbaseuifactory;
  }

  public WindowFactory getWindowFactory()
  {
    Trace.println("TextUIToolkit getWindowFactory()");
    return new TextWindowFactory();
  }

  public Preloader getDefaultPreloader()
  {
    Trace.println("TextUIToolkit getDefaultPreloader()");
    return this.textbasepreloader;
  }

  public boolean isHeadless()
  {
    Trace.println("TextUIToolkit isHeadLess() return false");
    return false;
  }

  public void setContextClassLoader(ClassLoader paramClassLoader)
  {
    Trace.println("TextUIToolkit setContextClassLoader()");
  }

  public void warmup()
  {
    Trace.println("TextUIToolkit warmup()");
  }

  public Applet2Adapter getApplet2Adapter(Applet2Context paramApplet2Context)
  {
    Trace.println("TextUIToolkit getApplet2Adapter()");
    return new TextAppletAdapter(paramApplet2Context);
  }

  public SecurityManager getSecurityManager()
  {
    SecurityManager localSecurityManager = null;
    try
    {
      localSecurityManager = (SecurityManager)(SecurityManager)ReflectionUtil.createInstance("sun.plugin2.applet.FXAppletSecurityManager", Thread.currentThread().getContextClassLoader());
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    return localSecurityManager;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.text.TextUIToolkit
 * JD-Core Version:    0.6.0
 */