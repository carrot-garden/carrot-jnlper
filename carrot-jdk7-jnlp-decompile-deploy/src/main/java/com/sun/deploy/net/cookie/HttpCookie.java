package com.sun.deploy.net.cookie;

import java.net.URL;
import java.util.Date;
import java.util.StringTokenizer;

class HttpCookie
  implements Comparable
{
  private static final int SIZE_LIMIT = 4096;
  private Date expirationDate = null;
  private String nameAndValue = null;
  private String path = null;
  private String domain = null;
  private boolean isSecure = false;

  private HttpCookie()
  {
  }

  private HttpCookie(Date paramDate, String paramString1, String paramString2, String paramString3, boolean paramBoolean)
  {
    this.expirationDate = paramDate;
    this.nameAndValue = paramString1;
    this.path = paramString2;
    this.domain = stripPort(paramString3);
    this.isSecure = paramBoolean;
  }

  private static String stripPort(String paramString)
  {
    int i = paramString.indexOf(':');
    if (i == -1)
      return paramString;
    return paramString.substring(0, i);
  }

  static HttpCookie create(Date paramDate, String paramString1, String paramString2, String paramString3, boolean paramBoolean)
  {
    if ((paramString1 == null) || (paramString2 == null) || (paramString3 == null))
      return null;
    if (paramString2.equals(""))
      paramString2 = "/";
    if (paramString1.length() > 4096)
      paramString1 = paramString1.substring(0, 4096);
    return new HttpCookie(paramDate, paramString1, paramString2, paramString3, paramBoolean);
  }

  static HttpCookie create(URL paramURL, String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ";");
    if (!localStringTokenizer.hasMoreTokens())
      return null;
    HttpCookie localHttpCookie = new HttpCookie();
    localHttpCookie.nameAndValue = localStringTokenizer.nextToken().trim();
    if (localHttpCookie.nameAndValue.length() > 4096)
      localHttpCookie.nameAndValue = localHttpCookie.nameAndValue.substring(0, 4096);
    while (localStringTokenizer.hasMoreTokens())
    {
      String str1 = localStringTokenizer.nextToken().trim();
      if (str1.equalsIgnoreCase("secure"))
      {
        localHttpCookie.isSecure = true;
      }
      else
      {
        int j = str1.indexOf("=");
        if (j < 0)
          continue;
        String str2 = str1.substring(0, j);
        String str3 = str1.substring(j + 1);
        if (str2.equalsIgnoreCase("path"))
          localHttpCookie.path = str3;
        else if (str2.equalsIgnoreCase("domain"))
        {
          if (str3.indexOf(".") == 0)
            localHttpCookie.domain = stripPort(str3.substring(1));
          else
            localHttpCookie.domain = stripPort(str3);
        }
        else if (str2.equalsIgnoreCase("expires"))
          localHttpCookie.expirationDate = parseExpireDate(str3);
      }
    }
    if (localHttpCookie.domain == null)
      localHttpCookie.domain = paramURL.getHost();
    if (localHttpCookie.path == null)
    {
      localHttpCookie.path = paramURL.getFile();
      int i = localHttpCookie.path.lastIndexOf("/");
      if (i > -1)
        localHttpCookie.path = localHttpCookie.path.substring(0, i);
    }
    return localHttpCookie;
  }

  public String getNameValue()
  {
    return this.nameAndValue;
  }

  public String getName()
  {
    int i = this.nameAndValue.indexOf("=");
    return this.nameAndValue.substring(0, i);
  }

  public String getValue()
  {
    int i = this.nameAndValue.indexOf("=");
    return this.nameAndValue.substring(i + 1);
  }

  public String getDomain()
  {
    return this.domain;
  }

  public String getPath()
  {
    return this.path;
  }

  public Date getExpirationDate()
  {
    return this.expirationDate;
  }

  boolean hasExpired()
  {
    if (this.expirationDate == null)
      return false;
    return this.expirationDate.getTime() <= System.currentTimeMillis();
  }

  boolean isSaveable()
  {
    return (this.expirationDate != null) && (this.expirationDate.getTime() > System.currentTimeMillis());
  }

  public boolean isSecure()
  {
    return this.isSecure;
  }

  private static Date parseExpireDate(String paramString)
  {
    RfcDateParser localRfcDateParser = new RfcDateParser(paramString);
    Date localDate = localRfcDateParser.getDate();
    return localDate;
  }

  public String toString()
  {
    String str = this.nameAndValue;
    if (this.expirationDate != null)
      str = str + "; expires=" + this.expirationDate;
    if (this.path != null)
      str = str + "; path=" + this.path;
    if (this.domain != null)
      str = str + "; domain=" + this.domain;
    if (this.isSecure)
      str = str + "; secure";
    return str;
  }

  public int compareTo(Object paramObject)
  {
    HttpCookie localHttpCookie = (HttpCookie)paramObject;
    if (localHttpCookie == this)
      return 0;
    int i = getDomain().compareTo(localHttpCookie.getDomain());
    if (i != 0)
      return i;
    i = getPath().compareTo(localHttpCookie.getPath());
    if (i != 0)
      return i;
    return getNameValue().compareTo(localHttpCookie.getNameValue());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.cookie.HttpCookie
 * JD-Core Version:    0.6.0
 */