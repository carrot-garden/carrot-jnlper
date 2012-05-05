package com.sun.deploy.xml;

import java.net.URL;

public class XMLAttributeBuilder
{
  private XMLAttribute _root = null;
  private XMLAttribute _next;

  public void add(XMLAttribute paramXMLAttribute)
  {
    if (paramXMLAttribute != null)
      if (this._next == null)
      {
        this._root = (this._next = paramXMLAttribute);
        paramXMLAttribute.setNext(null);
      }
      else
      {
        this._next.setNext(paramXMLAttribute);
        this._next = paramXMLAttribute;
        paramXMLAttribute.setNext(null);
      }
  }

  public void add(String paramString1, String paramString2)
  {
    if ((paramString2 != null) && (paramString2.length() > 0))
      add(new XMLAttribute(paramString1, paramString2));
  }

  public void add(String paramString, URL paramURL)
  {
    if (paramURL != null)
      add(new XMLAttribute(paramString, paramURL.toString()));
  }

  public void add(String paramString, long paramLong)
  {
    if (paramLong != 0L)
      add(new XMLAttribute(paramString, new Long(paramLong).toString()));
  }

  public void add(String paramString, boolean paramBoolean)
  {
    add(new XMLAttribute(paramString, paramBoolean ? "true" : "false"));
  }

  public XMLAttribute getAttributeList()
  {
    return this._root;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.xml.XMLAttributeBuilder
 * JD-Core Version:    0.6.0
 */