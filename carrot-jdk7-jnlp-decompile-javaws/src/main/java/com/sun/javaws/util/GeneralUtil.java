package com.sun.javaws.util;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

public class GeneralUtil
{
  public static boolean prefixMatch(String paramString1, String paramString2)
  {
    if (paramString1 == null)
      return true;
    if (paramString2 == null)
      return false;
    return paramString2.startsWith(paramString1);
  }

  public static boolean prefixMatchStringList(String[] paramArrayOfString, String paramString)
  {
    if (paramArrayOfString == null)
      return true;
    if (paramString == null)
      return false;
    for (int i = 0; i < paramArrayOfString.length; i++)
      if (paramString.startsWith(paramArrayOfString[i]))
        return true;
    return false;
  }

  public static String[] getStringList(String paramString)
  {
    if (paramString == null)
      return null;
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    int j = paramString.length();
    StringBuffer localStringBuffer = null;
    while (i < j)
    {
      char c = paramString.charAt(i);
      if (c == ' ')
      {
        if (localStringBuffer != null)
        {
          localArrayList.add(localStringBuffer.toString());
          localStringBuffer = null;
        }
      }
      else if (c == '\\')
      {
        if (i + 1 < j)
        {
          i++;
          c = paramString.charAt(i);
          if (localStringBuffer == null)
            localStringBuffer = new StringBuffer();
          localStringBuffer.append(c);
        }
      }
      else
      {
        if (localStringBuffer == null)
          localStringBuffer = new StringBuffer();
        localStringBuffer.append(c);
      }
      i++;
    }
    if (localStringBuffer != null)
      localArrayList.add(localStringBuffer.toString());
    if (localArrayList.size() == 0)
      return null;
    String[] arrayOfString = new String[localArrayList.size()];
    return (String[])(String[])localArrayList.toArray(arrayOfString);
  }

  public static boolean matchLocale(String[] paramArrayOfString, Locale paramLocale)
  {
    if (paramArrayOfString == null)
      return true;
    for (int i = 0; i < paramArrayOfString.length; i++)
      if (matchLocale(paramArrayOfString[i], paramLocale))
        return true;
    return false;
  }

  public static boolean matchLocale(String paramString, Locale paramLocale)
  {
    if ((paramString == null) || (paramString.length() == 0))
      return true;
    String str1 = "";
    String str2 = "";
    String str3 = "";
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "_", false);
    if ((localStringTokenizer.hasMoreElements()) && (paramLocale.getLanguage().length() > 0))
    {
      str1 = localStringTokenizer.nextToken();
      if (!str1.equalsIgnoreCase(paramLocale.getLanguage()))
        return false;
    }
    if ((localStringTokenizer.hasMoreElements()) && (paramLocale.getCountry().length() > 0))
    {
      str2 = localStringTokenizer.nextToken();
      if (!str2.equalsIgnoreCase(paramLocale.getCountry()))
        return false;
    }
    if ((localStringTokenizer.hasMoreElements()) && (paramLocale.getVariant().length() > 0))
    {
      str3 = localStringTokenizer.nextToken();
      if (!str3.equalsIgnoreCase(paramLocale.getVariant()))
        return false;
    }
    return true;
  }

  public static long heapValToLong(String paramString)
  {
    if (paramString == null)
      return -1L;
    long l1 = 1L;
    if (paramString.toLowerCase().lastIndexOf('m') != -1)
    {
      l1 = 1048576L;
      paramString = paramString.substring(0, paramString.length() - 1);
    }
    else if (paramString.toLowerCase().lastIndexOf('k') != -1)
    {
      l1 = 1024L;
      paramString = paramString.substring(0, paramString.length() - 1);
    }
    long l2 = -1L;
    try
    {
      l2 = Long.parseLong(paramString);
      l2 *= l1;
    }
    catch (NumberFormatException localNumberFormatException)
    {
      l2 = -1L;
    }
    return l2;
  }

  public static Frame getActiveTopLevelFrame()
  {
    Frame[] arrayOfFrame = Frame.getFrames();
    int i = -1;
    if (arrayOfFrame == null)
      return null;
    for (int j = 0; j < arrayOfFrame.length; j++)
    {
      if (arrayOfFrame[j].getFocusOwner() == null)
        continue;
      i = j;
    }
    return i >= 0 ? arrayOfFrame[i] : null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.util.GeneralUtil
 * JD-Core Version:    0.6.0
 */