package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;

public class ShortcutDesc
  implements XMLable
{
  private boolean _online;
  private boolean _install;
  private boolean _desktop;
  private boolean _menu;
  private String _submenu;

  public ShortcutDesc(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, String paramString)
  {
    this._online = paramBoolean1;
    this._install = paramBoolean2;
    this._desktop = paramBoolean3;
    this._menu = paramBoolean4;
    this._submenu = paramString;
  }

  public boolean getOnline()
  {
    return this._online;
  }

  public boolean getInstall()
  {
    return this._install;
  }

  public boolean getDesktop()
  {
    return this._desktop;
  }

  public boolean getMenu()
  {
    return this._menu;
  }

  public String getSubmenu()
  {
    return this._submenu;
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("online", this._online);
    localXMLAttributeBuilder.add("install", this._install);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("shortcut", localXMLAttributeBuilder.getAttributeList());
    if (this._desktop)
      localXMLNodeBuilder.add(new XMLNode("desktop", null));
    if (this._menu)
      if (this._submenu == null)
        localXMLNodeBuilder.add(new XMLNode("menu", null));
      else
        localXMLNodeBuilder.add(new XMLNode("menu", new XMLAttribute("submenu", this._submenu)));
    return localXMLNodeBuilder.getNode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.ShortcutDesc
 * JD-Core Version:    0.6.0
 */