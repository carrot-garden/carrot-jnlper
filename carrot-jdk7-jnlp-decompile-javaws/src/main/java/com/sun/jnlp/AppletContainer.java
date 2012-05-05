package com.sun.jnlp;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.javaws.Main;
import com.sun.javaws.exceptions.ExitException;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

public final class AppletContainer extends JPanel
{
  final AppletContainerCallback callback;
  final Applet applet;
  final String appletName;
  final URL documentBase;
  final URL codeBase;
  final Properties parameters;
  final boolean[] isActive = { false };
  int appletWidth;
  int appletHeight;
  final JLabel statusLabel = new JLabel("");
  static PrivilegedAction loadImageActionDummy = new LoadImageAction(null);

  public AppletContainer(AppletContainerCallback paramAppletContainerCallback, Applet paramApplet, String paramString, URL paramURL1, URL paramURL2, int paramInt1, int paramInt2, Properties paramProperties)
  {
    this.callback = paramAppletContainerCallback;
    this.applet = paramApplet;
    this.appletName = paramString;
    this.documentBase = paramURL1;
    this.codeBase = paramURL2;
    this.parameters = paramProperties;
    this.isActive[0] = false;
    this.appletWidth = paramInt1;
    this.appletHeight = paramInt2;
    AppletContainerContext localAppletContainerContext = new AppletContainerContext();
    AppletContainerStub localAppletContainerStub = new AppletContainerStub(localAppletContainerContext);
    paramApplet.setStub(localAppletContainerStub);
    this.statusLabel.setBorder(new EtchedBorder());
    this.statusLabel.setText("Loading...");
    setLayout(new BorderLayout());
    add("Center", paramApplet);
    add("South", this.statusLabel);
    Dimension localDimension = new Dimension(this.appletWidth, this.appletHeight + (int)this.statusLabel.getPreferredSize().getHeight());
    setPreferredSize(localDimension);
  }

  public Applet getApplet()
  {
    return this.applet;
  }

  public void setStatus(String paramString)
  {
    this.statusLabel.setText(paramString);
  }

  public void resizeApplet(int paramInt1, int paramInt2)
  {
    if ((paramInt1 < 0) || (paramInt2 < 0))
      return;
    int i = paramInt1 - this.appletWidth;
    int j = paramInt2 - this.appletHeight;
    Dimension localDimension1 = getSize();
    Dimension localDimension2 = new Dimension((int)localDimension1.getWidth() + i, (int)localDimension1.getHeight() + j);
    setSize(localDimension2);
    this.callback.relativeResize(new Dimension(i, j));
    this.appletWidth = paramInt1;
    this.appletHeight = paramInt2;
  }

  public Dimension getPreferredFrameSize(Frame paramFrame)
  {
    Insets localInsets = paramFrame.getInsets();
    int i = this.appletWidth + (localInsets.left + localInsets.right);
    int j = this.appletHeight + this.statusLabel.getHeight() + (localInsets.top + localInsets.bottom);
    return new Dimension(i, j);
  }

  public void startApplet()
  {
    ImageCache.initialize();
    new AppletAudioClip();
    new Thread()
    {
      public void run()
      {
        try
        {
          AppletContainer.this.setStatus("Initializing Applet");
          AppletContainer.this.applet.init();
          try
          {
            AppletContainer.this.isActive[0] = true;
            AppletContainer.this.applet.start();
            AppletContainer.this.setStatus("Applet running...");
          }
          catch (Throwable localThrowable1)
          {
            AppletContainer.this.setStatus("Failed to start Applet: " + localThrowable1.toString());
            localThrowable1.printStackTrace(System.out);
            AppletContainer.this.isActive[0] = false;
          }
        }
        catch (Throwable localThrowable2)
        {
          AppletContainer.this.setStatus("Failed to initialize: " + localThrowable2.toString());
          localThrowable2.printStackTrace(System.out);
        }
      }
    }
    .start();
  }

  public void stopApplet()
  {
    this.applet.stop();
    this.applet.destroy();
    try
    {
      Main.systemExit(0);
    }
    catch (ExitException localExitException)
    {
      Trace.println("systemExit: " + localExitException, TraceLevel.BASIC);
      Trace.ignoredException(localExitException);
    }
  }

  static void showApplet(AppletContainerCallback paramAppletContainerCallback, Applet paramApplet, String paramString, URL paramURL1, URL paramURL2, int paramInt1, int paramInt2, Properties paramProperties)
  {
    JFrame localJFrame = new JFrame("Applet Window");
    AppletContainer localAppletContainer = new AppletContainer(paramAppletContainerCallback, paramApplet, paramString, paramURL1, paramURL2, paramInt1, paramInt2, paramProperties);
    localJFrame.getContentPane().setLayout(new BorderLayout());
    localJFrame.getContentPane().add("Center", localAppletContainer);
    localJFrame.pack();
    localJFrame.setVisible(true);
    SwingUtilities.invokeLater(new Runnable(localAppletContainer, paramApplet)
    {
      private final AppletContainer val$container;
      private final Applet val$applet;

      public void run()
      {
        try
        {
          this.val$container.setStatus("Initializing Applet");
          this.val$applet.init();
          this.val$applet.start();
          this.val$container.setStatus("Applet Running");
        }
        catch (Throwable localThrowable)
        {
          this.val$container.setStatus("Failed to start Applet");
        }
      }
    });
  }

  class AppletContainerContext
    implements AppletContext
  {
    AppletContainerContext()
    {
    }

    public Applet getApplet(String paramString)
    {
      return paramString.equals(AppletContainer.this.appletName) ? AppletContainer.this.applet : null;
    }

    public Enumeration getApplets()
    {
      Vector localVector = new Vector();
      localVector.add(AppletContainer.this.applet);
      return localVector.elements();
    }

    public AudioClip getAudioClip(URL paramURL)
    {
      return AppletAudioClip.get(paramURL);
    }

    public Image getImage(URL paramURL)
    {
      AppletContainer.LoadImageAction localLoadImageAction = new AppletContainer.LoadImageAction(paramURL);
      return (Image)AccessController.doPrivileged(localLoadImageAction);
    }

    public void showDocument(URL paramURL)
    {
      AccessController.doPrivileged(new PrivilegedAction(paramURL)
      {
        private final URL val$url;

        public Object run()
        {
          1 local1 = new Thread()
          {
            public void run()
            {
              AppletContainer.this.callback.showDocument(AppletContainer.AppletContainerContext.1.this.val$url);
            }
          };
          local1.start();
          return null;
        }
      });
    }

    public void showDocument(URL paramURL, String paramString)
    {
      showDocument(paramURL);
    }

    public void showStatus(String paramString)
    {
      AppletContainer.this.statusLabel.setText(paramString);
    }

    public void setStream(String paramString, InputStream paramInputStream)
    {
    }

    public InputStream getStream(String paramString)
    {
      return null;
    }

    public Iterator getStreamKeys()
    {
      return null;
    }
  }

  final class AppletContainerStub
    implements AppletStub
  {
    AppletContext context;

    AppletContainerStub(AppletContext arg2)
    {
      Object localObject;
      this.context = localObject;
    }

    public void appletResize(int paramInt1, int paramInt2)
    {
      AppletContainer.this.resizeApplet(paramInt1, paramInt2);
    }

    public AppletContext getAppletContext()
    {
      return this.context;
    }

    public URL getCodeBase()
    {
      return AppletContainer.this.codeBase;
    }

    public URL getDocumentBase()
    {
      return AppletContainer.this.documentBase;
    }

    public String getParameter(String paramString)
    {
      return AppletContainer.this.parameters.getProperty(paramString);
    }

    public boolean isActive()
    {
      return AppletContainer.this.isActive[0];
    }
  }

  static class LoadImageAction
    implements PrivilegedAction
  {
    URL _url;

    public LoadImageAction(URL paramURL)
    {
      this._url = paramURL;
    }

    public Object run()
    {
      return ImageCache.getImage(this._url);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.AppletContainer
 * JD-Core Version:    0.6.0
 */