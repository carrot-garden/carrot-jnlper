package com.sun.javaws.ui;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.CacheUtil;
import com.sun.javaws.jnl.LaunchDesc;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

class CacheTable extends JTable
{
  private static final TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
  static final int JNLP_ROW_HEIGHT = 36;
  static final int RESOURCE_ROW_HEIGHT = 26;
  static final int JNLP_TYPE = 0;
  static final int RESOURCE_TYPE = 1;
  static final int DELETED_TYPE = 2;
  private final CacheViewer viewer;
  private final int tableType;
  private final boolean isSystem;

  public CacheTable(CacheViewer paramCacheViewer, int paramInt, boolean paramBoolean)
  {
    this.viewer = paramCacheViewer;
    this.tableType = paramInt;
    this.isSystem = paramBoolean;
    setShowGrid(false);
    setIntercellSpacing(new Dimension(0, 0));
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    int i = this.tableType == 0 ? 36 : 26;
    setRowHeight(i);
    setPreferredScrollableViewportSize(new Dimension(640, 280));
    addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent paramMouseEvent)
      {
        if (paramMouseEvent.isPopupTrigger())
        {
          int i = paramMouseEvent.getY();
          int j = i / CacheTable.this.getRowHeight();
          if (j < CacheTable.this.getModel().getRowCount())
          {
            CacheTable.this.getSelectionModel().clearSelection();
            CacheTable.this.getSelectionModel().addSelectionInterval(j, j);
            CacheTable.this.viewer.popupApplicationMenu(CacheTable.this, paramMouseEvent.getX(), i);
          }
        }
      }

      public void mouseReleased(MouseEvent paramMouseEvent)
      {
        if (paramMouseEvent.isPopupTrigger())
        {
          int i = paramMouseEvent.getY();
          int j = i / CacheTable.this.getRowHeight();
          if (j < CacheTable.this.getModel().getRowCount())
          {
            CacheTable.this.getSelectionModel().clearSelection();
            CacheTable.this.getSelectionModel().addSelectionInterval(j, j);
            CacheTable.this.viewer.popupApplicationMenu(CacheTable.this, paramMouseEvent.getX(), i);
          }
        }
      }

      public void mouseClicked(MouseEvent paramMouseEvent)
      {
        Point localPoint = paramMouseEvent.getPoint();
        if ((paramMouseEvent.getClickCount() == 2) && (CacheTable.this.getSelectedRows().length == 1) && (paramMouseEvent.getButton() == 1))
        {
          int i = CacheTable.this.getColumnModel().getColumnIndexAtX(localPoint.x);
          if ((CacheTable.this.getSelectedRow() == CacheTable.this.rowAtPoint(localPoint)) && (i < 3))
            if (CacheTable.this.tableType == 0)
              CacheTable.this.viewer.runApplication();
            else if (CacheTable.this.tableType == 2)
              CacheTable.this.viewer.importApplication();
            else
              CacheTable.this.viewer.showInformation();
        }
      }
    });
    addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent paramKeyEvent)
      {
        int i = paramKeyEvent.getKeyCode();
        int j = paramKeyEvent.getModifiers();
        int k;
        if ((i == 121) && ((j & 0x1) != 0))
        {
          k = CacheTable.this.getRowHeight() * CacheTable.this.getSelectedRow() + 6;
          int m = 100;
          if ((CacheTable.this.getModel() instanceof CacheTable.CacheTableModel))
          {
            CacheTable.CacheTableModel localCacheTableModel = (CacheTable.CacheTableModel)CacheTable.this.getModel();
            m = localCacheTableModel.getPreferredWidth(0);
          }
          int n = 2 * m / 3;
          CacheTable.this.viewer.popupApplicationMenu(CacheTable.this, n, k);
        }
        else if (i == 10)
        {
          k = (j & 0x2) == 0 ? 1 : 0;
          if (CacheTable.this.tableType == 0)
            CacheTable.this.viewer.runApplication(k);
          else if (CacheTable.this.tableType == 2)
            CacheTable.this.viewer.importApplication();
          else
            CacheTable.this.viewer.showInformation();
          paramKeyEvent.consume();
        }
        else if ((i == 127) || (i == 8))
        {
          CacheTable.this.viewer.delete();
        }
      }
    });
    reset();
  }

  public String getSizeLabelText()
  {
    long l1 = Cache.getCacheSize(this.isSystem);
    long l2 = getInstalledSize(this.isSystem);
    long l3 = l1 - l2;
    String str1 = getSizeString(l2);
    String str2 = getSizeString(l3);
    if (this.isSystem)
      return ResourceManager.getString("viewer.size.system", str1, str2);
    return ResourceManager.getString("viewer.size", str1, str2);
  }

  private String getSizeString(long paramLong)
  {
    String str;
    if (paramLong > 10240L)
      str = " " + paramLong / 1024L + " KB";
    else
      str = " " + paramLong / 1024L + "." + paramLong % 1024L / 102L + " KB";
    return str;
  }

  private long getInstalledSize(boolean paramBoolean)
  {
    ArrayList localArrayList = CacheUtil.getInstalledResources(paramBoolean);
    Iterator localIterator = localArrayList.iterator();
    long l = 0L;
    while (localIterator.hasNext())
    {
      CacheEntry localCacheEntry = Cache.getCacheEntryFromFile((File)localIterator.next());
      if (localCacheEntry != null)
        l += localCacheEntry.getSize();
    }
    return l;
  }

  public void reset()
  {
    getSelectionModel().clearSelection();
    getSelectionModel().removeListSelectionListener(this.viewer);
    TableModel localTableModel = getModel();
    if ((localTableModel instanceof CacheTableModel))
      ((CacheTableModel)localTableModel).removeMouseListenerFromHeaderInTable(this);
    CacheTableModel localCacheTableModel = new CacheTableModel(this.tableType, this.isSystem);
    setModel(localCacheTableModel);
    for (int i = 0; i < getModel().getColumnCount(); i++)
    {
      TableColumn localTableColumn = getColumnModel().getColumn(i);
      localTableColumn.setHeaderRenderer(new CacheTableHeaderRenderer(null));
      int j = localCacheTableModel.getPreferredWidth(i);
      localTableColumn.setPreferredWidth(j);
      localTableColumn.setMinWidth(j);
    }
    setDefaultRenderer(JLabel.class, localCacheTableModel);
    localCacheTableModel.addMouseListenerToHeaderInTable(this);
    getSelectionModel().addListSelectionListener(this.viewer);
    getSelectionModel().clearSelection();
  }

  public CacheObject getCacheObject(int paramInt)
  {
    return ((CacheTableModel)getModel()).getCacheObject(paramInt);
  }

  public String[] getAllHrefs()
  {
    ArrayList localArrayList = new ArrayList();
    TableModel localTableModel = getModel();
    if ((localTableModel instanceof CacheTableModel))
      for (int i = 0; i < localTableModel.getRowCount(); i++)
      {
        String str = ((CacheTableModel)localTableModel).getRowHref(i);
        if (str == null)
          continue;
        localArrayList.add(str);
      }
    return (String[])(String[])localArrayList.toArray(new String[0]);
  }

  public boolean getScrollableTracksViewportHeight()
  {
    if ((getParent() instanceof JViewport))
      return ((JViewport)getParent()).getHeight() > getPreferredSize().height;
    return false;
  }

  private class CacheTableHeaderRenderer extends DefaultTableCellRenderer
  {
    private final CacheTable this$0;

    private CacheTableHeaderRenderer()
    {
      this.this$0 = this$1;
    }

    public Component getTableCellRendererComponent(JTable paramJTable, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
    {
      if (paramJTable != null)
      {
        localObject = paramJTable.getTableHeader();
        if (localObject != null)
        {
          setForeground(((JTableHeader)localObject).getForeground());
          setBackground(((JTableHeader)localObject).getBackground());
          setFont(((JTableHeader)localObject).getFont());
        }
      }
      setText(paramObject == null ? "" : paramObject.toString());
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      setHorizontalAlignment(0);
      Object localObject = CacheObject.getHeaderToolTipText(paramInt2, this.this$0.tableType);
      if ((localObject != null) && (((String)localObject).length() > 0))
        setToolTipText((String)localObject);
      return (Component)this;
    }

    CacheTableHeaderRenderer(CacheTable.1 arg2)
    {
      this();
    }
  }

  private class CacheTableModel extends AbstractTableModel
    implements TableCellRenderer
  {
    private boolean isSystem;
    private int tableType;
    private CacheObject[] rows;
    private int sortColumn;
    private boolean sortAscending;
    private MouseListener mouseListener = null;

    public CacheTableModel(int paramBoolean, boolean arg3)
    {
      this.tableType = paramBoolean;
      boolean bool;
      this.isSystem = bool;
      this.rows = new CacheObject[0];
      this.sortColumn = -1;
      this.sortAscending = true;
      refresh();
      fireTableDataChanged();
    }

    public Component getTableCellRendererComponent(JTable paramJTable, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
    {
      if ((paramObject instanceof Component))
      {
        Component localComponent = (Component)paramObject;
        if (paramBoolean1)
        {
          localComponent.setForeground(paramJTable.getSelectionForeground());
          localComponent.setBackground(paramJTable.getSelectionBackground());
        }
        else
        {
          localComponent.setForeground(paramJTable.getForeground());
          localComponent.setBackground(paramJTable.getBackground());
        }
        CacheObject.hasFocus(localComponent, paramBoolean2);
        return localComponent;
      }
      return CacheTable.defaultRenderer.getTableCellRendererComponent(paramJTable, paramObject, paramBoolean1, paramBoolean2, paramInt1, paramInt2);
    }

    private boolean isEntryIPEqual(CacheObject paramCacheObject1, CacheObject paramCacheObject2)
    {
      String str1 = paramCacheObject1.getCodebaseIP();
      String str2 = paramCacheObject2.getCodebaseIP();
      return ((str1 == null) && (str2 == null)) || ((str1 != null) && (str1.equals(str2))) || ((str2 != null) && (str2.equals(str1)));
    }

    private boolean validateEntry(ArrayList paramArrayList, CacheObject paramCacheObject)
    {
      for (int i = 0; i < paramArrayList.size(); i++)
      {
        CacheObject localCacheObject = (CacheObject)paramArrayList.get(i);
        if ((localCacheObject.getUrlString().equals(paramCacheObject.getUrlString())) && (!isEntryIPEqual(localCacheObject, paramCacheObject)))
          return false;
      }
      return true;
    }

    public void refresh()
    {
      Cache.removeDuplicateEntries(this.isSystem, false);
      ArrayList localArrayList = new ArrayList();
      Object localObject1;
      Object localObject2;
      Object localObject3;
      if (this.tableType == 0)
      {
        localObject1 = Cache.getJnlpCacheEntries(this.isSystem).iterator();
        while (((Iterator)localObject1).hasNext())
        {
          File localFile = (File)((Iterator)localObject1).next();
          localObject2 = new File(localFile.getPath() + ".idx");
          localObject3 = Cache.getCacheEntryFromFile((File)localObject2);
          if ((localObject3 != null) && (((CacheEntry)localObject3).isValidEntry()))
          {
            CacheObject localCacheObject = new CacheObject((CacheEntry)localObject3, this, this.tableType);
            LaunchDesc localLaunchDesc = localCacheObject.getLaunchDesc();
            if ((localLaunchDesc != null) && (localLaunchDesc.isApplicationDescriptor()) && (validateEntry(localArrayList, localCacheObject)))
              localArrayList.add(localCacheObject);
          }
        }
      }
      else if (this.tableType == 1)
      {
        localObject1 = Cache.getCacheEntries(this.isSystem);
        for (int i = 0; i < localObject1.length; i++)
        {
          localObject2 = Cache.getCacheEntryFromFile(localObject1[i]);
          if ((localObject2 == null) || (!((CacheEntry)localObject2).isValidEntry()))
            continue;
          localObject3 = new CacheObject((CacheEntry)localObject2, this, this.tableType);
          if (!validateEntry(localArrayList, (CacheObject)localObject3))
            continue;
          localArrayList.add(localObject3);
        }
      }
      else if (this.tableType == 2)
      {
        localObject1 = Cache.getRemovedApps();
        Enumeration localEnumeration = ((Properties)localObject1).propertyNames();
        while (localEnumeration.hasMoreElements())
        {
          localObject2 = (String)localEnumeration.nextElement();
          localObject3 = ((Properties)localObject1).getProperty((String)localObject2);
          localArrayList.add(new CacheObject((String)localObject3, (String)localObject2, this));
        }
      }
      this.rows = ((CacheObject[])(CacheObject[])localArrayList.toArray(new CacheObject[0]));
      if (this.sortColumn != -1)
        sort();
    }

    CacheObject getCacheObject(int paramInt)
    {
      return this.rows[paramInt];
    }

    public Object getValueAt(int paramInt1, int paramInt2)
    {
      return this.rows[paramInt1].getObject(paramInt2);
    }

    public int getRowCount()
    {
      return this.rows.length;
    }

    public String getRowHref(int paramInt)
    {
      return this.rows[paramInt].getHref();
    }

    public int getColumnCount()
    {
      return CacheObject.getColumnCount(this.tableType);
    }

    public boolean isCellEditable(int paramInt1, int paramInt2)
    {
      return this.rows[paramInt1].isEditable(paramInt2);
    }

    public Class getColumnClass(int paramInt)
    {
      return CacheObject.getClass(paramInt, this.tableType);
    }

    public String getColumnName(int paramInt)
    {
      return CacheObject.getColumnName(paramInt, this.tableType);
    }

    public void setValueAt(Object paramObject, int paramInt1, int paramInt2)
    {
      this.rows[paramInt1].setValue(paramInt2, paramObject);
    }

    public int getPreferredWidth(int paramInt)
    {
      return CacheObject.getPreferredWidth(paramInt, this.tableType);
    }

    public void removeMouseListenerFromHeaderInTable(JTable paramJTable)
    {
      if (this.mouseListener != null)
        paramJTable.getTableHeader().removeMouseListener(this.mouseListener);
    }

    public void addMouseListenerToHeaderInTable(JTable paramJTable)
    {
      JTable localJTable = paramJTable;
      localJTable.setColumnSelectionAllowed(false);
      ListSelectionModel localListSelectionModel = localJTable.getSelectionModel();
      this.mouseListener = new MouseAdapter(localJTable, localListSelectionModel)
      {
        private final JTable val$tableView;
        private final ListSelectionModel val$lsm;

        public void mouseClicked(MouseEvent paramMouseEvent)
        {
          TableColumnModel localTableColumnModel = this.val$tableView.getColumnModel();
          int i = localTableColumnModel.getColumnIndexAtX(paramMouseEvent.getX());
          int j = this.val$lsm.getMinSelectionIndex();
          this.val$lsm.clearSelection();
          int k = this.val$tableView.convertColumnIndexToModel(i);
          if ((paramMouseEvent.getClickCount() == 1) && (k >= 0))
          {
            int m = paramMouseEvent.getModifiers() & 0x1;
            CacheTable.CacheTableModel.access$402(CacheTable.CacheTableModel.this, m == 0);
            CacheTable.CacheTableModel.access$502(CacheTable.CacheTableModel.this, k);
            CacheTable.CacheTableModel.this.runSort(this.val$lsm, j);
          }
        }
      };
      localJTable.getTableHeader().addMouseListener(this.mouseListener);
    }

    public void sort()
    {
      int i = 0;
      int j;
      int k;
      CacheObject localCacheObject;
      if (this.sortAscending)
        for (j = 0; j < getRowCount(); j++)
          for (k = j + 1; k < getRowCount(); k++)
          {
            if (this.rows[j].compareColumns(this.rows[k], this.sortColumn) <= 0)
              continue;
            i = 1;
            localCacheObject = this.rows[j];
            this.rows[j] = this.rows[k];
            this.rows[k] = localCacheObject;
          }
      else
        for (j = 0; j < getRowCount(); j++)
          for (k = j + 1; k < getRowCount(); k++)
          {
            if (this.rows[k].compareColumns(this.rows[j], this.sortColumn) <= 0)
              continue;
            i = 1;
            localCacheObject = this.rows[j];
            this.rows[j] = this.rows[k];
            this.rows[k] = localCacheObject;
          }
      if (i != 0)
        fireTableDataChanged();
    }

    private void runSort(ListSelectionModel paramListSelectionModel, int paramInt)
    {
      new Thread(new Runnable(paramInt, paramListSelectionModel)
      {
        private final int val$selected;
        private final ListSelectionModel val$lsm;

        public void run()
        {
          try
          {
            CacheObject localCacheObject = null;
            if (this.val$selected >= 0)
              localCacheObject = CacheTable.CacheTableModel.this.rows[this.val$selected];
            CacheTable.CacheTableModel.this.sort();
            if (localCacheObject != null)
              for (int i = 0; i < CacheTable.CacheTableModel.this.rows.length; i++)
              {
                if (CacheTable.CacheTableModel.this.rows[i] != localCacheObject)
                  continue;
                this.val$lsm.clearSelection();
                this.val$lsm.addSelectionInterval(i, i);
                break;
              }
          }
          finally
          {
          }
        }
      }).start();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.CacheTable
 * JD-Core Version:    0.6.0
 */