package com.sun.deploy.net;

import com.sun.deploy.trace.Trace;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sun.net.www.protocol.http.HttpURLConnection;

public class CrossDomainXML
{
  private static final String ALT_XDOMAIN_FILES = "jnlp.altCrossDomainXMLFiles";
  public static final int CHECK_RESOLVE = -1;
  public static final int CHECK_SET_HOST = -2;
  public static final int CHECK_SUBPATH = -3;
  public static final int CHECK_CONNECT = -4;
  private static PermissionCollection allowedSocketHosts;
  private static PermissionCollection allowedURLHosts;
  private static final Map allowedURLs = new HashMap();
  private static final Set processedHosts = new HashSet();
  private static List alternateURLs;

  public static synchronized boolean quickCheck(URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    return (allowedSocketHosts != null) && (checkImplies(allowedSocketHosts, paramString, paramInt1, paramInt2));
  }

  public static boolean check(Class[] paramArrayOfClass, URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    int i = 0;
    try
    {
      synchronized (CrossDomainXML.class)
      {
        if (quickFullCheck(paramArrayOfClass, paramURL, paramString, paramInt1, paramInt2))
          return true;
        ArrayList localArrayList = new ArrayList();
        int j = 0;
        List localList = getAlternateURLs();
        if (!processedHosts.contains(paramString.toLowerCase()))
        {
          localArrayList.add(new URL("http", paramString, "/crossdomain.xml"));
          processedHosts.add(paramString.toLowerCase());
        }
        Iterator localIterator = localList.iterator();
        URL localURL;
        while (localIterator.hasNext())
        {
          localURL = (URL)localIterator.next();
          if (paramString.equalsIgnoreCase(localURL.getHost()))
          {
            localArrayList.add(localURL);
            localIterator.remove();
          }
        }
        localIterator = localArrayList.iterator();
        while (localIterator.hasNext())
        {
          localURL = (URL)localIterator.next();
          if (!check(localURL))
            continue;
          String str = paramString;
          int k = localURL.getPort();
          if (k == -1)
            k = localURL.getDefaultPort();
          if ((!str.startsWith("[")) && (str.indexOf(':') != -1))
            str = "[" + str + "]";
          if (localURL.getPath().equals("/crossdomain.xml"))
          {
            localObject1 = new SocketPermission(str + ":1024-", "connect,resolve");
            if (allowedSocketHosts == null)
              allowedSocketHosts = ((SocketPermission)localObject1).newPermissionCollection();
            allowedSocketHosts.add((Permission)localObject1);
          }
          Object localObject1 = (List)allowedURLs.get(paramString.toLowerCase());
          if (localObject1 == null)
            localObject1 = new ArrayList();
          ((List)localObject1).add(localURL);
          allowedURLs.put(paramString.toLowerCase(), localObject1);
          SocketPermission localSocketPermission = new SocketPermission(str + ":" + k, "connect,resolve");
          if (allowedURLHosts == null)
            allowedURLHosts = localSocketPermission.newPermissionCollection();
          allowedURLHosts.add(localSocketPermission);
          j = 1;
        }
        if ((j != 0) && (quickFullCheck(paramArrayOfClass, paramURL, paramString, paramInt1, paramInt2)))
          return true;
      }
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    catch (Throwable localThrowable)
    {
      if (Trace.isEnabled())
        localThrowable.printStackTrace();
      if ((localThrowable instanceof ThreadDeath))
        throw ((ThreadDeath)localThrowable);
    }
    return false;
  }

  private static boolean quickFullCheck(Class[] paramArrayOfClass, URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    if (quickCheck(paramURL, paramString, paramInt1, paramInt2))
      return true;
    if (paramInt2 == -2)
      return false;
    if ((paramInt2 == -3) && (checkSubpath(paramURL, paramString, paramInt1)))
      return true;
    if (((paramInt2 == -1) || (paramInt2 == -4)) && (checkContext(paramArrayOfClass, HttpURLConnection.class)))
      return (allowedURLHosts != null) && (checkImplies(allowedURLHosts, paramString, paramInt1, paramInt2));
    return false;
  }

  private static boolean checkImplies(PermissionCollection paramPermissionCollection, String paramString, int paramInt1, int paramInt2)
  {
    if ((!paramString.startsWith("[")) && (paramString.indexOf(':') != -1))
      paramString = "[" + paramString + "]";
    SocketPermission localSocketPermission;
    if (paramInt2 == -1)
      localSocketPermission = new SocketPermission(paramString, "resolve");
    else
      localSocketPermission = new SocketPermission(paramString + ":" + paramInt1, "connect");
    return paramPermissionCollection.implies(localSocketPermission);
  }

  private static boolean checkSubpath(URL paramURL, String paramString, int paramInt)
  {
    List localList = (List)allowedURLs.get(paramString.toLowerCase());
    if (localList == null)
      return false;
    String str1 = paramURL.getPath();
    if (str1 == "")
      str1 = "/";
    Iterator localIterator = localList.iterator();
    while (localIterator.hasNext())
    {
      URL localURL = (URL)localIterator.next();
      int i = localURL.getPort();
      if (i == -1)
        i = localURL.getDefaultPort();
      if (paramInt == i)
      {
        String str2 = localURL.getPath();
        if (str2 == "")
          str2 = "/";
        int j = str2.lastIndexOf('/');
        if (j != -1)
        {
          String str3 = str2.substring(0, j + 1);
          if (str1.startsWith(str3))
            return true;
        }
      }
    }
    return false;
  }

  private static boolean checkContext(Class[] paramArrayOfClass, Class paramClass)
  {
    for (int i = 0; i < paramArrayOfClass.length; i++)
    {
      if (paramArrayOfClass[i].getClassLoader() != null)
        return false;
      if (paramClass.isAssignableFrom(paramArrayOfClass[i]))
        return true;
    }
    return false;
  }

  private static List getAlternateURLs()
  {
    if (alternateURLs == null)
    {
      alternateURLs = new ArrayList();
      try
      {
        String str1 = (String)AccessController.doPrivileged(new PrivilegedAction()
        {
          public Object run()
          {
            return System.getProperty("jnlp.altCrossDomainXMLFiles");
          }
        });
        if (str1 != null)
        {
          String[] arrayOfString = str1.split(",");
          for (int i = 0; i < arrayOfString.length; i++)
          {
            String str2 = arrayOfString[i];
            if (str2 == null)
              continue;
            try
            {
              URL localURL = new URL(str2);
              if (("http".equalsIgnoreCase(localURL.getProtocol())) || ("https".equalsIgnoreCase(localURL.getProtocol())))
                alternateURLs.add(localURL);
            }
            catch (MalformedURLException localMalformedURLException)
            {
            }
          }
        }
      }
      catch (Throwable localThrowable)
      {
        if ((localThrowable instanceof ThreadDeath))
          throw ((ThreadDeath)localThrowable);
      }
    }
    return alternateURLs;
  }

  private static boolean check(URL paramURL)
  {
    try
    {
      Handler localHandler = new Handler(null);
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramURL, localHandler)
      {
        private final URL val$url;
        private final CrossDomainXML.Handler val$handler;

        public Object run()
          throws Exception
        {
          try
          {
            URLConnection localURLConnection = this.val$url.openConnection();
            localURLConnection.connect();
            BufferedInputStream localBufferedInputStream = new BufferedInputStream(localURLConnection.getInputStream());
            SAXParserFactory localSAXParserFactory = SAXParserFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", null);
            SAXParser localSAXParser = localSAXParserFactory.newSAXParser();
            localSAXParser.parse(localBufferedInputStream, this.val$handler);
            localBufferedInputStream.close();
          }
          catch (FileNotFoundException localFileNotFoundException)
          {
          }
          catch (NoSuchMethodError localNoSuchMethodError)
          {
            if (Trace.isEnabled())
              Trace.msgNetPrintln("CrossDomainXML: cannot parse crossdomain.xml. You may be running in a JRE older than version 6.0");
          }
          return null;
        }
      });
      return localHandler.isAllowed();
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    catch (Throwable localThrowable)
    {
      if (Trace.isEnabled())
        localThrowable.printStackTrace();
    }
    return false;
  }

  private static boolean checkFile(String paramString)
  {
    try
    {
      Handler localHandler = new Handler(null);
      AccessController.doPrivileged(new PrivilegedExceptionAction(paramString, localHandler)
      {
        private final String val$filename;
        private final CrossDomainXML.Handler val$handler;

        public Object run()
          throws Exception
        {
          try
          {
            SAXParserFactory localSAXParserFactory = SAXParserFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", null);
            SAXParser localSAXParser = localSAXParserFactory.newSAXParser();
            localSAXParser.parse(new File(this.val$filename), this.val$handler);
          }
          catch (FileNotFoundException localFileNotFoundException)
          {
          }
          catch (NoSuchMethodError localNoSuchMethodError)
          {
            if (Trace.isEnabled())
              Trace.msgNetPrintln("CrossDomainXML: cannot parse crossdomain.xml. You may be running in a JRE older than version 6.0");
          }
          return null;
        }
      });
      return localHandler.isAllowed();
    }
    catch (Exception localException)
    {
      Trace.ignoredException(localException);
    }
    catch (Throwable localThrowable)
    {
      if (Trace.isEnabled())
        localThrowable.printStackTrace();
    }
    return false;
  }

  public static void main(String[] paramArrayOfString)
  {
    for (int i = 0; i < paramArrayOfString.length; i++)
      System.out.println(paramArrayOfString[i] + ": " + (check(new Class[0], null, paramArrayOfString[i], -1, -1) ? "Allowed" : "Denied"));
  }

  private static class Handler extends DefaultHandler
  {
    private static final int INITIAL = 0;
    private static final int IN_CROSS_DOMAIN_POLICY = 1;
    private static final int ALLOWED = 2;
    private static final int DENIED = 3;
    private static final int UNKNOWN = 4;
    private int depth = 0;
    private int state = 0;
    private int result = 4;

    private Handler()
    {
    }

    public void startElement(String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
      throws SAXException
    {
      this.depth += 1;
      switch (this.state)
      {
      case 0:
        if ((this.depth != 1) || (!paramString3.equals("cross-domain-policy")))
          break;
        this.state = 1;
        break;
      case 1:
        if (this.depth != 2)
          break;
        if (paramString3.equals("allow-access-from"))
        {
          String str = paramAttributes.getValue("domain");
          if ((str.equals("*")) && (this.result == 4))
            this.result = 2;
          else
            this.result = 3;
        }
        else
        {
          this.state = 0;
        }
        break;
      }
    }

    public void endElement(String paramString1, String paramString2, String paramString3)
      throws SAXException
    {
      this.depth -= 1;
    }

    public boolean isAllowed()
    {
      return this.result == 2;
    }

    Handler(CrossDomainXML.1 param1)
    {
      this();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.CrossDomainXML
 * JD-Core Version:    0.6.0
 */