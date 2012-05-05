package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;

public class InstallerDesc
  implements XMLable
{
  private String _mainClass;

  public InstallerDesc(String paramString)
  {
    this._mainClass = paramString;
  }

  public String getMainClass()
  {
    return this._mainClass;
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("main-class", this._mainClass);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("installer-desc", localXMLAttributeBuilder.getAttributeList());
    return localXMLNodeBuilder.getNode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.InstallerDesc
 * JD-Core Version:    0.6.0
 */