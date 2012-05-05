package com.sun.javaws.jnl;

import com.sun.deploy.config.JREInfo;
import com.sun.deploy.net.HttpUtils;
import com.sun.deploy.security.CachedCertificatesHelper;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.util.JVMParameters;
import com.sun.deploy.util.VersionID;
import com.sun.deploy.util.VersionString;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;
import com.sun.javaws.exceptions.JNLPSigningException;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class LaunchDesc
  implements XMLable
{
  private String _specVersion;
  private String _version;
  private final URL _home;
  private URL _codebase;
  private InformationDesc _information;
  private int _securiyModel;
  private UpdateDesc _update;
  private ResourcesDesc _resources;
  private int _launchType;
  private ApplicationDesc _applicationDesc;
  private AppletDesc _appletDesc;
  private JavaFXAppDesc _jfxDesc;
  private LibraryDesc _libraryDesc;
  private InstallerDesc _installerDesc;
  private String _internalCommand;
  private String _source = null;
  private boolean _propsSet = false;
  private byte[] _bits = null;
  private JREInfo _selectedJRE = null;
  private JREInfo _homeJRE = null;
  private MatchJREIf _matchImpl = null;
  private boolean _signed = false;
  private LDUpdater _updater = null;
  private final XMLNode _xmlNode;
  private CachedCertificatesHelper[] _certificates = null;
  private URL _originalURL = null;
  public static final int SANDBOX_SECURITY = 0;
  public static final int ALLPERMISSIONS_SECURITY = 1;
  public static final int J2EE_APP_CLIENT_SECURITY = 2;
  private boolean _trusted = false;
  public static final int APPLICATION_DESC_TYPE = 1;
  public static final int APPLET_DESC_TYPE = 2;
  public static final int LIBRARY_DESC_TYPE = 3;
  public static final int INSTALLER_DESC_TYPE = 4;
  public static final int INTERNAL_TYPE = 5;
  public static final int FXAPP_TYPE = 6;

  public LaunchDesc(String paramString1, URL paramURL1, URL paramURL2, String paramString2, InformationDesc paramInformationDesc, int paramInt1, CachedCertificatesHelper[] paramArrayOfCachedCertificatesHelper, UpdateDesc paramUpdateDesc, ResourcesDesc paramResourcesDesc, int paramInt2, ApplicationDesc paramApplicationDesc, AppletDesc paramAppletDesc, JavaFXAppDesc paramJavaFXAppDesc, LibraryDesc paramLibraryDesc, InstallerDesc paramInstallerDesc, String paramString3, XMLNode paramXMLNode, MatchJREIf paramMatchJREIf)
  {
    this._specVersion = paramString1;
    this._version = paramString2;
    this._codebase = paramURL1;
    this._home = paramURL2;
    this._information = paramInformationDesc;
    this._securiyModel = paramInt1;
    this._update = paramUpdateDesc;
    this._resources = paramResourcesDesc;
    this._launchType = paramInt2;
    this._applicationDesc = paramApplicationDesc;
    this._appletDesc = paramAppletDesc;
    this._jfxDesc = paramJavaFXAppDesc;
    this._libraryDesc = paramLibraryDesc;
    this._installerDesc = paramInstallerDesc;
    this._internalCommand = paramString3;
    this._xmlNode = paramXMLNode;
    this._matchImpl = paramMatchJREIf;
    this._signed = false;
    this._certificates = paramArrayOfCachedCertificatesHelper;
    if (this._resources != null)
    {
      this._resources.setParent(this);
      if ((isApplicationDescriptor()) || (isInstaller()))
      {
        JARDesc localJARDesc = this._resources.getMainJar(true);
        if (localJARDesc != null)
          localJARDesc.setLazyDownload(false);
      }
    }
  }

  public CachedCertificatesHelper[] getCachedCertificates()
  {
    return this._certificates;
  }

  public URL getSourceURL()
  {
    return this._originalURL;
  }

  public void setSourceURL(URL paramURL)
  {
    this._originalURL = paramURL;
  }

  public JREInfo getHomeJRE()
  {
    if (this._homeJRE == null)
      this._homeJRE = JREInfo.getHomeJRE();
    return this._homeJRE;
  }

  public MatchJREIf getJREMatcher()
  {
    if (!this._matchImpl.hasBeenRun())
      selectJRE();
    return this._matchImpl;
  }

  public JREInfo selectJRE()
  {
    this._selectedJRE = LaunchSelection.selectJRE(this, this._matchImpl);
    return this._selectedJRE;
  }

  public JREInfo selectJRE(MatchJREIf paramMatchJREIf)
  {
    this._matchImpl = paramMatchJREIf;
    return selectJRE();
  }

  public String getSpecVersion()
  {
    return this._specVersion;
  }

  public synchronized URL getCodebase()
  {
    return this._codebase;
  }

  public byte[] getBytes()
  {
    if (this._bits == null)
      this._bits = getSource().getBytes();
    return this._bits;
  }

  public synchronized URL getLocation()
  {
    return this._home;
  }

  public synchronized URL getCanonicalHome()
  {
    if ((this._home == null) && (this._resources != null))
    {
      JARDesc localJARDesc = this._resources.getMainJar(true);
      URL localURL1 = null;
      try
      {
        Object localObject;
        if (localJARDesc != null)
        {
          localObject = HttpUtils.removeQueryStringFromURL(localJARDesc.getLocation());
          localURL1 = new URL(((URL)localObject).toString() + "jnlp");
        }
        else
        {
          localObject = this._resources.getExtensionDescs();
          if (localObject.length > 0)
          {
            URL localURL2 = HttpUtils.removeQueryStringFromURL(localObject[0].getLocation());
            localURL1 = new URL(localURL2.toString() + ".jarjnlp");
          }
        }
      }
      catch (MalformedURLException localMalformedURLException)
      {
        Trace.ignoredException(localMalformedURLException);
      }
      return localURL1 != null ? localURL1 : null;
    }
    return (URL)this._home;
  }

  public synchronized String getSplashCanonicalHome()
  {
    if ((this._home == null) && (this._resources != null))
    {
      JARDesc localJARDesc = this._resources.getMainJar(true);
      return localJARDesc != null ? localJARDesc.getLocation().toString() + "jnlp" : null;
    }
    return this._home.toString();
  }

  public InformationDesc getInformation()
  {
    return this._information;
  }

  public String getInternalCommand()
  {
    return this._internalCommand;
  }

  public int getSecurityModel()
  {
    return this._securiyModel;
  }

  public UpdateDesc getUpdate()
  {
    return this._update;
  }

  public boolean isSigned()
  {
    return this._signed;
  }

  public boolean hasIdenticalContent(LaunchDesc paramLaunchDesc)
  {
    return (this._xmlNode != null) && (paramLaunchDesc != null) && (this._xmlNode.equals(paramLaunchDesc._xmlNode));
  }

  public boolean hasIdenticalContent(File paramFile)
  {
    Object localObject = null;
    try
    {
      byte[] arrayOfByte = LaunchDescFactory.readBytes(new FileInputStream(paramFile), paramFile.length());
      XMLNode localXMLNode = XMLFormat.parseBits(arrayOfByte);
      return this._xmlNode.equals(localXMLNode);
    }
    catch (Exception localException)
    {
    }
    return false;
  }

  public boolean equalsTemplate(XMLNode paramXMLNode)
  {
    return (this._xmlNode != null) && (!XMLFormat.isBlacklisted(paramXMLNode)) && (this._xmlNode.equalsTemplate(paramXMLNode));
  }

  public boolean checkSigningTemplate(byte[] paramArrayOfByte)
    throws JNLPSigningException
  {
    XMLNode localXMLNode = null;
    try
    {
      localXMLNode = XMLFormat.parseBits(paramArrayOfByte);
      if (equalsTemplate(localXMLNode))
      {
        this._signed = true;
        return true;
      }
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
    throw new JNLPSigningException(this, localXMLNode == null ? new String(paramArrayOfByte) : localXMLNode.toString(true));
  }

  public void setTrusted()
  {
    Trace.println("Mark trusted: " + this._home, TraceLevel.SECURITY);
    this._trusted = true;
  }

  public boolean isTrusted()
  {
    Trace.println("Istrusted: " + this._home + " " + this._trusted, TraceLevel.SECURITY);
    return this._trusted;
  }

  public boolean isSecure()
  {
    return 0 == getSecurityModel();
  }

  public boolean isSecureJVMArgs()
  {
    return getJREMatcher().getSelectedJVMParameters().isSecure();
  }

  public ResourcesDesc getResources()
  {
    return this._resources;
  }

  public boolean arePropsSet()
  {
    return this._propsSet;
  }

  public void setPropsSet(boolean paramBoolean)
  {
    this._propsSet = paramBoolean;
  }

  public JREInfo getSelectedJRE()
  {
    if (this._selectedJRE == null)
      selectJRE();
    return this._selectedJRE;
  }

  public int getLaunchType()
  {
    return this._launchType;
  }

  public ApplicationDesc getApplicationDescriptor()
  {
    return this._applicationDesc;
  }

  public AppletDesc getAppletDescriptor()
  {
    return this._appletDesc;
  }

  public JavaFXAppDesc getJavaFXAppDescriptor()
  {
    return this._jfxDesc;
  }

  public InstallerDesc getInstallerDescriptor()
  {
    return this._installerDesc;
  }

  public final boolean isApplication()
  {
    return this._launchType == 1;
  }

  public final boolean isApplet()
  {
    return this._launchType == 2;
  }

  public final boolean isLibrary()
  {
    return this._launchType == 3;
  }

  public final boolean isInstaller()
  {
    return this._launchType == 4;
  }

  public final boolean isFXAppOnly()
  {
    return this._launchType == 6;
  }

  public final boolean isFXApp()
  {
    return getJavaFXAppDescriptor() != null;
  }

  public final boolean needFX()
  {
    return (isFXApp()) || (null != getJavaFXRuntimeDescriptor());
  }

  public final boolean isApplicationDescriptor()
  {
    return (isApplication()) || (isApplet()) || (isFXAppOnly());
  }

  public boolean isHttps()
  {
    if (this._codebase != null)
      return this._codebase.getProtocol().equals("https");
    getCanonicalHome();
    if (this._home != null)
      return this._home.getProtocol().equals("https");
    return false;
  }

  public String getSource()
  {
    if (this._source == null)
      this._source = this._xmlNode.toString();
    return this._source;
  }

  public XMLNode getXmlNode()
  {
    return this._xmlNode;
  }

  public void checkSigning(LaunchDesc paramLaunchDesc)
    throws JNLPSigningException
  {
    if ((paramLaunchDesc != null) && (paramLaunchDesc.getXmlNode().equals(getXmlNode())))
      this._signed = true;
    else
      throw new JNLPSigningException(this, paramLaunchDesc.getXmlNode().toString(true));
  }

  public boolean isJRESpecified()
  {
    boolean[] arrayOfBoolean1 = new boolean[1];
    boolean[] arrayOfBoolean2 = new boolean[1];
    if (getResources() != null)
      getResources().visit(new ResourceVisitor(arrayOfBoolean2, arrayOfBoolean1)
      {
        private final boolean[] val$needJre;
        private final boolean[] val$hasJre;

        public void visitJARDesc(JARDesc paramJARDesc)
        {
          this.val$needJre[0] = true;
        }

        public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
        {
          this.val$needJre[0] = true;
        }

        public void visitJREDesc(JREDesc paramJREDesc)
        {
          this.val$hasJre[0] = true;
        }
      });
    if ((this._launchType == 1) || (this._launchType == 2))
      arrayOfBoolean2[0] = true;
    return (arrayOfBoolean1[0] != 0) || (arrayOfBoolean2[0] == 0);
  }

  public AppInfo getAppInfo()
  {
    AppInfo localAppInfo = new AppInfo(getLaunchType(), this._information.getTitle(), this._information.getVendor(), getCanonicalHome(), null, null, false, false, null, null);
    IconDesc localIconDesc = this._information.getIconLocation(48, 0);
    if (localIconDesc != null)
    {
      localAppInfo.setIconRef(localIconDesc.getLocation());
      localAppInfo.setIconVersion(localIconDesc.getVersion());
    }
    return localAppInfo;
  }

  public synchronized LDUpdater getUpdater()
  {
    if (this._updater == null)
      this._updater = new LDUpdater(this);
    return this._updater;
  }

  public String getProgressClassName()
  {
    String str = null;
    if (this._jfxDesc != null)
      str = this._jfxDesc.getPreloaderClass();
    else if (this._applicationDesc != null)
      str = this._applicationDesc.getProgressClass();
    else if (this._appletDesc != null)
      str = this._appletDesc.getProgressClass();
    else if (this._libraryDesc != null)
      str = this._libraryDesc.getProgressClass();
    else
      str = null;
    if (str != null)
      return str;
    String[] arrayOfString = new String[1];
    if (getResources() != null)
      getResources().visit(new ResourceVisitor(arrayOfString)
      {
        private final String[] val$results;

        public void visitExtensionDesc(ExtensionDesc paramExtensionDesc)
        {
          LaunchDesc localLaunchDesc = paramExtensionDesc.getExtensionDesc();
          if ((localLaunchDesc != null) && (this.val$results[0] == null))
            this.val$results[0] = localLaunchDesc.getProgressClassName();
        }
      });
    return arrayOfString[0];
  }

  public JavaFXRuntimeDesc getJavaFXRuntimeDescriptor()
  {
    JavaFXRuntimeDesc[] arrayOfJavaFXRuntimeDesc = { null };
    if (getResources() != null)
      getResources().visit(new ResourceVisitor(arrayOfJavaFXRuntimeDesc)
      {
        private final JavaFXRuntimeDesc[] val$jfxd;

        public void visitJFXDesc(JavaFXRuntimeDesc paramJavaFXRuntimeDesc)
        {
          this.val$jfxd[0] = paramJavaFXRuntimeDesc;
        }
      });
    return arrayOfJavaFXRuntimeDesc[0];
  }

  public boolean isValidSpecificationVersion()
  {
    VersionString localVersionString = new VersionString(getSpecVersion());
    return (localVersionString.contains(new VersionID("7.0"))) || (localVersionString.contains(new VersionID("6.0.18"))) || (localVersionString.contains(new VersionID("6.0.10"))) || (localVersionString.contains(new VersionID("6.0"))) || (localVersionString.contains(new VersionID("1.5"))) || (localVersionString.contains(new VersionID("1.0")));
  }

  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder1 = new XMLAttributeBuilder();
    localXMLAttributeBuilder1.add("spec", this._specVersion);
    localXMLAttributeBuilder1.add("codebase", this._codebase);
    localXMLAttributeBuilder1.add("version", this._version);
    localXMLAttributeBuilder1.add("href", this._home);
    XMLNodeBuilder localXMLNodeBuilder1 = new XMLNodeBuilder("jnlp", localXMLAttributeBuilder1.getAttributeList());
    localXMLNodeBuilder1.add(this._information);
    if (this._securiyModel != 0)
    {
      XMLNode localXMLNode = null;
      String str = "all-permissions";
      if (this._securiyModel == 2)
        str = "j2ee-application-client-permissions";
      if (this._certificates != null)
      {
        XMLAttributeBuilder localXMLAttributeBuilder2 = new XMLAttributeBuilder();
        localXMLAttributeBuilder2.add("signedjnlp", this._certificates[0].isSignedJNLP());
        XMLNodeBuilder localXMLNodeBuilder2 = new XMLNodeBuilder("security", null);
        XMLNodeBuilder localXMLNodeBuilder3 = new XMLNodeBuilder("jfx:details", localXMLAttributeBuilder2.getAttributeList());
        for (int i = 0; i < this._certificates.length; i++)
        {
          XMLAttributeBuilder localXMLAttributeBuilder3 = new XMLAttributeBuilder();
          if (this._certificates[i].getTimestamp() != null)
            localXMLAttributeBuilder3.add("timestamp", "" + this._certificates[i].getTimestamp().getTime());
          XMLNodeBuilder localXMLNodeBuilder4 = new XMLNodeBuilder("jfx:certificate-path", localXMLAttributeBuilder3.getAttributeList());
          localXMLNodeBuilder4.add(new XMLNode(this._certificates[i].exportCertificatesToBase64()));
          localXMLNodeBuilder3.add(localXMLNodeBuilder4.getNode());
        }
        localXMLNodeBuilder2.add(new XMLNode(str, null));
        localXMLNodeBuilder2.add(localXMLNodeBuilder3.getNode());
        localXMLNode = localXMLNodeBuilder2.getNode();
      }
      else
      {
        localXMLNode = new XMLNode("security", null, new XMLNode(str, null), null);
      }
      localXMLNodeBuilder1.add(localXMLNode);
    }
    localXMLNodeBuilder1.add(this._update);
    localXMLNodeBuilder1.add(this._resources);
    localXMLNodeBuilder1.add(this._applicationDesc);
    localXMLNodeBuilder1.add(this._appletDesc);
    localXMLNodeBuilder1.add(this._jfxDesc);
    localXMLNodeBuilder1.add(this._libraryDesc);
    localXMLNodeBuilder1.add(this._installerDesc);
    return localXMLNodeBuilder1.getNode();
  }

  public String toString()
  {
    return asXML().toString();
  }

  public String getVersion()
  {
    return this._version;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.LaunchDesc
 * JD-Core Version:    0.6.0
 */