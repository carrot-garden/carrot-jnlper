package com.sun.deploy.panel;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

public class TreeEditors
{
  private static DefaultTreeCellEditor _radioEditor = null;
  private static DefaultTreeCellEditor _checkBoxEditor = null;
  private static DefaultTreeCellEditor _textFieldEditor = null;
  private static TreeEditors _instance = null;

  private static TreeEditors getInstance()
  {
    if (_instance == null)
      _instance = new TreeEditors();
    return _instance;
  }

  private DefaultTreeCellEditor getEditor(JTree paramJTree, IProperty paramIProperty)
  {
    DefaultTreeCellRenderer localDefaultTreeCellRenderer = TreeRenderers.getRenderer();
    if ((paramIProperty instanceof RadioProperty))
    {
      if (_radioEditor == null)
        _radioEditor = new RadioEditor(paramJTree, localDefaultTreeCellRenderer);
      return _radioEditor;
    }
    if ((paramIProperty instanceof ToggleProperty))
    {
      if (_checkBoxEditor == null)
        _checkBoxEditor = new CheckBoxEditor(paramJTree, localDefaultTreeCellRenderer);
      return _checkBoxEditor;
    }
    if ((paramIProperty instanceof TextFieldProperty))
    {
      if (_textFieldEditor == null)
        _textFieldEditor = new TextFieldEditor(paramJTree, localDefaultTreeCellRenderer);
      return _textFieldEditor;
    }
    return null;
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  private class CheckBoxEditor extends TreeEditors.DeployEditor
  {
    private JCheckBox cb = new JCheckBox();

    public CheckBoxEditor(JTree paramDefaultTreeCellRenderer, DefaultTreeCellRenderer arg3)
    {
      super(paramDefaultTreeCellRenderer, localDefaultTreeCellRenderer);
      this.cb.addActionListener(new ActionListener(TreeEditors.this)
      {
        private final TreeEditors val$this$0;

        public void actionPerformed(ActionEvent paramActionEvent)
        {
          TreeEditors.CheckBoxEditor.this.editingStopped();
        }
      });
    }

    public Component getTreeCellEditorComponent(JTree paramJTree, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt)
    {
      IProperty localIProperty = (IProperty)paramObject;
      this.cb.setSelected(localIProperty.isSelected());
      this.cb.setText(localIProperty.getDescription());
      this.cb.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      return this.cb;
    }

    public Object getCellEditorValue()
    {
      return this.cb.isSelected() ? "true" : "false";
    }
  }

  public static final class DelegateEditor
    implements TreeCellEditor
  {
    private Vector vListeners = new Vector();
    private TreeCellEditor currentEditor;
    private JTree tree;
    private CellEditorListener listener = new CellEditorListener()
    {
      public void editingStopped(ChangeEvent paramChangeEvent)
      {
        Vector localVector = (Vector)TreeEditors.DelegateEditor.this.vListeners.clone();
        Iterator localIterator = localVector.iterator();
        while (localIterator.hasNext())
          ((CellEditorListener)localIterator.next()).editingStopped(paramChangeEvent);
      }

      public void editingCanceled(ChangeEvent paramChangeEvent)
      {
        Vector localVector = (Vector)TreeEditors.DelegateEditor.this.vListeners.clone();
        Iterator localIterator = localVector.iterator();
        while (localIterator.hasNext())
          ((CellEditorListener)localIterator.next()).editingCanceled(paramChangeEvent);
      }
    };

    public DelegateEditor(JTree paramJTree)
    {
      this.tree = paramJTree;
    }

    public Component getTreeCellEditorComponent(JTree paramJTree, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt)
    {
      return this.currentEditor.getTreeCellEditorComponent(paramJTree, paramObject, paramBoolean1, paramBoolean2, paramBoolean3, paramInt);
    }

    public void addCellEditorListener(CellEditorListener paramCellEditorListener)
    {
      this.vListeners.add(paramCellEditorListener);
    }

    public void removeCellEditorListener(CellEditorListener paramCellEditorListener)
    {
      this.vListeners.remove(paramCellEditorListener);
    }

    public Object getCellEditorValue()
    {
      return this.currentEditor != null ? this.currentEditor.getCellEditorValue() : null;
    }

    public boolean isCellEditable(EventObject paramEventObject)
    {
      setCurrentEditor(paramEventObject);
      return this.currentEditor != null;
    }

    public boolean shouldSelectCell(EventObject paramEventObject)
    {
      return this.currentEditor == null ? true : this.currentEditor.shouldSelectCell(paramEventObject);
    }

    public boolean stopCellEditing()
    {
      return this.currentEditor != null ? this.currentEditor.stopCellEditing() : true;
    }

    public void cancelCellEditing()
    {
      if (this.currentEditor != null)
        this.currentEditor.cancelCellEditing();
    }

    private void setCurrentEditor(EventObject paramEventObject)
    {
      TreeCellEditor localTreeCellEditor = pickEditor(paramEventObject);
      if (this.currentEditor != null)
        this.currentEditor.removeCellEditorListener(this.listener);
      this.currentEditor = localTreeCellEditor;
      if (this.currentEditor != null)
        this.currentEditor.addCellEditorListener(this.listener);
    }

    private TreeCellEditor pickEditor(EventObject paramEventObject)
    {
      DefaultTreeCellEditor localDefaultTreeCellEditor = null;
      Object localObject1;
      Object localObject2;
      if ((paramEventObject instanceof MouseEvent))
      {
        localObject1 = (MouseEvent)paramEventObject;
        localObject2 = this.tree.getPathForLocation(((MouseEvent)localObject1).getX(), ((MouseEvent)localObject1).getY());
        if ((((TreePath)localObject2).getLastPathComponent() instanceof IProperty))
        {
          IProperty localIProperty = (IProperty)((TreePath)localObject2).getLastPathComponent();
          String str = localIProperty.getPropertyName();
          if ((localIProperty instanceof RadioProperty))
            str = ((RadioProperty)localIProperty).getGroupName();
          if (!Config.get().isPropertyLocked(str))
            localDefaultTreeCellEditor = TreeEditors.access$000().getEditor(this.tree, localIProperty);
        }
      }
      else
      {
        localObject1 = this.tree.getSelectionPath();
        if ((((TreePath)localObject1).getLastPathComponent() instanceof TextFieldProperty))
        {
          localObject2 = (IProperty)((TreePath)localObject1).getLastPathComponent();
          if (!Config.get().isPropertyLocked(((IProperty)localObject2).getPropertyName()))
            localDefaultTreeCellEditor = TreeEditors.access$000().getEditor(this.tree, (IProperty)localObject2);
        }
      }
      return (TreeCellEditor)(TreeCellEditor)localDefaultTreeCellEditor;
    }
  }

  private class DeployEditor extends DefaultTreeCellEditor
  {
    private Vector vListeners = new Vector();
    private ChangeEvent changeEvent = new ChangeEvent(this);

    public DeployEditor(JTree paramDefaultTreeCellRenderer, DefaultTreeCellRenderer arg3)
    {
      super(localDefaultTreeCellRenderer);
    }

    public void addCellEditorListener(CellEditorListener paramCellEditorListener)
    {
      this.vListeners.add(paramCellEditorListener);
    }

    public void removeCellEditorListener(CellEditorListener paramCellEditorListener)
    {
      this.vListeners.add(paramCellEditorListener);
    }

    public boolean isCellEditable(EventObject paramEventObject)
    {
      return true;
    }

    public boolean shouldSelectCell(EventObject paramEventObject)
    {
      if ((paramEventObject instanceof MouseEvent))
      {
        MouseEvent localMouseEvent = (MouseEvent)paramEventObject;
        return localMouseEvent.getID() != 506;
      }
      return true;
    }

    public boolean stopCellEditing()
    {
      return true;
    }

    public void cancelCellEditing()
    {
    }

    protected void editingStopped()
    {
      Vector localVector = (Vector)this.vListeners.clone();
      Iterator localIterator = localVector.iterator();
      while (localIterator.hasNext())
        ((CellEditorListener)localIterator.next()).editingStopped(this.changeEvent);
    }

    protected void editingCancelled()
    {
      Vector localVector = (Vector)this.vListeners.clone();
      Iterator localIterator = localVector.iterator();
      while (localIterator.hasNext())
        ((CellEditorListener)localIterator.next()).editingCanceled(this.changeEvent);
    }
  }

  private class RadioEditor extends TreeEditors.DeployEditor
  {
    private JRadioButton button = new JRadioButton();

    public RadioEditor(JTree paramDefaultTreeCellRenderer, DefaultTreeCellRenderer arg3)
    {
      super(paramDefaultTreeCellRenderer, localDefaultTreeCellRenderer);
      this.button.addMouseListener(new MouseListener(TreeEditors.this)
      {
        private final TreeEditors val$this$0;

        public void mouseClicked(MouseEvent paramMouseEvent)
        {
        }

        public void mousePressed(MouseEvent paramMouseEvent)
        {
        }

        public void mouseReleased(MouseEvent paramMouseEvent)
        {
          TreeEditors.RadioEditor.this.editingStopped();
        }

        public void mouseExited(MouseEvent paramMouseEvent)
        {
          TreeEditors.RadioEditor.this.editingCancelled();
        }

        public void mouseEntered(MouseEvent paramMouseEvent)
        {
        }
      });
    }

    public Component getTreeCellEditorComponent(JTree paramJTree, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt)
    {
      IProperty localIProperty = (IProperty)paramObject;
      this.button.setSelected(localIProperty.isSelected());
      this.button.setText(localIProperty.getDescription());
      this.button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      return this.button;
    }

    public Object getCellEditorValue()
    {
      String str = "";
      if (this.button.isSelected())
        str = this.button.getText();
      return str;
    }
  }

  private class TextFieldEditor extends TreeEditors.DeployEditor
  {
    private JPanel panel = new JPanel();
    private JButton browse_btn = new JButton(TreeEditors.access$300("deploy.advanced.browse.browse_btn"));
    private JTextField path = new JTextField("");

    public TextFieldEditor(JTree paramDefaultTreeCellRenderer, DefaultTreeCellRenderer arg3)
    {
      super(paramDefaultTreeCellRenderer, localDefaultTreeCellRenderer);
      this.panel.setLayout(new BorderLayout());
      this.path.setColumns(22);
      this.panel.add(this.path, "Center");
      this.panel.add(this.browse_btn, "East");
      this.browse_btn.setMnemonic(ResourceManager.getVKCode("deploy.advanced.browse.browse_btn.mnemonic"));
      this.path.addActionListener(new ActionListener(TreeEditors.this)
      {
        private final TreeEditors val$this$0;

        public void actionPerformed(ActionEvent paramActionEvent)
        {
          TreeEditors.TextFieldEditor.this.editingStopped();
        }
      });
      this.browse_btn.addActionListener(new ActionListener(TreeEditors.this)
      {
        private final TreeEditors val$this$0;

        public void actionPerformed(ActionEvent paramActionEvent)
        {
          JFileChooser localJFileChooser = new JFileChooser();
          localJFileChooser.setFileSelectionMode(2);
          localJFileChooser.setDialogTitle(TreeEditors.access$300("deploy.advanced.browse.title"));
          localJFileChooser.setApproveButtonText(TreeEditors.access$300("deploy.advanced.browse.select"));
          String str1 = TreeEditors.access$300("deploy.advanced.browse.select_tooltip");
          localJFileChooser.setApproveButtonToolTipText(str1);
          int i = ResourceManager.getVKCode("deploy.advanced.browse.select_mnemonic");
          localJFileChooser.setApproveButtonMnemonic(i);
          File localFile = new File(TreeEditors.TextFieldEditor.this.path.getText());
          localJFileChooser.setCurrentDirectory(localFile);
          if (localJFileChooser.showDialog(TreeEditors.TextFieldEditor.this.panel, null) == 0)
          {
            String str2 = "";
            try
            {
              str2 = localJFileChooser.getSelectedFile().getCanonicalPath();
            }
            catch (IOException localIOException)
            {
              str2 = localJFileChooser.getSelectedFile().getPath();
            }
            TreeEditors.TextFieldEditor.this.path.setText(str2);
          }
          TreeEditors.TextFieldEditor.this.editingStopped();
        }
      });
      Set localSet = Collections.synchronizedSet(new HashSet());
      localSet.add(AWTKeyStroke.getAWTKeyStroke(39, 0, true));
      this.panel.setFocusTraversalKeys(0, localSet);
      localSet.clear();
      localSet.add(AWTKeyStroke.getAWTKeyStroke(37, 0, true));
      this.panel.setFocusTraversalKeys(1, localSet);
      KeyStroke localKeyStroke = KeyStroke.getKeyStroke(9, 0, true);
      this.panel.getInputMap(1).put(localKeyStroke, "StopEditingAction");
      this.panel.getActionMap().put("StopEditingAction", new AbstractAction(TreeEditors.this)
      {
        private final TreeEditors val$this$0;

        public void actionPerformed(ActionEvent paramActionEvent)
        {
          TreeEditors.TextFieldEditor.this.editingStopped();
        }
      });
    }

    public Component getTreeCellEditorComponent(JTree paramJTree, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt)
    {
      if (paramBoolean1)
      {
        this.panel.setBackground(this.renderer.getBackgroundSelectionColor());
        this.panel.setBorder(BorderFactory.createLineBorder(this.renderer.getBorderSelectionColor()));
      }
      else
      {
        this.panel.setBackground(this.renderer.getBackgroundNonSelectionColor());
        this.panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      }
      IProperty localIProperty = (IProperty)paramObject;
      this.path.setText(localIProperty.getValue());
      this.path.setFont(paramJTree.getFont());
      return this.panel;
    }

    public Object getCellEditorValue()
    {
      return this.path.getText();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.TreeEditors
 * JD-Core Version:    0.6.0
 */