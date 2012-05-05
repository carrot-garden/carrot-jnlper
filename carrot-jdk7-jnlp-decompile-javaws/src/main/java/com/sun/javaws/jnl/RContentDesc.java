package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;
import java.net.URL;

public class RContentDesc
  implements XMLable
{
  private URL _href;
  private String _title;
  private String _description;
  private URL _icon;
  private boolean _isApplication;

  public RContentDesc(URL paramURL1, String paramString1, String paramString2, URL paramURL2)
  {
    this._href = paramURL1;
    this._title = paramString1;
    this._description = paramString2;
    this._icon = paramURL2;
    this._isApplication = ((paramURL1 != null) && (paramURL1.toString().endsWith(".jnlp")));
  }

  public URL getHref()
  {
    return this._href;
  }

  public URL getIcon()
  {
    return this._icon;
  }

  public String getTitle()
  {
    if (this._title == null)
    {
      String str = this._href.getPath();
      return str.substring(str.lastIndexOf('/') + 1);
    }
    return this._title;
  }

  public String getDescription()
  {
    return this._description;
  }

  public boolean isApplication()
  {
    return this._isApplication;
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("href", this._href);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("related-content", localXMLAttributeBuilder.getAttributeList());
    if (this._title != null)
      localXMLNodeBuilder.add("title", this._title);
    if (this._description != null)
      localXMLNodeBuilder.add("description", this._description);
    if (this._icon != null)
      localXMLNodeBuilder.add(new XMLNode("icon", new XMLAttribute("href", this._icon.toString())));
    return localXMLNodeBuilder.getNode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.RContentDesc
 * JD-Core Version:    0.6.0
 */