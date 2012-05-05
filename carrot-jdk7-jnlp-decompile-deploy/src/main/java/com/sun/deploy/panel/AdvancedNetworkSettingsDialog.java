package com.sun.deploy.panel;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.ui.DialogTemplate;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;

public class AdvancedNetworkSettingsDialog extends JDialog
{
  private JTextArea bypassTextArea;
  private JTextField httpTextField;
  private JTextField secureTextField;
  private JTextField ftpTextField;
  private JTextField socksTextField;
  private JTextField httpPortTextField;
  private JTextField securePortTextField;
  private JTextField ftpPortTextField;
  private JTextField socksPortTextField;
  private JButton okButton;
  private JButton cancelButton;
  private JCheckBox useForAllChBox;
  private String lastFtpProxy;
  private String lastFtpPort;
  private String lastSecureProxy;
  private String lastSecurePort;

  public AdvancedNetworkSettingsDialog(Dialog paramDialog, boolean paramBoolean)
  {
    super(paramDialog, paramBoolean);
    initComponents();
  }

  private void initComponents()
  {
    setTitle(getMessage("advanced.network.dlg.title"));
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent paramWindowEvent)
      {
        AdvancedNetworkSettingsDialog.this.closeDialog();
      }
    });
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(new BorderLayout());
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setLayout(new BorderLayout());
    localJPanel2.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), getMessage("advanced.network.dlg.servers"), 0, 0));
    JPanel localJPanel3 = new JPanel();
    localJPanel3.setLayout(new BoxLayout(localJPanel3, 0));
    JPanel localJPanel4 = new JPanel();
    localJPanel4.setLayout(new GridLayout(5, 1, 0, 5));
    JLabel localJLabel1 = new JLabel(getMessage("advanced.network.dlg.type"));
    localJLabel1.setHorizontalAlignment(0);
    JLabel localJLabel2 = new JLabel(getMessage("advanced.network.dlg.http"));
    JLabel localJLabel3 = new JLabel(getMessage("advanced.network.dlg.secure"));
    JLabel localJLabel4 = new JLabel(getMessage("advanced.network.dlg.ftp"));
    JLabel localJLabel5 = new JLabel(getMessage("advanced.network.dlg.socks"));
    localJPanel4.add(localJLabel1);
    localJPanel4.add(localJLabel2);
    localJPanel4.add(localJLabel3);
    localJPanel4.add(localJLabel4);
    localJPanel4.add(localJLabel5);
    localJPanel3.add(Box.createHorizontalStrut(5));
    localJPanel3.add(localJPanel4);
    localJPanel3.add(Box.createHorizontalStrut(5));
    JPanel localJPanel5 = new JPanel();
    localJPanel5.setLayout(new GridLayout(5, 1, 0, 5));
    JLabel localJLabel6 = new JLabel(getMessage("advanced.network.dlg.proxy_address"));
    localJLabel6.setHorizontalAlignment(0);
    this.httpTextField = new JTextField(20);
    this.secureTextField = new JTextField(20);
    this.ftpTextField = new JTextField(20);
    this.socksTextField = new JTextField(20);
    localJPanel5.add(localJLabel6);
    localJPanel5.add(this.httpTextField);
    localJPanel5.add(this.secureTextField);
    localJPanel5.add(this.ftpTextField);
    localJPanel5.add(this.socksTextField);
    localJPanel3.add(localJPanel5);
    localJPanel3.add(Box.createHorizontalStrut(5));
    JPanel localJPanel6 = new JPanel();
    localJPanel6.setLayout(new GridLayout(5, 1, 0, 5));
    JLabel localJLabel7 = new JLabel(":");
    localJLabel7.setHorizontalAlignment(0);
    JLabel localJLabel8 = new JLabel(":");
    localJLabel8.setHorizontalAlignment(0);
    JLabel localJLabel9 = new JLabel(":");
    localJLabel9.setHorizontalAlignment(0);
    JLabel localJLabel10 = new JLabel(":");
    localJLabel10.setHorizontalAlignment(0);
    localJPanel6.add(Box.createGlue());
    localJPanel6.add(localJLabel7);
    localJPanel6.add(localJLabel8);
    localJPanel6.add(localJLabel9);
    localJPanel6.add(localJLabel10);
    localJPanel3.add(localJPanel6);
    localJPanel3.add(Box.createHorizontalStrut(5));
    JPanel localJPanel7 = new JPanel();
    localJPanel7.setLayout(new GridLayout(5, 1, 0, 5));
    JLabel localJLabel11 = new JLabel(getMessage("advanced.network.dlg.port"));
    localJLabel11.setHorizontalAlignment(0);
    this.httpPortTextField = new JTextField(6);
    this.httpPortTextField.setDocument(new NumberDocument());
    this.securePortTextField = new JTextField(6);
    this.securePortTextField.setDocument(new NumberDocument());
    this.ftpPortTextField = new JTextField(6);
    this.ftpPortTextField.setDocument(new NumberDocument());
    this.socksPortTextField = new JTextField(6);
    this.socksPortTextField.setDocument(new NumberDocument());
    localJPanel7.add(localJLabel11);
    localJPanel7.add(this.httpPortTextField);
    localJPanel7.add(this.securePortTextField);
    localJPanel7.add(this.ftpPortTextField);
    localJPanel7.add(this.socksPortTextField);
    localJPanel3.add(localJPanel7);
    localJPanel3.add(Box.createHorizontalStrut(5));
    JPanel localJPanel8 = new JPanel();
    localJPanel8.setLayout(new FlowLayout(1));
    this.useForAllChBox = new JCheckBox(getMessage("advanced.network.dlg.same_proxy"));
    this.useForAllChBox.setSelected(false);
    this.useForAllChBox.setMnemonic(ResourceManager.getVKCode("advanced.network.dlg.same_proxy.mnemonic"));
    this.useForAllChBox.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent paramItemEvent)
      {
        AdvancedNetworkSettingsDialog.this.useForAllChBoxItemStateChanged(paramItemEvent);
      }
    });
    localJPanel8.add(this.useForAllChBox);
    localJPanel2.add(localJPanel3, "Center");
    localJPanel2.add(localJPanel8, "South");
    localJPanel1.add(localJPanel2, "North");
    JPanel localJPanel9 = new JPanel();
    localJPanel9.setLayout(new BorderLayout());
    localJPanel9.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), getMessage("advanced.network.dlg.exceptions"), 0, 0));
    JLabel localJLabel12 = new JLabel(getMessage("advanced.network.dlg.no_proxy"));
    this.bypassTextArea = new JTextArea(3, 1);
    this.bypassTextArea.setFont(ResourceManager.getUIFont());
    this.bypassTextArea.setLineWrap(true);
    JScrollPane localJScrollPane = new JScrollPane();
    localJScrollPane.setHorizontalScrollBarPolicy(31);
    localJScrollPane.setVerticalScrollBarPolicy(22);
    localJScrollPane.setAutoscrolls(true);
    localJScrollPane.setViewportView(this.bypassTextArea);
    JLabel localJLabel13 = new JLabel(getMessage("advanced.network.dlg.no_proxy_note"));
    JPanel localJPanel10 = new JPanel();
    localJPanel10.setLayout(new BorderLayout());
    localJPanel10.add(localJLabel12, "North");
    localJPanel10.add(localJScrollPane, "Center");
    localJPanel10.add(localJLabel13, "South");
    localJPanel9.add(Box.createHorizontalStrut(10), "West");
    localJPanel9.add(localJPanel10, "Center");
    localJPanel9.add(Box.createHorizontalStrut(10), "East");
    localJPanel9.add(Box.createVerticalStrut(5), "South");
    localJPanel1.add(localJPanel9, "Center");
    JPanel localJPanel11 = new JPanel();
    localJPanel11.setLayout(new FlowLayout(2));
    this.okButton = new JButton(getMessage("common.ok_btn"));
    this.okButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        AdvancedNetworkSettingsDialog.this.okButtonActionPerformed(paramActionEvent);
      }
    });
    this.cancelButton = new JButton(getMessage("common.cancel_btn"));
    JButton[] arrayOfJButton = { this.okButton, this.cancelButton };
    DialogTemplate.resizeButtons(arrayOfJButton);
    4 local4 = new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        AdvancedNetworkSettingsDialog.this.closeDialog();
      }
    };
    this.cancelButton.addActionListener(local4);
    getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
    getRootPane().getActionMap().put("cancel", local4);
    localJPanel11.add(this.okButton);
    localJPanel11.add(this.cancelButton);
    getRootPane().setDefaultButton(this.okButton);
    localJPanel1.add(localJPanel11, "South");
    getContentPane().add(localJPanel1);
    pack();
    setValues();
    setResizable(false);
    setFocusSequence();
  }

  private void setValues()
  {
    boolean bool;
    if ((getParent() != null) && ((getParent() instanceof NetworkSettingsDialog)))
    {
      getValuesFromParent();
      bool = ((NetworkSettingsDialog)getParent()).getUseSameProxy();
      this.useForAllChBox.setSelected(bool);
    }
    else
    {
      getValuesFromConfig();
      bool = Config.getBooleanProperty("deployment.proxy.same");
      this.useForAllChBox.setSelected(bool);
    }
    this.useForAllChBox.setEnabled(!Config.get().isPropertyLocked("deployment.proxy.same"));
    this.bypassTextArea.setEditable(!Config.get().isPropertyLocked("deployment.proxy.bypass.list"));
    setUseForAll(bool);
  }

  private void getValuesFromParent()
  {
    NetworkSettingsDialog localNetworkSettingsDialog = (NetworkSettingsDialog)getParent();
    if (this.httpTextField.getText().trim().equals(""))
      this.httpTextField.setText(localNetworkSettingsDialog.getProxyAddressField("deployment.proxy.http.host"));
    if (this.httpPortTextField.getText().trim().equals(""))
      this.httpPortTextField.setText(localNetworkSettingsDialog.getProxyPortField("deployment.proxy.http.port"));
    if (this.secureTextField.getText().trim().equals(""))
      this.secureTextField.setText(localNetworkSettingsDialog.getProxyAddressField("deployment.proxy.https.host"));
    if (this.securePortTextField.getText().trim().equals(""))
      this.securePortTextField.setText(localNetworkSettingsDialog.getProxyPortField("deployment.proxy.https.port"));
    if (this.ftpTextField.getText().trim().equals(""))
      this.ftpTextField.setText(localNetworkSettingsDialog.getProxyAddressField("deployment.proxy.ftp.host"));
    if (this.ftpPortTextField.getText().trim().equals(""))
      this.ftpPortTextField.setText(localNetworkSettingsDialog.getProxyPortField("deployment.proxy.ftp.port"));
    if (this.socksTextField.getText().trim().equals(""))
      this.socksTextField.setText(localNetworkSettingsDialog.getProxyAddressField("deployment.proxy.socks.host"));
    if (this.socksPortTextField.getText().trim().equals(""))
      this.socksPortTextField.setText(localNetworkSettingsDialog.getProxyPortField("deployment.proxy.socks.port"));
    if (this.bypassTextArea.getText().trim().equals(""))
      this.bypassTextArea.setText(localNetworkSettingsDialog.getBypassString());
  }

  private void getValuesFromConfig()
  {
    if (this.httpTextField.getText().trim().equals(""))
      this.httpTextField.setText(Config.getStringProperty("deployment.proxy.http.host"));
    if (this.httpPortTextField.getText().trim().equals(""))
      this.httpPortTextField.setText(Config.getStringProperty("deployment.proxy.http.port"));
    if (this.secureTextField.getText().trim().equals(""))
      this.secureTextField.setText(Config.getStringProperty("deployment.proxy.https.host"));
    if (this.securePortTextField.getText().trim().equals(""))
      this.securePortTextField.setText(Config.getStringProperty("deployment.proxy.https.port"));
    if (this.ftpTextField.getText().trim().equals(""))
      this.ftpTextField.setText(Config.getStringProperty("deployment.proxy.ftp.host"));
    if (this.ftpPortTextField.getText().trim().equals(""))
      this.ftpPortTextField.setText(Config.getStringProperty("deployment.proxy.ftp.port"));
    if (this.socksTextField.getText().trim().equals(""))
      this.socksTextField.setText(Config.getStringProperty("deployment.proxy.socks.host"));
    if (this.socksPortTextField.getText().trim().equals(""))
      this.socksPortTextField.setText(Config.getStringProperty("deployment.proxy.socks.port"));
    if (this.bypassTextArea.getText().trim().equals(""))
      this.bypassTextArea.setText(Config.getStringProperty("deployment.proxy.bypass.list"));
  }

  private void setFocusSequence()
  {
    this.httpTextField.setNextFocusableComponent(this.httpPortTextField);
    this.secureTextField.setNextFocusableComponent(this.securePortTextField);
    this.ftpTextField.setNextFocusableComponent(this.ftpPortTextField);
    this.socksTextField.setNextFocusableComponent(this.socksPortTextField);
    this.httpPortTextField.setNextFocusableComponent(this.secureTextField);
    this.securePortTextField.setNextFocusableComponent(this.ftpTextField);
    this.ftpPortTextField.setNextFocusableComponent(this.socksTextField);
    this.bypassTextArea.setNextFocusableComponent(this.okButton);
  }

  private void setComponent(JComponent paramJComponent, boolean paramBoolean)
  {
    if (((paramJComponent instanceof JLabel)) || ((paramJComponent instanceof JCheckBox)) || ((paramJComponent instanceof JButton)))
    {
      paramJComponent.setEnabled(paramBoolean);
    }
    else if ((paramJComponent instanceof JTextField))
    {
      paramJComponent.setEnabled(paramBoolean);
      ((JTextField)paramJComponent).setEditable(paramBoolean);
    }
  }

  private void okButtonActionPerformed(ActionEvent paramActionEvent)
  {
    if ((getParent() instanceof NetworkSettingsDialog))
      ((NetworkSettingsDialog)getParent()).updateProxyInfo(this.httpTextField.getText(), this.httpPortTextField.getText(), this.secureTextField.getText(), this.securePortTextField.getText(), this.ftpTextField.getText(), this.ftpPortTextField.getText(), this.socksTextField.getText(), this.socksPortTextField.getText(), this.useForAllChBox.isSelected(), this.bypassTextArea.getText());
    else
      savePropertiesInConfig();
    setVisible(false);
    dispose();
  }

  private void savePropertiesInConfig()
  {
    Config.setStringProperty("deployment.proxy.http.host", this.httpTextField.getText());
    Config.setStringProperty("deployment.proxy.http.port", this.httpPortTextField.getText());
    Config.setStringProperty("deployment.proxy.https.host", this.secureTextField.getText());
    Config.setStringProperty("deployment.proxy.https.port", this.securePortTextField.getText());
    Config.setStringProperty("deployment.proxy.ftp.host", this.ftpTextField.getText());
    Config.setStringProperty("deployment.proxy.ftp.port", this.ftpPortTextField.getText());
    Config.setStringProperty("deployment.proxy.socks.host", this.socksTextField.getText());
    Config.setStringProperty("deployment.proxy.socks.port", this.socksPortTextField.getText());
    Config.setBooleanProperty("deployment.proxy.same", this.useForAllChBox.isSelected());
    Config.setStringProperty("deployment.proxy.bypass.list", this.bypassTextArea.getText());
  }

  private void useForAllChBoxItemStateChanged(ItemEvent paramItemEvent)
  {
    if (this.useForAllChBox.isSelected())
    {
      this.lastFtpProxy = this.ftpTextField.getText();
      this.lastFtpPort = this.ftpPortTextField.getText();
      this.lastSecureProxy = this.secureTextField.getText();
      this.lastSecurePort = this.securePortTextField.getText();
    }
    else
    {
      this.secureTextField.setText(this.lastSecureProxy);
      this.securePortTextField.setText(this.lastSecurePort);
      this.ftpTextField.setText(this.lastFtpProxy);
      this.ftpPortTextField.setText(this.lastFtpPort);
    }
    setUseForAll(this.useForAllChBox.isSelected());
  }

  private void setUseForAll(boolean paramBoolean)
  {
    if ((getParent() != null) && ((getParent() instanceof NetworkSettingsDialog)))
      getValuesFromParent();
    else
      getValuesFromConfig();
    setTextFields(!paramBoolean);
    if (paramBoolean)
    {
      this.secureTextField.setText(this.httpTextField.getText());
      this.securePortTextField.setText(this.httpPortTextField.getText());
      this.ftpTextField.setText(this.httpTextField.getText());
      this.ftpPortTextField.setText(this.httpPortTextField.getText());
    }
  }

  private void setTextFields(boolean paramBoolean)
  {
    boolean bool = !Config.get().isPropertyLocked("deployment.proxy.http.host");
    this.httpTextField.setEnabled(bool);
    this.httpTextField.setEditable(bool);
    bool = !Config.get().isPropertyLocked("deployment.proxy.http.port");
    this.httpPortTextField.setEnabled(bool);
    this.httpPortTextField.setEditable(bool);
    bool = (paramBoolean) && (!Config.get().isPropertyLocked("deployment.proxy.https.host"));
    this.secureTextField.setEnabled(bool);
    this.secureTextField.setEditable(bool);
    bool = (paramBoolean) && (!Config.get().isPropertyLocked("deployment.proxy.https.port"));
    this.securePortTextField.setEnabled(bool);
    this.securePortTextField.setEditable(bool);
    bool = (paramBoolean) && (!Config.get().isPropertyLocked("deployment.proxy.ftp.host"));
    this.ftpTextField.setEnabled(bool);
    this.ftpTextField.setEditable(bool);
    bool = (paramBoolean) && (!Config.get().isPropertyLocked("deployment.proxy.ftp.port"));
    this.ftpPortTextField.setEnabled(bool);
    this.ftpPortTextField.setEditable(bool);
    bool = !Config.get().isPropertyLocked("deployment.proxy.socks.host");
    this.socksTextField.setEnabled(bool);
    this.socksTextField.setEditable(bool);
    bool = !Config.get().isPropertyLocked("deployment.proxy.socks.port");
    this.socksPortTextField.setEnabled(bool);
    this.socksPortTextField.setEditable(bool);
  }

  private void closeDialog()
  {
    setVisible(false);
    dispose();
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.AdvancedNetworkSettingsDialog
 * JD-Core Version:    0.6.0
 */