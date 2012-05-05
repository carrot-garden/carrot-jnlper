package com.sun.deploy.security;

public class URLClassPathControl
{
  private static final ThreadLocal level = new ThreadLocal();

  public static boolean isDisabledInCurrentThread()
  {
    return currentLevel() > 0;
  }

  public static void disable()
  {
    level.set(Integer.valueOf(currentLevel() + 1));
  }

  public static void enable()
  {
    int i = currentLevel();
    if (i > 0)
    {
      i--;
      level.set(Integer.valueOf(i));
    }
  }

  private static int currentLevel()
  {
    Object localObject = level.get();
    if ((localObject instanceof Integer))
      return ((Integer)localObject).intValue();
    return 0;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.URLClassPathControl
 * JD-Core Version:    0.6.0
 */