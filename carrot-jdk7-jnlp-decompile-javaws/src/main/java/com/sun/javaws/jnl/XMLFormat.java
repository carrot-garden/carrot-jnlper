package com.sun.javaws.jnl;

import com.sun.deploy.Environment;
import com.sun.deploy.cache.AssociationDesc;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.config.Config;
import com.sun.deploy.security.CachedCertificatesHelper;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.URLUtil;
import com.sun.deploy.xml.BadTokenException;
import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLEncoding;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLParser;
import com.sun.javaws.Globals;
import com.sun.javaws.exceptions.BadFieldException;
import com.sun.javaws.exceptions.JNLParseException;
import com.sun.javaws.exceptions.MissingFieldException;
import com.sun.javaws.util.GeneralUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class XMLFormat
{
  public static LaunchDesc parse(byte[] paramArrayOfByte, URL paramURL1, URL paramURL2, URL paramURL3)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    return parse(paramArrayOfByte, paramURL1, paramURL2, paramURL3, new DefaultMatchJRE());
  }

  public static XMLNode parseBits(byte[] paramArrayOfByte)
    throws JNLParseException
  {
    return parse(decode(paramArrayOfByte));
  }

  private static String decode(byte[] paramArrayOfByte)
    throws JNLParseException
  {
    try
    {
      return XMLEncoding.decodeXML(paramArrayOfByte);
    }
    catch (Exception localException)
    {
    }
    throw new JNLParseException(null, localException, "exception determining encoding of jnlp file", 0);
  }

  private static XMLNode parse(String paramString)
    throws JNLParseException
  {
    try
    {
      return new XMLParser(paramString).parse();
    }
    catch (BadTokenException localBadTokenException)
    {
      throw new JNLParseException(paramString, localBadTokenException, "wrong kind of token found", localBadTokenException.getLine());
    }
    catch (Exception localException)
    {
    }
    throw new JNLParseException(paramString, localException, "exception parsing jnlp file", 0);
  }

  public static LaunchDesc parse(byte[] paramArrayOfByte, URL paramURL1, URL paramURL2, URL paramURL3, MatchJREIf paramMatchJREIf)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    String str1 = decode(paramArrayOfByte);
    XMLNode localXMLNode = parse(str1);
    InformationDesc localInformationDesc = null;
    ResourcesDesc localResourcesDesc = null;
    UpdateDesc localUpdateDesc = null;
    ApplicationDesc localApplicationDesc = null;
    AppletDesc localAppletDesc = null;
    JavaFXAppDesc localJavaFXAppDesc = null;
    LibraryDesc localLibraryDesc = null;
    InstallerDesc localInstallerDesc = null;
    String str2 = null;
    if ((localXMLNode == null) || (localXMLNode.getName() == null))
      throw new JNLParseException(str1, null, null, 0);
    if ((localXMLNode.getName().equals("player")) || (localXMLNode.getName().equals("viewer")))
    {
      str3 = XMLUtils.getAttribute(localXMLNode, null, "tab");
      return LaunchDescFactory.buildInternalLaunchDesc(localXMLNode, str3);
    }
    if (!localXMLNode.getName().equals("jnlp"))
      throwNewException(str1, new MissingFieldException(str1, "<jnlp>"));
    String str3 = XMLUtils.getAttribute(localXMLNode, "", "spec", "1.0+");
    String str4 = XMLUtils.getAttribute(localXMLNode, "", "version");
    URL localURL1 = URLUtil.asPathURL(XMLUtils.getAttributeURL(str1, paramURL1, localXMLNode, "", "codebase"));
    if ((localURL1 == null) && (paramURL1 != null))
      localURL1 = paramURL1;
    URL localURL2 = XMLUtils.getAttributeURL(str1, localURL1, localXMLNode, "<applet-desc>", "documentbase");
    if (localURL2 == null)
      localURL2 = XMLUtils.getAttributeURL(str1, localURL1, localXMLNode, "<applet-desc>", "documentBase");
    if (paramURL2 != null)
      localURL2 = paramURL2;
    URL localURL3 = XMLUtils.getAttributeURL(str1, localURL1, localXMLNode, "", "href");
    int i = 0;
    CachedCertificatesHelper[] arrayOfCachedCertificatesHelper = null;
    if (XMLUtils.isElementPath(localXMLNode, "<security><all-permissions>"))
    {
      i = 1;
      arrayOfCachedCertificatesHelper = buildCachedCertificates(localXMLNode);
    }
    else if (XMLUtils.isElementPath(localXMLNode, "<security><j2ee-application-client-permissions>"))
    {
      i = 2;
      arrayOfCachedCertificatesHelper = buildCachedCertificates(localXMLNode);
    }
    if (XMLUtils.isElementPath(localXMLNode, "<javafx-desc>"))
      localJavaFXAppDesc = buildFXAppDesc(str1, localXMLNode, "<javafx-desc>");
    int j = 0;
    if (XMLUtils.isElementPath(localXMLNode, "<application-desc>"))
    {
      j = 1;
      localApplicationDesc = buildApplicationDesc(str1, localXMLNode);
    }
    else if (XMLUtils.isElementPath(localXMLNode, "<component-desc>"))
    {
      j = 3;
      localLibraryDesc = buildLibraryDesc(str1, localXMLNode);
    }
    else if (XMLUtils.isElementPath(localXMLNode, "<installer-desc>"))
    {
      if (!Cache.isCacheEnabled())
        throwNewException(str1, new BadFieldException(str1, "<installer-desc>", ""));
      j = 4;
      localInstallerDesc = buildInstallerDesc(str1, localURL1, localXMLNode);
    }
    else if (XMLUtils.isElementPath(localXMLNode, "<applet-desc>"))
    {
      j = 2;
      localAppletDesc = buildAppletDesc(str1, localURL1, localURL2, localXMLNode);
    }
    else if (localJavaFXAppDesc != null)
    {
      j = 6;
    }
    else
    {
      throwNewException(str1, new MissingFieldException(str1, "<jnlp>(<application-desc>|<applet-desc>|<installer-desc>|<component-desc>)"));
    }
    localUpdateDesc = getUpdateDesc(localXMLNode);
    localInformationDesc = buildInformationDesc(str1, localURL1, localXMLNode);
    localResourcesDesc = buildResourcesDesc(str1, localURL1, localXMLNode, false);
    LaunchDesc localLaunchDesc = new LaunchDesc(str3, localURL1, localURL3, str4, localInformationDesc, i, arrayOfCachedCertificatesHelper, localUpdateDesc, localResourcesDesc, j, localApplicationDesc, localAppletDesc, localJavaFXAppDesc, localLibraryDesc, localInstallerDesc, str2, localXMLNode, paramMatchJREIf);
    localLaunchDesc.setSourceURL(paramURL3);
    if (Trace.isEnabled(TraceLevel.TEMP))
      Trace.println("returning LaunchDesc from XMLFormat.parse():\n" + localLaunchDesc, TraceLevel.TEMP);
    return localLaunchDesc;
  }

  private static void throwNewException(String paramString, Exception paramException)
    throws JNLParseException, MissingFieldException, BadFieldException
  {
    XMLParser localXMLParser = new XMLParser(paramString);
    BadTokenException localBadTokenException = null;
    try
    {
      localXMLParser.parse();
      localBadTokenException = localXMLParser.getSavedException();
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
    if (localBadTokenException != null)
    {
      Trace.println("JNLP Parse Exception: " + localBadTokenException, TraceLevel.TEMP);
      throw new JNLParseException(paramString, localBadTokenException, "wrong kind of token found", localBadTokenException.getLine());
    }
    if ((paramException instanceof MissingFieldException))
      throw ((MissingFieldException)paramException);
    if ((paramException instanceof BadFieldException))
      throw ((BadFieldException)paramException);
  }

  private static InformationDesc combineInformationDesc(InformationDesc paramInformationDesc1, InformationDesc paramInformationDesc2)
  {
    if (paramInformationDesc1 == null)
      return paramInformationDesc2;
    if (paramInformationDesc2 == null)
      return paramInformationDesc1;
    String str1 = paramInformationDesc1.getTitle() != null ? paramInformationDesc1.getTitle() : paramInformationDesc2.getTitle();
    String str2 = paramInformationDesc1.getVendor() != null ? paramInformationDesc1.getVendor() : paramInformationDesc2.getVendor();
    URL localURL = paramInformationDesc1.getHome() != null ? paramInformationDesc1.getHome() : paramInformationDesc2.getHome();
    String[] arrayOfString = new String[4];
    for (int i = 0; i < arrayOfString.length; i++)
      arrayOfString[i] = (paramInformationDesc1.getDescription(i) != null ? paramInformationDesc1.getDescription(i) : paramInformationDesc2.getDescription(i));
    ArrayList localArrayList = new ArrayList();
    if (paramInformationDesc2.getIcons() != null)
      localArrayList.addAll(Arrays.asList(paramInformationDesc2.getIcons()));
    if (paramInformationDesc1.getIcons() != null)
      localArrayList.addAll(Arrays.asList(paramInformationDesc1.getIcons()));
    IconDesc[] arrayOfIconDesc = new IconDesc[localArrayList.size()];
    arrayOfIconDesc = (IconDesc[])(IconDesc[])localArrayList.toArray(arrayOfIconDesc);
    boolean bool = (paramInformationDesc1.supportsOfflineOperation()) || (paramInformationDesc2.supportsOfflineOperation());
    ShortcutDesc localShortcutDesc = paramInformationDesc1.getShortcut() != null ? paramInformationDesc1.getShortcut() : paramInformationDesc2.getShortcut();
    AssociationDesc[] arrayOfAssociationDesc = (AssociationDesc[])(AssociationDesc[])addArrays((Object[])paramInformationDesc1.getAssociations(), (Object[])paramInformationDesc2.getAssociations());
    RContentDesc[] arrayOfRContentDesc = (RContentDesc[])(RContentDesc[])addArrays((Object[])paramInformationDesc1.getRelatedContent(), (Object[])paramInformationDesc2.getRelatedContent());
    return new InformationDesc(str1, str2, localURL, arrayOfString, arrayOfIconDesc, localShortcutDesc, arrayOfRContentDesc, arrayOfAssociationDesc, bool);
  }

  private static InformationDesc buildInformationDesc(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<information>", new XMLUtils.ElementVisitor(paramString, paramURL, localArrayList)
    {
      private final String val$source;
      private final URL val$codebase;
      private final ArrayList val$list;

      public void visitElement(XMLNode paramXMLNode)
        throws BadFieldException, MissingFieldException
      {
        String[] arrayOfString1 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramXMLNode, "", "os", null));
        String[] arrayOfString2 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramXMLNode, "", "arch", null));
        String[] arrayOfString3 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramXMLNode, "", "platform", null));
        String[] arrayOfString4 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramXMLNode, "", "locale", null));
        if ((GeneralUtil.prefixMatchStringList(arrayOfString1, Config.getOSFullName())) && (GeneralUtil.prefixMatchStringList(arrayOfString2, Config.getOSArch())) && (GeneralUtil.prefixMatchStringList(arrayOfString3, Config.getOSPlatform())) && (XMLFormat.matchDefaultLocale(arrayOfString4)))
        {
          String str1 = XMLUtils.getElementContents(paramXMLNode, "<title>");
          String str2 = XMLUtils.getElementContents(paramXMLNode, "<vendor>");
          URL localURL = XMLUtils.getAttributeURL(this.val$source, this.val$codebase, paramXMLNode, "<homepage>", "href");
          String[] arrayOfString5 = new String[4];
          arrayOfString5[0] = XMLUtils.getElementContentsWithAttribute(paramXMLNode, "<description>", "kind", "", null);
          arrayOfString5[2] = XMLUtils.getElementContentsWithAttribute(paramXMLNode, "<description>", "kind", "one-line", null);
          arrayOfString5[1] = XMLUtils.getElementContentsWithAttribute(paramXMLNode, "<description>", "kind", "short", null);
          arrayOfString5[3] = XMLUtils.getElementContentsWithAttribute(paramXMLNode, "<description>", "kind", "tooltip", null);
          IconDesc[] arrayOfIconDesc = XMLFormat.access$000(this.val$source, this.val$codebase, paramXMLNode);
          ShortcutDesc localShortcutDesc = XMLFormat.access$100(paramXMLNode);
          RContentDesc[] arrayOfRContentDesc = XMLFormat.access$200(this.val$source, this.val$codebase, paramXMLNode);
          AssociationDesc[] arrayOfAssociationDesc = XMLFormat.access$300(this.val$source, this.val$codebase, paramXMLNode);
          this.val$list.add(new InformationDesc(str1, str2, localURL, arrayOfString5, arrayOfIconDesc, localShortcutDesc, arrayOfRContentDesc, arrayOfAssociationDesc, XMLUtils.isElementPath(paramXMLNode, "<offline-allowed>")));
        }
      }
    });
    InformationDesc localInformationDesc1 = new InformationDesc(null, null, null, null, null, null, null, null, false);
    for (int i = 0; i < localArrayList.size(); i++)
    {
      InformationDesc localInformationDesc2 = (InformationDesc)localArrayList.get(i);
      localInformationDesc1 = combineInformationDesc(localInformationDesc2, localInformationDesc1);
    }
    if (localInformationDesc1.getTitle() == null)
      throw new MissingFieldException(paramString, "<jnlp><information><title>");
    if (localInformationDesc1.getVendor() == null)
      throw new MissingFieldException(paramString, "<jnlp><information><vendor>");
    return localInformationDesc1;
  }

  private static Object[] addArrays(Object[] paramArrayOfObject1, Object[] paramArrayOfObject2)
  {
    if (paramArrayOfObject1 == null)
      return paramArrayOfObject2;
    if (paramArrayOfObject2 == null)
      return paramArrayOfObject1;
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    while (i < paramArrayOfObject1.length)
      localArrayList.add(paramArrayOfObject1[(i++)]);
    i = 0;
    while (i < paramArrayOfObject2.length)
      localArrayList.add(paramArrayOfObject2[(i++)]);
    return (Object[])localArrayList.toArray(paramArrayOfObject1);
  }

  public static boolean matchDefaultLocale(String[] paramArrayOfString)
  {
    return GeneralUtil.matchLocale(paramArrayOfString, Globals.getDefaultLocale());
  }

  static ResourcesDesc buildResourcesDesc(String paramString, URL paramURL, XMLNode paramXMLNode, boolean paramBoolean)
    throws MissingFieldException, BadFieldException
  {
    ResourcesDesc localResourcesDesc = new ResourcesDesc();
    XMLUtils.visitElements(paramXMLNode, "<resources>", new XMLUtils.ElementVisitor(paramString, paramURL, localResourcesDesc, paramBoolean)
    {
      private final String val$source;
      private final URL val$codebase;
      private final ResourcesDesc val$rdesc;
      private final boolean val$ignoreJres;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String[] arrayOfString1 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramXMLNode, "", "os", null));
        String[] arrayOfString2 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramXMLNode, "", "arch", null));
        String[] arrayOfString3 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramXMLNode, "", "platform", null));
        String[] arrayOfString4 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramXMLNode, "", "locale", null));
        if ((GeneralUtil.prefixMatchStringList(arrayOfString1, Config.getOSFullName())) && (GeneralUtil.prefixMatchStringList(arrayOfString2, Config.getOSArch())) && (GeneralUtil.prefixMatchStringList(arrayOfString2, Config.getOSArch())) && (GeneralUtil.prefixMatchStringList(arrayOfString3, Config.getOSPlatform())) && (XMLFormat.matchDefaultLocale(arrayOfString4)))
          XMLUtils.visitChildrenElements(paramXMLNode, new XMLUtils.ElementVisitor()
          {
            public void visitElement(XMLNode paramXMLNode)
              throws MissingFieldException, BadFieldException
            {
              XMLFormat.access$800(XMLFormat.2.this.val$source, XMLFormat.2.this.val$codebase, paramXMLNode, XMLFormat.2.this.val$rdesc, XMLFormat.2.this.val$ignoreJres);
            }
          });
      }
    });
    if (!localResourcesDesc.isEmpty())
    {
      boolean bool1 = localResourcesDesc.isPack200Enabled();
      boolean bool2 = localResourcesDesc.isVersionEnabled();
      if ((bool1) || (bool2))
      {
        JARDesc[] arrayOfJARDesc = localResourcesDesc.getLocalJarDescs();
        for (int i = 0; i < arrayOfJARDesc.length; i++)
        {
          JARDesc localJARDesc = arrayOfJARDesc[i];
          if (bool1)
            localJARDesc.setPack200Enabled();
          if (!bool2)
            continue;
          localJARDesc.setVersionEnabled();
        }
      }
    }
    return localResourcesDesc.isEmpty() ? null : localResourcesDesc;
  }

  private static IconDesc[] getIconDescs(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<icon>", new XMLUtils.ElementVisitor(paramString, paramURL, localArrayList)
    {
      private final String val$source;
      private final URL val$codebase;
      private final ArrayList val$answer;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getAttribute(paramXMLNode, "", "kind", "");
        URL localURL = XMLUtils.getRequiredURL(this.val$source, this.val$codebase, paramXMLNode, "", "href");
        String str2 = XMLUtils.getAttribute(paramXMLNode, "", "version", null);
        int i = XMLUtils.getIntAttribute(this.val$source, paramXMLNode, "", "height", 0);
        int j = XMLUtils.getIntAttribute(this.val$source, paramXMLNode, "", "width", 0);
        int k = XMLUtils.getIntAttribute(this.val$source, paramXMLNode, "", "depth", 0);
        int m = 0;
        if (str1.equals("selected"))
          m = 1;
        else if (str1.equals("disabled"))
          m = 2;
        else if (str1.equals("rollover"))
          m = 3;
        else if (str1.equals("splash"))
          m = 4;
        else if (str1.equals("shortcut"))
          m = 5;
        this.val$answer.add(new IconDesc(localURL, str2, i, j, k, m));
      }
    });
    return (IconDesc[])(IconDesc[])localArrayList.toArray(new IconDesc[localArrayList.size()]);
  }

  private static ShortcutDesc getShortcutDesc(XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<shortcut>", new XMLUtils.ElementVisitor(localArrayList)
    {
      private final ArrayList val$shortcuts;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getAttribute(paramXMLNode, "", "online", "true");
        boolean bool1 = str1.equalsIgnoreCase("true");
        String str2 = XMLUtils.getAttribute(paramXMLNode, "", "install", "false");
        boolean bool2 = str2.equalsIgnoreCase("true");
        boolean bool3 = XMLUtils.isElementPath(paramXMLNode, "<desktop>");
        boolean bool4 = XMLUtils.isElementPath(paramXMLNode, "<menu>");
        String str3 = XMLUtils.getAttribute(paramXMLNode, "<menu>", "submenu");
        this.val$shortcuts.add(new ShortcutDesc(bool1, bool2, bool3, bool4, str3));
      }
    });
    if (localArrayList.size() > 0)
      return (ShortcutDesc)localArrayList.get(0);
    return null;
  }

  private static CachedCertificatesHelper[] buildCachedCertificates(XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<security><details>", new XMLUtils.ElementVisitor(localArrayList)
    {
      private final ArrayList val$certs;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getAttribute(paramXMLNode, "", "signedjnlp");
        boolean bool = "true".equalsIgnoreCase(str1);
        String str2 = XMLUtils.getAttribute(paramXMLNode, "<certificate-path>", "timestamp");
        String str3 = XMLUtils.getElementContents(paramXMLNode, "<certificate-path>");
        Date localDate = str2 == null ? null : new Date(Long.parseLong(str2));
        CachedCertificatesHelper localCachedCertificatesHelper = CachedCertificatesHelper.create(localDate, str3, bool);
        if (localCachedCertificatesHelper != null)
          this.val$certs.add(localCachedCertificatesHelper);
      }
    });
    if (localArrayList.size() > 0)
      return (CachedCertificatesHelper[])(CachedCertificatesHelper[])localArrayList.toArray(new CachedCertificatesHelper[0]);
    return null;
  }

  private static UpdateDesc getUpdateDesc(XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<update>", new XMLUtils.ElementVisitor(localArrayList)
    {
      private final ArrayList val$updates;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getAttribute(paramXMLNode, "", "check", "timeout");
        String str2 = XMLUtils.getAttribute(paramXMLNode, "", "policy", "always");
        this.val$updates.add(new UpdateDesc(str1, str2));
      }
    });
    if (localArrayList.size() > 0)
      return (UpdateDesc)localArrayList.get(0);
    return new UpdateDesc("timeout", "always");
  }

  private static AssociationDesc[] getAssociationDesc(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<association>", new XMLUtils.ElementVisitor(paramString, paramURL, localArrayList)
    {
      private final String val$source;
      private final URL val$codebase;
      private final ArrayList val$answer;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getAttribute(paramXMLNode, "", "extensions");
        String str2 = XMLUtils.getAttribute(paramXMLNode, "", "mime-type");
        String str3 = XMLUtils.getElementContents(paramXMLNode, "<description>");
        URL localURL = XMLUtils.getAttributeURL(this.val$source, this.val$codebase, paramXMLNode, "<icon>", "href");
        if ((str1 == null) && (str2 == null))
          throw new MissingFieldException(this.val$source, "<association>(<extensions><mime-type>)");
        if (str1 == null)
          throw new MissingFieldException(this.val$source, "<association><extensions>");
        if (str2 == null)
          throw new MissingFieldException(this.val$source, "<association><mime-type>");
        if ("gnome".equals(System.getProperty("sun.desktop")))
        {
          str1 = str1.toLowerCase();
          str2 = str2.toLowerCase();
        }
        this.val$answer.add(new AssociationDesc(str1, str2, str3, localURL));
      }
    });
    return (AssociationDesc[])(AssociationDesc[])localArrayList.toArray(new AssociationDesc[localArrayList.size()]);
  }

  private static RContentDesc[] getRContentDescs(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<related-content>", new XMLUtils.ElementVisitor(paramString, paramURL, localArrayList)
    {
      private final String val$source;
      private final URL val$codebase;
      private final ArrayList val$answer;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        URL localURL1 = XMLUtils.getRequiredURL(this.val$source, this.val$codebase, paramXMLNode, "", "href");
        String str1 = XMLUtils.getElementContents(paramXMLNode, "<title>");
        String str2 = XMLUtils.getElementContents(paramXMLNode, "<description>");
        URL localURL2 = XMLUtils.getAttributeURL(this.val$source, this.val$codebase, paramXMLNode, "<icon>", "href");
        this.val$answer.add(new RContentDesc(localURL1, str1, str2, localURL2));
      }
    });
    return (RContentDesc[])(RContentDesc[])localArrayList.toArray(new RContentDesc[localArrayList.size()]);
  }

  private static void handleResourceElement(String paramString, URL paramURL, XMLNode paramXMLNode, ResourcesDesc paramResourcesDesc, boolean paramBoolean)
    throws MissingFieldException, BadFieldException
  {
    String str1 = paramXMLNode.getName();
    Object localObject1;
    Object localObject2;
    String str5;
    Object localObject4;
    if ((str1.equals("jar")) || (str1.equals("nativelib")))
    {
      localObject1 = XMLUtils.getRequiredURL(paramString, paramURL, paramXMLNode, "", "href");
      localObject2 = XMLUtils.getAttribute(paramXMLNode, "", "version", null);
      String str2 = XMLUtils.getAttribute(paramXMLNode, "", "download");
      String str4 = XMLUtils.getAttribute(paramXMLNode, "", "main");
      str5 = XMLUtils.getAttribute(paramXMLNode, "", "part");
      int j = XMLUtils.getIntAttribute(paramString, paramXMLNode, "", "size", 0);
      boolean bool2 = str1.equals("nativelib");
      if ((!Cache.isCacheEnabled()) && (bool2))
        throw new BadFieldException(paramString, "nativelib", ((URL)localObject1).toString());
      boolean bool3 = "lazy".equalsIgnoreCase(str2);
      boolean bool4 = "progress".equalsIgnoreCase(str2);
      boolean bool5 = "true".equalsIgnoreCase(str4);
      localObject4 = null;
      if ((Environment.isImportMode()) && (Environment.getImportModeCodebaseOverride() != null) && (((URL)localObject1).toString().endsWith("/")))
      {
        String str6 = XMLUtils.getAttribute(paramXMLNode, "", "href");
        File localFile = null;
        try
        {
          URI localURI = new URI(Environment.getImportModeCodebaseOverride().toString().replace("\\", "/") + XMLUtils.getAttribute(paramXMLNode, "", "href"));
          localFile = new File(localURI);
        }
        catch (URISyntaxException localURISyntaxException)
        {
          Trace.ignoredException(localURISyntaxException);
        }
        if ((localFile != null) && (localFile.isDirectory()))
        {
          File[] arrayOfFile = localFile.listFiles();
          for (int k = 0; k < arrayOfFile.length; k++)
            try
            {
              URL localURL = new URL(((URL)localObject1).toString() + arrayOfFile[k].getName());
              localObject4 = new JARDesc(localURL, (String)localObject2, bool3, bool5, bool2, str5, j, paramResourcesDesc, bool4);
              paramResourcesDesc.addResource((ResourceType)localObject4);
            }
            catch (MalformedURLException localMalformedURLException)
            {
              Trace.ignoredException(localMalformedURLException);
            }
        }
      }
      else
      {
        localObject4 = new JARDesc((URL)localObject1, (String)localObject2, bool3, bool5, bool2, str5, j, paramResourcesDesc, bool4);
        paramResourcesDesc.addResource((ResourceType)localObject4);
      }
    }
    else if (str1.equals("property"))
    {
      localObject1 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "", "name");
      localObject2 = XMLUtils.getRequiredAttributeEmptyOK(paramString, paramXMLNode, "", "value");
      if ((((String)localObject1).equals("jnlp.versionEnabled")) && (((String)localObject2).equalsIgnoreCase("true")))
        paramResourcesDesc.setVersionEnabled();
      else if ((((String)localObject1).equals("jnlp.packEnabled")) && (((String)localObject2).equalsIgnoreCase("true")))
        paramResourcesDesc.setPack200Enabled();
      else if (((String)localObject1).equals("jnlp.concurrentDownloads"))
      {
        if (localObject2 != null)
        {
          int i = 0;
          try
          {
            i = Integer.parseInt(((String)localObject2).trim());
          }
          catch (NumberFormatException localNumberFormatException)
          {
          }
          paramResourcesDesc.setConcurrentDownloads(i);
        }
      }
      else
        paramResourcesDesc.addResource(new PropertyDesc((String)localObject1, (String)localObject2));
    }
    else
    {
      String str3;
      if (str1.equals("package"))
      {
        localObject1 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "", "name");
        localObject2 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "", "part");
        str3 = XMLUtils.getAttribute(paramXMLNode, "", "recursive", "false");
        boolean bool1 = "true".equals(str3);
        paramResourcesDesc.addResource(new PackageDesc((String)localObject1, (String)localObject2, bool1));
      }
      else
      {
        Object localObject3;
        if (str1.equals("extension"))
        {
          localObject1 = XMLUtils.getAttribute(paramXMLNode, "", "name");
          localObject2 = XMLUtils.getRequiredURL(paramString, paramURL, paramXMLNode, "", "href");
          str3 = XMLUtils.getAttribute(paramXMLNode, "", "version", null);
          localObject3 = getExtDownloadDescs(paramString, paramXMLNode);
          paramResourcesDesc.addResource(new ExtensionDesc((String)localObject1, (URL)localObject2, str3, localObject3));
        }
        else if (((str1.equals("java")) || (str1.equals("j2se"))) && (!paramBoolean))
        {
          localObject1 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "", "version");
          localObject2 = XMLUtils.getAttributeURL(paramString, paramURL, paramXMLNode, "", "href");
          str3 = XMLUtils.getAttribute(paramXMLNode, "", "initial-heap-size");
          localObject3 = XMLUtils.getAttribute(paramXMLNode, "", "max-heap-size");
          str5 = XMLUtils.getAttribute(paramXMLNode, "", "java-vm-args");
          long l1 = -1L;
          long l2 = -1L;
          l1 = GeneralUtil.heapValToLong(str3);
          l2 = GeneralUtil.heapValToLong((String)localObject3);
          ResourcesDesc localResourcesDesc = buildResourcesDesc(paramString, paramURL, paramXMLNode, true);
          localObject4 = new JREDesc((String)localObject1, l1, l2, str5, (URL)localObject2, localResourcesDesc);
          paramResourcesDesc.addResource((ResourceType)localObject4);
        }
        else if (str1.equals("javafx-runtime"))
        {
          localObject1 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "", "version");
          localObject2 = XMLUtils.getAttributeURL(paramString, paramURL, paramXMLNode, "", "href");
          paramResourcesDesc.addResource(new JavaFXRuntimeDesc((String)localObject1, (URL)localObject2));
        }
      }
    }
  }

  private static ExtDownloadDesc[] getExtDownloadDescs(String paramString, XMLNode paramXMLNode)
    throws BadFieldException, MissingFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<ext-download>", new XMLUtils.ElementVisitor(paramString, localArrayList)
    {
      private final String val$source;
      private final ArrayList val$al;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException
      {
        String str1 = XMLUtils.getRequiredAttribute(this.val$source, paramXMLNode, "", "ext-part");
        String str2 = XMLUtils.getAttribute(paramXMLNode, "", "part");
        String str3 = XMLUtils.getAttribute(paramXMLNode, "", "download", "eager");
        boolean bool = "lazy".equals(str3);
        this.val$al.add(new ExtDownloadDesc(str1, str2, bool));
      }
    });
    ExtDownloadDesc[] arrayOfExtDownloadDesc = new ExtDownloadDesc[localArrayList.size()];
    return (ExtDownloadDesc[])(ExtDownloadDesc[])localArrayList.toArray(arrayOfExtDownloadDesc);
  }

  private static ApplicationDesc buildApplicationDesc(String paramString, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    String str1 = XMLUtils.getClassName(paramString, paramXMLNode, "<application-desc>", "main-class", false);
    String str2 = XMLUtils.getClassName(paramString, paramXMLNode, "<application-desc>", "progress-class", false);
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<application-desc><argument>", new XMLUtils.ElementVisitor(paramString, localArrayList)
    {
      private final String val$source;
      private final ArrayList val$al1;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str = XMLUtils.getElementContents(paramXMLNode, "", null);
        if (str == null)
          throw new BadFieldException(this.val$source, XMLUtils.getPathString(paramXMLNode), "");
        this.val$al1.add(str);
      }
    });
    String[] arrayOfString = new String[localArrayList.size()];
    arrayOfString = (String[])(String[])localArrayList.toArray(arrayOfString);
    return new ApplicationDesc(str1, str2, arrayOfString);
  }

  private static JavaFXAppDesc buildFXAppDesc(String paramString1, XMLNode paramXMLNode, String paramString2)
    throws MissingFieldException, BadFieldException
  {
    String str1 = XMLUtils.getClassName(paramString1, paramXMLNode, paramString2, "main-class", true);
    String str2 = XMLUtils.getClassName(paramString1, paramXMLNode, paramString2, "preloader-class", false);
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<javafx-desc><argument>", new XMLUtils.ElementVisitor(paramString1, localArrayList)
    {
      private final String val$source;
      private final ArrayList val$al1;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str = XMLUtils.getElementContents(paramXMLNode, "", null);
        if (str == null)
          throw new BadFieldException(this.val$source, XMLUtils.getPathString(paramXMLNode), "");
        this.val$al1.add(str);
      }
    });
    String[] arrayOfString = null;
    if (!localArrayList.isEmpty())
    {
      arrayOfString = new String[localArrayList.size()];
      arrayOfString = (String[])(String[])localArrayList.toArray(arrayOfString);
    }
    Properties localProperties = new Properties();
    XMLUtils.visitElements(paramXMLNode, "<javafx-desc><param>", new XMLUtils.ElementVisitor(paramString1, localProperties)
    {
      private final String val$source;
      private final Properties val$params;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getRequiredAttribute(this.val$source, paramXMLNode, "", "name");
        String str2 = XMLUtils.getRequiredAttributeEmptyOK(this.val$source, paramXMLNode, "", "value");
        this.val$params.setProperty(str1, str2);
      }
    });
    return new JavaFXAppDesc(str1, str2, arrayOfString, localProperties);
  }

  private static LibraryDesc buildLibraryDesc(String paramString, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    String str = XMLUtils.getClassName(paramString, paramXMLNode, "<component-desc>", "progress-class", false);
    return new LibraryDesc(str);
  }

  private static InstallerDesc buildInstallerDesc(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    String str = XMLUtils.getClassName(paramString, paramXMLNode, "<installer-desc>", "main-class", false);
    return new InstallerDesc(str);
  }

  private static AppletDesc buildAppletDesc(String paramString, URL paramURL1, URL paramURL2, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    String str1 = XMLUtils.getClassName(paramString, paramXMLNode, "<applet-desc>", "main-class", true);
    String str2 = XMLUtils.getClassName(paramString, paramXMLNode, "<applet-desc>", "progress-class", false);
    String str3 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "<applet-desc>", "name");
    int i = XMLUtils.getRequiredIntAttribute(paramString, paramXMLNode, "<applet-desc>", "width");
    int j = XMLUtils.getRequiredIntAttribute(paramString, paramXMLNode, "<applet-desc>", "height");
    if (i <= 0)
      throw new BadFieldException(paramString, XMLUtils.getPathString(paramXMLNode) + "<applet-desc>width", new Integer(i).toString());
    if (j <= 0)
      throw new BadFieldException(paramString, XMLUtils.getPathString(paramXMLNode) + "<applet-desc>height", new Integer(j).toString());
    Properties localProperties = new Properties();
    XMLUtils.visitElements(paramXMLNode, "<applet-desc><param>", new XMLUtils.ElementVisitor(paramString, localProperties)
    {
      private final String val$source;
      private final Properties val$params;

      public void visitElement(XMLNode paramXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getRequiredAttribute(this.val$source, paramXMLNode, "", "name");
        String str2 = XMLUtils.getRequiredAttributeEmptyOK(this.val$source, paramXMLNode, "", "value");
        this.val$params.setProperty(str1, str2);
      }
    });
    return new AppletDesc(str3, str1, paramURL2, i, j, localProperties, str2);
  }

  public static boolean isBlacklisted(XMLNode paramXMLNode)
  {
    if (paramXMLNode == null)
      return false;
    if (paramXMLNode.getName() != null)
    {
      XMLAttribute localXMLAttribute;
      String str;
      if ((paramXMLNode.getName().equals("java")) || (paramXMLNode.getName().equals("j2se")))
        for (localXMLAttribute = paramXMLNode.getAttributes(); localXMLAttribute != null; localXMLAttribute = localXMLAttribute.getNext())
        {
          if (!localXMLAttribute.getName().equals("java-vm-args"))
            continue;
          str = localXMLAttribute.getValue();
          if ((str == null) || (str.indexOf("*") < 0))
            continue;
          Trace.println("Blacklisted - a = " + localXMLAttribute, TraceLevel.SECURITY);
          return true;
        }
      else if (paramXMLNode.getName().equals("property"))
        for (localXMLAttribute = paramXMLNode.getAttributes(); localXMLAttribute != null; localXMLAttribute = localXMLAttribute.getNext())
        {
          str = localXMLAttribute.getValue();
          if ((str == null) || (str.indexOf("*") < 0))
            continue;
          Trace.println("Blacklisted - a = " + localXMLAttribute, TraceLevel.SECURITY);
          return true;
        }
    }
    if (isBlacklisted(paramXMLNode.getNested()))
      return true;
    return isBlacklisted(paramXMLNode.getNext());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.XMLFormat
 * JD-Core Version:    0.6.0
 */