package com.sun.jnlp;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.jnl.LaunchDesc;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;

public final class PersistenceServiceImpl
  implements PersistenceService
{
  static final int MUFFIN_TAG_INDEX = 0;
  static final int MUFFIN_MAXSIZE_INDEX = 1;
  private long _globalLimit = -1L;
  private long _appLimit = -1L;
  private long _size = -1L;
  private static PersistenceServiceImpl _sharedInstance = null;
  private final ApiDialog _apiDialog = new ApiDialog();

  public static synchronized PersistenceServiceImpl getInstance()
  {
    initialize();
    return _sharedInstance;
  }

  public static synchronized void initialize()
  {
    if (_sharedInstance == null)
      _sharedInstance = new PersistenceServiceImpl();
    if (_sharedInstance != null)
      _sharedInstance._appLimit = (Config.getIntProperty("deployment.javaws.muffin.max") * 1024L);
  }

  long getLength(URL paramURL)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    return Cache.getMuffinSize(paramURL);
  }

  long getMaxLength(URL paramURL)
    throws MalformedURLException, IOException
  {
    Long localLong = null;
    try
    {
      localLong = (Long)AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL)
      {
        private final URL val$url;

        public Object run()
          throws IOException
        {
          long[] arrayOfLong = Cache.getMuffinAttributes(this.val$url);
          if (arrayOfLong == null)
            return new Long(-1L);
          return new Long(arrayOfLong[1]);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
    return localLong.longValue();
  }

  long setMaxLength(URL paramURL, long paramLong)
    throws MalformedURLException, IOException
  {
    long l1 = 0L;
    checkAccess(paramURL);
    if ((l1 = checkSetMaxSize(paramURL, paramLong)) < 0L)
      return -1L;
    long l2 = l1;
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL, l2)
      {
        private final URL val$url;
        private final long val$f_newmaxsize;

        public Object run()
          throws MalformedURLException, IOException
        {
          Cache.putMuffinAttributes(this.val$url, PersistenceServiceImpl.this.getTag(this.val$url), this.val$f_newmaxsize);
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException))
        throw ((IOException)localException);
      if ((localException instanceof MalformedURLException))
        throw ((MalformedURLException)localException);
    }
    return l1;
  }

  private long checkSetMaxSize(URL paramURL, long paramLong)
    throws IOException
  {
    URL[] arrayOfURL = null;
    try
    {
      arrayOfURL = (URL[])(URL[])AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL)
      {
        private final URL val$url;

        public Object run()
          throws IOException
        {
          return Cache.getAccessibleMuffins(this.val$url);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
    long l1 = 0L;
    if (arrayOfURL != null)
      for (int i = 0; i < arrayOfURL.length; i++)
      {
        if (arrayOfURL[i] == null)
          continue;
        URL localURL = arrayOfURL[i];
        if (localURL.equals(paramURL))
          continue;
        long l3 = 0L;
        try
        {
          l3 = getMaxLength(localURL);
        }
        catch (IOException localIOException)
        {
        }
        l1 += l3;
      }
    long l2 = this._appLimit - l1;
    if (paramLong > l2)
      return reconcileMaxSize(paramLong, l1, this._appLimit);
    return paramLong;
  }

  private long reconcileMaxSize(long paramLong1, long paramLong2, long paramLong3)
  {
    long l = paramLong1 + paramLong2;
    boolean bool = CheckServicePermission.hasFileAccessPermissions();
    if ((bool) || (askUser(l, paramLong3)))
    {
      this._appLimit = l;
      return paramLong1;
    }
    return paramLong3 - paramLong2;
  }

  private URL[] getAccessibleMuffins(URL paramURL)
    throws IOException
  {
    return Cache.getAccessibleMuffins(paramURL);
  }

  public long create(URL paramURL, long paramLong)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    Long localLong = null;
    long l1 = -1L;
    if ((l1 = checkSetMaxSize(paramURL, paramLong)) < 0L)
      return -1L;
    long l2 = l1;
    try
    {
      localLong = (Long)AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL, l2)
      {
        private final URL val$url;
        private final long val$pass_newmaxsize;

        public Object run()
          throws MalformedURLException, IOException
        {
          Cache.createMuffinEntry(this.val$url, 0, this.val$pass_newmaxsize);
          return new Long(this.val$pass_newmaxsize);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException))
        throw ((IOException)localException);
      if ((localException instanceof MalformedURLException))
        throw ((MalformedURLException)localException);
    }
    return localLong.longValue();
  }

  public FileContents get(URL paramURL)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    File localFile = null;
    try
    {
      localFile = (File)AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL)
      {
        private final URL val$url;

        public Object run()
          throws MalformedURLException, IOException
        {
          return Cache.getMuffinFile(this.val$url);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException))
        throw ((IOException)localException);
      if ((localException instanceof MalformedURLException))
        throw ((MalformedURLException)localException);
    }
    if (localFile == null)
      throw new FileNotFoundException(paramURL.toString());
    return new FileContentsImpl(localFile, this, paramURL, getMaxLength(paramURL));
  }

  public void delete(URL paramURL)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL)
      {
        private final URL val$url;

        public Object run()
          throws MalformedURLException, IOException
        {
          Cache.removeMuffinEntry(this.val$url);
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException))
        throw ((IOException)localException);
      if ((localException instanceof MalformedURLException))
        throw ((MalformedURLException)localException);
    }
  }

  public String[] getNames(URL paramURL)
    throws MalformedURLException, IOException
  {
    String[] arrayOfString = null;
    URL localURL = URLUtil.asPathURL(paramURL);
    checkAccess(localURL);
    try
    {
      arrayOfString = (String[])(String[])AccessController.doPrivileged(new PrivilegedExceptionAction(localURL)
      {
        private final URL val$pathUrl;

        public Object run()
          throws MalformedURLException, IOException
        {
          return Cache.getMuffinNames(this.val$pathUrl);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException))
        throw ((IOException)localException);
      if ((localException instanceof MalformedURLException))
        throw ((MalformedURLException)localException);
    }
    return arrayOfString;
  }

  public int getTag(URL paramURL)
    throws MalformedURLException, IOException
  {
    Integer localInteger = null;
    checkAccess(paramURL);
    try
    {
      localInteger = (Integer)AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL)
      {
        private final URL val$url;

        public Object run()
          throws MalformedURLException, IOException
        {
          long[] arrayOfLong = Cache.getMuffinAttributes(this.val$url);
          if (arrayOfLong == null)
            throw new MalformedURLException();
          return new Integer((int)arrayOfLong[0]);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException))
        throw ((IOException)localException);
      if ((localException instanceof MalformedURLException))
        throw ((MalformedURLException)localException);
    }
    return localInteger.intValue();
  }

  public void setTag(URL paramURL, int paramInt)
    throws MalformedURLException, IOException
  {
    checkAccess(paramURL);
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL, paramInt)
      {
        private final URL val$url;
        private final int val$tag;

        public Object run()
          throws MalformedURLException, IOException
        {
          Cache.putMuffinAttributes(this.val$url, this.val$tag, PersistenceServiceImpl.this.getMaxLength(this.val$url));
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException))
        throw ((IOException)localException);
      if ((localException instanceof MalformedURLException))
        throw ((MalformedURLException)localException);
    }
  }

  private boolean askUser(long paramLong1, long paramLong2)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(paramLong1, paramLong2)
    {
      private final long val$requested;
      private final long val$currentLimit;

      public Object run()
      {
        String str1 = ResourceManager.getString("api.persistence.title");
        String str2 = ResourceManager.getString("api.persistence.message");
        String str3 = ResourceManager.getString("api.persistence.detail", new Long(this.val$requested), new Long(this.val$currentLimit));
        boolean bool = PersistenceServiceImpl.this._apiDialog.askUser(str1, str2, null, null, str3, false);
        if (bool)
        {
          long l = Math.min(2147483647L, (this.val$requested + 1023L) / 1024L);
          Config.setIntProperty("deployment.javaws.muffin.max", (int)l);
          Config.get().storeIfNeeded();
        }
        return new Boolean(bool);
      }
    });
    return localBoolean.booleanValue();
  }

  protected void checkAccess(URL paramURL)
    throws MalformedURLException
  {
    LaunchDesc localLaunchDesc = JNLPClassLoaderUtil.getInstance().getLaunchDesc();
    if ((localLaunchDesc != null) && (localLaunchDesc.getSecurityModel() == 0))
    {
      URL localURL = localLaunchDesc.getCodebase();
      if (localURL != null)
      {
        if ((paramURL == null) || (!localURL.getHost().equals(paramURL.getHost())))
          throwAccessDenied(paramURL);
        String str = paramURL.getFile();
        if (str == null)
          throwAccessDenied(paramURL);
        int i = str.lastIndexOf('/');
        if (i == -1)
          return;
        if (!localURL.getFile().startsWith(str.substring(0, i + 1)))
          throwAccessDenied(paramURL);
      }
    }
  }

  private void throwAccessDenied(URL paramURL)
    throws MalformedURLException
  {
    throw new MalformedURLException(ResourceManager.getString("api.persistence.accessdenied", paramURL.toString()));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.PersistenceServiceImpl
 * JD-Core Version:    0.6.0
 */