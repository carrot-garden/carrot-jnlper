package com.sun.deploy.util;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StringQuoteUtil
{
  private static final char SQUO = '\'';
  private static final char DQUO = '"';
  private static final char ESCC = '\\';
  private static final char NILL = '\000';

  public static List parseCommandLine(String paramString)
  {
    ArrayList localArrayList = new ArrayList();
    StringBuffer localStringBuffer = null;
    char c1 = '\000';
    int i = 0;
    int j = 0;
    if (paramString == null)
      return localArrayList;
    int k = paramString.length();
    for (int m = 0; m < k; m++)
    {
      char c2 = paramString.charAt(m);
      if (j != 0)
      {
        if (i != 0)
        {
          localStringBuffer.append(c2);
          i = 0;
        }
        else if (c1 == 0)
        {
          if (isWhiteSpace(c2))
          {
            j = 0;
          }
          else if ((c2 == '\'') || (c2 == '"'))
          {
            c1 = c2;
            localStringBuffer.append(c2);
          }
          else
          {
            localStringBuffer.append(c2);
          }
        }
        else if ((c2 == c1) && ((m + 1 == k) || (isWhiteSpace(paramString.charAt(m + 1)))))
        {
          localStringBuffer.append(c2);
          c1 = '\000';
        }
        else
        {
          localStringBuffer.append(c2);
        }
        if ((j == 0) || (m == k - 1))
        {
          localArrayList.add(unquoteIfNeeded(localStringBuffer.toString()));
          j = 0;
          localStringBuffer = null;
        }
        else
        {
          if (c2 != '\\')
            continue;
          i = 1;
        }
      }
      else
      {
        if (isWhiteSpace(c2))
          continue;
        j = 1;
        localStringBuffer = new StringBuffer();
        m--;
      }
    }
    Trace.println(" --- parseCommandLine converted : " + paramString + "\ninto:\n" + localArrayList, TraceLevel.SECURITY);
    return localArrayList;
  }

  public static String getStringByCommandList(List paramList)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = 0;
    Iterator localIterator = paramList.iterator();
    while (localIterator.hasNext())
    {
      if (i != 0)
        localStringBuffer.append(" ");
      else
        i = 1;
      String str = (String)localIterator.next();
      localStringBuffer.append(quoteIfNeeded(str));
    }
    return localStringBuffer.toString();
  }

  public static String quoteIfNeeded(String paramString)
  {
    if (paramString == null)
      return null;
    if ((containsWhiteSpace(paramString)) && (!isQuoted(paramString)))
    {
      if (containsUnescaped(paramString, '"'))
        return "\"" + escapeInstances(paramString, '"') + "\"";
      return "\"" + paramString + "\"";
    }
    return paramString;
  }

  public static String unquoteIfNeeded(String paramString)
  {
    if (paramString == null)
      return null;
    int i = paramString.length();
    if (isQuoted(paramString))
      return unescapeInstances(paramString.substring(1, i - 1), paramString.charAt(0));
    return paramString;
  }

  private static boolean containsWhiteSpace(String paramString)
  {
    for (int i = 0; i < paramString.length(); i++)
      if (isWhiteSpace(paramString.charAt(i)))
        return true;
    return false;
  }

  private static boolean isQuoted(String paramString)
  {
    int i = paramString.length() - 1;
    if (i < 1)
      return false;
    return ((paramString.charAt(0) == '"') && (paramString.charAt(i) == '"')) || ((paramString.charAt(0) == '\'') && (paramString.charAt(i) == '\'') && (!containsUnescaped(paramString.substring(1, i), paramString.charAt(0))));
  }

  private static boolean isWhiteSpace(char paramChar)
  {
    return (paramChar == ' ') || (paramChar == '\t') || (paramChar == '\n') || (paramChar == '\r') || (paramChar == '\f');
  }

  private static boolean containsUnescaped(String paramString, char paramChar)
  {
    for (int i = 0; i < paramString.length(); i++)
      if (paramString.charAt(i) == '\\')
        i++;
      else if (paramString.charAt(i) == paramChar)
        return true;
    return false;
  }

  private static String escapeInstances(String paramString, char paramChar)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < paramString.length(); i++)
      if (paramString.charAt(i) == '\\')
      {
        localStringBuffer.append(paramString.charAt(i++));
        if (i >= paramString.length())
          continue;
        localStringBuffer.append(paramString.charAt(i));
      }
      else if (paramString.charAt(i) == paramChar)
      {
        localStringBuffer.append('\\');
        localStringBuffer.append(paramChar);
      }
      else
      {
        localStringBuffer.append(paramString.charAt(i));
      }
    return localStringBuffer.toString();
  }

  private static String unescapeInstances(String paramString, char paramChar)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = paramString.length();
    for (int j = 0; j < i; j++)
    {
      if ((j != i - 1) && (paramString.charAt(j) == '\\') && (paramString.charAt(j + 1) == paramChar))
        continue;
      localStringBuffer.append(paramString.charAt(j));
    }
    return localStringBuffer.toString();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.StringQuoteUtil
 * JD-Core Version:    0.6.0
 */