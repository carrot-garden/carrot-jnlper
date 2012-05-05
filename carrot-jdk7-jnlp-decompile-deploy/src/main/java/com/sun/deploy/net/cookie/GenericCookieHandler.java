package com.sun.deploy.net.cookie;

import com.sun.deploy.config.Config;
import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

public class GenericCookieHandler
  implements CookieHandler
{
  private CookieStore sessionCookieStore = new SessionCookieStore();
  private CookieStore persistCookieStore;

  public GenericCookieHandler()
  {
    File localFile = new File(Config.getUserCookieFile());
    this.persistCookieStore = new NetscapeCookieStore(localFile);
  }

  public synchronized String getCookieInfo(URL paramURL)
    throws CookieUnavailableException
  {
    try
    {
      if ((paramURL.getPath() == null) || (paramURL.getPath().equals("")))
        paramURL = new URL(paramURL, "/");
    }
    catch (MalformedURLException localMalformedURLException)
    {
      throw new CookieUnavailableException(localMalformedURLException.getMessage(), localMalformedURLException);
    }
    return getRelevantCookies(paramURL);
  }

  public synchronized void setCookieInfo(URL paramURL, String paramString)
    throws CookieUnavailableException
  {
    recordCookie(paramURL, paramString);
  }

  private void recordCookie(URL paramURL, String paramString)
  {
    HttpCookie localHttpCookie = HttpCookie.create(paramURL, paramString);
    if (localHttpCookie == null)
      return;
    String[] arrayOfString = { "com", "edu", "net", "org", "gov", "mil", "int" };
    String str1 = localHttpCookie.getDomain();
    if (str1 == null)
      return;
    str1 = str1.toLowerCase();
    String str2 = paramURL.getHost();
    str2 = str2.toLowerCase();
    boolean bool = str2.equals(str1);
    if ((!bool) && (str2.endsWith(str1)))
    {
      int i = 2;
      for (int j = 0; j < arrayOfString.length; j++)
      {
        if (!str1.endsWith(arrayOfString[j]))
          continue;
        i = 1;
      }
      j = str1.length();
      while ((j > 0) && (i > 0))
      {
        j = str1.lastIndexOf('.', j - 1);
        i--;
      }
      if (j > 0)
        bool = true;
    }
    if (bool)
      recordCookie(localHttpCookie);
  }

  private void recordCookie(HttpCookie paramHttpCookie)
  {
    if (!this.sessionCookieStore.shouldRejectCookie(paramHttpCookie))
      this.sessionCookieStore.recordCookie(paramHttpCookie);
    if (!this.persistCookieStore.shouldRejectCookie(paramHttpCookie))
      this.persistCookieStore.recordCookie(paramHttpCookie);
  }

  private String getRelevantCookies(URL paramURL)
  {
    String str1 = this.sessionCookieStore.getRelevantCookies(paramURL);
    String str2 = this.persistCookieStore.getRelevantCookies(paramURL);
    if (str1 == null)
      return str2;
    if (str2 == null)
      return str1;
    return str1 + "; " + str2;
  }

  public static void main(String[] paramArrayOfString)
  {
    try
    {
      GenericCookieHandler localGenericCookieHandler = new GenericCookieHandler();
      URL localURL1 = new URL("http://java.sun.com/bar/index.html");
      URL localURL2 = new URL("http://java.sun.com");
      URL localURL3 = new URL("http://java.sun.com/xyz/bar/index.html");
      URL localURL4 = new URL("https://java.sun.com");
      URL localURL5 = new URL("https://java.sun.com/foo/xyz/index.html");
      URL localURL6 = new URL("https://java.sun.com/foobar/xyz/index.html");
      URL localURL7 = new URL("https://java.sun.com/xyz/foo/index.html");
      URL localURL8 = new URL("http://xyz.sun.com/");
      URL localURL9 = new URL("http://xyz.sun.com/ammo/index.html");
      System.out.println("Client --> " + localURL1);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL1));
      System.out.println("");
      System.out.println("Server --> " + localURL1);
      System.out.println("Set-Cookie: CUSTOMER_EXPIRED=WILE_E_COYOTE_EXPIRED; path=/; expires=Wednesday, 09-Nov-99 23:12:40 GMT");
      System.out.println("Set-Cookie: CUSTOMER_NOT_EXPIRED=WILE_E_COYOTE_NOT_EXPIRED; path=/; expires=Wednesday, 09-Nov-03 23:12:40 GMT");
      System.out.println("Set-Cookie: CUSTOMER_SECURE=WILE_E_COYOTE_SECURE; path=/; secure");
      System.out.println("");
      localGenericCookieHandler.setCookieInfo(localURL1, "CUSTOMER_EXPIRED=WILE_E_COYOTE_EXPIRED; path=/; expires=Wednesday, 09-Nov-99 23:12:40 GMT");
      localGenericCookieHandler.setCookieInfo(localURL1, "CUSTOMER_NOT_EXPIRED=WILE_E_COYOTE_NOT_EXPIRED; path=/; expires=Wednesday, 09-Nov-03 23:12:40 GMT");
      localGenericCookieHandler.setCookieInfo(localURL1, "CUSTOMER_SECURE=WILE_E_COYOTE_SECURE; path=/; secure");
      System.out.println("Client --> " + localURL2);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL2));
      System.out.println("");
      System.out.println("Server --> " + localURL3);
      System.out.println("Set-Cookie: PART_NUMBER=ROCKET_LAUNCHER_0001; path=/");
      System.out.println("");
      localGenericCookieHandler.setCookieInfo(localURL3, "PART_NUMBER=ROCKET_LAUNCHER_0001; path=/");
      System.out.println("Client --> " + localURL2);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL2));
      System.out.println("");
      System.out.println("Server --> " + localURL1);
      System.out.println("Set-Cookie: SHIPPING=FEDEX; path=/foo");
      System.out.println("Set-Cookie: SHIPPING_SECURE=UPS; path=/foo; secure");
      System.out.println("");
      localGenericCookieHandler.setCookieInfo(localURL1, "SHIPPING=FEDEX; path=/foo");
      localGenericCookieHandler.setCookieInfo(localURL1, "SHIPPING_SECURE=UPS; path=/foo; secure");
      System.out.println("Client --> " + localURL2);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL2));
      System.out.println("");
      System.out.println("Client --> " + localURL1);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL1));
      System.out.println("");
      System.out.println("Client --> " + localURL4);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL4));
      System.out.println("");
      System.out.println("Client --> " + localURL5);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL5));
      System.out.println("");
      System.out.println("Client --> " + localURL6);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL6));
      System.out.println("");
      System.out.println("Client --> " + localURL7);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL7));
      System.out.println("");
      System.out.println("Client --> " + localURL8);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL8));
      System.out.println("");
      System.out.println("Server --> " + localURL8);
      System.out.println("Set-Cookie: PART_NUMBER=ROCKET_LAUNCHER_0001; path=/");
      System.out.println("");
      localGenericCookieHandler.setCookieInfo(localURL8, "PART_NUMBER=ROCKET_LAUNCHER_0001; path=/");
      System.out.println("Client --> " + localURL8);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL8));
      System.out.println("");
      System.out.println("Server --> " + localURL8);
      System.out.println("Set-Cookie: PART_NUMBER=RIDING_ROCKET_0023; path=/ammo");
      System.out.println("");
      localGenericCookieHandler.setCookieInfo(localURL8, "PART_NUMBER=RIDING_ROCKET_0023; path=/ammo");
      System.out.println("Client --> " + localURL9);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL9));
      System.out.println("");
      System.out.println("Client --> " + localURL8);
      System.out.println("Cookie    : " + localGenericCookieHandler.getCookieInfo(localURL8));
      System.out.println("");
      System.out.println(localGenericCookieHandler.sessionCookieStore.toString());
      System.out.println("");
      System.out.println(localGenericCookieHandler.persistCookieStore.toString());
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.cookie.GenericCookieHandler
 * JD-Core Version:    0.6.0
 */