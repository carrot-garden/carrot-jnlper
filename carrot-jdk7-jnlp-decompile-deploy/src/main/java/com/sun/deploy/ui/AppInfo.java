package com.sun.deploy.ui;

import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.cache.AssociationDesc;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import java.net.URL;

public class AppInfo
{
  public static final int TYPE_UNKNOWN = 0;
  public static final int TYPE_APPLICATION = 1;
  public static final int TYPE_APPLET = 2;
  public static final int TYPE_LIBRARY = 3;
  public static final int TYPE_INSTALLER = 4;
  public static final int ICON_SIZE = 48;
  public static final int TITLE_MAX = 40;
  public static final int VENDOR_MAX = 40;
  public static final int URL_MAX = 54;
  private int type = 0;
  private String title = null;
  private String vendor = null;
  private URL from = null;
  private URL iconRef = null;
  private String iconVersion = null;
  private boolean desktopHint = false;
  private boolean menuHint = false;
  private String submenu = null;
  private AssociationDesc[] associations = new AssociationDesc[0];

  public AppInfo()
  {
    this.type = 2;
    Object localObject = ToolkitStore.get().getAppContext().get("deploy.trust.decider.app.name");
    if (localObject != null)
      this.title = localObject.toString();
  }

  public AppInfo(int paramInt, String paramString1, String paramString2, URL paramURL1, URL paramURL2, String paramString3, boolean paramBoolean1, boolean paramBoolean2, String paramString4, AssociationDesc[] paramArrayOfAssociationDesc)
  {
    this.type = paramInt;
    this.title = paramString1;
    this.vendor = paramString2;
    this.from = paramURL1;
    this.iconRef = paramURL2;
    this.iconVersion = paramString3;
    this.desktopHint = paramBoolean1;
    this.menuHint = paramBoolean2;
    this.submenu = paramString4;
    this.associations = paramArrayOfAssociationDesc;
  }

  private String limitString(String paramString, int paramInt)
  {
    if ((paramString != null) && (paramString.length() > paramInt))
      return paramString.substring(0, paramInt - 4) + " ...";
    return paramString;
  }

  public int getType()
  {
    return this.type;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getVendor()
  {
    return this.vendor;
  }

  public URL getFrom()
  {
    return this.from;
  }

  public URL getIconRef()
  {
    return this.iconRef;
  }

  public String getIconVersion()
  {
    return this.iconVersion;
  }

  public boolean getDesktopHint()
  {
    return this.desktopHint;
  }

  public boolean getMenuHint()
  {
    return this.menuHint;
  }

  public String getSubmenu()
  {
    return this.submenu;
  }

  public AssociationDesc[] getAssociations()
  {
    return this.associations;
  }

  public String getDisplayTitle()
  {
    return limitString(this.title, 40);
  }

  public String getDisplayVendor()
  {
    return limitString(this.vendor, 40);
  }

  public String getDisplayFrom()
  {
    String str1 = this.from.toString();
    int i = str1.length();
    if (i <= 54)
      return str1;
    String str2 = "";
    String str3 = str1;
    int j = str1.lastIndexOf("/");
    if ((j > 10) && (j < i))
    {
      str2 = str1.substring(j);
      str3 = str1.substring(0, j);
    }
    while (str3.length() + str2.length() >= 51)
    {
      int k = str3.lastIndexOf("/");
      if (k > 0)
        str3 = str3.substring(0, k);
      else
        return limitString(str1, 54);
    }
    return str3 + "/..." + str2;
  }

  public void setType(int paramInt)
  {
    this.type = paramInt;
  }

  public void setTitle(String paramString)
  {
    this.title = paramString;
  }

  public void setVendor(String paramString)
  {
    this.vendor = paramString;
  }

  public void setFrom(URL paramURL)
  {
    this.from = paramURL;
  }

  public void setIconRef(URL paramURL)
  {
    this.iconRef = paramURL;
  }

  public void setIconVersion(String paramString)
  {
    this.iconVersion = paramString;
  }

  public void setDesktopHint(boolean paramBoolean)
  {
    this.desktopHint = paramBoolean;
  }

  public void setMenuHint(boolean paramBoolean)
  {
    this.menuHint = paramBoolean;
  }

  public void setSubmenu(String paramString)
  {
    this.submenu = paramString;
  }

  public void setAssociations(AssociationDesc[] paramArrayOfAssociationDesc)
  {
    this.associations = paramArrayOfAssociationDesc;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.AppInfo
 * JD-Core Version:    0.6.0
 */