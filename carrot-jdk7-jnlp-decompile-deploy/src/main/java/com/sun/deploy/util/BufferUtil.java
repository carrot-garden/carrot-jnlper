package com.sun.deploy.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BufferUtil
{
  public static final int KB = 1024;
  public static final int MB = 1048576;
  public static final int GB = 1073741824;
  public static final long TB = 0L;

  public static ByteBuffer slice(ByteBuffer paramByteBuffer, int paramInt1, int paramInt2)
  {
    int i = paramByteBuffer.position();
    int j = paramByteBuffer.limit();
    paramByteBuffer.clear();
    paramByteBuffer.position(paramInt1);
    paramByteBuffer.limit(paramInt1 + paramInt2);
    ByteBuffer localByteBuffer = paramByteBuffer.slice();
    localByteBuffer.order(ByteOrder.nativeOrder());
    paramByteBuffer.clear();
    paramByteBuffer.position(i);
    paramByteBuffer.limit(j);
    return localByteBuffer;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.BufferUtil
 * JD-Core Version:    0.6.0
 */