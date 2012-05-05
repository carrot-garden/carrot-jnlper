package com.sun.applet2.preloader.event;

import com.sun.applet2.Applet2;

public class AppletInitEvent extends PreloaderEvent
{
  public static final int ABOUT_TO_LOAD_APPCLASS = 1;
  public static final int ABOUT_TO_CALL_CONSTRUCTOR = 2;
  public static final int ABOUT_TO_CALL_APPLET_INIT = 3;
  public static final int ABOUT_TO_CALL_APPLET_START = 4;
  public static final int ABOUT_TO_LAUNCH_INSTALLER = 5;
  public static final int ABOUT_TO_REUSE_APPLET = 6;
  private int subtype = 0;
  private Applet2 applet = null;
  private final String[] labels = { "LoadClass", "CallConstructor", "CallInit", "CallStart", "LaunchInstaller", "ReuseApplet" };

  public AppletInitEvent(int paramInt, Applet2 paramApplet2)
  {
    super(5);
    this.subtype = paramInt;
    this.applet = paramApplet2;
  }

  public Applet2 getApplet()
  {
    return this.applet;
  }

  public int getSubtype()
  {
    return this.subtype;
  }

  public String toString()
  {
    return "AppletInitEvent[type=" + this.labels[(this.subtype - 1)] + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.preloader.event.AppletInitEvent
 * JD-Core Version:    0.6.0
 */