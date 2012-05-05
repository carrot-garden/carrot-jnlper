package com.sun.deploy.security;

import java.security.PrivateKey;

abstract class MozillaJSSPrivateKey
  implements PrivateKey
{
  protected Object key = null;
  protected int keyLength = 0;

  protected MozillaJSSPrivateKey(Object paramObject, int paramInt)
  {
    this.key = paramObject;
    this.keyLength = paramInt;
  }

  public int bitLength()
  {
    return this.keyLength;
  }

  public Object getJSSPrivateKey()
  {
    return this.key;
  }

  public abstract String getAlgorithm();

  public String getFormat()
  {
    return null;
  }

  public byte[] getEncoded()
  {
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.MozillaJSSPrivateKey
 * JD-Core Version:    0.6.0
 */