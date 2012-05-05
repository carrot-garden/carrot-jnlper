package com.sun.deploy.security;

import com.sun.applet2.preloader.CancelException;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.UserDeclinedEvent;
import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import com.sun.deploy.util.DeployLock;
import com.sun.deploy.util.PerfLogger;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedExceptionAction;
import java.security.Security;
import java.security.Timestamp;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.OCSP;
import sun.security.provider.certpath.OCSP.RevocationStatus;
import sun.security.provider.certpath.OCSP.RevocationStatus.CertStatus;
import sun.security.validator.PKIXValidator;
import sun.security.validator.Validator;
import sun.security.validator.ValidatorException;

public class TrustDecider
{
  public static final int TrustOption_GrantThisSession = 0;
  public static final int TrustOption_Deny = 1;
  public static final int TrustOption_GrantAlways = 2;
  private static CertStore rootStore = null;
  private static CertStore permanentStore = null;
  private static CertStore sessionStore = null;
  private static CertStore deniedStore = null;
  private static CertStore browserRootStore = null;
  private static CertStore browserTrustedStore = null;
  private static LazyRootStore lazyRootStore = null;
  private static List jurisdictionList = null;
  private static X509CRL crl509 = null;
  private static boolean ocspValidConfig = false;
  private static String ocspSigner = null;
  private static String ocspURL = null;
  private static boolean crlCheck = false;
  private static boolean ocspCheck = false;
  private static boolean ocspEECheck = false;
  private static DeployLock deployLock = null;
  public static final long PERMISSION_GRANTED_FOR_SESSION = 1L;
  public static final long PERMISSION_DENIED = 0L;
  private static final String SUN_NAMESPACE = "OU=Java Signed Extensions,OU=Corporate Object Signing,O=Sun Microsystems Inc";
  private static final String ORACLE_NAMESPACE = "OU=Java Signed Extensions,OU=Corporate Object Signing,O=Oracle Corporation";
  private static final String[] PRE_TRUSTED_NAMESPACES = { "OU=Java Signed Extensions,OU=Corporate Object Signing,O=Sun Microsystems Inc", "OU=Java Signed Extensions,OU=Corporate Object Signing,O=Oracle Corporation" };
  private static final List preTrustList = Arrays.asList(PRE_TRUSTED_NAMESPACES);
  private static boolean storesLoaded;
  private static boolean reloadDeniedStore;

  private static void grabDeployLock()
    throws InterruptedException
  {
    deployLock.lock();
  }

  private static void releaseDeployLock()
  {
    try
    {
      deployLock.unlock();
    }
    catch (IllegalMonitorStateException localIllegalMonitorStateException)
    {
    }
  }

  public static void resetDenyStore()
  {
    Trace.msgSecurityPrintln("trustdecider.check.reset.denystore");
    try
    {
      grabDeployLock();
      deniedStore = new DeniedCertStore();
      reloadDeniedStore = true;
    }
    catch (InterruptedException localInterruptedException)
    {
      throw new RuntimeException(localInterruptedException);
    }
    finally
    {
      releaseDeployLock();
    }
  }

  // ERROR //
  public static void reset()
  {
    // Byte code:
    //   0: ldc 21
    //   2: invokestatic 782	com/sun/deploy/util/PerfLogger:setTime	(Ljava/lang/String;)V
    //   5: invokestatic 750	com/sun/deploy/security/TrustDecider:grabDeployLock	()V
    //   8: iconst_0
    //   9: putstatic 712	com/sun/deploy/security/TrustDecider:storesLoaded	Z
    //   12: invokestatic 747	com/sun/deploy/security/RootCertStore:getCertStore	()Lcom/sun/deploy/security/CertStore;
    //   15: putstatic 717	com/sun/deploy/security/TrustDecider:rootStore	Lcom/sun/deploy/security/CertStore;
    //   18: invokestatic 741	com/sun/deploy/security/DeploySigningCertStore:getCertStore	()Lcom/sun/deploy/security/CertStore;
    //   21: putstatic 716	com/sun/deploy/security/TrustDecider:permanentStore	Lcom/sun/deploy/security/CertStore;
    //   24: new 426	com/sun/deploy/security/SessionCertStore
    //   27: dup
    //   28: invokespecial 748	com/sun/deploy/security/SessionCertStore:<init>	()V
    //   31: putstatic 718	com/sun/deploy/security/TrustDecider:sessionStore	Lcom/sun/deploy/security/CertStore;
    //   34: new 421	com/sun/deploy/security/DeniedCertStore
    //   37: dup
    //   38: invokespecial 739	com/sun/deploy/security/DeniedCertStore:<init>	()V
    //   41: putstatic 715	com/sun/deploy/security/TrustDecider:deniedStore	Lcom/sun/deploy/security/CertStore;
    //   44: aconst_null
    //   45: putstatic 725	com/sun/deploy/security/TrustDecider:jurisdictionList	Ljava/util/List;
    //   48: ldc 33
    //   50: invokestatic 733	com/sun/deploy/config/Config:getBooleanProperty	(Ljava/lang/String;)Z
    //   53: ifeq +9 -> 62
    //   56: getstatic 726	com/sun/deploy/security/TrustDecider:preTrustList	Ljava/util/List;
    //   59: putstatic 725	com/sun/deploy/security/TrustDecider:jurisdictionList	Ljava/util/List;
    //   62: ldc 32
    //   64: invokestatic 733	com/sun/deploy/config/Config:getBooleanProperty	(Ljava/lang/String;)Z
    //   67: ifeq +25 -> 92
    //   70: invokestatic 771	com/sun/deploy/services/ServiceManager:getService	()Lcom/sun/deploy/services/Service;
    //   73: astore_0
    //   74: aload_0
    //   75: invokeinterface 852 1 0
    //   80: putstatic 713	com/sun/deploy/security/TrustDecider:browserRootStore	Lcom/sun/deploy/security/CertStore;
    //   83: aload_0
    //   84: invokeinterface 853 1 0
    //   89: putstatic 714	com/sun/deploy/security/TrustDecider:browserTrustedStore	Lcom/sun/deploy/security/CertStore;
    //   92: new 424	com/sun/deploy/security/LazyRootStore
    //   95: dup
    //   96: getstatic 713	com/sun/deploy/security/TrustDecider:browserRootStore	Lcom/sun/deploy/security/CertStore;
    //   99: getstatic 717	com/sun/deploy/security/TrustDecider:rootStore	Lcom/sun/deploy/security/CertStore;
    //   102: invokespecial 745	com/sun/deploy/security/LazyRootStore:<init>	(Lcom/sun/deploy/security/CertStore;Lcom/sun/deploy/security/CertStore;)V
    //   105: putstatic 719	com/sun/deploy/security/TrustDecider:lazyRootStore	Lcom/sun/deploy/security/LazyRootStore;
    //   108: goto +8 -> 116
    //   111: astore_0
    //   112: aload_0
    //   113: invokevirtual 787	java/lang/Exception:printStackTrace	()V
    //   116: new 428	com/sun/deploy/security/TrustDecider$1
    //   119: dup
    //   120: invokespecial 767	com/sun/deploy/security/TrustDecider$1:<init>	()V
    //   123: invokestatic 803	java/security/AccessController:doPrivileged	(Ljava/security/PrivilegedExceptionAction;)Ljava/lang/Object;
    //   126: pop
    //   127: goto +8 -> 135
    //   130: astore_0
    //   131: aload_0
    //   132: invokevirtual 809	java/security/PrivilegedActionException:printStackTrace	()V
    //   135: new 429	com/sun/deploy/security/TrustDecider$2
    //   138: dup
    //   139: invokespecial 768	com/sun/deploy/security/TrustDecider$2:<init>	()V
    //   142: invokestatic 803	java/security/AccessController:doPrivileged	(Ljava/security/PrivilegedExceptionAction;)Ljava/lang/Object;
    //   145: pop
    //   146: goto +8 -> 154
    //   149: astore_0
    //   150: aload_0
    //   151: invokevirtual 809	java/security/PrivilegedActionException:printStackTrace	()V
    //   154: jsr +22 -> 176
    //   157: goto +25 -> 182
    //   160: astore_0
    //   161: new 452	java/lang/RuntimeException
    //   164: dup
    //   165: aload_0
    //   166: invokespecial 791	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
    //   169: athrow
    //   170: astore_1
    //   171: jsr +5 -> 176
    //   174: aload_1
    //   175: athrow
    //   176: astore_2
    //   177: invokestatic 751	com/sun/deploy/security/TrustDecider:releaseDeployLock	()V
    //   180: ret 2
    //   182: ldc 9
    //   184: invokestatic 782	com/sun/deploy/util/PerfLogger:setTime	(Ljava/lang/String;)V
    //   187: return
    //
    // Exception table:
    //   from	to	target	type
    //   92	108	111	java/lang/Exception
    //   116	127	130	java/security/PrivilegedActionException
    //   135	146	149	java/security/PrivilegedActionException
    //   5	154	160	java/lang/InterruptedException
    //   5	157	170	finally
    //   160	174	170	finally
  }

  public static long isAllPermissionGranted(CodeSource paramCodeSource, Preloader paramPreloader)
    throws CertificateEncodingException, CertificateExpiredException, CertificateNotYetValidException, CertificateParsingException, CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, CRLException, InvalidAlgorithmParameterException
  {
    return isAllPermissionGranted(paramCodeSource, new AppInfo(), false, paramPreloader);
  }

  private static void notifyOnUserDeclined(Preloader paramPreloader, String paramString)
  {
    if (paramPreloader == null)
      paramPreloader = (Preloader)ToolkitStore.get().getAppContext().get("preloader_key");
    try
    {
      if (paramPreloader != null)
        paramPreloader.handleEvent(new UserDeclinedEvent(paramString));
    }
    catch (CancelException localCancelException)
    {
    }
  }

  private static void doCheckRevocationStatus(X509Certificate[] paramArrayOfX509Certificate, Date paramDate)
    throws KeyStoreException, CertificateException, NoSuchAlgorithmException
  {
    if (!permanentStore.contains(paramArrayOfX509Certificate[0]))
      try
      {
        OCSP.RevocationStatus.CertStatus localCertStatus = doOCSPEEValidation(paramArrayOfX509Certificate[0], paramArrayOfX509Certificate[1], lazyRootStore, paramDate);
        if (localCertStatus != OCSP.RevocationStatus.CertStatus.GOOD)
        {
          Trace.msgSecurityPrintln("trustdecider.check.ocsp.ee.bad");
          String str = ResourceManager.getMessage("trustdecider.check.ocsp.ee.revoked");
          throw new CertificateException(str);
        }
        Trace.msgSecurityPrintln("trustdecider.check.ocsp.ee.good");
      }
      catch (IOException localIOException)
      {
        Trace.msgSecurityPrintln(localIOException.getMessage());
      }
      catch (CertPathValidatorException localCertPathValidatorException)
      {
        Trace.msgSecurityPrintln(localCertPathValidatorException.getMessage());
        throw new CertificateException(localCertPathValidatorException);
      }
      catch (NoClassDefFoundError localNoClassDefFoundError)
      {
      }
  }

  private static List breakDownMultiSignerChains(Certificate[] paramArrayOfCertificate)
  {
    int i = 0;
    int j = 0;
    int k = 0;
    ArrayList localArrayList1 = new ArrayList();
    while (j < paramArrayOfCertificate.length)
    {
      ArrayList localArrayList2 = new ArrayList();
      for (int m = i; (m + 1 < paramArrayOfCertificate.length) && ((paramArrayOfCertificate[m] instanceof X509Certificate)) && ((paramArrayOfCertificate[(m + 1)] instanceof X509Certificate)) && (CertUtils.isIssuerOf((X509Certificate)paramArrayOfCertificate[m], (X509Certificate)paramArrayOfCertificate[(m + 1)])); m++);
      j = m + 1;
      for (int n = i; n < j; n++)
        localArrayList2.add(paramArrayOfCertificate[n]);
      localArrayList1.add(localArrayList2);
      i = j;
      k++;
    }
    return localArrayList1;
  }

  private static boolean haveValidatorSupport()
  {
    if (Config.isJavaVersionAtLeast16())
      try
      {
        Class localClass = Class.forName("sun.security.validator.Validator", true, ClassLoader.getSystemClassLoader());
        if (localClass != null)
          return true;
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        Trace.msgSecurityPrintln("trustdecider.check.validate.notfound");
      }
    return false;
  }

  private static boolean hasCRL(X509Certificate[] paramArrayOfX509Certificate)
    throws IOException
  {
    if (crl509 != null)
      return true;
    for (int i = 0; i < paramArrayOfX509Certificate.length; i++)
      if (CertUtils.getCertCRLExtension(paramArrayOfX509Certificate[i]))
        return true;
    return false;
  }

  private static boolean hasOCSP(X509Certificate[] paramArrayOfX509Certificate)
    throws IOException
  {
    if (ocspValidConfig)
      return true;
    for (int i = 0; i < paramArrayOfX509Certificate.length; i++)
      if (CertUtils.hasAIAExtensionWithOCSPAccessMethod(paramArrayOfX509Certificate[i]))
        return true;
    return false;
  }

  public static synchronized void validateChainForWarmup(X509Certificate[] paramArrayOfX509Certificate, CodeSource paramCodeSource, int paramInt, AppInfo paramAppInfo, boolean paramBoolean)
    throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, CRLException, InvalidAlgorithmParameterException
  {
    try
    {
      grabDeployLock();
      ensureBasicStoresLoaded();
      long l = validateChain(paramArrayOfX509Certificate, paramCodeSource, paramInt, paramAppInfo, paramBoolean, null);
      Trace.println("Warmup validation completed (res=" + l + ")", TraceLevel.SECURITY);
    }
    catch (InterruptedException localInterruptedException)
    {
    }
    finally
    {
      releaseDeployLock();
    }
  }

  private static long validateChain(X509Certificate[] paramArrayOfX509Certificate, CodeSource paramCodeSource, int paramInt, AppInfo paramAppInfo, boolean paramBoolean, Preloader paramPreloader)
    throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, CRLException, InvalidAlgorithmParameterException
  {
    boolean bool1 = crlCheck;
    boolean bool2 = ocspCheck;
    boolean bool3 = ocspEECheck;
    boolean bool4 = false;
    boolean bool5 = false;
    long l1 = 0L;
    long l2 = 0L;
    int i = 0;
    int j = 0;
    String str2 = paramCodeSource.getLocation() != null ? paramCodeSource.getLocation().toString() : null;
    Object localObject1 = null;
    Object localObject2 = null;
    l2 = 9223372036854775807L;
    for (int k = 0; k < paramArrayOfX509Certificate.length; k++)
    {
      long l3 = paramArrayOfX509Certificate[k].getNotAfter().getTime();
      if (l3 < l2)
        l2 = l3;
      try
      {
        paramArrayOfX509Certificate[k].checkValidity();
      }
      catch (CertificateExpiredException localCertificateExpiredException)
      {
        if (localObject1 == null)
        {
          localObject1 = localCertificateExpiredException;
          i = -1;
          j = -1;
          bool5 = true;
        }
      }
      catch (CertificateNotYetValidException localCertificateNotYetValidException)
      {
        if (localObject2 != null)
          continue;
        localObject2 = localCertificateNotYetValidException;
        i = 1;
        j = 1;
        bool5 = true;
      }
    }
    k = paramArrayOfX509Certificate.length;
    X509Certificate localX509Certificate = paramArrayOfX509Certificate[(k - 1)];
    X500Principal localX500Principal1 = localX509Certificate.getIssuerX500Principal();
    X500Principal localX500Principal2 = localX509Certificate.getSubjectX500Principal();
    PerfLogger.setTime("Security: End check certificate expired and start replace CA check");
    Object localObject3 = lazyRootStore.getTrustAnchors(localX509Certificate);
    String str1;
    if (localObject3 == null)
    {
      if (!Config.getBooleanProperty("deployment.security.askgrantdialog.notinca"))
      {
        str1 = ResourceManager.getMessage("trustdecider.user.cannot.grant.notinca");
        throw new CertificateException(str1);
      }
      bool4 = true;
      localObject3 = new ArrayList();
      ((List)localObject3).add(localX509Certificate);
    }
    PerfLogger.setTime("Security: End replace CA check and start timestamp check");
    Date localDate = getTimeStampInfo(paramCodeSource, paramInt, paramArrayOfX509Certificate, lazyRootStore, bool5);
    if (localDate != null)
    {
      bool5 = false;
      i = 0;
    }
    PerfLogger.setTime("Security: End timestamp check and start pre-trusted certificate check");
    boolean bool6 = false;
    if (jurisdictionList != null)
    {
      Trace.msgSecurityPrintln("trustdecider.check.jurisdiction.found");
      if ((!bool4) && (i == 0))
      {
        Trace.msgSecurityPrintln("trustdecider.check.trustextension.on");
        bool6 = checkTrustedExtension(paramArrayOfX509Certificate[0]);
      }
      else
      {
        Trace.msgSecurityPrintln("trustdecider.check.trustextension.off");
      }
    }
    else
    {
      Trace.msgSecurityPrintln("trustdecider.check.jurisdiction.notfound");
    }
    PerfLogger.setTime("Security: End pre-trusted certificate check");
    if ((!bool4) && (j == 0) && (paramAppInfo.getType() == 3) && (!paramBoolean) && (!bool6) && (!permanentStore.contains(paramArrayOfX509Certificate[0])))
    {
      bool3 = true;
      Trace.msgSecurityPrintln("trustdecider.check.extensioninstall.on");
    }
    boolean bool7 = false;
    boolean bool8 = false;
    Object localObject4 = null;
    Object localObject5;
    try
    {
      bool7 = (bool1) && (hasCRL(paramArrayOfX509Certificate));
      bool8 = (bool2) && (hasOCSP(paramArrayOfX509Certificate));
      PerfLogger.setTime("Security: Start getting validator class");
      Validator localValidator = Validator.getInstance("PKIX", "plugin code signing", (Collection)localObject3);
      localObject5 = (PKIXValidator)localValidator;
      localObject4 = ((PKIXValidator)localObject5).getParameters();
      ((PKIXParameters)localObject4).addCertPathChecker(new DeployCertPathChecker((PKIXValidator)localObject5));
      PerfLogger.setTime("Security: End getting validator class and start CRL revocation check");
      if (bool1)
      {
        Trace.msgSecurityPrintln("trustdecider.check.validation.crl.on");
        localObject4 = doCRLValidation((PKIXParameters)localObject4, bool7);
      }
      else
      {
        Trace.msgSecurityPrintln("trustdecider.check.validation.crl.off");
      }
      PerfLogger.setTime("Security: End CRL and start OCSP revocation check");
      if (bool2)
      {
        Trace.msgSecurityPrintln("trustdecider.check.validation.ocsp.on");
        doOCSPValidation((PKIXParameters)localObject4, lazyRootStore, paramArrayOfX509Certificate, bool8, bool1);
      }
      else
      {
        Trace.msgSecurityPrintln("trustdecider.check.validation.ocsp.off");
      }
      PerfLogger.setTime("Security: End OCSP revocation check and start validator class");
      localObject6 = new X509Certificate[k];
      for (int i1 = 0; i1 < k; i1++)
        localObject6[i1] = new X509CertificateWrapper(paramArrayOfX509Certificate[i1]);
      localValidator.validate(localObject6);
      PerfLogger.setTime("Security: End call validator class");
      if (((bool1) && (bool7)) || ((bool2) && (bool8)))
        Trace.msgSecurityPrintln("trustdecider.check.revocation.succeed");
    }
    catch (CertificateException localCertificateException)
    {
      Object localObject6;
      if (!Config.getBooleanProperty("deployment.security.askgrantdialog.notinca"))
      {
        str1 = ResourceManager.getMessage("trustdecider.user.cannot.grant.notinca");
        throw new CertificateException(str1);
      }
      if ((localCertificateException instanceof ValidatorException))
      {
        localObject5 = (ValidatorException)localCertificateException;
        if (ValidatorException.T_NO_TRUST_ANCHOR.equals(((ValidatorException)localObject5).getErrorType()))
        {
          bool4 = true;
        }
        else
        {
          localObject6 = "Certificate has been revoked";
          if (((bool1) && (bool7)) || ((bool2) && (bool8)))
          {
            String str3 = ((ValidatorException)localObject5).getMessage();
            if (str3.contains((CharSequence)localObject6))
              Trace.msgSecurityPrintln("trustdecider.check.validation.revoked");
            else
              Trace.msgSecurityPrintln(str3);
            throw ((Throwable)localObject5);
          }
          throw ((Throwable)localObject5);
        }
      }
      else
      {
        throw localCertificateException;
      }
    }
    catch (IOException localIOException)
    {
      Trace.msgSecurityPrintln(localIOException.getMessage());
      throw localIOException;
    }
    catch (InvalidAlgorithmParameterException localInvalidAlgorithmParameterException)
    {
      Trace.msgSecurityPrintln(localInvalidAlgorithmParameterException.getMessage());
      throw localInvalidAlgorithmParameterException;
    }
    catch (CRLException localCRLException)
    {
      Trace.msgSecurityPrintln(localCRLException.getMessage());
      throw localCRLException;
    }
    finally
    {
      Security.setProperty("com.sun.security.onlyCheckRevocationOfEECert", "false");
    }
    PerfLogger.setTime("Security: End certificate validation and start OCSP End-Entity revocation check");
    if ((bool3) && (!bool2) && (k > 1) && (!bool6) && (!bool4) && (j == 0))
      doCheckRevocationStatus(paramArrayOfX509Certificate, ((PKIXParameters)localObject4).getDate());
    else
      Trace.msgSecurityPrintln("trustdecider.check.ocsp.ee.off");
    PerfLogger.setTime("Security: End OCSP End-Entity revocation check");
    int m = 0;
    if (deniedStore.contains(paramArrayOfX509Certificate[0]))
    {
      notifyOnUserDeclined(paramPreloader, str2);
      if (deniedStore.contains(paramArrayOfX509Certificate[0], true))
        m = 1;
      else
        m = bool5;
    }
    if (m == 0)
    {
      if ((permanentStore.contains(paramArrayOfX509Certificate[0])) && ((!bool5) || (!permanentStore.contains(paramArrayOfX509Certificate[0], true))))
        return l2;
      if ((bool6) && (!permanentStore.contains(paramArrayOfX509Certificate[0], true)))
      {
        localObject5 = DeploySigningCertStore.getUserCertStore();
        ((CertStore)localObject5).load(true);
        if (((CertStore)localObject5).add(paramArrayOfX509Certificate[0], true))
          ((CertStore)localObject5).save();
        storesLoaded = false;
        Trace.msgSecurityPrintln("trustdecider.check.trustextension.add");
        return l2;
      }
      if ((sessionStore.contains(paramArrayOfX509Certificate[0])) && ((!bool5) || (!sessionStore.contains(paramArrayOfX509Certificate[0], true))))
        return 1L;
      if ((browserTrustedStore != null) && (browserTrustedStore.contains(paramArrayOfX509Certificate[0])))
        return 1L;
      if (!Config.getBooleanProperty("deployment.security.askgrantdialog.show"))
      {
        str1 = ResourceManager.getMessage("trustdecider.user.cannot.grant.any");
        throw new CertificateException(str1);
      }
      releaseDeployLock();
      int n = X509Util.showSecurityDialog(paramArrayOfX509Certificate, paramCodeSource.getLocation(), 0, k, bool4, i, localDate, paramAppInfo, paramBoolean);
      try
      {
        grabDeployLock();
      }
      catch (InterruptedException localInterruptedException)
      {
        throw new RuntimeException("Intermediate error trying to perform security validation");
      }
      PerfLogger.setTime("Security: Start take action on security dialog box");
      if (n == 0)
      {
        Trace.msgSecurityPrintln("trustdecider.user.grant.session");
        sessionStore.add(paramArrayOfX509Certificate[0], !bool5);
        sessionStore.save();
        l1 = 1L;
      }
      else if (n == 2)
      {
        Trace.msgSecurityPrintln("trustdecider.user.grant.forever");
        CertStore localCertStore = DeploySigningCertStore.getUserCertStore();
        localCertStore.load(true);
        if (localCertStore.add(paramArrayOfX509Certificate[0], !bool5))
          localCertStore.save();
        storesLoaded = false;
        l1 = l2;
      }
      else
      {
        Trace.msgSecurityPrintln("trustdecider.user.deny");
        deniedStore.add(paramArrayOfX509Certificate[0], !bool5);
        deniedStore.save();
        notifyOnUserDeclined(paramPreloader, str2);
      }
      PerfLogger.setTime("Security: End take action on security dialog box");
      return l1;
    }
    return 0L;
  }

  private static void ensureBasicStoresLoaded()
    throws InterruptedException, IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    if ((reloadDeniedStore) || (!storesLoaded))
    {
      deniedStore.load();
      reloadDeniedStore = false;
    }
    if (storesLoaded)
      return;
    storesLoaded = true;
    PerfLogger.setTime("Security: Start loading JRE permanent certStore");
    permanentStore.load();
    PerfLogger.setTime("Security: End loading JRE permanent certStore");
    sessionStore.load();
    PerfLogger.setTime("Security: start loading browser Trust certStore");
    if (browserTrustedStore != null)
      browserTrustedStore.load();
    PerfLogger.setTime("Security: End loading browser Trust certStore");
  }

  public static synchronized long isAllPermissionGranted(CodeSource paramCodeSource, AppInfo paramAppInfo, boolean paramBoolean, Preloader paramPreloader)
    throws CertificateEncodingException, CertificateExpiredException, CertificateNotYetValidException, CertificateParsingException, CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, CRLException, InvalidAlgorithmParameterException
  {
    try
    {
      grabDeployLock();
      Certificate[] arrayOfCertificate = paramCodeSource.getCertificates();
      long l1;
      if (arrayOfCertificate == null)
      {
        l1 = 0L;
        jsr 229;
      }
      ensureBasicStoresLoaded();
      List localList1 = breakDownMultiSignerChains(arrayOfCertificate);
      PerfLogger.setTime("Security: End break certificate chain");
      if (haveValidatorSupport())
      {
        Trace.msgSecurityPrintln("trustdecider.check.validate.certpath.algorithm");
        l2 = 0L;
        Iterator localIterator = localList1.iterator();
        long l3;
        for (int i = 0; localIterator.hasNext(); i++)
        {
          PerfLogger.setTime("Security: Start check certificate expired");
          List localList2 = (List)localIterator.next();
          X509Certificate[] arrayOfX509Certificate = (X509Certificate[])(X509Certificate[])localList2.toArray(new X509Certificate[0]);
          l2 = validateChain(arrayOfX509Certificate, paramCodeSource, i, paramAppInfo, paramBoolean, paramPreloader);
          if (l2 == 0L)
            continue;
          l3 = l2;
          jsr 112;
        }
      }
      else
      {
        Trace.msgSecurityPrintln("trustdecider.check.validate.legacy.algorithm");
        rootStore.load();
        if (browserRootStore != null)
          browserRootStore.load();
        if (CertValidator.validate(paramCodeSource, paramAppInfo, arrayOfCertificate, localList1.size(), rootStore, browserRootStore, browserTrustedStore, sessionStore, permanentStore, deniedStore))
        {
          l2 = 1L;
          jsr 32;
        }
      }
    }
    catch (InterruptedException localInterruptedException)
    {
      long l2;
      throw new RuntimeException(localInterruptedException);
    }
    finally
    {
      releaseDeployLock();
    }
    return 0L;
  }

  private static boolean checkTSAPath(CertPath paramCertPath, LazyRootStore paramLazyRootStore)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    Trace.msgSecurityPrintln("trustdecider.check.timestamping.tsapath");
    List localList1 = paramCertPath.getCertificates();
    X509Certificate[] arrayOfX509Certificate = (X509Certificate[])(X509Certificate[])localList1.toArray(new X509Certificate[0]);
    int i = arrayOfX509Certificate.length;
    X509Certificate localX509Certificate = arrayOfX509Certificate[(i - 1)];
    List localList2 = paramLazyRootStore.getTrustAnchors(localX509Certificate);
    if (localList2 == null)
      return false;
    Validator localValidator = Validator.getInstance("PKIX", "tsa server", localList2);
    try
    {
      arrayOfX509Certificate = localValidator.validate(arrayOfX509Certificate);
    }
    catch (CertificateException localCertificateException)
    {
      Trace.msgSecurityPrintln(localCertificateException.getMessage());
      return false;
    }
    return true;
  }

  private static PKIXParameters doCRLValidation(PKIXParameters paramPKIXParameters, boolean paramBoolean)
    throws IOException, InvalidAlgorithmParameterException, CRLException, NoSuchAlgorithmException
  {
    if (crl509 != null)
    {
      Trace.msgSecurityPrintln("trustdecider.check.validation.crl.system.on");
      System.clearProperty("com.sun.security.enableCRLDP");
      paramPKIXParameters.setRevocationEnabled(true);
      paramPKIXParameters.addCertStore(java.security.cert.CertStore.getInstance("Collection", new CollectionCertStoreParameters(Collections.singletonList(crl509))));
    }
    else
    {
      Trace.msgSecurityPrintln("trustdecider.check.validation.crl.system.off");
      paramPKIXParameters.setRevocationEnabled(paramBoolean);
      System.setProperty("com.sun.security.enableCRLDP", Boolean.toString(paramBoolean));
    }
    return paramPKIXParameters;
  }

  private static void doOCSPValidation(PKIXParameters paramPKIXParameters, LazyRootStore paramLazyRootStore, X509Certificate[] paramArrayOfX509Certificate, boolean paramBoolean1, boolean paramBoolean2)
    throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    X509Certificate localX509Certificate = null;
    boolean bool = false;
    Security.setProperty("ocsp.enable", Boolean.toString(paramBoolean1));
    if (ocspValidConfig)
      Security.setProperty("ocsp.responderURL", ocspURL);
    paramPKIXParameters.setRevocationEnabled(paramBoolean1);
    if (ocspValidConfig)
    {
      Trace.msgSecurityPrintln("trustdecider.check.validation.ocsp.system.on");
      bool = paramLazyRootStore.containSubject(ocspSigner);
      localX509Certificate = paramLazyRootStore.getOCSPCert();
      if ((bool) && (localX509Certificate != null))
        Security.setProperty("ocsp.responderCertSubjectName", localX509Certificate.getSubjectX500Principal().getName());
    }
    else
    {
      Trace.msgSecurityPrintln("trustdecider.check.validation.ocsp.system.off");
    }
    if ((!paramBoolean2) && (paramBoolean1))
      System.setProperty("com.sun.security.enableCRLDP", "true");
  }

  private static OCSP.RevocationStatus.CertStatus doOCSPEEValidation(X509Certificate paramX509Certificate1, X509Certificate paramX509Certificate2, LazyRootStore paramLazyRootStore, Date paramDate)
    throws IOException, CertPathValidatorException, CertificateException, KeyStoreException, NoSuchAlgorithmException
  {
    Trace.msgSecurityPrintln("trustdecider.check.ocsp.ee.start");
    boolean bool = false;
    URI localURI = null;
    X509Certificate localX509Certificate = paramX509Certificate2;
    if (ocspValidConfig)
    {
      try
      {
        localURI = new URI(ocspURL);
      }
      catch (URISyntaxException localURISyntaxException)
      {
        Trace.msgSecurityPrintln("trustdecider.check.ocsp.ee.responderURI.no");
        return OCSP.RevocationStatus.CertStatus.GOOD;
      }
      bool = paramLazyRootStore.containSubject(ocspSigner);
      if (bool)
        localX509Certificate = paramLazyRootStore.getOCSPCert();
    }
    else
    {
      localURI = OCSP.getResponderURI(paramX509Certificate1);
    }
    if (localURI == null)
    {
      Trace.msgSecurityPrintln("trustdecider.check.ocsp.ee.responderURI.no");
      return OCSP.RevocationStatus.CertStatus.GOOD;
    }
    Object localObject = localURI.toString();
    Trace.msgSecurityPrintln("trustdecider.check.ocsp.ee.responderURI.value", new Object[] { localObject });
    localObject = OCSP.check(paramX509Certificate1, paramX509Certificate2, localURI, localX509Certificate, paramDate).getCertStatus();
    String str = ((OCSP.RevocationStatus.CertStatus)localObject).name();
    Trace.msgSecurityPrintln("trustdecider.check.ocsp.ee.return.status", new Object[] { str });
    return (OCSP.RevocationStatus.CertStatus)localObject;
  }

  private static boolean checkTrustedExtension(X509Certificate paramX509Certificate)
  {
    Trace.msgSecurityPrintln("trustdecider.check.trustextension.jurisdiction");
    X500Principal localX500Principal = paramX509Certificate.getSubjectX500Principal();
    String str1 = localX500Principal.getName();
    Iterator localIterator = jurisdictionList.iterator();
    while (localIterator.hasNext())
    {
      String str2 = (String)localIterator.next();
      if (str1.endsWith(str2))
      {
        Trace.msgSecurityPrintln("trustdecider.check.trustextension.jurisdiction.found");
        return true;
      }
    }
    return false;
  }

  private static Date getTimeStampInfo(CodeSource paramCodeSource, int paramInt, X509Certificate[] paramArrayOfX509Certificate, LazyRootStore paramLazyRootStore, boolean paramBoolean)
  {
    Date localDate1 = null;
    if (!paramBoolean)
    {
      Trace.msgSecurityPrintln("trustdecider.check.timestamping.noneed");
      return null;
    }
    try
    {
      Trace.msgSecurityPrintln("trustdecider.check.timestamping.need");
      CodeSigner[] arrayOfCodeSigner = paramCodeSource.getCodeSigners();
      Timestamp localTimestamp = arrayOfCodeSigner[paramInt].getTimestamp();
      if (localTimestamp != null)
      {
        Trace.msgSecurityPrintln("trustdecider.check.timestamping.yes");
        localDate1 = localTimestamp.getTimestamp();
        CertPath localCertPath = localTimestamp.getSignerCertPath();
        Trace.msgSecurityPrintln("trustdecider.check.timestamping.need");
        Date localDate2 = paramArrayOfX509Certificate[(paramArrayOfX509Certificate.length - 1)].getNotAfter();
        Date localDate3 = paramArrayOfX509Certificate[(paramArrayOfX509Certificate.length - 1)].getNotBefore();
        if ((localDate1.before(localDate2)) && (localDate1.after(localDate3)))
        {
          Trace.msgSecurityPrintln("trustdecider.check.timestamping.valid");
          if (!checkTSAPath(localCertPath, paramLazyRootStore))
            localDate1 = null;
        }
        else
        {
          Trace.msgSecurityPrintln("trustdecider.check.timestamping.invalid");
          localDate1 = null;
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
    catch (KeyStoreException localKeyStoreException)
    {
      Trace.msgSecurityPrintln("trustdecider.check.timestamping.notfound");
    }
    catch (IOException localIOException)
    {
      Trace.msgSecurityPrintln("trustdecider.check.timestamping.notfound");
    }
    catch (CertificateException localCertificateException)
    {
      Trace.msgSecurityPrintln("trustdecider.check.timestamping.notfound");
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      Trace.msgSecurityPrintln("trustdecider.check.timestamping.notfound");
    }
    return localDate1;
  }

  static
  {
    deployLock = new DeployLock();
    reset();
    storesLoaded = false;
    reloadDeniedStore = false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.TrustDecider
 * JD-Core Version:    0.6.0
 */