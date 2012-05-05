package com.sun.javaws.jnl;

import com.sun.deploy.cache.AssociationDesc;
import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;
import java.net.URL;

public class InformationDesc
  implements XMLable
{
  private String _title;
  private String _vendor;
  private URL _home;
  private String[] _descriptions;
  private IconDesc[] _icons;
  private ShortcutDesc _shortcutHints;
  private AssociationDesc[] _associations;
  private RContentDesc[] _relatedContent;
  private boolean _supportOfflineOperation;
  public static final int DESC_DEFAULT = 0;
  public static final int DESC_SHORT = 1;
  public static final int DESC_ONELINE = 2;
  public static final int DESC_TOOLTIP = 3;
  public static final int NOF_DESC = 4;

  public InformationDesc(String paramString1, String paramString2, URL paramURL, String[] paramArrayOfString, IconDesc[] paramArrayOfIconDesc, ShortcutDesc paramShortcutDesc, RContentDesc[] paramArrayOfRContentDesc, AssociationDesc[] paramArrayOfAssociationDesc, boolean paramBoolean)
  {
    this._title = paramString1;
    this._vendor = paramString2;
    this._home = paramURL;
    if (paramArrayOfString == null)
      paramArrayOfString = new String[4];
    this._descriptions = paramArrayOfString;
    this._icons = paramArrayOfIconDesc;
    this._shortcutHints = paramShortcutDesc;
    this._associations = paramArrayOfAssociationDesc;
    this._relatedContent = paramArrayOfRContentDesc;
    this._supportOfflineOperation = paramBoolean;
  }

  public String getTitle()
  {
    return this._title;
  }

  public String getVendor()
  {
    return this._vendor;
  }

  public URL getHome()
  {
    return this._home;
  }

  public boolean supportsOfflineOperation()
  {
    return this._supportOfflineOperation;
  }

  public IconDesc[] getIcons()
  {
    return this._icons;
  }

  public ShortcutDesc getShortcut()
  {
    return this._shortcutHints;
  }

  public AssociationDesc[] getAssociations()
  {
    return this._associations;
  }

  public boolean hintsInstall()
  {
    return false;
  }

  public void setShortcut(ShortcutDesc paramShortcutDesc)
  {
    this._shortcutHints = paramShortcutDesc;
  }

  public void setAssociation(AssociationDesc paramAssociationDesc)
  {
    this._associations = new AssociationDesc[] { paramAssociationDesc };
  }

  public RContentDesc[] getRelatedContent()
  {
    return this._relatedContent;
  }

  public String getDescription(int paramInt)
  {
    return this._descriptions[paramInt];
  }

  public IconDesc getIconLocation(int paramInt1, int paramInt2)
  {
    if (this._icons == null)
      return null;
    Object localObject = null;
    long l1 = 0L;
    for (int i = 0; i < this._icons.length; i++)
    {
      IconDesc localIconDesc = this._icons[i];
      int j = (paramInt2 == 5) || (!localIconDesc.getSuffix().equalsIgnoreCase(".ico")) ? 1 : 0;
      if ((localIconDesc.getKind() != paramInt2) || (j == 0))
        continue;
      if ((localIconDesc.getHeight() == paramInt1) && (localIconDesc.getWidth() == paramInt1))
        return localIconDesc;
      if ((localIconDesc.getHeight() == 0) && (localIconDesc.getWidth() == 0))
      {
        if (localObject != null)
          continue;
        localObject = localIconDesc;
      }
      else
      {
        int k = localIconDesc.getHeight() + localIconDesc.getWidth() - 2 * paramInt1;
        long l2 = Math.abs(k);
        if ((l1 == 0L) || (l2 < l1))
        {
          l1 = l2;
          localObject = localIconDesc;
        }
        else
        {
          if ((l2 != l1) || (k <= 0))
            continue;
          localObject = localIconDesc;
        }
      }
    }
    return localObject;
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("information", localXMLAttributeBuilder.getAttributeList());
    localXMLNodeBuilder.add("title", this._title);
    localXMLNodeBuilder.add("vendor", this._vendor);
    localXMLNodeBuilder.add(new XMLNode("homepage", new XMLAttribute("href", this._home != null ? this._home.toString() : null), null, null));
    localXMLNodeBuilder.add(getDescriptionNode(0, ""));
    localXMLNodeBuilder.add(getDescriptionNode(1, "short"));
    localXMLNodeBuilder.add(getDescriptionNode(2, "one-line"));
    localXMLNodeBuilder.add(getDescriptionNode(3, "tooltip"));
    int i;
    if (this._icons != null)
      for (i = 0; i < this._icons.length; i++)
        localXMLNodeBuilder.add(this._icons[i]);
    if (this._shortcutHints != null)
      localXMLNodeBuilder.add(this._shortcutHints);
    if (this._associations != null)
      for (i = 0; i < this._associations.length; i++)
        localXMLNodeBuilder.add(this._associations[i]);
    if (this._relatedContent != null)
      for (i = 0; i < this._relatedContent.length; i++)
        localXMLNodeBuilder.add(this._relatedContent[i]);
    if (this._supportOfflineOperation)
      localXMLNodeBuilder.add(new XMLNode("offline-allowed", null));
    return localXMLNodeBuilder.getNode();
  }

  private XMLNode getDescriptionNode(int paramInt, String paramString)
  {
    String str = this._descriptions[paramInt];
    if (str == null)
      return null;
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("kind", paramString);
    return new XMLNode("description", localXMLAttributeBuilder.getAttributeList(), new XMLNode(str), null);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.InformationDesc
 * JD-Core Version:    0.6.0
 */