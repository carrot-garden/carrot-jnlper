package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.ByteArrayInputStream;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.util.Date;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CachedCertificatesHelper
{
  private static final boolean DEBUG = (Config.getDeployDebug()) || (Config.getPluginDebug());
  private final CertPath path;
  private final Date tm;
  private final boolean signedJNLP;

  public CachedCertificatesHelper(Date paramDate, CertPath paramCertPath)
  {
    this(paramDate, paramCertPath, false);
  }

  public CachedCertificatesHelper(Date paramDate, CertPath paramCertPath, boolean paramBoolean)
  {
    this.tm = paramDate;
    this.path = paramCertPath;
    this.signedJNLP = paramBoolean;
  }

  public static CachedCertificatesHelper create(Date paramDate, String paramString, boolean paramBoolean)
  {
    if (paramString == null)
      return null;
    try
    {
      CertificateFactory localCertificateFactory = CertificateFactory.getInstance("X.509");
      localObject = new BASE64Decoder();
      byte[] arrayOfByte = ((BASE64Decoder)localObject).decodeBuffer(paramString);
      CertPath localCertPath = localCertificateFactory.generateCertPath(new ByteArrayInputStream(arrayOfByte));
      if (localCertPath != null)
        return new CachedCertificatesHelper(paramDate, localCertPath, paramBoolean);
    }
    catch (Exception localException)
    {
      Object localObject = ResourceManager.getString("launch.error.embedded.cert", localException.getLocalizedMessage());
      if (DEBUG)
        Trace.println((String)localObject, TraceLevel.SECURITY);
    }
    return (CachedCertificatesHelper)null;
  }

  public boolean isSignedJNLP()
  {
    return this.signedJNLP;
  }

  public CertPath getCertPath()
  {
    return this.path;
  }

  public String exportCertificatesToBase64()
  {
    if (this.path != null)
      try
      {
        BASE64Encoder localBASE64Encoder = new BASE64Encoder();
        return localBASE64Encoder.encode(this.path.getEncoded());
      }
      catch (Exception localException)
      {
        Trace.println("Failed to export certificats in BASE64 form", TraceLevel.SECURITY);
      }
    return null;
  }

  public Date getTimestamp()
  {
    if (this.tm != null)
      return new Date(this.tm.getTime());
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CachedCertificatesHelper
 * JD-Core Version:    0.6.0
 */