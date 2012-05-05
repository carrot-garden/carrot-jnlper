package com.sun.javaws.security;

import com.sun.deploy.net.CrossDomainXML;
import com.sun.jnlp.ApiDialog;
import com.sun.jnlp.JNLPClassLoaderIf;
import com.sun.jnlp.PrintServiceImpl;
import java.net.MalformedURLException;
import java.net.URL;

public class JavaWebStartSecurity extends SecurityManager
{
  private ApiDialog _connect;
  private ApiDialog _accept;

  private JNLPClassLoaderIf currentJNLPClassLoader()
  {
    Class[] arrayOfClass = getClassContext();
    for (int i = 0; i < arrayOfClass.length; i++)
    {
      localClassLoader = arrayOfClass[i].getClassLoader();
      if ((localClassLoader instanceof JNLPClassLoaderIf))
        return (JNLPClassLoaderIf)localClassLoader;
    }
    ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
    if ((localClassLoader instanceof JNLPClassLoaderIf))
      return (JNLPClassLoaderIf)localClassLoader;
    return (JNLPClassLoaderIf)null;
  }

  public void checkAwtEventQueueAccess()
  {
    if ((!AppContextUtil.isApplicationAppContext()) && (currentJNLPClassLoader() != null))
      super.checkAwtEventQueueAccess();
  }

  public Class[] getExecutionStackContext()
  {
    return super.getClassContext();
  }

  public void checkPrintJobAccess()
  {
    try
    {
      super.checkPrintJobAccess();
    }
    catch (SecurityException localSecurityException)
    {
      if (PrintServiceImpl.requestPrintPermission())
        return;
      throw localSecurityException;
    }
  }

  public void checkConnect(String paramString, int paramInt)
  {
    URL localURL = null;
    int i = paramInt < 0 ? paramInt : -4;
    try
    {
      if ((i == -2) || (i == -3))
      {
        localURL = new URL(paramString);
        paramString = localURL.getHost();
        paramInt = localURL.getPort();
        if (paramInt == -1)
          paramInt = localURL.getDefaultPort();
      }
      if (CrossDomainXML.quickCheck(localURL, paramString, paramInt, i))
        return;
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    try
    {
      super.checkConnect(paramString, paramInt);
    }
    catch (SecurityException localSecurityException)
    {
      if (CrossDomainXML.check(getClassContext(), localURL, paramString, paramInt, i))
        return;
      if (this._connect == null)
        this._connect = new ApiDialog();
      if (this._connect.askConnect(paramString))
        return;
      throw localSecurityException;
    }
  }

  public void checkConnect(String paramString, int paramInt, Object paramObject)
  {
    URL localURL = null;
    int i = paramInt < 0 ? paramInt : -4;
    try
    {
      if ((i == -2) || (i == -3))
      {
        localURL = new URL(paramString);
        paramString = localURL.getHost();
        paramInt = localURL.getPort();
        if (paramInt == -1)
          paramInt = localURL.getDefaultPort();
      }
      if (CrossDomainXML.quickCheck(localURL, paramString, paramInt, i))
        return;
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    try
    {
      super.checkConnect(paramString, paramInt, paramObject);
    }
    catch (SecurityException localSecurityException)
    {
      if (CrossDomainXML.check(getClassContext(), localURL, paramString, paramInt, i))
        return;
      if (this._connect == null)
        this._connect = new ApiDialog();
      if (this._connect.askConnect(paramString))
        return;
      throw localSecurityException;
    }
  }

  public void checkAccept(String paramString, int paramInt)
  {
    try
    {
      super.checkAccept(paramString, paramInt);
    }
    catch (SecurityException localSecurityException)
    {
      if (this._accept == null)
        this._accept = new ApiDialog();
      if (this._accept.askAccept(paramString))
        return;
      throw localSecurityException;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.security.JavaWebStartSecurity
 * JD-Core Version:    0.6.0
 */