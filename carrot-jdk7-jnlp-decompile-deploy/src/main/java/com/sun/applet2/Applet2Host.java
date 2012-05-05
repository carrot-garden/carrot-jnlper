package com.sun.applet2;

import java.net.URL;

public abstract interface Applet2Host
{
  public abstract URL getDocumentBase();

  public abstract void showDocument(URL paramURL);

  public abstract void showDocument(URL paramURL, String paramString);

  public abstract void showApplet();

  public abstract void showError(String paramString, Throwable paramThrowable, boolean paramBoolean);

  public abstract void reloadAppletPage();

  public abstract Object getWindow();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.Applet2Host
 * JD-Core Version:    0.6.0
 */