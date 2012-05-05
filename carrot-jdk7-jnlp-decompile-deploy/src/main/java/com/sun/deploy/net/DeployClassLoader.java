package com.sun.deploy.net;

import com.sun.deploy.config.JfxRuntime;
import com.sun.deploy.config.Platform;
import com.sun.deploy.util.VersionID;
import com.sun.deploy.util.VersionString;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.util.List;

public final class DeployClassLoader extends URLClassLoader
{
  private boolean fx_injected = false;
  private final PermissionCollection all_perms;
  private VersionID fx_ver = null;
  public static final ThreadLocal loadingClass = new ThreadLocal()
  {
    protected Object initialValue()
    {
      return Boolean.FALSE;
    }
  };

  public DeployClassLoader()
  {
    super(new URL[0]);
    AllPermission localAllPermission = new AllPermission();
    this.all_perms = localAllPermission.newPermissionCollection();
    this.all_perms.add(localAllPermission);
  }

  private void checkInjectJfxPermission(String paramString)
    throws IllegalStateException, ClassNotFoundException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (null != localSecurityManager)
      localSecurityManager.checkPackageDefinition("com.javafx");
    VersionID localVersionID = new VersionID(paramString);
    if ((this.fx_injected) && (this.fx_ver.match(localVersionID)))
      throw new IllegalStateException();
  }

  public synchronized void injectJfx(String paramString)
    throws ClassNotFoundException, IllegalStateException
  {
    checkInjectJfxPermission(paramString);
    VersionString localVersionString = new VersionString(paramString);
    List localList = localVersionString.getAllVersionIDs();
    for (int i = 0; i < localList.size(); i++)
    {
      VersionID localVersionID = (VersionID)localList.get(i);
      JfxRuntime localJfxRuntime = Platform.get().getBestJfxRuntime(localVersionID);
      if (localJfxRuntime == null)
        continue;
      injectJfx(localJfxRuntime);
      return;
    }
    throw new ClassNotFoundException();
  }

  public synchronized void injectJfx(JfxRuntime paramJfxRuntime)
    throws ClassNotFoundException, IllegalStateException
  {
    checkInjectJfxPermission(null);
    if (paramJfxRuntime == null)
      throw new ClassNotFoundException();
    URL[] arrayOfURL = paramJfxRuntime.getURLs();
    if (null == arrayOfURL)
      throw new ClassNotFoundException();
    for (int i = 0; i < arrayOfURL.length; i++)
      addURL(arrayOfURL[i]);
    this.fx_injected = true;
    this.fx_ver = paramJfxRuntime.getProductVersion();
  }

  protected PermissionCollection getPermissions(CodeSource paramCodeSource)
  {
    URL localURL = paramCodeSource.getLocation();
    URL[] arrayOfURL = getURLs();
    for (int i = 0; i < arrayOfURL.length; i++)
      if (arrayOfURL[i].equals(localURL))
        return this.all_perms;
    return super.getPermissions(paramCodeSource);
  }

  protected Class loadClass(String paramString, boolean paramBoolean)
    throws ClassNotFoundException
  {
    if (!this.fx_injected)
      return super.loadClass(paramString, paramBoolean);
    loadingClass.set(Boolean.TRUE);
    Class localClass = null;
    Object localObject1 = null;
    try
    {
      localClass = super.loadClass(paramString, paramBoolean);
      localObject1 = loadingClass.get();
    }
    finally
    {
      loadingClass.set(Boolean.FALSE);
    }
    if (localClass != null)
      return localClass;
    if ((localObject1 instanceof RuntimeException))
      throw ((RuntimeException)localObject1);
    return null;
  }

  public static void setClassLoadingException(Throwable paramThrowable)
  {
    loadingClass.set(paramThrowable);
  }

  public static boolean isLoadingClass()
  {
    Object localObject = loadingClass.get();
    if ((localObject instanceof Boolean))
      return ((Boolean)localObject).booleanValue();
    return false;
  }

  private URL getResourceImpl(String paramString)
  {
    return super.getResource(paramString);
  }

  public URL getResource(String paramString)
  {
    String str = paramString;
    URL localURL = (URL)AccessController.doPrivileged(new PrivilegedAction(str)
    {
      private final String val$resName;

      public Object run()
      {
        return DeployClassLoader.this.getResourceImpl(this.val$resName);
      }
    });
    if (null == localURL);
    return localURL;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.DeployClassLoader
 * JD-Core Version:    0.6.0
 */