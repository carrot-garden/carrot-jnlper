package com.sun.javaws;

import java.net.URL;

public abstract class BrowserSupport
{
  private static BrowserSupport _browserSupportImplementation = null;

  public static synchronized BrowserSupport getInstance()
  {
    if (_browserSupportImplementation == null)
      _browserSupportImplementation = BrowserSupportFactory.newInstance();
    return _browserSupportImplementation;
  }

  public static boolean isWebBrowserSupported()
  {
    return getInstance().isWebBrowserSupportedImpl();
  }

  public static boolean showDocument(URL paramURL)
  {
    return getInstance().showDocumentImpl(paramURL);
  }

  public abstract boolean isWebBrowserSupportedImpl();

  public abstract boolean showDocumentImpl(URL paramURL);

  public abstract String getNS6MailCapInfo();

  public abstract OperaSupport getOperaSupport();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.BrowserSupport
 * JD-Core Version:    0.6.0
 */