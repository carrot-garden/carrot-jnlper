package com.sun.deploy.net.socket;

public class UnixDomainSocketException extends UnixSocketException
{
  public static UnixDomainSocketException createUnixDomainSocketException(String paramString, int paramInt)
  {
    return new UnixDomainSocketException(paramString, paramInt);
  }

  public UnixDomainSocketException(String paramString, int paramInt)
  {
    super(paramString, paramInt);
  }

  public UnixDomainSocketException(String paramString)
  {
    super(paramString);
  }

  public UnixDomainSocketException()
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.socket.UnixDomainSocketException
 * JD-Core Version:    0.6.0
 */