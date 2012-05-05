package com.sun.deploy.security;

public final class MozillaSSLRootCertStore extends MozillaCertStore
{
  protected String getName()
  {
    return "ROOT";
  }

  protected boolean isTrustedSigningCACertStore()
  {
    return false;
  }

  protected boolean isTrustedSSLCACertStore()
  {
    return true;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.MozillaSSLRootCertStore
 * JD-Core Version:    0.6.0
 */