package com.sun.deploy.security;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

public abstract class DeployURLClassPathCallback
{
  public abstract Element openClassPathElement(URL paramURL)
    throws IOException;

  public abstract Element openClassPathElement(JarFile paramJarFile, URL paramURL)
    throws IOException;

  public static class Element
  {
    protected JarFile jar;
    protected URL url;

    public Element(JarFile paramJarFile, URL paramURL)
    {
      this.jar = paramJarFile;
      this.url = paramURL;
    }

    public Element(URL paramURL)
    {
      this(null, paramURL);
    }

    public void checkResource(String paramString)
    {
      throw new SecurityException("checkResource() method not implemented");
    }

    public boolean skip()
    {
      return false;
    }

    public boolean defer()
    {
      return false;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.DeployURLClassPathCallback
 * JD-Core Version:    0.6.0
 */