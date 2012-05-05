package com.sun.javaws.security;

import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;

public class AppContextUtil
{
  private static AppContext _mainAppContext = null;
  private static AppContext _securityAppContext = null;

  public static void createSecurityAppContext()
  {
    if (_mainAppContext == null)
      _mainAppContext = ToolkitStore.get().getAppContext();
    if (_securityAppContext == null)
    {
      ToolkitStore.get().createAppContext();
      _securityAppContext = ToolkitStore.get().getAppContext();
    }
  }

  public static boolean isSecurityAppContext()
  {
    return ToolkitStore.get().getAppContext().equals(_securityAppContext);
  }

  public static boolean isApplicationAppContext()
  {
    return ToolkitStore.get().getAppContext().equals(_mainAppContext);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.security.AppContextUtil
 * JD-Core Version:    0.6.0
 */