package com.sun.deploy.panel;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class PropertyTreeModel
  implements TreeModel
{
  private ITreeNode root;
  private EventListenerList listenersList = new EventListenerList();

  public PropertyTreeModel(ITreeNode paramITreeNode)
  {
    this.root = paramITreeNode;
  }

  public Object getRoot()
  {
    return this.root;
  }

  public int getChildCount(Object paramObject)
  {
    if ((paramObject instanceof ITreeNode))
    {
      ITreeNode localITreeNode = (ITreeNode)paramObject;
      return localITreeNode.getChildNodeCount() + localITreeNode.getPropertyCount();
    }
    return 0;
  }

  public Object getChild(Object paramObject, int paramInt)
  {
    ITreeNode localITreeNode = (ITreeNode)paramObject;
    int i = localITreeNode.getChildNodeCount();
    return paramInt < i ? localITreeNode.getChildNode(paramInt) : localITreeNode.getProperty(paramInt - i);
  }

  public boolean isLeaf(Object paramObject)
  {
    return paramObject instanceof IProperty;
  }

  public void valueForPathChanged(TreePath paramTreePath, Object paramObject)
  {
    Object localObject = paramTreePath.getLastPathComponent();
    if ((localObject instanceof IProperty))
    {
      ((IProperty)localObject).setValue((String)paramObject);
      fireTreeNodesChanged(paramTreePath);
    }
  }

  protected void fireTreeNodesChanged(TreePath paramTreePath)
  {
    Object[] arrayOfObject = this.listenersList.getListenerList();
    TreeModelEvent localTreeModelEvent = null;
    int i = arrayOfObject.length - 2;
    while (i >= 0)
    {
      if (arrayOfObject[i] == TreeModelListener.class)
      {
        if (localTreeModelEvent == null)
          localTreeModelEvent = new TreeModelEvent(this, paramTreePath);
        ((TreeModelListener)arrayOfObject[(i + 1)]).treeNodesChanged(localTreeModelEvent);
      }
      i -= 2;
    }
  }

  public int getIndexOfChild(Object paramObject1, Object paramObject2)
  {
    for (int i = 0; i < getChildCount(paramObject1); i++)
      if (getChild(paramObject1, i) == paramObject2)
        return i;
    return -1;
  }

  public void addTreeModelListener(TreeModelListener paramTreeModelListener)
  {
    this.listenersList.add(TreeModelListener.class, paramTreeModelListener);
  }

  public void removeTreeModelListener(TreeModelListener paramTreeModelListener)
  {
    this.listenersList.remove(TreeModelListener.class, paramTreeModelListener);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.PropertyTreeModel
 * JD-Core Version:    0.6.0
 */