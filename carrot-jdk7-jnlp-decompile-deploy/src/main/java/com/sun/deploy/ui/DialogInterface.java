package com.sun.deploy.ui;

import java.awt.Container;
import java.awt.event.WindowListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

abstract interface DialogInterface
{
  public abstract JDialog getDialog();

  public abstract Container getContentPane();

  public abstract void setContentPane(Container paramContainer);

  public abstract void setModalOnTop(boolean paramBoolean);

  public abstract void setDefaultButton(JButton paramJButton);

  public abstract void setResizable(boolean paramBoolean);

  public abstract void setVisible(boolean paramBoolean);

  public abstract void pack();

  public abstract void dispose();

  public abstract void setCancelAction(AbstractAction paramAbstractAction);

  public abstract void addWindowListener(WindowListener paramWindowListener);

  public abstract void removeWindowListener(WindowListener paramWindowListener);

  public abstract void setTitle(String paramString);

  public abstract void setInitialFocusComponent(JComponent paramJComponent);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.DialogInterface
 * JD-Core Version:    0.6.0
 */