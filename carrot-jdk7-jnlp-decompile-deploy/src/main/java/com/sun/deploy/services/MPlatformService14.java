package com.sun.deploy.services;

import com.sun.deploy.net.cookie.CookieHandler;
import com.sun.deploy.net.offline.OfflineHandler;
import com.sun.deploy.net.proxy.BrowserProxyConfig;
import com.sun.deploy.net.proxy.BrowserProxyConfigCanonicalizer;
import com.sun.deploy.net.proxy.MDefaultBrowserProxyConfig;
import com.sun.deploy.net.proxy.MSystemProxyHandler;
import com.sun.deploy.net.proxy.ProxyHandler;
import com.sun.deploy.net.proxy.SunAutoProxyHandler;
import com.sun.deploy.security.BrowserAuthenticator;
import com.sun.deploy.security.CertStore;
import com.sun.deploy.security.CredentialManager;
import java.io.File;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;

public class MPlatformService14
  implements Service
{
  public CookieHandler getCookieHandler()
  {
    return null;
  }

  public BrowserProxyConfig getProxyConfig()
  {
    return new BrowserProxyConfigCanonicalizer(new MDefaultBrowserProxyConfig(), getAutoProxyHandler());
  }

  public ProxyHandler getAutoProxyHandler()
  {
    return new SunAutoProxyHandler();
  }

  public ProxyHandler getSystemProxyHandler()
  {
    return new MSystemProxyHandler();
  }

  public ProxyHandler getBrowserProxyHandler()
  {
    return null;
  }

  public CertStore getBrowserSigningRootCertStore()
  {
    return null;
  }

  public CertStore getBrowserSSLRootCertStore()
  {
    return null;
  }

  public CertStore getBrowserTrustedCertStore()
  {
    return null;
  }

  public CredentialManager getCredentialManager()
  {
    return null;
  }

  public KeyStore getBrowserClientAuthKeyStore()
  {
    return null;
  }

  public BrowserAuthenticator getBrowserAuthenticator()
  {
    return null;
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
    return false;
  }

  public OfflineHandler getOfflineHandler()
  {
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.services.MPlatformService14
 * JD-Core Version:    0.6.0
 */