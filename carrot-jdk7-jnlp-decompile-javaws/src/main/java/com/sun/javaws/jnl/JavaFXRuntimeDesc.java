package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import java.net.URL;

public class JavaFXRuntimeDesc
  implements ResourceType
{
  private URL _href;
  private String _vs;

  public JavaFXRuntimeDesc(String paramString, URL paramURL)
  {
    this._vs = paramString;
    this._href = paramURL;
  }

  public String getVersion()
  {
    return this._vs;
  }

  public URL getDownloadURL()
  {
    return this._href;
  }

  public void visit(ResourceVisitor paramResourceVisitor)
  {
    paramResourceVisitor.visitJFXDesc(this);
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("version", this._vs);
    localXMLAttributeBuilder.add("href", this._href);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("javafx-runtime", localXMLAttributeBuilder.getAttributeList());
    return localXMLNodeBuilder.getNode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.JavaFXRuntimeDesc
 * JD-Core Version:    0.6.0
 */