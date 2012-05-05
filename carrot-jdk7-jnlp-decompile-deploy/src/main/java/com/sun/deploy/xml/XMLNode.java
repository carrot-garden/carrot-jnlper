package com.sun.deploy.xml;

import java.io.PrintWriter;
import java.io.StringWriter;

public class XMLNode
{
  private boolean _isElement;
  private String _name;
  private XMLAttribute _attr;
  private XMLNode _parent;
  private XMLNode _nested;
  private XMLNode _next;
  public static final String WILDCARD = "*";

  public XMLNode(String paramString)
  {
    this._isElement = false;
    this._name = paramString;
    this._attr = null;
    this._nested = null;
    this._next = null;
    this._parent = null;
  }

  public XMLNode(String paramString, XMLAttribute paramXMLAttribute)
  {
    this(paramString, paramXMLAttribute, null, null);
  }

  public XMLNode(String paramString, XMLAttribute paramXMLAttribute, XMLNode paramXMLNode1, XMLNode paramXMLNode2)
  {
    this._isElement = true;
    this._name = stripNameSpace(paramString);
    this._attr = paramXMLAttribute;
    this._nested = paramXMLNode1;
    this._next = paramXMLNode2;
    this._parent = null;
  }

  public String getName()
  {
    return this._name;
  }

  public XMLAttribute getAttributes()
  {
    return this._attr;
  }

  public XMLNode getNested()
  {
    return this._nested;
  }

  public XMLNode getNext()
  {
    return this._next;
  }

  public boolean isElement()
  {
    return this._isElement;
  }

  public void setParent(XMLNode paramXMLNode)
  {
    this._parent = paramXMLNode;
  }

  public XMLNode getParent()
  {
    return this._parent;
  }

  public void setNext(XMLNode paramXMLNode)
  {
    this._next = paramXMLNode;
  }

  public void setNested(XMLNode paramXMLNode)
  {
    this._nested = paramXMLNode;
  }

  public static String stripNameSpace(String paramString)
  {
    if ((paramString != null) && (!paramString.startsWith("xmlns:")))
    {
      int i = paramString.lastIndexOf(":");
      if ((i >= 0) && (i < paramString.length()))
        return paramString.substring(i + 1);
    }
    return paramString;
  }

  public boolean equalsTemplate(Object paramObject)
  {
    if ((paramObject == null) || (!(paramObject instanceof XMLNode)))
      return false;
    XMLNode localXMLNode = (XMLNode)paramObject;
    int i = (matchTemplateName(localXMLNode._name)) && (matchTemplateAttribute(this._attr, localXMLNode._attr)) && (matchTemplateNode(this._nested, localXMLNode._nested)) && (matchTemplateNode(this._next, localXMLNode._next)) ? 1 : 0;
    return i;
  }

  public boolean equals(Object paramObject)
  {
    if ((paramObject == null) || (!(paramObject instanceof XMLNode)))
      return false;
    XMLNode localXMLNode = (XMLNode)paramObject;
    int i = (match(this._name, localXMLNode._name)) && (match(this._attr, localXMLNode._attr)) && (match(this._nested, localXMLNode._nested)) && (match(this._next, localXMLNode._next)) ? 1 : 0;
    return i;
  }

  public String getAttribute(String paramString)
  {
    for (XMLAttribute localXMLAttribute = this._attr; localXMLAttribute != null; localXMLAttribute = localXMLAttribute.getNext())
      if (paramString.equals(localXMLAttribute.getName()))
        return localXMLAttribute.getValue();
    return "";
  }

  private static boolean match(Object paramObject1, Object paramObject2)
  {
    if (paramObject1 == null)
      return paramObject2 == null;
    return paramObject1.equals(paramObject2);
  }

  private boolean matchTemplateName(String paramString)
  {
    if (this._name == null)
      return paramString == null;
    if (this._name.equals(paramString))
      return true;
    return (!this._isElement) && (paramString.equals("*"));
  }

  private static boolean matchTemplateNode(XMLNode paramXMLNode1, XMLNode paramXMLNode2)
  {
    if (paramXMLNode1 == null)
      return paramXMLNode2 == null;
    return paramXMLNode1.equalsTemplate(paramXMLNode2);
  }

  private static boolean matchTemplateAttribute(XMLAttribute paramXMLAttribute1, XMLAttribute paramXMLAttribute2)
  {
    if (paramXMLAttribute1 == null)
      return paramXMLAttribute2 == null;
    return paramXMLAttribute1.equalsTemplate(paramXMLAttribute2);
  }

  public void printToStream(PrintWriter paramPrintWriter)
  {
    printToStream(paramPrintWriter, false);
  }

  public void printToStream(PrintWriter paramPrintWriter, boolean paramBoolean)
  {
    printToStream(paramPrintWriter, 0, paramBoolean);
  }

  public void printToStream(PrintWriter paramPrintWriter, int paramInt, boolean paramBoolean)
  {
    String str;
    if (!isElement())
    {
      str = this._name;
      if ((paramBoolean) && (str.length() > 512))
        str = "...";
      paramPrintWriter.print(str);
    }
    else if (this._nested == null)
    {
      str = " " + this._attr.toString();
      lineln(paramPrintWriter, paramInt, "<" + this._name + str + "/>");
    }
    else
    {
      str = " " + this._attr.toString();
      lineln(paramPrintWriter, paramInt, "<" + this._name + str + ">");
      this._nested.printToStream(paramPrintWriter, paramInt + 1, paramBoolean);
      if (this._nested.isElement())
        lineln(paramPrintWriter, paramInt, "</" + this._name + ">");
      else
        paramPrintWriter.print("</" + this._name + ">");
    }
    if (this._next != null)
      this._next.printToStream(paramPrintWriter, paramInt, paramBoolean);
  }

  private static void lineln(PrintWriter paramPrintWriter, int paramInt, String paramString)
  {
    paramPrintWriter.println("");
    for (int i = 0; i < paramInt; i++)
      paramPrintWriter.print("  ");
    paramPrintWriter.print(paramString);
  }

  public String toString()
  {
    return toString(false);
  }

  public String toString(boolean paramBoolean)
  {
    StringWriter localStringWriter = new StringWriter(1000);
    PrintWriter localPrintWriter = new PrintWriter(localStringWriter);
    printToStream(localPrintWriter, paramBoolean);
    localPrintWriter.close();
    return localStringWriter.toString();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.xml.XMLNode
 * JD-Core Version:    0.6.0
 */