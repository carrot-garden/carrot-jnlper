package com.sun.deploy.net.proxy;

import com.sun.deploy.config.Platform;
import com.sun.deploy.trace.Trace;
import java.util.StringTokenizer;

public class WebProxyAutoDetection
{
  private static final int MINIMUM_DOMAIN_LEVEL = 2;

  private static native String getFQHostName();

  private static int getDomainLevel(String paramString)
  {
    int i = 0;
    if (paramString != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ".");
      i = localStringTokenizer.countTokens();
    }
    return i;
  }

  public static String getWPADURL()
  {
    String str1 = null;
    String str2 = getFQHostName();
    String str3 = "";
    if (str2 != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(str2, ".");
      localStringTokenizer.nextToken();
      while (localStringTokenizer.hasMoreTokens())
      {
        str3 = str3 + localStringTokenizer.nextToken();
        if (!localStringTokenizer.hasMoreTokens())
          continue;
        str3 = str3 + ".";
      }
      if (getDomainLevel(str3) >= 2)
        str1 = buildWPADURL(str3);
      else
        Trace.msgNetPrintln("net.proxy.browser.pDetectionError", new Object[] { str3 });
    }
    return str1;
  }

  private static String buildWPADURL(String paramString)
  {
    return "http://wpad." + paramString + "/wpad.dat";
  }

  static
  {
    Platform.get().loadDeployNativeLib();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.WebProxyAutoDetection
 * JD-Core Version:    0.6.0
 */