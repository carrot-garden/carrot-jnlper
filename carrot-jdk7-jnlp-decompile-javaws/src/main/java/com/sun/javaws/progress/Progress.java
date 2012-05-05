package com.sun.javaws.progress;

import com.sun.deploy.uitoolkit.Applet2Adapter;
import java.util.WeakHashMap;

public class Progress
{
  private static WeakHashMap map = new WeakHashMap();

  public static synchronized PreloaderDelegate get(Applet2Adapter paramApplet2Adapter)
  {
    Object localObject = paramApplet2Adapter;
    if (paramApplet2Adapter == null)
      localObject = Progress.class;
    PreloaderDelegate localPreloaderDelegate = (PreloaderDelegate)map.get(localObject);
    if (localPreloaderDelegate == null)
    {
      localPreloaderDelegate = new PreloaderDelegate(paramApplet2Adapter);
      map.put(localObject, localPreloaderDelegate);
    }
    return (PreloaderDelegate)localPreloaderDelegate;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.progress.Progress
 * JD-Core Version:    0.6.0
 */