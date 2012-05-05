package com.sun.javaws;

import com.sun.deploy.Environment;
import com.sun.deploy.association.Action;
import com.sun.deploy.association.Association;
import com.sun.deploy.association.AssociationAlreadyRegisteredException;
import com.sun.deploy.association.AssociationNotRegisteredException;
import com.sun.deploy.association.AssociationService;
import com.sun.deploy.association.RegisterFailedException;
import com.sun.deploy.cache.AssociationDesc;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.ComponentRef;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.RContentDesc;
import com.sun.javaws.jnl.ShortcutDesc;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;

public abstract class LocalInstallHandler
{
  private static LocalInstallHandler _installHandler;
  public static final int DESKTOP_INDEX = 0;
  public static final int MENU_INDEX = 1;

  public static synchronized LocalInstallHandler getInstance()
  {
    if (_installHandler == null)
      _installHandler = LocalInstallHandlerFactory.newInstance();
    return _installHandler;
  }

  public void install(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean1, boolean paramBoolean2, ComponentRef paramComponentRef)
  {
    if (paramLaunchDesc.isApplicationDescriptor())
    {
      boolean bool1 = false;
      boolean bool2 = false;
      int i = 0;
      if (isLocalInstallSupported())
      {
        AssociationDesc[] arrayOfAssociationDesc = paramLocalApplicationProperties.getAssociations();
        Object localObject;
        if ((arrayOfAssociationDesc == null) || (arrayOfAssociationDesc.length <= 0))
        {
          localObject = paramLaunchDesc.getInformation().getAssociations();
          bool1 = (isAssociationSupported()) && (localObject != null) && (localObject.length > 0);
        }
        else if (paramBoolean1)
        {
          removeAssociations(paramLaunchDesc, paramLocalApplicationProperties);
          createAssociations(paramLaunchDesc, paramLocalApplicationProperties);
        }
        if (paramLocalApplicationProperties.isShortcutInstalled())
        {
          if ((!paramLocalApplicationProperties.isShortcutInstalledSystem()) && (paramBoolean1))
          {
            localObject = whichShortcutsExist(paramLocalApplicationProperties);
            removeShortcuts(paramLaunchDesc, paramLocalApplicationProperties, true);
            if ((localObject[0] == 0) && (localObject[1] == 0))
              bool2 = true;
            else
              createShortcuts(paramLaunchDesc, paramLocalApplicationProperties, localObject);
          }
        }
        else
          bool2 = true;
      }
      else if (!paramLocalApplicationProperties.getAskedForInstall())
      {
        try
        {
          performIntegration(paramComponentRef, paramLaunchDesc, paramLocalApplicationProperties, paramBoolean2, false, false);
        }
        catch (Throwable localThrowable1)
        {
          Trace.ignored(localThrowable1);
        }
        paramLocalApplicationProperties.setAskedForInstall(true);
      }
      if (((bool2) || (bool1)) && (!paramLocalApplicationProperties.getAskedForInstall()))
      {
        try
        {
          performIntegration(paramComponentRef, paramLaunchDesc, paramLocalApplicationProperties, paramBoolean2, bool2, bool1);
        }
        catch (Throwable localThrowable2)
        {
          Trace.ignored(localThrowable2);
        }
        paramLocalApplicationProperties.setAskedForInstall(true);
      }
      if ((paramBoolean1) || (paramLocalApplicationProperties.getLaunchCount() <= 1))
        updateInstallPanel(paramLaunchDesc, paramLocalApplicationProperties);
    }
  }

  public void updateInstallPanel(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    removeFromInstallPanel(paramLaunchDesc, paramLocalApplicationProperties, false);
    if (paramLocalApplicationProperties.isJnlpInstalled())
      registerWithInstallPanel(paramLaunchDesc, paramLocalApplicationProperties);
  }

  public void uninstall(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    uninstall(paramLaunchDesc, paramLocalApplicationProperties, false);
  }

  public void uninstall(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean)
  {
    removeShortcuts(paramLaunchDesc, paramLocalApplicationProperties, true);
    removeAssociations(paramLaunchDesc, paramLocalApplicationProperties);
    if ((paramLocalApplicationProperties.isJnlpInstalled()) || (paramBoolean))
    {
      removeFromInstallPanel(paramLaunchDesc, paramLocalApplicationProperties, paramBoolean);
      paramLocalApplicationProperties.setJnlpInstalled(false);
    }
  }

  public abstract boolean isLocalInstallSupported();

  public abstract boolean isAssociationSupported();

  abstract boolean isAssociationFileExtSupported(String paramString);

  public abstract String getAssociationOpenCommand(String paramString);

  public abstract String getAssociationPrintCommand(String paramString);

  public abstract void registerAssociationInternal(Association paramAssociation)
    throws AssociationAlreadyRegisteredException, RegisterFailedException;

  public abstract void unregisterAssociationInternal(Association paramAssociation)
    throws AssociationNotRegisteredException, RegisterFailedException;

  public abstract boolean hasAssociation(Association paramAssociation);

  public abstract String getDefaultIconPath();

  public abstract boolean isShortcutExists(LocalApplicationProperties paramLocalApplicationProperties);

  public abstract boolean[] whichShortcutsExist(LocalApplicationProperties paramLocalApplicationProperties);

  protected abstract boolean createShortcuts(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean[] paramArrayOfBoolean);

  protected abstract boolean removeShortcuts(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean);

  protected abstract boolean removePathShortcut(String paramString);

  protected abstract void registerWithInstallPanel(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties);

  protected abstract void removeFromInstallPanel(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean);

  private String getJnlpLocation(LaunchDesc paramLaunchDesc)
  {
    File localFile = null;
    try
    {
      localFile = DownloadEngine.getCachedFile(paramLaunchDesc.getCanonicalHome());
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    String str;
    if (localFile != null)
      str = localFile.getAbsolutePath();
    else
      str = paramLaunchDesc.getLocation().toString();
    return str;
  }

  private String getOpenActionCommand(Association paramAssociation)
  {
    Action localAction = paramAssociation.getActionByVerb("open");
    String str = null;
    if (localAction != null)
      str = localAction.getCommand();
    return str;
  }

  private boolean registerAssociation(LaunchDesc paramLaunchDesc, String paramString1, String paramString2, String paramString3, URL paramURL)
  {
    AssociationService localAssociationService = new AssociationService();
    Association localAssociation1 = new Association();
    int i = 0;
    Association localAssociation2 = null;
    String str1 = "";
    String str2 = null;
    Object localObject1;
    if (paramString1 != null)
    {
      localObject1 = new StringTokenizer(paramString1);
      while (((StringTokenizer)localObject1).hasMoreTokens())
      {
        localObject2 = "." + ((StringTokenizer)localObject1).nextToken();
        Trace.println("associate with ext: " + (String)localObject2, TraceLevel.BASIC);
        if (!isAssociationFileExtSupported((String)localObject2))
        {
          Trace.println("association with ext: " + (String)localObject2 + " is not supported", TraceLevel.BASIC);
          return false;
        }
        if (str1 == "")
          str1 = (String)localObject2 + " file";
        localAssociation2 = localAssociationService.getFileExtensionAssociation((String)localObject2);
        if (localAssociation2 != null)
        {
          Trace.println("associate with ext: " + (String)localObject2 + " already EXIST", TraceLevel.BASIC);
          if (str2 == null)
            str2 = getOpenActionCommand(localAssociation2);
          i = 1;
          if ((str2 == null) || (!promptReplace(paramLaunchDesc, (String)localObject2, null, str2)))
            return false;
        }
        localAssociation1.addFileExtension((String)localObject2);
      }
    }
    if ((paramString3 != null) && (paramString3.length() > 0))
      str1 = paramString3;
    if (paramString2 != null)
    {
      Trace.println("associate with mime: " + paramString2, TraceLevel.BASIC);
      localAssociation2 = localAssociationService.getMimeTypeAssociation(paramString2);
      if (((localAssociation2 != null ? 1 : 0) & (i == 0 ? 1 : 0)) != 0)
      {
        Trace.println("associate with mime: " + paramString2 + " already EXIST", TraceLevel.BASIC);
        localObject1 = getOpenActionCommand(localAssociation2);
        if ((localObject1 != str2) && (!promptReplace(paramLaunchDesc, null, paramString2, (String)localObject1)))
          return false;
        i = 1;
      }
      localAssociation1.setMimeType(paramString2);
    }
    localAssociation1.setName(paramLaunchDesc.getInformation().getTitle());
    localAssociation1.setDescription(str1);
    if (paramURL != null)
      localObject1 = IcoEncoder.getIconPath(paramURL, null);
    else
      localObject1 = IcoEncoder.getIconPath(paramLaunchDesc);
    if (localObject1 == null)
      localObject1 = getDefaultIconPath();
    localAssociation1.setIconFileName((String)localObject1);
    String str3 = getJnlpLocation(paramLaunchDesc);
    String str4 = getAssociationOpenCommand(str3);
    String str5 = getAssociationPrintCommand(str3);
    Trace.println("register OPEN using: " + str4, TraceLevel.BASIC);
    Object localObject2 = new Action("open", str4, "open the file");
    localAssociation1.addAction((Action)localObject2);
    if (str5 != null)
    {
      Trace.println("register PRINT using: " + str5, TraceLevel.BASIC);
      localObject2 = new Action("print", str5, "print the file");
      localAssociation1.addAction((Action)localObject2);
    }
    try
    {
      registerAssociationInternal(localAssociation1);
    }
    catch (AssociationAlreadyRegisteredException localAssociationAlreadyRegisteredException1)
    {
      try
      {
        unregisterAssociationInternal(localAssociation1);
        registerAssociationInternal(localAssociation1);
      }
      catch (AssociationNotRegisteredException localAssociationNotRegisteredException)
      {
        Trace.ignoredException(localAssociationNotRegisteredException);
        return false;
      }
      catch (AssociationAlreadyRegisteredException localAssociationAlreadyRegisteredException2)
      {
        Trace.ignoredException(localAssociationAlreadyRegisteredException2);
        return false;
      }
      catch (RegisterFailedException localRegisterFailedException2)
      {
        Trace.ignoredException(localRegisterFailedException2);
        return false;
      }
    }
    catch (RegisterFailedException localRegisterFailedException1)
    {
      Trace.ignoredException(localRegisterFailedException1);
      return false;
    }
    return true;
  }

  private void unregisterAssociation(LaunchDesc paramLaunchDesc, String paramString1, String paramString2)
  {
    AssociationService localAssociationService = new AssociationService();
    Association localAssociation = null;
    if (paramString2 != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString2);
      while (localStringTokenizer.hasMoreTokens())
      {
        String str = "." + localStringTokenizer.nextToken();
        localAssociation = localAssociationService.getFileExtensionAssociation(str);
        if (localAssociation == null)
          continue;
        localAssociation.setName(paramLaunchDesc.getInformation().getTitle());
        Trace.println("remove association with ext: " + str, TraceLevel.BASIC);
        try
        {
          unregisterAssociationInternal(localAssociation);
        }
        catch (AssociationNotRegisteredException localAssociationNotRegisteredException2)
        {
          Trace.ignoredException(localAssociationNotRegisteredException2);
        }
        catch (RegisterFailedException localRegisterFailedException2)
        {
          Trace.ignoredException(localRegisterFailedException2);
        }
      }
    }
    if (paramString1 != null)
    {
      localAssociation = localAssociationService.getMimeTypeAssociation(paramString1);
      if (localAssociation != null)
      {
        localAssociation.setName(paramLaunchDesc.getInformation().getTitle());
        Trace.println("remove association with mime: " + paramString1, TraceLevel.BASIC);
        try
        {
          unregisterAssociationInternal(localAssociation);
        }
        catch (AssociationNotRegisteredException localAssociationNotRegisteredException1)
        {
          Trace.ignoredException(localAssociationNotRegisteredException1);
        }
        catch (RegisterFailedException localRegisterFailedException1)
        {
          Trace.ignoredException(localRegisterFailedException1);
        }
      }
    }
  }

  public void removeAssociations(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    if (isAssociationSupported())
    {
      AssociationDesc[] arrayOfAssociationDesc = paramLocalApplicationProperties.getAssociations();
      if (arrayOfAssociationDesc != null)
      {
        for (int i = 0; i < arrayOfAssociationDesc.length; i++)
        {
          String str1 = arrayOfAssociationDesc[i].getExtensions();
          String str2 = arrayOfAssociationDesc[i].getMimeType();
          removeAssociationIfCurent(paramLaunchDesc, str2, str1);
        }
        paramLocalApplicationProperties.setAssociations(null);
      }
    }
  }

  private void removeAssociationIfCurent(LaunchDesc paramLaunchDesc, String paramString1, String paramString2)
  {
    String str1 = getAssociationOpenCommand(getJnlpLocation(paramLaunchDesc));
    AssociationService localAssociationService = new AssociationService();
    Association localAssociation = localAssociationService.getMimeTypeAssociation(paramString1);
    if (localAssociation != null)
    {
      String str2 = getOpenActionCommand(localAssociation);
      if (str1.equals(str2))
        unregisterAssociation(paramLaunchDesc, paramString1, paramString2);
      else
        Trace.println("Not removing association because existing command is: " + str2 + " instead of: " + str1, TraceLevel.BASIC);
    }
  }

  public boolean createAssociations(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    if (Config.getAssociationValue() == 0)
      return false;
    int i = 1;
    if (isAssociationSupported())
    {
      AssociationDesc[] arrayOfAssociationDesc = paramLaunchDesc.getInformation().getAssociations();
      for (int j = 0; (arrayOfAssociationDesc != null) && (j < arrayOfAssociationDesc.length); j++)
      {
        String str1 = arrayOfAssociationDesc[j].getExtensions();
        String str2 = arrayOfAssociationDesc[j].getMimeType();
        String str3 = arrayOfAssociationDesc[j].getMimeDescription();
        URL localURL = arrayOfAssociationDesc[j].getIconUrl();
        if (registerAssociation(paramLaunchDesc, str1, str2, str3, localURL))
        {
          paramLocalApplicationProperties.addAssociation(arrayOfAssociationDesc[j]);
          save(paramLocalApplicationProperties);
        }
        else
        {
          i = 0;
        }
      }
    }
    else
    {
      i = 0;
    }
    return i;
  }

  public boolean performIntegration(ComponentRef paramComponentRef, LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    boolean bool1 = true;
    boolean bool2 = true;
    int i = 1;
    boolean bool3 = false;
    boolean bool4 = false;
    int j = 0;
    int k = 0;
    int m = 0;
    boolean bool5 = false;
    int n = paramLaunchDesc.getSecurityModel() != 0 ? 1 : 0;
    ShortcutDesc localShortcutDesc = paramLaunchDesc.getInformation().getShortcut();
    if (localShortcutDesc != null)
    {
      if ((!localShortcutDesc.getDesktop()) && (!localShortcutDesc.getMenu()))
        paramBoolean2 = false;
      bool5 = localShortcutDesc.getInstall();
    }
    if (paramBoolean2)
      if (paramBoolean1)
      {
        bool3 = (Globals.createShortcut()) || (Config.getShortcutValue() == 1) || (Config.getShortcutValue() == 4);
        k = 0;
      }
      else
      {
        switch (Config.getShortcutValue())
        {
        case 0:
          bool3 = false;
          k = 0;
          break;
        case 1:
          bool3 = true;
          k = 0;
          break;
        case 4:
          bool3 = localShortcutDesc != null;
          k = 0;
          break;
        case 3:
          bool3 = localShortcutDesc != null;
          k = localShortcutDesc != null ? 1 : 0;
          break;
        case 2:
        default:
          bool3 = true;
          k = 1;
        }
      }
    if (paramBoolean3)
    {
      AssociationService localAssociationService = new AssociationService();
      boolean bool6 = true;
      AssociationDesc[] arrayOfAssociationDesc = paramLaunchDesc.getInformation().getAssociations();
      for (int i1 = 0; (arrayOfAssociationDesc != null) && (i1 < arrayOfAssociationDesc.length); i1++)
      {
        if (localAssociationService.getMimeTypeAssociation(arrayOfAssociationDesc[i1].getMimeType()) != null)
        {
          bool6 = false;
          break;
        }
        String str1 = arrayOfAssociationDesc[i1].getExtensions();
        StringTokenizer localStringTokenizer = new StringTokenizer(str1);
        while (localStringTokenizer.hasMoreTokens())
        {
          String str2 = "." + localStringTokenizer.nextToken();
          if (localAssociationService.getFileExtensionAssociation(str2) != null)
          {
            bool6 = false;
            break;
          }
        }
      }
      if (paramBoolean1)
      {
        m = 0;
        switch (Config.getAssociationValue())
        {
        case 0:
          bool4 = false;
          break;
        case 1:
          bool4 = bool6;
          break;
        case 3:
          bool4 = (bool6) || (Globals.createAssoc());
          break;
        case 4:
          bool4 = true;
          break;
        case 2:
        default:
          bool4 = Globals.createAssoc();
          break;
        }
      }
      else
      {
        switch (Config.getAssociationValue())
        {
        case 0:
          bool4 = false;
          m = 0;
          break;
        case 1:
          bool4 = bool6;
          m = 0;
          break;
        case 3:
          bool4 = true;
          m = !bool6 ? 1 : 0;
          break;
        case 4:
          bool4 = true;
          m = 0;
          break;
        case 2:
        default:
          bool4 = true;
          m = 1;
        }
      }
    }
    if (bool5)
      switch (Config.getInstallMode())
      {
      case 0:
      case 1:
      case 2:
        j = 0;
        break;
      case 3:
      default:
        j = 1;
      }
    if ((k != 0) || (m != 0))
    {
      if ((Environment.isImportMode()) || (n == 0))
      {
        if (!showDialog(paramComponentRef == null ? null : paramComponentRef.get(), paramLaunchDesc, paramLocalApplicationProperties, bool3, bool4));
      }
      else
      {
        if (k != 0)
          bool3 = true;
        if (m == 0)
          break label688;
        bool4 = true;
        break label688;
      }
      if (k != 0)
        bool3 = false;
      if (m != 0)
        bool4 = false;
    }
    label688: if (bool3)
      bool1 = installShortcuts(paramLaunchDesc, paramLocalApplicationProperties, null);
    if (bool4)
      bool2 = createAssociations(paramLaunchDesc, paramLocalApplicationProperties);
    if (j != 0)
    {
      paramLocalApplicationProperties.setJnlpInstalled(true);
      updateInstallPanel(paramLaunchDesc, paramLocalApplicationProperties);
      save(paramLocalApplicationProperties);
    }
    return (bool1) && (bool2);
  }

  public boolean installShortcuts(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    boolean bool = installShortcuts(paramLaunchDesc, paramLocalApplicationProperties, null);
    return bool;
  }

  private boolean installShortcuts(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean[] paramArrayOfBoolean)
  {
    boolean bool = createShortcuts(paramLaunchDesc, paramLocalApplicationProperties, paramArrayOfBoolean);
    paramLocalApplicationProperties.setAskedForInstall(true);
    RContentDesc[] arrayOfRContentDesc = paramLaunchDesc.getInformation().getRelatedContent();
    int i;
    Object localObject;
    if (arrayOfRContentDesc != null)
      for (i = 0; i < arrayOfRContentDesc.length; i++)
      {
        localObject = arrayOfRContentDesc[i].getHref();
        if (("jar".equals(((URL)localObject).getProtocol())) || (!((URL)localObject).toString().endsWith(".jnlp")))
          continue;
        try
        {
          Main.importApp(((URL)localObject).toString());
        }
        catch (Exception localException)
        {
          Trace.ignoredException(localException);
        }
      }
    if ((bool) && (!paramLocalApplicationProperties.isJnlpInstalled()))
    {
      i = Config.getInstallMode();
      localObject = paramLaunchDesc.getInformation().getShortcut();
      if ((i == 1) || ((i == 2) && (localObject != null) && (((ShortcutDesc)localObject).getInstall())))
      {
        paramLocalApplicationProperties.setJnlpInstalled(true);
        updateInstallPanel(paramLaunchDesc, paramLocalApplicationProperties);
        save(paramLocalApplicationProperties);
      }
    }
    return bool;
  }

  private boolean showDialog(Object paramObject, LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean1, boolean paramBoolean2)
  {
    InformationDesc localInformationDesc = paramLaunchDesc.getInformation();
    IconDesc localIconDesc = localInformationDesc.getIconLocation(48, 0);
    URL localURL = localIconDesc == null ? null : localIconDesc.getLocation();
    String str1 = localIconDesc == null ? null : localIconDesc.getVersion();
    boolean bool1 = false;
    boolean bool2 = false;
    String str2 = null;
    if (paramBoolean1)
    {
      localObject = localInformationDesc.getShortcut();
      if (localObject != null)
      {
        bool1 = ((ShortcutDesc)localObject).getDesktop();
        bool2 = ((ShortcutDesc)localObject).getMenu();
        str2 = ((ShortcutDesc)localObject).getSubmenu();
      }
      else
      {
        bool1 = true;
        bool2 = true;
      }
    }
    Object localObject = paramBoolean2 ? localInformationDesc.getAssociations() : new AssociationDesc[0];
    AppInfo localAppInfo = new AppInfo(paramLaunchDesc.getLaunchType(), localInformationDesc.getTitle(), localInformationDesc.getVendor(), paramLaunchDesc.getCanonicalHome(), localURL, str1, bool1, bool2, str2, localObject);
    ToolkitStore.getUI();
    ToolkitStore.getUI();
    return ToolkitStore.getUI().showMessageDialog(paramObject, localAppInfo, 5, null, null, null, null, null, null, null) == 0;
  }

  private boolean promptReplace(LaunchDesc paramLaunchDesc, String paramString1, String paramString2, String paramString3)
  {
    String str1;
    if (paramString1 != null)
      str1 = ResourceManager.getString("association.replace.ext", paramString1);
    else
      str1 = ResourceManager.getString("association.replace.mime", paramString2);
    String str2 = paramString3;
    String str3 = Cache.getCacheDir().toString();
    int i = paramString3.indexOf(str3);
    if (i >= 0)
    {
      int j = paramString3.indexOf("\"", i + str3.length());
      if (j < 0)
        j = paramString3.indexOf(" ", i + str3.length());
      if (j >= 0)
        str4 = paramString3.substring(i, j);
      else
        str4 = paramString3.substring(i);
      try
      {
        LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(str4);
        if (localLaunchDesc != null)
        {
          str2 = localLaunchDesc.getInformation().getTitle();
          if (localLaunchDesc.getCanonicalHome().toString().equals(paramLaunchDesc.getCanonicalHome().toString()))
            return true;
        }
      }
      catch (Exception localException)
      {
        return true;
      }
    }
    String str4 = ResourceManager.getString("association.replace.info", str2);
    String str5 = ResourceManager.getString("association.replace.title");
    String str6 = ResourceManager.getString("common.ok_btn");
    String str7 = ResourceManager.getString("common.cancel_btn");
    ToolkitStore.getUI();
    ToolkitStore.getUI();
    return ToolkitStore.getUI().showMessageDialog(null, paramLaunchDesc.getAppInfo(), 3, str5, null, str1, str4, str6, str7, null) == 0;
  }

  public boolean uninstallShortcuts(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    return removeShortcuts(paramLaunchDesc, paramLocalApplicationProperties, true);
  }

  public static boolean shouldInstallOverExisting(LaunchDesc paramLaunchDesc)
  {
    int[] tmp3_1 = new int[1];
    ToolkitStore.getUI();
    tmp3_1[0] = -1;
    int[] arrayOfInt = tmp3_1;
    1 local1 = new Runnable(paramLaunchDesc, arrayOfInt)
    {
      private final LaunchDesc val$ld;
      private final int[] val$result;

      public void run()
      {
        String str1 = ResourceManager.getString("install.alreadyInstalledTitle");
        String str2 = ResourceManager.getString("install.alreadyInstalled", this.val$ld.getInformation().getTitle());
        String str3 = ResourceManager.getString("common.ok_btn");
        String str4 = ResourceManager.getString("common.cancel_btn");
        ToolkitStore.getUI();
        this.val$result[0] = ToolkitStore.getUI().showMessageDialog(null, this.val$ld.getAppInfo(), 3, str1, null, str2, null, str3, str4, null);
      }
    };
    if (!Globals.isSilentMode())
      invokeRunnable(local1);
    ToolkitStore.getUI();
    return arrayOfInt[0] == 0;
  }

  public static void invokeRunnable(Runnable paramRunnable)
  {
    if (SwingUtilities.isEventDispatchThread())
      paramRunnable.run();
    else
      try
      {
        SwingUtilities.invokeAndWait(paramRunnable);
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      catch (InvocationTargetException localInvocationTargetException)
      {
      }
  }

  public static void save(LocalApplicationProperties paramLocalApplicationProperties)
  {
    try
    {
      paramLocalApplicationProperties.store();
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
  }

  public boolean addUninstallShortcut()
  {
    return (Config.getBooleanProperty("deployment.javaws.uninstall.shortcut")) && (!Environment.isSystemCacheMode());
  }

  boolean removeShortcuts(String paramString)
  {
    return removePathShortcut(paramString);
  }

  void removeAssociations(String paramString1, String paramString2)
  {
    Association localAssociation = new Association();
    String str1 = "";
    if (paramString2 != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString2);
      while (localStringTokenizer.hasMoreTokens())
      {
        String str2 = "." + localStringTokenizer.nextToken();
        if (str1 == "")
          str1 = str2 + " file";
        localAssociation.addFileExtension(str2);
      }
    }
    if (paramString1 != null)
      localAssociation.setMimeType(paramString1);
    localAssociation.setName(" ");
    localAssociation.setDescription(str1);
    try
    {
      unregisterAssociationInternal(localAssociation);
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
  }

  void reinstallShortcuts(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties, boolean paramBoolean1, boolean paramBoolean2)
  {
    boolean[] arrayOfBoolean = new boolean[2];
    arrayOfBoolean[0] = paramBoolean1;
    arrayOfBoolean[1] = paramBoolean2;
    installShortcuts(paramLaunchDesc, paramLocalApplicationProperties, arrayOfBoolean);
  }

  void reinstallAssociations(LaunchDesc paramLaunchDesc, LocalApplicationProperties paramLocalApplicationProperties)
  {
    createAssociations(paramLaunchDesc, paramLocalApplicationProperties);
  }

  protected String checkTitleString(String paramString1, String paramString2, char paramChar)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i;
    int j;
    if (Config.isJavaVersionAtLeast15())
    {
      i = 0;
      while (i < paramString1.length())
      {
        j = paramString1.codePointAt(i);
        if (paramString2.indexOf(j) >= 0)
          localStringBuffer.appendCodePoint(paramChar);
        else if (!isShortCutSafe(j))
          localStringBuffer.appendCodePoint(paramChar);
        else
          localStringBuffer.appendCodePoint(j);
        i += Character.charCount(j);
      }
    }
    else
    {
      for (i = 0; i < paramString1.length(); i++)
      {
        j = paramString1.charAt(i);
        if (paramString2.indexOf(j) >= 0)
          localStringBuffer.append(paramChar);
        else if (!isShortCutSafe14(j))
          localStringBuffer.append(paramChar);
        else
          localStringBuffer.append(j);
      }
    }
    return localStringBuffer.toString();
  }

  private static boolean isShortCutSafe(int paramInt)
  {
    return (!Character.isIdentifierIgnorable(paramInt)) && (paramInt >= 32);
  }

  private static boolean isShortCutSafe14(char paramChar)
  {
    return (!Character.isIdentifierIgnorable(paramChar)) && (paramChar >= ' ');
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.LocalInstallHandler
 * JD-Core Version:    0.6.0
 */