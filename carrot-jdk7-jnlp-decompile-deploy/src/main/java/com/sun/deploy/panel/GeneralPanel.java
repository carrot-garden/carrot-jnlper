package com.sun.deploy.panel;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.AboutDialog;
import com.sun.deploy.ui.DialogTemplate;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class GeneralPanel extends JPanel
{
  JButton cacheViewBtn;

  public GeneralPanel()
  {
    initComponents();
  }

  public void initComponents()
  {
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(new BoxLayout(localJPanel1, 1));
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setBorder(new TitledBorder(new TitledBorder(new EtchedBorder()), getMessage("general.about.border"), 0, 0));
    localJPanel2.setLayout(new BorderLayout());
    JSmartTextArea localJSmartTextArea1 = new JSmartTextArea(getMessage("general.about.text"));
    JPanel localJPanel3 = new JPanel();
    localJPanel3.setLayout(new FlowLayout(2));
    JButton localJButton1 = new JButton(getMessage("general.about.btn"));
    localJButton1.setMnemonic(ResourceManager.getVKCode("general.about.btn.mnemonic"));
    localJButton1.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        GeneralPanel.this.aboutBtnActionPerformed(paramActionEvent);
      }
    });
    localJButton1.setToolTipText(getMessage("general.about.btn.tooltip"));
    localJPanel3.add(localJButton1);
    localJPanel2.add(localJSmartTextArea1, "North");
    localJPanel2.add(localJPanel3, "South");
    JPanel localJPanel4 = new JPanel();
    localJPanel4.setLayout(new BorderLayout());
    localJPanel4.setBorder(new TitledBorder(new TitledBorder(new EtchedBorder()), getMessage("general.cache.border.text"), 0, 0));
    JPanel localJPanel5 = new JPanel();
    localJPanel5.setLayout(new FlowLayout(2));
    this.cacheViewBtn = makeButton("general.cache.view.text");
    this.cacheViewBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        GeneralPanel.this.viewBtnAction();
      }
    });
    JButton localJButton2 = makeButton("general.cache.settings.text");
    localJButton2.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        GeneralPanel.this.tempFilesSettingsBtnActionPerformed(paramActionEvent);
      }
    });
    localJButton2.setToolTipText(getMessage("temp.files.settings.btn.tooltip"));
    JButton[] arrayOfJButton = { this.cacheViewBtn, localJButton2 };
    DialogTemplate.resizeButtons(arrayOfJButton);
    JSmartTextArea localJSmartTextArea2 = new JSmartTextArea(getMessage("general.cache.desc.text"));
    localJPanel5.add(localJButton2);
    localJPanel5.add(this.cacheViewBtn);
    localJPanel4.add(localJSmartTextArea2, "North");
    localJPanel4.add(localJPanel5, "South");
    JPanel localJPanel6 = new JPanel();
    localJPanel6.setLayout(new BorderLayout());
    localJPanel6.setBorder(new TitledBorder(new TitledBorder(new EtchedBorder()), getMessage("general.network.border.text"), 0, 0));
    JPanel localJPanel7 = new JPanel();
    localJPanel7.setLayout(new FlowLayout(2));
    JButton localJButton3 = makeButton("general.network.settings.text");
    localJButton3.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        GeneralPanel.this.networkSettingsBtnActionPerformed(paramActionEvent);
      }
    });
    localJButton3.setToolTipText(getMessage("network.settings.btn.tooltip"));
    localJPanel7.add(localJButton3);
    JSmartTextArea localJSmartTextArea3 = new JSmartTextArea(getMessage("general.network.desc.text"));
    localJPanel6.add(localJSmartTextArea3, "North");
    localJPanel6.add(localJPanel7, "South");
    localJPanel1.add(localJPanel2);
    localJPanel1.add(localJPanel6);
    localJPanel1.add(localJPanel4);
    add(localJPanel1, "Center");
  }

  private void aboutBtnActionPerformed(ActionEvent paramActionEvent)
  {
    AboutDialog localAboutDialog = new AboutDialog((JFrame)getTopLevelAncestor(), true);
    localAboutDialog.setLocationRelativeTo(this);
    localAboutDialog.setVisible(true);
  }

  void viewBtnAction()
  {
    String str = Environment.getDeploymentHomePath() + File.separator + "lib" + File.separator + "javaws.jar";
    URL[] arrayOfURL = new URL[1];
    try
    {
      arrayOfURL[0] = new URL("file", null, -1, str);
      Thread localThread = Thread.currentThread();
      URLClassLoader localURLClassLoader = new URLClassLoader(arrayOfURL, localThread.getContextClassLoader());
      localThread.setContextClassLoader(localURLClassLoader);
      Class localClass = localURLClassLoader.loadClass("com.sun.javaws.ui.CacheViewer");
      JFrame localJFrame = (JFrame)getTopLevelAncestor();
      Class[] arrayOfClass = { new JFrame().getClass() };
      Method localMethod = localClass.getMethod("showCacheViewer", arrayOfClass);
      if (!Modifier.isStatic(localMethod.getModifiers()))
        throw new NoSuchMethodException("com.sun.javaws.ui.CacheViewer.showCacheViewer");
      localMethod.setAccessible(true);
      Object[] arrayOfObject = new Object[1];
      arrayOfObject[0] = localJFrame;
      localMethod.invoke(null, arrayOfObject);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  private void tempFilesSettingsBtnActionPerformed(ActionEvent paramActionEvent)
  {
    CacheSettingsDialog localCacheSettingsDialog = new CacheSettingsDialog((JFrame)getTopLevelAncestor(), true);
    localCacheSettingsDialog.pack();
    localCacheSettingsDialog.setLocationRelativeTo(this);
    localCacheSettingsDialog.setVisible(true);
  }

  private void networkSettingsBtnActionPerformed(ActionEvent paramActionEvent)
  {
    NetworkSettingsDialog localNetworkSettingsDialog = new NetworkSettingsDialog((JFrame)getTopLevelAncestor(), true);
    localNetworkSettingsDialog.pack();
    localNetworkSettingsDialog.setLocationRelativeTo(this);
    localNetworkSettingsDialog.setVisible(true);
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  public JButton makeButton(String paramString)
  {
    JButton localJButton = new JButton(getMessage(paramString));
    localJButton.setMnemonic(ResourceManager.getVKCode(paramString + ".mnemonic"));
    return localJButton;
  }

  void enableViewButton(boolean paramBoolean)
  {
    if (!paramBoolean)
    {
      this.cacheViewBtn.setToolTipText(getMessage("general.cache.view.tooltip.unapplied"));
      this.cacheViewBtn.setEnabled(false);
    }
    else if (!Config.getBooleanProperty("deployment.cache.enabled"))
    {
      this.cacheViewBtn.setToolTipText(getMessage("general.cache.view.tooltip.disabled"));
      this.cacheViewBtn.setEnabled(false);
    }
    else
    {
      this.cacheViewBtn.setToolTipText(getMessage("general.cache.view.tooltip"));
      this.cacheViewBtn.setEnabled(true);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.GeneralPanel
 * JD-Core Version:    0.6.0
 */