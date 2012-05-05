package com.sun.deploy.appcontext;

import java.lang.reflect.InvocationTargetException;

public abstract interface AppContext
{
  public abstract Object get(Object paramObject);

  public abstract Object put(Object paramObject1, Object paramObject2);

  public abstract Object remove(Object paramObject);

  public abstract void invokeLater(Runnable paramRunnable);

  public abstract void invokeAndWait(Runnable paramRunnable)
    throws InterruptedException, InvocationTargetException;

  public abstract ThreadGroup getThreadGroup();

  public abstract void dispose();

  public abstract boolean destroy(long paramLong);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.appcontext.AppContext
 * JD-Core Version:    0.6.0
 */