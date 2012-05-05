package com.sun.javaws.ui;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ResourcesDesc;

public class AutoDownloadPrompt
{
  public static int _result = -1;

  public static boolean prompt(Object paramObject, LaunchDesc paramLaunchDesc)
  {
    if (_result < 0)
    {
      String str1 = paramLaunchDesc.getResources().getSelectedJRE().getVersion();
      String str2 = ResourceManager.getString("download.jre.prompt.title");
      String str3 = ResourceManager.getString("download.jre.prompt", str1);
      String str4 = ResourceManager.getString("common.ok_btn");
      String str5 = ResourceManager.getString("common.cancel_btn");
      ToolkitStore.getUI();
      _result = ToolkitStore.getUI().showMessageDialog(paramObject, paramLaunchDesc.getAppInfo(), 3, str2, null, str3, null, str4, str5, null);
    }
    ToolkitStore.getUI();
    return _result == 0;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.AutoDownloadPrompt
 * JD-Core Version:    0.6.0
 */