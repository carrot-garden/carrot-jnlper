package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

public final class CertificateHostnameVerifier
  implements HostnameVerifier
{
  private static HashSet hashSet = new HashSet();

  public boolean verify(String paramString, SSLSession paramSSLSession)
  {
    String str = ResourceManager.getMessage("https.dialog.unknown.host");
    X509Certificate localX509Certificate;
    try
    {
      Certificate[] arrayOfCertificate = paramSSLSession.getPeerCertificates();
      if ((arrayOfCertificate[0] instanceof X509Certificate))
        localX509Certificate = (X509Certificate)arrayOfCertificate[0];
      else
        throw new SSLPeerUnverifiedException("");
      localObject = localX509Certificate.getSubjectDN().getName();
      if (localObject != null)
      {
        int i = ((String)localObject).toUpperCase().indexOf("CN=");
        if (i != -1)
        {
          int j = ((String)localObject).indexOf(",", i);
          if (j != -1)
            str = ((String)localObject).substring(i + 3, j);
          else
            str = ((String)localObject).substring(i + 3);
        }
      }
    }
    catch (SSLPeerUnverifiedException localSSLPeerUnverifiedException)
    {
      return false;
    }
    ArrayList localArrayList = CertUtils.getServername(localX509Certificate);
    if (CertUtils.checkWildcardDomainList(paramString, localArrayList))
      return true;
    Object localObject = hashSet.iterator();
    while (((Iterator)localObject).hasNext())
    {
      Object[] arrayOfObject = (Object[])(Object[])((Iterator)localObject).next();
      if ((arrayOfObject[0].toString().equalsIgnoreCase(paramString)) && (arrayOfObject[1].toString().equalsIgnoreCase(str)))
        return true;
    }
    return showHostnameMismatchDialog(paramString, str);
  }

  private boolean showHostnameMismatchDialog(String paramString1, String paramString2)
  {
    String str1 = ResourceManager.getMessage("https.dialog.caption");
    String str2 = ResourceManager.getMessage("https.dialog.masthead");
    AppInfo localAppInfo = new AppInfo(0, paramString1, paramString2, null, null, null, false, false, null, null);
    String str3 = ResourceManager.getMessage("security.dialog.signed.buttonContinue");
    String str4 = ResourceManager.getMessage("security.dialog.signed.buttonCancel");
    ToolkitStore.getUI();
    int i = -1;
    MessageFormat localMessageFormat = new MessageFormat(ResourceManager.getMessage("security.dialog.hostname.mismatch.sub"));
    Object[] arrayOfObject1 = { paramString1, paramString2 };
    String[] arrayOfString = { localMessageFormat.format(arrayOfObject1) };
    if (!Trace.isAutomationEnabled())
    {
      if (Config.getBooleanProperty("deployment.security.jsse.hostmismatch.warning"))
      {
        i = ToolkitStore.getUI().showSecurityDialog(localAppInfo, str1, str2, paramString2, null, false, false, str3, str4, arrayOfString, null, false, null, -1, -1, false);
      }
      else
      {
        ToolkitStore.getUI();
        i = 0;
      }
    }
    else
    {
      Trace.msgSecurityPrintln("hostnameverifier.automation.ignoremismatch");
      ToolkitStore.getUI();
      i = 0;
    }
    ToolkitStore.getUI();
    if (i == 0)
    {
      Object[] arrayOfObject2 = new Object[2];
      arrayOfObject2[0] = paramString1;
      arrayOfObject2[1] = paramString2;
      hashSet.add(arrayOfObject2);
    }
    ToolkitStore.getUI();
    return i == 0;
  }

  public static void reset()
  {
    hashSet.clear();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CertificateHostnameVerifier
 * JD-Core Version:    0.6.0
 */