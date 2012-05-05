package com.sun.deploy.uitoolkit.impl.awt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.lang.reflect.InvocationTargetException;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

public class OldPluginAWTUtil
{
  public static void postEvent(Component paramComponent, AWTEvent paramAWTEvent)
  {
    SunToolkit.postEvent(SunToolkit.targetToAppContext(paramComponent), paramAWTEvent);
  }

  public static void invokeLater(Component paramComponent, Runnable paramRunnable)
  {
    SunToolkit.postEvent(SunToolkit.targetToAppContext(paramComponent), new InvocationEvent(Toolkit.getDefaultToolkit(), paramRunnable));
  }

  public static void invokeLater(AppContext paramAppContext, Runnable paramRunnable)
  {
    SunToolkit.postEvent(paramAppContext, new InvocationEvent(Toolkit.getDefaultToolkit(), paramRunnable));
  }

  public static void invokeAndWait(Component paramComponent, Runnable paramRunnable)
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
      SunToolkit.postEvent(SunToolkit.targetToAppContext(paramComponent), localInvocationEvent);
      local1AWTInvocationLock.wait();
    }
    ??? = localInvocationEvent.getException();
    if (??? != null)
      throw new InvocationTargetException((Throwable)???);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.awt.OldPluginAWTUtil
 * JD-Core Version:    0.6.0
 */