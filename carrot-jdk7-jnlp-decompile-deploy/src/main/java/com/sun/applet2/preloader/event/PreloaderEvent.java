package com.sun.applet2.preloader.event;

public class PreloaderEvent
{
  public static final int INIT_EVENT = 1;
  public static final int CONFIG_EVENT = 2;
  public static final int DOWNLOAD_EVENT = 3;
  public static final int APPINIT_EVENT = 4;
  public static final int APPLETINIT_EVENT = 5;
  public static final int ERROR_EVENT = 6;
  public static final int USERDECLINED_EVENT = 7;
  public static final int CACHEVIEW_EVENT = 100;
  int type = -1;

  public PreloaderEvent(int paramInt)
  {
    this.type = paramInt;
  }

  public int getType()
  {
    return this.type;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.preloader.event.PreloaderEvent
 * JD-Core Version:    0.6.0
 */