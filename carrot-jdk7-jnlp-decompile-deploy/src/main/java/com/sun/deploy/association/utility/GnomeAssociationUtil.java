package com.sun.deploy.association.utility;

import com.sun.deploy.association.Action;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GnomeAssociationUtil
{
  public static final String GNOME_VFS_MIME_KEY_DESCRIPTION = "description";
  public static final String GNOME_VFS_MIME_KEY_ICON_FILENAME = "icon_filename";

  public static String getMimeTypeByFileExt(String paramString)
  {
    Object localObject = null;
    String[] arrayOfString1 = GnomeVfsWrapper.gnome_vfs_get_registered_mime_types();
    if (arrayOfString1 == null)
      return null;
    for (int i = 0; i < arrayOfString1.length; i++)
    {
      String str = arrayOfString1[i];
      String[] arrayOfString2 = GnomeVfsWrapper.gnome_vfs_mime_get_extensions_list(str);
      if (arrayOfString2 != null)
        for (int j = 0; j < arrayOfString2.length; j++)
        {
          if (!arrayOfString2[j].equals(paramString))
            continue;
          localObject = arrayOfString1[i];
          break;
        }
      if (localObject != null)
        break;
    }
    return localObject;
  }

  public static List getFileExtListByMimeType(String paramString)
  {
    String[] arrayOfString = GnomeVfsWrapper.gnome_vfs_mime_get_extensions_list(paramString);
    if (arrayOfString == null)
      return null;
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < arrayOfString.length; i++)
      localArrayList.add(arrayOfString[i]);
    return localArrayList;
  }

  public static String getIconFileNameByMimeType(String paramString)
  {
    return GnomeVfsWrapper.gnome_vfs_mime_get_icon(paramString);
  }

  public static String getDescriptionByMimeType(String paramString)
  {
    return GnomeVfsWrapper.gnome_vfs_mime_get_description(paramString);
  }

  public static List getActionListByMimeType(String paramString)
  {
    ArrayList localArrayList = new ArrayList();
    Action localAction = null;
    String[] arrayOfString = GnomeVfsWrapper.gnome_vfs_mime_get_key_list(paramString);
    if (arrayOfString != null)
    {
      str = null;
      for (int i = 0; i < arrayOfString.length; i++)
      {
        str = GnomeVfsWrapper.gnome_vfs_mime_get_value(paramString, arrayOfString[i]);
        if (str == null)
          continue;
        localAction = new Action(arrayOfString[i], str);
        localArrayList.add(localAction);
      }
    }
    String str = GnomeVfsWrapper.gnome_vfs_mime_get_default_application_command(paramString);
    if (str != null)
      localArrayList.add(new Action("open", str));
    if (localArrayList.isEmpty())
      return null;
    return localArrayList;
  }

  public static String getMimeTypeByURL(URL paramURL)
  {
    return GnomeVfsWrapper.gnome_vfs_get_mime_type(paramURL.toString());
  }

  public static boolean isMimeTypeExist(String paramString)
  {
    int i = 0;
    String[] arrayOfString = GnomeVfsWrapper.gnome_vfs_get_registered_mime_types();
    if (arrayOfString == null)
      return false;
    for (int j = 0; j < arrayOfString.length; j++)
    {
      if (!paramString.equals(arrayOfString[j]))
        continue;
      i = 1;
      break;
    }
    return i;
  }

  public static boolean isFileExtExist(String paramString)
  {
    return getMimeTypeByFileExt(paramString) != null;
  }

  public static String getEnv(String paramString)
  {
    return GnomeVfsWrapper.getenv(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.GnomeAssociationUtil
 * JD-Core Version:    0.6.0
 */