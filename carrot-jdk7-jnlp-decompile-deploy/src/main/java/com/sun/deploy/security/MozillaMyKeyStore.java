package com.sun.deploy.security;

import com.sun.deploy.trace.Trace;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.SecurityPermission;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

public final class MozillaMyKeyStore extends KeyStoreSpi
{
  private Collection keyEntries = new ArrayList();

  public Key engineGetKey(String paramString, char[] paramArrayOfChar)
    throws NoSuchAlgorithmException, UnrecoverableKeyException
  {
    if ((paramArrayOfChar != null) && (paramArrayOfChar.length > 0))
      throw new UnrecoverableKeyException("Password is not required for Mozilla MY keystore.");
    if (!engineIsKeyEntry(paramString))
      return null;
    Iterator localIterator = this.keyEntries.iterator();
    while (localIterator.hasNext())
    {
      KeyEntry localKeyEntry = (KeyEntry)localIterator.next();
      if (paramString.equals(localKeyEntry.getAlias()))
        return localKeyEntry.getPrivateKey();
    }
    return null;
  }

  public Certificate[] engineGetCertificateChain(String paramString)
  {
    Iterator localIterator = this.keyEntries.iterator();
    while (localIterator.hasNext())
    {
      KeyEntry localKeyEntry = (KeyEntry)localIterator.next();
      if (paramString.equals(localKeyEntry.getAlias()))
      {
        X509Certificate[] arrayOfX509Certificate1 = localKeyEntry.getCertificateChain();
        X509Certificate[] arrayOfX509Certificate2 = (X509Certificate[])(X509Certificate[])arrayOfX509Certificate1.clone();
        return arrayOfX509Certificate2;
      }
    }
    return null;
  }

  public Certificate engineGetCertificate(String paramString)
  {
    Iterator localIterator = this.keyEntries.iterator();
    while (localIterator.hasNext())
    {
      KeyEntry localKeyEntry = (KeyEntry)localIterator.next();
      if (paramString.equals(localKeyEntry.getAlias()))
      {
        X509Certificate[] arrayOfX509Certificate = localKeyEntry.getCertificateChain();
        return arrayOfX509Certificate[0];
      }
    }
    return null;
  }

  public Date engineGetCreationDate(String paramString)
  {
    return new Date();
  }

  public void engineSetKeyEntry(String paramString, Key paramKey, char[] paramArrayOfChar, Certificate[] paramArrayOfCertificate)
    throws KeyStoreException
  {
    throw new KeyStoreException("Cannot assign the given key to the given alias.");
  }

  public void engineSetKeyEntry(String paramString, byte[] paramArrayOfByte, Certificate[] paramArrayOfCertificate)
    throws KeyStoreException
  {
    throw new KeyStoreException("Cannot assign the given key to the given alias.");
  }

  public void engineSetCertificateEntry(String paramString, Certificate paramCertificate)
    throws KeyStoreException
  {
    throw new KeyStoreException("Cannot assign the given certificate to the given alias.");
  }

  public void engineDeleteEntry(String paramString)
    throws KeyStoreException
  {
    throw new KeyStoreException("Mozilla does not support alias removal.");
  }

  public Enumeration engineAliases()
  {
    Iterator localIterator = this.keyEntries.iterator();
    return new Enumeration(localIterator)
    {
      private final Iterator val$iter;

      public boolean hasMoreElements()
      {
        return this.val$iter.hasNext();
      }

      public Object nextElement()
      {
        MozillaMyKeyStore.KeyEntry localKeyEntry = (MozillaMyKeyStore.KeyEntry)this.val$iter.next();
        return localKeyEntry.getAlias();
      }
    };
  }

  public boolean engineContainsAlias(String paramString)
  {
    Enumeration localEnumeration = engineAliases();
    while (localEnumeration.hasMoreElements())
    {
      String str = (String)localEnumeration.nextElement();
      if (str.equals(paramString))
        return true;
    }
    return false;
  }

  public int engineSize()
  {
    return this.keyEntries.size();
  }

  public boolean engineIsKeyEntry(String paramString)
  {
    return (paramString.startsWith("MozillaJSSRSAPrivateKey")) || (paramString.startsWith("MozillaJSSDSAPrivateKey"));
  }

  public boolean engineIsCertificateEntry(String paramString)
  {
    return false;
  }

  public String engineGetCertificateAlias(Certificate paramCertificate)
  {
    Iterator localIterator = this.keyEntries.iterator();
    while (localIterator.hasNext())
    {
      KeyEntry localKeyEntry = (KeyEntry)localIterator.next();
      if ((localKeyEntry.certChain != null) && (localKeyEntry.certChain[0].equals(paramCertificate)))
        return localKeyEntry.getAlias();
    }
    return null;
  }

  public void engineStore(OutputStream paramOutputStream, char[] paramArrayOfChar)
    throws IOException, NoSuchAlgorithmException, CertificateException
  {
    throw new IOException("Mozilla cert store cannot be stored into stream.");
  }

  public void engineLoad(InputStream paramInputStream, char[] paramArrayOfChar)
    throws IOException, NoSuchAlgorithmException, CertificateException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPermission(new SecurityPermission("authProvider.SunDeploy-MozillaJSS"));
    if (paramInputStream != null)
      throw new IOException("Mozilla cert store cannot be loaded from stream.");
    this.keyEntries.clear();
    try
    {
      Class localClass = Class.forName("org.mozilla.jss.CryptoManager", true, ClassLoader.getSystemClassLoader());
      Object localObject1 = BrowserKeystore.getJSSCryptoManager();
      Method localMethod1 = localClass.getMethod("getInternalKeyStorageToken", null);
      Object localObject2 = localMethod1.invoke(localObject1, null);
      generateKeyAndCertificateFromToken(localObject1, localObject2);
      Method localMethod2 = localClass.getMethod("getExternalTokens", null);
      Enumeration localEnumeration = (Enumeration)localMethod2.invoke(localObject1, null);
      while (localEnumeration.hasMoreElements())
      {
        Object localObject3 = localEnumeration.nextElement();
        generateKeyAndCertificateFromToken(localObject1, localObject3);
      }
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
  }

  private void generateKeyAndCertificateFromToken(Object paramObject1, Object paramObject2)
    throws Throwable
  {
    Class localClass1 = Class.forName("org.mozilla.jss.CryptoManager", true, ClassLoader.getSystemClassLoader());
    Class localClass2 = Class.forName("org.mozilla.jss.crypto.CryptoToken", true, ClassLoader.getSystemClassLoader());
    Method localMethod1 = localClass2.getMethod("getCryptoStore", null);
    Object localObject1 = localMethod1.invoke(paramObject2, null);
    Class localClass3 = Class.forName("org.mozilla.jss.crypto.CryptoStore", true, ClassLoader.getSystemClassLoader());
    Method localMethod2 = localClass3.getMethod("getCertificates", null);
    Object[] arrayOfObject1 = (Object[])(Object[])localMethod2.invoke(localObject1, null);
    for (int i = 0; i < arrayOfObject1.length; i++)
    {
      Class localClass4 = Class.forName("org.mozilla.jss.crypto.X509Certificate", true, ClassLoader.getSystemClassLoader());
      Method localMethod3 = localClass4.getMethod("getNickname", null);
      String str = (String)localMethod3.invoke(arrayOfObject1[i], null);
      if (str == null)
        continue;
      Class localClass5 = Class.forName("org.mozilla.jss.CryptoManager$CertUsage", true, ClassLoader.getSystemClassLoader());
      Class[] arrayOfClass1 = { String.class, Boolean.TYPE, localClass5 };
      Method localMethod4 = localClass1.getMethod("isCertValid", arrayOfClass1);
      Field localField1 = localClass5.getField("SSLClient");
      Object[] arrayOfObject2 = new Object[3];
      arrayOfObject2[0] = str;
      arrayOfObject2[1] = Boolean.FALSE;
      arrayOfObject2[2] = localField1.get(arrayOfObject1[i]);
      Boolean localBoolean = (Boolean)localMethod4.invoke(paramObject1, arrayOfObject2);
      if (!localBoolean.booleanValue())
        continue;
      try
      {
        Class[] arrayOfClass2 = { localClass4 };
        Method localMethod5 = localClass1.getMethod("findPrivKeyByCert", arrayOfClass2);
        Object[] arrayOfObject3 = { arrayOfObject1[i] };
        Object localObject2 = localMethod5.invoke(paramObject1, arrayOfObject3);
        Class localClass6 = Class.forName("org.mozilla.jss.crypto.PrivateKey", true, ClassLoader.getSystemClassLoader());
        Method localMethod6 = localClass6.getMethod("getType", null);
        Object localObject3 = localMethod6.invoke(localObject2, null);
        Class localClass7 = Class.forName("org.mozilla.jss.crypto.PrivateKey$Type", true, ClassLoader.getSystemClassLoader());
        Field localField2 = localClass7.getField("RSA");
        Field localField3 = localClass7.getField("DSA");
        Object localObject4 = localField2.get(null);
        Object localObject5 = localField3.get(null);
        if ((!localObject3.equals(localObject4)) && (!localObject3.equals(localObject5)))
          continue;
        Class[] arrayOfClass3 = { localClass4 };
        Method localMethod7 = localClass1.getMethod("buildCertificateChain", arrayOfClass3);
        Object[] arrayOfObject4 = { arrayOfObject1[i] };
        Object[] arrayOfObject5 = (Object[])(Object[])localMethod7.invoke(paramObject1, arrayOfObject4);
        ArrayList localArrayList = new ArrayList();
        for (int j = 0; j < arrayOfObject5.length; j++)
        {
          localObject6 = localClass4.getMethod("getEncoded", null);
          Object localObject7 = ((Method)localObject6).invoke(arrayOfObject5[j], null);
          generateCertificate((byte[])(byte[])localObject7, localArrayList);
        }
        Method localMethod8 = localClass6.getMethod("getStrength", null);
        Object localObject6 = (Integer)localMethod8.invoke(localObject2, null);
        if (localObject3.equals(localObject4))
          generateRSAKeyAndCertificateChain(localObject2, ((Integer)localObject6).intValue(), localArrayList, this.keyEntries);
        else
          generateDSAKeyAndCertificateChain(localObject2, ((Integer)localObject6).intValue(), localArrayList, this.keyEntries);
      }
      catch (Throwable localThrowable)
      {
        Trace.msgSecurityPrintln("mozillamykeystore.priv.notfound", new Object[] { str });
      }
    }
  }

  private void generateRSAKeyAndCertificateChain(Object paramObject, int paramInt, Collection paramCollection1, Collection paramCollection2)
  {
    try
    {
      X509Certificate[] arrayOfX509Certificate = new X509Certificate[paramCollection1.size()];
      int i = 0;
      Object localObject = paramCollection1.iterator();
      while (((Iterator)localObject).hasNext())
      {
        arrayOfX509Certificate[i] = ((X509Certificate)((Iterator)localObject).next());
        i++;
      }
      localObject = new KeyEntry(new MozillaJSSRSAPrivateKey(paramObject, paramInt), arrayOfX509Certificate);
      paramCollection2.add(localObject);
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
  }

  private void generateDSAKeyAndCertificateChain(Object paramObject, int paramInt, Collection paramCollection1, Collection paramCollection2)
  {
    try
    {
      X509Certificate[] arrayOfX509Certificate = new X509Certificate[paramCollection1.size()];
      int i = 0;
      Object localObject = paramCollection1.iterator();
      while (((Iterator)localObject).hasNext())
      {
        arrayOfX509Certificate[i] = ((X509Certificate)((Iterator)localObject).next());
        i++;
      }
      localObject = new KeyEntry(new MozillaJSSDSAPrivateKey(paramObject, paramInt), arrayOfX509Certificate);
      paramCollection2.add(localObject);
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
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

  protected String getName()
  {
    return "MY";
  }

  class KeyEntry
  {
    private MozillaJSSPrivateKey privateKey;
    private X509Certificate[] certChain;

    KeyEntry(MozillaJSSPrivateKey paramArrayOfX509Certificate, X509Certificate[] arg3)
    {
      this.privateKey = paramArrayOfX509Certificate;
      Object localObject;
      this.certChain = localObject;
    }

    String getAlias()
    {
      return this.privateKey.toString();
    }

    Key getPrivateKey()
    {
      return this.privateKey;
    }

    X509Certificate[] getCertificateChain()
    {
      return this.certChain;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.MozillaMyKeyStore
 * JD-Core Version:    0.6.0
 */