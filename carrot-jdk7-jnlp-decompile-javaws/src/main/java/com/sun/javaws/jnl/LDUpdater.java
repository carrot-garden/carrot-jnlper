package com.sun.javaws.jnl;

import com.sun.deploy.Environment;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.UpdateTracker;
import com.sun.deploy.net.offline.DeployOfflineManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.javaws.Globals;
import com.sun.javaws.Launcher;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.exceptions.FailedDownloadingResourceException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.exceptions.LaunchDescException;
import com.sun.javaws.progress.PreloaderDelegate;
import com.sun.javaws.ui.UpdateDialog;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLHandshakeException;

public class LDUpdater
{
  private LaunchDesc _ld = null;
  private boolean _updateAvailable;
  private boolean _updateDownloaded;
  private boolean _updateChecked;
  private boolean _checkAborted;
  private boolean _checkFaulted;
  private LocalApplicationProperties _lap = null;
  private volatile Thread backgroundUpdateThread = null;
  private Exception _exception = null;
  private int _numTasks = 0;
  private int _numTasksMax = 0;
  private boolean _checkDone;
  private RapidUpdateCheckerQueue queue = null;
  private static final String APPCONTEXT_LD_KEY = "deploy-mainlaunchdescinappcontext";
  private static int sequenceNumber = 0;

  public LDUpdater(LaunchDesc paramLaunchDesc)
  {
    this._ld = paramLaunchDesc;
    setMainLaunchDescInAppContext();
  }

  public boolean isCheckAborted()
  {
    return this._checkAborted;
  }

  public boolean isUpdateAvailable()
    throws Exception
  {
    boolean bool = Environment.isJavaWebStart();
    if (this._ld.isHttps())
      return isUpdateAvailable(bool, true, false);
    return isUpdateAvailable(bool, true, true);
  }

  public boolean isUpdateAvailable(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
    throws Exception
  {
    synchronized (this)
    {
      if (this._updateChecked)
        return this._updateAvailable;
    }
    try
    {
      if (this._ld.isApplicationDescriptor())
        startUpdateCheck(paramBoolean1, paramBoolean2, paramBoolean3);
      else
        this._updateAvailable = updateCheck(paramBoolean1, paramBoolean2, paramBoolean3);
      synchronized (this)
      {
        this._updateChecked = true;
        this._updateDownloaded = (!this._updateAvailable);
      }
      if ((this._ld.isInstaller()) && (this._updateAvailable) && (DownloadEngine.isBackgroundUpdateRequest()))
        setForceUpdateCheck();
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
      throw localException;
    }
    return this._updateAvailable;
  }

  public boolean isUpdateDownloaded()
  {
    return this._updateDownloaded;
  }

  public void downloadUpdate(boolean paramBoolean)
    throws Exception
  {
    if (this._updateAvailable)
      download(paramBoolean);
    synchronized (this)
    {
      this._updateAvailable = false;
      this._updateDownloaded = true;
    }
  }

  private void startUpdateCheck(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
    throws Exception
  {
    if (this._lap == null)
      this._lap = Cache.getLocalApplicationProperties(this._ld.getCanonicalHome());
    int i = this._ld.getUpdate().getCheck();
    if (i == 2)
      return;
    int j = Config.getIntProperty("deployment.javaws.update.timeout");
    boolean[] arrayOfBoolean = new boolean[4];
    new Thread(new Runnable(paramBoolean1, paramBoolean2, paramBoolean3, arrayOfBoolean)
    {
      private final boolean val$checkIcon;
      private final boolean val$checkLazy;
      private final boolean val$rapidCheck;
      private final boolean[] val$results;

      public void run()
      {
        int i = 0;
        int j = 0;
        boolean bool = false;
        int k = 0;
        try
        {
          if (LDUpdater.this.updateCheck(this.val$checkIcon, this.val$checkLazy, this.val$rapidCheck))
          {
            i = 1;
            LDUpdater.this.setForceUpdateCheck();
          }
          j = 1;
        }
        catch (FailedDownloadingResourceException localFailedDownloadingResourceException)
        {
          if (LDUpdater.this._ld.isHttps())
          {
            Throwable localThrowable = localFailedDownloadingResourceException.getWrappedException();
            if ((localThrowable != null) && ((localThrowable instanceof SSLHandshakeException)))
              k = 1;
          }
          Trace.ignored(localFailedDownloadingResourceException);
          bool = LDUpdater.this.checkException(localFailedDownloadingResourceException);
        }
        catch (Exception localException)
        {
          Trace.ignored(localException);
          bool = LDUpdater.this.checkException(localException);
        }
        synchronized (this.val$results)
        {
          this.val$results[0] = i;
          this.val$results[1] = j;
          this.val$results[2] = bool;
          this.val$results[3] = k;
          this.val$results.notifyAll();
        }
      }
    }
    , "" + this._ld.getLocation()).start();
    synchronized (arrayOfBoolean)
    {
      while ((arrayOfBoolean[1] == 0) && (arrayOfBoolean[2] == 0) && (arrayOfBoolean[3] == 0))
      {
        try
        {
          arrayOfBoolean.wait(j);
        }
        catch (InterruptedException localInterruptedException)
        {
          Trace.ignored(localInterruptedException);
        }
        if ((i != 1) || (this._ld.isHttps()))
          continue;
      }
      this._checkFaulted = arrayOfBoolean[2];
      this._checkAborted = arrayOfBoolean[3];
      this._updateAvailable = arrayOfBoolean[0];
    }
    if (this._checkFaulted)
      throw this._exception;
  }

  public synchronized boolean isBackgroundUpdateRunning()
  {
    return null != this.backgroundUpdateThread;
  }

  public void startBackgroundUpdateOpt()
  {
    if ((!Cache.isCacheEnabled()) || (DeployOfflineManager.isForcedOffline()))
      return;
    if (!this._ld.getUpdate().isBackgroundCheck())
      return;
    if (null != this.backgroundUpdateThread)
      return;
    startBackgroundUpdate();
  }

  public boolean needUpdatePerPolicy(PreloaderDelegate paramPreloaderDelegate)
    throws ExitException
  {
    boolean bool = false;
    switch (this._ld.getUpdate().getPolicy())
    {
    case 0:
    default:
      bool = true;
      break;
    case 1:
      bool = UpdateDialog.showUpdateDialog(this._ld, paramPreloaderDelegate);
      if (bool)
        break;
      Trace.println("Start from cached after user chose not to update", TraceLevel.BASIC);
      startBackgroundUpdate();
      break;
    case 2:
      bool = UpdateDialog.showUpdateDialog(this._ld, paramPreloaderDelegate);
      if (bool)
        break;
      Trace.println("Exiting after user chose not to update", TraceLevel.BASIC);
      throw new ExitException(new LaunchDescException(this._ld, "User cancelled mandatory update - aborted", null), 3);
    }
    return bool;
  }

  public boolean needUpdatePerPolicy()
    throws ExitException
  {
    return needUpdatePerPolicy(null);
  }

  private synchronized void startBackgroundUpdate()
  {
    startBackgroundUpdate(5000L);
  }

  protected synchronized void startBackgroundUpdate(long paramLong)
  {
    if (null != this.backgroundUpdateThread)
      return;
    Trace.println("LDUpdater: started background update check", TraceLevel.NETWORK);
    ArrayList localArrayList = buildBackgroundDownloadList();
    storeBackgroundListInAppContext(localArrayList);
    this.backgroundUpdateThread = new Thread(new Runnable(paramLong, localArrayList)
    {
      private final long val$delayMillis;
      private final ArrayList val$backgroundList;

      public void run()
      {
        try
        {
          DownloadEngine.setBackgroundUpdateRequest(true);
          try
          {
            Thread.sleep(this.val$delayMillis);
          }
          catch (InterruptedException localInterruptedException)
          {
          }
          int i = 0;
          if (LDUpdater.this.updateCheck(false, true, false))
            try
            {
              LDUpdater.this.download(true);
            }
            catch (Exception localException2)
            {
              i = 1;
              Trace.println("LDUpdater: exception in background update download, set force update check to true", TraceLevel.NETWORK);
              Trace.ignoredException(localException2);
              LDUpdater.this.setForceUpdateCheck();
            }
          if (i == 0)
            LDUpdater.this.resetForceUpdateCheck();
        }
        catch (Exception localException1)
        {
          Trace.ignoredException(localException1);
        }
        finally
        {
          LDUpdater.this.backgroundUpdateEnd(this.val$backgroundList);
        }
      }
    });
    this.backgroundUpdateThread.setName("Background Update Thread");
    this.backgroundUpdateThread.setPriority(1);
    this.backgroundUpdateThread.setDaemon(true);
    this.backgroundUpdateThread.start();
  }

  protected void backgroundUpdateEnd(List paramList)
  {
    removeBackgroundListInAppContext(paramList);
  }

  private boolean updateCheck(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
    throws Exception
  {
    if ((paramBoolean3) && (this.queue == null))
    {
      this.queue = getQueue();
      if (this.queue == null)
        paramBoolean3 = false;
    }
    Object localObject3;
    if (paramBoolean3)
    {
      localURL = this._ld.getLocation();
      if (localURL != null)
      {
        incrementTaskNum();
        this.queue.enqueue(new RapidUpdateChecker(localURL, 0, null));
      }
      ResourcesDesc localResourcesDesc1 = this._ld.getResources();
      if (localResourcesDesc1 != null)
      {
        JARDesc[] arrayOfJARDesc = localResourcesDesc1.getLocalJarDescs();
        for (int i = 0; i < arrayOfJARDesc.length; i++)
        {
          if ((!paramBoolean2) && (arrayOfJARDesc[i].isLazyDownload()))
            continue;
          incrementTaskNum();
          this.queue.enqueue(new RapidUpdateChecker(arrayOfJARDesc[i], 1, null));
        }
        if (paramBoolean1)
        {
          localObject1 = this._ld.getInformation().getIcons();
          if (localObject1 != null)
            for (k = 0; k < localObject1.length; k++)
            {
              incrementTaskNum();
              this.queue.enqueue(new RapidUpdateChecker(localObject1[k], 2, null));
            }
        }
        Object localObject1 = new ArrayList();
        getAllExtensions(localResourcesDesc1, (ArrayList)localObject1);
        for (k = 0; k < ((ArrayList)localObject1).size(); k++)
        {
          localObject3 = ((ExtensionDesc)((ArrayList)localObject1).get(k)).getExtensionDesc();
          incrementTaskNum();
          new Thread(new RapidUpdateChecker(localObject3, 3, null), "Rapid Update Checker- " + nextSequenceNumber()).start();
        }
      }
      synchronized (this)
      {
        while (true)
          if ((!this._checkDone) && (!this._updateAvailable))
            try
            {
              wait();
            }
            catch (InterruptedException localInterruptedException)
            {
              Trace.ignored(localInterruptedException);
            }
      }
      stopQueue();
      if ((!this._updateAvailable) && (this._exception != null))
        throw this._exception;
      return this._updateAvailable;
    }
    URL localURL = this._ld.getLocation();
    if (localURL != null)
      try
      {
        if (DownloadEngine.isUpdateAvailable(localURL, null))
          return true;
      }
      catch (IOException localIOException1)
      {
        throw new FailedDownloadingResourceException(localURL, null, localIOException1);
      }
    ResourcesDesc localResourcesDesc2 = this._ld.getResources();
    if (localResourcesDesc2 == null)
      return false;
    ??? = localResourcesDesc2.getLocalJarDescs();
    for (int j = 0; j < ???.length; j++)
      if (((paramBoolean2) || (!???[j].isLazyDownload())) && (???[j].getUpdater().isUpdateAvailable()))
        return true;
    if (paramBoolean1)
    {
      localObject2 = this._ld.getInformation().getIcons();
      if (localObject2 != null)
        for (k = 0; k < localObject2.length; k++)
        {
          localObject3 = localObject2[k].getLocation();
          String str = localObject2[k].getVersion();
          try
          {
            if (DownloadEngine.isUpdateAvailable((URL)localObject3, str))
              Globals.setIconImageUpdated(true);
          }
          catch (IOException localIOException2)
          {
            throw new FailedDownloadingResourceException((URL)localObject3, null, localIOException2);
          }
        }
    }
    Object localObject2 = new ArrayList();
    getAllExtensions(localResourcesDesc2, (ArrayList)localObject2);
    for (int k = 0; k < ((ArrayList)localObject2).size(); k++)
    {
      localObject3 = ((ExtensionDesc)((ArrayList)localObject2).get(k)).getExtensionDesc();
      try
      {
        if (((LaunchDesc)localObject3).getUpdater().isUpdateAvailable(paramBoolean1, paramBoolean2, paramBoolean3))
          return true;
      }
      catch (NullPointerException localNullPointerException)
      {
        Trace.ignored(localNullPointerException);
      }
    }
    return false;
  }

  private void getAllExtensions(ResourcesDesc paramResourcesDesc, ArrayList paramArrayList)
  {
    paramResourcesDesc.visit(new ResourceVisitor(paramArrayList)
    {
      private final ArrayList val$list;

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        this.val$list.add(paramExtensionDesc);
      }
    });
  }

  private void download(boolean paramBoolean)
    throws Exception, JNLPException
  {
    int i = DownloadEngine.incrementInternalUse();
    try
    {
      _download(paramBoolean);
    }
    finally
    {
      DownloadEngine.decrementInternalUse(i);
    }
  }

  private void _download(boolean paramBoolean)
    throws Exception, JNLPException
  {
    URL localURL1 = this._ld.getLocation();
    boolean bool = DownloadEngine.isUpdateAvailable(localURL1, null);
    if (bool)
    {
      localObject1 = DownloadEngine.getUpdatedFile(localURL1, null);
      if (!this._ld.hasIdenticalContent((File)localObject1))
        this._ld = LaunchDescFactory.buildDescriptor((File)localObject1, this._ld.getCodebase(), localURL1, localURL1);
      localObject2 = Cache.getLocalApplicationProperties(localURL1);
      if (localObject2 != null)
        Launcher.notifyLocalInstallHandler(this._ld, (LocalApplicationProperties)localObject2, true, true, null);
    }
    Object localObject1 = this._ld.getResources();
    if (localObject1 == null)
      return;
    Object localObject2 = ((ResourcesDesc)localObject1).getLocalJarDescs();
    ArrayList localArrayList = new ArrayList();
    if (paramBoolean)
      localArrayList.addAll(Arrays.asList(localObject2));
    else
      for (i = 0; i < localObject2.length; i++)
      {
        if (localObject2[i].isLazyDownload())
          continue;
        localArrayList.add(localObject2[i]);
      }
    JARDesc localJARDesc;
    for (int i = 0; i < localArrayList.size(); i++)
    {
      localJARDesc = (JARDesc)localArrayList.get(i);
      if (!localJARDesc.getUpdater().isUpdateAvailable())
        continue;
      localJARDesc.getUpdater().downloadUpdate();
    }
    for (i = 0; i < localArrayList.size(); i++)
    {
      localJARDesc = (JARDesc)localArrayList.get(i);
      if (!localJARDesc.getUpdater().isUpdateDownloaded())
        continue;
      localJARDesc.getUpdater().updateCache();
    }
    IconDesc localIconDesc = this._ld.getInformation().getIconLocation(48, 0);
    if (localIconDesc != null)
      try
      {
        DownloadEngine.getResource(localIconDesc.getLocation(), null, localIconDesc.getVersion(), null, true, 1);
        Trace.println("Downloaded " + localIconDesc.getLocation(), TraceLevel.NETWORK);
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
    ExtensionDesc[] arrayOfExtensionDesc = ((ResourcesDesc)localObject1).getExtensionDescs();
    for (int j = 0; j < arrayOfExtensionDesc.length; j++)
    {
      LaunchDesc localLaunchDesc = arrayOfExtensionDesc[j].getExtensionDesc();
      URL localURL2 = localLaunchDesc.getLocation();
      try
      {
        if (localLaunchDesc.getUpdater().isUpdateAvailable(false, paramBoolean, false))
          localLaunchDesc.getUpdater().downloadUpdate(paramBoolean);
      }
      catch (NullPointerException localNullPointerException)
      {
        Trace.ignored(localNullPointerException);
      }
      catch (IOException localIOException)
      {
        throw new FailedDownloadingResourceException(localURL2, null, localIOException);
      }
    }
  }

  private void setForceUpdateCheck()
  {
    if ((this._lap != null) && (!this._lap.forceUpdateCheck()))
    {
      this._lap.setForceUpdateCheck(true);
      try
      {
        this._lap.store();
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
  }

  private void resetForceUpdateCheck()
  {
    if ((this._lap != null) && (this._lap.forceUpdateCheck()))
    {
      this._lap.setForceUpdateCheck(false);
      try
      {
        this._lap.store();
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
  }

  private ArrayList buildBackgroundDownloadList()
  {
    ArrayList localArrayList = new ArrayList();
    try
    {
      URL localURL1 = this._ld.getLocation();
      if (localURL1 != null)
        localArrayList.add(localURL1.toString());
      ResourcesDesc localResourcesDesc = this._ld.getResources();
      if (localResourcesDesc != null)
      {
        ExtensionDesc[] arrayOfExtensionDesc = localResourcesDesc.getExtensionDescs();
        for (int i = 0; i < arrayOfExtensionDesc.length; i++)
        {
          URL localURL2 = arrayOfExtensionDesc[i].getLocation();
          if (localURL2 == null)
            continue;
          localArrayList.add(localURL2.toString());
        }
        JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(true);
        if (arrayOfJARDesc != null)
          for (int j = 0; j < arrayOfJARDesc.length; j++)
          {
            URL localURL3 = arrayOfJARDesc[j].getLocation();
            if (localURL3 == null)
              continue;
            localArrayList.add(localURL3.toString());
          }
        IconDesc[] arrayOfIconDesc = this._ld.getInformation().getIcons();
        if (arrayOfIconDesc != null)
          for (int k = 0; k < arrayOfIconDesc.length; k++)
          {
            URL localURL4 = arrayOfIconDesc[k].getLocation();
            if (localURL4 == null)
              continue;
            localArrayList.add(localURL4.toString());
          }
      }
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
    return localArrayList;
  }

  private void storeBackgroundListInAppContext(ArrayList paramArrayList)
  {
    if (paramArrayList == null)
      return;
    for (int i = 0; i < paramArrayList.size(); i++)
    {
      String str = (String)paramArrayList.get(i);
      if (str == null)
        continue;
      UpdateTracker.addPending(str);
      ToolkitStore.get().getAppContext().put("deploy-bg-" + str, "background");
    }
  }

  private void removeBackgroundListInAppContext(List paramList)
  {
    if (paramList == null)
      return;
    for (int i = 0; i < paramList.size(); i++)
    {
      String str = (String)paramList.get(i);
      if (str == null)
        continue;
      ToolkitStore.get().getAppContext().remove("deploy-bg-" + str);
    }
  }

  private static int nextSequenceNumber()
  {
    synchronized (LDUpdater.class)
    {
      return ++sequenceNumber;
    }
  }

  private LDUpdater getMainLDUpdater()
  {
    if (this._ld.isApplicationDescriptor())
      return this._ld.getUpdater();
    LaunchDesc localLaunchDesc = getMainLaunchDescFromAppContext();
    if (null != localLaunchDesc)
      return localLaunchDesc.getUpdater();
    return null;
  }

  private RapidUpdateCheckerQueue getQueue()
  {
    if (!this._ld.isApplicationDescriptor())
    {
      LDUpdater localLDUpdater = getMainLDUpdater();
      if (localLDUpdater != null)
        return localLDUpdater.getQueue();
      return null;
    }
    if (this.queue == null)
    {
      int i = getNumTasksMax();
      this.queue = new RapidUpdateCheckerQueue(i, null);
      new Thread(this.queue, "Rapid Update Checker Queue").start();
    }
    return this.queue;
  }

  private void stopQueue()
  {
    if (!this._ld.isApplicationDescriptor())
      return;
    if (this.queue != null)
      this.queue.stop();
  }

  private int getNumTasksMax()
  {
    if (this._numTasksMax != 0)
      return this._numTasksMax;
    if (this._ld.isApplicationDescriptor())
    {
      this._numTasksMax = this._ld.getResources().getConcurrentDownloads();
      return this._numTasksMax;
    }
    LaunchDesc localLaunchDesc = getMainLaunchDescFromAppContext();
    if (localLaunchDesc != null)
      return localLaunchDesc.getUpdater().getNumTasksMax();
    return 4;
  }

  private void setMainLaunchDescInAppContext()
  {
    if (this._ld.isApplicationDescriptor())
      ToolkitStore.get().getAppContext().put("deploy-mainlaunchdescinappcontext", this._ld);
  }

  private LaunchDesc getMainLaunchDescFromAppContext()
  {
    Object localObject = ToolkitStore.get().getAppContext().get("deploy-mainlaunchdescinappcontext");
    if (null != localObject)
      return (LaunchDesc)localObject;
    return null;
  }

  private synchronized void incrementTaskNum()
  {
    this._numTasks += 1;
  }

  private synchronized void notifyUpdate(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      this._updateAvailable = true;
      notifyAll();
    }
    else if (--this._numTasks == 0)
    {
      this._checkDone = true;
      notifyAll();
    }
  }

  private synchronized void notifyException(Exception paramException)
  {
    if ((this._ld != null) && (this._ld.getInformation().supportsOfflineOperation()))
    {
      Trace.ignoredException(paramException);
      return;
    }
    if (this._exception == null)
      this._exception = paramException;
  }

  private boolean checkException(Exception paramException)
  {
    if ((this._ld.getUpdate().getCheck() == 1) && (this._ld.getInformation().supportsOfflineOperation()) && ((paramException instanceof JNLPException)))
    {
      Throwable localThrowable = ((JNLPException)paramException).getWrappedException();
      if ((localThrowable instanceof IOException))
        return false;
    }
    return true;
  }

  private class RapidUpdateChecker
    implements Runnable
  {
    private static final int HOME_URL = 0;
    private static final int JAR_DESC = 1;
    private static final int ICON_DESC = 2;
    private static final int EXT_LD = 3;
    private Object target;
    private int type;
    private boolean update;
    private final LDUpdater this$0;

    private RapidUpdateChecker(Object paramInt, int arg3)
    {
      this.this$0 = this$1;
      this.update = false;
      this.target = paramInt;
      int i;
      this.type = i;
    }

    public void run()
    {
      try
      {
        switch (this.type)
        {
        case 0:
          URL localURL1 = (URL)this.target;
          Trace.println("LDUpdater: update check for " + localURL1, TraceLevel.NETWORK);
          try
          {
            this.update = DownloadEngine.isUpdateAvailable(localURL1, null);
          }
          catch (IOException localIOException1)
          {
            throw new FailedDownloadingResourceException(localURL1, null, localIOException1);
          }
        case 1:
          JARDesc localJARDesc = (JARDesc)this.target;
          Trace.println("LDUpdater: update check for " + localJARDesc.getLocation(), TraceLevel.NETWORK);
          this.update = localJARDesc.getUpdater().isUpdateAvailable();
          break;
        case 2:
          IconDesc localIconDesc = (IconDesc)this.target;
          URL localURL2 = localIconDesc.getLocation();
          String str = localIconDesc.getVersion();
          try
          {
            if (DownloadEngine.isUpdateAvailable(localURL2, str))
              Globals.setIconImageUpdated(true);
          }
          catch (IOException localIOException2)
          {
            throw new FailedDownloadingResourceException(localURL2, null, localIOException2);
          }
        case 3:
          LaunchDesc localLaunchDesc = (LaunchDesc)this.target;
          Trace.println("LDUpdater: update check for " + localLaunchDesc.getLocation(), TraceLevel.NETWORK);
          this.update = localLaunchDesc.getUpdater().isUpdateAvailable();
        }
      }
      catch (Exception localException)
      {
        this.this$0.notifyException(localException);
      }
      finally
      {
        this.this$0.notifyUpdate(this.update);
        LDUpdater.RapidUpdateCheckerQueue.access$1400(this.this$0.queue);
      }
    }

    RapidUpdateChecker(Object paramInt, int param1, LDUpdater.1 arg4)
    {
      this(paramInt, param1);
    }
  }

  private class RapidUpdateCheckerQueue
    implements Runnable
  {
    private static final String RAPID_CHECK_THREAD_NAME = "Rapid Update Checker- ";
    private volatile boolean shouldStop;
    private final Object lock;
    private LinkedList workQueue;
    private int nThreads;
    private int nThreadsMax;
    private final LDUpdater this$0;

    private RapidUpdateCheckerQueue(int arg2)
    {
      this.this$0 = this$1;
      this.lock = new Object();
      this.workQueue = new LinkedList();
      this.nThreads = 0;
      int i;
      this.nThreadsMax = i;
    }

    private void enqueue(Runnable paramRunnable)
    {
      synchronized (this.lock)
      {
        this.workQueue.add(paramRunnable);
        this.lock.notifyAll();
      }
    }

    private void stop()
    {
      this.shouldStop = true;
      synchronized (this.lock)
      {
        this.lock.notifyAll();
      }
    }

    public void run()
    {
      try
      {
        if (!this.shouldStop)
        {
          synchronized (this.lock)
          {
            while ((!this.shouldStop) && (this.workQueue.isEmpty()))
              try
              {
                this.lock.wait();
              }
              catch (InterruptedException localInterruptedException1)
              {
                localInterruptedException1.printStackTrace();
              }
          }
          while ((!this.shouldStop) && (!this.workQueue.isEmpty()))
          {
            ??? = null;
            synchronized (this.lock)
            {
              ??? = (Runnable)this.workQueue.removeFirst();
            }
            synchronized (this)
            {
              while (this.nThreads >= this.nThreadsMax)
                try
                {
                  wait();
                }
                catch (InterruptedException localInterruptedException2)
                {
                  localInterruptedException2.printStackTrace();
                }
              this.nThreads += 1;
            }
            new Thread((Runnable)???, "Rapid Update Checker- " + LDUpdater.access$1000()).start();
          }
        }
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
    }

    private synchronized void decreaseNumTasks()
    {
      this.nThreads -= 1;
      notifyAll();
    }

    RapidUpdateCheckerQueue(int param1, LDUpdater.1 arg3)
    {
      this(param1);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.LDUpdater
 * JD-Core Version:    0.6.0
 */