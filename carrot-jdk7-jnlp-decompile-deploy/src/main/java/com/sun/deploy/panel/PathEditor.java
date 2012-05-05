package com.sun.deploy.panel;

import com.sun.deploy.config.JREInfo;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;
import javax.swing.text.Document;

public class PathEditor extends DefaultCellEditor
  implements DocumentListener
{
  private int row;
  private Border badBorder = new LineBorder(Color.red);
  private Border goodBorder = new LineBorder(Color.black);
  private Border currentBorder = this.badBorder;
  private JTable table;

  PathEditor()
  {
    super(new JTextField());
    ((JTextField)this.editorComponent).getDocument().addDocumentListener(this);
  }

  public Component getTableCellEditorComponent(JTable paramJTable, Object paramObject, boolean paramBoolean, int paramInt1, int paramInt2)
  {
    paramJTable.getTableHeader().setResizingAllowed(false);
    this.row = paramInt1;
    this.table = paramJTable;
    super.getTableCellEditorComponent(paramJTable, paramObject, paramBoolean, paramInt1, paramInt2);
    this.currentBorder = (((JreTableModel)paramJTable.getModel()).isPathValid(paramInt1) ? this.goodBorder : this.badBorder);
    this.editorComponent.setBorder(this.currentBorder);
    return this.editorComponent;
  }

  public void insertUpdate(DocumentEvent paramDocumentEvent)
  {
    updateBorderFromEditor();
  }

  public void removeUpdate(DocumentEvent paramDocumentEvent)
  {
    updateBorderFromEditor();
  }

  public void changedUpdate(DocumentEvent paramDocumentEvent)
  {
  }

  private void updateBorderFromEditor()
  {
    Object localObject = getCellEditorValue();
    boolean bool;
    if (((localObject instanceof String)) && ((this.table.getModel() instanceof JreTableModel)))
      bool = JREInfo.isValidJREPath((String)localObject);
    else
      bool = false;
    if (bool)
      this.editorComponent.setBorder(this.goodBorder);
    else
      this.editorComponent.setBorder(this.badBorder);
  }

  public boolean stopCellEditing()
  {
    this.table.getTableHeader().setResizingAllowed(true);
    return super.stopCellEditing();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.PathEditor
 * JD-Core Version:    0.6.0
 */