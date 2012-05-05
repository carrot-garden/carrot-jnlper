package com.sun.deploy.uitoolkit.impl.awt;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InvocationEvent;
import java.lang.reflect.InvocationTargetException;
import sun.awt.SunToolkit;

public class AWTAppContext
  implements com.sun.deploy.appcontext.AppContext
{
  private static final String DEPLOY_AWT_APPCONTEXT_KEY = "DEPLOY_AWT_APPCONTEXT";
  private sun.awt.AppContext awtAppContext = null;

  private AWTAppContext(sun.awt.AppContext paramAppContext)
  {
    this.awtAppContext = paramAppContext;
  }

  public boolean equals(Object paramObject)
  {
    if ((paramObject instanceof AWTAppContext))
      return this == (AWTAppContext)paramObject;
    return false;
  }

  public static synchronized AWTAppContext getInstance()
  {
    sun.awt.AppContext localAppContext = sun.awt.AppContext.getAppContext();
    AWTAppContext localAWTAppContext = (AWTAppContext)localAppContext.get("DEPLOY_AWT_APPCONTEXT");
    if (localAWTAppContext == null)
    {
      localAWTAppContext = new AWTAppContext(localAppContext);
      localAppContext.put("DEPLOY_AWT_APPCONTEXT", localAWTAppContext);
    }
    return localAWTAppContext;
  }

  public static AWTAppContext createAppContext()
  {
    sun.awt.AppContext localAppContext = SunToolkit.createNewAppContext();
    AWTAppContext localAWTAppContext = new AWTAppContext(localAppContext);
    localAppContext.put("DEPLOY_AWT_APPCONTEXT", localAWTAppContext);
    return localAWTAppContext;
  }

  public Object get(Object paramObject)
  {
    return this.awtAppContext.get(paramObject);
  }

  public Object put(Object paramObject1, Object paramObject2)
  {
    return this.awtAppContext.put(paramObject1, paramObject2);
  }

  public Object remove(Object paramObject)
  {
    return this.awtAppContext.remove(paramObject);
  }

  public sun.awt.AppContext getAWTAppContext()
  {
    return this.awtAppContext;
  }

  public void invokeLater(Runnable paramRunnable)
  {
    SunToolkit.postEvent(this.awtAppContext, new InvocationEvent(Toolkit.getDefaultToolkit(), paramRunnable));
  }

  public void invokeAndWait(Runnable paramRunnable)
    throws InterruptedException, InvocationTargetException
  {
    if (EventQueue.isDispatchThread())
      throw new Error("Cannot call invokeAndWait from the event dispatcher thread");
    1AWTInvocationLock local1AWTInvocationLock = new Object()
    {
    };
    InvocationEvent localInvocationEvent = new InvocationEvent(Toolkit.getDefaultToolkit(), paramRunnable, local1AWTInvocationLock, true);
    synchronized (local1AWTInvocationLock)
    {
      SunToolkit.postEvent(this.awtAppContext, localInvocationEvent);
      local1AWTInvocationLock.wait();
    }
    ??? = localInvocationEvent.getException();
    if (??? != null)
      throw new InvocationTargetException((Throwable)???);
  }

  public ThreadGroup getThreadGroup()
  {
    return this.awtAppContext.getThreadGroup();
  }

  public void dispose()
  {
    this.awtAppContext.dispose();
  }

  public boolean destroy(long paramLong)
  {
    ThreadGroup localThreadGroup = this.awtAppContext.getThreadGroup();
    Window[][] arrayOfWindow; = new Window[1][];
    Object localObject1 = new Object();
    Thread localThread = new Thread(localThreadGroup, new Runnable(arrayOfWindow;, localObject1)
    {
      private final Window[][] val$windowBox;
      private final Object val$enumeratorLock;

      public void run()
      {
        this.val$windowBox[0] = (Config.isJavaVersionAtLeast16() ? Window.getOwnerlessWindows() : Frame.getFrames());
        synchronized (this.val$enumeratorLock)
        {
          this.val$enumeratorLock.notifyAll();
        }
      }
    }
    , "Window enumerator");
    synchronized (localObject1)
    {
      localThread.start();
      try
      {
        localObject1.wait(paramLong);
      }
      catch (InterruptedException localInterruptedException1)
      {
      }
    }
    ??? = arrayOfWindow;[0];
    AppContextDisposer localAppContextDisposer = new AppContextDisposer(this.awtAppContext, null);
    new Thread(localAppContextDisposer).start();
    synchronized (localAppContextDisposer)
    {
      try
      {
        localAppContextDisposer.wait(paramLong);
      }
      catch (InterruptedException localInterruptedException2)
      {
      }
    }
    if (??? != null)
      for (int i = 0; i < ???.length; i++)
      {
        if (!???[i].isDisplayable())
          continue;
        Trace.println("Plugin2Manager calling stopFailed() because of displayable window " + ???[i]);
        return false;
      }
    return true;
  }

  private class AppContextDisposer
    implements Runnable
  {
    private sun.awt.AppContext appContext;
    private boolean completed;
    private final AWTAppContext this$0;

    private AppContextDisposer(sun.awt.AppContext arg2)
    {
      this.this$0 = this$1;
      this.completed = false;
      Object localObject;
      this.appContext = localObject;
    }

    public synchronized boolean isFailed()
    {
      return !this.completed;
    }

    public synchronized void run()
    {
      try
      {
        this.appContext.dispose();
        this.completed = true;
      }
      catch (Exception localException)
      {
        Trace.println("Plugin2Manager calling stopFailed() because of exception during AppContext.dispose()");
      }
      catch (Throwable localThrowable)
      {
        Trace.ignored(localThrowable);
      }
      notifyAll();
    }

    AppContextDisposer(sun.awt.AppContext param1, AWTAppContext.1 arg3)
    {
      this(param1);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.awt.AWTAppContext
 * JD-Core Version:    0.6.0
 */