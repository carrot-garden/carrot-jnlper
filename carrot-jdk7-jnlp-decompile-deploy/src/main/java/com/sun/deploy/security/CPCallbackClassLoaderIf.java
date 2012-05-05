package com.sun.deploy.security;

import java.security.CodeSource;

public abstract interface CPCallbackClassLoaderIf
{
  public abstract CodeSource[] getTrustedCodeSources(CodeSource[] paramArrayOfCodeSource);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CPCallbackClassLoaderIf
 * JD-Core Version:    0.6.0
 */