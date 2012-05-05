package com.sun.deploy.security;

import com.sun.deploy.trace.Trace;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Random;

final class DeniedCertStore
  implements CertStore
{
  private KeyStore deniedKS = null;

  public void load()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    load(false);
  }

  public void load(boolean paramBoolean)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    if (this.deniedKS == null)
      try
      {
        this.deniedKS = KeyStore.getInstance("JKS");
        this.deniedKS.load(null, new char[0]);
      }
      catch (IOException localIOException)
      {
        Trace.msgSecurityPrintln(localIOException.getMessage());
      }
      catch (KeyStoreException localKeyStoreException)
      {
        Trace.msgSecurityPrintln(localKeyStoreException.getMessage());
      }
      catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
      {
        Trace.msgSecurityPrintln(localNoSuchAlgorithmException.getMessage());
      }
      catch (CertificateException localCertificateException)
      {
        Trace.msgSecurityPrintln(localCertificateException.getMessage());
      }
  }

  public void save()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
  }

  public boolean add(Certificate paramCertificate)
    throws KeyStoreException
  {
    return add(paramCertificate, false);
  }

  public boolean add(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException
  {
    Trace.msgSecurityPrintln("deniedcertstore.cert.adding");
    String str1 = this.deniedKS.getCertificateAlias(paramCertificate);
    int i = 1;
    if (str1 != null)
      try
      {
        if ((!paramBoolean) || (str1.indexOf("$tsflag") > -1))
          i = 0;
        else
          remove(paramCertificate);
      }
      catch (IOException localIOException)
      {
        Trace.securityPrintException(localIOException);
      }
    if (i != 0)
    {
      Random localRandom = new Random();
      String str2 = null;
      while (true)
      {
        if (paramBoolean)
          str2 = "deploymentdeniedcert$tsflag" + localRandom.nextLong();
        else
          str2 = "deploymentdeniedcert" + localRandom.nextLong();
        if (this.deniedKS.getCertificate(str2) == null)
          break;
      }
      this.deniedKS.setCertificateEntry(str2, paramCertificate);
      Trace.msgSecurityPrintln("deniedcertstore.cert.added");
    }
    return true;
  }

  public boolean remove(Certificate paramCertificate)
    throws IOException, KeyStoreException
  {
    Trace.msgSecurityPrintln("deniedcertstore.cert.removing");
    String str = this.deniedKS.getCertificateAlias(paramCertificate);
    if (str != null)
      this.deniedKS.deleteEntry(str);
    Trace.msgSecurityPrintln("deniedcertstore.cert.removed");
    return true;
  }

  public boolean contains(Certificate paramCertificate)
    throws KeyStoreException
  {
    return contains(paramCertificate, false);
  }

  public boolean contains(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException
  {
    Trace.msgSecurityPrintln("deniedcertstore.cert.instore");
    String str = this.deniedKS.getCertificateAlias(paramCertificate);
    if (paramBoolean)
      return (str != null) && (str.indexOf("$tsflag") > -1);
    return str != null;
  }

  public boolean verify(Certificate paramCertificate)
    throws KeyStoreException
  {
    return false;
  }

  public Collection getCertificates()
    throws KeyStoreException
  {
    Trace.msgSecurityPrintln("deniedcertstore.cert.getcertificates");
    ArrayList localArrayList = new ArrayList();
    Enumeration localEnumeration = this.deniedKS.aliases();
    while (localEnumeration.hasMoreElements())
    {
      String str = (String)localEnumeration.nextElement();
      Certificate localCertificate = this.deniedKS.getCertificate(str);
      localArrayList.add(localCertificate);
    }
    return localArrayList;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.DeniedCertStore
 * JD-Core Version:    0.6.0
 */