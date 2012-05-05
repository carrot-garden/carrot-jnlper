package com.sun.deploy.uitoolkit.impl.awt;

import com.sun.deploy.uitoolkit.Window;
import com.sun.deploy.uitoolkit.WindowFactory;

public class AWTWindowFactory extends WindowFactory
{
  public Window createWindow()
  {
    return new AWTFrameWindow();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.awt.AWTWindowFactory
 * JD-Core Version:    0.6.0
 */