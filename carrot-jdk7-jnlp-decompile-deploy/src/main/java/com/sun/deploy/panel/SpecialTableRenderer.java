package com.sun.deploy.panel;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class SpecialTableRenderer extends DefaultTableCellRenderer
{
  public Component getTableCellRendererComponent(JTable paramJTable, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
  {
    Component localComponent = super.getTableCellRendererComponent(paramJTable, paramObject, paramBoolean1, paramBoolean2, paramInt1, paramInt2);
    if ((localComponent instanceof JComponent))
    {
      String str = null;
      if ((paramObject != null) && (!paramObject.toString().trim().equals("")))
        str = paramObject.toString();
      ((JComponent)localComponent).setToolTipText(str);
    }
    return localComponent;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.SpecialTableRenderer
 * JD-Core Version:    0.6.0
 */