package com.sun.deploy.net.protocol.https;

import com.sun.deploy.config.Config;
import com.sun.deploy.security.BrowserKeystore;
import com.sun.deploy.security.CertificateHostnameVerifier;
import com.sun.deploy.security.X509DeployKeyManager;
import com.sun.deploy.security.X509DeployTrustManager;
import com.sun.deploy.security.X509Extended7DeployTrustManager;
import com.sun.deploy.security.X509ExtendedDeployTrustManager;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureRandom;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class Handler extends sun.net.www.protocol.https.Handler
{
  protected URLConnection openConnection(URL paramURL)
    throws IOException
  {
    Initializer.init();
    return super.openConnection(paramURL);
  }

  static class Initializer
  {
    static void init()
    {
    }

    static
    {
      try
      {
        AccessController.doPrivileged(new PrivilegedExceptionAction()
        {
          public Object run()
            throws Exception
          {
            if (Config.getBooleanProperty("deployment.security.browser.keystore.use"))
              BrowserKeystore.registerSecurityProviders();
            SecureRandom localSecureRandom = ServiceManager.getService().getSecureRandom();
            localSecureRandom.nextInt();
            SSLContext localSSLContext = SSLContext.getInstance("SSL");
            Object localObject1;
            if (Config.isJavaVersionAtLeast17())
            {
              localObject1 = new X509Extended7DeployTrustManager();
            }
            else if (Config.isJavaVersionAtLeast16())
            {
              localObject1 = new X509ExtendedDeployTrustManager();
            }
            else
            {
              localObject2 = new CertificateHostnameVerifier();
              HttpsURLConnection.setDefaultHostnameVerifier((HostnameVerifier)localObject2);
              localObject1 = new X509DeployTrustManager();
            }
            Object localObject2 = new TrustManager[1];
            localObject2[0] = localObject1;
            X509DeployKeyManager localX509DeployKeyManager = new X509DeployKeyManager();
            KeyManager[] arrayOfKeyManager = new KeyManager[1];
            arrayOfKeyManager[0] = localX509DeployKeyManager;
            localSSLContext.init(arrayOfKeyManager, localObject2, localSecureRandom);
            HttpsURLConnection.setDefaultSSLSocketFactory(localSSLContext.getSocketFactory());
            return null;
          }
        });
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        localPrivilegedActionException.printStackTrace();
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.protocol.https.Handler
 * JD-Core Version:    0.6.0
 */