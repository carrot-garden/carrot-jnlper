package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.CodeSigner;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Locale;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.pkcs.PKCS9Attributes;
import sun.security.pkcs.ParsingException;
import sun.security.pkcs.SignerInfo;
import sun.security.timestamp.TimestampToken;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

public class JarSignature
{
  public static String BLOB_SIGNATURE = "META-INF/SIGNATURE.BSF";
  private final Signature sig;
  private final X509Certificate[] certChain;
  private final CodeSigner[] codeSigners;
  private final SignerInfo[] signerInfos;

  public static JarSignature load(byte[] paramArrayOfByte)
    throws ParsingException, CertificateException, IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException
  {
    PKCS7 localPKCS7 = new PKCS7(paramArrayOfByte);
    SignerInfo[] arrayOfSignerInfo = localPKCS7.getSignerInfos();
    if ((arrayOfSignerInfo == null) || (arrayOfSignerInfo.length != 1))
      throw new IllegalArgumentException("BLOB signature currently only support single signer.");
    X509Certificate localX509Certificate = arrayOfSignerInfo[0].getCertificate(localPKCS7);
    PublicKey localPublicKey = localX509Certificate.getPublicKey();
    CodeSigner[] arrayOfCodeSigner = extractCodeSigners(arrayOfSignerInfo, localPKCS7);
    Signature localSignature = getSignature(arrayOfSignerInfo[0]);
    localSignature.initVerify(localPublicKey);
    return new JarSignature(localSignature, arrayOfSignerInfo, arrayOfCodeSigner);
  }

  public static JarSignature create(PrivateKey paramPrivateKey, X509Certificate[] paramArrayOfX509Certificate)
    throws NoSuchAlgorithmException, InvalidKeyException
  {
    Signature localSignature = getSignature(paramPrivateKey.getAlgorithm());
    localSignature.initSign(paramPrivateKey);
    return new JarSignature(localSignature, paramArrayOfX509Certificate);
  }

  private JarSignature(Signature paramSignature, X509Certificate[] paramArrayOfX509Certificate)
  {
    this.certChain = paramArrayOfX509Certificate;
    this.signerInfos = null;
    this.codeSigners = null;
    this.sig = paramSignature;
  }

  private JarSignature(Signature paramSignature, SignerInfo[] paramArrayOfSignerInfo, CodeSigner[] paramArrayOfCodeSigner)
  {
    this.certChain = null;
    this.signerInfos = paramArrayOfSignerInfo;
    this.codeSigners = paramArrayOfCodeSigner;
    this.sig = paramSignature;
  }

  public boolean isValidationMode()
  {
    return this.certChain == null;
  }

  private static Signature getSignature(String paramString)
    throws NoSuchAlgorithmException
  {
    if (paramString.equalsIgnoreCase("DSA"))
      return Signature.getInstance("SHA1withDSA");
    if (paramString.equalsIgnoreCase("RSA"))
      return Signature.getInstance("SHA256withRSA");
    if (paramString.equalsIgnoreCase("EC"))
      return Signature.getInstance("SHA256withECDSA");
    throw new IllegalArgumentException("Key algorithm should be either DSA, RSA or EC");
  }

  private static Signature getSignature(SignerInfo paramSignerInfo)
    throws NoSuchAlgorithmException
  {
    String str1 = paramSignerInfo.getDigestAlgorithmId().getName();
    String str2 = paramSignerInfo.getDigestEncryptionAlgorithmId().getName();
    String str3 = makeSigAlg(str1, str2);
    return Signature.getInstance(str3);
  }

  String getSignatureAlgorithm()
    throws NoSuchAlgorithmException
  {
    return this.sig.getAlgorithm();
  }

  AlgorithmId getDigestAlgorithm()
    throws NoSuchAlgorithmException
  {
    String str = getDigAlgFromSigAlg(this.sig.getAlgorithm());
    return str != null ? AlgorithmId.get(str) : null;
  }

  AlgorithmId getKeyAlgorithm()
    throws NoSuchAlgorithmException
  {
    String str = getEncAlgFromSigAlg(this.sig.getAlgorithm());
    return str != null ? AlgorithmId.get(str) : null;
  }

  public byte[] getEncoded()
    throws NoSuchAlgorithmException, SignatureException, IOException
  {
    if (isValidationMode())
      throw new UnsupportedOperationException("Method is not for validation mode.");
    AlgorithmId localAlgorithmId = getDigestAlgorithm();
    AlgorithmId[] arrayOfAlgorithmId = { localAlgorithmId };
    ContentInfo localContentInfo = new ContentInfo(ContentInfo.DATA_OID, null);
    Principal localPrincipal = this.certChain[0].getIssuerDN();
    BigInteger localBigInteger = this.certChain[0].getSerialNumber();
    byte[] arrayOfByte = this.sig.sign();
    SignerInfo localSignerInfo = new SignerInfo((X500Name)localPrincipal, localBigInteger, localAlgorithmId, getKeyAlgorithm(), arrayOfByte);
    SignerInfo[] arrayOfSignerInfo = { localSignerInfo };
    PKCS7 localPKCS7 = new PKCS7(arrayOfAlgorithmId, localContentInfo, this.certChain, arrayOfSignerInfo);
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(8192);
    localPKCS7.encodeSignedData(localByteArrayOutputStream);
    return localByteArrayOutputStream.toByteArray();
  }

  public InputStream updateWithZipEntry(String paramString, InputStream paramInputStream)
    throws SignatureException
  {
    try
    {
      this.sig.update(paramString.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new SignatureException(localUnsupportedEncodingException);
    }
    return new ValidationStream(paramInputStream);
  }

  public void update(byte[] paramArrayOfByte)
    throws SignatureException
  {
    this.sig.update(paramArrayOfByte);
  }

  public boolean isValid()
  {
    try
    {
      return this.sig.verify(this.signerInfos[0].getEncryptedDigest());
    }
    catch (Exception localException)
    {
      if ((Config.getDeployDebug()) || (Config.getPluginDebug()))
        Trace.ignored(localException);
    }
    return false;
  }

  public CodeSigner[] getCodeSigners()
  {
    return this.codeSigners;
  }

  private static CodeSigner[] extractCodeSigners(SignerInfo[] paramArrayOfSignerInfo, PKCS7 paramPKCS7)
    throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException
  {
    ArrayList localArrayList1 = new ArrayList();
    CertificateFactory localCertificateFactory = CertificateFactory.getInstance("X509");
    for (int i = 0; i < paramArrayOfSignerInfo.length; i++)
    {
      SignerInfo localSignerInfo = paramArrayOfSignerInfo[i];
      ArrayList localArrayList2 = localSignerInfo.getCertificateChain(paramPKCS7);
      CertPath localCertPath = localCertificateFactory.generateCertPath(localArrayList2);
      CodeSigner localCodeSigner = new CodeSigner(localCertPath, getTimestamp(localSignerInfo, localCertificateFactory));
      localArrayList1.add(localCodeSigner);
    }
    return (CodeSigner[])(CodeSigner[])localArrayList1.toArray(new CodeSigner[localArrayList1.size()]);
  }

  private static Timestamp getTimestamp(SignerInfo paramSignerInfo, CertificateFactory paramCertificateFactory)
    throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException
  {
    Timestamp localTimestamp = null;
    PKCS9Attributes localPKCS9Attributes = paramSignerInfo.getUnauthenticatedAttributes();
    if (localPKCS9Attributes != null)
    {
      PKCS9Attribute localPKCS9Attribute = localPKCS9Attributes.getAttribute("signatureTimestampToken");
      if (localPKCS9Attribute != null)
      {
        PKCS7 localPKCS7 = new PKCS7((byte[])(byte[])localPKCS9Attribute.getValue());
        byte[] arrayOfByte = localPKCS7.getContentInfo().getData();
        SignerInfo[] arrayOfSignerInfo = localPKCS7.verify(arrayOfByte);
        ArrayList localArrayList = arrayOfSignerInfo[0].getCertificateChain(localPKCS7);
        CertPath localCertPath = paramCertificateFactory.generateCertPath(localArrayList);
        TimestampToken localTimestampToken = new TimestampToken(arrayOfByte);
        localTimestamp = new Timestamp(localTimestampToken.getDate(), localCertPath);
      }
    }
    return localTimestamp;
  }

  private static String makeSigAlg(String paramString1, String paramString2)
  {
    paramString1 = paramString1.replace("-", "").toUpperCase(Locale.ENGLISH);
    if (paramString1.equalsIgnoreCase("SHA"))
      paramString1 = "SHA1";
    paramString2 = paramString2.toUpperCase(Locale.ENGLISH);
    if (paramString2.equals("EC"))
      paramString2 = "ECDSA";
    return paramString1 + "with" + paramString2;
  }

  private static String getDigAlgFromSigAlg(String paramString)
  {
    paramString = paramString.toUpperCase(Locale.ENGLISH);
    int i = paramString.indexOf("WITH");
    if (i > 0)
      return paramString.substring(0, i);
    return null;
  }

  private static String getEncAlgFromSigAlg(String paramString)
  {
    paramString = paramString.toUpperCase(Locale.ENGLISH);
    int i = paramString.indexOf("WITH");
    String str = null;
    if (i > 0)
    {
      int j = paramString.indexOf("AND", i + 4);
      if (j > 0)
        str = paramString.substring(i + 4, j);
      else
        str = paramString.substring(i + 4);
      if (str.equalsIgnoreCase("ECDSA"))
        str = "EC";
    }
    return str;
  }

  private class ValidationStream extends InputStream
  {
    InputStream dataStream = null;

    public ValidationStream(InputStream arg2)
    {
      Object localObject;
      this.dataStream = localObject;
    }

    public int read()
      throws IOException
    {
      int i = this.dataStream.read();
      if (i > -1)
        try
        {
          JarSignature.this.sig.update((byte)i);
        }
        catch (SignatureException localSignatureException)
        {
          if ((Config.getDeployDebug()) || (Config.getPluginDebug()))
            Trace.ignored(localSignatureException);
        }
      return i;
    }

    public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
      throws IOException
    {
      paramInt2 = this.dataStream.read(paramArrayOfByte, paramInt1, paramInt2);
      if (paramInt2 > 0)
        try
        {
          JarSignature.this.sig.update(paramArrayOfByte, paramInt1, paramInt2);
        }
        catch (SignatureException localSignatureException)
        {
          if ((Config.getDeployDebug()) || (Config.getPluginDebug()))
            Trace.ignored(localSignatureException);
        }
      return paramInt2;
    }

    public void close()
      throws IOException
    {
      this.dataStream.close();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.JarSignature
 * JD-Core Version:    0.6.0
 */