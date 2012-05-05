package com.sun.javaws.util;

import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JNLPUtils
{
  private static void addLoadedJarsEntry(Set paramSet, List paramList, JARDesc paramJARDesc)
  {
    if (!paramSet.contains(paramJARDesc))
    {
      paramList.add(paramJARDesc);
      paramSet.add(paramJARDesc);
    }
  }

  public static void sortResourcesForClasspath(ResourcesDesc paramResourcesDesc, List paramList1, List paramList2)
  {
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    ArrayList localArrayList3 = new ArrayList();
    HashSet localHashSet = new HashSet();
    JARDesc[] arrayOfJARDesc = paramResourcesDesc.getEagerOrAllJarDescs(true);
    localHashSet.addAll(paramList1);
    for (int i = 0; i < arrayOfJARDesc.length; i++)
    {
      if (arrayOfJARDesc[i].isNativeLib())
        continue;
      if (arrayOfJARDesc[i].isProgressJar())
        addLoadedJarsEntry(localHashSet, paramList1, arrayOfJARDesc[i]);
      else if (arrayOfJARDesc[i].isMainJarFile())
        localArrayList2.add(arrayOfJARDesc[i]);
      else if (!arrayOfJARDesc[i].isLazyDownload())
        localArrayList3.add(arrayOfJARDesc[i]);
      else if (!paramResourcesDesc.isPackagePart(arrayOfJARDesc[i].getPartName()))
        localArrayList1.add(arrayOfJARDesc[i]);
      else
        paramList2.add(arrayOfJARDesc[i]);
    }
    for (i = 0; i < localArrayList2.size(); i++)
      addLoadedJarsEntry(localHashSet, paramList1, (JARDesc)localArrayList2.get(i));
    for (i = 0; i < localArrayList3.size(); i++)
      addLoadedJarsEntry(localHashSet, paramList1, (JARDesc)localArrayList3.get(i));
    for (i = 0; i < localArrayList1.size(); i++)
      addLoadedJarsEntry(localHashSet, paramList1, (JARDesc)localArrayList1.get(i));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.util.JNLPUtils
 * JD-Core Version:    0.6.0
 */