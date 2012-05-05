package com.sun.deploy.xml;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class XMLEncoding
{
  private static final int BOM_LENGTH = 4;
  private static final int MAX_ENC_NAME = 512;
  private static final int SPACE = 32;
  private static final int TAB = 9;
  private static final int LINEFEED = 10;
  private static final int RETURN = 13;
  private static final int EQUAL = 61;
  private static final int DOUBLE_QUOTE = 34;
  private static final int SINGLE_QUOTE = 39;
  private static final int UTF_32_BE_BOM = 65279;
  private static final int UTF_32_LE_BOM = -131072;
  private static final int UTF_16_BE_BOM = -16842752;
  private static final int UTF_16_LE_BOM = -131072;
  private static final int UTF_8_BOM = -272908544;
  private static final int UNUSUAL_OCTET_1 = 15360;
  private static final int UNUSUAL_OCTET_2 = 3932160;
  private static final int UTF_16BE = 3932223;
  private static final int UTF_16LE = 1006649088;
  private static final int EBCDIC = 1282385812;
  private static final int XML_DECLARATION = 1010792557;
  private static final String UTF_32_ENC = "UTF-32";
  private static final String UTF_16_ENC = "UTF-16";
  private static final String UTF_16BE_ENC = "UTF-16BE";
  private static final String UTF_16LE_ENC = "UTF-16LE";
  private static final String UTF_8_ENC = "UTF-8";
  private static final String IBM037_ENC = "IBM037";
  private static final String XML_DECL_START = "<?xml";
  private static final String ENCODING_DECL = "encoding";

  public static String decodeXML(byte[] paramArrayOfByte)
    throws IOException
  {
    int i = 0;
    String str = null;
    if (paramArrayOfByte.length < 4)
      throw new EOFException(ResourceManager.getMessage("encoding.error.not.xml"));
    int j = 0xFF000000 & paramArrayOfByte[0] << 24 | 0xFF0000 & paramArrayOfByte[1] << 16 | 0xFF00 & paramArrayOfByte[2] << 8 | 0xFF & paramArrayOfByte[3];
    switch (j)
    {
    case 1282385812:
      str = examineEncodingDeclaration(paramArrayOfByte, "IBM037");
      break;
    case 1010792557:
      str = examineEncodingDeclaration(paramArrayOfByte, "UTF-8");
      break;
    case 3932223:
      str = "UTF-16BE";
      Trace.println("Detected UTF-16BE encoding from first four bytes", TraceLevel.BASIC);
      break;
    case 1006649088:
      str = "UTF-16LE";
      Trace.println("Detected UTF-16LE encoding from first four bytes", TraceLevel.BASIC);
      break;
    case 15360:
    case 3932160:
      throw new UnsupportedEncodingException(ResourceManager.getMessage("encoding.error.unusual.octet"));
    case -131072:
    case 65279:
      str = "UTF-32";
      break;
    default:
      int k = j & 0xFFFFFF00;
      switch (k)
      {
      case -272908544:
        i = 3;
        str = "UTF-8";
        break;
      default:
        int m = j & 0xFFFF0000;
        switch (m)
        {
        case -16842752:
        case -131072:
          str = "UTF-16";
          break;
        default:
          str = "UTF-8";
        }
      }
    }
    return new String(paramArrayOfByte, i, paramArrayOfByte.length - i, str);
  }

  private static String examineEncodingDeclaration(byte[] paramArrayOfByte, String paramString)
    throws IOException
  {
    int i = 0;
    int j = 0;
    int k = 0;
    int m = 0;
    int n = 0;
    int i1 = 0;
    int i2 = 0;
    int i3 = -1;
    InputStreamReader localInputStreamReader = null;
    String str = paramString != null ? paramString : "UTF-8";
    localInputStreamReader = new InputStreamReader(new ByteArrayInputStream(paramArrayOfByte), str);
    i3 = localInputStreamReader.read();
    for (int i4 = 0; (i4 < "<?xml".length()) && (n == 0); i4++)
    {
      if (i3 != "<?xml".charAt(i4))
      {
        n = 1;
        break;
      }
      i3 = localInputStreamReader.read();
    }
    i = 1;
    while ((i == 1) && (n == 0))
      switch (i3)
      {
      case 9:
      case 10:
      case 13:
      case 32:
        i3 = localInputStreamReader.read();
        break;
      case -1:
        n = 1;
        break;
      default:
        i = 0;
      }
    i = 1;
    while ((i == 1) && (n == 0))
    {
      if (i3 == -1)
      {
        n = 1;
        break;
      }
      if (j == 1)
      {
        switch (i3)
        {
        case 9:
        case 10:
        case 13:
        case 32:
          break;
        case 61:
          if (k == 0)
          {
            k = 1;
          }
          else
          {
            j = 0;
            n = 1;
          }
          break;
        case 34:
        case 39:
          if (k == 1)
          {
            i = 0;
          }
          else
          {
            j = 0;
            n = 1;
          }
          break;
        default:
          j = 0;
          if (k != 1)
            break;
          n = 1;
        }
        if (j == 0)
        {
          i2 = 0;
          continue;
        }
      }
      else if (i3 == "encoding".charAt(i2++))
      {
        if ("encoding".length() == i2)
          j = 1;
      }
      else if (i3 == 63)
      {
        m = 1;
        i2 = 0;
      }
      else
      {
        if ((i3 == 62) && (m == 1))
        {
          n = 1;
          continue;
        }
        i2 = 0;
      }
      i3 = localInputStreamReader.read();
    }
    if (n == 0)
    {
      StringBuffer localStringBuffer = new StringBuffer(512);
      if ((((i3 >= 97) && (i3 <= 122) ? 1 : 0) | ((i3 >= 65) && (i3 <= 90) ? 1 : 0)) != 0)
      {
        localStringBuffer.append((char)i3);
        i = 1;
        while ((i == 1) && (n == 0))
        {
          i3 = localInputStreamReader.read();
          if (((i3 >= 97) && (i3 <= 122)) || ((i3 >= 65) && (i3 <= 90)) || ((i3 >= 48) && (i3 <= 57)) || (i3 == 95) || (i3 == 46) || (i3 == 45))
          {
            localStringBuffer.append((char)i3);
            continue;
          }
          if ((i3 == 34) || (i3 == 39))
          {
            i1 = 1;
            n = 1;
            str = localStringBuffer.toString();
            continue;
          }
          n = 1;
        }
      }
      n = 1;
    }
    return str;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.xml.XMLEncoding
 * JD-Core Version:    0.6.0
 */