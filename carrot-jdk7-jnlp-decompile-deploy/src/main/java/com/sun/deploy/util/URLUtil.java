package com.sun.deploy.util;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.BitSet;
import java.util.WeakHashMap;

public class URLUtil
{
  static BitSet encodedInPath = new BitSet(256);
  private static WeakHashMap canonicalizedURLMap;

  public static void setHostHeader(URLConnection paramURLConnection)
  {
    int i = paramURLConnection.getURL().getPort();
    String str = paramURLConnection.getURL().getHost();
    if ((i != -1) && (i != 80))
      str = str + ":" + String.valueOf(i);
    paramURLConnection.setRequestProperty("Host", str);
  }

  public static URL getBase(URL paramURL)
  {
    if (paramURL == null)
      return null;
    String str = paramURL.getFile();
    if (str != null)
    {
      int i = str.lastIndexOf('/');
      if (i != -1)
        str = str.substring(0, i + 1);
      try
      {
        return new URL(paramURL.getProtocol(), paramURL.getHost(), paramURL.getPort(), str);
      }
      catch (MalformedURLException localMalformedURLException)
      {
        Trace.ignoredException(localMalformedURLException);
      }
    }
    return paramURL;
  }

  public static boolean isUNCFileURL(URL paramURL)
  {
    if ((paramURL == null) || (!paramURL.getProtocol().equalsIgnoreCase("file")))
      return false;
    String str = paramURL.toString();
    str = str.replaceAll("////", "//");
    try
    {
      URL localURL = new URL(str);
      if (localURL.getHost().equals(""))
        return false;
    }
    catch (Exception localException)
    {
    }
    return true;
  }

  public static URL asPathURL(URL paramURL)
  {
    if (paramURL == null)
      return null;
    String str = paramURL.getFile();
    if ((str != null) && (!str.endsWith("/")))
      try
      {
        return new URL(paramURL.getProtocol(), paramURL.getHost(), paramURL.getPort(), paramURL.getFile() + "/");
      }
      catch (MalformedURLException localMalformedURLException)
      {
      }
    return paramURL;
  }

  public static String toNormalizedString(URL paramURL)
  {
    if (paramURL == null)
      return "";
    try
    {
      if (paramURL.getPort() == paramURL.getDefaultPort())
        paramURL = new URL(paramURL.getProtocol().toLowerCase(), paramURL.getHost().toLowerCase(), -1, paramURL.getFile());
      else
        paramURL = new URL(paramURL.getProtocol().toLowerCase(), paramURL.getHost().toLowerCase(), paramURL.getPort(), paramURL.getFile());
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    return paramURL.toExternalForm();
  }

  public static boolean sameURLs(URL paramURL1, URL paramURL2)
  {
    if ((paramURL1 == null) || (paramURL2 == null) || (paramURL1 == paramURL2))
      return paramURL1 == paramURL2;
    String str1 = paramURL1.getFile();
    String str2 = paramURL2.getFile();
    return (str1.length() == str2.length()) && (sameBase(paramURL1, paramURL2)) && (str1.equalsIgnoreCase(str2));
  }

  public static boolean sameBase(URL paramURL1, URL paramURL2)
  {
    return (paramURL1 != null) && (paramURL2 != null) && (sameHost(paramURL1, paramURL2)) && (samePort(paramURL1, paramURL2)) && (sameProtocol(paramURL1, paramURL2));
  }

  private static boolean sameProtocol(URL paramURL1, URL paramURL2)
  {
    return paramURL1.getProtocol().equals(paramURL2.getProtocol());
  }

  private static boolean sameHost(URL paramURL1, URL paramURL2)
  {
    String str1 = paramURL1.getHost();
    String str2 = paramURL2.getHost();
    if ((str1 == null) || (str2 == null))
      return (str1 == null) && (str2 == null);
    return (str1.length() == str2.length()) && (str1.equalsIgnoreCase(str2));
  }

  private static boolean samePort(URL paramURL1, URL paramURL2)
  {
    return getPort(paramURL1) == getPort(paramURL2);
  }

  private static int getPort(URL paramURL)
  {
    if (paramURL.getPort() != -1)
      return paramURL.getPort();
    return paramURL.getDefaultPort();
  }

  public static String encodePath(String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = paramString.length();
    for (int j = 0; j < i; j++)
    {
      int k = paramString.charAt(j);
      if (k == File.separatorChar)
      {
        localStringBuffer.append('/');
      }
      else if (k <= 127)
      {
        if (encodedInPath.get(k))
          escape(localStringBuffer, k);
        else
          localStringBuffer.append(k);
      }
      else if (k > 2047)
      {
        escape(localStringBuffer, (char)(0xE0 | k >> 12 & 0xF));
        escape(localStringBuffer, (char)(0x80 | k >> 6 & 0x3F));
        escape(localStringBuffer, (char)(0x80 | k >> 0 & 0x3F));
      }
      else
      {
        escape(localStringBuffer, (char)(0xC0 | k >> 6 & 0x1F));
        escape(localStringBuffer, (char)(0x80 | k >> 0 & 0x3F));
      }
    }
    if (!paramString.equals(localStringBuffer.toString()))
    {
      Trace.println("     String: " + paramString, TraceLevel.BASIC);
      Trace.println(" encoded to: " + localStringBuffer.toString(), TraceLevel.BASIC);
    }
    return localStringBuffer.toString();
  }

  private static void escape(StringBuffer paramStringBuffer, char paramChar)
  {
    paramStringBuffer.append('%');
    paramStringBuffer.append(Character.forDigit(paramChar >> '\004' & 0xF, 16));
    paramStringBuffer.append(Character.forDigit(paramChar & 0xF, 16));
  }

  public static String decodePath(String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 0;
    while (i < paramString.length())
    {
      char c = paramString.charAt(i);
      if (c != '%')
        i++;
      else
        try
        {
          c = unescape(paramString, i);
          i += 3;
          if ((c & 0x80) != 0)
          {
            int j;
            switch (c >> '\004')
            {
            case 12:
            case 13:
              j = unescape(paramString, i);
              i += 3;
              c = (char)((c & 0x1F) << '\006' | j & 0x3F);
              break;
            case 14:
              j = unescape(paramString, i);
              i += 3;
              int k = unescape(paramString, i);
              i += 3;
              c = (char)((c & 0xF) << '\f' | (j & 0x3F) << 6 | k & 0x3F);
              break;
            default:
              Trace.ignoredException(new IllegalArgumentException());
            }
          }
        }
        catch (NumberFormatException localNumberFormatException)
        {
          Trace.ignoredException(localNumberFormatException);
        }
      localStringBuffer.append(c);
    }
    if (!paramString.equals(localStringBuffer.toString()))
    {
      Trace.println("     String: " + paramString, TraceLevel.BASIC);
      Trace.println(" decoded to: " + localStringBuffer.toString(), TraceLevel.BASIC);
    }
    return localStringBuffer.toString();
  }

  private static char unescape(String paramString, int paramInt)
  {
    return (char)Integer.parseInt(paramString.substring(paramInt + 1, paramInt + 3), 16);
  }

  public static String getEncodedPath(File paramFile)
  {
    String str = paramFile.getAbsolutePath();
    if ((!str.endsWith(File.separator)) && (paramFile.isDirectory()))
      str = str + File.separator;
    return encodePath(str);
  }

  public static String getDecodedPath(URL paramURL)
  {
    String str = paramURL.getFile();
    str = str.replace('/', File.separatorChar);
    return decodePath(str);
  }

  public static String getPathFromURL(URL paramURL)
  {
    return getDecodedPath(paramURL);
  }

  public static synchronized String canonicalize(String paramString)
  {
    if (paramString.indexOf("file:") == -1)
      return paramString;
    String str1 = (String)canonicalizedURLMap.get(paramString);
    if (str1 != null)
      return str1;
    StringBuffer localStringBuffer = new StringBuffer();
    if (paramString.indexOf("file://///") == 0)
    {
      localStringBuffer.append("file:////");
      localStringBuffer.append(paramString.substring(10));
    }
    else if (paramString.indexOf("file:///\\") == 0)
    {
      localStringBuffer.append("file:////");
      localStringBuffer.append(paramString.substring(9));
    }
    else if (paramString.indexOf("file://\\") == 0)
    {
      localStringBuffer.append("file:////");
      localStringBuffer.append(paramString.substring(9));
    }
    else if (paramString.indexOf("file:\\") == 0)
    {
      if ((paramString.indexOf(':', 6) != -1) || (paramString.indexOf('|', 6) != -1))
        localStringBuffer.append("file:///");
      else
        localStringBuffer.append("file:////");
      localStringBuffer.append(paramString.substring(6));
    }
    else if ((paramString.indexOf("file://") == 0) && (paramString.charAt(7) != '/'))
    {
      if ((paramString.indexOf(':', 7) != -1) || (paramString.indexOf('|', 7) != -1))
        localStringBuffer.append("file:///");
      else
        localStringBuffer.append("file:////");
      localStringBuffer.append(paramString.substring(7));
    }
    else
    {
      localStringBuffer.append(paramString);
    }
    int i = 0;
    for (int j = 0; j < localStringBuffer.length(); j++)
    {
      int k = localStringBuffer.charAt(j);
      if (k == 92)
      {
        localStringBuffer.setCharAt(j, '/');
      }
      else
      {
        if ((i != 0) || (k != 124))
          continue;
        localStringBuffer.setCharAt(j, ':');
        i = 1;
      }
    }
    String str2 = localStringBuffer.toString();
    canonicalizedURLMap.put(paramString, str2);
    return str2;
  }

  public static String canonicalizeDocumentBaseURL(String paramString)
  {
    int i = -1;
    int j = paramString.indexOf('#');
    int k = paramString.indexOf('?');
    if ((k != -1) && (j != -1))
      i = Math.min(j, k);
    else if (j != -1)
      i = j;
    else if (k != -1)
      i = k;
    String str;
    if (i == -1)
      str = paramString;
    else
      str = paramString.substring(0, i);
    StringBuffer localStringBuffer = new StringBuffer(str);
    int m = localStringBuffer.toString().indexOf("|");
    if (m >= 0)
      localStringBuffer.setCharAt(m, ':');
    if (i != -1)
      localStringBuffer.append(paramString.substring(i));
    return localStringBuffer.toString();
  }

  private static String slashify(String paramString, boolean paramBoolean)
  {
    String str = paramString;
    if (File.separatorChar != '/')
      str = str.replace(File.separatorChar, '/');
    if (!str.startsWith("/"))
      str = "/" + str;
    if ((!str.endsWith("/")) && (paramBoolean))
      str = str + "/";
    return str;
  }

  public static URL fileToURL(File paramFile)
  {
    if (Config.isJavaVersionAtLeast14())
      try
      {
        File localFile = paramFile.getAbsoluteFile();
        String str = slashify(localFile.getPath(), localFile.isDirectory());
        if (str.startsWith("//"))
          str = "//" + str;
        return new URI("file", null, str, null).toURL();
      }
      catch (URISyntaxException localURISyntaxException)
      {
        throw new Error(localURISyntaxException);
      }
      catch (MalformedURLException localMalformedURLException1)
      {
        throw new Error(localMalformedURLException1);
      }
    try
    {
      return paramFile.toURL();
    }
    catch (MalformedURLException localMalformedURLException2)
    {
    }
    throw new Error(localMalformedURLException2);
  }

  public static String urlNoFragString(URL paramURL)
  {
    StringBuffer localStringBuffer = new StringBuffer(128);
    String str1 = paramURL.getProtocol();
    if (str1 != null)
    {
      str1 = str1.toLowerCase();
      localStringBuffer.append(str1);
      localStringBuffer.append("://");
    }
    String str2 = paramURL.getHost();
    if (str2 != null)
    {
      str2 = str2.toLowerCase();
      localStringBuffer.append(str2);
      int i = paramURL.getPort();
      if (i == -1)
        try
        {
          i = paramURL.getDefaultPort();
        }
        catch (NoSuchMethodError localNoSuchMethodError)
        {
          i = paramURL.getPort();
        }
      if (i != -1)
        localStringBuffer.append(":").append(i);
    }
    String str3 = paramURL.getFile();
    if (str3 != null)
      localStringBuffer.append(str3);
    return localStringBuffer.toString();
  }

  public static boolean checkTargetURL(URL paramURL1, URL paramURL2)
  {
    if ((paramURL1 == null) || (paramURL2 == null))
      return false;
    if ("jar".equals(paramURL1.getProtocol()))
      try
      {
        paramURL1 = new URL(paramURL1.toString().substring(4));
      }
      catch (MalformedURLException localMalformedURLException1)
      {
        Trace.ignoredException(localMalformedURLException1);
        return false;
      }
    if ("jar".equals(paramURL2.getProtocol()))
      try
      {
        paramURL2 = new URL(paramURL2.toString().substring(4));
      }
      catch (MalformedURLException localMalformedURLException2)
      {
        Trace.ignoredException(localMalformedURLException2);
        return false;
      }
    if ("file".equals(paramURL1.getProtocol()))
    {
      boolean bool1 = isUNCFileURL(paramURL1);
      boolean bool2 = isUNCFileURL(paramURL2);
      return (!bool1) || (bool2);
    }
    return (!paramURL2.getProtocol().equals("file")) && (!paramURL2.getProtocol().equals("netdoc"));
  }

  public static URL getJarEntryURL(URL paramURL, String paramString)
    throws MalformedURLException
  {
    return getJarEntryURL(paramURL.toString(), paramString);
  }

  public static URL getJarEntryURL(String paramString1, String paramString2)
    throws MalformedURLException
  {
    if (paramString2 == null)
      throw new MalformedURLException("null entryName");
    if (!paramString2.startsWith("/"))
      paramString2 = "/" + paramString2;
    return new URL("jar:" + paramString1 + "!" + paramString2);
  }

  static
  {
    encodedInPath.set(61);
    encodedInPath.set(59);
    encodedInPath.set(63);
    encodedInPath.set(47);
    encodedInPath.set(35);
    encodedInPath.set(32);
    encodedInPath.set(60);
    encodedInPath.set(62);
    encodedInPath.set(37);
    encodedInPath.set(34);
    encodedInPath.set(123);
    encodedInPath.set(125);
    encodedInPath.set(124);
    encodedInPath.set(92);
    encodedInPath.set(94);
    encodedInPath.set(91);
    encodedInPath.set(93);
    encodedInPath.set(96);
    for (int i = 0; i < 32; i++)
      encodedInPath.set(i);
    encodedInPath.set(127);
    canonicalizedURLMap = new WeakHashMap();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.URLUtil
 * JD-Core Version:    0.6.0
 */