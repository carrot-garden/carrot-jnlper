package com.sun.deploy.net;

import com.sun.deploy.resources.ResourceManager;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

public class DownloadException extends IOException
{
  private URL _location;
  private String _version;
  private Exception _e;
  private String _message;

  public DownloadException(URL paramURL, String paramString)
  {
    this(paramURL, paramString, null);
  }

  protected DownloadException(URL paramURL, String paramString, Exception paramException)
  {
    super(ResourceManager.getString("launch.error.category.download"));
    this._location = paramURL;
    this._version = paramString;
    this._e = paramException;
  }

  public URL getLocation()
  {
    return this._location;
  }

  public String getVersion()
  {
    return this._version;
  }

  public String getResourceString()
  {
    String str = this._location.toString();
    if (this._version == null)
      return ResourceManager.getString("launch.error.resourceID", str);
    return ResourceManager.getString("launch.error.resourceID-version", str, this._version);
  }

  public String getRealMessage()
  {
    return this._message;
  }

  public String getMessage()
  {
    return getRealMessage();
  }

  public String getBriefMessage()
  {
    return null;
  }

  public Throwable getWrappedException()
  {
    return this._e;
  }

  public void printStackTrace()
  {
    super.printStackTrace();
    if (this._e != null)
    {
      System.err.println("Caused by:");
      this._e.printStackTrace();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.DownloadException
 * JD-Core Version:    0.6.0
 */