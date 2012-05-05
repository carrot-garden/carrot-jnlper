package com.sun.deploy.config;

import com.sun.deploy.trace.Trace;

public class VerboseDefaultConfig extends DefaultConfig
{
  private static DefaultConfig _defaultConfigInstance;
  private static boolean _constructionComplete = false;

  public static synchronized DefaultConfig getDefaultConfig()
  {
    if (_defaultConfigInstance == null)
    {
      _defaultConfigInstance = new VerboseDefaultConfig();
      _constructionComplete = true;
    }
    return _defaultConfigInstance;
  }

  public String getProperty(String paramString)
  {
    String str = super.getProperty(paramString);
    if ((_constructionComplete) && (Config.getDeployDebug()))
    {
      Trace.println("--- Warning --- VerboseDefaultConfig.getProperty(" + paramString + "); called, returning default value: " + str);
      new Throwable().printStackTrace();
    }
    return str;
  }

  public Object setProperty(String paramString1, String paramString2)
  {
    if ((_constructionComplete) && (Config.getDeployDebug()))
    {
      Trace.println("--- Warning --- VerboseDefaultConfig.setProperty(" + paramString1 + ", " + paramString2 + "); called,  value was: " + super.getProperty(paramString1));
      new Throwable().printStackTrace();
    }
    return super.setProperty(paramString1, paramString2);
  }

  public void storeIfNeeded()
  {
    if (Config.getDeployDebug())
      Trace.println("--- Warning --- VerboseDefaultConfig.storeIfNeeded()");
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.VerboseDefaultConfig
 * JD-Core Version:    0.6.0
 */