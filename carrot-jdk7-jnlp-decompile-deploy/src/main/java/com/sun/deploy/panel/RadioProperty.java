package com.sun.deploy.panel;

final class RadioProperty extends BasicProperty
{
  private RadioPropertyGroup group;

  public RadioProperty(String paramString1, String paramString2)
  {
    super(paramString1 + "." + paramString2, paramString2);
  }

  public void setGroup(RadioPropertyGroup paramRadioPropertyGroup)
  {
    this.group = paramRadioPropertyGroup;
  }

  public boolean isSelected()
  {
    return this.group.getCurrentSelection().equalsIgnoreCase(getValue());
  }

  public void setValue(String paramString)
  {
    this.group.setCurrentSelection(getValue());
  }

  public String getGroupName()
  {
    return this.group.getPropertyName();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.RadioProperty
 * JD-Core Version:    0.6.0
 */