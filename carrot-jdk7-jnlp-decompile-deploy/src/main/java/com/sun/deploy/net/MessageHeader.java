package com.sun.deploy.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessageHeader
{
  private String[] keys;
  private String[] values;
  private int nkeys;

  public MessageHeader()
  {
    grow();
  }

  public MessageHeader(MessageHeader paramMessageHeader)
  {
    this.keys = ((String[])(String[])paramMessageHeader.keys.clone());
    this.values = ((String[])(String[])paramMessageHeader.values.clone());
    this.nkeys = paramMessageHeader.nkeys;
  }

  synchronized String findValue(String paramString)
  {
    int i;
    if (paramString == null)
    {
      i = this.nkeys;
      while (true)
      {
        i--;
        if (i < 0)
          break;
        if (this.keys[i] == null)
          return this.values[i];
      }
    }
    else
    {
      i = this.nkeys;
      while (true)
      {
        i--;
        if (i < 0)
          break;
        if (paramString.equalsIgnoreCase(this.keys[i]))
          return this.values[i];
      }
    }
    return null;
  }

  public synchronized int getKey(String paramString)
  {
    int i = this.nkeys;
    while (true)
    {
      i--;
      if (i < 0)
        break;
      if ((this.keys[i] == paramString) || ((paramString != null) && (paramString.equalsIgnoreCase(this.keys[i]))))
        return i;
    }
    return -1;
  }

  public synchronized String getKey(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= this.nkeys))
      return null;
    return this.keys[paramInt];
  }

  public synchronized String getValue(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= this.nkeys))
      return null;
    return this.values[paramInt];
  }

  public synchronized Map getHeaders()
  {
    return getHeaders(null);
  }

  public synchronized Map getHeaders(String[] paramArrayOfString)
  {
    int i = 0;
    HashMap localHashMap = new HashMap();
    int j = this.nkeys;
    while (true)
    {
      j--;
      if (j < 0)
        break;
      if (paramArrayOfString != null)
        for (int k = 0; k < paramArrayOfString.length; k++)
        {
          if ((paramArrayOfString[k] == null) || (!paramArrayOfString[k].equalsIgnoreCase(this.keys[j])))
            continue;
          i = 1;
          break;
        }
      if (i == 0)
      {
        localObject1 = (List)localHashMap.get(this.keys[j]);
        if (localObject1 == null)
        {
          localObject1 = new ArrayList();
          localHashMap.put(this.keys[j], localObject1);
        }
        ((List)localObject1).add(this.values[j]);
        continue;
      }
      i = 0;
    }
    Set localSet = localHashMap.keySet();
    Object localObject1 = localSet.iterator();
    while (((Iterator)localObject1).hasNext())
    {
      Object localObject2 = ((Iterator)localObject1).next();
      List localList = (List)localHashMap.get(localObject2);
      localHashMap.put(localObject2, Collections.unmodifiableList(localList));
    }
    return (Map)Collections.unmodifiableMap(localHashMap);
  }

  public synchronized void add(String paramString1, String paramString2)
  {
    grow();
    this.keys[this.nkeys] = paramString1;
    this.values[this.nkeys] = paramString2;
    this.nkeys += 1;
  }

  public static MessageHeader merge(MessageHeader paramMessageHeader1, MessageHeader paramMessageHeader2)
  {
    if (paramMessageHeader2 == null)
      return paramMessageHeader1;
    HashSet localHashSet = new HashSet(Arrays.asList(paramMessageHeader2.keys));
    MessageHeader localMessageHeader = new MessageHeader();
    String str1;
    String str2;
    for (int i = 0; i < paramMessageHeader1.keys.length; i++)
    {
      str1 = paramMessageHeader1.keys[i];
      str2 = paramMessageHeader1.values[i];
      if (str1 == null)
      {
        if (str2 == null)
          continue;
        localMessageHeader.add(str1, str2);
      }
      else
      {
        int j = paramMessageHeader2.getKey(str1);
        if (j == -1)
        {
          if (str2 == null)
            continue;
          localMessageHeader.add(str1, str2);
        }
        else
        {
          localHashSet.remove(str1);
          String str3 = paramMessageHeader2.values[j];
          if (str3 == null)
            continue;
          localMessageHeader.add(str1, str3);
        }
      }
    }
    Iterator localIterator = localHashSet.iterator();
    while (localIterator.hasNext())
    {
      str1 = (String)localIterator.next();
      str2 = paramMessageHeader2.findValue(str1);
      if (str2 != null)
        localMessageHeader.add(str1, str2);
    }
    return localMessageHeader;
  }

  private void grow()
  {
    if ((this.keys == null) || (this.nkeys >= this.keys.length))
    {
      String[] arrayOfString1 = new String[this.nkeys + 4];
      String[] arrayOfString2 = new String[this.nkeys + 4];
      if (this.keys != null)
        System.arraycopy(this.keys, 0, arrayOfString1, 0, this.nkeys);
      if (this.values != null)
        System.arraycopy(this.values, 0, arrayOfString2, 0, this.nkeys);
      this.keys = arrayOfString1;
      this.values = arrayOfString2;
    }
  }

  public synchronized String toString()
  {
    String str = super.toString();
    for (int i = 0; i < this.keys.length; i++)
      str = str + "{" + this.keys[i] + ": " + this.values[i] + "}";
    return str;
  }

  public int getContentLength(int paramInt)
  {
    String str = findValue("content-length");
    if (str != null)
      try
      {
        return Integer.parseInt(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    return paramInt;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.MessageHeader
 * JD-Core Version:    0.6.0
 */