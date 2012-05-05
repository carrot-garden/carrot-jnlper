package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class ErrorCodeResponseException extends DownloadException
{
  private String _errorLine;
  private int _errorCode;
  private boolean _jreDownload;
  public static final int ERR_10_NO_RESOURCE = 10;
  public static final int ERR_11_NO_VERSION = 11;
  public static final int ERR_20_UNSUP_OS = 20;
  public static final int ERR_21_UNSUP_ARCH = 21;
  public static final int ERR_22_UNSUP_LOCALE = 22;
  public static final int ERR_23_UNSUP_JRE = 23;
  public static final int ERR_99_UNKNOWN = 99;

  public ErrorCodeResponseException(URL paramURL, String paramString1, String paramString2)
  {
    super(paramURL, paramString1);
    this._errorLine = paramString2;
    this._jreDownload = false;
    this._errorCode = 99;
    if (this._errorLine != null)
      try
      {
        int i = this._errorLine.indexOf(' ');
        if (i != -1)
          this._errorCode = Integer.parseInt(this._errorLine.substring(0, i));
      }
      catch (NumberFormatException localNumberFormatException)
      {
        this._errorCode = 99;
      }
  }

  public void setJreDownload(boolean paramBoolean)
  {
    this._jreDownload = paramBoolean;
  }

  public int getErrorCode()
  {
    return this._errorCode;
  }

  public String getRealMessage()
  {
    String str = this._jreDownload ? ResourceManager.getString("launch.error.noJre") : "";
    if (this._errorCode != 99)
      return str + ResourceManager.getString("launch.error.errorcoderesponse-known", getResourceString(), this._errorCode, this._errorLine);
    return str + ResourceManager.getString("launch.error.errorcoderesponse-unknown", getResourceString());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.ErrorCodeResponseException
 * JD-Core Version:    0.6.0
 */