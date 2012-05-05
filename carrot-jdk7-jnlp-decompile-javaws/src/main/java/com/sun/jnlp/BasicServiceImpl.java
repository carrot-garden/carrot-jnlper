package com.sun.jnlp;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.offline.DeployOfflineManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.PerfLogger;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.BrowserSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.BasicService;
import sun.awt.DesktopBrowse;

public final class BasicServiceImpl
  implements BasicService
{
  private URL _codebase = null;
  private String _codebaseProtocol = null;
  private boolean _isWebBrowserSupported;
  private static BasicServiceImpl _sharedInstance = null;

  private BasicServiceImpl(URL paramURL, boolean paramBoolean, String paramString)
  {
    this._codebaseProtocol = paramString;
    this._codebase = paramURL;
    this._isWebBrowserSupported = paramBoolean;
    if (Config.isJavaVersionAtLeast16())
      try
      {
        DesktopBrowse.setInstance(new BasicServiceBrowser(null));
      }
      catch (IllegalStateException localIllegalStateException)
      {
        if (Config.getDeployDebug())
          localIllegalStateException.printStackTrace(System.out);
      }
      catch (Throwable localThrowable)
      {
        Trace.ignored(localThrowable);
      }
  }

  public static BasicServiceImpl getInstance()
  {
    return _sharedInstance;
  }

  public static void initialize(URL paramURL, boolean paramBoolean, String paramString)
  {
    if (_sharedInstance == null)
      _sharedInstance = new BasicServiceImpl(paramURL, paramBoolean, paramString);
  }

  public URL getCodeBase()
  {
    return this._codebase;
  }

  public boolean isOffline()
  {
    return DeployOfflineManager.isGlobalOffline();
  }

  private boolean isFileProtocolCodebase()
  {
    return (this._codebaseProtocol != null) && (this._codebaseProtocol.equalsIgnoreCase("file"));
  }

  public boolean showDocument(URL paramURL)
  {
    if (paramURL == null)
      return false;
    Object localObject;
    if (!URLUtil.checkTargetURL(this._codebase, paramURL))
    {
      localObject = paramURL;
      int i = 0;
      if ("jar".equals(paramURL.getProtocol()))
        try
        {
          localObject = new URL(paramURL.toString().substring(4));
        }
        catch (MalformedURLException localMalformedURLException2)
        {
          Trace.ignoredException(localMalformedURLException2);
          return i;
        }
      if ("file".equals(((URL)localObject).getProtocol()))
        try
        {
          File localFile = new File(((URL)localObject).getFile());
          localFile.canRead();
          i = 1;
        }
        catch (SecurityException localSecurityException)
        {
        }
      if (i == 0)
        throw new SecurityException("ShowDocument url permission denied");
    }
    if (Config.isJavaVersionAtLeast16())
    {
      localObject = DesktopBrowse.getInstance();
      if ((localObject != null) && (!(localObject instanceof BasicServiceBrowser)))
      {
        if (!isWebBrowserSupported())
          return false;
        try
        {
          paramURL = new URL(this._codebase, paramURL.toString());
        }
        catch (MalformedURLException localMalformedURLException1)
        {
        }
        ((DesktopBrowse)localObject).browse(paramURL);
        return true;
      }
    }
    return showDocumentHelper(paramURL);
  }

  private boolean showDocumentHelper(URL paramURL)
  {
    boolean bool = paramURL.toString().toLowerCase().endsWith(".jnlp");
    int i = 0;
    try
    {
      File localFile = DownloadEngine.getCachedFile(paramURL);
      i = localFile != null ? 1 : 0;
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    if ((bool) || (i != 0))
    {
      localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(paramURL)
      {
        private final URL val$url;

        public Object run()
        {
          if (DownloadEngine.isJnlpURL(this.val$url))
            try
            {
              String str = DownloadEngine.getCachedResourceFilePath(this.val$url, null);
              if (str != null)
              {
                String[] arrayOfString = new String[3];
                arrayOfString[0] = Environment.getJavawsCommand();
                arrayOfString[1] = "-Xnosplash";
                arrayOfString[2] = str;
                Runtime.getRuntime().exec(arrayOfString);
                return new Boolean(true);
              }
            }
            catch (Exception localException)
            {
              Trace.ignored(localException);
            }
          return new Boolean(false);
        }
      });
      if ((localBoolean != null) && (localBoolean.booleanValue() == true))
        return true;
    }
    if (!isWebBrowserSupported())
      return false;
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(paramURL)
    {
      private final URL val$url;

      public Object run()
      {
        Object localObject = this.val$url;
        try
        {
          URL localURL = new URL(BasicServiceImpl.this._codebase, ((URL)localObject).toString());
          localObject = localURL;
        }
        catch (MalformedURLException localMalformedURLException)
        {
        }
        return new Boolean(BrowserSupport.showDocument((URL)localObject));
      }
    });
    return localBoolean == null ? false : localBoolean.booleanValue();
  }

  public boolean isWebBrowserSupported()
  {
    PerfLogger.setEndTime("BasicService.isWebBrowserSupported called");
    PerfLogger.outputLog();
    return this._isWebBrowserSupported;
  }

  public void logPerfStartMessage(String paramString)
  {
    PerfLogger.setStartTime(paramString);
  }

  public void logPerfEndMessage(String paramString)
  {
    PerfLogger.setEndTime(paramString);
  }

  public void logPerfTime(String paramString)
  {
    PerfLogger.setTime(paramString);
  }

  private class BasicServiceBrowser extends DesktopBrowse
  {
    private final BasicServiceImpl this$0;

    private BasicServiceBrowser()
    {
      this.this$0 = this$1;
    }

    public void browse(URL paramURL)
    {
      this.this$0.showDocument(paramURL);
    }

    BasicServiceBrowser(BasicServiceImpl.1 arg2)
    {
      this();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.BasicServiceImpl
 * JD-Core Version:    0.6.0
 */