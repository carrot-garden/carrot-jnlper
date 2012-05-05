package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.ComponentRef;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.MessageFormat;

public class DeployAuthenticator extends Authenticator
  implements AuthKey
{
  private static final String SCHEME_NTLM = "NTLM";
  private static final String SCHEME_DIGEST = "DIGEST";
  private static final String SCHEME_BASIC = "BASIC";
  private final long CANCEL_DURATION = 3000L;
  private final int ACTIVE = 1;
  private final int CANCEL = 2;
  protected ComponentRef parentComponent = null;
  private CredentialManager cm;
  private boolean cmInitialized;
  private StateMonitor stateMonitor = new StateMonitor(null);

  private CredentialManager getCredentialManager()
  {
    if (!this.cmInitialized)
    {
      this.cmInitialized = true;
      this.cm = ServiceManager.getService().getCredentialManager();
    }
    return this.cm;
  }

  private BrowserAuthenticator getBrowserAuthenticator()
  {
    Service localService = ServiceManager.getService();
    return localService.getBrowserAuthenticator();
  }

  protected synchronized PasswordAuthentication getPasswordAuthentication()
  {
    PasswordAuthentication localPasswordAuthentication = null;
    if (Config.isJavaVersionAtLeast15())
    {
      getCredentialManager();
      if (this.stateMonitor.getState(CredentialManager.buildConnectionKey(this)) == 2)
        return null;
    }
    try
    {
      InetAddress localInetAddress = getRequestingSite();
      String str;
      if (localInetAddress != null)
      {
        str = localInetAddress.toString();
      }
      else
      {
        str = getHost();
        if ((str == null) || (str.length() == 0))
          str = getMessage("net.authenticate.unknownSite");
      }
      StringBuffer localStringBuffer = new StringBuffer();
      localStringBuffer.append("Firewall authentication: site=");
      localStringBuffer.append(getRequestingSite());
      localStringBuffer.append(":" + getRequestingPort());
      localStringBuffer.append(", protocol=");
      localStringBuffer.append(getRequestingProtocol());
      localStringBuffer.append(", prompt=");
      localStringBuffer.append(getRequestingPrompt());
      localStringBuffer.append(", scheme=");
      localStringBuffer.append(getRequestingScheme());
      Trace.netPrintln(localStringBuffer.toString());
      CredentialInfo localCredentialInfo1 = new CredentialInfo();
      CredentialInfo localCredentialInfo2 = null;
      if (Config.isJavaVersionAtLeast15())
      {
        if (getCredentialManager() != null)
          localCredentialInfo1 = getCredentialManager().getCredential(this);
        if ((localCredentialInfo1.isCredentialEmpty()) || (!getCredentialManager().isCredentialValid(localCredentialInfo1)))
        {
          localCredentialInfo1 = CredentialInfo.passAuthToCredentialInfo(getBrowserCredential());
          if (getCredentialManager() != null)
            localCredentialInfo1.setSessionId(getCredentialManager().getLoginSessionId());
        }
        if ((getCredentialManager() == null) || (!getCredentialManager().isCredentialValid(localCredentialInfo1)))
        {
          localCredentialInfo2 = openDialog(str, getRequestingPrompt(), getRequestingScheme(), localCredentialInfo1);
          if (localCredentialInfo2 != null)
          {
            getCredentialManager().saveCredential(this, localCredentialInfo2);
            localPasswordAuthentication = localCredentialInfo2.getPasswordAuthentication();
          }
          else
          {
            getCredentialManager();
            this.stateMonitor.setCancel(CredentialManager.buildConnectionKey(this));
          }
        }
        else
        {
          localPasswordAuthentication = localCredentialInfo1.getPasswordAuthentication();
        }
      }
      else
      {
        localCredentialInfo2 = openDialog(str, getRequestingPrompt(), getRequestingScheme(), localCredentialInfo1);
        localPasswordAuthentication = localCredentialInfo2.getPasswordAuthentication();
      }
    }
    catch (Exception localException)
    {
      Trace.netPrintException(localException);
    }
    return localPasswordAuthentication;
  }

  private PasswordAuthentication getBrowserCredential()
  {
    PasswordAuthentication localPasswordAuthentication = null;
    BrowserAuthenticator localBrowserAuthenticator = getBrowserAuthenticator();
    if (localBrowserAuthenticator != null)
      localPasswordAuthentication = localBrowserAuthenticator.getAuthentication(getRequestingProtocol(), getHost(), getRequestingPort(), getRequestingScheme(), getRequestingPrompt(), getURL(), isProxy());
    return localPasswordAuthentication;
  }

  private CredentialInfo openDialog(String paramString1, String paramString2, String paramString3, CredentialInfo paramCredentialInfo)
  {
    if (paramString1 == null)
      paramString1 = "";
    if ((paramString2 == null) || (paramString2.trim().equals("")))
      paramString2 = "<default>";
    boolean bool1 = false;
    String str1 = null;
    if (paramString3 != null)
    {
      if (paramString3.equalsIgnoreCase("BASIC"))
      {
        str1 = getMessage("net.authenticate.basic.display.string");
      }
      else if (paramString3.equalsIgnoreCase("DIGEST"))
      {
        str1 = getMessage("net.authenticate.digest.display.string");
      }
      else if (paramString3.equalsIgnoreCase("NTLM"))
      {
        str1 = getMessage("net.authenticate.ntlm.display.string");
        bool1 = true;
      }
      else
      {
        str1 = getMessage("net.authenticate.unknown.display.string");
      }
    }
    else
      str1 = getMessage("net.authenticate.unknown.display.string");
    MessageFormat localMessageFormat = new MessageFormat(getMessage("net.authenticate.text"));
    Object[] arrayOfObject = { paramString2, paramString1 };
    String str2 = localMessageFormat.format(arrayOfObject);
    CredentialInfo localCredentialInfo = null;
    boolean bool2 = false;
    if (getCredentialManager() != null)
      bool2 = getCredentialManager().isPasswordEncryptionSupported();
    try
    {
      localCredentialInfo = ToolkitStore.getUI().showPasswordDialog(this.parentComponent == null ? null : this.parentComponent.get(), getMessage("password.dialog.title"), str2, true, bool1, paramCredentialInfo, bool2, str1);
    }
    catch (Exception localException)
    {
      Trace.securityPrintException(localException);
    }
    return localCredentialInfo;
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  public void setParentComponent(ComponentRef paramComponentRef)
  {
    this.parentComponent = paramComponentRef;
  }

  public String getProtocolScheme()
  {
    return getRequestingProtocol();
  }

  public String getHost()
  {
    try
    {
      return getRequestingHost();
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
    }
    return "";
  }

  public int getPort()
  {
    return getRequestingPort();
  }

  public String getPath()
  {
    URL localURL = getURL();
    if (localURL != null)
      return localURL.getPath();
    return null;
  }

  public boolean isProxy()
  {
    try
    {
      return getRequestorType() == Authenticator.RequestorType.PROXY;
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
    }
    return false;
  }

  public URL getURL()
  {
    try
    {
      return getRequestingURL();
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
    }
    return null;
  }

  private class StateMonitor
  {
    private int currentState;
    private long timeOfLastCancel;
    private String siteKey;
    private final DeployAuthenticator this$0;

    private StateMonitor()
    {
      this.this$0 = this$1;
      this.currentState = 1;
      this.timeOfLastCancel = 0L;
      this.siteKey = "";
    }

    private void setCancel(String paramString)
    {
      this.currentState = 2;
      this.timeOfLastCancel = System.currentTimeMillis();
      this.siteKey = trimPath(paramString);
    }

    private int getState(String paramString)
    {
      int i = 1;
      if (this.currentState == 2)
        if (System.currentTimeMillis() - this.timeOfLastCancel > 3000L)
          this.currentState = 1;
        else if (paramString.startsWith(this.siteKey))
          i = 2;
      return i;
    }

    public String trimPath(String paramString)
    {
      int i = paramString.lastIndexOf('/');
      String str = paramString.substring(0, i + 1);
      return str;
    }

    StateMonitor(DeployAuthenticator.1 arg2)
    {
      this();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.DeployAuthenticator
 * JD-Core Version:    0.6.0
 */