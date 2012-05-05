package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.AppInfo;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class X509DeployTrustManager
  implements X509TrustManager
{
  private X509TrustManager trustManager = null;
  private static CertStore rootStore = null;
  private static CertStore sslRootStore = null;
  private static CertStore permanentStore = null;
  private static CertStore sessionStore = null;
  private static CertStore deniedStore = null;
  private static CertStore browserSSLRootStore = null;
  private static boolean isBrowserSSLRootStoreLoaded = false;

  public static void reset()
  {
    rootStore = RootCertStore.getCertStore();
    sslRootStore = SSLRootCertStore.getCertStore();
    permanentStore = DeploySSLCertStore.getCertStore();
    sessionStore = new SessionCertStore();
    deniedStore = new DeniedCertStore();
    if (Config.getBooleanProperty("deployment.security.browser.keystore.use"))
    {
      Service localService = ServiceManager.getService();
      browserSSLRootStore = localService.getBrowserSSLRootCertStore();
      isBrowserSSLRootStoreLoaded = false;
    }
  }

  public X509DeployTrustManager()
    throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException
  {
    TrustManagerFactory localTrustManagerFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
    localTrustManagerFactory.init((KeyStore)null);
    TrustManager[] arrayOfTrustManager = localTrustManagerFactory.getTrustManagers();
    this.trustManager = ((X509TrustManager)arrayOfTrustManager[0]);
  }

  public synchronized void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
    throws CertificateException
  {
    boolean bool = false;
    int i = 0;
    if (this.trustManager == null)
      throw new IllegalStateException("TrustManager should not be null");
    int j = -1;
    try
    {
      rootStore.load();
      sslRootStore.load();
      permanentStore.load();
      sessionStore.load();
      deniedStore.load();
      if ((browserSSLRootStore != null) && (!isBrowserSSLRootStoreLoaded))
      {
        browserSSLRootStore.load();
        isBrowserSSLRootStoreLoaded = true;
      }
      if (deniedStore.contains(paramArrayOfX509Certificate[0]))
        throw new CertificateException("Certificate has been denied");
      try
      {
        this.trustManager.checkClientTrusted(paramArrayOfX509Certificate, paramString);
        return;
      }
      catch (CertificateException localCertStore)
      {
        if (sessionStore.contains(paramArrayOfX509Certificate[0]))
          return;
        if (permanentStore.contains(paramArrayOfX509Certificate[0]))
          return;
        if ((paramArrayOfX509Certificate != null) && (paramArrayOfX509Certificate.length > 0))
        {
          k = paramArrayOfX509Certificate.length - 1;
          if ((!rootStore.verify(paramArrayOfX509Certificate[k])) && (!sslRootStore.verify(paramArrayOfX509Certificate[k])) && ((browserSSLRootStore == null) || (!browserSSLRootStore.verify(paramArrayOfX509Certificate[k]))))
            bool = true;
        }
        for (int k = 0; k < paramArrayOfX509Certificate.length; k++)
          try
          {
            paramArrayOfX509Certificate[k].checkValidity();
          }
          catch (CertificateExpiredException localCertificateExpiredException)
          {
            i = -1;
          }
          catch (CertificateNotYetValidException localCertificateNotYetValidException)
          {
            i = 1;
          }
        if (!Trace.isAutomationEnabled())
        {
          j = TrustDeciderDialog.showDialog(paramArrayOfX509Certificate, null, 0, paramArrayOfX509Certificate.length, bool, i, null, new AppInfo(), true);
        }
        else
        {
          Trace.msgSecurityPrintln("x509trustmgr.automation.ignoreclientcert");
          j = 0;
        }
        if (j == 0)
        {
          sessionStore.add(paramArrayOfX509Certificate[0]);
          sessionStore.save();
        }
        else if (j == 2)
        {
          CertStore localCertStore = DeploySSLCertStore.getUserCertStore();
          localCertStore.load(true);
          if (localCertStore.add(paramArrayOfX509Certificate[0]))
            localCertStore.save();
        }
        else
        {
          deniedStore.add(paramArrayOfX509Certificate[0]);
          deniedStore.save();
        }
      }
    }
    catch (CertificateException localCertificateException2)
    {
      throw localCertificateException2;
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
    if ((j != 0) && (j != 2))
      throw new CertificateException("Java couldn't trust Client");
  }

  public synchronized void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
    throws CertificateException
  {
    boolean bool = false;
    int i = 0;
    if (this.trustManager == null)
      throw new IllegalStateException("TrustManager should not be null");
    int j = -1;
    try
    {
      rootStore.load();
      sslRootStore.load();
      permanentStore.load();
      sessionStore.load();
      deniedStore.load();
      if ((browserSSLRootStore != null) && (!isBrowserSSLRootStoreLoaded))
      {
        browserSSLRootStore.load();
        isBrowserSSLRootStoreLoaded = true;
      }
      if (deniedStore.contains(paramArrayOfX509Certificate[0]))
        throw new CertificateException("Certificate has been denied");
      try
      {
        this.trustManager.checkServerTrusted(paramArrayOfX509Certificate, paramString);
        return;
      }
      catch (CertificateException localCertStore)
      {
        if (sessionStore.contains(paramArrayOfX509Certificate[0]))
          return;
        if (permanentStore.contains(paramArrayOfX509Certificate[0]))
          return;
        if ((paramArrayOfX509Certificate != null) && (paramArrayOfX509Certificate.length > 0))
        {
          k = paramArrayOfX509Certificate.length - 1;
          if ((!rootStore.verify(paramArrayOfX509Certificate[k])) && (!sslRootStore.verify(paramArrayOfX509Certificate[k])) && ((browserSSLRootStore == null) || (!browserSSLRootStore.verify(paramArrayOfX509Certificate[k]))))
            bool = true;
        }
        for (int k = 0; k < paramArrayOfX509Certificate.length; k++)
          try
          {
            paramArrayOfX509Certificate[k].checkValidity();
          }
          catch (CertificateExpiredException localCertificateExpiredException)
          {
            i = -1;
          }
          catch (CertificateNotYetValidException localCertificateNotYetValidException)
          {
            i = 1;
          }
        if (!Trace.isAutomationEnabled())
        {
          j = TrustDeciderDialog.showDialog(paramArrayOfX509Certificate, null, 0, paramArrayOfX509Certificate.length, bool, i, null, new AppInfo(), true);
        }
        else
        {
          Trace.msgSecurityPrintln("x509trustmgr.automation.ignoreservercert");
          j = 0;
        }
        if (j == 0)
        {
          sessionStore.add(paramArrayOfX509Certificate[0]);
          sessionStore.save();
        }
        else if (j == 2)
        {
          CertStore localCertStore = DeploySSLCertStore.getUserCertStore();
          localCertStore.load(true);
          if (localCertStore.add(paramArrayOfX509Certificate[0]))
            localCertStore.save();
        }
        else
        {
          deniedStore.add(paramArrayOfX509Certificate[0]);
          deniedStore.save();
        }
      }
    }
    catch (CertificateException localCertificateException2)
    {
      throw localCertificateException2;
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
    if ((j != 0) && (j != 2))
      throw new CertificateException("Java couldn't trust Server");
  }

  public X509Certificate[] getAcceptedIssuers()
  {
    return (X509Certificate[])this.trustManager.getAcceptedIssuers();
  }

  static
  {
    reset();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.X509DeployTrustManager
 * JD-Core Version:    0.6.0
 */