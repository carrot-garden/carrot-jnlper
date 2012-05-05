package com.sun.deploy.panel;

import com.sun.deploy.config.Config;

public class RadioPropertyGroup
{
  private String propertyName;
  private String selectionKey;

  public RadioPropertyGroup(String paramString1, String paramString2)
  {
    this.propertyName = paramString1;
    this.selectionKey = paramString2;
    String str = Config.getStringProperty(this.propertyName);
    if (str != null)
      this.selectionKey = str;
  }

  public void setCurrentSelection(String paramString)
  {
    if (paramString.trim().equals(""))
      return;
    this.selectionKey = paramString;
    Config.setStringProperty(this.propertyName, this.selectionKey);
  }

  public String getCurrentSelection()
  {
    return this.selectionKey;
  }

  public String getPropertyName()
  {
    return this.propertyName;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.RadioPropertyGroup
 * JD-Core Version:    0.6.0
 */