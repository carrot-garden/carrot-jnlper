package com.sun.deploy.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.Platform;
import com.sun.deploy.panel.JSmartTextArea;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.security.CertificateDialog;
import com.sun.deploy.security.URLClassPathControl;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.DialogListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

public class DialogTemplate
  implements ActionListener, ImageLoaderCallback
{
  private final Color LINK_COLOR = new Color(33, 79, 131);
  private final Color LINK_HIGHLIGHT_COLOR = new Color(192, 63, 63);
  private Font ssvFont;
  private Font ssvBigFont;
  private Font ssvBigBoldFont;
  private Font ssvSmallFont;
  private Font ssvSmallBoldFont;
  private DialogInterface dialogInterface;
  private AppInfo ainfo = null;
  private String topText = null;
  private String appTitle = null;
  private String appPublisher = null;
  private URL appURL = null;
  private Frame dummyFrame = null;
  private boolean useErrorIcon = false;
  private boolean useWarningIcon = false;
  private boolean useInfoIcon = false;
  private boolean useMixcodeIcon = false;
  private JLabel progressStatusLabel = null;
  private JPanel topPanel;
  private JPanel centerPanel;
  private JPanel bottomPanel;
  private JLabel topIcon;
  private JLabel securityIcon;
  private JLabel nameInfo;
  private JLabel publisherInfo;
  private JLabel urlInfo;
  private JButton okBtn;
  private JButton cancelBtn;
  private JCheckBox always;
  private JLabel mixedCodeLabel;
  private JTextComponent masthead = null;
  private static final int ICON_SIZE = 48;
  private int userAnswer = -1;
  private final int DIALOG_WIDTH = 510;
  private final int SMALL_DIALOG_WIDTH = 260;
  private int dialogWidth = 510;
  private final int MAX_LARGE_SCROLL_WIDTH = 600;
  private final Color BG = Color.white;
  private final String SECURITY_ALERT_HIGH = "com/sun/deploy/resources/image/security_high.png";
  private final String SECURITY_ALERT_LOW = "com/sun/deploy/resources/image/security_low.png";
  private static int MAIN_TEXT_WIDTH = 426;
  private final String OK_ACTION = "OK";
  private final int MAX_BUTTONS = 2;
  private int start;
  private int end;
  private Certificate[] certs;
  private String[] alertStrs;
  private String[] infoStrs;
  private int securityInfoCount;
  private String acceptTitle = null;
  private String acceptText = null;
  private Color originalColor;
  private Cursor handCursor = new Cursor(12);
  private Cursor originalCursor = null;
  protected JProgressBar progressBar = null;
  private boolean stayAliveOnOk = false;
  private String contentString = null;
  private String cacheUpgradeContentString = null;
  private String contentLabel = null;
  private String alwaysString = null;
  private String mixedCodeString = null;
  private boolean contentScroll = false;
  private boolean includeMasthead = true;
  private boolean includeAppInfo = true;
  private boolean largeScroll = false;
  private Throwable throwable = null;
  private JPanel detailPanel = null;
  private char[] pwd = new char[0];
  private String userName;
  private String domain;
  private JTextField pwdName;
  private JTextField pwdDomain;
  private JPasswordField password;
  private JList scrollList;
  private boolean showDetails = false;
  TreeMap clientAuthCertsMap;
  static int minFontSize = ResourceManager.getMinFontSize();
  private boolean majorWarning = false;

  DialogTemplate(AppInfo paramAppInfo, Component paramComponent, String paramString1, String paramString2, boolean paramBoolean)
  {
    this(paramAppInfo, paramComponent, paramString1, paramString2, paramBoolean, true);
  }

  DialogTemplate(AppInfo paramAppInfo, Component paramComponent, String paramString1, String paramString2, boolean paramBoolean1, boolean paramBoolean2)
  {
    this.dialogInterface = new DialogHelper(paramComponent, paramAppInfo, paramString1, paramBoolean1);
    this.dialogInterface.setModalOnTop(paramBoolean2);
    this.ainfo = paramAppInfo;
    this.topText = paramString2;
    this.appTitle = paramAppInfo.getDisplayTitle();
    this.appPublisher = paramAppInfo.getDisplayVendor();
    this.appURL = paramAppInfo.getFrom();
  }

  void setSecurityContent(boolean paramBoolean1, boolean paramBoolean2, String paramString1, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2, int paramInt1, boolean paramBoolean3, Certificate[] paramArrayOfCertificate, int paramInt2, int paramInt3, boolean paramBoolean4)
  {
    this.certs = paramArrayOfCertificate;
    this.start = paramInt2;
    this.end = paramInt3;
    this.alertStrs = paramArrayOfString1;
    this.infoStrs = paramArrayOfString2;
    this.securityInfoCount = paramInt1;
    this.majorWarning = paramBoolean4;
    if ((paramArrayOfString1 != null) && (paramArrayOfString1.length > 0))
      this.useWarningIcon = true;
    try
    {
      Container localContainer = this.dialogInterface.getContentPane();
      localContainer.setLayout(new BorderLayout());
      localContainer.add(createTopPanel(false), "North");
      this.dialogInterface.setModalOnTop(true);
      if (paramBoolean1)
        this.alwaysString = ResourceManager.getString("security.dialog.always");
      localContainer.add(createCenterPanel(paramBoolean2, paramString1, paramString2, -1, true), "Center");
      localContainer.add(createBottomPanel(paramBoolean3), "South");
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setNewSecurityContent(boolean paramBoolean1, boolean paramBoolean2, String paramString1, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2, int paramInt1, boolean paramBoolean3, Certificate[] paramArrayOfCertificate, int paramInt2, int paramInt3, boolean paramBoolean4, String paramString3, String paramString4)
  {
    this.certs = paramArrayOfCertificate;
    this.start = paramInt2;
    this.end = paramInt3;
    this.alertStrs = paramArrayOfString1;
    this.infoStrs = paramArrayOfString2;
    this.securityInfoCount = paramInt1;
    this.majorWarning = paramBoolean4;
    this.acceptTitle = paramString3;
    this.acceptText = paramString4;
    try
    {
      this.alwaysString = ResourceManager.getString("security.dialog.always");
      ssvSetFonts();
      JPanel localJPanel1 = createShadedContentPanel();
      this.dialogInterface.setContentPane(localJPanel1);
      localJPanel1.setLayout(new BorderLayout());
      localJPanel1.setOpaque(false);
      this.dialogInterface.setModalOnTop(true);
      JPanel localJPanel2 = createSecurityTopPanel();
      localJPanel1.add(localJPanel2, "North");
      JPanel localJPanel3 = createSecurityRiskPanel(paramArrayOfString1[0]);
      JPanel localJPanel4 = createSecurityAcceptPanel(paramString1, paramString2);
      JPanel localJPanel5 = new JPanel(new BorderLayout());
      localJPanel5.add(localJPanel3, "North");
      localJPanel5.add(localJPanel4, "South");
      localJPanel5.setOpaque(false);
      localJPanel1.add(localJPanel5, "Center");
      this.dialogInterface.setDefaultButton(this.cancelBtn);
      JPanel localJPanel6 = createSecurityBottomPanel();
      localJPanel1.add(localJPanel6, "South");
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setSSVContent(String paramString1, String paramString2, URL paramURL, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7)
  {
    try
    {
      ssvSetFonts();
      JPanel localJPanel1 = createShadedContentPanel();
      this.dialogInterface.setContentPane(localJPanel1);
      localJPanel1.setLayout(new BorderLayout());
      localJPanel1.setOpaque(false);
      this.dialogInterface.setModalOnTop(true);
      localJPanel1.add(createSSVTopPanel(this.topText, this.appTitle, this.ainfo.getDisplayFrom()), "North");
      JPanel localJPanel2 = createSSVRiskPanel(paramString1, paramString2, paramURL);
      SSVChoicePanel localSSVChoicePanel = new SSVChoicePanel(paramString3, paramString4, paramString5);
      JPanel localJPanel3 = new JPanel(new BorderLayout());
      localJPanel3.add(localJPanel2, "North");
      localJPanel3.add(localSSVChoicePanel, "South");
      localJPanel3.setOpaque(false);
      localJPanel1.add(localJPanel3, "Center");
      JPanel localJPanel4 = new JPanel(new FlowLayout(4, 0, 0));
      localJPanel4.setOpaque(false);
      localJPanel4.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
      this.okBtn = new JButton(paramString6);
      this.okBtn.setFont(this.ssvFont);
      this.okBtn.setOpaque(false);
      this.okBtn.addActionListener(new ActionListener(localSSVChoicePanel)
      {
        private final DialogTemplate.SSVChoicePanel val$choicePanel;

        public void actionPerformed(ActionEvent paramActionEvent)
        {
          if (this.val$choicePanel.getSelection() == 0)
            DialogTemplate.this.setUserAnswer(2);
          else
            DialogTemplate.this.setUserAnswer(0);
          DialogTemplate.this.setVisible(false);
        }
      });
      localJPanel4.add(this.okBtn);
      this.cancelBtn = new JButton(paramString7);
      this.cancelBtn.setFont(this.ssvFont);
      this.cancelBtn.setOpaque(false);
      this.cancelBtn.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          DialogTemplate.this.cancelAction();
        }
      });
      this.dialogInterface.setCancelAction(new AbstractAction()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          DialogTemplate.this.cancelAction();
        }
      });
      localJPanel4.add(Box.createHorizontalStrut(10));
      localJPanel4.add(this.cancelBtn);
      this.dialogInterface.setDefaultButton(this.okBtn);
      localJPanel1.add(localJPanel4, "South");
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setCannotDownloadContent(String paramString1, String paramString2, URL paramURL, String paramString3)
  {
    Container localContainer = this.dialogInterface.getContentPane();
    if ((localContainer instanceof JComponent))
      ((JComponent)localContainer).setOpaque(false);
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createLinkPanel(this.topText, paramURL, paramString3), "North");
      this.dialogInterface.getContentPane().add(createCenterPanel(false, paramString1, paramString2, -1, false), "Center");
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setSimpleContent(String paramString1, boolean paramBoolean1, String paramString2, String paramString3, String paramString4, boolean paramBoolean2, boolean paramBoolean3)
  {
    this.contentString = paramString1;
    this.contentScroll = paramBoolean1;
    this.throwable = this.throwable;
    this.detailPanel = this.detailPanel;
    this.includeMasthead = paramBoolean2;
    this.includeAppInfo = paramBoolean2;
    this.largeScroll = (!paramBoolean2);
    this.useWarningIcon = paramBoolean3;
    if (paramString2 != null)
    {
      localObject = new String[] { paramString2 };
      if (paramBoolean3)
        this.alertStrs = ((String)localObject);
      else
        this.infoStrs = ((String)localObject);
    }
    Object localObject = this.dialogInterface.getContentPane();
    if ((localObject instanceof JComponent))
      ((JComponent)localObject).setOpaque(false);
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createTopPanel(false), "North");
      this.dialogInterface.getContentPane().add(createCenterPanel(false, paramString3, paramString4, -1, false), "Center");
      this.dialogInterface.getContentPane().add(createBottomPanel(false), "South");
      boolean bool = paramBoolean1;
      this.dialogInterface.setResizable(bool);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setMixedCodeContent(String paramString1, boolean paramBoolean1, String paramString2, String paramString3, String paramString4, String paramString5, boolean paramBoolean2, boolean paramBoolean3)
  {
    this.contentString = paramString1;
    this.contentScroll = paramBoolean1;
    this.throwable = this.throwable;
    this.detailPanel = this.detailPanel;
    this.includeMasthead = paramBoolean2;
    this.includeAppInfo = paramBoolean2;
    this.largeScroll = (!paramBoolean2);
    this.useMixcodeIcon = true;
    this.alertStrs = new String[1];
    String[] arrayOfString1 = { paramString3 };
    this.alertStrs = arrayOfString1;
    this.infoStrs = new String[3];
    String str1 = ResourceManager.getString("security.dialog.mixcode.info1");
    String str2 = ResourceManager.getString("security.dialog.mixcode.info2");
    String str3 = ResourceManager.getString("security.dialog.mixcode.info3");
    String[] arrayOfString2 = { str1, str2, str3 };
    this.infoStrs = arrayOfString2;
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createTopPanel(false), "North");
      this.mixedCodeString = paramString2;
      this.dialogInterface.getContentPane().add(createCenterPanel(false, paramString4, paramString5, -1, true), "Center");
      this.dialogInterface.getContentPane().add(createBottomPanel(false), "South");
      this.dialogInterface.setModalOnTop(true);
      this.okBtn.requestFocusInWindow();
      boolean bool = paramBoolean1;
      this.dialogInterface.setResizable(bool);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setListContent(String paramString1, JList paramJList, boolean paramBoolean, String paramString2, String paramString3, TreeMap paramTreeMap)
  {
    this.useWarningIcon = true;
    this.includeAppInfo = false;
    this.clientAuthCertsMap = paramTreeMap;
    this.contentLabel = paramString1;
    this.contentScroll = true;
    this.scrollList = paramJList;
    this.showDetails = paramBoolean;
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createTopPanel(false), "North");
      this.dialogInterface.getContentPane().add(createCenterPanel(false, paramString2, paramString3, -1, true), "Center");
      this.dialogInterface.getContentPane().add(createBottomPanel(false), "South");
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setApiContent(String paramString1, String paramString2, String paramString3, boolean paramBoolean, String paramString4, String paramString5)
  {
    this.contentString = paramString1;
    this.contentLabel = paramString2;
    this.contentScroll = (paramString1 != null);
    this.alwaysString = paramString3;
    if ((paramString2 == null) && (paramString1 != null))
    {
      this.infoStrs = new String[1];
      this.infoStrs[0] = paramString1;
      this.contentString = null;
    }
    this.includeMasthead = true;
    this.includeAppInfo = (this.contentString == null);
    this.largeScroll = false;
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createTopPanel(false), "North");
      this.dialogInterface.getContentPane().add(createCenterPanel(false, paramString4, paramString5, -1, false), "Center");
      this.dialogInterface.getContentPane().add(createBottomPanel(false), "South");
      this.dialogInterface.setModalOnTop(true);
      boolean bool = this.contentScroll;
      this.dialogInterface.setResizable(bool);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setErrorContent(String paramString1, String paramString2, String paramString3, Throwable paramThrowable, JPanel paramJPanel, Certificate[] paramArrayOfCertificate, boolean paramBoolean)
  {
    this.contentString = paramString1;
    this.throwable = paramThrowable;
    this.detailPanel = paramJPanel;
    this.certs = paramArrayOfCertificate;
    if (paramBoolean)
      this.includeAppInfo = false;
    this.useErrorIcon = true;
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createTopPanel(false), "North");
      this.dialogInterface.getContentPane().add(createCenterPanel(false, paramString2, paramString3, -1, true), "Center");
      this.dialogInterface.getContentPane().add(createBottomPanel(false), "South");
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setMultiButtonErrorContent(String paramString1, String paramString2, String paramString3, String paramString4)
  {
    this.useErrorIcon = true;
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createTopPanel(false), "North");
      this.dialogInterface.getContentPane().add(createInfoPanel(paramString1), "Center");
      this.dialogInterface.getContentPane().add(createThreeButtonsPanel(paramString2, paramString3, paramString4, false), "South");
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setInfoContent(String paramString1, String paramString2)
  {
    this.useInfoIcon = true;
    this.contentString = paramString1;
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createTopPanel(false), "North");
      this.dialogInterface.getContentPane().add(createCenterPanel(false, paramString2, null, -1, true), "Center");
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setPasswordContent(String paramString1, boolean paramBoolean1, boolean paramBoolean2, String paramString2, String paramString3, boolean paramBoolean3, char[] paramArrayOfChar, String paramString4)
  {
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createPasswordPanel(paramString1, paramBoolean1, paramBoolean2, paramString2, paramString3, paramBoolean3, paramArrayOfChar, paramString4), "Center");
      this.dialogInterface.setModalOnTop(true);
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setUpdateCheckContent(String paramString1, String paramString2, String paramString3, String paramString4)
  {
    try
    {
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createTopPanel(false), "North");
      this.dialogInterface.getContentPane().add(createInfoPanel(paramString1), "Center");
      this.dialogInterface.getContentPane().add(createThreeButtonsPanel(paramString2, paramString3, paramString4, true), "South");
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void setProgressContent(String paramString1, String paramString2, String paramString3, boolean paramBoolean, int paramInt)
  {
    try
    {
      this.cacheUpgradeContentString = paramString3;
      this.dialogInterface.getContentPane().setLayout(new BorderLayout());
      this.dialogInterface.getContentPane().add(createTopPanel(false), "North");
      this.dialogInterface.getContentPane().add(createCenterPanel(false, paramString1, paramString2, paramInt, true), "Center");
      if (this.cacheUpgradeContentString == null)
        this.dialogInterface.getContentPane().add(createBottomPanel(false), "South");
      this.dialogInterface.setResizable(false);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  public void setTitle(String paramString)
  {
    this.dialogInterface.setTitle(paramString);
  }

  private JPanel createInfoPanel(String paramString)
  {
    JPanel localJPanel = new JPanel();
    localJPanel.setLayout(new FlowLayout(3, 0, 0));
    localJPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 12));
    UITextArea localUITextArea = new UITextArea(ResourceManager.getUIFont().getSize(), 478, false);
    localUITextArea.setText(paramString);
    localUITextArea.setSize(localUITextArea.getPreferredSize());
    localJPanel.add(localUITextArea);
    return localJPanel;
  }

  private JPanel createThreeButtonsPanel(String paramString1, String paramString2, String paramString3, boolean paramBoolean)
  {
    JPanel localJPanel = new JPanel();
    localJPanel.setLayout(new FlowLayout(4, 6, 0));
    localJPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    JButton localJButton1 = new JButton(getMessage(paramString1));
    localJButton1.setMnemonic(ResourceManager.getVKCode(paramString1 + ".mnemonic"));
    localJButton1.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        DialogTemplate.this.setUserAnswer(0);
        DialogTemplate.this.setVisible(false);
      }
    });
    localJPanel.add(localJButton1);
    JButton localJButton2 = new JButton(getMessage(paramString2));
    localJButton2.setMnemonic(ResourceManager.getVKCode(paramString2 + ".mnemonic"));
    localJButton2.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        DialogTemplate.this.setUserAnswer(1);
        DialogTemplate.this.setVisible(false);
      }
    });
    localJPanel.add(localJButton2);
    JButton localJButton3 = null;
    if (paramString3 != null)
    {
      localJButton3 = new JButton(getMessage(paramString3));
      localJButton3.setMnemonic(ResourceManager.getVKCode(paramString3 + ".mnemonic"));
      localJButton3.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          DialogTemplate.this.setUserAnswer(3);
          DialogTemplate.this.setVisible(false);
        }
      });
      localJPanel.add(localJButton3);
    }
    if (paramBoolean)
      localJButton3.setToolTipText(ResourceManager.getMessage("autoupdatecheck.masthead"));
    JButton[] arrayOfJButton;
    if (localJButton3 != null)
    {
      arrayOfJButton = new JButton[] { localJButton1, localJButton2, localJButton3 };
      resizeButtons(arrayOfJButton);
    }
    else
    {
      arrayOfJButton = new JButton[] { localJButton1, localJButton2 };
      resizeButtons(arrayOfJButton);
    }
    return localJPanel;
  }

  private JPanel createTopPanel(boolean paramBoolean)
  {
    this.topPanel = new JPanel();
    this.topPanel.setBackground(this.BG);
    GridBagLayout localGridBagLayout = new GridBagLayout();
    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    this.topPanel.setLayout(localGridBagLayout);
    Font localFont = ResourceManager.getUIFont();
    int i = localFont.getSize() + 4;
    this.masthead = new UITextArea(i, MAIN_TEXT_WIDTH, true);
    this.masthead.setText(this.topText);
    Dimension localDimension = this.masthead.getPreferredSize();
    this.masthead.setSize(localDimension.width, localDimension.height);
    this.masthead.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 6));
    if (paramBoolean)
    {
      if (this.includeMasthead)
      {
        localGridBagConstraints.fill = 2;
        localGridBagConstraints.gridwidth = -1;
        localGridBagConstraints.anchor = 17;
        localGridBagLayout.setConstraints(this.masthead, localGridBagConstraints);
        this.topPanel.add(this.masthead);
        localObject = ResourceManager.getIcon("progress.background.image");
        if ((this.masthead instanceof UITextArea))
          ((UITextArea)this.masthead).setBackgroundImage(((ImageIcon)localObject).getImage());
      }
    }
    else if (this.includeMasthead)
    {
      this.topPanel.setBackground(Color.white);
      this.topPanel.setForeground(Color.white);
      this.topPanel.setOpaque(true);
      this.topIcon = new JLabel();
      this.topIcon.setHorizontalAlignment(0);
      this.topIcon.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 12));
      localObject = ResourceManager.getIcon("java48.image");
      if (this.useErrorIcon)
        localObject = ResourceManager.getIcon("error48.image");
      if (this.useInfoIcon)
        localObject = ResourceManager.getIcon("info48.image");
      if (this.useMixcodeIcon)
        localObject = ResourceManager.getIcon("mixcode.image");
      this.topIcon.setIcon((Icon)localObject);
      if (this.useWarningIcon)
      {
        localObject = ResourceManager.getIcon("warning48.image");
        this.topIcon.setIcon((Icon)localObject);
      }
      else if (this.ainfo.getIconRef() != null)
      {
        ImageLoader.getInstance().loadImage(this.ainfo.getIconRef(), this.ainfo.getIconVersion(), this);
      }
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.gridwidth = -1;
      localGridBagConstraints.anchor = 13;
      localGridBagLayout.setConstraints(this.masthead, localGridBagConstraints);
      this.topPanel.add(this.masthead);
      localGridBagConstraints.fill = 0;
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.anchor = 17;
      localGridBagLayout.setConstraints(this.topIcon, localGridBagConstraints);
      this.topPanel.add(this.topIcon);
    }
    Object localObject = new JSeparator();
    ((JSeparator)localObject).setPreferredSize(new Dimension(510, 1));
    localGridBagConstraints.gridy = 1;
    localGridBagConstraints.gridwidth = 0;
    localGridBagLayout.setConstraints((Component)localObject, localGridBagConstraints);
    this.topPanel.add((Component)localObject);
    return (JPanel)this.topPanel;
  }

  private JPanel createLinkPanel(String paramString1, URL paramURL, String paramString2)
  {
    this.dialogWidth = ((this.appTitle != null) || (this.appPublisher != null) ? 510 : 260);
    JPanel localJPanel = new JPanel(new BorderLayout());
    ssvSetFonts();
    LinkText localLinkText = new LinkText(paramString1, paramString2, paramURL);
    localLinkText.setFont(this.ssvFont);
    localLinkText.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
    this.topIcon = new JLabel();
    this.topIcon.setHorizontalAlignment(0);
    this.topIcon.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 6));
    this.topIcon.setIcon(new ImageIcon(ClassLoader.getSystemResource("com/sun/deploy/resources/image/security_high.png")));
    localJPanel.add(this.topIcon, "West");
    localJPanel.add(localLinkText, "Center");
    JSeparator localJSeparator = new JSeparator();
    localJSeparator.setPreferredSize(new Dimension(this.dialogWidth, 0));
    localJPanel.add(localJSeparator, "South");
    return localJPanel;
  }

  private JPanel createCenterPanel(boolean paramBoolean1, String paramString1, String paramString2, int paramInt, boolean paramBoolean2)
  {
    Font localFont1 = ResourceManager.getUIFont();
    int i = getSubpanelFontSize();
    Font localFont2 = localFont1.deriveFont(localFont1.getStyle(), i);
    this.centerPanel = new JPanel();
    this.centerPanel.setLayout(new BoxLayout(this.centerPanel, 1));
    this.centerPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 12, 12));
    GridBagLayout localGridBagLayout = new GridBagLayout();
    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(localGridBagLayout);
    localJPanel1.setBorder(BorderFactory.createEmptyBorder());
    JLabel localJLabel1 = new JLabel(getMessage("dialog.template.name"));
    Font localFont3 = localJLabel1.getFont().deriveFont(1);
    Font localFont4 = localJLabel1.getFont().deriveFont(0);
    localJLabel1.setFont(localFont3);
    JLabel localJLabel2 = new JLabel(getMessage("dialog.template.publisher"));
    localJLabel2.setFont(localFont3);
    JLabel localJLabel3 = new JLabel(getMessage("dialog.template.from"));
    localJLabel3.setFont(localFont3);
    this.nameInfo = new JLabel();
    this.publisherInfo = new JLabel();
    this.urlInfo = new JLabel();
    this.nameInfo.setFont(localFont4);
    this.publisherInfo.setFont(localFont4);
    this.urlInfo.setFont(localFont4);
    this.nameInfo.putClientProperty("html.disable", Boolean.TRUE);
    this.publisherInfo.putClientProperty("html.disable", Boolean.TRUE);
    this.urlInfo.putClientProperty("html.disable", Boolean.TRUE);
    localGridBagConstraints.fill = 2;
    localGridBagConstraints.gridwidth = 1;
    localGridBagConstraints.anchor = 17;
    localGridBagConstraints.insets = new Insets(0, 0, 0, 12);
    localGridBagConstraints.weightx = 0.0D;
    if (this.appTitle != null)
    {
      localGridBagLayout.setConstraints(localJLabel1, localGridBagConstraints);
      localJPanel1.add(localJLabel1);
      localGridBagConstraints.insets = new Insets(0, 0, 0, 12);
      localGridBagConstraints.gridx = -1;
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.weightx = 1.0D;
      localGridBagLayout.setConstraints(this.nameInfo, localGridBagConstraints);
      localJPanel1.add(this.nameInfo);
    }
    if ((paramBoolean2) && (this.appPublisher != null))
    {
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.insets = new Insets(12, 0, 0, 12);
      localGridBagConstraints.gridwidth = 1;
      localGridBagConstraints.weightx = 0.0D;
      localGridBagConstraints.anchor = 17;
      localGridBagLayout.setConstraints(localJLabel2, localGridBagConstraints);
      localJPanel1.add(localJLabel2);
      localGridBagConstraints.insets = new Insets(12, 0, 0, 12);
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.weightx = 1.0D;
      localGridBagLayout.setConstraints(this.publisherInfo, localGridBagConstraints);
      localJPanel1.add(this.publisherInfo);
    }
    if ((this.appTitle != null) && (this.appURL != null))
    {
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.weightx = 0.0D;
      localGridBagConstraints.gridwidth = 1;
      localGridBagConstraints.anchor = 17;
      localGridBagConstraints.insets = new Insets(12, 0, 0, 12);
      localGridBagLayout.setConstraints(localJLabel3, localGridBagConstraints);
      localJPanel1.add(localJLabel3);
      localGridBagConstraints.insets = new Insets(12, 0, 0, 12);
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.weightx = 1.0D;
      localGridBagLayout.setConstraints(this.urlInfo, localGridBagConstraints);
      localJPanel1.add(this.urlInfo);
    }
    setInfo(this.appTitle, this.appPublisher, this.appURL);
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setLayout(new FlowLayout(3, 0, 0));
    JPanel localJPanel3 = new JPanel();
    localJPanel3.setLayout(new BorderLayout());
    JPanel localJPanel4 = new JPanel();
    localJPanel4.setLayout(new BorderLayout());
    Object localObject1;
    if (this.alwaysString != null)
    {
      localObject1 = "security.dialog.always";
      this.always = new JCheckBox(this.alwaysString);
      this.always.setMnemonic(ResourceManager.getVKCode((String)localObject1 + ".mnemonic"));
      this.always.setSelected(paramBoolean1);
      this.always.setFont(localFont4);
      int k = this.always.getBorder().getBorderInsets(this.always).left;
      localJPanel1.setBorder(BorderFactory.createEmptyBorder(0, k, 0, 0));
      localJPanel2.add(this.always);
      localJPanel2.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
    }
    if (this.mixedCodeString != null)
    {
      this.mixedCodeLabel = new JLabel(this.mixedCodeString);
      this.mixedCodeLabel.setFont(localFont4);
      localObject1 = new JPanel();
      ((JPanel)localObject1).setLayout(new BorderLayout());
      localObject2 = null;
      String str = "dialog.template.more.info";
      localObject2 = new FancyButton(ResourceManager.getMessage(str), ResourceManager.getAcceleratorKey(str));
      localObject4 = localFont1.deriveFont(i);
      ((FancyButton)localObject2).setFont((Font)localObject4);
      ((FancyButton)localObject2).addMouseListener(new MouseListener()
      {
        public void mouseClicked(MouseEvent paramMouseEvent)
        {
          if ((paramMouseEvent.getComponent() instanceof FancyButton))
            DialogTemplate.this.showMixedcodeMoreInfo();
        }

        public void mousePressed(MouseEvent paramMouseEvent)
        {
        }

        public void mouseReleased(MouseEvent paramMouseEvent)
        {
        }

        public void mouseEntered(MouseEvent paramMouseEvent)
        {
        }

        public void mouseExited(MouseEvent paramMouseEvent)
        {
        }
      });
      ((FancyButton)localObject2).addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          DialogTemplate.this.showMixedcodeMoreInfo();
        }
      });
      ((JPanel)localObject1).add((Component)localObject2, "West");
      localJPanel3.add(this.mixedCodeLabel, "North");
      localJPanel3.add((Component)localObject1, "South");
    }
    int j = paramInt >= 0 ? 1 : 0;
    if (j != 0)
    {
      this.progressBar = new JProgressBar(0, 100);
      this.progressBar.setVisible(paramInt <= 100);
    }
    if (this.contentString != null)
    {
      if (this.contentLabel != null)
      {
        localObject2 = new JPanel(new BorderLayout());
        ((JPanel)localObject2).add(new JLabel(this.contentLabel), "West");
        localJPanel4.add((Component)localObject2, "North");
      }
      if (this.contentScroll)
      {
        boolean bool = this.largeScroll;
        if (this.largeScroll)
          localObject2 = new JTextArea(this.contentString, 20, 80);
        else
          localObject2 = new JTextArea(this.contentString, 4, 40);
        ((JTextArea)localObject2).setEditable(false);
        localObject4 = new JScrollPane((Component)localObject2, 20, 30, bool)
        {
          private final boolean val$limitWidth;

          public Dimension getPreferredSize()
          {
            Dimension localDimension = super.getPreferredSize();
            if (this.val$limitWidth)
              localDimension.width = Math.min(localDimension.width, 600);
            return localDimension;
          }
        };
        localJPanel4.add((Component)localObject4, "Center");
      }
      else
      {
        localObject2 = new UITextArea(this.contentString);
        ((UITextArea)localObject2).setSize(((UITextArea)localObject2).getPreferredSize());
        localJPanel4.add((Component)localObject2, "Center");
      }
      localJPanel4.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
    }
    if (this.scrollList != null)
    {
      if (this.contentLabel != null)
      {
        localObject2 = new JPanel(new BorderLayout());
        ((JPanel)localObject2).add(new JLabel(this.contentLabel), "West");
        localJPanel4.add((Component)localObject2, "North");
      }
      if (this.contentScroll)
      {
        localObject2 = new JScrollPane(this.scrollList, 20, 30);
        localJPanel4.add((Component)localObject2, "Center");
      }
      if (this.showDetails)
      {
        localObject2 = "security.more.info.details";
        localObject3 = new FancyButton(ResourceManager.getMessage((String)localObject2), ResourceManager.getAcceleratorKey((String)localObject2));
        ((FancyButton)localObject3).setFont(localFont2);
        ((FancyButton)localObject3).addMouseListener(new MouseListener()
        {
          public void mouseClicked(MouseEvent paramMouseEvent)
          {
            if ((paramMouseEvent.getComponent() instanceof FancyButton))
              DialogTemplate.this.showCertificateDetails();
          }

          public void mousePressed(MouseEvent paramMouseEvent)
          {
          }

          public void mouseReleased(MouseEvent paramMouseEvent)
          {
          }

          public void mouseEntered(MouseEvent paramMouseEvent)
          {
          }

          public void mouseExited(MouseEvent paramMouseEvent)
          {
          }
        });
        localObject4 = new JPanel();
        ((JPanel)localObject4).setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        ((JPanel)localObject4).setLayout(new FlowLayout(4, 0, 0));
        ((JPanel)localObject4).add((Component)localObject3);
        ((JPanel)localObject4).add(Box.createGlue());
        localJPanel4.add((Component)localObject4, "South");
      }
    }
    Object localObject2 = new JPanel();
    ((JPanel)localObject2).setLayout(new FlowLayout(4, 0, 0));
    this.okBtn = new JButton(paramString1 == null ? "" : getMessage(paramString1));
    this.okBtn.addActionListener(this);
    this.okBtn.setActionCommand("OK");
    ((JPanel)localObject2).add(this.okBtn);
    this.okBtn.setVisible(paramString1 != null);
    this.cancelBtn = new JButton(paramString2 == null ? "" : getMessage(paramString2));
    this.cancelBtn.addActionListener(this);
    ((JPanel)localObject2).add(Box.createHorizontalStrut(6));
    ((JPanel)localObject2).add(this.cancelBtn);
    this.cancelBtn.setVisible(paramString2 != null);
    if (this.okBtn.isVisible())
    {
      this.dialogInterface.setDefaultButton(this.okBtn);
      this.dialogInterface.setInitialFocusComponent(this.okBtn);
    }
    else if (this.cancelBtn.isVisible())
    {
      this.dialogInterface.setDefaultButton(this.cancelBtn);
      this.dialogInterface.setInitialFocusComponent(this.cancelBtn);
    }
    Object localObject3 = { this.okBtn, this.cancelBtn };
    resizeButtons(localObject3);
    if (this.cacheUpgradeContentString != null)
    {
      localObject4 = new UITextArea(this.cacheUpgradeContentString);
      ((UITextArea)localObject4).setBorder(null);
      localJPanel4.add((Component)localObject4, "North");
      this.centerPanel.add(localJPanel4);
      this.centerPanel.add(Box.createVerticalStrut(12));
    }
    else
    {
      if (this.includeAppInfo)
      {
        this.centerPanel.add(localJPanel1);
        this.centerPanel.add(Box.createVerticalStrut(12));
      }
      if (this.alwaysString != null)
        this.centerPanel.add(localJPanel2);
      if (this.mixedCodeString != null)
        this.centerPanel.add(localJPanel3);
      if ((this.contentString != null) || (this.scrollList != null))
        this.centerPanel.add(localJPanel4);
    }
    Object localObject4 = new JPanel(new BorderLayout());
    this.centerPanel.add((Component)localObject4);
    JPanel localJPanel5 = new JPanel(new BorderLayout());
    if (j != 0)
    {
      this.progressStatusLabel = new JLabel(" ");
      this.progressStatusLabel.setFont(localFont2);
      ((JPanel)localObject4).add(this.progressStatusLabel, "West");
      this.progressBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
      localJPanel5.add(this.progressBar, "Center");
    }
    localJPanel5.add((Component)localObject2, "East");
    this.centerPanel.add(localJPanel5);
    this.dialogInterface.setCancelAction(new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        DialogTemplate.this.cancelAction();
      }
    });
    return (JPanel)(JPanel)(JPanel)(JPanel)this.centerPanel;
  }

  private JPanel createBottomPanel(boolean paramBoolean)
  {
    this.bottomPanel = new JPanel();
    GridBagLayout localGridBagLayout = new GridBagLayout();
    this.bottomPanel.setLayout(localGridBagLayout);
    if ((this.alertStrs != null) || (this.infoStrs != null))
    {
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.gridwidth = 0;
      JSeparator localJSeparator = new JSeparator();
      localJSeparator.setPreferredSize(new Dimension(510, 1));
      localGridBagLayout.setConstraints(localJSeparator, localGridBagConstraints);
      this.bottomPanel.add(localJSeparator);
      this.securityIcon = new JLabel();
      String str1 = "com/sun/deploy/resources/image/security_high.png";
      if ((this.alertStrs == null) || (this.alertStrs.length == 0))
      {
        str1 = "com/sun/deploy/resources/image/security_low.png";
        if (this.always != null)
          this.always.setSelected(true);
      }
      else if (this.mixedCodeString == null)
      {
        this.dialogInterface.setDefaultButton(this.cancelBtn);
      }
      this.securityIcon.setIcon(new ImageIcon(ClassLoader.getSystemResource(str1)));
      localGridBagConstraints.insets = new Insets(12, 12, 12, 20);
      localGridBagConstraints.gridy = 1;
      localGridBagConstraints.gridwidth = 1;
      localGridBagLayout.setConstraints(this.securityIcon, localGridBagConstraints);
      this.bottomPanel.add(this.securityIcon);
      Font localFont1 = ResourceManager.getUIFont();
      int i = getSubpanelFontSize();
      int j = 0;
      FancyButton localFancyButton = null;
      if (paramBoolean)
      {
        String str2 = "dialog.template.more.info";
        localFancyButton = new FancyButton(ResourceManager.getMessage(str2), ResourceManager.getAcceleratorKey(str2));
        localObject = localFont1.deriveFont(i);
        localFancyButton.setFont((Font)localObject);
        localFancyButton.addMouseListener(new MouseListener()
        {
          public void mouseClicked(MouseEvent paramMouseEvent)
          {
            if ((paramMouseEvent.getComponent() instanceof FancyButton))
              DialogTemplate.this.showMoreInfo();
          }

          public void mousePressed(MouseEvent paramMouseEvent)
          {
          }

          public void mouseReleased(MouseEvent paramMouseEvent)
          {
          }

          public void mouseEntered(MouseEvent paramMouseEvent)
          {
          }

          public void mouseExited(MouseEvent paramMouseEvent)
          {
          }
        });
        localFancyButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent paramActionEvent)
          {
            DialogTemplate.this.showMoreInfo();
          }
        });
        j = localFancyButton.getPreferredSize().width + localFancyButton.getInsets().left + localFancyButton.getInsets().right;
      }
      int k = 486 - this.securityIcon.getPreferredSize().width - 20 - 6 - j - 8;
      Object localObject = new UITextArea(i, k, false);
      Font localFont2 = ((UITextArea)localObject).getFont().deriveFont(0);
      ((UITextArea)localObject).setFont(localFont2);
      if (((this.alertStrs == null) || (this.alertStrs.length == 0)) && (this.infoStrs != null) && (this.infoStrs.length != 0))
      {
        ((UITextArea)localObject).setText(this.infoStrs[0] != null ? this.infoStrs[0] : " ");
        ((UITextArea)localObject).setSize(((UITextArea)localObject).getPreferredSize());
      }
      else if ((this.alertStrs != null) && (this.alertStrs.length != 0))
      {
        ((UITextArea)localObject).setText(this.alertStrs[0] != null ? this.alertStrs[0] : " ");
        ((UITextArea)localObject).setSize(((UITextArea)localObject).getPreferredSize());
      }
      localGridBagConstraints.gridwidth = -1;
      localGridBagConstraints.insets = new Insets(12, 0, 12, 6);
      localGridBagConstraints.weightx = 0.0D;
      localGridBagLayout.setConstraints((Component)localObject, localGridBagConstraints);
      this.bottomPanel.add((Component)localObject);
      localGridBagConstraints.insets = new Insets(12, 0, 12, 12);
      localGridBagConstraints.gridwidth = 0;
      if (localFancyButton != null)
      {
        localGridBagConstraints.gridx = 22;
        localGridBagLayout.setConstraints(localFancyButton, localGridBagConstraints);
        this.bottomPanel.add(localFancyButton);
      }
    }
    else
    {
      this.bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
    }
    return (JPanel)this.bottomPanel;
  }

  private void ssvSetFonts()
  {
    if (this.ssvFont == null)
    {
      JLabel localJLabel = new JLabel();
      int i = ResourceManager.getUIFont().getSize();
      int j = i;
      int k = i + 2;
      int m = i + 4;
      this.ssvSmallFont = localJLabel.getFont().deriveFont(0, j);
      this.ssvSmallBoldFont = localJLabel.getFont().deriveFont(1, j);
      this.ssvFont = localJLabel.getFont().deriveFont(0, k);
      this.ssvBigFont = localJLabel.getFont().deriveFont(0, m);
      this.ssvBigBoldFont = localJLabel.getFont().deriveFont(1, m);
    }
  }

  private JPanel createShadedContentPanel()
  {
    14 local14 = new JPanel()
    {
      protected void paintComponent(Graphics paramGraphics)
      {
        Color localColor1 = new Color(240, 240, 240);
        Color localColor2 = new Color(200, 200, 200);
        Graphics2D localGraphics2D = (Graphics2D)paramGraphics;
        localGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint localGradientPaint = new GradientPaint(0.0F, 0.0F, localColor1, 0.0F, 4 * getHeight() / 7, localColor2, false);
        localGraphics2D.setPaint(localGradientPaint);
        Insets localInsets = getInsets();
        localGraphics2D.fillRect(localInsets.left, localInsets.top, getWidth() - localInsets.left - localInsets.right, getHeight() - localInsets.top - localInsets.bottom);
        super.paintComponent(paramGraphics);
      }
    };
    return local14;
  }

  private JPanel createSecurityTopPanel()
  {
    JPanel localJPanel1 = new JPanel(new BorderLayout());
    localJPanel1.setOpaque(false);
    localJPanel1.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
    JLabel localJLabel1 = new JLabel(this.topText);
    localJLabel1.setOpaque(false);
    localJLabel1.setFont(this.ssvBigBoldFont);
    localJPanel1.add(localJLabel1, "North");
    GridBagLayout localGridBagLayout = new GridBagLayout();
    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setOpaque(false);
    localJPanel2.setLayout(localGridBagLayout);
    localJPanel2.setBorder(BorderFactory.createEmptyBorder());
    JLabel localJLabel2 = new JLabel(getMessage("dialog.template.name"));
    localJLabel2.setFont(this.ssvSmallBoldFont);
    localJLabel2.setOpaque(false);
    JLabel localJLabel3 = new JLabel(getMessage("dialog.template.publisher"));
    localJLabel3.setFont(this.ssvSmallBoldFont);
    localJLabel3.setOpaque(false);
    JLabel localJLabel4 = new JLabel(getMessage("dialog.template.from"));
    localJLabel4.setFont(this.ssvSmallBoldFont);
    localJLabel4.setOpaque(false);
    this.nameInfo = new JLabel();
    this.publisherInfo = new JLabel();
    this.urlInfo = new JLabel();
    this.nameInfo.setFont(this.ssvBigBoldFont);
    this.publisherInfo.setFont(this.ssvSmallFont);
    this.urlInfo.setFont(this.ssvSmallFont);
    this.nameInfo.putClientProperty("html.disable", Boolean.TRUE);
    this.publisherInfo.putClientProperty("html.disable", Boolean.TRUE);
    this.urlInfo.putClientProperty("html.disable", Boolean.TRUE);
    localGridBagConstraints.fill = 2;
    localGridBagConstraints.gridwidth = 1;
    localGridBagConstraints.anchor = 17;
    localGridBagConstraints.insets = new Insets(0, 32, 0, 12);
    localGridBagConstraints.weightx = 0.0D;
    if (this.appTitle != null)
    {
      localGridBagLayout.setConstraints(localJLabel2, localGridBagConstraints);
      localJPanel2.add(localJLabel2);
      localGridBagConstraints.insets = new Insets(0, 0, 0, 12);
      localGridBagConstraints.gridx = -1;
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.weightx = 1.0D;
      localGridBagLayout.setConstraints(this.nameInfo, localGridBagConstraints);
      localJPanel2.add(this.nameInfo);
    }
    if (this.appPublisher != null)
    {
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.insets = new Insets(12, 32, 0, 12);
      localGridBagConstraints.gridwidth = 1;
      localGridBagConstraints.weightx = 0.0D;
      localGridBagConstraints.anchor = 17;
      localGridBagLayout.setConstraints(localJLabel3, localGridBagConstraints);
      localJPanel2.add(localJLabel3);
      localGridBagConstraints.insets = new Insets(12, 0, 0, 12);
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.weightx = 1.0D;
      localGridBagLayout.setConstraints(this.publisherInfo, localGridBagConstraints);
      localJPanel2.add(this.publisherInfo);
    }
    if ((this.appTitle != null) && (this.appURL != null))
    {
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.weightx = 0.0D;
      localGridBagConstraints.gridwidth = 1;
      localGridBagConstraints.anchor = 17;
      localGridBagConstraints.insets = new Insets(12, 32, 0, 12);
      localGridBagLayout.setConstraints(localJLabel4, localGridBagConstraints);
      localJPanel2.add(localJLabel4);
      localGridBagConstraints.insets = new Insets(12, 0, 0, 12);
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.weightx = 1.0D;
      localGridBagLayout.setConstraints(this.urlInfo, localGridBagConstraints);
      localJPanel2.add(this.urlInfo);
    }
    setInfo(this.appTitle, this.appPublisher, this.appURL);
    JLabel localJLabel5 = new JLabel();
    localJLabel5.setOpaque(false);
    localJLabel5.setIcon(ResourceManager.getIcon("warning48s.image"));
    localJLabel5.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 0));
    JPanel localJPanel3 = new JPanel(new BorderLayout());
    localJPanel3.setOpaque(false);
    localJPanel3.add(localJLabel5, "West");
    localJPanel3.add(localJPanel2, "Center");
    localJPanel3.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
    localJPanel1.add(localJPanel3, "South");
    return localJPanel1;
  }

  private JPanel createSSVTopPanel(String paramString1, String paramString2, String paramString3)
  {
    JPanel localJPanel = new JPanel(new BorderLayout());
    localJPanel.setOpaque(false);
    localJPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
    JLabel localJLabel1 = new JLabel(paramString1);
    localJLabel1.setOpaque(false);
    localJLabel1.setFont(this.ssvBigBoldFont);
    localJLabel1.setEnabled(true);
    localJPanel.add(localJLabel1, "North");
    JLabel localJLabel2 = new JLabel(getMessage("dialog.template.name"));
    localJLabel2.setFont(this.ssvSmallBoldFont);
    localJLabel2.setOpaque(false);
    localJLabel2.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
    JLabel localJLabel3 = new JLabel(getMessage("dialog.template.from"));
    localJLabel3.setFont(this.ssvSmallBoldFont);
    localJLabel3.setOpaque(false);
    localJLabel3.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
    this.nameInfo = new JLabel(paramString2);
    this.nameInfo.setFont(this.ssvBigBoldFont);
    this.nameInfo.setOpaque(false);
    JLabel localJLabel4 = new JLabel(paramString3);
    localJLabel4.setFont(this.ssvSmallFont);
    localJLabel4.setOpaque(false);
    JPanel[] arrayOfJPanel = new JPanel[4];
    for (int i = 0; i < 4; i++)
    {
      arrayOfJPanel[i] = new JPanel(new BorderLayout());
      arrayOfJPanel[i].setOpaque(false);
    }
    JLabel localJLabel5 = new JLabel();
    localJLabel5.setOpaque(false);
    localJLabel5.setIcon(ResourceManager.getIcon("warning48.image"));
    localJLabel5.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 0));
    arrayOfJPanel[2].add(localJLabel2, "North");
    arrayOfJPanel[2].add(localJLabel3, "South");
    arrayOfJPanel[2].setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    arrayOfJPanel[3].add(this.nameInfo, "North");
    arrayOfJPanel[3].add(localJLabel4, "South");
    arrayOfJPanel[1].add(arrayOfJPanel[2], "West");
    arrayOfJPanel[1].add(arrayOfJPanel[3], "Center");
    arrayOfJPanel[1].setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
    arrayOfJPanel[0].add(localJLabel5, "West");
    arrayOfJPanel[0].add(arrayOfJPanel[1], "East");
    arrayOfJPanel[0].setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    localJPanel.add(arrayOfJPanel[0], "South");
    return localJPanel;
  }

  private JPanel createSecurityRiskPanel(String paramString)
  {
    JPanel localJPanel1 = new JPanel(new BorderLayout());
    localJPanel1.setOpaque(false);
    localJPanel1.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    int i = paramString.indexOf(" ");
    if (i < paramString.length() - 2)
    {
      String str1 = paramString.substring(0, i);
      String str2 = paramString.substring(i + 1);
      JPanel localJPanel2 = new JPanel(new BorderLayout());
      localJPanel2.setOpaque(false);
      JLabel localJLabel = new JLabel(str1);
      localJLabel.setFont(this.ssvSmallBoldFont);
      localJLabel.setOpaque(false);
      localJPanel2.add(localJLabel, "North");
      localJPanel2.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
      JPanel localJPanel3 = new JPanel(new BorderLayout());
      localJPanel3.setOpaque(false);
      JSmartTextArea localJSmartTextArea = new JSmartTextArea(str2);
      localJSmartTextArea.setFont(this.ssvSmallFont);
      localJSmartTextArea.setLineWrap(true);
      localJSmartTextArea.setWrapStyleWord(true);
      localJSmartTextArea.setOpaque(false);
      localJSmartTextArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      localJPanel3.add(localJSmartTextArea, "North");
      String str3 = "dialog.template.more.info2";
      FancyButton localFancyButton = new FancyButton(ResourceManager.getMessage(str3), ResourceManager.getAcceleratorKey(str3));
      localFancyButton.setOpaque(false);
      localFancyButton.setFont(this.ssvSmallFont);
      localFancyButton.addMouseListener(new MouseListener()
      {
        public void mouseClicked(MouseEvent paramMouseEvent)
        {
          if ((paramMouseEvent.getComponent() instanceof FancyButton))
            DialogTemplate.this.showMoreInfo();
        }

        public void mousePressed(MouseEvent paramMouseEvent)
        {
        }

        public void mouseReleased(MouseEvent paramMouseEvent)
        {
        }

        public void mouseEntered(MouseEvent paramMouseEvent)
        {
        }

        public void mouseExited(MouseEvent paramMouseEvent)
        {
        }
      });
      localFancyButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          DialogTemplate.this.showMoreInfo();
        }
      });
      JPanel localJPanel4 = new JPanel(new BorderLayout());
      localJPanel4.setOpaque(false);
      localJPanel4.add(localFancyButton, "West");
      localJPanel3.add(localJPanel4, "Center");
      localJPanel1.add(localJPanel2, "West");
      localJPanel1.add(localJPanel3, "Center");
    }
    return localJPanel1;
  }

  private JPanel createSSVRiskPanel(String paramString1, String paramString2, URL paramURL)
  {
    JPanel localJPanel1 = new JPanel(new BorderLayout());
    localJPanel1.setOpaque(false);
    localJPanel1.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
    int i = paramString1.indexOf(" ");
    if (i < paramString1.length() - 2)
    {
      String str1 = paramString1.substring(0, i);
      String str2 = paramString1.substring(i + 1);
      JPanel localJPanel2 = new JPanel(new BorderLayout());
      localJPanel2.setOpaque(false);
      JLabel localJLabel = new JLabel(str1);
      localJLabel.setFont(this.ssvSmallBoldFont);
      localJLabel.setOpaque(false);
      localJPanel2.add(localJLabel, "North");
      localJPanel2.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
      JPanel localJPanel3 = new JPanel(new BorderLayout());
      localJPanel3.setOpaque(false);
      LinkText localLinkText = new LinkText(str2, paramString2, paramURL);
      localLinkText.setFont(this.ssvSmallFont);
      localLinkText.setOpaque(false);
      localJPanel3.add(localLinkText, "Center");
      localJPanel1.add(localJPanel2, "West");
      localJPanel1.add(localJPanel3, "Center");
    }
    return localJPanel1;
  }

  private JPanel createSecurityAcceptPanel(String paramString1, String paramString2)
  {
    JPanel localJPanel1 = new JPanel(new BorderLayout());
    localJPanel1.setOpaque(false);
    localJPanel1.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
    Font localFont = ResourceManager.getUIFont().deriveFont(1);
    JPanel localJPanel2 = new JPanel(new BorderLayout());
    JPanel localJPanel3 = new JPanel(new BorderLayout());
    localJPanel2.setOpaque(false);
    localJPanel3.setOpaque(false);
    JLabel localJLabel = new JLabel(this.acceptTitle);
    localJLabel.setOpaque(false);
    localJLabel.setFont(this.ssvSmallBoldFont);
    localJPanel2.add(localJLabel, "Center");
    JCheckBox localJCheckBox = new JCheckBox(this.acceptText);
    localJCheckBox.setFont(this.ssvSmallFont);
    localJCheckBox.setOpaque(false);
    localJCheckBox.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent paramItemEvent)
      {
        if (paramItemEvent.getStateChange() == 2)
        {
          DialogTemplate.this.okBtn.setEnabled(false);
          DialogTemplate.this.dialogInterface.setDefaultButton(DialogTemplate.this.cancelBtn);
          if (DialogTemplate.this.always != null)
          {
            DialogTemplate.this.always.setSelected(false);
            DialogTemplate.this.always.setEnabled(false);
          }
        }
        if (paramItemEvent.getStateChange() == 1)
        {
          DialogTemplate.this.okBtn.setEnabled(true);
          DialogTemplate.this.dialogInterface.setDefaultButton(DialogTemplate.this.okBtn);
          if (DialogTemplate.this.always != null)
            DialogTemplate.this.always.setEnabled(true);
        }
      }
    });
    String str = "security.dialog.accept.text";
    localJCheckBox.setMnemonic(ResourceManager.getVKCode(str + ".mnemonic"));
    localJCheckBox.setSelected(false);
    JPanel localJPanel4 = new JPanel();
    localJPanel4.setLayout(new FlowLayout(4, 0, 0));
    localJPanel4.setOpaque(false);
    localJPanel4.setBorder(BorderFactory.createEmptyBorder(8, 32, 8, 0));
    this.okBtn = new JButton(paramString1);
    this.okBtn.addActionListener(this);
    this.okBtn.setActionCommand("OK");
    this.okBtn.setOpaque(false);
    this.okBtn.setEnabled(false);
    localJPanel4.add(this.okBtn);
    this.cancelBtn = new JButton(paramString2);
    this.cancelBtn.addActionListener(this);
    this.cancelBtn.setOpaque(false);
    localJPanel4.add(Box.createHorizontalStrut(10));
    localJPanel4.add(this.cancelBtn);
    this.dialogInterface.setDefaultButton(this.cancelBtn);
    this.dialogInterface.setInitialFocusComponent(localJCheckBox);
    JButton[] arrayOfJButton = { this.okBtn, this.cancelBtn };
    resizeButtons(arrayOfJButton);
    localJPanel3.add(localJCheckBox, "West");
    localJPanel3.add(localJPanel4, "East");
    localJPanel3.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
    localJPanel1.add(localJPanel2, "North");
    localJPanel1.add(localJPanel3, "South");
    this.dialogInterface.setCancelAction(new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        DialogTemplate.this.cancelAction();
      }
    });
    return localJPanel1;
  }

  private JPanel createSecurityBottomPanel()
  {
    JPanel localJPanel1 = new JPanel(new BorderLayout());
    localJPanel1.setOpaque(false);
    localJPanel1.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 16));
    JPanel localJPanel2 = new JPanel(new BorderLayout());
    JPanel localJPanel3 = new JPanel(new BorderLayout());
    localJPanel2.setOpaque(false);
    localJPanel3.setOpaque(false);
    String str1 = getMessage("security.dialog.show.options");
    String str2 = getMessage("security.dialog.hide.options");
    ImageIcon localImageIcon1 = ResourceManager.getIcon("toggle_up2.image");
    ImageIcon localImageIcon2 = ResourceManager.getIcon("toggle_down2.image");
    JButton localJButton = new JButton(str1, localImageIcon2);
    localJButton.setOpaque(false);
    localJButton.setEnabled(true);
    localJButton.setContentAreaFilled(false);
    localJButton.setBorderPainted(false);
    String str3 = "security.dialog.showbtn";
    localJButton.setMnemonic(ResourceManager.getVKCode(str3 + ".mnemonic"));
    localJPanel2.add(localJButton, "West");
    String str4 = "security.dialog.always";
    this.always = new JCheckBox(this.alwaysString);
    this.always.setMnemonic(ResourceManager.getVKCode(str4 + ".mnemonic"));
    this.always.setFont(this.ssvSmallFont);
    this.always.setOpaque(false);
    this.always.setSelected(false);
    this.always.setEnabled(false);
    this.always.setVisible(false);
    localJPanel3.add(this.always, "West");
    localJPanel3.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 16));
    localJButton.addActionListener(new ActionListener(str1, localJButton, localImageIcon1, str2, localImageIcon2)
    {
      private final String val$showStr;
      private final JButton val$showBtn;
      private final ImageIcon val$iconUp;
      private final String val$hideStr;
      private final ImageIcon val$iconDown;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        String str = paramActionEvent.getActionCommand();
        if (str.equals(this.val$showStr))
        {
          this.val$showBtn.setIcon(this.val$iconUp);
          this.val$showBtn.setText(this.val$hideStr);
          DialogTemplate.this.always.setVisible(true);
        }
        else
        {
          this.val$showBtn.setIcon(this.val$iconDown);
          this.val$showBtn.setText(this.val$showStr);
          DialogTemplate.this.always.setVisible(false);
        }
        DialogTemplate.this.dialogInterface.pack();
        DialogTemplate.this.dialogInterface.setResizable(false);
      }
    });
    localJPanel1.add(localJPanel2, "North");
    localJPanel1.add(localJPanel3, "South");
    return localJPanel1;
  }

  private JPanel createPasswordPanel(String paramString1, boolean paramBoolean1, boolean paramBoolean2, String paramString2, String paramString3, boolean paramBoolean3, char[] paramArrayOfChar, String paramString4)
  {
    JLabel localJLabel1 = new JLabel();
    JLabel localJLabel2 = new JLabel();
    Font localFont1 = localJLabel1.getFont().deriveFont(0);
    Font localFont2 = localJLabel1.getFont().deriveFont(1);
    JLabel localJLabel3 = new JLabel();
    localJLabel3.setIcon(ResourceManager.getIcon("pwd-masthead.png"));
    if (paramBoolean1)
    {
      str = "password.dialog.username";
      localJLabel1.setText(getMessage(str));
      localJLabel1.setDisplayedMnemonic(ResourceManager.getAcceleratorKey(str));
      this.pwdName = new JTextField();
      this.pwdName.setText(paramString2);
      localJLabel1.setLabelFor(this.pwdName);
      localJLabel1.setFont(localFont2);
    }
    String str = "password.dialog.password";
    JLabel localJLabel4 = new JLabel(getMessage(str));
    this.password = new JPasswordField();
    this.password.setText(String.valueOf(paramArrayOfChar));
    localJLabel4.setLabelFor(this.password);
    localJLabel4.setDisplayedMnemonic(ResourceManager.getAcceleratorKey(str));
    localJLabel4.setFont(localFont2);
    if (paramBoolean2)
    {
      localObject1 = "password.dialog.domain";
      localJLabel2.setText(getMessage((String)localObject1));
      this.pwdDomain = new JTextField();
      this.pwdDomain.setText(paramString3);
      localJLabel2.setLabelFor(this.pwdDomain);
      localJLabel2.setDisplayedMnemonic(ResourceManager.getAcceleratorKey((String)localObject1));
      localJLabel2.setFont(localFont2);
    }
    Object localObject1 = new GridBagLayout();
    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout((LayoutManager)localObject1);
    localGridBagConstraints.fill = 1;
    localGridBagConstraints.gridwidth = 0;
    ((GridBagLayout)localObject1).setConstraints(localJLabel3, localGridBagConstraints);
    localJPanel1.add(localJLabel3);
    JSeparator localJSeparator = new JSeparator();
    ((GridBagLayout)localObject1).setConstraints(localJSeparator, localGridBagConstraints);
    localJPanel1.add(localJSeparator);
    UITextArea localUITextArea = new UITextArea(localJLabel1.getFont().getSize(), localJLabel3.getPreferredSize().width - 24, false);
    localUITextArea.setFont(localFont1);
    localUITextArea.setText(paramString1);
    Dimension localDimension = localUITextArea.getPreferredSize();
    localUITextArea.setSize(localDimension.width, localDimension.height);
    localGridBagConstraints.gridwidth = 0;
    localGridBagConstraints.insets = new Insets(12, 12, 0, 12);
    ((GridBagLayout)localObject1).setConstraints(localUITextArea, localGridBagConstraints);
    localJPanel1.add(localUITextArea);
    Insets localInsets1 = new Insets(12, 12, 0, 6);
    Insets localInsets2 = new Insets(12, 0, 0, 12);
    localGridBagConstraints.gridwidth = 1;
    localGridBagConstraints.insets = localInsets1;
    if (paramBoolean1)
    {
      ((GridBagLayout)localObject1).setConstraints(localJLabel1, localGridBagConstraints);
      localJPanel1.add(localJLabel1);
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.insets = localInsets2;
      ((GridBagLayout)localObject1).setConstraints(this.pwdName, localGridBagConstraints);
      localJPanel1.add(this.pwdName);
    }
    localGridBagConstraints.gridwidth = 1;
    localGridBagConstraints.insets = localInsets1;
    ((GridBagLayout)localObject1).setConstraints(localJLabel4, localGridBagConstraints);
    localJPanel1.add(localJLabel4);
    localGridBagConstraints.gridwidth = 0;
    localGridBagConstraints.insets = localInsets2;
    ((GridBagLayout)localObject1).setConstraints(this.password, localGridBagConstraints);
    localJPanel1.add(this.password);
    if (paramBoolean2)
    {
      localGridBagConstraints.gridwidth = 1;
      localGridBagConstraints.insets = localInsets1;
      ((GridBagLayout)localObject1).setConstraints(localJLabel2, localGridBagConstraints);
      localJPanel1.add(localJLabel2);
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.insets = localInsets2;
      ((GridBagLayout)localObject1).setConstraints(this.pwdDomain, localGridBagConstraints);
      localJPanel1.add(this.pwdDomain);
    }
    if ((Config.getBooleanProperty("deployment.security.password.cache")) && (paramBoolean3 == true))
    {
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.insets = new Insets(12, 8, 0, 12);
      localObject2 = "password.dialog.save";
      this.always = new JCheckBox(getMessage((String)localObject2));
      if (paramArrayOfChar.length > 0)
        this.always.setSelected(true);
      else
        this.always.setSelected(false);
      ((GridBagLayout)localObject1).setConstraints(this.always, localGridBagConstraints);
      localJPanel1.add(this.always);
    }
    Object localObject2 = new JPanel();
    ((JPanel)localObject2).setLayout(new BoxLayout((Container)localObject2, 2));
    this.okBtn = new JButton(getMessage("common.ok_btn"));
    this.okBtn.setActionCommand("OK");
    this.okBtn.addActionListener(this);
    this.dialogInterface.setDefaultButton(this.okBtn);
    this.cancelBtn = new JButton(getMessage("common.cancel_btn"));
    this.cancelBtn.addActionListener(this);
    this.dialogInterface.setCancelAction(new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        DialogTemplate.this.cancelAction();
      }
    });
    JButton[] arrayOfJButton = { this.okBtn, this.cancelBtn };
    resizeButtons(arrayOfJButton);
    ((JPanel)localObject2).add(Box.createHorizontalGlue());
    ((JPanel)localObject2).add(this.okBtn);
    ((JPanel)localObject2).add(Box.createHorizontalStrut(6));
    ((JPanel)localObject2).add(this.cancelBtn);
    ((JPanel)localObject2).add(Box.createHorizontalStrut(12));
    JPanel localJPanel2 = null;
    if (paramString4 != null)
    {
      localJPanel2 = new JPanel();
      localJPanel2.setLayout(new FlowLayout(3, 0, 0));
      localJPanel2.add(Box.createHorizontalStrut(12));
      localObject3 = new MessageFormat(getMessage("password.dialog.scheme"));
      Object[] arrayOfObject = { paramString4 };
      JLabel localJLabel5 = new JLabel(((MessageFormat)localObject3).format(arrayOfObject));
      localJLabel5.setFont(localFont1);
      localJPanel2.add(localJLabel5);
    }
    Object localObject3 = new JPanel();
    ((JPanel)localObject3).setLayout(new BoxLayout((Container)localObject3, 3));
    ((JPanel)localObject3).add(localJPanel1);
    ((JPanel)localObject3).add(Box.createVerticalStrut(24));
    ((JPanel)localObject3).add((Component)localObject2);
    ((JPanel)localObject3).add(Box.createVerticalStrut(12));
    if (localJPanel2 != null)
    {
      ((JPanel)localObject3).add(new JSeparator());
      ((JPanel)localObject3).add(Box.createVerticalStrut(12));
      ((JPanel)localObject3).add(localJPanel2);
      ((JPanel)localObject3).add(Box.createVerticalStrut(12));
    }
    return (JPanel)(JPanel)(JPanel)localObject3;
  }

  void showMoreInfo()
  {
    MoreInfoDialog localMoreInfoDialog;
    if ((this.throwable == null) && (this.detailPanel == null))
      localMoreInfoDialog = new MoreInfoDialog(this.dialogInterface.getDialog(), this.alertStrs, this.infoStrs, this.securityInfoCount, this.certs, this.start, this.end, this.majorWarning);
    else
      localMoreInfoDialog = new MoreInfoDialog(this.dialogInterface.getDialog(), this.detailPanel, this.throwable, this.certs, false);
    localMoreInfoDialog.setVisible(true);
  }

  void showMixedcodeMoreInfo()
  {
    MoreInfoDialog localMoreInfoDialog = new MoreInfoDialog(this.dialogInterface.getDialog(), null, this.infoStrs, 0, null, 0, 0, false);
    localMoreInfoDialog.setVisible(true);
  }

  void showCertificateDetails()
  {
    int i = this.scrollList.getSelectedIndex();
    X509Certificate[] arrayOfX509Certificate = null;
    Iterator localIterator = this.clientAuthCertsMap.values().iterator();
    while ((i >= 0) && (localIterator.hasNext()))
    {
      arrayOfX509Certificate = (X509Certificate[])(X509Certificate[])localIterator.next();
      i--;
    }
    if (arrayOfX509Certificate != null)
      CertificateDialog.showCertificates(this.dialogInterface.getDialog(), arrayOfX509Certificate, 0, arrayOfX509Certificate.length);
  }

  public void setVisible(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      DialogListener localDialogListener = UIFactory.getDialogListener();
      DialogInterface localDialogInterface = this.dialogInterface;
      Frame localFrame = this.dummyFrame;
      boolean bool = URLClassPathControl.isDisabledInCurrentThread();
      21 local21 = new Runnable(localDialogListener, localFrame, bool, localDialogInterface)
      {
        private final DialogListener val$dl;
        private final Frame val$dummy;
        private final boolean val$disabled;
        private final DialogInterface val$dlg;

        public void run()
        {
          if (this.val$dl != null)
            this.val$dl.beforeShow();
          if (this.val$dummy != null)
            this.val$dummy.setVisible(true);
          if (this.val$disabled)
            URLClassPathControl.disable();
          try
          {
            this.val$dlg.setVisible(true);
          }
          finally
          {
            if (this.val$disabled)
              URLClassPathControl.enable();
          }
        }
      };
      if (SwingUtilities.isEventDispatchThread())
        local21.run();
      else
        try
        {
          SwingUtilities.invokeAndWait(local21);
        }
        catch (Exception localException)
        {
          Trace.ignored(localException);
        }
    }
    else
    {
      this.dialogInterface.setVisible(false);
      this.dialogInterface.dispose();
      if (this.dummyFrame != null)
      {
        this.dummyFrame.setVisible(false);
        this.dummyFrame.dispose();
      }
    }
  }

  public void setMasthead(String paramString, boolean paramBoolean)
  {
    if (this.masthead != null)
    {
      this.topText = paramString;
      this.masthead.setText(paramString);
      if (paramBoolean)
        this.masthead.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 6));
      else
        this.masthead.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 6));
    }
  }

  public void setIcon(Image paramImage)
  {
    this.topIcon.setIcon(new ImageIcon(paramImage));
  }

  public static void resizeButtons(JButton[] paramArrayOfJButton)
  {
    int i = paramArrayOfJButton.length;
    int j = 50;
    for (int k = 0; k < i; k++)
    {
      if (paramArrayOfJButton[k].getPreferredSize().width <= j)
        continue;
      j = paramArrayOfJButton[k].getPreferredSize().width;
    }
    for (k = 0; k < i; k++)
    {
      Dimension localDimension = paramArrayOfJButton[k].getPreferredSize();
      localDimension.width = j;
      paramArrayOfJButton[k].setPreferredSize(localDimension);
    }
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    String str = paramActionEvent.getActionCommand();
    if (str.equals("OK"))
    {
      this.userAnswer = 0;
      if ((this.always != null) && (this.always.isSelected()))
        this.userAnswer = 2;
      if (this.stayAliveOnOk == true)
        return;
      if (this.password != null)
        this.pwd = this.password.getPassword();
      if (this.pwdName != null)
        this.userName = this.pwdName.getText();
      if (this.pwdDomain != null)
        this.domain = this.pwdDomain.getText();
      if (this.scrollList != null)
        this.userAnswer = this.scrollList.getSelectedIndex();
    }
    else
    {
      if ((this.throwable != null) || (this.detailPanel != null))
      {
        showMoreInfo();
        return;
      }
      this.userAnswer = 1;
      if (this.scrollList != null)
        this.userAnswer = -1;
    }
    setVisible(false);
  }

  public void cancelAction()
  {
    this.userAnswer = 1;
    setVisible(false);
  }

  public int getUserAnswer()
  {
    return this.userAnswer;
  }

  void setUserAnswer(int paramInt)
  {
    this.userAnswer = paramInt;
  }

  char[] getPassword()
  {
    return this.pwd;
  }

  String getUserName()
  {
    return this.userName;
  }

  String getDomain()
  {
    return this.domain;
  }

  public boolean isPasswordSaved()
  {
    return (this.always != null) && (this.always.isSelected());
  }

  public void progress(int paramInt)
  {
    if (this.progressBar != null)
      if (paramInt <= 100)
      {
        boolean bool = this.progressBar.isVisible();
        this.progressBar.setValue(paramInt);
        this.progressBar.setVisible(true);
      }
      else
      {
        this.progressBar.setVisible(false);
      }
  }

  void setMastheadBackground(Image paramImage)
  {
    if ((this.masthead != null) && ((this.masthead instanceof UITextArea)))
      ((UITextArea)this.masthead).setBackgroundImage(paramImage);
  }

  public void disposeDialog()
  {
    this.dialogInterface.dispose();
  }

  public Component deriveParent(Component paramComponent, String paramString)
  {
    if ((paramComponent == null) && (Config.getOSName().equals("Windows")))
    {
      this.dummyFrame = new Frame(paramString);
      this.dummyFrame.setLocation(-4096, -4096);
      return this.dummyFrame;
    }
    for (Object localObject = paramComponent; localObject != null; localObject = ((Component)localObject).getParent())
      if (((localObject instanceof Dialog)) || ((localObject instanceof Frame)))
        return localObject;
    return (Component)(Component)null;
  }

  public JDialog getDialog()
  {
    return this.dialogInterface.getDialog();
  }

  public void setInfo(String paramString1, String paramString2, URL paramURL)
  {
    if (this.nameInfo != null)
      this.nameInfo.setText(paramString1);
    if (this.publisherInfo != null)
    {
      this.appPublisher = paramString2;
      this.publisherInfo.setText(paramString2);
    }
    if (this.urlInfo != null)
    {
      this.appURL = paramURL;
      String str1 = " ";
      String str2 = "";
      if (paramURL != null)
      {
        str1 = paramURL.getProtocol() + "://" + paramURL.getHost();
        int i = paramURL.getPort();
        if (i != -1)
          str1 = str1 + ":" + Integer.toString(i);
        str2 = paramURL.toString();
      }
      this.urlInfo.setText(str1);
      this.urlInfo.setToolTipText(str2);
    }
  }

  void showOk(boolean paramBoolean)
  {
    JButton[] arrayOfJButton = { this.okBtn, this.cancelBtn };
    resizeButtons(arrayOfJButton);
    this.okBtn.setVisible(paramBoolean);
  }

  void stayAlive()
  {
    this.stayAliveOnOk = true;
  }

  public void setProgressStatusText(String paramString)
  {
    if (this.progressStatusLabel != null)
    {
      if ((paramString == null) || (paramString.length() == 0))
        paramString = " ";
      this.progressStatusLabel.setText(paramString);
    }
  }

  public void imageAvailable(URL paramURL, String paramString, Image paramImage, File paramFile)
  {
    int i = paramImage.getWidth(null);
    int j = paramImage.getHeight(null);
    Image localImage = paramImage;
    JLabel localJLabel = this.topIcon;
    new Thread(new Runnable(localImage, i, j, localJLabel)
    {
      private final Image val$imageIn;
      private final int val$w;
      private final int val$h;
      private final JLabel val$label;

      public void run()
      {
        Image localImage = this.val$imageIn;
        if ((48 != this.val$w) || (48 != this.val$h))
          localImage = this.val$imageIn.getScaledInstance(48, 48, 1);
        this.val$label.setIcon(new ImageIcon(localImage));
      }
    }).start();
  }

  public void finalImageAvailable(URL paramURL, String paramString, Image paramImage, File paramFile)
  {
    imageAvailable(paramURL, paramString, paramImage, paramFile);
  }

  public static int getSubpanelFontSize()
  {
    Font localFont = ResourceManager.getUIFont();
    int i = localFont.getSize() - 2;
    if (Config.getOSName().equalsIgnoreCase("windows"))
      i = localFont.getSize() - 1;
    if (i < minFontSize)
      i = minFontSize;
    return i;
  }

  public void addWindowListener(WindowListener paramWindowListener)
  {
    this.dialogInterface.addWindowListener(paramWindowListener);
  }

  public void removeWindowListener(WindowListener paramWindowListener)
  {
    this.dialogInterface.removeWindowListener(paramWindowListener);
  }

  private class LinkText extends JPanel
  {
    private JSmartTextArea _text;
    private FancyButton _link;
    Dimension size = null;

    public LinkText(String paramString1, String paramURL, URL arg4)
    {
      super();
      Object localObject;
      String str = localObject == null ? "http://java.com" : localObject.toString();
      if ((paramString1 != null) && (paramString1.length() > 0))
        this._text = new JSmartTextArea(paramString1);
      this._link = new FancyButton(paramURL, 0, DialogTemplate.this.LINK_COLOR);
      this._link.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
      this._link.addMouseListener(new MouseListener(DialogTemplate.this)
      {
        private final DialogTemplate val$this$0;

        public void mouseClicked(MouseEvent paramMouseEvent)
        {
        }

        public void mousePressed(MouseEvent paramMouseEvent)
        {
        }

        public void mouseReleased(MouseEvent paramMouseEvent)
        {
        }

        public void mouseEntered(MouseEvent paramMouseEvent)
        {
          DialogTemplate.LinkText.this._link.setForeground(DialogTemplate.this.LINK_HIGHLIGHT_COLOR);
        }

        public void mouseExited(MouseEvent paramMouseEvent)
        {
          DialogTemplate.LinkText.this._link.setForeground(DialogTemplate.this.LINK_COLOR);
        }
      });
      this._link.addActionListener(new ActionListener(DialogTemplate.this, str)
      {
        private final DialogTemplate val$this$0;
        private final String val$urlStr;

        public void actionPerformed(ActionEvent paramActionEvent)
        {
          Platform.get().showDocument(this.val$urlStr);
        }
      });
      JPanel localJPanel = new JPanel(new BorderLayout());
      localJPanel.setOpaque(false);
      localJPanel.add(this._link, "West");
      if (this._text != null)
        add(this._text, "North");
      add(localJPanel, "South");
    }

    public void setText(String paramString)
    {
      if (this._text != null)
        this._text.setText(paramString);
    }

    public void setFont(Font paramFont)
    {
      if (this._text != null)
        this._text.setFont(paramFont);
      if (this._link != null)
        this._link.setFont(paramFont);
      this.size = null;
    }

    public void setOpaque(boolean paramBoolean)
    {
      super.setOpaque(paramBoolean);
      if (this._text != null)
        this._text.setOpaque(paramBoolean);
      if (this._link != null)
        this._link.setOpaque(paramBoolean);
    }
  }

  private class SSVChoicePanel extends JPanel
  {
    ButtonGroup group;
    JRadioButton button1;
    JRadioButton button2;

    public SSVChoicePanel(String paramString1, String paramString2, String arg4)
    {
      super();
      setOpaque(false);
      setBorder(BorderFactory.createEmptyBorder(8, 16, 0, 16));
      Font localFont = ResourceManager.getUIFont().deriveFont(1);
      JPanel localJPanel1 = new JPanel(new BorderLayout());
      JPanel localJPanel2 = new JPanel(new BorderLayout());
      localJPanel1.setOpaque(false);
      localJPanel2.setOpaque(false);
      JLabel localJLabel = new JLabel(paramString1);
      localJLabel.setOpaque(false);
      localJLabel.setFont(DialogTemplate.this.ssvSmallBoldFont);
      localJPanel1.add(localJLabel, "Center");
      this.button1 = new JRadioButton(paramString2, true);
      this.button1.setFont(DialogTemplate.this.ssvSmallFont);
      this.button1.setOpaque(false);
      String str;
      this.button2 = new JRadioButton(str, false);
      this.button2.setFont(DialogTemplate.this.ssvSmallFont);
      this.button2.setOpaque(false);
      this.group = new ButtonGroup();
      this.group.add(this.button1);
      this.group.add(this.button2);
      localJPanel2.add(this.button1, "North");
      localJPanel2.add(this.button2, "South");
      localJPanel2.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 16));
      add(localJPanel1, "North");
      add(localJPanel2, "South");
      DialogTemplate.this.dialogInterface.setInitialFocusComponent(this.button1);
    }

    public int getSelection()
    {
      if (this.button2.isSelected())
        return 1;
      return 0;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.DialogTemplate
 * JD-Core Version:    0.6.0
 */