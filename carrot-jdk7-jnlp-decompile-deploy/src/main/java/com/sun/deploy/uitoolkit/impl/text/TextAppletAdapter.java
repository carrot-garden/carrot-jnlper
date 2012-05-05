package com.sun.deploy.uitoolkit.impl.text;

import com.sun.applet2.Applet2;
import com.sun.applet2.Applet2Context;
import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.Applet2Adapter;
import com.sun.deploy.uitoolkit.Window;
import java.lang.reflect.Constructor;

public class TextAppletAdapter extends Applet2Adapter
{
  Preloader preloader = null;
  private Window parent = null;
  Object applet = null;

  public TextAppletAdapter(Applet2Context paramApplet2Context)
  {
    super(paramApplet2Context);
  }

  public Applet2 getApplet2()
  {
    return (Applet2)this.applet;
  }

  public void init()
  {
    markAlive(true);
    Trace.println("TextAppletAdapter init()", TraceLevel.UI);
    try
    {
      getApplet2().init(getApplet2Context());
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
      throw new RuntimeException("Call to applet2.init() failed", localThrowable);
    }
  }

  public void start()
  {
    markAlive(true);
    Trace.println("TextAppletAdapter start()", TraceLevel.UI);
    getApplet2().start();
  }

  public void stop()
  {
    Trace.println("TextAppletAdapter stop()", TraceLevel.UI);
    getApplet2().stop();
    markAlive(false);
  }

  public void destroy()
  {
    Trace.println("TextAppletAdapter destroy()", TraceLevel.UI);
    getApplet2().destroy();
  }

  public void resize(int paramInt1, int paramInt2)
  {
    Trace.println("TextAppletAdapter resize()", TraceLevel.UI);
  }

  public void doClearAppletArea()
  {
    Trace.println("TextAppletAdapter removeApplet()", TraceLevel.UI);
  }

  public synchronized Preloader getPreloader()
  {
    return this.preloader;
  }

  public synchronized Preloader instantiatePreloader(Class paramClass)
  {
    Trace.println("TextAppletAdapter createCustomProgress " + paramClass, TraceLevel.UI);
    if (paramClass != null)
      try
      {
        Class[] arrayOfClass = { Applet2Context.class };
        Object[] arrayOfObject = { null };
        Constructor localConstructor = null;
        localConstructor = paramClass.getConstructor(arrayOfClass);
        this.preloader = ((Preloader)localConstructor.newInstance(arrayOfObject));
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
    if (this.preloader == null)
      this.preloader = new TextPreloader();
    return this.preloader;
  }

  public void instantiateSerialApplet(ClassLoader paramClassLoader, String paramString)
  {
    Trace.println("Request to load serial applet from " + paramString + ". Noop.");
  }

  public void setParentContainer(Window paramWindow)
  {
    Trace.println("TextAppletAdapter setParentContainer(): " + paramWindow, TraceLevel.UI);
    this.parent = paramWindow;
  }

  public synchronized void instantiateApplet(Class paramClass)
    throws InstantiationException, IllegalAccessException
  {
    Trace.println("TextAppletAdapter instantiateApplet(): " + paramClass, TraceLevel.UI);
    if (null != this.applet)
      throw new IllegalStateException();
    if (!Applet2.class.isAssignableFrom(paramClass))
      throw new IllegalArgumentException();
    this.applet = paramClass.newInstance();
  }

  public void doShowApplet()
  {
    Trace.println("TextAppletAdapter showApplet()", TraceLevel.UI);
  }

  public void doShowPreloader()
  {
    Trace.println("TextAdapter: request to show preloader", TraceLevel.UI);
  }

  public void doShowError(String paramString, Throwable paramThrowable, boolean paramBoolean)
  {
    Trace.println("TextAdapter: request to show error (offerReload=" + paramBoolean + ")", TraceLevel.UI);
    if (paramThrowable != null)
      Trace.ignored(paramThrowable);
  }

  public Object getLiveConnectObject()
  {
    return this.applet;
  }

  public void abort()
  {
    Trace.println("TextAppletAdapter.abort() called");
    this.applet = null;
  }

  public boolean isInstantiated()
  {
    return this.applet != null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.text.TextAppletAdapter
 * JD-Core Version:    0.6.0
 */