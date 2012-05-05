package com.sun.deploy.panel;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class PathRenderer extends DefaultTableCellRenderer
{
  private Border badBorder = new LineBorder(Color.red);
  private Border goodBorder = new LineBorder(Color.black);

  public Component getTableCellRendererComponent(JTable paramJTable, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
  {
    Component localComponent = super.getTableCellRendererComponent(paramJTable, paramObject, paramBoolean1, paramBoolean2, paramInt1, paramInt2);
    if (((localComponent instanceof JComponent)) && ((paramJTable.getModel() instanceof JreTableModel)) && (!((JreTableModel)paramJTable.getModel()).isPathValid(paramInt1)))
      ((JComponent)localComponent).setBorder(this.badBorder);
    else if ((localComponent instanceof JComponent))
      ((JComponent)localComponent).setBorder(this.goodBorder);
    if ((localComponent instanceof JComponent))
    {
      String str = null;
      if (paramObject != null)
        str = paramObject.toString();
      ((JComponent)localComponent).setToolTipText(str);
    }
    return localComponent;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.PathRenderer
 * JD-Core Version:    0.6.0
 */