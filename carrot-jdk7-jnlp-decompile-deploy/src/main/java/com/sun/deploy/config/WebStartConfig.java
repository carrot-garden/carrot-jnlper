package com.sun.deploy.config;

public class WebStartConfig extends ClientConfig
{
  public boolean init(String paramString1, String paramString2)
  {
    boolean bool = super.init(paramString1, paramString2);
    try
    {
      for (int i = 0; i < PROXY_KEYS.length; i++)
      {
        String str1 = PROXY_KEYS[i];
        String str2 = str1.replaceFirst("deployment.", "deployment.javaws.");
        String str3 = "active." + str1;
        if (getProperty(str2) != null)
          setProperty(str3, getProperty(str2));
        else
          setProperty(str3, getProperty(str1));
      }
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
      localNoSuchMethodError.printStackTrace();
    }
    return bool;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.WebStartConfig
 * JD-Core Version:    0.6.0
 */