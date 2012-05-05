package com.sun.deploy.services;

import com.sun.deploy.net.cookie.CookieHandler;
import com.sun.deploy.net.cookie.GenericCookieHandler;
import com.sun.deploy.net.offline.OfflineHandler;
import com.sun.deploy.net.proxy.BrowserProxyConfig;
import com.sun.deploy.net.proxy.MDefaultBrowserProxyConfig;
import com.sun.deploy.net.proxy.MSystemProxyHandler;
import com.sun.deploy.net.proxy.ProxyHandler;
import com.sun.deploy.net.proxy.SunAutoProxyHandler;
import com.sun.deploy.security.BrowserAuthenticator;
import com.sun.deploy.security.BrowserKeystore;
import com.sun.deploy.security.CertStore;
import com.sun.deploy.security.CredentialManager;
import com.sun.deploy.security.MozillaSSLRootCertStore;
import com.sun.deploy.security.MozillaSigningRootCertStore;
import java.io.File;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.Security;

public class MPlatformService
  implements Service
{
  public CookieHandler getCookieHandler()
  {
    return new GenericCookieHandler();
  }

  public BrowserProxyConfig getProxyConfig()
  {
    return new MDefaultBrowserProxyConfig();
  }

  public ProxyHandler getSystemProxyHandler()
  {
    return new MSystemProxyHandler();
  }

  public ProxyHandler getAutoProxyHandler()
  {
    return new SunAutoProxyHandler();
  }

  public ProxyHandler getBrowserProxyHandler()
  {
    return null;
  }

  public CertStore getBrowserSigningRootCertStore()
  {
    if (BrowserKeystore.isJSSCryptoConfigured())
      return new MozillaSigningRootCertStore();
    return null;
  }

  public CertStore getBrowserSSLRootCertStore()
  {
    if (BrowserKeystore.isJSSCryptoConfigured())
      return new MozillaSSLRootCertStore();
    return null;
  }

  public CertStore getBrowserTrustedCertStore()
  {
    return null;
  }

  public KeyStore getBrowserClientAuthKeyStore()
  {
    if (BrowserKeystore.isJSSCryptoConfigured())
    {
      KeyStore localKeyStore = (KeyStore)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          try
          {
            return KeyStore.getInstance("MozillaMy");
          }
          catch (KeyStoreException localKeyStoreException)
          {
            localKeyStoreException.printStackTrace();
          }
          return null;
        }
      });
      return localKeyStore;
    }
    return null;
  }

  public BrowserAuthenticator getBrowserAuthenticator()
  {
    return null;
  }

  public CredentialManager getCredentialManager()
  {
    return CredentialManager.getInstance();
  }

  public SecureRandom getSecureRandom()
  {
    try
    {
      File localFile = new File("/dev/urandom");
      if ((localFile != null) && (localFile.exists()))
        Security.setProperty("securerandom.source", "file:/dev/urandom");
    }
    catch (Throwable localThrowable)
    {
    }
    return new SecureRandom();
  }

  public boolean isIExplorer()
  {
    return false;
  }

  public boolean isNetscape()
  {
    return true;
  }

  public OfflineHandler getOfflineHandler()
  {
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.services.MPlatformService
 * JD-Core Version:    0.6.0
 */