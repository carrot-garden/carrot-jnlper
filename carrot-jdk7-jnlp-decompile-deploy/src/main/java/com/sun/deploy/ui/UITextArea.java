package com.sun.deploy.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class UITextArea extends JTextArea
{
  int preferred_width = 360;
  Image backgroundImage = null;

  public UITextArea()
  {
    JLabel localJLabel = new JLabel();
    setBorder(new EmptyBorder(new Insets(0, 5, 0, 0)));
    setEditable(false);
    setLineWrap(true);
    setWrapStyleWord(true);
    setFont(localJLabel.getFont());
    setRows(0);
    setHighlighter(null);
    invalidate();
  }

  public UITextArea(String paramString)
  {
    this();
    setText(paramString);
  }

  public UITextArea(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    this();
    this.preferred_width = paramInt2;
    JLabel localJLabel = new JLabel();
    Font localFont1 = localJLabel.getFont();
    Font localFont2;
    if (paramBoolean)
      localFont2 = localFont1.deriveFont(1, paramInt1);
    else
      localFont2 = localFont1.deriveFont(paramInt1);
    setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
    setFont(localFont2);
    invalidate();
  }

  public boolean isFocusTraversable()
  {
    return false;
  }

  public void setText(String paramString)
  {
    super.setText(paramString);
    invalidate();
  }

  public void setBackgroundImage(Image paramImage)
  {
    this.backgroundImage = paramImage;
    boolean bool = this.backgroundImage == null;
    setOpaque(bool);
  }

  public void paintComponent(Graphics paramGraphics)
  {
    if (this.backgroundImage != null)
      paramGraphics.drawImage(this.backgroundImage, 0, 0, this);
    else
      setBackground(new Color(getParent().getBackground().getRGB()));
    super.paintComponent(paramGraphics);
  }

  public Dimension getPreferredSize()
  {
    Dimension localDimension;
    if (this.backgroundImage != null)
    {
      localDimension = new Dimension(this.backgroundImage.getWidth(this), this.backgroundImage.getHeight(this));
    }
    else if ((getRows() == 0) && (getColumns() == 0))
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
 * Qualified Name:     com.sun.deploy.ui.UITextArea
 * JD-Core Version:    0.6.0
 */