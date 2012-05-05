package com.sun.deploy.uitoolkit.impl.awt.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.UIFactory;
import com.sun.deploy.uitoolkit.ui.ConsoleController;
import com.sun.deploy.uitoolkit.ui.ConsoleHelper;
import com.sun.deploy.uitoolkit.ui.ConsoleWindow;
import com.sun.deploy.util.DeploySysAction;
import com.sun.deploy.util.DeploySysRun;
import com.sun.deploy.util.PerfLogger;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog.ModalExclusionType;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public final class SwingConsoleWindow extends JFrame
  implements ConsoleWindow
{
  private final ConsoleController controller;
  private final JTextArea textArea;
  private final JScrollPane scroller;
  private final JScrollBar sbHor;
  private final JScrollBar sbVer;

  public static SwingConsoleWindow create(ConsoleController paramConsoleController)
    throws Exception
  {
    SwingConsoleWindow[] arrayOfSwingConsoleWindow = new SwingConsoleWindow[1];
    invokeAndWait(new Runnable(arrayOfSwingConsoleWindow, paramConsoleController)
    {
      private final SwingConsoleWindow[] val$ret;
      private final ConsoleController val$controller;

      public void run()
      {
        this.val$ret[0] = new SwingConsoleWindow(this.val$controller);
      }
    });
    return arrayOfSwingConsoleWindow[0];
  }

  public SwingConsoleWindow(ConsoleController paramConsoleController)
  {
    super(ResourceManager.getMessage("console.caption"));
    if (Config.isJavaVersionAtLeast16())
      setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
    this.controller = paramConsoleController;
    Rectangle localRectangle = UIFactory.getMouseScreenBounds();
    setBounds(localRectangle.x, localRectangle.y, 450, 400);
    setResizable(true);
    setDefaultCloseOperation(0);
    getContentPane().setLayout(new BorderLayout());
    this.textArea = new JTextArea();
    this.textArea.setFont(ResourceManager.getUIFont());
    this.textArea.setEditable(false);
    this.textArea.setMargin(new Insets(0, 5, 0, 0));
    this.scroller = new JScrollPane(this.textArea);
    JViewport localJViewport = this.scroller.getViewport();
    localJViewport.setScrollMode(1);
    this.sbVer = this.scroller.getVerticalScrollBar();
    this.sbHor = this.scroller.getHorizontalScrollBar();
    getContentPane().add(this.scroller, "Center");
    2 local2 = new ActionListener(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        Trace.print(ResourceManager.getMessage("console.dump.stack"));
        Trace.print(ResourceManager.getMessage("console.menu.text.top"));
        ConsoleHelper.dumpAllStacks(this.val$controller);
        Trace.print(ResourceManager.getMessage("console.menu.text.tail"));
        Trace.print(ResourceManager.getMessage("console.done"));
      }
    };
    if (paramConsoleController.isJCovSupported())
    {
      localObject = new ActionListener(paramConsoleController)
      {
        private final ConsoleController val$controller;

        public void actionPerformed(ActionEvent paramActionEvent)
        {
          if (this.val$controller.dumpJCovData())
            Trace.println(ResourceManager.getMessage("console.jcov.info"));
          else
            Trace.println(ResourceManager.getMessage("console.jcov.error"));
        }
      };
      this.textArea.registerKeyboardAction((ActionListener)localObject, KeyStroke.getKeyStroke(74, 0), 2);
    }
    Object localObject = new ActionListener(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        Trace.print(ResourceManager.getMessage("console.dump.thread"));
        ThreadGroup localThreadGroup = this.val$controller.getMainThreadGroup();
        ConsoleHelper.dumpThreadGroup(localThreadGroup);
        Trace.println(ResourceManager.getMessage("console.done"));
      }
    };
    5 local5 = new ActionListener(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        Trace.print(ResourceManager.getMessage("console.reload.policy"));
        this.val$controller.reloadSecurityPolicy();
        Trace.println(ResourceManager.getMessage("console.completed"));
      }
    };
    6 local6 = new ActionListener(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        Trace.println(ResourceManager.getMessage("console.reload.proxy"));
        this.val$controller.reloadProxyConfig();
        Trace.println(ResourceManager.getMessage("console.done"));
      }
    };
    7 local7 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        ConsoleHelper.displaySystemProperties();
      }
    };
    8 local8 = new ActionListener(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        ConsoleHelper.displayHelp(this.val$controller, SwingConsoleWindow.this);
      }
    };
    9 local9 = new ActionListener(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        Trace.println(this.val$controller.dumpClassLoaders());
      }
    };
    10 local10 = new ActionListener(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        this.val$controller.clearClassLoaders();
        Trace.println(ResourceManager.getMessage("console.clear.classloader"));
      }
    };
    11 local11 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        PerfLogger.setEndTime("End at clear console");
        PerfLogger.outputLog();
        SwingConsoleWindow.this.clear();
      }
    };
    12 local12 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        int i = SwingConsoleWindow.this.textArea.getSelectionStart();
        int j = SwingConsoleWindow.this.textArea.getSelectionEnd();
        if (j - i <= 0)
          SwingConsoleWindow.this.textArea.selectAll();
        SwingConsoleWindow.this.textArea.copy();
      }
    };
    13 local13 = new ActionListener(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        PerfLogger.setEndTime("End at close console");
        PerfLogger.outputLog();
        if (this.val$controller.isIconifiedOnClose())
        {
          SwingConsoleWindow.this.setState(1);
        }
        else
        {
          SwingConsoleWindow.this.setVisible(false);
          SwingConsoleWindow.this.dispose();
        }
        this.val$controller.notifyConsoleClosed();
      }
    };
    14 local14 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        long l1 = Runtime.getRuntime().freeMemory() / 1024L;
        long l2 = Runtime.getRuntime().totalMemory() / 1024L;
        long l3 = ()(100.0D / (l2 / l1));
        MessageFormat localMessageFormat = new MessageFormat(ResourceManager.getMessage("console.memory"));
        Object[] arrayOfObject = { new Long(l2), new Long(l1), new Long(l3) };
        Trace.print(localMessageFormat.format(arrayOfObject));
        Trace.println(ResourceManager.getMessage("console.completed"));
      }
    };
    15 local15 = new ActionListener(local14)
    {
      private final ActionListener val$showMemory;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        Trace.print(ResourceManager.getMessage("console.finalize"));
        System.runFinalization();
        Trace.println(ResourceManager.getMessage("console.completed"));
        this.val$showMemory.actionPerformed(paramActionEvent);
      }
    };
    16 local16 = new ActionListener(local14)
    {
      private final ActionListener val$showMemory;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        Trace.print(ResourceManager.getMessage("console.gc"));
        System.gc();
        Trace.println(ResourceManager.getMessage("console.completed"));
        this.val$showMemory.actionPerformed(paramActionEvent);
      }
    };
    17 local17 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        ConsoleHelper.setTraceLevel(0);
      }
    };
    18 local18 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        ConsoleHelper.setTraceLevel(1);
      }
    };
    19 local19 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        ConsoleHelper.setTraceLevel(2);
      }
    };
    20 local20 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        ConsoleHelper.setTraceLevel(3);
      }
    };
    21 local21 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        ConsoleHelper.setTraceLevel(4);
      }
    };
    22 local22 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        ConsoleHelper.setTraceLevel(5);
      }
    };
    23 local23 = new ActionListener(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void actionPerformed(ActionEvent paramActionEvent)
      {
        Trace.println(ResourceManager.getMessage("console.log") + this.val$controller.toggleLogging() + ResourceManager.getMessage("console.completed"));
      }
    };
    if (paramConsoleController.isDumpStackSupported())
      this.textArea.registerKeyboardAction(local2, KeyStroke.getKeyStroke(86, 0), 2);
    if (paramConsoleController.isProxyConfigReloadSupported())
      this.textArea.registerKeyboardAction(local6, KeyStroke.getKeyStroke(80, 0), 2);
    if (paramConsoleController.isSecurityPolicyReloadSupported())
      this.textArea.registerKeyboardAction(local5, KeyStroke.getKeyStroke(82, 0), 2);
    if (paramConsoleController.isClearClassLoaderSupported())
      this.textArea.registerKeyboardAction(local10, KeyStroke.getKeyStroke(88, 0), 2);
    if (paramConsoleController.isDumpClassLoaderSupported())
      this.textArea.registerKeyboardAction(local9, KeyStroke.getKeyStroke(76, 0), 2);
    if (paramConsoleController.isLoggingSupported())
      this.textArea.registerKeyboardAction(local23, KeyStroke.getKeyStroke(79, 0), 2);
    this.textArea.registerKeyboardAction((ActionListener)localObject, KeyStroke.getKeyStroke(84, 0), 2);
    this.textArea.registerKeyboardAction(local7, KeyStroke.getKeyStroke(83, 0), 2);
    this.textArea.registerKeyboardAction(local8, KeyStroke.getKeyStroke(72, 0), 2);
    this.textArea.registerKeyboardAction(local14, KeyStroke.getKeyStroke(77, 0), 2);
    this.textArea.registerKeyboardAction(local11, KeyStroke.getKeyStroke(67, 0), 2);
    this.textArea.registerKeyboardAction(local16, KeyStroke.getKeyStroke(71, 0), 2);
    this.textArea.registerKeyboardAction(local15, KeyStroke.getKeyStroke(70, 0), 2);
    this.textArea.registerKeyboardAction(local13, KeyStroke.getKeyStroke(81, 0), 2);
    this.textArea.registerKeyboardAction(local17, KeyStroke.getKeyStroke(48, 0), 2);
    this.textArea.registerKeyboardAction(local18, KeyStroke.getKeyStroke(49, 0), 2);
    this.textArea.registerKeyboardAction(local19, KeyStroke.getKeyStroke(50, 0), 2);
    this.textArea.registerKeyboardAction(local20, KeyStroke.getKeyStroke(51, 0), 2);
    this.textArea.registerKeyboardAction(local21, KeyStroke.getKeyStroke(52, 0), 2);
    this.textArea.registerKeyboardAction(local22, KeyStroke.getKeyStroke(53, 0), 2);
    JButton localJButton1 = new JButton(ResourceManager.getMessage("console.clear"));
    localJButton1.setMnemonic(ResourceManager.getAcceleratorKey("console.clear"));
    JButton localJButton2 = new JButton(ResourceManager.getMessage("console.copy"));
    localJButton2.setMnemonic(ResourceManager.getAcceleratorKey("console.copy"));
    JButton localJButton3 = new JButton(ResourceManager.getMessage("console.close"));
    localJButton3.setMnemonic(ResourceManager.getAcceleratorKey("console.close"));
    JPanel localJPanel = new JPanel();
    localJPanel.setLayout(new FlowLayout(1));
    localJPanel.add(localJButton1);
    localJPanel.add(new JLabel("    "));
    localJPanel.add(localJButton2);
    localJPanel.add(new JLabel("    "));
    localJPanel.add(localJButton3);
    getContentPane().add(localJPanel, "South");
    addWindowListener(new WindowAdapter(paramConsoleController)
    {
      private final ConsoleController val$controller;

      public void windowClosing(WindowEvent paramWindowEvent)
      {
        if (this.val$controller.isIconifiedOnClose())
        {
          SwingConsoleWindow.this.setState(1);
        }
        else
        {
          SwingConsoleWindow.this.setVisible(false);
          SwingConsoleWindow.this.dispose();
        }
        this.val$controller.notifyConsoleClosed();
      }
    });
    localJButton1.addActionListener(local11);
    localJButton2.addActionListener(local12);
    localJButton3.addActionListener(local13);
  }

  public void clear()
  {
    invokeLater(new Runnable()
    {
      public void run()
      {
        PlainDocument localPlainDocument = new PlainDocument();
        SwingConsoleWindow.this.textArea.setDocument(localPlainDocument);
        SwingConsoleWindow.this.textArea.revalidate();
        SwingConsoleWindow.this.setScrollPosition();
        ConsoleHelper.displayVersion(SwingConsoleWindow.this.controller, SwingConsoleWindow.this);
        SwingConsoleWindow.this.append("\n");
        ConsoleHelper.displayHelp(SwingConsoleWindow.this.controller, SwingConsoleWindow.this);
      }
    });
  }

  private void invokeLater(Runnable paramRunnable)
  {
    if (paramRunnable == null)
      return;
    try
    {
      DeploySysRun.execute(new DeploySysAction(paramRunnable)
      {
        private final Runnable val$r;

        public Object execute()
        {
          SwingUtilities.invokeLater(this.val$r);
          return null;
        }
      });
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
  }

  private static void invokeAndWait(Runnable paramRunnable)
  {
    if (paramRunnable == null)
      return;
    try
    {
      DeploySysRun.execute(new DeploySysAction(paramRunnable)
      {
        private final Runnable val$r;

        public Object execute()
        {
          try
          {
            if (!EventQueue.isDispatchThread())
              SwingUtilities.invokeAndWait(this.val$r);
            else
              this.val$r.run();
          }
          catch (Exception localException)
          {
          }
          return null;
        }
      });
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
  }

  public void append(String paramString)
  {
    invokeLater(new Runnable(paramString)
    {
      private final String val$text;

      public void run()
      {
        int i = SwingConsoleWindow.this.textArea.getText().length() + this.val$text.length() - 1048575;
        try
        {
          if (i > 0)
            SwingConsoleWindow.this.textArea.replaceRange("", 0, i);
          SwingConsoleWindow.this.textArea.append(this.val$text);
          SwingConsoleWindow.this.textArea.revalidate();
          SwingConsoleWindow.this.setScrollPosition();
        }
        catch (Exception localException)
        {
          SwingConsoleWindow.this.clear();
          Trace.println(ResourceManager.getMessage("console.trace.error"));
        }
      }
    });
  }

  private void setScrollPosition()
  {
    this.scroller.validate();
    this.sbVer.setValue(this.sbVer.getMaximum());
    this.sbHor.setValue(this.sbHor.getMinimum());
  }

  public void setVisible(boolean paramBoolean)
  {
    invokeLater(new Runnable(paramBoolean)
    {
      private final boolean val$visible;

      public void run()
      {
        SwingConsoleWindow.this.setVisibleImpl(this.val$visible);
      }
    });
  }

  private void setVisibleImpl(boolean paramBoolean)
  {
    if (this.controller.isIconifiedOnClose())
    {
      setState(paramBoolean ? 0 : 1);
      super.setVisible(true);
    }
    else
    {
      if (isVisible() != paramBoolean)
        super.setVisible(paramBoolean);
      if (paramBoolean)
        toFront();
      else
        dispose();
    }
  }

  public void setTitle(String paramString)
  {
    invokeLater(new Runnable(paramString)
    {
      private final String val$string;

      public void run()
      {
        SwingConsoleWindow.this.setTitleImpl(this.val$string);
      }
    });
  }

  private void setTitleImpl(String paramString)
  {
    super.setTitle(paramString);
  }

  public boolean isVisible()
  {
    if (this.controller.isIconifiedOnClose())
      return getState() == 0;
    return super.isVisible();
  }

  public String getRecentLog()
  {
    String[] arrayOfString = new String[1];
    arrayOfString[0] = "empty";
    Trace.flush();
    invokeAndWait(new Runnable(arrayOfString)
    {
      private final String[] val$ret;

      public void run()
      {
        Document localDocument = SwingConsoleWindow.this.textArea.getDocument();
        try
        {
          this.val$ret[0] = localDocument.getText(0, localDocument.getLength());
        }
        catch (BadLocationException localBadLocationException)
        {
        }
      }
    });
    return arrayOfString[0];
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.impl.awt.ui.SwingConsoleWindow
 * JD-Core Version:    0.6.0
 */