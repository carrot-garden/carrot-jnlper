package com.sun.javaws.progress;

import com.sun.applet2.preloader.CancelException;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.AppletInitEvent;
import com.sun.applet2.preloader.event.DownloadErrorEvent;
import com.sun.applet2.preloader.event.DownloadEvent;
import com.sun.applet2.preloader.event.ErrorEvent;
import com.sun.applet2.preloader.event.PreloaderEvent;
import com.sun.applet2.preloader.event.UserDeclinedEvent;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.perf.DeployPerfUtil;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.Applet2Adapter;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.deploy.uitoolkit.ui.ComponentRef;
import com.sun.javaws.exceptions.JNLPException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.jnlp.DownloadServiceListener;

public class PreloaderDelegate extends Preloader
  implements DownloadServiceListener
{
  private WeakReference _plRef = null;
  private Preloader _plStrongRef = null;
  private boolean plNeedStrongRef = false;
  protected ThreadGroup _appThreadGroup = null;
  private boolean isLoaded = true;
  private Exception loadingException = null;
  private boolean isReady = false;
  private static ThreadLocal isUserDeclinedPreloader = new ThreadLocal();
  private final List pendingEvents = new LinkedList();
  private WeakReference listenerRef = null;
  WeakReference hostAdapterRef = null;
  private String preloaderClassName = null;
  private boolean canStop = false;
  private final ArrayList queue = new ArrayList();
  private Thread progressThread = null;
  private ProgressQueueChecker checker = null;
  private boolean disposed = false;
  int rescaleBaseline = -1;
  private CancelException ce = null;
  private boolean seenUserDeclined = false;
  long lastPercentage = -1L;

  public void setPostEventListener(PreloaderPostEventListener paramPreloaderPostEventListener)
  {
    this.listenerRef = new WeakReference(paramPreloaderPostEventListener);
  }

  public void keepPreloaderAlive(boolean paramBoolean)
  {
    this.plNeedStrongRef = paramBoolean;
    this._plStrongRef = (paramBoolean ? get() : null);
  }

  public PreloaderDelegate(Applet2Adapter paramApplet2Adapter)
  {
    Trace.println("Construct preloader delegate", TraceLevel.PRELOADER);
    if (paramApplet2Adapter != null)
    {
      this.hostAdapterRef = new WeakReference(paramApplet2Adapter);
      paramApplet2Adapter.addCleanupAction(new Runnable()
      {
        public void run()
        {
          PreloaderDelegate.this.shutdown();
        }
      });
    }
  }

  private Applet2Adapter getHostAdapter()
  {
    if (null == this.hostAdapterRef)
      return null;
    return (Applet2Adapter)this.hostAdapterRef.get();
  }

  public void setPreloaderClass(String paramString)
  {
    this.preloaderClassName = paramString;
  }

  public Preloader get()
  {
    if (this._plRef == null)
      return null;
    Object localObject = this._plRef.get();
    if (localObject == null)
    {
      Trace.println("Referenced preloader is cleared.", TraceLevel.PRELOADER);
      return null;
    }
    return (Preloader)localObject;
  }

  private synchronized void set(Preloader paramPreloader)
  {
    if (this.plNeedStrongRef)
      this._plStrongRef = paramPreloader;
    this._plRef = new WeakReference(paramPreloader);
  }

  public PreloaderDelegate(Preloader paramPreloader)
  {
    set(paramPreloader);
    this._appThreadGroup = Thread.currentThread().getThreadGroup();
    markLoaded(null);
    markReady();
  }

  public void initPreloader(ClassLoader paramClassLoader, ThreadGroup paramThreadGroup)
  {
    ToolkitStore.get().getAppContext().put("preloader_key", this);
    2 local2 = new Runnable(paramClassLoader)
    {
      private final ClassLoader val$cl;

      public void run()
      {
        PreloaderDelegate.this.doInitPreloader(this.val$cl);
      }
    };
    Thread localThread = new Thread(paramThreadGroup, local2, "preloaderMain");
    localThread.setDaemon(true);
    localThread.start();
    try
    {
      localThread.join();
    }
    catch (InterruptedException localInterruptedException)
    {
    }
    finally
    {
      this._appThreadGroup = paramThreadGroup;
      markReady();
    }
  }

  private Object createUsingDefaultConstructor(Class paramClass)
    throws Exception
  {
    return paramClass.getConstructor(new Class[0]).newInstance(new Object[0]);
  }

  private void doInitPreloader(ClassLoader paramClassLoader)
  {
    Object localObject1 = null;
    try
    {
      DeployPerfUtil.put("Preloader constructor started");
      Trace.println("Using preloader class: " + this.preloaderClassName + " " + getHostAdapter(), TraceLevel.PRELOADER);
      if (this.preloaderClassName != null)
      {
        isUserDeclinedPreloader.set(Boolean.FALSE);
        Class localClass = Class.forName(this.preloaderClassName, false, paramClassLoader);
        if (isUserDeclinedPreloader.get() != Boolean.TRUE)
        {
          Trace.println("User accept signed preloader or preloader not signed", TraceLevel.PRELOADER);
          if (getHostAdapter() != null)
          {
            set(getHostAdapter().instantiatePreloader(localClass));
          }
          else if (Preloader.class.isAssignableFrom(localClass))
          {
            Trace.println("Preloader class: " + this.preloaderClassName, TraceLevel.PRELOADER);
            set((Preloader)createUsingDefaultConstructor(localClass));
          }
          else if (DownloadServiceListener.class.isAssignableFrom(localClass))
          {
            Trace.println("CustomProgress: " + this.preloaderClassName, TraceLevel.PRELOADER);
            DownloadServiceListener localDownloadServiceListener = (DownloadServiceListener)createUsingDefaultConstructor(localClass);
            set(new CustomProgress2PreloaderAdapter(localDownloadServiceListener));
          }
          else
          {
            Trace.println("Preloader class of unknown type. Ignoring it.", TraceLevel.PRELOADER);
          }
        }
      }
    }
    catch (Exception localCancelException2)
    {
      Trace.print("Failed to init custom preloader", TraceLevel.PRELOADER);
      Trace.ignoredException(localException);
      localObject1 = localException;
    }
    finally
    {
      if (get() == null)
      {
        installDefaultPreloader();
        if (localObject1 != null)
          try
          {
            handleEvent(new ErrorEvent(null, "Failed to initialize custom preloader", (Throwable)localObject1));
            filterPendingEventsOnError();
          }
          catch (CancelException localCancelException3)
          {
          }
      }
      isUserDeclinedPreloader.set(null);
    }
  }

  private synchronized void installDefaultPreloader()
  {
    Trace.println("Using default preloader", TraceLevel.PRELOADER);
    if (getHostAdapter() != null)
      set(getHostAdapter().instantiatePreloader(null));
    else
      set(ToolkitStore.get().getDefaultPreloader());
  }

  public ComponentRef getOwnerRef()
  {
    return new ComponentRef()
    {
      public Object get()
      {
        return PreloaderDelegate.this.getOwner();
      }
    };
  }

  public Object getOwner()
  {
    if (get() == null)
      return null;
    return get().getOwner();
  }

  private boolean canStop()
  {
    synchronized (this.queue)
    {
      return this.canStop;
    }
  }

  private void markCanStop(boolean paramBoolean)
  {
    synchronized (this.queue)
    {
      this.canStop = paramBoolean;
      this.queue.notifyAll();
    }
  }

  private synchronized void startProcessingIfNeeded()
  {
    if (this.progressThread == null)
    {
      if (this.checker == null)
        this.checker = new ProgressQueueChecker();
      this.progressThread = new Thread(this._appThreadGroup, this.checker, "ProgressReporter");
      this.progressThread.setDaemon(true);
      this.progressThread.start();
    }
    markCanStop(false);
  }

  private synchronized void markReady()
  {
    this.isReady = true;
    ListIterator localListIterator = this.pendingEvents.listIterator();
    Object localObject = null;
    while (localListIterator.hasNext())
    {
      PreloaderEvent localPreloaderEvent1 = (PreloaderEvent)localListIterator.next();
      if ((localPreloaderEvent1 instanceof DownloadEvent))
        localObject = localPreloaderEvent1;
    }
    localListIterator = this.pendingEvents.listIterator();
    for (int i = 0; localListIterator.hasNext(); i++)
    {
      PreloaderEvent localPreloaderEvent2 = (PreloaderEvent)localListIterator.next();
      if (((localPreloaderEvent2 instanceof DownloadEvent)) && (localPreloaderEvent2 != localObject))
        continue;
      try
      {
        deliver(localPreloaderEvent2);
      }
      catch (CancelException localCancelException)
      {
        Trace.println("CancelException when preloader is being created. " + localCancelException, TraceLevel.PRELOADER);
      }
    }
    Trace.println("Skipped all (" + i + ") download events prior to " + localObject, TraceLevel.PRELOADER);
    this.pendingEvents.clear();
  }

  public synchronized void waitTillLoaded()
    throws IOException, JNLPException
  {
    Trace.println("Enter wait for preloader jars to be loaded ", TraceLevel.PRELOADER);
    while ((!this.isLoaded) && (this.loadingException == null))
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException)
      {
        if (!getHostAdapter().isAlive())
          return;
      }
    Trace.println("Done with loading of preloader jars. Error=" + this.loadingException, TraceLevel.PRELOADER);
    if (this.loadingException != null)
    {
      if ((this.loadingException instanceof IOException))
        throw ((IOException)this.loadingException);
      if ((this.loadingException instanceof JNLPException))
        throw ((JNLPException)this.loadingException);
      if ((this.loadingException instanceof RuntimeException))
      {
        if ((this.loadingException.getCause() instanceof IOException))
          throw ((IOException)this.loadingException.getCause());
        if ((this.loadingException.getCause() instanceof JNLPException))
          throw ((JNLPException)this.loadingException.getCause());
        throw new RuntimeException(this.loadingException);
      }
      throw new RuntimeException(this.loadingException);
    }
  }

  public synchronized void markLoadingStarted()
  {
    this.isLoaded = false;
  }

  public synchronized void markLoaded(Exception paramException)
  {
    Trace.println("Preloader jars loaded. Error state=" + paramException, TraceLevel.PRELOADER);
    this.isLoaded = true;
    this.loadingException = paramException;
    notifyAll();
  }

  public void upgradingArchive(URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    try
    {
      handleEvent(new DownloadEvent(2, paramURL, paramString, null, paramInt1, paramInt2, paramInt2));
    }
    catch (CancelException localCancelException)
    {
      throw new RuntimeException(this.ce);
    }
  }

  public void progress(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
  {
    try
    {
      handleEvent(new DownloadEvent(0, paramURL, paramString, null, paramLong1, paramLong2, paramInt));
    }
    catch (CancelException localCancelException)
    {
      throw new RuntimeException(this.ce);
    }
  }

  public void validating(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt)
  {
    try
    {
      handleEvent(new DownloadEvent(1, paramURL, paramString, null, paramLong1, paramLong2, paramInt));
    }
    catch (CancelException localCancelException)
    {
      throw new RuntimeException(this.ce);
    }
  }

  public void downloadFailed(URL paramURL, String paramString)
  {
    try
    {
      handleEvent(new DownloadErrorEvent(paramURL, paramString));
    }
    catch (CancelException localCancelException)
    {
      throw new RuntimeException(this.ce);
    }
  }

  public void extensionDownload(String paramString, int paramInt)
  {
  }

  public void jreDownload(String paramString, URL paramURL)
  {
  }

  public void setHeading(String paramString, boolean paramBoolean)
  {
  }

  public void setStatus(String paramString)
  {
  }

  public void setVisible(boolean paramBoolean)
  {
  }

  public void setProgressBarVisible(boolean paramBoolean)
  {
  }

  public void setProgressBarValue(int paramInt)
  {
  }

  public void forceFlushForTCK()
  {
    while (true)
      synchronized (this.queue)
      {
        if (this.queue.isEmpty())
          return;
        try
        {
          this.queue.wait(50L);
        }
        catch (InterruptedException localInterruptedException)
        {
          if (!getHostAdapter().isAlive())
            return;
        }
      }
  }

  private void shutdown()
  {
    synchronized (this.queue)
    {
      this.queue.clear();
      this.queue.notifyAll();
    }
    this.checker = null;
    this.progressThread = null;
    this.disposed = true;
  }

  private synchronized boolean isReady()
  {
    return this.isReady;
  }

  private void enQueue(Runnable paramRunnable)
  {
    synchronized (this.queue)
    {
      if (!this.disposed)
      {
        this.queue.add(paramRunnable);
        this.queue.notifyAll();
      }
    }
  }

  synchronized void setPendingException(CancelException paramCancelException)
  {
    this.ce = paramCancelException;
  }

  private synchronized CancelException getPendingException()
  {
    return this.ce;
  }

  private void deliver(PreloaderEvent paramPreloaderEvent)
    throws CancelException
  {
    CancelException localCancelException = getPendingException();
    if (localCancelException != null)
    {
      setPendingException(null);
      throw localCancelException;
    }
    Preloader localPreloader = get();
    if (localPreloader != null)
    {
      startProcessingIfNeeded();
      if ((paramPreloaderEvent instanceof DownloadEvent))
      {
        DownloadEvent localDownloadEvent = (DownloadEvent)paramPreloaderEvent;
        if (this.rescaleBaseline == -1)
          this.rescaleBaseline = localDownloadEvent.getOverallPercentage();
        if (!localDownloadEvent.isExplicit())
          localDownloadEvent.normalize(this.rescaleBaseline);
      }
      Trace.println("Delivering: " + paramPreloaderEvent, TraceLevel.PRELOADER);
      enQueue(new Runnable(localPreloader, paramPreloaderEvent)
      {
        private final Preloader val$preloader;
        private final PreloaderEvent val$pe;

        public void run()
        {
          boolean bool = true;
          try
          {
            try
            {
              bool = this.val$preloader.handleEvent(this.val$pe);
            }
            catch (CancelException localCancelException)
            {
              PreloaderDelegate.this.setPendingException(localCancelException);
            }
            if (!bool)
              PreloaderDelegate.this.doDefaultEventProcessing(this.val$pe);
          }
          catch (Throwable localThrowable)
          {
            Trace.println("Preloader failed to handle " + this.val$pe, TraceLevel.PRELOADER);
            PreloaderDelegate.this.doDefaultEventProcessing(new ErrorEvent(null, localThrowable));
          }
          if ((this.val$pe instanceof UserDeclinedEvent))
            PreloaderDelegate.access$302(PreloaderDelegate.this, true);
          if (PreloaderDelegate.this.listenerRef != null)
          {
            PreloaderPostEventListener localPreloaderPostEventListener = (PreloaderPostEventListener)PreloaderDelegate.this.listenerRef.get();
            if (localPreloaderPostEventListener != null)
              localPreloaderPostEventListener.eventHandled(this.val$pe);
          }
          if ((this.val$pe instanceof ErrorEvent))
            PreloaderDelegate.this.markCanStop(true);
        }
      });
    }
    else
    {
      Trace.println("Dropping " + paramPreloaderEvent + " because preloader was not created", TraceLevel.PRELOADER);
    }
  }

  private void doDefaultEventProcessing(PreloaderEvent paramPreloaderEvent)
  {
    if (getHostAdapter() != null)
    {
      Object localObject;
      if ((paramPreloaderEvent instanceof ErrorEvent))
      {
        localObject = (ErrorEvent)paramPreloaderEvent;
        getHostAdapter().doShowError(((ErrorEvent)localObject).getValue(), ((ErrorEvent)localObject).getException(), this.seenUserDeclined);
      }
      if ((paramPreloaderEvent instanceof AppletInitEvent))
      {
        localObject = (AppletInitEvent)paramPreloaderEvent;
        if (((AppletInitEvent)localObject).getSubtype() == 3)
          getHostAdapter().doShowApplet();
      }
    }
  }

  private synchronized void filterPendingEventsOnError()
  {
    ListIterator localListIterator = this.pendingEvents.listIterator();
    while (localListIterator.hasNext())
    {
      Object localObject = localListIterator.next();
      if ((localObject instanceof DownloadEvent))
        localListIterator.remove();
    }
  }

  public synchronized boolean handleEvent(PreloaderEvent paramPreloaderEvent)
    throws CancelException
  {
    if ((paramPreloaderEvent instanceof DownloadEvent))
    {
      DownloadEvent localDownloadEvent = (DownloadEvent)paramPreloaderEvent;
      int i = localDownloadEvent.getOverallPercentage();
      if ((i == this.lastPercentage) && (i != 100))
        return true;
      this.lastPercentage = i;
    }
    if ((isReady()) && (get() != null))
    {
      deliver(paramPreloaderEvent);
    }
    else
    {
      this.pendingEvents.add(paramPreloaderEvent);
      Trace.println("Added pending event " + this.pendingEvents.size() + ": " + paramPreloaderEvent, TraceLevel.PRELOADER);
    }
    return true;
  }

  public class ProgressQueueChecker
    implements Runnable
  {
    static final int WAIT_CYCLES = 10;
    private int waitCycles;

    public ProgressQueueChecker()
    {
    }

    public void run()
    {
      Trace.println("Start progressCheck thread", TraceLevel.PRELOADER);
      Runnable localRunnable = null;
      this.waitCycles = 10;
      int i = 1;
      while (i != 0)
      {
        synchronized (PreloaderDelegate.this.queue)
        {
          if (PreloaderDelegate.this.queue.isEmpty())
            try
            {
              PreloaderDelegate.this.queue.wait(500L);
              this.waitCycles -= 1;
            }
            catch (InterruptedException localInterruptedException)
            {
              if (!PreloaderDelegate.this.getHostAdapter().isAlive())
                return;
            }
          else
            try
            {
              localRunnable = (Runnable)PreloaderDelegate.this.queue.remove(0);
            }
            catch (ClassCastException localClassCastException)
            {
              localClassCastException.printStackTrace();
            }
            finally
            {
              PreloaderDelegate.this.queue.notifyAll();
            }
        }
        try
        {
          if (localRunnable != null)
          {
            localRunnable.run();
            this.waitCycles = 10;
            localRunnable = null;
          }
        }
        catch (Exception )
        {
          System.err.println("Unexpected exception from progress handler: " + ???);
          ???.printStackTrace();
        }
        synchronized (PreloaderDelegate.this.queue)
        {
          i = ((!PreloaderDelegate.this.canStop()) && (this.waitCycles != 0)) || (!PreloaderDelegate.this.queue.isEmpty()) ? 1 : 0;
        }
      }
      Trace.println("Stop progressCheck thread", TraceLevel.PRELOADER);
      synchronized (PreloaderDelegate.this)
      {
        PreloaderDelegate.access$902(PreloaderDelegate.this, null);
      }
      int j = 0;
      synchronized (PreloaderDelegate.this.queue)
      {
        j = !PreloaderDelegate.this.queue.isEmpty() ? 1 : 0;
      }
      if (j != 0)
        PreloaderDelegate.this.startProcessingIfNeeded();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.progress.PreloaderDelegate
 * JD-Core Version:    0.6.0
 */