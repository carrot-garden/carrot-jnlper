package com.sun.deploy.cache;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;
import java.net.URL;

public class AssociationDesc
  implements XMLable
{
  private String _extensions;
  private String _mimeType;
  private String _description;
  private URL _icon;

  public AssociationDesc(String paramString1, String paramString2, String paramString3, URL paramURL)
  {
    this._extensions = paramString1;
    this._mimeType = paramString2;
    this._description = paramString3;
    this._icon = paramURL;
  }

  public String getExtensions()
  {
    return this._extensions;
  }

  public String getMimeType()
  {
    return this._mimeType;
  }

  public String getMimeDescription()
  {
    return this._description;
  }

  public URL getIconUrl()
  {
    return this._icon;
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("extensions", this._extensions);
    localXMLAttributeBuilder.add("mime-type", this._mimeType);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("association", localXMLAttributeBuilder.getAttributeList());
    if (this._description != null)
      localXMLNodeBuilder.add(new XMLNode("description", null, new XMLNode(this._description), null));
    if (this._icon != null)
      localXMLNodeBuilder.add(new XMLNode("icon", new XMLAttribute("href", this._icon.toString()), null, null));
    return localXMLNodeBuilder.getNode();
  }

  private XMLNode getIconNode()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("href", this._icon);
    return new XMLNode("icon", localXMLAttributeBuilder.getAttributeList(), null, null);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.AssociationDesc
 * JD-Core Version:    0.6.0
 */