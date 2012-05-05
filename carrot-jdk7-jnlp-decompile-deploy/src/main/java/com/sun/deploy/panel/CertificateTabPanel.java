package com.sun.deploy.panel;

import com.sun.deploy.security.CertUtils;
import java.awt.BorderLayout;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

class CertificateTabPanel extends JPanel
{
  private JTable certsTable;
  int certLevel;

  CertificateTabPanel(CertificatesInfo paramCertificatesInfo, int paramInt)
  {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    this.certLevel = paramInt;
    JScrollPane localJScrollPane = new JScrollPane();
    localJScrollPane.setHorizontalScrollBarPolicy(30);
    localJScrollPane.setVerticalScrollBarPolicy(22);
    localJScrollPane.setAutoscrolls(true);
    Collection localCollection = paramCertificatesInfo.getTrustedCertificates(paramInt);
    ReadOnlyTableModel localReadOnlyTableModel = null;
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
        X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
        localReadOnlyTableModel.setValueAt(CertUtils.extractSubjectAliasName(localX509Certificate), i, 0);
        localReadOnlyTableModel.setValueAt(CertUtils.extractIssuerAliasName(localX509Certificate), i, 1);
        i++;
      }
    }
    this.certsTable = new JTable(localReadOnlyTableModel);
    this.certsTable.getTableHeader().setFocusable(false);
    DefaultTableCellRenderer localDefaultTableCellRenderer = new DefaultTableCellRenderer();
    localDefaultTableCellRenderer.putClientProperty("html.disable", Boolean.TRUE);
    this.certsTable.setDefaultRenderer(Object.class, localDefaultTableCellRenderer);
    this.certsTable.setDragEnabled(false);
    this.certsTable.setColumnSelectionAllowed(false);
    if ((localCollection != null) && (localCollection.size() > 0))
      this.certsTable.setRowSelectionInterval(0, 0);
    localJScrollPane.setViewportView(this.certsTable);
    add(localJScrollPane, "Center");
  }

  void setCertificateTableModel(TableModel paramTableModel)
  {
    this.certsTable.setModel(paramTableModel);
    this.certsTable.setDragEnabled(false);
    if (this.certLevel == 0)
      this.certsTable.setSelectionMode(2);
    else
      this.certsTable.setSelectionMode(0);
    this.certsTable.setColumnSelectionAllowed(false);
    if (paramTableModel.getRowCount() > 0)
      this.certsTable.setRowSelectionInterval(0, 0);
    this.certsTable.updateUI();
  }

  public void registerSelectionListener(ListSelectionListener paramListSelectionListener)
  {
    if (this.certsTable != null)
    {
      ListSelectionModel localListSelectionModel = this.certsTable.getSelectionModel();
      localListSelectionModel.addListSelectionListener(paramListSelectionListener);
    }
  }

  int getSelectedCertificateTableRow()
  {
    return this.certsTable.getSelectedRow();
  }

  int[] getSelectedCertificateTableRows()
  {
    return this.certsTable.getSelectedRows();
  }

  boolean isCertificateSelected()
  {
    return getSelectedCertificateTableRow() != -1;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.CertificateTabPanel
 * JD-Core Version:    0.6.0
 */