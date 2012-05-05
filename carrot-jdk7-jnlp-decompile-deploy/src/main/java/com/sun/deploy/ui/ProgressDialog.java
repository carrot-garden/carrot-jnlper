package com.sun.deploy.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import java.awt.Component;
import java.net.URL;
import javax.swing.JProgressBar;

public class ProgressDialog extends DialogTemplate
{
  public static final int INVISIBLE = 9999;

  protected ProgressDialog(AppInfo paramAppInfo, Component paramComponent, String paramString1, String paramString2, boolean paramBoolean)
  {
    super(paramAppInfo, paramComponent, paramString1, "", true, false);
    setProgressContent(ResourceManager.getMessage("common.ok_btn"), ResourceManager.getMessage("common.cancel_btn"), paramString2, paramBoolean, 9999);
    showOk(paramBoolean);
    stayAlive();
  }

  public void reset(AppInfo paramAppInfo, String paramString, boolean paramBoolean)
  {
    setTitle(paramString);
    showOk(paramBoolean);
    setInfo(paramAppInfo.getDisplayTitle(), paramAppInfo.getDisplayVendor(), paramAppInfo.getFrom());
    setMasthead("", false);
  }

  public void showProgress(int paramInt)
  {
    super.progress(paramInt);
  }

  public void setMasthead(String paramString, boolean paramBoolean)
  {
    super.setMasthead(paramString, paramBoolean);
  }

  public void setApplication(String paramString1, String paramString2, String paramString3)
  {
    URL localURL = null;
    if (paramString3 != null)
      try
      {
        localURL = new URL(paramString3);
      }
      catch (Exception localException)
      {
      }
    setInfo(paramString1, paramString2, localURL);
  }

  public void setUserAnswer(int paramInt)
  {
    super.setUserAnswer(paramInt);
  }

  public void setIndeterminate(boolean paramBoolean)
  {
    if (Config.isJavaVersionAtLeast14())
      this.progressBar.setIndeterminate(paramBoolean);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.ProgressDialog
 * JD-Core Version:    0.6.0
 */