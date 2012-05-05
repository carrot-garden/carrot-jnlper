package com.sun.deploy.trace;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketTraceListener
  implements TraceListener
{
  private final String host;
  private final int port;
  private Socket socket;
  private PrintStream socketTraceStream = null;

  public SocketTraceListener(String paramString, int paramInt)
  {
    this.host = paramString;
    this.port = paramInt;
    try
    {
      this.socket = new Socket(paramString, paramInt);
      this.socketTraceStream = new PrintStream(new BufferedOutputStream(this.socket.getOutputStream()));
    }
    catch (UnknownHostException localUnknownHostException)
    {
      localUnknownHostException.printStackTrace();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  public Socket getSocket()
  {
    return this.socket;
  }

  public void print(String paramString)
  {
    if (this.socketTraceStream == null)
      return;
    this.socketTraceStream.print(paramString);
    this.socketTraceStream.flush();
  }

  public void flush()
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.trace.SocketTraceListener
 * JD-Core Version:    0.6.0
 */