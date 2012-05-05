package com.sun.javaws;

import com.sun.deploy.config.Platform;
import java.net.URL;

public class UnixBrowserSupport extends BrowserSupport
{
  public String getNS6MailCapInfo()
  {
    return "user_pref(\"helpers.private_mailcap_file\", \"" + System.getProperty("user.home") + "/.mailcap\");\nuser_pref(\"helpers.private_mime_types_file\", \"" + System.getProperty("user.home") + "/.mime.types\");\n";
  }

  public OperaSupport getOperaSupport()
  {
    return new UnixOperaSupport();
  }

  public boolean isWebBrowserSupportedImpl()
  {
    return true;
  }

  public boolean showDocumentImpl(URL paramURL)
  {
    return Platform.get().showDocument(paramURL.toString());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.UnixBrowserSupport
 * JD-Core Version:    0.6.0
 */