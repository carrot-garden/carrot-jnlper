package com.sun.deploy.xml;

public class XMLAttribute
{
  private String _name;
  private String _value;
  private XMLAttribute _next;

  public XMLAttribute(String paramString1, String paramString2)
  {
    this(paramString1, paramString2, null);
  }

  public XMLAttribute(String paramString1, String paramString2, XMLAttribute paramXMLAttribute)
  {
    this._name = XMLNode.stripNameSpace(paramString1);
    this._value = paramString2;
    this._next = paramXMLAttribute;
  }

  public String getName()
  {
    return this._name;
  }

  public String getValue()
  {
    return this._value;
  }

  public XMLAttribute getNext()
  {
    return this._next;
  }

  public void setNext(XMLAttribute paramXMLAttribute)
  {
    this._next = paramXMLAttribute;
  }

  public boolean equals(Object paramObject)
  {
    if ((paramObject == null) || (!(paramObject instanceof XMLAttribute)))
      return false;
    XMLAttribute localXMLAttribute = (XMLAttribute)paramObject;
    return (match(this._name, localXMLAttribute._name)) && (match(this._value, localXMLAttribute._value)) && (match(this._next, localXMLAttribute._next));
  }

  public boolean equalsTemplate(XMLAttribute paramXMLAttribute)
  {
    if (paramXMLAttribute == null)
      return false;
    return (match(this._name, paramXMLAttribute._name)) && (matchTemplateValue(this._value, paramXMLAttribute._value)) && (matchTemplateAttribute(this._next, paramXMLAttribute._next));
  }

  private static boolean match(Object paramObject1, Object paramObject2)
  {
    if (paramObject1 == null)
      return paramObject2 == null;
    return paramObject1.equals(paramObject2);
  }

  private static boolean matchTemplateValue(String paramString1, String paramString2)
  {
    if (paramString1 == null)
      return paramString2 == null;
    return (paramString1.equals(paramString2)) || ("*".equals(paramString2));
  }

  private static boolean matchTemplateAttribute(XMLAttribute paramXMLAttribute1, XMLAttribute paramXMLAttribute2)
  {
    if (paramXMLAttribute1 == null)
      return paramXMLAttribute2 == null;
    return paramXMLAttribute1.equalsTemplate(paramXMLAttribute2);
  }

  public String toString()
  {
    if (this._next != null)
      return this._name + "=\"" + this._value + "\" " + this._next.toString();
    return this._name + "=\"" + this._value + "\"";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.xml.XMLAttribute
 * JD-Core Version:    0.6.0
 */