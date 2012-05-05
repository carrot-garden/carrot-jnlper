package com.sun.deploy.net;

import java.util.HashMap;
import java.util.Map;

public class UpdateTracker
{
  private static Object DONE = new Object();
  private static Object PENDING = new Object();
  private static Map state = new HashMap();

  public static synchronized void checkDone(String paramString)
  {
    if (paramString != null)
      state.put(paramString, DONE);
  }

  public static synchronized void addPending(String paramString)
  {
    if ((paramString != null) && (state.get(paramString) != DONE))
      state.put(paramString, PENDING);
  }

  public static synchronized boolean isUpdated(String paramString)
  {
    return (paramString != null) && (state.get(paramString) == DONE);
  }

  public static synchronized boolean isUpdateCheckNeeded(String paramString)
  {
    return (paramString != null) && (state.get(paramString) == null);
  }

  public static synchronized void forceUpdate(String paramString)
  {
    state.remove(paramString);
  }

  public static void clear()
  {
    state.clear();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.UpdateTracker
 * JD-Core Version:    0.6.0
 */