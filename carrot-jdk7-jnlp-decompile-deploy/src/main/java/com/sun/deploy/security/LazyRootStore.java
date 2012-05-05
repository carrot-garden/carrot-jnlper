package com.sun.deploy.security;

import com.sun.deploy.util.PerfLogger;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import javax.security.auth.x500.X500Principal;

final class LazyRootStore
{
  private CertStore browserRootStore = null;
  private CertStore jreRootStore = null;
  private boolean isBrowserRootStoreLoaded = false;
  private boolean isJRERootStoreLoaded = false;
  private X509Certificate ocspCertCA = null;
  private LinkedHashSet allRootCerts = new LinkedHashSet();
  private LinkedHashSet jreRootCerts = new LinkedHashSet();
  private HashMap allTrustedSubjects = new HashMap();

  LazyRootStore(CertStore paramCertStore1, CertStore paramCertStore2)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    this.browserRootStore = paramCertStore1;
    this.jreRootStore = paramCertStore2;
  }

  private void loadBrowserStore()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    if ((this.browserRootStore != null) && (!this.isBrowserRootStoreLoaded))
    {
      PerfLogger.setTime("Security: Start loading browser Root certStore");
      this.browserRootStore.load();
      this.isBrowserRootStoreLoaded = true;
      PerfLogger.setTime("Security: End loading browser Root certStore");
      this.allRootCerts.addAll(this.browserRootStore.getCertificates());
    }
    createAllTrustedSubject(this.allRootCerts);
  }

  private void loadJREStore()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    if (this.isJRERootStoreLoaded)
      return;
    PerfLogger.setTime("Security: Start loading JRE root cert store");
    this.jreRootStore.load();
    this.jreRootCerts.addAll(this.jreRootStore.getCertificates());
    this.allRootCerts.addAll(this.jreRootStore.getCertificates());
    createAllTrustedSubject(this.jreRootCerts);
    this.isJRERootStoreLoaded = true;
    PerfLogger.setTime("Security: End loading JRE root cert store");
  }

  List getTrustAnchors(X509Certificate paramX509Certificate)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    if (!this.isBrowserRootStoreLoaded)
      loadBrowserStore();
    X500Principal localX500Principal = paramX509Certificate.getIssuerX500Principal();
    Object localObject;
    if (this.allRootCerts.contains(paramX509Certificate))
    {
      localObject = new ArrayList();
      ((List)localObject).add(paramX509Certificate);
      return localObject;
    }
    if (this.allTrustedSubjects.containsKey(localX500Principal))
    {
      localObject = (List)this.allTrustedSubjects.get(localX500Principal);
      return localObject;
    }
    if (!this.isJRERootStoreLoaded)
    {
      loadJREStore();
      if (this.allRootCerts.contains(paramX509Certificate))
      {
        localObject = new ArrayList();
        ((List)localObject).add(paramX509Certificate);
        return localObject;
      }
      if (this.allTrustedSubjects.containsKey(localX500Principal))
      {
        localObject = (List)this.allTrustedSubjects.get(localX500Principal);
        return localObject;
      }
    }
    return (List)null;
  }

  private void createAllTrustedSubject(LinkedHashSet paramLinkedHashSet)
  {
    Iterator localIterator = paramLinkedHashSet.iterator();
    while (localIterator.hasNext())
    {
      X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
      X500Principal localX500Principal = localX509Certificate.getSubjectX500Principal();
      Object localObject;
      if (this.allTrustedSubjects.containsKey(localX500Principal))
      {
        localObject = (List)this.allTrustedSubjects.get(localX500Principal);
      }
      else
      {
        localObject = new ArrayList();
        this.allTrustedSubjects.put(localX500Principal, localObject);
      }
      ((List)localObject).add(localX509Certificate);
    }
  }

  boolean containSubject(String paramString)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    X509Certificate localX509Certificate = null;
    String str = null;
    if (!this.isBrowserRootStoreLoaded)
      loadBrowserStore();
    Iterator localIterator = this.allRootCerts.iterator();
    while (localIterator.hasNext())
    {
      localX509Certificate = (X509Certificate)localIterator.next();
      str = CertUtils.extractSubjectAliasName(localX509Certificate);
      if (!paramString.equals(str))
        continue;
      this.ocspCertCA = localX509Certificate;
      return true;
    }
    if (!this.isJRERootStoreLoaded)
    {
      loadJREStore();
      localIterator = this.jreRootCerts.iterator();
      while (localIterator.hasNext())
      {
        localX509Certificate = (X509Certificate)localIterator.next();
        str = CertUtils.extractSubjectAliasName(localX509Certificate);
        if (!paramString.equals(str))
          continue;
        this.ocspCertCA = localX509Certificate;
        return true;
      }
    }
    this.ocspCertCA = null;
    return false;
  }

  X509Certificate getOCSPCert()
  {
    return this.ocspCertCA;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.LazyRootStore
 * JD-Core Version:    0.6.0
 */