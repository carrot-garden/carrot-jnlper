package com.sun.deploy.security;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

final class X509CertificateWrapper extends X509Certificate
{
  private final X509Certificate cert;
  private static final String OID_NETSCAPE_CERT_TYPE = "2.16.840.1.113730.1.1";
  private static final String NSCT_OBJECT_SIGNING_CA = "object_signing_ca";
  private static final String NSCT_SSL_CA = "ssl_ca";
  private static final String NSCT_S_MIME_CA = "s_mime_ca";

  X509CertificateWrapper(X509Certificate paramX509Certificate)
  {
    this.cert = paramX509Certificate;
  }

  public int getBasicConstraints()
  {
    int i = this.cert.getBasicConstraints();
    if (i != -1)
      return i;
    try
    {
      if ((this.cert.getExtensionValue("2.16.840.1.113730.1.1") != null) && ((CertUtils.getNetscapeCertTypeBit(this.cert, "ssl_ca") == true) || (CertUtils.getNetscapeCertTypeBit(this.cert, "s_mime_ca") == true) || (CertUtils.getNetscapeCertTypeBit(this.cert, "object_signing_ca") == true)))
        return 2147483647;
    }
    catch (CertificateException localCertificateException)
    {
      localCertificateException.printStackTrace();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return -1;
  }

  public boolean[] getKeyUsage()
  {
    return this.cert.getKeyUsage();
  }

  public boolean[] getSubjectUniqueID()
  {
    return this.cert.getSubjectUniqueID();
  }

  public boolean[] getIssuerUniqueID()
  {
    return this.cert.getIssuerUniqueID();
  }

  public byte[] getSigAlgParams()
  {
    return this.cert.getSigAlgParams();
  }

  public String getSigAlgOID()
  {
    return this.cert.getSigAlgOID();
  }

  public String getSigAlgName()
  {
    return this.cert.getSigAlgName();
  }

  public byte[] getSignature()
  {
    return this.cert.getSignature();
  }

  public BigInteger getSerialNumber()
  {
    return this.cert.getSerialNumber();
  }

  public Date getNotAfter()
  {
    return this.cert.getNotAfter();
  }

  public Date getNotBefore()
  {
    return this.cert.getNotBefore();
  }

  public Principal getSubjectDN()
  {
    return this.cert.getSubjectDN();
  }

  public byte[] getTBSCertificate()
    throws CertificateEncodingException
  {
    return this.cert.getTBSCertificate();
  }

  public int getVersion()
  {
    return this.cert.getVersion();
  }

  public Principal getIssuerDN()
  {
    return this.cert.getIssuerDN();
  }

  public void checkValidity()
    throws CertificateExpiredException, CertificateNotYetValidException
  {
  }

  public void checkValidity(Date paramDate)
    throws CertificateExpiredException, CertificateNotYetValidException
  {
  }

  public PublicKey getPublicKey()
  {
    return this.cert.getPublicKey();
  }

  public byte[] getEncoded()
    throws CertificateEncodingException
  {
    return this.cert.getEncoded();
  }

  public String toString()
  {
    return this.cert.toString();
  }

  public void verify(PublicKey paramPublicKey)
    throws CertificateException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException
  {
    this.cert.verify(paramPublicKey);
  }

  public void verify(PublicKey paramPublicKey, String paramString)
    throws CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException
  {
    this.cert.verify(paramPublicKey, paramString);
  }

  public byte[] getExtensionValue(String paramString)
  {
    return this.cert.getExtensionValue(paramString);
  }

  public Set getNonCriticalExtensionOIDs()
  {
    return this.cert.getNonCriticalExtensionOIDs();
  }

  public Set getCriticalExtensionOIDs()
  {
    return this.cert.getCriticalExtensionOIDs();
  }

  public boolean hasUnsupportedCriticalExtension()
  {
    return this.cert.hasUnsupportedCriticalExtension();
  }

  public List getExtendedKeyUsage()
    throws CertificateParsingException
  {
    return this.cert.getExtendedKeyUsage();
  }

  public Collection getIssuerAlternativeNames()
    throws CertificateParsingException
  {
    return this.cert.getIssuerAlternativeNames();
  }

  public X500Principal getIssuerX500Principal()
  {
    return this.cert.getIssuerX500Principal();
  }

  public Collection getSubjectAlternativeNames()
    throws CertificateParsingException
  {
    return this.cert.getSubjectAlternativeNames();
  }

  public X500Principal getSubjectX500Principal()
  {
    return this.cert.getSubjectX500Principal();
  }

  public boolean equals(Object paramObject)
  {
    return this.cert.equals(paramObject);
  }

  public int hashCode()
  {
    return this.cert.hashCode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.X509CertificateWrapper
 * JD-Core Version:    0.6.0
 */