package javax.jnlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract interface FileContents
{
  public abstract String getName()
    throws IOException;

  public abstract InputStream getInputStream()
    throws IOException;

  public abstract OutputStream getOutputStream(boolean paramBoolean)
    throws IOException;

  public abstract long getLength()
    throws IOException;

  public abstract boolean canRead()
    throws IOException;

  public abstract boolean canWrite()
    throws IOException;

  public abstract JNLPRandomAccessFile getRandomAccessFile(String paramString)
    throws IOException;

  public abstract long getMaxLength()
    throws IOException;

  public abstract long setMaxLength(long paramLong)
    throws IOException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.FileContents
 * JD-Core Version:    0.6.0
 */