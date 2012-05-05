package com.sun.deploy.uitoolkit;

import com.sun.deploy.net.DeployClassLoader;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.deploy.util.ReflectionUtil;
import java.io.PrintStream;

public class ToolkitStore
{
  public static final String UITOOLKIT = "jnlp.uitoolkit";
  public static final String JNLP_TK = "jnlp.tk";
  public static final String JNLP_FX = "jnlp.fx";
  public static final String JNLP_TK_AWT = "awt";
  public static final String JNLP_TK_FX = "jfx";
  public static final String JNLP_TK_TEXT = "text";
  public static final String AWT_IMPL_CLASS = "com.sun.deploy.uitoolkit.impl.awt.UIToolkitImpl";
  public static final String AWT_PLUGIN_IMPL_CLASS = "com.sun.deploy.uitoolkit.impl.awt.AWTPluginUIToolkit";
  public static final String FX_PLUGIN_IMPL_CLASS = "com.sun.deploy.uitoolkit.impl.fx.FXPluginToolkit";
  public static final String TEXT_PLUGIN_IMPL_CLASS = "com.sun.deploy.uitoolkit.impl.text.TextPluginUIToolkit";
  public static final String TEXT_IMPL_CLASS = "com.sun.deploy.uitoolkit.impl.text.TextUIToolkit";
  private static Class toolkitClass;
  private static volatile UIToolkit toolkitImpl = null;
  public static final int PLUGIN_MODE = 1;
  public static final int DESKTOP_MODE = 0;
  public static final int AWT_TOOLKIT = 10;
  public static final int FX_TOOLKIT = 11;
  public static final int TEXT_TOOLKIT = 12;
  private static int useTk = 10;
  private static boolean isPlugin = false;
  private static boolean forceToolkitMatchForTests = false;

  private static boolean isInitialized()
  {
    return null != toolkitImpl;
  }

  private static UIToolkit loadToolkit(String paramString)
    throws Exception
  {
    ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
    if (!ReflectionUtil.isClassAvailable(paramString, localClassLoader))
      throw new ClassNotFoundException(paramString + " cannot be found.");
    Class localClass = Class.forName(paramString, true, localClassLoader);
    UIToolkit localUIToolkit = (UIToolkit)localClass.newInstance();
    localUIToolkit.init();
    return localUIToolkit;
  }

  private static String getToolkitClassName(int paramInt1, int paramInt2)
  {
    String str;
    switch (paramInt1)
    {
    case 10:
      str = paramInt2 == 1 ? "com.sun.deploy.uitoolkit.impl.awt.AWTPluginUIToolkit" : "com.sun.deploy.uitoolkit.impl.awt.UIToolkitImpl";
      break;
    case 11:
      str = "com.sun.deploy.uitoolkit.impl.fx.FXPluginToolkit";
      break;
    case 12:
      str = paramInt2 == 1 ? "com.sun.deploy.uitoolkit.impl.text.TextPluginUIToolkit" : "com.sun.deploy.uitoolkit.impl.text.TextUIToolkit";
      break;
    default:
      throw new IllegalArgumentException("Invalid toolkit type.");
    }
    return str;
  }

  private static void ensureJfxAvailability(String paramString)
    throws ClassNotFoundException
  {
    if ((null == paramString) || (paramString.length() == 0))
      paramString = "2.0+";
    String str = paramString;
    for (ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader(); localClassLoader != null; localClassLoader = localClassLoader.getParent())
    {
      if (!(localClassLoader instanceof DeployClassLoader))
        continue;
      DeployClassLoader localDeployClassLoader = (DeployClassLoader)localClassLoader;
      try
      {
        localDeployClassLoader.injectJfx(str);
      }
      catch (IllegalStateException localIllegalStateException)
      {
        Trace.ignored(localIllegalStateException);
      }
      finally
      {
      }
    }
  }

  private static void ensureAWTAvailability()
    throws Exception
  {
    if (!ReflectionUtil.isClassAvailable("sun.awt.SunToolkit", null))
      throw new Exception("AWT is not available !!");
  }

  private static synchronized UIToolkit init()
    throws Exception
  {
    String str1 = System.getProperty("jnlp.uitoolkit");
    if (str1 != null)
    {
      str1 = str1.trim();
      Trace.println("Set UIToolkit through system property: " + str1, TraceLevel.UI);
      try
      {
        if (str1.equalsIgnoreCase("com.sun.deploy.uitoolkit.impl.fx.FXPluginToolkit"))
          ensureJfxAvailability("2.0+");
        toolkitImpl = loadToolkit(str1);
        toolkitClass = toolkitImpl.getClass();
        return toolkitImpl;
      }
      catch (Exception localException1)
      {
        Trace.println(str1 + " is not available, move on to next...", TraceLevel.UI);
      }
    }
    else
    {
      String str2 = System.getProperty("jnlp.tk");
      if (str2 != null)
      {
        str2 = str2.trim();
        if (str2.equalsIgnoreCase("jfx"))
        {
          setToolkitType(11);
          try
          {
            ensureJfxAvailability("2.0+");
            setToolkitType(11);
          }
          catch (ClassNotFoundException localClassNotFoundException1)
          {
            Trace.ignored(localClassNotFoundException1);
            setToolkitType(10);
          }
        }
        else if (str2.equalsIgnoreCase("text"))
        {
          setToolkitType(12);
        }
        else
        {
          setToolkitType(10);
        }
      }
      else
      {
        String str3 = System.getProperty("jnlp.fx");
        if (str3 != null)
          try
          {
            ensureJfxAvailability(str3);
            setToolkitType(11);
          }
          catch (ClassNotFoundException localClassNotFoundException2)
          {
            Trace.ignored(localClassNotFoundException2);
            setToolkitType(10);
          }
        else
          setToolkitType(10);
      }
    }
    Trace.println("toolkit class not specified, use current settings...", TraceLevel.UI);
    if (useTk == 10)
      ensureAWTAvailability();
    try
    {
      str1 = getToolkitClassName(useTk, isPlugin ? 1 : 0);
      Trace.println("Try to load UIToolkit: " + str1, TraceLevel.UI);
      toolkitImpl = loadToolkit(str1);
    }
    catch (Exception localException2)
    {
      if (useTk != 10)
      {
        Trace.println("Fallback to AWT toolkit...", TraceLevel.UI);
        ensureAWTAvailability();
        str1 = getToolkitClassName(10, isPlugin ? 1 : 0);
        toolkitImpl = loadToolkit(str1);
      }
      else
      {
        throw localException2;
      }
    }
    toolkitImpl.init();
    toolkitClass = toolkitImpl.getClass();
    return toolkitImpl;
  }

  public static void setMode(int paramInt)
  {
    if (isInitialized())
    {
      if ((paramInt == 1) == isPlugin)
      {
        Trace.println("don't need to switch mode", TraceLevel.UI);
        return;
      }
      Trace.println("Attempt to switch toolkit mode after initialization to mode: " + paramInt, TraceLevel.UI);
      toolkitImpl = toolkitImpl.changeMode(paramInt);
    }
    if (paramInt == 1)
      isPlugin = true;
    else if (paramInt == 0)
      isPlugin = false;
    else
      Trace.println("Error: ToolkitStore.setMode() invalid argument " + paramInt, TraceLevel.UI);
  }

  public static void setToolkitType(int paramInt)
  {
    if (isInitialized())
      Trace.println("Warning: ToolkitStore.setType() called after initialized, may not be effective", TraceLevel.UI);
    if ((paramInt != 11) && (paramInt != 12) && (paramInt != 10))
      Trace.println("Error: ToolkitStore.setType() invalid argument " + paramInt, TraceLevel.UI);
    useTk = paramInt;
  }

  static synchronized void setUIToolkit(Class paramClass)
  {
    if (isInitialized())
      if (toolkitClass != paramClass)
        Trace.println("Warning: SetUIToolkit changed toolkit after initialization", TraceLevel.UI);
      else
        return;
    Trace.println("UIToolkit set to" + paramClass.getName(), TraceLevel.UI);
    if (UIToolkit.class.isAssignableFrom(paramClass))
    {
      toolkitClass = paramClass;
      if (toolkitImpl != null)
        try
        {
          toolkitImpl = (UIToolkit)toolkitClass.newInstance();
        }
        catch (Exception localException)
        {
          System.err.println("Exception instantiating toolkit" + localException);
        }
    }
    else
    {
      System.err.println("setUIToolkit: class is not a  UIToolkit class");
    }
  }

  public static synchronized void dispose()
    throws Exception
  {
    forceToolkitMatchForTests = false;
    if (!isInitialized())
      return;
    toolkitImpl.dispose();
    toolkitImpl = null;
    toolkitClass = null;
  }

  public static UIToolkit get()
  {
    if (!isInitialized())
      synchronized (ToolkitStore.class)
      {
        if (!isInitialized())
          try
          {
            init();
          }
          catch (Exception localException)
          {
            localException.printStackTrace();
          }
      }
    return toolkitImpl;
  }

  public static UIFactory getUI()
  {
    return get().getUIFactory();
  }

  public static WindowFactory getWindowFactory()
  {
    return get().getWindowFactory();
  }

  public static boolean isUsingPreferredToolkit(int paramInt1, int paramInt2)
  {
    if (forceToolkitMatchForTests)
      return true;
    if (!isInitialized())
      synchronized (ToolkitStore.class)
      {
        setMode(paramInt2);
        setToolkitType(paramInt1);
        get();
      }
    ??? = get().getClass().getCanonicalName();
    String str = System.getProperty("jnlp.uitoolkit");
    if (null != str)
      str = str.trim();
    else
      str = getToolkitClassName(paramInt1, paramInt2);
    return ((String)???).equals(str);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.ToolkitStore
 * JD-Core Version:    0.6.0
 */