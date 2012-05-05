package com.sun.deploy.net;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class FailedDownloadException extends DownloadException
{
  private boolean _offline = false;

  public FailedDownloadException(URL paramURL, String paramString, Exception paramException)
  {
    super(paramURL, paramString, paramException);
  }

  public FailedDownloadException(URL paramURL, String paramString, Exception paramException, boolean paramBoolean)
  {
    super(paramURL, paramString, paramException);
    this._offline = true;
  }

  public String getRealMessage()
  {
    if (this._offline)
      return ResourceManager.getString("launch.error.offline", getResourceString());
    return ResourceManager.getString("launch.error.failedloadingresource", getResourceString());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.FailedDownloadException
 * JD-Core Version:    0.6.0
 */