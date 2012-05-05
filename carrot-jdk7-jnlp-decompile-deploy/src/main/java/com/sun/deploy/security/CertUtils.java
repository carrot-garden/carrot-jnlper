package com.sun.deploy.security;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sun.security.util.BitArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.CRLDistributionPointsExtension;
import sun.security.x509.NetscapeCertTypeExtension;
import sun.security.x509.X509CertImpl;

public class CertUtils
{
  private static final String OID_BASIC_CONSTRAINTS = "2.5.29.19";
  private static final String OID_KEY_USAGE = "2.5.29.15";
  private static final String OID_EXTENDED_KEY_USAGE = "2.5.29.37";
  private static final String OID_NETSCAPE_CERT_TYPE = "2.16.840.1.113730.1.1";
  private static final String OID_EKU_ANY_USAGE = "2.5.29.37.0";
  private static final String OID_EKU_CODE_SIGNING = "1.3.6.1.5.5.7.3.3";
  private static final String OID_EKU_SERVER_AUTH = "1.3.6.1.5.5.7.3.1";
  private static final String OID_EKU_CLIENT_AUTH = "1.3.6.1.5.5.7.3.2";
  private static final String OID_EKU_TIME_STAMPING = "1.3.6.1.5.5.7.3.8";
  private static final String OID_CRL = "2.5.29.31";
  private static final String OID_AIA = "1.3.6.1.5.5.7.1.1";
  private static final String NSCT_OBJECT_SIGNING_CA = "object_signing_ca";
  private static final String NSCT_OBJECT_SIGNING = "object_signing";
  private static final String NSCT_SSL_CA = "ssl_ca";
  private static final String NSCT_S_MIME_CA = "s_mime_ca";
  private static final String NSCT_S_MIME = "s_mime";
  private static final String NSCT_SSL_CLIENT = "ssl_client";
  private static final String NSCT_SSL_SERVER = "ssl_server";
  private static final int KU_SIGNATURE = 0;

  public static KeyStore createEmptyKeyStore()
  {
    KeyStore localKeyStore = null;
    try
    {
      localKeyStore = KeyStore.getInstance("JKS");
      localKeyStore.load(null, null);
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    return localKeyStore;
  }

  public static void checkUsageForCodeSigning(X509Certificate paramX509Certificate, int paramInt)
    throws CertificateException, IOException
  {
    checkUsageForCodeSigning(paramX509Certificate, paramInt, false);
  }

  public static void checkUsageForCodeSigning(X509Certificate paramX509Certificate, int paramInt, boolean paramBoolean)
    throws CertificateException, IOException
  {
    String str = null;
    Set localSet = paramX509Certificate.getCriticalExtensionOIDs();
    if (localSet == null)
      localSet = Collections.EMPTY_SET;
    if (!checkBasicConstraintsForCodeSigning(paramX509Certificate, localSet, paramInt))
    {
      Trace.msgSecurityPrintln("trustdecider.check.basicconstraints");
      str = ResourceManager.getMessage("trustdecider.check.basicconstraints");
      throw new CertificateException(str);
    }
    if (paramInt == 0)
    {
      if (!checkLeafKeyUsageForCodeSigning(paramX509Certificate, localSet, paramBoolean))
      {
        Trace.msgSecurityPrintln("trustdecider.check.leafkeyusage");
        str = ResourceManager.getMessage("trustdecider.check.leafkeyusage");
        throw new CertificateException(str);
      }
    }
    else if (!checkSignerKeyUsage(paramX509Certificate, localSet))
    {
      Trace.msgSecurityPrintln("trustdecider.check.signerkeyusage");
      str = ResourceManager.getMessage("trustdecider.check.signerkeyusage");
      throw new CertificateException(str);
    }
    if (!localSet.isEmpty())
    {
      Trace.msgSecurityPrintln("trustdecider.check.extensions");
      str = ResourceManager.getMessage("trustdecider.check.extensions");
      throw new CertificateException(str);
    }
  }

  private static boolean checkBasicConstraintsForCodeSigning(X509Certificate paramX509Certificate, Set paramSet, int paramInt)
    throws CertificateException, IOException
  {
    paramSet.remove("2.5.29.19");
    paramSet.remove("2.16.840.1.113730.1.1");
    if (paramInt == 0)
      return true;
    if (paramX509Certificate.getExtensionValue("2.5.29.19") == null)
    {
      if (paramX509Certificate.getExtensionValue("2.16.840.1.113730.1.1") != null)
      {
        if (!getNetscapeCertTypeBit(paramX509Certificate, "object_signing_ca"))
        {
          Trace.msgSecurityPrintln("trustdecider.check.basicconstraints.certtypebit");
          return false;
        }
      }
      else
      {
        Trace.msgSecurityPrintln("trustdecider.check.basicconstraints.extensionvalue");
        return false;
      }
    }
    else
    {
      if ((paramX509Certificate.getExtensionValue("2.16.840.1.113730.1.1") != null) && ((getNetscapeCertTypeBit(paramX509Certificate, "ssl_ca")) || (getNetscapeCertTypeBit(paramX509Certificate, "s_mime_ca")) || (getNetscapeCertTypeBit(paramX509Certificate, "object_signing_ca"))) && (!getNetscapeCertTypeBit(paramX509Certificate, "object_signing_ca")))
      {
        Trace.msgSecurityPrintln("trustdecider.check.basicconstraints.bitvalue");
        return false;
      }
      int i = paramX509Certificate.getBasicConstraints();
      if (i < 0)
      {
        Trace.msgSecurityPrintln("trustdecider.check.basicconstraints.enduser");
        return false;
      }
      if (paramInt - 1 > i)
      {
        Trace.msgSecurityPrintln("trustdecider.check.basicconstraints.pathlength");
        return false;
      }
    }
    return true;
  }

  private static boolean checkLeafKeyUsageForCodeSigning(X509Certificate paramX509Certificate, Set paramSet, boolean paramBoolean)
    throws CertificateException, IOException
  {
    paramSet.remove("2.5.29.15");
    boolean[] arrayOfBoolean = paramX509Certificate.getKeyUsage();
    if (arrayOfBoolean != null)
    {
      if (arrayOfBoolean.length == 0)
      {
        Trace.msgSecurityPrintln("trustdecider.check.leafkeyusage.length");
        return false;
      }
      int i = arrayOfBoolean[0];
      if (i == 0)
      {
        Trace.msgSecurityPrintln("trustdecider.check.leafkeyusage.digitalsignature");
        return false;
      }
    }
    List localList = X509Util.getExtendedKeyUsage(paramX509Certificate);
    Set localSet = paramX509Certificate.getNonCriticalExtensionOIDs();
    if ((localList != null) && ((paramSet.contains("2.5.29.37")) || (localSet.contains("2.5.29.37"))))
    {
      paramSet.remove("2.5.29.37");
      if (paramBoolean)
      {
        if ((!localList.contains("2.5.29.37.0")) && (!localList.contains("1.3.6.1.5.5.7.3.8")))
        {
          Trace.msgSecurityPrintln("trustdecider.check.leafkeyusage.tsaextkeyusageinfo");
          return false;
        }
      }
      else if ((!localList.contains("2.5.29.37.0")) && (!localList.contains("1.3.6.1.5.5.7.3.3")))
      {
        Trace.msgSecurityPrintln("trustdecider.check.leafkeyusage.extkeyusageinfo");
        return false;
      }
    }
    if ((paramX509Certificate.getExtensionValue("2.16.840.1.113730.1.1") != null) && (!getNetscapeCertTypeBit(paramX509Certificate, "object_signing")))
    {
      Trace.msgSecurityPrintln("trustdecider.check.leafkeyusage.certtypebit");
      return false;
    }
    return true;
  }

  private static boolean checkSignerKeyUsage(X509Certificate paramX509Certificate, Set paramSet)
    throws CertificateException, IOException
  {
    paramSet.remove("2.5.29.15");
    boolean[] arrayOfBoolean = paramX509Certificate.getKeyUsage();
    if ((arrayOfBoolean != null) && ((arrayOfBoolean.length < 6) || (arrayOfBoolean[5] == 0)))
    {
      Trace.msgSecurityPrintln("trustdecider.check.signerkeyusage.lengthandbit");
      return false;
    }
    List localList = X509Util.getExtendedKeyUsage(paramX509Certificate);
    Set localSet = paramX509Certificate.getNonCriticalExtensionOIDs();
    if ((localList != null) && ((paramSet.contains("2.5.29.37")) || (localSet.contains("2.5.29.37"))))
    {
      paramSet.remove("2.5.29.37");
      if ((!localList.contains("2.5.29.37.0")) && (!localList.contains("1.3.6.1.5.5.7.3.3")))
      {
        Trace.msgSecurityPrintln("trustdecider.check.signerkeyusage.keyusage");
        return false;
      }
    }
    return true;
  }

  static boolean getNetscapeCertTypeBit(X509Certificate paramX509Certificate, String paramString)
    throws CertificateException, IOException
  {
    byte[] arrayOfByte1 = paramX509Certificate.getExtensionValue("2.16.840.1.113730.1.1");
    if (arrayOfByte1 == null)
      return false;
    DerInputStream localDerInputStream = new DerInputStream(arrayOfByte1);
    byte[] arrayOfByte2 = localDerInputStream.getOctetString();
    arrayOfByte2 = new DerValue(arrayOfByte2).getUnalignedBitString().toByteArray();
    NetscapeCertTypeExtension localNetscapeCertTypeExtension = new NetscapeCertTypeExtension(arrayOfByte2);
    Boolean localBoolean = (Boolean)localNetscapeCertTypeExtension.get(paramString);
    return localBoolean.booleanValue();
  }

  private static boolean checkKeyUsage(X509Certificate paramX509Certificate, int paramInt)
  {
    boolean[] arrayOfBoolean = paramX509Certificate.getKeyUsage();
    if (arrayOfBoolean == null)
      return true;
    return (arrayOfBoolean.length > paramInt) && (arrayOfBoolean[paramInt] != 0);
  }

  private static boolean checkEKU(X509Certificate paramX509Certificate, String paramString)
    throws CertificateException
  {
    List localList = paramX509Certificate.getExtendedKeyUsage();
    if (localList == null)
      return true;
    return (localList.contains(paramString)) || (localList.contains("2.5.29.37.0"));
  }

  static boolean checkTLSClient(X509Certificate paramX509Certificate)
    throws CertificateException
  {
    if (!checkKeyUsage(paramX509Certificate, 0))
    {
      Trace.msgSecurityPrintln("clientauth.checkTLSClient.checkKeyUsage");
      return false;
    }
    if (!checkEKU(paramX509Certificate, "1.3.6.1.5.5.7.3.2"))
    {
      Trace.msgSecurityPrintln("clientauth.checkTLSClient.checkEKU");
      return false;
    }
    return true;
  }

  public static boolean isIssuerOf(X509Certificate paramX509Certificate1, X509Certificate paramX509Certificate2)
  {
    Principal localPrincipal1 = paramX509Certificate1.getIssuerDN();
    Principal localPrincipal2 = paramX509Certificate2.getSubjectDN();
    return localPrincipal1.equals(localPrincipal2);
  }

  private static String extractFromQuote(String paramString1, String paramString2)
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

  public static String extractSubjectAliasName(X509Certificate paramX509Certificate)
  {
    String str1 = ResourceManager.getMessage("config.unknownSubject");
    try
    {
      Principal localPrincipal = paramX509Certificate.getSubjectDN();
      String str2 = localPrincipal.getName();
      str1 = extractFromQuote(str2, "CN=");
      if (str1 == null)
      {
        String str3 = extractFromQuote(str2, "O=");
        String str4 = extractFromQuote(str2, "OU=");
        if ((str3 != null) || (str4 != null))
        {
          MessageFormat localMessageFormat = new MessageFormat(ResourceManager.getMessage("config.certShowOOU"));
          Object[] arrayOfObject = { str3, str4 };
          if (str3 == null)
            arrayOfObject[0] = "";
          if (str4 == null)
            arrayOfObject[1] = "";
          str1 = localMessageFormat.format(arrayOfObject);
        }
      }
      if (str1 == null)
        str1 = ResourceManager.getMessage("config.unknownSubject");
    }
    catch (Exception localException)
    {
    }
    return str1;
  }

  public static String extractIssuerAliasName(X509Certificate paramX509Certificate)
  {
    String str1 = ResourceManager.getMessage("config.unknownIssuer");
    try
    {
      Principal localPrincipal = paramX509Certificate.getIssuerDN();
      String str2 = localPrincipal.getName();
      str1 = extractFromQuote(str2, "CN=");
      if (str1 == null)
      {
        String str3 = extractFromQuote(str2, "O=");
        String str4 = extractFromQuote(str2, "OU=");
        if ((str3 != null) || (str4 != null))
        {
          MessageFormat localMessageFormat = new MessageFormat(ResourceManager.getMessage("config.certShowOOU"));
          Object[] arrayOfObject = { str3, str4 };
          if (str3 == null)
            arrayOfObject[0] = "";
          if (str4 == null)
            arrayOfObject[1] = "";
          str1 = localMessageFormat.format(arrayOfObject);
        }
      }
      if (str1 == null)
        str1 = ResourceManager.getMessage("config.unknownIssuer");
    }
    catch (Exception localException)
    {
    }
    return str1;
  }

  static long getFileLastModified(String paramString)
  {
    long l = 0L;
    try
    {
      l = ((Long)AccessController.doPrivileged(new PrivilegedExceptionAction(paramString)
      {
        private final String val$filename;

        public Object run()
          throws SecurityException
        {
          return new Long(new File(this.val$filename).lastModified());
        }
      })).longValue();
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Trace.securityPrintException(localPrivilegedActionException);
    }
    return l;
  }

  static boolean getCertCRLExtension(X509Certificate paramX509Certificate)
    throws IOException
  {
    byte[] arrayOfByte = paramX509Certificate.getExtensionValue("2.5.29.31");
    if (arrayOfByte == null)
    {
      Trace.msgSecurityPrintln("trustdecider.check.validation.crl.notfound");
      return false;
    }
    if (arrayOfByte[0] == 4)
      arrayOfByte = new DerValue(arrayOfByte).getOctetString();
    Trace.msgSecurityPrintln(extractSubjectAliasName(paramX509Certificate));
    CRLDistributionPointsExtension localCRLDistributionPointsExtension = new CRLDistributionPointsExtension(new Boolean(false), arrayOfByte);
    Trace.msgSecurityPrintln(localCRLDistributionPointsExtension.toString());
    return true;
  }

  static boolean hasAIAExtensionWithOCSPAccessMethod(X509Certificate paramX509Certificate)
    throws IOException
  {
    AuthorityInfoAccessExtension localAuthorityInfoAccessExtension = null;
    Object localObject;
    if ((paramX509Certificate instanceof X509CertImpl))
    {
      localAuthorityInfoAccessExtension = ((X509CertImpl)paramX509Certificate).getAuthorityInfoAccessExtension();
    }
    else
    {
      localObject = paramX509Certificate.getExtensionValue("1.3.6.1.5.5.7.1.1");
      if (localObject == null)
      {
        Trace.msgSecurityPrintln("trustdecider.check.validation.ocsp.notfound");
        return false;
      }
      if (localObject[0] == 4)
        localObject = new DerValue(localObject).getOctetString();
      Trace.msgSecurityPrintln(extractSubjectAliasName(paramX509Certificate));
      localAuthorityInfoAccessExtension = new AuthorityInfoAccessExtension(new Boolean(false), localObject);
    }
    if (localAuthorityInfoAccessExtension != null)
    {
      Trace.msgSecurityPrintln(localAuthorityInfoAccessExtension.toString());
      localObject = localAuthorityInfoAccessExtension.getAccessDescriptions();
      Iterator localIterator = ((List)localObject).iterator();
      while (localIterator.hasNext())
      {
        AccessDescription localAccessDescription = (AccessDescription)localIterator.next();
        if (localAccessDescription.getAccessMethod().equals(AccessDescription.Ad_OCSP_Id))
          return true;
      }
    }
    return false;
  }

  static boolean checkWildcardDomainList(String paramString, ArrayList paramArrayList)
  {
    for (int i = 0; i < paramArrayList.size(); i++)
    {
      String str = (String)paramArrayList.get(i);
      if (checkWildcardDomain(paramString, str))
        return true;
    }
    return false;
  }

  private static boolean checkWildcardDomain(String paramString1, String paramString2)
  {
    if ((paramString1 == null) || (paramString1.length() == 0) || (paramString2 == null) || (paramString2.length() == 0))
      return false;
    paramString2 = paramString2.trim();
    paramString1 = paramString1.trim();
    if (paramString2.equalsIgnoreCase(paramString1))
      return true;
    int i = paramString2.indexOf("*.");
    if (i == -1)
      return false;
    String str1 = paramString2.substring(i + 1);
    String str2 = paramString2.substring(0, i);
    return (!str2.contains(".")) && (paramString1.indexOf(str2) == 0) && (paramString1.endsWith(str1)) && (paramString1.length() >= paramString2.length());
  }

  public static ArrayList getServername(X509Certificate paramX509Certificate)
  {
    ArrayList localArrayList = new ArrayList();
    try
    {
      Collection localCollection = paramX509Certificate.getSubjectAlternativeNames();
      if (localCollection != null)
      {
        Iterator localIterator = localCollection.iterator();
        while (localIterator.hasNext())
        {
          List localList = (List)localIterator.next();
          if (((Integer)localList.get(0)).intValue() == 2)
          {
            String str2 = (String)localList.get(1);
            localArrayList.add(str2);
          }
        }
        if (localArrayList.size() > 0)
          return localArrayList;
      }
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
    }
    catch (CertificateException localCertificateException)
    {
    }
    String str1 = extractSubjectAliasName(paramX509Certificate);
    localArrayList.add(str1);
    return localArrayList;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CertUtils
 * JD-Core Version:    0.6.0
 */