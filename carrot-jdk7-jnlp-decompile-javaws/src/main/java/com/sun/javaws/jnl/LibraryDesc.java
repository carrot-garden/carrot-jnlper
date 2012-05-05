package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;

public class LibraryDesc
  implements XMLable
{
  String _progressClass;

  public LibraryDesc(String paramString)
  {
    this._progressClass = paramString;
  }

  public String getProgressClass()
  {
    return this._progressClass;
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("progress-class", this._progressClass);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("component-desc", localXMLAttributeBuilder.getAttributeList());
    return localXMLNodeBuilder.getNode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.LibraryDesc
 * JD-Core Version:    0.6.0
 */