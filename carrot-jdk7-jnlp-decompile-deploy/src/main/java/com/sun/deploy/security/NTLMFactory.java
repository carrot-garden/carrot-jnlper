package com.sun.deploy.security;

import sun.net.www.protocol.http.ntlm.NTLMAuthenticationCallback;

public class NTLMFactory
{
  public static NTLMAuthenticationCallback newInstance()
  {
    return new UnixDeployNTLMAuthCallback();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.NTLMFactory
 * JD-Core Version:    0.6.0
 */