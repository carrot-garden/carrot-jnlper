package com.sun.applet2.preloader.event;

public class AppInitEvent extends PreloaderEvent
{
  public static final int ABOUT_TO_LOAD_APPCLASS = 1;
  public static final int ABOUT_TO_CALL_MAIN = 2;
  private int subtype = 0;
  private final String[] labels = { "LoadClass", "CallMain" };

  public AppInitEvent(int paramInt)
  {
    super(4);
    this.subtype = paramInt;
  }

  public int getSubtype()
  {
    return this.subtype;
  }

  public String toString()
  {
    return "AppInitEvent[type=" + this.labels[(this.subtype - 1)] + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.preloader.event.AppInitEvent
 * JD-Core Version:    0.6.0
 */