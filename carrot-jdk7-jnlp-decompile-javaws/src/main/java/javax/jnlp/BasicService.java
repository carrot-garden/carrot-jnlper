package javax.jnlp;

import java.net.URL;

public abstract interface BasicService
{
  public abstract URL getCodeBase();

  public abstract boolean isOffline();

  public abstract boolean showDocument(URL paramURL);

  public abstract boolean isWebBrowserSupported();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.BasicService
 * JD-Core Version:    0.6.0
 */