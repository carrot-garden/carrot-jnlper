package com.sun.deploy.security;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.security.CodeSource;
import java.security.cert.Certificate;

public class BadCertificateDialog
{
  private static boolean _isHttps = false;

  public static void showDialog(CodeSource paramCodeSource, AppInfo paramAppInfo, Exception paramException)
  {
    Certificate[] arrayOfCertificate = paramCodeSource.getCertificates();
    String str2 = getMessage("security.badcert.caption");
    String str1;
    if (getHttpsDialog())
      str1 = getMessage("security.badcert.https.text");
    else if ((paramException instanceof CertificateConfigException))
      str1 = getMessage("security.badcert.config.text");
    else
      str1 = getMessage("security.badcert.text");
    if (!Trace.isAutomationEnabled())
      ToolkitStore.getUI().showExceptionDialog(null, paramAppInfo, paramException, str2, str1, null, arrayOfCertificate);
    else
      Trace.msgSecurityPrintln("trustdecider.automation.badcert");
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  private static int getAcceleratorKey(String paramString)
  {
    return ResourceManager.getAcceleratorKey(paramString);
  }

  private static boolean getHttpsDialog()
  {
    return _isHttps;
  }

  public static void setHttpsDialog(boolean paramBoolean)
  {
    _isHttps = paramBoolean;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.BadCertificateDialog
 * JD-Core Version:    0.6.0
 */