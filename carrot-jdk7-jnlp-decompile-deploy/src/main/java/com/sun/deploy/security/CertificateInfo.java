package com.sun.deploy.security;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

class CertificateInfo
{
  X509Certificate cert;

  CertificateInfo(X509Certificate paramX509Certificate)
  {
    this.cert = paramX509Certificate;
  }

  public X509Certificate getCertificate()
  {
    return this.cert;
  }

  private String extractAliasName(X509Certificate paramX509Certificate)
  {
    String str1 = getMessage("security.dialog.unknown.subject");
    String str2 = getMessage("security.dialog.unknown.issuer");
    try
    {
      Principal localPrincipal = paramX509Certificate.getSubjectDN();
      localObject = paramX509Certificate.getIssuerDN();
      String str3 = localPrincipal.getName();
      String str4 = ((Principal)localObject).getName();
      str1 = extractFromQuote(str3, "CN=");
      if (str1 == null)
        str1 = extractFromQuote(str3, "O=");
      if (str1 == null)
        str1 = getMessage("security.dialog.unknown.subject");
      str2 = extractFromQuote(str4, "CN=");
      if (str2 == null)
        str2 = extractFromQuote(str4, "O=");
      if (str2 == null)
        str2 = getMessage("security.dialog.unknown.issuer");
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
    MessageFormat localMessageFormat = new MessageFormat(getMessage("security.dialog.certShowName"));
    Object localObject = { str1, str2 };
    return (String)localMessageFormat.format(localObject);
  }

  private String extractFromQuote(String paramString1, String paramString2)
  {
    if (paramString1 == null)
      return null;
    int i = paramString1.indexOf(paramString2);
    int j = 0;
    if (i >= 0)
    {
      i += paramString2.length();
      if (paramString1.charAt(i) == '"')
      {
        i += 1;
        j = paramString1.indexOf('"', i);
      }
      else
      {
        j = paramString1.indexOf(',', i);
      }
      if (j < 0)
        return paramString1.substring(i);
      return paramString1.substring(i, j);
    }
    return null;
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  public String toString()
  {
    return extractAliasName(this.cert);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CertificateInfo
 * JD-Core Version:    0.6.0
 */