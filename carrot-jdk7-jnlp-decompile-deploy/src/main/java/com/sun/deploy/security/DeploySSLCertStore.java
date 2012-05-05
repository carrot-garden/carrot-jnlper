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

public final class DeploySSLCertStore
  implements CertStore
{
  private static String _userFilename = null;
  private static String _systemFilename = null;
  private long _userLastModified = 0L;
  private long _sysLastModified = 0L;
  private KeyStore _deploymentUserCerts = CertUtils.createEmptyKeyStore();
  private KeyStore _deploymentSystemCerts = CertUtils.createEmptyKeyStore();
  private char[] keyPassphrase = new char[0];
  private boolean cancelFlag = false;
  private int certStoreType = 0;

  private DeploySSLCertStore(int paramInt)
  {
    this.certStoreType = paramInt;
  }

  public static CertStore getCertStore()
  {
    return new ImmutableCertStore(new DeploySSLCertStore(3));
  }

  public static CertStore getUserCertStore()
  {
    return new DeploySSLCertStore(1);
  }

  public static CertStore getSystemCertStore()
  {
    return new ImmutableCertStore(new DeploySSLCertStore(2));
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
        this._deploymentUserCerts = loadCertStore(_userFilename, paramBoolean);
        this._userLastModified = l;
      }
    }
    if (((this.certStoreType & 0x2) == 2) && (_systemFilename != null))
    {
      l = CertUtils.getFileLastModified(_systemFilename);
      if (l != this._sysLastModified)
      {
        this._deploymentSystemCerts = loadCertStore(_systemFilename, paramBoolean);
        this._sysLastModified = l;
      }
    }
  }

  private KeyStore loadCertStore(String paramString, boolean paramBoolean)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    Trace.msgSecurityPrintln("httpscertstore.cert.loading", new Object[] { paramString });
    File localFile = new File(paramString);
    KeyStore localKeyStore = KeyStore.getInstance("JKS");
    localKeyStore.load(null, null);
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(localFile, paramBoolean, localKeyStore)
      {
        private final File val$file;
        private final boolean val$integrityCheck;
        private final KeyStore val$keyStore;

        public Object run()
          throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
        {
          if (this.val$file.exists())
          {
            FileInputStream localFileInputStream = new FileInputStream(this.val$file);
            BufferedInputStream localBufferedInputStream = new BufferedInputStream(localFileInputStream);
            if (this.val$integrityCheck)
            {
              DeploySSLCertStore.access$002(DeploySSLCertStore.this, false);
              this.val$keyStore.load(localBufferedInputStream, new char[0]);
            }
            else
            {
              this.val$keyStore.load(localBufferedInputStream, null);
            }
            localBufferedInputStream.close();
            localFileInputStream.close();
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
          CredentialInfo localCredentialInfo = ToolkitStore.getUI().showPasswordDialog(null, ResourceManager.getMessage("password.dialog.title"), ResourceManager.getMessage("httpscertstore.password.dialog.text"), false, false, null, false, null);
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
    Trace.msgSecurityPrintln("httpscertstore.cert.loaded", new Object[] { paramString });
    return localKeyStore;
  }

  public void save()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    Trace.msgSecurityPrintln("httpscertstore.cert.saving", new Object[] { _userFilename });
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run()
          throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
        {
          File localFile = new File(DeploySSLCertStore._userFilename);
          localFile.getParentFile().mkdirs();
          FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
          BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(localFileOutputStream);
          DeploySSLCertStore.this._deploymentUserCerts.store(localBufferedOutputStream, DeploySSLCertStore.this.keyPassphrase);
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
    Trace.msgSecurityPrintln("httpscertstore.cert.saved", new Object[] { _userFilename });
  }

  public boolean add(Certificate paramCertificate)
    throws KeyStoreException
  {
    return add(paramCertificate, false);
  }

  public boolean add(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException
  {
    Trace.msgSecurityPrintln("httpscertstore.cert.adding");
    if (this.cancelFlag)
      return false;
    String str1 = this._deploymentUserCerts.getCertificateAlias(paramCertificate);
    if (str1 == null)
    {
      Random localRandom = new Random();
      int i = 0;
      String str2 = null;
      do
      {
        str2 = "deploymentuserhttpscert" + localRandom.nextLong();
        Certificate localCertificate = this._deploymentUserCerts.getCertificate(str2);
        if (localCertificate != null)
          continue;
        i = 1;
      }
      while (i == 0);
      this._deploymentUserCerts.setCertificateEntry(str2, paramCertificate);
      Trace.msgSecurityPrintln("httpscertstore.cert.added", new Object[] { str2 });
    }
    return true;
  }

  public boolean remove(Certificate paramCertificate)
    throws IOException, KeyStoreException
  {
    Trace.msgSecurityPrintln("httpscertstore.cert.removing");
    if (this.cancelFlag)
      return false;
    String str = this._deploymentUserCerts.getCertificateAlias(paramCertificate);
    if (str != null)
      this._deploymentUserCerts.deleteEntry(str);
    Trace.msgSecurityPrintln("httpscertstore.cert.removed", new Object[] { str });
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
    Trace.msgSecurityPrintln("httpscertstore.cert.instore");
    String str = null;
    str = this._deploymentSystemCerts.getCertificateAlias(paramCertificate);
    if (str != null)
      return true;
    str = this._deploymentUserCerts.getCertificateAlias(paramCertificate);
    return str != null;
  }

  public boolean verify(Certificate paramCertificate)
  {
    Trace.msgSecurityPrintln("httpscertstore.cert.canverify");
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
    Trace.msgSecurityPrintln("httpscertstore.cert.getcertificates");
    ArrayList localArrayList = new ArrayList();
    KeyStore localKeyStore = null;
    if (paramInt == 1)
      localKeyStore = this._deploymentUserCerts;
    else
      localKeyStore = this._deploymentSystemCerts;
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
    _userFilename = Config.getUserTrustedHttpsCertificateFile();
    _systemFilename = Config.getSystemTrustedHttpsCertificateFile();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.DeploySSLCertStore
 * JD-Core Version:    0.6.0
 */