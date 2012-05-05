package com.sun.deploy.security;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import sun.security.validator.PKIXValidator;

final class DeployCertPathChecker extends PKIXCertPathChecker
{
  private int remainingCerts;
  private PKIXValidator pv;
  private static final String OID_BASIC_CONSTRAINTS = "2.5.29.19";
  private static final String OID_NETSCAPE_CERT_TYPE = "2.16.840.1.113730.1.1";
  private static final Set extSet = Collections.singleton("2.16.840.1.113730.1.1");
  private static final String NSCT_OBJECT_SIGNING_CA = "object_signing_ca";
  private static final String NSCT_SSL_CA = "ssl_ca";
  private static final String NSCT_S_MIME_CA = "s_mime_ca";

  DeployCertPathChecker(PKIXValidator paramPKIXValidator)
  {
    this.pv = paramPKIXValidator;
  }

  public void check(Certificate paramCertificate, Collection paramCollection)
    throws CertPathValidatorException
  {
    X509Certificate localX509Certificate = (X509Certificate)paramCertificate;
    String str = null;
    if ((paramCollection != null) && (!paramCollection.isEmpty()))
      paramCollection.remove("2.16.840.1.113730.1.1");
    this.remainingCerts -= 1;
    if (this.remainingCerts == 0)
      return;
    try
    {
      if (localX509Certificate.getExtensionValue("2.5.29.19") == null)
      {
        if (localX509Certificate.getExtensionValue("2.16.840.1.113730.1.1") != null)
        {
          if (!CertUtils.getNetscapeCertTypeBit(localX509Certificate, "object_signing_ca"))
          {
            Trace.msgSecurityPrintln("trustdecider.check.basicconstraints.certtypebit");
            str = ResourceManager.getMessage("trustdecider.check.basicconstraints.certtypebit");
            throw new CertPathValidatorException(str);
          }
        }
        else
        {
          Trace.msgSecurityPrintln("trustdecider.check.basicconstraints.extensionvalue");
          str = ResourceManager.getMessage("trustdecider.check.basicconstraints.extensionvalue");
          throw new CertPathValidatorException(str);
        }
      }
      else if ((localX509Certificate.getExtensionValue("2.16.840.1.113730.1.1") != null) && ((CertUtils.getNetscapeCertTypeBit(localX509Certificate, "ssl_ca") == true) || (CertUtils.getNetscapeCertTypeBit(localX509Certificate, "s_mime_ca") == true)) && (!CertUtils.getNetscapeCertTypeBit(localX509Certificate, "object_signing_ca")))
      {
        Trace.msgSecurityPrintln("trustdecider.check.basicconstraints.bitvalue");
        str = ResourceManager.getMessage("trustdecider.check.basicconstraints.bitvalue");
        throw new CertPathValidatorException(str);
      }
    }
    catch (IOException localIOException)
    {
      throw new CertPathValidatorException();
    }
    catch (CertificateException localCertificateException)
    {
      throw new CertPathValidatorException();
    }
  }

  public Set getSupportedExtensions()
  {
    return extSet;
  }

  public boolean isForwardCheckingSupported()
  {
    return true;
  }

  public void init(boolean paramBoolean)
    throws CertPathValidatorException
  {
    this.remainingCerts = this.pv.getCertPathLength();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.DeployCertPathChecker
 * JD-Core Version:    0.6.0
 */