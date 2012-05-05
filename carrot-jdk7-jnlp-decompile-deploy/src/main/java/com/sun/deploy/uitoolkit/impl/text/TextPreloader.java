package com.sun.deploy.uitoolkit.impl.text;

import com.sun.applet2.Applet2Context;
import com.sun.applet2.Applet2Host;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.AppletInitEvent;
import com.sun.applet2.preloader.event.PreloaderEvent;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;

public class TextPreloader extends Preloader
{
  boolean hadSwitched = false;

  public Object getOwner()
  {
    return null;
  }

  public boolean handleEvent(PreloaderEvent paramPreloaderEvent)
  {
    Trace.println("TextPreloader handleEvent:");
    Trace.println("TextPreloader got: " + paramPreloaderEvent, TraceLevel.PRELOADER);
    if ((paramPreloaderEvent instanceof AppletInitEvent))
    {
      AppletInitEvent localAppletInitEvent = (AppletInitEvent)paramPreloaderEvent;
      if (((!this.hadSwitched) && ((localAppletInitEvent.getSubtype() == 3) || (localAppletInitEvent.getSubtype() == 4))) || (localAppletInitEvent.getSubtype() == 6))
      {
        this.ctx.getHost().showApplet();
        this.hadSwitched = true;
      }
      return true;
    }
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.text.TextPreloader
 * JD-Core Version:    0.6.0
 */