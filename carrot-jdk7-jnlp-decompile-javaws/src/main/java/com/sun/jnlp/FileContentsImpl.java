package com.sun.jnlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.jnlp.FileContents;
import javax.jnlp.JNLPRandomAccessFile;

public final class FileContentsImpl
  implements FileContents
{
  private String _name = null;
  private File _file = null;
  private long _limit = 9223372036854775807L;
  private URL _url = null;
  private JNLPRandomAccessFile _raf = null;
  private PersistenceServiceImpl _psCallback = null;

  FileContentsImpl(File paramFile, long paramLong)
    throws IOException
  {
    this._file = paramFile;
    this._limit = paramLong;
    this._name = this._file.getName();
  }

  FileContentsImpl(File paramFile, PersistenceServiceImpl paramPersistenceServiceImpl, URL paramURL, long paramLong)
  {
    this._file = paramFile;
    this._url = paramURL;
    this._psCallback = paramPersistenceServiceImpl;
    this._limit = paramLong;
    int i = paramURL.getFile().lastIndexOf('/');
    this._name = (i != -1 ? paramURL.getFile().substring(i + 1) : paramURL.getFile());
  }

  public String getName()
  {
    return this._name;
  }

  public long getLength()
  {
    Long localLong = (Long)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new Long(FileContentsImpl.this._file.length());
      }
    });
    return localLong.longValue();
  }

  public InputStream getInputStream()
    throws IOException
  {
    try
    {
      InputStream localInputStream = (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run()
          throws IOException
        {
          return new FileInputStream(FileContentsImpl.this._file);
        }
      });
      return localInputStream;
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
    }
    throw rethrowException(localPrivilegedActionException);
  }

  public OutputStream getOutputStream(boolean paramBoolean)
    throws IOException
  {
    try
    {
      OutputStream localOutputStream = (OutputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(paramBoolean)
      {
        private final boolean val$append;

        public Object run()
          throws IOException
        {
          return new MeteredFileOutputStream(FileContentsImpl.this._file, !this.val$append, FileContentsImpl.this);
        }
      });
      return localOutputStream;
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
    }
    throw rethrowException(localPrivilegedActionException);
  }

  public boolean canRead()
    throws IOException
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new Boolean(FileContentsImpl.this._file.canRead());
      }
    });
    return localBoolean.booleanValue();
  }

  public boolean canWrite()
    throws IOException
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new Boolean(FileContentsImpl.this.canWriteFile(FileContentsImpl.this._file));
      }
    });
    return localBoolean.booleanValue();
  }

  private boolean canWriteFile(File paramFile)
  {
    if (paramFile.exists())
      return paramFile.canWrite();
    File localFile = paramFile.getParentFile();
    return (localFile != null) && (localFile.exists()) && (localFile.canWrite());
  }

  public JNLPRandomAccessFile getRandomAccessFile(String paramString)
    throws IOException
  {
    try
    {
      return (JNLPRandomAccessFile)AccessController.doPrivileged(new PrivilegedExceptionAction(paramString)
      {
        private final String val$mode;

        public Object run()
          throws MalformedURLException, IOException
        {
          return new JNLPRandomAccessFileImpl(FileContentsImpl.this._file, this.val$mode, FileContentsImpl.this);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
    }
    throw rethrowException(localPrivilegedActionException);
  }

  public long getMaxLength()
    throws IOException
  {
    return this._limit;
  }

  public long setMaxLength(long paramLong)
    throws IOException
  {
    if (this._psCallback != null)
    {
      this._limit = this._psCallback.setMaxLength(this._url, paramLong);
      return this._limit;
    }
    this._limit = paramLong;
    return this._limit;
  }

  private IOException rethrowException(PrivilegedActionException paramPrivilegedActionException)
    throws IOException
  {
    Exception localException = paramPrivilegedActionException.getException();
    if ((localException instanceof IOException))
      throw new IOException("IOException from FileContents");
    if ((localException instanceof RuntimeException))
      throw ((RuntimeException)localException);
    throw new IOException(localException.getMessage());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.FileContentsImpl
 * JD-Core Version:    0.6.0
 */