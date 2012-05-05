package com.sun.jnlp;

import com.sun.deploy.association.Association;
import com.sun.deploy.association.AssociationNotRegisteredException;
import com.sun.deploy.association.RegisterFailedException;
import com.sun.deploy.cache.AssociationDesc;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.javaws.LocalInstallHandler;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ShortcutDesc;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.IntegrationService;

public class IntegrationServiceImpl
  implements IntegrationService
{
  private JNLPClassLoaderIf jnlpClassLoader;
  private static final String TSPECIALS = "()<>@,;:\\\"/[]?=";

  public IntegrationServiceImpl(JNLPClassLoaderIf paramJNLPClassLoaderIf)
  {
    this.jnlpClassLoader = paramJNLPClassLoaderIf;
  }

  public boolean requestShortcut(boolean paramBoolean1, boolean paramBoolean2, String paramString)
  {
    boolean bool = (paramBoolean1) || (paramBoolean2);
    LaunchDesc localLaunchDesc = this.jnlpClassLoader.getLaunchDesc();
    InformationDesc localInformationDesc = localLaunchDesc.getInformation();
    localInformationDesc.setShortcut(new ShortcutDesc(true, false, paramBoolean1, paramBoolean2, paramString));
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    LocalApplicationProperties localLocalApplicationProperties = getLocalApplicationProperties(localLaunchDesc);
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(localLocalInstallHandler, localLaunchDesc, localLocalApplicationProperties, bool)
    {
      private final LocalInstallHandler val$lih;
      private final LaunchDesc val$ld;
      private final LocalApplicationProperties val$lap;
      private final boolean val$anyShortcuts;

      public Object run()
      {
        boolean bool = false;
        if (this.val$lih.isLocalInstallSupported())
        {
          bool = this.val$lih.performIntegration(null, this.val$ld, this.val$lap, false, true, false);
          try
          {
            this.val$lap.store();
          }
          catch (Exception localException)
          {
          }
        }
        else
        {
          bool = !this.val$anyShortcuts;
        }
        return Boolean.valueOf(bool);
      }
    });
    return localBoolean.booleanValue();
  }

  public boolean hasDesktopShortcut()
  {
    return hasShortcut(0);
  }

  public boolean hasMenuShortcut()
  {
    return hasShortcut(1);
  }

  private boolean hasShortcut(int paramInt)
  {
    LaunchDesc localLaunchDesc = this.jnlpClassLoader.getLaunchDesc();
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    LocalApplicationProperties localLocalApplicationProperties = getLocalApplicationProperties(localLaunchDesc);
    boolean[] arrayOfBoolean = (boolean[])(boolean[])AccessController.doPrivileged(new PrivilegedAction(localLocalInstallHandler, localLocalApplicationProperties)
    {
      private final LocalInstallHandler val$lih;
      private final LocalApplicationProperties val$lap;

      public Object run()
      {
        return this.val$lih.whichShortcutsExist(this.val$lap);
      }
    });
    return arrayOfBoolean[paramInt];
  }

  private LocalApplicationProperties getLocalApplicationProperties(LaunchDesc paramLaunchDesc)
  {
    LocalApplicationProperties localLocalApplicationProperties = (LocalApplicationProperties)AccessController.doPrivileged(new PrivilegedAction(paramLaunchDesc)
    {
      private final LaunchDesc val$ld;

      public Object run()
      {
        URL localURL = this.val$ld.getCanonicalHome();
        return Cache.getLocalApplicationProperties(localURL);
      }
    });
    return localLocalApplicationProperties;
  }

  public boolean removeShortcuts()
  {
    LaunchDesc localLaunchDesc = this.jnlpClassLoader.getLaunchDesc();
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    LocalApplicationProperties localLocalApplicationProperties = getLocalApplicationProperties(localLaunchDesc);
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(localLocalInstallHandler, localLaunchDesc, localLocalApplicationProperties)
    {
      private final LocalInstallHandler val$lih;
      private final LaunchDesc val$ld;
      private final LocalApplicationProperties val$lap;

      public Object run()
      {
        boolean bool = this.val$lih.uninstallShortcuts(this.val$ld, this.val$lap);
        try
        {
          this.val$lap.store();
        }
        catch (Exception localException)
        {
        }
        return Boolean.valueOf(bool);
      }
    });
    return localBoolean.booleanValue();
  }

  public boolean requestAssociation(String paramString, String[] paramArrayOfString)
  {
    validateAssociationArguments(paramString, paramArrayOfString);
    LaunchDesc localLaunchDesc = this.jnlpClassLoader.getLaunchDesc();
    InformationDesc localInformationDesc = localLaunchDesc.getInformation();
    AssociationDesc localAssociationDesc = createAssociationDesc(paramString, paramArrayOfString);
    localInformationDesc.setAssociation(localAssociationDesc);
    LocalApplicationProperties localLocalApplicationProperties = getLocalApplicationProperties(localLaunchDesc);
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    return ((Boolean)AccessController.doPrivileged(new PrivilegedAction(localLocalInstallHandler, localLaunchDesc, localLocalApplicationProperties)
    {
      private final LocalInstallHandler val$lih;
      private final LaunchDesc val$ld;
      private final LocalApplicationProperties val$lap;

      public Object run()
      {
        boolean bool = false;
        if (this.val$lih.isAssociationSupported())
          bool = this.val$lih.performIntegration(null, this.val$ld, this.val$lap, false, false, true);
        return Boolean.valueOf(bool);
      }
    })).booleanValue();
  }

  public boolean hasAssociation(String paramString, String[] paramArrayOfString)
  {
    validateAssociationArguments(paramString, paramArrayOfString);
    Association localAssociation = createAssociation(paramString, paramArrayOfString);
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    return ((Boolean)AccessController.doPrivileged(new PrivilegedAction(localLocalInstallHandler, localAssociation)
    {
      private final LocalInstallHandler val$lih;
      private final Association val$association;

      public Object run()
      {
        return Boolean.valueOf(this.val$lih.hasAssociation(this.val$association));
      }
    })).booleanValue();
  }

  public boolean removeAssociation(String paramString, String[] paramArrayOfString)
  {
    validateAssociationArguments(paramString, paramArrayOfString);
    Association localAssociation = createAssociation(paramString, paramArrayOfString);
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    return ((Boolean)AccessController.doPrivileged(new PrivilegedAction(localLocalInstallHandler, localAssociation)
    {
      private final LocalInstallHandler val$lih;
      private final Association val$association;

      public Object run()
      {
        Boolean localBoolean = Boolean.TRUE;
        if (this.val$lih.isAssociationSupported())
          try
          {
            this.val$lih.unregisterAssociationInternal(this.val$association);
          }
          catch (AssociationNotRegisteredException localAssociationNotRegisteredException)
          {
            localBoolean = Boolean.FALSE;
          }
          catch (RegisterFailedException localRegisterFailedException)
          {
            localBoolean = Boolean.FALSE;
          }
        return localBoolean;
      }
    })).booleanValue();
  }

  private void validateAssociationArguments(String paramString, String[] paramArrayOfString)
  {
    validateMimeType(paramString);
    validateExtensions(paramArrayOfString);
  }

  private void validateExtensions(String[] paramArrayOfString)
  {
    if (paramArrayOfString == null)
      throw new IllegalArgumentException("Null extensions array not allowed");
    for (int i = 0; i < paramArrayOfString.length; i++)
    {
      String str = paramArrayOfString[i];
      if (str == null)
        throw new IllegalArgumentException("Null extension not allowed");
      if (!str.equals(""))
        continue;
      throw new IllegalArgumentException("Empty extension not allowed");
    }
  }

  private void validateMimeType(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("Null mimetype not allowed");
    int i = paramString.indexOf('/');
    int j = paramString.indexOf(';');
    if ((i < 0) && (j < 0))
      throw new IllegalArgumentException("Unable to find a sub type.");
    if ((i < 0) && (j >= 0))
      throw new IllegalArgumentException("Unable to find a sub type.");
    String str1;
    String str2;
    if ((i >= 0) && (j < 0))
    {
      str1 = paramString.substring(0, i).trim().toLowerCase();
      str2 = paramString.substring(i + 1).trim().toLowerCase();
    }
    else if (i < j)
    {
      str1 = paramString.substring(0, i).trim().toLowerCase();
      str2 = paramString.substring(i + 1, j).trim().toLowerCase();
    }
    else
    {
      throw new IllegalArgumentException("Unable to find a sub type.");
    }
    if (!isValidToken(str1))
      throw new IllegalArgumentException("Primary type is invalid.");
    if (!isValidToken(str2))
      throw new IllegalArgumentException("Sub type is invalid.");
  }

  private boolean isValidToken(String paramString)
  {
    int i = paramString.length();
    if (i > 0)
    {
      for (int j = 0; j < i; j++)
      {
        char c = paramString.charAt(j);
        if (!isTokenChar(c))
          return false;
      }
      return true;
    }
    return false;
  }

  private static boolean isTokenChar(char paramChar)
  {
    return (paramChar > ' ') && (paramChar < '') && ("()<>@,;:\\\"/[]?=".indexOf(paramChar) < 0);
  }

  private Association createAssociation(String paramString, String[] paramArrayOfString)
  {
    Association localAssociation = new Association();
    localAssociation.setMimeType(paramString);
    for (int i = 0; i < paramArrayOfString.length; i++)
      localAssociation.addFileExtension(paramArrayOfString[i]);
    LaunchDesc localLaunchDesc = this.jnlpClassLoader.getLaunchDesc();
    localAssociation.setName(getAssociationDescription(localLaunchDesc));
    return localAssociation;
  }

  private AssociationDesc createAssociationDesc(String paramString, String[] paramArrayOfString)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; i < paramArrayOfString.length; i++)
    {
      localStringBuilder.append(paramArrayOfString[i]);
      if (i >= paramArrayOfString.length - 1)
        continue;
      localStringBuilder.append(' ');
    }
    LaunchDesc localLaunchDesc = this.jnlpClassLoader.getLaunchDesc();
    AssociationDesc localAssociationDesc = new AssociationDesc(localStringBuilder.toString(), paramString, getAssociationDescription(localLaunchDesc), null);
    return localAssociationDesc;
  }

  private String getAssociationDescription(LaunchDesc paramLaunchDesc)
  {
    return paramLaunchDesc.getInformation().getTitle();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.IntegrationServiceImpl
 * JD-Core Version:    0.6.0
 */