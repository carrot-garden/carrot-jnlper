package com.sun.applet2.preloader.event;

import com.sun.deploy.ui.AppInfo;

public class ConfigEvent extends PreloaderEvent
{
  public static final int STATUS = 1;
  public static final int HEADING = 2;
  public static final int APPINFO = 3;
  public static final int SET_VISIBLE = 4;
  public static final int HIDE_PROGRESSBAR = 5;
  public static final int HIDE_WINDOW = 6;
  private static String[] labels = { "Status", "HEADING", "AppInfo", "SetVisible", "HideProgressBar", "HideWindow" };
  public int config;
  public Object value;

  public String toString()
  {
    return "ConfigEvent[type=" + labels[(this.config - 1)] + ", value=" + this.value + "]";
  }

  public ConfigEvent(int paramInt)
  {
    super(2);
    this.config = paramInt;
    this.value = null;
  }

  public ConfigEvent(int paramInt, boolean paramBoolean)
  {
    super(2);
    this.config = paramInt;
    this.value = Boolean.valueOf(paramBoolean);
  }

  public ConfigEvent(int paramInt, String paramString)
  {
    super(2);
    this.config = paramInt;
    this.value = paramString;
  }

  public ConfigEvent(int paramInt, AppInfo paramAppInfo)
  {
    super(2);
    this.config = paramInt;
    this.value = paramAppInfo;
  }

  public ConfigEvent(int paramInt1, int paramInt2)
  {
    super(2);
    this.config = paramInt1;
    this.value = Integer.valueOf(paramInt2);
  }

  public int getAction()
  {
    return this.config;
  }

  public Object getValue()
  {
    return this.value;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.preloader.event.ConfigEvent
 * JD-Core Version:    0.6.0
 */