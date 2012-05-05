package com.sun.deploy.security;

import com.sun.deploy.trace.Trace;
import sun.net.www.protocol.http.ntlm.NTLMAuthenticationCallback;

public class DeployNTLMAuthCallback
{
  private static boolean callBackInstalled = false;

  public static synchronized void install()
  {
    try
    {
      Class localClass = Class.forName("sun.net.www.protocol.http.ntlm.NTLMAuthenticationCallback", false, null);
    }
    catch (Throwable localThrowable)
    {
      Trace.msgSecurityPrintln("net.authenticate.ntlm.callback.install.failed");
      return;
    }
    if (!callBackInstalled)
    {
      NTLMAuthenticationCallback.setNTLMAuthenticationCallback(NTLMFactory.newInstance());
      callBackInstalled = true;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.DeployNTLMAuthCallback
 * JD-Core Version:    0.6.0
 */