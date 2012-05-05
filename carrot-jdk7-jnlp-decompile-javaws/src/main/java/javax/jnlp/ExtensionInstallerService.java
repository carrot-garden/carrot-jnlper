package javax.jnlp;

import java.net.URL;

public abstract interface ExtensionInstallerService
{
  public abstract String getInstallPath();

  public abstract String getExtensionVersion();

  public abstract URL getExtensionLocation();

  public abstract void hideProgressBar();

  public abstract void hideStatusWindow();

  public abstract void setHeading(String paramString);

  public abstract void setStatus(String paramString);

  public abstract void updateProgress(int paramInt);

  public abstract void installSucceeded(boolean paramBoolean);

  public abstract void installFailed();

  public abstract void setJREInfo(String paramString1, String paramString2);

  public abstract void setNativeLibraryInfo(String paramString);

  public abstract String getInstalledJRE(URL paramURL, String paramString);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.ExtensionInstallerService
 * JD-Core Version:    0.6.0
 */