package com.sun.javaws.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.DeploySysAction;
import com.sun.deploy.util.DeploySysRun;
import com.sun.javaws.Main;
import com.sun.javaws.security.AppContextUtil;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public final class JavawsSysRun extends DeploySysRun
{
  private final SecureThread t = new SecureThread();

  private void delegateFromEDT(Job paramJob)
    throws Exception
  {
    DummyDialog localDummyDialog = new DummyDialog();
    paramJob.setDialog(localDummyDialog);
    if (Config.getOSName().equals("Windows"))
    {
      localDummyDialog.setLocation(-200, -200);
    }
    else
    {
      Rectangle localRectangle = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());
      localDummyDialog.setLocation(localRectangle.x + localRectangle.width / 2 - 50, localRectangle.y + localRectangle.height / 2);
    }
    localDummyDialog.setResizable(false);
    localDummyDialog.toBack();
    localDummyDialog.addWindowListener(new WindowAdapter(paramJob)
    {
      private final JavawsSysRun.Job val$job;

      public void windowOpened(WindowEvent paramWindowEvent)
      {
        synchronized (JavawsSysRun.this.t.mutex)
        {
          JavawsSysRun.this.t.addJob(this.val$job);
          JavawsSysRun.this.t.mutex.notifyAll();
        }
      }
    });
    localDummyDialog.setVisible(true);
    localDummyDialog.dispose();
  }

  public Object delegate(DeploySysAction paramDeploySysAction)
    throws Exception
  {
    if ((Main.getSecurityThreadGroup() == null) || (Main.getSecurityThreadGroup().equals(Thread.currentThread().getThreadGroup())))
      return paramDeploySysAction.execute();
    Job localJob = new Job(paramDeploySysAction);
    if ((AppContextUtil.isApplicationAppContext()) && (SwingUtilities.isEventDispatchThread()))
      delegateFromEDT(localJob);
    else
      synchronized (this.t.mutex)
      {
        this.t.addJob(localJob);
        this.t.mutex.notifyAll();
        while (!localJob.done)
          try
          {
            this.t.mutex.wait();
          }
          catch (InterruptedException localInterruptedException)
          {
            Trace.ignoredException(localInterruptedException);
          }
        this.t.mutex.notifyAll();
      }
    if (localJob.exception != null)
      throw localJob.exception;
    return localJob.result;
  }

  static void invokeLater(Runnable paramRunnable)
  {
    if ((Main.getSecurityThreadGroup() == null) || (Main.getSecurityThreadGroup().equals(Thread.currentThread().getThreadGroup())))
    {
      SwingUtilities.invokeLater(paramRunnable);
      return;
    }
    2 local2 = new Runnable(paramRunnable)
    {
      private final Runnable val$runner;

      public void run()
      {
        SwingUtilities.invokeLater(this.val$runner);
      }
    };
    AccessController.doPrivileged(new PrivilegedAction(local2)
    {
      private final Runnable val$invoker;

      public Object run()
      {
        Thread localThread = new Thread(Main.getSecurityThreadGroup(), this.val$invoker);
        localThread.setContextClassLoader(Main.getSecureContextClassLoader());
        localThread.start();
        return null;
      }
    });
  }

  private class DummyDialog extends JDialog
  {
    private ThreadGroup _callingTG = Thread.currentThread().getThreadGroup();

    DummyDialog()
    {
      super(true);
    }

    public void secureHide()
    {
      new Thread(this._callingTG, new Runnable()
      {
        public void run()
        {
          JavawsSysRun.DummyDialog.this.setVisible(false);
        }
      }).start();
    }
  }

  class Job
  {
    final DeploySysAction action;
    JavawsSysRun.DummyDialog dialog;
    Object result;
    Exception exception;
    boolean done;

    Job(DeploySysAction arg2)
    {
      Object localObject;
      this.action = localObject;
      this.done = false;
    }

    void setDialog(JavawsSysRun.DummyDialog paramDummyDialog)
    {
      this.dialog = paramDummyDialog;
    }
  }

  class SecureThread extends Thread
  {
    Object mutex = new Object();
    LinkedList jobList = new LinkedList();

    SecureThread()
    {
      super("Javaws Secure Thread");
      setDaemon(true);
      setContextClassLoader(Main.getSecureContextClassLoader());
      start();
    }

    void addJob(JavawsSysRun.Job paramJob)
    {
      this.jobList.add(paramJob);
    }

    private void doWork(JavawsSysRun.Job paramJob)
    {
      try
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            Thread.currentThread().setContextClassLoader(Main.getSecureContextClassLoader());
          }
        });
        paramJob.result = paramJob.action.execute();
      }
      catch (Exception localException)
      {
        paramJob.exception = localException;
      }
      finally
      {
        paramJob.done = true;
        if (paramJob.dialog != null)
          paramJob.dialog.secureHide();
      }
    }

    public void run()
    {
      synchronized (this.mutex)
      {
        while (true)
        {
          if (!this.jobList.isEmpty())
          {
            JavawsSysRun.Job localJob = (JavawsSysRun.Job)this.jobList.removeFirst();
            doWork(localJob);
            continue;
          }
          this.mutex.notifyAll();
          try
          {
            this.mutex.wait();
          }
          catch (InterruptedException localInterruptedException)
          {
            Trace.ignoredException(localInterruptedException);
          }
        }
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.JavawsSysRun
 * JD-Core Version:    0.6.0
 */