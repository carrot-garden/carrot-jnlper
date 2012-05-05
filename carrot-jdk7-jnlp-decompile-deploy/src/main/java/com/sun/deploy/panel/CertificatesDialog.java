package com.sun.deploy.panel;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.security.CertUtils;
import com.sun.deploy.security.CertificateDialog;
import com.sun.deploy.security.CredentialInfo;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.DialogTemplate;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import sun.misc.BASE64Encoder;

public class CertificatesDialog extends JDialog
  implements ListSelectionListener, ChangeListener
{
  public static final int USER_LEVEL = 0;
  public static final int SYSTEM_LEVEL = 1;
  private JComboBox certsComboBox;
  private JButton importButton;
  private JButton exportButton;
  private JButton removeButton;
  private JButton detailsButton;
  private JButton closeButton;
  private CertificatesInfo model = new CertificatesInfo();
  private JTabbedPane tabbedPane;
  private CertificateTabPanel userTab;
  private CertificateTabPanel systemTab;
  private String strTrustedCerts = getMessage("cert.type.trusted_certs");
  private String strSecureSite = getMessage("cert.type.secure_site");
  private String strSignerCa = getMessage("cert.type.signer_ca");
  private String strSecureSiteCa = getMessage("cert.type.secure_site_ca");
  private String strClientAuth = getMessage("cert.type.client_auth");
  private String[] certTypeName = { this.strTrustedCerts, this.strSecureSite, this.strSignerCa, this.strSecureSiteCa, this.strClientAuth };

  public CertificatesDialog(Frame paramFrame, boolean paramBoolean)
  {
    super(paramFrame, paramBoolean);
    initComponents();
  }

  private void initComponents()
  {
    getContentPane().setLayout(new BorderLayout());
    setDefaultCloseOperation(2);
    setTitle(getMessage("cert.settings"));
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent paramWindowEvent)
      {
        CertificatesDialog.this.closeDialog(paramWindowEvent);
      }
    });
    2 local2 = new JPanel()
    {
      public Dimension getPreferredSize()
      {
        Dimension localDimension = super.getPreferredSize();
        localDimension.height = 300;
        if (localDimension.width < 500)
          localDimension.width = 500;
        return localDimension;
      }
    };
    local2.setLayout(new BoxLayout(local2, 1));
    local2.setBorder(BorderFactory.createRaisedBevelBorder());
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(new BoxLayout(localJPanel1, 0));
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setLayout(new BoxLayout(localJPanel2, 1));
    localJPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
    localJPanel2.add(Box.createHorizontalStrut(60));
    this.certsComboBox = new JComboBox();
    this.certsComboBox.setModel(new DefaultComboBoxModel(this.certTypeName));
    this.certsComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CertificatesDialog.this.certsComboBoxActionPerformed(paramActionEvent);
      }
    });
    localJPanel2.add(this.certsComboBox);
    localJPanel1.add(new JLabel(getMessage("cert.dialog.certtype")));
    localJPanel1.add(localJPanel2);
    localJPanel1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    local2.add(localJPanel1);
    this.tabbedPane = new JTabbedPane();
    this.userTab = new CertificateTabPanel(this.model, 0);
    this.systemTab = new CertificateTabPanel(this.model, 1);
    this.userTab.registerSelectionListener(this);
    this.systemTab.registerSelectionListener(this);
    this.tabbedPane.setName(" ");
    this.tabbedPane.addTab(getMessage("cert.dialog.user.level"), this.userTab);
    this.tabbedPane.addTab(getMessage("cert.dialog.system.level"), this.systemTab);
    this.tabbedPane.setSelectedIndex(0);
    this.tabbedPane.addChangeListener(this);
    local2.add(this.tabbedPane);
    JPanel localJPanel3 = new JPanel();
    localJPanel3.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
    this.importButton = new JButton(getMessage("cert.import_button"));
    this.importButton.setMnemonic(ResourceManager.getVKCode("cert.import_button.mnemonic"));
    this.importButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CertificatesDialog.this.importButtonActionPerformed(paramActionEvent);
      }
    });
    this.importButton.setToolTipText(getMessage("cert.import_btn.tooltip"));
    localJPanel3.add(this.importButton);
    this.exportButton = new JButton(getMessage("cert.export_button"));
    this.exportButton.setMnemonic(ResourceManager.getVKCode("cert.export_button.mnemonic"));
    this.exportButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CertificatesDialog.this.exportButtonActionPerformed(paramActionEvent);
      }
    });
    this.exportButton.setToolTipText(getMessage("cert.export_btn.tooltip"));
    localJPanel3.add(this.exportButton);
    this.removeButton = new JButton(getMessage("cert.remove_button"));
    this.removeButton.setMnemonic(ResourceManager.getVKCode("cert.remove_button.mnemonic"));
    this.removeButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CertificatesDialog.this.removeButtonActionPerformed(paramActionEvent);
      }
    });
    this.removeButton.setToolTipText(getMessage("cert.remove_btn.tooltip"));
    localJPanel3.add(this.removeButton);
    this.detailsButton = new JButton(getMessage("cert.details_button"));
    this.detailsButton.setMnemonic(ResourceManager.getVKCode("cert.details_button.mnemonic"));
    this.detailsButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CertificatesDialog.this.detailsButtonActionPerformed(paramActionEvent);
      }
    });
    this.detailsButton.setToolTipText(getMessage("cert.details_btn.tooltip"));
    localJPanel3.add(this.detailsButton);
    JButton[] arrayOfJButton = { this.importButton, this.exportButton, this.detailsButton, this.removeButton };
    DialogTemplate.resizeButtons(arrayOfJButton);
    local2.add(localJPanel3);
    getContentPane().add(local2, "Center");
    JPanel localJPanel4 = new JPanel();
    localJPanel4.setLayout(new FlowLayout(2));
    localJPanel4.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    this.closeButton = new JButton(getMessage("cert.close_button"));
    8 local8 = new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CertificatesDialog.this.closeButtonActionPerformed(paramActionEvent);
      }
    };
    this.closeButton.addActionListener(local8);
    if (Config.isJavaVersionAtLeast13())
    {
      getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
      getRootPane().getActionMap().put("cancel", local8);
    }
    localJPanel4.add(this.closeButton);
    getContentPane().add(localJPanel4, "South");
    updateButtonState();
    getRootPane().setDefaultButton(this.closeButton);
    pack();
    setResizable(false);
  }

  public void valueChanged(ListSelectionEvent paramListSelectionEvent)
  {
    if (paramListSelectionEvent.getValueIsAdjusting())
      return;
    updateButtonState();
  }

  private void certsComboBoxActionPerformed(ActionEvent paramActionEvent)
  {
    reset();
  }

  private void closeButtonActionPerformed(ActionEvent paramActionEvent)
  {
    setVisible(false);
    dispose();
  }

  private void importButtonActionPerformed(ActionEvent paramActionEvent)
  {
    JFileChooser localJFileChooser = new JFileChooser();
    CertFileFilter localCertFileFilter = new CertFileFilter();
    localCertFileFilter.addExtension("csr");
    localCertFileFilter.addExtension("p12");
    localCertFileFilter.setDescription("Certificate Files");
    localJFileChooser.setFileFilter(localCertFileFilter);
    localJFileChooser.setFileSelectionMode(0);
    localJFileChooser.setDialogType(0);
    localJFileChooser.setMultiSelectionEnabled(false);
    int i = localJFileChooser.showOpenDialog(this);
    if (i == 0)
    {
      File localFile = localJFileChooser.getSelectedFile();
      if (localFile == null)
        return;
      try
      {
        Object localObject1 = System.in;
        localObject1 = new FileInputStream(localFile);
        boolean bool = false;
        bool = importCertificate((InputStream)localObject1);
        if (!bool)
        {
          localObject2 = System.in;
          localObject2 = new FileInputStream(localFile);
          str2 = (String)this.certsComboBox.getSelectedItem();
          if (str2.equals(this.strClientAuth))
            importPKCS12CertKey((InputStream)localObject2);
          else
            importPKCS12Certificate((InputStream)localObject2);
        }
      }
      catch (Throwable localThrowable)
      {
        String str1 = getMessage("cert.dialog.import.file.masthead");
        Object localObject2 = getMessage("cert.dialog.import.file.text");
        String str2 = getMessage("cert.dialog.import.error.caption");
        ToolkitStore.getUI().showExceptionDialog(getParent(), null, localThrowable, str2, str1, (String)localObject2, null);
      }
    }
    reset();
  }

  private void exportButtonActionPerformed(ActionEvent paramActionEvent)
  {
    String str1 = (String)this.certsComboBox.getSelectedItem();
    int i = -1;
    int j = 0;
    if (isUserLevelSelected())
    {
      j = 0;
      i = this.userTab.getSelectedCertificateTableRow();
    }
    else
    {
      j = 1;
      i = this.systemTab.getSelectedCertificateTableRow();
    }
    if (i != -1)
    {
      Collection localCollection = null;
      X509Certificate localX509Certificate = null;
      Certificate[] arrayOfCertificate = { null };
      if (str1.equals(this.strTrustedCerts))
        localCollection = this.model.getTrustedCertificates(j);
      else if (str1.equals(this.strSecureSite))
        localCollection = this.model.getHttpsCertificates(j);
      else if (str1.equals(this.strSignerCa))
        localCollection = this.model.getRootCACertificates(j);
      else if (str1.equals(this.strSecureSiteCa))
        localCollection = this.model.getHttpsRootCACertificates(j);
      else if (str1.equals(this.strClientAuth))
        localCollection = this.model.getClientAuthCertificates(j);
      Object[] arrayOfObject = localCollection.toArray();
      if (str1.equals(this.strClientAuth))
      {
        arrayOfCertificate = (Certificate[])(Certificate[])arrayOfObject[i];
        localX509Certificate = (X509Certificate)arrayOfCertificate[0];
      }
      else
      {
        localX509Certificate = (X509Certificate)arrayOfObject[i];
      }
      Object localObject1;
      Object localObject2;
      Object localObject3;
      if (localX509Certificate != null)
      {
        localObject1 = new JFileChooser();
        ((JFileChooser)localObject1).setFileSelectionMode(0);
        ((JFileChooser)localObject1).setDialogType(1);
        ((JFileChooser)localObject1).setMultiSelectionEnabled(false);
        int k = ((JFileChooser)localObject1).showSaveDialog(this);
        if (k == 0)
        {
          localObject2 = ((JFileChooser)localObject1).getSelectedFile();
          if (localObject2 == null)
            return;
          localObject3 = null;
          try
          {
            if (str1.equals(this.strClientAuth))
            {
              exportPKCS12Cert(arrayOfCertificate, (File)localObject2, j);
            }
            else
            {
              localObject3 = new PrintStream(new BufferedOutputStream(new FileOutputStream((File)localObject2)));
              exportCertificate(localX509Certificate, (PrintStream)localObject3);
            }
          }
          catch (Throwable localThrowable)
          {
            String str3 = getMessage("cert.dialog.export.masthead");
            String str4 = getMessage("cert.dialog.export.text");
            String str5 = getMessage("cert.dialog.export.error.caption");
            ToolkitStore.getUI().showExceptionDialog(getParent(), null, localThrowable, str5, str3, str4, null);
          }
          finally
          {
            if (localObject3 != null)
              ((PrintStream)localObject3).close();
          }
        }
      }
      else
      {
        localObject1 = getMessage("cert.dialog.export.text");
        String str2 = getMessage("cert.dialog.export.error.caption");
        localObject2 = ResourceManager.getString("common.ok_btn");
        localObject3 = ResourceManager.getString("common.detail.button");
        ToolkitStore.getUI();
        ToolkitStore.getUI().showMessageDialog(this, null, 0, str2, (String)localObject1, null, null, (String)localObject2, (String)localObject3, null);
      }
    }
  }

  private void removeButtonActionPerformed(ActionEvent paramActionEvent)
  {
    String str1 = (String)this.certsComboBox.getSelectedItem();
    int[] arrayOfInt = null;
    int i = 0;
    if (isUserLevelSelected())
    {
      i = 0;
      arrayOfInt = this.userTab.getSelectedCertificateTableRows();
    }
    else
    {
      i = 1;
      arrayOfInt = this.systemTab.getSelectedCertificateTableRows();
    }
    if (arrayOfInt.length != 0)
    {
      String str2 = getMessage("cert.dialog.remove.masthead");
      String str3 = getMessage("cert.dialog.remove.text");
      String str4 = getMessage("cert.dialog.remove.caption");
      String str5 = ResourceManager.getString("common.ok_btn");
      String str6 = ResourceManager.getString("common.cancel_btn");
      ToolkitStore.getUI();
      int j = ToolkitStore.getUI().showMessageDialog(this, null, 2, str4, str2, str3, null, str5, str6, null);
      ToolkitStore.getUI();
      if (j == 0)
      {
        Collection localCollection;
        Object[] arrayOfObject;
        int k;
        if (str1.equals(this.strTrustedCerts))
        {
          localCollection = this.model.getTrustedCertificates(i);
          arrayOfObject = localCollection.toArray();
          for (k = 0; k < arrayOfInt.length; k++)
            this.model.removeTrustedCertificate((Certificate)arrayOfObject[arrayOfInt[k]]);
        }
        else if (str1.equals(this.strSecureSite))
        {
          localCollection = this.model.getHttpsCertificates(i);
          arrayOfObject = localCollection.toArray();
          for (k = 0; k < arrayOfInt.length; k++)
            this.model.removeHttpsCertificate((Certificate)arrayOfObject[arrayOfInt[k]]);
        }
        else if (str1.equals(this.strSignerCa))
        {
          localCollection = this.model.getRootCACertificates(i);
          arrayOfObject = localCollection.toArray();
          for (k = 0; k < arrayOfInt.length; k++)
            this.model.removeRootCACertificate((Certificate)arrayOfObject[arrayOfInt[k]]);
        }
        else if (str1.equals(this.strSecureSiteCa))
        {
          localCollection = this.model.getHttpsRootCACertificates(i);
          arrayOfObject = localCollection.toArray();
          for (k = 0; k < arrayOfInt.length; k++)
            this.model.removeHttpsRootCACertificate((Certificate)arrayOfObject[arrayOfInt[k]]);
        }
        else if (str1.equals(this.strClientAuth))
        {
          localCollection = this.model.getClientAuthCertificates(i);
          arrayOfObject = localCollection.toArray();
          Certificate[][] arrayOfCertificate; = new Certificate[arrayOfInt.length][];
          for (int m = 0; m < arrayOfInt.length; m++)
            arrayOfCertificate;[m] = ((Certificate[])(Certificate[])arrayOfObject[arrayOfInt[m]]);
          this.model.removeClientAuthCertificate(this, arrayOfCertificate;);
        }
        reset();
      }
    }
  }

  private void detailsButtonActionPerformed(ActionEvent paramActionEvent)
  {
    String str = (String)this.certsComboBox.getSelectedItem();
    int i = -1;
    if (isUserLevelSelected())
      i = this.userTab.getSelectedCertificateTableRow();
    else
      i = this.systemTab.getSelectedCertificateTableRow();
    if (i != -1)
    {
      Collection localCollection = null;
      Object localObject = null;
      Certificate[] arrayOfCertificate = { null };
      int j = 0;
      if (!isUserLevelSelected())
        j = 1;
      if (str.equals(this.strTrustedCerts))
        localCollection = this.model.getTrustedCertificates(j);
      else if (str.equals(this.strSecureSite))
        localCollection = this.model.getHttpsCertificates(j);
      else if (str.equals(this.strSignerCa))
        localCollection = this.model.getRootCACertificates(j);
      else if (str.equals(this.strSecureSiteCa))
        localCollection = this.model.getHttpsRootCACertificates(j);
      else if (str.equals(this.strClientAuth))
        localCollection = this.model.getClientAuthCertificates(j);
      Object[] arrayOfObject = localCollection.toArray();
      if (str.equals(this.strClientAuth))
        arrayOfCertificate = (Certificate[])(Certificate[])arrayOfObject[i];
      else
        arrayOfCertificate = new Certificate[] { (Certificate)arrayOfObject[i] };
      if (arrayOfCertificate[0] != null)
        CertificateDialog.showCertificates(this, arrayOfCertificate, 0, arrayOfCertificate.length);
    }
  }

  private void closeDialog(WindowEvent paramWindowEvent)
  {
    setVisible(false);
    dispose();
  }

  private void exportPKCS12Cert(Certificate[] paramArrayOfCertificate, File paramFile, int paramInt)
  {
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    String str1 = null;
    try
    {
      if (paramInt == 0)
        str1 = Config.getUserClientAuthCertFile();
      else
        str1 = Config.getSystemClientAuthCertFile();
      KeyStore localKeyStore1 = KeyStore.getInstance("JKS");
      localObject1 = new FileInputStream(str1);
      arrayOfChar1 = getPasswordDialog("cert.dialog.exportpassword.text", "password.dialog.title");
      localKeyStore1.load((InputStream)localObject1, arrayOfChar1);
      if (arrayOfChar1 != null)
      {
        str2 = localKeyStore1.getCertificateAlias(paramArrayOfCertificate[0]);
        localObject2 = localKeyStore1.getKey(str2, arrayOfChar1);
        FileOutputStream localFileOutputStream = new FileOutputStream(paramFile);
        KeyStore localKeyStore2 = KeyStore.getInstance("PKCS12");
        localKeyStore2.load(null, null);
        arrayOfChar2 = getPasswordDialog("cert.dialog.savepassword.text", "password.dialog.title");
        if (arrayOfChar2 != null)
        {
          localKeyStore2.setKeyEntry(str2, (Key)localObject2, arrayOfChar2, paramArrayOfCertificate);
          localKeyStore2.store(localFileOutputStream, arrayOfChar2);
        }
      }
    }
    catch (Exception localException)
    {
      Object localObject1 = getMessage("cert.dialog.export.password.masthead");
      String str2 = getMessage("cert.dialog.export.password.text");
      Object localObject2 = getMessage("cert.dialog.export.error.caption");
      ToolkitStore.getUI().showExceptionDialog(getParent(), null, localException, (String)localObject2, (String)localObject1, str2, null);
    }
  }

  private void exportCertificate(X509Certificate paramX509Certificate, PrintStream paramPrintStream)
  {
    BASE64Encoder localBASE64Encoder = new BASE64Encoder();
    paramPrintStream.println("-----BEGIN CERTIFICATE-----");
    try
    {
      localBASE64Encoder.encodeBuffer(paramX509Certificate.getEncoded(), paramPrintStream);
    }
    catch (Throwable localThrowable)
    {
    }
    paramPrintStream.println("-----END CERTIFICATE-----");
  }

  void importPKCS12Certificate(InputStream paramInputStream)
  {
    char[] arrayOfChar = null;
    try
    {
      KeyStore localKeyStore = KeyStore.getInstance("PKCS12");
      arrayOfChar = getPasswordDialog("cert.dialog.password.text", "password.dialog.title");
      if (arrayOfChar != null)
      {
        localKeyStore.load(paramInputStream, arrayOfChar);
        localObject1 = localKeyStore.aliases();
        str1 = (String)this.certsComboBox.getSelectedItem();
        while (((Enumeration)localObject1).hasMoreElements())
        {
          str2 = (String)((Enumeration)localObject1).nextElement();
          localObject2 = (X509Certificate)localKeyStore.getCertificate(str2);
          if (str1.equals(this.strTrustedCerts))
            this.model.addTrustedCertificate((Certificate)localObject2);
          else if (str1.equals(this.strSecureSite))
            this.model.addHttpsCertificate((Certificate)localObject2);
          else if (str1.equals(this.strSignerCa))
            this.model.addCACertificate((Certificate)localObject2);
          else if (str1.equals(this.strSecureSiteCa))
            this.model.addHttpsCACertificate((Certificate)localObject2);
          reset();
        }
      }
    }
    catch (Throwable localThrowable)
    {
      String str1;
      String str2;
      Object localObject2;
      Object localObject1 = "uninitializedValue";
      if (!compareCharArray(arrayOfChar, ((String)localObject1).toCharArray()))
      {
        str1 = getMessage("cert.dialog.import.password.masthead");
        str2 = getMessage("cert.dialog.import.password.text");
        localObject2 = getMessage("cert.dialog.import.error.caption");
        ToolkitStore.getUI().showExceptionDialog(getParent(), null, localThrowable, (String)localObject2, str1, str2, null);
      }
    }
    finally
    {
      if (arrayOfChar != null)
        Arrays.fill(arrayOfChar, ' ');
    }
  }

  void importPKCS12CertKey(InputStream paramInputStream)
  {
    char[] arrayOfChar = null;
    try
    {
      arrayOfChar = getPasswordDialog("cert.dialog.password.text", "password.dialog.title");
      if (arrayOfChar != null)
      {
        KeyStore localKeyStore = KeyStore.getInstance("PKCS12");
        localKeyStore.load(paramInputStream, arrayOfChar);
        localObject1 = localKeyStore.aliases();
        while (((Enumeration)localObject1).hasMoreElements())
        {
          str = (String)((Enumeration)localObject1).nextElement();
          localObject2 = localKeyStore.getCertificateChain(str);
          Key localKey = localKeyStore.getKey(str, arrayOfChar);
          this.model.addClientAuthCertChain(this, localObject2, localKey);
        }
      }
    }
    catch (Throwable localThrowable)
    {
      Object localObject1 = getMessage("cert.dialog.import.password.masthead");
      String str = getMessage("cert.dialog.import.password.text");
      Object localObject2 = getMessage("cert.dialog.import.error.caption");
      ToolkitStore.getUI().showExceptionDialog(getParent(), null, localThrowable, (String)localObject2, (String)localObject1, str, null);
    }
    finally
    {
      if (arrayOfChar != null)
        Arrays.fill(arrayOfChar, ' ');
    }
  }

  private boolean compareCharArray(char[] paramArrayOfChar1, char[] paramArrayOfChar2)
  {
    if (paramArrayOfChar1.length != paramArrayOfChar2.length)
      return false;
    for (int i = 0; i < paramArrayOfChar1.length; i++)
      if (paramArrayOfChar1[i] != paramArrayOfChar2[i])
        return false;
    return true;
  }

  boolean importCertificate(InputStream paramInputStream)
  {
    CertificateFactory localCertificateFactory = null;
    X509Certificate localX509Certificate = null;
    try
    {
      String str1 = (String)this.certsComboBox.getSelectedItem();
      localCertificateFactory = CertificateFactory.getInstance("X.509");
      localX509Certificate = (X509Certificate)localCertificateFactory.generateCertificate(paramInputStream);
      if (str1.equals(this.strTrustedCerts))
        this.model.addTrustedCertificate(localX509Certificate);
      else if (str1.equals(this.strSecureSite))
        this.model.addHttpsCertificate(localX509Certificate);
      else if (str1.equals(this.strSignerCa))
        this.model.addCACertificate(localX509Certificate);
      else if (str1.equals(this.strSecureSiteCa))
        this.model.addHttpsCACertificate(localX509Certificate);
      else if (str1.equals(this.strClientAuth))
        return false;
      reset();
    }
    catch (CertificateParsingException localCertificateParsingException)
    {
      return false;
    }
    catch (CertificateException localCertificateException)
    {
      String str2 = getMessage("cert.dialog.import.format.masthead");
      String str3 = getMessage("cert.dialog.import.format.text");
      String str4 = getMessage("cert.dialog.import.error.caption");
      ToolkitStore.getUI().showExceptionDialog(getParent(), null, localCertificateException, str4, str2, str3, null);
    }
    return true;
  }

  private void reset()
  {
    String str = (String)this.certsComboBox.getSelectedItem();
    Collection localCollection = null;
    if (isUserLevelSelected())
    {
      if (str.equals(this.strTrustedCerts))
        localCollection = this.model.getTrustedCertificates(0);
      else if (str.equals(this.strSecureSite))
        localCollection = this.model.getHttpsCertificates(0);
      else if (str.equals(this.strSignerCa))
        localCollection = this.model.getRootCACertificates(0);
      else if (str.equals(this.strSecureSiteCa))
        localCollection = this.model.getHttpsRootCACertificates(0);
      else if (str.equals(this.strClientAuth))
        localCollection = this.model.getClientAuthCertificates(0);
    }
    else if (str.equals(this.strTrustedCerts))
      localCollection = this.model.getTrustedCertificates(1);
    else if (str.equals(this.strSecureSite))
      localCollection = this.model.getHttpsCertificates(1);
    else if (str.equals(this.strSignerCa))
      localCollection = this.model.getRootCACertificates(1);
    else if (str.equals(this.strSecureSiteCa))
      localCollection = this.model.getHttpsRootCACertificates(1);
    else if (str.equals(this.strClientAuth))
      localCollection = this.model.getClientAuthCertificates(1);
    ReadOnlyTableModel localReadOnlyTableModel;
    if ((localCollection == null) || (localCollection.size() == 0))
    {
      localReadOnlyTableModel = new ReadOnlyTableModel();
    }
    else
    {
      localReadOnlyTableModel = new ReadOnlyTableModel(localCollection.size());
      int i = 0;
      Iterator localIterator = localCollection.iterator();
      while (localIterator.hasNext())
      {
        X509Certificate localX509Certificate = null;
        if (str.equals(this.strClientAuth))
        {
          Certificate[] arrayOfCertificate = (Certificate[])(Certificate[])localIterator.next();
          localX509Certificate = (X509Certificate)arrayOfCertificate[0];
        }
        else
        {
          localX509Certificate = (X509Certificate)localIterator.next();
        }
        localReadOnlyTableModel.setValueAt(CertUtils.extractSubjectAliasName(localX509Certificate), i, 0);
        localReadOnlyTableModel.setValueAt(CertUtils.extractIssuerAliasName(localX509Certificate), i, 1);
        i++;
      }
    }
    if (isUserLevelSelected())
      this.userTab.setCertificateTableModel(localReadOnlyTableModel);
    else
      this.systemTab.setCertificateTableModel(localReadOnlyTableModel);
    updateButtonState();
  }

  boolean isUserLevelSelected()
  {
    return this.tabbedPane.getSelectedIndex() == 0;
  }

  private void setEnabled(JComponent paramJComponent, boolean paramBoolean)
  {
    paramJComponent.setEnabled(paramBoolean);
    paramJComponent.repaint();
  }

  public void stateChanged(ChangeEvent paramChangeEvent)
  {
    reset();
  }

  private void updateButtonState()
  {
    if (isUserLevelSelected())
    {
      int[] arrayOfInt = this.userTab.getSelectedCertificateTableRows();
      if (arrayOfInt.length == 0)
      {
        setEnabled(this.removeButton, false);
        setEnabled(this.exportButton, false);
        setEnabled(this.importButton, true);
        setEnabled(this.detailsButton, false);
      }
      else if (arrayOfInt.length == 1)
      {
        setEnabled(this.removeButton, true);
        setEnabled(this.exportButton, true);
        setEnabled(this.importButton, true);
        setEnabled(this.detailsButton, true);
      }
      else
      {
        setEnabled(this.removeButton, true);
        setEnabled(this.exportButton, false);
        setEnabled(this.importButton, false);
        setEnabled(this.detailsButton, false);
      }
    }
    else
    {
      boolean bool = this.systemTab.isCertificateSelected();
      setEnabled(this.removeButton, false);
      setEnabled(this.exportButton, bool);
      setEnabled(this.importButton, false);
      setEnabled(this.detailsButton, bool);
    }
  }

  private char[] getPasswordDialog(String paramString1, String paramString2)
  {
    try
    {
      CredentialInfo localCredentialInfo = ToolkitStore.getUI().showPasswordDialog(this, getMessage(paramString2), getMessage(paramString1), false, false, null, false, null);
      if (localCredentialInfo != null)
        return localCredentialInfo.getPassword();
      return null;
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    return null;
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.CertificatesDialog
 * JD-Core Version:    0.6.0
 */