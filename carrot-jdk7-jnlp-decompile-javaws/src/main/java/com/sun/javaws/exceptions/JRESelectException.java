package com.sun.javaws.exceptions;

import com.sun.javaws.jnl.JREDesc;

public class JRESelectException extends ExitException
{
  private JREDesc _jreDesc;
  private String _jvmArgs;

  public JRESelectException(JREDesc paramJREDesc, String paramString)
  {
    this(paramJREDesc, paramString, null);
  }

  public JRESelectException(JREDesc paramJREDesc, String paramString, Throwable paramThrowable)
  {
    super(paramThrowable, 2);
    this._jreDesc = paramJREDesc;
    this._jvmArgs = paramString;
  }

  public JREDesc getJREDesc()
  {
    return this._jreDesc;
  }

  public String getJVMArgs()
  {
    return this._jvmArgs;
  }

  public String toString()
  {
    String str = "JRESelectException[ jreDesc: " + this._jreDesc + "; jvmArgs: " + this._jvmArgs + " ]";
    if (getException() != null)
      str = str + getException().toString();
    return str;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.JRESelectException
 * JD-Core Version:    0.6.0
 */