package com.sun.deploy.panel;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class NumberDocument extends PlainDocument
{
  public void insertString(int paramInt, String paramString, AttributeSet paramAttributeSet)
    throws BadLocationException
  {
    if (isNumeric(paramString))
      super.insertString(paramInt, paramString, paramAttributeSet);
    else
      Toolkit.getDefaultToolkit().beep();
  }

  private boolean isNumeric(String paramString)
  {
    try
    {
      Long.valueOf(paramString);
    }
    catch (NumberFormatException localNumberFormatException)
    {
      return false;
    }
    return true;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.NumberDocument
 * JD-Core Version:    0.6.0
 */