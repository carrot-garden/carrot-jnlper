package com.sun.deploy.cache;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

public abstract interface LocalApplicationProperties
{
  public abstract URL getLocation();

  public abstract String getVersionId();

  public abstract void setLastAccessed(Date paramDate);

  public abstract Date getLastAccessed();

  public abstract int getLaunchCount();

  public abstract void incrementLaunchCount();

  public abstract void setAskedForInstall(boolean paramBoolean);

  public abstract boolean getAskedForInstall();

  public abstract void setRebootNeeded(boolean paramBoolean);

  public abstract boolean isRebootNeeded();

  public abstract void setShortcutInstalled(boolean paramBoolean);

  public abstract boolean isShortcutInstalled();

  public abstract boolean isShortcutInstalledSystem();

  public abstract void setExtensionInstalled(boolean paramBoolean);

  public abstract boolean isExtensionInstalled();

  public abstract void setJnlpInstalled(boolean paramBoolean);

  public abstract boolean isJnlpInstalled();

  public abstract boolean forceUpdateCheck();

  public abstract void setForceUpdateCheck(boolean paramBoolean);

  public abstract boolean isApplicationDescriptor();

  public abstract boolean isExtensionDescriptor();

  public abstract AssociationDesc[] getAssociations();

  public abstract void addAssociation(AssociationDesc paramAssociationDesc);

  public abstract void setAssociations(AssociationDesc[] paramArrayOfAssociationDesc);

  public abstract String getNativeLibDirectory();

  public abstract String getInstallDirectory();

  public abstract void setNativeLibDirectory(String paramString);

  public abstract void setInstallDirectory(String paramString);

  public abstract String getRegisteredTitle();

  public abstract void setRegisteredTitle(String paramString);

  public abstract void put(String paramString1, String paramString2);

  public abstract String get(String paramString);

  public abstract int getInteger(String paramString);

  public abstract boolean getBoolean(String paramString);

  public abstract Date getDate(String paramString);

  public abstract void store()
    throws IOException;

  public abstract void refreshIfNecessary();

  public abstract void refresh();

  public abstract void setDraggedApplet();

  public abstract boolean isDraggedApplet();

  public abstract void setDocumentBase(String paramString);

  public abstract String getDocumentBase();

  public abstract void setCodebase(String paramString);

  public abstract String getCodebase();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.LocalApplicationProperties
 * JD-Core Version:    0.6.0
 */