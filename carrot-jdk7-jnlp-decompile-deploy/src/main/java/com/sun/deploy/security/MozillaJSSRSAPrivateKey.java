package com.sun.deploy.security;

class MozillaJSSRSAPrivateKey extends MozillaJSSPrivateKey
{
  MozillaJSSRSAPrivateKey(Object paramObject, int paramInt)
  {
    super(paramObject, paramInt);
  }

  public String getAlgorithm()
  {
    return "RSA";
  }

  public String toString()
  {
    return "MozillaJSSRSAPrivateKey [JSSKey=" + this.key + ", key length=" + this.keyLength + "bits]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.MozillaJSSRSAPrivateKey
 * JD-Core Version:    0.6.0
 */