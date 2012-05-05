package com.sun.javaws.jnl;

import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.URLUtil;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import java.io.File;
import java.net.URL;
import java.util.HashSet;

public class ExtensionDesc
  implements ResourceType
{
  private String _name;
  private URL _location;
  private URL _codebase;
  private String _version;
  private boolean _isInstaller;
  private ExtDownloadDesc[] _extDownloadDescs;
  private LaunchDesc _extensionLd;

  public ExtensionDesc(String paramString1, URL paramURL, String paramString2, ExtDownloadDesc[] paramArrayOfExtDownloadDesc)
  {
    this._name = paramString1;
    this._location = paramURL;
    this._codebase = URLUtil.asPathURL(URLUtil.getBase(paramURL));
    this._version = paramString2;
    if (paramArrayOfExtDownloadDesc == null)
      paramArrayOfExtDownloadDesc = new ExtDownloadDesc[0];
    this._extDownloadDescs = paramArrayOfExtDownloadDesc;
    this._extensionLd = null;
    this._isInstaller = false;
  }

  public void setInstaller(boolean paramBoolean)
  {
    this._isInstaller = paramBoolean;
  }

  public boolean isInstaller()
  {
    return this._isInstaller;
  }

  public String getVersion()
  {
    return this._version;
  }

  public URL getLocation()
  {
    return this._location;
  }

  public URL getCodebase()
  {
    return this._codebase;
  }

  public String getName()
  {
    return this._name;
  }

  ExtDownloadDesc[] getExtDownloadDescs()
  {
    return this._extDownloadDescs;
  }

  public LaunchDesc getExtensionDesc()
  {
    if (this._extensionLd == null)
      try
      {
        File localFile = DownloadEngine.getCachedFile(getLocation(), getVersion());
        if (localFile != null)
          this._extensionLd = LaunchDescFactory.buildDescriptor(localFile, getCodebase(), getLocation(), getLocation());
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    return this._extensionLd;
  }

  public void setExtensionDesc(LaunchDesc paramLaunchDesc)
  {
    this._extensionLd = paramLaunchDesc;
  }

  ResourcesDesc getExtensionResources()
  {
    return this._extensionLd.getResources();
  }

  HashSet getExtensionPackages(HashSet paramHashSet, boolean paramBoolean)
  {
    HashSet localHashSet = new HashSet();
    for (int i = 0; i < this._extDownloadDescs.length; i++)
    {
      ExtDownloadDesc localExtDownloadDesc = this._extDownloadDescs[i];
      int j = (paramBoolean) && (!localExtDownloadDesc.isLazy()) ? 1 : 0;
      if ((j == 0) && ((paramHashSet == null) || (!paramHashSet.contains(localExtDownloadDesc.getPart()))))
        continue;
      localHashSet.add(localExtDownloadDesc.getExtensionPart());
    }
    return localHashSet;
  }

  public void visit(ResourceVisitor paramResourceVisitor)
  {
    paramResourceVisitor.visitExtensionDesc(this);
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("href", this._location);
    localXMLAttributeBuilder.add("version", this._version);
    localXMLAttributeBuilder.add("name", this._name);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("extension", localXMLAttributeBuilder.getAttributeList());
    for (int i = 0; i < this._extDownloadDescs.length; i++)
      localXMLNodeBuilder.add(this._extDownloadDescs[i]);
    return localXMLNodeBuilder.getNode();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.ExtensionDesc
 * JD-Core Version:    0.6.0
 */