package sun.net.www.protocol.http;

import com.sun.deploy.security.AuthKey;

public class AuthCacheBridge
  implements AuthKey
{
  AuthenticationInfo cacheValue;

  public AuthCacheBridge(AuthCacheValue paramAuthCacheValue)
  {
    this.cacheValue = ((AuthenticationInfo)paramAuthCacheValue);
  }

  public boolean isProxy()
  {
    return this.cacheValue.getAuthType() == AuthCacheValue.Type.Proxy;
  }

  public String getProtocolScheme()
  {
    return this.cacheValue.getProtocolScheme();
  }

  public int getPort()
  {
    return this.cacheValue.getPort();
  }

  public String getHost()
  {
    return this.cacheValue.getHost();
  }

  public String getPath()
  {
    return this.cacheValue.getPath();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     sun.net.www.protocol.http.AuthCacheBridge
 * JD-Core Version:    0.6.0
 */