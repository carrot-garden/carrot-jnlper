package com.sun.deploy.net;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.SystemUtils;
import com.sun.deploy.util.URLUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public final class BasicHttpRequest
  implements HttpRequest
{
  private static final String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
  private static final String USER_AGENT = "User-Agent";
  private static String[] fieldName = { "content-length", "last-modified", "expires", "content-type", "content-encoding", "date", "server", "x-java-jnlp-version-id", "pragma", "cache-control" };

  public static boolean isHeaderFieldCached(String paramString)
  {
    if (paramString == null)
      return false;
    for (int i = 0; i < fieldName.length; i++)
      if (paramString.equalsIgnoreCase(fieldName[i]))
        return true;
    return false;
  }

  public HttpResponse doGetRequestEX(URL paramURL, long paramLong)
    throws IOException
  {
    return doRequest(paramURL, false, null, null, true, paramLong);
  }

  public HttpResponse doGetRequestEX(URL paramURL, String[] paramArrayOfString1, String[] paramArrayOfString2, long paramLong)
    throws IOException
  {
    return doRequest(paramURL, false, paramArrayOfString1, paramArrayOfString2, true, paramLong);
  }

  public HttpResponse doGetRequest(URL paramURL)
    throws IOException
  {
    return doRequest(paramURL, false, null, null, true);
  }

  public HttpResponse doGetRequest(URL paramURL, boolean paramBoolean)
    throws IOException
  {
    return doRequest(paramURL, false, null, null, paramBoolean);
  }

  public HttpResponse doHeadRequest(URL paramURL)
    throws IOException
  {
    return doRequest(paramURL, !Environment.isJavaPlugin(), null, null, true);
  }

  public HttpResponse doHeadRequest(URL paramURL, boolean paramBoolean)
    throws IOException
  {
    return doRequest(paramURL, true, null, null, paramBoolean);
  }

  public HttpResponse doGetRequest(URL paramURL, String[] paramArrayOfString1, String[] paramArrayOfString2)
    throws IOException
  {
    return doRequest(paramURL, false, paramArrayOfString1, paramArrayOfString2, true);
  }

  public HttpResponse doGetRequest(URL paramURL, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean)
    throws IOException
  {
    return doRequest(paramURL, false, paramArrayOfString1, paramArrayOfString2, paramBoolean);
  }

  public HttpResponse doHeadRequest(URL paramURL, String[] paramArrayOfString1, String[] paramArrayOfString2)
    throws IOException
  {
    return doRequest(paramURL, true, paramArrayOfString1, paramArrayOfString2, true);
  }

  public HttpResponse doHeadRequest(URL paramURL, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean)
    throws IOException
  {
    return doRequest(paramURL, true, paramArrayOfString1, paramArrayOfString2, paramBoolean);
  }

  private HttpResponse doRequest(URL paramURL, boolean paramBoolean1, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean2)
    throws IOException
  {
    return doRequest(paramURL, paramBoolean1, paramArrayOfString1, paramArrayOfString2, paramBoolean2, 0L);
  }

  private HttpResponse doRequest(URL paramURL, boolean paramBoolean1, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean2, long paramLong)
    throws IOException
  {
    long l1 = 0L;
    String str1 = null;
    if (("file".equals(paramURL.getProtocol())) && (paramURL.getFile() != null))
      try
      {
        String str2 = URLUtil.getPathFromURL(paramURL);
        File localFile = new File(str2);
        l1 = localFile.lastModified();
        if (str2.endsWith(".jnlp"))
          str1 = "application/x-java-jnlp-file";
        else if (str2.endsWith(".jardiff"))
          str1 = "application/x-java-archive-diff";
      }
      catch (Exception localException)
      {
      }
    Object localObject = null;
    if (paramURL.getProtocol().equals("file"))
      localObject = createUrlConnection(HttpUtils.removeQueryStringFromURL(paramURL), paramBoolean1, paramArrayOfString1, paramArrayOfString2, paramBoolean2);
    else
      localObject = createUrlConnection(paramURL, paramBoolean1, paramArrayOfString1, paramArrayOfString2, paramBoolean2);
    URLUtil.setHostHeader((URLConnection)localObject);
    ((URLConnection)localObject).setUseCaches(false);
    ((URLConnection)localObject).setIfModifiedSince(paramLong);
    int i = 0;
    if ((Environment.getImportModeCodebase() == null) && (Environment.getImportModeCodebaseOverride() == null) && ((localObject instanceof HttpURLConnection)))
    {
      localObject = HttpUtils.followRedirects((URLConnection)localObject);
      i = 1;
    }
    if (i == 0)
      ((URLConnection)localObject).connect();
    int j = 200;
    HttpURLConnection localHttpURLConnection = null;
    if ((localObject instanceof HttpURLConnection))
    {
      localHttpURLConnection = (HttpURLConnection)localObject;
      j = localHttpURLConnection.getResponseCode();
    }
    if (Trace.isEnabled(TraceLevel.NETWORK))
      Trace.println(ResourceManager.getString("basicHttpRequest.responseCode", paramURL == null ? "" : paramURL.toString(), String.valueOf(j)), TraceLevel.NETWORK);
    int k = ((URLConnection)localObject).getContentLength();
    long l2 = l1 != 0L ? l1 : ((URLConnection)localObject).getLastModified();
    long l3 = ((URLConnection)localObject).getExpiration();
    String str3 = str1 != null ? str1 : ((URLConnection)localObject).getContentType();
    if ((str3 != null) && (str3.indexOf(';') != -1))
      str3 = str3.substring(0, str3.indexOf(';')).trim();
    MessageHeader localMessageHeader = initializeHeaderFields((URLConnection)localObject);
    String str4 = localMessageHeader.findValue("content-encoding");
    if (str4 != null)
      str4 = str4.toLowerCase();
    if (Trace.isEnabled(TraceLevel.NETWORK))
      Trace.println(ResourceManager.getString("basicHttpRequest.encoding", paramURL == null ? "" : paramURL.toString(), str4), TraceLevel.NETWORK);
    BufferedInputStream localBufferedInputStream = null;
    if (paramBoolean1)
      localBufferedInputStream = null;
    else
      localBufferedInputStream = new BufferedInputStream(((URLConnection)localObject).getInputStream());
    return (HttpResponse)new BasicHttpResponse(paramURL, j, k, l3, l2, str3, localMessageHeader, localBufferedInputStream, localHttpURLConnection, str4);
  }

  public static MessageHeader initializeHeaderFields(URLConnection paramURLConnection)
    throws IOException
  {
    MessageHeader localMessageHeader = new MessageHeader();
    String str1 = paramURLConnection.getHeaderFieldKey(0);
    String str2 = paramURLConnection.getHeaderField(0);
    if ((null == str1) && (null != str2))
      localMessageHeader.add(null, str2);
    for (int i = 0; i < fieldName.length; i++)
    {
      String str3 = paramURLConnection.getHeaderField(fieldName[i]);
      if (str3 == null)
        continue;
      if (str3.equalsIgnoreCase("application/x-java-archive-diff"))
        str3 = "application/java-archive";
      localMessageHeader.add(fieldName[i], str3);
    }
    return localMessageHeader;
  }

  private URLConnection createUrlConnection(URL paramURL, boolean paramBoolean1, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean2)
    throws MalformedURLException, IOException
  {
    URLConnection localURLConnection = paramURL.openConnection();
    if (paramBoolean2)
    {
      String str1 = "gzip";
      String str2 = HttpUtils.removeQueryStringFromURL(paramURL).toString().toLowerCase();
      if ((str2.endsWith(".jar")) || (str2.endsWith(".jarjar")))
      {
        addToRequestProperty(localURLConnection, "content-type", "application/x-java-archive");
        if (DownloadEngine.isPack200Supported())
          str1 = "pack200-gzip," + str1;
      }
      addToRequestProperty(localURLConnection, "accept-encoding", str1);
    }
    if (SystemUtils.priviledgedGetSystemProperty("http.agent") == null)
    {
      localURLConnection.setRequestProperty("User-Agent", Environment.getUserAgent());
      localURLConnection.setRequestProperty("UA-Java-Version", Config.getJavaVersion());
    }
    if ((paramArrayOfString1 != null) && (paramArrayOfString2 != null))
      for (int i = 0; i < paramArrayOfString1.length; i++)
        localURLConnection.setRequestProperty(paramArrayOfString1[i], paramArrayOfString2[i]);
    if ((localURLConnection instanceof HttpURLConnection))
      ((HttpURLConnection)localURLConnection).setRequestMethod(paramBoolean1 ? "HEAD" : "GET");
    return localURLConnection;
  }

  private void addToRequestProperty(URLConnection paramURLConnection, String paramString1, String paramString2)
  {
    String str = paramURLConnection.getRequestProperty(paramString1);
    if ((str == null) || (str.trim().length() == 0))
      str = paramString2;
    else
      str = str + "," + paramString2;
    paramURLConnection.setRequestProperty(paramString1, str);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.BasicHttpRequest
 * JD-Core Version:    0.6.0
 */