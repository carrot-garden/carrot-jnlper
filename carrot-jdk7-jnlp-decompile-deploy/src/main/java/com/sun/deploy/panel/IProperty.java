package com.sun.deploy.panel;

public abstract interface IProperty
{
  public abstract String getDescription();

  public abstract String getPropertyName();

  public abstract String getTooltip();

  public abstract String getValue();

  public abstract void setValue(String paramString);

  public abstract boolean isSelected();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.IProperty
 * JD-Core Version:    0.6.0
 */