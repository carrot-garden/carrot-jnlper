package com.sun.deploy.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

public class LinkMouseListener
  implements MouseListener
{
  private Cursor handCursor = new Cursor(12);
  private Color txtColor = null;
  private Cursor lblCursor = null;
  private JLabel label;

  private native void launchLink(String paramString);

  public LinkMouseListener(JLabel paramJLabel)
  {
    this.label = paramJLabel;
  }

  public void mouseClicked(MouseEvent paramMouseEvent)
  {
    String str = this.label.getText();
    Thread localThread = new Thread(new Runnable(str)
    {
      private final String val$url;

      public void run()
      {
        LinkMouseListener.this.launchLink(this.val$url);
      }
    });
    localThread.start();
  }

  public void mouseEntered(MouseEvent paramMouseEvent)
  {
    this.txtColor = this.label.getForeground();
    this.lblCursor = this.label.getCursor();
    this.label.setForeground(Color.RED);
    this.label.setCursor(this.handCursor);
  }

  public void mouseExited(MouseEvent paramMouseEvent)
  {
    if (this.txtColor != null)
      this.label.setForeground(this.txtColor);
    if (this.lblCursor != null)
      this.label.setCursor(this.lblCursor);
  }

  public void mousePressed(MouseEvent paramMouseEvent)
  {
  }

  public void mouseReleased(MouseEvent paramMouseEvent)
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.LinkMouseListener
 * JD-Core Version:    0.6.0
 */