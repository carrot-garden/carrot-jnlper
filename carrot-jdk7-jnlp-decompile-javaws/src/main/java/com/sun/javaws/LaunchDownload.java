package com.sun.javaws;

import com.sun.applet2.preloader.CancelException;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.DownloadErrorEvent;
import com.sun.applet2.preloader.event.DownloadEvent;
import com.sun.deploy.Environment;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.DownloadEngine.DownloadDelegate;
import com.sun.deploy.perf.DeployPerfUtil;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.javaws.exceptions.BadFieldException;
import com.sun.javaws.exceptions.FailedDownloadingResourceException;
import com.sun.javaws.exceptions.JNLPException;
import com.sun.javaws.exceptions.JNLParseException;
import com.sun.javaws.exceptions.LaunchDescException;
import com.sun.javaws.exceptions.MissingFieldException;
import com.sun.javaws.exceptions.MultipleHostsException;
import com.sun.javaws.exceptions.NativeLibViolationException;
import com.sun.javaws.jnl.AppletDesc;
import com.sun.javaws.jnl.ApplicationDesc;
import com.sun.javaws.jnl.ExtensionDesc;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.InstallerDesc;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.JavaFXAppDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.ResourceVisitor;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.progress.PreloaderDelegate;
import com.sun.javaws.security.JNLPSignedResourcesHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class LaunchDownload
{
  private static boolean updateAvailable = false;
  private static int _numThread = 0;
  private static JNLPException _exception = null;
  private static final Object syncObj = new Object();
  public static final String APPCONTEXT_THREADPOOL_KEY = "deploy-launchdownloadthreadpoolinappcontext";

  public static LaunchDesc updateLaunchDescInCache(LaunchDesc paramLaunchDesc)
  {
    return updateLaunchDescInCache(paramLaunchDesc, null, null);
  }

  public static LaunchDesc updateLaunchDescInCache(LaunchDesc paramLaunchDesc, URL paramURL1, URL paramURL2)
  {
    if (!Cache.isCacheEnabled())
      return paramLaunchDesc;
    int i = paramLaunchDesc.getLocation() == null ? 1 : 0;
    URL localURL = i != 0 ? paramLaunchDesc.getCanonicalHome() : paramLaunchDesc.getLocation();
    try
    {
      File localFile = DownloadEngine.getCachedFile(localURL);
      if (localFile == null)
      {
        Cache.createOrUpdateCacheEntry(localURL, paramLaunchDesc.getBytes());
        return paramLaunchDesc;
      }
      Trace.println("Loaded descriptor from cache at: " + localURL, TraceLevel.BASIC);
      LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile, paramURL1, paramURL2, null);
      if (paramLaunchDesc.hasIdenticalContent(localLaunchDesc))
        return localLaunchDesc;
      Cache.createOrUpdateCacheEntry(localURL, paramLaunchDesc.getBytes());
      return paramLaunchDesc;
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    catch (BadFieldException localBadFieldException)
    {
      Trace.ignoredException(localBadFieldException);
    }
    catch (MissingFieldException localMissingFieldException)
    {
      Trace.ignoredException(localMissingFieldException);
    }
    catch (JNLParseException localJNLParseException)
    {
      Trace.ignoredException(localJNLParseException);
    }
    return paramLaunchDesc;
  }

  public static LaunchDesc getUpdatedLaunchDesc(URL paramURL1, URL paramURL2)
    throws JNLPException, IOException
  {
    if (paramURL1 == null)
      return null;
    boolean bool;
    try
    {
      bool = DownloadEngine.isUpdateAvailable(paramURL1, null);
    }
    catch (IOException localIOException)
    {
      Trace.ignored(localIOException);
      bool = false;
    }
    if (!bool)
    {
      Trace.println("Update JNLP: no update for: " + paramURL1, TraceLevel.BASIC);
      return null;
    }
    Trace.println("Update JNLP: " + paramURL1 + ", thisCodebase: " + paramURL2, TraceLevel.BASIC);
    File localFile = null;
    try
    {
      DownloadEngine.getResource(paramURL1, null, null, null, true);
      localFile = DownloadEngine.getCachedFile(paramURL1);
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      Trace.ignoredException(localFileNotFoundException);
    }
    try
    {
      if (localFile != null)
      {
        LaunchDesc localLaunchDesc = null;
        try
        {
          localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile, paramURL2, paramURL1, paramURL1);
          return localLaunchDesc;
        }
        catch (LaunchDescException localLaunchDescException)
        {
          localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile);
          if (localLaunchDesc == null)
            throw localLaunchDescException;
          return localLaunchDesc;
        }
      }
      return LaunchDescFactory.buildDescriptor(paramURL1, paramURL1);
    }
    catch (JNLPException localJNLPException)
    {
    }
    throw localJNLPException;
  }

  public static boolean isJnlpCached(LaunchDesc paramLaunchDesc)
  {
    try
    {
      return DownloadEngine.isResourceCached(paramLaunchDesc.getCanonicalHome(), null, null);
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
    return false;
  }

  public static boolean isInCache(LaunchDesc paramLaunchDesc)
  {
    return isInCache(paramLaunchDesc, false);
  }

  public static boolean isInCache(LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return true;
    try
    {
      if ((paramLaunchDesc.getLocation() != null) && (!DownloadEngine.isResourceCached(paramLaunchDesc.getLocation(), null, null)))
        return false;
      if (!paramBoolean)
      {
        boolean bool = getCachedExtensions(paramLaunchDesc);
        if (!bool)
          return false;
      }
      JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(false);
      for (int i = 0; i < arrayOfJARDesc.length; i++)
      {
        if (!DownloadEngine.isResourceCached(arrayOfJARDesc[i].getLocation(), null, arrayOfJARDesc[i].getVersion(), arrayOfJARDesc[i].isNativeLib() ? 272 : 1))
          return false;
        if (!DownloadEngine.isJarFileCorrupted(arrayOfJARDesc[i].getLocation(), arrayOfJARDesc[i].getVersion()))
          continue;
        DownloadEngine.removeCachedResource(arrayOfJARDesc[i].getLocation(), null, arrayOfJARDesc[i].getVersion());
        return false;
      }
    }
    catch (JNLPException localJNLPException)
    {
      Trace.ignoredException(localJNLPException);
      return false;
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
      return false;
    }
    return true;
  }

  private static void updateCheck(URL paramURL, String paramString, boolean paramBoolean)
  {
    updateCheck(paramURL, paramString, paramBoolean, false);
  }

  private static void updateCheck(URL paramURL, String paramString, boolean paramBoolean1, boolean paramBoolean2)
  {
    updateCheck(paramURL, paramString, paramBoolean1, paramBoolean2, false);
  }

  private static void updateCheck(URL paramURL, String paramString, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    if (paramString != null)
      return;
    synchronized (syncObj)
    {
      _numThread += 1;
    }
    new Thread(new Runnable(paramURL, paramString, paramBoolean3, paramBoolean2)
    {
      private final URL val$url;
      private final String val$version;
      private final boolean val$isPack200;
      private final boolean val$isIcon;

      public void run()
      {
        Object localObject1 = null;
        try
        {
          boolean bool = DownloadEngine.isUpdateAvailable(this.val$url, this.val$version, this.val$isPack200);
          if ((this.val$isIcon) && (bool))
            Globals.setIconImageUpdated(true);
          synchronized (LaunchDownload.syncObj)
          {
            if ((bool) && (!LaunchDownload.updateAvailable))
              LaunchDownload.access$102(true);
          }
        }
        catch (IOException bool)
        {
          localObject1 = new FailedDownloadingResourceException(this.val$url, this.val$version, ???);
        }
        finally
        {
          synchronized (LaunchDownload.syncObj)
          {
            if (LaunchDownload._exception == null)
              LaunchDownload.access$202((JNLPException)localObject1);
            LaunchDownload.access$310();
          }
        }
      }
    }).start();
  }

  public static boolean isUpdateAvailable(LaunchDesc paramLaunchDesc)
    throws JNLPException
  {
    URL localURL1 = paramLaunchDesc.getLocation();
    if (localURL1 != null)
      try
      {
        boolean bool = DownloadEngine.isUpdateAvailable(localURL1, null);
        if (bool)
          return true;
      }
      catch (IOException localIOException1)
      {
        throw new FailedDownloadingResourceException(localURL1, null, localIOException1);
      }
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return false;
    ExtensionDesc[] arrayOfExtensionDesc = localResourcesDesc.getExtensionDescs();
    for (int i = 0; i < arrayOfExtensionDesc.length; i++)
    {
      URL localURL2 = arrayOfExtensionDesc[i].getLocation();
      if (localURL2 == null)
        continue;
      updateCheck(localURL2, arrayOfExtensionDesc[i].getVersion(), false);
    }
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(true);
    Object localObject1;
    for (int j = 0; j < arrayOfJARDesc.length; j++)
    {
      URL localURL3 = arrayOfJARDesc[j].getLocation();
      localObject1 = arrayOfJARDesc[j].getVersion();
      try
      {
        if (DownloadEngine.isResourceCached(localURL3, null, (String)localObject1))
          updateCheck(localURL3, (String)localObject1, arrayOfJARDesc[j].isLazyDownload(), false, arrayOfJARDesc[j].isPack200Enabled());
      }
      catch (IOException localIOException2)
      {
        Trace.ignoredException(localIOException2);
      }
    }
    IconDesc[] arrayOfIconDesc = paramLaunchDesc.getInformation().getIcons();
    if (arrayOfIconDesc != null)
      for (int k = 0; k < arrayOfIconDesc.length; k++)
      {
        localObject1 = arrayOfIconDesc[k].getLocation();
        String str = arrayOfIconDesc[k].getVersion();
        try
        {
          if (DownloadEngine.getCachedFile((URL)localObject1, str) != null)
            updateCheck((URL)localObject1, str, false, true);
        }
        catch (IOException localIOException3)
        {
          Trace.ignoredException(localIOException3);
        }
      }
    while (_numThread > 0)
      synchronized (syncObj)
      {
        if (updateAvailable)
          break;
        if (_exception != null)
          throw _exception;
      }
    return updateAvailable;
  }

  public static void downloadExtensions(LaunchDesc paramLaunchDesc, Preloader paramPreloader, int paramInt, ArrayList paramArrayList)
    throws IOException, JNLPException
  {
    downloadExtensionsHelper(paramLaunchDesc, paramPreloader, paramInt, false, paramArrayList);
  }

  public static boolean getCachedExtensions(LaunchDesc paramLaunchDesc)
    throws IOException, JNLPException
  {
    return downloadExtensionsHelper(paramLaunchDesc, null, 0, true, null);
  }

  private static boolean downloadExtensionsHelper(LaunchDesc paramLaunchDesc, Preloader paramPreloader, int paramInt, boolean paramBoolean, ArrayList paramArrayList)
    throws IOException, JNLPException
  {
    int i = DownloadEngine.incrementInternalUse();
    try
    {
      boolean bool = _downloadExtensionsHelper(paramLaunchDesc, paramPreloader, paramInt, paramBoolean, paramArrayList);
      return bool;
    }
    finally
    {
      DownloadEngine.decrementInternalUse(i);
    }
    throw localObject;
  }

  private static boolean _downloadExtensionsHelper(LaunchDesc paramLaunchDesc, Preloader paramPreloader, int paramInt, boolean paramBoolean, ArrayList paramArrayList)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return true;
    String str1 = JREInfo.getKnownPlatforms();
    ArrayList localArrayList = new ArrayList();
    localResourcesDesc.visit(new ResourceVisitor(localArrayList)
    {
      private final ArrayList val$list;

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        this.val$list.add(paramExtensionDesc);
      }
    });
    paramInt += localArrayList.size();
    for (int i = 0; i < localArrayList.size(); i++)
    {
      ExtensionDesc localExtensionDesc = (ExtensionDesc)localArrayList.get(i);
      String str2 = localExtensionDesc.getName();
      if (str2 == null)
      {
        str2 = localExtensionDesc.getLocation().toString();
        int j = str2.lastIndexOf('/');
        if (j > 0)
          str2 = str2.substring(j + 1, str2.length());
      }
      paramInt--;
      if (paramPreloader != null)
        paramPreloader.handleEvent(new DownloadEvent(0, localExtensionDesc.getLocation(), localExtensionDesc.getVersion(), str2, paramInt, localArrayList.size(), localArrayList.size()));
      File localFile = DownloadEngine.getCachedFile(localExtensionDesc.getLocation(), localExtensionDesc.getVersion(), !paramBoolean, false, JREInfo.getKnownPlatforms());
      Trace.println("Downloaded extension: " + localExtensionDesc.getLocation() + "\n\tcodebase: " + localExtensionDesc.getCodebase() + "\n\tld parentCodebase: " + paramLaunchDesc.getCodebase() + "\n\tfile: " + localFile, TraceLevel.NETWORK);
      if (localFile == null)
        return false;
      LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile, localExtensionDesc.getCodebase(), localExtensionDesc.getLocation(), localExtensionDesc.getLocation());
      int k = 0;
      if (localLaunchDesc.getLaunchType() == 3)
      {
        k = 1;
      }
      else if (localLaunchDesc.getLaunchType() == 4)
      {
        localExtensionDesc.setInstaller(true);
        LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(localExtensionDesc.getLocation(), localExtensionDesc.getVersion(), false);
        k = !localLocalApplicationProperties.isExtensionInstalled() ? 1 : 0;
        if ((paramArrayList != null) && ((isUpdateAvailable(localLaunchDesc)) || (k != 0)))
          paramArrayList.add(localFile);
        if ((paramBoolean) && (k != 0))
          return false;
      }
      else
      {
        throw new MissingFieldException(localLaunchDesc.getSource(), "<component-desc>|<installer-desc>");
      }
      if (k == 0)
        continue;
      localExtensionDesc.setExtensionDesc(localLaunchDesc);
      boolean bool = downloadExtensionsHelper(localLaunchDesc, paramPreloader, paramInt, paramBoolean, paramArrayList);
      if (!bool)
        return false;
    }
    return true;
  }

  public static void downloadJRE(LaunchDesc paramLaunchDesc, Preloader paramPreloader, ArrayList paramArrayList)
    throws JNLPException, IOException
  {
    JREDesc localJREDesc = paramLaunchDesc.getResources().getSelectedJRE();
    String str1 = localJREDesc.getVersion();
    URL localURL = localJREDesc.getHref();
    boolean bool = localURL == null;
    if (localURL == null)
    {
      str2 = Config.getStringProperty("deployment.javaws.installURL");
      if (str2 != null)
        try
        {
          localURL = new URL(str2);
        }
        catch (MalformedURLException localMalformedURLException)
        {
        }
    }
    paramPreloader.handleEvent(new DownloadEvent(0, localURL, str1, str1, 0L, 1000L, 0));
    String str2 = JREInfo.getKnownPlatforms();
    File localFile = DownloadEngine.getUpdatedFile(localURL, str1, bool, str2);
    LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile, null, null, null);
    if (localLaunchDesc.getLaunchType() != 4)
      throw new MissingFieldException(localLaunchDesc.getSource(), "<installer-desc>");
    if (paramArrayList != null)
      paramArrayList.add(localFile);
    localJREDesc.setExtensionDesc(localLaunchDesc);
    downloadExtensionsHelper(localLaunchDesc, paramPreloader, 0, false, paramArrayList);
  }

  public static void downloadResource(LaunchDesc paramLaunchDesc, URL paramURL, String paramString, Preloader paramPreloader, boolean paramBoolean)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return;
    int i = localResourcesDesc.getConcurrentDownloads();
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getResource(paramURL, paramString);
    downloadJarFiles(arrayOfJARDesc, paramPreloader, paramBoolean, i);
  }

  public static void downloadParts(LaunchDesc paramLaunchDesc, String[] paramArrayOfString, Preloader paramPreloader, boolean paramBoolean)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return;
    int i = localResourcesDesc.getConcurrentDownloads();
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getPartJars(paramArrayOfString);
    downloadJarFiles(arrayOfJARDesc, paramPreloader, paramBoolean, i);
  }

  public static void downloadExtensionPart(LaunchDesc paramLaunchDesc, URL paramURL, String paramString, String[] paramArrayOfString, Preloader paramPreloader, boolean paramBoolean)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return;
    int i = localResourcesDesc.getConcurrentDownloads();
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getExtensionPart(paramURL, paramString, paramArrayOfString);
    downloadJarFiles(arrayOfJARDesc, paramPreloader, paramBoolean, i);
  }

  public static void downloadEagerorAll(LaunchDesc paramLaunchDesc, boolean paramBoolean1, Preloader paramPreloader, boolean paramBoolean2)
    throws IOException, JNLPException
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return;
    Object localObject1 = localResourcesDesc.getEagerOrAllJarDescs(paramBoolean1);
    if (!paramBoolean1)
    {
      JARDesc[] arrayOfJARDesc1 = localResourcesDesc.getEagerOrAllJarDescs(true);
      if (arrayOfJARDesc1.length != localObject1.length)
      {
        localObject2 = new HashSet(Arrays.asList(localObject1));
        int j = 0;
        for (int k = 0; k < arrayOfJARDesc1.length; k++)
        {
          URL localURL = arrayOfJARDesc1[k].getLocation();
          String str = arrayOfJARDesc1[k].getVersion();
          if ((!((HashSet)localObject2).contains(arrayOfJARDesc1[k])) && (DownloadEngine.getCachedJarFile(localURL, str) != null))
            j++;
          else
            arrayOfJARDesc1[k] = null;
        }
        if (j > 0)
        {
          JARDesc[] arrayOfJARDesc2 = new JARDesc[localObject1.length + j];
          System.arraycopy(localObject1, 0, arrayOfJARDesc2, 0, localObject1.length);
          int m = localObject1.length;
          for (int n = 0; n < arrayOfJARDesc1.length; n++)
          {
            if (arrayOfJARDesc1[n] == null)
              continue;
            arrayOfJARDesc2[(m++)] = arrayOfJARDesc1[n];
          }
          localObject1 = arrayOfJARDesc2;
        }
      }
    }
    int i = paramLaunchDesc.getResources().getConcurrentDownloads();
    Trace.println("LaunchDownload: concurrent downloads from LD: " + i, TraceLevel.NETWORK);
    downloadJarFiles(localObject1, paramPreloader, paramBoolean2, i);
    Object localObject2 = paramLaunchDesc.getInformation().getIconLocation(48, 0);
    if (localObject2 != null)
      try
      {
        DownloadEngine.getResource(((IconDesc)localObject2).getLocation(), null, ((IconDesc)localObject2).getVersion(), null, true, 1);
        Trace.println("Downloaded " + ((IconDesc)localObject2).getLocation(), TraceLevel.NETWORK);
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
  }

  public static void reverse(JARDesc[] paramArrayOfJARDesc)
  {
    int i = 0;
    for (int j = paramArrayOfJARDesc.length - 1; i < j; j--)
    {
      JARDesc localJARDesc = paramArrayOfJARDesc[i];
      paramArrayOfJARDesc[i] = paramArrayOfJARDesc[j];
      paramArrayOfJARDesc[j] = localJARDesc;
      i++;
    }
  }

  public static int getDownloadType(JARDesc paramJARDesc)
  {
    int i = 256;
    if (paramJARDesc.isNativeLib())
      i |= 16;
    if (paramJARDesc.isPack200Enabled())
      i |= 4096;
    if (paramJARDesc.isVersionEnabled())
      i |= 65536;
    return i;
  }

  public static void prepareCustomProgress(LaunchDesc paramLaunchDesc, PreloaderDelegate paramPreloaderDelegate, JNLPSignedResourcesHelper paramJNLPSignedResourcesHelper, Runnable paramRunnable1, Runnable paramRunnable2, boolean paramBoolean)
  {
    prepareCustomProgress(paramLaunchDesc, paramPreloaderDelegate, paramJNLPSignedResourcesHelper, paramRunnable1, paramRunnable2, paramBoolean, true);
  }

  static void prepareCustomProgress(LaunchDesc paramLaunchDesc, PreloaderDelegate paramPreloaderDelegate, JNLPSignedResourcesHelper paramJNLPSignedResourcesHelper, Runnable paramRunnable1, Runnable paramRunnable2, boolean paramBoolean1, boolean paramBoolean2)
  {
    DeployPerfUtil.put("begining of prepareCustomProgress()");
    paramPreloaderDelegate.setPreloaderClass(paramLaunchDesc.getProgressClassName());
    paramPreloaderDelegate.markLoadingStarted();
    3 local3 = new Runnable(paramBoolean1, paramLaunchDesc, paramPreloaderDelegate, paramRunnable1, paramJNLPSignedResourcesHelper, paramRunnable2)
    {
      private final boolean val$doUpdate;
      private final LaunchDesc val$ld;
      private final PreloaderDelegate val$delegate;
      private final Runnable val$okAction;
      private final JNLPSignedResourcesHelper val$signingHelper;
      private final Runnable val$failAction;

      public void run()
      {
        try
        {
          if (this.val$doUpdate)
            LaunchDownload.downloadProgressJars(this.val$ld, this.val$delegate);
          if (this.val$okAction != null)
            this.val$okAction.run();
          this.val$delegate.markLoaded(null);
          this.val$signingHelper.warmup();
        }
        catch (Exception localException2)
        {
          Exception localException2;
          if ((localException1 instanceof RuntimeException))
            localException2 = (localException1.getCause() instanceof Exception) ? (Exception)localException1.getCause() : localException1;
          Trace.println("Error preparing preloader : " + localException2, TraceLevel.PRELOADER);
          Trace.ignored(localException2);
          this.val$delegate.markLoaded(localException2);
          if (this.val$failAction != null)
            this.val$failAction.run();
        }
      }
    };
    if (paramBoolean2)
    {
      Thread localThread = new Thread(local3, "Loading Custom Progress");
      localThread.setDaemon(true);
      localThread.start();
    }
    else
    {
      local3.run();
    }
  }

  static void downloadProgressJars(LaunchDesc paramLaunchDesc, PreloaderDelegate paramPreloaderDelegate)
    throws IOException, JNLPException
  {
    ExecutorService localExecutorService = null;
    List localList = null;
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return;
    localExecutorService = getThreadPool(2);
    if (localExecutorService == null)
      return;
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(false);
    ArrayList localArrayList = new ArrayList(2);
    for (int i = 0; i < arrayOfJARDesc.length; i++)
    {
      JARDesc localJARDesc = arrayOfJARDesc[i];
      if (!localJARDesc.isProgressJar())
        continue;
      DownloadTask localDownloadTask = new DownloadTask(localJARDesc.getLocation(), null, localJARDesc.getVersion(), null, true, getDownloadType(localJARDesc), null, null, null);
      if (localArrayList.contains(localDownloadTask))
        continue;
      localArrayList.add(localDownloadTask);
    }
    if (localArrayList.size() > 0)
    {
      try
      {
        localList = localExecutorService.invokeAll(localArrayList);
      }
      catch (InterruptedException localInterruptedException)
      {
        Trace.ignored(localInterruptedException);
        localExecutorService.shutdownNow();
      }
      localExecutorService.shutdown();
      validateResults(localList, localArrayList, null);
    }
  }

  private static void downloadJarFiles(JARDesc[] paramArrayOfJARDesc, Preloader paramPreloader, boolean paramBoolean, int paramInt)
    throws JNLPException, IOException
  {
    if (paramArrayOfJARDesc == null)
      return;
    DeployPerfUtil.put("LaunchDownload.downloadJarFiles - begin");
    if (Globals.isReverseMode())
      reverse(paramArrayOfJARDesc);
    long l = 0L;
    DownloadCallbackHelper localDownloadCallbackHelper = new DownloadCallbackHelper(paramPreloader);
    int i = 0;
    int j = 1;
    for (int k = 0; k < paramArrayOfJARDesc.length; k++)
    {
      m = paramArrayOfJARDesc[k].getSize();
      if (paramArrayOfJARDesc[k].isProgressJar())
        continue;
      if (m > 0)
      {
        i++;
        l += m;
      }
      else
      {
        j = 0;
      }
    }
    k = 0;
    for (int m = 0; m < paramArrayOfJARDesc.length; m++)
    {
      int n = paramArrayOfJARDesc[m].getSize();
      if (paramArrayOfJARDesc[m].isProgressJar())
        continue;
      if (n <= 0)
        localDownloadCallbackHelper.register(paramArrayOfJARDesc[m].getLocation().toString(), paramArrayOfJARDesc[m].getVersion(), 0, 1.0D);
      else if (n > 0)
        localDownloadCallbackHelper.register(paramArrayOfJARDesc[m].getLocation().toString(), paramArrayOfJARDesc[m].getVersion(), n, 0.5D + n * i / l);
      k++;
    }
    if (j == 0)
      l = -1L;
    Trace.println("Total size to download: " + l, TraceLevel.NETWORK);
    if (l == 0L)
      return;
    localDownloadCallbackHelper.setTotalSize(l);
    localDownloadCallbackHelper.setNumOfJars(paramArrayOfJARDesc.length);
    int[] arrayOfInt = new int[1];
    arrayOfInt[0] = 0;
    ExecutorService localExecutorService = getThreadPool(paramInt);
    if (localExecutorService != null)
    {
      ToolkitStore.get().getAppContext().put("deploy-launchdownloadthreadpoolinappcontext", localExecutorService);
      localDownloadCallbackHelper.setNumOfJars(k);
    }
    ArrayList localArrayList = new ArrayList(paramArrayOfJARDesc.length);
    for (int i1 = 0; i1 < paramArrayOfJARDesc.length; i1++)
    {
      JARDesc localJARDesc = paramArrayOfJARDesc[i1];
      try
      {
        int i2 = getDownloadType(localJARDesc);
        Object localObject;
        if (localExecutorService == null)
        {
          localObject = DownloadEngine.getResource(localJARDesc.getLocation(), null, localJARDesc.getVersion(), localDownloadCallbackHelper, true, i2);
          arrayOfInt[0] += 1;
          localDownloadCallbackHelper.setJarsDone(arrayOfInt[0]);
          if ((Cache.isCacheEnabled()) && (localObject == null) && (!Environment.isImportMode()))
            throw new FailedDownloadingResourceException(null, localJARDesc.getLocation(), localJARDesc.getVersion(), null);
        }
        else if (!localJARDesc.isProgressJar())
        {
          localObject = new DownloadTask(localJARDesc.getLocation(), null, localJARDesc.getVersion(), localDownloadCallbackHelper, true, i2, paramPreloader, arrayOfInt, localDownloadCallbackHelper);
          if (!localArrayList.contains(localObject))
            localArrayList.add(localObject);
        }
      }
      catch (JNLPException localJNLPException)
      {
        if (paramPreloader != null)
          paramPreloader.handleEvent(new DownloadErrorEvent(localJARDesc.getLocation(), localJARDesc.getVersion()));
        throw localJNLPException;
      }
    }
    List localList = null;
    try
    {
      if (localExecutorService != null)
        localList = localExecutorService.invokeAll(localArrayList);
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
    if (localExecutorService != null)
    {
      ToolkitStore.get().getAppContext().remove("deploy-launchdownloadthreadpoolinappcontext");
      localExecutorService.shutdown();
      validateResults(localList, localArrayList, paramPreloader);
    }
    DeployPerfUtil.put("LaunchDownload.downloadJarFiles - end");
  }

  private static void validateResults(List paramList, ArrayList paramArrayList, Preloader paramPreloader)
    throws IOException, JNLPException
  {
    if (paramList != null)
    {
      int i = 0;
      Iterator localIterator = paramList.iterator();
      while (localIterator.hasNext())
      {
        Future localFuture = (Future)localIterator.next();
        URL localURL = ((DownloadTask)paramArrayList.get(i)).getURL();
        String str = ((DownloadTask)paramArrayList.get(i)).getVersion();
        i++;
        try
        {
          localFuture.get();
        }
        catch (ExecutionException localExecutionException)
        {
          Throwable localThrowable = localExecutionException.getCause();
          if (null != localThrowable)
          {
            if ((localThrowable instanceof IOException))
            {
              if (paramPreloader != null)
                paramPreloader.handleEvent(new DownloadErrorEvent(localURL, str, localThrowable));
              throw ((IOException)localThrowable);
            }
            if ((localThrowable instanceof JNLPException))
            {
              if (paramPreloader != null)
                paramPreloader.handleEvent(new DownloadErrorEvent(localURL, str));
              throw ((JNLPException)localThrowable);
            }
            throw new IOException("JNLP Jar download failure.");
          }
        }
        catch (InterruptedException localInterruptedException)
        {
          Trace.ignored(localInterruptedException);
        }
      }
    }
  }

  private static synchronized void notifyProgress(DownloadCallbackHelper paramDownloadCallbackHelper, int[] paramArrayOfInt, URL paramURL)
  {
    if ((paramArrayOfInt != null) && (paramDownloadCallbackHelper != null))
    {
      paramArrayOfInt[0] += 1;
      Trace.println("Download Progress: jarsDone: " + paramArrayOfInt[0], TraceLevel.NETWORK);
      paramDownloadCallbackHelper.jarDone(paramURL);
      paramDownloadCallbackHelper.setJarsDone(paramArrayOfInt[0]);
    }
  }

  private static ExecutorService getThreadPool(int paramInt)
  {
    if (Config.isJavaVersionAtLeast15())
    {
      ExecutorService localExecutorService = Executors.newFixedThreadPool(paramInt, new ThreadFactory()
      {
        public Thread newThread(Runnable paramRunnable)
        {
          Thread localThread = new Thread(paramRunnable);
          localThread.setDaemon(true);
          return localThread;
        }
      });
      return localExecutorService;
    }
    return null;
  }

  public static void checkJNLPSecurity(LaunchDesc paramLaunchDesc)
    throws MultipleHostsException, NativeLibViolationException
  {
    boolean[] arrayOfBoolean1 = new boolean[1];
    boolean[] arrayOfBoolean2 = new boolean[1];
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return;
    JARDesc localJARDesc = paramLaunchDesc.getResources().getMainJar(true);
    if (localJARDesc == null)
      return;
    checkJNLPSecurityHelper(paramLaunchDesc, localJARDesc.getLocation().getHost(), arrayOfBoolean2, arrayOfBoolean1);
    if (arrayOfBoolean2[0] != 0)
      throw new MultipleHostsException();
    if (arrayOfBoolean1[0] != 0)
      throw new NativeLibViolationException();
  }

  private static void checkJNLPSecurityHelper(LaunchDesc paramLaunchDesc, String paramString, boolean[] paramArrayOfBoolean1, boolean[] paramArrayOfBoolean2)
  {
    if (paramLaunchDesc.getSecurityModel() != 0)
      return;
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return;
    localResourcesDesc.visit(new ResourceVisitor(paramArrayOfBoolean1, paramString, paramArrayOfBoolean2)
    {
      private final boolean[] val$hostViolation;
      private final String val$host;
      private final boolean[] val$nativeLibViolation;

      public void visitJARDesc(JARDesc paramJARDesc)
      {
        String str = paramJARDesc.getLocation().getHost();
        this.val$hostViolation[0] = ((this.val$hostViolation[0] != 0) || (!this.val$host.equals(str)) ? 1 : false);
        this.val$nativeLibViolation[0] = ((this.val$nativeLibViolation[0] != 0) || (paramJARDesc.isNativeLib()) ? 1 : false);
      }

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        if ((this.val$hostViolation[0] == 0) && (this.val$nativeLibViolation[0] == 0))
        {
          LaunchDesc localLaunchDesc = paramExtensionDesc.getExtensionDesc();
          String str = paramExtensionDesc.getLocation().getHost();
          if ((localLaunchDesc != null) && (localLaunchDesc.getSecurityModel() == 0) && (this.val$hostViolation[0] == 0))
            LaunchDownload.access$500(localLaunchDesc, str, this.val$hostViolation, this.val$nativeLibViolation);
        }
      }
    });
  }

  public static long getCachedSize(LaunchDesc paramLaunchDesc)
  {
    long l = 0L;
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return l;
    JARDesc[] arrayOfJARDesc = localResourcesDesc.getEagerOrAllJarDescs(true);
    for (int i = 0; i < arrayOfJARDesc.length; i++)
      try
      {
        l += DownloadEngine.getCachedSize(arrayOfJARDesc[i].getLocation(), null, arrayOfJARDesc[i].getVersion(), null);
      }
      catch (IOException localIOException1)
      {
        Trace.ignoredException(localIOException1);
      }
    IconDesc[] arrayOfIconDesc = paramLaunchDesc.getInformation().getIcons();
    if (arrayOfIconDesc != null)
      for (int j = 0; j < arrayOfIconDesc.length; j++)
        try
        {
          l += DownloadEngine.getCachedSize(arrayOfIconDesc[j].getLocation(), null, arrayOfIconDesc[j].getVersion(), null);
        }
        catch (IOException localIOException2)
        {
          Trace.ignoredException(localIOException2);
        }
    return l;
  }

  static String getMainClassName(LaunchDesc paramLaunchDesc, boolean paramBoolean)
    throws IOException, JNLPException, LaunchDescException
  {
    String str1 = null;
    ApplicationDesc localApplicationDesc = paramLaunchDesc.getApplicationDescriptor();
    if (localApplicationDesc != null)
      str1 = localApplicationDesc.getMainClass();
    InstallerDesc localInstallerDesc = paramLaunchDesc.getInstallerDescriptor();
    if (localInstallerDesc != null)
      str1 = localInstallerDesc.getMainClass();
    AppletDesc localAppletDesc = paramLaunchDesc.getAppletDescriptor();
    if (localAppletDesc != null)
      str1 = localAppletDesc.getAppletClass();
    JavaFXAppDesc localJavaFXAppDesc = paramLaunchDesc.getJavaFXAppDescriptor();
    if (localJavaFXAppDesc != null)
      str1 = localJavaFXAppDesc.getMainClass();
    if ((str1 != null) && (str1.length() == 0))
      str1 = null;
    if (str1 != null)
      return str1;
    if (paramLaunchDesc.getResources() == null)
      return null;
    JARDesc localJARDesc = paramLaunchDesc.getResources().getMainJar(paramBoolean);
    if (localJARDesc == null)
      return null;
    JarFile localJarFile = null;
    try
    {
      localJarFile = new JarFile(DownloadEngine.getCachedResourceFilePath(localJARDesc.getLocation(), localJARDesc.getVersion()), false);
      if ((localJarFile != null) && (str1 == null) && (paramLaunchDesc.getLaunchType() != 2))
      {
        localObject1 = localJarFile.getManifest();
        str1 = localObject1 != null ? ((Manifest)localObject1).getMainAttributes().getValue("Main-Class") : null;
      }
      if (str1 == null)
        throw new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.nomainclassspec"), null);
      Object localObject1 = str1.replace('.', '/') + ".class";
      if (localJarFile.getEntry((String)localObject1) == null)
        throw new LaunchDescException(paramLaunchDesc, ResourceManager.getString("launch.error.nomainclass", str1, localJARDesc.getLocation().toString()), null);
      String str2 = str1;
      return str2;
    }
    finally
    {
      if (localJarFile != null)
        localJarFile.close();
    }
    throw localObject2;
  }

  public static boolean inCache(JARDesc paramJARDesc)
  {
    try
    {
      return DownloadEngine.isResourceCached(paramJARDesc.getLocation(), null, paramJARDesc.getVersion(), 256);
    }
    catch (IOException localIOException)
    {
      if (Config.getDeployDebug())
        Trace.ignored(localIOException);
    }
    return false;
  }

  private static class DownloadCallbackHelper
    implements DownloadEngine.DownloadDelegate
  {
    Preloader _preloader;
    long _totalSize = -1L;
    final ArrayList _records;
    int _numOfJars = 1;
    int _jarsDone = 0;

    public DownloadCallbackHelper(Preloader paramPreloader)
    {
      this._preloader = paramPreloader;
      this._records = new ArrayList();
    }

    public void register(String paramString1, String paramString2, int paramInt, double paramDouble)
    {
      LaunchDownload.ProgressRecord localProgressRecord = getProgressRecord(paramString1);
      if (localProgressRecord == null)
      {
        localProgressRecord = new LaunchDownload.ProgressRecord(paramString1, paramString2, paramInt);
        localProgressRecord.setWeight(paramDouble);
        synchronized (this._records)
        {
          this._records.add(localProgressRecord);
        }
      }
      else
      {
        localProgressRecord.setWeight(paramDouble);
        localProgressRecord.setSize(paramInt);
      }
    }

    public void setTotalSize(long paramLong)
    {
      this._totalSize = paramLong;
    }

    public void setNumOfJars(int paramInt)
    {
      this._numOfJars = paramInt;
    }

    public void setJarsDone(int paramInt)
    {
      this._jarsDone = paramInt;
    }

    public void downloading(URL paramURL, String paramString, int paramInt1, int paramInt2, boolean paramBoolean)
      throws CancelException
    {
      if (this._preloader != null)
      {
        String str = paramURL.toString();
        LaunchDownload.ProgressRecord localProgressRecord = getProgressRecord(str);
        if (localProgressRecord == null)
        {
          localProgressRecord = new LaunchDownload.ProgressRecord(str, paramString, paramInt2);
          synchronized (this._records)
          {
            this._records.add(localProgressRecord);
          }
        }
        else
        {
          localProgressRecord.setSize(paramInt2);
        }
        localProgressRecord.downloadProgress(paramInt1);
        int i = getOverallPercent();
        this._preloader.handleEvent(new DownloadEvent(0, paramURL, paramString, null, paramInt1, paramInt2, i));
      }
    }

    public void patching(URL paramURL, String paramString, int paramInt)
      throws CancelException
    {
      if (this._preloader != null)
      {
        String str = paramURL.toString();
        LaunchDownload.ProgressRecord localProgressRecord = getProgressRecord(str);
        if (localProgressRecord != null)
        {
          localProgressRecord.patchProgress(paramInt);
          int i = getOverallPercent();
          this._preloader.handleEvent(new DownloadEvent(2, paramURL, paramString, null, paramInt, 100L, i));
        }
      }
    }

    public void validating(URL paramURL, int paramInt1, int paramInt2)
      throws CancelException
    {
      if (this._preloader != null)
      {
        String str = paramURL.toString();
        LaunchDownload.ProgressRecord localProgressRecord = getProgressRecord(str);
        if (localProgressRecord != null)
        {
          localProgressRecord.validateProgress(paramInt1, paramInt2);
          int i = getOverallPercent();
          this._preloader.handleEvent(new DownloadEvent(1, paramURL, null, null, paramInt1, paramInt2, i));
        }
      }
    }

    public LaunchDownload.ProgressRecord getProgressRecord(String paramString)
    {
      synchronized (this._records)
      {
        Iterator localIterator = this._records.iterator();
        while (localIterator.hasNext())
        {
          LaunchDownload.ProgressRecord localProgressRecord = (LaunchDownload.ProgressRecord)localIterator.next();
          if ((paramString != null) && (paramString.equals(localProgressRecord.getUrl())))
            return localProgressRecord;
        }
      }
      return null;
    }

    public int getOverallPercent()
    {
      double d1 = 0.0D;
      double d2 = 0.0D;
      synchronized (this._records)
      {
        Iterator localIterator = this._records.iterator();
        while (localIterator.hasNext())
        {
          LaunchDownload.ProgressRecord localProgressRecord = (LaunchDownload.ProgressRecord)localIterator.next();
          d1 += localProgressRecord.getPercent() * localProgressRecord.getWeight();
          d2 += localProgressRecord.getWeight();
        }
      }
      int i = (int)(d1 * 100.0D / d2);
      if (i > 100)
        i = 100;
      return i;
    }

    public void downloadFailed(URL paramURL, String paramString)
    {
      if (this._preloader != null)
        try
        {
          this._preloader.handleEvent(new DownloadErrorEvent(paramURL, paramString));
        }
        catch (CancelException localCancelException)
        {
        }
    }

    void jarDone(URL paramURL)
    {
      if (this._preloader != null)
      {
        String str = paramURL.toString();
        LaunchDownload.ProgressRecord localProgressRecord = getProgressRecord(str);
        if ((localProgressRecord != null) && (localProgressRecord.getPercent() < 1.0D))
        {
          LaunchDownload.ProgressRecord.access$400(localProgressRecord);
          int i = getOverallPercent();
        }
      }
    }
  }

  private static class DownloadTask
    implements Callable
  {
    private URL url;
    private int downloadType;
    private String resourceID;
    private String versionString;
    private DownloadEngine.DownloadDelegate dd;
    private final boolean doDownload;
    private Preloader dp;
    private int[] counterBox;
    private LaunchDownload.DownloadCallbackHelper dch;

    public DownloadTask(URL paramURL, String paramString1, String paramString2, DownloadEngine.DownloadDelegate paramDownloadDelegate, boolean paramBoolean, int paramInt, Preloader paramPreloader, int[] paramArrayOfInt, LaunchDownload.DownloadCallbackHelper paramDownloadCallbackHelper)
    {
      this.url = paramURL;
      this.downloadType = paramInt;
      this.resourceID = paramString1;
      this.versionString = paramString2;
      this.dd = paramDownloadDelegate;
      this.doDownload = paramBoolean;
      this.dp = paramPreloader;
      this.counterBox = paramArrayOfInt;
      this.dch = paramDownloadCallbackHelper;
    }

    public URL getURL()
    {
      return this.url;
    }

    public String getVersion()
    {
      return this.versionString;
    }

    public int hashCode()
    {
      if (this.url == null)
        return 0;
      return this.url.hashCode();
    }

    public String toString()
    {
      return this.url.toString() + (this.versionString != null ? ":" + this.versionString : "");
    }

    public boolean equals(Object paramObject)
    {
      if ((paramObject instanceof DownloadTask))
      {
        DownloadTask localDownloadTask = (DownloadTask)paramObject;
        URL localURL = localDownloadTask.getURL();
        String str = localDownloadTask.getVersion();
        if (this.url.toString().equals(localURL.toString()))
        {
          if ((this.versionString == null) && (str == null))
            return true;
          if ((this.versionString != null) && (str != null) && (this.versionString.equals(str)))
            return true;
        }
      }
      return false;
    }

    public Object call()
      throws IOException, JNLPException
    {
      int i = DownloadEngine.incrementInternalUse();
      try
      {
        URL localURL = DownloadEngine.getResource(this.url, this.resourceID, this.versionString, this.dch, this.doDownload, this.downloadType);
        if ((Cache.isCacheEnabled()) && (localURL == null) && (!Environment.isImportMode()))
          throw new FailedDownloadingResourceException(null, this.url, this.versionString, null);
        LaunchDownload.access$600(this.dch, this.counterBox, this.url);
      }
      finally
      {
        DownloadEngine.decrementInternalUse(i);
      }
      return null;
    }
  }

  private static class ProgressRecord
  {
    private String _url;
    private String _ver;
    private int _size;
    private double _percent;
    private double _weight;

    public ProgressRecord(String paramString1, String paramString2, int paramInt)
    {
      this._url = paramString1;
      this._ver = paramString2;
      this._size = paramInt;
      this._weight = 1.0D;
      this._percent = 0.0D;
    }

    public void setWeight(double paramDouble)
    {
      this._weight = paramDouble;
    }

    public void setSize(int paramInt)
    {
      this._size = paramInt;
    }

    public double getPercent()
    {
      return this._percent;
    }

    public String getUrl()
    {
      return this._url;
    }

    public int hashCode()
    {
      int i = 7;
      i = 79 * i + (this._url != null ? this._url.hashCode() : 0);
      return i;
    }

    public boolean equals(Object paramObject)
    {
      return this._url.equals(((ProgressRecord)paramObject)._url);
    }

    public double getWeight()
    {
      return this._weight;
    }

    public void downloadProgress(int paramInt)
    {
      if (this._size != 0)
        this._percent = (paramInt / this._size * 0.8D);
      else
        this._percent = 0.8D;
    }

    public void patchProgress(int paramInt)
    {
      this._percent = (paramInt / 100.0D * 0.1D + 0.8D);
    }

    public void validateProgress(int paramInt1, int paramInt2)
    {
      if (paramInt2 != 0)
        this._percent = (paramInt1 / paramInt2 * 0.05D + 0.9D);
      else
        this._percent = 0.95D;
    }

    private void markComplete()
    {
      this._percent = 1.0D;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.LaunchDownload
 * JD-Core Version:    0.6.0
 */