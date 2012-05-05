package com.sun.deploy.uitoolkit.impl.text;

import com.sun.deploy.appcontext.AppContext;
import java.util.HashMap;

public class FXAppContext
  implements AppContext
{
  private HashMap storage = new HashMap();

  public static synchronized FXAppContext getInstance()
  {
    return new FXAppContext();
  }

  public Object get(Object paramObject)
  {
    return this.storage.get(paramObject);
  }

  public Object put(Object paramObject1, Object paramObject2)
  {
    return this.storage.put(paramObject1, paramObject2);
  }

  public Object remove(Object paramObject)
  {
    return this.storage.remove(paramObject);
  }

  public void invokeLater(Runnable paramRunnable)
  {
    paramRunnable.run();
  }

  public void invokeAndWait(Runnable paramRunnable)
  {
    paramRunnable.run();
  }

  public ThreadGroup getThreadGroup()
  {
    return Thread.currentThread().getThreadGroup();
  }

  public void dispose()
  {
    this.storage.clear();
  }

  public boolean destroy(long paramLong)
  {
    return true;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.text.FXAppContext
 * JD-Core Version:    0.6.0
 */