package com.sun.javaws.jnl;

import com.sun.deploy.config.JREInfo;
import com.sun.deploy.util.SecurityBaseline;
import com.sun.deploy.util.VersionString;
import java.net.URL;

public class SecureMatchJRE extends DefaultMatchJRE
{
  public boolean isVersionMatch(LaunchDesc paramLaunchDesc, JREInfo paramJREInfo)
  {
    return SecurityBaseline.satisfiesSecurityBaseline(paramJREInfo.getProduct());
  }

  public boolean isVersionMatch(JREInfo paramJREInfo, VersionString paramVersionString, URL paramURL)
  {
    return SecurityBaseline.satisfiesSecurityBaseline(paramJREInfo.getProduct());
  }

  public boolean isRunningJVMSatisfying(boolean paramBoolean)
  {
    String str = System.getProperty("java.version");
    boolean bool = SecurityBaseline.satisfiesSecurityBaseline(str);
    return (super.isRunningJVMSatisfying(paramBoolean)) && (bool);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.SecureMatchJRE
 * JD-Core Version:    0.6.0
 */