package com.sun.deploy.net.socket;

import com.sun.deploy.config.Platform;
import java.io.File;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnixDomainSocket
{
  public static final String pipeFileNamePrefix = ".com.sun.deploy.net.socket.";
  public static final String pipeFileNameSuffix = ".AF_UNIX";
  public static final int STATUS_CLOSE = 0;
  public static final int STATUS_OPEN = 1;
  public static final int STATUS_CONNECT = 2;
  public static final int STATUS_BIND = 3;
  public static final int STATUS_LISTEN = 4;
  public static final int STATUS_ACCEPT = 5;
  private volatile int socketStatus;
  private volatile long socketHandle;
  private volatile boolean unlinkFile;
  private String fileName;
  private boolean abstractNamespace;
  private int protocol;
  private int backlog;
  private static ShutdownHookUnlinkFiles shutdownHookUnlinkFiles;

  public static boolean isSupported()
  {
    return UnixSocketImpl.unStreamSocketSupported();
  }

  public static UnixDomainSocket CreateClientConnect(String paramString, boolean paramBoolean, int paramInt)
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    UnixDomainSocket localUnixDomainSocket = new UnixDomainSocket(paramString, paramBoolean, paramInt);
    localUnixDomainSocket.connect();
    return localUnixDomainSocket;
  }

  public static UnixDomainSocket CreateServerBindListen(String paramString, boolean paramBoolean, int paramInt1, int paramInt2)
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    UnixDomainSocket localUnixDomainSocket = new UnixDomainSocket(paramString, paramBoolean, paramInt1);
    localUnixDomainSocket.bind();
    localUnixDomainSocket.listen(paramInt2);
    return localUnixDomainSocket;
  }

  public static UnixDomainSocket CreateServerBindListen(int paramInt1, int paramInt2)
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    UnixDomainSocket localUnixDomainSocket = new UnixDomainSocket(paramInt1);
    localUnixDomainSocket.bind();
    localUnixDomainSocket.listen(paramInt2);
    return localUnixDomainSocket;
  }

  public UnixDomainSocket(String paramString, boolean paramBoolean, int paramInt)
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    setup(paramString, false, paramInt);
  }

  public UnixDomainSocket(int paramInt)
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    long l = Platform.get().getNativePID();
    String str = null;
    try
    {
      File localFile1 = localFile1 = File.createTempFile(".com.sun.deploy.net.socket." + String.valueOf(l) + ".", ".AF_UNIX");
      str = localFile1.getAbsolutePath();
      if (localFile1.exists())
        localFile1.delete();
      else
        str = null;
    }
    catch (Exception localException1)
    {
      localException1.printStackTrace();
      str = null;
    }
    if (str == null)
      try
      {
        str = new String("/tmp/.com.sun.deploy.net.socket." + String.valueOf(l) + ".AF_UNIX");
        File localFile2 = localFile2 = new File(str);
        if (localFile2.exists())
          localFile2.delete();
      }
      catch (Exception localException2)
      {
        localException2.printStackTrace();
        str = null;
      }
    if (str == null)
      throw new RuntimeException("could not create a temp pipe filename");
    setup(str, false, paramInt);
  }

  public synchronized void open()
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    validateSocketStatusTransition(1);
    this.socketHandle = UnixSocketImpl.unStreamSocketCreate(this.fileName, this.abstractNamespace, this.protocol);
    this.socketStatus = 1;
  }

  public void close()
  {
    this.socketStatus = 0;
    if (0L != this.socketHandle)
    {
      long l = this.socketHandle;
      this.socketHandle = 0L;
      try
      {
        UnixSocketImpl.unStreamSocketClose(l);
      }
      catch (Exception localException2)
      {
      }
    }
    if (this.unlinkFile)
      try
      {
        File localFile = new File(this.fileName);
        if (null != localFile)
          localFile.delete();
      }
      catch (Exception localException1)
      {
      }
    this.backlog = 0;
  }

  public void deleteFileOnClose()
  {
    if ((!this.unlinkFile) && (!this.abstractNamespace))
    {
      this.unlinkFile = true;
      shutdownHookUnlinkFiles.add(this.fileName);
    }
  }

  public synchronized void bind()
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    validateSocketStatusTransition(3);
    UnixSocketImpl.unStreamSocketBind(this.socketHandle);
    this.socketStatus = 3;
    deleteFileOnClose();
  }

  public synchronized void listen(int paramInt)
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    validateSocketStatusTransition(4);
    UnixSocketImpl.unStreamSocketListen(this.socketHandle, paramInt);
    this.backlog = paramInt;
    this.socketStatus = 4;
  }

  public UnixDomainSocket accept()
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    validateSocketStatusTransition(5);
    long l = UnixSocketImpl.unStreamSocketAccept(this.socketHandle);
    this.socketStatus = 5;
    return new UnixDomainSocket(this, l, 2);
  }

  public void connect()
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    validateSocketStatusTransition(2);
    UnixSocketImpl.unStreamSocketConnect(this.socketHandle);
    this.socketStatus = 2;
  }

  public int read(ByteBuffer paramByteBuffer)
    throws UnixDomainSocketException, BufferOverflowException, UnsupportedOperationException
  {
    return read(paramByteBuffer, paramByteBuffer.remaining());
  }

  public int read(ByteBuffer paramByteBuffer, int paramInt)
    throws UnixDomainSocketException, BufferOverflowException, UnsupportedOperationException
  {
    validateSocketStatusForReadWrite();
    if (null == paramByteBuffer)
      throw new IllegalArgumentException("Argument buffer is null");
    if (!paramByteBuffer.isDirect())
      throw new IllegalArgumentException("Argument buffer is not direct");
    int i = paramByteBuffer.limit();
    int j = paramByteBuffer.position();
    if (j >= i)
      throw new BufferOverflowException();
    if (j + paramInt > i)
      paramInt = i - j;
    int k = UnixSocketImpl.unStreamSocketRead(this.socketHandle, paramByteBuffer, j, paramInt);
    if (k > 0)
      paramByteBuffer.position(j + k);
    return k;
  }

  public int write(ByteBuffer paramByteBuffer)
    throws UnixDomainSocketException, BufferUnderflowException, UnsupportedOperationException
  {
    return write(paramByteBuffer, paramByteBuffer.remaining());
  }

  public int write(ByteBuffer paramByteBuffer, int paramInt)
    throws UnixDomainSocketException, BufferUnderflowException, UnsupportedOperationException
  {
    validateSocketStatusForReadWrite();
    if (null == paramByteBuffer)
      throw new IllegalArgumentException("Argument buffer is null");
    if (!paramByteBuffer.isDirect())
      throw new IllegalArgumentException("Argument buffer is not direct");
    int i = paramByteBuffer.limit();
    int j = paramByteBuffer.position();
    if (j >= i)
      throw new BufferUnderflowException();
    if (j + paramInt > i)
      paramInt = i - j;
    int k = UnixSocketImpl.unStreamSocketWrite(this.socketHandle, paramByteBuffer, j, paramInt);
    if (k > 0)
      paramByteBuffer.position(j + k);
    return k;
  }

  public synchronized boolean isOpenAndValid()
    throws UnsupportedOperationException
  {
    boolean bool = false;
    if ((0L != this.socketHandle) && (this.socketStatus != 0))
      try
      {
        bool = UnixSocketImpl.unStreamSocketIsValid(this.socketHandle);
      }
      catch (Exception localException)
      {
      }
    if (!bool)
      this.socketStatus = 0;
    return bool;
  }

  public boolean isOpen()
  {
    return this.socketStatus != 0;
  }

  public boolean isConnected()
  {
    return this.socketStatus != 2;
  }

  public String getFilename()
  {
    return this.fileName;
  }

  public boolean getIsAbstractNamespace()
  {
    return this.abstractNamespace;
  }

  public int getProtocol()
  {
    return this.protocol;
  }

  public int getBacklog()
  {
    return this.backlog;
  }

  public int getStatus()
  {
    return this.socketStatus;
  }

  public String getStatusAsString()
  {
    switch (this.socketStatus)
    {
    case 0:
      return "close";
    case 1:
      return "open";
    case 2:
      return "connect";
    case 3:
      return "bind";
    case 4:
      return "listen";
    case 5:
      return "accept";
    }
    return "invalid";
  }

  public boolean isServer()
  {
    switch (this.socketStatus)
    {
    case 3:
    case 4:
    case 5:
      return true;
    }
    return false;
  }

  public String getNativeInfo()
  {
    String str = "n.a.";
    if (0L != this.socketHandle)
      try
      {
        str = UnixSocketImpl.unStreamSocketGetNativeInfo(this.socketHandle);
      }
      catch (Exception localException)
      {
      }
    return str;
  }

  public String toString()
  {
    return "UnixDomainSocket[" + getStatusAsString() + ", pipe: " + getFilename() + ", ans: " + getIsAbstractNamespace() + ", proto: " + getProtocol() + ", backlog: " + getBacklog() + ", info: " + getNativeInfo() + "]";
  }

  protected void finalize()
    throws Throwable
  {
    try
    {
      close();
    }
    finally
    {
      super.finalize();
    }
  }

  private void setup(String paramString, boolean paramBoolean, int paramInt)
    throws UnixDomainSocketException, UnsupportedOperationException
  {
    if (null == paramString)
      throw new IllegalArgumentException("Argument fileName is null");
    this.socketHandle = 0L;
    this.fileName = paramString;
    this.abstractNamespace = paramBoolean;
    this.protocol = paramInt;
    this.backlog = 0;
    this.socketStatus = 0;
    open();
  }

  private UnixDomainSocket(UnixDomainSocket paramUnixDomainSocket, long paramLong, int paramInt)
  {
    this.socketHandle = paramLong;
    this.fileName = paramUnixDomainSocket.fileName;
    this.abstractNamespace = paramUnixDomainSocket.abstractNamespace;
    this.protocol = paramUnixDomainSocket.protocol;
    this.backlog = 0;
    this.socketStatus = paramInt;
  }

  private void validateSocketStatusTransition(int paramInt)
    throws UnixDomainSocketException
  {
    if ((0L == this.socketHandle) && (0 != this.socketStatus))
      throw new UnixDomainSocketException(toString(), UnixSocketException.EINVAL);
    switch (paramInt)
    {
    case 1:
      if (this.socketStatus != 0)
        break;
      return;
    case 2:
    case 3:
      if (this.socketStatus != 1)
        break;
      return;
    case 4:
      if (this.socketStatus != 3)
        break;
      return;
    case 5:
      if ((this.socketStatus != 4) && (this.socketStatus != 5))
        break;
      return;
    }
    throw new UnixDomainSocketException(toString(), UnixSocketException.EINVAL);
  }

  private void validateSocketStatusForReadWrite()
    throws UnixDomainSocketException
  {
    if ((0L != this.socketHandle) && (2 == this.socketStatus))
      return;
    throw new UnixDomainSocketException(toString(), UnixSocketException.EINVAL);
  }

  static
  {
    Platform.get();
    shutdownHookUnlinkFiles = new ShutdownHookUnlinkFiles();
    Runtime.getRuntime().addShutdownHook(shutdownHookUnlinkFiles);
  }

  private static class ShutdownHookUnlinkFiles extends Thread
  {
    private List files = new ArrayList();

    public synchronized void add(String paramString)
    {
      try
      {
        File localFile = new File(paramString);
        this.files.add(localFile);
      }
      catch (Exception localException)
      {
      }
    }

    public void run()
    {
      Iterator localIterator = this.files.iterator();
      while (localIterator.hasNext())
      {
        File localFile = (File)localIterator.next();
        try
        {
          if (null != localFile)
            localFile.delete();
        }
        catch (Exception localException)
        {
        }
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.socket.UnixDomainSocket
 * JD-Core Version:    0.6.0
 */