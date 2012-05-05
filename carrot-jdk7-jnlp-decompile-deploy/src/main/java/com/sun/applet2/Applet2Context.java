package com.sun.applet2;

import com.sun.applet2.preloader.Preloader;
import java.net.URL;

public abstract interface Applet2Context
{
  public abstract String getName();

  public abstract int getHeight();

  public abstract int getWidth();

  public abstract AppletParameters getParameters();

  public abstract String getParameter(String paramString);

  public abstract boolean isActive();

  public abstract URL getCodeBase();

  public abstract Applet2Host getHost();

  public abstract Preloader getPreloader();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.Applet2Context
 * JD-Core Version:    0.6.0
 */