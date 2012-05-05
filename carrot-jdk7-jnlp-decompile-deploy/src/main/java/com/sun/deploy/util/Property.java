package com.sun.deploy.util;

import com.sun.deploy.appcontext.AppContext;
import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.UIToolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Property
  implements Cloneable
{
  private static final boolean DEBUG = false;
  public static final String JNLP_PACK_ENABLED = "jnlp.packEnabled";
  public static final String JNLP_VERSION_ENABLED = "jnlp.versionEnabled";
  public static final String JNLP_CONCURRENT_DOWNLOADS = "jnlp.concurrentDownloads";
  public static final int CONCURRENT_DOWNLOADS_DEF = Config.getIntProperty("deployment.javaws.concurrentDownloads") == -1 ? 4 : Config.getIntProperty("deployment.javaws.concurrentDownloads");
  String key;
  String value;
  boolean isSecure;
  private static final boolean _quoteWholePropertySpec;
  private static List jnlpProps;

  public Property(String paramString)
  {
    paramString = StringQuoteUtil.unquoteIfNeeded(paramString);
    int i = paramString.indexOf("-D");
    if ((i < 0) || (i == paramString.length() - 2))
      throw new IllegalArgumentException("Property invalid");
    i += 2;
    int j = paramString.indexOf("=");
    if (j < 0)
    {
      this.key = paramString.substring(i);
      this.value = new String("");
    }
    else
    {
      this.key = paramString.substring(i, j);
      this.value = StringQuoteUtil.unquoteIfNeeded(paramString.substring(j + 1));
    }
    this.isSecure = Config.isSecureProperty(this.key, this.value);
  }

  public static Property createProperty(String paramString)
  {
    Property localProperty = null;
    try
    {
      localProperty = new Property(paramString);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
    }
    return localProperty;
  }

  public Property(String paramString1, String paramString2)
  {
    this.key = paramString1;
    if (paramString2 != null)
      this.value = StringQuoteUtil.unquoteIfNeeded(paramString2);
    else
      this.value = new String("");
    this.isSecure = Config.isSecureProperty(this.key, this.value);
  }

  public String getKey()
  {
    return this.key;
  }

  public String getValue()
  {
    return this.value;
  }

  public boolean isSecure()
  {
    return this.isSecure;
  }

  public String toString()
  {
    return toString(false);
  }

  public String toString(boolean paramBoolean)
  {
    if (this.value.length() == 0)
      return "-D" + this.key;
    if ((paramBoolean) && (_quoteWholePropertySpec))
      return StringQuoteUtil.quoteIfNeeded("-D" + this.key + "=" + this.value);
    return "-D" + this.key + "=" + StringQuoteUtil.quoteIfNeeded(this.value);
  }

  public void addTo(Properties paramProperties)
  {
    paramProperties.setProperty(this.key, this.value);
  }

  public Object clone()
  {
    return new Property(this.key, this.value);
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof Property))
      return false;
    Property localProperty = (Property)paramObject;
    int i = localProperty.hashCode();
    int j = hashCode();
    return i == j;
  }

  public int hashCode()
  {
    return this.key.hashCode();
  }

  public static final boolean getQuotesWholePropertySpec()
  {
    return _quoteWholePropertySpec;
  }

  public static boolean isJnlpProperty(String paramString)
  {
    try
    {
      Property localProperty = new Property(paramString);
      return isJnlpPropertyKey(localProperty.getKey());
    }
    catch (Exception localException)
    {
    }
    return false;
  }

  public static boolean isJnlpPropertyKey(String paramString)
  {
    return (paramString != null) && (jnlpProps.contains(paramString));
  }

  public static List getJnlpProperties(String paramString)
  {
    List localList = StringQuoteUtil.parseCommandLine(paramString);
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < localList.size(); i++)
    {
      String str = (String)localList.get(i);
      try
      {
        Property localProperty = new Property(str);
        if (localProperty.getKey().startsWith("jnlp."))
          localArrayList.add(localProperty);
      }
      catch (IllegalArgumentException localIllegalArgumentException)
      {
      }
    }
    return localArrayList;
  }

  public static boolean collectsJnlpPropertyIntoAppContext(String paramString1, String paramString2)
  {
    if (isJnlpPropertyKey(paramString1))
    {
      collectsJnlpProperties(paramString1, paramString2, ToolkitStore.get().getAppContext());
      return true;
    }
    return false;
  }

  public static void collectsJnlpProperties(String paramString, AppContext paramAppContext)
  {
    List localList = getJnlpProperties(paramString);
    for (int i = 0; i < localList.size(); i++)
    {
      Property localProperty = (Property)localList.get(i);
      collectsJnlpProperties(localProperty.getKey(), localProperty.getValue(), paramAppContext);
    }
  }

  private static void collectsJnlpProperties(String paramString1, String paramString2, AppContext paramAppContext)
  {
    if (paramAppContext == null)
      return;
    Object localObject = null;
    if (paramString1.equals("jnlp.concurrentDownloads"))
    {
      int i = 0;
      try
      {
        i = Integer.parseInt(paramString2);
      }
      catch (NumberFormatException localNumberFormatException)
      {
        Trace.println(localNumberFormatException.getLocalizedMessage() + " " + paramString1, TraceLevel.NETWORK);
      }
      if (i <= 0)
        i = CONCURRENT_DOWNLOADS_DEF;
      if (i > 10)
        i = 10;
      localObject = Integer.valueOf(i);
    }
    else if ((paramString1.equals("jnlp.packEnabled")) || (paramString1.equals("jnlp.versionEnabled")))
    {
      localObject = Boolean.valueOf(paramString2);
    }
    paramAppContext.put(paramString1, localObject);
  }

  public static boolean isPackEnabled()
  {
    return isPackEnabled(ToolkitStore.get().getAppContext());
  }

  public static boolean isPackEnabled(AppContext paramAppContext)
  {
    if (!Config.isJavaVersionAtLeast15())
      return false;
    return booleanValue(paramAppContext, "jnlp.packEnabled");
  }

  public static boolean isVersionEnabled()
  {
    return isVersionEnabled(ToolkitStore.get().getAppContext());
  }

  public static boolean isVersionEnabled(AppContext paramAppContext)
  {
    return booleanValue(paramAppContext, "jnlp.versionEnabled");
  }

  public static int getConcurrentDownloads()
  {
    return getConcurrentDownloads(ToolkitStore.get().getAppContext());
  }

  public static int getConcurrentDownloads(AppContext paramAppContext)
  {
    int i = intValue(paramAppContext, "jnlp.concurrentDownloads");
    if (i <= 0)
      i = CONCURRENT_DOWNLOADS_DEF;
    return i;
  }

  private static boolean booleanValue(AppContext paramAppContext, String paramString)
  {
    if (paramAppContext == null)
      return false;
    Object localObject = paramAppContext.get(paramString);
    if ((localObject instanceof Boolean))
      return ((Boolean)localObject).booleanValue();
    if ((localObject instanceof String))
      return Boolean.valueOf((String)localObject).booleanValue();
    return false;
  }

  private static int intValue(AppContext paramAppContext, String paramString)
  {
    if (paramAppContext == null)
      return -1;
    Object localObject = paramAppContext.get(paramString);
    if ((localObject instanceof Integer))
      return ((Integer)localObject).intValue();
    if ((localObject instanceof String))
      try
      {
        return Integer.valueOf((String)localObject).intValue();
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    return -1;
  }

  static
  {
    if (Config.getOSName().startsWith("Win"))
      _quoteWholePropertySpec = true;
    else
      _quoteWholePropertySpec = false;
    jnlpProps = Arrays.asList(new Object[] { "jnlp.packEnabled", "jnlp.versionEnabled", "jnlp.concurrentDownloads" });
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.Property
 * JD-Core Version:    0.6.0
 */