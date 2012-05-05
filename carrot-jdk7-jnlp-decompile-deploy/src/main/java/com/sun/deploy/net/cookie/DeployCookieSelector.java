package com.sun.deploy.net.cookie;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.security.SecureCookiePermission;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeployCookieSelector extends java.net.CookieHandler
{
  private HashMap cookieTable = new HashMap();
  private CookieHandler cookieHandler = null;

  public static synchronized void reset()
  {
    java.net.CookieHandler.setDefault(new DeployCookieSelector());
  }

  private boolean canServeCookies(URI paramURI)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      try
      {
        localSecurityManager.checkPermission(new SecureCookiePermission(paramURI));
      }
      catch (SecurityException localSecurityException)
      {
        if ((paramURI != null) && (!paramURI.getScheme().toLowerCase().equals("http")))
        {
          Trace.msgSecurityPrintln("Possible use of 'Secure' cookies blocked for " + paramURI);
          Trace.msgSecurityPrintln("To use secure cookie (HTTPS), consider signing the application or host application with HTTPS");
        }
        return false;
      }
    return true;
  }

  public synchronized Map get(URI paramURI, Map paramMap)
    throws IOException
  {
    HashMap localHashMap = new HashMap();
    URI localURI = paramURI;
    if (!canServeCookies(localURI))
    {
      try
      {
        localURI = new URI("http", paramURI.getSchemeSpecificPart(), paramURI.getFragment());
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
      if (!canServeCookies(localURI))
        return localHashMap;
    }
    String str = getCookieInfo(localURI.toURL());
    if (str != null)
    {
      ArrayList localArrayList = new ArrayList();
      localArrayList.add(str);
      localHashMap.put("Cookie", localArrayList);
    }
    return localHashMap;
  }

  public synchronized void put(URI paramURI, Map paramMap)
    throws IOException
  {
    Iterator localIterator1 = paramMap.keySet().iterator();
    while (localIterator1.hasNext())
    {
      String str1 = (String)localIterator1.next();
      if ((str1 != null) && ((str1.equalsIgnoreCase("Set-Cookie")) || (str1.equalsIgnoreCase("Set-Cookie2"))))
      {
        List localList = (List)paramMap.get(str1);
        if (localList != null)
        {
          Iterator localIterator2 = localList.iterator();
          while (localIterator2.hasNext())
          {
            String str2 = (String)localIterator2.next();
            if (str2 != null)
              setCookieInfo(paramURI.toURL(), str2);
          }
        }
      }
    }
  }

  protected void setCookieInfo(URL paramURL, String paramString)
  {
    initializeImpl();
    if (Trace.isEnabled(TraceLevel.NETWORK))
      Trace.msgNetPrintln("net.cookie.server", new Object[] { paramURL, paramString });
    try
    {
      setCookieInBrowser(paramURL, paramString);
    }
    catch (CookieUnavailableException localCookieUnavailableException)
    {
      System.out.println(ResourceManager.getMessage("net.cookie.ignore.setcookie"));
    }
  }

  protected String getCookieInfo(URL paramURL)
  {
    initializeImpl();
    String str1 = null;
    try
    {
      String str2 = paramURL.getFile();
      int i = str2.lastIndexOf('/');
      if (i != -1)
        str2 = str2.substring(0, i);
      String str3 = paramURL.getProtocol() + "://" + paramURL.getHost() + str2;
      try
      {
        str1 = getCookieFromBrowser(paramURL);
        if ((str1 != null) && (!str1.equals("")) && (!str1.equals("\n")) && (!str1.equals("\r\n")))
        {
          this.cookieTable.put(str3, str1);
        }
        else
        {
          this.cookieTable.put(str3, "");
          str1 = null;
        }
      }
      catch (CookieUnavailableException localCookieUnavailableException)
      {
        if (Trace.isEnabled(TraceLevel.NETWORK))
          Trace.msgNetPrintln(ResourceManager.getMessage("net.cookie.noservice"));
        str1 = (String)this.cookieTable.get(str3);
      }
      if ((str1 != null) && (Trace.isEnabled(TraceLevel.NETWORK)))
        Trace.msgNetPrintln("net.cookie.connect", new Object[] { paramURL, str1 });
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
    return str1;
  }

  protected void initializeImpl()
  {
    if (this.cookieHandler == null)
    {
      Service localService = ServiceManager.getService();
      this.cookieHandler = localService.getCookieHandler();
    }
  }

  protected void setCookieInBrowser(URL paramURL, String paramString)
    throws CookieUnavailableException
  {
    if (this.cookieHandler != null)
      this.cookieHandler.setCookieInfo(paramURL, paramString);
  }

  protected String getCookieFromBrowser(URL paramURL)
    throws CookieUnavailableException
  {
    if (this.cookieHandler != null)
      return this.cookieHandler.getCookieInfo(paramURL);
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.cookie.DeployCookieSelector
 * JD-Core Version:    0.6.0
 */