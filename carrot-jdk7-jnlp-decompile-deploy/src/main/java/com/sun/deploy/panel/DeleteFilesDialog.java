package com.sun.deploy.panel;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.security.CredentialManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.DialogTemplate;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public class DeleteFilesDialog extends JDialog
{
  private JCheckBox traceCheckBox;
  private JCheckBox applicationCheckBox;
  private JCheckBox installedAppsCheckBox;

  public DeleteFilesDialog(Dialog paramDialog)
  {
    super(paramDialog, true);
    setTitle(getMessage("delete.files.dlg.title"));
    initComponents();
    setLocationRelativeTo(paramDialog);
    setVisible(true);
  }

  private void initComponents()
  {
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(new FlowLayout());
    localJPanel1.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    JLabel localJLabel = new JLabel(getMessage("delete.files.dlg.temp_files"));
    localJLabel.setIcon(ResourceManager.getIcon("warning32.image"));
    localJLabel.setIconTextGap(12);
    localJPanel1.add(localJLabel);
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setLayout(new BoxLayout(localJPanel2, 1));
    localJPanel2.setBorder(BorderFactory.createEmptyBorder(0, 68, 24, 12));
    this.applicationCheckBox = new JCheckBox(getMessage("delete.files.dlg.applications"));
    this.installedAppsCheckBox = new JCheckBox(getMessage("delete.files.dlg.installedapps"));
    boolean bool = Config.getBooleanProperty("deployment.cache.enabled");
    this.applicationCheckBox.setSelected(bool);
    this.applicationCheckBox.setEnabled(bool);
    this.installedAppsCheckBox.setSelected(false);
    this.installedAppsCheckBox.setEnabled(bool);
    String str = "";
    if (bool)
      str = getMessage("delete.files.dlg.applications.tooltip.enabled");
    else
      str = getMessage("delete.files.dlg.applications.tooltip.disabled");
    this.applicationCheckBox.setToolTipText(str);
    str = "";
    if (bool)
      str = getMessage("delete.files.dlg.installedapps.tooltip.enabled");
    else
      str = getMessage("delete.files.dlg.installedapps.tooltip.disabled");
    this.installedAppsCheckBox.setToolTipText(str);
    this.traceCheckBox = new JCheckBox(getMessage("delete.files.dlg.trace"));
    this.traceCheckBox.setSelected(true);
    this.traceCheckBox.setToolTipText(getMessage("delete.files.dlg.trace.tooltip"));
    localJPanel2.add(this.traceCheckBox);
    localJPanel2.add(this.applicationCheckBox);
    localJPanel2.add(this.installedAppsCheckBox);
    JPanel localJPanel3 = new JPanel();
    localJPanel3.setLayout(new FlowLayout(4));
    localJPanel3.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
    JButton localJButton1 = new JButton(getMessage("common.ok_btn"));
    localJButton1.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        DeleteFilesDialog.this.okBtnActionPerformed(paramActionEvent);
      }
    });
    JButton localJButton2 = new JButton(getMessage("common.cancel_btn"));
    2 local2 = new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        DeleteFilesDialog.this.cancelBtnActionPerformed(paramActionEvent);
      }
    };
    localJButton2.addActionListener(local2);
    localJPanel3.add(localJButton1);
    localJPanel3.add(Box.createHorizontalStrut(6));
    localJPanel3.add(localJButton2);
    localJPanel3.add(Box.createHorizontalStrut(12));
    JButton[] arrayOfJButton = { localJButton1, localJButton2 };
    DialogTemplate.resizeButtons(arrayOfJButton);
    getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
    getRootPane().getActionMap().put("cancel", local2);
    getRootPane().setDefaultButton(localJButton1);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(localJPanel1, "North");
    getContentPane().add(localJPanel2, "Center");
    getContentPane().add(localJPanel3, "South");
    pack();
    setResizable(false);
  }

  private void cancelBtnActionPerformed(ActionEvent paramActionEvent)
  {
    setVisible(false);
  }

  private void okBtnActionPerformed(ActionEvent paramActionEvent)
  {
    ProcessBuilder localProcessBuilder;
    Process localProcess;
    if (this.installedAppsCheckBox.isSelected())
      try
      {
        ArrayList localArrayList1 = new ArrayList();
        localArrayList1.add(Environment.getJavawsCommand());
        localArrayList1.add("-uninstall");
        localProcessBuilder = new ProcessBuilder(localArrayList1);
        localProcess = localProcessBuilder.start();
        Trace.println("launching javaws -uninstall", TraceLevel.BASIC);
        localProcess.waitFor();
      }
      catch (IOException localIOException1)
      {
      }
      catch (InterruptedException localInterruptedException1)
      {
      }
    else if (this.applicationCheckBox.isSelected())
      try
      {
        ArrayList localArrayList2 = new ArrayList();
        localArrayList2.add(Environment.getJavawsCommand());
        localArrayList2.add("-clearcache");
        Trace.println("launching javaws -clearcache", TraceLevel.BASIC);
        localProcessBuilder = new ProcessBuilder(localArrayList2);
        localProcess = localProcessBuilder.start();
        localProcess.waitFor();
      }
      catch (IOException localIOException2)
      {
      }
      catch (InterruptedException localInterruptedException2)
      {
      }
    File localFile;
    if (this.traceCheckBox.isSelected())
    {
      localFile = new File(Config.getLogDirectory());
      deleteFiles(localFile);
    }
    if ((this.applicationCheckBox.isSelected()) || (this.traceCheckBox.isSelected()))
    {
      localFile = new File(Config.getUserExtensionDirectory());
      deleteFiles(localFile);
      localFile = new File(Config.getTempCacheDir());
      deleteFiles(localFile);
      CredentialManager.removePersistantCredentials();
    }
    setVisible(false);
  }

  private void deleteFiles(File paramFile)
  {
    if ((paramFile.exists()) && (paramFile.isDirectory()))
    {
      File[] arrayOfFile = paramFile.listFiles();
      for (int i = 0; i < arrayOfFile.length; i++)
      {
        if (arrayOfFile[i].isDirectory())
          deleteFiles(arrayOfFile[i]);
        arrayOfFile[i].delete();
      }
    }
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.DeleteFilesDialog
 * JD-Core Version:    0.6.0
 */