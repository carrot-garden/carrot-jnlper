package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;

public class ApplicationDesc
  implements XMLable
{
  private String _mainClass;
  private String _progressClass;
  private String[] _arguments;

  public ApplicationDesc(String paramString1, String paramString2, String[] paramArrayOfString)
  {
    this._mainClass = paramString1;
    this._progressClass = paramString2;
    this._arguments = paramArrayOfString;
  }

  public String getMainClass()
  {
    return this._mainClass;
  }

  public String getProgressClass()
  {
    return this._progressClass;
  }

  public String[] getArguments()
  {
    return this._arguments;
  }

  public void setArguments(String[] paramArrayOfString)
  {
    this._arguments = paramArrayOfString;
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("main-class", this._mainClass);
    localXMLAttributeBuilder.add("progress-class", this._progressClass);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("application-desc", localXMLAttributeBuilder.getAttributeList());
    if (this._arguments != null)
      for (int i = 0; i < this._arguments.length; i++)
        localXMLNodeBuilder.add(new XMLNode("argument", null, new XMLNode(this._arguments[i]), null));
    return localXMLNodeBuilder.getNode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.ApplicationDesc
 * JD-Core Version:    0.6.0
 */