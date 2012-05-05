package com.sun.deploy.uitoolkit.ui;

import com.sun.deploy.security.CredentialInfo;
import com.sun.deploy.ui.AppInfo;
import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.TreeMap;
import java.util.Vector;

public abstract class UIFactory
{
  public static final int ERROR = -1;
  public static final int OK = 0;
  public static final int OK_OLD_JRE = 0;
  public static final int CANCEL = 1;
  public static final int ALWAYS = 2;
  public static final int OK_LATEST_JRE = 2;
  public static final int ASK_ME_LATER = 3;
  public static final int PLAIN_MESSAGE = -1;
  public static final int ERROR_MESSAGE = 0;
  public static final int INFORMATION_MESSAGE = 1;
  public static final int WARNING_MESSAGE = 2;
  public static final int QUESTION_MESSAGE = 3;
  public static final int MIXCODE_MESSAGE = 4;
  public static final int INTEGRATION_MESSAGE = 5;
  public static final int UPDATE_MESSAGE = 6;
  public static final int API_MESSAGE = 7;
  public static final int OPEN_DIALOG = 8;
  public static final int SAVE_DIALOG = 9;

  public abstract int showMessageDialog(Object paramObject, AppInfo paramAppInfo, int paramInt, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7);

  public abstract void showExceptionDialog(Object paramObject, AppInfo paramAppInfo, Throwable paramThrowable, String paramString1, String paramString2, String paramString3, Certificate[] paramArrayOfCertificate);

  public abstract CredentialInfo showPasswordDialog(Object paramObject, String paramString1, String paramString2, boolean paramBoolean1, boolean paramBoolean2, CredentialInfo paramCredentialInfo, boolean paramBoolean3, String paramString3);

  public abstract int showSecurityDialog(AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, URL paramURL, boolean paramBoolean1, boolean paramBoolean2, String paramString4, String paramString5, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean3, Certificate[] paramArrayOfCertificate, int paramInt1, int paramInt2, boolean paramBoolean4);

  public int showSecurityDialog(AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, URL paramURL, boolean paramBoolean1, boolean paramBoolean2, String paramString4, String paramString5, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean3, Certificate[] paramArrayOfCertificate, int paramInt1, int paramInt2, boolean paramBoolean4, boolean paramBoolean5)
  {
    return showSecurityDialog(paramAppInfo, paramString1, paramString2, paramString3, paramURL, paramBoolean1, paramBoolean2, paramString4, paramString5, paramArrayOfString1, paramArrayOfString2, paramBoolean3, paramArrayOfCertificate, paramInt1, paramInt2, paramBoolean4);
  }

  public abstract void showAboutJavaDialog();

  public abstract int showListDialog(Object paramObject, String paramString1, String paramString2, String paramString3, boolean paramBoolean, Vector paramVector, TreeMap paramTreeMap);

  public abstract int showUpdateCheckDialog();

  public abstract ConsoleWindow getConsole(ConsoleController paramConsoleController);

  public abstract void setDialogHook(DialogHook paramDialogHook);

  public int showSSVDialog(Object paramObject, AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, String paramString4, URL paramURL, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9)
  {
    return 2;
  }

  public int showMessageDialog(Object paramObject, AppInfo paramAppInfo, int paramInt, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, URL paramURL, String paramString8)
  {
    return showMessageDialog(paramObject, paramAppInfo, paramInt, paramString1, paramString2, paramString3, paramString4, paramString5, paramString6, paramString7);
  }

  public abstract File[] showFileChooser(String paramString, String[] paramArrayOfString, int paramInt, boolean paramBoolean);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.ui.UIFactory
 * JD-Core Version:    0.6.0
 */