package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class BadMimeTypeResponseException extends DownloadException
{
  private String _mimeType;

  public BadMimeTypeResponseException(URL paramURL, String paramString1, String paramString2)
  {
    super(paramURL, paramString1);
    this._mimeType = paramString2;
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.badmimetyperesponse", getResourceString(), this._mimeType);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.BadMimeTypeResponseException
 * JD-Core Version:    0.6.0
 */