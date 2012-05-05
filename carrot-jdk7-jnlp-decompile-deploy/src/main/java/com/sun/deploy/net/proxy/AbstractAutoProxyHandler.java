package com.sun.deploy.net.proxy;

import com.sun.deploy.trace.Trace;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

public abstract class AbstractAutoProxyHandler
  implements ProxyHandler
{
  private BrowserProxyInfo bpi = null;
  protected StringBuffer autoProxyScript = null;
  protected String jsPacScript = null;

  public final boolean isSupported(int paramInt)
  {
    return paramInt == 2;
  }

  public final boolean isProxyCacheSupported()
  {
    return true;
  }

  protected String getBrowserSpecificAutoProxy()
  {
    return "function isInNet(ipaddr, pattern, maskstr) {\n    var ipPattern = /^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$/;\n    var test = ipaddr.match(ipPattern);\n    if (test == null) {\n        ipaddr = dnsResolve(ipaddr);\n        if (ipaddr == null)\n            return false;\n    } else if ((test[1] > 255) || (test[2] > 255) || \n               (test[3] > 255) || (test[4] > 255) ) {\n        return false;\n    }\n    var host = convert_addr(ipaddr);\n    var pat  = convert_addr(pattern);\n    var mask = convert_addr(maskstr);\n    return ((host & mask) == (pat & mask));\n    \n}\n" + AutoProxyScript.jsDnsResolveForIE;
  }

  public final void init(BrowserProxyInfo paramBrowserProxyInfo)
    throws ProxyConfigException
  {
    Trace.msgNetPrintln("net.proxy.loading.auto");
    if (!isSupported(paramBrowserProxyInfo.getType()))
      throw new ProxyConfigException("Unable to support proxy type: " + paramBrowserProxyInfo.getType());
    this.bpi = paramBrowserProxyInfo;
    this.autoProxyScript = new StringBuffer();
    this.autoProxyScript.append("var _mon = new Array('JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC');var _day = new Array('SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT');function _isGmt(i) { return typeof i == 'string' && i == 'GMT'; }");
    this.autoProxyScript.append("function dnsDomainIs(host, domain) {if (domain != null && domain.charAt(0) != '.')return shExpMatch(host, domain);return shExpMatch(host, '*' + domain); }");
    this.autoProxyScript.append("function isPlainHostName(host){return (dnsDomainLevels(host) == 0); }");
    this.autoProxyScript.append("function convert_addr(ipchars) {\n    var bytes = ipchars.split('.');\n    var result = ((bytes[0] & 0xff) << 24) |\n                 ((bytes[1] & 0xff) << 16) |\n                 ((bytes[2] & 0xff) <<  8) |\n                  (bytes[3] & 0xff);\n    return result;\n}\n");
    this.autoProxyScript.append(getBrowserSpecificAutoProxy());
    this.autoProxyScript.append("function isResolvable(host){return (dnsResolve(host) != ''); }");
    this.autoProxyScript.append("function localHostOrDomainIs(host, hostdom){return shExpMatch(hostdom, host + '*'); }");
    this.autoProxyScript.append("function dnsDomainLevels(host){var s = host + '';for (var i=0, j=0; i < s.length; i++)if (s.charAt(i) == '.')j++;return j; }");
    this.autoProxyScript.append("function myIpAddress(){return '");
    try
    {
      InetAddress localInetAddress = InetAddress.getLocalHost();
      this.autoProxyScript.append(localInetAddress.getHostAddress());
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
      this.autoProxyScript.append("127.0.0.1");
    }
    this.autoProxyScript.append("'; }");
    this.autoProxyScript.append("function shExpMatch(str, shexp){  if (typeof str != 'string' || typeof shexp != 'string') return false;  if (shexp == '*') return true;  if (str == '' && shexp == '') return true;  str = str.toLowerCase(); shexp = shexp.toLowerCase(); var index = shexp.indexOf('*'); if (index == -1) { return (str == shexp); }  else if (index == 0) {  for (var i=0; i <= str.length; i++) {  if (shExpMatch(str.substring(i), shexp.substring(1))) return true;  } return false; } else { var sub = null, sub2 = null; sub = shexp.substring(0, index);if (index <= str.length) sub2 = str.substring(0, index); if (sub != '' && sub2 != '' && sub == sub2) { return shExpMatch(str.substring(index), shexp.substring(index)); }else { return false; }} }");
    this.autoProxyScript.append("function _dateRange(day1, month1, year1, day2, month2, year2, gmt){if (typeof day1 != 'number' || day1 <= 0 || typeof month1 != 'string' || typeof year1 != 'number' || year1 <= 0 || typeof day2 != 'number' || day2 <= 0 || typeof month2 != 'string' || typeof year2 != 'number' || year2 <= 0 || typeof gmt != 'boolean') return false; var m1 = -1, m2 = -1;for (var i=0; i < _mon.length; i++){if (_mon[i] == month1)m1 = i;if (_mon[i] == month2)m2 = i;}var cur = new Date();var d1 = new Date(year1, m1, day1, 0, 0, 0);var d2 = new Date(year2, m2, day2, 23, 59, 59);if (gmt == true)cur = new Date(cur.getTime() - cur.getTimezoneOffset() * 60 * 1000);return ((d1.getTime() <= cur.getTime()) && (cur.getTime() <= d2.getTime()));}function dateRange(p1, p2, p3, p4, p5, p6, p7){var cur = new Date();if (typeof p1 == 'undefined')return false;else if (typeof p2 == 'undefined' || _isGmt(p2)){if ((typeof p1) == 'string')return _dateRange(1, p1, cur.getFullYear(), 31, p1, cur.getFullYear(), _isGmt(p2));else if (typeof p1 == 'number' && p1 > 31)return _dateRange(1, 'JAN', p1, 31, 'DEC', p1, _isGmt(p2));else {for (var i=0; i < _mon.length; i++)if (_dateRange(p1, _mon[i], cur.getFullYear(), p1, _mon[i], cur.getFullYear(), _isGmt(p2))) return true;return false;}}else if (typeof p3 == 'undefined' || _isGmt(p3)){if ((typeof p1) == 'string')return _dateRange(1, p1, cur.getFullYear(), 31, p2, cur.getFullYear(), _isGmt(p3));else if (typeof p1 == 'number' && typeof p2 == 'number' && (p1 > 31 || p2 > 31))return _dateRange(1, 'JAN', p1, 31, 'DEC', p2, _isGmt(p3));else {if ((typeof p2) == 'string'){return _dateRange(p1, p2, cur.getFullYear(), p1, p2, cur.getFullYear(), _isGmt(p3));}else {for (var i=0; i < _mon.length; i++)if (_dateRange(p1, _mon[i], cur.getFullYear(), p2, _mon[i], cur.getFullYear(), _isGmt(p3)))return true;return false;}}}else if (typeof p4 == 'undefined' || _isGmt(p4))return _dateRange(p1, p2, p3, p1, p2, p3, _isGmt(p4));else if (typeof p5 == 'undefined' || _isGmt(p5)){if (typeof p2 == 'number')return _dateRange(1, p1, p2, 31, p3, p4, _isGmt(p5));else return _dateRange(p1, p2, cur.getFullYear(), p3, p4, cur.getFullYear(), _isGmt(p5))}else if (typeof p6 == 'undefined')return false;else return _dateRange(p1, p2, p3, p4, p5, p6, _isGmt(p7));}");
    this.autoProxyScript.append("function timeRange(p1, p2, p3, p4, p5, p6, p7) {if (typeof p1 == 'undefined')return false;else if (typeof p2 == 'undefined' || _isGmt(p2))return _timeRange(p1, 0, 0, p1, 59, 59, _isGmt(p2));else if (typeof p3 == 'undefined' || _isGmt(p3))return _timeRange(p1, 0, 0, p2, 0, 0, _isGmt(p3));else if (typeof p4 == 'undefined')return false;else if (typeof p5 == 'undefined' || _isGmt(p5))return _timeRange(p1, p2, 0, p3, p4, 0, _isGmt(p5));else if (typeof p6 == 'undefined')return false;else return _timeRange(p1, p2, p3, p4, p5, p6, _isGmt(p7));}function _timeRange(hour1, min1, sec1, hour2, min2, sec2, gmt) {if (typeof hour1 != 'number' || typeof min1 != 'number' || typeof sec1 != 'number' || hour1 < 0 || min1 < 0 || sec1 < 0 || typeof hour2 != 'number' || typeof min2 != 'number' || typeof sec2 != 'number' || hour2 < 0 || min2 < 0 || sec2 < 0 || typeof gmt != 'boolean')  return false; var cur = new Date();var d1 = new Date();var d2 = new Date();d1.setHours(hour1);d1.setMinutes(min1);d1.setSeconds(sec1);d2.setHours(hour2);d2.setMinutes(min2);d2.setSeconds(sec2);if (gmt == true)cur = new Date(cur.getTime() - cur.getTimezoneOffset() * 60 * 1000);return ((d1.getTime() <= cur.getTime()) && (cur.getTime() <= d2.getTime()));}");
    this.autoProxyScript.append("function weekdayRange(wd1, wd2, gmt){if (typeof wd1 == 'undefined') return false;else if (typeof wd2 == 'undefined' || _isGmt(wd2)) return _weekdayRange(wd1, wd1, _isGmt(wd2)); else return _weekdayRange(wd1, wd2, _isGmt(gmt)); }function _weekdayRange(wd1, wd2, gmt) {if (typeof wd1 != 'string' || typeof wd2 != 'string' || typeof gmt != 'boolean') return false; var w1 = -1, w2 = -1;for (var i=0; i < _day.length; i++) {if (_day[i] == wd1)w1 = i;if (_day[i] == wd2)w2 = i; }var cur = new Date();if (gmt == true)cur = new Date(cur.getTime() - cur.getTimezoneOffset() * 60 * 1000);var w3 = cur.getDay();if (w1 > w2)w2 = w2 + 7;if (w1 > w3)w3 = w3 + 7;return (w1 <= w3 && w3 <= w2); }");
    URL localURL = null;
    try
    {
      localURL = new URL(this.bpi.getAutoConfigURL());
    }
    catch (MalformedURLException localMalformedURLException)
    {
      throw new ProxyConfigException("Auto config URL is malformed");
    }
    if (localURL.getFile().toLowerCase().endsWith(".ins"))
      this.jsPacScript = getJSFileFromURL(getAutoConfigURLFromINS(localURL));
    else
      this.jsPacScript = getJSFileFromURL(localURL);
    this.autoProxyScript.append(this.jsPacScript);
    Trace.msgNetPrintln("net.proxy.loading.done");
  }

  public abstract ProxyInfo[] getProxyInfo(URL paramURL)
    throws ProxyUnavailableException;

  private URLConnection getDirectURLConnection(URL paramURL)
    throws ProxyConfigException
  {
    URLConnection localURLConnection = null;
    if (paramURL == null)
      return null;
    try
    {
      String str1 = paramURL.getProtocol();
      if (str1.equals("file"))
      {
        String str2 = paramURL.toExternalForm();
        int i = str2.indexOf('/');
        if (i == -1)
          throw new ProxyConfigException("Malformed URL specified:" + paramURL);
        do
          i++;
        while (str2.charAt(i) == '/');
        URL localURL = new URL("file:/" + str2.substring(i));
        localURLConnection = localURL.openConnection();
      }
      else
      {
        try
        {
          localURLConnection = paramURL.openConnection(Proxy.NO_PROXY);
        }
        catch (NoClassDefFoundError localNoClassDefFoundError)
        {
          localURLConnection = paramURL.openConnection();
        }
      }
    }
    catch (IOException localIOException)
    {
      throw new ProxyConfigException("Unable to obtain a connection from " + paramURL, localIOException);
    }
    return localURLConnection;
  }

  private URL getAutoConfigURLFromINS(URL paramURL)
    throws ProxyConfigException
  {
    Trace.msgNetPrintln("net.proxy.auto.download.ins", new Object[] { paramURL });
    String str1 = null;
    try
    {
      URLConnection localURLConnection = getDirectURLConnection(paramURL);
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localURLConnection.getInputStream()));
      String str2 = null;
      do
      {
        str2 = localBufferedReader.readLine();
        if ((str2 == null) || (str2.indexOf("AutoConfigJSURL=") != 0))
          continue;
        str1 = str2.substring(16);
        break;
      }
      while (str2 != null);
      localBufferedReader.close();
      if (str1 != null)
        return new URL(str1);
      throw new ProxyConfigException("Unable to locate 'AutoConfigJSURL' in INS file");
    }
    catch (ProxyConfigException localProxyConfigException)
    {
      throw localProxyConfigException;
    }
    catch (MalformedURLException localMalformedURLException)
    {
      throw new ProxyConfigException("Malformed URL specified in INS file: " + str1, localMalformedURLException);
    }
    catch (Throwable localThrowable)
    {
    }
    throw new ProxyConfigException("Unable to obtain INS file from " + paramURL, localThrowable);
  }

  private String getJSFileFromURL(URL paramURL)
    throws ProxyConfigException
  {
    Trace.msgNetPrintln("net.proxy.auto.download.js", new Object[] { paramURL });
    try
    {
      URLConnection localURLConnection = getDirectURLConnection(paramURL);
      RemoveCommentReader localRemoveCommentReader = new RemoveCommentReader(new InputStreamReader(localURLConnection.getInputStream()));
      BufferedReader localBufferedReader = new BufferedReader(localRemoveCommentReader);
      StringWriter localStringWriter = new StringWriter();
      char[] arrayOfChar = new char[4096];
      int i;
      while ((i = localBufferedReader.read(arrayOfChar)) != -1)
        localStringWriter.write(arrayOfChar, 0, i);
      localBufferedReader.close();
      localRemoveCommentReader.close();
      localStringWriter.close();
      return localStringWriter.toString();
    }
    catch (Throwable localThrowable)
    {
    }
    throw new ProxyConfigException("Unable to obtain auto proxy file from " + paramURL, localThrowable);
  }

  protected final ProxyInfo[] extractAutoProxySetting(String paramString)
  {
    if (paramString != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ";", false);
      ProxyInfo[] arrayOfProxyInfo = new ProxyInfo[localStringTokenizer.countTokens()];
      int i = 0;
      while (localStringTokenizer.hasMoreTokens())
      {
        String str = localStringTokenizer.nextToken();
        int j = str.indexOf("PROXY");
        if (j != -1)
        {
          arrayOfProxyInfo[(i++)] = new ProxyInfo(str.substring(j + 6));
          continue;
        }
        j = str.indexOf("SOCKS");
        if (j != -1)
        {
          arrayOfProxyInfo[(i++)] = new ProxyInfo(null, str.substring(j + 6));
          continue;
        }
        arrayOfProxyInfo[(i++)] = new ProxyInfo(null, -1);
      }
      return arrayOfProxyInfo;
    }
    return new ProxyInfo[] { new ProxyInfo(null) };
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.AbstractAutoProxyHandler
 * JD-Core Version:    0.6.0
 */