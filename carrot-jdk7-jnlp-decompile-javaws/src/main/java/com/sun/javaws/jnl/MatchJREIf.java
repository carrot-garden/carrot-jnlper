package com.sun.javaws.jnl;

import com.sun.deploy.config.JREInfo;
import com.sun.deploy.util.JVMParameters;
import com.sun.deploy.util.VersionString;
import java.net.URL;

public abstract interface MatchJREIf
{
  public abstract boolean hasBeenRun();

  public abstract void beginTraversal(LaunchDesc paramLaunchDesc);

  public abstract void digest(JREDesc paramJREDesc, JREInfo paramJREInfo);

  public abstract void digest(LaunchDesc paramLaunchDesc);

  public abstract void endTraversal(LaunchDesc paramLaunchDesc);

  public abstract JREInfo getSelectedJREInfo();

  public abstract JREDesc getSelectedJREDesc();

  public abstract JVMParameters getSelectedJVMParameters();

  public abstract String getSelectedJVMParameterString();

  public abstract long getSelectedInitHeapSize();

  public abstract long getSelectedMaxHeapSize();

  public abstract boolean isRunningJVMSatisfying(boolean paramBoolean);

  public abstract boolean isRunningJVMVersionSatisfying();

  public abstract boolean isRunningJVMArgsSatisfying(boolean paramBoolean);

  public abstract boolean isVersionMatch(LaunchDesc paramLaunchDesc, JREInfo paramJREInfo);

  public abstract boolean isVersionMatch(JREInfo paramJREInfo, VersionString paramVersionString, URL paramURL);

  public abstract String toString();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.MatchJREIf
 * JD-Core Version:    0.6.0
 */