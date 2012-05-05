package com.sun.javaws.ui;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.javaws.JnlpxArgs;
import com.sun.javaws.jnl.LaunchDesc;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SplashScreen
{
  private static boolean _alreadyHidden = false;
  private static final int HIDE_SPASH_SCREEN_TOKEN = 90;

  public static void hide()
  {
    hide(JnlpxArgs.getSplashPort());
  }

  private static void hide(int paramInt)
  {
    if ((paramInt <= 0) || (_alreadyHidden))
      return;
    _alreadyHidden = true;
    Socket localSocket = null;
    try
    {
      localSocket = new Socket("127.0.0.1", paramInt);
      if (localSocket != null)
      {
        OutputStream localOutputStream = localSocket.getOutputStream();
        try
        {
          localOutputStream.write(90);
          localOutputStream.flush();
        }
        catch (IOException localIOException3)
        {
        }
        localOutputStream.close();
      }
    }
    catch (IOException localIOException1)
    {
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    if (localSocket != null)
      try
      {
        localSocket.close();
      }
      catch (IOException localIOException2)
      {
        Trace.println("exception closing socket: " + localIOException2, TraceLevel.BASIC);
      }
  }

  public static void generateCustomSplash(LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    if (!Cache.isCacheEnabled())
      return;
    if (paramLaunchDesc.isApplicationDescriptor())
    {
      SplashGenerator localSplashGenerator = new SplashGenerator(paramLaunchDesc);
      if ((paramBoolean) || (localSplashGenerator.needsCustomSplash()))
        localSplashGenerator.start();
    }
  }

  public static void removeCustomSplash(LaunchDesc paramLaunchDesc)
  {
    if (paramLaunchDesc.isApplicationDescriptor())
    {
      SplashGenerator localSplashGenerator = new SplashGenerator(paramLaunchDesc);
      localSplashGenerator.remove();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.SplashScreen
 * JD-Core Version:    0.6.0
 */