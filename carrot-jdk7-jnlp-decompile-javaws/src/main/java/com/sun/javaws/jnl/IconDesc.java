package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLable;
import java.net.URL;

public class IconDesc
  implements XMLable
{
  private URL _location;
  private String _version;
  private String _locationString;
  private int _height;
  private int _width;
  private int _depth;
  private int _kind;
  private String _suffix;
  public static final int ICON_KIND_DEFAULT = 0;
  public static final int ICON_KIND_SELECTED = 1;
  public static final int ICON_KIND_DISABLED = 2;
  public static final int ICON_KIND_ROLLOVER = 3;
  public static final int ICON_KIND_SPLASH = 4;
  public static final int ICON_KIND_SHORTCUT = 5;
  private static final String[] kinds = { "default", "selected", "disabled", "rollover", "splash", "shortcut" };

  public IconDesc(URL paramURL, String paramString, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this._location = paramURL;
    this._suffix = "";
    if (this._location != null)
    {
      this._locationString = this._location.toExternalForm();
      int i = this._locationString.lastIndexOf(".");
      if (i > 0)
        this._suffix = this._locationString.substring(i);
    }
    this._height = paramInt1;
    this._width = paramInt2;
    this._depth = paramInt3;
    this._kind = paramInt4;
    this._version = paramString;
  }

  public URL getLocation()
  {
    return this._location;
  }

  public String getVersion()
  {
    return this._version;
  }

  public int getHeight()
  {
    return this._height;
  }

  public int getWidth()
  {
    return this._width;
  }

  public int getDepth()
  {
    return this._depth;
  }

  public int getKind()
  {
    return this._kind;
  }

  public String getSuffix()
  {
    return this._suffix;
  }

  public final boolean equals(Object paramObject)
  {
    if (paramObject == this)
      return true;
    if ((paramObject instanceof IconDesc))
    {
      IconDesc localIconDesc = (IconDesc)paramObject;
      if (((this._version == null) && (localIconDesc._version == null)) || ((this._version != null) && (this._version.equals(localIconDesc._version)) && (((this._locationString == null) && (localIconDesc._locationString == null)) || ((this._locationString != null) && (this._locationString.equals(localIconDesc._locationString))))))
        return true;
    }
    return false;
  }

  public int hashCode()
  {
    int i = 0;
    if (this._locationString != null)
      i |= this._locationString.hashCode();
    if (this._version != null)
      i |= this._version.hashCode();
    return i;
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("href", this._location);
    localXMLAttributeBuilder.add("version", this._version);
    localXMLAttributeBuilder.add("height", this._height);
    localXMLAttributeBuilder.add("width", this._width);
    localXMLAttributeBuilder.add("depth", this._depth);
    localXMLAttributeBuilder.add("kind", kinds[this._kind]);
    return new XMLNode("icon", localXMLAttributeBuilder.getAttributeList());
  }

  public String toString()
  {
    return asXML().toString();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.IconDesc
 * JD-Core Version:    0.6.0
 */