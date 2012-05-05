package com.sun.javaws.jnl;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.xml.XMLNode;
import com.sun.javaws.exceptions.BadFieldException;
import com.sun.javaws.exceptions.MissingFieldException;
import java.net.MalformedURLException;
import java.net.URL;

public class XMLUtils
{
  public static int getIntAttribute(String paramString1, XMLNode paramXMLNode, String paramString2, String paramString3, int paramInt)
    throws BadFieldException
  {
    String str = getAttribute(paramXMLNode, paramString2, paramString3);
    if (str == null)
      return paramInt;
    try
    {
      return Integer.parseInt(str);
    }
    catch (NumberFormatException localNumberFormatException)
    {
    }
    throw new BadFieldException(paramString1, getPathString(paramXMLNode) + paramString2 + paramString3, str);
  }

  public static int getRequiredIntAttribute(String paramString1, XMLNode paramXMLNode, String paramString2, String paramString3)
    throws BadFieldException, MissingFieldException
  {
    String str = getAttribute(paramXMLNode, paramString2, paramString3);
    if (str == null)
      throw new MissingFieldException(paramString1, getPathString(paramXMLNode) + paramString2 + paramString3);
    try
    {
      return Integer.parseInt(str);
    }
    catch (NumberFormatException localNumberFormatException)
    {
    }
    throw new BadFieldException(paramString1, getPathString(paramXMLNode) + paramString2 + paramString3, str);
  }

  public static String getAttribute(XMLNode paramXMLNode, String paramString1, String paramString2)
  {
    return getAttribute(paramXMLNode, paramString1, paramString2, null);
  }

  public static String getRequiredAttributeEmptyOK(String paramString1, XMLNode paramXMLNode, String paramString2, String paramString3)
    throws MissingFieldException
  {
    String str = null;
    XMLNode localXMLNode = findElementPath(paramXMLNode, paramString2);
    if (localXMLNode != null)
      str = localXMLNode.getAttribute(paramString3);
    if (str == null)
      throw new MissingFieldException(paramString1, getPathString(paramXMLNode) + paramString2 + paramString3);
    return str;
  }

  public static String getClassName(String paramString1, XMLNode paramXMLNode, String paramString2, String paramString3, boolean paramBoolean)
    throws BadFieldException, MissingFieldException
  {
    String str1 = null;
    if (paramBoolean)
      str1 = getRequiredAttribute(paramString1, paramXMLNode, paramString2, paramString3);
    else
      str1 = getAttribute(paramXMLNode, paramString2, paramString3);
    if ((str1 != null) && (str1.endsWith(".class")))
    {
      int i = str1.lastIndexOf(".class");
      String str2 = str1.substring(0, i);
      Trace.println("invalid class cname in jnlp file: " + str1 + " - using " + str2 + " instead", TraceLevel.BASIC);
      return str2;
    }
    return str1;
  }

  public static String getRequiredAttribute(String paramString1, XMLNode paramXMLNode, String paramString2, String paramString3)
    throws MissingFieldException
  {
    String str = getAttribute(paramXMLNode, paramString2, paramString3, null);
    if (str == null)
      throw new MissingFieldException(paramString1, getPathString(paramXMLNode) + paramString2 + paramString3);
    str = str.trim();
    return str.length() == 0 ? null : str;
  }

  public static String getAttribute(XMLNode paramXMLNode, String paramString1, String paramString2, String paramString3)
  {
    XMLNode localXMLNode = findElementPath(paramXMLNode, paramString1);
    if (localXMLNode == null)
      return paramString3;
    String str = localXMLNode.getAttribute(paramString2);
    return (str == null) || (str.length() == 0) ? paramString3 : str;
  }

  public static URL getAttributeURL(String paramString1, URL paramURL, XMLNode paramXMLNode, String paramString2, String paramString3)
    throws BadFieldException
  {
    String str1 = getAttribute(paramXMLNode, paramString2, paramString3);
    if (str1 == null)
      return null;
    try
    {
      if (str1.startsWith("jar:"))
      {
        int i = str1.indexOf("!/");
        if (i > 0)
        {
          String str2 = str1.substring(i);
          String str3 = str1.substring(4, i);
          URL localURL = paramURL == null ? new URL(str3) : new URL(paramURL, str3);
          return new URL("jar:" + localURL.toString() + str2);
        }
      }
      return paramURL == null ? new URL(str1) : new URL(paramURL, str1);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      if (localMalformedURLException.getMessage().indexOf("https") != -1)
        throw new BadFieldException(paramString1, "<jnlp>", "https");
    }
    throw new BadFieldException(paramString1, getPathString(paramXMLNode) + paramString2 + paramString3, str1);
  }

  public static URL getAttributeURL(String paramString1, XMLNode paramXMLNode, String paramString2, String paramString3)
    throws BadFieldException
  {
    return getAttributeURL(paramString1, null, paramXMLNode, paramString2, paramString3);
  }

  public static URL getRequiredURL(String paramString1, URL paramURL, XMLNode paramXMLNode, String paramString2, String paramString3)
    throws BadFieldException, MissingFieldException
  {
    URL localURL = getAttributeURL(paramString1, paramURL, paramXMLNode, paramString2, paramString3);
    if (localURL == null)
      throw new MissingFieldException(paramString1, getPathString(paramXMLNode) + paramString2 + paramString3);
    return localURL;
  }

  public static URL getRequiredURL(String paramString1, XMLNode paramXMLNode, String paramString2, String paramString3)
    throws BadFieldException, MissingFieldException
  {
    return getRequiredURL(paramString1, null, paramXMLNode, paramString2, paramString3);
  }

  public static boolean isElementPath(XMLNode paramXMLNode, String paramString)
  {
    return findElementPath(paramXMLNode, paramString) != null;
  }

  public static URL getElementURL(String paramString1, XMLNode paramXMLNode, String paramString2)
    throws BadFieldException
  {
    String str = getElementContents(paramXMLNode, paramString2);
    try
    {
      return new URL(str);
    }
    catch (MalformedURLException localMalformedURLException)
    {
    }
    throw new BadFieldException(paramString1, getPathString(paramXMLNode) + paramString2, str);
  }

  public static String getPathString(XMLNode paramXMLNode)
  {
    return getPathString(paramXMLNode.getParent()) + "<" + paramXMLNode.getName() + ">";
  }

  public static String getElementContentsWithAttribute(XMLNode paramXMLNode, String paramString1, String paramString2, String paramString3, String paramString4)
    throws BadFieldException, MissingFieldException
  {
    XMLNode localXMLNode = getElementWithAttribute(paramXMLNode, paramString1, paramString2, paramString3);
    if (localXMLNode == null)
      return paramString4;
    return getElementContents(localXMLNode, "", paramString4);
  }

  public static URL getAttributeURLWithAttribute(String paramString1, XMLNode paramXMLNode, String paramString2, String paramString3, String paramString4, String paramString5, URL paramURL)
    throws BadFieldException, MissingFieldException
  {
    XMLNode localXMLNode = getElementWithAttribute(paramXMLNode, paramString2, paramString3, paramString4);
    if (localXMLNode == null)
      return paramURL;
    URL localURL = getAttributeURL(paramString1, localXMLNode, "", paramString5);
    if (localURL == null)
      return paramURL;
    return localURL;
  }

  public static XMLNode getElementWithAttribute(XMLNode paramXMLNode, String paramString1, String paramString2, String paramString3)
    throws BadFieldException, MissingFieldException
  {
    XMLNode[] arrayOfXMLNode = { null };
    visitElements(paramXMLNode, paramString1, new ElementVisitor(arrayOfXMLNode, paramString2, paramString3)
    {
      private final XMLNode[] val$result;
      private final String val$attr;
      private final String val$val;

      public void visitElement(XMLNode paramXMLNode)
        throws BadFieldException, MissingFieldException
      {
        if ((this.val$result[0] == null) && (paramXMLNode.getAttribute(this.val$attr).equals(this.val$val)))
          this.val$result[0] = paramXMLNode;
      }
    });
    return arrayOfXMLNode[0];
  }

  public static String getElementContents(XMLNode paramXMLNode, String paramString)
  {
    return getElementContents(paramXMLNode, paramString, null);
  }

  public static String getElementContents(XMLNode paramXMLNode, String paramString1, String paramString2)
  {
    XMLNode localXMLNode1 = findElementPath(paramXMLNode, paramString1);
    if (localXMLNode1 == null)
      return paramString2;
    XMLNode localXMLNode2 = localXMLNode1.getNested();
    if ((localXMLNode2 != null) && (!localXMLNode2.isElement()))
      return localXMLNode2.getName();
    return paramString2;
  }

  public static XMLNode findElementPath(XMLNode paramXMLNode, String paramString)
  {
    if (paramXMLNode == null)
      return null;
    if ((paramString == null) || (paramString.length() == 0))
      return paramXMLNode;
    int i = paramString.indexOf('>');
    if (paramString.charAt(0) != '<')
      throw new IllegalArgumentException("bad path. Missing begin tag");
    if (i == -1)
      throw new IllegalArgumentException("bad path. Missing end tag");
    String str1 = paramString.substring(1, i);
    String str2 = paramString.substring(i + 1);
    return findElementPath(findChildElement(paramXMLNode, str1), str2);
  }

  public static XMLNode findChildElement(XMLNode paramXMLNode, String paramString)
  {
    for (XMLNode localXMLNode = paramXMLNode.getNested(); localXMLNode != null; localXMLNode = localXMLNode.getNext())
      if ((localXMLNode.isElement()) && (localXMLNode.getName().equals(paramString)))
        return localXMLNode;
    return null;
  }

  public static void visitElements(XMLNode paramXMLNode, String paramString, ElementVisitor paramElementVisitor)
    throws BadFieldException, MissingFieldException
  {
    int i = paramString.lastIndexOf('<');
    if (i == -1)
      throw new IllegalArgumentException("bad path. Must contain atleast one tag");
    if ((paramString.length() == 0) || (paramString.charAt(paramString.length() - 1) != '>'))
      throw new IllegalArgumentException("bad path. Must end with a >");
    String str1 = paramString.substring(0, i);
    String str2 = paramString.substring(i + 1, paramString.length() - 1);
    XMLNode localXMLNode1 = findElementPath(paramXMLNode, str1);
    if (localXMLNode1 == null)
      return;
    for (XMLNode localXMLNode2 = localXMLNode1.getNested(); localXMLNode2 != null; localXMLNode2 = localXMLNode2.getNext())
    {
      if ((!localXMLNode2.isElement()) || (!localXMLNode2.getName().equals(str2)))
        continue;
      paramElementVisitor.visitElement(localXMLNode2);
    }
  }

  public static void visitChildrenElements(XMLNode paramXMLNode, ElementVisitor paramElementVisitor)
    throws BadFieldException, MissingFieldException
  {
    for (XMLNode localXMLNode = paramXMLNode.getNested(); localXMLNode != null; localXMLNode = localXMLNode.getNext())
    {
      if (!localXMLNode.isElement())
        continue;
      paramElementVisitor.visitElement(localXMLNode);
    }
  }

  public static abstract class ElementVisitor
  {
    public abstract void visitElement(XMLNode paramXMLNode)
      throws BadFieldException, MissingFieldException;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.XMLUtils
 * JD-Core Version:    0.6.0
 */