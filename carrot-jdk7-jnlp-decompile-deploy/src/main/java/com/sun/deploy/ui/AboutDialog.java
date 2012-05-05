package com.sun.deploy.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.OSType;
import com.sun.deploy.config.Platform;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.si.DeploySIListener;
import com.sun.deploy.si.SingleInstanceImpl;
import com.sun.deploy.si.SingleInstanceManager;
import com.sun.deploy.util.DeployUIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

public class AboutDialog extends JDialog
  implements DeploySIListener
{
  private SingleInstanceImpl sil = null;
  private static String ABOUT_JAVA_ID = "com_sun_deploy_AboutJava-" + Config.getStringProperty("deployment.version");
  private final String TEXT_COLOR = "#7A7277";
  private final String LINK_COLOR = "#214F83";
  private final String LINK_HIGHLIGHT_COLOR = "#C03F3F";

  public AboutDialog(JFrame paramJFrame, boolean paramBoolean1, boolean paramBoolean2)
  {
    super(paramJFrame, paramBoolean1);
    if (paramBoolean2)
      initSIImpl();
    initComponents();
  }

  public AboutDialog(JFrame paramJFrame, boolean paramBoolean)
  {
    super(paramJFrame, paramBoolean);
    initComponents();
  }

  public AboutDialog(JDialog paramJDialog, boolean paramBoolean)
  {
    super(paramJDialog, paramBoolean);
    initComponents();
  }

  private void initSIImpl()
  {
    this.sil = new SingleInstanceImpl();
    this.sil.addSingleInstanceListener(this, ABOUT_JAVA_ID);
  }

  public static boolean shouldStartNewInstance()
  {
    return (!SingleInstanceManager.isServerRunning(ABOUT_JAVA_ID)) || (!SingleInstanceManager.connectToServer(""));
  }

  private void initComponents()
  {
    setTitle(getMessage("about.dialog.title"));
    setDefaultCloseOperation(2);
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent paramWindowEvent)
      {
        AboutDialog.this.closeDialog(paramWindowEvent);
      }
    });
    Color localColor1 = Color.white;
    Color localColor2 = new Color(122, 114, 119);
    JPanel localJPanel1 = new JPanel(new BorderLayout());
    localJPanel1.setForeground(localColor1);
    localJPanel1.setBackground(localColor1);
    localJPanel1.setOpaque(true);
    localJPanel1.setBorder(BorderFactory.createLineBorder(Color.black));
    GridBagLayout localGridBagLayout1 = new GridBagLayout();
    GridBagConstraints localGridBagConstraints1 = new GridBagConstraints();
    localJPanel1.setLayout(localGridBagLayout1);
    localGridBagConstraints1.fill = 1;
    URL localURL = ClassLoader.getSystemResource("com/sun/deploy/resources/image/aboutjava.png");
    ImageIcon localImageIcon1 = new ImageIcon(localURL);
    JLabel localJLabel1 = new JLabel(localImageIcon1);
    localGridBagConstraints1.gridwidth = 0;
    localGridBagLayout1.setConstraints(localJLabel1, localGridBagConstraints1);
    localJPanel1.add(localJLabel1);
    int i = localImageIcon1.getIconWidth() - 20;
    String str1 = System.getProperty("java.version");
    int j = str1.indexOf(".");
    String str2 = str1.substring(j + 1, str1.indexOf(".", j + 1));
    int k = str1.lastIndexOf("_");
    String str3 = null;
    if (k != -1)
    {
      int m = str1.indexOf("-");
      if (m != -1)
        str3 = str1.substring(k + 1, m);
      else
        str3 = str1.substring(k + 1, str1.length());
      if (str3.startsWith("0"))
        str3 = str3.substring(1);
    }
    String str4 = null;
    if (str3 != null)
      str4 = MessageFormat.format(getMessage("about.java.version.update"), new String[] { str2, str3 });
    else
      str4 = MessageFormat.format(getMessage("about.java.version"), new String[] { str2 });
    String str5 = MessageFormat.format(getMessage("about.java.build"), new String[] { System.getProperty("java.runtime.version") });
    Font localFont = ResourceManager.getUIFont().deriveFont(DialogTemplate.getSubpanelFontSize());
    JLabel localJLabel2 = new JLabel(str4);
    localJLabel2.setForeground(localColor2);
    localJLabel2.setBackground(localColor1);
    localJLabel2.setFont(localFont);
    JTextArea localJTextArea = new JTextArea(str5);
    localJTextArea.setEditable(false);
    localJTextArea.setForeground(localColor2);
    localJTextArea.setBackground(localColor1);
    localJTextArea.setFont(localFont);
    String str6 = getMessage("about.copyright");
    String str7 = getMessage("about.prompt.info");
    String str8 = getMessage("about.home.link");
    UITextArea localUITextArea = new UITextArea(DialogTemplate.getSubpanelFontSize(), i, false);
    localUITextArea.setText(str6);
    localUITextArea.setForeground(localColor2);
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setBackground(localColor1);
    GridBagLayout localGridBagLayout2 = new GridBagLayout();
    GridBagConstraints localGridBagConstraints2 = new GridBagConstraints();
    localJPanel2.setLayout(localGridBagLayout2);
    localGridBagConstraints2.fill = 1;
    localGridBagConstraints2.gridwidth = -1;
    localGridBagLayout2.setConstraints(localJLabel2, localGridBagConstraints2);
    localJPanel2.add(localJLabel2);
    localGridBagConstraints2.gridwidth = 0;
    localGridBagConstraints2.fill = 2;
    localGridBagConstraints2.anchor = 15;
    localGridBagConstraints2.insets = new Insets(0, 10, 0, 0);
    localGridBagLayout2.setConstraints(localJTextArea, localGridBagConstraints2);
    localJPanel2.add(localJTextArea);
    localGridBagConstraints2.fill = 1;
    localGridBagConstraints2.insets = new Insets(0, 0, 0, 0);
    localGridBagConstraints2.weightx = 0.0D;
    localGridBagLayout2.setConstraints(localUITextArea, localGridBagConstraints2);
    localJPanel2.add(localUITextArea);
    String str9 = "<font face=" + ResourceManager.getUIFont().getFamily() + " color=#7A7277>" + str7 + " " + " <a style='color:#214F83' href=" + str8 + '>' + str8 + "</a></font>";
    2 local2 = new JEditorPane(i)
    {
      private final int val$lineWidth;

      public Dimension getPreferredSize()
      {
        if (getWidth() == 0)
        {
          localObject = getInsets();
          setSize(this.val$lineWidth, ((Insets)localObject).top + ((Insets)localObject).bottom + 1);
        }
        Object localObject = super.getPreferredSize();
        ((Dimension)localObject).width = getWidth();
        return (Dimension)localObject;
      }
    };
    local2.setFont(localFont);
    local2.putClientProperty("JEditorPane.honorDisplayProperties", Boolean.TRUE);
    local2.setContentType("text/html");
    local2.setText(str9);
    local2.setForeground(localColor2);
    local2.setBackground(localColor1);
    FontMetrics localFontMetrics = local2.getFontMetrics(localFont);
    int n = localFontMetrics.stringWidth(str7 + str8);
    local2.setEditable(false);
    local2.addHyperlinkListener(new HyperlinkListener(local2, str9)
    {
      private final JEditorPane val$editPane;
      private final String val$infoText;

      public void hyperlinkUpdate(HyperlinkEvent paramHyperlinkEvent)
      {
        if (paramHyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
          Platform.get().showDocument(paramHyperlinkEvent.getURL().toString());
        else if (paramHyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ENTERED)
          this.val$editPane.setText(this.val$infoText.replaceFirst("color:#214F83", "color:#C03F3F"));
        else if (paramHyperlinkEvent.getEventType() == HyperlinkEvent.EventType.EXITED)
          this.val$editPane.setText(this.val$infoText);
      }
    });
    localGridBagConstraints1.insets = new Insets(10, 10, 0, 10);
    localGridBagLayout1.setConstraints(localJPanel2, localGridBagConstraints1);
    localJPanel1.add(localJPanel2);
    localGridBagConstraints1.insets = new Insets(10, 10, 30, 10);
    localGridBagLayout1.setConstraints(local2, localGridBagConstraints1);
    localJPanel1.add(local2);
    JLabel localJLabel3 = new JLabel();
    ImageIcon localImageIcon2 = ResourceManager.getIcon("sun.logo.image");
    localJLabel3.setIcon(localImageIcon2);
    localGridBagConstraints1.insets = new Insets(0, 10, 10, 10);
    localGridBagLayout1.setConstraints(localJLabel3, localGridBagConstraints1);
    localJPanel1.add(localJLabel3);
    JPanel localJPanel3 = new JPanel(new FlowLayout(2, 10, 10));
    JButton localJButton = new JButton(getMessage("about.option.close"));
    4 local4 = new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        AboutDialog.this.okBtnActionPerformed(paramActionEvent);
      }
    };
    localJButton.addActionListener(local4);
    KeyStroke localKeyStroke = KeyStroke.getKeyStroke(27, 0);
    getRootPane().getInputMap(2).put(localKeyStroke, "cancel");
    getRootPane().getActionMap().put("cancel", local4);
    local2.getInputMap().put(localKeyStroke, "cancel");
    local2.getActionMap().put("cancel", null);
    localJPanel3.add(localJButton);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(localJPanel1, "Center");
    if (OSType.getOSType() != 3)
      getContentPane().add(localJPanel3, "South");
    else
      getRootPane().putClientProperty("Window.zoomable", "false");
    getRootPane().setDefaultButton(localJButton);
    pack();
    setResizable(false);
  }

  private void okBtnActionPerformed(ActionEvent paramActionEvent)
  {
    if (this.sil != null)
      this.sil.removeSingleInstanceListener(this);
    setVisible(false);
    dispose();
  }

  private void closeDialog(WindowEvent paramWindowEvent)
  {
    if (this.sil != null)
      this.sil.removeSingleInstanceListener(this);
    setVisible(false);
    dispose();
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  public void newActivation(String[] paramArrayOfString)
  {
    toFront();
  }

  public Object getSingleInstanceListener()
  {
    return this;
  }

  static
  {
    DeployUIManager.setLookAndFeel();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.AboutDialog
 * JD-Core Version:    0.6.0
 */