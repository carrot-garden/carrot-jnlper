package com.sun.deploy.security;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collection;

public abstract interface CertStore
{
  public static final int USER = 1;
  public static final int SYSTEM = 2;
  public static final int ALL = 3;
  public static final String TSFLAG = "$tsflag";

  public abstract void load()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException;

  public abstract void load(boolean paramBoolean)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException;

  public abstract void save()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException;

  public abstract boolean add(Certificate paramCertificate)
    throws KeyStoreException;

  public abstract boolean add(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException;

  public abstract boolean remove(Certificate paramCertificate)
    throws IOException, KeyStoreException;

  public abstract boolean contains(Certificate paramCertificate)
    throws KeyStoreException;

  public abstract boolean contains(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException;

  public abstract boolean verify(Certificate paramCertificate)
    throws KeyStoreException;

  public abstract Collection getCertificates()
    throws KeyStoreException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CertStore
 * JD-Core Version:    0.6.0
 */