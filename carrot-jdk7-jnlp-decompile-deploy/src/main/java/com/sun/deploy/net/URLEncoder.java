package com.sun.deploy.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

public final class URLEncoder
{
  private static BitSet dontNeedEncoding = new BitSet(256);
  private static final int caseDiff = 32;

  public static String encode(String paramString1, String paramString2)
    throws UnsupportedEncodingException
  {
    int i = 0;
    int j = 0;
    int k = 10;
    StringBuffer localStringBuffer = new StringBuffer(paramString1.length());
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(k);
    OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(localByteArrayOutputStream, paramString2);
    for (int m = 0; m < paramString1.length(); m++)
    {
      int n = paramString1.charAt(m);
      if (dontNeedEncoding.get(n))
      {
        if (n == 32)
        {
          n = 43;
          i = 1;
        }
        localStringBuffer.append((char)n);
        j = 1;
      }
      else
      {
        try
        {
          if (j != 0)
          {
            localOutputStreamWriter = new OutputStreamWriter(localByteArrayOutputStream, paramString2);
            j = 0;
          }
          localOutputStreamWriter.write(n);
          if ((n >= 55296) && (n <= 56319) && (m + 1 < paramString1.length()))
          {
            int i1 = paramString1.charAt(m + 1);
            if ((i1 >= 56320) && (i1 <= 57343))
            {
              localOutputStreamWriter.write(i1);
              m++;
            }
          }
          localOutputStreamWriter.flush();
        }
        catch (IOException localIOException)
        {
          localByteArrayOutputStream.reset();
          continue;
        }
        byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
        for (int i2 = 0; i2 < arrayOfByte.length; i2++)
        {
          localStringBuffer.append('%');
          char c = Character.forDigit(arrayOfByte[i2] >> 4 & 0xF, 16);
          if (Character.isLetter(c))
            c = (char)(c - ' ');
          localStringBuffer.append(c);
          c = Character.forDigit(arrayOfByte[i2] & 0xF, 16);
          if (Character.isLetter(c))
            c = (char)(c - ' ');
          localStringBuffer.append(c);
        }
        localByteArrayOutputStream.reset();
        i = 1;
      }
    }
    return i != 0 ? localStringBuffer.toString() : paramString1;
  }

  static
  {
    for (int i = 97; i <= 122; i++)
      dontNeedEncoding.set(i);
    for (i = 65; i <= 90; i++)
      dontNeedEncoding.set(i);
    for (i = 48; i <= 57; i++)
      dontNeedEncoding.set(i);
    dontNeedEncoding.set(32);
    dontNeedEncoding.set(45);
    dontNeedEncoding.set(95);
    dontNeedEncoding.set(46);
    dontNeedEncoding.set(42);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.URLEncoder
 * JD-Core Version:    0.6.0
 */