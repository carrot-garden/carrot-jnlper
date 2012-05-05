package com.sun.deploy.net.cookie;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

abstract class CookieStore
{
  protected transient TreeMap cookieJar = new TreeMap();

  public void recordCookie(HttpCookie paramHttpCookie)
  {
    if (shouldRejectCookie(paramHttpCookie))
      return;
    loadCookieJar();
    String str = paramHttpCookie.getDomain().toLowerCase();
    ArrayList localArrayList = (ArrayList)this.cookieJar.get(str);
    if (localArrayList == null)
      localArrayList = new ArrayList();
    if (addOrReplaceCookie(localArrayList, paramHttpCookie))
    {
      this.cookieJar.put(str, localArrayList);
      saveCookieJar();
    }
  }

  protected abstract void loadCookieJar();

  protected abstract void saveCookieJar();

  protected abstract String getName();

  protected boolean addOrReplaceCookie(ArrayList paramArrayList, HttpCookie paramHttpCookie)
  {
    int i = paramArrayList.size();
    String str1 = paramHttpCookie.getPath();
    String str2 = paramHttpCookie.getName();
    Object localObject = null;
    int j = -1;
    for (int k = 0; k < i; k++)
    {
      HttpCookie localHttpCookie = (HttpCookie)paramArrayList.get(k);
      String str3 = localHttpCookie.getPath();
      if (!str1.equals(str3))
        continue;
      String str4 = localHttpCookie.getName();
      if (!str2.equals(str4))
        continue;
      localObject = localHttpCookie;
      j = k;
      break;
    }
    if (localObject != null)
      paramArrayList.set(j, paramHttpCookie);
    else
      paramArrayList.add(paramHttpCookie);
    return true;
  }

  protected boolean shouldRejectCookie(HttpCookie paramHttpCookie)
  {
    return (paramHttpCookie.getDomain() == null) || (paramHttpCookie.getPath() == null) || (paramHttpCookie.getName() == null);
  }

  public String getRelevantCookies(URL paramURL)
  {
    loadCookieJar();
    String str1 = paramURL.getHost();
    Object localObject = getCookiesForHost(str1, paramURL);
    int i;
    while ((i = str1.indexOf('.', 1)) >= 0)
    {
      str1 = str1.substring(i + 1);
      String str2 = getCookiesForHost(str1, paramURL);
      if (str2 != null)
        if (localObject == null)
          localObject = str2;
        else
          localObject = (String)localObject + "; " + str2;
    }
    return (String)localObject;
  }

  private String getCookiesForHost(String paramString, URL paramURL)
  {
    ArrayList localArrayList1 = (ArrayList)this.cookieJar.get(paramString);
    if (localArrayList1 == null)
      return null;
    String str1 = paramURL.getFile();
    int i = str1.indexOf('?');
    if (i > 0)
      str1 = str1.substring(0, i);
    Iterator localIterator = localArrayList1.iterator();
    ArrayList localArrayList2 = new ArrayList(10);
    Object localObject2;
    while (localIterator.hasNext())
    {
      localObject1 = (HttpCookie)localIterator.next();
      localObject2 = ((HttpCookie)localObject1).getPath();
      if ((str1.startsWith((String)localObject2)) && (!((HttpCookie)localObject1).hasExpired()))
      {
        String str2 = paramURL.getProtocol();
        if ((str2.equals("https")) || ((str2.equals("http")) && (!((HttpCookie)localObject1).isSecure())))
          localArrayList2.add(localObject1);
      }
    }
    Collections.sort(localArrayList2);
    localIterator = localArrayList2.iterator();
    Object localObject1 = null;
    while (localIterator.hasNext())
    {
      localObject2 = (HttpCookie)localIterator.next();
      if (localObject1 == null)
        localObject1 = new StringBuffer(((HttpCookie)localObject2).getNameValue());
      else
        ((StringBuffer)localObject1).append("; ").append(((HttpCookie)localObject2).getNameValue());
    }
    if (localObject1 == null)
      return null;
    return (String)(String)((StringBuffer)localObject1).toString();
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append(getName());
    localStringBuffer.append("\n[\n");
    Iterator localIterator1 = this.cookieJar.values().iterator();
    while (localIterator1.hasNext())
    {
      List localList = (List)localIterator1.next();
      Iterator localIterator2 = localList.iterator();
      while (localIterator2.hasNext())
      {
        HttpCookie localHttpCookie = (HttpCookie)localIterator2.next();
        localStringBuffer.append("\t");
        localStringBuffer.append(localHttpCookie.toString());
        localStringBuffer.append("\n");
      }
    }
    localStringBuffer.append("]");
    return localStringBuffer.toString();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.cookie.CookieStore
 * JD-Core Version:    0.6.0
 */