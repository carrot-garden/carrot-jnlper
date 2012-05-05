package com.sun.jnlp;

import com.sun.deploy.config.Platform;
import com.sun.deploy.si.DeploySIListener;
import com.sun.deploy.si.SingleInstanceImpl;
import com.sun.deploy.si.SingleInstanceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.javaws.Globals;
import com.sun.javaws.JnlpxArgs;
import com.sun.javaws.Main;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.jnl.ApplicationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.XMLFormat;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;

public final class SingleInstanceServiceImpl extends SingleInstanceImpl
  implements SingleInstanceService
{
  private static SingleInstanceServiceImpl _sharedInstance = null;
  private static boolean listenerExists = false;

  public static synchronized SingleInstanceServiceImpl getInstance()
  {
    if (_sharedInstance == null)
      _sharedInstance = new SingleInstanceServiceImpl();
    return _sharedInstance;
  }

  public synchronized void addSingleInstanceListener(SingleInstanceListener paramSingleInstanceListener)
  {
    if (paramSingleInstanceListener == null)
      return;
    LaunchDesc localLaunchDesc = JNLPClassLoaderUtil.getInstance().getLaunchDesc();
    URL localURL = localLaunchDesc.getCanonicalHome();
    int i = localURL.toString().lastIndexOf('?');
    if (i != -1)
      try
      {
        localURL = new URL(localURL.toString().substring(0, i));
      }
      catch (MalformedURLException localMalformedURLException)
      {
        Trace.ignoredException(localMalformedURLException);
      }
    String str = localURL.toString();
    if (!listenerExists)
      AccessController.doPrivileged(new PrivilegedAction(str, localLaunchDesc)
      {
        private final String val$jnlpUrlString;
        private final LaunchDesc val$ld;

        public Object run()
        {
          if (SingleInstanceManager.isServerRunning(this.val$jnlpUrlString))
          {
            String[] arrayOfString = Globals.getApplicationArgs();
            if (arrayOfString != null)
            {
              ApplicationDesc localApplicationDesc = this.val$ld.getApplicationDescriptor();
              if (localApplicationDesc != null)
                localApplicationDesc.setArguments(arrayOfString);
            }
            if (SingleInstanceManager.connectToServer(this.val$ld.toString()))
              try
              {
                Main.systemExit(0);
              }
              catch (ExitException localExitException)
              {
                Trace.println("systemExit: " + localExitException, TraceLevel.BASIC);
                Trace.ignoredException(localExitException);
              }
          }
          return null;
        }
      });
    super.addSingleInstanceListener(new TransferListener(paramSingleInstanceListener), str);
    listenerExists = true;
  }

  public void removeSingleInstanceListener(SingleInstanceListener paramSingleInstanceListener)
  {
    super.removeSingleInstanceListener(new TransferListener(paramSingleInstanceListener));
  }

  public boolean isSame(String paramString1, String paramString2)
  {
    LaunchDesc localLaunchDesc = null;
    try
    {
      localLaunchDesc = XMLFormat.parse(paramString1.getBytes(), null, null, null);
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    if (localLaunchDesc != null)
    {
      URL localURL = localLaunchDesc.getCanonicalHome();
      int i = localURL.toString().lastIndexOf('?');
      if (i != -1)
        try
        {
          localURL = new URL(localURL.toString().substring(0, i));
        }
        catch (MalformedURLException localMalformedURLException)
        {
          Trace.ignoredException(localMalformedURLException);
        }
      String str = localURL.toString() + Platform.get().getSessionSpecificString();
      Trace.println("GOT: " + str, TraceLevel.BASIC);
      if (paramString2.equals(str))
        return true;
    }
    return false;
  }

  public String[] getArguments(String paramString1, String paramString2)
  {
    LaunchDesc localLaunchDesc = null;
    try
    {
      localLaunchDesc = XMLFormat.parse(paramString1.getBytes(), null, null, null);
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    if (localLaunchDesc != null)
    {
      if (localLaunchDesc.isApplication())
        return localLaunchDesc.getApplicationDescriptor().getArguments();
      return super.getArguments(paramString1, paramString2);
    }
    return new String[0];
  }

  private class TransferListener
    implements DeploySIListener
  {
    SingleInstanceListener _sil;

    public TransferListener(SingleInstanceListener arg2)
    {
      Object localObject;
      this._sil = localObject;
    }

    public void newActivation(String[] paramArrayOfString)
    {
      if (paramArrayOfString.length == 2)
      {
        String str1 = SingleInstanceManager.getOpenPrintFilePath();
        String str2 = SingleInstanceManager.getActionName();
        if ((str1 != null) && (str2 != null) && ((str2.equals("-open")) || (str2.equals("-print"))) && (str1.equals(paramArrayOfString[1])) && (str2.equals(paramArrayOfString[0])))
        {
          JnlpxArgs.getFileReadWriteList().add(paramArrayOfString[1]);
          SingleInstanceManager.setOpenPrintFilePath(null);
          SingleInstanceManager.setActionName(null);
        }
      }
      this._sil.newActivation(paramArrayOfString);
    }

    public Object getSingleInstanceListener()
    {
      return this._sil;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.SingleInstanceServiceImpl
 * JD-Core Version:    0.6.0
 */