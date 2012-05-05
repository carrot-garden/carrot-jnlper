package com.sun.deploy.net.socket;

import com.sun.deploy.config.Platform;

public class UnixSocketImpl
{
  protected static boolean unStreamSocketSupported()
  {
    return true;
  }

  protected static native long unStreamSocketCreate(String paramString, boolean paramBoolean, int paramInt)
    throws UnixDomainSocketException;

  protected static native void unStreamSocketClose(long paramLong)
    throws UnixDomainSocketException;

  protected static native boolean unStreamSocketIsValid(long paramLong)
    throws UnixDomainSocketException;

  protected static native void unStreamSocketBind(long paramLong)
    throws UnixDomainSocketException;

  protected static native void unStreamSocketListen(long paramLong, int paramInt)
    throws UnixDomainSocketException;

  protected static native long unStreamSocketAccept(long paramLong)
    throws UnixDomainSocketException;

  protected static native void unStreamSocketConnect(long paramLong)
    throws UnixDomainSocketException;

  protected static native int unStreamSocketRead(long paramLong, Object paramObject, int paramInt1, int paramInt2)
    throws UnixDomainSocketException;

  protected static native int unStreamSocketWrite(long paramLong, Object paramObject, int paramInt1, int paramInt2)
    throws UnixDomainSocketException;

  protected static native String unStreamSocketGetNativeInfo(long paramLong);

  static
  {
    Platform.get().loadDeployNativeLib();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.socket.UnixSocketImpl
 * JD-Core Version:    0.6.0
 */