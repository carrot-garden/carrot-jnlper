package com.sun.javaws.jnl;

import com.sun.deploy.config.Config;
import com.sun.deploy.util.URLUtil;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import java.net.URL;

public class JARDesc
  implements ResourceType
{
  private URL _location;
  private String _locationString;
  private String _version;
  private int _size;
  private boolean _isNativeLib;
  private boolean _isLazyDownload;
  private boolean _isProgressDownload;
  private boolean _isMainFile;
  private String _part;
  private ResourcesDesc _parent;
  private JARUpdater _updater = null;
  private boolean _pack200Enabled = false;
  private boolean _versionEnabled = false;

  public JARDesc(URL paramURL, String paramString1, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, String paramString2, int paramInt, ResourcesDesc paramResourcesDesc)
  {
    this(paramURL, paramString1, paramBoolean1, paramBoolean2, paramBoolean3, paramString2, paramInt, paramResourcesDesc, false);
  }

  public JARDesc(URL paramURL, String paramString1, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, String paramString2, int paramInt, ResourcesDesc paramResourcesDesc, boolean paramBoolean4)
  {
    this._location = paramURL;
    this._locationString = URLUtil.toNormalizedString(paramURL);
    this._version = paramString1;
    this._isLazyDownload = ((paramBoolean1) && (!this._isMainFile));
    this._isNativeLib = paramBoolean3;
    this._isMainFile = paramBoolean2;
    this._part = paramString2;
    this._size = paramInt;
    this._parent = paramResourcesDesc;
    this._isProgressDownload = paramBoolean4;
  }

  public void setPack200Enabled()
  {
    this._pack200Enabled = true;
  }

  public void setVersionEnabled()
  {
    this._versionEnabled = true;
  }

  public boolean isPack200Enabled()
  {
    if (Config.isJavaVersionAtLeast15())
      return this._pack200Enabled;
    return false;
  }

  public boolean isVersionEnabled()
  {
    return this._versionEnabled;
  }

  public boolean isNativeLib()
  {
    return this._isNativeLib;
  }

  public boolean isJavaFile()
  {
    return !this._isNativeLib;
  }

  public boolean isProgressJar()
  {
    return this._isProgressDownload;
  }

  public URL getLocation()
  {
    return this._location;
  }

  public String getLocationString()
  {
    return this._locationString;
  }

  public String getVersion()
  {
    return this._version;
  }

  public boolean isLazyDownload()
  {
    return this._isLazyDownload;
  }

  public void setLazyDownload(boolean paramBoolean)
  {
    this._isLazyDownload = paramBoolean;
  }

  public boolean isMainJarFile()
  {
    return this._isMainFile;
  }

  public String getPartName()
  {
    return this._part;
  }

  public int getSize()
  {
    return this._size;
  }

  public ResourcesDesc getParent()
  {
    return this._parent;
  }

  public void visit(ResourceVisitor paramResourceVisitor)
  {
    paramResourceVisitor.visitJARDesc(this);
  }

  public synchronized JARUpdater getUpdater()
  {
    if (this._updater == null)
      this._updater = new JARUpdater(this);
    return this._updater;
  }

  public XMLNode asXML()
  {
    XMLNode localXMLNode = null;
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("href", this._location);
    localXMLAttributeBuilder.add("version", this._version);
    localXMLAttributeBuilder.add("part", this._part);
    localXMLAttributeBuilder.add("download", isLazyDownload() ? "lazy" : isProgressJar() ? "progress" : "eager");
    localXMLAttributeBuilder.add("main", isMainJarFile() ? "true" : "false");
    String str = this._isNativeLib ? "nativelib" : "jar";
    localXMLNode = new XMLNode(str, localXMLAttributeBuilder.getAttributeList());
    return localXMLNode;
  }

  public String toString()
  {
    return "JARDesc[" + this._locationString + ":" + this._version + "]";
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof JARDesc))
      return false;
    if (this == paramObject)
      return true;
    JARDesc localJARDesc = (JARDesc)paramObject;
    if (getVersion() != null)
      return (getVersion().equals(localJARDesc.getVersion())) && (this._locationString.equals(localJARDesc._locationString));
    return (localJARDesc.getVersion() == null) && (this._locationString.equals(localJARDesc._locationString));
  }

  public int hashCode()
  {
    int i = 0;
    if (getVersion() != null)
      i = getVersion().hashCode();
    if (this._locationString != null)
      i ^= this._locationString.hashCode();
    return i;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.JARDesc
 * JD-Core Version:    0.6.0
 */