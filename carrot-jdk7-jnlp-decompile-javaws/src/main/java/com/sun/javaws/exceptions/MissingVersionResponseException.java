package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class MissingVersionResponseException extends DownloadException
{
  public MissingVersionResponseException(URL paramURL, String paramString)
  {
    super(paramURL, paramString);
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.missingversionresponse", getResourceString());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.MissingVersionResponseException
 * JD-Core Version:    0.6.0
 */