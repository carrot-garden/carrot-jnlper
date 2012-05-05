package com.sun.deploy.uitoolkit;

public abstract class Window
{
  public abstract Object getWindowObject();

  public abstract void setBackground(int paramInt);

  public abstract void setForeground(int paramInt);

  public abstract void setVisible(boolean paramBoolean);

  public abstract void setSize(int paramInt1, int paramInt2);

  public abstract WindowSize getSize();

  public abstract void dispose();

  public abstract void dispose(DisposeListener paramDisposeListener, long paramLong);

  public abstract void setPosition(int paramInt1, int paramInt2);

  public static abstract interface DisposeListener
  {
    public abstract void disposeFailed();
  }

  public class WindowSize
  {
    public int width;
    public int height;

    public WindowSize(int paramInt1, int arg3)
    {
      this.width = paramInt1;
      int i;
      this.height = i;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.Window
 * JD-Core Version:    0.6.0
 */