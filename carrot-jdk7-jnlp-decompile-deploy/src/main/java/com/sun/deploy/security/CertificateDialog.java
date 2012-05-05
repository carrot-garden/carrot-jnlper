package com.sun.deploy.security;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import sun.misc.HexDumpEncoder;
import sun.security.x509.SerialNumber;

public class CertificateDialog
{
  public static void showCertificates(JDialog paramJDialog, Certificate[] paramArrayOfCertificate, int paramInt1, int paramInt2)
  {
    JDialog localJDialog = new JDialog(paramJDialog, ResourceManager.getMessage("cert.dialog.caption"), true);
    localJDialog.getContentPane().setLayout(new BorderLayout());
    localJDialog.getContentPane().add(getComponents(paramJDialog, paramArrayOfCertificate, paramInt1, paramInt2), "Center");
    JPanel localJPanel = new JPanel();
    localJPanel.setLayout(new FlowLayout(4));
    JButton localJButton = new JButton(getMessage("cert.dialog.close"));
    localJButton.setMnemonic(getAcceleratorKey("cert.dialog.close"));
    localJButton.addActionListener(new ActionListener(localJDialog)
    {
      private final JDialog val$details;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        this.val$details.setVisible(false);
      }
    });
    localJPanel.add(localJButton);
    localJDialog.getContentPane().add(localJPanel, "South");
    localJDialog.pack();
    localJDialog.setLocationRelativeTo(paramJDialog);
    localJDialog.setResizable(false);
    if (!Trace.isAutomationEnabled())
      localJDialog.setVisible(true);
  }

  private static JPanel getComponents(JDialog paramJDialog, Certificate[] paramArrayOfCertificate, int paramInt1, int paramInt2)
  {
    if ((paramArrayOfCertificate.length > paramInt1) && ((paramArrayOfCertificate[paramInt1] instanceof X509Certificate)))
    {
      JTable localJTable = new JTable();
      JTextArea localJTextArea = new JTextArea();
      Border localBorder = BorderFactory.createEtchedBorder();
      JTree localJTree = buildCertChainTree(paramArrayOfCertificate, paramInt1, paramInt2);
      localJTree.addTreeSelectionListener(new TreeSelectionListener(localJTree, localJTable, localJTextArea)
      {
        private final JTree val$certChainTree;
        private final JTable val$certInfoTable;
        private final JTextArea val$textArea;

        public void valueChanged(TreeSelectionEvent paramTreeSelectionEvent)
        {
          DefaultMutableTreeNode localDefaultMutableTreeNode = (DefaultMutableTreeNode)this.val$certChainTree.getLastSelectedPathComponent();
          if (localDefaultMutableTreeNode != null)
          {
            CertificateInfo localCertificateInfo = (CertificateInfo)localDefaultMutableTreeNode.getUserObject();
            CertificateDialog.access$000(localCertificateInfo.getCertificate(), this.val$certInfoTable, this.val$textArea);
          }
        }
      });
      showCertificateInfo((X509Certificate)paramArrayOfCertificate[paramInt1], localJTable, localJTextArea);
      localJTable.setSelectionMode(0);
      ListSelectionModel localListSelectionModel = localJTable.getSelectionModel();
      localListSelectionModel.addListSelectionListener(new ListSelectionListener(localJTable, localJTextArea)
      {
        private final JTable val$certInfoTable;
        private final JTextArea val$textArea;

        public void valueChanged(ListSelectionEvent paramListSelectionEvent)
        {
          int i = this.val$certInfoTable.getSelectedRow();
          if (i >= 0)
          {
            String str = (String)this.val$certInfoTable.getValueAt(i, 1);
            this.val$textArea.setText(str);
            this.val$textArea.repaint();
          }
        }
      });
      localJTextArea.setLineWrap(false);
      localJTextArea.setEditable(false);
      localJTextArea.setRows(10);
      localJTextArea.setColumns(40);
      Font localFont1 = ResourceManager.getUIFont();
      Font localFont2 = new Font("Monospaced", 0, localFont1.getSize());
      localJTextArea.setFont(localFont2);
      int i = localFont1.getSize();
      localJTable.setRowSelectionInterval(8, 8);
      JPanel localJPanel1 = new JPanel();
      localJPanel1.setLayout(new BorderLayout());
      Dimension localDimension = localJTable.getPreferredScrollableViewportSize();
      int j = Math.max(145, 8 + 9 * i);
      localDimension.setSize(localDimension.getWidth(), j);
      localJTable.setPreferredScrollableViewportSize(localDimension);
      JScrollPane localJScrollPane = new JScrollPane(localJTable);
      localJScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0), localJScrollPane.getBorder()));
      localJPanel1.add(localJScrollPane, "Center");
      localJPanel1.add(new JScrollPane(localJTextArea), "South");
      localJPanel1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      JPanel localJPanel2 = new JPanel();
      localJPanel2.setLayout(new BorderLayout());
      localDimension = localJTree.getPreferredScrollableViewportSize();
      localDimension.setSize(200.0D, 100.0D);
      localJScrollPane = new JScrollPane(localJTree);
      localJScrollPane.setPreferredSize(localDimension);
      localJPanel2.add(localJScrollPane, "West");
      localJPanel2.add(localJPanel1, "East");
      return localJPanel2;
    }
    return new JPanel();
  }

  private static String formatDNString(String paramString)
  {
    int i = paramString.length();
    int j = 0;
    int k = 0;
    StringBuffer localStringBuffer = new StringBuffer();
    for (int m = 0; m < i; m++)
    {
      char c = paramString.charAt(m);
      if ((c == '"') || (c == '\''))
        k = k == 0 ? 1 : 0;
      if ((c == ',') && (k == 0))
        localStringBuffer.append(",\n");
      else
        localStringBuffer.append(c);
    }
    return localStringBuffer.toString();
  }

  private static JTree buildCertChainTree(Certificate[] paramArrayOfCertificate, int paramInt1, int paramInt2)
  {
    Object localObject1 = null;
    Object localObject2 = null;
    for (int i = paramInt1; (i < paramArrayOfCertificate.length) && (i < paramInt2); i++)
    {
      localObject3 = new DefaultMutableTreeNode(new CertificateInfo((X509Certificate)paramArrayOfCertificate[i]));
      if (localObject1 == null)
      {
        localObject1 = localObject3;
        localObject2 = localObject3;
      }
      else
      {
        localObject2.add((MutableTreeNode)localObject3);
        localObject2 = localObject3;
      }
    }
    JTree localJTree = new JTree(localObject1);
    Object localObject3 = new DefaultTreeCellRenderer();
    ((DefaultTreeCellRenderer)localObject3).putClientProperty("html.disable", Boolean.TRUE);
    localJTree.setCellRenderer((TreeCellRenderer)localObject3);
    localJTree.getSelectionModel().setSelectionMode(1);
    localJTree.putClientProperty("JTree.lineStyle", "Angled");
    return (JTree)localJTree;
  }

  private static void byte2hex(byte paramByte, StringBuffer paramStringBuffer)
  {
    char[] arrayOfChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    int i = (paramByte & 0xF0) >> 4;
    int j = paramByte & 0xF;
    paramStringBuffer.append(arrayOfChar[i]);
    paramStringBuffer.append(arrayOfChar[j]);
  }

  private static String toHexString(byte[] paramArrayOfByte)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = paramArrayOfByte.length;
    for (int j = 0; j < i; j++)
    {
      byte2hex(paramArrayOfByte[j], localStringBuffer);
      if (j >= i - 1)
        continue;
      localStringBuffer.append(":");
    }
    return localStringBuffer.toString();
  }

  private static String getCertFingerPrint(String paramString, X509Certificate paramX509Certificate)
    throws Exception
  {
    byte[] arrayOfByte1 = paramX509Certificate.getEncoded();
    MessageDigest localMessageDigest = MessageDigest.getInstance(paramString);
    byte[] arrayOfByte2 = localMessageDigest.digest(arrayOfByte1);
    return toHexString(arrayOfByte2);
  }

  private static void showCertificateInfo(X509Certificate paramX509Certificate, JTable paramJTable, JTextArea paramJTextArea)
  {
    String str1 = "V" + paramX509Certificate.getVersion();
    String str2 = "[xxxxx-xxxxx]";
    String str3 = null;
    String str4 = null;
    try
    {
      SerialNumber localSerialNumber = new SerialNumber(paramX509Certificate.getSerialNumber());
      str2 = "[" + localSerialNumber.getNumber() + "]";
      str3 = getCertFingerPrint("MD5", paramX509Certificate);
      str4 = getCertFingerPrint("SHA1", paramX509Certificate);
    }
    catch (Throwable localThrowable)
    {
    }
    String str5 = "[" + paramX509Certificate.getSigAlgName() + "]";
    String str6 = formatDNString(paramX509Certificate.getIssuerDN().toString());
    String str7 = "[From: " + paramX509Certificate.getNotBefore() + ",\n To: " + paramX509Certificate.getNotAfter() + "]";
    String str8 = formatDNString(paramX509Certificate.getSubjectDN().toString());
    HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
    String str9 = localHexDumpEncoder.encodeBuffer(paramX509Certificate.getSignature());
    Object[][] arrayOfObject; = { { getMessage("cert.dialog.field.Version"), str1 }, { getMessage("cert.dialog.field.SerialNumber"), str2 }, { getMessage("cert.dialog.field.SignatureAlg"), str5 }, { getMessage("cert.dialog.field.Issuer"), str6 }, { getMessage("cert.dialog.field.Validity"), str7 }, { getMessage("cert.dialog.field.Subject"), str8 }, { getMessage("cert.dialog.field.Signature"), str9 }, { getMessage("cert.dialog.field.md5Fingerprint"), str3 }, { getMessage("cert.dialog.field.sha1Fingerprint"), str4 } };
    String[] arrayOfString = { getMessage("cert.dialog.field"), getMessage("cert.dialog.value") };
    paramJTable.setModel(new DefaultTableModel(arrayOfObject;, arrayOfString)
    {
      public boolean isCellEditable(int paramInt1, int paramInt2)
      {
        return false;
      }
    });
    paramJTable.setRowSelectionInterval(8, 8);
    paramJTable.repaint();
    paramJTextArea.repaint();
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  private static int getAcceleratorKey(String paramString)
  {
    return ResourceManager.getAcceleratorKey(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CertificateDialog
 * JD-Core Version:    0.6.0
 */