package com.sun.deploy.panel;

import com.sun.deploy.resources.ResourceManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class JavaPanel extends JPanel
{
  private JSmartTextArea jreTextArea;
  private JButton jreSettingsBtn;

  public JavaPanel()
  {
    initComponents();
  }

  private void initComponents()
  {
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setBorder(new TitledBorder(new TitledBorder(new EtchedBorder()), getMessage("java.panel.jre.border"), 0, 0));
    localJPanel1.setLayout(new BorderLayout());
    this.jreTextArea = new JSmartTextArea(getMessage("java.panel.jre.text"));
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setLayout(new FlowLayout(2));
    this.jreSettingsBtn = new JButton(getMessage("java.panel.jre_view_btn"));
    this.jreSettingsBtn.setMnemonic(ResourceManager.getVKCode("java.panel.jre_view_btn.mnemonic"));
    this.jreSettingsBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        JavaPanel.this.jreSettingsBtnActionPerformed(paramActionEvent);
      }
    });
    this.jreSettingsBtn.setToolTipText(getMessage("java.panel.jre_view_btn.tooltip"));
    localJPanel2.add(this.jreSettingsBtn);
    localJPanel1.add(this.jreTextArea, "North");
    localJPanel1.add(localJPanel2, "Center");
    add(localJPanel1, "Center");
  }

  private void jreSettingsBtnActionPerformed(ActionEvent paramActionEvent)
  {
    JreDialog localJreDialog = new JreDialog((JFrame)getTopLevelAncestor(), true);
    localJreDialog.setLocationRelativeTo(this);
    localJreDialog.setVisible(true);
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.JavaPanel
 * JD-Core Version:    0.6.0
 */