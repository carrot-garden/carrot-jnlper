package com.sun.applet2;

import com.sun.deploy.util.StringUtils;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AppletParameters extends HashMap
{
  HashMap mixedCaseMap = new HashMap();

  public Object put(Object paramObject1, Object paramObject2)
  {
    if (paramObject1 == null)
      return null;
    Object localObject = paramObject1;
    if ((paramObject1 instanceof String))
      localObject = StringUtils.trimWhitespace((String)paramObject1).toLowerCase(Locale.ENGLISH);
    this.mixedCaseMap.put(paramObject1, paramObject2);
    return super.put(localObject, paramObject2);
  }

  public Map rawMap()
  {
    return this.mixedCaseMap;
  }

  public void dump()
  {
    System.out.println("============= Dump AppletParameters [lowercase=" + size() + "] [raw=" + this.mixedCaseMap.size() + "]");
    Iterator localIterator = keySet().iterator();
    Object localObject;
    while (localIterator.hasNext())
    {
      localObject = localIterator.next();
      System.out.println("  key=[" + localObject + "] value=[" + get(localObject) + "]");
    }
    if (!isEmpty())
      System.out.println(" -----------");
    localIterator = this.mixedCaseMap.keySet().iterator();
    while (localIterator.hasNext())
    {
      localObject = localIterator.next();
      System.out.println("  key=[" + localObject + "] value=[" + this.mixedCaseMap.get(localObject) + "]");
    }
    System.err.println("=============== Dump done");
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.AppletParameters
 * JD-Core Version:    0.6.0
 */