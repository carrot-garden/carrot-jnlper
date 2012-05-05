package com.sun.deploy.cache;

import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.HttpUtils;
import com.sun.deploy.net.UpdateTracker;
import com.sun.deploy.net.offline.DeployOfflineManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.HttpURLConnection;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DeployCacheHandler extends ResponseCache
{
  private static final HashSet isNotCacheable = new HashSet();
  private final HashMap inProgress = new HashMap();
  private static final ThreadLocal isDeployPackURL = new ThreadLocal();
  private ThreadLocal inCacheHandler = new ThreadLocal();

  public static void setDeployPackURL(URL paramURL)
  {
    isDeployPackURL.set(paramURL);
  }

  public static void clearDeployPackURL()
  {
    isDeployPackURL.set(null);
  }

  public static void reset()
  {
    ResponseCache.setDefault(new DeployCacheHandler());
    clearDeployPackURL();
  }

  public CacheResponse get(URI paramURI, String paramString, Map paramMap)
    throws IOException
  {
    if (DownloadEngine.haveDownloadInProgress())
      return null;
    Object localObject1 = null;
    if ((!Cache.isCacheEnabled()) || (!paramString.equals("GET")) || (this.inCacheHandler.get() != null))
      return null;
    Object localObject2;
    synchronized (this.inProgress)
    {
      if (!this.inProgress.containsKey(paramURI))
        this.inProgress.put(paramURI, new Object());
      localObject2 = this.inProgress.get(paramURI);
    }
    synchronized (localObject2)
    {
      try
      {
        this.inCacheHandler.set(Boolean.TRUE);
        File localFile = null;
        boolean bool1 = false;
        URL localURL1 = (URL)isDeployPackURL.get();
        if (localURL1 != null)
          bool1 = true;
        URL localURL2 = bool1 ? localURL1 : paramURI.toURL();
        URL localURL3 = HttpUtils.removeQueryStringFromURL(localURL2);
        String str = (String)ToolkitStore.get().getAppContext().get("deploy-" + localURL3);
        boolean bool2 = false;
        if ((!DeployOfflineManager.isGlobalOffline()) && (UpdateTracker.isUpdateCheckNeeded(localURL2.toString())))
          bool2 = DownloadEngine.isUpdateAvailable(localURL2, str, bool1, paramMap);
        if (!bool2)
          try
          {
            localFile = (File)AccessController.doPrivileged(new PrivilegedExceptionAction(str, localURL2, localURL3, paramMap)
            {
              private final String val$jarVersion;
              private final URL val$url;
              private final URL val$urlNoQuery;
              private final Map val$requestHeaders;

              public Object run()
                throws IOException
              {
                URL localURL = this.val$jarVersion == null ? this.val$url : this.val$urlNoQuery;
                return DeployCacheHandler.this.getCacheFile(localURL, this.val$jarVersion, this.val$requestHeaders);
              }
            });
          }
          catch (PrivilegedActionException localPrivilegedActionException1)
          {
            Trace.ignoredException(localPrivilegedActionException1);
          }
        if (localFile == null)
        {
          localInputStream = null;
          this.inCacheHandler.set(null);
          synchronized (this.inProgress)
          {
            this.inProgress.remove(localObject2);
          }
          return localInputStream;
        }
        InputStream localInputStream = null;
        ??? = localFile;
        if (??? != null)
        {
          try
          {
            localInputStream = (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction((File)???)
            {
              private final File val$cachedFileF;

              public Object run()
                throws IOException
              {
                return new FileInputStream(this.val$cachedFileF);
              }
            });
          }
          catch (PrivilegedActionException localPrivilegedActionException2)
          {
            Trace.ignoredException(localPrivilegedActionException2);
          }
          if (localInputStream != null)
          {
            Map localMap = DownloadEngine.getCachedHeaders(str == null ? localURL2 : localURL3, str);
            if (paramURI.getScheme().equals("https"))
              localObject1 = new DeploySecureCacheResponse(localInputStream, localMap);
            else
              localObject1 = new DeployCacheResponse(localInputStream, localMap);
          }
        }
      }
      finally
      {
        this.inCacheHandler.set(null);
        synchronized (this.inProgress)
        {
          this.inProgress.remove(localObject2);
        }
      }
    }
    return (CacheResponse)(CacheResponse)(CacheResponse)localObject1;
  }

  private File getCacheFile(URL paramURL, String paramString, Map paramMap)
  {
    CacheEntry localCacheEntry = Cache.getCacheEntry(paramURL, null, paramString);
    if ((localCacheEntry != null) && (!DownloadEngine.isInternalUse()) && (localCacheEntry.isJarFile()) && (!localCacheEntry.hasCompressEncoding()) && (!HttpUtils.matchEncoding("gzip", paramMap, localCacheEntry.getHeaders())))
      localCacheEntry = null;
    if (localCacheEntry != null)
      return new File(localCacheEntry.getResourceFilename());
    return null;
  }

  private static boolean isResourceCacheable(String paramString, URLConnection paramURLConnection)
  {
    if ((!Cache.isCacheEnabled()) || ((!paramURLConnection.getUseCaches()) && (!DownloadEngine.isAlwaysCached(paramString))))
      return false;
    if (((paramURLConnection instanceof HttpURLConnection)) && (!((HttpURLConnection)paramURLConnection).getRequestMethod().equals("GET")))
      return false;
    if (paramURLConnection.getHeaderField("content-range") != null)
      return false;
    String str = paramURLConnection.getHeaderField("cache-control");
    if ((str != null) && (str.toLowerCase().indexOf("no-store") != -1))
      return false;
    if ((paramURLConnection.getLastModified() == 0L) && (paramURLConnection.getExpiration() == 0L))
    {
      synchronized (isNotCacheable)
      {
        isNotCacheable.add(paramString);
        if (Cache.DEBUG)
          Trace.println(paramString + " is not cacheable.", TraceLevel.BASIC);
      }
      return false;
    }
    return true;
  }

  public static boolean resourceNotCached(String paramString)
  {
    synchronized (isNotCacheable)
    {
      return isNotCacheable.contains(paramString);
    }
  }

  public CacheRequest put(URI paramURI, URLConnection paramURLConnection)
    throws IOException
  {
    if (DownloadEngine.haveDownloadInProgress())
      return null;
    if (!isResourceCacheable(paramURI.toString(), paramURLConnection))
      return null;
    URL localURL1 = (URL)isDeployPackURL.get();
    boolean bool = false;
    if (localURL1 != null)
      bool = true;
    URL localURL2 = bool ? localURL1 : paramURI.toURL();
    return new DeployCacheRequest(localURL2, paramURLConnection, bool);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.DeployCacheHandler
 * JD-Core Version:    0.6.0
 */