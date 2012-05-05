package com.sun.jnlp;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.util.JNLPUtils;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.ArrayList;

public final class PreverificationClassLoader extends URLClassLoader
{
  private ArrayList _jarsInURLClassLoader = new ArrayList();
  private ArrayList _jarsNotInURLClassLoader = new ArrayList();

  public PreverificationClassLoader(ClassLoader paramClassLoader)
  {
    super(new URL[0], paramClassLoader);
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkCreateClassLoader();
  }

  protected PermissionCollection getPermissions(CodeSource paramCodeSource)
  {
    PermissionCollection localPermissionCollection = super.getPermissions(paramCodeSource);
    localPermissionCollection.add(new AllPermission());
    return localPermissionCollection;
  }

  public void preverifyJARs()
  {
    if (!Cache.isCacheEnabled())
      return;
    long l1 = System.currentTimeMillis();
    for (int i = 0; i < this._jarsInURLClassLoader.size(); i++)
    {
      JARDesc localJARDesc = (JARDesc)this._jarsInURLClassLoader.get(i);
      File localFile = new File(Config.getSystemCacheDirectory() + File.separator + Cache.getCacheVersionString());
      CacheEntry localCacheEntry = Cache.getCacheEntry(localJARDesc.getLocation(), null, localJARDesc.getVersion(), localFile);
      if ((localCacheEntry == null) || (localCacheEntry.getClassesVerificationStatus() != 0))
        continue;
      localCacheEntry.verifyJAR(this);
    }
    long l2 = System.currentTimeMillis();
    Trace.println("PreverificationCL, Cached JAR preverification took (ms): " + (l2 - l1), TraceLevel.CACHE);
  }

  public void initialize(LaunchDesc paramLaunchDesc)
  {
    ResourcesDesc localResourcesDesc = paramLaunchDesc.getResources();
    if (localResourcesDesc != null)
    {
      JNLPUtils.sortResourcesForClasspath(localResourcesDesc, this._jarsInURLClassLoader, this._jarsNotInURLClassLoader);
      for (int i = 0; i < this._jarsInURLClassLoader.size(); i++)
      {
        JARDesc localJARDesc = (JARDesc)this._jarsInURLClassLoader.get(i);
        addURL(localJARDesc.getLocation());
      }
    }
  }

  private void addLoadedJarsEntry(JARDesc paramJARDesc)
  {
    if (!this._jarsInURLClassLoader.contains(paramJARDesc))
      this._jarsInURLClassLoader.add(paramJARDesc);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.PreverificationClassLoader
 * JD-Core Version:    0.6.0
 */