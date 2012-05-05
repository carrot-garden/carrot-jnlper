package com.sun.deploy.trace;

import java.io.ByteArrayOutputStream;

class TraceStream extends ByteArrayOutputStream
{
  private void enQueueData()
  {
    String str = toString();
    reset();
    Trace.print(str, TraceLevel.DEFAULT);
  }

  public void write(int paramInt)
  {
    super.write(paramInt);
    if ((paramInt == 10) || (paramInt == 13))
      enQueueData();
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    super.write(paramArrayOfByte, paramInt1, paramInt2);
    if ((paramArrayOfByte[(paramInt2 - 1)] == 10) || (paramArrayOfByte[(paramInt2 - 1)] == 13))
      enQueueData();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.trace.TraceStream
 * JD-Core Version:    0.6.0
 */