package com.sun.deploy.uitoolkit.impl.text;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.Window;
import com.sun.deploy.uitoolkit.Window.DisposeListener;
import com.sun.deploy.uitoolkit.Window.WindowSize;

public class TextWindow extends Window
{
  private int bgColor = 0;
  private boolean visible = false;
  private Window.WindowSize size = new Window.WindowSize(this, 0, 0);
  int x = 0;
  int y = 0;
  private boolean disposed = false;

  private void println(String paramString)
  {
    Trace.println("TextWindow:  " + paramString);
  }

  public Object getWindowObject()
  {
    println("getWindowObject(), returning null");
    return null;
  }

  public void setBackground(int paramInt)
  {
    println("Background set to" + Integer.toHexString(paramInt));
    this.bgColor = paramInt;
  }

  public void setForeground(int paramInt)
  {
    println("Foreground set to" + Integer.toHexString(paramInt));
    this.bgColor = paramInt;
  }

  public void setVisible(boolean paramBoolean)
  {
    println("setVisible(" + paramBoolean + ")");
    this.visible = paramBoolean;
  }

  public void setSize(int paramInt1, int paramInt2)
  {
    println("setSize(" + paramInt1 + ", " + paramInt2 + ")");
    this.size.width = paramInt1;
    this.size.height = paramInt2;
  }

  public Window.WindowSize getSize()
  {
    return new Window.WindowSize(this, this.size.width, this.size.height);
  }

  public void dispose()
  {
    println("dispose()");
    this.disposed = true;
  }

  public void dispose(Window.DisposeListener paramDisposeListener, long paramLong)
  {
    println("dispose(" + paramDisposeListener + ", " + paramLong + ")");
    dispose();
  }

  public void setPosition(int paramInt1, int paramInt2)
  {
    println("setPosition(" + paramInt1 + ", " + paramInt2 + ")");
    this.x = paramInt1;
    this.y = paramInt2;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.text.TextWindow
 * JD-Core Version:    0.6.0
 */