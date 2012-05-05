package com.sun.deploy.util;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class SyncFileAccess
{
  private static final int TYPE_RANDOM_ACCESS_FILE = 1;
  private static final int TYPE_FILE_INPUT_STREAM = 2;
  private static final int TYPE_FILE_OUTPUT_STREAM = 3;
  private SyncAccess fileAccSync;
  private File file;

  public SyncFileAccess(File paramFile)
  {
    this.file = paramFile;
    this.fileAccSync = new SyncAccess(8);
  }

  public RandomAccessFileLock openLockRandomAccessFile(String paramString, int paramInt, boolean paramBoolean)
    throws IOException
  {
    Object localObject = openLockFileObject(1, paramString, paramInt, paramBoolean, false);
    return (RandomAccessFileLock)localObject;
  }

  public FileInputStreamLock openLockFileInputStream(int paramInt, boolean paramBoolean)
    throws IOException
  {
    Object localObject = openLockFileObject(2, "r", paramInt, paramBoolean, false);
    return (FileInputStreamLock)localObject;
  }

  public FileOutputStreamLock openLockFileOutputStream(boolean paramBoolean1, int paramInt, boolean paramBoolean2)
    throws IOException
  {
    Object localObject = openLockFileObject(3, "rw", paramInt, paramBoolean2, paramBoolean1);
    return (FileOutputStreamLock)localObject;
  }

  private Object openLockFileObject(int paramInt1, String paramString, int paramInt2, boolean paramBoolean1, boolean paramBoolean2)
    throws IOException
  {
    int i = paramInt2 == 0 ? 1 : 0;
    boolean bool = paramString.equals("r");
    SyncAccess.Lock localLock = this.fileAccSync.lock(bool ? 2 : 4);
    Object localObject1 = null;
    try
    {
      while (null == localObject1)
      {
        if (paramBoolean1)
          localObject1 = AccessController.doPrivileged(new PrivilegedAction(paramInt1, paramString, paramBoolean2)
          {
            private final int val$type;
            private final String val$mode;
            private final boolean val$fopt1;

            public Object run()
            {
              Object localObject = null;
              try
              {
                switch (this.val$type)
                {
                case 1:
                  localObject = new RandomAccessFile(SyncFileAccess.this.file, this.val$mode);
                  break;
                case 2:
                  localObject = new FileInputStream(SyncFileAccess.this.file);
                  break;
                case 3:
                  localObject = new FileOutputStream(SyncFileAccess.this.file.getPath(), this.val$fopt1);
                  break;
                default:
                  throw new InternalError("wrong fobj type: " + this.val$type);
                }
              }
              catch (FileNotFoundException localFileNotFoundException)
              {
                Trace.ignoredException(localFileNotFoundException);
              }
              return localObject;
            }
          });
        else
          try
          {
            switch (paramInt1)
            {
            case 1:
              localObject1 = new RandomAccessFile(this.file, paramString);
              break;
            case 2:
              localObject1 = new FileInputStream(this.file);
              break;
            case 3:
              localObject1 = new FileOutputStream(this.file.getPath(), paramBoolean2);
              break;
            default:
              throw new InternalError("wrong fobj type: " + paramInt1);
            }
          }
          catch (FileNotFoundException localFileNotFoundException)
          {
            Trace.ignoredException(localFileNotFoundException);
          }
        if (null == localObject1)
          throw new FileNotFoundException("index file not found");
        if (!Config.isJavaVersionAtLeast14())
        {
          if (localObject1 != null)
          {
            localObject2 = FObjLock.createFObjLock(paramInt1, localObject1, localLock);
            return localObject2;
          }
          localObject2 = null;
          return localObject2;
        }
        Object localObject2 = null;
        switch (paramInt1)
        {
        case 1:
          localObject2 = ((RandomAccessFile)localObject1).getChannel();
          break;
        case 2:
          localObject2 = ((FileInputStream)localObject1).getChannel();
          break;
        case 3:
          localObject2 = ((FileOutputStream)localObject1).getChannel();
          break;
        default:
          throw new InternalError("wrong fobj type: " + paramInt1);
        }
        if (!((FileChannel)localObject2).isOpen())
        {
          localObject2 = null;
          localObject1 = null;
          if ((i == 0) && (paramInt2 <= 0))
            throw new IOException("index file could not be opened, timeout reached");
          Trace.println("SyncFileAccess.openLock: index file not opened, remaining TO : " + paramInt2, TraceLevel.NETWORK);
          try
          {
            if (i == 0)
            {
              Thread.sleep(paramInt2);
              paramInt2 -= 100;
            }
            else
            {
              Thread.sleep(100L);
            }
          }
          catch (Exception localException1)
          {
          }
          continue;
        }
        try
        {
          FileLock localFileLock = null;
          while (localFileLock == null)
            try
            {
              localFileLock = ((FileChannel)localObject2).lock(0L, 9223372036854775807L, bool);
            }
            catch (OverlappingFileLockException localOverlappingFileLockException)
            {
              if ((i == 0) && (paramInt2 <= 0))
                throw new IOException("handled OverlappingFileLockException, timeout reached", localOverlappingFileLockException);
              Trace.println("SyncFileAccess.openLock: handled OverlappingFileLockException, remainint TO : " + paramInt2, TraceLevel.NETWORK);
              try
              {
                if (i == 0)
                {
                  Thread.sleep(paramInt2);
                  paramInt2 -= 100;
                }
                else
                {
                  Thread.sleep(100L);
                }
              }
              catch (Exception localException3)
              {
              }
              localFileLock = null;
            }
            catch (IOException localIOException)
            {
              localObject1 = null;
              Object localObject3 = null;
              if (localObject1 == null)
                localLock.release();
              return localObject3;
            }
        }
        catch (ClosedChannelException localClosedChannelException)
        {
          localObject2 = null;
          localObject1 = null;
          if ((i == 0) && (paramInt2 <= 0))
            throw new IOException("handled ClosedChannelException, timeout reached", localClosedChannelException);
          Trace.println("SyncFileAccess.openLock: handled ClosedChannelException, remaining TO: " + paramInt2, TraceLevel.NETWORK);
          try
          {
            if (i == 0)
            {
              Thread.sleep(paramInt2);
              paramInt2 -= 100;
            }
            else
            {
              Thread.sleep(100L);
            }
          }
          catch (Exception localException2)
          {
          }
        }
      }
    }
    finally
    {
      if (localObject1 == null)
        localLock.release();
    }
    if (localObject1 != null)
      return FObjLock.createFObjLock(paramInt1, localObject1, localLock);
    return null;
  }

  private static class FObjLock
  {
    protected Object fobj;
    private SyncAccess.Lock lock;

    private FObjLock(Object paramObject, SyncAccess.Lock paramLock)
    {
      this.fobj = paramObject;
      this.lock = paramLock;
    }

    public void release()
    {
      if (this.lock != null)
      {
        this.lock.release();
        this.lock = null;
      }
    }

    protected static Object createFObjLock(int paramInt, Object paramObject, SyncAccess.Lock paramLock)
    {
      Object localObject;
      switch (paramInt)
      {
      case 1:
        localObject = new SyncFileAccess.RandomAccessFileLock((RandomAccessFile)paramObject, paramLock, null);
        break;
      case 2:
        localObject = new SyncFileAccess.FileInputStreamLock((FileInputStream)paramObject, paramLock, null);
        break;
      case 3:
        localObject = new SyncFileAccess.FileOutputStreamLock((FileOutputStream)paramObject, paramLock, null);
        break;
      default:
        throw new InternalError("wrong fobj type: " + paramInt);
      }
      return localObject;
    }

    FObjLock(Object paramObject, SyncAccess.Lock paramLock, SyncFileAccess.1 param1)
    {
      this(paramObject, paramLock);
    }
  }

  public static class FileInputStreamLock extends SyncFileAccess.FObjLock
  {
    private FileInputStreamLock(FileInputStream paramFileInputStream, SyncAccess.Lock paramLock)
    {
      super(paramLock, null);
    }

    public FileInputStream getFileInputStream()
    {
      return (FileInputStream)this.fobj;
    }

    FileInputStreamLock(FileInputStream paramFileInputStream, SyncAccess.Lock paramLock, SyncFileAccess.1 param1)
    {
      this(paramFileInputStream, paramLock);
    }
  }

  public static class FileOutputStreamLock extends SyncFileAccess.FObjLock
  {
    private FileOutputStreamLock(FileOutputStream paramFileOutputStream, SyncAccess.Lock paramLock)
    {
      super(paramLock, null);
    }

    public FileOutputStream getFileOutputStream()
    {
      return (FileOutputStream)this.fobj;
    }

    FileOutputStreamLock(FileOutputStream paramFileOutputStream, SyncAccess.Lock paramLock, SyncFileAccess.1 param1)
    {
      this(paramFileOutputStream, paramLock);
    }
  }

  public static class RandomAccessFileLock extends SyncFileAccess.FObjLock
  {
    private RandomAccessFileLock(RandomAccessFile paramRandomAccessFile, SyncAccess.Lock paramLock)
    {
      super(paramLock, null);
    }

    public RandomAccessFile getRandomAccessFile()
    {
      return (RandomAccessFile)this.fobj;
    }

    RandomAccessFileLock(RandomAccessFile paramRandomAccessFile, SyncAccess.Lock paramLock, SyncFileAccess.1 param1)
    {
      this(paramRandomAccessFile, paramLock);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.SyncFileAccess
 * JD-Core Version:    0.6.0
 */