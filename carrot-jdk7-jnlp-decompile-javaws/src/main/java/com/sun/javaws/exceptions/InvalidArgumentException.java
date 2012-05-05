package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class InvalidArgumentException extends JNLPException
{
  private String[] _arguments;

  public InvalidArgumentException(String[] paramArrayOfString)
  {
    super(ResourceManager.getString("launch.error.category.arguments"));
    this._arguments = paramArrayOfString;
  }

  public String getRealMessage()
  {
    StringBuffer localStringBuffer = new StringBuffer("{");
    for (int i = 0; i < this._arguments.length; i++)
    {
      localStringBuffer.append(this._arguments[i]);
      if (i >= this._arguments.length - 1)
        continue;
      localStringBuffer.append(", ");
    }
    localStringBuffer.append(" }");
    return ResourceManager.getString("launch.error.toomanyargs", localStringBuffer.toString());
  }

  public String getField()
  {
    return getMessage();
  }

  public String toString()
  {
    return "InvalidArgumentException[ " + getRealMessage() + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.InvalidArgumentException
 * JD-Core Version:    0.6.0
 */