package com.sun.jnlp;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.javaws.jnl.LaunchDesc;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

public final class ApiDialog
{
  private boolean _remembered = false;
  private boolean _answer = false;
  private boolean _cbChecked = false;
  private String _initMessage = null;
  private HashSet _connect = new HashSet();
  private HashSet _connectNo = new HashSet();
  private HashSet _accept = new HashSet();
  private HashSet _acceptNo = new HashSet();

  boolean askUser(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, boolean paramBoolean)
  {
    if (!Config.getBooleanProperty("deployment.security.sandbox.jnlp.enhanced"))
      return false;
    if (this._remembered)
      return this._answer;
    AppInfo localAppInfo = JNLPClassLoaderUtil.getInstance().getLaunchDesc().getAppInfo();
    ToolkitStore.getUI();
    int i = ToolkitStore.getUI().showMessageDialog(null, localAppInfo, 7, paramString1, null, paramString2, paramString4, paramString5, paramString3, null);
    ToolkitStore.getUI();
    if (i == 2)
    {
      this._remembered = true;
      this._answer = true;
    }
    else
    {
      ToolkitStore.getUI();
      this._answer = (i == 0);
    }
    return this._answer;
  }

  boolean askUser(String paramString1, String paramString2, String paramString3)
  {
    return askUser(paramString1, paramString2, paramString3, null, null, false);
  }

  public boolean askConnect(String paramString)
  {
    if (this._connect.contains(paramString))
      return true;
    if (this._connectNo.contains(paramString))
      return false;
    String str1 = ResourceManager.getString("api.ask.host.title");
    String str2 = ResourceManager.getString("api.ask.connect", paramString);
    if (askUser(str1, str2, null))
    {
      this._connect.add(paramString);
      try
      {
        String str3 = InetAddress.getByName(paramString).getHostAddress();
        this._connect.add(str3);
      }
      catch (UnknownHostException localUnknownHostException)
      {
        Trace.ignored(localUnknownHostException);
      }
      return true;
    }
    this._connectNo.add(paramString);
    return false;
  }

  public boolean askAccept(String paramString)
  {
    if (this._accept.contains(paramString))
      return true;
    if (this._acceptNo.contains(paramString))
      return false;
    try
    {
      paramString = InetAddress.getByName(paramString).getHostAddress();
    }
    catch (UnknownHostException localUnknownHostException)
    {
      Trace.ignored(localUnknownHostException);
    }
    if (this._accept.contains(paramString))
      return true;
    if (this._acceptNo.contains(paramString))
      return false;
    String str1 = ResourceManager.getString("api.ask.host.title");
    String str2 = ResourceManager.getString("api.ask.accept", paramString);
    if (askUser(str1, str2, null))
    {
      this._accept.add(paramString);
      return true;
    }
    this._acceptNo.add(paramString);
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.ApiDialog
 * JD-Core Version:    0.6.0
 */