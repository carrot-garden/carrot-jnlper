package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.jnlp.FileContents;
import javax.jnlp.JNLPRandomAccessFile;

public final class JNLPRandomAccessFileImpl
  implements JNLPRandomAccessFile
{
  private RandomAccessFile _raf = null;
  private FileContents _contents = null;
  private long _length = 0L;
  private String _message = null;

  JNLPRandomAccessFileImpl(File paramFile, String paramString, FileContents paramFileContents)
    throws IOException
  {
    this._raf = new RandomAccessFile(paramFile, paramString);
    this._length = this._raf.length();
    this._contents = paramFileContents;
    if (this._contents == null)
      throw new IllegalArgumentException("FileContents can not be null");
    if (this._message == null)
      this._message = ResourceManager.getString("api.persistence.filesizemessage");
  }

  public void close()
    throws IOException
  {
    this._raf.close();
  }

  public long length()
    throws IOException
  {
    return this._raf.length();
  }

  public long getFilePointer()
    throws IOException
  {
    return this._raf.getFilePointer();
  }

  public int read()
    throws IOException
  {
    return this._raf.read();
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    return this._raf.read(paramArrayOfByte, paramInt1, paramInt2);
  }

  public int read(byte[] paramArrayOfByte)
    throws IOException
  {
    return this._raf.read(paramArrayOfByte);
  }

  public void readFully(byte[] paramArrayOfByte)
    throws IOException
  {
    this._raf.readFully(paramArrayOfByte);
  }

  public void readFully(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    this._raf.readFully(paramArrayOfByte, paramInt1, paramInt2);
  }

  public int skipBytes(int paramInt)
    throws IOException
  {
    return this._raf.skipBytes(paramInt);
  }

  public boolean readBoolean()
    throws IOException
  {
    return this._raf.readBoolean();
  }

  public byte readByte()
    throws IOException
  {
    return this._raf.readByte();
  }

  public int readUnsignedByte()
    throws IOException
  {
    return this._raf.readUnsignedByte();
  }

  public short readShort()
    throws IOException
  {
    return this._raf.readShort();
  }

  public int readUnsignedShort()
    throws IOException
  {
    return this._raf.readUnsignedShort();
  }

  public char readChar()
    throws IOException
  {
    return this._raf.readChar();
  }

  public int readInt()
    throws IOException
  {
    return this._raf.readInt();
  }

  public long readLong()
    throws IOException
  {
    return this._raf.readLong();
  }

  public float readFloat()
    throws IOException
  {
    return this._raf.readFloat();
  }

  public double readDouble()
    throws IOException
  {
    return this._raf.readDouble();
  }

  public String readLine()
    throws IOException
  {
    return this._raf.readLine();
  }

  public String readUTF()
    throws IOException
  {
    return this._raf.readUTF();
  }

  public void seek(long paramLong)
    throws IOException
  {
    this._raf.seek(paramLong);
  }

  public void setLength(long paramLong)
    throws IOException
  {
    if (paramLong > this._contents.getMaxLength())
      throw new IOException(this._message);
    this._raf.setLength(paramLong);
  }

  public void write(int paramInt)
    throws IOException
  {
    checkWrite(1);
    this._raf.write(paramInt);
  }

  public void write(byte[] paramArrayOfByte)
    throws IOException
  {
    if (paramArrayOfByte != null)
      checkWrite(paramArrayOfByte.length);
    this._raf.write(paramArrayOfByte);
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    checkWrite(paramInt2);
    this._raf.write(paramArrayOfByte, paramInt1, paramInt2);
  }

  public void writeBoolean(boolean paramBoolean)
    throws IOException
  {
    checkWrite(1);
    this._raf.writeBoolean(paramBoolean);
  }

  public void writeByte(int paramInt)
    throws IOException
  {
    checkWrite(1);
    this._raf.writeByte(paramInt);
  }

  public void writeShort(int paramInt)
    throws IOException
  {
    checkWrite(2);
    this._raf.writeShort(paramInt);
  }

  public void writeChar(int paramInt)
    throws IOException
  {
    checkWrite(2);
    this._raf.writeChar(paramInt);
  }

  public void writeInt(int paramInt)
    throws IOException
  {
    checkWrite(4);
    this._raf.writeInt(paramInt);
  }

  public void writeLong(long paramLong)
    throws IOException
  {
    checkWrite(8);
    this._raf.writeLong(paramLong);
  }

  public void writeFloat(float paramFloat)
    throws IOException
  {
    checkWrite(4);
    this._raf.writeFloat(paramFloat);
  }

  public void writeDouble(double paramDouble)
    throws IOException
  {
    checkWrite(8);
    this._raf.writeDouble(paramDouble);
  }

  public void writeBytes(String paramString)
    throws IOException
  {
    if (paramString != null)
      checkWrite(paramString.length());
    this._raf.writeBytes(paramString);
  }

  public void writeChars(String paramString)
    throws IOException
  {
    if (paramString != null)
      checkWrite(paramString.length() * 2);
    this._raf.writeChars(paramString);
  }

  public void writeUTF(String paramString)
    throws IOException
  {
    if (paramString != null)
      checkWrite(getUTFLen(paramString));
    this._raf.writeUTF(paramString);
  }

  private int getUTFLen(String paramString)
  {
    int i = paramString.length();
    char[] arrayOfChar = new char[i];
    paramString.getChars(0, i, arrayOfChar, 0);
    int k = 2;
    for (int m = 0; m < i; m++)
    {
      int j = arrayOfChar[m];
      if ((j >= 1) && (j <= 127))
        k++;
      else if (j > 2047)
        k += 3;
      else
        k += 2;
    }
    return k;
  }

  private void checkWrite(int paramInt)
    throws IOException
  {
    if (this._raf.getFilePointer() + paramInt > this._contents.getMaxLength())
      throw new IOException(this._message);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.JNLPRandomAccessFileImpl
 * JD-Core Version:    0.6.0
 */