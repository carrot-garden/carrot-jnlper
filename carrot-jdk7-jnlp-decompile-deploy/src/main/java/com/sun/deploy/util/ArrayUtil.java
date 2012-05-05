package com.sun.deploy.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ArrayUtil
{
  public static String mapToString(Map paramMap)
  {
    Iterator localIterator = paramMap.keySet().iterator();
    StringBuffer localStringBuffer = new StringBuffer();
    while (localIterator.hasNext())
    {
      String str1 = (String)localIterator.next();
      String str2 = (String)paramMap.get(str1);
      localStringBuffer.append(str1 + "=" + str2 + " ");
    }
    return localStringBuffer.toString();
  }

  public static String propertiesToString(Properties paramProperties)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    ArrayList localArrayList = new ArrayList();
    Enumeration localEnumeration = paramProperties.propertyNames();
    while (localEnumeration.hasMoreElements())
    {
      String str = (String)localEnumeration.nextElement();
      localStringBuffer.append(str + "=" + paramProperties.getProperty(str) + " ");
    }
    return localStringBuffer.toString();
  }

  public static String arrayToString(String[] paramArrayOfString)
  {
    if (paramArrayOfString == null)
      return null;
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < paramArrayOfString.length; i++)
      localStringBuffer.append(paramArrayOfString[i] + " ");
    return localStringBuffer.toString();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.ArrayUtil
 * JD-Core Version:    0.6.0
 */