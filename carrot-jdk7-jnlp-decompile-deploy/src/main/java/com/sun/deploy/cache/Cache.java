package com.sun.deploy.cache;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.BasicHttpRequest;
import com.sun.deploy.net.CanceledDownloadException;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.DownloadEngine.DownloadDelegate;
import com.sun.deploy.net.HttpDownload;
import com.sun.deploy.net.HttpDownloadListener;
import com.sun.deploy.net.HttpResponse;
import com.sun.deploy.net.HttpUtils;
import com.sun.deploy.net.MessageHeader;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.URLUtil;
import com.sun.deploy.util.VersionString;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class Cache
{
  static final boolean DEBUG = (Config.getDeployDebug()) || (Config.getPluginDebug());
  public static final int VERSION_INT = 604;
  static final int VERSION_603 = 603;
  static final int VERSION_602 = 602;
  public static final String VERSION6_STRING = "6.0";
  public static final String VERSION_STRING = "6.0";
  private static boolean doIPLookup = true;
  private static final String IP_ADDR_CANNOT_RESOLVE = "0.0.0.0";
  private static final String DASH = "-";
  static final String INDEX_FILE_EXT = ".idx";
  static final String MUFFIN_FILE_EXT = ".muf";
  static final String HOST_FILE_EXT = ".hst";
  static final String LAP_FILE_EXT = ".lap";
  static final String MUFFIN_DIRNAME = "muffin";
  static final String SPLASH_DIRNAME = "splash";
  static final String HOST_DIRNAME = "host";
  static final int NUM_OF_CACHE_SUBDIR = 64;
  static final char APPLICATION_TYPE = 'A';
  static final char EXTENSION_TYPE = 'E';
  private static SecureRandom random = null;
  private static String cachePath;
  private static File cacheDir;
  private static File sysCacheDir;
  private static File muffinDir;
  private static File hostDir;
  private static final int BUF_SIZE = 32768;
  private static final Map loadedProperties = new HashMap();
  private static final CleanupThread ct;
  private static final Object syncObject = new Object();
  private static boolean cleanupEnabled = true;
  public static final long TIME_WAIT_BEFORE_JAR_PERVERIFY = 30000L;
  static final String LAST_ACCESS_FILE = "lastAccessed";
  static final String REMOVED_APPS = "removed.apps";
  private static final String SIGNED_JNLP_ENTRY = "JNLP-INF/APPLICATION.JNLP";
  private static final String SIGNED_JNLP_TEMPLATE = "JNLP-INF/APPLICATION_TEMPLATE.JNLP";

  public static void setCleanupEnabled(boolean paramBoolean)
  {
    cleanupEnabled = paramBoolean;
  }

  public static void setDoIPLookup(boolean paramBoolean)
  {
    doIPLookup = paramBoolean;
  }

  private static void createCacheBucketDirectories(String paramString)
  {
    for (int i = 0; i < 64; i++)
    {
      File localFile = new File(paramString + File.separator + i);
      localFile.mkdir();
    }
  }

  public static void reset()
  {
    synchronized (syncObject)
    {
      MemoryCache.reset();
      synchronized (loadedProperties)
      {
        loadedProperties.clear();
      }
      cachePath = Config.getCacheDirectory() + File.separator + "6.0";
      ??? = cachePath + File.separator + "muffin";
      String str1 = cachePath + File.separator + "host";
      cacheDir = new File(cachePath);
      muffinDir = new File((String)???);
      hostDir = new File(str1);
      if (!cacheDir.exists())
      {
        cacheDir.mkdirs();
        Cache6Upgrader.initializeUpgraderKeys();
      }
      hostDir.mkdirs();
      createCacheBucketDirectories(cachePath);
      muffinDir.mkdirs();
      if ((Config.getSystemCacheDirectory() != null) && (Config.getSystemCacheDirectory().length() != 0))
      {
        String str2 = Config.getSystemCacheDirectory() + File.separator + "6.0";
        sysCacheDir = new File(str2);
        if (Environment.isSystemCacheMode())
        {
          sysCacheDir.mkdirs();
          createCacheBucketDirectories(str2);
        }
      }
      else
      {
        sysCacheDir = null;
      }
      long l = Config.getCacheSizeMax();
      if ((l > 0L) && (l < 5242880L))
        l = 5242880L;
    }
  }

  static void addToCleanupThreadLoadedResourceList(String paramString)
  {
    if ((ct != null) && (cleanupEnabled))
      ct.addToLoadedResourceList(paramString);
  }

  static void cleanup()
  {
    if ((ct != null) && (cleanupEnabled))
      ct.startCleanup();
  }

  public static boolean hasIncompatibleCompressEncoding(CacheEntry paramCacheEntry)
  {
    return (paramCacheEntry != null) && (DownloadEngine.isInternalUse()) && (paramCacheEntry.hasCompressEncoding());
  }

  static void markResourceIncomplete(CacheEntry paramCacheEntry)
  {
    if (paramCacheEntry != null)
      synchronized (paramCacheEntry)
      {
        paramCacheEntry.setIncomplete(CacheEntry.INCOMPLETE_TRUE);
        try
        {
          paramCacheEntry.updateIndexHeaderOnDisk();
        }
        catch (IOException localIOException)
        {
          Trace.ignoredException(localIOException);
        }
      }
  }

  static boolean isSystemCacheEntry(CacheEntry paramCacheEntry)
  {
    if ((paramCacheEntry != null) && (sysCacheDir != null))
    {
      File localFile = paramCacheEntry.getIndexFile();
      if ((localFile != null) && (localFile.getParentFile() != null))
        return sysCacheDir.equals(localFile.getParentFile().getParentFile());
    }
    return false;
  }

  public static CacheEntry getSystemCacheEntry(URL paramURL, String paramString)
  {
    if (paramURL == null)
      return null;
    CacheEntry localCacheEntry = (CacheEntry)MemoryCache.getLoadedResource(paramURL.toString());
    if (isSystemCacheEntry(localCacheEntry))
    {
      String str = localCacheEntry.getVersion();
      if (((paramString == null) && (str == null)) || ((paramString != null) && (str != null) && (str.compareTo(paramString) >= 0)))
        return localCacheEntry;
    }
    localCacheEntry = getCacheEntry(paramURL, null, paramString, sysCacheDir);
    if (localCacheEntry == null)
      localCacheEntry = CacheUpgrader.getSystemInstance().upgradeItem(paramURL, paramString, 1);
    if (localCacheEntry != null)
      MemoryCache.addLoadedResource(paramURL.toString(), localCacheEntry);
    return localCacheEntry;
  }

  public static boolean isSupportedProtocol(URL paramURL)
  {
    String str = paramURL.getProtocol();
    return (str != null) && ((str.equalsIgnoreCase("http")) || (str.equalsIgnoreCase("https")));
  }

  public static boolean isCacheEnabled()
  {
    return Config.getBooleanProperty("deployment.cache.enabled");
  }

  public static void removeLoadedProperties(String paramString)
  {
    synchronized (loadedProperties)
    {
      loadedProperties.remove(paramString);
    }
  }

  public static LocalApplicationProperties getLocalApplicationProperties(CacheEntry paramCacheEntry)
  {
    URL localURL = null;
    try
    {
      localURL = new URL(paramCacheEntry.getURL());
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignoredException(localMalformedURLException);
    }
    return getLocalApplicationProperties(localURL, paramCacheEntry.getVersion(), true);
  }

  public static LocalApplicationProperties getLocalApplicationProperties(String paramString)
  {
    if (!isCacheEnabled())
      return null;
    File localFile = new File(paramString + ".idx");
    CacheEntry localCacheEntry = (CacheEntry)AccessController.doPrivileged(new PrivilegedAction(localFile)
    {
      private final File val$file;

      public Object run()
      {
        return Cache.getCacheEntryFromFile(this.val$file);
      }
    });
    if (localCacheEntry == null)
      return null;
    URL localURL1 = null;
    try
    {
      localURL1 = new URL(localCacheEntry.getURL());
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignoredException(localMalformedURLException);
      return null;
    }
    URL localURL2 = localURL1;
    return (LocalApplicationProperties)AccessController.doPrivileged(new PrivilegedAction(localURL2, localCacheEntry)
    {
      private final URL val$url;
      private final CacheEntry val$ce;

      public Object run()
      {
        return Cache.getLocalApplicationProperties(this.val$url, this.val$ce.getVersion(), true);
      }
    });
  }

  public static LocalApplicationProperties getLocalApplicationProperties(URL paramURL)
  {
    return getLocalApplicationProperties(paramURL, null, true);
  }

  public static LocalApplicationProperties getLocalApplicationProperties(URL paramURL, String paramString, boolean paramBoolean)
  {
    Object localObject1 = null;
    if ((isCacheEnabled()) && (paramURL != null))
    {
      String str = paramURL.toString() + "?" + paramString;
      synchronized (loadedProperties)
      {
        localObject1 = (LocalApplicationProperties)loadedProperties.get(str);
        if (localObject1 == null)
        {
          localObject1 = new DefaultLocalApplicationProperties(paramURL, paramString, paramBoolean);
          loadedProperties.put(str, localObject1);
        }
        else
        {
          ((LocalApplicationProperties)localObject1).refreshIfNecessary();
        }
      }
    }
    return (LocalApplicationProperties)localObject1;
  }

  public static String getLapFileName(URL paramURL, String paramString)
  {
    String str = getKey(paramURL);
    return getBucket(str) + File.separator + str + "6.0" + getVersionTag(paramString) + ".lap";
  }

  public static void putLapData(char paramChar, URL paramURL, String paramString, byte[] paramArrayOfByte)
    throws IOException
  {
    File localFile1 = getActiveCacheDir();
    File localFile2 = new File(localFile1, getLapFileName(paramURL, paramString));
    ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramArrayOfByte);
    BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile2));
    byte[] arrayOfByte = new byte[32768];
    try
    {
      for (int i = localByteArrayInputStream.read(arrayOfByte); i >= 0; i = localByteArrayInputStream.read(arrayOfByte))
        localBufferedOutputStream.write(arrayOfByte, 0, i);
    }
    finally
    {
      localBufferedOutputStream.close();
      localByteArrayInputStream.close();
    }
  }

  public static byte[] getLapData(char paramChar, URL paramURL, String paramString, boolean paramBoolean)
    throws IOException
  {
    File localFile1 = paramBoolean ? sysCacheDir : cacheDir;
    if (localFile1 == null)
      return null;
    File localFile2 = new File(localFile1, getLapFileName(paramURL, paramString));
    return getLapBytes(localFile2);
  }

  public static byte[] getLapBytes(File paramFile)
    throws IOException
  {
    ByteArrayOutputStream localByteArrayOutputStream = null;
    try
    {
      localByteArrayOutputStream = (ByteArrayOutputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(paramFile)
      {
        private final File val$lapFile;

        public Object run()
          throws IOException
        {
          long l = this.val$lapFile.length();
          if ((l > 0L) && (l < 1048576L))
          {
            BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(this.val$lapFile));
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream((int)l);
            byte[] arrayOfByte = new byte[32768];
            try
            {
              for (int i = localBufferedInputStream.read(arrayOfByte); i >= 0; i = localBufferedInputStream.read(arrayOfByte))
                localByteArrayOutputStream.write(arrayOfByte, 0, i);
            }
            finally
            {
              localByteArrayOutputStream.close();
              localBufferedInputStream.close();
            }
            return localByteArrayOutputStream;
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
    if (localByteArrayOutputStream != null)
      return localByteArrayOutputStream.toByteArray();
    return null;
  }

  public static String getNewExtensionInstallDirectory()
    throws IOException
  {
    String str1 = cacheDir.getAbsolutePath() + File.separator + "ext";
    String str2 = null;
    int i = 0;
    do
    {
      str2 = str1 + File.separator + "E" + new Date().getTime() + File.separator;
      File localFile = new File(str2);
      if (!localFile.mkdirs())
        str2 = null;
      Thread.yield();
      if (str2 != null)
        break;
      i++;
    }
    while (i < 50);
    if (str2 == null)
      throw new IOException("Unable to create temp. dir for extension");
    return str2;
  }

  public static String getCacheEntryVersion(URL paramURL, String paramString)
  {
    String str = null;
    CacheEntry localCacheEntry = getLatestCacheEntry(paramURL, paramString);
    if (localCacheEntry != null)
      str = localCacheEntry.getVersion();
    return str;
  }

  private static void writeBytes(File paramFile, byte[] paramArrayOfByte)
    throws IOException
  {
    BufferedOutputStream localBufferedOutputStream = null;
    int i = 0;
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(paramFile);
      localBufferedOutputStream = new BufferedOutputStream(localFileOutputStream);
      localBufferedOutputStream.write(paramArrayOfByte);
    }
    catch (IOException localIOException)
    {
      i = 1;
      throw localIOException;
    }
    finally
    {
      if (localBufferedOutputStream != null)
        localBufferedOutputStream.close();
      if (i != 0)
        paramFile.delete();
    }
  }

  private static CacheEntry createNewCacheEntry(URL paramURL, String paramString)
    throws IOException
  {
    String str = generateCacheFileName(paramURL, paramString);
    File localFile1 = getActiveCacheDir();
    File localFile2 = new File(localFile1, str + getIndexFileExtension());
    CacheEntry localCacheEntry = new CacheEntry(localFile2);
    localCacheEntry.writeFileToDisk();
    return localCacheEntry;
  }

  static CacheEntry downloadResourceToCache(URL paramURL1, String paramString, URLConnection paramURLConnection, URL paramURL2, boolean paramBoolean1, int paramInt, InputStream paramInputStream, boolean paramBoolean2)
    throws IOException, CanceledDownloadException
  {
    CacheEntry localCacheEntry = null;
    try
    {
      localCacheEntry = (CacheEntry)AccessController.doPrivileged(new PrivilegedExceptionAction(paramURLConnection, paramInt, paramURL1, paramString, paramInputStream, paramBoolean2, paramBoolean1, paramURL2)
      {
        private final URLConnection val$conn;
        private final int val$contentType;
        private final URL val$href;
        private final String val$downloadVersion;
        private final InputStream val$is;
        private final boolean val$decompress;
        private final boolean val$applyJarDiff;
        private final URL val$requestURL;

        public Object run()
          throws IOException, CanceledDownloadException
        {
          String str1 = this.val$conn.getContentEncoding();
          if (DownloadEngine.isPackContentType(this.val$contentType))
            str1 = "pack200-gzip";
          CacheEntry localCacheEntry = Cache.access$000(this.val$href, this.val$downloadVersion);
          MessageHeader localMessageHeader1 = DownloadEngine.getHttpDownloadImpl().download(this.val$conn.getContentLength(), this.val$conn.getURL(), this.val$is, str1, localCacheEntry.getTempDataFile(), null, this.val$contentType, this.val$decompress);
          if (localCacheEntry.processTempDataFile(this.val$applyJarDiff, null, this.val$href, this.val$requestURL, this.val$downloadVersion))
          {
            localCacheEntry.setBusy(CacheEntry.BUSY_FALSE);
            localCacheEntry.setIncomplete(CacheEntry.INCOMPLETE_FALSE);
            localCacheEntry.setURL(this.val$downloadVersion == null ? this.val$requestURL.toString() : this.val$href.toString());
            if (this.val$applyJarDiff)
              localCacheEntry.setContentLength((int)new File(localCacheEntry.getResourceFilename()).length());
            else
              localCacheEntry.setContentLength(this.val$conn.getContentLength());
            localCacheEntry.setLastModified(this.val$conn.getLastModified());
            localCacheEntry.setExpirationDate(this.val$conn.getExpiration());
            if (this.val$downloadVersion != null)
              localCacheEntry.setVersion(this.val$downloadVersion);
            MessageHeader localMessageHeader2 = BasicHttpRequest.initializeHeaderFields(this.val$conn);
            localMessageHeader2 = MessageHeader.merge(localMessageHeader2, localMessageHeader1);
            if ((this.val$conn instanceof HttpURLConnection))
            {
              ((HttpURLConnection)this.val$conn).disconnect();
              String str2 = this.val$conn.getRequestProperty("content-type");
              if ((str2 != null) && (localMessageHeader2 != null))
                localMessageHeader2.add("deploy-request-content-type", str2);
            }
            localCacheEntry.setHeaders(localMessageHeader2);
            localCacheEntry.writeFileToDisk(this.val$contentType, null);
            Cache.recordLastAccessed();
            return localCacheEntry;
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      if ((localPrivilegedActionException.getException() instanceof IOException))
        throw ((IOException)localPrivilegedActionException.getException());
      if ((localPrivilegedActionException.getException() instanceof CanceledDownloadException))
        throw ((CanceledDownloadException)localPrivilegedActionException.getException());
    }
    return localCacheEntry;
  }

  public static CacheEntry downloadResourceToCache(URL paramURL1, String paramString, HttpResponse paramHttpResponse, HttpDownloadListener paramHttpDownloadListener, DownloadEngine.DownloadDelegate paramDownloadDelegate, boolean paramBoolean1, URL paramURL2, CacheEntry paramCacheEntry, boolean paramBoolean2, int paramInt)
    throws IOException, CanceledDownloadException
  {
    URL localURL1 = HttpUtils.getFinalRedirectedURL(paramHttpResponse);
    int i = 0;
    if ((localURL1 != null) && (!URLUtil.sameURLs(localURL1, paramURL2)))
      i = 1;
    URL localURL2 = i != 0 ? localURL1 : paramURL1;
    URL localURL3 = i != 0 ? localURL1 : paramURL2;
    CacheEntry localCacheEntry1 = downloadResourceToTempFile(localURL2, paramString, paramHttpResponse, paramHttpDownloadListener, paramDownloadDelegate, paramBoolean1, localURL3, paramCacheEntry, paramBoolean2, paramInt);
    if (localCacheEntry1 != null)
    {
      CacheEntry localCacheEntry2 = processNewCacheEntry(paramURL1, paramBoolean1, localCacheEntry1, paramCacheEntry);
      if (i != 0)
        createRedirectEntry(paramURL1, localURL1, localCacheEntry2);
      return localCacheEntry2;
    }
    return null;
  }

  public static CacheEntry downloadResourceToTempFile(URL paramURL1, String paramString, HttpResponse paramHttpResponse, HttpDownloadListener paramHttpDownloadListener, DownloadEngine.DownloadDelegate paramDownloadDelegate, boolean paramBoolean1, URL paramURL2, CacheEntry paramCacheEntry, boolean paramBoolean2, int paramInt)
    throws IOException, CanceledDownloadException
  {
    CacheEntry localCacheEntry = createNewCacheEntry(paramURL1, paramString);
    String str = paramHttpResponse.getContentEncoding();
    if (DownloadEngine.isPackContentType(paramInt))
      str = "pack200-gzip";
    MessageHeader localMessageHeader1 = null;
    boolean bool = DownloadEngine.isInternalUse();
    try
    {
      BufferedInputStream localBufferedInputStream = paramHttpResponse.getInputStream();
      localMessageHeader1 = DownloadEngine.getHttpDownloadImpl().download(paramHttpResponse.getContentLength(), paramHttpResponse.getRequest(), localBufferedInputStream, str, localCacheEntry.getTempDataFile(), paramHttpDownloadListener, paramInt, bool);
    }
    catch (IOException localIOException)
    {
      removeCacheEntry(localCacheEntry);
      throw localIOException;
    }
    catch (CanceledDownloadException localCanceledDownloadException)
    {
      removeCacheEntry(localCacheEntry);
      throw localCanceledDownloadException;
    }
    finally
    {
      paramHttpResponse.disconnect();
    }
    if (localCacheEntry.processTempDataFile(paramBoolean2, paramDownloadDelegate, paramURL1, paramURL2, paramString))
    {
      localCacheEntry.setURL((paramString == null) && (!Environment.isImportMode()) ? paramURL2.toString() : paramURL1.toString());
      MessageHeader localMessageHeader2 = paramHttpResponse.getHeaders();
      if (localMessageHeader2 != null)
      {
        if (DownloadEngine.isJarContentType(paramInt))
          localMessageHeader2.add("deploy-request-content-type", "application/x-java-archive");
        localMessageHeader2 = MessageHeader.merge(localMessageHeader2, localMessageHeader1);
      }
      localCacheEntry.setHeaders(localMessageHeader2);
      localCacheEntry.setContentLength(paramHttpResponse.getContentLength());
      localCacheEntry.setLastModified(paramHttpResponse.getLastModified());
      localCacheEntry.setExpirationDate(paramHttpResponse.getExpiration());
      if (paramString != null)
        localCacheEntry.setVersion(paramString);
      localCacheEntry.writeFileToDisk(paramInt, paramDownloadDelegate);
      return localCacheEntry;
    }
    return null;
  }

  public static boolean isBackgroundVerificationEnabled()
  {
    if (!isCacheEnabled())
      return false;
    String str = System.getProperty("jnlp.disableBackgroundVerification");
    if ((str != null) && (str.equalsIgnoreCase("true")))
    {
      Trace.println("Cached JAR background verification disabled", TraceLevel.CACHE);
      return false;
    }
    return true;
  }

  public static CacheEntry processNewCacheEntry(URL paramURL, boolean paramBoolean, CacheEntry paramCacheEntry1, CacheEntry paramCacheEntry2)
    throws IOException
  {
    Trace.println("Cache: Enable a new CacheEntry: " + paramURL.toString(), TraceLevel.NETWORK);
    paramCacheEntry1.setBusy(CacheEntry.BUSY_FALSE);
    paramCacheEntry1.setIncomplete(CacheEntry.INCOMPLETE_FALSE);
    paramCacheEntry1.updateIndexHeaderOnDisk();
    if (paramCacheEntry2 != null)
    {
      if ((DownloadEngine.isBackgroundUpdateRequest()) && (MemoryCache.isCacheEntryLoaded(paramURL.toString(), paramCacheEntry2.getVersion())))
      {
        paramBoolean = false;
        paramCacheEntry2.markIncompleteOnHold();
      }
      if (paramBoolean)
        removeCacheEntry(paramCacheEntry2, false);
    }
    recordLastAccessed();
    return paramCacheEntry1;
  }

  public static int getCacheVersion()
  {
    return 604;
  }

  public static String getCacheVersionString()
  {
    return "6.0";
  }

  public static long getCacheSize(boolean paramBoolean)
  {
    long l = 0L;
    File[] arrayOfFile = getCacheEntries(paramBoolean);
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      l += arrayOfFile[i].length();
      CacheEntry localCacheEntry = getCacheEntryFromFile(arrayOfFile[i]);
      if (localCacheEntry == null)
        continue;
      l += new File(localCacheEntry.getResourceFilename()).length();
      l += getTotalSize(new File(localCacheEntry.getNativeLibPath()));
    }
    return l;
  }

  private static long getTotalSize(File paramFile)
  {
    long l = 0L;
    if ((paramFile != null) && (paramFile.exists()))
      if (paramFile.isDirectory())
      {
        File[] arrayOfFile = paramFile.listFiles();
        for (int i = 0; i < arrayOfFile.length; i++)
          l += getTotalSize(arrayOfFile[i]);
      }
      else
      {
        l += paramFile.length();
      }
    return l;
  }

  private static SecureRandom getSecureRandom()
  {
    if (random == null)
    {
      random = ServiceManager.getService().getSecureRandom();
      random.nextInt();
    }
    return random;
  }

  public static boolean exists()
  {
    if (Environment.isSystemCacheMode())
      return (sysCacheDir != null) && (sysCacheDir.exists());
    return cacheDir.exists();
  }

  public static boolean canWrite()
  {
    if (Environment.isSystemCacheMode())
      return (sysCacheDir != null) && (sysCacheDir.canWrite());
    return cacheDir.canWrite();
  }

  public static CacheEntry addLoadedResource(URL paramURL, String paramString1, String paramString2)
  {
    if (paramURL == null)
      return null;
    CacheEntry localCacheEntry = (CacheEntry)MemoryCache.getLoadedResource(paramURL.toString());
    if (localCacheEntry != null)
      return localCacheEntry;
    localCacheEntry = getCacheEntry(paramURL, paramString1, paramString2);
    if (localCacheEntry != null)
      return (CacheEntry)MemoryCache.addLoadedResource(paramURL.toString(), localCacheEntry);
    return null;
  }

  public static CacheEntry createOrUpdateCacheEntry(URL paramURL, byte[] paramArrayOfByte)
    throws IOException
  {
    CacheEntry localCacheEntry1 = getCacheEntry(paramURL, null, null, getActiveCacheDir());
    CacheEntry localCacheEntry2 = createNewCacheEntry(paramURL, null);
    writeBytes(localCacheEntry2.getTempDataFile(), paramArrayOfByte);
    if (localCacheEntry2.processTempDataFile(false, null, paramURL, paramURL, null))
    {
      localCacheEntry2.setBusy(CacheEntry.BUSY_FALSE);
      localCacheEntry2.setIncomplete(CacheEntry.INCOMPLETE_FALSE);
      localCacheEntry2.setURL(paramURL.toString());
      localCacheEntry2.writeFileToDisk();
      if (localCacheEntry1 != null)
        removeCacheEntry(localCacheEntry1, false);
      recordLastAccessed();
      addLoadedResource(localCacheEntry2);
    }
    else
    {
      removeCacheEntry(localCacheEntry2, false);
    }
    return localCacheEntry2;
  }

  public static CacheEntry createRedirectEntry(URL paramURL1, URL paramURL2, CacheEntry paramCacheEntry)
    throws IOException
  {
    if (paramCacheEntry == null)
    {
      paramCacheEntry = getCacheEntry(paramURL2, null, null);
      if (paramCacheEntry == null)
        return null;
    }
    CacheEntry localCacheEntry = createNewCacheEntry(paramURL1, paramCacheEntry.getVersion());
    localCacheEntry.processRedirectData(paramURL1, paramCacheEntry);
    recordLastAccessed();
    addLoadedResource(paramCacheEntry);
    addLoadedResource(paramURL1.toString(), paramCacheEntry);
    return localCacheEntry;
  }

  private static void addLoadedResource(CacheEntry paramCacheEntry)
  {
    addLoadedResource(paramCacheEntry.getURL().toString(), paramCacheEntry);
  }

  public static void addLoadedResource(String paramString, CacheEntry paramCacheEntry)
  {
    CacheEntry localCacheEntry = (CacheEntry)MemoryCache.getLoadedResource(paramString);
    if (localCacheEntry != paramCacheEntry)
    {
      localCacheEntry = (CacheEntry)MemoryCache.addLoadedResource(paramString, paramCacheEntry);
      if ((localCacheEntry != paramCacheEntry) && (localCacheEntry != null) && (localCacheEntry.getVersion() == null) && (paramCacheEntry.getVersion() == null))
        markResourceIncomplete(localCacheEntry);
    }
  }

  public static String getIndexFileExtension()
  {
    return ".idx";
  }

  static String getVersionTag(String paramString)
  {
    return "-" + paramString + "-";
  }

  public static File getCacheDir()
  {
    return cacheDir;
  }

  static File getSystemCacheDir()
  {
    return sysCacheDir;
  }

  public static void setSystemCacheDir(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0))
    {
      sysCacheDir = null;
    }
    else
    {
      String str = paramString + File.separator + "6.0";
      sysCacheDir = new File(str);
    }
  }

  public static File getActiveCacheDir()
  {
    return Environment.isSystemCacheMode() ? sysCacheDir : cacheDir;
  }

  public static File[] getCacheEntries(boolean paramBoolean)
  {
    File localFile = (paramBoolean) || (Environment.isSystemCacheMode()) ? sysCacheDir : cacheDir;
    if (localFile == null)
      return new File[0];
    return getIndexFiles(localFile);
  }

  public static File[] getIndexFiles(File paramFile)
  {
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < 64; i++)
    {
      File localFile = new File(paramFile, String.valueOf(i));
      if (!localFile.isDirectory())
        continue;
      File[] arrayOfFile = localFile.listFiles(new FileFilter()
      {
        public boolean accept(File paramFile)
        {
          String str = paramFile.getName();
          boolean bool = str.endsWith(".idx");
          return bool;
        }
      });
      localArrayList.addAll(Arrays.asList(arrayOfFile));
    }
    return (File[])(File[])localArrayList.toArray(new File[localArrayList.size()]);
  }

  public static ArrayList getJnlpCacheEntries(boolean paramBoolean)
  {
    ArrayList localArrayList = new ArrayList();
    File[] arrayOfFile = getCacheEntries(paramBoolean);
    if (arrayOfFile != null)
      for (int i = 0; i < arrayOfFile.length; i++)
      {
        CacheEntry localCacheEntry = getCacheEntryFromFile(arrayOfFile[i]);
        if ((localCacheEntry == null) || (!localCacheEntry.isJNLPFile()))
          continue;
        String str1 = arrayOfFile[i].getPath();
        String str2 = str1.substring(0, str1.length() - 4);
        localArrayList.add(new File(str2));
      }
    return localArrayList;
  }

  public static void removeRemovedApp(String paramString1, String paramString2)
  {
    Properties localProperties = getRemovedApps();
    String str = localProperties.getProperty(paramString1);
    if ((str != null) && (str.equals(paramString2)))
    {
      localProperties.remove(paramString1);
      setRemovedApps(localProperties);
    }
  }

  public static void saveRemovedApp(URL paramURL, String paramString)
  {
    if (Environment.isSystemCacheMode())
    {
      if (getCacheEntry(paramURL, null, null, cacheDir) != null)
        return;
    }
    else if (getCacheEntry(paramURL, null, null, sysCacheDir) != null)
      return;
    Properties localProperties = getRemovedApps();
    localProperties.setProperty(paramURL.toString(), paramString);
    setRemovedApps(localProperties);
  }

  public static void setRemovedApps(Properties paramProperties)
  {
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(getRemovePath());
      paramProperties.store(localFileOutputStream, "Removed JNLP Applications");
      localFileOutputStream.close();
    }
    catch (IOException localIOException)
    {
    }
  }

  public static Properties getRemovedApps()
  {
    Properties localProperties = new Properties();
    FileInputStream localFileInputStream = null;
    try
    {
      localFileInputStream = new FileInputStream(getRemovePath());
      localProperties.load(localFileInputStream);
    }
    catch (Throwable localIOException2)
    {
    }
    finally
    {
      if (localFileInputStream != null)
        try
        {
          localFileInputStream.close();
        }
        catch (IOException localIOException3)
        {
        }
    }
    return localProperties;
  }

  public static long getLastAccessed(boolean paramBoolean)
  {
    File localFile = new File(paramBoolean ? sysCacheDir : cacheDir, "lastAccessed");
    Long localLong = (Long)AccessController.doPrivileged(new PrivilegedAction(localFile)
    {
      private final File val$f;

      public Object run()
      {
        return new Long(this.val$f.lastModified());
      }
    });
    return localLong.longValue();
  }

  static void recordLastAccessed()
  {
    File localFile = new File(getActiveCacheDir(), "lastAccessed");
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
      localFileOutputStream.write(46);
      localFileOutputStream.close();
    }
    catch (IOException localIOException)
    {
    }
  }

  public static String getRemovePath()
  {
    return cachePath + File.separator + "removed.apps";
  }

  public static boolean removeCacheEntry(URL paramURL, String paramString1, String paramString2)
  {
    CacheEntry localCacheEntry = getCacheEntry(paramURL, paramString1, paramString2, getActiveCacheDir());
    return removeCacheEntry(localCacheEntry);
  }

  public static void touch(File paramFile)
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramFile)
      {
        private final File val$file;

        public Object run()
          throws IOException
        {
          this.val$file.setLastModified(System.currentTimeMillis());
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Trace.ignoredException(localPrivilegedActionException);
    }
  }

  public static void removeAllCacheEntries(CacheEntry paramCacheEntry)
  {
    if (paramCacheEntry == null)
      return;
    URL localURL = null;
    try
    {
      localURL = new URL(paramCacheEntry.getURL());
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    String str1 = paramCacheEntry.getVersion();
    removeCacheEntry(paramCacheEntry);
    if (localURL == null)
      return;
    File[] arrayOfFile = getMatchingIndexFiles(getActiveCacheDir(), localURL);
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      CacheEntry localCacheEntry = new CacheEntry(arrayOfFile[i]);
      String str2 = localCacheEntry.getVersion();
      if (((str1 != null) || (str2 != null)) && ((str1 == null) || (!str1.equals(str2))))
        continue;
      removeCacheEntry(localCacheEntry);
    }
  }

  public static int removeDuplicateEntries(boolean paramBoolean1, boolean paramBoolean2)
  {
    int i = 0;
    File[] arrayOfFile = getCacheEntries(paramBoolean1);
    for (int j = 0; j < arrayOfFile.length; j++)
    {
      CacheEntry localCacheEntry = getCacheEntryFromFile(arrayOfFile[j]);
      i += removeDuplicateEntries(localCacheEntry, paramBoolean2);
    }
    if (i > 0)
      Trace.println("Remove All Duplicates: " + i + " bytes", TraceLevel.NETWORK);
    return i;
  }

  public static int removeDuplicateEntries(CacheEntry paramCacheEntry, boolean paramBoolean)
  {
    int i = 0;
    String str1 = null != paramCacheEntry ? paramCacheEntry.getURL() : null;
    if ((paramCacheEntry == null) || (str1 == null) || (paramCacheEntry.isIncomplete()) || (MemoryCache.contains(str1)))
      return i;
    URL localURL = null;
    try
    {
      localURL = new URL(str1);
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    if (localURL == null)
      return i;
    File[] arrayOfFile = getMatchingIndexFiles(cacheDir, localURL);
    Object localObject1 = paramCacheEntry;
    Object localObject2;
    String str4;
    String str5;
    for (int j = 0; j < arrayOfFile.length; j++)
    {
      CacheEntry localCacheEntry = new CacheEntry(arrayOfFile[j]);
      String str3 = ((CacheEntry)localObject1).getVersion();
      localObject2 = localCacheEntry.getVersion();
      if (((str3 != null) || (localObject2 != null)) && ((str3 == null) || (!str3.equals(localObject2))))
        continue;
      str4 = paramBoolean ? null : ((CacheEntry)localObject1).getCodebaseIP();
      str5 = paramBoolean ? null : localCacheEntry.getCodebaseIP();
      if (((str4 != str5) && ((str4 == null) || (!str4.equals(str5)))) || (!((CacheEntry)localObject1).removeBefore(localCacheEntry)))
        continue;
      localObject1 = localCacheEntry;
    }
    String str2 = ((CacheEntry)localObject1).getVersion();
    int k = 0;
    for (int m = 0; m < arrayOfFile.length; m++)
    {
      localObject2 = new CacheEntry(arrayOfFile[m]);
      str4 = ((CacheEntry)localObject2).getVersion();
      if (((str2 != null) || (str4 != null)) && ((str2 == null) || (!str2.equals(str4))))
        continue;
      str5 = paramBoolean ? null : ((CacheEntry)localObject1).getCodebaseIP();
      String str6 = paramBoolean ? null : ((CacheEntry)localObject2).getCodebaseIP();
      if (((str5 != str6) && ((str5 == null) || (!str5.equals(str6)))) || (((CacheEntry)localObject2).getIndexFile().equals(((CacheEntry)localObject1).getIndexFile())) || (!removeCacheEntry((CacheEntry)localObject2, false)))
        continue;
      i += ((CacheEntry)localObject2).getContentLength();
      k++;
    }
    if (i > 0)
    {
      Trace.println("Remove " + k + " Duplicates of: [" + ((CacheEntry)localObject1).getURL() + ", ", TraceLevel.NETWORK);
      Trace.println("\tidx: " + ((CacheEntry)localObject1).getIndexFile() + "], " + i + " bytes", TraceLevel.NETWORK);
    }
    return i;
  }

  public static boolean removeCacheEntry(CacheEntry paramCacheEntry, boolean paramBoolean)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(paramCacheEntry, paramBoolean)
    {
      private final CacheEntry val$ce;
      private final boolean val$removeLAP;

      public Object run()
      {
        boolean bool = Cache.access$100(this.val$ce, this.val$removeLAP);
        return Boolean.valueOf(String.valueOf(bool));
      }
    });
    boolean bool = localBoolean.booleanValue();
    if ((!bool) && (!paramCacheEntry.isIncomplete()))
      markResourceIncomplete(paramCacheEntry);
    return bool;
  }

  private static boolean removeCacheEntryImpl(CacheEntry paramCacheEntry, boolean paramBoolean)
  {
    if (Trace.isEnabled(TraceLevel.NETWORK))
      Trace.println(ResourceManager.getString("cache.removeCacheEntry", paramCacheEntry == null ? "" : paramCacheEntry.getURL()), TraceLevel.NETWORK);
    if (!paramCacheEntry.getIndexFile().getParentFile().getParentFile().equals(getActiveCacheDir()))
      return true;
    File localFile1 = new File(paramCacheEntry.getResourceFilename());
    File localFile2 = paramCacheEntry.getIndexFile();
    String str1 = paramCacheEntry.getURL();
    String str2 = paramCacheEntry.getVersion();
    boolean bool1 = false;
    boolean bool2 = false;
    if (!MemoryCache.contains(str1))
    {
      bool1 = localFile1.delete();
      if (!bool1)
      {
        Trace.println("Failed to delete: " + localFile1, TraceLevel.CACHE);
        localFile1.deleteOnExit();
      }
      bool2 = localFile2.delete();
      if (!bool2)
        localFile2.deleteOnExit();
    }
    else
    {
      localFile1.deleteOnExit();
      localFile2.deleteOnExit();
    }
    clobber(new File(paramCacheEntry.getNativeLibPath()));
    try
    {
      String str3 = getLapFileName(new URL(str1), str2);
      File localFile4 = new File(getActiveCacheDir(), str3);
      if ((localFile4.exists()) && (paramBoolean))
        localFile4.delete();
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    File localFile3 = new File(paramCacheEntry.getResourceFilename() + ".ico");
    ensureFileDeleted(localFile3);
    recordLastAccessed();
    MemoryCache.removeLoadedResource(paramCacheEntry.getURL());
    return (bool1) && (bool2);
  }

  private static void clobber(File paramFile)
  {
    if (paramFile.exists())
      if (paramFile.isDirectory())
      {
        File[] arrayOfFile = paramFile.listFiles();
        for (int i = 0; i < arrayOfFile.length; i++)
          clobber(arrayOfFile[i]);
        ensureFileDeleted(paramFile);
      }
      else
      {
        ensureFileDeleted(paramFile);
      }
  }

  private static void ensureFileDeleted(File paramFile)
  {
    if ((paramFile != null) && (paramFile.exists()) && (!paramFile.delete()))
      paramFile.deleteOnExit();
  }

  public static boolean removeCacheEntry(CacheEntry paramCacheEntry)
  {
    return removeCacheEntry(paramCacheEntry, true);
  }

  static File[] getMatchingIndexFiles(File paramFile, URL paramURL)
  {
    String str = getKey(paramURL);
    File localFile = new File(paramFile.getPath() + File.separator + getBucket(str));
    File[] arrayOfFile = (File[])(File[])AccessController.doPrivileged(new PrivilegedAction(localFile, str)
    {
      private final File val$directory;
      private final String val$key;

      public Object run()
      {
        File[] arrayOfFile = this.val$directory.listFiles(new FileFilter()
        {
          public boolean accept(File paramFile)
          {
            String str = paramFile.getName();
            return (str.startsWith(Cache.9.this.val$key)) && (str.endsWith(".idx"));
          }
        });
        return arrayOfFile;
      }
    });
    return arrayOfFile;
  }

  public static CacheEntry getCacheEntryFromFile(File paramFile)
  {
    return getCacheEntryFromFile(paramFile, false);
  }

  public static CacheEntry getCacheEntryFromFile(File paramFile, boolean paramBoolean)
  {
    CacheEntry localCacheEntry = new CacheEntry(paramFile, paramBoolean);
    if ((localCacheEntry != null) && (!localCacheEntry.isIncomplete()) && (isCacheEntryIPValid(localCacheEntry)))
      return localCacheEntry;
    return null;
  }

  public static String getVersionFromFilename(String paramString)
  {
    int i = paramString.indexOf("-");
    int j = paramString.indexOf("-", i + 1);
    int k = paramString.lastIndexOf("-");
    if ((j >= 0) && (k > j + 1))
      return paramString.substring(j + 1, k);
    return null;
  }

  public static CacheEntry getLatestCacheEntry(URL paramURL, String paramString)
  {
    File localFile = null;
    String str1 = null;
    File[] arrayOfFile = getMatchingIndexFiles(cacheDir, paramURL);
    for (int i = 0; i < arrayOfFile.length; i++)
      if (localFile == null)
      {
        localFile = arrayOfFile[i];
      }
      else
      {
        String str2 = getVersionFromFilename(arrayOfFile[i].getName());
        String str3 = getVersionFromFilename(localFile.getName());
        if ((str2 == null) || ((str3 != null) && (str2.compareTo(str3) <= 0)))
          continue;
        localFile = arrayOfFile[i];
      }
    if (localFile != null)
      return getCacheEntryFromFile(localFile);
    return CacheUpgrader.getInstance().upgradeItem(paramURL, str1, 1);
  }

  private static CacheEntry getCacheEntryFromIdxFiles(File[] paramArrayOfFile, URL paramURL, String paramString, int paramInt)
  {
    CacheEntry localCacheEntry1 = null;
    CacheEntry localCacheEntry2 = null;
    if (paramArrayOfFile != null)
      for (int i = 0; i < paramArrayOfFile.length; i++)
      {
        localCacheEntry1 = new CacheEntry(paramArrayOfFile[i]);
        if (localCacheEntry1.getIncomplete() == CacheEntry.INCOMPLETE_FALSE)
        {
          if (localCacheEntry1.getURL().equals(paramURL.toString()))
          {
            if (!isCacheEntryIPValid(localCacheEntry1))
              continue;
            if ((paramString == null) && (localCacheEntry1.getVersion() == null))
            {
              localCacheEntry2 = localCacheEntry1;
              break;
            }
            if ((paramString == null) || (!new VersionString(paramString).contains(localCacheEntry1.getVersion())))
              continue;
            if (localCacheEntry2 == null)
            {
              localCacheEntry2 = localCacheEntry1;
            }
            else
            {
              if ((localCacheEntry1.getVersion() == null) || (localCacheEntry1.getVersion().compareTo(localCacheEntry2.getVersion()) <= 0))
                continue;
              localCacheEntry2 = localCacheEntry1;
            }
          }
          else
          {
            if (localCacheEntry1.getURL().indexOf('?') == -1)
              continue;
            removeCacheEntry(localCacheEntry1, false);
          }
        }
        else
          cleanup();
      }
    if ((localCacheEntry2 != null) && (DownloadEngine.isNativeContentType(paramInt)) && (!localCacheEntry2.isRedirectEntry()))
    {
      File localFile = new File(localCacheEntry2.getNativeLibPath());
      if (!localFile.isDirectory())
      {
        removeCacheEntry(localCacheEntry2);
        localCacheEntry2 = null;
      }
    }
    return localCacheEntry2;
  }

  public static void updateHostIPFile(String paramString)
  {
    String str1 = getCachedHostIP(paramString);
    if (str1 == null)
    {
      createHostEntry(paramString);
    }
    else
    {
      String str2 = getCurrentIP(paramString, str1);
      if ((str2 != null) && (!str2.equals(str1)))
        updateHostEntry(paramString);
    }
  }

  private static URL getHostURL(String paramString)
  {
    URL localURL = null;
    try
    {
      localURL = new URL("http://" + paramString);
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    return localURL;
  }

  private static void updateHostEntry(String paramString)
  {
    URL localURL = getHostURL(paramString);
    File[] arrayOfFile = getMatchingHostFiles(hostDir, localURL);
    AccessController.doPrivileged(new PrivilegedAction(arrayOfFile)
    {
      private final File[] val$hostFiles;

      public Object run()
      {
        for (int i = 0; i < this.val$hostFiles.length; i++)
          this.val$hostFiles[i].delete();
        return null;
      }
    });
    createHostEntry(paramString);
  }

  private static String getCurrentIP(String paramString1, String paramString2)
  {
    if ((paramString1 == null) || (paramString2 == null))
      return null;
    if (!doIPLookup)
      return null;
    String str = null;
    Class localClass = null;
    try
    {
      localClass = Class.forName("java.net.InetAddress");
    }
    catch (ClassNotFoundException localClassNotFoundException1)
    {
      Trace.ignoredException(localClassNotFoundException1);
      return null;
    }
    InetAddress localInetAddress1 = null;
    try
    {
      localInetAddress1 = InetAddress.getByName(paramString2);
    }
    catch (UnknownHostException localUnknownHostException)
    {
      Trace.ignoredException(localUnknownHostException);
    }
    Object[] arrayOfObject = { paramString1, localInetAddress1 };
    Class[] arrayOfClass = new Class[2];
    try
    {
      arrayOfClass[0] = Class.forName("java.lang.String");
      arrayOfClass[1] = localClass;
    }
    catch (ClassNotFoundException localClassNotFoundException2)
    {
      Trace.ignoredException(localClassNotFoundException2);
      return null;
    }
    Method localMethod;
    try
    {
      localMethod = localClass.getDeclaredMethod("getByName", arrayOfClass);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      return null;
    }
    AccessController.doPrivileged(new PrivilegedAction(localMethod)
    {
      private final Method val$lookupMethod;

      public Object run()
      {
        this.val$lookupMethod.setAccessible(true);
        return null;
      }
    });
    if (!Modifier.isStatic(localMethod.getModifiers()))
      return null;
    InetAddress localInetAddress2 = null;
    try
    {
      localInetAddress2 = (InetAddress)(InetAddress)localMethod.invoke((Object)null, arrayOfObject);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      Trace.ignoredException(localIllegalAccessException);
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
    }
    if (localInetAddress2 != null)
      str = localInetAddress2.getHostAddress();
    return str;
  }

  private static boolean isCacheEntryIPValid(CacheEntry paramCacheEntry)
  {
    int i = 1;
    if ((paramCacheEntry != null) && (paramCacheEntry.isKnownToBeSigned()))
      return i;
    String str1 = paramCacheEntry.getCodebaseIP();
    if (str1 == null)
      return i;
    URL localURL = null;
    try
    {
      localURL = new URL(paramCacheEntry.getURL());
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignoredException(localMalformedURLException);
      return false;
    }
    String str2 = localURL.getHost();
    String str3 = getCurrentIP(str2, str1);
    if (str3 == null)
      return true;
    if (!str3.equals(str1))
    {
      Trace.println("CacheEntry IP mismatch: " + str3 + " != " + str1, TraceLevel.CACHE);
      i = 0;
    }
    return i;
  }

  public static CacheEntry getCacheEntry(URL paramURL, String paramString1, String paramString2)
  {
    return getCacheEntry(paramURL, paramString1, paramString2, 1);
  }

  public static CacheEntry getCacheEntry(URL paramURL, String paramString1, String paramString2, int paramInt)
  {
    CacheEntry localCacheEntry1 = (CacheEntry)MemoryCache.getLoadedResource(paramURL.toString());
    if ((localCacheEntry1 != null) && (localCacheEntry1.matchesVersionString(paramString2, true)))
      return localCacheEntry1;
    Object localObject = getCacheEntry(paramURL, paramString1, paramString2, cacheDir, paramInt);
    if (localObject == null)
      localObject = CacheUpgrader.getInstance().upgradeItem(paramURL, paramString2, paramInt);
    CacheEntry localCacheEntry2 = getCacheEntry(paramURL, paramString1, paramString2, sysCacheDir, paramInt);
    if (localCacheEntry2 == null)
      localCacheEntry2 = CacheUpgrader.getSystemInstance().upgradeItem(paramURL, paramString2, paramInt);
    if (Environment.isSystemCacheMode())
    {
      if (localCacheEntry2 != null)
      {
        MemoryCache.addLoadedResource(paramURL.toString(), localCacheEntry2);
        Trace.println("System Cache: " + ResourceManager.getString("cache.getCacheEntry.return.found", paramURL == null ? "" : paramURL.toString(), paramString2) + " prevalidated=" + localCacheEntry2.isKnownToBeSigned() + "/" + localCacheEntry2.getClassesVerificationStatus(), TraceLevel.NETWORK);
      }
      return localCacheEntry2;
    }
    if (localCacheEntry2 != null)
      if (localObject != null)
      {
        if (((CacheEntry)localObject).getLastModified() <= localCacheEntry2.getLastModified())
          localObject = localCacheEntry2;
      }
      else
        localObject = localCacheEntry2;
    if (hasIncompatibleCompressEncoding((CacheEntry)localObject))
    {
      markResourceIncomplete((CacheEntry)localObject);
      localObject = null;
    }
    if (localObject == null)
    {
      if (Trace.isEnabled(TraceLevel.NETWORK))
        Trace.println(ResourceManager.getString("cache.getCacheEntry.return.notfound", paramURL == null ? "" : paramURL.toString(), paramString2), TraceLevel.NETWORK);
    }
    else
    {
      if (Trace.isEnabled(TraceLevel.NETWORK))
        Trace.println(ResourceManager.getString("cache.getCacheEntry.return.found", paramURL == null ? "" : paramURL.toString(), paramString2) + " prevalidated=" + ((CacheEntry)localObject).isKnownToBeSigned() + "/" + ((CacheEntry)localObject).getClassesVerificationStatus(), TraceLevel.NETWORK);
      MemoryCache.addLoadedResource(paramURL.toString(), localObject);
    }
    return (CacheEntry)localObject;
  }

  public static CacheEntry getCacheEntry(URL paramURL, String paramString1, String paramString2, File paramFile)
  {
    return getCacheEntry(paramURL, paramString1, paramString2, paramFile, 1);
  }

  private static CacheEntry getCacheEntry(URL paramURL, String paramString1, String paramString2, File paramFile, int paramInt)
  {
    if (paramFile == null)
      return null;
    File[] arrayOfFile = getMatchingIndexFiles(paramFile, paramURL);
    CacheEntry localCacheEntry = getCacheEntryFromIdxFiles(arrayOfFile, paramURL, paramString2, paramInt);
    return followsRedirect(localCacheEntry, paramFile, paramInt);
  }

  private static CacheEntry followsRedirect(CacheEntry paramCacheEntry, File paramFile, int paramInt)
  {
    URL localURL = paramCacheEntry == null ? null : paramCacheEntry.getRedirectFinalURL();
    if (localURL != null)
    {
      CacheEntry localCacheEntry = getCacheEntry(localURL, null, paramCacheEntry.getVersion(), paramFile, paramInt);
      if (localCacheEntry != null)
        addLoadedResource(localCacheEntry);
      return localCacheEntry;
    }
    return paramCacheEntry;
  }

  private static File[] getMatchingMuffinFiles(File paramFile, URL paramURL)
  {
    String str = getKey(paramURL);
    File[] arrayOfFile = paramFile.listFiles(new FileFilter(str)
    {
      private final String val$key;

      public boolean accept(File paramFile)
      {
        String str = paramFile.getName();
        return (str.startsWith(this.val$key)) && (!str.endsWith(".muf"));
      }
    });
    return arrayOfFile;
  }

  private static File[] getMatchingMuffinAttributeFiles(File paramFile, URL paramURL)
  {
    String str = getKey(paramURL);
    File[] arrayOfFile = paramFile.listFiles(new FileFilter(str)
    {
      private final String val$key;

      public boolean accept(File paramFile)
      {
        String str = paramFile.getName();
        return (str.startsWith(this.val$key)) && (str.endsWith(".muf"));
      }
    });
    return arrayOfFile;
  }

  public static File getMuffinFile(URL paramURL)
  {
    String str = getKey(paramURL);
    File[] arrayOfFile = getMatchingMuffinFiles(muffinDir, paramURL);
    if ((arrayOfFile == null) || (arrayOfFile.length == 0))
      return null;
    return arrayOfFile[0];
  }

  public static File getMuffinAttributeFile(URL paramURL)
  {
    String str = getKey(paramURL);
    File[] arrayOfFile = getMatchingMuffinAttributeFiles(muffinDir, paramURL);
    if ((arrayOfFile == null) || (arrayOfFile.length == 0))
      return null;
    return arrayOfFile[0];
  }

  public static long[] getMuffinAttributes(URL paramURL)
    throws IOException
  {
    BufferedReader localBufferedReader = null;
    long l1 = -1L;
    long l2 = -1L;
    try
    {
      File localFile = getMuffinAttributeFile(paramURL);
      if (localFile == null)
        throw new FileNotFoundException("Muffin not found for " + paramURL);
      FileInputStream localFileInputStream = new FileInputStream(localFile);
      localBufferedReader = new BufferedReader(new InputStreamReader(localFileInputStream));
      String str = localBufferedReader.readLine();
      try
      {
        l1 = Integer.parseInt(str);
      }
      catch (NumberFormatException localNumberFormatException1)
      {
        throw new IOException(localNumberFormatException1.getMessage());
      }
      str = localBufferedReader.readLine();
      try
      {
        l2 = Long.parseLong(str);
      }
      catch (NumberFormatException localNumberFormatException2)
      {
        throw new IOException(localNumberFormatException2.getMessage());
      }
    }
    finally
    {
      if (localBufferedReader != null)
        localBufferedReader.close();
    }
    return new long[] { l1, l2 };
  }

  public static void removeMuffinEntry(URL paramURL)
    throws IOException
  {
    File localFile1 = getMuffinFile(paramURL);
    if (localFile1 != null)
    {
      if (!localFile1.delete())
        throw new IOException("delete failed for muffin: " + paramURL);
      File localFile2 = new File(localFile1.getPath() + ".muf");
      if (!localFile2.delete())
        throw new IOException("delete failed for muffin: " + paramURL);
    }
    else
    {
      throw new FileNotFoundException("Muffin for " + paramURL + " does not exists");
    }
  }

  private static String getCachedHostIP(String paramString)
  {
    if (paramString == null)
      return null;
    URL localURL = getHostURL(paramString);
    String str = null;
    File localFile = getHostFile(localURL);
    if (localFile != null)
      str = (String)AccessController.doPrivileged(new PrivilegedAction(localFile)
      {
        private final File val$cachedHostFile;

        public Object run()
        {
          String str = null;
          try
          {
            BufferedReader localBufferedReader = new BufferedReader(new FileReader(this.val$cachedHostFile));
            str = localBufferedReader.readLine();
            localBufferedReader.close();
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
          return str;
        }
      });
    return str;
  }

  private static File getHostFile(URL paramURL)
  {
    File[] arrayOfFile = getMatchingHostFiles(hostDir, paramURL);
    if ((arrayOfFile == null) || (arrayOfFile.length == 0))
      return null;
    return arrayOfFile[0];
  }

  private static File[] getMatchingHostFiles(File paramFile, URL paramURL)
  {
    String str = getKey(paramURL);
    File[] arrayOfFile = (File[])(File[])AccessController.doPrivileged(new PrivilegedAction(paramFile, str)
    {
      private final File val$directory;
      private final String val$key;

      public Object run()
      {
        File[] arrayOfFile = this.val$directory.listFiles(new FileFilter()
        {
          public boolean accept(File paramFile)
          {
            String str = paramFile.getName();
            return (str.startsWith(Cache.15.this.val$key)) && (str.endsWith(".hst"));
          }
        });
        return arrayOfFile;
      }
    });
    return arrayOfFile;
  }

  static InetAddress getHostIP(String paramString)
  {
    InetAddress localInetAddress = null;
    try
    {
      localInetAddress = InetAddress.getByName(paramString);
    }
    catch (UnknownHostException localUnknownHostException1)
    {
      try
      {
        localInetAddress = InetAddress.getByName("0.0.0.0");
      }
      catch (UnknownHostException localUnknownHostException2)
      {
      }
    }
    return localInetAddress;
  }

  private static void createHostEntry(String paramString)
  {
    URL localURL = getHostURL(paramString);
    String str1 = getKey(localURL);
    String str2 = str1 + Integer.toString(getRandom(), 16);
    File localFile = new File(hostDir, str2 + ".hst");
    InetAddress localInetAddress = getHostIP(paramString);
    if (localInetAddress != null)
    {
      String str3 = localInetAddress.getHostAddress();
      AccessController.doPrivileged(new PrivilegedAction(localFile, str3)
      {
        private final File val$hostFile;
        private final String val$hostAddr;

        public Object run()
        {
          try
          {
            BufferedWriter localBufferedWriter = new BufferedWriter(new FileWriter(this.val$hostFile));
            localBufferedWriter.write(this.val$hostAddr);
            localBufferedWriter.close();
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
          return null;
        }
      });
    }
  }

  public static void createMuffinEntry(URL paramURL, int paramInt, long paramLong)
    throws IOException
  {
    String str1 = getKey(paramURL);
    File[] arrayOfFile = getMatchingMuffinAttributeFiles(muffinDir, paramURL);
    if (arrayOfFile.length != 0)
      throw new IOException("insert failed in cache: target already exixts");
    String str2 = str1 + Integer.toString(getRandom(), 16);
    File localFile2 = new File(muffinDir, str2 + ".muf");
    File localFile1 = new File(muffinDir, str2);
    putMuffinAttributes(localFile2, paramURL, paramInt, paramLong);
    localFile1.createNewFile();
  }

  public static String[] getMuffinNames(URL paramURL)
  {
    Vector localVector = new Vector();
    File[] arrayOfFile = muffinDir.listFiles(new FileFilter()
    {
      public boolean accept(File paramFile)
      {
        String str = paramFile.getName();
        return str.endsWith(".muf");
      }
    });
    URL localURL1 = null;
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      try
      {
        localURL1 = getCachedMuffinURL(arrayOfFile[i]);
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
      if (localURL1 == null)
        continue;
      URL localURL2 = HttpUtils.removeQueryStringFromURL(localURL1);
      String str = localURL2.getFile().substring(1 + localURL2.getFile().lastIndexOf('/'));
      if (!localURL1.toString().equals(paramURL.toString() + str))
        continue;
      localVector.add(str);
    }
    return (String[])(String[])localVector.toArray(new String[0]);
  }

  public static URL[] getAccessibleMuffins(URL paramURL)
    throws IOException
  {
    ArrayList localArrayList = new ArrayList();
    File[] arrayOfFile = muffinDir.listFiles(new FileFilter()
    {
      public boolean accept(File paramFile)
      {
        String str = paramFile.getName();
        return str.endsWith(".muf");
      }
    });
    int i = 0;
    for (int j = 0; j < arrayOfFile.length; j++)
    {
      URL localURL = getCachedMuffinURL(arrayOfFile[j]);
      if (!localURL.getHost().equals(paramURL.getHost()))
        continue;
      localArrayList.add(localURL);
    }
    return (URL[])(URL[])localArrayList.toArray(new URL[0]);
  }

  static URL getCachedMuffinURL(File paramFile)
    throws IOException
  {
    BufferedReader localBufferedReader = null;
    long l1 = -1L;
    long l2 = -1L;
    String str = null;
    try
    {
      FileInputStream localFileInputStream = new FileInputStream(paramFile);
      localBufferedReader = new BufferedReader(new InputStreamReader(localFileInputStream));
      str = localBufferedReader.readLine();
      str = localBufferedReader.readLine();
      str = localBufferedReader.readLine();
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    finally
    {
      if (localBufferedReader != null)
        localBufferedReader.close();
    }
    URL localURL = null;
    try
    {
      localURL = new URL(str);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Trace.ignoredException(localMalformedURLException);
    }
    return localURL;
  }

  public static long getMuffinSize(URL paramURL)
    throws IOException
  {
    long l = 0L;
    File localFile = getMuffinFile(paramURL);
    if ((localFile != null) && (localFile.exists()))
      l += localFile.length();
    return l;
  }

  private static void putMuffinAttributes(File paramFile, URL paramURL, int paramInt, long paramLong)
    throws IOException
  {
    PrintStream localPrintStream = new PrintStream(new FileOutputStream(paramFile));
    try
    {
      localPrintStream.println(paramInt);
      localPrintStream.println(paramLong);
      localPrintStream.println(paramURL.toString());
    }
    finally
    {
      if (localPrintStream != null)
        localPrintStream.close();
    }
  }

  public static void putMuffinAttributes(URL paramURL, int paramInt, long paramLong)
    throws IOException
  {
    File localFile = getMuffinAttributeFile(paramURL);
    PrintStream localPrintStream = new PrintStream(new FileOutputStream(localFile));
    try
    {
      localPrintStream.println(paramInt);
      localPrintStream.println(paramLong);
      localPrintStream.println(paramURL.toString());
    }
    finally
    {
      if (localPrintStream != null)
        localPrintStream.close();
    }
  }

  static String generateCacheFileName(URL paramURL, String paramString)
    throws IOException
  {
    String str = null;
    try
    {
      str = (String)AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL, paramString)
      {
        private final URL val$url;
        private final String val$version;

        public Object run()
          throws IOException
        {
          String str1 = Cache.getKey(this.val$url);
          String str2;
          do
          {
            str2 = Cache.access$400(str1) + File.separator + str1 + Integer.toString(Cache.getRandom(), 16) + Cache.getVersionTag(this.val$version);
            localFile1 = new File(Cache.cacheDir, str2);
            localFile2 = new File(Cache.cacheDir, str2 + ".idx");
          }
          while ((localFile2.exists()) || (localFile1.exists()));
          File localFile2 = null;
          File localFile1 = null;
          return str2;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      if ((localPrivilegedActionException.getException() instanceof IOException))
        throw ((IOException)localPrivilegedActionException.getException());
    }
    return str;
  }

  private static int getBucket(String paramString)
  {
    return Integer.valueOf(paramString.substring(0, paramString.length() - 1), 16).intValue() & 0x3F;
  }

  protected static String getKey(URL paramURL)
  {
    int i = hashCode(HttpUtils.removeQueryStringFromURL(paramURL));
    if (i < 0)
      i -= -2147483648;
    String str = Integer.toString(i, 16);
    return str + "-";
  }

  protected static int hashCode(URL paramURL)
  {
    int i = paramURL.toString().hashCode();
    i += (i << 9 ^ 0xFFFFFFFF);
    i ^= i >>> 14;
    i += (i << 4);
    i ^= i >>> 10;
    return i;
  }

  protected static String getFileExtension(String paramString)
  {
    String str = "";
    int i = paramString.lastIndexOf('.');
    if (i != -1)
      str = paramString.substring(i);
    return str;
  }

  protected static int getRandom()
  {
    return 268435456 + getSecureRandom().nextInt(1879048191);
  }

  public static void insertFile(File paramFile, int paramInt, URL paramURL, String paramString, long paramLong1, long paramLong2)
    throws IOException
  {
    CacheEntry localCacheEntry = createNewCacheEntry(paramURL, paramString);
    try
    {
      copyFile(paramFile, new File(localCacheEntry.getResourceFilename()));
    }
    catch (IOException localIOException)
    {
      removeCacheEntry(localCacheEntry);
      throw localIOException;
    }
    localCacheEntry.setBusy(CacheEntry.BUSY_FALSE);
    localCacheEntry.setIncomplete(CacheEntry.INCOMPLETE_FALSE);
    localCacheEntry.setURL(paramURL.toString());
    localCacheEntry.setContentLength((int)paramFile.length());
    localCacheEntry.setLastModified(paramLong1);
    localCacheEntry.setExpirationDate(paramLong2);
    if (paramString != null)
      localCacheEntry.setVersion(paramString);
    if (DownloadEngine.isJarContentType(paramInt))
    {
      MessageHeader localMessageHeader = new MessageHeader();
      localMessageHeader.add("deploy-request-content-type", "application/x-java-archive");
      localCacheEntry.setHeaders(localMessageHeader);
    }
    localCacheEntry.writeFileToDisk(paramInt, null);
    recordLastAccessed();
  }

  public static void insertMuffin(URL paramURL, File paramFile, int paramInt, long paramLong)
    throws IOException
  {
    File[] arrayOfFile = getMatchingMuffinAttributeFiles(muffinDir, paramURL);
    if (arrayOfFile.length != 0)
      throw new IOException("insert failed in cache: target already exixts");
    String str2 = getKey(paramURL);
    String str1 = str2 + Integer.toString(getRandom(), 16);
    File localFile = new File(muffinDir, str1 + ".muf");
    putMuffinAttributes(localFile, paramURL, paramInt, paramLong);
    copyFile(paramFile, new File(muffinDir, str1));
  }

  public static void copyFile(File paramFile1, File paramFile2)
    throws IOException
  {
    Object localObject1 = null;
    Object localObject2 = null;
    copyStream(new FileInputStream(paramFile1), new FileOutputStream(paramFile2));
  }

  public static void copyStream(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(paramInputStream);
    BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(paramOutputStream);
    byte[] arrayOfByte = new byte[10240];
    try
    {
      for (int i = localBufferedInputStream.read(arrayOfByte); i >= 0; i = localBufferedInputStream.read(arrayOfByte))
        localBufferedOutputStream.write(arrayOfByte, 0, i);
    }
    finally
    {
      try
      {
        if (localBufferedOutputStream != null)
          localBufferedOutputStream.close();
      }
      catch (Exception localException3)
      {
      }
      try
      {
        if (localBufferedInputStream != null)
          localBufferedInputStream.close();
      }
      catch (Exception localException4)
      {
      }
    }
  }

  public static void removeAllMuffins()
  {
    File[] arrayOfFile = muffinDir.listFiles();
    for (int i = 0; i < arrayOfFile.length; i++)
      arrayOfFile[i].delete();
  }

  public static byte[] getSignedJNLPBits(URL paramURL, String paramString, boolean paramBoolean)
    throws IOException
  {
    String str = paramBoolean ? "JNLP-INF/APPLICATION_TEMPLATE.JNLP" : "JNLP-INF/APPLICATION.JNLP";
    JarFile localJarFile = null;
    try
    {
      localJarFile = new JarFile(DownloadEngine.getCachedResourceFilePath(paramURL, paramString), false);
      Object localObject1 = localJarFile.getJarEntry(str);
      if (localObject1 == null)
      {
        localObject2 = localJarFile.entries();
        while ((((Enumeration)localObject2).hasMoreElements()) && (localObject1 == null))
        {
          localObject3 = (JarEntry)((Enumeration)localObject2).nextElement();
          if (((JarEntry)localObject3).getName().equalsIgnoreCase(str))
            localObject1 = localObject3;
        }
      }
      if (localObject1 == null)
      {
        localObject2 = null;
        return localObject2;
      }
      Object localObject2 = new byte[(int)((JarEntry)localObject1).getSize()];
      Object localObject3 = new DataInputStream(localJarFile.getInputStream((ZipEntry)localObject1));
      ((DataInputStream)localObject3).readFully(localObject2, 0, (int)((JarEntry)localObject1).getSize());
      ((DataInputStream)localObject3).close();
      Object localObject4 = localObject2;
      return localObject4;
    }
    finally
    {
      if (localJarFile != null)
        localJarFile.close();
    }
    throw localObject5;
  }

  static
  {
    reset();
    if (isCacheEnabled())
    {
      ct = new CleanupThread("CacheCleanUpThread", syncObject);
      ct.start();
    }
    else
    {
      ct = null;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.Cache
 * JD-Core Version:    0.6.0
 */