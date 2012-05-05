package com.sun.deploy.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.jar.Manifest;
import sun.misc.Resource;

class InvalidLoader extends DeployURLClassPath.Loader
{
  private final InvalidResource invalid;

  public InvalidLoader(IOException paramIOException, URL paramURL)
  {
    super(paramURL);
    this.invalid = new InvalidResource(paramIOException, paramURL);
  }

  URL findResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator)
  {
    return null;
  }

  Resource getResource(String paramString, boolean paramBoolean, DeployURLClassPath.PathIterator paramPathIterator)
  {
    return this.invalid;
  }

  void close()
    throws IOException
  {
  }

  private class InvalidResource extends Resource
  {
    private final IOException error;
    private final URL location;

    InvalidResource(IOException paramURL, URL arg3)
    {
      if (paramURL == null)
        throw new IllegalArgumentException("Expect a IOException");
      Object localObject;
      if (localObject == null)
        throw new IllegalArgumentException("Expect location URL");
      this.error = paramURL;
      this.location = localObject;
    }

    public Throwable getError()
    {
      return this.error;
    }

    public String getName()
    {
      return "Invalid Resource";
    }

    public URL getURL()
    {
      return this.location;
    }

    public URL getCodeSourceURL()
    {
      return this.location;
    }

    public Certificate[] getCertificates()
    {
      return super.getCertificates();
    }

    public CodeSigner[] getCodeSigners()
    {
      return super.getCodeSigners();
    }

    public InputStream getInputStream()
      throws IOException
    {
      throw this.error;
    }

    public int getContentLength()
      throws IOException
    {
      throw this.error;
    }

    public ByteBuffer getByteBuffer()
      throws IOException
    {
      throw this.error;
    }

    public byte[] getBytes()
      throws IOException
    {
      throw this.error;
    }

    public Manifest getManifest()
      throws IOException
    {
      throw this.error;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.InvalidLoader
 * JD-Core Version:    0.6.0
 */