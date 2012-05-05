package com.sun.deploy.net.cookie;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class RfcDateParser
{
  private boolean isGMT = false;
  private static final String[] standardFormats = { "EEEE', 'dd-MMM-yy HH:mm:ss z", "EEEE', 'dd-MMM-yy HH:mm:ss", "EEE', 'dd-MMM-yyyy HH:mm:ss z", "EEE', 'dd MMM yyyy HH:mm:ss z", "EEEE', 'dd MMM yyyy HH:mm:ss z", "EEE', 'dd MMM yyyy hh:mm:ss z", "EEEE', 'dd MMM yyyy hh:mm:ss z", "EEE MMM dd HH:mm:ss z yyyy", "EEE MMM dd HH:mm:ss yyyy", "EEE', 'dd-MMM-yy HH:mm:ss", "EEE', 'dd-MMM-yyyy HH:mm:ss" };
  private static final String[] gmtStandardFormats = { "EEEE',' dd-MMM-yy HH:mm:ss 'GMT'", "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'", "EEE',' dd MMM yyyy HH:mm:ss 'GMT'", "EEEE',' dd MMM yyyy HH:mm:ss 'GMT'", "EEE',' dd MMM yyyy hh:mm:ss 'GMT'", "EEEE',' dd MMM yyyy hh:mm:ss 'GMT'", "EEE MMM dd HH:mm:ss 'GMT' yyyy" };
  String dateString;

  RfcDateParser(String paramString)
  {
    this.dateString = paramString.trim();
    if (this.dateString.indexOf("GMT") != -1)
      this.isGMT = true;
  }

  Date getDate()
  {
    int i = this.isGMT ? gmtStandardFormats.length : standardFormats.length;
    for (int j = 0; j < i; j++)
    {
      Date localDate;
      if (this.isGMT)
        localDate = tryParsing(gmtStandardFormats[j]);
      else
        localDate = tryParsing(standardFormats[j]);
      if (localDate != null)
        return localDate;
    }
    return null;
  }

  private Date tryParsing(String paramString)
  {
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(paramString, Locale.US);
    if (this.isGMT)
      localSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    try
    {
      return localSimpleDateFormat.parse(this.dateString);
    }
    catch (Exception localException)
    {
    }
    return null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.cookie.RfcDateParser
 * JD-Core Version:    0.6.0
 */