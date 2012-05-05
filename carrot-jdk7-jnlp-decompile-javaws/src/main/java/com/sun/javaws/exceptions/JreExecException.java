package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class JreExecException extends JNLPException
{
  private String _version;

  public JreExecException(String paramString, Exception paramException)
  {
    super(ResourceManager.getString("launch.error.category.unexpected"), paramException);
    this._version = paramString;
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.failedexec", this._version);
  }

  public String toString()
  {
    return "JreExecException[ " + getMessage() + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.JreExecException
 * JD-Core Version:    0.6.0
 */