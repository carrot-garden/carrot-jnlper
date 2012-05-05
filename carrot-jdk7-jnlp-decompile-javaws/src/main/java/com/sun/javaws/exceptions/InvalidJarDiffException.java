package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class InvalidJarDiffException extends DownloadException
{
  public InvalidJarDiffException(URL paramURL, String paramString, Exception paramException)
  {
    super(null, paramURL, paramString, paramException);
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.invalidjardiff", getResourceString());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.InvalidJarDiffException
 * JD-Core Version:    0.6.0
 */