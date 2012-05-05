package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

final class ManualProxyHandler
  implements ProxyHandler
{
  private BrowserProxyInfo bpi = null;
  private List proxyOverridePatterns = null;

  public boolean isSupported(int paramInt)
  {
    return paramInt == 1;
  }

  public boolean isProxyCacheSupported()
  {
    return true;
  }

  public void init(BrowserProxyInfo paramBrowserProxyInfo)
    throws ProxyConfigException
  {
    Trace.msgNetPrintln("net.proxy.loading.manual");
    if (!isSupported(paramBrowserProxyInfo.getType()))
      throw new ProxyConfigException("Unable to support proxy type: " + paramBrowserProxyInfo.getType());
    this.bpi = paramBrowserProxyInfo;
    this.proxyOverridePatterns = new ArrayList();
    String[] arrayOfString = this.bpi.getOverrides();
    if (arrayOfString != null)
    {
      Trace.msgNetPrintln("net.proxy.pattern.convert");
      for (int i = 0; (arrayOfString != null) && (i < arrayOfString.length); i++)
      {
        String str = arrayOfString[i];
        try
        {
          if (str.equals("<local>"))
          {
            this.proxyOverridePatterns.add(Pattern.compile("[^.]+"));
            Trace.netPrintln("    <local> --> [^.]+");
          }
          else
          {
            Pattern localPattern = canonicalizePattern(str);
            this.proxyOverridePatterns.add(localPattern);
            Trace.netPrintln("    " + str + " --> " + localPattern.pattern());
          }
        }
        catch (PatternSyntaxException localPatternSyntaxException)
        {
          localPatternSyntaxException.printStackTrace();
          Trace.msgNetPrintln("net.proxy.bypass.convert.error");
        }
      }
    }
    Trace.msgNetPrintln("net.proxy.loading.done");
  }

  public ProxyInfo[] getProxyInfo(URL paramURL)
  {
    String str1 = paramURL.getProtocol();
    String str2 = paramURL.getHost();
    if (isProxyOverriden(str2.toUpperCase()))
      return new ProxyInfo[] { new ProxyInfo(null) };
    String str3 = null;
    int i = -1;
    if (str1.equals("http"))
    {
      str3 = this.bpi.getHttpHost();
      i = this.bpi.getHttpPort();
    }
    else if (str1.equals("https"))
    {
      str3 = this.bpi.getHttpsHost();
      i = this.bpi.getHttpsPort();
    }
    else if (str1.equals("ftp"))
    {
      str3 = this.bpi.getFtpHost();
      i = this.bpi.getFtpPort();
    }
    else if (str1.equals("gopher"))
    {
      str3 = this.bpi.getGopherHost();
      i = this.bpi.getGopherPort();
    }
    return new ProxyInfo[] { new ProxyInfo(str3, i, this.bpi.getSocksHost(), this.bpi.getSocksPort()) };
  }

  private Pattern canonicalizePattern(String paramString)
    throws PatternSyntaxException
  {
    if (paramString == null)
      return null;
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < paramString.length(); i++)
    {
      char c = paramString.charAt(i);
      if (c == '*')
        localStringBuffer.append(".*");
      else
        localStringBuffer.append(Character.toUpperCase(c));
    }
    return Pattern.compile(localStringBuffer.toString());
  }

  private boolean isProxyOverriden(String paramString)
  {
    Iterator localIterator = this.proxyOverridePatterns.iterator();
    while (localIterator.hasNext())
    {
      Pattern localPattern = (Pattern)localIterator.next();
      Matcher localMatcher = localPattern.matcher(paramString);
      if (localMatcher.matches())
        return true;
    }
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.ManualProxyHandler
 * JD-Core Version:    0.6.0
 */