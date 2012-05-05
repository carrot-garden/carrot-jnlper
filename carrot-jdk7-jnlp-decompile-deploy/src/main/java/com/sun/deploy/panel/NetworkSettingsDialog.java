package com.sun.deploy.panel;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.ui.DialogTemplate;
import com.sun.deploy.ui.UITextArea;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class NetworkSettingsDialog extends JDialog
  implements ActionListener
{
  private ButtonGroup proxySettingsButtonGroup;
  private UITextArea descriptionTextArea;
  private JLabel addressLabel;
  private JLabel portLabel;
  private JLabel locationLabel;
  private JRadioButton browserRbutton;
  private JRadioButton manualRbutton;
  private JRadioButton autoConfigRbutton;
  private JRadioButton directRbutton;
  private JTextField addressTextField;
  private JTextField portTextField;
  private JTextField locationTextField;
  private JCheckBox bypassProxyChbox;
  private JButton advancedBtn;
  private JButton okButton;
  private JButton cancelButton;
  private String http_host;
  private String http_port;
  private String https_host;
  private String https_port;
  private String ftp_host;
  private String ftp_port;
  private String socks_host;
  private String socks_port;
  private String bypass;
  private boolean same_for_all = false;

  NetworkSettingsDialog(JFrame paramJFrame, boolean paramBoolean)
  {
    super(paramJFrame, paramBoolean);
    initComponents();
  }

  private void initComponents()
  {
    this.proxySettingsButtonGroup = new ButtonGroup();
    this.browserRbutton = new JRadioButton();
    this.manualRbutton = new JRadioButton();
    this.bypassProxyChbox = new JCheckBox();
    this.autoConfigRbutton = new JRadioButton();
    this.directRbutton = new JRadioButton();
    setDefaultCloseOperation(2);
    setTitle(getMessage("network.settings.dlg.title"));
    setModal(true);
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent paramWindowEvent)
      {
        NetworkSettingsDialog.this.closeDialog(paramWindowEvent);
      }
    });
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(new BorderLayout());
    localJPanel1.setBorder(new TitledBorder(new TitledBorder(new EtchedBorder()), getMessage("network.settings.dlg.border_title"), 0, 0));
    this.descriptionTextArea = new UITextArea();
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setLayout(new BorderLayout());
    localJPanel2.add(this.descriptionTextArea, "North");
    localJPanel2.add(new JLabel(" "), "Center");
    localJPanel1.add(localJPanel2, "North");
    JPanel localJPanel3 = new JPanel();
    localJPanel3.setLayout(new BoxLayout(localJPanel3, 1));
    JPanel localJPanel4 = new JPanel();
    localJPanel4.setLayout(new BorderLayout());
    this.browserRbutton.setText(getMessage("network.settings.dlg.browser_rbtn"));
    this.browserRbutton.setMnemonic(ResourceManager.getVKCode("browser_rbtn.mnemonic"));
    this.proxySettingsButtonGroup.add(this.browserRbutton);
    localJPanel4.add(this.browserRbutton, "North");
    localJPanel3.add(localJPanel4);
    JPanel localJPanel5 = new JPanel();
    localJPanel5.setLayout(new BorderLayout());
    this.manualRbutton.setText(getMessage("network.settings.dlg.manual_rbtn"));
    this.manualRbutton.setMnemonic(ResourceManager.getVKCode("manual_rbtn.mnemonic"));
    this.proxySettingsButtonGroup.add(this.manualRbutton);
    localJPanel5.add(this.manualRbutton, "North");
    JPanel localJPanel6 = new JPanel();
    localJPanel6.add(Box.createRigidArea(new Dimension(20, 1)));
    this.addressLabel = new JLabel(getMessage("network.settings.dlg.address_lbl"));
    localJPanel6.add(this.addressLabel);
    this.addressTextField = new JTextField("");
    this.addressTextField.setColumns(10);
    localJPanel6.add(this.addressTextField);
    localJPanel6.add(Box.createGlue());
    this.portLabel = new JLabel(getMessage("network.settings.dlg.port_lbl"));
    localJPanel6.add(this.portLabel);
    this.portTextField = new JTextField("");
    this.portTextField.setColumns(3);
    this.portTextField.setDocument(new NumberDocument());
    localJPanel6.add(this.portTextField);
    localJPanel6.add(Box.createGlue());
    localJPanel5.add(localJPanel6, "West");
    JPanel localJPanel7 = new JPanel();
    this.advancedBtn = makeButton("network.settings.dlg.advanced_btn");
    this.advancedBtn.setMnemonic(ResourceManager.getVKCode("network.settings.dlg.advanced_btn.mnemonic"));
    this.advancedBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        NetworkSettingsDialog.this.advancedBtnActionPerformed(paramActionEvent);
      }
    });
    this.advancedBtn.setToolTipText(getMessage("network.settings.advanced_btn.tooltip"));
    localJPanel7.add(this.advancedBtn);
    localJPanel5.add(localJPanel7, "East");
    JPanel localJPanel8 = new JPanel();
    localJPanel8.setLayout(new BoxLayout(localJPanel8, 0));
    localJPanel8.add(Box.createRigidArea(new Dimension(20, 1)));
    this.bypassProxyChbox.setText(getMessage("network.settings.dlg.bypass_text"));
    this.bypassProxyChbox.setMnemonic(ResourceManager.getVKCode("network.settings.dlg.bypass.mnemonic"));
    localJPanel8.add(this.bypassProxyChbox);
    localJPanel8.add(Box.createGlue());
    localJPanel5.add(localJPanel8, "South");
    localJPanel3.add(localJPanel5);
    JPanel localJPanel9 = new JPanel();
    localJPanel9.setLayout(new BorderLayout());
    this.autoConfigRbutton.setText(getMessage("network.settings.dlg.autoconfig_rbtn"));
    this.autoConfigRbutton.setMnemonic(ResourceManager.getVKCode("autoconfig_rbtn.mnemonic"));
    this.proxySettingsButtonGroup.add(this.autoConfigRbutton);
    localJPanel9.add(this.autoConfigRbutton, "North");
    JPanel localJPanel10 = new JPanel();
    localJPanel10.setLayout(new FlowLayout(0));
    this.locationLabel = new JLabel(getMessage("network.settings.dlg.location_lbl"));
    localJPanel10.add(Box.createHorizontalStrut(20));
    localJPanel10.add(this.locationLabel);
    this.locationTextField = new JTextField("");
    this.locationTextField.setColumns(20);
    this.locationTextField.setNextFocusableComponent(this.directRbutton);
    localJPanel10.add(this.locationTextField);
    localJPanel9.add(localJPanel10, "Center");
    localJPanel3.add(localJPanel9);
    JPanel localJPanel11 = new JPanel();
    localJPanel11.setLayout(new BorderLayout());
    this.directRbutton.setText(getMessage("network.settings.dlg.direct_rbtn"));
    this.directRbutton.setMnemonic(ResourceManager.getVKCode("direct_rbtn.mnemonic"));
    this.proxySettingsButtonGroup.add(this.directRbutton);
    localJPanel11.add(this.directRbutton, "North");
    localJPanel3.add(localJPanel11);
    localJPanel1.add(localJPanel3, "Center");
    getContentPane().add(localJPanel1, "Center");
    JPanel localJPanel12 = new JPanel();
    localJPanel12.setLayout(new FlowLayout(2));
    this.okButton = makeButton("common.ok_btn");
    this.okButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        NetworkSettingsDialog.this.okBtnActionPerformed(paramActionEvent);
      }
    });
    localJPanel12.add(this.okButton);
    this.cancelButton = makeButton("common.cancel_btn");
    4 local4 = new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        NetworkSettingsDialog.this.cancelBtnActionPerformed(paramActionEvent);
      }
    };
    this.cancelButton.addActionListener(local4);
    getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
    getRootPane().getActionMap().put("cancel", local4);
    JButton[] arrayOfJButton = { this.okButton, this.cancelButton };
    DialogTemplate.resizeButtons(arrayOfJButton);
    localJPanel12.add(this.cancelButton);
    getContentPane().add(localJPanel12, "South");
    this.browserRbutton.addActionListener(this);
    this.browserRbutton.setActionCommand("useBrowser");
    this.manualRbutton.addActionListener(this);
    this.manualRbutton.setActionCommand("useProxy");
    this.autoConfigRbutton.addActionListener(this);
    this.autoConfigRbutton.setActionCommand("useScript");
    this.directRbutton.addActionListener(this);
    this.directRbutton.setActionCommand("noProxy");
    getRootPane().setDefaultButton(this.okButton);
    setValues();
    pack();
    setResizable(false);
  }

  private void setValues()
  {
    this.addressTextField.setText(Config.getStringProperty("deployment.proxy.http.host"));
    this.portTextField.setText(Config.getStringProperty("deployment.proxy.http.port"));
    this.locationTextField.setText(Config.getStringProperty("deployment.proxy.auto.config.url"));
    this.bypassProxyChbox.setSelected(Config.getBooleanProperty("deployment.proxy.bypass.local"));
    this.same_for_all = Config.getBooleanProperty("deployment.proxy.same");
    String[] arrayOfString = { getMessage("network.settings.dlg.browser_text"), getMessage("network.settings.dlg.proxy_text"), getMessage("network.settings.dlg.auto_text"), getMessage("network.settings.dlg.none_text") };
    String str = "";
    for (int i = 0; i < arrayOfString.length; i++)
    {
      if (arrayOfString[i].length() <= str.length())
        continue;
      str = arrayOfString[i];
    }
    this.descriptionTextArea.setText(str);
    this.descriptionTextArea.setSize(this.descriptionTextArea.getPreferredSize());
    this.descriptionTextArea.setPreferredSize(this.descriptionTextArea.getPreferredSize());
    i = Config.getIntProperty("deployment.proxy.type");
    boolean bool = !Config.get().isPropertyLocked("deployment.proxy.type");
    switch (i)
    {
    case 1:
      this.manualRbutton.setSelected(true);
      useProxy();
      break;
    case 2:
      this.autoConfigRbutton.setSelected(true);
      useScript();
      break;
    case 0:
      this.directRbutton.setSelected(true);
      noProxy();
      break;
    default:
      this.browserRbutton.setSelected(true);
      useBrowser();
    }
    this.manualRbutton.setEnabled(bool);
    this.autoConfigRbutton.setEnabled(bool);
    this.directRbutton.setEnabled(bool);
    this.browserRbutton.setEnabled(bool);
    this.https_host = Config.getStringProperty("deployment.proxy.https.host");
    this.https_port = Config.getStringProperty("deployment.proxy.https.port");
    this.ftp_host = Config.getStringProperty("deployment.proxy.ftp.host");
    this.ftp_port = Config.getStringProperty("deployment.proxy.ftp.port");
    this.socks_host = Config.getStringProperty("deployment.proxy.socks.host");
    this.socks_port = Config.getStringProperty("deployment.proxy.socks.port");
    this.bypass = Config.getStringProperty("deployment.proxy.bypass.list");
  }

  public void updateProxyInfo(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, boolean paramBoolean, String paramString9)
  {
    this.addressTextField.setText(paramString1);
    this.portTextField.setText(paramString2);
    this.http_host = paramString1;
    this.http_port = paramString2;
    this.https_host = paramString3;
    this.https_port = paramString4;
    this.ftp_host = paramString5;
    this.ftp_port = paramString6;
    this.socks_host = paramString7;
    this.socks_port = paramString8;
    this.same_for_all = paramBoolean;
    this.bypass = paramString9;
  }

  public String getProxyAddressField(String paramString)
  {
    if (paramString.equals("deployment.proxy.socks.host"))
      return this.socks_host;
    if (paramString.equals("deployment.proxy.https.host"))
      return this.https_host;
    if (paramString.equals("deployment.proxy.ftp.host"))
      return this.ftp_host;
    return this.addressTextField.getText();
  }

  public String getProxyPortField(String paramString)
  {
    if (paramString.equals("deployment.proxy.socks.port"))
      return this.socks_port;
    if (paramString.equals("deployment.proxy.https.port"))
      return this.https_port;
    if (paramString.equals("deployment.proxy.ftp.port"))
      return this.ftp_port;
    return this.portTextField.getText();
  }

  public boolean getUseSameProxy()
  {
    return this.same_for_all;
  }

  public String getBypassString()
  {
    return this.bypass;
  }

  private void disableAll()
  {
    this.addressLabel.setEnabled(false);
    this.addressTextField.setEnabled(false);
    this.portLabel.setEnabled(false);
    this.portTextField.setEnabled(false);
    this.advancedBtn.setEnabled(false);
    this.bypassProxyChbox.setEnabled(false);
    this.locationLabel.setEnabled(false);
    this.locationTextField.setEnabled(false);
    this.addressTextField.setEditable(false);
    this.locationTextField.setEditable(false);
    this.portTextField.setEditable(false);
  }

  private void useBrowser()
  {
    this.descriptionTextArea.setText(getMessage("network.settings.dlg.browser_text"));
    pack();
    disableAll();
  }

  private void useProxy()
  {
    this.descriptionTextArea.setText(getMessage("network.settings.dlg.proxy_text"));
    disableAll();
    if (!Config.get().isPropertyLocked("deployment.proxy.http.host"))
    {
      this.addressLabel.setEnabled(true);
      this.addressTextField.setEnabled(true);
      this.addressTextField.setEditable(true);
    }
    if (!Config.get().isPropertyLocked("deployment.proxy.http.port"))
    {
      this.portLabel.setEnabled(true);
      this.portTextField.setEnabled(true);
      this.portTextField.setEditable(true);
    }
    this.advancedBtn.setEnabled(true);
    if (!Config.get().isPropertyLocked("deployment.proxy.bypass.local"))
      this.bypassProxyChbox.setEnabled(true);
  }

  private void useScript()
  {
    this.descriptionTextArea.setText(getMessage("network.settings.dlg.auto_text"));
    disableAll();
    if (!Config.get().isPropertyLocked("deployment.proxy.auto.config.url"))
    {
      this.locationLabel.setEnabled(true);
      this.locationTextField.setEnabled(true);
      this.locationTextField.setEditable(true);
    }
  }

  private void noProxy()
  {
    this.descriptionTextArea.setText(getMessage("network.settings.dlg.none_text"));
    disableAll();
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    String str = paramActionEvent.getActionCommand();
    if (str.equalsIgnoreCase("useBrowser"))
      useBrowser();
    else if (str.equalsIgnoreCase("useProxy"))
      useProxy();
    else if (str.equalsIgnoreCase("useScript"))
      useScript();
    else if (str.equalsIgnoreCase("noProxy"))
      noProxy();
  }

  private void advancedBtnActionPerformed(ActionEvent paramActionEvent)
  {
    AdvancedNetworkSettingsDialog localAdvancedNetworkSettingsDialog = new AdvancedNetworkSettingsDialog(this, true);
    localAdvancedNetworkSettingsDialog.setLocationRelativeTo(this);
    localAdvancedNetworkSettingsDialog.setVisible(true);
  }

  private void okBtnActionPerformed(ActionEvent paramActionEvent)
  {
    if (this.browserRbutton.isSelected())
      Config.setIntProperty("deployment.proxy.type", 3);
    else if (this.manualRbutton.isSelected())
      Config.setIntProperty("deployment.proxy.type", 1);
    else if (this.autoConfigRbutton.isSelected())
      Config.setIntProperty("deployment.proxy.type", 2);
    else if (this.directRbutton.isSelected())
      Config.setIntProperty("deployment.proxy.type", 0);
    Config.setStringProperty("deployment.proxy.auto.config.url", this.locationTextField.getText());
    if (this.same_for_all)
    {
      Config.setStringProperty("deployment.proxy.http.host", this.addressTextField.getText());
      Config.setStringProperty("deployment.proxy.http.port", this.portTextField.getText());
      Config.setStringProperty("deployment.proxy.https.host", this.addressTextField.getText());
      Config.setStringProperty("deployment.proxy.https.port", this.portTextField.getText());
      Config.setStringProperty("deployment.proxy.ftp.host", this.addressTextField.getText());
      Config.setStringProperty("deployment.proxy.ftp.port", this.portTextField.getText());
    }
    else
    {
      Config.setStringProperty("deployment.proxy.http.host", this.addressTextField.getText());
      Config.setStringProperty("deployment.proxy.http.port", this.portTextField.getText());
      Config.setStringProperty("deployment.proxy.https.host", this.https_host);
      Config.setStringProperty("deployment.proxy.https.port", this.https_port);
      Config.setStringProperty("deployment.proxy.ftp.host", this.ftp_host);
      Config.setStringProperty("deployment.proxy.ftp.port", this.ftp_port);
    }
    Config.setStringProperty("deployment.proxy.socks.host", this.socks_host);
    Config.setStringProperty("deployment.proxy.socks.port", this.socks_port);
    Config.setBooleanProperty("deployment.proxy.same", this.same_for_all);
    Config.setStringProperty("deployment.proxy.bypass.list", this.bypass);
    Config.setBooleanProperty("deployment.proxy.bypass.local", this.bypassProxyChbox.isSelected());
    setVisible(false);
    dispose();
  }

  private void cancelBtnActionPerformed(ActionEvent paramActionEvent)
  {
    setVisible(false);
    dispose();
  }

  private void closeDialog(WindowEvent paramWindowEvent)
  {
    setVisible(false);
    dispose();
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  public JButton makeButton(String paramString)
  {
    JButton localJButton = new JButton(getMessage(paramString));
    return localJButton;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.NetworkSettingsDialog
 * JD-Core Version:    0.6.0
 */