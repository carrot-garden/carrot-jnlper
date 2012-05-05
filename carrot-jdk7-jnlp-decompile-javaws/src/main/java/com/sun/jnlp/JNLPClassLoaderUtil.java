package com.sun.jnlp;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;

public class JNLPClassLoaderUtil
{
  public static JNLPClassLoaderIf getInstance()
  {
    ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
    if ((localClassLoader instanceof JNLPClassLoaderIf))
      return (JNLPClassLoaderIf)localClassLoader;
    JNLPClassLoaderIf localJNLPClassLoaderIf = JNLPClassLoader.getInstance();
    if ((localJNLPClassLoaderIf == null) && (Config.getDeployDebug()))
      Trace.println("JNLPClassLoaderUtil: couldn't find a valid JNLPClassLoaderIf", TraceLevel.NETWORK);
    return localJNLPClassLoaderIf;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.JNLPClassLoaderUtil
 * JD-Core Version:    0.6.0
 */