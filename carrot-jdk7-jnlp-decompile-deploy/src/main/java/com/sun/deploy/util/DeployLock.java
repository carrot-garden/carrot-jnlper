package com.sun.deploy.util;

public class DeployLock
{
  private Thread lockingThread = null;

  public synchronized boolean lock()
    throws InterruptedException
  {
    int i = 0;
    Thread localThread = Thread.currentThread();
    if (this.lockingThread != localThread)
    {
      while ((this.lockingThread != null) && (this.lockingThread != localThread))
        wait();
      i = 1;
    }
    this.lockingThread = localThread;
    return i;
  }

  public synchronized void unlock()
  {
    if (this.lockingThread != Thread.currentThread())
      throw new IllegalMonitorStateException("Calling thread has not locked this lock");
    this.lockingThread = null;
    notify();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.DeployLock
 * JD-Core Version:    0.6.0
 */