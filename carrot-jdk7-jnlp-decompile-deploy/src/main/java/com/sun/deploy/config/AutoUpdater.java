package com.sun.deploy.config;

import com.sun.deploy.Environment;
import java.util.Timer;
import java.util.TimerTask;

public class AutoUpdater
{
  private boolean done = false;
  private boolean enabled = !"false".equals(Environment.getenv("JAVA_AUTOUPDATE"));
  protected Relauncher relauncher;

  public synchronized void checkForUpdate(Relauncher paramRelauncher)
  {
    if ((!this.done) && (this.enabled))
    {
      this.done = true;
      this.relauncher = paramRelauncher;
      Timer localTimer = new Timer(true);
      1 local1 = new TimerTask(localTimer)
      {
        private final Timer val$t;

        public void run()
        {
          try
          {
            AutoUpdater.this.initiateUpdateCheck();
          }
          finally
          {
            this.val$t.cancel();
          }
        }
      };
      localTimer.schedule(local1, 10000L);
    }
  }

  protected void initiateUpdateCheck()
  {
  }

  public static abstract class Relauncher
  {
    public abstract void relaunch();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.AutoUpdater
 * JD-Core Version:    0.6.0
 */