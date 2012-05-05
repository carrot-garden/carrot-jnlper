package com.sun.deploy.security;

class MozillaJSSDSAPrivateKey extends MozillaJSSPrivateKey
{
  MozillaJSSDSAPrivateKey(Object paramObject, int paramInt)
  {
    super(paramObject, paramInt);
  }

  public String getAlgorithm()
  {
    return "DSA";
  }

  public String toString()
  {
    return "MozillaJSSDSAPrivateKey [JSSKey=" + this.key + ", key length=" + this.keyLength + "bits]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.MozillaJSSDSAPrivateKey
 * JD-Core Version:    0.6.0
 */