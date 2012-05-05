package com.sun.jnlp;

import com.sun.deploy.util.DeployRMIClassLoaderSpi;

public final class JNLPRMIClassLoaderSpi extends DeployRMIClassLoaderSpi
{
  protected boolean useRMIServerCodebaseForClass(Class paramClass)
  {
    return (paramClass != null) && ((paramClass.getClassLoader() instanceof JNLPClassLoader));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.JNLPRMIClassLoaderSpi
 * JD-Core Version:    0.6.0
 */