package com.sun.javaws.exceptions;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.resources.ResourceManager;

public class BadFieldException extends LaunchDescException
{
  private String _field;
  private String _value;
  private String _launchDescSource;

  public BadFieldException(String paramString1, String paramString2, String paramString3)
  {
    this._value = paramString3;
    this._field = paramString2;
    this._launchDescSource = paramString1;
  }

  public String getField()
  {
    return getMessage();
  }

  public String getValue()
  {
    return this._value;
  }

  public String getRealMessage()
  {
    if (!Cache.isCacheEnabled())
      return ResourceManager.getString("launch.error.badfield.nocache");
    if (getValue().equals("https"))
      return ResourceManager.getString("launch.error.badfield", this._field, this._value) + "\n" + ResourceManager.getString("launch.error.badfield.https");
    if (!isSignedLaunchDesc())
      return ResourceManager.getString("launch.error.badfield", this._field, this._value);
    return ResourceManager.getString("launch.error.badfield-signedjnlp", this._field, this._value);
  }

  public String getLaunchDescSource()
  {
    return this._launchDescSource;
  }

  public String toString()
  {
    if (getValue().equals("https"))
      return "BadFieldException[ " + getRealMessage() + "]";
    return "BadFieldException[ " + getField() + "," + getValue() + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.BadFieldException
 * JD-Core Version:    0.6.0
 */