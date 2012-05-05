package com.sun.deploy.panel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class SpecialTreeListener extends KeyAdapter
{
  public void keyPressed(KeyEvent paramKeyEvent)
  {
    if ((paramKeyEvent.getSource() instanceof JTree))
    {
      JTree localJTree = (JTree)paramKeyEvent.getSource();
      TreePath localTreePath = localJTree.getSelectionPath();
      switch (paramKeyEvent.getKeyCode())
      {
      case 32:
        if ((localTreePath == null) || (!(localTreePath.getLastPathComponent() instanceof IProperty)))
          break;
        IProperty localIProperty = (IProperty)localTreePath.getLastPathComponent();
        if ((localIProperty instanceof ToggleProperty))
          ((ToggleProperty)localIProperty).setValue("true".equalsIgnoreCase(localIProperty.getValue()) ? "false" : "true");
        if ((localIProperty instanceof RadioProperty))
          ((RadioProperty)localIProperty).setValue(localIProperty.getValue());
        localJTree.repaint();
        break;
      }
    }
  }

  public void keyReleased(KeyEvent paramKeyEvent)
  {
    if ((paramKeyEvent.getSource() instanceof JTree))
    {
      JTree localJTree = (JTree)paramKeyEvent.getSource();
      TreePath localTreePath = localJTree.getSelectionPath();
      switch (paramKeyEvent.getKeyCode())
      {
      case 40:
        if ((localTreePath == null) || (!(localTreePath.getLastPathComponent() instanceof TextFieldProperty)))
          break;
        localJTree.startEditingAtPath(localTreePath);
        break;
      case 39:
        if ((localTreePath == null) || (!(localTreePath.getLastPathComponent() instanceof TextFieldProperty)))
          break;
        localJTree.startEditingAtPath(localTreePath);
        break;
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.SpecialTreeListener
 * JD-Core Version:    0.6.0
 */