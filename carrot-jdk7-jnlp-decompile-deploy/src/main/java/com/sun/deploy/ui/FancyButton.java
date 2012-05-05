package com.sun.deploy.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.KeyStroke;

class FancyButton extends JButton
  implements MouseListener
{
  private static Color originalColor = new Color(53, 85, 107);
  private Color activeColor = new Color(192, 102, 0);
  private Cursor handCursor = new Cursor(12);

  FancyButton(String paramString, int paramInt)
  {
    this(paramString, paramInt, originalColor);
  }

  FancyButton(String paramString, int paramInt, Color paramColor)
  {
    super(paramString);
    setMnemonic(paramInt);
    originalColor = paramColor;
    setForeground(originalColor);
    setCursor(this.handCursor);
    setBorderPainted(false);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    addMouseListener(this);
    setRolloverEnabled(false);
    setContentAreaFilled(false);
    KeyStroke localKeyStroke = KeyStroke.getKeyStroke(10, 0);
    getInputMap().put(localKeyStroke, "none");
  }

  public void mouseClicked(MouseEvent paramMouseEvent)
  {
  }

  public void mouseEntered(MouseEvent paramMouseEvent)
  {
    setForeground(this.activeColor);
  }

  public void mouseExited(MouseEvent paramMouseEvent)
  {
    if (originalColor != null)
      setForeground(originalColor);
  }

  public void mouseReleased(MouseEvent paramMouseEvent)
  {
  }

  public void mousePressed(MouseEvent paramMouseEvent)
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.FancyButton
 * JD-Core Version:    0.6.0
 */