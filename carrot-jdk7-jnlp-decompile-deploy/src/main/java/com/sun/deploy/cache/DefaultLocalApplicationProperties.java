package com.sun.deploy.cache;

import com.sun.deploy.Environment;
import com.sun.deploy.trace.Trace;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class DefaultLocalApplicationProperties
  implements LocalApplicationProperties
{
  private static final String REBOOT_NEEDED_KEY = "rebootNeeded";
  private static final String UPDATE_CHECK_KEY = "forcedUpdateCheck";
  private static final String NATIVELIB_DIR_KEY = "nativeLibDir";
  private static final String INSTALL_DIR_KEY = "installDir";
  private static final String LAST_ACCESSED_KEY = "lastAccessed";
  private static final String LAUNCH_COUNT_KEY = "launchCount";
  private static final String ASK_INSTALL_KEY = "askedInstall";
  private static final String SHORTCUT_KEY = "locallyInstalled";
  private static final String EXTENSION_KEY = "extensionInstalled";
  private static final String JNLP_INSTALLED_KEY = "jnlpInstalled";
  private static final String INDIRECT_PATH_KEY = "indirectPath";
  private static final String ASSOCIATION_MIME_KEY = "mime.types.";
  private static final String REGISTERED_TITLE_KEY = "title";
  private static final String ASSOCIATION_EXTENSIONS_KEY = "extensions.";
  private static final String DRAGGED_APPLET_KEY = "draggedApplet";
  private static final String DOCUMENTBASE_KEY = "documentBase";
  private static final String CODEBASE_KEY = "codebase";
  private static final DateFormat _df = DateFormat.getDateTimeInstance();
  private Properties _properties;
  private URL _location;
  private String _versionId;
  private long _lastAccessed;
  private boolean _isApplicationDescriptor;
  private boolean _dirty;
  private boolean _isShortcutInstalledSystem;

  public DefaultLocalApplicationProperties(URL paramURL, String paramString, boolean paramBoolean)
  {
    this._location = paramURL;
    this._versionId = paramString;
    this._isApplicationDescriptor = paramBoolean;
    this._properties = getLocalApplicationPropertiesStorage(this);
    this._isShortcutInstalledSystem = false;
  }

  public URL getLocation()
  {
    return this._location;
  }

  public String getVersionId()
  {
    return this._versionId;
  }

  public void setLastAccessed(Date paramDate)
  {
    put("lastAccessed", _df.format(paramDate));
  }

  public Date getLastAccessed()
  {
    return getDate("lastAccessed");
  }

  public void incrementLaunchCount()
  {
    int i = getLaunchCount();
    i++;
    put("launchCount", Integer.toString(i));
  }

  public int getLaunchCount()
  {
    return getInteger("launchCount");
  }

  public void setAskedForInstall(boolean paramBoolean)
  {
    put("askedInstall", new Boolean(paramBoolean).toString());
  }

  public boolean getAskedForInstall()
  {
    return getBoolean("askedInstall");
  }

  public void setRebootNeeded(boolean paramBoolean)
  {
    put("rebootNeeded", new Boolean(paramBoolean).toString());
  }

  public boolean isRebootNeeded()
  {
    return getBoolean("rebootNeeded");
  }

  public void setDraggedApplet()
  {
    put("draggedApplet", new Boolean(true).toString());
  }

  public boolean isDraggedApplet()
  {
    return getBoolean("draggedApplet");
  }

  public void setDocumentBase(String paramString)
  {
    put("documentBase", paramString);
  }

  public String getDocumentBase()
  {
    return get("documentBase");
  }

  public void setCodebase(String paramString)
  {
    put("codebase", paramString);
  }

  public String getCodebase()
  {
    return get("codebase");
  }

  public void setShortcutInstalled(boolean paramBoolean)
  {
    put("locallyInstalled", new Boolean(paramBoolean).toString());
  }

  public boolean isShortcutInstalled()
  {
    return getBoolean("locallyInstalled");
  }

  public boolean isShortcutInstalledSystem()
  {
    return this._isShortcutInstalledSystem;
  }

  public void setExtensionInstalled(boolean paramBoolean)
  {
    setShortcutInstalled(false);
    put("extensionInstalled", new Boolean(paramBoolean).toString());
  }

  public boolean isExtensionInstalled()
  {
    return (getBoolean("extensionInstalled")) || (getBoolean("locallyInstalled"));
  }

  public void setJnlpInstalled(boolean paramBoolean)
  {
    put("jnlpInstalled", new Boolean(paramBoolean).toString());
  }

  public boolean isJnlpInstalled()
  {
    return getBoolean("jnlpInstalled");
  }

  public boolean forceUpdateCheck()
  {
    return getBoolean("forcedUpdateCheck");
  }

  public void setForceUpdateCheck(boolean paramBoolean)
  {
    put("forcedUpdateCheck", new Boolean(paramBoolean).toString());
  }

  public boolean isApplicationDescriptor()
  {
    return this._isApplicationDescriptor;
  }

  public boolean isExtensionDescriptor()
  {
    return !this._isApplicationDescriptor;
  }

  public String getInstallDirectory()
  {
    return get("installDir");
  }

  public void setInstallDirectory(String paramString)
  {
    put("installDir", paramString);
  }

  public String getNativeLibDirectory()
  {
    return get("nativeLibDir");
  }

  public void setNativeLibDirectory(String paramString)
  {
    put("nativeLibDir", paramString);
  }

  public String getRegisteredTitle()
  {
    return get("title");
  }

  public void setRegisteredTitle(String paramString)
  {
    put("title", paramString);
  }

  public void setAssociations(AssociationDesc[] paramArrayOfAssociationDesc)
  {
    int i = 0;
    if (paramArrayOfAssociationDesc == null)
    {
      AssociationDesc[] arrayOfAssociationDesc = getAssociations();
      if (arrayOfAssociationDesc != null)
      {
        put("mime.types." + i, null);
        put("extensions." + i, null);
      }
    }
    else
    {
      for (i = 0; i < paramArrayOfAssociationDesc.length; i++)
      {
        put("mime.types." + i, paramArrayOfAssociationDesc[i].getMimeType());
        put("extensions." + i, paramArrayOfAssociationDesc[i].getExtensions());
      }
      put("mime.types." + i, null);
      put("extensions." + i, null);
    }
  }

  public void addAssociation(AssociationDesc paramAssociationDesc)
  {
    AssociationDesc[] arrayOfAssociationDesc2 = getAssociations();
    int i = 0;
    AssociationDesc[] arrayOfAssociationDesc1;
    if (arrayOfAssociationDesc2 == null)
    {
      arrayOfAssociationDesc1 = new AssociationDesc[1];
    }
    else
    {
      arrayOfAssociationDesc1 = new AssociationDesc[arrayOfAssociationDesc2.length + 1];
      while (i < arrayOfAssociationDesc2.length)
      {
        arrayOfAssociationDesc1[i] = arrayOfAssociationDesc2[i];
        i++;
      }
    }
    arrayOfAssociationDesc1[i] = paramAssociationDesc;
    setAssociations(arrayOfAssociationDesc1);
  }

  public AssociationDesc[] getAssociations()
  {
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; ; i++)
    {
      String str1 = get("mime.types." + i);
      String str2 = get("extensions." + i);
      if ((str1 == null) && (str2 == null))
        break;
      localArrayList.add(new AssociationDesc(str2, str1, null, null));
    }
    return (AssociationDesc[])(AssociationDesc[])localArrayList.toArray(new AssociationDesc[0]);
  }

  public void put(String paramString1, String paramString2)
  {
    synchronized (this)
    {
      if (paramString2 == null)
        this._properties.remove(paramString1);
      else
        this._properties.put(paramString1, paramString2);
      this._dirty = true;
    }
  }

  public String get(String paramString)
  {
    synchronized (this)
    {
      return (String)this._properties.get(paramString);
    }
  }

  public int getInteger(String paramString)
  {
    String str = get(paramString);
    if (str == null)
      return 0;
    int i = 0;
    try
    {
      i = Integer.parseInt(str);
    }
    catch (NumberFormatException localNumberFormatException)
    {
      i = 0;
    }
    return i;
  }

  public boolean getBoolean(String paramString)
  {
    String str = get(paramString);
    if (str == null)
      return false;
    return Boolean.valueOf(str).booleanValue();
  }

  public Date getDate(String paramString)
  {
    String str = get(paramString);
    if (str == null)
      return null;
    try
    {
      return _df.parse(str);
    }
    catch (ParseException localParseException)
    {
    }
    return null;
  }

  public boolean doesNewVersionExist()
  {
    synchronized (this)
    {
      long l = Cache.getLastAccessed(Environment.isSystemCacheMode());
      if (l == 0L)
        return false;
      if (l > this._lastAccessed)
        return true;
    }
    return false;
  }

  public synchronized void store()
    throws IOException
  {
    putLocalApplicationPropertiesStorage(this, this._properties);
    this._dirty = false;
  }

  public void refreshIfNecessary()
  {
    synchronized (this)
    {
      if ((!this._dirty) && (doesNewVersionExist()))
        refresh();
    }
  }

  public void refresh()
  {
    synchronized (this)
    {
      Properties localProperties = getLocalApplicationPropertiesStorage(this);
      this._properties = localProperties;
      this._dirty = false;
    }
  }

  private Properties getLocalApplicationPropertiesStorage(DefaultLocalApplicationProperties paramDefaultLocalApplicationProperties)
  {
    Properties localProperties = new Properties();
    try
    {
      URL localURL = paramDefaultLocalApplicationProperties.getLocation();
      String str1 = paramDefaultLocalApplicationProperties.getVersionId();
      if (localURL != null)
      {
        char c = paramDefaultLocalApplicationProperties.isApplicationDescriptor() ? 'A' : 'E';
        byte[] arrayOfByte = Cache.getLapData(c, localURL, str1, true);
        if (arrayOfByte != null)
        {
          localProperties.load(new ByteArrayInputStream(arrayOfByte));
          String str2 = (String)localProperties.get("locallyInstalled");
          if (str2 != null)
            this._isShortcutInstalledSystem = Boolean.valueOf(str2).booleanValue();
        }
        arrayOfByte = Cache.getLapData(c, localURL, str1, false);
        if (arrayOfByte != null)
          localProperties.load(new ByteArrayInputStream(arrayOfByte));
        this._lastAccessed = System.currentTimeMillis();
      }
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
    return localProperties;
  }

  private void putLocalApplicationPropertiesStorage(DefaultLocalApplicationProperties paramDefaultLocalApplicationProperties, Properties paramProperties)
    throws IOException
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    try
    {
      paramProperties.store(localByteArrayOutputStream, "LAP");
    }
    catch (IOException localIOException)
    {
    }
    localByteArrayOutputStream.close();
    char c = paramDefaultLocalApplicationProperties.isApplicationDescriptor() ? 'A' : 'E';
    Cache.putLapData(c, paramDefaultLocalApplicationProperties.getLocation(), paramDefaultLocalApplicationProperties.getVersionId(), localByteArrayOutputStream.toByteArray());
    this._lastAccessed = System.currentTimeMillis();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.DefaultLocalApplicationProperties
 * JD-Core Version:    0.6.0
 */