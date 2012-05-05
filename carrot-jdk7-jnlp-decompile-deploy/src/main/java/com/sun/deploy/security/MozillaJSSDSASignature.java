package com.sun.deploy.security;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public abstract class MozillaJSSDSASignature extends SignatureSpi
{
  private MozillaJSSDSAPrivateKey privateKey = null;

  protected abstract void update(byte[] paramArrayOfByte, int paramInt1, int paramInt2);

  protected abstract byte[] getDigest()
    throws SignatureException;

  protected abstract void resetDigest();

  private Object getJSSSignature(Object paramObject)
    throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException
  {
    Class localClass1 = Class.forName("org.mozilla.jss.crypto.CryptoToken", true, ClassLoader.getSystemClassLoader());
    Class localClass2 = Class.forName("org.mozilla.jss.crypto.SignatureAlgorithm", true, ClassLoader.getSystemClassLoader());
    Class[] arrayOfClass = { localClass2 };
    Method localMethod = localClass1.getMethod("getSignatureContext", arrayOfClass);
    Field localField = localClass2.getField("DSASignature");
    Object[] arrayOfObject = new Object[1];
    arrayOfObject[0] = localField.get(null);
    return localMethod.invoke(paramObject, arrayOfObject);
  }

  protected void engineInitVerify(PublicKey paramPublicKey)
    throws InvalidKeyException
  {
    throw new InvalidKeyException("Key not supported");
  }

  protected void engineInitSign(PrivateKey paramPrivateKey)
    throws InvalidKeyException
  {
    if (!(paramPrivateKey instanceof MozillaJSSDSAPrivateKey))
      throw new InvalidKeyException("Key not supported");
    this.privateKey = ((MozillaJSSDSAPrivateKey)paramPrivateKey);
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
    try
    {
      byte[] arrayOfByte1 = getDigest();
      localObject1 = this.privateKey.getJSSPrivateKey();
      Class localClass1 = Class.forName("org.mozilla.jss.crypto.PrivateKey", true, ClassLoader.getSystemClassLoader());
      Method localMethod1 = localClass1.getMethod("getOwningToken", null);
      Object localObject2 = localMethod1.invoke(localObject1, null);
      Object localObject3 = getJSSSignature(localObject2);
      Class localClass2 = Class.forName("org.mozilla.jss.crypto.Signature", true, ClassLoader.getSystemClassLoader());
      Class[] arrayOfClass1 = { localClass1 };
      Method localMethod2 = localClass2.getMethod("initSign", arrayOfClass1);
      Object[] arrayOfObject1 = { localObject1 };
      Object localObject4 = localMethod2.invoke(localObject3, arrayOfObject1);
      Class[] arrayOfClass2 = { new byte[0].getClass() };
      Method localMethod3 = localClass2.getMethod("update", arrayOfClass2);
      Object[] arrayOfObject2 = { arrayOfByte1 };
      Object localObject5 = localMethod3.invoke(localObject3, arrayOfObject2);
      Method localMethod4 = localClass2.getMethod("sign", null);
      byte[] arrayOfByte2 = (byte[])(byte[])localMethod4.invoke(localObject3, null);
      byte[] arrayOfByte3 = new byte[20];
      byte[] arrayOfByte4 = new byte[20];
      System.arraycopy(arrayOfByte2, 0, arrayOfByte3, 0, 20);
      System.arraycopy(arrayOfByte2, 20, arrayOfByte4, 0, 20);
      BigInteger localBigInteger1 = new BigInteger(arrayOfByte3);
      BigInteger localBigInteger2 = new BigInteger(arrayOfByte4);
      DerOutputStream localDerOutputStream = new DerOutputStream(100);
      localDerOutputStream.putInteger(localBigInteger1);
      localDerOutputStream.putInteger(localBigInteger2);
      DerValue localDerValue = new DerValue(48, localDerOutputStream.toByteArray());
      arrayOfByte5 = localDerValue.toByteArray();
    }
    catch (SignatureException localSignatureException)
    {
      byte[] arrayOfByte5;
      throw localSignatureException;
    }
    catch (Throwable localThrowable)
    {
      Object localObject1 = new SignatureException("Error generating signature.");
      ((SignatureException)localObject1).initCause(localThrowable);
      throw ((Throwable)localObject1);
    }
    finally
    {
      resetDigest();
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

  public static class NONEwithDSA extends MozillaJSSDSASignature
  {
    private static final int SHA1_LEN = 20;
    private final byte[] digestBuffer = new byte[20];
    private int offset;

    protected void update(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    {
      if ((paramInt2 == 0) || (paramArrayOfByte == null))
        return;
      if (this.offset + paramInt2 > 20)
      {
        this.offset = 21;
        return;
      }
      System.arraycopy(paramArrayOfByte, paramInt1, this.digestBuffer, this.offset, paramInt2);
      this.offset += paramInt2;
    }

    protected byte[] getDigest()
      throws SignatureException
    {
      if (this.offset != 20)
        throw new SignatureException("Data for RawDSA must be exactly 20 bytes long");
      this.offset = 0;
      return this.digestBuffer;
    }

    protected void resetDigest()
    {
      this.offset = 0;
    }
  }

  public static class SHA1withDSA extends MozillaJSSDSASignature
  {
    private final MessageDigest dataSHA = MessageDigest.getInstance("SHA-1");

    public SHA1withDSA()
      throws NoSuchAlgorithmException
    {
      this.dataSHA.reset();
    }

    protected void update(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    {
      if ((paramInt2 == 0) || (paramArrayOfByte == null))
        return;
      this.dataSHA.update(paramArrayOfByte, paramInt1, paramInt2);
    }

    protected byte[] getDigest()
    {
      return this.dataSHA.digest();
    }

    protected void resetDigest()
    {
      this.dataSHA.reset();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.MozillaJSSDSASignature
 * JD-Core Version:    0.6.0
 */