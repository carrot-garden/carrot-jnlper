package com.sun.deploy.util;

public abstract class Waiter
{
  public static final Waiter SYNC_ALWAYS = new Waiter()
  {
    protected Object wait(Waiter.WaiterTask paramWaiterTask)
      throws Exception
    {
      return paramWaiterTask.run();
    }
  };
  private static Waiter default_waiter = SYNC_ALWAYS;

  protected abstract Object wait(WaiterTask paramWaiterTask)
    throws Exception;

  public static synchronized void set(Waiter paramWaiter)
  {
    default_waiter = paramWaiter;
  }

  public static synchronized Waiter get()
  {
    return default_waiter;
  }

  public static Object runAndWait(WaiterTask paramWaiterTask)
    throws Exception
  {
    return get().wait(paramWaiterTask);
  }

  public static abstract interface WaiterTask
  {
    public abstract Object run()
      throws Exception;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.Waiter
 * JD-Core Version:    0.6.0
 */