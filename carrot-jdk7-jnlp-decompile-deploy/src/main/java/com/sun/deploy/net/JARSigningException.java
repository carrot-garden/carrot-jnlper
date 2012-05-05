package com.sun.deploy.net;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class JARSigningException extends DownloadException
{
  private int _code;
  private String _missingEntry;
  public static final int MULTIPLE_CERTIFICATES = 0;
  public static final int MULTIPLE_SIGNERS = 1;
  public static final int BAD_SIGNING = 2;
  public static final int UNSIGNED_FILE = 3;
  public static final int MISSING_ENTRY = 4;
  public static final int BLACKLISTED = 5;

  public JARSigningException(URL paramURL, String paramString, int paramInt)
  {
    super(paramURL, paramString);
    this._code = paramInt;
  }

  public JARSigningException(URL paramURL, String paramString1, int paramInt, String paramString2)
  {
    super(paramURL, paramString1);
    this._code = paramInt;
    this._missingEntry = paramString2;
  }

  public JARSigningException(URL paramURL, String paramString, int paramInt, Exception paramException)
  {
    super(paramURL, paramString, paramException);
    this._code = paramInt;
  }

  public String getRealMessage()
  {
    switch (this._code)
    {
    case 0:
      return ResourceManager.getString("launch.error.jarsigning-multicerts", getResourceString());
    case 1:
      return ResourceManager.getString("launch.error.jarsigning-multisigners", getResourceString());
    case 2:
      return ResourceManager.getString("launch.error.jarsigning-badsigning", getResourceString());
    case 3:
      return ResourceManager.getString("launch.error.jarsigning-unsignedfile", getResourceString());
    case 4:
      return ResourceManager.getString("launch.error.jarsigning-missingentry", getResourceString()) + "\n" + ResourceManager.getString("launch.error.jarsigning-missingentryname", this._missingEntry);
    case 5:
      return ResourceManager.getString("downloadengine.check.blacklist.found", getResourceString());
    }
    return "<error>";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.JARSigningException
 * JD-Core Version:    0.6.0
 */