package com.sun.deploy.security;

import com.sun.deploy.trace.Trace;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

abstract class MozillaCertStore
  implements CertStore
{
  protected static final int VALID_PEER = 1;
  protected static final int TRUSTED_PEER = 2;
  protected static final int VALID_CA = 8;
  protected static final int TRUSTED_CA = 16;
  protected static final int USER = 64;
  protected static final int TRUSTED_CLIENT_CA = 128;
  private Collection certs = new ArrayList();

  public void load()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    load(false);
  }

  public void load(boolean paramBoolean)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    Trace.msgSecurityPrintln("mozilla.cert.loading", new Object[] { getName() });
    this.certs.clear();
    try
    {
      Class localClass1 = Class.forName("org.mozilla.jss.CryptoManager", true, ClassLoader.getSystemClassLoader());
      Object localObject1 = BrowserKeystore.getJSSCryptoManager();
      Method localMethod1 = localClass1.getMethod("getCACerts", null);
      Object[] arrayOfObject = (Object[])(Object[])localMethod1.invoke(localObject1, null);
      for (int i = 0; i < arrayOfObject.length; i++)
      {
        Class localClass2 = Class.forName("org.mozilla.jss.crypto.X509Certificate", true, ClassLoader.getSystemClassLoader());
        Method localMethod2 = localClass2.getMethod("getNickname", null);
        String str = (String)localMethod2.invoke(arrayOfObject[i], null);
        Class localClass3 = Class.forName("org.mozilla.jss.CryptoManager$CertUsage", true, ClassLoader.getSystemClassLoader());
        Class[] arrayOfClass = { String.class, Boolean.TYPE, localClass3 };
        Method localMethod3 = localClass1.getMethod("isCertValid", arrayOfClass);
        Boolean localBoolean1;
        if (isTrustedSigningCACertStore())
        {
          localObject2 = localClass3.getField("ObjectSigner");
          localObject3 = new Object[3];
          localObject3[0] = str;
          localObject3[1] = Boolean.FALSE;
          localObject3[2] = ((Field)localObject2).get(arrayOfObject[i]);
          localBoolean1 = (Boolean)localMethod3.invoke(localObject1, localObject3);
          Field localField = localClass3.getField("EmailSigner");
          localObject3[2] = localField.get(arrayOfObject[i]);
          Boolean localBoolean2 = (Boolean)localMethod3.invoke(localObject1, localObject3);
          if ((!localBoolean1.booleanValue()) && (!localBoolean2.booleanValue()))
            continue;
        }
        if (isTrustedSSLCACertStore())
        {
          localObject2 = localClass3.getField("SSLCA");
          localObject3 = new Object[3];
          localObject3[0] = str;
          localObject3[1] = Boolean.FALSE;
          localObject3[2] = ((Field)localObject2).get(arrayOfObject[i]);
          localBoolean1 = (Boolean)localMethod3.invoke(localObject1, localObject3);
          if (!localBoolean1.booleanValue())
            continue;
        }
        Object localObject2 = localClass2.getMethod("getEncoded", null);
        Object localObject3 = (byte[])(byte[])((Method)localObject2).invoke(arrayOfObject[i], null);
        generateCertificate(localObject3, this.certs);
      }
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
    Trace.msgSecurityPrintln("mozilla.cert.loaded", new Object[] { getName() });
  }

  public void save()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    throw new KeyStoreException("Cannot store certificate in Mozilla \"" + getName() + "\" certificate store.");
  }

  public boolean add(Certificate paramCertificate)
    throws KeyStoreException
  {
    return add(paramCertificate, false);
  }

  public boolean add(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException
  {
    throw new KeyStoreException("Cannot add certificate in Mozilla \"" + getName() + "\" certificate store.");
  }

  public boolean remove(Certificate paramCertificate)
    throws IOException, KeyStoreException
  {
    throw new KeyStoreException("Cannot remove certificate from Mozilla \"" + getName() + "\" certificate store.");
  }

  public boolean contains(Certificate paramCertificate)
    throws KeyStoreException
  {
    return contains(paramCertificate, false);
  }

  public boolean contains(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException
  {
    Trace.msgSecurityPrintln("mozilla.cert.instore", new Object[] { getName() });
    return this.certs.contains(paramCertificate);
  }

  public boolean verify(Certificate paramCertificate)
  {
    Trace.msgSecurityPrintln("mozilla.cert.canverify", new Object[] { getName() });
    Trace.msgSecurityPrintln("mozilla.cert.tobeverified", new Object[] { paramCertificate });
    Iterator localIterator = getCertificates().iterator();
    while (localIterator.hasNext())
    {
      X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
      Trace.msgSecurityPrintln("mozilla.cert.tobecompared", new Object[] { getName(), localX509Certificate });
      try
      {
        paramCertificate.verify(localX509Certificate.getPublicKey());
        Trace.msgSecurityPrintln("mozilla.cert.verify.ok", new Object[] { getName() });
        return true;
      }
      catch (GeneralSecurityException localGeneralSecurityException)
      {
      }
    }
    Trace.msgSecurityPrintln("mozilla.cert.verify.fail", new Object[] { getName() });
    return false;
  }

  public Collection getCertificates()
  {
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = this.certs.iterator();
    while (localIterator.hasNext())
    {
      Certificate localCertificate = (Certificate)localIterator.next();
      localArrayList.add(localCertificate);
    }
    return localArrayList;
  }

  private void generateCertificate(byte[] paramArrayOfByte, Collection paramCollection)
  {
    try
    {
      ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramArrayOfByte);
      CertificateFactory localCertificateFactory = CertificateFactory.getInstance("X.509");
      Collection localCollection = localCertificateFactory.generateCertificates(localByteArrayInputStream);
      Iterator localIterator = localCollection.iterator();
      while (localIterator.hasNext())
      {
        X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
        paramCollection.add(localX509Certificate);
      }
    }
    catch (CertificateException localCertificateException)
    {
      localCertificateException.printStackTrace();
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
  }

  protected abstract String getName();

  protected abstract boolean isTrustedSigningCACertStore();

  protected abstract boolean isTrustedSSLCACertStore();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.MozillaCertStore
 * JD-Core Version:    0.6.0
 */