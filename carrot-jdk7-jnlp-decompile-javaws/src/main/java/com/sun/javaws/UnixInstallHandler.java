package com.sun.javaws;

import com.sun.deploy.Environment;
import com.sun.deploy.association.Action;
import com.sun.deploy.association.Association;
import com.sun.deploy.association.AssociationAlreadyRegisteredException;
import com.sun.deploy.association.AssociationNotRegisteredException;
import com.sun.deploy.association.AssociationService;
import com.sun.deploy.association.RegisterFailedException;
import com.sun.deploy.association.utility.DesktopEntry;
import com.sun.deploy.association.utility.DesktopEntryFile;
import com.sun.deploy.association.utility.GnomeVfsWrapper;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.Platform;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.RContentDesc;
import com.sun.javaws.jnl.ShortcutDesc;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

public class UnixInstallHandler extends LocalInstallHandler
{
  private static final String INSTALLED_DESKTOP_SHORTCUT_KEY = "installed.desktop";
  private static final String INSTALLED_DESKTOP_SHORTCUT_GNOME26_KEY = "installed.desktop.gnome26";
  private static final String INSTALLED_DIRECTORY_KEY = "installed.directory";
  private static final String INSTALLED_GNOME_START_MENU_KEY = "installed.menu";
  private static final String INSTALLED_UNINSTALL_KEY = "installed.uninstalled";
  private static final String INSTALLED_RC_KEY = "installed.rc";
  static boolean GNOMELoaded = GnomeVfsWrapper.openGNOMELibrary();
  static boolean GNOMEInitializeded = GnomeVfsWrapper.initGNOMELibrary();
  private final String nameBadChars = "\"\\/|:?*<>#";
  private final String dirBadChars = "\"|:?*<>#";

  public boolean isShortcutExists(LocalApplicationProperties paramLocalApplicationProperties)
  {
    String str1 = paramLocalApplicationProperties.get("installed.desktop");
    String str2 = paramLocalApplicationProperties.get("installed.menu");
    boolean bool1 = false;
    boolean bool2 = false;
    if (str1 == null)
      str1 = paramLocalApplicationProperties.get("installed.desktop.gnome26");
    if (str1 != null)
      bool1 = new DesktopEntryFile(str1).exists();
    if (str2 != null)
      bool2 = new DesktopEntryFile(str2).exists();
    if ((str1 != null) && (str2 != null))
      return (bool1) && (bool2);
    return (bool1) || (bool2);
  }

  public boolean[] whichShortcutsExist(LocalApplicationProperties paramLocalApplicationProperties)
  {
    String str1 = paramLocalApplicationProperties.get("installed.desktop");
    String str2 = paramLocalApplicationProperties.get("installed.menu");
    if (str1 == null)
      str1 = paramLocalApplicationProperties.get("installed.desktop.gnome26");
    boolean[] arrayOfBoolean = new boolean[2];
    arrayOfBoolean[0] = ((str1 != null) && (new DesktopEntryFile(str1).exists()) ? 1 : false);
    arrayOfBoolean[1] = ((str2 != null) && (new DesktopEntryFile(str2).exists()) ? 1 : false);
    return arrayOfBoolean;
  }

  public String getAssociationPrintCommand(String paramString)
  {
    return null;
  }

  public String getAssociationOpenCommand(String paramString)
  {
    return Platform.get().getSystemJavawsPath() + " " + paramString + " -open";
  }

  public void registerAssociationInternal(Association paramAssociation)
    throws AssociationAlreadyRegisteredException, RegisterFailedException
  {
    AssociationService localAssociationService = new AssociationService();
    if (Environment.isSystemCacheMode())
      localAssociationService.registerSystemAssociation(paramAssociation);
    else
      localAssociationService.registerUserAssociation(paramAssociation);
  }

  public void unregisterAssociationInternal(Association paramAssociation)
    throws AssociationNotRegisteredException, RegisterFailedException
  {
    AssociationService localAssociationService = new AssociationService();
    if (Environment.isSystemCacheMode())
      localAssociationService.unregisterSystemAssociation(paramAssociation);
    else
      localAssociationService.unregisterUserAssociation(paramAssociation);
  }

  public boolean hasAssociation(Association paramAssociation)
  {
    AssociationService localAssociationService = new AssociationService();
    return localAssociationService.hasAssociation(paramAssociation);
  }

  protected boolean createShortcuts(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean[] paramArrayOfBoolean)
  {
    Trace.println("createShortcuts called in UnixInstallHandler", TraceLevel.BASIC);
    File localFile = null;
    ShortcutDesc localShortcutDesc = paramLaunchDesc.getInformation().getShortcut();
    boolean bool1 = localShortcutDesc == null ? true : localShortcutDesc.getDesktop();
    boolean bool2 = localShortcutDesc == null ? true : localShortcutDesc.getMenu();
    boolean bool3 = false;
    int i = 1;
    if (Environment.isSystemCacheMode())
    {
      bool1 = false;
      bool3 = true;
    }
    if (paramArrayOfBoolean != null)
    {
      bool1 = (bool1) && (paramArrayOfBoolean[0] != 0);
      bool2 = (bool2) && (paramArrayOfBoolean[1] != 0);
    }
    if (!new DesktopEntryFile("applications-all-users:///").exists())
    {
      Trace.println("Found old gnome vfs api, don't create start menu", TraceLevel.TEMP);
      bool2 = false;
    }
    if ((isShortcutExists(paramLocalApplicationProperties)) && (!shouldInstallOverExisting(paramLaunchDesc)))
      return false;
    if ((bool2) || (bool1))
    {
      try
      {
        localFile = DownloadEngine.getCachedFile(paramLaunchDesc.getCanonicalHome());
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
      if (localFile == null)
        i = 0;
      Object localObject;
      if ((bool1) && (i != 0))
      {
        localObject = getGnomeDesktopPath();
        String str1 = null;
        String str2 = null;
        if (new File((String)localObject).exists())
        {
          str1 = createDesktopShortcut(paramLaunchDesc, localFile, "file://" + (String)localObject);
          if (str1 != null)
            paramLocalApplicationProperties.put("installed.desktop", str1);
        }
        String str3 = getGnome26DesktopPath();
        if (new File(str3).exists())
        {
          str2 = createDesktopShortcut(paramLaunchDesc, localFile, "file://" + str3);
          if (str2 != null)
            paramLocalApplicationProperties.put("installed.desktop.gnome26", str2);
        }
        if ((str1 == null) && (str2 == null))
          i = 0;
      }
      if ((bool2) && (i != 0))
      {
        localObject = createStartMenuShortcut(paramLaunchDesc, localFile, bool3);
        if (localObject[0] != null)
        {
          paramLocalApplicationProperties.put("installed.menu", localObject[0]);
          if (localObject[1] != null)
            paramLocalApplicationProperties.put("installed.directory", localObject[1]);
          if (localObject[2] != null)
            paramLocalApplicationProperties.put("installed.uninstalled", localObject[2]);
          if (localObject[3] != null)
            paramLocalApplicationProperties.put("installed.rc", localObject[3]);
        }
        else
        {
          i = 0;
          removeShortcuts(paramLaunchDesc, paramLocalApplicationProperties, bool1);
        }
      }
      if (i != 0)
      {
        paramLocalApplicationProperties.setShortcutInstalled(true);
        save(paramLocalApplicationProperties);
      }
      else
      {
        installFailed(paramLaunchDesc);
      }
    }
    return i;
  }

  protected void registerWithInstallPanel(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
  }

  protected void removeFromInstallPanel(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean)
  {
  }

  public String getDefaultIconPath()
  {
    return Environment.getJavaHome() + File.separator + "lib" + File.separator + "deploy" + File.separator + "java-icon.ico";
  }

  private String getIcon(LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    String str = IcoEncoder.getIconPath(paramLaunchDesc, paramBoolean);
    if (str == null)
      str = getDefaultIconPath();
    return str;
  }

  private String getRCIcon(RContentDesc paramRContentDesc, LaunchDesc paramLaunchDesc)
  {
    URL localURL = paramRContentDesc.getIcon();
    String str = null;
    if (localURL != null)
      str = IcoEncoder.getIconPath(localURL, null);
    if (str == null)
      str = getIcon(paramLaunchDesc, false);
    return str;
  }

  private String[] createStartMenuShortcut(LaunchDesc paramLaunchDesc, File paramFile, boolean paramBoolean)
  {
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    ShortcutDesc localShortcutDesc = localInformationDesc.getShortcut();
    String[] arrayOfString = new String[5];
    String str1 = nameFilter(localInformationDesc.getTitle());
    String str2 = getIcon(paramLaunchDesc, false);
    String str3 = "";
    String str4 = getMenuEntryDirPath(paramLaunchDesc, paramBoolean);
    if ((localInformationDesc.supportsOfflineOperation() == true) && (localShortcutDesc != null) && (!localShortcutDesc.getOnline()))
      str3 = "-offline";
    arrayOfString[0] = createDesktopFile(paramLaunchDesc, str1, str2, str4, paramFile.getAbsolutePath(), str3);
    arrayOfString[1] = str4;
    if (addUninstallShortcut())
      arrayOfString[2] = createDesktopFile(paramLaunchDesc, ResourceManager.getString("install.startMenuUninstallShortcutName", str1), str2, str4, paramFile.getAbsolutePath(), "-uninstall");
    Trace.println("directoryFileName: " + arrayOfString[1], TraceLevel.BASIC);
    Trace.println("desktopFileName: " + arrayOfString[0], TraceLevel.BASIC);
    RContentDesc[] arrayOfRContentDesc = localInformationDesc.getRelatedContent();
    if (arrayOfRContentDesc != null)
    {
      StringBuffer localStringBuffer = new StringBuffer(512 * arrayOfRContentDesc.length);
      for (int i = 0; i < arrayOfRContentDesc.length; i++)
      {
        URL localURL = arrayOfRContentDesc[i].getHref();
        if ((localURL != null) && (localURL.toString().endsWith(".jnlp")))
          continue;
        String str5 = createRCDesktopFile(arrayOfRContentDesc[i], getRCIcon(arrayOfRContentDesc[i], paramLaunchDesc), str4);
        if (str5 == null)
          continue;
        localStringBuffer.append(str5);
        localStringBuffer.append(";");
      }
      arrayOfString[3] = localStringBuffer.toString();
    }
    return arrayOfString;
  }

  private String getFolderName(LaunchDesc paramLaunchDesc)
  {
    String str = null;
    if (paramLaunchDesc.getInformation().getShortcut() != null)
      str = paramLaunchDesc.getInformation().getShortcut().getSubmenu();
    if (str == null)
      str = nameFilter(paramLaunchDesc.getInformation().getTitle());
    str = str.replace('<', '-');
    str = str.replace('>', '-');
    return str;
  }

  private String createDesktopShortcut(LaunchDesc paramLaunchDesc, File paramFile, String paramString)
  {
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    ShortcutDesc localShortcutDesc = localInformationDesc.getShortcut();
    String str1 = nameFilter(localInformationDesc.getTitle());
    String str2 = getIcon(paramLaunchDesc, true);
    String str3 = "";
    Trace.println("iconPath: " + str2, TraceLevel.TEMP);
    String str4 = paramLaunchDesc.getLocation() != null ? paramLaunchDesc.getLocation().toString() : null;
    String str5 = "-J-Djnlp.application.href=" + str4 + " ";
    if ((localInformationDesc.supportsOfflineOperation() == true) && (localShortcutDesc != null) && (!localShortcutDesc.getOnline()))
      str3 = "-offline ";
    String str6 = "-localfile " + str3 + str5;
    return createDesktopFile(paramLaunchDesc, str1, str2, paramString, paramFile.getAbsolutePath(), str6);
  }

  private String getGnomeDesktopPath()
  {
    return System.getProperty("user.home") + File.separator + ".gnome-desktop";
  }

  private String getGnome26DesktopPath()
  {
    return System.getProperty("user.home") + File.separator + "Desktop";
  }

  private String getMenuEntryDirPath(LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    ShortcutDesc localShortcutDesc = localInformationDesc.getShortcut();
    String str1 = null;
    if (localShortcutDesc != null)
      str1 = dirFilter(localShortcutDesc.getSubmenu());
    if (str1 == null)
      str1 = dirFilter(localInformationDesc.getTitle());
    if (paramBoolean)
    {
      if (str1.startsWith("applications://"))
      {
        String str2 = "applications://";
        str1 = "applications-all-users://" + str1.substring(str1.indexOf(str2), str2.length());
      }
      else
      {
        str1 = "applications-all-users://" + File.separator + str1;
      }
    }
    else if (!str1.startsWith("applications://"))
      str1 = "applications://" + File.separator + str1;
    return str1;
  }

  private String getRCCommand(URL paramURL)
  {
    File localFile = null;
    try
    {
      localFile = DownloadEngine.getCachedFileNative(paramURL);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    Object localObject = "";
    if (paramURL.toString().endsWith(".jnlp"))
      return Environment.getJavawsCommand() + " " + paramURL.toString();
    String str1;
    if (localFile != null)
    {
      str1 = localFile.getAbsolutePath();
      String str2 = str1.substring(str1.lastIndexOf("."), str1.length());
      if ((isAssociationSupported()) && (!str2.equals(".html")))
      {
        AssociationService localAssociationService = new AssociationService();
        Association localAssociation = localAssociationService.getFileExtensionAssociation(str2);
        if (localAssociation != null)
        {
          Action localAction = localAssociation.getActionByVerb("open");
          if (localAction != null)
          {
            String str3 = localAction.getCommand();
            StringTokenizer localStringTokenizer = new StringTokenizer(str3);
            if (localStringTokenizer.hasMoreTokens())
              str3 = localStringTokenizer.nextToken();
            localObject = str3;
          }
        }
      }
      if (localObject == "")
        localObject = Config.getStringProperty("deployment.browser.path");
    }
    else
    {
      str1 = paramURL.toString();
      localObject = Config.getStringProperty("deployment.browser.path");
    }
    return (String)((String)localObject + " " + str1);
  }

  private String createRCDesktopFile(RContentDesc paramRContentDesc, String paramString1, String paramString2)
  {
    URL localURL = paramRContentDesc.getHref();
    String str1 = nameFilter(paramRContentDesc.getTitle());
    DesktopEntry localDesktopEntry = new DesktopEntry();
    localDesktopEntry.setType("Application");
    localDesktopEntry.setExec(getRCCommand(localURL));
    localDesktopEntry.setIcon(paramString1);
    localDesktopEntry.setTerminal(false);
    localDesktopEntry.setName(str1);
    localDesktopEntry.setComment(paramRContentDesc.getDescription());
    localDesktopEntry.setCategories("Applications;" + str1);
    String str2 = paramString2 + File.separator + uniqDesktopFileName(str1);
    try
    {
      new DesktopEntryFile(str2).writeEntry(localDesktopEntry);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
      return null;
    }
    return str2;
  }

  private String createDesktopFile(LaunchDesc paramLaunchDesc, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5)
  {
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    String str1 = getFolderName(paramLaunchDesc);
    String str2 = localInformationDesc.getDescription(0);
    String str3 = localInformationDesc.getDescription(3);
    String str4 = str3 == null ? str2 : str3;
    if (paramString5 == null)
      paramString5 = "";
    else if ((paramString5.length() > 0) && (!paramString5.endsWith(" ")))
      paramString5 = paramString5 + " ";
    StringBuffer localStringBuffer = new StringBuffer(512);
    localStringBuffer.append(paramString3).append(File.separator).append(uniqDesktopFileName(paramString1));
    Trace.println("desktopFilePath: " + localStringBuffer, TraceLevel.BASIC);
    DesktopEntry localDesktopEntry = new DesktopEntry();
    localDesktopEntry.setEncoding("UTF-8");
    localDesktopEntry.setType("Application");
    localDesktopEntry.setExec(Environment.getJavawsCommand() + " " + paramString5 + paramString4);
    localDesktopEntry.setIcon(paramString2);
    localDesktopEntry.setTerminal(false);
    localDesktopEntry.setName(paramString1);
    localDesktopEntry.setComment(str4);
    localDesktopEntry.setCategories("Applications;" + str1);
    try
    {
      Trace.println("fileContents: " + localDesktopEntry, TraceLevel.TEMP);
      new DesktopEntryFile(localStringBuffer.toString()).writeEntry(localDesktopEntry);
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
      return null;
    }
    return localStringBuffer.toString();
  }

  private String uniqDesktopFileName(String paramString)
  {
    return "jws_app_shortcut_" + System.currentTimeMillis() + ".desktop";
  }

  private void installFailed(LaunchDesc paramLaunchDesc)
  {
    1 local1 = new Runnable(paramLaunchDesc)
    {
      private final LaunchDesc val$desc;

      public void run()
      {
        ToolkitStore.getUI();
        ToolkitStore.getUI().showMessageDialog(null, null, 0, ResourceManager.getString("install.installFailedTitle"), ResourceManager.getString("install.installFailed"), UnixInstallHandler.this.nameFilter(this.val$desc.getInformation().getTitle()), null, null, null, null);
      }
    };
    invokeRunnable(local1);
  }

  public boolean removePathShortcut(String paramString)
  {
    DesktopEntryFile localDesktopEntryFile = new DesktopEntryFile(paramString);
    if (localDesktopEntryFile.exists())
      return localDesktopEntryFile.delete();
    return false;
  }

  protected boolean removeShortcuts(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean)
  {
    int i = 0;
    if (Environment.isSystemCacheMode())
    {
      paramBoolean = false;
      i = 1;
    }
    Trace.println("uninstall called in UnixInstallHandler", TraceLevel.BASIC);
    if (paramBoolean)
    {
      str1 = paramLocalApplicationProperties.get("installed.desktop");
      localObject = paramLocalApplicationProperties.get("installed.desktop.gnome26");
      if (str1 != null)
      {
        new DesktopEntryFile(str1).delete();
        Trace.println("file removed: " + str1, TraceLevel.BASIC);
        paramLocalApplicationProperties.put("installed.desktop", null);
      }
      if (localObject != null)
      {
        new DesktopEntryFile((String)localObject).delete();
        Trace.println("file removed: " + (String)localObject, TraceLevel.BASIC);
        paramLocalApplicationProperties.put("installed.desktop.gnome26", null);
      }
    }
    String str1 = paramLocalApplicationProperties.get("installed.menu");
    if (str1 != null)
    {
      new DesktopEntryFile(str1).delete();
      Trace.println("file removed: " + str1, TraceLevel.BASIC);
      paramLocalApplicationProperties.put("installed.menu", null);
    }
    str1 = paramLocalApplicationProperties.get("installed.uninstalled");
    if (str1 != null)
    {
      new DesktopEntryFile(str1).delete();
      Trace.println("file removed: " + str1, TraceLevel.BASIC);
      paramLocalApplicationProperties.put("installed.uninstalled", null);
    }
    str1 = paramLocalApplicationProperties.get("installed.rc");
    if (str1 != null)
    {
      localObject = new StringTokenizer(str1, ";");
      while (((StringTokenizer)localObject).hasMoreElements())
      {
        String str2 = ((StringTokenizer)localObject).nextToken();
        if ((str2 == null) || (str2.trim().length() == 0))
          continue;
        new DesktopEntryFile(str2).delete();
        Trace.println("file removed: " + str2, TraceLevel.BASIC);
      }
      paramLocalApplicationProperties.put("installed.rc", null);
    }
    Object localObject = paramLocalApplicationProperties.get("installed.directory");
    if (localObject != null)
    {
      new DesktopEntryFile((String)localObject).deleteToNonEmptyParent();
      Trace.println("directory removed: " + (String)localObject, TraceLevel.BASIC);
      paramLocalApplicationProperties.put("installed.directory", null);
    }
    paramLocalApplicationProperties.setShortcutInstalled(false);
    save(paramLocalApplicationProperties);
    return true;
  }

  public boolean isLocalInstallSupported()
  {
    return (GNOMELoaded) && (GNOMEInitializeded) && (Platform.get().isLocalInstallSupported());
  }

  boolean isAssociationFileExtSupported(String paramString)
  {
    return true;
  }

  public boolean isAssociationSupported()
  {
    String str = GnomeVfsWrapper.getVersion();
    int i = (str != null) && (compareVersion(str, "2.8") < 0) ? 1 : 0;
    int j = (GNOMELoaded) && (GNOMEInitializeded) && (i != 0) ? 1 : 0;
    return j;
  }

  private int compareVersion(String paramString1, String paramString2)
  {
    StringTokenizer localStringTokenizer1 = new StringTokenizer(paramString1, ".");
    StringTokenizer localStringTokenizer2 = new StringTokenizer(paramString2, ".");
    int i = 0;
    while ((localStringTokenizer1.hasMoreTokens()) && (i == 0))
    {
      if (localStringTokenizer2.hasMoreTokens())
      {
        i = Integer.parseInt(localStringTokenizer1.nextToken()) - Integer.parseInt(localStringTokenizer2.nextToken());
        continue;
      }
      i = Integer.parseInt(localStringTokenizer1.nextToken());
    }
    while ((i == 0) && (localStringTokenizer2.hasMoreTokens()))
      i = 0 - Integer.parseInt(localStringTokenizer2.nextToken());
    return i;
  }

  private String nameFilter(String paramString)
  {
    return Filter(paramString, "\"\\/|:?*<>#", '-');
  }

  private String dirFilter(String paramString)
  {
    String str = Filter(paramString, "\"|:?*<>#", '-');
    return Filter(str, "/\\", File.separatorChar);
  }

  private String Filter(String paramString1, String paramString2, char paramChar)
  {
    if (paramString1 == null)
      return null;
    return checkTitleString(paramString1, paramString2, paramChar);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.UnixInstallHandler
 * JD-Core Version:    0.6.0
 */