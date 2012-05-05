package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.security.DeployAuthenticator;
import com.sun.deploy.uitoolkit.ui.ComponentRef;
import java.net.PasswordAuthentication;

public class JAuthenticator extends DeployAuthenticator
{
  private static JAuthenticator _instance;
  private boolean _challanging = false;
  private boolean _cancel = false;

  public static synchronized JAuthenticator getInstance(ComponentRef paramComponentRef)
  {
    if (_instance == null)
      _instance = new JAuthenticator();
    _instance.setParentComponent(paramComponentRef);
    return _instance;
  }

  protected synchronized PasswordAuthentication getPasswordAuthentication()
  {
    PasswordAuthentication localPasswordAuthentication = null;
    if (Config.getBooleanProperty("deployment.security.authenticator"))
    {
      this._challanging = true;
      localPasswordAuthentication = super.getPasswordAuthentication();
      this._challanging = false;
    }
    return localPasswordAuthentication;
  }

  public boolean isChallanging()
  {
    return this._challanging;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.JAuthenticator
 * JD-Core Version:    0.6.0
 */