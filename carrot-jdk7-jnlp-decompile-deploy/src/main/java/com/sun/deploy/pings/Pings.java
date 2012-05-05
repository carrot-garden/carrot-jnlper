package com.sun.deploy.pings;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Platform;

public class Pings
{
  public static final String JAVAFX_RT_JNLP_URL = "http://dl.javafx.com/javafx-rt.jnlp";
  public static final String JAVAFX_CACHE_JNLP_URL = "http://dl.javafx.com/javafx-cache.jnlp";
  public static final String JAVAFX_PRELOAD_INSTALL_METHOD = "jfxp";
  public static final String JAVAFX_AUTOUPDATE_INSTALL_METHOD = "jfxau";
  public static final String JAVAFX_INSTALL_COMPLETED_PING = "jfxic";
  public static final int JAVAFX_RETURNCODE_SUCCESS = 0;
  public static final int JAVAFX_RETURNCODE_UNKNOWN_FAILURE = 2;
  public static final int JAVAFX_RETURNCODE_DOWNLOAD_FAILED_FAILURE = 3;
  public static final String JAVAFX_UNDEFINED_PING_FIELD = "XX";

  public static void sendJFXPing(String paramString1, String paramString2, String paramString3, int paramInt, String paramString4)
  {
    if (Environment.allowAltJavaFxRuntimeURL())
      return;
    String str1 = "XX";
    if (Environment.getJavaFxInstallMode() == 2)
      str1 = "jfxau";
    else if (Environment.getJavaFxInstallMode() == 1)
      str1 = "jfxp";
    String str2 = System.getProperty("java.version");
    String str3 = "XX";
    if (paramString4 != null)
      str3 = paramString4;
    Platform.get().sendJFXPing(str1, paramString1, paramString2, paramString3, str2, paramInt, str3);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.pings.Pings
 * JD-Core Version:    0.6.0
 */