package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class CouldNotLoadArgumentException extends JNLPException
{
  private String _argument;

  public CouldNotLoadArgumentException(String paramString, Exception paramException)
  {
    super(ResourceManager.getString("launch.error.category.arguments"), paramException);
    this._argument = paramString;
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.couldnotloadarg", this._argument);
  }

  public String getField()
  {
    return getMessage();
  }

  public String toString()
  {
    return "CouldNotLoadArgumentException[ " + getRealMessage() + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.CouldNotLoadArgumentException
 * JD-Core Version:    0.6.0
 */