package com.sun.deploy.panel;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.security.CertStore;
import com.sun.deploy.security.DeployClientAuthCertStore;
import com.sun.deploy.security.DeploySSLCertStore;
import com.sun.deploy.security.DeploySigningCertStore;
import com.sun.deploy.security.RootCertStore;
import com.sun.deploy.security.SSLRootCertStore;
import com.sun.deploy.trace.Trace;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.swing.JDialog;

class CertificatesInfo
{
  private LinkedHashMap activeTrustedCertsMap = new LinkedHashMap();
  private LinkedHashMap activeHttpsCertsMap = new LinkedHashMap();
  private LinkedHashMap activeRootCACertsMap = new LinkedHashMap();
  private LinkedHashMap activeHttpsRootCACertsMap = new LinkedHashMap();
  private LinkedHashMap activeClientAuthCertsMap = new LinkedHashMap();
  private LinkedHashMap activeSysTrustedCertsMap = new LinkedHashMap();
  private LinkedHashMap activeSysHttpsCertsMap = new LinkedHashMap();
  private LinkedHashMap activeSysRootCACertsMap = new LinkedHashMap();
  private LinkedHashMap activeSysHttpsRootCACertsMap = new LinkedHashMap();
  private LinkedHashMap activeSysClientAuthCertsMap = new LinkedHashMap();

  public CertificatesInfo()
  {
    reset();
  }

  public Collection getTrustedCertificates(int paramInt)
  {
    if (paramInt == 0)
      return this.activeTrustedCertsMap.keySet();
    return this.activeSysTrustedCertsMap.keySet();
  }

  public Collection getHttpsCertificates(int paramInt)
  {
    if (paramInt == 0)
      return this.activeHttpsCertsMap.keySet();
    return this.activeSysHttpsCertsMap.keySet();
  }

  public Collection getRootCACertificates(int paramInt)
  {
    if (paramInt == 0)
      return this.activeRootCACertsMap.keySet();
    return this.activeSysRootCACertsMap.keySet();
  }

  public Collection getHttpsRootCACertificates(int paramInt)
  {
    if (paramInt == 0)
      return this.activeHttpsRootCACertsMap.keySet();
    return this.activeSysHttpsRootCACertsMap.keySet();
  }

  public Collection getClientAuthCertificates(int paramInt)
  {
    if (paramInt == 0)
      return this.activeClientAuthCertsMap.keySet();
    return this.activeSysClientAuthCertsMap.keySet();
  }

  public void removeTrustedCertificate(Certificate paramCertificate)
  {
    CertStore localCertStore = DeploySigningCertStore.getUserCertStore();
    try
    {
      localCertStore.load(true);
      if (localCertStore.remove(paramCertificate))
      {
        localCertStore.save();
        Object localObject = this.activeTrustedCertsMap.remove(paramCertificate);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
  }

  public void removeHttpsCertificate(Certificate paramCertificate)
  {
    CertStore localCertStore = DeploySSLCertStore.getUserCertStore();
    try
    {
      localCertStore.load(true);
      if (localCertStore.remove(paramCertificate))
      {
        localCertStore.save();
        Object localObject = this.activeHttpsCertsMap.remove(paramCertificate);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
  }

  public void removeRootCACertificate(Certificate paramCertificate)
  {
    CertStore localCertStore = RootCertStore.getUserCertStore();
    try
    {
      localCertStore.load(true);
      if (localCertStore.remove(paramCertificate))
      {
        localCertStore.save();
        Object localObject = this.activeRootCACertsMap.remove(paramCertificate);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
  }

  public void removeHttpsRootCACertificate(Certificate paramCertificate)
  {
    CertStore localCertStore = SSLRootCertStore.getUserCertStore();
    try
    {
      localCertStore.load(true);
      if (localCertStore.remove(paramCertificate))
      {
        localCertStore.save();
        Object localObject = this.activeHttpsRootCACertsMap.remove(paramCertificate);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
  }

  public void removeClientAuthCertificate(JDialog paramJDialog, Certificate[][] paramArrayOfCertificate)
  {
    DeployClientAuthCertStore localDeployClientAuthCertStore = DeployClientAuthCertStore.getUserCertStore(paramJDialog);
    Certificate[] arrayOfCertificate = new Certificate[paramArrayOfCertificate.length];
    try
    {
      localDeployClientAuthCertStore.load(true);
      for (int i = 0; i < paramArrayOfCertificate.length; i++)
        arrayOfCertificate[i] = paramArrayOfCertificate[i][0];
      boolean bool = localDeployClientAuthCertStore.remove(arrayOfCertificate);
      if (bool)
      {
        localDeployClientAuthCertStore.save();
        for (int j = 0; j < paramArrayOfCertificate.length; j++)
          this.activeClientAuthCertsMap.remove(paramArrayOfCertificate[j]);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(paramJDialog, localException);
    }
  }

  public void addTrustedCertificate(Certificate paramCertificate)
  {
    CertStore localCertStore = DeploySigningCertStore.getUserCertStore();
    try
    {
      localCertStore.load(true);
      if (localCertStore.add(paramCertificate))
      {
        localCertStore.save();
        this.activeTrustedCertsMap.put(paramCertificate, paramCertificate);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
  }

  public void addHttpsCertificate(Certificate paramCertificate)
  {
    CertStore localCertStore = DeploySSLCertStore.getUserCertStore();
    try
    {
      localCertStore.load(true);
      if (localCertStore.add(paramCertificate))
      {
        localCertStore.save();
        this.activeHttpsCertsMap.put(paramCertificate, paramCertificate);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
  }

  public void addCACertificate(Certificate paramCertificate)
  {
    CertStore localCertStore = RootCertStore.getUserCertStore();
    try
    {
      localCertStore.load(true);
      if (localCertStore.add(paramCertificate))
      {
        localCertStore.save();
        this.activeRootCACertsMap.put(paramCertificate, paramCertificate);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
  }

  public void addHttpsCACertificate(Certificate paramCertificate)
  {
    CertStore localCertStore = SSLRootCertStore.getUserCertStore();
    try
    {
      localCertStore.load(true);
      if (localCertStore.add(paramCertificate))
      {
        localCertStore.save();
        this.activeHttpsRootCACertsMap.put(paramCertificate, paramCertificate);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
  }

  public void addClientAuthCertChain(JDialog paramJDialog, Certificate[] paramArrayOfCertificate, Key paramKey)
  {
    DeployClientAuthCertStore localDeployClientAuthCertStore = DeployClientAuthCertStore.getUserCertStore(paramJDialog);
    try
    {
      localDeployClientAuthCertStore.load(true);
      boolean bool = localDeployClientAuthCertStore.addCertKey(paramArrayOfCertificate, paramKey);
      if (bool)
      {
        localDeployClientAuthCertStore.save();
        this.activeClientAuthCertsMap.put(paramArrayOfCertificate, paramArrayOfCertificate);
      }
    }
    catch (Exception localException)
    {
      Trace.printException(localException);
    }
  }

  public void reset()
  {
    this.activeTrustedCertsMap.clear();
    this.activeHttpsCertsMap.clear();
    this.activeRootCACertsMap.clear();
    this.activeHttpsRootCACertsMap.clear();
    this.activeClientAuthCertsMap.clear();
    this.activeSysTrustedCertsMap.clear();
    this.activeSysHttpsCertsMap.clear();
    this.activeSysRootCACertsMap.clear();
    this.activeSysHttpsRootCACertsMap.clear();
    this.activeSysClientAuthCertsMap.clear();
    CertStore localCertStore1 = DeploySigningCertStore.getUserCertStore();
    Object localObject1;
    try
    {
      localCertStore1.load();
      Iterator localIterator = localCertStore1.getCertificates().iterator();
      while (localIterator.hasNext())
      {
        localObject1 = (X509Certificate)localIterator.next();
        this.activeTrustedCertsMap.put(localObject1, localObject1);
      }
    }
    catch (Exception localException1)
    {
      Trace.printException(localException1);
    }
    CertStore localCertStore2 = DeploySigningCertStore.getSystemCertStore();
    Object localObject2;
    try
    {
      localCertStore2.load();
      localObject1 = localCertStore2.getCertificates().iterator();
      while (((Iterator)localObject1).hasNext())
      {
        localObject2 = (X509Certificate)((Iterator)localObject1).next();
        this.activeSysTrustedCertsMap.put(localObject2, localObject2);
      }
    }
    catch (Exception localException2)
    {
      Trace.printException(localException2);
    }
    CertStore localCertStore3 = DeploySSLCertStore.getUserCertStore();
    Object localObject3;
    try
    {
      localCertStore3.load();
      localObject2 = localCertStore3.getCertificates().iterator();
      while (((Iterator)localObject2).hasNext())
      {
        localObject3 = (X509Certificate)((Iterator)localObject2).next();
        this.activeHttpsCertsMap.put(localObject3, localObject3);
      }
    }
    catch (Exception localException3)
    {
      Trace.printException(localException3);
    }
    CertStore localCertStore4 = DeploySSLCertStore.getSystemCertStore();
    Object localObject4;
    try
    {
      localCertStore4.load();
      localObject3 = localCertStore4.getCertificates().iterator();
      while (((Iterator)localObject3).hasNext())
      {
        localObject4 = (X509Certificate)((Iterator)localObject3).next();
        this.activeSysHttpsCertsMap.put(localObject4, localObject4);
      }
    }
    catch (Exception localException4)
    {
      Trace.printException(localException4);
    }
    CertStore localCertStore5 = RootCertStore.getUserCertStore();
    Object localObject5;
    try
    {
      localCertStore5.load();
      localObject4 = localCertStore5.getCertificates().iterator();
      while (((Iterator)localObject4).hasNext())
      {
        localObject5 = (X509Certificate)((Iterator)localObject4).next();
        this.activeRootCACertsMap.put(localObject5, localObject5);
      }
    }
    catch (Exception localException5)
    {
      Trace.printException(localException5);
    }
    CertStore localCertStore6 = RootCertStore.getSystemCertStore();
    Object localObject6;
    try
    {
      localCertStore6.load();
      localObject5 = localCertStore6.getCertificates().iterator();
      while (((Iterator)localObject5).hasNext())
      {
        localObject6 = (X509Certificate)((Iterator)localObject5).next();
        this.activeSysRootCACertsMap.put(localObject6, localObject6);
      }
    }
    catch (Exception localException6)
    {
      Trace.printException(localException6);
    }
    CertStore localCertStore7 = SSLRootCertStore.getUserCertStore();
    Object localObject7;
    try
    {
      localCertStore7.load();
      localObject6 = localCertStore7.getCertificates().iterator();
      while (((Iterator)localObject6).hasNext())
      {
        localObject7 = (X509Certificate)((Iterator)localObject6).next();
        this.activeHttpsRootCACertsMap.put(localObject7, localObject7);
      }
    }
    catch (Exception localException7)
    {
      Trace.printException(localException7);
    }
    CertStore localCertStore8 = SSLRootCertStore.getSystemCertStore();
    Object localObject8;
    try
    {
      localCertStore8.load();
      localObject7 = localCertStore8.getCertificates().iterator();
      while (((Iterator)localObject7).hasNext())
      {
        localObject8 = (X509Certificate)((Iterator)localObject7).next();
        this.activeSysHttpsRootCACertsMap.put(localObject8, localObject8);
      }
    }
    catch (Exception localException8)
    {
      Trace.printException(localException8);
    }
    DeployClientAuthCertStore localDeployClientAuthCertStore = DeployClientAuthCertStore.getUserCertStore(null);
    Object localObject9;
    try
    {
      localDeployClientAuthCertStore.load();
      localObject8 = localDeployClientAuthCertStore.getCertificates().iterator();
      while (((Iterator)localObject8).hasNext())
      {
        localObject9 = (Certificate[])(Certificate[])((Iterator)localObject8).next();
        this.activeClientAuthCertsMap.put(localObject9, localObject9);
      }
    }
    catch (Exception localException9)
    {
      Trace.printException(localException9);
    }
    CertStore localCertStore9 = DeployClientAuthCertStore.getSystemCertStore(null);
    try
    {
      localCertStore9.load();
      localObject9 = localCertStore9.getCertificates().iterator();
      while (((Iterator)localObject9).hasNext())
      {
        Certificate[] arrayOfCertificate = (Certificate[])(Certificate[])((Iterator)localObject9).next();
        this.activeSysClientAuthCertsMap.put(arrayOfCertificate, arrayOfCertificate);
      }
    }
    catch (Exception localException10)
    {
      Trace.printException(localException10);
    }
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.CertificatesInfo
 * JD-Core Version:    0.6.0
 */