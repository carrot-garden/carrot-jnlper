package com.sun.javaws.jnl;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.VersionString;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class LaunchSelection
{
  private static final String anyJREVersion = "0+";

  protected static JREInfo selectJRE(LaunchDesc paramLaunchDesc, MatchJREIf paramMatchJREIf)
  {
    synchronized (paramMatchJREIf)
    {
      paramMatchJREIf.beginTraversal(paramLaunchDesc);
      ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
      if (localResourcesDesc != null)
      {
        selectJREDescHelper(paramLaunchDesc, paramMatchJREIf);
        JREDesc localJREDesc = paramMatchJREIf.getSelectedJREDesc();
        if (localJREDesc != null)
        {
          localJREDesc.markAsSelected();
          localResourcesDesc.addNested(localJREDesc.getNestedResources());
        }
        selectJREExtensionHelper(paramLaunchDesc, paramMatchJREIf);
      }
      paramMatchJREIf.endTraversal(paramLaunchDesc);
      return paramMatchJREIf.getSelectedJREInfo();
    }
  }

  private static void selectJREDescHelper(LaunchDesc paramLaunchDesc, MatchJREIf paramMatchJREIf)
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return;
    ArrayList localArrayList = new ArrayList();
    localResourcesDesc.visit(new ResourceVisitor(paramLaunchDesc, localArrayList)
    {
      private final LaunchDesc val$ld;
      private final ArrayList val$listJREDesc;

      public void visitJREDesc(JREDesc paramJREDesc)
      {
        if (this.val$ld.isLibrary())
          Trace.println("JNLP JREDesc in Component ignored: " + this.val$ld.getLocation());
        else
          this.val$listJREDesc.add(paramJREDesc);
      }
    });
    JREDesc localJREDesc = null;
    JREInfo localJREInfo = null;
    if (localArrayList.size() > 0)
    {
      for (int i = 0; (localJREInfo == null) && (i < localArrayList.size()); i++)
      {
        localJREDesc = (JREDesc)localArrayList.get(i);
        URL localURL = localJREDesc.getHref();
        localJREInfo = selectJRE(localURL, localJREDesc.getVersion(), paramMatchJREIf);
      }
      if (localJREInfo == null)
        localJREDesc = (JREDesc)localArrayList.get(0);
    }
    else
    {
      localJREDesc = new JREDesc("0+", -1L, -1L, null, null, new ResourcesDesc());
      localResourcesDesc.addResource(localJREDesc);
      localJREInfo = selectJRE(localJREDesc.getHref(), localJREDesc.getVersion(), paramMatchJREIf);
    }
    paramMatchJREIf.digest(localJREDesc, localJREInfo);
  }

  private static void selectJREExtensionHelper(LaunchDesc paramLaunchDesc, MatchJREIf paramMatchJREIf)
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc == null)
      return;
    ArrayList localArrayList = new ArrayList();
    localResourcesDesc.visit(new ResourceVisitor(localArrayList)
    {
      private final ArrayList val$listExtDesc;

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        this.val$listExtDesc.add(paramExtensionDesc);
      }
    });
    paramMatchJREIf.digest(paramLaunchDesc);
    if (paramLaunchDesc.isInstaller())
      return;
    for (int i = 0; i < localArrayList.size(); i++)
    {
      ExtensionDesc localExtensionDesc = (ExtensionDesc)localArrayList.get(i);
      LaunchDesc localLaunchDesc = localExtensionDesc.getExtensionDesc();
      if (localLaunchDesc == null)
        try
        {
          File localFile = DownloadEngine.getCachedFile(localExtensionDesc.getLocation(), localExtensionDesc.getVersion(), false, false, null);
          if (null != localFile)
            localLaunchDesc = LaunchDescFactory.buildDescriptor(localFile, localExtensionDesc.getCodebase(), localExtensionDesc.getLocation(), localExtensionDesc.getLocation());
        }
        catch (Exception localException)
        {
          Trace.ignoredException(localException);
        }
      if (null == localLaunchDesc)
        continue;
      if (localLaunchDesc.isInstaller())
      {
        localExtensionDesc.setInstaller(true);
      }
      else
      {
        localExtensionDesc.setExtensionDesc(localLaunchDesc);
        selectJREExtensionHelper(localLaunchDesc, paramMatchJREIf);
      }
    }
  }

  public static JREInfo selectJRE(URL paramURL, String paramString, MatchJREIf paramMatchJREIf)
  {
    JREInfo[] arrayOfJREInfo = JREInfo.getAll();
    if (arrayOfJREInfo == null)
      return null;
    VersionString localVersionString = new VersionString(paramString);
    for (int i = 0; i < arrayOfJREInfo.length; i++)
      if ((arrayOfJREInfo[i].isOsInfoMatch(Config.getOSName(), Config.getOSArch())) && ((arrayOfJREInfo[i].isEnabled()) || (arrayOfJREInfo[i].getPath().equals(JREInfo.getHomeJRE().getPath()))) && (paramMatchJREIf.isVersionMatch(arrayOfJREInfo[i], localVersionString, paramURL)))
        return arrayOfJREInfo[i];
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.LaunchSelection
 * JD-Core Version:    0.6.0
 */