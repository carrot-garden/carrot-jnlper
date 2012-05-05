package com.sun.javaws.jnl;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import java.net.URL;

public class JREDesc
  implements ResourceType
{
  private String _version;
  private long _maxHeap;
  private long _minHeap;
  private String _vmargs;
  private URL _href;
  private boolean _isSelected;
  private ResourcesDesc _resourceDesc;
  private LaunchDesc _extensioDesc;

  public JREDesc(String paramString1, long paramLong1, long paramLong2, String paramString2, URL paramURL, ResourcesDesc paramResourcesDesc)
  {
    this._version = paramString1;
    this._maxHeap = paramLong2;
    this._minHeap = paramLong1;
    this._vmargs = paramString2;
    this._href = paramURL;
    this._isSelected = false;
    this._resourceDesc = paramResourcesDesc;
    this._extensioDesc = null;
  }

  public String toString()
  {
    return "JREDesc[version " + this._version + ", heap=" + this._minHeap + "-" + this._maxHeap + ", args=" + this._vmargs + ", href=" + this._href + ", sel=" + this._isSelected + ", " + this._resourceDesc + ", " + this._extensioDesc + "]";
  }

  public String getVersion()
  {
    return this._version;
  }

  public URL getHref()
  {
    return this._href;
  }

  public long getMinHeap()
  {
    return this._minHeap;
  }

  public long getMaxHeap()
  {
    return this._maxHeap;
  }

  public String getVmArgs()
  {
    return this._vmargs;
  }

  public boolean isSelected()
  {
    return this._isSelected;
  }

  public void markAsSelected()
  {
    this._isSelected = true;
  }

  public ResourcesDesc getNestedResources()
  {
    return this._resourceDesc;
  }

  public LaunchDesc getExtensionDesc()
  {
    return this._extensioDesc;
  }

  public void setExtensionDesc(LaunchDesc paramLaunchDesc)
  {
    this._extensioDesc = paramLaunchDesc;
  }

  public void visit(ResourceVisitor paramResourceVisitor)
  {
    paramResourceVisitor.visitJREDesc(this);
  }

  public String getSource()
  {
    if (this._href != null)
      return this._href.getHost();
    try
    {
      URL localURL = new URL(Config.getStringProperty("deployment.javaws.installURL"));
      return localURL.getHost();
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
    return "";
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    if (this._minHeap > 0L)
      localXMLAttributeBuilder.add("initial-heap-size", this._minHeap);
    if (this._maxHeap > 0L)
      localXMLAttributeBuilder.add("max-heap-size", this._maxHeap);
    if (this._vmargs != null)
      localXMLAttributeBuilder.add("java-vm-args", this._vmargs);
    localXMLAttributeBuilder.add("href", this._href);
    if (this._version != null)
      localXMLAttributeBuilder.add("version", this._version);
    XMLNode localXMLNode = this._extensioDesc != null ? this._extensioDesc.asXML() : null;
    if (this._resourceDesc != null)
      localXMLNode = this._resourceDesc.asXML();
    return new XMLNode("java", localXMLAttributeBuilder.getAttributeList(), localXMLNode, null);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.JREDesc
 * JD-Core Version:    0.6.0
 */