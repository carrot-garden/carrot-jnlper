package com.sun.deploy.uitoolkit;

import com.sun.applet2.Applet2;
import com.sun.applet2.Applet2Context;
import com.sun.applet2.Applet2Host;
import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class Applet2Adapter
{
  private final Applet2Context ctx;
  private boolean isAlive = false;
  public static final String PRELOADER_KEY = "preloader_key";
  List cleanupActions = new LinkedList();

  protected Applet2Adapter(Applet2Context paramApplet2Context)
  {
    this.ctx = paramApplet2Context;
  }

  protected Applet2Context getApplet2Context()
  {
    return this.ctx;
  }

  protected Applet2Host getApplet2Host()
  {
    return this.ctx.getHost();
  }

  public abstract void setParentContainer(Window paramWindow);

  public abstract void instantiateApplet(Class paramClass)
    throws InstantiationException, IllegalAccessException;

  public abstract void instantiateSerialApplet(ClassLoader paramClassLoader, String paramString);

  public abstract Object getLiveConnectObject();

  public abstract Applet2 getApplet2();

  public abstract boolean isInstantiated();

  public void cleanup()
  {
    synchronized (this)
    {
      Iterator localIterator = this.cleanupActions.iterator();
      while (localIterator.hasNext())
      {
        Runnable localRunnable = (Runnable)localIterator.next();
        try
        {
          localRunnable.run();
        }
        catch (Exception localException)
        {
          Trace.println("Got exception executing cleanup action", TraceLevel.UI);
          Trace.ignored(localException);
        }
      }
    }
  }

  public synchronized void addCleanupAction(Runnable paramRunnable)
  {
    this.cleanupActions.add(paramRunnable);
  }

  public abstract Preloader instantiatePreloader(Class paramClass);

  public abstract Preloader getPreloader();

  public abstract void init();

  public abstract void start();

  public abstract void stop();

  public abstract void destroy();

  public abstract void abort();

  public abstract void resize(int paramInt1, int paramInt2);

  public abstract void doShowApplet();

  public abstract void doShowPreloader();

  public abstract void doShowError(String paramString, Throwable paramThrowable, boolean paramBoolean);

  public abstract void doClearAppletArea();

  public synchronized boolean isAlive()
  {
    return this.isAlive;
  }

  protected synchronized void markAlive(boolean paramBoolean)
  {
    this.isAlive = paramBoolean;
  }

  public void setAppletAppContext(AppContext paramAppContext)
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.Applet2Adapter
 * JD-Core Version:    0.6.0
 */