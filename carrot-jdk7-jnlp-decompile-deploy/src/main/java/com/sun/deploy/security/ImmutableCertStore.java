package com.sun.deploy.security;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collection;

final class ImmutableCertStore
  implements CertStore
{
  private CertStore certStore = null;

  ImmutableCertStore(CertStore paramCertStore)
  {
    this.certStore = paramCertStore;
  }

  public void load()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    this.certStore.load();
  }

  public void load(boolean paramBoolean)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    this.certStore.load(paramBoolean);
  }

  public void save()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    throw new IOException("Cannot modify certificate store.");
  }

  public boolean add(Certificate paramCertificate)
    throws KeyStoreException
  {
    throw new KeyStoreException("Cannot modify certificate store.");
  }

  public boolean add(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException
  {
    throw new KeyStoreException("Cannot modify certificate store.");
  }

  public boolean remove(Certificate paramCertificate)
    throws IOException, KeyStoreException
  {
    throw new IOException("Cannot modify certificate store.");
  }

  public boolean contains(Certificate paramCertificate)
    throws KeyStoreException
  {
    return this.certStore.contains(paramCertificate);
  }

  public boolean contains(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException
  {
    return this.certStore.contains(paramCertificate, paramBoolean);
  }

  public boolean verify(Certificate paramCertificate)
    throws KeyStoreException
  {
    return this.certStore.verify(paramCertificate);
  }

  public Collection getCertificates()
    throws KeyStoreException
  {
    return this.certStore.getCertificates();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.ImmutableCertStore
 * JD-Core Version:    0.6.0
 */