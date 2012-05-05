package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class JNLParseException extends LaunchDescException
{
  private String _msg;
  private int _line;
  private String _launchDescSource;

  public JNLParseException(String paramString1, Exception paramException, String paramString2, int paramInt)
  {
    super(paramException);
    this._msg = paramString2;
    this._line = paramInt;
    this._launchDescSource = paramString1;
  }

  public int getLine()
  {
    return this._line;
  }

  public String getRealMessage()
  {
    if (!isSignedLaunchDesc())
      return ResourceManager.getString("launch.error.parse", this._line);
    return ResourceManager.getString("launch.error.parse-signedjnlp", this._line);
  }

  public String getLaunchDescSource()
  {
    return this._launchDescSource;
  }

  public String toString()
  {
    return "JNLParseException[ " + getMessage() + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.JNLParseException
 * JD-Core Version:    0.6.0
 */