package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import sun.net.www.protocol.http.AuthCacheBridge;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

public class CredentialManager
{
  public static final long LOGIN_SESSION_INVALID = -1L;
  protected static CredentialManager instance = null;
  private CredentialCache credCache = new CredentialCache();
  private CredentialPersistor persistor = new CredentialPersistor();
  private Map serverMap = this.persistor.getAllPersistedCredentials();

  protected CredentialManager()
  {
    if (this.persistor.getSavedCredentialCount() > this.serverMap.size())
      this.persistor.persistAllCredentials(this.serverMap);
    AuthCacheValue.setAuthCache(this.credCache);
  }

  public static synchronized CredentialManager getInstance()
  {
    if (instance == null)
      instance = new CredentialManager();
    return instance;
  }

  protected long getLoginSessionId()
  {
    return -1L;
  }

  protected boolean isPasswordEncryptionSupported()
  {
    return false;
  }

  protected byte[] encryptPassword(char[] paramArrayOfChar)
  {
    return new byte[0];
  }

  protected char[] decryptPassword(byte[] paramArrayOfByte)
  {
    return new char[0];
  }

  public void saveCredential(AuthKey paramAuthKey, CredentialInfo paramCredentialInfo)
  {
    paramCredentialInfo.setSessionId(getLoginSessionId());
    CredentialInfo localCredentialInfo = (CredentialInfo)paramCredentialInfo.clone();
    if ((isPasswordEncryptionSupported()) && (localCredentialInfo.isPasswordSaveApproved()))
      localCredentialInfo.setEncryptedPassword(encryptPassword(paramCredentialInfo.getPassword()));
    else
      localCredentialInfo.setPassword(null);
    String str = buildConnectionKey(paramAuthKey);
    this.serverMap.put(str, localCredentialInfo);
    this.persistor.persistCredential(str);
  }

  public boolean isCredentialValid(CredentialInfo paramCredentialInfo)
  {
    int i = 0;
    if ((paramCredentialInfo.getUserName().length() > 0) && (paramCredentialInfo.getPassword().length > 0) && (paramCredentialInfo.getSessionId() != -1L) && (paramCredentialInfo.getSessionId() == getLoginSessionId()))
      i = 1;
    return i;
  }

  public static void removePersistantCredentials()
  {
    try
    {
      File localFile = new File(Config.getUserAuthFile());
      if (!localFile.delete())
        localFile.deleteOnExit();
    }
    catch (Exception localException)
    {
      Trace.securityPrintException(localException);
    }
  }

  public void clearCredentialPassword(AuthKey paramAuthKey)
  {
    String str = buildConnectionKey(paramAuthKey);
    CredentialInfo localCredentialInfo = findServerCredential(str);
    if ((!this.serverMap.containsKey(str)) && (localCredentialInfo != null))
    {
      localCredentialInfo.setPassword(null);
      saveCredential(paramAuthKey, localCredentialInfo);
    }
    this.persistor.persistCredential(str);
  }

  protected CredentialInfo getCredential(AuthKey paramAuthKey)
  {
    String str = buildConnectionKey(paramAuthKey);
    CredentialInfo localCredentialInfo = (CredentialInfo)this.serverMap.get(str);
    if ((localCredentialInfo == null) || ((localCredentialInfo != null) && (localCredentialInfo.isCredentialEmpty())))
    {
      localCredentialInfo = findServerCredential(str);
      if (localCredentialInfo != null)
        localCredentialInfo.setSessionId(-1L);
      else
        localCredentialInfo = new CredentialInfo();
    }
    if (localCredentialInfo.getPassword().length == 0)
    {
      byte[] arrayOfByte = localCredentialInfo.getEncryptedPassword();
      if (arrayOfByte.length > 0)
        localCredentialInfo.setPassword(decryptPassword(arrayOfByte));
    }
    return localCredentialInfo;
  }

  private CredentialInfo findServerCredential(String paramString)
  {
    CredentialInfo localCredentialInfo1 = null;
    Set localSet = this.serverMap.keySet();
    Iterator localIterator = localSet.iterator();
    while ((localIterator.hasNext()) && ((localCredentialInfo1 == null) || (localCredentialInfo1.getEncryptedPassword().length == 0)))
    {
      String str = (String)localIterator.next();
      if (getServerFromKey(paramString).equals(getServerFromKey(str)))
      {
        CredentialInfo localCredentialInfo2 = (CredentialInfo)this.serverMap.get(str);
        if (!localCredentialInfo2.isCredentialEmpty())
          localCredentialInfo1 = (CredentialInfo)this.serverMap.get(str);
      }
    }
    return localCredentialInfo1;
  }

  private static String getServerFromKey(String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ":");
    return localStringTokenizer.nextToken();
  }

  public static String buildConnectionKey(AuthKey paramAuthKey)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    if (paramAuthKey.isProxy())
      localStringBuffer.append("p:");
    else
      localStringBuffer.append("s:");
    localStringBuffer.append(paramAuthKey.getProtocolScheme());
    localStringBuffer.append(':');
    localStringBuffer.append(paramAuthKey.getHost());
    localStringBuffer.append(':');
    localStringBuffer.append(paramAuthKey.getPort());
    localStringBuffer.append(':');
    localStringBuffer.append(paramAuthKey.getPath());
    return localStringBuffer.toString().toLowerCase();
  }

  private class CredentialCache extends AuthCacheImpl
  {
    HashMap map = new HashMap();

    public CredentialCache()
    {
      setMap(this.map);
    }

    public void remove(String paramString, AuthCacheValue paramAuthCacheValue)
    {
      try
      {
        super.remove(paramString, paramAuthCacheValue);
        AuthCacheBridge localAuthCacheBridge = new AuthCacheBridge(paramAuthCacheValue);
        CredentialManager.getInstance().clearCredentialPassword(localAuthCacheBridge);
      }
      catch (Exception localException)
      {
        Trace.securityPrintException(localException);
      }
    }
  }

  private class CredentialPersistor
  {
    private int credentialCount = 0;

    public CredentialPersistor()
    {
    }

    private int getSavedCredentialCount()
    {
      return this.credentialCount;
    }

    private synchronized void persistCredential(String paramString)
    {
      ObjectOutputStream localObjectOutputStream = null;
      try
      {
        CredentialInfo localCredentialInfo = (CredentialInfo)CredentialManager.this.serverMap.get(paramString);
        if (localCredentialInfo != null)
        {
          OutputStream localOutputStream = openOutputFile(true);
          localObjectOutputStream = new ObjectOutputStream(localOutputStream);
          localObjectOutputStream.writeObject(paramString);
          localCredentialInfo.writeExternal(localObjectOutputStream);
          localObjectOutputStream.flush();
          localObjectOutputStream.close();
          localOutputStream.flush();
          localOutputStream.close();
        }
      }
      catch (Exception localException)
      {
        Trace.securityPrintException(localException);
      }
    }

    private synchronized void deleteCredentials()
    {
      try
      {
        File localFile = new File(Config.getUserAuthFile());
        if (!localFile.delete())
          localFile.deleteOnExit();
      }
      catch (Exception localException)
      {
        Trace.securityPrintException(localException);
      }
    }

    private synchronized void persistAllCredentials(Map paramMap)
    {
      ObjectOutputStream localObjectOutputStream = null;
      OutputStream localOutputStream = null;
      try
      {
        localOutputStream = openOutputFile(false);
        Set localSet = paramMap.keySet();
        Iterator localIterator = localSet.iterator();
        while (localIterator.hasNext())
        {
          localObjectOutputStream = new ObjectOutputStream(localOutputStream);
          String str = (String)localIterator.next();
          CredentialInfo localCredentialInfo = (CredentialInfo)paramMap.get(str);
          localObjectOutputStream.writeObject(str);
          localCredentialInfo.writeExternal(localObjectOutputStream);
          localObjectOutputStream.flush();
        }
      }
      catch (Throwable localThrowable)
      {
        Trace.securityPrintException(localThrowable);
      }
      finally
      {
        try
        {
          if (localObjectOutputStream != null)
            localObjectOutputStream.flush();
          localOutputStream.flush();
          localOutputStream.close();
        }
        catch (Exception localException)
        {
          Trace.securityPrintException(localException);
        }
      }
    }

    private synchronized void getPersistedCredential(ObjectInputStream paramObjectInputStream, String paramString)
    {
      try
      {
        CredentialInfo localCredentialInfo = new CredentialInfo();
        localCredentialInfo.readExternal(paramObjectInputStream);
        CredentialManager.this.serverMap.put(paramString, localCredentialInfo);
      }
      catch (Exception localException)
      {
        Trace.securityPrintException(localException);
      }
    }

    private synchronized InputStream openInputStream()
    {
      InputStream localInputStream = null;
      try
      {
        File localFile = new File(Config.getUserAuthFile());
        localInputStream = (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(localFile)
        {
          private final File val$f;

          public Object run()
            throws IOException
          {
            if (!this.val$f.exists())
            {
              this.val$f.getParentFile().mkdirs();
              this.val$f.createNewFile();
            }
            return new BufferedInputStream(new FileInputStream(this.val$f));
          }
        });
      }
      catch (Exception localException)
      {
        Trace.securityPrintException(localException);
      }
      return localInputStream;
    }

    private synchronized OutputStream openOutputFile(boolean paramBoolean)
    {
      OutputStream localOutputStream = null;
      try
      {
        File localFile = new File(Config.getUserAuthFile());
        localOutputStream = (OutputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(localFile, paramBoolean)
        {
          private final File val$f;
          private final boolean val$append;

          public Object run()
            throws IOException
          {
            if (!this.val$f.exists())
            {
              this.val$f.getParentFile().mkdirs();
              this.val$f.createNewFile();
            }
            return new BufferedOutputStream(new FileOutputStream(this.val$f, this.val$append));
          }
        });
      }
      catch (Exception localException)
      {
        Trace.securityPrintException(localException);
      }
      return localOutputStream;
    }

    private synchronized Map getAllPersistedCredentials()
    {
      ObjectInputStream localObjectInputStream = null;
      InputStream localInputStream = null;
      HashMap localHashMap = null;
      try
      {
        localHashMap = new HashMap();
        localInputStream = openInputStream();
        for (localObjectInputStream = new ObjectInputStream(localInputStream); localObjectInputStream != null; localObjectInputStream = new ObjectInputStream(localInputStream))
        {
          String str = (String)localObjectInputStream.readObject();
          CredentialInfo localCredentialInfo = new CredentialInfo();
          localCredentialInfo.readExternal(localObjectInputStream);
          localHashMap.put(str, localCredentialInfo);
          this.credentialCount += 1;
        }
        localInputStream.close();
      }
      catch (EOFException localEOFException)
      {
      }
      catch (Exception localException1)
      {
        Trace.securityPrintException(localException1);
        try
        {
          localInputStream.close();
          if (this.credentialCount > 0)
            persistAllCredentials(localHashMap);
        }
        catch (Exception localException2)
        {
          Trace.securityPrintException(localException1);
        }
      }
      return localHashMap;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CredentialManager
 * JD-Core Version:    0.6.0
 */