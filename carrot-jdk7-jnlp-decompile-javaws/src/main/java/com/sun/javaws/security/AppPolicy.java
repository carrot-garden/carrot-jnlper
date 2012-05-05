package com.sun.javaws.security;

import com.sun.applet2.preloader.Preloader;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.security.BadCertificateDialog;
import com.sun.deploy.security.CeilingPolicy;
import com.sun.deploy.security.TrustDecider;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.util.PerfLogger;
import com.sun.javaws.Globals;
import com.sun.javaws.LocalInstallHandler;
import com.sun.javaws.Main;
import com.sun.javaws.exceptions.ExitException;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ResourcesDesc;
import com.sun.javaws.jnl.ShortcutDesc;
import com.sun.jnlp.JNLPClassLoader;
import com.sun.jnlp.JNLPClassLoaderIf;
import com.sun.jnlp.JNLPClassLoaderUtil;
import java.awt.AWTPermission;
import java.io.File;
import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.AccessControlException;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.Enumeration;
import java.util.Properties;
import java.util.PropertyPermission;

public class AppPolicy
{
  private String _host = null;
  private File _extensionDir = null;
  private static AppPolicy _instance = null;

  public static AppPolicy getInstance()
  {
    return _instance;
  }

  public static AppPolicy createInstance(String paramString)
  {
    if (_instance == null)
      _instance = new AppPolicy(paramString);
    return _instance;
  }

  private AppPolicy(String paramString)
  {
    this._host = paramString;
    this._extensionDir = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "ext");
  }

  public boolean addPermissions(JNLPClassLoaderIf paramJNLPClassLoaderIf, PermissionCollection paramPermissionCollection, CodeSource paramCodeSource, boolean paramBoolean)
    throws ExitException
  {
    Trace.println("JAVAWS AppPolicy Permission requested for: " + paramCodeSource.getLocation(), TraceLevel.SECURITY);
    JARDesc localJARDesc = paramJNLPClassLoaderIf.getJarDescFromURL(paramCodeSource.getLocation());
    if (localJARDesc == null)
      return false;
    LaunchDesc localLaunchDesc = null;
    int i = 0;
    int j = 0;
    if (localJARDesc.getParent() != null)
    {
      localLaunchDesc = localJARDesc.getParent().getParent();
      i = localLaunchDesc.getSecurityModel();
    }
    if ((paramBoolean) && (i != 0))
    {
      grantUnrestrictedAccess(localLaunchDesc, paramCodeSource, paramJNLPClassLoaderIf.getPreloader());
      j = 1;
      if (i == 1)
        CeilingPolicy.addTrustedPermissions(paramPermissionCollection);
      else
        addJ2EEApplicationClientPermissionsObject(paramPermissionCollection);
    }
    if (!paramPermissionCollection.implies(new AllPermission()))
      addSandboxPermissionsObject(paramPermissionCollection, (localLaunchDesc != null) && (localLaunchDesc.getLaunchType() == 2));
    if ((localLaunchDesc != null) && (!localLaunchDesc.arePropsSet()))
    {
      Properties localProperties = localLaunchDesc.getResources().getResourceProperties();
      Enumeration localEnumeration = localProperties.keys();
      while (localEnumeration.hasMoreElements())
      {
        String str1 = (String)localEnumeration.nextElement();
        String str2 = localProperties.getProperty(str1);
        PropertyPermission localPropertyPermission = new PropertyPermission(str1, "write");
        PermissionCollection localPermissionCollection = Policy.getPolicy().getPermissions(paramCodeSource);
        if ((paramPermissionCollection.implies(localPropertyPermission)) || (localPermissionCollection.implies(localPropertyPermission)))
          System.setProperty(str1, str2);
        else
          Trace.ignoredException(new AccessControlException("access denied " + localPropertyPermission, localPropertyPermission));
      }
      localLaunchDesc.setPropsSet(true);
    }
    return j;
  }

  private void setUnrestrictedProps(LaunchDesc paramLaunchDesc)
  {
    if (!paramLaunchDesc.arePropsSet())
    {
      Properties localProperties = paramLaunchDesc.getResources().getResourceProperties();
      Enumeration localEnumeration = localProperties.keys();
      while (localEnumeration.hasMoreElements())
      {
        String str = (String)localEnumeration.nextElement();
        System.setProperty(str, localProperties.getProperty(str));
      }
      paramLaunchDesc.setPropsSet(true);
    }
  }

  public long grantUnrestrictedAccess(LaunchDesc paramLaunchDesc, CodeSource paramCodeSource, Preloader paramPreloader)
    throws ExitException
  {
    PerfLogger.setTime("Security: Start grantUnrestrictedAccess in AppPolicy class");
    int i = (paramLaunchDesc.isTrusted()) || ((Globals.isSecureMode()) && (paramLaunchDesc.isInstaller())) ? 1 : 0;
    AppInfo localAppInfo = null;
    boolean bool;
    if (i == 0)
    {
      localAppInfo = paramLaunchDesc.getAppInfo();
      bool = false;
      if ((paramLaunchDesc.getAppletDescriptor() != null) || (paramLaunchDesc.getApplicationDescriptor() != null))
      {
        LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
        Object localObject;
        if (localLocalInstallHandler.isLocalInstallSupported())
        {
          localObject = Cache.getLocalApplicationProperties(paramLaunchDesc.getCanonicalHome());
          if ((localObject != null) && (!((LocalApplicationProperties)localObject).getAskedForInstall()))
            bool = true;
        }
        if (bool)
        {
          localObject = paramLaunchDesc.getInformation().getShortcut();
          switch (Config.getShortcutValue())
          {
          case 0:
          default:
            break;
          case 3:
            if (localObject == null)
              break;
          case 1:
          case 2:
          case 4:
            if (localObject != null)
            {
              localAppInfo.setDesktopHint(((ShortcutDesc)localObject).getDesktop());
              localAppInfo.setMenuHint(((ShortcutDesc)localObject).getMenu());
              localAppInfo.setSubmenu(((ShortcutDesc)localObject).getSubmenu());
            }
            else
            {
              localAppInfo.setDesktopHint(true);
              localAppInfo.setMenuHint(true);
            }
          }
          if ((localLocalInstallHandler.isAssociationSupported()) && (Config.getAssociationValue() != 0))
            localAppInfo.setAssociations(paramLaunchDesc.getInformation().getAssociations());
        }
      }
    }
    try
    {
      bool = (!paramLaunchDesc.isSigned()) && (!paramLaunchDesc.isSecureJVMArgs());
      if (i != 0)
      {
        setUnrestrictedProps(paramLaunchDesc);
        return 0L;
      }
      long l = TrustDecider.isAllPermissionGranted(paramCodeSource, localAppInfo, bool, paramPreloader);
      if (l != 0L)
      {
        PerfLogger.setTime("Security: End grantUnrestrictedAccess in AppPolicy class");
        setUnrestrictedProps(paramLaunchDesc);
        if (l != 1L)
          return l;
        return 0L;
      }
      Trace.println("We were not granted permission, exiting", TraceLevel.SECURITY);
    }
    catch (Exception localException)
    {
      BadCertificateDialog.showDialog(paramCodeSource, localAppInfo, localException);
    }
    Main.systemExit(-1);
    return 0L;
  }

  private void addJ2EEApplicationClientPermissionsObject(PermissionCollection paramPermissionCollection)
  {
    Trace.println("Creating J2EE-application-client-permisisons object", TraceLevel.SECURITY);
    paramPermissionCollection.add(new AWTPermission("accessClipboard"));
    paramPermissionCollection.add(new AWTPermission("accessEventQueue"));
    paramPermissionCollection.add(new AWTPermission("showWindowWithoutWarningBanner"));
    paramPermissionCollection.add(new RuntimePermission("exitVM"));
    paramPermissionCollection.add(new RuntimePermission("loadLibrary"));
    paramPermissionCollection.add(new RuntimePermission("queuePrintJob"));
    paramPermissionCollection.add(new SocketPermission("*", "connect"));
    paramPermissionCollection.add(new SocketPermission("localhost:1024-", "accept,listen"));
    paramPermissionCollection.add(new FilePermission("*", "read,write"));
    paramPermissionCollection.add(new PropertyPermission("*", "read"));
  }

  private void addSandboxPermissionsObject(PermissionCollection paramPermissionCollection, boolean paramBoolean)
  {
    Trace.println("Add sandbox permissions", TraceLevel.SECURITY);
    paramPermissionCollection.add(new PropertyPermission("java.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vendor", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vendor.url", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.class.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("os.name", "read"));
    paramPermissionCollection.add(new PropertyPermission("os.arch", "read"));
    paramPermissionCollection.add(new PropertyPermission("os.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("file.separator", "read"));
    paramPermissionCollection.add(new PropertyPermission("path.separator", "read"));
    paramPermissionCollection.add(new PropertyPermission("line.separator", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.specification.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.specification.vendor", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.specification.name", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.specification.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.specification.vendor", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.specification.name", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.version", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.vendor", "read"));
    paramPermissionCollection.add(new PropertyPermission("java.vm.name", "read"));
    paramPermissionCollection.add(new PropertyPermission("javawebstart.version", "read"));
    if ((JNLPClassLoaderUtil.getInstance() instanceof JNLPClassLoader))
    {
      paramPermissionCollection.add(new RuntimePermission("exitVM"));
      paramPermissionCollection.add(new RuntimePermission("stopThread"));
    }
    String str = "Java " + (paramBoolean ? "Applet" : "Application") + " Window";
    if (Config.getBooleanProperty("deployment.security.sandbox.awtwarningwindow"))
      System.setProperty("awt.appletWarning", str);
    else
      paramPermissionCollection.add(new AWTPermission("showWindowWithoutWarningBanner"));
    paramPermissionCollection.add(new SocketPermission("localhost:1024-", "listen"));
    paramPermissionCollection.add(new PropertyPermission("jnlp.*", "read,write"));
    paramPermissionCollection.add(new PropertyPermission("javaws.*", "read,write"));
    paramPermissionCollection.add(new PropertyPermission("javapi.*", "read,write"));
    String[] arrayOfString1 = Config.getSecureProperties();
    for (int i = 0; i < arrayOfString1.length; i++)
      paramPermissionCollection.add(new PropertyPermission(arrayOfString1[i], "read,write"));
    String[] arrayOfString2 = Globals.getApplicationArgs();
    if ((arrayOfString2 != null) && (arrayOfString2.length == 2))
      if (arrayOfString2[0].equals("-open"))
      {
        paramPermissionCollection.add(new FilePermission(arrayOfString2[1], "read, write"));
      }
      else if (arrayOfString2[0].equals("-print"))
      {
        paramPermissionCollection.add(new FilePermission(arrayOfString2[1], "read, write"));
        paramPermissionCollection.add(new RuntimePermission("queuePrintJob"));
      }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.security.AppPolicy
 * JD-Core Version:    0.6.0
 */