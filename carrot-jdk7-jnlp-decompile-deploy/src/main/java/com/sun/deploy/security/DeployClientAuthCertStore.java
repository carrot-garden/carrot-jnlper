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
import java.security.Key;
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
import javax.swing.JDialog;

public final class DeployClientAuthCertStore
  implements CertStore
{
  private static JDialog myParent = null;
  private static String _userFilename = null;
  private static String _systemFilename = null;
  private long _userLastModified = 0L;
  private long _sysLastModified = 0L;
  private KeyStore _deploymentUserClientCerts = CertUtils.createEmptyKeyStore();
  private KeyStore _deploymentSystemClientCerts = CertUtils.createEmptyKeyStore();
  private char[] keyPass = null;
  private boolean cancelFlag = false;
  private int certStoreType = 0;

  private DeployClientAuthCertStore(JDialog paramJDialog, int paramInt)
  {
    myParent = paramJDialog;
    this.certStoreType = paramInt;
  }

  public static CertStore getCertStore(JDialog paramJDialog)
  {
    return new ImmutableCertStore(new DeployClientAuthCertStore(paramJDialog, 3));
  }

  public static DeployClientAuthCertStore getUserCertStore(JDialog paramJDialog)
  {
    return new DeployClientAuthCertStore(paramJDialog, 1);
  }

  public static CertStore getSystemCertStore(JDialog paramJDialog)
  {
    return new ImmutableCertStore(new DeployClientAuthCertStore(paramJDialog, 2));
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
        this._deploymentUserClientCerts = loadCertStore(_userFilename, paramBoolean);
        this._userLastModified = l;
      }
    }
    if (((this.certStoreType & 0x2) == 2) && (_systemFilename != null))
    {
      l = CertUtils.getFileLastModified(_systemFilename);
      if (l != this._sysLastModified)
      {
        this._deploymentSystemClientCerts = loadCertStore(_systemFilename, paramBoolean);
        this._sysLastModified = l;
      }
    }
  }

  private KeyStore loadCertStore(String paramString, boolean paramBoolean)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    Trace.msgSecurityPrintln("clientauthcertstore.cert.loading", new Object[] { paramString });
    KeyStore localKeyStore = KeyStore.getInstance("JKS");
    localKeyStore.load(null, null);
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramString, paramBoolean, localKeyStore)
      {
        private final String val$filename;
        private final boolean val$integrityCheck;
        private final KeyStore val$keyStore;

        public Object run()
          throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
        {
          File localFile = new File(this.val$filename);
          if (localFile.exists())
          {
            FileInputStream localFileInputStream = new FileInputStream(localFile);
            BufferedInputStream localBufferedInputStream = new BufferedInputStream(localFileInputStream);
            if (this.val$integrityCheck)
            {
              DeployClientAuthCertStore.access$002(DeployClientAuthCertStore.this, DeployClientAuthCertStore.this.getPasswordDialog());
              if (DeployClientAuthCertStore.this.keyPass != null)
              {
                DeployClientAuthCertStore.access$202(DeployClientAuthCertStore.this, false);
                this.val$keyStore.load(localBufferedInputStream, DeployClientAuthCertStore.this.keyPass);
              }
              else
              {
                DeployClientAuthCertStore.access$202(DeployClientAuthCertStore.this, true);
              }
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
        throw ((IOException)localException);
      if ((localException instanceof CertificateException))
        throw ((CertificateException)localException);
      if ((localException instanceof KeyStoreException))
        throw ((KeyStoreException)localException);
      if ((localException instanceof NoSuchAlgorithmException))
        throw ((NoSuchAlgorithmException)localException);
      Trace.securityPrintException(localPrivilegedActionException);
    }
    Trace.msgSecurityPrintln("clientauthcertstore.cert.loaded", new Object[] { paramString });
    return localKeyStore;
  }

  public void save()
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    Trace.msgSecurityPrintln("clientauthcertstore.cert.saving", new Object[] { _userFilename });
    if (this.keyPass != null)
      try
      {
        AccessController.doPrivileged(new PrivilegedExceptionAction()
        {
          public Object run()
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
          {
            File localFile = new File(DeployClientAuthCertStore._userFilename);
            localFile.getParentFile().mkdirs();
            FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
            BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(localFileOutputStream);
            DeployClientAuthCertStore.this._deploymentUserClientCerts.store(localBufferedOutputStream, DeployClientAuthCertStore.this.keyPass);
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
    Trace.msgSecurityPrintln("clientauthcertstore.cert.saved", new Object[] { _userFilename });
  }

  public boolean add(Certificate paramCertificate)
    throws KeyStoreException
  {
    return add(paramCertificate, false);
  }

  public boolean add(Certificate paramCertificate, boolean paramBoolean)
    throws KeyStoreException
  {
    Trace.msgSecurityPrintln("clientauthcertstore.cert.adding");
    String str1 = this._deploymentUserClientCerts.getCertificateAlias(paramCertificate);
    if (str1 == null)
    {
      Random localRandom = new Random();
      int i = 0;
      String str2 = null;
      do
      {
        str2 = "clientauthcert" + localRandom.nextLong();
        Certificate localCertificate = this._deploymentUserClientCerts.getCertificate(str2);
        if (localCertificate != null)
          continue;
        i = 1;
      }
      while (i == 0);
      this._deploymentUserClientCerts.setCertificateEntry(str2, paramCertificate);
      Trace.msgSecurityPrintln("clientauthcertstore.cert.added", new Object[] { str2 });
    }
    return true;
  }

  public boolean addCertKey(Certificate[] paramArrayOfCertificate, Key paramKey)
    throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
  {
    String str1 = this._deploymentUserClientCerts.getCertificateAlias(paramArrayOfCertificate[0]);
    if (str1 == null)
    {
      Random localRandom = new Random();
      int i = 0;
      String str2 = null;
      Object localObject1;
      do
      {
        str2 = "clientauthcert" + localRandom.nextLong();
        localObject1 = this._deploymentUserClientCerts.getCertificate(str2);
        if (localObject1 != null)
          continue;
        i = 1;
      }
      while (i == 0);
      if ((this.keyPass == null) && (!this.cancelFlag))
        this.keyPass = getPasswordDialog();
      if (this.keyPass != null)
      {
        localObject1 = new File(_userFilename);
        if (((File)localObject1).exists())
        {
          FileInputStream localFileInputStream = new FileInputStream(_userFilename);
          BufferedInputStream localBufferedInputStream = new BufferedInputStream(localFileInputStream);
          try
          {
            this._deploymentUserClientCerts.load(localBufferedInputStream, this.keyPass);
            this._deploymentUserClientCerts.setKeyEntry(str2, paramKey, this.keyPass, paramArrayOfCertificate);
          }
          catch (Exception localException)
          {
            Trace.securityPrintException(localException);
            int j = 0;
            return j;
          }
          finally
          {
            localBufferedInputStream.close();
            localFileInputStream.close();
          }
        }
        else
        {
          this._deploymentUserClientCerts.setKeyEntry(str2, paramKey, this.keyPass, paramArrayOfCertificate);
        }
        return true;
      }
      return false;
    }
    return false;
  }

  public boolean remove(Certificate paramCertificate)
    throws IOException, KeyStoreException
  {
    Certificate[] arrayOfCertificate = new Certificate[1];
    arrayOfCertificate[0] = paramCertificate;
    return remove(arrayOfCertificate);
  }

  public boolean remove(Certificate[] paramArrayOfCertificate)
    throws IOException, KeyStoreException
  {
    Trace.msgSecurityPrintln("clientauthcertstore.cert.removing");
    String str = null;
    if (this.keyPass != null)
      try
      {
        File localFile = new File(_userFilename);
        KeyStore localKeyStore = KeyStore.getInstance("JKS");
        localKeyStore.load(new BufferedInputStream(new FileInputStream(localFile)), this.keyPass);
        for (int i = 0; i < paramArrayOfCertificate.length; i++)
        {
          str = this._deploymentUserClientCerts.getCertificateAlias(paramArrayOfCertificate[i]);
          if (str == null)
            continue;
          this._deploymentUserClientCerts.deleteEntry(str);
        }
      }
      catch (Exception localException)
      {
        if ((localException instanceof IOException))
          throw ((IOException)localException);
        if ((localException instanceof KeyStoreException))
          throw ((KeyStoreException)localException);
        Trace.securityPrintException(localException);
        return false;
      }
    else
      return false;
    Trace.msgSecurityPrintln("clientauthcertstore.cert.removed", new Object[] { str });
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
    Trace.msgSecurityPrintln("clientauthcertstore.cert.instore");
    String str = null;
    str = this._deploymentSystemClientCerts.getCertificateAlias(paramCertificate);
    if (str != null)
      return true;
    str = this._deploymentUserClientCerts.getCertificateAlias(paramCertificate);
    return str != null;
  }

  public boolean verify(Certificate paramCertificate)
  {
    Trace.msgSecurityPrintln("clientauthcertstore.cert.canverify");
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
    Trace.msgSecurityPrintln("clientauthcertstore.cert.getcertificates");
    KeyStore localKeyStore = null;
    if (paramInt == 1)
      localKeyStore = this._deploymentUserClientCerts;
    else
      localKeyStore = this._deploymentSystemClientCerts;
    Enumeration localEnumeration = localKeyStore.aliases();
    TreeSet localTreeSet = new TreeSet();
    while (localEnumeration.hasMoreElements())
    {
      localObject = (String)localEnumeration.nextElement();
      if (localKeyStore.isKeyEntry((String)localObject))
        localTreeSet.add(localObject);
    }
    Object localObject = new ArrayList();
    Iterator localIterator = localTreeSet.iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      Certificate[] arrayOfCertificate = localKeyStore.getCertificateChain(str);
      if (arrayOfCertificate != null)
        ((Collection)localObject).add(arrayOfCertificate);
    }
    return (Collection)localObject;
  }

  private char[] getPasswordDialog()
  {
    CredentialInfo localCredentialInfo = ToolkitStore.getUI().showPasswordDialog(myParent, getMessage("password.dialog.title"), getMessage("clientauth.user.password.dialog.text"), false, false, null, false, null);
    if (localCredentialInfo == null)
      return null;
    return localCredentialInfo.getPassword();
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  static
  {
    _userFilename = Config.getUserClientAuthCertFile();
    _systemFilename = Config.getSystemClientAuthCertFile();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.DeployClientAuthCertStore
 * JD-Core Version:    0.6.0
 */