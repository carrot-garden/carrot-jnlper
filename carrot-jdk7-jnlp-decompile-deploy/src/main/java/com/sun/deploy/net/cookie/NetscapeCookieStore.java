package com.sun.deploy.net.cookie;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeMap;

final class NetscapeCookieStore extends CookieStore
{
  private Date lastAccessDate = new Date(0L);
  private File cookieFile = null;

  NetscapeCookieStore(File paramFile)
  {
    this.cookieFile = paramFile;
  }

  protected void loadCookieJar()
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        NetscapeCookieStore.this.loadCookieJarFromStorage();
        return null;
      }
    });
  }

  private void loadCookieJarFromStorage()
  {
    if (this.cookieFile.exists())
    {
      Date localDate = new Date(this.cookieFile.lastModified());
      if (localDate.after(this.lastAccessDate))
      {
        TreeMap localTreeMap = new TreeMap();
        try
        {
          FileInputStream localFileInputStream = new FileInputStream(this.cookieFile);
          InputStreamReader localInputStreamReader = new InputStreamReader(localFileInputStream);
          BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader);
          String str1 = null;
          while ((str1 = localBufferedReader.readLine()) != null)
          {
            if ((str1.startsWith("#")) || (str1.trim().equals("")))
              continue;
            HttpCookie localHttpCookie = readCookieRecord(str1);
            if ((localHttpCookie != null) && (!localHttpCookie.hasExpired()))
            {
              if (shouldRejectCookie(localHttpCookie))
                continue;
              String str2 = localHttpCookie.getDomain().toLowerCase();
              ArrayList localArrayList = (ArrayList)localTreeMap.get(str2);
              if (localArrayList == null)
                localArrayList = new ArrayList();
              if (addOrReplaceCookie(localArrayList, localHttpCookie))
                localTreeMap.put(str2, localArrayList);
            }
          }
          localBufferedReader.close();
          localInputStreamReader.close();
          localFileInputStream.close();
          this.lastAccessDate = new Date(this.cookieFile.lastModified());
          this.cookieJar = localTreeMap;
        }
        catch (IOException localIOException)
        {
          localIOException.printStackTrace();
        }
        catch (Throwable localThrowable)
        {
          localThrowable.printStackTrace();
        }
      }
    }
  }

  protected void saveCookieJar()
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        NetscapeCookieStore.this.saveCookieJarToStorage();
        return null;
      }
    });
  }

  private void saveCookieJarToStorage()
  {
    try
    {
      String str = System.getProperty("line.separator");
      StringBuffer localStringBuffer = new StringBuffer();
      localStringBuffer.append("# Java Deployment HTTP Cookie File");
      localStringBuffer.append(str);
      localStringBuffer.append("# http://www.netscape.com/newsref/std/cookie_spec.html");
      localStringBuffer.append(str);
      localStringBuffer.append("# This is a generated file!  Do not edit.");
      localStringBuffer.append(str);
      localStringBuffer.append(str);
      Object localObject1 = this.cookieJar.values().iterator();
      while (((Iterator)localObject1).hasNext())
      {
        localObject2 = (List)((Iterator)localObject1).next();
        localObject3 = ((List)localObject2).iterator();
        while (((Iterator)localObject3).hasNext())
        {
          HttpCookie localHttpCookie = (HttpCookie)((Iterator)localObject3).next();
          if (!localHttpCookie.hasExpired())
          {
            writeCookieRecord(localHttpCookie, localStringBuffer);
            localStringBuffer.append(str);
          }
        }
      }
      this.cookieFile.getParentFile().mkdirs();
      localObject1 = new FileOutputStream(this.cookieFile);
      Object localObject2 = new BufferedOutputStream((OutputStream)localObject1);
      Object localObject3 = new PrintWriter((OutputStream)localObject2);
      ((PrintWriter)localObject3).println(localStringBuffer.toString());
      ((PrintWriter)localObject3).close();
      ((BufferedOutputStream)localObject2).close();
      ((FileOutputStream)localObject1).close();
      this.lastAccessDate = new Date(this.cookieFile.lastModified());
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
  }

  protected String getName()
  {
    return "Persistent Cookie Store";
  }

  protected boolean shouldRejectCookie(HttpCookie paramHttpCookie)
  {
    if (super.shouldRejectCookie(paramHttpCookie))
      return true;
    return (paramHttpCookie.getExpirationDate() == null) || (paramHttpCookie.hasExpired());
  }

  private HttpCookie readCookieRecord(String paramString)
  {
    try
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "\t", true);
      if (localStringTokenizer.countTokens() < 10)
        return null;
      String str1 = localStringTokenizer.nextToken();
      if (str1.indexOf(".") == -1)
        return null;
      localStringTokenizer.nextToken();
      String str2 = localStringTokenizer.nextToken();
      if (str2.equals("\t"))
        str2 = "FALSE";
      else
        localStringTokenizer.nextToken();
      String str3 = localStringTokenizer.nextToken();
      if (str3.equals("\t"))
        str3 = "/";
      else
        localStringTokenizer.nextToken();
      String str4 = localStringTokenizer.nextToken();
      if (str4.equals("\t"))
        str4 = "TRUE";
      else
        localStringTokenizer.nextToken();
      boolean bool = !str4.equalsIgnoreCase("false");
      Date localDate = new Date(new Long(localStringTokenizer.nextToken()).longValue() * 1000L);
      localStringTokenizer.nextToken();
      String str5 = localStringTokenizer.nextToken();
      if ((str5.equals("\t")) || (str5.trim().equals("")))
        return null;
      localStringTokenizer.nextToken();
      for (String str6 = localStringTokenizer.nextToken(); localStringTokenizer.hasMoreTokens(); str6 = str6 + localStringTokenizer.nextToken());
      String str7 = str5 + "=" + str6;
      return HttpCookie.create(localDate, str7, str3, str1, bool);
    }
    catch (NoSuchElementException localNoSuchElementException)
    {
      return null;
    }
    catch (NumberFormatException localNumberFormatException)
    {
    }
    return null;
  }

  private void writeCookieRecord(HttpCookie paramHttpCookie, StringBuffer paramStringBuffer)
  {
    String str = paramHttpCookie.getDomain();
    paramStringBuffer.append(str);
    paramStringBuffer.append("\t");
    if (str.startsWith("."))
      paramStringBuffer.append("TRUE\t");
    else
      paramStringBuffer.append("FALSE\t");
    paramStringBuffer.append(paramHttpCookie.getPath());
    paramStringBuffer.append("\t");
    if (paramHttpCookie.isSecure())
      paramStringBuffer.append("TRUE\t");
    else
      paramStringBuffer.append("FALSE\t");
    paramStringBuffer.append(paramHttpCookie.getExpirationDate().getTime() / 1000L + "\t");
    paramStringBuffer.append(paramHttpCookie.getName());
    paramStringBuffer.append("\t");
    paramStringBuffer.append(paramHttpCookie.getValue());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.cookie.NetscapeCookieStore
 * JD-Core Version:    0.6.0
 */