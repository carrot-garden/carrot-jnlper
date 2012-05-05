package com.sun.deploy.xml;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;

public class XMLParser
{
  public XMLNode _root;
  public String _source;
  public String _current;
  public int _tokenType;
  public String _tokenData;
  public static final int TOKEN_EOF = 1;
  public static final int TOKEN_END_TAG = 2;
  public static final int TOKEN_BEGIN_TAG = 3;
  public static final int TOKEN_CLOSE_TAG = 4;
  public static final int TOKEN_EMPTY_CLOSE_TAG = 5;
  public static final int TOKEN_PCDATA = 6;
  private BadTokenException _savedBte;
  public static final GeneralEntity[] REQUIRED_CHARACTER_REFERENCES = { new GeneralEntity("quot", "\""), new GeneralEntity("amp", "&"), new GeneralEntity("apos", "'"), new GeneralEntity("lt", "<"), new GeneralEntity("gt", ">") };
  private static final String CDStart = "<![CDATA[";
  private static final String CDEnd = "]]>";

  public XMLParser(String paramString)
  {
    Trace.println("new XMLParser with source:", TraceLevel.TEMP);
    Trace.println(paramString, TraceLevel.TEMP);
    this._source = paramString;
    this._current = paramString;
    this._root = null;
    this._tokenData = null;
    this._savedBte = null;
  }

  public BadTokenException getSavedException()
  {
    return this._savedBte;
  }

  public XMLNode parse()
    throws BadTokenException
  {
    try
    {
      nextToken(this._current);
      this._root = parseXMLElement();
    }
    catch (NullPointerException localNullPointerException)
    {
      Trace.println("NULL Pointer Exception: " + localNullPointerException, TraceLevel.TEMP);
      throw localNullPointerException;
    }
    catch (BadTokenException localBadTokenException)
    {
      Trace.println("JNLP Parse Exception: " + localBadTokenException, TraceLevel.TEMP);
      throw localBadTokenException;
    }
    if (Trace.isEnabled(TraceLevel.TEMP))
      Trace.println("\n\nreturning ROOT as follows:\n" + this._root, TraceLevel.TEMP);
    return this._root;
  }

  private void nextToken(String paramString)
    throws BadTokenException
  {
    this._current = skipFilling(paramString);
    if ((this._current == null) || (this._current.equals("")))
    {
      this._tokenType = 1;
    }
    else if (this._current.startsWith("<![CDATA["))
    {
      this._tokenType = 6;
      this._current = skipPCData(paramString, '<');
    }
    else if (this._current.startsWith("</"))
    {
      this._tokenType = 2;
      this._current = skipXMLName(skipForward(this._current, 2, 0));
    }
    else if (this._current.startsWith("<"))
    {
      this._tokenType = 3;
      this._current = skipXMLName(skipForward(this._current, 1, 0));
    }
    else if (this._current.startsWith(">"))
    {
      this._tokenType = 4;
      this._current = skipForward(this._current, 1, 0);
    }
    else if (this._current.startsWith("/>"))
    {
      this._tokenType = 5;
      this._current = skipForward(this._current, 2, 0);
    }
    else
    {
      this._tokenType = 6;
      this._current = skipPCData(paramString, '<');
    }
  }

  private String skipPCData(String paramString, char paramChar)
    throws BadTokenException
  {
    int i = paramString.indexOf(paramChar);
    String str1 = null;
    if (i >= 0)
    {
      int j = paramString.indexOf("<![CDATA[");
      if ((j >= 0) && (j <= i))
      {
        String str2 = parseCharacterReferences(paramString.substring(0, j));
        String str3 = paramString.substring(j + "<![CDATA[".length());
        int k = str3.indexOf("]]>");
        if (k >= 0)
        {
          str1 = skipPCData(str3.substring(k + "]]>".length()), paramChar);
          this._tokenData = (str2 + str3.substring(0, k) + this._tokenData);
        }
        else
        {
          this._current = str3;
          throw new BadTokenException("Found the start of a PCDATA element with no end marker.", this._source, getLineNumber());
        }
      }
      else
      {
        this._tokenData = parseCharacterReferences(paramString.substring(0, i));
        str1 = paramString.substring(i);
      }
    }
    else if (paramString.trim().length() != 0)
    {
      this._current = paramString;
      throw new BadTokenException("Failed to find the '" + paramChar + "' charater that marks the end of a CDATA element.", this._source, getLineNumber());
    }
    return str1;
  }

  private XMLNode parseXMLElement()
    throws BadTokenException
  {
    XMLNode localXMLNode1;
    if (this._tokenType == 3)
    {
      String str1 = this._tokenData;
      int i = getLineNumber();
      XMLAttribute localXMLAttribute2 = parseXMLAttribute(this._current);
      XMLAttribute localXMLAttribute1;
      for (Object localObject = localXMLAttribute2; localObject != null; localObject = localXMLAttribute1)
      {
        localXMLAttribute1 = parseXMLAttribute(this._current);
        ((XMLAttribute)localObject).setNext(localXMLAttribute1);
      }
      localXMLNode1 = new XMLNode(str1, localXMLAttribute2);
      nextToken(this._current);
      if ((this._tokenType != 5) && (this._tokenType != 4) && (this._tokenType != 1))
        throw new BadTokenException(this._source, getLineNumber());
      if (this._tokenType == 5)
      {
        nextToken(this._current);
      }
      else if (this._tokenType == 4)
      {
        nextToken(this._current);
        XMLNode localXMLNode2 = parseXMLElement();
        if (localXMLNode2 != null)
        {
          localXMLNode1.setNested(localXMLNode2);
          localXMLNode2.setParent(localXMLNode1);
        }
        if (this._tokenType == 2)
        {
          String str2 = this._tokenData;
          if (!str1.equals(str2))
          {
            if (this._savedBte == null)
              this._savedBte = new BadTokenException("WARNING: <" + str1 + "> tag is not closed correctly", this._source, i);
            Trace.println("<" + str1 + "> tag at line number " + i + " is not closed correctly", TraceLevel.TEMP);
          }
          do
            nextToken(this._current);
          while ((this._tokenType != 1) && (this._tokenType != 4));
          nextToken(this._current);
        }
      }
      if (this._tokenType != 1)
      {
        XMLNode localXMLNode3 = parseXMLElement();
        localXMLNode1.setNext(localXMLNode3);
      }
      return localXMLNode1;
    }
    if (this._tokenType == 6)
    {
      localXMLNode1 = new XMLNode(this._tokenData);
      nextToken(this._current);
      return localXMLNode1;
    }
    return (XMLNode)null;
  }

  private XMLAttribute parseXMLAttribute(String paramString)
    throws BadTokenException
  {
    if (paramString == null)
      return null;
    this._current = skipFilling(paramString);
    if ((this._current == null) || (this._current.startsWith(">")) || (this._current.startsWith("/>")))
      return null;
    this._current = skipAttributeName(this._current);
    String str1 = this._tokenData;
    this._current = skipFilling(this._current);
    if (!this._current.startsWith("="))
    {
      if (paramString.equals(this._current))
        this._current = skipForward(this._current, 1, 0);
      return parseXMLAttribute(this._current);
    }
    this._current = skipForward(this._current, 1, 0);
    this._current = skipWhitespace(this._current);
    String str2;
    if ((this._current.startsWith("\"")) || (this._current.startsWith("'")))
    {
      char c = this._current.charAt(0);
      this._current = skipForward(this._current, 1, 0);
      this._current = skipPCData(this._current, c);
      str2 = this._tokenData;
      this._current = skipForward(this._current, 1, 0);
    }
    else
    {
      this._current = skipNonSpace(this._current);
      str2 = this._tokenData;
    }
    if (str2 != null)
      str2 = str2.trim();
    return new XMLAttribute(str1, str2);
  }

  private String parseCharacterReferences(String paramString)
  {
    String str1 = paramString;
    int i = paramString.indexOf("&");
    if (i >= 0)
    {
      String str2 = paramString.substring(0, i);
      String str3 = "&";
      String str4 = paramString.substring(i + 1);
      int j = paramString.indexOf(";", i);
      if (j > i)
      {
        int k = 0;
        str3 = paramString.substring(i + 1, j);
        str4 = paramString.substring(j + 1);
        if (str3.startsWith("#"))
          try
          {
            int m = 10;
            int i1 = 1;
            char[] arrayOfChar = { '\000' };
            if (str3.startsWith("#x"))
            {
              m = 16;
              i1 = 2;
            }
            arrayOfChar[0] = (char)Integer.parseInt(str3.substring(i1), m);
            str3 = new String(arrayOfChar);
            k = 1;
          }
          catch (NumberFormatException localNumberFormatException)
          {
          }
        else
          for (int n = 0; n < REQUIRED_CHARACTER_REFERENCES.length; n++)
          {
            if (!REQUIRED_CHARACTER_REFERENCES[n].equals(str3))
              continue;
            str3 = REQUIRED_CHARACTER_REFERENCES[n].getValue();
            k = 1;
            break;
          }
        if (k == 0)
        {
          str3 = "&" + str3 + ";";
          Trace.println("Unrecognized character entity reference: " + str3, TraceLevel.BASIC);
        }
      }
      str1 = str2 + str3 + parseCharacterReferences(str4);
    }
    return str1;
  }

  private String skipForward(String paramString, int paramInt1, int paramInt2)
  {
    if ((paramInt1 < 0) || (paramInt1 + paramInt2 >= paramString.length()))
      return null;
    return paramString.substring(paramInt1 + paramInt2);
  }

  private String skipNonSpace(String paramString)
    throws BadTokenException
  {
    int i = 0;
    if (paramString == null)
      return null;
    int j = paramString.length();
    while ((i < j) && (!Character.isWhitespace(paramString.charAt(i))))
      i++;
    return skipPCData(paramString, paramString.charAt(i));
  }

  private String skipWhitespace(String paramString)
  {
    int i = 0;
    if (paramString == null)
      return null;
    while ((i < paramString.length()) && (Character.isWhitespace(paramString.charAt(i))))
      i++;
    return paramString.substring(i);
  }

  private boolean legalTokenStartChar(char paramChar)
  {
    return ((paramChar >= 'a') && (paramChar <= 'z')) || ((paramChar >= 'A') && (paramChar <= 'Z')) || (paramChar == '_') || (paramChar == ':');
  }

  private boolean legalTokenChar(char paramChar)
  {
    return ((paramChar >= 'a') && (paramChar <= 'z')) || ((paramChar >= 'A') && (paramChar <= 'Z')) || ((paramChar >= '0') && (paramChar <= '9')) || (paramChar == '_') || (paramChar == ':') || (paramChar == '.') || (paramChar == '-');
  }

  private String skipAttributeName(String paramString)
  {
    if (paramString == null)
      return null;
    int i = paramString.indexOf("=");
    if (i >= 0)
    {
      this._tokenData = paramString.substring(0, i);
      if (this._tokenData != null)
        this._tokenData = this._tokenData.trim();
    }
    else
    {
      this._tokenData = null;
    }
    return skipForward(paramString, i, 0);
  }

  private String skipXMLName(String paramString)
  {
    int i = 0;
    if (paramString == null)
      return null;
    if (legalTokenStartChar(paramString.charAt(0)))
      for (i = 1; (i < paramString.length()) && (legalTokenChar(paramString.charAt(i))); i++);
    this._tokenData = paramString.substring(0, i);
    if (this._tokenData != null)
      this._tokenData = this._tokenData.trim();
    return skipForward(paramString, i, 0);
  }

  private String skipXMLComment(String paramString)
  {
    if ((paramString != null) && (paramString.startsWith("<!--")))
    {
      int i = paramString.indexOf("-->", 4);
      return skipForward(paramString, i, 3);
    }
    return paramString;
  }

  private String skipXMLDocType(String paramString)
  {
    if ((paramString != null) && (paramString.startsWith("<!")) && (!paramString.startsWith("<![CDATA[")))
    {
      int i = paramString.indexOf(">", 2);
      return skipForward(paramString, i, 1);
    }
    return paramString;
  }

  private String skipXMLProlog(String paramString)
  {
    if ((paramString != null) && (paramString.startsWith("<?")))
    {
      int i = paramString.indexOf("?>", 2);
      return skipForward(paramString, i, 2);
    }
    return paramString;
  }

  private String skipFilling(String paramString)
  {
    String str1 = paramString;
    String str2;
    do
    {
      str2 = str1;
      str1 = skipWhitespace(str1);
      str1 = skipXMLComment(str1);
      str1 = skipXMLDocType(str1);
      str1 = skipXMLProlog(str1);
    }
    while (str1 != str2);
    return str1;
  }

  private int getLineNumber()
  {
    int i;
    if (this._current == null)
      i = this._source.length();
    else
      i = this._source.indexOf(this._current);
    int j = 0;
    int k = 0;
    while ((k < i) && (k != -1))
    {
      k = this._source.indexOf("\n", k);
      if (k < 0)
        continue;
      k++;
      j++;
    }
    return j;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.xml.XMLParser
 * JD-Core Version:    0.6.0
 */