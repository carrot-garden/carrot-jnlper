package com.sun.deploy.uitoolkit.impl.awt;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.Window.DisposeListener;
import com.sun.deploy.uitoolkit.Window.WindowSize;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class AWTFrameWindow extends com.sun.deploy.uitoolkit.Window
{
  private Frame frame;
  private final Object frameLock = new Object();
  protected Window.WindowSize currentSize = new Window.WindowSize(this, 0, 0);

  private Frame getFrame()
  {
    synchronized (this.frameLock)
    {
      return this.frame;
    }
  }

  protected void setFrame(Frame paramFrame)
  {
    synchronized (this.frameLock)
    {
      this.frame = paramFrame;
    }
  }

  AWTFrameWindow()
  {
    JFrame localJFrame = new JFrame();
    localJFrame.getContentPane().setLayout(new BorderLayout());
    this.frame = localJFrame;
  }

  public Object getWindowObject()
  {
    return getFrame();
  }

  public void setBackground(int paramInt)
  {
    Frame localFrame = getFrame();
    if (localFrame != null)
      localFrame.setBackground(new Color(paramInt));
  }

  public void setForeground(int paramInt)
  {
    Frame localFrame = getFrame();
    if (localFrame != null)
      localFrame.setForeground(new Color(paramInt));
  }

  public void setVisible(boolean paramBoolean)
  {
    Frame localFrame = getFrame();
    if (localFrame != null)
      localFrame.setVisible(paramBoolean);
  }

  public void setSize(int paramInt1, int paramInt2)
  {
    Frame localFrame = getFrame();
    if ((localFrame != null) && (paramInt1 > 0) && (paramInt2 > 0))
      invokeLater(new Runnable(localFrame, paramInt1, paramInt2)
      {
        private final Frame val$ef;
        private final int val$width;
        private final int val$height;

        public void run()
        {
          this.val$ef.setSize(this.val$width, this.val$height);
        }
      });
  }

  public Window.WindowSize getSize()
  {
    Frame localFrame = getFrame();
    if (localFrame != null)
    {
      Dimension localDimension = localFrame.getSize();
      this.currentSize.width = localDimension.width;
      this.currentSize.height = localDimension.height;
    }
    return this.currentSize;
  }

  public void dispose()
  {
    dispose(null, 0L);
  }

  public void dispose(Window.DisposeListener paramDisposeListener, long paramLong)
  {
    Frame localFrame = getFrame();
    synchronized (this.frameLock)
    {
      this.frame = null;
    }
    if ((localFrame != null) && ((localFrame instanceof java.awt.Window)))
    {
      ??? = localFrame;
      Object localObject2 = new Object();
      synchronized (localObject2)
      {
        invokeLater((Component)???, new Runnable((java.awt.Window)???, paramDisposeListener, localObject2)
        {
          private final java.awt.Window val$parentWindow;
          private final Window.DisposeListener val$disposeListener;
          private final Object val$disposeLock;

          public void run()
          {
            try
            {
              this.val$parentWindow.dispose();
            }
            catch (Exception localException)
            {
              Trace.ignored(localException);
              if (this.val$disposeListener != null)
                this.val$disposeListener.disposeFailed();
            }
            synchronized (this.val$disposeLock)
            {
              this.val$disposeLock.notifyAll();
            }
          }
        });
        if (paramLong > 0L)
          try
          {
            localObject2.wait(paramLong);
          }
          catch (InterruptedException localInterruptedException)
          {
          }
      }
    }
  }

  public void setPosition(int paramInt1, int paramInt2)
  {
    Frame localFrame = getFrame();
    if (localFrame != null)
      localFrame.setLocation(paramInt1, paramInt2);
  }

  public void invokeLater(Runnable paramRunnable)
  {
    SwingUtilities.invokeLater(paramRunnable);
  }

  public void invokeLater(Component paramComponent, Runnable paramRunnable)
  {
    invokeLater(paramRunnable);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.awt.AWTFrameWindow
 * JD-Core Version:    0.6.0
 */