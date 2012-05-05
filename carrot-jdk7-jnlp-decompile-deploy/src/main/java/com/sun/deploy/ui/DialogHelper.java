package com.sun.deploy.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import sun.awt.EmbeddedFrame;

class DialogHelper
  implements DialogInterface
{
  private boolean allowEmbed = false;
  private Component owner = null;
  private AppInfo ainfo = null;
  private String title = null;
  private JDialog dialog = null;
  private Container content = null;
  private boolean modalOnTop = false;
  private JButton defaultButton = null;
  private boolean resizable = false;
  private Component savedContent = null;
  private boolean gradientBackground = true;
  private AbstractAction cancelAction = null;
  private static final int SHADOW_WIDTH = 6;
  private Component initialFocusComponent = null;
  private WindowListener[] windowListeners = null;

  public DialogHelper(Component paramComponent, AppInfo paramAppInfo, String paramString, boolean paramBoolean)
  {
    this.allowEmbed = paramBoolean;
    this.owner = paramComponent;
    this.ainfo = paramAppInfo;
    this.title = paramString;
  }

  public Container getContentPane()
  {
    if (this.dialog != null)
      this.content = this.dialog.getContentPane();
    else if (this.content == null)
      this.content = new JPanel();
    return this.content;
  }

  public void setContentPane(Container paramContainer)
  {
    this.content = paramContainer;
    if (this.dialog != null)
      this.dialog.setContentPane(this.content);
  }

  public void setModalOnTop(boolean paramBoolean)
  {
    this.modalOnTop = paramBoolean;
  }

  public void setDefaultButton(JButton paramJButton)
  {
    this.defaultButton = paramJButton;
    if (this.dialog != null)
      this.dialog.getRootPane().setDefaultButton(paramJButton);
  }

  public void setResizable(boolean paramBoolean)
  {
    this.resizable = paramBoolean;
    if (this.dialog != null)
      this.dialog.setResizable(this.resizable);
  }

  public JDialog getDialog()
  {
    return this.dialog;
  }

  public void pack()
  {
    if (this.dialog != null)
      this.dialog.pack();
  }

  public void dispose()
  {
    if (this.dialog != null)
      this.dialog.dispose();
  }

  public void setCancelAction(AbstractAction paramAbstractAction)
  {
    this.cancelAction = paramAbstractAction;
    if (this.dialog != null)
    {
      this.dialog.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
      this.dialog.getRootPane().getActionMap().put("cancel", this.cancelAction);
    }
  }

  public void addWindowListener(WindowListener paramWindowListener)
  {
    if (this.dialog != null)
    {
      this.dialog.addWindowListener(paramWindowListener);
    }
    else
    {
      int i = 1;
      if (this.windowListeners != null)
        i += this.windowListeners.length;
      WindowListener[] arrayOfWindowListener = new WindowListener[i];
      for (int j = 0; j < i - 1; j++)
        arrayOfWindowListener[j] = this.windowListeners[j];
      arrayOfWindowListener[(i - 1)] = paramWindowListener;
      this.windowListeners = arrayOfWindowListener;
    }
  }

  public void removeWindowListener(WindowListener paramWindowListener)
  {
    if (this.dialog != null)
      this.dialog.removeWindowListener(paramWindowListener);
    else
      this.windowListeners = null;
  }

  public void setTitle(String paramString)
  {
    this.title = paramString;
    if (this.dialog != null)
      this.dialog.setTitle(paramString);
  }

  public void setVisible(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      if (this.dialog != null)
      {
        this.dialog.setVisible(paramBoolean);
      }
      else if (useEmbeddedFrame())
      {
        JApplet localJApplet = new JApplet();
        Container localContainer = getContentPane();
        localJApplet.setContentPane(localContainer);
        if ((localContainer instanceof JPanel))
          setContentBorder((JPanel)localContainer);
        DeployEmbeddedFrameIf localDeployEmbeddedFrameIf = (DeployEmbeddedFrameIf)this.owner;
        EmbeddedFrame localEmbeddedFrame = (EmbeddedFrame)localDeployEmbeddedFrameIf;
        localJApplet.setVisible(true);
        if (this.cancelAction != null)
        {
          localJApplet.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
          localJApplet.getRootPane().getActionMap().put("cancel", this.cancelAction);
        }
        if (this.defaultButton != null)
          localJApplet.getRootPane().setDefaultButton(this.defaultButton);
        if (this.windowListeners != null)
          for (int j = 0; j < this.windowListeners.length; j++)
            localEmbeddedFrame.addWindowListener(this.windowListeners[j]);
        localDeployEmbeddedFrameIf.push(localJApplet);
        localEmbeddedFrame.pack();
        if ((localContainer instanceof JPanel))
          setContentBorder((JPanel)localContainer);
        UIFactory.afterDialog();
        showModalComponent(localJApplet, (Frame)this.owner);
        localDeployEmbeddedFrameIf.pop();
        UIFactory.beforeDialog(this.owner);
      }
      else
      {
        if ((this.owner == null) || ((this.owner instanceof Frame)))
          this.dialog = new JDialog((Frame)this.owner, this.title, this.modalOnTop);
        else
          this.dialog = new JDialog((Dialog)this.owner, this.title, this.modalOnTop);
        if (this.content != null)
          setContentPane(this.content);
        else
          this.content = this.dialog.getContentPane();
        if ((Config.isJavaVersionAtLeast16()) && (this.modalOnTop))
        {
          this.dialog.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
          this.dialog.setAlwaysOnTop(true);
        }
        setResizable(this.resizable);
        if (this.cancelAction != null)
        {
          this.dialog.getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
          this.dialog.getRootPane().getActionMap().put("cancel", this.cancelAction);
        }
        if (this.defaultButton != null)
          this.dialog.getRootPane().setDefaultButton(this.defaultButton);
        if (this.windowListeners != null)
          for (int i = 0; i < this.windowListeners.length; i++)
            this.dialog.addWindowListener(this.windowListeners[i]);
        this.dialog.pack();
        UIFactory.placeWindow(this.dialog);
        this.dialog.pack();
        if (this.initialFocusComponent != null)
          this.initialFocusComponent.requestFocusInWindow();
        this.dialog.setVisible(true);
      }
    }
    else if (this.dialog != null)
      this.dialog.setVisible(false);
    else
      getContentPane().setVisible(false);
  }

  public void setInitialFocusComponent(JComponent paramJComponent)
  {
    this.initialFocusComponent = paramJComponent;
  }

  private boolean useEmbeddedFrame()
  {
    if ((this.owner == null) || (!(this.owner instanceof DeployEmbeddedFrameIf)))
      return false;
    if (!this.allowEmbed)
    {
      Trace.println("Dialog type is not candidate for embedding", TraceLevel.BASIC);
      return false;
    }
    if (!Config.getBooleanProperty("deployment.allow.embedded.dialog"))
    {
      Trace.println("Embedding dialogs not enabled in Configuration", TraceLevel.BASIC);
      return false;
    }
    if (System.getProperty("javapi.noembedded.dialog") != null)
    {
      Trace.println("Applet specifically prohibited embedding dialogs", TraceLevel.BASIC);
      return false;
    }
    Dimension localDimension1 = getContentPane().getPreferredSize();
    Dimension localDimension2 = this.owner.getSize();
    if ((localDimension1.height + 2 + 6 > localDimension2.height) || (localDimension1.width + 2 + 6 > localDimension2.width))
    {
      Trace.println("Dialog would not fit in applet area - use Dialog", TraceLevel.BASIC);
      return false;
    }
    Trace.println("Using EmbeddedFrame to show dialog in Applet Area", TraceLevel.BASIC);
    return true;
  }

  private void setContentBorder(JPanel paramJPanel)
  {
    DropShadowBorder localDropShadowBorder = new DropShadowBorder(6, Color.GRAY);
    paramJPanel.setBorder(localDropShadowBorder);
    Dimension localDimension1 = paramJPanel.getPreferredSize();
    Dimension localDimension2 = this.owner.getSize();
    int i = localDimension2.width - localDimension1.width;
    int j = localDimension2.height - localDimension1.height;
    int k = i >= 40 ? 20 : i / 2;
    int m = j >= 40 ? 20 : j / 2;
    EmptyBorder localEmptyBorder = new EmptyBorder(m, k, j - m, i - k);
    paramJPanel.setBorder(BorderFactory.createCompoundBorder(localEmptyBorder, localDropShadowBorder));
  }

  private Color muchBrighter(Color paramColor)
  {
    int i = paramColor.getRed();
    int j = paramColor.getGreen();
    int k = paramColor.getBlue();
    return new Color(i + (255 - i) * 30 / 100, j + (255 - j) * 30 / 100, k + (255 - k) * 30 / 100);
  }

  public void showModalComponent(JApplet paramJApplet, Frame paramFrame)
  {
    paramJApplet.getContentPane().setVisible(true);
    try
    {
      new InvocationHandler(paramJApplet)
      {
        private final JApplet val$applet;

        public Object invoke(Object paramObject, Method paramMethod, Object[] paramArrayOfObject)
          throws Throwable
        {
          return this.val$applet.getContentPane().isVisible() ? Boolean.TRUE : Boolean.FALSE;
        }

        public void start()
          throws Exception
        {
          Class localClass = Class.forName("java.awt.Conditional");
          Object localObject = Proxy.newProxyInstance(localClass.getClassLoader(), new Class[] { localClass }, this);
          Method localMethod = Class.forName("java.awt.EventDispatchThread").getDeclaredMethod("pumpEvents", new Class[] { localClass });
          localMethod.setAccessible(true);
          localMethod.invoke(Thread.currentThread(), new Object[] { localObject });
        }
      }
      .start();
    }
    catch (Throwable localThrowable)
    {
      throw new RuntimeException(localThrowable);
    }
  }

  private class DropShadowBorder extends AbstractBorder
  {
    private int width;
    private Color color;

    public DropShadowBorder(int paramColor, Color arg3)
    {
      this.width = paramColor;
      Object localObject;
      this.color = localObject;
    }

    public Insets getBorderInsets(Component paramComponent)
    {
      return new Insets(1, 1, this.width + 1, this.width + 1);
    }

    public Insets getBorderInsets(Component paramComponent, Insets paramInsets)
    {
      paramInsets.top = 1;
      paramInsets.left = 1;
      paramInsets.bottom = this.width;
      paramInsets.right = this.width;
      return paramInsets;
    }

    public boolean isBorderOpaque()
    {
      return true;
    }

    public void paintBorder(Component paramComponent, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      Color localColor = paramGraphics.getColor();
      paramGraphics.setColor(this.color);
      paramGraphics.drawRect(paramInt1, paramInt2, paramInt3 - this.width - 1, paramInt4 - this.width - 1);
      paramGraphics.setColor(this.color.darker());
      for (int n = 0; n < this.width; n++)
      {
        int i = paramInt1 + n;
        int j = paramInt2 + paramInt4 - this.width + n;
        int k = paramInt1 + paramInt3 - this.width + n;
        int m = j;
        paramGraphics.drawLine(i, j, k, m);
        i = paramInt1 + paramInt3 - this.width + n;
        j = paramInt2 + n;
        k = i;
        m = paramInt2 + paramInt4 - this.width + n;
        paramGraphics.drawLine(i, j, k, m);
        paramGraphics.setColor(DialogHelper.this.muchBrighter(paramGraphics.getColor()));
      }
      paramGraphics.setColor(localColor);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.DialogHelper
 * JD-Core Version:    0.6.0
 */