package com.sun.deploy.net.socket;

import java.net.SocketException;

public class UnixSocketException extends SocketException
{
  public static int EANY = -1;
  public static int ENOENT = 2;
  public static int ENOMEM = 12;
  public static int EACCES = 13;
  public static int EFAULT = 14;
  public static int ENOTDIR = 20;
  public static int EINVAL = 22;
  public static int ENFILE = 23;
  public static int EMFILE = 24;
  public static int EROFS = 30;
  public static int ELOOP = 40;
  public static int ENAMETOOLONG = 36;
  public static int EBADFD = 77;
  public static int ENOTSOCK = 88;
  public static int EAFNOSUPPORT = 97;
  public static int EPROTONOSUPPORT = 93;
  public static int ESOCKTNOSUPPORT = 94;
  public static int EOPNOTSUPP = 95;
  public static int EADDRINUSE = 98;
  public static int EADDRNOTAVAIL = 99;
  public static int ENOBUFS = 105;
  private int errno;

  public static UnixSocketException createUnixSocketException(String paramString, int paramInt)
  {
    return new UnixSocketException(paramString, paramInt);
  }

  public UnixSocketException(String paramString, int paramInt)
  {
    super(paramString);
    this.errno = paramInt;
  }

  public UnixSocketException(String paramString)
  {
    super(paramString);
    this.errno = EANY;
  }

  public UnixSocketException()
  {
    this.errno = EANY;
  }

  public String getMessage()
  {
    return "errno " + this.errno + ": " + super.getMessage();
  }

  public int errno()
  {
    return this.errno;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.socket.UnixSocketException
 * JD-Core Version:    0.6.0
 */