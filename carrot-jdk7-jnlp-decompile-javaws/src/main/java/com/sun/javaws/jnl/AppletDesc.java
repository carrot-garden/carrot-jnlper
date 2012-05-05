package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public class AppletDesc
  implements XMLable
{
  private String _name;
  private String _appletClass;
  private String _progressClass;
  private URL _documentBase;
  private int _width;
  private int _height;
  private Properties _params;

  public AppletDesc(String paramString1, String paramString2, URL paramURL, int paramInt1, int paramInt2, Properties paramProperties, String paramString3)
  {
    this._name = paramString1;
    this._appletClass = paramString2;
    this._progressClass = paramString3;
    this._documentBase = paramURL;
    this._width = paramInt1;
    this._height = paramInt2;
    this._params = paramProperties;
  }

  public String getName()
  {
    return this._name;
  }

  public String getAppletClass()
  {
    return this._appletClass;
  }

  public String getProgressClass()
  {
    return this._progressClass;
  }

  public URL getDocumentBase()
  {
    return this._documentBase;
  }

  public int getWidth()
  {
    return this._width;
  }

  public int getHeight()
  {
    return this._height;
  }

  public Properties getParameters()
  {
    return this._params;
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("name", this._name);
    localXMLAttributeBuilder.add("main-class", this._appletClass);
    localXMLAttributeBuilder.add("progress-class", this._progressClass);
    localXMLAttributeBuilder.add("documentbase", this._documentBase);
    localXMLAttributeBuilder.add("width", this._width);
    localXMLAttributeBuilder.add("height", this._height);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("applet-desc", localXMLAttributeBuilder.getAttributeList());
    if (this._params != null)
    {
      Enumeration localEnumeration = this._params.keys();
      while (localEnumeration.hasMoreElements())
      {
        String str1 = (String)localEnumeration.nextElement();
        String str2 = this._params.getProperty(str1);
        localXMLNodeBuilder.add(new XMLNode("param", new XMLAttribute("name", str1, new XMLAttribute("value", str2)), null, null));
      }
    }
    return localXMLNodeBuilder.getNode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.AppletDesc
 * JD-Core Version:    0.6.0
 */