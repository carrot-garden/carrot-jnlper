package com.sun.deploy.uitoolkit.impl.text;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ui.ConsoleWindow;

public class TextConsoleWindow
  implements ConsoleWindow
{
  public void dispose()
  {
    Trace.println("TextConsoleWindow dispose()");
  }

  public void append(String paramString)
  {
  }

  public void clear()
  {
    Trace.println("TextConsoleWindow clear()");
  }

  public void setTitle(String paramString)
  {
    Trace.println("TextConsoleWindow setTitle()");
  }

  public void setVisible(boolean paramBoolean)
  {
    Trace.println("TextConsoleWindow setVisible()");
  }

  public boolean isVisible()
  {
    Trace.println("TextConsoleWindow isVisible()");
    return true;
  }

  public String getRecentLog()
  {
    Trace.println("TextConsoleWindow getRecentLog()");
    return "";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.text.TextConsoleWindow
 * JD-Core Version:    0.6.0
 */