package com.sun.javaws.jnl;

import com.sun.deploy.Environment;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.config.Platform;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.JVMParameters;
import com.sun.deploy.util.VersionID;
import com.sun.deploy.util.VersionString;
import java.io.File;
import java.net.URL;

public class DefaultMatchJRE
  implements MatchJREIf
{
  private static final boolean DEBUG = false;
  private JREDesc selectedJREDesc;
  private JREInfo selectedJREInfo;
  private boolean matchComplete;
  private boolean matchSecureComplete;
  private boolean matchVersion;
  private boolean matchJVMArgs;
  private boolean matchSecureJVMArgs;
  private long selectedMaxHeap;
  private long selectedInitHeap;
  private String selectedJVMArgString;
  private JVMParameters selectedJVMArgs;

  public DefaultMatchJRE()
  {
    reset(null);
  }

  public boolean hasBeenRun()
  {
    return null != this.selectedJVMArgs;
  }

  public void beginTraversal(LaunchDesc paramLaunchDesc)
  {
    reset(paramLaunchDesc);
  }

  private void reset(LaunchDesc paramLaunchDesc)
  {
    this.matchComplete = false;
    this.matchSecureComplete = false;
    this.matchVersion = false;
    this.matchJVMArgs = false;
    this.matchSecureJVMArgs = false;
    this.selectedInitHeap = -1L;
    this.selectedJVMArgString = null;
    this.selectedJREDesc = null;
    this.selectedJREInfo = null;
    if (null == paramLaunchDesc)
    {
      this.selectedMaxHeap = -1L;
      this.selectedJVMArgs = null;
    }
    else
    {
      this.selectedMaxHeap = JVMParameters.getDefaultHeapSize();
      this.selectedJVMArgs = new JVMParameters();
    }
  }

  public JREInfo getSelectedJREInfo()
  {
    return this.selectedJREInfo;
  }

  public JREDesc getSelectedJREDesc()
  {
    return this.selectedJREDesc;
  }

  public JVMParameters getSelectedJVMParameters()
  {
    return this.selectedJVMArgs;
  }

  public String getSelectedJVMParameterString()
  {
    return this.selectedJVMArgString;
  }

  public long getSelectedInitHeapSize()
  {
    return this.selectedInitHeap;
  }

  public long getSelectedMaxHeapSize()
  {
    return this.selectedMaxHeap;
  }

  public boolean isRunningJVMSatisfying(boolean paramBoolean)
  {
    if (paramBoolean)
      return this.matchComplete;
    return this.matchSecureComplete;
  }

  public boolean isRunningJVMVersionSatisfying()
  {
    return this.matchVersion;
  }

  public boolean isRunningJVMArgsSatisfying(boolean paramBoolean)
  {
    if (paramBoolean)
      return this.matchJVMArgs;
    return this.matchSecureJVMArgs;
  }

  public void digest(JREDesc paramJREDesc, JREInfo paramJREInfo)
  {
    this.selectedJREDesc = paramJREDesc;
    this.selectedJREInfo = paramJREInfo;
    long l = paramJREDesc.getMaxHeap();
    if (l > this.selectedMaxHeap)
      this.selectedMaxHeap = l;
    l = paramJREDesc.getMinHeap();
    if (l > this.selectedInitHeap)
      this.selectedInitHeap = l;
    this.selectedJVMArgs.parse(paramJREDesc.getVmArgs());
    l = this.selectedJVMArgs.getMaxHeapSize();
    if (l > this.selectedMaxHeap)
      this.selectedMaxHeap = l;
    this.selectedJVMArgs.setMaxHeapSize(JVMParameters.getDefaultHeapSize());
  }

  public void digest(LaunchDesc paramLaunchDesc)
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (null != localResourcesDesc)
      this.selectedJVMArgs.addProperties(localResourcesDesc.getResourcePropertyList());
  }

  public void endTraversal(LaunchDesc paramLaunchDesc)
  {
    if ((paramLaunchDesc.isApplicationDescriptor()) && (null == this.selectedJREDesc))
      throw new IllegalArgumentException("selectedJREDesc null");
    if ((this.selectedInitHeap > 0L) && (this.selectedInitHeap != JVMParameters.getDefaultHeapSize()))
      this.selectedJVMArgs.parse("-Xms" + JVMParameters.unparseMemorySpec(this.selectedInitHeap));
    this.selectedJVMArgs.setMaxHeapSize(this.selectedMaxHeap);
    this.selectedJVMArgString = this.selectedJVMArgs.getCommandLineArgumentsAsString(false);
    if (this.selectedJREInfo == null)
      return;
    this.matchVersion = isVersionMatch(paramLaunchDesc, this.selectedJREInfo);
    JVMParameters localJVMParameters = JVMParameters.getRunningJVMParameters();
    if (localJVMParameters == null)
    {
      if (Trace.isEnabled(TraceLevel.BASIC))
        Trace.println("\t Match: Running JVM is not set: want:<" + this.selectedJVMArgs.getCommandLineArgumentsAsString(false) + ">", TraceLevel.BASIC);
      this.matchJVMArgs = false;
      this.matchSecureJVMArgs = false;
    }
    else if (localJVMParameters.satisfies(this.selectedJVMArgs))
    {
      this.matchJVMArgs = true;
      this.matchSecureJVMArgs = true;
    }
    else if (localJVMParameters.satisfiesSecure(this.selectedJVMArgs))
    {
      this.matchJVMArgs = false;
      this.matchSecureJVMArgs = true;
    }
    else
    {
      this.matchJVMArgs = false;
      this.matchSecureJVMArgs = false;
    }
    this.matchComplete = (this.matchVersion & this.matchJVMArgs);
    this.matchSecureComplete = (this.matchVersion & this.matchSecureJVMArgs);
  }

  public static boolean isInstallJRE(JREInfo paramJREInfo)
  {
    File localFile1 = new File(Environment.getJavaHome());
    File localFile2 = new File(paramJREInfo.getPath());
    File localFile3 = localFile2.getParentFile();
    File localFile4 = new File(localFile1, "lib" + File.separator + "rt.jar");
    if (!localFile4.exists())
      return true;
    return Platform.get().samePaths(localFile1.getPath(), localFile3.getParentFile().getPath());
  }

  public static boolean isPlatformMatch(JREInfo paramJREInfo, VersionString paramVersionString)
  {
    String str = paramJREInfo.getProduct();
    int i;
    if ((str == null) || (isInstallJRE(paramJREInfo)) || (str.indexOf('-') == -1) || (str.indexOf("-rev") != -1) || (str.indexOf("-er") != -1))
      i = 1;
    else
      i = 0;
    if (new File(paramJREInfo.getPath()).exists())
      return (paramVersionString.contains(paramJREInfo.getPlatform())) && (i != 0);
    return false;
  }

  public static boolean isProductMatch(JREInfo paramJREInfo, URL paramURL, VersionString paramVersionString)
  {
    if (new File(paramJREInfo.getPath()).exists())
      return (paramJREInfo.getLocation().equals(paramURL.toString())) && (paramVersionString.contains(paramJREInfo.getProduct()));
    return false;
  }

  public boolean isVersionMatch(JREInfo paramJREInfo, VersionString paramVersionString, URL paramURL)
  {
    return paramURL == null ? isPlatformMatch(paramJREInfo, paramVersionString) : isProductMatch(paramJREInfo, paramURL, paramVersionString);
  }

  public boolean isVersionMatch(LaunchDesc paramLaunchDesc, JREInfo paramJREInfo)
  {
    JREInfo localJREInfo = paramLaunchDesc.getHomeJRE();
    return localJREInfo.getProductVersion().match(paramJREInfo.getProductVersion());
  }

  public String toString()
  {
    return "DefaultMatchJRE: \n  JREDesc:    " + getSelectedJREDesc() + "\n  JREInfo:    " + getSelectedJREInfo() + "\n  Init Heap:  " + getSelectedInitHeapSize() + "\n  Max  Heap:  " + getSelectedMaxHeapSize() + "\n  Satisfying: " + isRunningJVMSatisfying(true) + ", " + isRunningJVMSatisfying(false) + "\n  SatisfyingVersion: " + isRunningJVMVersionSatisfying() + "\n  SatisfyingJVMArgs: " + isRunningJVMArgsSatisfying(true) + ", " + isRunningJVMSatisfying(false) + "\n  SatisfyingSecure: " + isRunningJVMSatisfying(true) + "\n  Selected JVMParam: " + getSelectedJVMParameters() + "\n  Running  JVMParam: " + JVMParameters.getRunningJVMParameters();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.DefaultMatchJRE
 * JD-Core Version:    0.6.0
 */