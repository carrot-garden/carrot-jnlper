package com.sun.applet2.preloader.event;

public class InitEvent extends PreloaderEvent
{
  public static final int TYPE_JREINSTALL = 0;
  public static final int TYPE_DOWNLOADRESOURCE = 1;
  public static final int TYPE_LAUNCH_INSTALLER = 2;
  public static final int TYPE_INSTALLER_COMPLETE = 3;
  public static final int TYPE_LAUNCH_APP = 4;
  public static final int TYPE_IMPORT_COMPLETE = 5;
  private int _type;
  private static String[] labels = { "JRE", "Download", "Installer", "Install_Complete", "App", "Import" };

  public InitEvent(int paramInt)
  {
    super(1);
    this._type = paramInt;
  }

  public int getInitType()
  {
    return this._type;
  }

  public String toString()
  {
    return "InitEvent[type=" + labels[this._type] + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.preloader.event.InitEvent
 * JD-Core Version:    0.6.0
 */