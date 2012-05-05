package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

final class MeteredFileOutputStream extends OutputStream
{
  static String _message = null;
  private FileContentsImpl _contents;
  private long _written = 0L;
  private FileOutputStream _fis;

  MeteredFileOutputStream(File paramFile, boolean paramBoolean, FileContentsImpl paramFileContentsImpl)
    throws FileNotFoundException
  {
    this._fis = new FileOutputStream(paramFile.getAbsolutePath(), paramBoolean);
    this._contents = paramFileContentsImpl;
    this._written = paramFile.length();
    if (_message == null)
      _message = ResourceManager.getString("api.persistence.filesizemessage");
  }

  public void write(int paramInt)
    throws IOException
  {
    checkWrite(1);
    this._fis.write(paramInt);
    this._written += 1L;
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    checkWrite(paramInt2);
    this._fis.write(paramArrayOfByte, paramInt1, paramInt2);
    this._written += paramInt2;
  }

  public void write(byte[] paramArrayOfByte)
    throws IOException
  {
    write(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public void close()
    throws IOException
  {
    this._fis.close();
    super.close();
  }

  public void flush()
    throws IOException
  {
    this._fis.flush();
    super.flush();
  }

  private void checkWrite(int paramInt)
    throws IOException
  {
    if (this._written + paramInt > this._contents.getMaxLength())
      throw new IOException(_message);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.MeteredFileOutputStream
 * JD-Core Version:    0.6.0
 */