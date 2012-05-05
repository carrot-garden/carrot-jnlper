package com.sun.javaws.security;

import B;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.jar.Manifest;

public abstract class Resource
{
  public abstract String getName();

  public abstract URL getURL();

  public abstract URL getCodeSourceURL();

  public abstract InputStream getInputStream()
    throws IOException;

  public abstract int getContentLength()
    throws IOException;

  public byte[] getBytes()
    throws IOException
  {
    InputStream localInputStream = getInputStream();
    int i = getContentLength();
    Object localObject1;
    try
    {
      if (i != -1)
      {
        localObject1 = new byte[i];
        while (i > 0)
        {
          j = localInputStream.read(localObject1, localObject1.length - i, i);
          if (j == -1)
            throw new IOException("unexpected EOF");
          i -= j;
        }
      }
      localObject1 = new byte[1024];
      int j = 0;
      byte[] arrayOfByte;
      while ((i = localInputStream.read(localObject1, j, localObject1.length - j)) != -1)
      {
        j += i;
        if (j < localObject1.length)
          continue;
        arrayOfByte = new byte[j * 2];
        System.arraycopy(localObject1, 0, arrayOfByte, 0, j);
        localObject1 = arrayOfByte;
      }
      if (j != localObject1.length)
      {
        arrayOfByte = new byte[j];
        System.arraycopy(localObject1, 0, arrayOfByte, 0, j);
        localObject1 = arrayOfByte;
      }
    }
    finally
    {
      localInputStream.close();
    }
    return (B)localObject1;
  }

  public Manifest getManifest()
    throws IOException
  {
    return null;
  }

  public Certificate[] getCertificates()
  {
    return null;
  }

  public CodeSigner[] getCodeSigners()
  {
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.security.Resource
 * JD-Core Version:    0.6.0
 */