package com.sun.jnlp;

import com.sun.applet2.preloader.CancelException;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.ConfigEvent;
import com.sun.applet2.preloader.event.InitEvent;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.javaws.CacheUtil;
import com.sun.javaws.LaunchDownload;
import com.sun.javaws.jnl.ExtensionDesc;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.ResourceVisitor;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.progress.CustomProgress2PreloaderAdapter;
import com.sun.javaws.progress.PreloaderDelegate;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import javax.jnlp.DownloadService;
import javax.jnlp.DownloadServiceListener;

public final class DownloadServiceImpl
  implements DownloadService
{
  private static DownloadServiceImpl _sharedInstance = null;
  private DownloadServiceListener _defaultProgressHelper = null;

  static synchronized void reset()
  {
    _sharedInstance = null;
  }

  public static synchronized DownloadServiceImpl getInstance()
  {
    initialize();
    return _sharedInstance;
  }

  public static synchronized void initialize()
  {
    if (_sharedInstance == null)
      _sharedInstance = new DownloadServiceImpl();
  }

  public DownloadServiceListener getDefaultProgressWindow()
  {
    if (this._defaultProgressHelper == null)
      this._defaultProgressHelper = ((DownloadServiceListener)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          Preloader localPreloader = ToolkitStore.get().getDefaultPreloader();
          try
          {
            localPreloader.handleEvent(new ConfigEvent(3, JNLPClassLoaderUtil.getInstance().getLaunchDesc().getAppInfo()));
            localPreloader.handleEvent(new InitEvent(1));
          }
          catch (CancelException localCancelException)
          {
            Trace.ignoredException(localCancelException);
          }
          return new PreloaderDelegate(localPreloader);
        }
      }));
    return this._defaultProgressHelper;
  }

  public boolean isResourceCached(URL paramURL, String paramString)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(paramURL, paramString)
    {
      private final URL val$ref;
      private final String val$version;

      public Object run()
      {
        if (DownloadServiceImpl.this.isResourceValid(this.val$ref, this.val$version))
          try
          {
            if (DownloadEngine.isResourceCached(this.val$ref, null, this.val$version))
              return Boolean.TRUE;
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
        return Boolean.FALSE;
      }
    });
    return localBoolean.booleanValue();
  }

  public boolean isPartCached(String paramString)
  {
    return isPartCached(new String[] { paramString });
  }

  public boolean isPartCached(String[] paramArrayOfString)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(paramArrayOfString)
    {
      private final String[] val$parts;

      public Object run()
      {
        LaunchDesc localLaunchDesc = JNLPClassLoaderUtil.getInstance().getLaunchDesc();
        ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
        if (localResourcesDesc == null)
          return Boolean.FALSE;
        JARDesc[] arrayOfJARDesc = localResourcesDesc.getPartJars(this.val$parts);
        return new Boolean(DownloadServiceImpl.this.isJARInCache(arrayOfJARDesc, true));
      }
    });
    return localBoolean.booleanValue();
  }

  public boolean isExtensionPartCached(URL paramURL, String paramString1, String paramString2)
  {
    return isExtensionPartCached(paramURL, paramString1, new String[] { paramString2 });
  }

  public boolean isExtensionPartCached(URL paramURL, String paramString, String[] paramArrayOfString)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(paramURL, paramString, paramArrayOfString)
    {
      private final URL val$ref;
      private final String val$version;
      private final String[] val$parts;

      public Object run()
      {
        LaunchDesc localLaunchDesc = JNLPClassLoaderUtil.getInstance().getLaunchDesc();
        ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
        if (localResourcesDesc == null)
          return Boolean.FALSE;
        JARDesc[] arrayOfJARDesc = localResourcesDesc.getExtensionPart(this.val$ref, this.val$version, this.val$parts);
        return new Boolean(DownloadServiceImpl.this.isJARInCache(arrayOfJARDesc, true));
      }
    });
    return localBoolean.booleanValue();
  }

  public void loadResource(URL paramURL, String paramString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    Trace.println(getClass().getName() + ".loadResource(" + paramURL + "," + paramDownloadServiceListener.getClass().getName() + ")");
    if (isResourceValid(paramURL, paramString))
      try
      {
        AccessController.doPrivileged(new PrivilegedExceptionAction(paramDownloadServiceListener, paramURL, paramString)
        {
          private final DownloadServiceListener val$progress;
          private final URL val$ref;
          private final String val$version;

          public Object run()
            throws IOException
          {
            CustomProgress2PreloaderAdapter localCustomProgress2PreloaderAdapter = new CustomProgress2PreloaderAdapter(this.val$progress);
            PreloaderDelegate localPreloaderDelegate = DownloadServiceImpl.this.getProgressHelper(localCustomProgress2PreloaderAdapter);
            try
            {
              Object localObject1;
              if (this.val$ref.toString().endsWith(".jar"))
              {
                localObject1 = JNLPClassLoaderUtil.getInstance();
                ((JNLPClassLoaderIf)localObject1).addResource(this.val$ref, this.val$version, null);
                if (!DownloadServiceImpl.this.isResourceCached(this.val$ref, this.val$version))
                  LaunchDownload.downloadResource(((JNLPClassLoaderIf)localObject1).getLaunchDesc(), this.val$ref, this.val$version, localPreloaderDelegate, true);
              }
              else
              {
                DownloadEngine.getResource(this.val$ref, null, this.val$version, null, true);
                localObject1 = Cache.getCacheEntry(this.val$ref, null, this.val$version);
                if ((localObject1 != null) && (((CacheEntry)localObject1).isJNLPFile()))
                  DownloadServiceImpl.this.loadResourceRecursivly((CacheEntry)localObject1, this.val$progress);
              }
            }
            catch (Exception localException)
            {
              throw new IOException(localException.getMessage());
            }
            finally
            {
              localPreloaderDelegate.forceFlushForTCK();
            }
            return null;
          }
        });
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        throw ((IOException)localPrivilegedActionException.getException());
      }
  }

  private void loadResourceRecursivly(CacheEntry paramCacheEntry, DownloadServiceListener paramDownloadServiceListener)
  {
    try
    {
      File localFile = new File(paramCacheEntry.getResourceFilename());
      URL localURL = new URL(paramCacheEntry.getURL());
      LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile, null, null, localURL);
      ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
      if (localResourcesDesc != null)
        localResourcesDesc.visit(new ResourceVisitor(paramDownloadServiceListener)
        {
          private final DownloadServiceListener val$progress;

          public void visitJARDesc(JARDesc paramJARDesc)
          {
            try
            {
              DownloadServiceImpl.this.loadResource(paramJARDesc.getLocation(), paramJARDesc.getVersion(), this.val$progress);
            }
            catch (IOException localIOException)
            {
              Trace.ignored(localIOException);
            }
          }

          public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
          {
            try
            {
              DownloadServiceImpl.this.loadResource(paramExtensionDesc.getLocation(), paramExtensionDesc.getVersion(), this.val$progress);
            }
            catch (IOException localIOException)
            {
              Trace.ignored(localIOException);
            }
          }
        });
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
  }

  public void loadPart(String paramString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    loadPart(new String[] { paramString }, paramDownloadServiceListener);
  }

  public void loadPart(String[] paramArrayOfString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    Trace.println(getClass().getName() + ".loadPart(" + Arrays.asList(paramArrayOfString) + "," + paramDownloadServiceListener.getClass().getName() + ")");
    if (isPartCached(paramArrayOfString))
      return;
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramDownloadServiceListener, paramArrayOfString)
      {
        private final DownloadServiceListener val$progress;
        private final String[] val$parts;

        public Object run()
          throws IOException
        {
          CustomProgress2PreloaderAdapter localCustomProgress2PreloaderAdapter = new CustomProgress2PreloaderAdapter(this.val$progress);
          PreloaderDelegate localPreloaderDelegate = DownloadServiceImpl.this.getProgressHelper(localCustomProgress2PreloaderAdapter);
          try
          {
            LaunchDownload.downloadParts(JNLPClassLoaderUtil.getInstance().getLaunchDesc(), this.val$parts, localPreloaderDelegate, true);
          }
          catch (Exception localException)
          {
            throw new IOException(localException.getMessage());
          }
          finally
          {
            localPreloaderDelegate.forceFlushForTCK();
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }

  public void loadExtensionPart(URL paramURL, String paramString1, String paramString2, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    loadExtensionPart(paramURL, paramString1, new String[] { paramString2 }, paramDownloadServiceListener);
  }

  public void loadExtensionPart(URL paramURL, String paramString, String[] paramArrayOfString, DownloadServiceListener paramDownloadServiceListener)
    throws IOException
  {
    try
    {
      Trace.println(getClass().getName() + ".loadExtensionPart(" + Arrays.asList(paramArrayOfString) + "," + paramDownloadServiceListener.getClass().getName() + ")");
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramDownloadServiceListener, paramURL, paramString, paramArrayOfString)
      {
        private final DownloadServiceListener val$progress;
        private final URL val$ref;
        private final String val$version;
        private final String[] val$parts;

        public Object run()
          throws IOException
        {
          CustomProgress2PreloaderAdapter localCustomProgress2PreloaderAdapter = new CustomProgress2PreloaderAdapter(this.val$progress);
          PreloaderDelegate localPreloaderDelegate = DownloadServiceImpl.this.getProgressHelper(localCustomProgress2PreloaderAdapter);
          try
          {
            LaunchDownload.downloadExtensionPart(JNLPClassLoaderUtil.getInstance().getLaunchDesc(), this.val$ref, this.val$version, this.val$parts, localPreloaderDelegate, true);
          }
          catch (Exception localException)
          {
            throw new IOException(localException.getMessage());
          }
          finally
          {
            localPreloaderDelegate.forceFlushForTCK();
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }

  public void removeResource(URL paramURL, String paramString)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL, paramString)
      {
        private final URL val$ref;
        private final String val$version;

        public Object run()
          throws IOException
        {
          if (DownloadServiceImpl.this.isResourceValid(this.val$ref, this.val$version))
          {
            if (this.val$ref.toString().endsWith("jnlp"))
              CacheUtil.remove(Cache.getCacheEntry(this.val$ref, null, this.val$version));
            DownloadEngine.removeCachedResource(this.val$ref, null, this.val$version);
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }

  public void removePart(String paramString)
    throws IOException
  {
    removePart(new String[] { paramString });
  }

  public void removePart(String[] paramArrayOfString)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramArrayOfString)
      {
        private final String[] val$parts;

        public Object run()
          throws IOException
        {
          LaunchDesc localLaunchDesc = JNLPClassLoaderUtil.getInstance().getLaunchDesc();
          ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
          if (localResourcesDesc == null)
            return null;
          JARDesc[] arrayOfJARDesc = localResourcesDesc.getPartJars(this.val$parts);
          DownloadServiceImpl.this.removeJARFromCache(arrayOfJARDesc);
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }

  public void removeExtensionPart(URL paramURL, String paramString1, String paramString2)
    throws IOException
  {
    removeExtensionPart(paramURL, paramString1, new String[] { paramString2 });
  }

  public void removeExtensionPart(URL paramURL, String paramString, String[] paramArrayOfString)
    throws IOException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL, paramString, paramArrayOfString)
      {
        private final URL val$ref;
        private final String val$version;
        private final String[] val$parts;

        public Object run()
          throws IOException
        {
          LaunchDesc localLaunchDesc = JNLPClassLoaderUtil.getInstance().getLaunchDesc();
          ResourcesDesc localResourcesDesc = localLaunchDesc.getResources();
          if (localResourcesDesc == null)
            return null;
          JARDesc[] arrayOfJARDesc = localResourcesDesc.getExtensionPart(this.val$ref, this.val$version, this.val$parts);
          DownloadServiceImpl.this.removeJARFromCache(arrayOfJARDesc);
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }

  private void removeJARFromCache(JARDesc[] paramArrayOfJARDesc)
    throws IOException
  {
    if (paramArrayOfJARDesc == null)
      return;
    if (paramArrayOfJARDesc.length == 0)
      return;
    for (int i = 0; i < paramArrayOfJARDesc.length; i++)
      DownloadEngine.removeCachedResource(paramArrayOfJARDesc[i].getLocation(), null, paramArrayOfJARDesc[i].getVersion());
  }

  private boolean isJARInCache(JARDesc[] paramArrayOfJARDesc, boolean paramBoolean)
  {
    if (paramArrayOfJARDesc == null)
      return false;
    if (paramArrayOfJARDesc.length == 0)
      return false;
    int i = 1;
    for (int j = 0; j < paramArrayOfJARDesc.length; j++)
      if (paramArrayOfJARDesc[j].isNativeLib())
        try
        {
          if (DownloadEngine.getCachedJarFile(paramArrayOfJARDesc[j].getLocation(), paramArrayOfJARDesc[j].getVersion()) != null)
          {
            if (!paramBoolean)
              return true;
          }
          else
            i = 0;
        }
        catch (IOException localIOException1)
        {
          Trace.ignoredException(localIOException1);
          i = 0;
        }
      else
        try
        {
          if (DownloadEngine.getCachedJarFile(paramArrayOfJARDesc[j].getLocation(), paramArrayOfJARDesc[j].getVersion()) != null)
          {
            if (!paramBoolean)
              return true;
          }
          else
            i = 0;
        }
        catch (IOException localIOException2)
        {
          Trace.ignoredException(localIOException2);
          i = 0;
        }
    return i;
  }

  private boolean isResourceValid(URL paramURL, String paramString)
  {
    LaunchDesc localLaunchDesc = JNLPClassLoaderUtil.getInstance().getLaunchDesc();
    JARDesc[] arrayOfJARDesc = localLaunchDesc.getResources().getEagerOrAllJarDescs(true);
    if (localLaunchDesc.getSecurityModel() != 0)
      return true;
    for (int i = 0; i < arrayOfJARDesc.length; i++)
      if ((paramURL.toString().equals(arrayOfJARDesc[i].getLocation().toString())) && ((paramString == null) || (paramString.equals(arrayOfJARDesc[i].getVersion()))))
        return true;
    URL localURL = localLaunchDesc.getCodebase();
    return (localURL != null) && (paramURL != null) && (paramURL.toString().startsWith(localURL.toString()));
  }

  private PreloaderDelegate getProgressHelper(CustomProgress2PreloaderAdapter paramCustomProgress2PreloaderAdapter)
  {
    return new PreloaderDelegate(paramCustomProgress2PreloaderAdapter);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.DownloadServiceImpl
 * JD-Core Version:    0.6.0
 */