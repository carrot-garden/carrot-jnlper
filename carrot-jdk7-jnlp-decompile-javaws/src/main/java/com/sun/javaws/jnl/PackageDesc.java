package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;

public class PackageDesc
  implements ResourceType
{
  private String _packageName;
  private String _part;
  private boolean _isRecursive;
  private boolean _isExact;

  public PackageDesc(String paramString1, String paramString2, boolean paramBoolean)
  {
    if (paramString1.endsWith(".*"))
    {
      this._packageName = paramString1.substring(0, paramString1.length() - 1);
      this._isExact = false;
    }
    else
    {
      this._isExact = true;
      this._packageName = paramString1;
    }
    this._part = paramString2;
    this._isRecursive = paramBoolean;
  }

  String getPackageName()
  {
    return this._packageName;
  }

  String getPart()
  {
    return this._part;
  }

  boolean isRecursive()
  {
    return this._isRecursive;
  }

  boolean match(String paramString)
  {
    if (this._isExact)
      return this._packageName.equals(paramString);
    if (this._isRecursive)
      return paramString.startsWith(this._packageName);
    int i = paramString.lastIndexOf('.');
    if (i != -1)
      paramString = paramString.substring(0, i + 1);
    return paramString.equals(this._packageName);
  }

  public void visit(ResourceVisitor paramResourceVisitor)
  {
    paramResourceVisitor.visitPackageDesc(this);
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("name", getPackageName());
    localXMLAttributeBuilder.add("part", getPart());
    localXMLAttributeBuilder.add("recursive", isRecursive());
    return new XMLNode("package", localXMLAttributeBuilder.getAttributeList());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.PackageDesc
 * JD-Core Version:    0.6.0
 */