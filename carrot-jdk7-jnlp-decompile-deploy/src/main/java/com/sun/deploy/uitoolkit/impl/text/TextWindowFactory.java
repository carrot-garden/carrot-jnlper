package com.sun.deploy.uitoolkit.impl.text;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.Window;
import com.sun.deploy.uitoolkit.WindowFactory;

public class TextWindowFactory extends WindowFactory
{
  public Window createWindow()
  {
    Trace.println("TextWindowFactory:createWindow()");
    return new TextWindow();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.text.TextWindowFactory
 * JD-Core Version:    0.6.0
 */