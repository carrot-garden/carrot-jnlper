package com.sun.deploy.security;

public abstract interface AuthKey
{
  public abstract boolean isProxy();

  public abstract String getProtocolScheme();

  public abstract String getHost();

  public abstract int getPort();

  public abstract String getPath();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.AuthKey
 * JD-Core Version:    0.6.0
 */