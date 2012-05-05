package com.sun.deploy.net;

import com.sun.applet2.preloader.CancelException;
import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Unpacker;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class HttpDownloadHelper
  implements HttpDownload
{
  private static final int BUF_SIZE = 32768;
  private static final int BUFFER_SIZE = 8192;
  private static final String JAR_FILE_EXT = ".jar";
  private static final String JARJAR_FILE_EXT = ".jarjar";
  private static final String META_FILE_DIR = "meta-inf/";
  private HttpRequest _httpRequest;
  private static int jarCompression = -1;

  public HttpDownloadHelper(HttpRequest paramHttpRequest)
  {
    this._httpRequest = paramHttpRequest;
  }

  private static int getJarCompressionLevel()
  {
    if (jarCompression == -1)
    {
      String str = Config.getStringProperty("deployment.cache.jarcompression");
      str = str.trim();
      try
      {
        jarCompression = Integer.valueOf(str).intValue();
        if ((jarCompression < 0) || (jarCompression > 9))
          jarCompression = 0;
      }
      catch (NumberFormatException localNumberFormatException)
      {
        jarCompression = 0;
      }
    }
    return jarCompression;
  }

  public MessageHeader download(int paramInt1, URL paramURL, InputStream paramInputStream, String paramString, File paramFile, HttpDownloadListener paramHttpDownloadListener, int paramInt2)
    throws CanceledDownloadException, IOException
  {
    return download(paramInt1, paramURL, paramInputStream, paramString, paramFile, paramHttpDownloadListener, paramInt2, true);
  }

  public MessageHeader download(int paramInt1, URL paramURL, InputStream paramInputStream, String paramString, File paramFile, HttpDownloadListener paramHttpDownloadListener, int paramInt2, boolean paramBoolean)
    throws CanceledDownloadException, IOException
  {
    MessageHeader localMessageHeader = null;
    boolean bool = needsGUnzip(paramBoolean, paramString);
    int i = paramInt1;
    if (paramHttpDownloadListener != null)
      paramHttpDownloadListener.downloadProgress(0, i);
    if (Trace.isEnabled(TraceLevel.NETWORK))
      Trace.println(ResourceManager.getString("httpDownloadHelper.doingDownload", paramURL == null ? "" : paramURL.toString(), paramInt1, paramString), TraceLevel.NETWORK);
    BufferedOutputStream localBufferedOutputStream = null;
    JarOutputStream localJarOutputStream = null;
    ZipInputStream localZipInputStream1 = null;
    ZipInputStream localZipInputStream2 = null;
    int j = 0;
    try
    {
      localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(paramFile));
      Object localObject1;
      Object localObject2;
      if ((DownloadEngine.isInternalUse()) && ("pack200-gzip".equals(paramString)) && (DownloadEngine.isPack200Supported()))
      {
        localObject1 = Pack200.newUnpacker();
        ((Pack200.Unpacker)localObject1).addPropertyChangeListener(new PropertyChangeListener(paramHttpDownloadListener, i)
        {
          private final HttpDownloadListener val$dl;
          private final int val$length;

          public void propertyChange(PropertyChangeEvent paramPropertyChangeEvent)
          {
            if ((this.val$dl != null) && (paramPropertyChangeEvent.getPropertyName().compareTo("unpack.progress") == 0))
            {
              String str = (String)paramPropertyChangeEvent.getNewValue();
              int i = str != null ? Integer.parseInt(str) : 0;
              try
              {
                this.val$dl.downloadProgress(i * this.val$length / 100, this.val$length);
              }
              catch (CancelException localCancelException)
              {
              }
            }
          }
        });
        localObject2 = new JarOutputStream(localBufferedOutputStream);
        if ((Environment.isJavaPlugin()) && (getJarCompressionLevel() != 0))
          ((JarOutputStream)localObject2).setLevel(getJarCompressionLevel());
        ((Pack200.Unpacker)localObject1).unpack(new GZIPInputStream(paramInputStream), (JarOutputStream)localObject2);
        ((JarOutputStream)localObject2).close();
        localMessageHeader = createHeaderAdjustment(paramFile.length(), null);
      }
      else
      {
        localObject1 = HttpUtils.removeQueryStringFromURL(paramURL).toString().toLowerCase();
        localObject2 = paramString;
        Object localObject3;
        if ((isJarOrJarjar((String)localObject1)) && ((!HttpUtils.hasGzipEncoding(paramString)) || (bool)))
        {
          localJarOutputStream = new JarOutputStream(localBufferedOutputStream);
          if (getJarCompressionLevel() != 0)
            localJarOutputStream.setLevel(getJarCompressionLevel());
          if (bool)
          {
            localZipInputStream1 = new ZipInputStream(new GZIPInputStream(new BufferedInputStream(paramInputStream), 8192));
            decompressWrite(localZipInputStream1, localJarOutputStream, i, paramHttpDownloadListener);
            localObject2 = HttpUtils.removeGzipEncoding(paramString);
          }
          else
          {
            localZipInputStream1 = new ZipInputStream(new BufferedInputStream(paramInputStream, 8192));
            if (((String)localObject1).endsWith(".jarjar"))
            {
              localObject3 = localZipInputStream1.getNextEntry();
              while (localObject3 != null)
              {
                if (((ZipEntry)localObject3).toString().toLowerCase().startsWith("meta-inf/"))
                {
                  localObject3 = localZipInputStream1.getNextEntry();
                  continue;
                }
                if (((ZipEntry)localObject3).toString().toLowerCase().endsWith(".jar"))
                  break;
                throw new IOException("cache.jarjar.invalid_file");
              }
              localZipInputStream2 = localZipInputStream1;
              localZipInputStream1 = new ZipInputStream(localZipInputStream1);
            }
            decompressWrite(localZipInputStream1, localJarOutputStream, i, paramHttpDownloadListener);
            if (localZipInputStream2 != null)
            {
              localObject3 = localZipInputStream2.getNextEntry();
              if (localObject3 != null)
              {
                String str = null;
                if (!((ZipEntry)localObject3).toString().toLowerCase().endsWith(".jar"))
                  str = "cache.jarjar.invalid_file";
                else
                  str = "cache.jarjar.multiple_jar";
                throw new IOException(str);
              }
            }
          }
          if (localJarOutputStream != null)
          {
            localJarOutputStream.close();
            localJarOutputStream = null;
          }
          localMessageHeader = createHeaderAdjustment(paramFile.length(), (String)localObject2);
        }
        else
        {
          localObject3 = new BufferedInputStream(paramInputStream);
          if (bool)
          {
            localObject3 = new GZIPInputStream((InputStream)localObject3, 8192);
            localObject2 = HttpUtils.removeGzipEncoding(paramString);
          }
          int k = 0;
          int m = 0;
          byte[] arrayOfByte = new byte[32768];
          for (int n = 0; (k = ((InputStream)localObject3).read(arrayOfByte, 0, arrayOfByte.length)) != -1; n++)
          {
            if ((DownloadEngine.isJarContentType(paramInt2)) && (n == 0) && (!DownloadEngine.isJarHeaderValid(arrayOfByte)))
              throw new IOException("Invalid jar file");
            localBufferedOutputStream.write(arrayOfByte, 0, k);
            m += k;
            if (paramHttpDownloadListener == null)
              continue;
            int i1 = m;
            if ((i1 > i) && (i != 0))
              i1 = i;
            paramHttpDownloadListener.downloadProgress(i1, i);
          }
          if (bool)
            localMessageHeader = createHeaderAdjustment(m, (String)localObject2);
        }
      }
      if (Trace.isEnabled(TraceLevel.NETWORK))
        Trace.println(ResourceManager.getString("httpDownloadHelper.wroteUrlToFile", paramURL == null ? "" : paramURL.toString(), paramFile == null ? "" : paramFile.toString()), TraceLevel.NETWORK);
    }
    catch (IOException localIOException)
    {
      j = 1;
      throw localIOException;
    }
    finally
    {
      if (localZipInputStream2 != null)
        localZipInputStream2.close();
      else if (localZipInputStream1 != null)
        localZipInputStream1.close();
      else if (paramInputStream != null)
        paramInputStream.close();
      if (localJarOutputStream != null)
        localJarOutputStream.close();
      else if (localBufferedOutputStream != null)
        localBufferedOutputStream.close();
      if ((j != 0) && (paramFile != null))
        paramFile.delete();
    }
    if (paramHttpDownloadListener != null)
      paramHttpDownloadListener.downloadProgress(i, i);
    return (MessageHeader)(MessageHeader)(MessageHeader)localMessageHeader;
  }

  private static boolean needsGUnzip(boolean paramBoolean, String paramString)
  {
    return (paramBoolean) && (HttpUtils.hasGzipEncoding(paramString));
  }

  private MessageHeader createHeaderAdjustment(long paramLong, String paramString)
  {
    MessageHeader localMessageHeader = new MessageHeader();
    localMessageHeader.add("content-encoding", paramString);
    String str = paramLong > -1L ? String.valueOf(paramLong) : null;
    localMessageHeader.add("content-length", str);
    return localMessageHeader;
  }

  private void decompressWrite(ZipInputStream paramZipInputStream, ZipOutputStream paramZipOutputStream, int paramInt, HttpDownloadListener paramHttpDownloadListener)
    throws IOException
  {
    byte[] arrayOfByte = new byte[8192];
    ZipEntry localZipEntry1 = paramZipInputStream.getNextEntry();
    int i = 0;
    while (localZipEntry1 != null)
    {
      ZipEntry localZipEntry2 = (ZipEntry)localZipEntry1.clone();
      localZipEntry2.setCompressedSize(-1L);
      paramZipOutputStream.putNextEntry(localZipEntry2);
      int j = 0;
      while ((j = paramZipInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1)
      {
        paramZipOutputStream.write(arrayOfByte, 0, j);
        i += j;
        if ((i > paramInt) && (paramInt != 0))
          i = paramInt;
        if (paramHttpDownloadListener == null)
          continue;
        paramHttpDownloadListener.downloadProgress(i, paramInt);
      }
      paramZipOutputStream.closeEntry();
      localZipEntry1 = paramZipInputStream.getNextEntry();
    }
    paramZipOutputStream.flush();
  }

  private boolean isJarOrJarjar(String paramString)
  {
    return (paramString.endsWith(".jar")) || (paramString.endsWith(".jarjar"));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.HttpDownloadHelper
 * JD-Core Version:    0.6.0
 */