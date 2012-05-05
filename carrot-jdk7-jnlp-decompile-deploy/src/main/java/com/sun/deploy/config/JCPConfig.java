package com.sun.deploy.config;

import com.sun.deploy.panel.ControlPanel;
import java.util.Properties;

public class JCPConfig extends ClientConfig
{
  public ControlPanel _jcp;

  public JCPConfig(ControlPanel paramControlPanel)
  {
    this._jcp = paramControlPanel;
  }

  public Object setProperty(String paramString1, String paramString2)
  {
    if (isDiskNewer())
      refreshIfNeeded();
    String str = super.getProperty(paramString1);
    if ((paramString2 == null) || (paramString2.length() == 0))
    {
      this._dirty |= containsKey(paramString1);
      remove(paramString1);
      this._changedProps.remove(paramString1);
    }
    else if (!paramString2.equals(str))
    {
      this._dirty = true;
      super.setProperty(paramString1, paramString2);
      this._changedProps.setProperty(paramString1, paramString2);
      if (this._jcp != null)
        ControlPanel.propertyChanged(true);
    }
    return str;
  }

  public void storeConfig()
  {
    super.storeConfig();
    if (this._jcp != null)
      ControlPanel.propertyChanged(false);
  }

  public void storeIfNeeded()
  {
    this._dirty = true;
    super.storeIfNeeded();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.JCPConfig
 * JD-Core Version:    0.6.0
 */