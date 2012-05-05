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

public class SecurityPanel extends JPanel
{
  public SecurityPanel()
  {
    initComponents();
  }

  public void initComponents()
  {
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(new BorderLayout());
    localJPanel1.setBorder(new TitledBorder(new TitledBorder(new EtchedBorder()), getMessage("security.certificates.border.text"), 0, 0));
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setLayout(new FlowLayout(2));
    JButton localJButton = new JButton(getMessage("security.certificates.button.text"));
    localJButton.setMnemonic(ResourceManager.getVKCode("security.certificates.button.mnemonic"));
    localJButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        SecurityPanel.this.certsBtnActionPerformed(paramActionEvent);
      }
    });
    localJButton.setToolTipText(getMessage("security.certs_btn.tooltip"));
    localJPanel2.add(localJButton);
    JSmartTextArea localJSmartTextArea = new JSmartTextArea(getMessage("security.certificates.desc.text"));
    localJPanel1.add(localJSmartTextArea, "North");
    localJPanel1.add(localJPanel2, "Center");
    add(localJPanel1, "Center");
  }

  private void certsBtnActionPerformed(ActionEvent paramActionEvent)
  {
    CertificatesDialog localCertificatesDialog = new CertificatesDialog((JFrame)getTopLevelAncestor(), true);
    localCertificatesDialog.setLocationRelativeTo(this);
    localCertificatesDialog.setVisible(true);
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.SecurityPanel
 * JD-Core Version:    0.6.0
 */