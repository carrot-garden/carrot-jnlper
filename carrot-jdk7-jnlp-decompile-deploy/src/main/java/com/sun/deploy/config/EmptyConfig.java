package com.sun.deploy.config;

import java.util.Properties;
import java.util.Vector;

public class EmptyConfig extends Config
{
  public boolean init(String paramString1, String paramString2)
  {
    return false;
  }

  public boolean isPropertyLocked(String paramString)
  {
    return false;
  }

  public void storeIfNeeded()
  {
  }

  public void refreshIfNeeded()
  {
  }

  public boolean getSuccessShown()
  {
    return false;
  }

  public void storeInstalledJREs(Vector paramVector)
  {
  }

  public String getEnterpriseString()
  {
    return "";
  }

  public boolean isValid()
  {
    return true;
  }

  public boolean isConfigDirty()
  {
    return false;
  }

  public boolean getJqs()
  {
    return true;
  }

  public boolean getJavaPlugin()
  {
    return true;
  }

  public Properties getSystemProps()
  {
    return new Properties();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.EmptyConfig
 * JD-Core Version:    0.6.0
 */