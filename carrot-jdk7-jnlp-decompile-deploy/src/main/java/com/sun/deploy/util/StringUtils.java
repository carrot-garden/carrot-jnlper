package com.sun.deploy.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

public class StringUtils
{
  public static String[] splitString(String paramString1, String paramString2)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString1, paramString2);
    String[] arrayOfString = new String[localStringTokenizer.countTokens()];
    for (int i = 0; i < arrayOfString.length; i++)
      arrayOfString[i] = localStringTokenizer.nextToken();
    return arrayOfString;
  }

  public static String trimWhitespace(String paramString)
  {
    if (paramString == null)
      return paramString;
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < paramString.length(); i++)
    {
      char c = paramString.charAt(i);
      if ((c == '\n') || (c == '\f') || (c == '\r') || (c == '\t'))
        continue;
      localStringBuffer.append(c);
    }
    return localStringBuffer.toString().trim();
  }

  public static String join(Collection paramCollection, String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    Iterator localIterator = paramCollection.iterator();
    while (localIterator.hasNext())
    {
      if (localStringBuffer.length() != 0)
        localStringBuffer.append(paramString);
      localStringBuffer.append((String)localIterator.next());
    }
    return localStringBuffer.toString();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.StringUtils
 * JD-Core Version:    0.6.0
 */