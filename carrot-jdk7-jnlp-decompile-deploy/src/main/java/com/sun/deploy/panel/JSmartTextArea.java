package com.sun.deploy.panel;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class JSmartTextArea extends JTextArea
{
  int preferred_width = 360;

  public JSmartTextArea(String paramString)
  {
    this();
    setText(paramString);
  }

  public JSmartTextArea()
  {
    JLabel localJLabel = new JLabel();
    setBorder(new EmptyBorder(new Insets(0, 5, 0, 0)));
    setEditable(false);
    setLineWrap(true);
    setWrapStyleWord(true);
    setFont(localJLabel.getFont());
    setFocusable(false);
    setRows(0);
    invalidate();
  }

  public JSmartTextArea(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    this.preferred_width = paramInt2;
    JLabel localJLabel = new JLabel();
    Font localFont1 = localJLabel.getFont();
    Font localFont2;
    if (paramBoolean)
      localFont2 = localFont1.deriveFont(1, paramInt1);
    else
      localFont2 = localFont1.deriveFont(localFont1.getStyle(), paramInt1);
    setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
    setEditable(false);
    setLineWrap(true);
    setWrapStyleWord(true);
    setFont(localFont2);
    setRows(0);
    setFocusable(false);
    invalidate();
  }

  public void setText(String paramString)
  {
    super.setText(paramString);
    invalidate();
  }

  public void paintComponent(Graphics paramGraphics)
  {
    setBackground(new Color(getParent().getBackground().getRGB()));
    super.paintComponent(paramGraphics);
  }

  public Dimension getPreferredSize()
  {
    Dimension localDimension;
    if ((getRows() == 0) && (getColumns() == 0))
    {
      int i = this.preferred_width / getColumnWidth();
      setColumns(i);
      localDimension = super.getPreferredSize();
      setColumns(0);
    }
    else
    {
      localDimension = super.getPreferredSize();
    }
    return localDimension;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.JSmartTextArea
 * JD-Core Version:    0.6.0
 */