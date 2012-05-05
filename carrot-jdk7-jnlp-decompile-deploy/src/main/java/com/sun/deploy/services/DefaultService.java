package com.sun.deploy.services;

import com.sun.deploy.net.cookie.CookieHandler;
import com.sun.deploy.net.offline.OfflineHandler;
import com.sun.deploy.net.proxy.BrowserProxyConfig;
import com.sun.deploy.net.proxy.ProxyHandler;
import com.sun.deploy.security.BrowserAuthenticator;
import com.sun.deploy.security.CertStore;
import com.sun.deploy.security.CredentialManager;
import java.security.KeyStore;
import java.security.SecureRandom;

public final class DefaultService
  implements Service
{
  public CookieHandler getCookieHandler()
  {
    return null;
  }

  public BrowserProxyConfig getProxyConfig()
  {
    return null;
  }

  public ProxyHandler getSystemProxyHandler()
  {
    return null;
  }

  public ProxyHandler getAutoProxyHandler()
  {
    return null;
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

  public KeyStore getBrowserClientAuthKeyStore()
  {
    return null;
  }

  public CredentialManager getCredentialManager()
  {
    return null;
  }

  public BrowserAuthenticator getBrowserAuthenticator()
  {
    return null;
  }

  public SecureRandom getSecureRandom()
  {
    return null;
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
 * Qualified Name:     com.sun.deploy.services.DefaultService
 * JD-Core Version:    0.6.0
 */