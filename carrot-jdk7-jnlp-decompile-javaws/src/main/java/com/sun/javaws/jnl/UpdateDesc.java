package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;

public class UpdateDesc
  implements XMLable
{
  public static final int CHECK_ALWAYS = 0;
  public static final int CHECK_TIMEOUT = 1;
  public static final int CHECK_BACKGROUND = 2;
  public static final int POLICY_ALWAYS = 0;
  public static final int POLICY_PROMPT_UPDATE = 1;
  public static final int POLICY_PROMPT_RUN = 2;
  private int _check;
  private int _policy;

  public UpdateDesc(String paramString1, String paramString2)
  {
    if (paramString1.equalsIgnoreCase("always"))
      this._check = 0;
    else if (paramString1.equalsIgnoreCase("background"))
      this._check = 2;
    else
      this._check = 1;
    if (paramString2.equalsIgnoreCase("prompt-run"))
      this._policy = 2;
    else if (paramString2.equalsIgnoreCase("prompt-update"))
      this._policy = 1;
    else
      this._policy = 0;
  }

  public int getCheck()
  {
    return this._check;
  }

  public boolean isBackgroundCheck()
  {
    return 2 == this._check;
  }

  public boolean isPromptPolicy()
  {
    return this._policy != 0;
  }

  public int getPolicy()
  {
    return this._policy;
  }

  public String getCheckString()
  {
    if (this._check == 0)
      return "always";
    if (this._check == 1)
      return "timeout";
    return "background";
  }

  public String getPolicyString()
  {
    if (this._policy == 0)
      return "always";
    if (this._policy == 1)
      return "prompt-update";
    return "prompt-run";
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("check", getCheckString());
    localXMLAttributeBuilder.add("policy", getPolicyString());
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("update", localXMLAttributeBuilder.getAttributeList());
    return localXMLNodeBuilder.getNode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.UpdateDesc
 * JD-Core Version:    0.6.0
 */