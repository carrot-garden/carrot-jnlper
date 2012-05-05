package com.sun.deploy.uitoolkit.ui;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.trace.TraceListener;

public final class ConsoleTraceListener
  implements TraceListener
{
  private static final long MIN_CONSOLE_OUTPUT_INTERVAL = 100L;
  private ConsoleWindow console = null;
  private final BoundedStringBuffer buffer;
  private ConsoleWriterThread writer;
  private boolean running;

  public ConsoleTraceListener()
  {
    this(1048575);
  }

  public ConsoleTraceListener(int paramInt)
  {
    this.buffer = new BoundedStringBuffer(paramInt);
  }

  public void flush()
  {
    if (this.writer != null)
      this.writer.flush();
  }

  public void setConsole(ConsoleWindow paramConsoleWindow)
  {
    if (this.console == paramConsoleWindow)
      return;
    if (paramConsoleWindow == null)
    {
      Trace.println("Calling ConsoleTraceListener.setConsole() with null console", TraceLevel.UI);
      cleanupWriter();
      return;
    }
    if ((this.console != null) || (this.writer != null))
    {
      Trace.println("Calling ConsoleTraceListener.setConsole() when console already set", TraceLevel.UI);
      cleanupWriter();
    }
    this.buffer.setBound(0);
    this.console = paramConsoleWindow;
    this.running = true;
    this.writer = new ConsoleWriterThread();
    this.writer.start();
  }

  public void print(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0))
      return;
    synchronized (this.buffer)
    {
      int i = this.buffer.length() == 0 ? 1 : 0;
      this.buffer.append(paramString);
      if ((i != 0) && (this.console != null))
        this.buffer.notifyAll();
    }
  }

  private void cleanupWriter()
  {
    if ((this.writer != null) && (this.writer.isAlive()))
    {
      this.running = false;
      this.writer.interrupt();
      this.writer = null;
    }
  }

  String getTraceMessages()
  {
    return this.buffer.toString();
  }

  void stopWriter()
  {
    if (this.writer != null)
    {
      this.running = false;
      this.writer.interrupt();
    }
  }

  static class BoundedStringBuffer
  {
    private int bound;
    private final StringBuffer sb = new StringBuffer();

    BoundedStringBuffer(int paramInt)
    {
      this.bound = paramInt;
    }

    public String toString()
    {
      return this.sb.toString();
    }

    public boolean equals(Object paramObject)
    {
      return this.sb.equals(paramObject);
    }

    public int hashCode()
    {
      return this.sb.hashCode();
    }

    int length()
    {
      return this.sb.length();
    }

    void append(String paramString)
    {
      if (this.bound == 0)
      {
        this.sb.append(paramString);
        return;
      }
      int i = 0;
      int j = this.sb.length() + paramString.length();
      if (j > this.bound)
        i = j - this.bound;
      if (i > this.sb.length())
        clear();
      else
        this.sb.delete(0, i);
      this.sb.append(paramString);
    }

    void clear()
    {
      this.sb.delete(0, this.sb.length());
    }

    void setBound(int paramInt)
    {
      this.bound = paramInt;
    }
  }

  class ConsoleWriterThread extends Thread
  {
    public ConsoleWriterThread()
    {
      super();
      setDaemon(true);
    }

    public void run()
    {
      long l1 = System.currentTimeMillis();
      while (ConsoleTraceListener.this.running)
      {
        long l2 = System.currentTimeMillis();
        String str;
        synchronized (ConsoleTraceListener.this.buffer)
        {
          if ((l2 - l1 >= 100L) && (ConsoleTraceListener.this.buffer.length() > 0))
          {
            str = ConsoleTraceListener.this.buffer.toString();
            ConsoleTraceListener.this.buffer.clear();
            l1 = l2;
          }
          else
          {
            try
            {
              if (ConsoleTraceListener.this.buffer.length() == 0)
                ConsoleTraceListener.this.buffer.wait();
              else
                ConsoleTraceListener.this.buffer.wait(100L - (l2 - l1));
            }
            catch (InterruptedException localInterruptedException)
            {
              Thread.interrupted();
            }
            continue;
          }
        }
        ConsoleTraceListener.this.console.append(str);
      }
    }

    public void flush()
    {
      String str;
      synchronized (ConsoleTraceListener.this.buffer)
      {
        if (ConsoleTraceListener.this.buffer.length() > 0)
        {
          str = ConsoleTraceListener.this.buffer.toString();
          ConsoleTraceListener.this.buffer.clear();
        }
        else
        {
          return;
        }
      }
      ConsoleTraceListener.this.console.append(str);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.ui.ConsoleTraceListener
 * JD-Core Version:    0.6.0
 */