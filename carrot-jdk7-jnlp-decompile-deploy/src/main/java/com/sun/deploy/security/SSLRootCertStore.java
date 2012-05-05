package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

public final class SSLRootCertStore
  implements CertStore
{
  private static String _userFilename = null;
  private static String _systemFilename = null;
  private long _userLastModified = 0L;
  private long _sysLastModified = 0L;
  private KeyStore _deploymentUserHttpsCACerts = CertUtils.createEmptyKeyStore();
  private KeyStore _deploymentSystemHttpsCACerts = CertUtils.createEmptyKeyStore();
  private char[] keyPassphrase = new char[0];
  private boolean cancelFlag = false;
  private int certStoreType = 0;

  private SSLRootCertStore(int paramInt)
  {
    this.certStoreType = paramInt;
  }

  public static CertStore getCertStore()
  {
    return new ImmutableCertStore(new SSLRootCertStore(3));
  }

  public static CertStore getUserCertStore()
  {
    return new SSLRootCertStore(1);
  }

  public static CertStore getSystemCertStore()
  {
    return new ImmutableCertStore(new SSLRootCertStore(2));
  }

  public void load()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    load(false);
  }

  public void load(boolean paramBoolean)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    long l;
    if (((this.certStoreType & 0x1) == 1) && (_userFilename != null))
    {
      l = CertUtils.getFileLastModified(_userFilename);
      if (l != this._userLastModified)
      {
        this._deploymentUserHttpsCACerts = loadCertStore(_userFilename, paramBoolean);
        this._userLastModified = l;
      }
    }
    if (((this.certStoreType & 0x2) == 2) && (_systemFilename != null))
    {
      l = CertUtils.getFileLastModified(_systemFilename);
      if (l != this._sysLastModified)
      {
        this._deploymentSystemHttpsCACerts = loadCertStore(_systemFilename, paramBoolean);
        this._sysLastModified = l;
      }
    }
  }

  private KeyStore loadCertStore(String paramString, boolean paramBoolean)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    Trace.msgSecurityPrintln("roothttpscertstore.cert.loading", new Object[] { paramString });
    File localFile = new File(paramString);
    KeyStore localKeyStore = KeyStore.getInstance("JKS");
    localKeyStore.load(null, null);
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(localFile, paramBoolean, localKeyStore, paramString)
      {
        private final File val$file;
        private final boolean val$integrityCheck;
        private final KeyStore val$keyStore;
        private final String val$filename;

        public Object run()
          throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
        {
          if (this.val$file.exists())
          {
            FileInputStream localFileInputStream = new FileInputStream(this.val$file);
            BufferedInputStream localBufferedInputStream = new BufferedInputStream(localFileInputStream);
            if (this.val$integrityCheck)
            {
              SSLRootCertStore.access$002(SSLRootCertStore.this, false);
              this.val$keyStore.load(localBufferedInputStream, new char[0]);
            }
            else
            {
              this.val$keyStore.load(localBufferedInputStream, null);
            }
            localBufferedInputStream.close();
            localFileInputStream.close();
          }
          else
          {
            Trace.msgSecurityPrintln("roothttpscertstore.cert.noload", new Object[] { this.val$filename });
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException))
      {
        if (paramBoolean)
        {
          FileInputStream localFileInputStream = new FileInputStream(localFile);
          BufferedInputStream localBufferedInputStream = new BufferedInputStream(localFileInputStream);
          CredentialInfo localCredentialInfo = ToolkitStore.getUI().showPasswordDialog(null, ResourceManager.getMessage("password.dialog.title"), ResourceManager.getMessage("roothttpscertstore.password.dialog.text"), false, false, null, false, null);
          if (localCredentialInfo != null)
          {
            this.cancelFlag = false;
            this.keyPassphrase = localCredentialInfo.getPassword();
            localKeyStore.load(localBufferedInputStream, this.keyPassphrase);
          }
          else
          {
            this.cancelFlag = true;
          }
          localBufferedInputStream.close();
          localFileInputStream.close();
        }
        else
        {
          throw ((IOException)localException);
        }
      }
      else
      {
        if ((localException instanceof CertificateException))
          throw ((CertificateException)localException);
        if ((localException instanceof KeyStoreException))
          throw ((KeyStoreException)localException);
        if ((localException instanceof NoSuchAlgorithmException))
          throw ((NoSuchAlgorithmException)localException);
        Trace.securityPrintException(localPrivilegedActionException);
      }
    }
    Trace.msgSecurityPrintln("roothttpscertstore.cert.loaded", new Object[] { paramString });
    return localKeyStore;
  }

  public void save()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    Trace.msgSecurityPrintln("roothttpscertstore.cert.saving", new Object[] { _userFilename });
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run()
          throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
        {
          File localFile = new File(SSLRootCertStore._userFilename);
          localFile.getParentFile().mkdirs();
          FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
          BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(localFileOutputStream);
          SSLRootCertStore.this._deploymentUserHttpsCACerts.store(localBufferedOutputStream, SSLRootCertStore.this.keyPassphrase);
          localBufferedOutputStream.close();
          localFileOutputStream.close();
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if ((localException instanceof IOException))
        throw ((IOException)localException);
      if ((localException instanceof CertificateException))
        throw ((CertificateException)localException);
      if ((localException instanceof KeyStoreException))
        throw ((KeyStoreException)localException);
      if ((localException instanceof NoSuchAlgorithmException))
        throw ((NoSuchAlgorithmException)localException);
      Trace.securityPrintException(localPrivilegedActionException);
    }
    Trace.msgSecurityPrintln("roothttpscertstore.cert.saved", new Object[] { _userFilename });
  }

  public boolean add(Certificate paramCertificate)
    throws KeyStoreException
  {
    return add(paramCertificate, false);
  }

  public boolean add(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException
  {
    Trace.msgSecurityPrintln("roothttpscertstore.cert.adding");
    if (this.cancelFlag)
      return false;
    String str1 = this._deploymentUserHttpsCACerts.getCertificateAlias(paramCertificate);
    if (str1 == null)
    {
      Random localRandom = new Random();
      int i = 0;
      String str2 = null;
      do
      {
        str2 = "userhttpscacert" + localRandom.nextLong();
        Certificate localCertificate = this._deploymentUserHttpsCACerts.getCertificate(str2);
        if (localCertificate != null)
          continue;
        i = 1;
      }
      while (i == 0);
      this._deploymentUserHttpsCACerts.setCertificateEntry(str2, paramCertificate);
      Trace.msgSecurityPrintln("roothttpscertstore.cert.added", new Object[] { str2 });
    }
    return true;
  }

  public boolean remove(Certificate paramCertificate)
    throws IOException, KeyStoreException
  {
    Trace.msgSecurityPrintln("roothttpscertstore.cert.removing");
    if (this.cancelFlag)
      return false;
    String str = this._deploymentUserHttpsCACerts.getCertificateAlias(paramCertificate);
    if (str != null)
      this._deploymentUserHttpsCACerts.deleteEntry(str);
    Trace.msgSecurityPrintln("rrothttpscertstore.cert.removed", new Object[] { str });
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
    Trace.msgSecurityPrintln("roothttpscertstore.cert.instore");
    String str = null;
    str = this._deploymentSystemHttpsCACerts.getCertificateAlias(paramCertificate);
    if (str != null)
      return true;
    str = this._deploymentUserHttpsCACerts.getCertificateAlias(paramCertificate);
    return str != null;
  }

  public boolean verify(Certificate paramCertificate)
    throws KeyStoreException
  {
    Trace.msgSecurityPrintln("roothttpscertstore.cert.canverify");
    Trace.msgSecurityPrintln("roothttpscertstore.cert.tobeverified", new Object[] { paramCertificate });
    StringBuffer localStringBuffer = new StringBuffer();
    Enumeration localEnumeration1 = this._deploymentSystemHttpsCACerts.aliases();
    Enumeration localEnumeration2 = this._deploymentUserHttpsCACerts.aliases();
    while ((localEnumeration1.hasMoreElements()) || (localEnumeration2.hasMoreElements()))
    {
      String str;
      Certificate localCertificate;
      if (localEnumeration1.hasMoreElements())
      {
        str = (String)localEnumeration1.nextElement();
        localCertificate = this._deploymentSystemHttpsCACerts.getCertificate(str);
      }
      else
      {
        str = (String)localEnumeration2.nextElement();
        localCertificate = this._deploymentUserHttpsCACerts.getCertificate(str);
      }
      Trace.msgSecurityPrintln("roothttpscertstore.cert.tobecompared", new Object[] { localCertificate });
      try
      {
        paramCertificate.verify(localCertificate.getPublicKey());
        Trace.msgSecurityPrintln("roothttpscertstore.cert.verify.ok");
        return true;
      }
      catch (GeneralSecurityException localGeneralSecurityException)
      {
      }
    }
    Trace.msgSecurityPrintln("roothttpscertstore.cert.verify.fail");
    return false;
  }

  public Collection getCertificates()
    throws KeyStoreException
  {
    HashSet localHashSet = new HashSet();
    if ((this.certStoreType & 0x1) == 1)
      localHashSet.addAll(getCertificates(1));
    if ((this.certStoreType & 0x2) == 2)
      localHashSet.addAll(getCertificates(2));
    return localHashSet;
  }

  private Collection getCertificates(int paramInt)
    throws KeyStoreException
  {
    Trace.msgSecurityPrintln("roothttpscertstore.cert.getcertificates");
    ArrayList localArrayList = new ArrayList();
    KeyStore localKeyStore = null;
    if (paramInt == 1)
      localKeyStore = this._deploymentUserHttpsCACerts;
    else
      localKeyStore = this._deploymentSystemHttpsCACerts;
    Enumeration localEnumeration = localKeyStore.aliases();
    TreeSet localTreeSet = new TreeSet();
    while (localEnumeration.hasMoreElements())
    {
      localObject = (String)localEnumeration.nextElement();
      localTreeSet.add(localObject);
    }
    Object localObject = localTreeSet.iterator();
    while (((Iterator)localObject).hasNext())
    {
      String str = (String)((Iterator)localObject).next();
      Certificate localCertificate = localKeyStore.getCertificate(str);
      localArrayList.add(localCertificate);
    }
    return (Collection)localArrayList;
  }

  static
  {
    _userFilename = Config.getUserSSLRootCertificateFile();
    if (Config.isJavaVersionAtLeast15())
      _systemFilename = Config.getSystemSSLRootCertificateFile();
    else
      _systemFilename = Config.getOldSystemSSLRootCertificateFile();
    if ((_systemFilename == null) || (!new File(_systemFilename).exists()))
      if (Config.isJavaVersionAtLeast15())
        _systemFilename = Config.getSystemRootCertificateFile();
      else
        _systemFilename = Config.getOldSystemRootCertificateFile();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.SSLRootCertStore
 * JD-Core Version:    0.6.0
 */