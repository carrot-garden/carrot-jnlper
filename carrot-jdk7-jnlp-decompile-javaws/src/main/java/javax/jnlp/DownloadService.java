package javax.jnlp;

import java.io.IOException;
import java.net.URL;

public abstract interface DownloadService
{
  public abstract boolean isResourceCached(URL paramURL, String paramString);

  public abstract boolean isPartCached(String paramString);

  public abstract boolean isPartCached(String[] paramArrayOfString);

  public abstract boolean isExtensionPartCached(URL paramURL, String paramString1, String paramString2);

  public abstract boolean isExtensionPartCached(URL paramURL, String paramString, String[] paramArrayOfString);

  public abstract void loadResource(URL paramURL, String paramString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException;

  public abstract void loadPart(String paramString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException;

  public abstract void loadPart(String[] paramArrayOfString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException;

  public abstract void loadExtensionPart(URL paramURL, String paramString1, String paramString2, DownloadServiceListener paramDownloadServiceListener)
    throws IOException;

  public abstract void loadExtensionPart(URL paramURL, String paramString, String[] paramArrayOfString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException;

  public abstract void removeResource(URL paramURL, String paramString)
    throws IOException;

  public abstract void removePart(String paramString)
    throws IOException;

  public abstract void removePart(String[] paramArrayOfString)
    throws IOException;

  public abstract void removeExtensionPart(URL paramURL, String paramString1, String paramString2)
    throws IOException;

  public abstract void removeExtensionPart(URL paramURL, String paramString, String[] paramArrayOfString)
    throws IOException;

  public abstract DownloadServiceListener getDefaultProgressWindow();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.DownloadService
 * JD-Core Version:    0.6.0
 */