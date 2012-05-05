package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.jnl.LaunchDesc;

public class NoLocalJREException extends JNLPException
{
  private String _message;

  public NoLocalJREException(LaunchDesc paramLaunchDesc, String paramString, boolean paramBoolean)
  {
    super(ResourceManager.getString("launch.error.category.config"), paramLaunchDesc);
    if (paramBoolean)
      this._message = ResourceManager.getString("launch.error.wont.download.jre", paramString);
    else
      this._message = ResourceManager.getString("launch.error.cant.download.jre", paramString);
  }

  public String getRealMessage()
  {
    return this._message;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.NoLocalJREException
 * JD-Core Version:    0.6.0
 */