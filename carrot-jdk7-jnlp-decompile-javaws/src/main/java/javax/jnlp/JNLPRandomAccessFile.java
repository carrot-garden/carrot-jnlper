package javax.jnlp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract interface JNLPRandomAccessFile extends DataInput, DataOutput
{
  public abstract void close()
    throws IOException;

  public abstract long length()
    throws IOException;

  public abstract long getFilePointer()
    throws IOException;

  public abstract int read()
    throws IOException;

  public abstract int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;

  public abstract int read(byte[] paramArrayOfByte)
    throws IOException;

  public abstract void readFully(byte[] paramArrayOfByte)
    throws IOException;

  public abstract void readFully(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;

  public abstract int skipBytes(int paramInt)
    throws IOException;

  public abstract boolean readBoolean()
    throws IOException;

  public abstract byte readByte()
    throws IOException;

  public abstract int readUnsignedByte()
    throws IOException;

  public abstract short readShort()
    throws IOException;

  public abstract int readUnsignedShort()
    throws IOException;

  public abstract char readChar()
    throws IOException;

  public abstract int readInt()
    throws IOException;

  public abstract long readLong()
    throws IOException;

  public abstract float readFloat()
    throws IOException;

  public abstract double readDouble()
    throws IOException;

  public abstract String readLine()
    throws IOException;

  public abstract String readUTF()
    throws IOException;

  public abstract void seek(long paramLong)
    throws IOException;

  public abstract void setLength(long paramLong)
    throws IOException;

  public abstract void write(int paramInt)
    throws IOException;

  public abstract void write(byte[] paramArrayOfByte)
    throws IOException;

  public abstract void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;

  public abstract void writeBoolean(boolean paramBoolean)
    throws IOException;

  public abstract void writeByte(int paramInt)
    throws IOException;

  public abstract void writeShort(int paramInt)
    throws IOException;

  public abstract void writeChar(int paramInt)
    throws IOException;

  public abstract void writeInt(int paramInt)
    throws IOException;

  public abstract void writeLong(long paramLong)
    throws IOException;

  public abstract void writeFloat(float paramFloat)
    throws IOException;

  public abstract void writeDouble(double paramDouble)
    throws IOException;

  public abstract void writeBytes(String paramString)
    throws IOException;

  public abstract void writeChars(String paramString)
    throws IOException;

  public abstract void writeUTF(String paramString)
    throws IOException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.JNLPRandomAccessFile
 * JD-Core Version:    0.6.0
 */