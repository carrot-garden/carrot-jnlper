package com.sun.deploy.uitoolkit.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public final class ConsoleHelper
{
  public static void dumpAllStacks(ConsoleController paramConsoleController)
  {
    if (!paramConsoleController.isDumpStackSupported())
      return;
    if (Config.isJavaVersionAtLeast16())
      Trace.println(dumpAllStacksImpl());
    else
      Trace.println(preMustangDumpAllStacksImpl());
  }

  private static native String dumpAllStacksImpl();

  private static native String preMustangDumpAllStacksImpl();

  public static void dumpThreadGroup(ThreadGroup paramThreadGroup)
  {
    if (paramThreadGroup != null)
    {
      try
      {
        if ((paramThreadGroup.activeCount() == 0) && (paramThreadGroup.activeGroupCount() == 0) && (!paramThreadGroup.isDestroyed()))
          paramThreadGroup.destroy();
      }
      catch (Throwable localThrowable)
      {
      }
      StringBuffer localStringBuffer = new StringBuffer();
      localStringBuffer.append("Group ").append(paramThreadGroup.getName());
      localStringBuffer.append(",ac=").append(paramThreadGroup.activeCount());
      localStringBuffer.append(",agc=").append(paramThreadGroup.activeGroupCount());
      localStringBuffer.append(",pri=").append(paramThreadGroup.getMaxPriority());
      if (paramThreadGroup.isDestroyed())
        localStringBuffer.append(",destoyed");
      if (paramThreadGroup.isDaemon())
        localStringBuffer.append(",daemon");
      Trace.println(localStringBuffer.toString());
      Thread[] arrayOfThread = new Thread[1000];
      paramThreadGroup.enumerate(arrayOfThread, false);
      for (int i = 0; i < arrayOfThread.length; i++)
      {
        if (arrayOfThread[i] == null)
          continue;
        localStringBuffer = new StringBuffer();
        localStringBuffer.append("    ");
        localStringBuffer.append(arrayOfThread[i].getName());
        localStringBuffer.append(",");
        localStringBuffer.append(arrayOfThread[i].getPriority());
        if (arrayOfThread[i].isAlive())
          localStringBuffer.append(",alive");
        else
          localStringBuffer.append(",not alive");
        if (arrayOfThread[i].isDaemon())
          localStringBuffer.append(",daemon");
        if (arrayOfThread[i].isInterrupted())
          localStringBuffer.append(",interrupted");
        Trace.println(localStringBuffer.toString());
      }
      ThreadGroup[] arrayOfThreadGroup = new ThreadGroup[1000];
      paramThreadGroup.enumerate(arrayOfThreadGroup, false);
      for (int j = 0; j < arrayOfThreadGroup.length; j++)
      {
        if (arrayOfThreadGroup[j] == null)
          continue;
        dumpThreadGroup(arrayOfThreadGroup[j]);
      }
    }
  }

  public static void displayHelp(ConsoleController paramConsoleController, ConsoleWindow paramConsoleWindow)
  {
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.top"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.c"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.f"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.g"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.h"));
    if (paramConsoleController.isDumpClassLoaderSupported())
      paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.l"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.m"));
    if (paramConsoleController.isLoggingSupported())
      paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.o"));
    if (paramConsoleController.isProxyConfigReloadSupported())
      paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.p"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.q"));
    if (paramConsoleController.isSecurityPolicyReloadSupported())
      paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.r"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.s"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.t"));
    if (paramConsoleController.isDumpStackSupported())
      paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.v"));
    if (paramConsoleController.isClearClassLoaderSupported())
      paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.x"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.0"));
    paramConsoleWindow.append(ResourceManager.getMessage("console.menu.text.tail"));
  }

  public static void displayVersion(ConsoleController paramConsoleController, ConsoleWindow paramConsoleWindow)
  {
    paramConsoleWindow.append(paramConsoleController.getProductName());
    paramConsoleWindow.append("\n");
    paramConsoleWindow.append(ResourceManager.getMessage("console.using_jre_version"));
    paramConsoleWindow.append(" ");
    paramConsoleWindow.append(System.getProperty("java.runtime.version"));
    paramConsoleWindow.append(" ");
    paramConsoleWindow.append(System.getProperty("java.vm.name"));
    paramConsoleWindow.append("\n");
    paramConsoleWindow.append(ResourceManager.getMessage("console.user_home"));
    paramConsoleWindow.append(" = ");
    paramConsoleWindow.append(System.getProperty("user.home"));
  }

  public static void displaySystemProperties()
  {
    TreeSet localTreeSet = new TreeSet();
    Config localConfig = Config.get();
    Object localObject = System.getProperties().propertyNames();
    String str1;
    while (((Enumeration)localObject).hasMoreElements())
    {
      str1 = (String)((Enumeration)localObject).nextElement();
      if (!localConfig.containsKey(str1))
        localTreeSet.add(str1);
    }
    Trace.print(ResourceManager.getMessage("console.dump.system.properties"));
    Trace.print(ResourceManager.getMessage("console.menu.text.top"));
    localObject = localTreeSet.iterator();
    String str2;
    while (((Iterator)localObject).hasNext())
    {
      str1 = (String)((Iterator)localObject).next();
      str2 = System.getProperty(str1);
      if (str2 != null)
        if (str2.equals("\n"))
          str2 = "\\n";
        else if (str2.equals("\r"))
          str2 = "\\r";
        else if (str2.equals("\r\n"))
          str2 = "\\r\\n";
        else if (str2.equals("\n\r"))
          str2 = "\\n\\r";
        else if (str2.equals("\n\n"))
          str2 = "\\n\\n";
        else if (str2.equals("\r\r"))
          str2 = "\\r\\r";
      Trace.println(str1 + " = " + str2);
    }
    Trace.print(ResourceManager.getMessage("console.menu.text.tail"));
    localTreeSet.clear();
    localObject = localConfig.propertyNames();
    while (((Enumeration)localObject).hasMoreElements())
      localTreeSet.add(((Enumeration)localObject).nextElement());
    Trace.print(ResourceManager.getMessage("console.dump.deployment.properties"));
    Trace.print(ResourceManager.getMessage("console.menu.text.top"));
    localObject = localTreeSet.iterator();
    while (((Iterator)localObject).hasNext())
    {
      str1 = (String)((Iterator)localObject).next();
      str2 = Config.getStringProperty(str1);
      if (str2 != null)
        if (str2.equals("\n"))
          str2 = "\\n";
        else if (str2.equals("\r"))
          str2 = "\\r";
        else if (str2.equals("\r\n"))
          str2 = "\\r\\n";
        else if (str2.equals("\n\r"))
          str2 = "\\n\\r";
        else if (str2.equals("\n\n"))
          str2 = "\\n\\n";
        else if (str2.equals("\r\r"))
          str2 = "\\r\\r";
      Trace.println(str1 + " = " + str2);
    }
    Trace.print(ResourceManager.getMessage("console.menu.text.tail"));
    Trace.print(ResourceManager.getMessage("console.done"));
  }

  public static void displayOldPluginWarning(ConsoleController paramConsoleController, ConsoleWindow paramConsoleWindow)
  {
    paramConsoleWindow.append(ResourceManager.getMessage("console.show.oldplugin.warning"));
    paramConsoleWindow.append("\n");
  }

  public static void setTraceLevel(int paramInt)
  {
    Trace.setEnabled(TraceLevel.BASIC, paramInt >= 1);
    Trace.setEnabled(TraceLevel.CACHE, paramInt >= 2);
    Trace.setEnabled(TraceLevel.NETWORK, paramInt >= 3);
    Trace.setEnabled(TraceLevel.SECURITY, paramInt >= 3);
    Trace.setEnabled(TraceLevel.EXTENSIONS, paramInt >= 4);
    Trace.setEnabled(TraceLevel.LIVECONNECT, paramInt >= 5);
    Trace.setEnabled(TraceLevel.TEMP, paramInt >= 5);
    Trace.print(ResourceManager.getMessage("console.trace.level." + Integer.toString(paramInt)));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.uitoolkit.ui.ConsoleHelper
 * JD-Core Version:    0.6.0
 */