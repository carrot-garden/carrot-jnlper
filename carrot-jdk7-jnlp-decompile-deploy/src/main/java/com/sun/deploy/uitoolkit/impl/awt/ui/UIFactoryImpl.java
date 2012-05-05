package com.sun.deploy.uitoolkit.impl.awt.ui;

import com.sun.deploy.security.CredentialInfo;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ui.ConsoleController;
import com.sun.deploy.uitoolkit.ui.ConsoleWindow;
import com.sun.deploy.uitoolkit.ui.DialogHook;
import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.filechooser.FileSystemView;

public class UIFactoryImpl extends com.sun.deploy.uitoolkit.ui.UIFactory
{
  private SwingConsoleWindow console;

  public int showMessageDialog(Object paramObject, AppInfo paramAppInfo, int paramInt, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7)
  {
    return showMessageDialog(paramObject, paramAppInfo, paramInt, paramString1, paramString2, paramString3, paramString4, paramString5, paramString6, paramString7, null, null);
  }

  public int showMessageDialog(Object paramObject, AppInfo paramAppInfo, int paramInt, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, URL paramURL, String paramString8)
  {
    switch (paramInt)
    {
    case -1:
    case 6:
    default:
      return com.sun.deploy.ui.UIFactory.showContentDialog((Component)paramObject, paramAppInfo, paramString1, paramString3, true, paramString5, paramString6);
    case 0:
      if (paramString4 != null)
        return com.sun.deploy.ui.UIFactory.showErrorDialog((Component)paramObject, paramAppInfo, paramString1, paramString3, null, paramString5, paramString6, null, com.sun.deploy.ui.UIFactory.getDetailPanel(paramString4), null);
      return com.sun.deploy.ui.UIFactory.showErrorDialog((Component)paramObject, paramAppInfo, paramString1, paramString2, paramString3, paramString5, paramString6, paramString7);
    case 1:
      com.sun.deploy.ui.UIFactory.showInformationDialog((Component)paramObject, paramString2, paramString3, paramString1);
      return -1;
    case 2:
      return com.sun.deploy.ui.UIFactory.showWarningDialog((Component)paramObject, paramAppInfo, paramString1, paramString2, paramString3, paramString5, paramString6, paramURL, paramString8);
    case 3:
      return com.sun.deploy.ui.UIFactory.showConfirmDialog((Component)paramObject, paramAppInfo, paramString1, paramString3, paramString4, paramString5, paramString6, true);
    case 5:
      return com.sun.deploy.ui.UIFactory.showIntegrationDialog((Component)paramObject, paramAppInfo);
    case 7:
      return com.sun.deploy.ui.UIFactory.showApiDialog(null, paramAppInfo, paramString1, paramString3, paramString4, paramString5, paramString6, false);
    case 4:
    }
    return com.sun.deploy.ui.UIFactory.showMixedCodeDialog((Component)paramObject, paramAppInfo, paramString1, paramString2, paramString3, paramString4, paramString5, paramString6, true);
  }

  public void showExceptionDialog(Object paramObject, AppInfo paramAppInfo, Throwable paramThrowable, String paramString1, String paramString2, String paramString3, Certificate[] paramArrayOfCertificate)
  {
    if (paramArrayOfCertificate == null)
      com.sun.deploy.ui.UIFactory.showExceptionDialog((Component)paramObject, paramThrowable, paramString2, paramString3, paramString1);
    else
      com.sun.deploy.ui.UIFactory.showCertificateExceptionDialog((Component)paramObject, paramAppInfo, paramThrowable, paramString2, paramString1, paramArrayOfCertificate);
  }

  public int showSecurityDialog(AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, URL paramURL, boolean paramBoolean1, boolean paramBoolean2, String paramString4, String paramString5, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean3, Certificate[] paramArrayOfCertificate, int paramInt1, int paramInt2, boolean paramBoolean4)
  {
    return com.sun.deploy.ui.UIFactory.showSecurityDialog(paramAppInfo, paramString1, paramString2, paramString3, paramURL, paramBoolean1, paramBoolean2, paramString4, paramString5, paramArrayOfString1, paramArrayOfString2, paramBoolean3, paramArrayOfCertificate, paramInt1, paramInt2, paramBoolean4, false);
  }

  public int showSecurityDialog(AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, URL paramURL, boolean paramBoolean1, boolean paramBoolean2, String paramString4, String paramString5, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean3, Certificate[] paramArrayOfCertificate, int paramInt1, int paramInt2, boolean paramBoolean4, boolean paramBoolean5)
  {
    return com.sun.deploy.ui.UIFactory.showSecurityDialog(paramAppInfo, paramString1, paramString2, paramString3, paramURL, paramBoolean1, paramBoolean2, paramString4, paramString5, paramArrayOfString1, paramArrayOfString2, paramBoolean3, paramArrayOfCertificate, paramInt1, paramInt2, paramBoolean4, paramBoolean5);
  }

  public void showAboutJavaDialog()
  {
    com.sun.deploy.ui.UIFactory.showAboutJavaDialog();
  }

  public CredentialInfo showPasswordDialog(Object paramObject, String paramString1, String paramString2, boolean paramBoolean1, boolean paramBoolean2, CredentialInfo paramCredentialInfo, boolean paramBoolean3, String paramString3)
  {
    return com.sun.deploy.ui.UIFactory.showPasswordDialog((Component)paramObject, paramString1, paramString2, paramBoolean1, paramBoolean2, paramCredentialInfo, paramBoolean3, paramString3);
  }

  public int showListDialog(Object paramObject, String paramString1, String paramString2, String paramString3, boolean paramBoolean, Vector paramVector, TreeMap paramTreeMap)
  {
    JList localJList = new JList();
    localJList.setSelectionMode(0);
    localJList.setListData(paramVector);
    if (paramVector.size() > 0)
      localJList.setSelectedIndex(0);
    return com.sun.deploy.ui.UIFactory.showListDialog((Component)paramObject, paramString1, paramString2, paramString3, paramBoolean, localJList, paramTreeMap);
  }

  public int showUpdateCheckDialog()
  {
    return com.sun.deploy.ui.UIFactory.showUpdateCheckDialog();
  }

  public void setDialogHook(DialogHook paramDialogHook)
  {
    com.sun.deploy.ui.UIFactory.setDialogHook(paramDialogHook);
  }

  public synchronized ConsoleWindow getConsole(ConsoleController paramConsoleController)
  {
    if (null == this.console)
      try
      {
        this.console = SwingConsoleWindow.create(paramConsoleController);
      }
      catch (Exception localException)
      {
        this.console = null;
        throw new RuntimeException("Could not create Swing Console due to exception", localException);
      }
    return this.console;
  }

  public int showSSVDialog(Object paramObject, AppInfo paramAppInfo, String paramString1, String paramString2, String paramString3, String paramString4, URL paramURL, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9)
  {
    return com.sun.deploy.ui.UIFactory.showSSVDialog(paramObject, paramAppInfo, paramString1, paramString2, paramString3, paramString4, paramURL, paramString5, paramString6, paramString7, paramString8, paramString9);
  }

  public File[] showFileChooser(String paramString, String[] paramArrayOfString, int paramInt, boolean paramBoolean)
  {
    FileSystemView localFileSystemView = FileSystemView.getFileSystemView();
    JFileChooser localJFileChooser = new JFileChooser(paramString, localFileSystemView);
    if (paramArrayOfString != null)
      localJFileChooser.addChoosableFileFilter(new FileFilterImpl(paramArrayOfString));
    localJFileChooser.setFileSelectionMode(0);
    if (paramInt == 8)
      localJFileChooser.setDialogType(0);
    else
      localJFileChooser.setDialogType(1);
    localJFileChooser.setMultiSelectionEnabled(paramBoolean);
    int i = localJFileChooser.showOpenDialog(null);
    if (i == 1)
      return null;
    if (paramBoolean)
      return localJFileChooser.getSelectedFiles();
    return new File[] { localJFileChooser.getSelectedFile() };
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.awt.ui.UIFactoryImpl
 * JD-Core Version:    0.6.0
 */