package com.sun.deploy.ui;

import com.sun.deploy.config.Platform;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.security.CertificateDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.cert.Certificate;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

class MoreInfoDialog extends JDialog
{
  private FancyButton details;
  private String[] alerts;
  private String[] infos;
  private int securityInfoCount;
  private Certificate[] certs;
  private int start;
  private int end;
  private boolean majorWarning;
  private final String WARNING_ICON = "com/sun/deploy/resources/image/icon-warning16.png";
  private final String INFO_ICON = "com/sun/deploy/resources/image/icon-info16.png";
  private final int VERTICAL_STRUT = 18;
  private final int HORIZONTAL_STRUT = 12;
  private final String linkUrlStr = "http://java.com/en/download/faq/self_signed.xml";
  private final int TEXT_WIDTH = 326;

  MoreInfoDialog(JDialog paramJDialog, String[] paramArrayOfString1, String[] paramArrayOfString2, int paramInt1, Certificate[] paramArrayOfCertificate, int paramInt2, int paramInt3, boolean paramBoolean)
  {
    super(paramJDialog, true);
    this.alerts = paramArrayOfString1;
    this.infos = paramArrayOfString2;
    this.securityInfoCount = paramInt1;
    this.certs = paramArrayOfCertificate;
    this.start = paramInt2;
    this.end = paramInt3;
    this.majorWarning = paramBoolean;
    initComponents(null, null);
    setResizable(false);
  }

  MoreInfoDialog(JDialog paramJDialog, JPanel paramJPanel, Throwable paramThrowable, Certificate[] paramArrayOfCertificate, boolean paramBoolean)
  {
    super(paramJDialog, true);
    this.certs = paramArrayOfCertificate;
    this.start = 0;
    this.end = (paramArrayOfCertificate == null ? 0 : paramArrayOfCertificate.length);
    this.majorWarning = paramBoolean;
    initComponents(paramJPanel, paramThrowable);
  }

  private void initComponents(JPanel paramJPanel, Throwable paramThrowable)
  {
    setTitle(getMessage("security.more.info.title"));
    getContentPane().setLayout(new BorderLayout());
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
    localJPanel1.setLayout(new BoxLayout(localJPanel1, 1));
    localJPanel1.setAlignmentX(0.0F);
    if (paramJPanel != null)
    {
      localJPanel1.add(paramJPanel);
    }
    else if (paramThrowable != null)
    {
      localJPanel1.add(Box.createHorizontalStrut(440));
      JPanel localJPanel2 = new JPanel(new BorderLayout());
      JLabel localJLabel = new JLabel(ResourceManager.getString("exception.details.label"));
      localJLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
      localJPanel2.add(localJLabel, "West");
      localJPanel1.add(localJPanel2);
      StringWriter localStringWriter = new StringWriter();
      PrintWriter localPrintWriter = new PrintWriter(localStringWriter);
      paramThrowable.printStackTrace(localPrintWriter);
      JTextArea localJTextArea = new JTextArea(localStringWriter.toString(), 20, 60);
      localJTextArea.setFont(ResourceManager.getUIFont());
      localJTextArea.setEditable(false);
      localJTextArea.setLineWrap(true);
      localJTextArea.setWrapStyleWord(false);
      JScrollPane localJScrollPane = new JScrollPane(localJTextArea, 20, 30);
      localJPanel1.add(localJScrollPane);
      if (this.certs != null)
      {
        localJPanel1.add(Box.createVerticalStrut(18));
        localJPanel1.add(getLinkPanel());
      }
    }
    else
    {
      localJPanel1.add(getSecurityPanel());
      if (this.certs != null)
        localJPanel1.add(getLinkPanel());
      localJPanel1.add(Box.createVerticalStrut(18));
      localJPanel1.add(getIntegrationPanel());
    }
    localJPanel1.add(Box.createVerticalStrut(18));
    localJPanel1.add(getBtnPanel());
    getContentPane().add(localJPanel1, "Center");
    pack();
    UIFactory.placeWindow(this);
    getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
    getRootPane().getActionMap().put("cancel", new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        MoreInfoDialog.this.dismissAction();
      }
    });
  }

  private JPanel getSecurityPanel()
  {
    JPanel localJPanel = new JPanel();
    localJPanel.setLayout(new BoxLayout(localJPanel, 1));
    int k = this.certs == null ? 1 : 0;
    int i = (k != 0) || (this.alerts == null) ? 0 : 1;
    int j = this.alerts == null ? 0 : this.alerts.length;
    localJPanel.add(blockPanel("com/sun/deploy/resources/image/icon-warning16.png", this.alerts, i, j));
    i = (k != 0) || (this.alerts != null) ? 0 : 1;
    j = this.securityInfoCount;
    localJPanel.add(blockPanel("com/sun/deploy/resources/image/icon-info16.png", this.infos, i, j));
    return localJPanel;
  }

  private JPanel getLinkPanel()
  {
    JPanel localJPanel = new JPanel();
    localJPanel.setLayout(new BoxLayout(localJPanel, 0));
    localJPanel.setAlignmentX(0.0F);
    localJPanel.add(Box.createHorizontalGlue());
    String str = "security.more.info.details";
    this.details = new FancyButton(ResourceManager.getMessage(str), ResourceManager.getAcceleratorKey(str), Color.blue);
    this.details.addMouseListener(new MouseListener()
    {
      public void mouseClicked(MouseEvent paramMouseEvent)
      {
        if ((paramMouseEvent.getComponent() instanceof FancyButton))
          MoreInfoDialog.this.showCertDetails();
      }

      public void mouseEntered(MouseEvent paramMouseEvent)
      {
      }

      public void mouseExited(MouseEvent paramMouseEvent)
      {
      }

      public void mousePressed(MouseEvent paramMouseEvent)
      {
      }

      public void mouseReleased(MouseEvent paramMouseEvent)
      {
      }
    });
    this.details.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        MoreInfoDialog.this.showCertDetails();
      }
    });
    localJPanel.add(this.details);
    return localJPanel;
  }

  private JPanel getIntegrationPanel()
  {
    int i = this.securityInfoCount;
    int j = this.infos == null ? 0 : this.infos.length;
    return blockPanel("com/sun/deploy/resources/image/icon-info16.png", this.infos, i, j);
  }

  private JPanel getBtnPanel()
  {
    JPanel localJPanel = new JPanel();
    localJPanel.setLayout(new BoxLayout(localJPanel, 0));
    localJPanel.setAlignmentX(0.0F);
    localJPanel.add(Box.createHorizontalGlue());
    JButton localJButton = new JButton(getMessage("common.close_btn"));
    localJButton.setAlignmentY(0.0F);
    localJButton.setAlignmentX(0.0F);
    localJButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        MoreInfoDialog.this.dismissAction();
      }
    });
    getRootPane().setDefaultButton(localJButton);
    if (this.majorWarning)
    {
      String str = "security.more.info.linkurl";
      FancyButton localFancyButton = new FancyButton(ResourceManager.getMessage(str), ResourceManager.getAcceleratorKey(str));
      localFancyButton.setAlignmentY(0.0F);
      localFancyButton.setAlignmentX(0.0F);
      localFancyButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          Platform.get().showDocument("http://java.com/en/download/faq/self_signed.xml");
        }
      });
      localJPanel.add(localFancyButton);
      localJPanel.add(Box.createHorizontalStrut(95));
    }
    localJPanel.add(localJButton);
    return localJPanel;
  }

  private JPanel blockPanel(String paramString, String[] paramArrayOfString, int paramInt1, int paramInt2)
  {
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(new BoxLayout(localJPanel1, 1));
    if (paramArrayOfString != null)
      for (int i = paramInt1; i < paramInt2; i++)
      {
        JPanel localJPanel2 = new JPanel();
        localJPanel2.setLayout(new BoxLayout(localJPanel2, 0));
        localJPanel2.setAlignmentX(0.0F);
        JLabel localJLabel = new JLabel();
        localJLabel.setIcon(new ImageIcon(ClassLoader.getSystemResource(paramString)));
        localJLabel.setAlignmentY(0.0F);
        localJLabel.setAlignmentX(0.0F);
        UITextArea localUITextArea = new UITextArea(new JLabel().getFont().getSize(), 326, false);
        localUITextArea.setText(paramArrayOfString[i]);
        Dimension localDimension = localUITextArea.getPreferredSize();
        localUITextArea.setSize(localDimension.width, localDimension.height);
        localUITextArea.setAlignmentY(0.0F);
        localUITextArea.setAlignmentX(0.0F);
        localJPanel2.add(localJLabel);
        localJPanel2.add(Box.createHorizontalStrut(12));
        localJPanel2.add(localUITextArea);
        localJPanel2.add(Box.createHorizontalGlue());
        localJPanel1.add(localJPanel2);
        localJPanel1.add(Box.createVerticalStrut(18));
      }
    return localJPanel1;
  }

  private void showCertDetails()
  {
    CertificateDialog.showCertificates(this, this.certs, this.start, this.end);
  }

  private void dismissAction()
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
 * Qualified Name:     com.sun.deploy.ui.MoreInfoDialog
 * JD-Core Version:    0.6.0
 */