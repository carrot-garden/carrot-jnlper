package com.sun.deploy.uitoolkit.impl.awt;

import com.sun.applet2.Applet2Context;
import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.Applet2Adapter;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.deploy.uitoolkit.WindowFactory;
import com.sun.deploy.uitoolkit.impl.awt.ui.DownloadWindow;
import com.sun.deploy.uitoolkit.impl.awt.ui.UIFactoryImpl;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.deploy.util.ReflectionUtil;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

public class UIToolkitImpl extends UIToolkit
{
  private final UIFactory uiFactory = new UIFactoryImpl();
  private AppContext appContext;

  public AppContext getAppContext()
  {
    return AWTAppContext.getInstance();
  }

  public AppContext createAppContext()
  {
    return AWTAppContext.createAppContext();
  }

  public void init()
    throws Exception
  {
  }

  public void dispose()
    throws Exception
  {
  }

  public UIFactory getUIFactory()
  {
    return this.uiFactory;
  }

  public boolean isDisposed(Object paramObject)
  {
    Window localWindow = (Window)paramObject;
    return !localWindow.isDisplayable();
  }

  public WindowFactory getWindowFactory()
  {
    return new AWTWindowFactory();
  }

  public Preloader getDefaultPreloader()
  {
    Trace.println("AWT UIToolkit get Downloadwindow as preloader ", TraceLevel.UI);
    return new DownloadWindow();
  }

  public boolean isHeadless()
  {
    if (!Config.isJavaVersionAtLeast14())
      return false;
    return GraphicsEnvironment.isHeadless();
  }

  public void setContextClassLoader(ClassLoader paramClassLoader)
  {
    if (paramClassLoader == null)
      return;
    try
    {
      SwingUtilities.invokeAndWait(new Runnable(paramClassLoader)
      {
        private final ClassLoader val$cl;

        public void run()
        {
          try
          {
            Thread.currentThread().setContextClassLoader(this.val$cl);
          }
          catch (Throwable localThrowable)
          {
            Trace.ignored(localThrowable);
          }
        }
      });
    }
    catch (InterruptedException localInterruptedException)
    {
      Trace.ignoredException(localInterruptedException);
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      Trace.ignoredException(localInvocationTargetException);
    }
  }

  public void warmup()
  {
    Toolkit.getDefaultToolkit();
    getAppContext().invokeLater(new Runnable()
    {
      public void run()
      {
      }
    });
  }

  public Applet2Adapter getApplet2Adapter(Applet2Context paramApplet2Context)
  {
    try
    {
      return (Applet2Adapter)ReflectionUtil.createInstance("com.sun.deploy.uitoolkit.impl.awt.AWTAppletAdapter", new Class[] { Applet2Context.class }, new Object[] { paramApplet2Context }, null);
    }
    catch (Exception localException)
    {
    }
    throw new RuntimeException(localException);
  }

  public SecurityManager getSecurityManager()
  {
    SecurityManager localSecurityManager = null;
    try
    {
      localSecurityManager = (SecurityManager)(SecurityManager)ReflectionUtil.createInstance("sun.plugin2.applet.AWTAppletSecurityManager", null);
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    return localSecurityManager;
  }

  public UIToolkit changeMode(int paramInt)
  {
    Object localObject = null;
    UIToolkit localUIToolkit = null;
    try
    {
      if (paramInt == 1)
      {
        localUIToolkit = (UIToolkit)ReflectionUtil.createInstance("com.sun.deploy.uitoolkit.impl.awt.AWTPluginUIToolkit", new Class[] { UIToolkitImpl.class }, new Object[] { this }, null);
      }
      else if (paramInt == 0)
      {
        localUIToolkit = (UIToolkit)ReflectionUtil.createInstance("com.sun.deploy.uitoolkit.impl.awt.UIToolkitImpl", null);
        ((UIToolkitImpl)localUIToolkit).appContext = this.appContext;
      }
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    if (localUIToolkit != null)
      return localUIToolkit;
    Trace.println("toolkit didn't switch to new mode successfully", TraceLevel.UI);
    return this;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.awt.UIToolkitImpl
 * JD-Core Version:    0.6.0
 */