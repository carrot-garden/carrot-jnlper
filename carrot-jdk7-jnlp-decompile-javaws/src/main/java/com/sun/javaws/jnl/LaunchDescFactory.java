package com.sun.javaws.jnl;

import B;
import com.sun.deploy.Environment;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.net.FailedDownloadException;
import com.sun.deploy.net.HttpRequest;
import com.sun.deploy.net.HttpResponse;
import com.sun.deploy.net.offline.DeployOfflineManager;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.URLUtil;
import com.sun.deploy.xml.XMLNode;
import com.sun.javaws.exceptions.BadFieldException;
import com.sun.javaws.exceptions.JNLParseException;
import com.sun.javaws.exceptions.MissingFieldException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

public class LaunchDescFactory
{
  private static final boolean DEBUG = false;
  private static final int BUFFER_SIZE = 8192;
  private static URL derivedCodebase = null;
  private static URL docbase = null;

  public static void setDocBase(URL paramURL)
  {
    docbase = paramURL;
  }

  public static URL getDocBase()
  {
    return docbase;
  }

  public static URL getDerivedCodebase()
  {
    if ((docbase != null) && (derivedCodebase == null))
      try
      {
        derivedCodebase = new URL(docbase.toString().substring(0, docbase.toString().lastIndexOf("/") + 1));
      }
      catch (MalformedURLException localMalformedURLException)
      {
        Trace.ignoredException(localMalformedURLException);
      }
    return derivedCodebase;
  }

  public static LaunchDesc buildDescriptor(byte[] paramArrayOfByte, URL paramURL1, URL paramURL2, URL paramURL3)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    return XMLFormat.parse(paramArrayOfByte, paramURL1, paramURL2, paramURL3, new DefaultMatchJRE());
  }

  public static LaunchDesc buildDescriptor(byte[] paramArrayOfByte, URL paramURL1, URL paramURL2)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    return buildDescriptor(paramArrayOfByte, paramURL1, paramURL2, null);
  }

  public static LaunchDesc buildDescriptor(File paramFile, URL paramURL1, URL paramURL2, URL paramURL3)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    return buildDescriptor(readBytes(new FileInputStream(paramFile), paramFile.length()), paramURL1, paramURL2, paramURL3);
  }

  public static LaunchDesc buildDescriptor(File paramFile)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(paramFile.getPath());
    Object localObject1;
    URL localURL;
    Object localObject2;
    if (localLocalApplicationProperties != null)
    {
      str = localLocalApplicationProperties.getDocumentBase();
      localObject1 = localLocalApplicationProperties.getCodebase();
      localURL = null;
      localObject2 = null;
      try
      {
        localURL = new URL((String)localObject1);
      }
      catch (MalformedURLException localMalformedURLException2)
      {
      }
      try
      {
        localObject2 = new URL(str);
      }
      catch (MalformedURLException localMalformedURLException3)
      {
      }
      return buildDescriptor(paramFile, localURL, (URL)localObject2, null);
    }
    String str = System.getProperty("jnlpx.origFilenameArg");
    if (str != null)
    {
      localObject1 = new File(str);
      localURL = null;
      try
      {
        localObject2 = ((File)localObject1).getAbsoluteFile().getParent();
        if (((String)localObject2).startsWith(File.separator))
          localObject2 = ((String)localObject2).substring(1, ((String)localObject2).length());
        localURL = new URL("file:/" + (String)localObject2 + File.separator);
      }
      catch (MalformedURLException localMalformedURLException1)
      {
        Trace.ignoredException(localMalformedURLException1);
      }
      if (localURL != null)
      {
        LaunchDesc localLaunchDesc = buildDescriptor(paramFile, localURL, null, null);
        derivedCodebase = localURL;
        return localLaunchDesc;
      }
    }
    return (LaunchDesc)(LaunchDesc)null;
  }

  public static LaunchDesc buildDescriptor(URL paramURL1, URL paramURL2)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    int i = DownloadEngine.incrementInternalUse();
    try
    {
      LaunchDesc localLaunchDesc = _buildDescriptor(paramURL1, paramURL2);
      return localLaunchDesc;
    }
    finally
    {
      DownloadEngine.decrementInternalUse(i);
    }
    throw localObject;
  }

  private static LaunchDesc _buildDescriptor(URL paramURL1, URL paramURL2)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    Object localObject1 = null;
    File localFile;
    try
    {
      localFile = DownloadEngine.getCachedFile(paramURL1, null, true, false, null);
    }
    catch (IOException localIOException)
    {
      if (((localIOException instanceof UnknownHostException)) || ((localIOException instanceof FailedDownloadException)) || ((localIOException instanceof ConnectException)) || ((localIOException instanceof SocketException)))
      {
        Trace.ignoredException(localIOException);
        localObject1 = localIOException;
        localFile = DownloadEngine.getCachedFile(paramURL1, null, false, false, null);
        if ((localFile == null) && (DeployOfflineManager.isForcedOffline()))
          throw localIOException;
      }
      else
      {
        throw localIOException;
      }
    }
    URL localURL = URLUtil.asPathURL(URLUtil.getBase(paramURL1));
    if ((localFile != null) && (localFile.exists()))
    {
      localObject2 = buildDescriptor(localFile, localURL, paramURL2, paramURL1);
      if ((localObject2 != null) && (((LaunchDesc)localObject2).getLaunchType() == 5))
        DownloadEngine.removeCachedResource(paramURL1, null, null);
      if ((localObject1 != null) && (!((LaunchDesc)localObject2).getInformation().supportsOfflineOperation()))
        throw localObject1;
      derivedCodebase = localURL;
      return localObject2;
    }
    Object localObject2 = DownloadEngine.getHttpRequestImpl();
    HttpResponse localHttpResponse = ((HttpRequest)localObject2).doGetRequest(paramURL1);
    Object localObject3 = localHttpResponse.getInputStream();
    int i = localHttpResponse.getContentLength();
    String str = localHttpResponse.getContentEncoding();
    if ((str != null) && (str.indexOf("gzip") >= 0))
      localObject3 = new GZIPInputStream((InputStream)localObject3, 8192);
    LaunchDesc localLaunchDesc = buildDescriptor(readBytes((InputStream)localObject3, i), localURL, paramURL2);
    ((InputStream)localObject3).close();
    return (LaunchDesc)(LaunchDesc)localLaunchDesc;
  }

  public static LaunchDesc buildDescriptor(String paramString1, String paramString2, URL paramURL, boolean paramBoolean)
    throws BadFieldException, MissingFieldException, JNLParseException
  {
    URL localURL = null;
    if (paramBoolean)
      System.out.println("JNLP Build LaunchDesc jnlp: " + paramString1 + ", codebaseStr: " + paramString2 + ", documentbase: " + paramURL);
    try
    {
      localURL = URLUtil.asPathURL(new URL(paramString2));
      if (paramBoolean)
        System.out.println("   JNLP Codebase (absolute): " + localURL);
    }
    catch (Exception localException1)
    {
      localURL = null;
    }
    if (localURL == null)
      try
      {
        localURL = URLUtil.asPathURL(new URL(URLUtil.getBase(paramURL), paramString2));
        if (paramBoolean)
          System.out.println("   JNLP Codebase (documentbase+codebase): " + localURL);
      }
      catch (Exception localException2)
      {
        localURL = null;
      }
    if ((paramBoolean) && (localURL == null))
      System.out.println("   JNLP Codebase (null)");
    return buildDescriptor(paramString1, localURL, paramURL, paramBoolean);
  }

  private static LaunchDesc buildDescriptorFromCache(URL paramURL1, URL paramURL2)
    throws BadFieldException, MissingFieldException, JNLParseException
  {
    try
    {
      File localFile = DownloadEngine.getCachedFile(paramURL1, null, false, false, null);
      if (localFile != null)
      {
        URL localURL = URLUtil.asPathURL(URLUtil.getBase(paramURL1));
        return buildDescriptor(localFile, localURL, paramURL2, paramURL1);
      }
    }
    catch (IOException localIOException)
    {
    }
    return null;
  }

  public static LaunchDesc buildDescriptorFromCache(String paramString, URL paramURL1, URL paramURL2)
    throws BadFieldException, MissingFieldException, JNLParseException
  {
    URL localURL = null;
    LaunchDesc localLaunchDesc = null;
    try
    {
      localURL = new URL(paramString);
    }
    catch (Exception localException1)
    {
    }
    if (localURL != null)
    {
      localLaunchDesc = buildDescriptorFromCache(localURL, paramURL2);
      if (localLaunchDesc != null)
        return localLaunchDesc;
    }
    if (paramURL1 != null)
    {
      try
      {
        localURL = new URL(paramURL1, paramString);
      }
      catch (Exception localException2)
      {
        localURL = null;
      }
      localLaunchDesc = buildDescriptorFromCache(localURL, paramURL2);
      if (localLaunchDesc != null)
        return localLaunchDesc;
    }
    if ((paramURL1 == null) && (paramURL2 != null))
    {
      try
      {
        localURL = new URL(URLUtil.getBase(paramURL2), paramString);
      }
      catch (Exception localException3)
      {
        localURL = null;
      }
      localLaunchDesc = buildDescriptorFromCache(localURL, paramURL2);
      if (localLaunchDesc != null)
        return localLaunchDesc;
    }
    return null;
  }

  public static LaunchDesc buildDescriptor(String paramString, URL paramURL1, URL paramURL2, boolean paramBoolean)
    throws BadFieldException, MissingFieldException, JNLParseException
  {
    URL localURL = null;
    try
    {
      localURL = new URL(paramString);
    }
    catch (Exception localException1)
    {
      localURL = null;
    }
    if (localURL != null)
      try
      {
        LaunchDesc localLaunchDesc1 = buildDescriptor(localURL, paramURL2);
        if (paramBoolean)
          System.out.println("   JNLP Ref (absolute): " + localURL.toString());
        return localLaunchDesc1;
      }
      catch (BadFieldException localBadFieldException1)
      {
        throw localBadFieldException1;
      }
      catch (MissingFieldException localMissingFieldException1)
      {
        throw localMissingFieldException1;
      }
      catch (JNLParseException localJNLParseException1)
      {
        throw localJNLParseException1;
      }
      catch (Exception localException2)
      {
        if (paramBoolean)
        {
          System.out.println(localException2);
          localException2.printStackTrace();
        }
        localURL = null;
      }
    if (paramURL1 != null)
    {
      try
      {
        localURL = new URL(paramURL1, paramString);
      }
      catch (Exception localException3)
      {
        localURL = null;
      }
      if (localURL != null)
        try
        {
          LaunchDesc localLaunchDesc2 = buildDescriptor(localURL, paramURL2);
          if (paramBoolean)
            System.out.println("   JNLP Ref (codebase + ref): " + localURL.toString());
          return localLaunchDesc2;
        }
        catch (BadFieldException localBadFieldException2)
        {
          throw localBadFieldException2;
        }
        catch (MissingFieldException localMissingFieldException2)
        {
          throw localMissingFieldException2;
        }
        catch (JNLParseException localJNLParseException2)
        {
          throw localJNLParseException2;
        }
        catch (Exception localException4)
        {
          if (paramBoolean)
          {
            System.out.println(localException4);
            localException4.printStackTrace();
          }
          localURL = null;
        }
    }
    if ((paramURL1 == null) && (paramURL2 != null))
    {
      try
      {
        localURL = new URL(URLUtil.getBase(paramURL2), paramString);
      }
      catch (Exception localException5)
      {
        localURL = null;
      }
      if (localURL != null)
        try
        {
          LaunchDesc localLaunchDesc3 = buildDescriptor(localURL, paramURL2);
          if (paramBoolean)
            System.out.println("   JNLP Ref (documentbase + ref): " + localURL.toString());
          return localLaunchDesc3;
        }
        catch (BadFieldException localBadFieldException3)
        {
          throw localBadFieldException3;
        }
        catch (MissingFieldException localMissingFieldException3)
        {
          throw localMissingFieldException3;
        }
        catch (JNLParseException localJNLParseException3)
        {
          throw localJNLParseException3;
        }
        catch (Exception localException6)
        {
          if (paramBoolean)
          {
            System.out.println(localException6);
            localException6.printStackTrace();
          }
          localURL = null;
        }
    }
    if (paramBoolean)
      System.out.println("   JNLP Ref (...): NULL !");
    return null;
  }

  public static LaunchDesc buildDescriptor(String paramString)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    FileInputStream localFileInputStream = null;
    Object localObject = null;
    int i = -1;
    try
    {
      URL localURL1 = new URL(paramString);
      if (paramString.endsWith(".jarjnlp"))
        try
        {
          return buildDescriptorFromCache(localURL1, null);
        }
        catch (Exception localException)
        {
          Trace.ignored(localException);
        }
      return buildDescriptor(localURL1, null);
    }
    catch (MalformedURLException localMalformedURLException1)
    {
      File localFile = new File(paramString);
      if ((!localFile.exists()) && (!Config.isJavaVersionAtLeast14()) && (localMalformedURLException1.getMessage().indexOf("https") != -1))
        throw new BadFieldException(ResourceManager.getString("launch.error.badfield.download.https"), "<jnlp>", "https");
      localFileInputStream = new FileInputStream(paramString);
      long l = localFile.length();
      if (l > 1048576L)
        throw new IOException("File too large");
      i = (int)l;
      if (Environment.isImportMode())
      {
        String str = localFile.getParent();
        if ((Environment.getImportModeCodebaseOverride() == null) && (str != null))
          try
          {
            URL localURL2 = new URL("file", null, URLUtil.encodePath(str));
            Environment.setImportModeCodebaseOverride(localURL2.toString());
          }
          catch (MalformedURLException localMalformedURLException2)
          {
            Trace.ignoredException(localMalformedURLException2);
          }
      }
    }
    return buildDescriptor(readBytes(localFileInputStream, i), null, null);
  }

  public static LaunchDesc buildInternalLaunchDesc(XMLNode paramXMLNode, String paramString)
  {
    return new LaunchDesc("0.1", null, null, null, null, 1, null, null, null, 5, null, null, null, null, null, paramString == null ? paramXMLNode.getName() : paramString, paramXMLNode, new DefaultMatchJRE());
  }

  public static byte[] readBytes(InputStream paramInputStream, long paramLong)
    throws IOException
  {
    if (paramLong > 1048576L)
      throw new IOException("File too large");
    BufferedInputStream localBufferedInputStream = null;
    if ((paramInputStream instanceof BufferedInputStream))
      localBufferedInputStream = (BufferedInputStream)paramInputStream;
    else
      localBufferedInputStream = new BufferedInputStream(paramInputStream);
    if (paramLong <= 0L)
      paramLong = 10240L;
    Object localObject = new byte[(int)paramLong];
    int j = 0;
    byte[] arrayOfByte;
    for (int i = localBufferedInputStream.read(localObject, j, localObject.length - j); i != -1; i = localBufferedInputStream.read(localObject, j, localObject.length - j))
    {
      j += i;
      if (localObject.length != j)
        continue;
      arrayOfByte = new byte[localObject.length * 2];
      System.arraycopy(localObject, 0, arrayOfByte, 0, localObject.length);
      localObject = arrayOfByte;
    }
    localBufferedInputStream.close();
    paramInputStream.close();
    if (j != localObject.length)
    {
      arrayOfByte = new byte[j];
      System.arraycopy(localObject, 0, arrayOfByte, 0, j);
      localObject = arrayOfByte;
    }
    return (B)localObject;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.LaunchDescFactory
 * JD-Core Version:    0.6.0
 */