package com.sun.javaws.jnl;

import com.sun.deploy.util.URLUtil;
import java.net.MalformedURLException;
import java.net.URL;

public class EmbeddedJNLPValidation
{
  private final LaunchDesc jnlp;
  private final URL codebase;

  public EmbeddedJNLPValidation(LaunchDesc paramLaunchDesc, URL paramURL)
  {
    if ((paramURL == null) || (paramLaunchDesc == null))
      throw new IllegalArgumentException("Should have jnlp and derivedCodebase.");
    if (paramURL.getProtocol().equals("http"))
    {
      String str = paramURL.getHost();
      if ((str == null) || (str.length() == 0))
        throw new IllegalArgumentException("Bad derivedCodebase: " + paramURL);
    }
    this.jnlp = paramLaunchDesc;
    this.codebase = paramURL;
  }

  public void validate()
  {
    String str1 = getCodebase();
    if ((str1 != null) && (str1.trim().length() > 0))
      throw new RuntimeException("Embbeded JNLP should not have non-empty codebase. Got: " + str1);
    String str2 = getHref();
    if (str2 == null)
      str2 = "";
    else
      str2 = str2.trim();
    try
    {
      URL localURL1 = new URL(str2);
      throw new RuntimeException("Absolute href not allowed with embbedded JNLP: " + str2);
    }
    catch (MalformedURLException localMalformedURLException2)
    {
      try
      {
        URL localURL2 = new URL(this.codebase, str2);
        if (!URLUtil.sameBase(this.codebase, localURL2))
          throw new RuntimeException("Invalid 'href' value in embedded JNLP: " + str2);
      }
      catch (MalformedURLException localMalformedURLException2)
      {
        throw new RuntimeException("Invalid URL derived with href: " + str2, localMalformedURLException2);
      }
    }
  }

  private String getCodebase()
  {
    return XMLUtils.getAttribute(this.jnlp.getXmlNode(), "", "codebase");
  }

  private String getHref()
  {
    return XMLUtils.getAttribute(this.jnlp.getXmlNode(), "", "href");
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.EmbeddedJNLPValidation
 * JD-Core Version:    0.6.0
 */