package com.sun.deploy.panel;

import com.sun.deploy.resources.ResourceManager;
import javax.swing.table.DefaultTableModel;

class ReadOnlyTableModel extends DefaultTableModel
{
  ReadOnlyTableModel()
  {
    super(new Object[] { getMessage("cert.dialog.issued.to"), getMessage("cert.dialog.issued.by") }, 0);
  }

  ReadOnlyTableModel(int paramInt)
  {
    super(new Object[] { getMessage("cert.dialog.issued.to"), getMessage("cert.dialog.issued.by") }, paramInt);
  }

  public boolean isCellEditable(int paramInt1, int paramInt2)
  {
    return false;
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.ReadOnlyTableModel
 * JD-Core Version:    0.6.0
 */