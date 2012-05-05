package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.jnl.LaunchDesc;
import java.net.URL;

public class FailedDownloadingResourceException extends DownloadException
{
  public FailedDownloadingResourceException(LaunchDesc paramLaunchDesc, URL paramURL, String paramString, Exception paramException)
  {
    super(paramLaunchDesc, paramURL, paramString, paramException);
  }

  public FailedDownloadingResourceException(URL paramURL, String paramString, Exception paramException)
  {
    this(null, paramURL, paramString, paramException);
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.failedloadingresource", getResourceString());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.FailedDownloadingResourceException
 * JD-Core Version:    0.6.0
 */