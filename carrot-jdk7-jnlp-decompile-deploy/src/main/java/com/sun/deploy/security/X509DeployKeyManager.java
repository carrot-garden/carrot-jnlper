package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.deploy.util.DeploySysAction;
import com.sun.deploy.util.DeploySysRun;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.KeyStore.Builder;
import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.callback.CallbackHandler;

public final class X509DeployKeyManager
  implements X509KeyManager
{
  private X509KeyManager myKeyManager = null;
  private X509KeyManager browserKeyManager = null;
  private String userKeyStore = null;
  private String systemKeyStore = null;
  private KeyStore browserKeyStore = null;
  private boolean isWindows = Config.getOSName().indexOf("Windows") != -1;
  private static HashMap clientAuthCertsCachedMap = new HashMap();
  private static ThreadLocal clientCertDialogCancelled = new ThreadLocal()
  {
    protected synchronized Object initialValue()
    {
      return Boolean.FALSE;
    }
  };
  private static ThreadLocal passwdDialogCancelled = new ThreadLocal()
  {
    protected synchronized Object initialValue()
    {
      return Boolean.FALSE;
    }
  };

  public X509DeployKeyManager()
  {
    if (Config.getBooleanProperty("deployment.security.browser.keystore.use"))
    {
      Service localService = ServiceManager.getService();
      this.browserKeyStore = localService.getBrowserClientAuthKeyStore();
    }
  }

  private void init()
    throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, FileNotFoundException, IOException, UnrecoverableKeyException, CertificateException
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run()
          throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, FileNotFoundException, IOException, UnrecoverableKeyException, CertificateException
        {
          X509DeployKeyManager.this.do_init();
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof KeyStoreException))
        throw ((KeyStoreException)localException);
      if ((localException instanceof NoSuchAlgorithmException))
        throw ((NoSuchAlgorithmException)localException);
      if ((localException instanceof NoSuchProviderException))
        throw ((NoSuchProviderException)localException);
      if ((localException instanceof FileNotFoundException))
        throw ((FileNotFoundException)localException);
      if ((localException instanceof IOException))
        throw ((IOException)localException);
      if ((localException instanceof UnrecoverableKeyException))
        throw ((UnrecoverableKeyException)localException);
      if ((localException instanceof CertificateException))
        throw ((CertificateException)localException);
      Trace.securityPrintException(localPrivilegedActionException);
    }
  }

  private void do_init()
    throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, FileNotFoundException, IOException, UnrecoverableKeyException, CertificateException
  {
    this.browserKeyManager = getBrowserKeyManager(this.browserKeyStore);
    if (Config.isJavaVersionAtLeast16())
      this.myKeyManager = getNewMyKeyManager(this.userKeyStore, this.systemKeyStore);
    else
      this.myKeyManager = getLegacyMyKeyManager(this.userKeyStore);
  }

  public synchronized String chooseClientAlias(String[] paramArrayOfString, Principal[] paramArrayOfPrincipal, Socket paramSocket)
  {
    TreeMap localTreeMap1 = new TreeMap();
    TreeMap localTreeMap2 = new TreeMap();
    String str1 = null;
    String str2 = null;
    if (clientCertDialogCancelled.get().equals(Boolean.FALSE))
    {
      str2 = getHostnameForSocket(paramSocket);
      Object localObject2;
      String str3;
      Object localObject3;
      for (int i = 0; i < paramArrayOfString.length; i++)
      {
        localObject2 = getClientAliases(paramArrayOfString[i], paramArrayOfPrincipal);
        if ((localObject2 == null) || (localObject2.length <= 0))
          continue;
        for (int j = 0; j < localObject2.length; j++)
        {
          int k = CertType.PLUGIN.getType().length();
          str3 = localObject2[j].substring(k);
          localObject3 = getCertificateChain(str3);
          try
          {
            if (CertUtils.checkTLSClient(localObject3[0]))
            {
              localTreeMap1.put(str3, localObject3);
              if (localObject2[j].startsWith(CertType.PLUGIN.getType()))
                localTreeMap2.put(str3, CertType.PLUGIN);
              if (localObject2[j].startsWith(CertType.BROWSER.getType()))
                localTreeMap2.put(str3, CertType.BROWSER);
            }
          }
          catch (CertificateException localCertificateException)
          {
            Trace.msgSecurityPrintln("clientauth.checkTLSClient.failed", new Object[] { str3 });
          }
        }
      }
      Object localObject1;
      if (passwdDialogCancelled.get().equals(Boolean.FALSE))
      {
        localObject1 = localTreeMap1;
        localObject2 = localTreeMap2;
        if (str2 != null)
          synchronized (clientAuthCertsCachedMap)
          {
            if (clientAuthCertsCachedMap.size() > 0)
            {
              Iterator localIterator = clientAuthCertsCachedMap.keySet().iterator();
              str3 = null;
              localObject3 = null;
              while (localIterator.hasNext())
              {
                str3 = (String)localIterator.next();
                if (str3.compareToIgnoreCase(str2) != 0)
                  continue;
                localObject3 = (String)clientAuthCertsCachedMap.get(str3);
                Trace.msgSecurityPrintln("clientauth.readFromCache.success", new Object[] { localObject3 });
                return localObject3;
              }
            }
          }
        if ((Config.getBooleanProperty("deployment.security.clientauth.keystore.auto")) && (((TreeMap)localObject1).size() <= 1))
        {
          if (((TreeMap)localObject1).size() == 0)
          {
            str1 = null;
          }
          else
          {
            ??? = ((TreeMap)localObject1).keySet().toArray();
            str1 = (String)???[0];
          }
        }
        else
        {
          ??? = new DeploySysAction((TreeMap)localObject1, (TreeMap)localObject2)
          {
            private final TreeMap val$theClientAuthCertsMap;
            private final TreeMap val$theClientAuthTypeMap;

            public Object execute()
            {
              return ClientCertDialog.showDialog(this.val$theClientAuthCertsMap, this.val$theClientAuthTypeMap);
            }
          };
          str1 = (String)DeploySysRun.executePrivileged((DeploySysAction)???, null);
        }
      }
      if (str1 == null)
        clientCertDialogCancelled.set(Boolean.TRUE);
      if ((paramSocket instanceof SSLSocket))
      {
        localObject1 = new MyListener(str2, str1);
        ((SSLSocket)paramSocket).addHandshakeCompletedListener((HandshakeCompletedListener)localObject1);
      }
      return str1;
    }
    return (String)(String)(String)(String)null;
  }

  public String chooseEngineClientAlias(String[] paramArrayOfString, Principal[] paramArrayOfPrincipal, SSLEngine paramSSLEngine)
  {
    return chooseClientAlias(paramArrayOfString, paramArrayOfPrincipal, null);
  }

  public synchronized String chooseServerAlias(String paramString, Principal[] paramArrayOfPrincipal, Socket paramSocket)
  {
    try
    {
      if ((this.myKeyManager == null) && (this.browserKeyManager == null) && (passwdDialogCancelled.get().equals(Boolean.FALSE)))
        init();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    String str = null;
    if (this.myKeyManager != null)
      str = this.myKeyManager.chooseServerAlias(paramString, paramArrayOfPrincipal, paramSocket);
    if ((str == null) && (this.browserKeyManager != null))
      str = this.browserKeyManager.chooseServerAlias(paramString, paramArrayOfPrincipal, paramSocket);
    return str;
  }

  public String chooseEngineServerAlias(String paramString, Principal[] paramArrayOfPrincipal, SSLEngine paramSSLEngine)
  {
    return chooseServerAlias(paramString, paramArrayOfPrincipal, null);
  }

  public synchronized X509Certificate[] getCertificateChain(String paramString)
  {
    try
    {
      if ((this.myKeyManager == null) && (this.browserKeyManager == null) && (passwdDialogCancelled.get().equals(Boolean.FALSE)))
        init();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    X509Certificate[] arrayOfX509Certificate = null;
    if ((this.myKeyManager != null) && (!paramString.contains("Mozilla")) && (!paramString.contains("MSCrypto")))
      arrayOfX509Certificate = this.myKeyManager.getCertificateChain(paramString);
    if ((arrayOfX509Certificate == null) && (this.browserKeyManager != null))
      arrayOfX509Certificate = this.browserKeyManager.getCertificateChain(paramString);
    return arrayOfX509Certificate;
  }

  public synchronized String[] getClientAliases(String paramString, Principal[] paramArrayOfPrincipal)
  {
    try
    {
      if ((this.myKeyManager == null) && (this.browserKeyManager == null) && (passwdDialogCancelled.get().equals(Boolean.FALSE)))
        init();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    String[] arrayOfString1 = null;
    String[] arrayOfString2 = null;
    if (this.myKeyManager != null)
      arrayOfString1 = this.myKeyManager.getClientAliases(paramString, paramArrayOfPrincipal);
    if (this.browserKeyManager != null)
      arrayOfString2 = this.browserKeyManager.getClientAliases(paramString, paramArrayOfPrincipal);
    if (arrayOfString1 == null)
    {
      if (arrayOfString2 != null)
        for (i = 0; i < arrayOfString2.length; i++)
          arrayOfString2[i] = (CertType.BROWSER.getType() + arrayOfString2[i]);
      return arrayOfString2;
    }
    if (arrayOfString2 == null)
    {
      if (arrayOfString1 != null)
        for (i = 0; i < arrayOfString1.length; i++)
          arrayOfString1[i] = (CertType.PLUGIN.getType() + arrayOfString1[i]);
      return arrayOfString1;
    }
    for (int i = 0; i < arrayOfString1.length; i++)
      arrayOfString1[i] = (CertType.PLUGIN.getType() + arrayOfString1[i]);
    for (i = 0; i < arrayOfString2.length; i++)
      arrayOfString2[i] = (CertType.BROWSER.getType() + arrayOfString2[i]);
    String[] arrayOfString3 = new String[arrayOfString1.length + arrayOfString2.length];
    System.arraycopy(arrayOfString1, 0, arrayOfString3, 0, arrayOfString1.length);
    System.arraycopy(arrayOfString2, 0, arrayOfString3, arrayOfString1.length, arrayOfString2.length);
    return arrayOfString3;
  }

  public synchronized String[] getServerAliases(String paramString, Principal[] paramArrayOfPrincipal)
  {
    try
    {
      if ((this.myKeyManager == null) && (this.browserKeyManager == null) && (passwdDialogCancelled.get().equals(Boolean.FALSE)))
        init();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    String[] arrayOfString1 = null;
    String[] arrayOfString2 = null;
    if (this.myKeyManager != null)
      arrayOfString1 = this.myKeyManager.getServerAliases(paramString, paramArrayOfPrincipal);
    if (this.browserKeyManager != null)
      arrayOfString2 = this.browserKeyManager.getServerAliases(paramString, paramArrayOfPrincipal);
    if (arrayOfString1 == null)
      return arrayOfString2;
    if (arrayOfString2 == null)
      return arrayOfString1;
    String[] arrayOfString3 = new String[arrayOfString1.length + arrayOfString2.length];
    System.arraycopy(arrayOfString1, 0, arrayOfString3, 0, arrayOfString1.length);
    System.arraycopy(arrayOfString2, 0, arrayOfString3, arrayOfString1.length, arrayOfString2.length);
    return arrayOfString3;
  }

  public PrivateKey getPrivateKey(String paramString)
  {
    try
    {
      if ((this.myKeyManager == null) && (this.browserKeyManager == null) && (passwdDialogCancelled.get().equals(Boolean.FALSE)))
        init();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    PrivateKey localPrivateKey = null;
    if ((this.myKeyManager != null) && (!paramString.contains("Mozilla")) && (!paramString.contains("MSCrypto")))
      localPrivateKey = this.myKeyManager.getPrivateKey(paramString);
    if ((localPrivateKey == null) && (this.browserKeyManager != null))
      localPrivateKey = this.browserKeyManager.getPrivateKey(paramString);
    return localPrivateKey;
  }

  private X509KeyManager getBrowserKeyManager(KeyStore paramKeyStore)
    throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, FileNotFoundException, IOException, UnrecoverableKeyException, CertificateException
  {
    X509KeyManager localX509KeyManager = null;
    if (paramKeyStore != null)
    {
      paramKeyStore.load(null, new char[0]);
      KeyManagerFactory localKeyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
      localKeyManagerFactory.init(paramKeyStore, new char[0]);
      KeyManager[] arrayOfKeyManager = localKeyManagerFactory.getKeyManagers();
      for (int i = 0; i < arrayOfKeyManager.length; i++)
      {
        if (!(arrayOfKeyManager[i] instanceof X509KeyManager))
          continue;
        localX509KeyManager = (X509KeyManager)arrayOfKeyManager[i];
        break;
      }
    }
    return localX509KeyManager;
  }

  private X509KeyManager getNewMyKeyManager(String paramString1, String paramString2)
    throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, FileNotFoundException, IOException, UnrecoverableKeyException, CertificateException
  {
    X509KeyManager localX509KeyManager = null;
    File localFile1 = new File(paramString1);
    File localFile2 = new File(paramString2);
    if ((localFile1.exists()) || (localFile2.exists()))
      try
      {
        PasswordCallbackHandler localPasswordCallbackHandler = new PasswordCallbackHandler("clientauth.user.password.dialog.text");
        localObject1 = new PasswordCallbackHandler("clientauth.system.password.dialog.text");
        localObject2 = null;
        KeyStore.Builder localBuilder = null;
        if (localFile1.exists())
          localObject2 = KeyStore.Builder.newInstance("JKS", null, localFile1, new KeyStore.CallbackHandlerProtection(localPasswordCallbackHandler));
        if (localFile2.exists())
          localBuilder = KeyStore.Builder.newInstance("JKS", null, localFile2, new KeyStore.CallbackHandlerProtection((CallbackHandler)localObject1));
        KeyStoreBuilderParameters localKeyStoreBuilderParameters = new KeyStoreBuilderParameters(Arrays.asList(new KeyStore.Builder[] { localObject2, localBuilder }));
        KeyManagerFactory localKeyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
        localKeyManagerFactory.init(localKeyStoreBuilderParameters);
        KeyManager[] arrayOfKeyManager = localKeyManagerFactory.getKeyManagers();
        for (int i = 0; i < arrayOfKeyManager.length; i++)
        {
          if (!(arrayOfKeyManager[i] instanceof X509KeyManager))
            continue;
          localX509KeyManager = (X509KeyManager)arrayOfKeyManager[i];
          break;
        }
      }
      catch (Exception localException)
      {
        Object localObject1;
        Object localObject2;
        localException.printStackTrace();
        if (!Trace.isAutomationEnabled())
        {
          localObject1 = getMessage("clientauth.password.dialog.error.text");
          localObject2 = getMessage("clientauth.password.dialog.error.caption");
          ToolkitStore.getUI().showExceptionDialog(null, null, localException, (String)localObject2, (String)localObject1, null, null);
        }
      }
    return (X509KeyManager)(X509KeyManager)localX509KeyManager;
  }

  private X509KeyManager getLegacyMyKeyManager(String paramString)
    throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, FileNotFoundException, IOException, UnrecoverableKeyException, CertificateException
  {
    X509KeyManager localX509KeyManager = null;
    File localFile = new File(paramString);
    if (localFile.exists())
    {
      int i = 1;
      while (i != 0)
        try
        {
          char[] arrayOfChar = getPasswordDialog("clientauth.user.password.dialog.text");
          if (passwdDialogCancelled.get().equals(Boolean.TRUE))
            break;
          str = System.getProperty("javax.net.ssl.keyStoreType");
          if (str == null)
            str = "JKS";
          localObject = KeyStore.getInstance(str);
          ((KeyStore)localObject).load(new BufferedInputStream(new FileInputStream(paramString)), arrayOfChar);
          KeyManagerFactory localKeyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
          localKeyManagerFactory.init((KeyStore)localObject, arrayOfChar);
          KeyManager[] arrayOfKeyManager = localKeyManagerFactory.getKeyManagers();
          for (int j = 0; j < arrayOfKeyManager.length; j++)
          {
            if (!(arrayOfKeyManager[j] instanceof X509KeyManager))
              continue;
            localX509KeyManager = (X509KeyManager)arrayOfKeyManager[j];
            break;
          }
          i = 0;
        }
        catch (IOException localIOException)
        {
          String str;
          Object localObject;
          localIOException.printStackTrace();
          if (!Trace.isAutomationEnabled())
          {
            str = getMessage("clientauth.password.dialog.error.text");
            localObject = getMessage("clientauth.password.dialog.error.caption");
            ToolkitStore.getUI().showExceptionDialog(null, null, localIOException, (String)localObject, str, null, null);
          }
        }
    }
    return (X509KeyManager)localX509KeyManager;
  }

  private char[] getPasswordDialog(String paramString)
  {
    CredentialInfo localCredentialInfo = ToolkitStore.getUI().showPasswordDialog(null, getMessage("password.dialog.title"), getMessage(paramString), false, false, null, false, null);
    if (localCredentialInfo == null)
    {
      passwdDialogCancelled.set(Boolean.TRUE);
      return null;
    }
    return localCredentialInfo.getPassword();
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  private static int getAcceleratorKey(String paramString)
  {
    return ResourceManager.getAcceleratorKey(paramString);
  }

  String getHostnameForSocket(Socket paramSocket)
  {
    String str1 = null;
    try
    {
      String str2 = "com.sun.net.ssl.internal.ssl.SSLSocketImpl";
      if (Config.isJavaVersionAtLeast17())
        str2 = "sun.security.ssl.SSLSocketImpl";
      Class localClass = Class.forName(str2);
      boolean bool = localClass.isInstance(paramSocket);
      if (bool)
      {
        Object localObject = localClass.cast(paramSocket);
        Method localMethod = localClass.getDeclaredMethod("getHost", null);
        AccessController.doPrivileged(new PrivilegedExceptionAction(localMethod)
        {
          private final Method val$privateGetHostMethod;

          public Object run()
            throws IOException
          {
            this.val$privateGetHostMethod.setAccessible(true);
            return null;
          }
        });
        str1 = (String)localMethod.invoke(localObject, null);
      }
    }
    catch (Exception localException)
    {
      Trace.msgSecurityPrintln("clientauth.readFromCache.failed");
      if (localException != null)
        Trace.msgSecurityPrintln(localException.toString());
    }
    return str1;
  }

  static final class MyListener
    implements HandshakeCompletedListener
  {
    private String hostname;
    private String certName;

    public MyListener(String paramString1, String paramString2)
    {
      this.hostname = paramString1;
      this.certName = paramString2;
    }

    public void handshakeCompleted(HandshakeCompletedEvent paramHandshakeCompletedEvent)
    {
      if (this.hostname != null)
        synchronized (X509DeployKeyManager.clientAuthCertsCachedMap)
        {
          X509DeployKeyManager.clientAuthCertsCachedMap.put(this.hostname, this.certName);
        }
      try
      {
        ??? = paramHandshakeCompletedEvent.getSocket();
        ((SSLSocket)???).removeHandshakeCompletedListener(this);
      }
      catch (IllegalArgumentException localIllegalArgumentException)
      {
      }
    }

    public boolean equals(Object paramObject)
    {
      if (!(paramObject instanceof MyListener))
        return false;
      MyListener localMyListener = (MyListener)paramObject;
      return localMyListener.hostname.compareToIgnoreCase(this.hostname) == 0;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.X509DeployKeyManager
 * JD-Core Version:    0.6.0
 */