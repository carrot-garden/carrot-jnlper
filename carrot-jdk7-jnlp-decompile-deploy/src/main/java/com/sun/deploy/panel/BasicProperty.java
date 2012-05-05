package com.sun.deploy.panel;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;

public abstract class BasicProperty
  implements IProperty
{
  private String sName;
  private String sValue;
  private String propertyName;
  private String tooltipStr;

  public BasicProperty(String paramString1, String paramString2)
  {
    this.sName = ResourceManager.getMessage(paramString1);
    this.propertyName = paramString1;
    this.sValue = paramString2;
    String str = ResourceManager.getMessage(paramString1 + ".tooltip");
    if (!str.equalsIgnoreCase(paramString1 + ".tooltip"))
      this.tooltipStr = str;
    else
      this.tooltipStr = this.sName;
  }

  public String getDescription()
  {
    return this.sName;
  }

  public String getPropertyName()
  {
    return this.propertyName;
  }

  public String getTooltip()
  {
    return this.tooltipStr;
  }

  public String getValue()
  {
    return this.sValue;
  }

  public void setValue(String paramString)
  {
    this.sValue = paramString;
    Config.setStringProperty(this.propertyName, paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.BasicProperty
 * JD-Core Version:    0.6.0
 */