package com.sun.deploy.util;

import com.sun.deploy.Environment;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class ReflectionUtil
{
  protected static final boolean DEBUG = Environment.getenv("JPI_PLUGIN2_DEBUG") != null;
  protected static final boolean VERBOSE = Environment.getenv("JPI_PLUGIN2_VERBOSE") != null;

  public static boolean isSubclassOf(Object paramObject, String paramString)
  {
    if (paramObject == null)
      return false;
    for (Class localClass = paramObject.getClass(); localClass != null; localClass = localClass.getSuperclass())
      if (localClass.getName().equals(paramString))
        return true;
    return false;
  }

  public static boolean isClassAvailable(String paramString, ClassLoader paramClassLoader)
  {
    if (null == paramClassLoader)
      paramClassLoader = ReflectionUtil.class.getClassLoader();
    try
    {
      Class localClass = Class.forName(paramString, false, paramClassLoader);
      return null != localClass;
    }
    catch (Throwable localThrowable)
    {
    }
    return false;
  }

  public static Class getClass(String paramString, ClassLoader paramClassLoader)
  {
    if (null == paramClassLoader)
      paramClassLoader = ReflectionUtil.class.getClassLoader();
    try
    {
      return Class.forName(paramString, false, paramClassLoader);
    }
    catch (Throwable localThrowable)
    {
      if (VERBOSE)
        localThrowable.printStackTrace();
    }
    return null;
  }

  private static Constructor getConstructor(String paramString, Class[] paramArrayOfClass, ClassLoader paramClassLoader)
    throws Exception
  {
    Class localClass = null;
    Constructor localConstructor = null;
    try
    {
      localClass = getClass(paramString, paramClassLoader);
      if (null == localClass)
        throw new Exception("Class: '" + paramString + "' not found");
      try
      {
        localConstructor = localClass.getDeclaredConstructor(paramArrayOfClass);
      }
      catch (NoSuchMethodException localNoSuchMethodException)
      {
        throw new Exception("Constructor: '" + paramString + "' (" + paramArrayOfClass + ") not found");
      }
      return localConstructor;
    }
    catch (Throwable localThrowable)
    {
      if (DEBUG)
        localThrowable.printStackTrace();
    }
    throw new Exception(localThrowable);
  }

  private static Constructor getConstructor(String paramString, ClassLoader paramClassLoader)
    throws Exception
  {
    return getConstructor(paramString, new Class[0], paramClassLoader);
  }

  public static Object createInstance(String paramString, Class[] paramArrayOfClass, Object[] paramArrayOfObject, ClassLoader paramClassLoader)
    throws Exception
  {
    Constructor localConstructor = null;
    try
    {
      localConstructor = getConstructor(paramString, paramArrayOfClass, paramClassLoader);
      return localConstructor.newInstance(paramArrayOfObject);
    }
    catch (Exception localException)
    {
    }
    throw new Exception(localException);
  }

  public static Object createInstance(String paramString, String[] paramArrayOfString, Object[] paramArrayOfObject, ClassLoader paramClassLoader)
    throws Exception
  {
    Class[] arrayOfClass = new Class[paramArrayOfString.length];
    for (int i = 0; i < paramArrayOfString.length; i++)
    {
      arrayOfClass[i] = getClass(paramArrayOfString[i], paramClassLoader);
      if (null != arrayOfClass[i])
        continue;
      throw new Exception("Class: '" + paramArrayOfString[i] + "' not found");
    }
    return createInstance(paramString, arrayOfClass, paramArrayOfObject, paramClassLoader);
  }

  public static Object createInstance(String paramString, ClassLoader paramClassLoader)
    throws Exception
  {
    return createInstance(paramString, new Class[0], new Object[0], paramClassLoader);
  }

  public static Method getMethod(Object paramObject, String paramString, Class[] paramArrayOfClass, boolean paramBoolean)
    throws Throwable
  {
    try
    {
      if (null == paramArrayOfClass)
        paramArrayOfClass = new Class[0];
      Method localMethod = paramObject.getClass().getMethod(paramString, paramArrayOfClass);
      if (VERBOSE)
        System.out.println("ReflectionUtil (" + paramObject.getClass() + ") Have: " + localMethod.toString());
      return localMethod;
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      if (paramBoolean)
        throw localNoSuchMethodException;
      if (VERBOSE)
        System.out.println("ReflectionUtil (" + paramObject.getClass() + ") NoSuchMethodException: " + localNoSuchMethodException.getMessage());
    }
    catch (Throwable localThrowable)
    {
      if (paramBoolean)
        throw localThrowable;
      if (VERBOSE)
        localThrowable.printStackTrace();
    }
    return null;
  }

  public static Object invoke(Object paramObject, String paramString, Class[] paramArrayOfClass, Object[] paramArrayOfObject)
    throws Exception
  {
    try
    {
      Method localMethod = getMethod(paramObject, paramString, paramArrayOfClass, true);
      return localMethod.invoke(paramObject, paramArrayOfObject);
    }
    catch (Throwable localThrowable)
    {
      if (DEBUG)
        localThrowable.printStackTrace();
    }
    throw new Exception(localThrowable);
  }

  public static Object invoke(Object paramObject, String paramString, String[] paramArrayOfString, Object[] paramArrayOfObject, ClassLoader paramClassLoader)
    throws Exception
  {
    Class[] arrayOfClass = new Class[paramArrayOfString.length];
    for (int i = 0; i < paramArrayOfString.length; i++)
    {
      arrayOfClass[i] = getClass(paramArrayOfString[i], paramClassLoader);
      if (null != arrayOfClass[i])
        continue;
      throw new Exception("Class: '" + paramArrayOfString[i] + "' not found");
    }
    return invoke(paramObject, paramString, arrayOfClass, paramArrayOfObject);
  }

  public static boolean instanceOf(Object paramObject, String paramString)
  {
    return instanceOf(paramObject.getClass(), paramString);
  }

  public static boolean instanceOf(Class paramClass, String paramString)
  {
    do
    {
      if (paramClass.getName().equals(paramString))
        return true;
      paramClass = paramClass.getSuperclass();
    }
    while (paramClass != null);
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.ReflectionUtil
 * JD-Core Version:    0.6.0
 */