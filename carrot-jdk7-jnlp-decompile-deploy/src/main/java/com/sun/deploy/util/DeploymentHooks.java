package com.sun.deploy.util;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class DeploymentHooks
{
  static boolean usageTrackerFailed = false;
  Object usageTracker;
  Method runMethod;

  public void preLaunch(String paramString1, String paramString2)
  {
    synchronized (this)
    {
      if (usageTrackerFailed)
        return;
      if (this.runMethod == null)
        try
        {
          Class localClass = Class.forName("sun.usagetracker.UsageTrackerClient", true, null);
          Constructor localConstructor = localClass.getConstructor(new Class[0]);
          this.usageTracker = localConstructor.newInstance(new Object[0]);
          this.runMethod = localClass.getMethod("run", new Class[] { String.class, String.class });
        }
        catch (Throwable localThrowable2)
        {
          usageTrackerFailed = true;
          return;
        }
    }
    try
    {
      ??? = new Object[] { paramString1, paramString2 };
      this.runMethod.invoke(this.usageTracker, ???);
    }
    catch (Throwable localThrowable1)
    {
      Trace.println("Error invoking UsageTracker:", TraceLevel.BASIC);
      Trace.ignored(localThrowable1);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.DeploymentHooks
 * JD-Core Version:    0.6.0
 */