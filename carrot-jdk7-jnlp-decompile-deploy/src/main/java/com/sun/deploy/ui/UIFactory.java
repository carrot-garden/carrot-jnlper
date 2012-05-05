package com.sun.deploy.ui;

import com.sun.deploy.cache.AssociationDesc;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.security.CredentialInfo;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ui.DialogHook;
import com.sun.deploy.util.DeploySysAction;
import com.sun.deploy.util.DeploySysRun;
import com.sun.deploy.util.DialogListener;
import com.sun.deploy.util.StringUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class UIFactory
{
  public static final int ERROR = -1;
  public static final int OK = 0;
  public static final int CANCEL = 1;
  public static final int ALWAYS = 2;
  public static final int ASK_ME_LATER = 3;
  public static final int OK_OLD_JRE = 0;
  public static final int OK_LATEST_JRE = 2;
  private static long tsLastActive = 0L;
  private static DialogListener dialogListener = null;
  private static DialogHook dialogHook;
  private static int visibleDialogs = 0;

  public static int showSecurityDialog(AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, URL paramURL, boolean paramBoolean1, boolean paramBoolean2, String paramString4, String paramString5, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean3, Certificate[] paramArrayOfCertificate, int paramInt1, int paramInt2, boolean paramBoolean4, boolean paramBoolean5)
  {
    Component localComponent = beforeDialog(null);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(paramArrayOfString2, paramAppInfo, paramString3, paramURL, localComponent, paramString1, paramString2, paramArrayOfString1, paramBoolean2, paramString4, paramString5, paramBoolean3, paramArrayOfCertificate, paramInt1, paramInt2, paramBoolean4, paramBoolean5, paramBoolean1)
      {
        private final String[] val$securityInfo;
        private final AppInfo val$ainfo;
        private final String val$publisher;
        private final URL val$appFrom;
        private final Component val$parent;
        private final String val$title;
        private final String val$topText;
        private final String[] val$securityAlerts;
        private final boolean val$checkAlways;
        private final String val$okBtnStr;
        private final String val$cancelBtnStr;
        private final boolean val$showMoreInfo;
        private final Certificate[] val$certs;
        private final int val$start;
        private final int val$end;
        private final boolean val$majorWarning;
        private final boolean val$httpsDialog;
        private final boolean val$showAlways;

        public Object execute()
          throws Exception
        {
          String[] arrayOfString = new String[0];
          if (this.val$securityInfo != null)
            arrayOfString = this.val$securityInfo;
          int i = arrayOfString.length;
          arrayOfString = UIFactory.access$000(arrayOfString, this.val$ainfo, true, true);
          this.val$ainfo.setVendor(this.val$publisher);
          this.val$ainfo.setFrom(this.val$appFrom);
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$parent, this.val$title, this.val$topText, false);
          if ((this.val$ainfo.getType() == 3) && (this.val$securityAlerts == null))
          {
            localDialogTemplate.setSecurityContent(false, this.val$checkAlways, this.val$okBtnStr, this.val$cancelBtnStr, this.val$securityAlerts, arrayOfString, i, this.val$showMoreInfo, this.val$certs, this.val$start, this.val$end, this.val$majorWarning);
          }
          else if ((this.val$majorWarning) && (!this.val$httpsDialog))
          {
            String str1 = ResourceManager.getString("security.dialog.accept.title");
            String str2 = ResourceManager.getString("security.dialog.accept.text");
            localDialogTemplate.setNewSecurityContent(this.val$showAlways, this.val$checkAlways, this.val$okBtnStr, this.val$cancelBtnStr, this.val$securityAlerts, arrayOfString, i, this.val$showMoreInfo, this.val$certs, this.val$start, this.val$end, this.val$majorWarning, str1, str2);
          }
          else
          {
            localDialogTemplate.setSecurityContent(this.val$showAlways, this.val$checkAlways, this.val$okBtnStr, this.val$cancelBtnStr, this.val$securityAlerts, arrayOfString, i, this.val$showMoreInfo, this.val$certs, this.val$start, this.val$end, this.val$majorWarning);
          }
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int j = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(j);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static int showIntegrationDialog(Component paramComponent, AppInfo paramAppInfo)
  {
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      i = ((Integer)DeploySysRun.execute(new DeploySysAction(paramAppInfo, localComponent)
      {
        private final AppInfo val$ainfo;
        private final Component val$fOwner;

        public Object execute()
          throws Exception
        {
          String str1 = ResourceManager.getString("integration.title");
          int i = (this.val$ainfo.getDesktopHint()) || (this.val$ainfo.getMenuHint()) ? 1 : 0;
          int j = (this.val$ainfo.getAssociations() != null) && (this.val$ainfo.getAssociations().length > 0) ? 1 : 0;
          String str2 = "integration.text.shortcut";
          if (j != 0)
            if (i != 0)
              str2 = "integration.text.both";
            else
              str2 = "integration.text.association";
          String str3 = ResourceManager.getString(str2);
          String[] arrayOfString1 = new String[0];
          String[] arrayOfString2 = new String[0];
          arrayOfString2 = UIFactory.access$000(arrayOfString2, this.val$ainfo, false, true);
          String[] arrayOfString3 = new String[0];
          arrayOfString3 = UIFactory.access$000(arrayOfString3, this.val$ainfo, true, false);
          boolean bool = arrayOfString2.length + arrayOfString3.length > 1;
          String str4 = ResourceManager.getString("common.ok_btn");
          String str5 = ResourceManager.getString("integration.skip.button");
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$fOwner, str1, str3, false);
          localDialogTemplate.setSecurityContent(false, false, str4, str5, arrayOfString2, arrayOfString3, 0, bool, null, 0, 0, false);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int k = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(k);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static int showErrorDialog(Component paramComponent, AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, Throwable paramThrowable, JPanel paramJPanel, Certificate[] paramArrayOfCertificate)
  {
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(paramAppInfo, localComponent, paramString1, paramString2, paramString3, paramString4, paramString5, paramThrowable, paramJPanel, paramArrayOfCertificate)
      {
        private final AppInfo val$ainfo;
        private final Component val$fOwner;
        private final String val$title;
        private final String val$masthead;
        private final String val$message;
        private final String val$okBtnStr;
        private final String val$detailBtnStr;
        private final Throwable val$throwable;
        private final JPanel val$detailPanel;
        private final Certificate[] val$certs;

        public Object execute()
          throws Exception
        {
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$fOwner, this.val$title, this.val$masthead, false);
          localDialogTemplate.setErrorContent(this.val$message, this.val$okBtnStr, this.val$detailBtnStr, this.val$throwable, this.val$detailPanel, this.val$certs, false);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static int showErrorDialog(Component paramComponent, AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6)
  {
    String str = paramString1 == null ? ResourceManager.getString("error.default.title") : paramString1;
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(paramAppInfo, localComponent, paramString2, paramString3, paramString4, paramString5, paramString6)
      {
        private final AppInfo val$ainfo;
        private final Component val$fOwner;
        private final String val$masthead;
        private final String val$message;
        private final String val$btnOneLabel;
        private final String val$btnTwoLabel;
        private final String val$btnThreeLabel;

        public Object execute()
          throws Exception
        {
          String str = ResourceManager.getString("error.default.title");
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$fOwner, str, this.val$masthead, false);
          localDialogTemplate.setMultiButtonErrorContent(this.val$message, this.val$btnOneLabel, this.val$btnTwoLabel, this.val$btnThreeLabel);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static CredentialInfo showPasswordDialog(Component paramComponent, String paramString1, String paramString2, boolean paramBoolean1, boolean paramBoolean2, CredentialInfo paramCredentialInfo, boolean paramBoolean3, String paramString3)
  {
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      localCredentialInfo = (CredentialInfo)DeploySysRun.executePrivileged(new DeploySysAction(paramCredentialInfo, localComponent, paramString1, paramString2, paramBoolean1, paramBoolean2, paramBoolean3, paramString3)
      {
        private final CredentialInfo val$info;
        private final Component val$fParent;
        private final String val$title;
        private final String val$notes;
        private final boolean val$showUsername;
        private final boolean val$showDomain;
        private final boolean val$saveEnabled;
        private final String val$scheme;

        public Object execute()
        {
          CredentialInfo localCredentialInfo1 = null;
          CredentialInfo localCredentialInfo2 = this.val$info;
          DialogTemplate localDialogTemplate = new DialogTemplate(new AppInfo(), this.val$fParent, this.val$title, "", false);
          if (localCredentialInfo2 == null)
            localCredentialInfo2 = new CredentialInfo();
          localDialogTemplate.setPasswordContent(this.val$notes, this.val$showUsername, this.val$showDomain, localCredentialInfo2.getUserName(), localCredentialInfo2.getDomain(), this.val$saveEnabled, localCredentialInfo2.getPassword(), this.val$scheme);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          if ((i == 0) || (i == 2))
          {
            localCredentialInfo1 = new CredentialInfo();
            localCredentialInfo1.setUserName(localDialogTemplate.getUserName());
            localCredentialInfo1.setDomain(localDialogTemplate.getDomain());
            localCredentialInfo1.setPassword(localDialogTemplate.getPassword());
            localCredentialInfo1.setPasswordSaveApproval(localDialogTemplate.isPasswordSaved());
          }
          localDialogTemplate.disposeDialog();
          return localCredentialInfo1;
        }
      }
      , null);
    }
    finally
    {
      CredentialInfo localCredentialInfo;
      afterDialog();
    }
  }

  public static void showExceptionOCSPDialog(Component paramComponent, Throwable paramThrowable, String paramString1, String paramString2, String paramString3)
  {
    String str1 = ResourceManager.getString("common.close_btn");
    String str2 = ResourceManager.getString("common.detail.button");
    if (paramString2 == null)
      paramString2 = paramThrowable.toString();
    showErrorDialog(paramComponent, new AppInfo(), paramString3, paramString1, paramString2, str1, str2, paramThrowable, null, null);
  }

  public static void showExceptionDialog(Component paramComponent, Throwable paramThrowable, String paramString1, String paramString2, String paramString3)
  {
    String str1 = ResourceManager.getString("common.ok_btn");
    String str2 = ResourceManager.getString("common.detail.button");
    if (paramString2 == null)
      paramString2 = paramThrowable.getClass().getSimpleName() + ": " + paramThrowable.getLocalizedMessage();
    if (paramString3 == null)
      paramString3 = ResourceManager.getString("error.default.title");
    showErrorDialog(paramComponent, new AppInfo(), paramString3, paramString1, paramString2, str1, str2, paramThrowable, null, null);
  }

  public static void showCertificateExceptionDialog(Component paramComponent, AppInfo paramAppInfo, Throwable paramThrowable, String paramString1, String paramString2, Certificate[] paramArrayOfCertificate)
  {
    String str1 = ResourceManager.getString("common.ok_btn");
    String str2 = ResourceManager.getString("common.detail.button");
    if (paramString1 == null)
      paramString1 = paramThrowable.toString();
    if (paramString2 == null)
      paramString2 = ResourceManager.getString("error.default.title");
    showErrorDialog(paramComponent, paramAppInfo, paramString2, paramString1, null, str1, str2, paramThrowable, null, paramArrayOfCertificate);
  }

  public static int showContentDialog(Component paramComponent, AppInfo paramAppInfo, String paramString1, String paramString2, boolean paramBoolean, String paramString3, String paramString4)
  {
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(paramAppInfo, localComponent, paramString1, paramString2, paramBoolean, paramString3, paramString4)
      {
        private final AppInfo val$ainfo;
        private final Component val$fOwner;
        private final String val$title;
        private final String val$content;
        private final boolean val$scroll;
        private final String val$okBtnStr;
        private final String val$cancelBtnStr;

        public Object execute()
          throws Exception
        {
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$fOwner, this.val$title, null, false);
          localDialogTemplate.setSimpleContent(this.val$content, this.val$scroll, null, this.val$okBtnStr, this.val$cancelBtnStr, false, false);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static int showConfirmDialog(Component paramComponent, AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, boolean paramBoolean)
  {
    AppInfo localAppInfo = paramAppInfo == null ? new AppInfo() : paramAppInfo;
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(localAppInfo, localComponent, paramString1, paramString2, paramString3, paramString4, paramString5, paramBoolean)
      {
        private final AppInfo val$ainfo;
        private final Component val$fOwner;
        private final String val$title;
        private final String val$message;
        private final String val$info;
        private final String val$okBtnStr;
        private final String val$cancelBtnStr;
        private final boolean val$useWarning;

        public Object execute()
          throws Exception
        {
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$fOwner, this.val$title, this.val$message, false);
          localDialogTemplate.setSimpleContent(null, false, this.val$info, this.val$okBtnStr, this.val$cancelBtnStr, true, this.val$useWarning);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static int showMixedCodeDialog(Component paramComponent, AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, boolean paramBoolean)
  {
    AppInfo localAppInfo = paramAppInfo == null ? new AppInfo() : paramAppInfo;
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(localAppInfo, localComponent, paramString1, paramString2, paramString3, paramString4, paramString5, paramString6, paramBoolean)
      {
        private final AppInfo val$ainfo;
        private final Component val$fOwner;
        private final String val$title;
        private final String val$masthead;
        private final String val$message;
        private final String val$info;
        private final String val$okBtnStr;
        private final String val$cancelBtnStr;
        private final boolean val$useWarning;

        public Object execute()
          throws Exception
        {
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$fOwner, this.val$title, this.val$masthead, false);
          localDialogTemplate.setMixedCodeContent(null, false, this.val$message, this.val$info, this.val$okBtnStr, this.val$cancelBtnStr, true, this.val$useWarning);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static void showInformationDialog(Component paramComponent, String paramString1, String paramString2, String paramString3)
  {
    String str = ResourceManager.getString("common.ok_btn");
    AppInfo localAppInfo = new AppInfo();
    try
    {
      DeploySysRun.executePrivileged(new DeploySysAction(localAppInfo, paramComponent, paramString3, paramString1, paramString2, str)
      {
        private final AppInfo val$ainfo;
        private final Component val$parent;
        private final String val$title;
        private final String val$masthead;
        private final String val$text;
        private final String val$okBtnStr;

        public Object execute()
        {
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$parent, this.val$title, this.val$masthead, false);
          localDialogTemplate.setInfoContent(this.val$text, this.val$okBtnStr);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          return null;
        }
      }
      , null);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  public static int showApiDialog(Component paramComponent, AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, boolean paramBoolean)
  {
    String str1 = ResourceManager.getString("common.ok_btn");
    String str2 = ResourceManager.getString("common.cancel_btn");
    AppInfo localAppInfo = paramAppInfo == null ? new AppInfo() : paramAppInfo;
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(localAppInfo, localComponent, paramString1, paramString2, paramString4, paramString3, paramString5, paramBoolean, str1, str2)
      {
        private final AppInfo val$ainfo;
        private final Component val$fOwner;
        private final String val$title;
        private final String val$message;
        private final String val$files;
        private final String val$label;
        private final String val$always;
        private final boolean val$checked;
        private final String val$okBtnStr;
        private final String val$cancelBtnStr;

        public Object execute()
          throws Exception
        {
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$fOwner, this.val$title, this.val$message, false);
          localDialogTemplate.setApiContent(this.val$files, this.val$label, this.val$always, this.val$checked, this.val$okBtnStr, this.val$cancelBtnStr);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static ProgressDialog createProgressDialog(AppInfo paramAppInfo, Component paramComponent, String paramString1, String paramString2, boolean paramBoolean)
  {
    String str = System.getProperty("test.progressdialog");
    if (str == null)
      try
      {
        return (ProgressDialog)DeploySysRun.execute(new DeploySysAction(paramAppInfo, paramComponent, paramString1, paramString2, paramBoolean)
        {
          private final AppInfo val$ainfo;
          private final Component val$owner;
          private final String val$title;
          private final String val$contentStr;
          private final boolean val$okBtn;

          public Object execute()
            throws Exception
          {
            return new ProgressDialog(this.val$ainfo, this.val$owner, this.val$title, this.val$contentStr, this.val$okBtn);
          }
        });
      }
      catch (Throwable localThrowable1)
      {
        Trace.ignored(localThrowable1);
        return null;
      }
    try
    {
      return (ProgressDialog)DeploySysRun.execute(new DeploySysAction(str)
      {
        private final String val$dialogClassName;

        public Object execute()
          throws Exception
        {
          Class localClass = Class.forName(this.val$dialogClassName, false, null);
          Class[] arrayOfClass = { AppInfo.class, Component.class, String.class, String.class, Boolean.TYPE, Integer.TYPE };
          Object[] arrayOfObject = { new AppInfo(), null, "dialog title", "string", Boolean.valueOf(false), new Integer(0) };
          return localClass.getConstructor(arrayOfClass).newInstance(arrayOfObject);
        }
      });
    }
    catch (Throwable localThrowable2)
    {
      new RuntimeException("can't construct testProgressDialog for testing!!");
      localThrowable2.printStackTrace();
    }
    return null;
  }

  public static int showProgressDialog(ProgressDialog paramProgressDialog)
  {
    beforeDialog(null);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(paramProgressDialog)
      {
        private final ProgressDialog val$progressDialog;

        public Object execute()
          throws Exception
        {
          UIFactory.placeWindow(this.val$progressDialog.getDialog());
          this.val$progressDialog.setVisible(true);
          return new Integer(this.val$progressDialog.getUserAnswer());
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static int hideProgressDialog(ProgressDialog paramProgressDialog)
  {
    try
    {
      return ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(paramProgressDialog)
      {
        private final ProgressDialog val$progressDialog;

        public Object execute()
          throws Exception
        {
          this.val$progressDialog.setVisible(false);
          return new Integer(0);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
    return -1;
  }

  public static void showAboutJavaDialog()
  {
    if (SwingUtilities.isEventDispatchThread())
      internalShowAboutJavaDialog();
    else
      try
      {
        SwingUtilities.invokeAndWait(new Runnable()
        {
          public void run()
          {
            UIFactory.access$100();
          }
        });
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
  }

  private static synchronized void internalShowAboutJavaDialog()
  {
    if ((System.currentTimeMillis() - tsLastActive > 500L) && (AboutDialog.shouldStartNewInstance()))
    {
      AboutDialog localAboutDialog = new AboutDialog((JFrame)null, true, true);
      localAboutDialog.pack();
      Dimension localDimension1 = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension localDimension2 = localAboutDialog.getSize();
      localAboutDialog.setLocation((localDimension1.width - localDimension2.width) / 2, (localDimension1.height - localDimension2.height) / 2);
      if (Config.isJavaVersionAtLeast15())
        localAboutDialog.setAlwaysOnTop(true);
      localAboutDialog.setVisible(true);
      tsLastActive = System.currentTimeMillis();
    }
  }

  public static int showWarningDialog(Component paramComponent, AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, URL paramURL, String paramString6)
  {
    AppInfo localAppInfo = paramAppInfo == null ? new AppInfo() : paramAppInfo;
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(paramURL, paramString6, localAppInfo, localComponent, paramString1, paramString2, paramString4, paramString5, paramString3)
      {
        private final URL val$moreURL;
        private final String val$moreText;
        private final AppInfo val$ainfo;
        private final Component val$fOwner;
        private final String val$title;
        private final String val$masthead;
        private final String val$okBtnStr;
        private final String val$cancelBtnStr;
        private final String val$message;

        public Object execute()
          throws Exception
        {
          boolean bool = (this.val$moreURL != null) && (this.val$moreText != null);
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$fOwner, this.val$title, this.val$masthead, bool);
          if (bool)
            localDialogTemplate.setCannotDownloadContent(this.val$okBtnStr, this.val$cancelBtnStr, this.val$moreURL, this.val$moreText);
          else
            localDialogTemplate.setSimpleContent(this.val$message, false, null, this.val$okBtnStr, this.val$cancelBtnStr, true, true);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static int showUpdateCheckDialog()
  {
    String str1 = ResourceManager.getMessage("autoupdatecheck.caption");
    String str2 = ResourceManager.getMessage("autoupdatecheck.message");
    String str3 = ResourceManager.getMessage("autoupdatecheck.masthead");
    AppInfo localAppInfo = new AppInfo();
    Component localComponent = beforeDialog(null);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(localAppInfo, localComponent, str1, str3, str2)
      {
        private final AppInfo val$ainfo;
        private final Component val$owner;
        private final String val$title;
        private final String val$masthead;
        private final String val$infoStr;

        public Object execute()
          throws Exception
        {
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$owner, this.val$title, this.val$masthead, false);
          localDialogTemplate.setUpdateCheckContent(this.val$infoStr, "autoupdatecheck.buttonYes", "autoupdatecheck.buttonNo", "autoupdatecheck.buttonAskLater");
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(3))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = 3;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static int showListDialog(Component paramComponent, String paramString1, String paramString2, String paramString3, boolean paramBoolean, JList paramJList, TreeMap paramTreeMap)
  {
    String str1 = ResourceManager.getString("common.ok_btn");
    String str2 = ResourceManager.getString("common.cancel_btn");
    Component localComponent = beforeDialog(paramComponent);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(localComponent, paramString1, paramString2, paramString3, paramJList, paramBoolean, str1, str2, paramTreeMap)
      {
        private final Component val$fOwner;
        private final String val$title;
        private final String val$message;
        private final String val$label;
        private final JList val$scrollList;
        private final boolean val$details;
        private final String val$okBtnStr;
        private final String val$cancelBtnStr;
        private final TreeMap val$clientAuthCertsMap;

        public Object execute()
          throws Exception
        {
          DialogTemplate localDialogTemplate = new DialogTemplate(new AppInfo(), this.val$fOwner, this.val$title, this.val$message, false);
          localDialogTemplate.setListContent(this.val$label, this.val$scrollList, this.val$details, this.val$okBtnStr, this.val$cancelBtnStr, this.val$clientAuthCertsMap);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  private static String[] addDetail(String[] paramArrayOfString, AppInfo paramAppInfo, boolean paramBoolean1, boolean paramBoolean2)
  {
    String str1 = paramAppInfo.getDisplayTitle();
    if (str1 == null)
      str1 = "";
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < paramArrayOfString.length; i++)
      localArrayList.add(paramArrayOfString[i]);
    Object localObject;
    if (paramBoolean2)
    {
      localObject = paramAppInfo.getAssociations();
      if (localObject != null)
        for (int j = 0; j < localObject.length; j++)
        {
          String str2 = localObject[j].getExtensions();
          String str3 = localObject[j].getMimeType();
          String str4 = localObject[j].getMimeDescription();
          String str5 = ResourceManager.getString("association.dialog.ask", str3, str2);
          localArrayList.add(str5);
        }
    }
    if (paramBoolean1)
    {
      localObject = null;
      if ((paramAppInfo.getDesktopHint()) && (paramAppInfo.getMenuHint()))
      {
        if (Config.getOSName().equalsIgnoreCase("Windows"))
          localObject = ResourceManager.getString("install.windows.both.message");
        else
          localObject = ResourceManager.getString("install.gnome.both.message");
      }
      else if (paramAppInfo.getDesktopHint())
        localObject = ResourceManager.getString("install.desktop.message");
      else if (paramAppInfo.getMenuHint())
        if (Config.getOSName().equalsIgnoreCase("Windows"))
          localObject = ResourceManager.getString("install.windows.menu.message");
        else
          localObject = ResourceManager.getString("install.gnome.menu.message");
      if (localObject != null)
        localArrayList.add(localObject);
    }
    return (String)(String[])(String[])localArrayList.toArray(paramArrayOfString);
  }

  public static int showSSVDialog(Object paramObject, AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, String paramString4, URL paramURL, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9)
  {
    Component localComponent = beforeDialog(null);
    try
    {
      i = ((Integer)DeploySysRun.executePrivileged(new DeploySysAction(paramAppInfo, localComponent, paramString1, paramString2, paramString3, paramString4, paramURL, paramString5, paramString6, paramString7, paramString8, paramString9)
      {
        private final AppInfo val$ainfo;
        private final Component val$parent;
        private final String val$title;
        private final String val$masthead;
        private final String val$message;
        private final String val$moreInfoText;
        private final URL val$moreInfoURL;
        private final String val$choiceText;
        private final String val$choice1Label;
        private final String val$choice2Label;
        private final String val$btnOneLabel;
        private final String val$btnTwoLabel;

        public Object execute()
          throws Exception
        {
          DialogTemplate localDialogTemplate = new DialogTemplate(this.val$ainfo, this.val$parent, this.val$title, this.val$masthead, true);
          localDialogTemplate.setSSVContent(this.val$message, this.val$moreInfoText, this.val$moreInfoURL, this.val$choiceText, this.val$choice1Label, this.val$choice2Label, this.val$btnOneLabel, this.val$btnTwoLabel);
          UIFactory.placeWindow(localDialogTemplate.getDialog());
          localDialogTemplate.setVisible(true);
          int i = localDialogTemplate.getUserAnswer();
          localDialogTemplate.disposeDialog();
          return new Integer(i);
        }
      }
      , new Integer(-1))).intValue();
    }
    catch (Throwable localThrowable)
    {
      int i;
      Trace.ignored(localThrowable);
      int j = -1;
      return j;
    }
    finally
    {
      afterDialog();
    }
  }

  public static void placeWindow(Window paramWindow)
  {
    if (paramWindow == null)
      return;
    Window localWindow = paramWindow.getOwner();
    if (ignoreOwnerVisibility())
      localWindow = null;
    int i = (localWindow != null) && (localWindow.isVisible()) ? 1 : 0;
    Rectangle localRectangle1 = getMouseScreenBounds();
    Rectangle localRectangle2 = paramWindow.getBounds();
    Rectangle localRectangle3 = (localWindow == null) || (!localWindow.isVisible()) ? localRectangle1 : localWindow.getBounds();
    if (localRectangle3.x + localRectangle3.width < 0)
    {
      i = 0;
      localRectangle3 = localRectangle1;
    }
    double d = localRectangle3.height - localRectangle3.height / 1.618D;
    localRectangle3.x += (localRectangle3.width - localRectangle2.width) / 2;
    int j = (int)(d - localRectangle2.height / 2);
    int k = i != 0 ? localWindow.getInsets().top : localRectangle1.height / 2 - 2 * localRectangle2.height / 3;
    localRectangle3.y += Math.max(j, k);
    if (localRectangle2.x + localRectangle2.width > localRectangle1.x + localRectangle1.width)
      localRectangle1.x += Math.max(localRectangle1.width - localRectangle2.width, 0);
    if (localRectangle2.y + localRectangle2.height > localRectangle1.y + localRectangle1.height)
      localRectangle1.y += Math.max(localRectangle1.height - localRectangle2.height, 0);
    if ((localRectangle2.y < 0) || (localRectangle2.x < 0))
    {
      Dimension localDimension1 = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension localDimension2 = paramWindow.getSize();
      paramWindow.setLocation(Math.abs(localDimension1.width - localDimension2.width) / 2, Math.abs(localDimension1.height - localDimension2.height) / 2);
    }
    else
    {
      paramWindow.setLocation(localRectangle2.x, localRectangle2.y);
    }
  }

  public static Rectangle getMouseScreenBounds()
  {
    Point localPoint = new Point(1, 1);
    if (Config.isJavaVersionAtLeast15())
      localPoint = MouseInfo.getPointerInfo().getLocation();
    GraphicsDevice[] arrayOfGraphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    for (int i = 0; i < arrayOfGraphicsDevice.length; i++)
    {
      Rectangle localRectangle = arrayOfGraphicsDevice[i].getDefaultConfiguration().getBounds();
      if ((localPoint.x >= localRectangle.x) && (localPoint.y >= localRectangle.y) && (localPoint.x <= localRectangle.x + localRectangle.width) && (localPoint.y <= localRectangle.y + localRectangle.height))
        return localRectangle;
    }
    return new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());
  }

  public static JPanel getDetailPanel(String paramString)
  {
    20 local20 = new JPanel()
    {
      public Dimension getPreferredSize()
      {
        return new Dimension(480, 300);
      }
    };
    local20.setLayout(new BorderLayout());
    JTabbedPane localJTabbedPane = new JTabbedPane();
    local20.add(localJTabbedPane, "Center");
    localJTabbedPane.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    JPanel localJPanel1 = new JPanel(new BorderLayout());
    JLabel localJLabel = new JLabel(ResourceManager.getString("launcherrordialog.error.label"));
    Font localFont = localJLabel.getFont().deriveFont(1);
    localJLabel.setFont(localFont);
    JPanel localJPanel2 = new JPanel(new BorderLayout());
    localJPanel2.add(localJLabel, "North");
    localJPanel2.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
    localJPanel1.add(localJPanel2, "West");
    String[] arrayOfString;
    try
    {
      arrayOfString = paramString.split("<split>");
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
      arrayOfString = StringUtils.splitString(paramString, "<split>");
    }
    UITextArea localUITextArea = new UITextArea(arrayOfString[0]);
    localJPanel1.add(localUITextArea, "Center");
    local20.add(localJPanel1, "North");
    int i = 1;
    while (i + 1 < arrayOfString.length)
    {
      JTextArea localJTextArea = new JTextArea();
      localJTextArea.setFont(ResourceManager.getUIFont());
      localJTextArea.setEditable(false);
      localJTextArea.setLineWrap(true);
      localJTextArea.setText(arrayOfString[(i + 1)]);
      localJTabbedPane.add(arrayOfString[i], new JScrollPane(localJTextArea));
      i += 2;
    }
    return local20;
  }

  public static void setDialogListener(DialogListener paramDialogListener)
  {
    dialogListener = paramDialogListener;
  }

  public static DialogListener getDialogListener()
  {
    return dialogListener;
  }

  public static void setDialogHook(DialogHook paramDialogHook)
  {
    dialogHook = paramDialogHook;
  }

  public static DialogHook getDialogHook()
  {
    return dialogHook;
  }

  public static boolean hasVisibleDialogs()
  {
    return visibleDialogs > 0;
  }

  static Component beforeDialog(Component paramComponent)
  {
    visibleDialogs += 1;
    if (dialogHook != null)
      return (Component)dialogHook.beforeDialog(paramComponent);
    return paramComponent;
  }

  private static boolean ignoreOwnerVisibility()
  {
    if (dialogHook != null)
      return dialogHook.ignoreOwnerVisibility();
    return false;
  }

  static void afterDialog()
  {
    visibleDialogs -= 1;
    if (dialogHook != null)
      dialogHook.afterDialog();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.UIFactory
 * JD-Core Version:    0.6.0
 */