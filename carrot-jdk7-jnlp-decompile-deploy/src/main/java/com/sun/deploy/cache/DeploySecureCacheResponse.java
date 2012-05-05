package com.sun.deploy.cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.SecureCacheResponse;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLPeerUnverifiedException;

class DeploySecureCacheResponse extends SecureCacheResponse
{
  protected InputStream is;
  protected Map headers;

  DeploySecureCacheResponse(InputStream paramInputStream, Map paramMap)
  {
    this.is = paramInputStream;
    this.headers = paramMap;
  }

  public InputStream getBody()
    throws IOException
  {
    return this.is;
  }

  public Map getHeaders()
    throws IOException
  {
    return this.headers;
  }

  public String getCipherSuite()
  {
    return null;
  }

  public List getLocalCertificateChain()
  {
    return null;
  }

  public List getServerCertificateChain()
    throws SSLPeerUnverifiedException
  {
    return null;
  }

  public Principal getPeerPrincipal()
    throws SSLPeerUnverifiedException
  {
    return null;
  }

  public Principal getLocalPrincipal()
  {
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.DeploySecureCacheResponse
 * JD-Core Version:    0.6.0
 */