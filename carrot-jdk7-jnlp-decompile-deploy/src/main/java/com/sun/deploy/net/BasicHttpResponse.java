package com.sun.deploy.net;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

final class BasicHttpResponse
  implements HttpResponse
{
  private URL _request;
  private int _status;
  private int _length;
  private long _lastModified;
  private long _expiration;
  private String _mimeType;
  private BufferedInputStream _bis;
  private HttpURLConnection _httpURLConnection;
  private String _contentEncoding;
  private MessageHeader _headers;

  BasicHttpResponse(URL paramURL, int paramInt1, int paramInt2, long paramLong1, long paramLong2, String paramString1, MessageHeader paramMessageHeader, BufferedInputStream paramBufferedInputStream, HttpURLConnection paramHttpURLConnection, String paramString2)
  {
    this._request = paramURL;
    this._status = paramInt1;
    this._length = paramInt2;
    this._expiration = paramLong1;
    this._lastModified = paramLong2;
    this._mimeType = paramString1;
    this._headers = paramMessageHeader;
    this._bis = paramBufferedInputStream;
    this._httpURLConnection = paramHttpURLConnection;
    this._contentEncoding = paramString2;
  }

  public void disconnect()
  {
    if (this._httpURLConnection != null)
    {
      this._httpURLConnection.disconnect();
      if (Trace.isEnabled(TraceLevel.NETWORK))
        Trace.println(ResourceManager.getString("basicHttpResponse.disconnect", this._request == null ? "" : this._request.toString()), TraceLevel.NETWORK);
    }
  }

  public MessageHeader getHeaders()
  {
    return this._headers;
  }

  public URL getRequest()
  {
    return this._request;
  }

  public int getStatusCode()
  {
    return this._status;
  }

  public int getContentLength()
  {
    return this._length;
  }

  public long getLastModified()
  {
    return this._lastModified;
  }

  public long getExpiration()
  {
    return this._expiration;
  }

  public String getContentType()
  {
    return this._mimeType;
  }

  public String getContentEncoding()
  {
    return this._contentEncoding;
  }

  public String getResponseHeader(String paramString)
  {
    return this._headers.findValue(paramString);
  }

  public BufferedInputStream getInputStream()
  {
    return this._bis;
  }

  public URL getFinalURL()
  {
    if (this._httpURLConnection != null)
      return this._httpURLConnection.getURL();
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.BasicHttpResponse
 * JD-Core Version:    0.6.0
 */