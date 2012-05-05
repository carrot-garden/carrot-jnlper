package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.jnl.LaunchDesc;
import java.net.URL;

public class UnsignedAccessViolationException extends JNLPException
{
  URL _url;
  boolean _initial;

  public UnsignedAccessViolationException(LaunchDesc paramLaunchDesc, URL paramURL, boolean paramBoolean)
  {
    super(ResourceManager.getString("launch.error.category.security"), paramLaunchDesc);
    this._url = paramURL;
    this._initial = paramBoolean;
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.unsignedAccessViolation") + "\n" + ResourceManager.getString("launch.error.unsignedResource", this._url.toString());
  }

  public String getBriefMessage()
  {
    if (this._initial)
      return null;
    return ResourceManager.getString("launcherrordialog.brief.continue");
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.UnsignedAccessViolationException
 * JD-Core Version:    0.6.0
 */