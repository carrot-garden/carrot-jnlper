package com.sun.deploy.config;

import com.sun.deploy.trace.Trace;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

class ConfigTrustManager
  implements X509TrustManager
{
  private static SSLSocketFactory ssf = null;
  private X509TrustManager defaultTM = null;
  private X509TrustManager nativeTM = null;

  public ConfigTrustManager()
    throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException
  {
    TrustManager[] arrayOfTrustManager1 = generateTrustManager(false);
    TrustManager[] arrayOfTrustManager2 = generateTrustManager(true);
    if (arrayOfTrustManager1.length > 0)
      this.defaultTM = ((X509TrustManager)arrayOfTrustManager1[0]);
    if (arrayOfTrustManager2.length > 0)
      this.nativeTM = ((X509TrustManager)arrayOfTrustManager2[0]);
  }

  public static void resetHttpsFactory(HttpsURLConnection paramHttpsURLConnection)
  {
    try
    {
      if (ssf == null)
      {
        ConfigTrustManager localConfigTrustManager = new ConfigTrustManager();
        TrustManager[] arrayOfTrustManager = new TrustManager[1];
        arrayOfTrustManager[0] = localConfigTrustManager;
        SSLContext localSSLContext = SSLContext.getInstance("TLS");
        localSSLContext.init(null, arrayOfTrustManager, null);
        ssf = localSSLContext.getSocketFactory();
      }
      if (ssf != null)
      {
        Trace.securityPrintln("Reset SSLSocketFactory using Config TrustManager");
        paramHttpsURLConnection.setSSLSocketFactory(ssf);
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }

  public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
    throws CertificateException
  {
  }

  public X509Certificate[] getAcceptedIssuers()
  {
    X509Certificate[] arrayOfX509Certificate1 = this.defaultTM.getAcceptedIssuers();
    X509Certificate[] arrayOfX509Certificate2 = this.nativeTM.getAcceptedIssuers();
    X509Certificate[] arrayOfX509Certificate3 = new X509Certificate[arrayOfX509Certificate1.length + arrayOfX509Certificate2.length];
    System.arraycopy(arrayOfX509Certificate1, 0, arrayOfX509Certificate3, 0, arrayOfX509Certificate1.length);
    System.arraycopy(arrayOfX509Certificate2, 0, arrayOfX509Certificate3, arrayOfX509Certificate1.length, arrayOfX509Certificate2.length);
    return arrayOfX509Certificate3;
  }

  public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
    throws CertificateException
  {
    int i = 0;
    if ((this.defaultTM == null) && (this.nativeTM == null))
      throw new CertificateException("Both TrustManager are null");
    if (this.defaultTM != null)
      try
      {
        this.defaultTM.checkServerTrusted(paramArrayOfX509Certificate, paramString);
        return;
      }
      catch (CertificateException localCertificateException1)
      {
        Trace.securityPrintln("Default TrustManager check failed");
        if (this.nativeTM == null)
        {
          Trace.securityPrintln("No native TrustManager available");
          throw localCertificateException1;
        }
        i = 1;
      }
    if ((this.nativeTM != null) && ((i != 0) || (this.defaultTM == null)))
      try
      {
        Trace.securityPrintln("Using native TrustManager");
        this.nativeTM.checkServerTrusted(paramArrayOfX509Certificate, paramString);
      }
      catch (CertificateException localCertificateException2)
      {
        Trace.securityPrintln("Native(IE browser) TrustManager check failed");
        throw localCertificateException2;
      }
  }

  private TrustManager[] generateTrustManager(boolean paramBoolean)
  {
    String str1 = "";
    try
    {
      String str2 = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory localTrustManagerFactory = TrustManagerFactory.getInstance(str2);
      if (paramBoolean)
      {
        KeyStore localKeyStore = KeyStore.getInstance("Windows-ROOT");
        localKeyStore.load(null, str1.toCharArray());
        localTrustManagerFactory.init(localKeyStore);
      }
      else
      {
        localTrustManagerFactory.init((KeyStore)null);
      }
      return localTrustManagerFactory.getTrustManagers();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.ConfigTrustManager
 * JD-Core Version:    0.6.0
 */