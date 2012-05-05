package com.sun.applet2.preloader;

import com.sun.applet2.Applet2Context;
import com.sun.applet2.preloader.event.PreloaderEvent;

public abstract class Preloader
{
  protected Applet2Context ctx;

  public Preloader()
  {
  }

  public Preloader(Applet2Context paramApplet2Context)
  {
    this.ctx = paramApplet2Context;
  }

  public abstract Object getOwner();

  public abstract boolean handleEvent(PreloaderEvent paramPreloaderEvent)
    throws CancelException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.preloader.Preloader
 * JD-Core Version:    0.6.0
 */