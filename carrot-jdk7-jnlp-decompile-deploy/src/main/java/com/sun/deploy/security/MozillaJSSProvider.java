package com.sun.deploy.security;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;

public final class MozillaJSSProvider extends Provider
{
  private static final String info = "SunDeploy-MozillaJSS Provider (implements RSA)";

  public MozillaJSSProvider()
  {
    super("SunDeploy-MozillaJSS", 1.5D, "SunDeploy-MozillaJSS Provider (implements RSA)");
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        MozillaJSSProvider.this.put("Signature.NONEwithRSA", "com.sun.deploy.security.MozillaJSSNONEwithRSASignature");
        MozillaJSSProvider.this.put("Signature.DSA", "com.sun.deploy.security.MozillaJSSDSASignature$SHA1withDSA");
        MozillaJSSProvider.this.put("Signature.RawDSA", "com.sun.deploy.security.MozillaJSSDSASignature$NONEwithDSA");
        MozillaJSSProvider.this.put("KeyStore.MozillaMy", "com.sun.deploy.security.MozillaMyKeyStore");
        return null;
      }
    });
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.MozillaJSSProvider
 * JD-Core Version:    0.6.0
 */