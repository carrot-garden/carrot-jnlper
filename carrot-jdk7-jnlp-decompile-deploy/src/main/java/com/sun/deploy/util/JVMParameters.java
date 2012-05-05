package com.sun.deploy.util;

import com.sun.deploy.Environment;
import com.sun.deploy.config.Config;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class JVMParameters
{
  private static final boolean DEBUG = false;
  private List internalArguments = new ArrayList();
  private ArgumentSet trustedArguments = new ArgumentSet();
  private ArgumentSet arguments = new ArgumentSet();
  private long maxHeap = 0L;
  private boolean isDefault;
  private static long DEFAULT_HEAP = 67108864L;
  private static final String INTERNAL_SEPARATOR_ARG = "---";
  private static final String TRUSTED_SEPARATOR_ARG = "--";
  private static JVMParameters runningJVMParameters = null;
  private static final String[] PLUGIN_DEPENDENT_JARS = { "deploy.jar", "javaws.jar", "plugin.jar" };
  private static final long[] MEMSIZES = { 268435456L, 201326592L, 134217728L, 67108864L, 50331648L, 33554432L };

  public JVMParameters()
  {
    clear();
  }

  public void clear()
  {
    this.internalArguments.clear();
    clearUserArguments();
  }

  public void clearUserArguments()
  {
    this.trustedArguments.clear();
    this.arguments.clear();
    this.maxHeap = 0L;
    this.isDefault = true;
  }

  public JVMParameters copy()
  {
    JVMParameters localJVMParameters = new JVMParameters();
    localJVMParameters.addArguments(this);
    return localJVMParameters;
  }

  public String[][] copyToStringArrays()
  {
    String[][] arrayOfString; = new String[8][];
    arrayOfString;[0] = ((String[])(String[])this.internalArguments.toArray(new String[0]));
    this.trustedArguments.copyToStringArrays(arrayOfString;, 1);
    String[] arrayOfString = new String[1];
    arrayOfString[0] = getXmx();
    arrayOfString;[4] = arrayOfString;
    this.arguments.copyToStringArrays(arrayOfString;, 5);
    return arrayOfString;;
  }

  public void getFromStringArrays(String[][] paramArrayOfString)
  {
    for (int i = 0; i < paramArrayOfString[0].length; i++)
      this.internalArguments.add(paramArrayOfString[0][i]);
    this.trustedArguments.getFromStringArrays(paramArrayOfString, 1);
    String str = paramArrayOfString[4][0];
    if (str != null)
      addArgument(str);
    this.arguments.getFromStringArrays(paramArrayOfString, 5);
    recomputeIsDefault();
  }

  public void parse(String paramString)
  {
    parseImpl(paramString, false, false);
  }

  public void parse(String paramString, boolean paramBoolean)
  {
    parseImpl(paramString, false, paramBoolean);
  }

  public void parseTrustedOptions(String paramString)
  {
    parseImpl(paramString, true, false);
  }

  private void parseImpl(String paramString, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramString == null)
      return;
    List localList;
    try
    {
      localList = StringQuoteUtil.parseCommandLine(paramString);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      System.out.println(localIllegalArgumentException.getMessage());
      return;
    }
    boolean bool = true;
    for (int i = 0; i < localList.size(); i++)
    {
      String str = (String)localList.get(i);
      bool = addArgumentImpl(str, bool, paramBoolean1, paramBoolean2);
    }
  }

  public void addProperties(Properties paramProperties)
  {
    this.arguments.addAll(paramProperties);
    recomputeIsDefault();
  }

  public void addProperties(List paramList)
  {
    this.arguments.addAll(paramList);
    recomputeIsDefault();
  }

  public void addArgument(String paramString)
  {
    addArgument(paramString, true);
  }

  public void addArgument(String paramString, boolean paramBoolean)
  {
    addArgumentImpl(StringQuoteUtil.unquoteIfNeeded(paramString), paramBoolean, false, false);
  }

  public void addInternalArgument(String paramString)
  {
    this.internalArguments.add(paramString);
  }

  public void addArguments(JVMParameters paramJVMParameters)
  {
    if (paramJVMParameters == null)
      return;
    this.internalArguments.addAll(paramJVMParameters.internalArguments);
    this.trustedArguments.addAll(paramJVMParameters.trustedArguments);
    this.arguments.addAll(paramJVMParameters.arguments);
    if ((!paramJVMParameters.isDefault) && (paramJVMParameters.maxHeap > 0L))
      this.maxHeap = paramJVMParameters.maxHeap;
    else if ((this.isDefault) && (paramJVMParameters.isDefault))
    {
      if ((this.maxHeap != 0L) || (paramJVMParameters.maxHeap != 0L))
        this.maxHeap = Math.max(getMaxHeapSize(), paramJVMParameters.getMaxHeapSize());
      else
        this.maxHeap = 0L;
    }
    else if (0L == this.maxHeap)
      this.maxHeap = paramJVMParameters.maxHeap;
    this.isDefault = ((this.isDefault) && (paramJVMParameters.isDefault));
  }

  public void removeArgument(String paramString)
  {
    if (paramString.startsWith("-Xmx"))
      this.maxHeap = 0L;
    else if (!this.trustedArguments.removeArgument(paramString))
      this.arguments.removeArgument(paramString);
    recomputeIsDefault();
  }

  public void parseBootClassPath(String[] paramArrayOfString)
  {
    parseBootClassPath(Config.getSystemProperty("sun.boot.class.path"), paramArrayOfString);
  }

  private void parseBootClassPath(String paramString, String[] paramArrayOfString)
  {
    if ((paramArrayOfString != null) && (paramArrayOfString.length > 0))
    {
      String[] arrayOfString = null;
      Object localObject = null;
      String str1 = Environment.getForcedDeployRoot();
      String str2 = Environment.getForcedBootClassPath();
      String str3;
      if (str1 != null)
      {
        for (int i = 0; i < paramArrayOfString.length; i++)
        {
          str3 = str1 + File.separator + "lib" + File.separator + paramArrayOfString[i];
          if (localObject == null)
            localObject = str3;
          else
            localObject = (String)localObject + File.pathSeparator + str3;
        }
        if (localObject != null)
          addInternalArgument("-Xbootclasspath/a:" + (String)localObject);
      }
      else if (str2 != null)
      {
        addInternalArgument("-Xbootclasspath/p:" + str2);
      }
      else
      {
        try
        {
          arrayOfString = paramString.split(File.pathSeparator);
        }
        catch (NoSuchMethodError localNoSuchMethodError)
        {
          arrayOfString = StringUtils.splitString(paramString, File.pathSeparator);
        }
        for (int j = 0; j < arrayOfString.length; j++)
        {
          str3 = arrayOfString[j];
          for (int k = 0; k < paramArrayOfString.length; k++)
          {
            if (!str3.endsWith(paramArrayOfString[k]))
              continue;
            if (localObject == null)
            {
              localObject = str3;
              break;
            }
            localObject = (String)localObject + File.pathSeparator + str3;
            break;
          }
        }
        if (localObject != null)
          addInternalArgument("-Xbootclasspath/a:" + (String)localObject);
      }
    }
  }

  public static String[] getPlugInDependentJars()
  {
    return (String[])(String[])PLUGIN_DEPENDENT_JARS.clone();
  }

  public List getCommandLineArguments(boolean paramBoolean1, boolean paramBoolean2)
  {
    return getCommandLineArguments(paramBoolean1, true, paramBoolean2, true, -1);
  }

  public List getCommandLineArguments(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, int paramInt)
  {
    int j = 0;
    if (paramInt < 0)
      paramInt = 2147483647;
    ArrayList localArrayList = new ArrayList();
    Object localObject2;
    int i;
    if (paramBoolean2)
    {
      localObject1 = this.internalArguments.iterator();
      while ((j < paramInt) && (((Iterator)localObject1).hasNext()))
      {
        localObject2 = (String)((Iterator)localObject1).next();
        i = ((String)localObject2).length() + 1;
        if (j + i >= paramInt)
          continue;
        localArrayList.add(localObject2);
        j += i;
      }
    }
    if (paramBoolean1)
      localArrayList.add("---");
    if (j < paramInt)
      j += this.trustedArguments.addTo(localArrayList, paramBoolean3, paramBoolean4, paramInt - j);
    if (paramBoolean1)
      localArrayList.add("--");
    Object localObject1 = getXmx();
    if (localObject1 != null)
    {
      i = ((String)localObject1).length() + 1;
      if (j + i < paramInt)
      {
        localArrayList.add(localObject1);
        j += i;
      }
    }
    if (j < paramInt)
      j += this.arguments.addTo(localArrayList, paramBoolean3, paramBoolean4, paramInt - j);
    if (j >= paramInt)
    {
      localObject2 = new Exception("Internal Error: JVMParameters.getCommandLineArguments: string size: " + j + " >= " + paramInt);
      ((Exception)localObject2).printStackTrace();
    }
    return (List)(List)localArrayList;
  }

  public String getCommandLineArgumentsAsString(boolean paramBoolean)
  {
    return StringQuoteUtil.getStringByCommandList(getCommandLineArguments(false, paramBoolean, false, true, -1));
  }

  public void addTo(Properties paramProperties)
  {
    this.trustedArguments.addTo(paramProperties);
    this.arguments.addTo(paramProperties);
  }

  public Properties getProperties()
  {
    Properties localProperties = new Properties();
    addTo(localProperties);
    return localProperties;
  }

  public void setDefault(boolean paramBoolean)
  {
    this.isDefault = paramBoolean;
  }

  public boolean isDefault()
  {
    return this.isDefault;
  }

  public boolean satisfies(JVMParameters paramJVMParameters)
  {
    if (paramJVMParameters == null)
      return false;
    if (getMaxHeapSize() < paramJVMParameters.getMaxHeapSize())
      return false;
    if ((this.isDefault) && (paramJVMParameters.isDefault))
      return true;
    if (isSecure() != paramJVMParameters.isSecure())
      return false;
    if ((!this.isDefault) && (paramJVMParameters.isDefault))
      return false;
    int i = paramJVMParameters.size();
    for (int j = 0; j < i; j++)
    {
      String str = paramJVMParameters.get(j);
      if ((!isExcluded(str)) && (!contains(str)))
        return false;
    }
    return true;
  }

  public boolean satisfiesSecure(JVMParameters paramJVMParameters)
  {
    if (paramJVMParameters == null)
      return false;
    if (getMaxHeapSize() < paramJVMParameters.getMaxHeapSize())
      return false;
    if ((this.isDefault) && (paramJVMParameters.isDefault))
      return true;
    if ((!this.isDefault) && (paramJVMParameters.isDefault))
      return false;
    int i = paramJVMParameters.size();
    for (int j = 0; j < i; j++)
    {
      String str = paramJVMParameters.get(j);
      if ((!isExcluded(str)) && (isSecureArgument(str)) && (!contains(str)))
        return false;
    }
    return true;
  }

  private static boolean isSecureArgument(String paramString)
  {
    Property localProperty = Property.createProperty(paramString);
    if (localProperty != null)
      return localProperty.isSecure();
    return Config.isSecureVmArg(paramString);
  }

  public static boolean isJVMCommandLineArgument(String paramString)
  {
    if (paramString == null)
      return false;
    paramString = StringQuoteUtil.unquoteIfNeeded(paramString);
    return paramString.charAt(0) == '-';
  }

  public long getMaxHeapSize()
  {
    return this.maxHeap > 0L ? this.maxHeap : getDefaultHeapSize();
  }

  public void setMaxHeapSize(long paramLong)
  {
    if (paramLong > 0L)
      this.maxHeap = paramLong;
    if ((this.maxHeap > 0L) && (this.maxHeap != getDefaultHeapSize()))
      this.isDefault = false;
  }

  public static final long getDefaultHeapSize()
  {
    if ((runningJVMParameters == null) || (!runningJVMParameters.isDefault()))
      return DEFAULT_HEAP;
    try
    {
      long l1 = Runtime.getRuntime().maxMemory();
      long l2 = snapToKnownMemorySize(l1);
      if (l2 > 0L)
        return l2;
    }
    catch (Throwable localThrowable)
    {
    }
    return DEFAULT_HEAP;
  }

  static long snapToKnownMemorySize(long paramLong)
  {
    if (paramLong < 0L)
      return 0L;
    long l = Math.max(paramLong / 10L, 16777216L);
    for (int i = 0; i < MEMSIZES.length; i++)
      if (Math.abs(paramLong - MEMSIZES[i]) < l)
        return MEMSIZES[i];
    return 0L;
  }

  public static JVMParameters getRunningJVMParameters()
  {
    return runningJVMParameters;
  }

  public static void setRunningJVMParameters(JVMParameters paramJVMParameters)
  {
    runningJVMParameters = new JVMParameters();
    Properties localProperties = new Properties();
    Config.addSecureSystemPropertiesTo(localProperties);
    runningJVMParameters.addProperties(localProperties);
    runningJVMParameters.addArguments(paramJVMParameters);
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer("[JVMParameters: isSecure: " + isSecure() + ", args: ");
    localStringBuffer.append(getCommandLineArgumentsAsString(true));
    localStringBuffer.append("]");
    return localStringBuffer.toString();
  }

  public boolean contains(String paramString)
  {
    int i = size();
    for (int j = 0; j < i; j++)
      if (paramString.equals(get(j)))
        return true;
    return false;
  }

  public boolean isExcluded(String paramString)
  {
    if ((paramString.startsWith("-Djnlp.")) || (paramString.startsWith("-Djavaws.")))
    {
      if (Environment.isJavaWebStart())
        return true;
      if (Property.isJnlpProperty(paramString))
        return true;
    }
    return false;
  }

  public boolean containsPrefix(String paramString)
  {
    int i = size();
    for (int j = 0; j < i; j++)
      if (get(j).startsWith(paramString))
        return true;
    return false;
  }

  public boolean isSecure()
  {
    return this.arguments.isSecure();
  }

  public static long parseMemorySpec(String paramString)
    throws IllegalArgumentException
  {
    char c = '\000';
    String str = paramString;
    for (int i = 0; i < paramString.length(); i++)
    {
      if (Character.isDigit(paramString.charAt(i)))
        continue;
      if (i != paramString.length() - 1)
        throw new IllegalArgumentException("Too many characters after heap size specifier: " + paramString);
      c = paramString.charAt(i);
      paramString = paramString.substring(0, paramString.length() - 1);
      break;
    }
    try
    {
      long l = Long.parseLong(paramString);
      if (c != 0)
        switch (c)
        {
        case 'T':
        case 't':
          l *= 0L;
          break;
        case 'G':
        case 'g':
          l *= 1073741824L;
          break;
        case 'M':
        case 'm':
          l *= 1048576L;
          break;
        case 'K':
        case 'k':
          l *= 1024L;
          break;
        default:
          throw new IllegalArgumentException("Illegal heap size specifier " + c + " in " + str);
        }
      return l;
    }
    catch (NumberFormatException localNumberFormatException)
    {
    }
    throw ((IllegalArgumentException)new IllegalArgumentException().initCause(localNumberFormatException));
  }

  public static String unparseMemorySpec(long paramLong)
  {
    if (paramLong % 1073741824L == 0L)
      return "" + paramLong / 1073741824L + "g";
    if (paramLong % 1048576L == 0L)
      return "" + paramLong / 1048576L + "m";
    if (paramLong % 1024L == 0L)
      return "" + paramLong / 1024L + "k";
    return "" + paramLong;
  }

  private static String tail(String paramString1, String paramString2)
  {
    return paramString1.substring(paramString2.length());
  }

  private boolean addArgumentImpl(String paramString, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    boolean bool;
    if ((paramBoolean1) && (paramString.startsWith("-Xmx")))
    {
      try
      {
        this.maxHeap = parseMemorySpec(tail(paramString, "-Xmx"));
      }
      catch (IllegalArgumentException localIllegalArgumentException)
      {
      }
      bool = true;
    }
    else if (paramBoolean2)
    {
      bool = this.trustedArguments.addArgument(paramString, paramBoolean1);
    }
    else if (paramBoolean3)
    {
      if ((Config.isSecureVmArg(paramString)) || (Config.isSecureSystemProperty(paramString)))
        bool = this.arguments.addArgument(paramString, paramBoolean1);
      else
        bool = true;
    }
    else
    {
      bool = this.arguments.addArgument(paramString, paramBoolean1);
    }
    recomputeIsDefault();
    return bool;
  }

  private String getXmx()
  {
    if (getMaxHeapSize() != getDefaultHeapSize())
      return "-Xmx" + unparseMemorySpec(this.maxHeap);
    return null;
  }

  private void recomputeIsDefault()
  {
    this.isDefault = ((getMaxHeapSize() == getDefaultHeapSize()) && (this.arguments.isDefault()));
  }

  private int size()
  {
    return this.trustedArguments.size() + this.arguments.size();
  }

  private String get(int paramInt)
  {
    if (paramInt < this.trustedArguments.size())
      return this.trustedArguments.get(paramInt);
    paramInt -= this.trustedArguments.size();
    return this.arguments.get(paramInt);
  }

  static class ArgumentSet
  {
    private OrderedHashSet dashXOptions = new OrderedHashSet();
    private OrderedHashSet systemProperties = new OrderedHashSet();
    private OrderedHashSet otherArguments = new OrderedHashSet();
    public static final int NUM_ARGUMENT_LISTS = 3;

    public void clear()
    {
      this.dashXOptions.clear();
      this.systemProperties.clear();
      this.otherArguments.clear();
    }

    private String[] systemProperties2StringArray()
    {
      String[] arrayOfString = new String[this.systemProperties.size()];
      int i = 0;
      Iterator localIterator = this.systemProperties.iterator();
      while (localIterator.hasNext())
        arrayOfString[(i++)] = ((Property)localIterator.next()).toString();
      return arrayOfString;
    }

    public void copyToStringArrays(String[][] paramArrayOfString, int paramInt)
    {
      paramArrayOfString[(paramInt + 0)] = ((String[])(String[])this.dashXOptions.toArray(new String[0]));
      paramArrayOfString[(paramInt + 1)] = systemProperties2StringArray();
      paramArrayOfString[(paramInt + 2)] = ((String[])(String[])this.otherArguments.toArray(new String[0]));
    }

    public void getFromStringArrays(String[][] paramArrayOfString, int paramInt)
    {
      for (int i = 0; i < paramArrayOfString[(paramInt + 0)].length; i++)
        this.dashXOptions.add(paramArrayOfString[(paramInt + 0)][i]);
      for (i = 0; i < paramArrayOfString[(paramInt + 1)].length; i++)
        this.systemProperties.add(new Property(paramArrayOfString[(paramInt + 1)][i]));
      for (i = 0; i < paramArrayOfString[(paramInt + 2)].length; i++)
        this.otherArguments.add(paramArrayOfString[(paramInt + 2)][i]);
    }

    public void addAll(ArgumentSet paramArgumentSet)
    {
      this.dashXOptions.addAll(paramArgumentSet.dashXOptions);
      this.systemProperties.addAll(paramArgumentSet.systemProperties);
      this.otherArguments.addAll(paramArgumentSet.otherArguments);
    }

    public void addAll(Properties paramProperties)
    {
      Enumeration localEnumeration = paramProperties.propertyNames();
      while (localEnumeration.hasMoreElements())
      {
        String str1 = (String)localEnumeration.nextElement();
        String str2 = paramProperties.getProperty(str1);
        this.systemProperties.add(new Property(str1, str2));
      }
    }

    public void addAll(List paramList)
    {
      Iterator localIterator = paramList.iterator();
      while (localIterator.hasNext())
      {
        Object localObject = localIterator.next();
        if ((localObject instanceof Property))
          this.systemProperties.add(localObject);
      }
    }

    public int addTo(List paramList, boolean paramBoolean1, boolean paramBoolean2, int paramInt)
    {
      int i = 0;
      ArrayList localArrayList = new ArrayList();
      Iterator localIterator = this.otherArguments.iterator();
      String str;
      int j;
      while ((i < paramInt) && (localIterator.hasNext()))
      {
        str = (String)localIterator.next();
        j = str.length() + 1;
        if (((!paramBoolean2) && (!Config.isSecureVmArg(str))) || (i + j >= paramInt))
          continue;
        localArrayList.add(str);
        i += j;
      }
      localIterator = this.dashXOptions.iterator();
      while ((i < paramInt) && (localIterator.hasNext()))
      {
        str = (String)localIterator.next();
        j = str.length() + 1;
        if (((!paramBoolean2) && (!Config.isSecureVmArg(str))) || (i + j >= paramInt))
          continue;
        paramList.add(str);
        i += j;
      }
      localIterator = this.systemProperties.iterator();
      while ((i < paramInt) && (localIterator.hasNext()))
      {
        Property localProperty = (Property)localIterator.next();
        str = localProperty.toString(paramBoolean1);
        j = str.length() + 1;
        if (((paramBoolean2) || (localProperty.isSecure())) && (i + j < paramInt))
        {
          paramList.add(str);
          i += j;
        }
      }
      paramList.addAll(localArrayList);
      return i;
    }

    public void addTo(Properties paramProperties)
    {
      Iterator localIterator = this.systemProperties.iterator();
      while (localIterator.hasNext())
        ((Property)localIterator.next()).addTo(paramProperties);
    }

    public boolean addArgument(String paramString, boolean paramBoolean)
    {
      int i = 0;
      Object localObject = null;
      if (paramString.startsWith("-Dsun.plugin2.jvm.args"))
        throw new IllegalArgumentException("May not specify the sun.plugin2.jvm.args system property");
      if (paramBoolean)
      {
        Property localProperty = Property.createProperty(paramString);
        if (localProperty != null)
        {
          this.systemProperties.add(localProperty);
          return true;
        }
        if (paramString.startsWith("-X"))
        {
          if ((paramString.startsWith("-Xbootclasspath:")) || (paramString.startsWith("-Xbootclasspath/p:")))
            return true;
          this.dashXOptions.add(paramString);
          return true;
        }
      }
      this.otherArguments.add(paramString);
      return paramString.startsWith("-");
    }

    public boolean removeArgument(String paramString)
    {
      if (paramString.startsWith("-X"))
        return removeArgumentHelper(this.dashXOptions, paramString);
      if (paramString.startsWith("-D"))
        return removeArgumentHelper(this.systemProperties, new Property(paramString));
      return removeArgumentHelper(this.otherArguments, paramString);
    }

    private boolean removeArgumentHelper(OrderedHashSet paramOrderedHashSet, Property paramProperty)
    {
      return paramOrderedHashSet.remove(paramProperty);
    }

    private boolean removeArgumentHelper(OrderedHashSet paramOrderedHashSet, String paramString)
    {
      Iterator localIterator = paramOrderedHashSet.iterator();
      while (localIterator.hasNext())
      {
        String str = (String)localIterator.next();
        if (str.startsWith(paramString))
        {
          paramOrderedHashSet.remove(str);
          return true;
        }
      }
      return false;
    }

    public boolean isDefault()
    {
      return (this.dashXOptions.isEmpty()) && (this.systemProperties.isEmpty()) && (this.otherArguments.isEmpty());
    }

    public boolean isSecure()
    {
      Iterator localIterator = this.dashXOptions.iterator();
      while (localIterator.hasNext())
        if (!Config.isSecureVmArg((String)localIterator.next()))
          return false;
      localIterator = this.otherArguments.iterator();
      while (localIterator.hasNext())
        if (!Config.isSecureVmArg((String)localIterator.next()))
          return false;
      localIterator = this.systemProperties.iterator();
      while (localIterator.hasNext())
      {
        Property localProperty = (Property)localIterator.next();
        if (!localProperty.isSecure())
          return false;
      }
      return true;
    }

    public int size()
    {
      return this.dashXOptions.size() + this.systemProperties.size() + this.otherArguments.size();
    }

    public String get(int paramInt)
    {
      if (paramInt < this.dashXOptions.size())
        return (String)this.dashXOptions.get(paramInt);
      paramInt -= this.dashXOptions.size();
      if (paramInt < this.systemProperties.size())
        return ((Property)this.systemProperties.get(paramInt)).toString();
      paramInt -= this.systemProperties.size();
      return (String)this.otherArguments.get(paramInt);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.JVMParameters
 * JD-Core Version:    0.6.0
 */