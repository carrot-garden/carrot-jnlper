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

public abstract interface Service
{
  public abstract CookieHandler getCookieHandler();

  public abstract BrowserProxyConfig getProxyConfig();

  public abstract ProxyHandler getSystemProxyHandler();

  public abstract ProxyHandler getAutoProxyHandler();

  public abstract ProxyHandler getBrowserProxyHandler();

  public abstract CertStore getBrowserSigningRootCertStore();

  public abstract CertStore getBrowserSSLRootCertStore();

  public abstract CertStore getBrowserTrustedCertStore();

  public abstract KeyStore getBrowserClientAuthKeyStore();

  public abstract BrowserAuthenticator getBrowserAuthenticator();

  public abstract CredentialManager getCredentialManager();

  public abstract SecureRandom getSecureRandom();

  public abstract boolean isIExplorer();

  public abstract boolean isNetscape();

  public abstract OfflineHandler getOfflineHandler();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.services.Service
 * JD-Core Version:    0.6.0
 */