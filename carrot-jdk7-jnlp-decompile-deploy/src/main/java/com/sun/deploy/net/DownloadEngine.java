package com.sun.deploy.net;

import com.sun.applet2.preloader.CancelException;
import com.sun.deploy.Environment;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.cache.MemoryCache;
import com.sun.deploy.config.Config;
import com.sun.deploy.jardiff.JarDiffPatcher;
import com.sun.deploy.jardiff.Patcher;
import com.sun.deploy.jardiff.Patcher.PatchDelegate;
import com.sun.deploy.net.offline.DeployOfflineManager;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.security.JarVerifier;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.deploy.util.URLUtil;
import com.sun.deploy.util.VersionID;
import com.sun.deploy.util.VersionString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Unpacker;
import java.util.zip.GZIPInputStream;

public class DownloadEngine
{
  private static final String ARG_ARCH = "arch";
  private static final String ARG_OS = "os";
  private static final String ARG_LOCALE = "locale";
  private static final String ARG_VERSION_ID = "version-id";
  public static final String ARG_CURRENT_VERSION_ID = "current-version-id";
  private static final String ARG_PLATFORM_VERSION_ID = "platform-version-id";
  private static final String ARG_KNOWN_PLATFORMS = "known-platforms";
  private static final String REPLY_JNLP_VERSION = "x-java-jnlp-version-id";
  private static final String ERROR_MIME_TYPE = "application/x-java-jnlp-error";
  private static final String JARDIFF_MIME_TYPE = "application/x-java-archive-diff";
  private static final String JNLP_MIME_TYPE = "application/x-java-jnlp-file";
  private static final int BASIC_DOWNLOAD_PROTOCOL = 1;
  private static final int VERSION_DOWNLOAD_PROTOCOL = 2;
  private static final int EXTENSION_DOWNLOAD_PROTOCOL = 3;
  public static final int NORMAL_CONTENT_BIT = 1;
  public static final int NATIVE_CONTENT_BIT = 16;
  public static final int JAR_CONTENT_BIT = 256;
  public static final int PACK200_CONTENT_BIT = 4096;
  public static final int VERSION_CONTENT_BIT = 65536;
  private static int BUF_SIZE = 8192;
  private static final String defaultLocaleString = Locale.getDefault().toString();
  private static HttpRequest _httpRequestImpl;
  private static HttpDownload _httpDownloadImpl;
  public static final String BACKGROUND_STRING = "background";
  public static final String APPCONTEXT_BG_KEY = "deploy-bg-";
  private static InheritableThreadLocal backgroundUpdateThreadLocal = new InheritableThreadLocal()
  {
    protected Object initialValue()
    {
      return Boolean.FALSE;
    }
  };
  private static ThreadLocal processingState = new ThreadLocal();
  private static final ThreadLocal internalUse = new ThreadLocal();
  private static Hashtable noCacheJarFileList = new Hashtable();
  private static Hashtable noCacheRedirectFinalURLs = new Hashtable();

  public static boolean haveDownloadInProgress()
  {
    return processingState.get() != null;
  }

  private static void setDownloadInProgress(boolean paramBoolean)
  {
    if (paramBoolean)
      processingState.set(Boolean.TRUE);
    else
      processingState.set(null);
  }

  public static boolean isBackgroundUpdateRequest()
  {
    return ((Boolean)backgroundUpdateThreadLocal.get()).booleanValue();
  }

  public static void setBackgroundUpdateRequest(boolean paramBoolean)
  {
    backgroundUpdateThreadLocal.set(new Boolean(paramBoolean));
  }

  public static int incrementInternalUse()
  {
    int i = getInternalUseLevel() + 1;
    internalUse.set(Integer.valueOf(i));
    return i;
  }

  public static void decrementInternalUse(int paramInt)
  {
    int i = getInternalUseLevel();
    if (i != paramInt)
    {
      if (Trace.isEnabled(TraceLevel.NETWORK))
        Trace.ignored(new Exception("WARNING: unbalanced internalUse level: expect=" + paramInt + " : got=" + i));
      i = paramInt;
    }
    i--;
    i = i;
    if (i < 0)
      i = 0;
    internalUse.set(Integer.valueOf(i));
  }

  public static boolean isInternalUse()
  {
    return getInternalUseLevel() > 0;
  }

  private static int getInternalUseLevel()
  {
    Integer localInteger = (Integer)internalUse.get();
    return localInteger != null ? localInteger.intValue() : 0;
  }

  public static boolean isNativeContentType(int paramInt)
  {
    return (paramInt & 0x10) == 16;
  }

  public static boolean isJarContentType(int paramInt)
  {
    return (paramInt & 0x100) == 256;
  }

  public static boolean isPackContentType(int paramInt)
  {
    return (paramInt & 0x1000) == 4096;
  }

  private static boolean isVersionContentType(int paramInt)
  {
    return (paramInt & 0x10000) == 65536;
  }

  static boolean isPack200Supported()
  {
    return Config.isJavaVersionAtLeast15();
  }

  public static String getCachedResourceFilePath(URL paramURL)
    throws IOException
  {
    return getCachedResourceFilePath(paramURL, null);
  }

  public static String getCachedResourceFilePath(URL paramURL, String paramString)
    throws IOException
  {
    Object localObject;
    if (Cache.isCacheEnabled())
    {
      localObject = Cache.getCacheEntry(paramURL, null, paramString);
      if (localObject != null)
        return ((CacheEntry)localObject).getResourceFilename();
    }
    else
    {
      localObject = MemoryCache.getLoadedResource(paramURL.toString());
      if (localObject != null)
      {
        if ((localObject instanceof JarFile))
          return ((JarFile)localObject).getName();
        if ((localObject instanceof CacheEntry))
          return ((CacheEntry)localObject).getResourceFilename();
      }
    }
    throw new IOException("Cannot find cached resource for URL: " + paramURL.toString());
  }

  public static JarFile downloadJarFileWithoutCache(URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, int paramInt)
    throws IOException
  {
    JarFile localJarFile = null;
    try
    {
      setDownloadInProgress(true);
      try
      {
        localJarFile = getJarFileWithoutCache(paramURL, paramString1, paramString2, paramDownloadDelegate, paramInt);
      }
      catch (JARSigningException localJARSigningException)
      {
        throw localJARSigningException;
      }
      catch (IOException localIOException)
      {
        localJarFile = getJarFileWithoutCache(paramURL, paramString1, paramString2, paramDownloadDelegate, 256);
      }
    }
    finally
    {
      setDownloadInProgress(false);
    }
    return localJarFile;
  }

  public static URL getResource(URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, boolean paramBoolean)
    throws IOException
  {
    return getResource(paramURL, paramString1, paramString2, paramDownloadDelegate, paramBoolean, 1);
  }

  public static URL getResource(URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, boolean paramBoolean, int paramInt)
    throws IOException
  {
    int i = incrementInternalUse();
    URL localURL = null;
    try
    {
      if (Cache.isCacheEnabled())
      {
        localObject1 = getResourceCacheEntry(paramURL, paramString1, paramString2, paramDownloadDelegate, paramBoolean, paramInt);
        if (localObject1 != null)
          localURL = URLUtil.fileToURL(new File(((CacheEntry)localObject1).getResourceFilename()));
      }
      else if ((isJarContentType(paramInt)) || (isPackContentType(paramInt)) || (isAlwaysCached(paramURL.toString().toLowerCase())))
      {
        localObject1 = downloadJarFileWithoutCache(paramURL, paramString1, paramString2, paramDownloadDelegate, paramInt);
        if (localObject1 != null)
          MemoryCache.addLoadedResource(paramURL.toString(), localObject1);
      }
      Object localObject1 = localURL;
      return localObject1;
    }
    finally
    {
      decrementInternalUse(i);
    }
    throw localObject2;
  }

  static boolean isZipFile(String paramString)
  {
    return paramString.toLowerCase().endsWith(".zip");
  }

  public static boolean isAlwaysCached(String paramString)
  {
    String str = paramString.toLowerCase();
    return (str.endsWith(".jar")) || (str.endsWith(".jarjar")) || (str.endsWith(".zip"));
  }

  public static long getCachedLastModified(URL paramURL, String paramString1, String paramString2)
    throws IOException
  {
    CacheEntry localCacheEntry = getResourceCacheEntry(paramURL, paramString1, paramString2, null, false);
    if (localCacheEntry != null)
      return localCacheEntry.getLastModified();
    return 0L;
  }

  public static long getCachedSize(URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate)
    throws IOException
  {
    CacheEntry localCacheEntry = null;
    localCacheEntry = getResourceCacheEntry(paramURL, paramString1, paramString2, paramDownloadDelegate, false);
    if (localCacheEntry != null)
      return localCacheEntry.getSize();
    return 0L;
  }

  public static URL getNonCacheRedirectFinalURL(URL paramURL)
  {
    return (URL)noCacheRedirectFinalURLs.get(paramURL.toString());
  }

  public static URL getKnownRedirectFinalURL(URL paramURL)
  {
    URL localURL = null;
    Object localObject = MemoryCache.getLoadedResource(paramURL.toString());
    if ((localObject instanceof CacheEntry))
    {
      CacheEntry localCacheEntry = (CacheEntry)localObject;
      try
      {
        localURL = new URL(localCacheEntry.getURL());
      }
      catch (MalformedURLException localMalformedURLException)
      {
        Trace.ignored(localMalformedURLException);
      }
    }
    else
    {
      localURL = getNonCacheRedirectFinalURL(paramURL);
    }
    if (localURL == null)
      localURL = paramURL;
    return localURL;
  }

  public static void clearNoCacheJarFileList()
  {
    noCacheJarFileList.clear();
    noCacheRedirectFinalURLs.clear();
  }

  private static URL deriveRequestURL(URL paramURL, String paramString1, String paramString2, int paramInt)
  {
    URL localURL = getRequestURL(paramURL, paramString1, paramString2, null, null, null, null, false, null, paramString2 == null ? 1 : 2);
    if (isVersionContentType(paramInt))
      localURL = getEmbeddedVersionUrl(localURL, paramString2);
    if (isPackContentType(paramInt))
      localURL = getPack200Url(localURL);
    return localURL;
  }

  private static JarFile getJarFileWithoutCache(URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, int paramInt)
    throws IOException
  {
    boolean bool = isPackContentType(paramInt);
    byte[] arrayOfByte = new byte[8192];
    URL localURL1 = deriveRequestURL(paramURL, paramString1, paramString2, paramInt);
    URLConnection localURLConnection = localURL1.openConnection();
    if (isPack200Supported())
      localURLConnection.setRequestProperty("accept-encoding", "pack200-gzip,gzip");
    else
      localURLConnection.setRequestProperty("accept-encoding", "gzip");
    Object localObject1 = localURLConnection.getInputStream();
    int i = localURLConnection.getContentLength();
    String str = localURLConnection.getContentEncoding();
    int j = 0;
    if (paramDownloadDelegate != null)
      paramDownloadDelegate.setTotalSize(i);
    2 local2 = paramDownloadDelegate == null ? null : new HttpDownloadListener(paramDownloadDelegate, paramURL, paramString2)
    {
      private final DownloadEngine.DownloadDelegate val$dd;
      private final URL val$resourceURL;
      private final String val$versionString;

      public boolean downloadProgress(int paramInt1, int paramInt2)
        throws CancelException
      {
        this.val$dd.downloading(this.val$resourceURL, this.val$versionString, paramInt1, paramInt2, false);
        return true;
      }
    };
    FileOutputStream localFileOutputStream = null;
    File localFile = null;
    int k = 0;
    if (Trace.isEnabled(TraceLevel.NETWORK))
      Trace.println(ResourceManager.getString("httpDownloadHelper.doingDownload", localURL1.toString(), i, str), TraceLevel.NETWORK);
    try
    {
      localFile = File.createTempFile("jar_cache", null);
      localFile.deleteOnExit();
      localFileOutputStream = new FileOutputStream(localFile);
      int m = 0;
      if ((bool) || ((str != null) && (str.equals("pack200-gzip"))))
      {
        JarOutputStream localJarOutputStream = new JarOutputStream(localFileOutputStream);
        Pack200.Unpacker localUnpacker = Pack200.newUnpacker();
        localUnpacker.unpack(new GZIPInputStream((InputStream)localObject1, BUF_SIZE), localJarOutputStream);
        localJarOutputStream.close();
      }
      else
      {
        if ((str != null) && (str.equals("gzip")))
          localObject1 = new GZIPInputStream((InputStream)localObject1, BUF_SIZE);
        for (int n = 0; (m = ((InputStream)localObject1).read(arrayOfByte)) != -1; n++)
        {
          if ((n == 0) && (!isJarHeaderValid(arrayOfByte)))
            throw new IOException("Invalid jar file");
          localFileOutputStream.write(arrayOfByte, 0, m);
          j += m;
          if ((j > i) && (i != 0))
            j = i;
          if (local2 == null)
            continue;
          local2.downloadProgress(j, i);
        }
      }
      k = 1;
    }
    finally
    {
      if (localObject1 != null)
        ((InputStream)localObject1).close();
      if (localFileOutputStream != null)
        localFileOutputStream.close();
      if ((k == 0) && (localFile != null))
      {
        localFile.delete();
        localFile = null;
      }
    }
    if (localFile == null)
      return null;
    JarFile localJarFile = JarVerifier.getValidatedJarFile(localFile, paramURL, localURL1, paramString2, paramDownloadDelegate);
    noCacheJarFileList.put(paramURL.toString(), localFile);
    URL localURL2 = localURLConnection.getURL();
    if (!URLUtil.sameURLs(localURL2, localURL1))
      noCacheRedirectFinalURLs.put(paramURL.toString(), localURL2);
    return (JarFile)localJarFile;
  }

  public static void clearTemporaryResourceMaps(URL paramURL)
  {
    noCacheJarFileList.remove(paramURL.toString());
    noCacheRedirectFinalURLs.remove(paramURL.toString());
  }

  public static JarFile getCachedJarFile(URL paramURL, String paramString)
    throws IOException
  {
    return getCachedJarFile(paramURL, paramString, false, 1);
  }

  public static String getLibraryDirForJar(String paramString1, URL paramURL, String paramString2)
    throws IOException
  {
    int i = incrementInternalUse();
    try
    {
      File localFile1 = getCachedFile(paramURL, paramString2);
      if (localFile1 == null)
      {
        localObject1 = null;
        localObject1 = getResourceCacheEntry(paramURL, null, paramString2, null, true, false, null, 272);
        if (localObject1 != null)
          localFile1 = ((CacheEntry)localObject1).getDataFile();
      }
      if (localFile1 != null)
      {
        localObject1 = localFile1.getPath() + "-n";
        File localFile2 = new File((String)localObject1, paramString1);
        Trace.println("Looking up native library in: " + localFile2, TraceLevel.NETWORK);
        if (localFile2.exists())
        {
          Object localObject2 = localObject1;
          return localObject2;
        }
      }
      Object localObject1 = null;
      return localObject1;
    }
    finally
    {
      decrementInternalUse(i);
    }
    throw localObject3;
  }

  public static JarFile getUpdatedJarFile(URL paramURL, String paramString, int paramInt)
    throws IOException
  {
    int i = incrementInternalUse();
    try
    {
      JarFile localJarFile = getCachedJarFile(paramURL, paramString, true, paramInt);
      return localJarFile;
    }
    finally
    {
      decrementInternalUse(i);
    }
    throw localObject;
  }

  private static JarFile getCachedJarFile(URL paramURL, String paramString, boolean paramBoolean, int paramInt)
    throws IOException
  {
    Object localObject;
    if (Cache.isCacheEnabled())
    {
      localObject = null;
      localObject = getResourceCacheEntry(paramURL, null, paramString, null, paramBoolean, paramInt);
      if (localObject != null)
        return ((CacheEntry)localObject).getJarFile();
    }
    else
    {
      localObject = MemoryCache.getLoadedResource(paramURL.toString());
      if ((localObject != null) && ((localObject instanceof JarFile)))
        return (JarFile)localObject;
      JarFile localJarFile = null;
      File localFile = (File)noCacheJarFileList.get(paramURL.toString());
      if (localFile != null)
      {
        URL localURL = deriveRequestURL(paramURL, null, paramString, paramInt);
        localJarFile = JarVerifier.getValidatedJarFile(localFile, paramURL, localURL, paramString, null);
      }
      else if (paramBoolean)
      {
        localJarFile = downloadJarFileWithoutCache(paramURL, null, paramString, null, paramInt);
      }
      if (localJarFile != null)
      {
        MemoryCache.addLoadedResource(paramURL.toString(), localJarFile);
        return localJarFile;
      }
    }
    return (JarFile)null;
  }

  public static File getCachedShortcutImage(URL paramURL, String paramString)
    throws IOException
  {
    return getShortcutImage(paramURL, paramString, false);
  }

  public static File getUpdatedShortcutImage(URL paramURL, String paramString)
    throws IOException
  {
    return getShortcutImage(paramURL, paramString, true);
  }

  private static File getShortcutImage(URL paramURL, String paramString, boolean paramBoolean)
    throws IOException
  {
    CacheEntry localCacheEntry = getResourceCacheEntry(paramURL, null, paramString, null, paramBoolean);
    if (localCacheEntry != null)
    {
      localCacheEntry.generateShortcutImage();
      return localCacheEntry.getDataFile();
    }
    return null;
  }

  public static File getCachedFileNative(URL paramURL)
    throws IOException
  {
    if (paramURL.getProtocol().equals("jar"))
    {
      String str1 = paramURL.getPath();
      int i = str1.indexOf("!/");
      if (i > 0)
        try
        {
          String str2 = str1.substring(i + 2);
          URL localURL = new URL(str1.substring(0, i));
          CacheEntry localCacheEntry = Cache.getCacheEntry(localURL, null, null);
          if (localCacheEntry != null)
          {
            String str3 = localCacheEntry.getNativeLibPath();
            if (str3 != null)
              return new File(str3, str2);
          }
        }
        catch (MalformedURLException localMalformedURLException)
        {
          Trace.ignored(localMalformedURLException);
        }
        catch (IOException localIOException)
        {
          Trace.ignored(localIOException);
        }
      return null;
    }
    return getCachedFile(paramURL);
  }

  public static File getCachedFile(URL paramURL)
    throws IOException
  {
    return getCachedFile(paramURL, null);
  }

  public static File getCachedFile(URL paramURL, String paramString)
    throws IOException
  {
    return getCachedFile(paramURL, paramString, false, false, null);
  }

  public static File getUpdatedFile(URL paramURL, String paramString)
    throws IOException
  {
    return getUpdatedFile(paramURL, paramString, false, null);
  }

  public static File getUpdatedFile(URL paramURL, String paramString1, boolean paramBoolean, String paramString2)
    throws IOException
  {
    int i = incrementInternalUse();
    try
    {
      File localFile = getCachedFile(paramURL, paramString1, true, paramBoolean, paramString2);
      return localFile;
    }
    finally
    {
      decrementInternalUse(i);
    }
    throw localObject;
  }

  public static File getCachedFile(URL paramURL, String paramString1, boolean paramBoolean1, boolean paramBoolean2, String paramString2)
    throws IOException
  {
    CacheEntry localCacheEntry = null;
    localCacheEntry = getResourceCacheEntry(paramURL, null, paramString1, null, paramBoolean1, paramBoolean2, paramString2, 1);
    if (localCacheEntry != null)
      return localCacheEntry.getDataFile();
    Trace.println("Cannot get resource from cache: " + paramURL, TraceLevel.CACHE);
    return null;
  }

  public static boolean isJarFileCorrupted(URL paramURL, String paramString)
  {
    JarFile localJarFile = null;
    try
    {
      CacheEntry localCacheEntry = null;
      localCacheEntry = getResourceCacheEntry(paramURL, null, paramString, null, false);
      localJarFile = new JarFile(localCacheEntry.getResourceFilename(), false);
      if (localJarFile != null)
      {
        int i = 0;
        return i;
      }
    }
    catch (IOException localIOException3)
    {
      Trace.ignoredException(localIOException2);
    }
    finally
    {
      try
      {
        if (localJarFile != null)
          localJarFile.close();
      }
      catch (IOException localIOException5)
      {
        Trace.ignoredException(localIOException5);
      }
    }
    return true;
  }

  public static boolean isResourceCached(URL paramURL, String paramString1, String paramString2)
    throws IOException
  {
    return isResourceCached(paramURL, paramString1, paramString2, 1);
  }

  public static boolean isResourceCached(URL paramURL, String paramString1, String paramString2, int paramInt)
    throws IOException
  {
    CacheEntry localCacheEntry = null;
    localCacheEntry = getResourceCacheEntry(paramURL, paramString1, paramString2, null, false, paramInt);
    return localCacheEntry != null;
  }

  public static void removeCachedResource(URL paramURL, String paramString1, String paramString2)
  {
    CacheEntry localCacheEntry = null;
    try
    {
      localCacheEntry = getResourceCacheEntry(paramURL, paramString1, paramString2, null, false);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    if (localCacheEntry != null)
    {
      if (MemoryCache.isCacheEntryLoaded(paramURL.toString(), paramString2))
        MemoryCache.removeLoadedResource(paramURL.toString());
      Cache.removeCacheEntry(localCacheEntry);
    }
  }

  public static Map getCachedHeaders(URL paramURL, String paramString)
    throws IOException
  {
    CacheEntry localCacheEntry = null;
    localCacheEntry = getResourceCacheEntry(paramURL, null, paramString, null, false);
    if (localCacheEntry != null)
      return localCacheEntry.getHeaders();
    return null;
  }

  public static HttpRequest getHttpRequestImpl()
  {
    return _httpRequestImpl;
  }

  public static HttpDownload getHttpDownloadImpl()
  {
    return _httpDownloadImpl;
  }

  public static File applyPatch(File paramFile1, File paramFile2, URL paramURL, String paramString1, DownloadDelegate paramDownloadDelegate, String paramString2)
    throws FailedDownloadException
  {
    JarDiffPatcher localJarDiffPatcher = new JarDiffPatcher();
    File localFile = new File(paramString2);
    FileOutputStream localFileOutputStream = null;
    int i = 0;
    try
    {
      localFileOutputStream = new FileOutputStream(localFile);
      3 local3 = null;
      if (paramDownloadDelegate != null)
      {
        paramDownloadDelegate.patching(paramURL, paramString1, 0);
        local3 = new Patcher.PatchDelegate(paramDownloadDelegate, paramURL, paramString1)
        {
          private final DownloadEngine.DownloadDelegate val$delegate;
          private final URL val$location;
          private final String val$newVersion;

          public void patching(int paramInt)
            throws CancelException
          {
            this.val$delegate.patching(this.val$location, this.val$newVersion, paramInt);
          }
        };
      }
      try
      {
        localJarDiffPatcher.applyPatch(local3, paramFile1.getPath(), paramFile2.getPath(), localFileOutputStream);
      }
      catch (IOException localIOException3)
      {
        throw new FailedDownloadException(paramURL, paramString1, localIOException3);
      }
      i = 1;
    }
    catch (IOException localIOException2)
    {
      throw new FailedDownloadException(paramURL, paramString1, localIOException2);
    }
    finally
    {
      try
      {
        if (localFileOutputStream != null)
          localFileOutputStream.close();
      }
      catch (IOException localIOException4)
      {
        Trace.ignoredException(localIOException4);
      }
      if (i == 0)
        localFile.delete();
      paramFile2.delete();
      if ((paramDownloadDelegate != null) && (i == 0))
        try
        {
          paramDownloadDelegate.downloadFailed(paramURL, paramString1);
        }
        catch (CancelException localCancelException2)
        {
          throw new FailedDownloadException(paramURL, paramString1, localCancelException2);
        }
    }
    return localFile;
  }

  public static boolean isJnlpURL(URL paramURL)
  {
    try
    {
      HttpResponse localHttpResponse = getHttpRequestImpl().doHeadRequest(paramURL);
      return localHttpResponse.getContentType().equals("application/x-java-jnlp-file");
    }
    catch (IOException localIOException)
    {
    }
    return false;
  }

  public static boolean isUrlInAppContext(URL paramURL)
  {
    String str = (String)ToolkitStore.get().getAppContext().get("deploy-bg-" + paramURL);
    return (str != null) && (str.equals("background"));
  }

  public static boolean isUpdateAvailable(URL paramURL, String paramString)
    throws IOException
  {
    return isUpdateAvailable(paramURL, paramString, false, null);
  }

  public static boolean isUpdateAvailable(URL paramURL, String paramString, boolean paramBoolean)
    throws IOException
  {
    return isUpdateAvailable(paramURL, paramString, paramBoolean, null);
  }

  public static boolean isUpdateAvailable(URL paramURL, String paramString, boolean paramBoolean, Map paramMap)
    throws IOException
  {
    if (UpdateTracker.isUpdated(paramURL.toString()))
      return false;
    if (paramString != null)
      return false;
    URL localURL1 = HttpUtils.removeQueryStringFromURL(paramURL);
    if ((!isBackgroundUpdateRequest()) && (isUrlInAppContext(paramURL)))
      return false;
    CacheEntry localCacheEntry = null;
    if (Cache.isCacheEnabled())
      localCacheEntry = Cache.getCacheEntry(paramString == null ? paramURL : localURL1, null, paramString);
    if (!isValidationRequired(localCacheEntry))
      return false;
    if (!DeployOfflineManager.promptUserGoOnline(paramURL))
      throw new FailedDownloadException(paramURL, null, null, true);
    if (DeployOfflineManager.isGlobalOffline())
      throw new FailedDownloadException(paramURL, null, null, true);
    if (localCacheEntry == null)
      return true;
    URL localURL2 = getRequestURL(paramURL, null, paramString, null, null, null, null, false, null, paramString == null ? 1 : 2);
    HttpRequest localHttpRequest = getHttpRequestImpl();
    HttpResponse localHttpResponse = null;
    long l1 = -1L;
    l1 = localCacheEntry.getLastModified();
    URL localURL3 = null;
    if (paramBoolean)
      localURL3 = getPack200Url(localURL2);
    String[] arrayOfString1 = null;
    String[] arrayOfString2 = null;
    if (paramMap != null)
    {
      arrayOfString1 = (String[])(String[])paramMap.keySet().toArray(new String[0]);
      arrayOfString2 = new String[arrayOfString1.length];
      for (int i = 0; i < arrayOfString1.length; i++)
      {
        Object localObject = paramMap.get(arrayOfString1[i]);
        if ((localObject == null) || (!(localObject instanceof List)))
          continue;
        arrayOfString2[i] = ((String)(String)((List)localObject).get(0));
      }
    }
    try
    {
      localHttpResponse = localHttpRequest.doGetRequestEX(localURL3 != null ? localURL3 : localURL2, arrayOfString1, arrayOfString2, l1);
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      if (localURL3 == null)
        throw localFileNotFoundException;
      localHttpResponse = localHttpRequest.doGetRequestEX(localURL2, l1);
    }
    if (localHttpResponse == null)
      return true;
    int j = localHttpResponse.getStatusCode();
    localHttpResponse.disconnect();
    boolean bool = true;
    if (j == 304)
    {
      bool = false;
    }
    else if (j == 200)
    {
      int k = localHttpResponse.getContentLength();
      long l3 = localHttpResponse.getLastModified();
      if ((l3 == l1) && (k == localCacheEntry.getContentLength()))
        bool = false;
      Trace.println("CacheEntry[" + localURL1 + "]: updateAvailable=" + bool + ",lastModified=" + new Date(l1) + ",length=" + localCacheEntry.getContentLength(), TraceLevel.DEFAULT);
    }
    if (!bool)
    {
      UpdateTracker.checkDone(paramURL.toString());
      long l2 = localHttpResponse.getExpiration();
      if (l2 != 0L)
        localCacheEntry.updateExpirationInIndexFile(l2);
    }
    return bool;
  }

  private static String getVersionJarPath(String paramString1, String paramString2)
  {
    String str1 = paramString1.substring(paramString1.lastIndexOf("/") + 1);
    paramString1 = paramString1.substring(0, paramString1.lastIndexOf("/") + 1);
    String str2 = str1;
    String str3 = null;
    if (str1.lastIndexOf(".") != -1)
    {
      str3 = str1.substring(str1.lastIndexOf(".") + 1);
      str1 = str1.substring(0, str1.lastIndexOf("."));
    }
    StringBuffer localStringBuffer = new StringBuffer(str1);
    if (paramString2 != null)
    {
      localStringBuffer.append("__V");
      localStringBuffer.append(paramString2);
    }
    if (str3 != null)
    {
      localStringBuffer.append(".");
      localStringBuffer.append(str3);
    }
    paramString1 = paramString1 + localStringBuffer.toString();
    return paramString1;
  }

  private static URL getEmbeddedVersionUrl(URL paramURL, String paramString)
  {
    if ((paramString == null) || (paramString.indexOf("*") != -1) || (paramString.indexOf("+") != -1))
      return paramURL;
    URL localURL = null;
    String str1 = paramURL.getProtocol();
    String str2 = paramURL.getHost();
    int i = paramURL.getPort();
    String str3 = paramURL.getPath();
    str3 = getVersionJarPath(str3, paramString);
    try
    {
      localURL = new URL(str1, str2, i, str3);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignoredException(localMalformedURLException);
    }
    return localURL;
  }

  public static URL getPack200Url(URL paramURL)
  {
    URL localURL = null;
    if (paramURL == null)
      return null;
    String str1 = paramURL.getProtocol();
    String str2 = paramURL.getHost();
    int i = paramURL.getPort();
    String str3 = paramURL.getPath();
    String str4 = paramURL.getQuery();
    StringBuffer localStringBuffer = new StringBuffer(str3);
    localStringBuffer.append(".pack.gz");
    if (str4 != null)
    {
      localStringBuffer.append("?");
      localStringBuffer.append(str4);
    }
    try
    {
      localURL = new URL(str1, str2, i, localStringBuffer.toString());
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignoredException(localMalformedURLException);
    }
    return localURL;
  }

  static CacheEntry actionDownload(CacheEntry paramCacheEntry, URL paramURL1, URL paramURL2, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, int paramInt, boolean paramBoolean1, boolean paramBoolean2)
    throws IOException
  {
    int i = 0;
    int j = -1;
    boolean bool1 = false;
    String str1 = null;
    if ((paramCacheEntry != null) && (paramString2 != null))
    {
      if ((paramCacheEntry.getVersion() != null) && (new VersionString(paramString2).contains(new VersionID(paramCacheEntry.getVersion()))))
        return paramCacheEntry;
      if (Environment.isJavaPlugin())
      {
        long l1 = paramCacheEntry.getExpirationDate();
        if ((l1 != 0L) && (new Date().after(new Date(l1))))
          paramCacheEntry = null;
      }
    }
    if (paramCacheEntry != null)
      str1 = paramCacheEntry.getVersion();
    CacheEntry localCacheEntry1 = null;
    try
    {
      setDownloadInProgress(true);
      HttpRequest localHttpRequest = getHttpRequestImpl();
      HttpResponse localHttpResponse = null;
      long l2 = 0L;
      if (paramCacheEntry != null)
        l2 = paramCacheEntry.getLastModified();
      URL localURL = paramURL2;
      if (isVersionContentType(paramInt))
        localURL = getEmbeddedVersionUrl(localURL, paramString2);
      if (isPackContentType(paramInt))
        localURL = getPack200Url(localURL);
      try
      {
        try
        {
          localHttpResponse = localHttpRequest.doGetRequestEX(localURL, l2);
        }
        catch (FileNotFoundException localFileNotFoundException1)
        {
          if (paramURL2.toString().equals(localURL.toString()))
            throw localFileNotFoundException1;
          localHttpResponse = localHttpRequest.doGetRequestEX(paramURL2, l2);
          i = 1;
          j = paramInt;
          if ((isPackContentType(paramInt)) && ((isNativeContentType(paramInt)) || (isJarContentType(paramInt))))
            j &= -4097;
          if (isVersionContentType(paramInt))
            j &= -65537;
        }
      }
      catch (FailedDownloadException localFailedDownloadException)
      {
        throw localFailedDownloadException;
      }
      catch (FileNotFoundException localFileNotFoundException2)
      {
        throw localFileNotFoundException2;
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
        localHttpResponse = localHttpRequest.doGetRequest(paramURL2, false);
      }
      int k = localHttpResponse.getStatusCode();
      if (k == 404)
        throw new FailedDownloadException(paramURL1, paramString2, new IOException("HTTP response 404"));
      if (k == 304)
      {
        localHttpResponse.disconnect();
        CacheEntry localCacheEntry2 = paramCacheEntry;
        return localCacheEntry2;
      }
      int m = localHttpResponse.getContentLength();
      long l3 = localHttpResponse.getLastModified();
      long l4 = localHttpResponse.getExpiration();
      boolean bool2 = "application/x-java-jnlp-file".equals(localHttpResponse.getContentType());
      if ((!bool2) && (Environment.isImportMode()) && (Environment.getImportModeExpiration() != null) && (l4 != 0L))
      {
        localObject1 = new Date(l4);
        if (((Date)localObject1).before(Environment.getImportModeExpiration()))
        {
          localHttpResponse.disconnect();
          localObject2 = null;
          return localObject2;
        }
      }
      else if ((!bool2) && (Environment.isImportMode()) && (Environment.getImportModeTimestamp() != null) && (l3 != 0L))
      {
        localObject1 = new Date(l3);
        if (((Date)localObject1).before(Environment.getImportModeTimestamp()))
        {
          localHttpResponse.disconnect();
          localObject2 = null;
          return localObject2;
        }
      }
      Object localObject1 = localHttpResponse.getResponseHeader("x-java-jnlp-version-id");
      if ((paramString2 != null) && (localObject1 == null) && (Environment.getImportModeCodebaseOverride() != null) && (new VersionID(paramString2).isSimpleVersion()))
        localObject1 = paramString2;
      if (paramCacheEntry != null)
      {
        if ((localObject1 != null) && (new VersionString((String)localObject1).contains(str1)))
        {
          localHttpResponse.disconnect();
          localObject2 = paramCacheEntry;
          return localObject2;
        }
        if ((m == paramCacheEntry.getContentLength()) && (l3 == l2) && (str1 == null))
        {
          localHttpResponse.disconnect();
          localObject2 = paramCacheEntry;
          return localObject2;
        }
        if (str1 == null)
          bool1 = true;
      }
      if (paramDownloadDelegate != null)
        paramDownloadDelegate.setTotalSize(m);
      if (localObject1 == null)
        if (!Environment.isJavaPlugin())
        {
          if (!isVersionContentType(i != 0 ? j : paramInt));
        }
        else
          localObject1 = paramString2;
      Object localObject2 = localObject1;
      String str2 = localHttpResponse.getContentType();
      boolean bool3 = (str2 != null) && (str2.equalsIgnoreCase("application/x-java-archive-diff"));
      if (Trace.isEnabled(TraceLevel.NETWORK))
        Trace.println(ResourceManager.getString("downloadEngine.serverResponse", String.valueOf(m), new Date(l3).toString(), (String)localObject1, str2), TraceLevel.NETWORK);
      if ((str2 != null) && (str2.equalsIgnoreCase("application/x-java-jnlp-error")))
      {
        localObject3 = localHttpResponse.getInputStream();
        localObject4 = new BufferedReader(new InputStreamReader((InputStream)localObject3));
        localObject5 = ((BufferedReader)localObject4).readLine();
        ((BufferedReader)localObject4).close();
        throw new FailedDownloadException(paramURL2, paramString2, new IOException("Error returned: " + (String)localObject5));
      }
      if ((localObject1 == null) && (paramString2 != null))
        if (!isVersionContentType(i != 0 ? j : paramInt))
          throw new FailedDownloadException(paramURL1, paramString2, new IOException("missing version response from server"));
      if ((localObject1 != null) && (paramString2 != null) && (!paramBoolean1))
      {
        if (!new VersionString(paramString2).contains((String)localObject1))
          throw new FailedDownloadException(paramURL1, paramString2, new IOException("bad version response from server:" + (String)localObject1));
        localObject3 = new VersionID((String)localObject1);
        if (!((VersionID)localObject3).isSimpleVersion())
          throw new FailedDownloadException(paramURL1, paramString2, new IOException("bad version response from server:" + (String)localObject1));
      }
      Object localObject3 = paramDownloadDelegate == null ? null : new HttpDownloadListener(paramDownloadDelegate, paramURL1, (String)localObject2)
      {
        private final DownloadEngine.DownloadDelegate val$dd;
        private final URL val$href;
        private final String val$responseVersion;

        public boolean downloadProgress(int paramInt1, int paramInt2)
          throws CancelException
        {
          this.val$dd.downloading(this.val$href, this.val$responseVersion, paramInt1, paramInt2, false);
          return true;
        }
      };
      if ((paramBoolean2) && (allowCaching(localHttpResponse)))
      {
        localObject4 = Cache.downloadResourceToCache(paramURL1, (String)localObject1, localHttpResponse, (HttpDownloadListener)localObject3, paramDownloadDelegate, bool1, paramURL2, paramCacheEntry, bool3, i != 0 ? j : paramInt);
        return localObject4;
      }
      Object localObject4 = Cache.downloadResourceToTempFile(paramURL1, (String)localObject1, localHttpResponse, (HttpDownloadListener)localObject3, paramDownloadDelegate, bool1, paramURL2, paramCacheEntry, bool3, i != 0 ? j : paramInt);
      ((CacheEntry)localObject4).setBusy(CacheEntry.BUSY_TRUE);
      ((CacheEntry)localObject4).setIncomplete(CacheEntry.INCOMPLETE_TRUE);
      ((CacheEntry)localObject4).updateIndexHeaderOnDisk();
      Object localObject5 = localObject4;
      return localObject5;
    }
    catch (Exception localException)
    {
      if (localCacheEntry1 != null)
        Cache.removeCacheEntry(localCacheEntry1);
      if ((localException instanceof JARSigningException))
        throw ((JARSigningException)localException);
      if ((localException instanceof FailedDownloadException))
        throw ((FailedDownloadException)localException);
      Trace.ignored(localException);
      throw new FailedDownloadException(paramURL2, paramString2, localException);
    }
    finally
    {
      setDownloadInProgress(false);
    }
    throw localObject6;
  }

  private static boolean allowCaching(HttpResponse paramHttpResponse)
  {
    if (paramHttpResponse != null)
    {
      String str = paramHttpResponse.getResponseHeader("cache-control");
      if ((str != null) && (str.toLowerCase().indexOf("no-store") != -1))
      {
        Trace.println("Not caching resource due to response header: cache-control: no-store", TraceLevel.NETWORK);
        return false;
      }
    }
    return true;
  }

  private static void addURLArgument(StringBuffer paramStringBuffer, String paramString1, String paramString2)
  {
    try
    {
      paramStringBuffer.append(URLEncoder.encode(paramString1, "UTF-8"));
      paramStringBuffer.append('=');
      paramStringBuffer.append(URLEncoder.encode(paramString2, "UTF-8"));
      paramStringBuffer.append('&');
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      Trace.ignoredException(localUnsupportedEncodingException);
    }
  }

  private static URL getRequestURL(URL paramURL, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, boolean paramBoolean, String paramString7, int paramInt)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    if ((paramString3 == null) && (paramString2 != null))
      paramString3 = Cache.getCacheEntryVersion(paramURL, paramString1);
    if ((paramString2 != null) && (paramInt == 2))
    {
      addURLArgument(localStringBuffer, "version-id", paramString2);
      if (paramString3 != null)
        addURLArgument(localStringBuffer, "current-version-id", paramString3);
    }
    if ((paramString2 != null) && (paramInt == 3))
    {
      if (paramBoolean)
        addURLArgument(localStringBuffer, "platform-version-id", paramString2);
      else
        addURLArgument(localStringBuffer, "version-id", paramString2);
      addURLArgument(localStringBuffer, "arch", Config.getOSArch());
      addURLArgument(localStringBuffer, "os", Config.getOSName());
      addURLArgument(localStringBuffer, "locale", defaultLocaleString);
      if (paramString7 != null)
        addURLArgument(localStringBuffer, "known-platforms", paramString7);
    }
    if (localStringBuffer.length() > 0)
      localStringBuffer.setLength(localStringBuffer.length() - 1);
    if (localStringBuffer.length() > 0)
      localStringBuffer.insert(0, '?');
    try
    {
      if ((Environment.getImportModeCodebaseOverride() != null) && (Environment.getImportModeCodebase() != null))
        return new URL(Environment.getImportModeCodebaseOverride() + paramURL.getFile().substring(Environment.getImportModeCodebase().getFile().length()) + localStringBuffer);
      return new URL(paramURL.getProtocol(), paramURL.getHost(), paramURL.getPort(), paramURL.getFile() + localStringBuffer);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignoredException(localMalformedURLException);
    }
    return null;
  }

  public static CacheEntry getResourceTempCacheEntry(URL paramURL, String paramString, int paramInt)
    throws IOException
  {
    CacheEntry localCacheEntry1 = Cache.getCacheEntry(paramURL, null, paramString, paramInt);
    CacheEntry localCacheEntry2 = getCacheEntry(localCacheEntry1, paramURL, null, paramString, null, false, null, paramInt, false);
    return localCacheEntry2;
  }

  public static CacheEntry getCacheEntryTemp(URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, boolean paramBoolean, String paramString3, int paramInt)
    throws IOException
  {
    return getCacheEntry(null, paramURL, paramString1, paramString2, paramDownloadDelegate, paramBoolean, paramString3, paramInt, false);
  }

  private static CacheEntry getCacheEntry(CacheEntry paramCacheEntry, URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, boolean paramBoolean, String paramString3, int paramInt)
    throws IOException
  {
    return getCacheEntry(paramCacheEntry, paramURL, paramString1, paramString2, paramDownloadDelegate, paramBoolean, paramString3, paramInt, true);
  }

  private static CacheEntry getCacheEntry(CacheEntry paramCacheEntry, URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, boolean paramBoolean1, String paramString3, int paramInt, boolean paramBoolean2)
    throws IOException
  {
    int i = 1;
    if (paramString3 != null)
      i = 3;
    else if (paramString2 != null)
      i = 2;
    URL localURL = getRequestURL(paramURL, paramString1, paramString2, null, null, null, null, paramBoolean1, paramString3, i);
    return actionDownload(paramCacheEntry, paramURL, localURL, paramString1, paramString2, paramDownloadDelegate, paramInt, paramBoolean1, paramBoolean2);
  }

  private static CacheEntry getResourceCacheEntry(URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, boolean paramBoolean, int paramInt)
    throws IOException
  {
    return getResourceCacheEntry(paramURL, paramString1, paramString2, paramDownloadDelegate, paramBoolean, false, null, paramInt);
  }

  private static CacheEntry getResourceCacheEntry(URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, boolean paramBoolean)
    throws IOException
  {
    return getResourceCacheEntry(paramURL, paramString1, paramString2, paramDownloadDelegate, paramBoolean, false, null, 1);
  }

  private static boolean isValidationRequired(CacheEntry paramCacheEntry)
  {
    if (paramCacheEntry == null)
      return true;
    int i = 1;
    if (!paramCacheEntry.isExpired())
      i = 0;
    else if (Trace.isEnabled(TraceLevel.CACHE))
      Trace.println("Resource " + paramCacheEntry.getURL() + " has expired.", TraceLevel.CACHE);
    if (paramCacheEntry.isHttpNoCacheEnabled())
    {
      i = 1;
      if (Trace.isEnabled(TraceLevel.CACHE))
        Trace.println("Resource " + paramCacheEntry.getURL() + " has cache control: no-cache.", TraceLevel.CACHE);
    }
    return i;
  }

  private static CacheEntry getResourceCacheEntry(URL paramURL, String paramString1, String paramString2, DownloadDelegate paramDownloadDelegate, boolean paramBoolean1, boolean paramBoolean2, String paramString3, int paramInt)
    throws IOException
  {
    if (((!Cache.isCacheEnabled()) && (!paramBoolean1)) || (paramURL == null))
      return null;
    CacheEntry localCacheEntry = null;
    int i = (!Cache.isCacheEnabled()) || ((paramBoolean1) && (isUpdateAvailable(paramURL, paramString2, isPackContentType(paramInt)))) ? 1 : 0;
    if (i == 0)
    {
      localCacheEntry = (CacheEntry)MemoryCache.getLoadedResource(paramURL.toString());
      if ((localCacheEntry != null) && (localCacheEntry.matchesVersionString(paramString2, true)))
        return localCacheEntry;
    }
    if (Cache.isCacheEnabled())
      localCacheEntry = Cache.getCacheEntry(paramURL, paramString1, paramString2, paramInt);
    if ((paramBoolean1) && ((localCacheEntry == null) || (isValidationRequired(localCacheEntry))))
    {
      Environment.setDownloadInitiated(true);
      if (Cache.isCacheEnabled())
        localCacheEntry = getCacheEntry(localCacheEntry, paramURL, paramString1, paramString2, paramDownloadDelegate, paramBoolean2, paramString3, paramInt);
      else
        localCacheEntry = getCacheEntryTemp(paramURL, paramString1, paramString2, paramDownloadDelegate, paramBoolean2, paramString3, paramInt);
      if (localCacheEntry != null)
      {
        Trace.println("Downloaded " + paramURL + ": " + localCacheEntry.getResourceFilename(), TraceLevel.NETWORK);
        MemoryCache.addLoadedResource(paramURL.toString(), localCacheEntry);
      }
      if ((localCacheEntry != null) && (Cache.isCacheEnabled()))
        Cache.touch(new File(localCacheEntry.getResourceFilename() + Cache.getIndexFileExtension()));
    }
    return localCacheEntry;
  }

  public static String getAvailableVersion(URL paramURL, String paramString1, boolean paramBoolean, String paramString2)
  {
    int i = paramString2 != null ? 3 : 2;
    URL localURL = getRequestURL(paramURL, null, paramString1, null, null, null, null, paramBoolean, paramString2, i);
    HttpRequest localHttpRequest = getHttpRequestImpl();
    HttpResponse localHttpResponse = null;
    String str = null;
    try
    {
      localHttpResponse = localHttpRequest.doGetRequest(localURL);
      if (localHttpResponse != null)
      {
        str = localHttpResponse.getResponseHeader("x-java-jnlp-version-id");
        localHttpResponse.disconnect();
      }
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
    return str;
  }

  static boolean isJarHeaderValid(byte[] paramArrayOfByte)
  {
    return get32(paramArrayOfByte, 0) == 67324752L;
  }

  private static final int get16(byte[] paramArrayOfByte, int paramInt)
  {
    return paramArrayOfByte[paramInt] & 0xFF | (paramArrayOfByte[(paramInt + 1)] & 0xFF) << 8;
  }

  private static final long get32(byte[] paramArrayOfByte, int paramInt)
  {
    return get16(paramArrayOfByte, paramInt) | get16(paramArrayOfByte, paramInt + 2) << 16;
  }

  static
  {
    _httpRequestImpl = new BasicHttpRequest();
    _httpDownloadImpl = new HttpDownloadHelper(_httpRequestImpl);
  }

  public static abstract interface DownloadDelegate
  {
    public abstract void setTotalSize(long paramLong);

    public abstract void downloading(URL paramURL, String paramString, int paramInt1, int paramInt2, boolean paramBoolean)
      throws CancelException;

    public abstract void validating(URL paramURL, int paramInt1, int paramInt2)
      throws CancelException;

    public abstract void patching(URL paramURL, String paramString, int paramInt)
      throws CancelException;

    public abstract void downloadFailed(URL paramURL, String paramString)
      throws CancelException;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.DownloadEngine
 * JD-Core Version:    0.6.0
 */