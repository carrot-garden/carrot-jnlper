package com.sun.deploy.util;

public class SyncAccess
{
  public static final int READ_OP = 2;
  public static final int WRITE_OP = 4;
  public static final int SHARED_READ_MODE = 8;
  private int lockedOP = 0;
  private Object syncObj = new Object();
  private int mode;

  public SyncAccess(int paramInt)
  {
    this.mode = paramInt;
  }

  public Lock lock(int paramInt)
  {
    int i = 0;
    if ((paramInt == 2) && ((this.mode & 0x8) != 0))
      i = 4;
    else
      i = 6;
    return new Lock(i, paramInt, null);
  }

  private void acquireLock(int paramInt1, int paramInt2)
  {
    synchronized (this.syncObj)
    {
      while ((this.lockedOP & paramInt1) != 0)
        try
        {
          this.syncObj.wait();
        }
        catch (InterruptedException localInterruptedException)
        {
        }
      this.lockedOP |= paramInt2;
    }
  }

  private void releaseLock(int paramInt)
  {
    synchronized (this.syncObj)
    {
      this.lockedOP &= (paramInt ^ 0xFFFFFFFF);
      this.syncObj.notifyAll();
    }
  }

  public class Lock
  {
    private int op;
    private final SyncAccess this$0;

    private Lock(int paramInt1, int arg3)
    {
      this.this$0 = this$1;
      int i;
      this.op = i;
      this$1.acquireLock(paramInt1, i);
    }

    public void release()
    {
      this.this$0.releaseLock(this.op);
    }

    Lock(int paramInt1, int param1, SyncAccess.1 arg4)
    {
      this(paramInt1, param1);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.SyncAccess
 * JD-Core Version:    0.6.0
 */