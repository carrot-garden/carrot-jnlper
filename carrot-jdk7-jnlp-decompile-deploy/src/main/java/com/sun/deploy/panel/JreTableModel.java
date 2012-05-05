package com.sun.deploy.panel;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class JreTableModel extends AbstractTableModel
{
  private ArrayList _jres = new ArrayList();
  private ArrayList _validPaths = new ArrayList();
  private String[] _columnNames;
  private boolean _system;
  private HashSet _hidden;
  private boolean dirty = false;

  public JreTableModel(boolean paramBoolean)
  {
    this(new String[] { ResourceManager.getMessage("controlpanel.jre.platformTableColumnTitle"), ResourceManager.getMessage("controlpanel.jre.productTableColumnTitle"), ResourceManager.getMessage("controlpanel.jre.locationTableColumnTitle"), ResourceManager.getMessage("controlpanel.jre.pathTableColumnTitle"), ResourceManager.getMessage("controlpanel.jre.vmargsTableColumnTitle"), ResourceManager.getMessage("controlpanel.jre.enabledTableColumnTitle") }, paramBoolean);
  }

  private JreTableModel(String[] paramArrayOfString, boolean paramBoolean)
  {
    this._columnNames = paramArrayOfString;
    this._system = paramBoolean;
    this._hidden = new HashSet();
    refresh();
    if (!paramBoolean)
      addTableModelListener(new TableModelListener()
      {
        public void tableChanged(TableModelEvent paramTableModelEvent)
        {
          JreTableModel.access$002(JreTableModel.this, true);
        }
      });
  }

  private void refresh()
  {
    Config.get().refreshIfNeeded();
    JREInfo[] arrayOfJREInfo = JREInfo.getAll();
    this._jres.clear();
    this._validPaths.clear();
    this._hidden.clear();
    Trace.println("refresh for " + (this._system ? "system" : "user") + " JREs", TraceLevel.BASIC);
    for (int i = 0; i < arrayOfJREInfo.length; i++)
    {
      JREInfo localJREInfo = arrayOfJREInfo[i];
      if (localJREInfo.isSystemJRE() != this._system)
        continue;
      if ((localJREInfo.getOSName() == null) || (localJREInfo.getOSArch() == null) || (localJREInfo.isOsInfoMatch()))
      {
        if ((localJREInfo.getPath() != null) && (JREInfo.isValidJREPath(localJREInfo.getPath())))
          add(new JREInfo(localJREInfo), true, false);
        else
          this._hidden.add(new JREInfo(localJREInfo));
      }
      else
        this._hidden.add(new JREInfo(localJREInfo));
    }
    fireTableDataChanged();
  }

  HashSet getHiddenJREs()
  {
    return this._hidden;
  }

  public Object getValueAt(int paramInt1, int paramInt2)
  {
    switch (paramInt2)
    {
    case 0:
      return getJRE(paramInt1).getPlatform();
    case 1:
      return getJRE(paramInt1).getProduct();
    case 2:
      return getJRE(paramInt1).getLocation();
    case 3:
      return getJRE(paramInt1).getPath();
    case 4:
      return getJRE(paramInt1).getVmArgs();
    }
    return new Boolean(getJRE(paramInt1).isEnabled());
  }

  public boolean isCellEditable(int paramInt1, int paramInt2)
  {
    return !getJRE(paramInt1).isSystemJRE();
  }

  public Class getColumnClass(int paramInt)
  {
    if (paramInt < 5)
      return String.class;
    return Boolean.class;
  }

  void add(JREInfo paramJREInfo, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramJREInfo.isSystemJRE() == this._system)
    {
      if (Trace.isEnabled(TraceLevel.TEMP))
        Trace.println("Table model adding jre: " + paramJREInfo, TraceLevel.TEMP);
      this._jres.add(paramJREInfo);
      if (paramBoolean1)
        this._validPaths.add(Boolean.TRUE);
      else
        this._validPaths.add(null);
      if (paramBoolean2)
        fireTableRowsInserted(this._jres.size() - 1, this._jres.size() - 1);
    }
  }

  public void setValueAt(Object paramObject, int paramInt1, int paramInt2)
  {
    if (paramInt1 >= this._jres.size())
      return;
    JREInfo localJREInfo = getJRE(paramInt1);
    switch (paramInt2)
    {
    case 0:
      String str = (String)paramObject;
      if ((str == null) || (str.equals("")))
        break;
      this._jres.set(paramInt1, new JREInfo((String)paramObject, localJREInfo.getProduct(), localJREInfo.getLocation(), localJREInfo.getPath(), localJREInfo.getVmArgs(), localJREInfo.getOSName(), localJREInfo.getOSArch(), localJREInfo.isEnabled(), localJREInfo.isRegistered()));
      break;
    case 1:
      this._jres.set(paramInt1, new JREInfo(localJREInfo.getPlatform(), (String)paramObject, localJREInfo.getLocation(), localJREInfo.getPath(), localJREInfo.getVmArgs(), localJREInfo.getOSName(), localJREInfo.getOSArch(), localJREInfo.isEnabled(), localJREInfo.isRegistered()));
      break;
    case 2:
      this._jres.set(paramInt1, new JREInfo(localJREInfo.getPlatform(), localJREInfo.getProduct(), (String)paramObject, localJREInfo.getPath(), localJREInfo.getVmArgs(), localJREInfo.getOSName(), localJREInfo.getOSArch(), localJREInfo.isEnabled(), localJREInfo.isRegistered()));
      break;
    case 3:
      this._jres.set(paramInt1, new JREInfo(localJREInfo.getPlatform(), localJREInfo.getProduct(), localJREInfo.getLocation(), (String)paramObject, localJREInfo.getVmArgs(), localJREInfo.getOSName(), localJREInfo.getOSArch(), localJREInfo.isEnabled(), localJREInfo.isRegistered()));
      this._validPaths.set(paramInt1, null);
      break;
    case 4:
      this._jres.set(paramInt1, new JREInfo(localJREInfo.getPlatform(), localJREInfo.getProduct(), localJREInfo.getLocation(), localJREInfo.getPath(), (String)paramObject, localJREInfo.getOSName(), localJREInfo.getOSArch(), localJREInfo.isEnabled(), localJREInfo.isRegistered()));
      break;
    default:
      this._jres.set(paramInt1, new JREInfo(localJREInfo.getPlatform(), localJREInfo.getProduct(), localJREInfo.getLocation(), localJREInfo.getPath(), localJREInfo.getVmArgs(), localJREInfo.getOSName(), localJREInfo.getOSArch(), ((Boolean)paramObject).booleanValue(), localJREInfo.isRegistered()));
    }
    fireTableRowsUpdated(paramInt1, paramInt1);
  }

  public int getColumnCount()
  {
    return this._columnNames.length;
  }

  public int getRowCount()
  {
    return this._jres.size();
  }

  JREInfo getJRE(int paramInt)
  {
    return (JREInfo)this._jres.get(paramInt);
  }

  public String getColumnName(int paramInt)
  {
    return this._columnNames[paramInt];
  }

  void validateAndSave()
  {
    JREInfo localJREInfo;
    if ((!this._system) && (this.dirty))
    {
      if (getRowCount() > 0)
      {
        JREInfo.clear();
        for (int i = 0; i < getRowCount(); i++)
        {
          if (!isPathValid(i))
            continue;
          JREInfo.addJRE(validatePlatform(i));
        }
        Iterator localIterator1 = getHiddenJREs().iterator();
        while (localIterator1.hasNext())
        {
          localJREInfo = (JREInfo)localIterator1.next();
          JREInfo.addJRE(localJREInfo);
        }
      }
    }
    else if (this._system == true)
    {
      if (getRowCount() > 0)
        for (int j = 0; j < getRowCount(); j++)
          JREInfo.addJRE(getJRE(j));
      Iterator localIterator2 = getHiddenJREs().iterator();
      while (localIterator2.hasNext())
      {
        localJREInfo = (JREInfo)localIterator2.next();
        JREInfo.addJRE(localJREInfo);
      }
    }
  }

  boolean isPathValid(int paramInt)
  {
    Boolean localBoolean = (Boolean)this._validPaths.get(paramInt);
    if (localBoolean == null)
    {
      if (JREInfo.isValidJREPath(getJRE(paramInt).getPath()))
        localBoolean = Boolean.TRUE;
      else
        localBoolean = Boolean.FALSE;
      this._validPaths.set(paramInt, localBoolean);
    }
    return Boolean.TRUE.equals(localBoolean);
  }

  private JREInfo validatePlatform(int paramInt)
  {
    JREInfo localJREInfo1 = getJRE(paramInt);
    if (localJREInfo1.getPlatform() == null)
    {
      String str = JREInfo.getPlatformByProduct(localJREInfo1.getProduct());
      if (str != null)
      {
        localJREInfo1.setPlatform(str);
      }
      else
      {
        JREInfo localJREInfo2 = null;
        try
        {
          localJREInfo2 = JreLocator.getVersion(new File(localJREInfo1.getPath()));
        }
        catch (Exception localException)
        {
          Trace.ignored(localException);
        }
        if (localJREInfo2 != null)
        {
          localJREInfo1.setProduct(localJREInfo2.getProduct());
          localJREInfo1.setPlatform(localJREInfo2.getPlatform());
        }
      }
    }
    return localJREInfo1;
  }

  void remove(int[] paramArrayOfInt)
  {
    if (paramArrayOfInt != null)
    {
      int i = getRowCount();
      for (int j = paramArrayOfInt.length - 1; j >= 0; j--)
      {
        if ((paramArrayOfInt[j] == -1) || (paramArrayOfInt[j] >= i))
          continue;
        this._jres.remove(paramArrayOfInt[j]);
        this._validPaths.remove(paramArrayOfInt[j]);
      }
    }
    fireTableDataChanged();
  }

  boolean isSystem()
  {
    return this._system;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.JreTableModel
 * JD-Core Version:    0.6.0
 */