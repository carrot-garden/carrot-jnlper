package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLNode;

public class PropertyDesc
  implements ResourceType
{
  private String _key;
  private String _value;

  public PropertyDesc(String paramString1, String paramString2)
  {
    this._key = paramString1;
    this._value = paramString2;
  }

  public String getKey()
  {
    return this._key;
  }

  public String getValue()
  {
    return this._value;
  }

  public void visit(ResourceVisitor paramResourceVisitor)
  {
    paramResourceVisitor.visitPropertyDesc(this);
  }

  public XMLNode asXML()
  {
    return new XMLNode("property", new XMLAttribute("name", getKey(), new XMLAttribute("value", getValue())), null, null);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.PropertyDesc
 * JD-Core Version:    0.6.0
 */