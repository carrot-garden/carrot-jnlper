package com.sun.deploy.xml;

public class BadTokenException extends Exception
{
  private int _line;
  private String _source;
  private String _message;

  public BadTokenException(String paramString, int paramInt)
  {
    this(null, paramString, paramInt);
  }

  public BadTokenException(String paramString1, String paramString2, int paramInt)
  {
    this._line = paramInt;
    this._source = paramString2;
    this._message = paramString1;
  }

  public int getLine()
  {
    return this._line;
  }

  public String getSource()
  {
    return this._source;
  }

  public String getReason()
  {
    return this._message;
  }

  public String toString()
  {
    String str;
    if (this._message != null)
      str = this._message + " Exception parsing xml at line " + this._line;
    else
      str = "Exception parsing xml at line " + this._line;
    return str;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.xml.BadTokenException
 * JD-Core Version:    0.6.0
 */