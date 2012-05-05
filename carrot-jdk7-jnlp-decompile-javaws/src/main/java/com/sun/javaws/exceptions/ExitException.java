package com.sun.javaws.exceptions;

public class ExitException extends Exception
{
  private int _reason;
  private Throwable _throwable;
  private String _msg;
  public static final int OK = 0;
  public static final int REBOOT = 1;
  public static final int JRE_MISMATCH = 2;
  public static final int LAUNCH_ERROR = 3;
  public static final int LAUNCH_ABORT_SILENT = 4;
  public static final int LAUNCH_SINGLETON = 5;
  public static final int LAUNCH_ERROR_MESSAGE = 6;

  public ExitException(Throwable paramThrowable, int paramInt)
  {
    this._throwable = paramThrowable;
    this._reason = paramInt;
  }

  public ExitException(String paramString, Throwable paramThrowable)
  {
    this._msg = paramString;
    this._throwable = paramThrowable;
    this._reason = 6;
  }

  public Throwable getException()
  {
    return this._throwable;
  }

  public int getReason()
  {
    return this._reason;
  }

  public boolean isErrorException()
  {
    return (this._reason != 0) && (this._reason != 5);
  }

  public boolean isSilentException()
  {
    return (this._reason == 0) || (this._reason >= 4);
  }

  public String getMessage()
  {
    if (this._msg != null)
      return this._msg;
    if ((this._throwable != null) && (this._throwable.getMessage() != null))
      return this._throwable.getMessage();
    return toString();
  }

  public String toString()
  {
    String str = "ExitException[ " + getReason() + "]";
    if (this._throwable != null)
      str = str + this._throwable.toString();
    return str;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.ExitException
 * JD-Core Version:    0.6.0
 */