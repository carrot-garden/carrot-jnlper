package com.sun.javaws.jnl;

import com.sun.deploy.config.Config;
import com.sun.deploy.util.OrderedHashSet;
import com.sun.deploy.util.Property;
import com.sun.deploy.util.URLUtil;
import com.sun.deploy.util.VersionString;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class ResourcesDesc
  implements ResourceType
{
  private ArrayList _list = null;
  private LaunchDesc _parent = null;
  private boolean _pack200Enabled = false;
  private boolean _versionEnabled = false;
  private int _concurrentDownloads = 0;

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

  public void setConcurrentDownloads(int paramInt)
  {
    this._concurrentDownloads = paramInt;
  }

  public int getConcurrentDownloads()
  {
    if (this._concurrentDownloads <= 0)
      return Property.CONCURRENT_DOWNLOADS_DEF;
    if (this._concurrentDownloads > 10)
      return 10;
    return this._concurrentDownloads;
  }

  public LaunchDesc getParent()
  {
    return this._parent;
  }

  void setParent(LaunchDesc paramLaunchDesc)
  {
    this._parent = paramLaunchDesc;
    for (int i = 0; i < this._list.size(); i++)
    {
      Object localObject = this._list.get(i);
      if (!(localObject instanceof JREDesc))
        continue;
      JREDesc localJREDesc = (JREDesc)localObject;
      if (localJREDesc.getNestedResources() == null)
        continue;
      localJREDesc.getNestedResources().setParent(paramLaunchDesc);
    }
  }

  public void addResource(ResourceType paramResourceType)
  {
    if (paramResourceType != null)
      this._list.add(paramResourceType);
  }

  boolean isEmpty()
  {
    return this._list.isEmpty();
  }

  public JREDesc getSelectedJRE()
  {
    for (int i = 0; i < this._list.size(); i++)
    {
      Object localObject = this._list.get(i);
      if (((localObject instanceof JREDesc)) && (((JREDesc)localObject).isSelected()))
        return (JREDesc)localObject;
    }
    return null;
  }

  public JARDesc[] getLocalJarDescs()
  {
    ArrayList localArrayList = new ArrayList(this._list.size());
    for (int i = 0; i < this._list.size(); i++)
    {
      Object localObject = this._list.get(i);
      if (!(localObject instanceof JARDesc))
        continue;
      localArrayList.add(localObject);
    }
    return toJARDescArray(localArrayList);
  }

  public ExtensionDesc[] getExtensionDescs()
  {
    ArrayList localArrayList = new ArrayList();
    ExtensionDesc[] arrayOfExtensionDesc = new ExtensionDesc[0];
    visit(new ResourceVisitor(localArrayList)
    {
      private final ArrayList val$l;

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        ResourcesDesc.this.addExtToList(this.val$l);
      }
    });
    return (ExtensionDesc[])(ExtensionDesc[])localArrayList.toArray(arrayOfExtensionDesc);
  }

  public JARDesc[] getEagerOrAllJarDescs(boolean paramBoolean)
  {
    HashSet localHashSet = new HashSet();
    if (!paramBoolean)
      visit(new ResourceVisitor(localHashSet)
      {
        private final HashSet val$eagerParts;

        public void visitJARDesc(JARDesc paramJARDesc)
        {
          if ((!paramJARDesc.isLazyDownload()) && (paramJARDesc.getPartName() != null))
            this.val$eagerParts.add(paramJARDesc.getPartName());
        }
      });
    ArrayList localArrayList = new ArrayList();
    addJarsToList(localArrayList, localHashSet, paramBoolean, true);
    return toJARDescArray(localArrayList);
  }

  private void addExtToList(ArrayList paramArrayList)
  {
    visit(new ResourceVisitor(paramArrayList)
    {
      private final ArrayList val$list;

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        if (paramExtensionDesc.getExtensionDesc() != null)
        {
          ResourcesDesc localResourcesDesc = paramExtensionDesc.getExtensionDesc().getResources();
          if (localResourcesDesc != null)
            localResourcesDesc.addExtToList(this.val$list);
        }
        this.val$list.add(paramExtensionDesc);
      }
    });
  }

  private void addJarsToList(ArrayList paramArrayList, HashSet paramHashSet, boolean paramBoolean1, boolean paramBoolean2)
  {
    visit(new ResourceVisitor(paramBoolean1, paramBoolean2, paramHashSet, paramArrayList)
    {
      private final boolean val$includeAll;
      private final boolean val$includeEager;
      private final HashSet val$includeParts;
      private final ArrayList val$list;

      public void visitJARDesc(JARDesc paramJARDesc)
      {
        if ((this.val$includeAll) || ((this.val$includeEager) && (!paramJARDesc.isLazyDownload())) || (this.val$includeParts.contains(paramJARDesc.getPartName())))
          this.val$list.add(paramJARDesc);
      }

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        HashSet localHashSet = paramExtensionDesc.getExtensionPackages(this.val$includeParts, this.val$includeEager);
        if (paramExtensionDesc.getExtensionDesc() != null)
        {
          ResourcesDesc localResourcesDesc = paramExtensionDesc.getExtensionDesc().getResources();
          if (localResourcesDesc != null)
            localResourcesDesc.addJarsToList(this.val$list, localHashSet, this.val$includeAll, this.val$includeEager);
        }
      }

      public void visitJREDesc(JREDesc paramJREDesc)
      {
        if (paramJREDesc.isSelected())
        {
          ResourcesDesc localResourcesDesc1 = paramJREDesc.getNestedResources();
          if (localResourcesDesc1 != null)
            localResourcesDesc1.addJarsToList(this.val$list, this.val$includeParts, this.val$includeAll, this.val$includeEager);
          if (paramJREDesc.getExtensionDesc() != null)
          {
            ResourcesDesc localResourcesDesc2 = paramJREDesc.getExtensionDesc().getResources();
            if (localResourcesDesc2 != null)
              localResourcesDesc2.addJarsToList(this.val$list, new HashSet(), this.val$includeAll, this.val$includeEager);
          }
        }
      }
    });
  }

  public JARDesc[] getPartJars(String[] paramArrayOfString)
  {
    HashSet localHashSet = new HashSet();
    for (int i = 0; i < paramArrayOfString.length; i++)
      localHashSet.add(paramArrayOfString[i]);
    ArrayList localArrayList = new ArrayList();
    addJarsToList(localArrayList, localHashSet, false, false);
    return toJARDescArray(localArrayList);
  }

  public JARDesc[] getPartJars(String paramString)
  {
    return getPartJars(new String[] { paramString });
  }

  public JARDesc[] getResource(URL paramURL, String paramString)
  {
    JARDesc[] arrayOfJARDesc = new JARDesc[1];
    visit(new ResourceVisitor(paramURL, paramString, arrayOfJARDesc)
    {
      private final URL val$location;
      private final String val$version;
      private final JARDesc[] val$resources;

      public void visitJARDesc(JARDesc paramJARDesc)
      {
        if (URLUtil.sameURLs(paramJARDesc.getLocation(), this.val$location))
        {
          Object localObject = paramJARDesc.getVersion() != null ? new VersionString(paramJARDesc.getVersion()) : null;
          if ((this.val$version == null) && (localObject == null))
            this.val$resources[0] = paramJARDesc;
          else if (localObject.contains(this.val$version))
            this.val$resources[0] = paramJARDesc;
        }
      }
    });
    if (arrayOfJARDesc[0] == null)
      return null;
    if (arrayOfJARDesc[0].getPartName() != null)
      return getPartJars(arrayOfJARDesc[0].getPartName());
    return arrayOfJARDesc;
  }

  public JARDesc[] getExtensionPart(URL paramURL, String paramString, String[] paramArrayOfString)
  {
    ExtensionDesc localExtensionDesc = findExtension(paramURL, paramString);
    if (localExtensionDesc == null)
      return null;
    ResourcesDesc localResourcesDesc = localExtensionDesc.getExtensionResources();
    if (localResourcesDesc == null)
      return null;
    return localResourcesDesc.getPartJars(paramArrayOfString);
  }

  private ExtensionDesc findExtension(URL paramURL, String paramString)
  {
    ExtensionDesc[] arrayOfExtensionDesc = new ExtensionDesc[1];
    visit(new ResourceVisitor(arrayOfExtensionDesc, paramURL, paramString)
    {
      private final ExtensionDesc[] val$ea;
      private final URL val$location;
      private final String val$version;

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        if (this.val$ea[0] == null)
          if ((URLUtil.sameURLs(paramExtensionDesc.getLocation(), this.val$location)) && ((this.val$version == null) || (new VersionString(this.val$version).contains(paramExtensionDesc.getVersion()))))
          {
            this.val$ea[0] = paramExtensionDesc;
          }
          else
          {
            LaunchDesc localLaunchDesc = paramExtensionDesc.getExtensionDesc();
            if ((localLaunchDesc != null) && (localLaunchDesc.getResources() != null))
              this.val$ea[0] = ResourcesDesc.access$200(localLaunchDesc.getResources(), this.val$location, this.val$version);
          }
      }
    });
    return arrayOfExtensionDesc[0];
  }

  public JARDesc getMainJar(boolean paramBoolean)
  {
    JARDesc[] arrayOfJARDesc = new JARDesc[2];
    visit(new ResourceVisitor(arrayOfJARDesc)
    {
      private final JARDesc[] val$results;

      public void visitJARDesc(JARDesc paramJARDesc)
      {
        if (paramJARDesc.isJavaFile())
        {
          if (this.val$results[0] == null)
            this.val$results[0] = paramJARDesc;
          if (paramJARDesc.isMainJarFile())
            this.val$results[1] = paramJARDesc;
        }
      }
    });
    JARDesc localJARDesc1 = arrayOfJARDesc[0];
    JARDesc localJARDesc2 = arrayOfJARDesc[1];
    return (localJARDesc2 == null) && (paramBoolean) ? localJARDesc1 : localJARDesc2;
  }

  public JARDesc[] getPart(String paramString)
  {
    ArrayList localArrayList = new ArrayList();
    visit(new ResourceVisitor(paramString, localArrayList)
    {
      private final String val$name;
      private final ArrayList val$l;

      public void visitJARDesc(JARDesc paramJARDesc)
      {
        if (this.val$name.equals(paramJARDesc.getPartName()))
          this.val$l.add(paramJARDesc);
      }
    });
    return toJARDescArray(localArrayList);
  }

  public JARDesc[] getExtensionPart(URL paramURL, String paramString1, String paramString2)
  {
    JARDesc[][] arrayOfJARDesc; = new JARDesc[1][];
    visit(new ResourceVisitor(paramURL, paramString1, arrayOfJARDesc;, paramString2)
    {
      private final URL val$url;
      private final String val$version;
      private final JARDesc[][] val$jdss;
      private final String val$part;

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        if (URLUtil.sameURLs(paramExtensionDesc.getLocation(), this.val$url))
          if (this.val$version == null)
          {
            if ((paramExtensionDesc.getVersion() == null) && (paramExtensionDesc.getExtensionResources() != null))
              this.val$jdss[0] = paramExtensionDesc.getExtensionResources().getPart(this.val$part);
          }
          else if ((this.val$version.equals(paramExtensionDesc.getVersion())) && (paramExtensionDesc.getExtensionResources() != null))
            this.val$jdss[0] = paramExtensionDesc.getExtensionResources().getPart(this.val$part);
      }
    });
    return arrayOfJARDesc;[0];
  }

  private JARDesc[] toJARDescArray(ArrayList paramArrayList)
  {
    JARDesc[] arrayOfJARDesc = new JARDesc[paramArrayList.size()];
    return (JARDesc[])(JARDesc[])paramArrayList.toArray(arrayOfJARDesc);
  }

  public Properties getResourceProperties()
  {
    Properties localProperties = new Properties();
    visit(new ResourceVisitor(localProperties)
    {
      private final Properties val$props;

      public void visitPropertyDesc(PropertyDesc paramPropertyDesc)
      {
        this.val$props.setProperty(paramPropertyDesc.getKey(), paramPropertyDesc.getValue());
      }
    });
    return localProperties;
  }

  public List getResourcePropertyList()
  {
    OrderedHashSet localOrderedHashSet = new OrderedHashSet();
    visit(new ResourceVisitor(localOrderedHashSet)
    {
      private final OrderedHashSet val$orderedProperties;

      public void visitPropertyDesc(PropertyDesc paramPropertyDesc)
      {
        this.val$orderedProperties.add(new Property(paramPropertyDesc.getKey(), paramPropertyDesc.getValue()));
      }
    });
    return localOrderedHashSet.toList();
  }

  public PackageInformation getPackageInformation(String paramString)
  {
    paramString = paramString.replace('/', '.');
    if (paramString.endsWith(".class"))
      paramString = paramString.substring(0, paramString.length() - 6);
    return visitPackageElements(getParent(), paramString);
  }

  public boolean isPackagePart(String paramString)
  {
    boolean[] arrayOfBoolean = { false };
    visit(new ResourceVisitor(arrayOfBoolean, paramString)
    {
      private final boolean[] val$result;
      private final String val$part;

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        if (!paramExtensionDesc.isInstaller())
        {
          LaunchDesc localLaunchDesc = paramExtensionDesc.getExtensionDesc();
          if ((this.val$result[0] == 0) && (localLaunchDesc != null) && (localLaunchDesc.isLibrary()) && (localLaunchDesc.getResources() != null))
            this.val$result[0] = localLaunchDesc.getResources().isPackagePart(this.val$part);
        }
      }

      public void visitPackageDesc(PackageDesc paramPackageDesc)
      {
        if (paramPackageDesc.getPart().equals(this.val$part))
          this.val$result[0] = true;
      }
    });
    return arrayOfBoolean[0];
  }

  private static PackageInformation visitPackageElements(LaunchDesc paramLaunchDesc, String paramString)
  {
    PackageInformation[] arrayOfPackageInformation = new PackageInformation[1];
    paramLaunchDesc.getResources().visit(new ResourceVisitor(arrayOfPackageInformation, paramString, paramLaunchDesc)
    {
      private final ResourcesDesc.PackageInformation[] val$result;
      private final String val$name;
      private final LaunchDesc val$ld;

      public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
      {
        if (!paramExtensionDesc.isInstaller())
        {
          LaunchDesc localLaunchDesc = paramExtensionDesc.getExtensionDesc();
          if ((this.val$result[0] == null) && (localLaunchDesc != null) && (localLaunchDesc.isLibrary()) && (localLaunchDesc.getResources() != null))
            this.val$result[0] = ResourcesDesc.access$300(localLaunchDesc, this.val$name);
        }
      }

      public void visitPackageDesc(PackageDesc paramPackageDesc)
      {
        if ((this.val$result[0] == null) && (paramPackageDesc.match(this.val$name)))
          this.val$result[0] = new ResourcesDesc.PackageInformation(this.val$ld, paramPackageDesc.getPart());
      }
    });
    return arrayOfPackageInformation[0];
  }

  public void visit(ResourceVisitor paramResourceVisitor)
  {
    for (int i = 0; i < this._list.size(); i++)
    {
      ResourceType localResourceType = (ResourceType)this._list.get(i);
      localResourceType.visit(paramResourceVisitor);
    }
  }

  public XMLNode asXML()
  {
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("resources", null);
    for (int i = 0; i < this._list.size(); i++)
    {
      ResourceType localResourceType = (ResourceType)this._list.get(i);
      localXMLNodeBuilder.add(localResourceType);
    }
    return localXMLNodeBuilder.getNode();
  }

  public void addNested(ResourcesDesc paramResourcesDesc)
  {
    if (paramResourcesDesc != null)
      paramResourcesDesc.visit(new ResourceVisitor()
      {
        public void visitJARDesc(JARDesc paramJARDesc)
        {
          ResourcesDesc.this._list.add(paramJARDesc);
        }

        public void visitPropertyDesc(PropertyDesc paramPropertyDesc)
        {
          ResourcesDesc.this._list.add(paramPropertyDesc);
        }

        public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
        {
          ResourcesDesc.this._list.add(paramExtensionDesc);
        }
      });
  }

  public static class PackageInformation
  {
    private LaunchDesc _launchDesc;
    private String _part;

    PackageInformation(LaunchDesc paramLaunchDesc, String paramString)
    {
      this._launchDesc = paramLaunchDesc;
      this._part = paramString;
    }

    public LaunchDesc getLaunchDesc()
    {
      return this._launchDesc;
    }

    public String getPart()
    {
      return this._part;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.ResourcesDesc
 * JD-Core Version:    0.6.0
 */