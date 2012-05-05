package com.sun.deploy.util;

import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.config.Config;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import java.util.HashMap;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

public final class DeployUIManager
{
  private static final String _v = System.getProperty("java.version");
  private static final boolean _isOldJava = (_v.startsWith("1.2")) || (_v.startsWith("1.3")) || (_v.startsWith("1.4"));

  public static void setLookAndFeel()
  {
    try
    {
      if (_isOldJava)
        MetalLookAndFeel.setCurrentTheme(new DeployMetalTheme());
      HashMap localHashMap = new HashMap(2);
      if (Config.useSystemLookAndFeel())
      {
        localHashMap.put("defaultlaf", getSystemLookAndFeelClassName());
        localHashMap.put("Slider.paintValue", Boolean.FALSE);
      }
      else
      {
        localHashMap.put("defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel");
      }
      ToolkitStore.get().getAppContext().put("swing.lafdata", localHashMap);
    }
    catch (Exception localException)
    {
    }
  }

  public static String getSystemLookAndFeelClassName()
  {
    String str1 = System.getProperty("swing.systemlaf");
    if (str1 != null)
      return str1;
    String str2 = System.getProperty("os.name");
    if (str2 != null)
    {
      if (str2.indexOf("Windows") != -1)
        return "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
      if (str2.startsWith("Mac"))
        return "com.apple.laf.AquaLookAndFeel";
      String str3 = System.getProperty("sun.desktop");
      if ("gnome".equals(str3))
        return "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
      if ((str2.indexOf("Solaris") != -1) || (str2.indexOf("SunOS") != -1))
        return "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    }
    return "javax.swing.plaf.metal.MetalLookAndFeel";
  }

  static class DeployMetalTheme extends DefaultMetalTheme
  {
    private FontUIResource controlTextFont = null;
    private FontUIResource menuTextFont = null;
    private FontUIResource windowTitleFont = null;

    DeployMetalTheme()
    {
      FontUIResource localFontUIResource = super.getControlTextFont();
      this.controlTextFont = new FontUIResource(localFontUIResource.getName(), localFontUIResource.getStyle() & 0xFFFFFFFE, localFontUIResource.getSize());
      localFontUIResource = super.getMenuTextFont();
      this.menuTextFont = new FontUIResource(localFontUIResource.getName(), localFontUIResource.getStyle() & 0xFFFFFFFE, localFontUIResource.getSize());
      localFontUIResource = super.getWindowTitleFont();
      this.windowTitleFont = new FontUIResource(localFontUIResource.getName(), localFontUIResource.getStyle() & 0xFFFFFFFE, localFontUIResource.getSize());
    }

    public FontUIResource getControlTextFont()
    {
      return this.controlTextFont;
    }

    public FontUIResource getMenuTextFont()
    {
      return this.menuTextFont;
    }

    public FontUIResource getWindowTitleFont()
    {
      return this.windowTitleFont;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.DeployUIManager
 * JD-Core Version:    0.6.0
 */