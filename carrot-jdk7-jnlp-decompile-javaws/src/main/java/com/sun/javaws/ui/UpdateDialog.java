package com.sun.javaws.ui;

import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.UpdateDesc;

public class UpdateDialog
{
  private static final String PROMPT_RUN = "update.dialog.prompt-run";
  private static final String PROMPT_UPDATE = "update.dialog.prompt-update";
  private static final String TITLE_KEY = "update.dialog.title";

  public static boolean showUpdateDialog(LaunchDesc paramLaunchDesc, Preloader paramPreloader)
  {
    String str1 = getKey(paramLaunchDesc);
    String str2 = paramLaunchDesc.getInformation().getTitle();
    String str3 = ResourceManager.getString(str1, str2);
    String str4 = ResourceManager.getMessage("update.dialog.title");
    String str5 = ResourceManager.getString("common.ok_btn");
    String str6 = ResourceManager.getString("common.cancel_btn");
    Object localObject = paramPreloader == null ? null : paramPreloader.getOwner();
    ToolkitStore.getUI();
    int i = ToolkitStore.getUI().showMessageDialog(localObject, paramLaunchDesc.getAppInfo(), 3, str4, null, str3, null, str5, str6, null);
    ToolkitStore.getUI();
    return i == 0;
  }

  private static String getKey(LaunchDesc paramLaunchDesc)
  {
    return paramLaunchDesc.getUpdate().getPolicy() == 2 ? "update.dialog.prompt-run" : "update.dialog.prompt-update";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.UpdateDialog
 * JD-Core Version:    0.6.0
 */