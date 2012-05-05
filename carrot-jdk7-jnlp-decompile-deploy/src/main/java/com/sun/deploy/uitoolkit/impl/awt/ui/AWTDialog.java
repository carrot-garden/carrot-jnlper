package com.sun.deploy.uitoolkit.impl.awt.ui;

import com.sun.deploy.uitoolkit.ui.AbstractDialog;
import java.awt.Dialog;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class AWTDialog extends AbstractDialog
{
  private static final Map awtDialogMap = new WeakHashMap();
  private final Dialog dialog;

  private AWTDialog(Dialog paramDialog)
  {
    this.dialog = paramDialog;
  }

  public static AWTDialog getAWTDialog(Dialog paramDialog)
  {
    AWTDialog localAWTDialog;
    synchronized (awtDialogMap)
    {
      WeakReference localWeakReference = (WeakReference)awtDialogMap.get(paramDialog);
      if ((localWeakReference == null) || ((localAWTDialog = (AWTDialog)localWeakReference.get()) == null))
      {
        localAWTDialog = new AWTDialog(paramDialog);
        awtDialogMap.put(paramDialog, new WeakReference(localAWTDialog));
      }
    }
    return localAWTDialog;
  }

  public Dialog getDialog()
  {
    return this.dialog;
  }

  public void toFront()
  {
    if (this.dialog != null)
      this.dialog.toFront();
  }

  public void requestFocus()
  {
    if (this.dialog != null)
      this.dialog.requestFocus();
  }

  public String toString()
  {
    if (this.dialog != null)
      return this.dialog.toString();
    return super.toString();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.awt.ui.AWTDialog
 * JD-Core Version:    0.6.0
 */