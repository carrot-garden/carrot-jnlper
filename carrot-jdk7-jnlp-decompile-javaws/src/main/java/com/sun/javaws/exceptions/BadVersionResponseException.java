package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class BadVersionResponseException extends DownloadException
{
  private String _responseVersionID;

  public BadVersionResponseException(URL paramURL, String paramString1, String paramString2)
  {
    super(paramURL, paramString1);
    this._responseVersionID = paramString2;
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.badversionresponse", getResourceString(), this._responseVersionID);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.BadVersionResponseException
 * JD-Core Version:    0.6.0
 */