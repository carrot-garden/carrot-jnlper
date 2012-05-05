package com.sun.deploy.security;

import com.sun.deploy.trace.Trace;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import sun.security.rsa.RSACore;
import sun.security.rsa.RSAPadding;

public final class MozillaJSSNONEwithRSASignature extends SignatureSpi
{
  private byte[] buffer;
  private int bufOfs;
  private MozillaJSSRSAPrivateKey privateKey = null;

  protected void engineInitVerify(PublicKey paramPublicKey)
    throws InvalidKeyException
  {
    throw new InvalidKeyException("Key not supported");
  }

  protected void engineInitSign(PrivateKey paramPrivateKey)
    throws InvalidKeyException
  {
    if (!(paramPrivateKey instanceof MozillaJSSRSAPrivateKey))
      throw new InvalidKeyException("Key not supported");
    this.privateKey = ((MozillaJSSRSAPrivateKey)paramPrivateKey);
    int i = this.privateKey.bitLength() + 7 >> 3;
    if (i < 64)
      throw new InvalidKeyException("RSA keys should be at least 512 bits long");
    this.bufOfs = 0;
    try
    {
      RSAPadding localRSAPadding = RSAPadding.getInstance(1, i, this.appRandom);
      int j = localRSAPadding.getMaxDataSize();
      this.buffer = new byte[j];
    }
    catch (InvalidAlgorithmParameterException localInvalidAlgorithmParameterException)
    {
      Trace.securityPrintException(localInvalidAlgorithmParameterException);
    }
  }

  private void update(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if ((paramInt2 == 0) || (paramArrayOfByte == null))
      return;
    if (this.bufOfs + paramInt2 > this.buffer.length)
    {
      this.bufOfs = (this.buffer.length + 1);
      return;
    }
    System.arraycopy(paramArrayOfByte, paramInt1, this.buffer, this.bufOfs, paramInt2);
    this.bufOfs += paramInt2;
  }

  protected void engineUpdate(byte paramByte)
    throws SignatureException
  {
    byte[] arrayOfByte = new byte[1];
    arrayOfByte[0] = paramByte;
    update(arrayOfByte, 0, 1);
  }

  protected void engineUpdate(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws SignatureException
  {
    update(paramArrayOfByte, paramInt1, paramInt2);
  }

  protected byte[] engineSign()
    throws SignatureException
  {
    if (this.bufOfs > this.buffer.length)
      throw new SignatureException("Data must not be longer than " + this.buffer.length + " bytes");
    try
    {
      byte[] arrayOfByte1 = RSACore.convert(this.buffer, 0, this.bufOfs);
      Object localObject1 = null;
      try
      {
        Object localObject2 = this.privateKey.getJSSPrivateKey();
        Class localClass1 = Class.forName("org.mozilla.jss.crypto.PrivateKey", true, ClassLoader.getSystemClassLoader());
        Method localMethod1 = localClass1.getMethod("getOwningToken", null);
        Object localObject3 = localMethod1.invoke(localObject2, null);
        Class localClass2 = Class.forName("org.mozilla.jss.crypto.CryptoToken", true, ClassLoader.getSystemClassLoader());
        Class localClass3 = Class.forName("org.mozilla.jss.crypto.SignatureAlgorithm", true, ClassLoader.getSystemClassLoader());
        Class[] arrayOfClass1 = { localClass3 };
        Method localMethod2 = localClass2.getMethod("getSignatureContext", arrayOfClass1);
        Field localField = localClass3.getField("RSASignature");
        Object[] arrayOfObject1 = new Object[1];
        arrayOfObject1[0] = localField.get(this.privateKey);
        Object localObject4 = localMethod2.invoke(localObject3, arrayOfObject1);
        Class localClass4 = Class.forName("org.mozilla.jss.crypto.Signature", true, ClassLoader.getSystemClassLoader());
        Class[] arrayOfClass2 = { localClass1 };
        Method localMethod3 = localClass4.getMethod("initSign", arrayOfClass2);
        Object[] arrayOfObject2 = { localObject2 };
        Object localObject5 = localMethod3.invoke(localObject4, arrayOfObject2);
        Class[] arrayOfClass3 = { new byte[0].getClass() };
        Method localMethod4 = localClass4.getMethod("update", arrayOfClass3);
        Object[] arrayOfObject3 = { arrayOfByte1 };
        Object localObject6 = localMethod4.invoke(localObject4, arrayOfObject3);
        Method localMethod5 = localClass4.getMethod("sign", null);
        localObject1 = localMethod5.invoke(localObject4, null);
      }
      catch (Throwable localThrowable)
      {
        localThrowable.printStackTrace();
      }
      arrayOfByte2 = (byte[])(byte[])localObject1;
    }
    finally
    {
      byte[] arrayOfByte2;
      this.bufOfs = 0;
    }
  }

  protected boolean engineVerify(byte[] paramArrayOfByte)
    throws SignatureException
  {
    throw new SignatureException("Signature verification not supported");
  }

  /** @deprecated */
  protected void engineSetParameter(String paramString, Object paramObject)
    throws InvalidParameterException
  {
    throw new InvalidParameterException("Parameter not supported");
  }

  /** @deprecated */
  protected Object engineGetParameter(String paramString)
    throws InvalidParameterException
  {
    throw new InvalidParameterException("Parameter not supported");
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.MozillaJSSNONEwithRSASignature
 * JD-Core Version:    0.6.0
 */