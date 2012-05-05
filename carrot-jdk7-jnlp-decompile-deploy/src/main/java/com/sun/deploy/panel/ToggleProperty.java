package com.sun.deploy.panel;

import com.sun.deploy.config.Config;

final class ToggleProperty extends BasicProperty
{
  public ToggleProperty(String paramString1, String paramString2)
  {
    super(paramString1, paramString2);
    String str = Config.getStringProperty(paramString1);
    if (str != null)
      setValue(str);
  }

  public boolean isSelected()
  {
    return "true".equalsIgnoreCase(getValue());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.ToggleProperty
 * JD-Core Version:    0.6.0
 */