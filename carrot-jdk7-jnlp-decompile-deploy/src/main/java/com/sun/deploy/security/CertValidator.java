package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.util.PerfLogger;
import java.io.IOException;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

final class CertValidator
{
  public static boolean validate(CodeSource paramCodeSource, AppInfo paramAppInfo, Certificate[] paramArrayOfCertificate, int paramInt, CertStore paramCertStore1, CertStore paramCertStore2, CertStore paramCertStore3, CertStore paramCertStore4, CertStore paramCertStore5, CertStore paramCertStore6)
    throws CertificateEncodingException, CertificateExpiredException, CertificateNotYetValidException, CertificateParsingException, CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException
  {
    PerfLogger.setTime("Security: Start CertValdator class");
    HashMap localHashMap = new HashMap();
    String str = null;
    boolean bool1 = false;
    int i = 0;
    int j = 0;
    int k = 0;
    int m = 0;
    int n = 0;
    localHashMap = getCertMap(paramCertStore1, paramCertStore2);
    Date localDate1 = new Date();
    paramArrayOfCertificate = canonicalize(paramArrayOfCertificate, localDate1, localHashMap);
    for (paramInt = 0; n < paramArrayOfCertificate.length; paramInt++)
    {
      int i1 = m;
      Object localObject1 = null;
      Object localObject2 = null;
      for (i1 = m; i1 < paramArrayOfCertificate.length; i1++)
      {
        X509Certificate localX509Certificate = null;
        localObject3 = null;
        if ((paramArrayOfCertificate[i1] instanceof X509Certificate))
          localX509Certificate = (X509Certificate)paramArrayOfCertificate[i1];
        if ((i1 + 1 < paramArrayOfCertificate.length) && ((paramArrayOfCertificate[(i1 + 1)] instanceof X509Certificate)))
          localObject3 = (X509Certificate)paramArrayOfCertificate[(i1 + 1)];
        else
          localObject3 = localX509Certificate;
        try
        {
          localX509Certificate.checkValidity();
        }
        catch (CertificateExpiredException localCertificateExpiredException)
        {
          if (localObject1 == null)
            localObject1 = localCertificateExpiredException;
        }
        catch (CertificateNotYetValidException localCertificateNotYetValidException)
        {
          if (localObject2 == null)
            localObject2 = localCertificateNotYetValidException;
        }
        if ((!paramCertStore1.contains(localX509Certificate)) && (i1 + 1 != paramArrayOfCertificate.length) && (CertUtils.isIssuerOf(localX509Certificate, (X509Certificate)localObject3)) && ((paramCertStore2 == null) || (!paramCertStore2.contains(localX509Certificate))))
          CertUtils.checkUsageForCodeSigning(localX509Certificate, i1 - m);
        if (!CertUtils.isIssuerOf(localX509Certificate, (X509Certificate)localObject3))
          break;
        try
        {
          localX509Certificate.verify(((X509Certificate)localObject3).getPublicKey());
        }
        catch (GeneralSecurityException localGeneralSecurityException)
        {
          Trace.msgSecurityPrintln("trustdecider.check.signature");
          str = ResourceManager.getMessage("trustdecider.check.signature");
          throw new CertificateException(str);
        }
      }
      n = i1 < paramArrayOfCertificate.length ? i1 + 1 : i1;
      if (!Config.getBooleanProperty("deployment.security.askgrantdialog.show"))
      {
        str = ResourceManager.getMessage("trustdecider.user.cannot.grant.any");
        throw new CertificateException(str);
      }
      int i2 = (paramCertStore1.verify(paramArrayOfCertificate[(n - 1)])) || ((paramCertStore2 != null) && (paramCertStore2.verify(paramArrayOfCertificate[(n - 1)]))) ? 1 : 0;
      if (i2 == 0)
      {
        if (!Config.getBooleanProperty("deployment.security.askgrantdialog.notinca"))
        {
          str = ResourceManager.getMessage("trustdecider.user.cannot.grant.notinca");
          throw new CertificateException(str);
        }
        bool1 = true;
      }
      if (localObject1 != null)
      {
        i = 1;
        j = -1;
      }
      if (localObject2 != null)
      {
        i = 1;
        j = 1;
      }
      Object localObject3 = null;
      Object localObject4;
      try
      {
        CodeSigner[] arrayOfCodeSigner = paramCodeSource.getCodeSigners();
        Timestamp localTimestamp = arrayOfCodeSigner[paramInt].getTimestamp();
        if (localTimestamp != null)
        {
          Trace.msgSecurityPrintln("trustdecider.check.timestamping.yes");
          localObject3 = localTimestamp.getTimestamp();
          localObject4 = localTimestamp.getSignerCertPath();
          if (i != 0)
          {
            Trace.msgSecurityPrintln("trustdecider.check.timestamping.need");
            Date localDate2 = ((X509Certificate)paramArrayOfCertificate[(n - 1)]).getNotAfter();
            Date localDate3 = ((X509Certificate)paramArrayOfCertificate[(n - 1)]).getNotBefore();
            if ((((Date)localObject3).before(localDate2)) && (((Date)localObject3).after(localDate3)))
            {
              Trace.msgSecurityPrintln("trustdecider.check.timestamping.valid");
              boolean bool2 = checkTSAPath((CertPath)localObject4, localDate1, paramCertStore2, paramCertStore1, localHashMap);
              if (bool2)
              {
                i = 0;
                j = 0;
              }
              else
              {
                localObject3 = null;
              }
            }
            else
            {
              Trace.msgSecurityPrintln("trustdecider.check.timestamping.invalid");
            }
          }
          else
          {
            Trace.msgSecurityPrintln("trustdecider.check.timestamping.noneed");
          }
        }
        else
        {
          Trace.msgSecurityPrintln("trustdecider.check.timestamping.no");
        }
      }
      catch (NoSuchMethodError localNoSuchMethodError)
      {
        Trace.msgSecurityPrintln("trustdecider.check.timestamping.notfound");
      }
      int i3 = 0;
      if (paramCertStore6.contains(paramArrayOfCertificate[m]))
        if (paramCertStore6.contains(paramArrayOfCertificate[m], true))
          i3 = 1;
        else
          i3 = i;
      if (i3 == 0)
      {
        if ((paramCertStore5.contains(paramArrayOfCertificate[m])) && ((i == 0) || (!paramCertStore5.contains(paramArrayOfCertificate[m], true))))
          return true;
        if ((paramCertStore4.contains(paramArrayOfCertificate[m])) && ((i == 0) || (!paramCertStore4.contains(paramArrayOfCertificate[m], true))))
          return true;
        if ((paramCertStore3 != null) && (paramCertStore3.contains(paramArrayOfCertificate[m])))
          return true;
        int i4 = X509Util.showSecurityDialog(paramArrayOfCertificate, paramCodeSource.getLocation(), m, n, bool1, j, (Date)localObject3, paramAppInfo);
        if (i4 == 0)
        {
          Trace.msgSecurityPrintln("trustdecider.user.grant.session");
          paramCertStore4.add(paramArrayOfCertificate[m], i == 0);
          paramCertStore4.save();
          k = 1;
        }
        else if (i4 == 2)
        {
          Trace.msgSecurityPrintln("trustdecider.user.grant.forever");
          localObject4 = DeploySigningCertStore.getUserCertStore();
          ((CertStore)localObject4).load(true);
          if (((CertStore)localObject4).add(paramArrayOfCertificate[m], i == 0))
            ((CertStore)localObject4).save();
          k = 1;
        }
        else
        {
          Trace.msgSecurityPrintln("trustdecider.user.deny");
          paramCertStore6.add(paramArrayOfCertificate[m], i == 0);
          paramCertStore6.save();
        }
        PerfLogger.setTime("Security: End CertValdator class");
        if (k != 0)
          return true;
      }
      m = n;
    }
    return false;
  }

  private static boolean checkTSAPath(CertPath paramCertPath, Date paramDate, CertStore paramCertStore1, CertStore paramCertStore2, HashMap paramHashMap)
  {
    Trace.msgSecurityPrintln("trustdecider.check.timestamping.tsapath");
    try
    {
      List localList = paramCertPath.getCertificates();
      Object[] arrayOfObject = localList.toArray();
      int i = arrayOfObject.length;
      Certificate[] arrayOfCertificate = new Certificate[i];
      for (int j = 0; j < i; j++)
        arrayOfCertificate[j] = ((Certificate)arrayOfObject[j]);
      arrayOfCertificate = canonicalize(arrayOfCertificate, paramDate, paramHashMap);
      j = arrayOfCertificate.length;
      Certificate localCertificate = arrayOfCertificate[(j - 1)];
      if ((paramCertStore2.verify(localCertificate) == true) || ((paramCertStore1 != null) && (paramCertStore1.verify(localCertificate) == true)))
      {
        Trace.msgSecurityPrintln("trustdecider.check.timestamping.inca");
        for (int k = 0; k < j - 1; k++)
        {
          X509Certificate localX509Certificate1 = (X509Certificate)arrayOfCertificate[k];
          X509Certificate localX509Certificate2 = (X509Certificate)arrayOfCertificate[(k + 1)];
          try
          {
            CertUtils.checkUsageForCodeSigning(localX509Certificate1, k, true);
            localX509Certificate1.verify(localX509Certificate2.getPublicKey());
          }
          catch (GeneralSecurityException localGeneralSecurityException)
          {
            Trace.msgSecurityPrintln("trustdecider.check.signature");
            return false;
          }
        }
        return true;
      }
      Trace.msgSecurityPrintln("trustdecider.check.timestamping.notinca");
      return false;
    }
    catch (Exception localException)
    {
    }
    return false;
  }

  private static synchronized HashMap getCertMap(CertStore paramCertStore1, CertStore paramCertStore2)
    throws KeyStoreException
  {
    HashMap localHashMap = new HashMap();
    Iterator localIterator;
    Certificate localCertificate;
    if (paramCertStore1 != null)
    {
      localIterator = paramCertStore1.getCertificates().iterator();
      while (localIterator.hasNext())
      {
        localCertificate = (Certificate)localIterator.next();
        if ((localCertificate instanceof X509Certificate))
          localHashMap = addTrustedCert((X509Certificate)localCertificate, localHashMap);
      }
    }
    if (paramCertStore2 != null)
    {
      localIterator = paramCertStore2.getCertificates().iterator();
      while (localIterator.hasNext())
      {
        localCertificate = (Certificate)localIterator.next();
        if ((localCertificate instanceof X509Certificate))
          localHashMap = addTrustedCert((X509Certificate)localCertificate, localHashMap);
      }
    }
    return localHashMap;
  }

  private static HashMap addTrustedCert(X509Certificate paramX509Certificate, HashMap paramHashMap)
  {
    Principal localPrincipal = X509Util.getSubjectPrincipal(paramX509Certificate);
    Object localObject = (Collection)paramHashMap.get(localPrincipal);
    if (localObject == null)
    {
      localObject = new ArrayList();
      paramHashMap.put(localPrincipal, localObject);
    }
    ((Collection)localObject).add(paramX509Certificate);
    return (HashMap)paramHashMap;
  }

  private static Certificate[] canonicalize(Certificate[] paramArrayOfCertificate, Date paramDate, HashMap paramHashMap)
    throws CertificateException
  {
    ArrayList localArrayList = new ArrayList(paramArrayOfCertificate.length);
    int i = 0;
    if (paramArrayOfCertificate.length == 0)
      return paramArrayOfCertificate;
    for (int j = 0; j < paramArrayOfCertificate.length; j++)
    {
      Object localObject = (X509Certificate)paramArrayOfCertificate[j];
      X509Certificate localX509Certificate1 = getTrustedCertificate((X509Certificate)localObject, paramDate, paramHashMap);
      if (localX509Certificate1 != null)
      {
        Trace.msgSecurityPrintln("trustdecider.check.canonicalize.updatecert");
        localObject = localX509Certificate1;
        i = 1;
      }
      localArrayList.add(localObject);
      Principal localPrincipal1 = X509Util.getSubjectPrincipal(paramArrayOfCertificate[j]);
      Principal localPrincipal2 = X509Util.getIssuerPrincipal(paramArrayOfCertificate[j]);
      Principal localPrincipal3 = null;
      if (j < paramArrayOfCertificate.length - 1)
        localPrincipal3 = X509Util.getSubjectPrincipal(paramArrayOfCertificate[(j + 1)]);
      if ((localPrincipal2.equals(localPrincipal1)) || (localPrincipal2.equals(localPrincipal3)))
        continue;
      X509Certificate localX509Certificate2 = getTrustedIssuerCertificate((X509Certificate)paramArrayOfCertificate[j], paramDate, paramHashMap);
      if (localX509Certificate2 == null)
        continue;
      Trace.msgSecurityPrintln("trustdecider.check.canonicalize.missing");
      i = 1;
      localArrayList.add(localX509Certificate2);
    }
    if (i != 0)
      return (Certificate[])(Certificate[])localArrayList.toArray(new Certificate[localArrayList.size()]);
    return (Certificate)paramArrayOfCertificate;
  }

  private static X509Certificate getTrustedCertificate(X509Certificate paramX509Certificate, Date paramDate, HashMap paramHashMap)
  {
    Principal localPrincipal1 = X509Util.getSubjectPrincipal(paramX509Certificate);
    List localList = (List)paramHashMap.get(localPrincipal1);
    if (localList == null)
      return null;
    Principal localPrincipal2 = X509Util.getIssuerPrincipal(paramX509Certificate);
    PublicKey localPublicKey = paramX509Certificate.getPublicKey();
    Iterator localIterator = localList.iterator();
    while (localIterator.hasNext())
    {
      X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
      if ((localX509Certificate.equals(paramX509Certificate)) || (!X509Util.getIssuerPrincipal(localX509Certificate).equals(localPrincipal2)) || (!localX509Certificate.getPublicKey().equals(localPublicKey)))
        continue;
      try
      {
        localX509Certificate.checkValidity(paramDate);
      }
      catch (Exception localException)
      {
      }
      continue;
      Trace.msgSecurityPrintln("trustdecider.check.gettrustedcert.find");
      return localX509Certificate;
    }
    return null;
  }

  private static X509Certificate getTrustedIssuerCertificate(X509Certificate paramX509Certificate, Date paramDate, HashMap paramHashMap)
  {
    Principal localPrincipal = X509Util.getIssuerPrincipal(paramX509Certificate);
    List localList = (List)paramHashMap.get(localPrincipal);
    if (localList == null)
      return null;
    Iterator localIterator = localList.iterator();
    while (localIterator.hasNext())
    {
      X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
      try
      {
        localX509Certificate.checkValidity(paramDate);
      }
      catch (Exception localException)
      {
      }
      continue;
      Trace.msgSecurityPrintln("trustdecider.check.gettrustedissuercert.find");
      return localX509Certificate;
    }
    return null;
  }

  public static boolean isSigner(Certificate paramCertificate1, Certificate paramCertificate2)
  {
    try
    {
      paramCertificate1.verify(paramCertificate2.getPublicKey());
      return true;
    }
    catch (Exception localException)
    {
    }
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CertValidator
 * JD-Core Version:    0.6.0
 */