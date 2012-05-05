package com.sun.deploy.security;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.AppInfo;
import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

class X509Util
{
  private static final String OID_EXTENDED_KEY_USAGE = "2.5.29.37";

  static Principal getIssuerPrincipal(Certificate paramCertificate)
  {
    try
    {
      return ((X509Certificate)paramCertificate).getIssuerX500Principal();
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
      Trace.println("NoSuchMethodError: X509Certificate.getIssuerX500Principal(): " + localNoSuchMethodError, TraceLevel.SECURITY);
    }
    return ((X509Certificate)paramCertificate).getIssuerDN();
  }

  static Principal getSubjectPrincipal(Certificate paramCertificate)
  {
    try
    {
      return ((X509Certificate)paramCertificate).getSubjectX500Principal();
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
      Trace.println("NoSuchMethodError: X509Certificate.getSubjectX500Principal(): " + localNoSuchMethodError, TraceLevel.SECURITY);
    }
    return ((X509Certificate)paramCertificate).getSubjectDN();
  }

  static List getExtendedKeyUsage(Certificate paramCertificate)
    throws CertificateParsingException
  {
    String str;
    try
    {
      return ((X509Certificate)paramCertificate).getExtendedKeyUsage();
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
      Trace.println("NoSuchMethodError: X509Certificate.getExtendedKeyUsage(): " + localNoSuchMethodError, TraceLevel.SECURITY);
      try
      {
        return oldGetExtendedKeyUsage((X509Certificate)paramCertificate);
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
        str = localIOException.toString();
        throw new CertificateParsingException(str);
      }
      catch (CertificateException localCertificateException)
      {
        Trace.ignoredException(localCertificateException);
        str = localCertificateException.toString();
      }
    }
    throw new CertificateParsingException(str);
  }

  private static List oldGetExtendedKeyUsage(X509Certificate paramX509Certificate)
    throws CertificateException, IOException
  {
    byte[] arrayOfByte1 = paramX509Certificate.getExtensionValue("2.5.29.37");
    if (arrayOfByte1 == null)
      return null;
    DerInputStream localDerInputStream = new DerInputStream(arrayOfByte1);
    byte[] arrayOfByte2 = localDerInputStream.getOctetString();
    DerValue localDerValue = new DerValue(arrayOfByte2);
    Vector localVector = new Vector(1, 1);
    while (localDerValue.data.available() != 0)
    {
      localObject = localDerValue.data.getDerValue();
      ObjectIdentifier localObjectIdentifier = ((DerValue)localObject).getOID();
      localVector.addElement(localObjectIdentifier);
    }
    Object localObject = new ArrayList(localVector.size());
    for (int i = 0; i < localVector.size(); i++)
      ((ArrayList)localObject).add(localVector.elementAt(i).toString());
    return (List)localObject;
  }

  static int showSecurityDialog(Certificate[] paramArrayOfCertificate, URL paramURL, int paramInt1, int paramInt2, boolean paramBoolean, int paramInt3, Date paramDate, AppInfo paramAppInfo)
    throws CertificateException
  {
    int i = showSecurityDialog(paramArrayOfCertificate, paramURL, paramInt1, paramInt2, paramBoolean, paramInt3, paramDate, paramAppInfo, false);
    return i;
  }

  static int showSecurityDialog(Certificate[] paramArrayOfCertificate, URL paramURL, int paramInt1, int paramInt2, boolean paramBoolean1, int paramInt3, Date paramDate, AppInfo paramAppInfo, boolean paramBoolean2)
    throws CertificateException
  {
    int i = TrustDeciderDialog.showDialog(paramArrayOfCertificate, paramURL, paramInt1, paramInt2, paramBoolean1, paramInt3, paramDate, paramAppInfo, false, null, paramBoolean2);
    return i;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.X509Util
 * JD-Core Version:    0.6.0
 */