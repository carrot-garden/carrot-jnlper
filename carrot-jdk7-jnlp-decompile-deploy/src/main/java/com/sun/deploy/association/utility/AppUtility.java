package com.sun.deploy.association.utility;

import java.net.URL;

public class AppUtility
{
  public static String getFileExtensionByURL(URL paramURL)
  {
    String str1 = paramURL.getFile().trim();
    if ((str1 == null) || (str1.equals("")) || (str1.equals("/")))
      return null;
    int i = str1.lastIndexOf("/");
    String str2 = str1.substring(i + 1, str1.length());
    i = str2.lastIndexOf(".");
    if ((i == -1) || (i == str2.length() - 1))
      return null;
    String str3 = str2.substring(i, str2.length());
    return str3;
  }

  public static String removeDotFromFileExtension(String paramString)
  {
    String str = paramString;
    if (paramString.charAt(0) == '.')
      str = paramString.substring(1, paramString.length());
    return str;
  }

  public static String addDotToFileExtension(String paramString)
  {
    String str1 = paramString;
    if (paramString.charAt(0) != '.')
    {
      String str2 = ".";
      str1 = str2.concat(paramString);
    }
    return str1;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.AppUtility
 * JD-Core Version:    0.6.0
 */