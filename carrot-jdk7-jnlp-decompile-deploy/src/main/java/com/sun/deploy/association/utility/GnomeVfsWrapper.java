package com.sun.deploy.association.utility;

import com.sun.deploy.config.Platform;

public class GnomeVfsWrapper
{
  public static final String GNOME_VFS_MIME_KEY_DESCRIPTION = "description";
  public static final String GNOME_VFS_MIME_DEFAULT_KEY_ICON_FILENAME = "icon_filename";

  public static native boolean openGNOMELibrary();

  public static native void closeGNOMELibrary();

  public static native boolean initGNOMELibrary();

  public static native String gnome_vfs_get_mime_type(String paramString);

  public static native String gnome_vfs_mime_get_description(String paramString);

  public static native String gnome_vfs_mime_get_icon(String paramString);

  public static native String[] gnome_vfs_mime_get_key_list(String paramString);

  public static native String gnome_vfs_mime_get_value(String paramString1, String paramString2);

  public static native String gnome_vfs_mime_get_default_application_command(String paramString);

  public static native String[] gnome_vfs_get_registered_mime_types();

  public static native String[] gnome_vfs_mime_get_extensions_list(String paramString);

  public static native String getenv(String paramString);

  public static native String getVersion();

  static
  {
    Platform.get().loadDeployNativeLib();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.GnomeVfsWrapper
 * JD-Core Version:    0.6.0
 */