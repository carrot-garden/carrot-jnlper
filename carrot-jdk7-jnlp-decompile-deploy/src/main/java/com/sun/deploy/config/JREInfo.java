package com.sun.deploy.config;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.SyncAccess;
import com.sun.deploy.util.SyncAccess.Lock;
import com.sun.deploy.util.VersionID;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

public class JREInfo
{
  private static ArrayList _jres = new ArrayList();
  private static SyncAccess syncAccess = new SyncAccess(8);
  private String _platform;
  private VersionID _platformVersion;
  private String _product;
  private VersionID _productVersion;
  private String _location;
  private String _path;
  private NativePlatform _nativePlatform;
  private String _vm_args;
  private boolean _enabled;
  private boolean _registered;
  private boolean _system;

  public String getPlatform()
  {
    return this._platform;
  }

  public VersionID getPlatformVersion()
  {
    return this._platformVersion;
  }

  public String getProduct()
  {
    return this._product;
  }

  public VersionID getProductVersion()
  {
    return this._productVersion;
  }

  public String getLocation()
  {
    return this._location;
  }

  public String getPath()
  {
    return this._path;
  }

  public String getVmArgs()
  {
    return this._vm_args;
  }

  public String getDebugJavaPath()
  {
    return Platform.get().getDebugJavaPath(this._path);
  }

  public String getOSName()
  {
    return this._nativePlatform.getOSName();
  }

  public String getOSArch()
  {
    return this._nativePlatform.getOSArch();
  }

  public boolean isEnabled()
  {
    return this._enabled;
  }

  public boolean isRegistered()
  {
    return this._registered;
  }

  public boolean isSystemJRE()
  {
    return this._system;
  }

  public void setPlatform(String paramString)
  {
    this._platform = paramString;
    this._platformVersion = new VersionID(paramString);
  }

  public void setProduct(String paramString)
  {
    this._product = paramString;
    this._productVersion = new VersionID(paramString);
  }

  public void setLocation(String paramString)
  {
    this._location = paramString;
  }

  public void setPath(String paramString)
  {
    this._path = Config.getJavaCommand(Platform.get().getLongPathName(paramString));
  }

  public void setVmArgs(String paramString)
  {
    this._vm_args = paramString;
  }

  public void setEnabled(boolean paramBoolean)
  {
    this._enabled = paramBoolean;
  }

  public void setRegistered(boolean paramBoolean)
  {
    this._registered = paramBoolean;
  }

  public void setSystemJRE(boolean paramBoolean)
  {
    this._system = paramBoolean;
  }

  public void setOSName(String paramString)
  {
    this._nativePlatform = new NativePlatform(paramString, getOSArch());
  }

  public void setOSArch(String paramString)
  {
    this._nativePlatform = new NativePlatform(getOSName(), paramString);
  }

  public JREInfo(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, boolean paramBoolean1, boolean paramBoolean2)
  {
    setProduct(paramString2);
    if (null != paramString1)
      setPlatform(paramString1);
    else
      setPlatform(getPlatformByProduct(paramString2));
    this._location = paramString3;
    this._path = Config.getJavaCommand(Platform.get().getLongPathName(paramString4));
    this._vm_args = paramString5;
    this._enabled = paramBoolean1;
    this._registered = paramBoolean2;
    this._system = false;
    this._nativePlatform = new NativePlatform(paramString6, paramString7);
    if (this._location == null)
      this._location = Config.getStringProperty("deployment.javaws.installURL");
  }

  public JREInfo(int paramInt, Properties paramProperties, boolean paramBoolean)
  {
    this(paramProperties.getProperty("deployment.javaws.jre." + paramInt + ".platform"), paramProperties.getProperty("deployment.javaws.jre." + paramInt + ".product"), paramProperties.getProperty("deployment.javaws.jre." + paramInt + ".location"), paramProperties.getProperty("deployment.javaws.jre." + paramInt + ".path"), paramProperties.getProperty("deployment.javaws.jre." + paramInt + ".args"), paramProperties.getProperty("deployment.javaws.jre." + paramInt + ".osname"), paramProperties.getProperty("deployment.javaws.jre." + paramInt + ".osarch"), false, false);
    String str = paramProperties.getProperty("deployment.javaws.jre." + paramInt + ".enabled");
    if ((str != null) && (Boolean.valueOf(str).booleanValue()))
      setEnabled(true);
    str = paramProperties.getProperty("deployment.javaws.jre." + paramInt + ".registered");
    if ((str != null) && (Boolean.valueOf(str).booleanValue()))
      setRegistered(true);
    setSystemJRE(paramBoolean);
  }

  public JREInfo(JREInfo paramJREInfo)
  {
    this(paramJREInfo.getPlatform(), paramJREInfo.getProduct(), paramJREInfo.getLocation(), paramJREInfo.getPath(), paramJREInfo.getVmArgs(), paramJREInfo.getOSName(), paramJREInfo.getOSArch(), paramJREInfo.isEnabled(), paramJREInfo.isRegistered());
    setSystemJRE(paramJREInfo.isSystemJRE());
  }

  public boolean isOsInfoMatch()
  {
    return this._nativePlatform.compatible(NativePlatform.getCurrentNativePlatform());
  }

  public boolean isOsInfoMatch(String paramString1, String paramString2)
  {
    return this._nativePlatform.compatible(new NativePlatform(paramString1, paramString2));
  }

  public String toString()
  {
    int i = findJREByPath_int(this, _jres);
    return (i >= 0 ? "JREInfo for index " + i + ":\n" : "JREInfo (not in list):\n") + "    platform is: " + this._platform + "\n" + "    product is: " + this._product + "\n" + "    location is: " + this._location + "\n" + "    path is: " + this._path + "\n" + "    args is: " + this._vm_args + "\n" + "    native platform is: " + this._nativePlatform.toString() + "\n" + "    enabled is: " + this._enabled + "\n" + "    registered is: " + this._registered + "\n" + "    system is: " + this._system + "\n" + "";
  }

  public String getJREPath()
  {
    return Config.getJavaHome(getPath());
  }

  public static String getPlatformByProduct(String paramString)
  {
    if (paramString != null)
    {
      int i = paramString.indexOf(".", 0);
      if ((i > 0) && (i < paramString.length() - 1))
      {
        int j = paramString.indexOf(".", i + 1);
        if (j > i)
          return paramString.substring(0, j);
        j = paramString.indexOf("*", i + 1);
        if (j > i)
          return paramString.substring(0, j);
      }
    }
    return paramString;
  }

  public static int findJREByPath(JREInfo paramJREInfo, ArrayList paramArrayList)
  {
    SyncAccess.Lock localLock = syncAccess.lock(2);
    int i;
    try
    {
      i = findJREByPath_int(paramJREInfo, paramArrayList);
    }
    finally
    {
      localLock.release();
    }
    return i;
  }

  public static int findJREByProductVersion(JREInfo paramJREInfo, ArrayList paramArrayList)
  {
    SyncAccess.Lock localLock = syncAccess.lock(2);
    int i;
    try
    {
      i = findJREByProductVersion_int(paramJREInfo, paramArrayList);
    }
    finally
    {
      localLock.release();
    }
    return i;
  }

  public static void addJRE(JREInfo paramJREInfo)
  {
    addJRE(paramJREInfo, true);
  }

  public static void addJRE(JREInfo paramJREInfo, boolean paramBoolean)
  {
    SyncAccess.Lock localLock = syncAccess.lock(4);
    try
    {
      addJRE_int(paramJREInfo, paramBoolean);
    }
    finally
    {
      localLock.release();
    }
  }

  public static void removeJRE(int paramInt)
  {
    SyncAccess.Lock localLock = syncAccess.lock(4);
    try
    {
      _jres.remove(paramInt);
    }
    finally
    {
      localLock.release();
    }
  }

  public static JREInfo getJREInfo(int paramInt)
  {
    SyncAccess.Lock localLock = syncAccess.lock(2);
    JREInfo localJREInfo;
    try
    {
      localJREInfo = (JREInfo)_jres.get(paramInt);
    }
    finally
    {
      localLock.release();
    }
    return localJREInfo;
  }

  public static void setJREInfo(int paramInt, JREInfo paramJREInfo)
  {
    SyncAccess.Lock localLock = syncAccess.lock(4);
    try
    {
      _jres.set(paramInt, paramJREInfo);
    }
    finally
    {
      localLock.release();
    }
  }

  public static void clear()
  {
    SyncAccess.Lock localLock = syncAccess.lock(4);
    try
    {
      _jres.clear();
    }
    finally
    {
      localLock.release();
    }
  }

  public static VersionID getLatestVersion(boolean paramBoolean)
  {
    SyncAccess.Lock localLock = syncAccess.lock(4);
    Object localObject1 = null;
    try
    {
      for (int i = 0; i < _jres.size(); i++)
      {
        JREInfo localJREInfo = (JREInfo)_jres.get(i);
        if ((!paramBoolean) && (!localJREInfo.isEnabled()))
          continue;
        VersionID localVersionID = localJREInfo.getProductVersion();
        if ((localVersionID == null) || ((localObject1 != null) && (!localVersionID.isGreaterThan((VersionID)localObject1))))
          continue;
        localObject1 = localVersionID;
      }
      localObject2 = localObject1;
    }
    finally
    {
      Object localObject2;
      localLock.release();
    }
  }

  public static JREInfo[] getAll()
  {
    SyncAccess.Lock localLock = syncAccess.lock(2);
    JREInfo[] arrayOfJREInfo;
    try
    {
      arrayOfJREInfo = (JREInfo[])(JREInfo[])_jres.toArray(new JREInfo[0]);
    }
    finally
    {
      localLock.release();
    }
    return arrayOfJREInfo;
  }

  public static void initialize(Properties paramProperties1, Properties paramProperties2)
  {
    SyncAccess.Lock localLock = syncAccess.lock(4);
    try
    {
      ArrayList localArrayList = new ArrayList();
      int i = -1;
      _jres.clear();
      Enumeration localEnumeration = paramProperties2.keys();
      String str;
      int j;
      Integer localInteger;
      while (localEnumeration.hasMoreElements())
      {
        str = (String)localEnumeration.nextElement();
        if (str.startsWith("deployment.javaws.jre."))
        {
          j = getJREIndex_int(str);
          if ((j >= 0) && (j != i))
          {
            localInteger = new Integer(j);
            if (!localArrayList.contains(localInteger))
            {
              localArrayList.add(localInteger);
              addJRE_int(new JREInfo(j, paramProperties2, false), true);
            }
            i = j;
          }
        }
      }
      localEnumeration = paramProperties1.keys();
      i = -1;
      localArrayList = new ArrayList();
      while (localEnumeration.hasMoreElements())
      {
        str = (String)localEnumeration.nextElement();
        if (str.startsWith("deployment.javaws.jre."))
        {
          j = getJREIndex_int(str);
          if ((j >= 0) && (j != i))
          {
            localInteger = new Integer(j);
            if (!localArrayList.contains(localInteger))
            {
              localArrayList.add(localInteger);
              addJRE_int(new JREInfo(j, paramProperties1, true), true);
            }
            i = j;
          }
        }
      }
      validateJREs_int();
      validateHomeJRE_int();
    }
    finally
    {
      localLock.release();
    }
  }

  public static void importJpiEntries(Properties paramProperties)
  {
    SyncAccess.Lock localLock = syncAccess.lock(4);
    try
    {
      HashMap localHashMap1 = new HashMap();
      HashMap localHashMap2 = new HashMap();
      HashMap localHashMap3 = new HashMap();
      HashMap localHashMap4 = new HashMap();
      HashMap localHashMap5 = new HashMap();
      Enumeration localEnumeration = paramProperties.keys();
      String str1;
      Object localObject2;
      while (localEnumeration.hasMoreElements())
      {
        localObject1 = (String)localEnumeration.nextElement();
        str1 = (String)paramProperties.get(localObject1);
        localObject2 = null;
        if (((String)localObject1).startsWith("deployment.javapi.jre."))
        {
          int i = "deployment.javapi.jre.".length();
          int j;
          if (((String)localObject1).endsWith(".osarch"))
          {
            j = ((String)localObject1).indexOf(".osarch");
            localObject2 = ((String)localObject1).substring(i, j);
            localHashMap5.put(localObject2, str1);
          }
          else if (((String)localObject1).endsWith(".osname"))
          {
            j = ((String)localObject1).indexOf(".osname");
            localObject2 = ((String)localObject1).substring(i, j);
            localHashMap4.put(localObject2, str1);
          }
          else if (((String)localObject1).endsWith(".path"))
          {
            j = ((String)localObject1).indexOf(".path");
            localObject2 = ((String)localObject1).substring(i, j);
            localHashMap1.put(localObject2, str1);
          }
          else if (((String)localObject1).endsWith(".args"))
          {
            j = ((String)localObject1).indexOf(".args");
            localObject2 = ((String)localObject1).substring(i, j);
            localHashMap2.put(localObject2, str1);
          }
          else if (((String)localObject1).endsWith(".enabled"))
          {
            j = ((String)localObject1).indexOf(".enabled");
            localObject2 = ((String)localObject1).substring(i, j);
            localHashMap3.put(localObject2, Boolean.valueOf(str1));
          }
        }
      }
      Object localObject1 = localHashMap1.keySet().iterator();
      while (((Iterator)localObject1).hasNext())
      {
        str1 = (String)((Iterator)localObject1).next();
        String str2 = (String)localHashMap1.get(str1);
        String str3 = (String)localHashMap4.get(str1);
        String str4 = (String)localHashMap5.get(str1);
        String str5 = (String)localHashMap2.get(str1);
        localObject2 = (Boolean)localHashMap3.get(str1);
        boolean bool = localObject2 == null ? true : ((Boolean)localObject2).booleanValue();
        JREInfo localJREInfo = new JREInfo((String)null, str1, (String)null, str2, str5, str3, str4, bool, false);
        addJRE_int(localJREInfo, true);
      }
    }
    finally
    {
      localLock.release();
    }
  }

  public static boolean setInstalledJREList(Vector paramVector)
  {
    int i = 0;
    SyncAccess.Lock localLock = syncAccess.lock(4);
    try
    {
      for (int j = 0; j < paramVector.size(); j += 2)
      {
        String str1 = (String)paramVector.get(j);
        String str2 = (String)paramVector.get(j + 1);
        if (str1.lastIndexOf(".") <= 2)
          continue;
        JREInfo localJREInfo = new JREInfo((String)null, str1, (String)null, str2, "", Config.getOSName(), Config.getOSArch(), true, true);
        int k = findJREByPath_int(localJREInfo, _jres);
        if (k < 0)
        {
          addJRE_int(localJREInfo, false);
          i = 1;
        }
        else
        {
          ((JREInfo)_jres.get(k)).setRegistered(true);
        }
      }
      validateJREs_int();
      validateHomeJRE_int();
    }
    finally
    {
      localLock.release();
    }
    return i;
  }

  public static void printJREs()
  {
    SyncAccess.Lock localLock = syncAccess.lock(2);
    try
    {
      System.out.println("\nJREInfo: " + _jres.size() + " entries");
      for (int i = 0; i < _jres.size(); i++)
      {
        System.out.println("JREInfo " + i + ":");
        System.out.println(_jres.get(i).toString());
      }
    }
    finally
    {
      localLock.release();
    }
  }

  public static void traceJREs()
  {
    if (!Trace.isEnabled(TraceLevel.BASIC))
      return;
    SyncAccess.Lock localLock = syncAccess.lock(2);
    try
    {
      Trace.println("\nJREInfo: " + _jres.size() + " entries", TraceLevel.BASIC);
      for (int i = 0; i < _jres.size(); i++)
      {
        Trace.println("JREInfo " + i + ":", TraceLevel.BASIC);
        Trace.println(_jres.get(i).toString(), TraceLevel.BASIC);
      }
    }
    finally
    {
      localLock.release();
    }
  }

  public static String getKnownPlatforms()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    SyncAccess.Lock localLock = syncAccess.lock(2);
    try
    {
      for (int i = 0; i < _jres.size(); i++)
      {
        JREInfo localJREInfo = (JREInfo)_jres.get(i);
        localStringBuffer.append(localJREInfo.getPlatform());
        localStringBuffer.append(" ");
      }
    }
    finally
    {
      localLock.release();
    }
    return localStringBuffer.toString();
  }

  public static String getDefaultJavaPath()
  {
    SyncAccess.Lock localLock = syncAccess.lock(2);
    String str;
    try
    {
      JREInfo localJREInfo = getHomeJRE_int();
      if (localJREInfo != null)
        str = localJREInfo.getPath();
      else
        str = ((JREInfo)_jres.get(0)).getPath();
    }
    finally
    {
      localLock.release();
    }
    return str;
  }

  public static JREInfo getHomeJRE()
  {
    SyncAccess.Lock localLock = syncAccess.lock(2);
    JREInfo localJREInfo;
    try
    {
      localJREInfo = getHomeJRE_int();
    }
    finally
    {
      localLock.release();
    }
    return localJREInfo;
  }

  public static void removeJREsIn(String paramString)
  {
    SyncAccess.Lock localLock = syncAccess.lock(4);
    try
    {
      Iterator localIterator = _jres.iterator();
      while (localIterator.hasNext())
      {
        JREInfo localJREInfo = (JREInfo)localIterator.next();
        String str = localJREInfo.getPath();
        if ((str != null) && (str.startsWith(paramString)))
          localIterator.remove();
      }
    }
    finally
    {
      localLock.release();
    }
  }

  public static boolean isValidJREPath(String paramString)
  {
    if (paramString != null)
    {
      File localFile = new File(paramString);
      return (localFile.exists()) && (!localFile.isDirectory());
    }
    return false;
  }

  private static int findJREByPath_int(JREInfo paramJREInfo, ArrayList paramArrayList)
  {
    for (int i = paramArrayList.size() - 1; i >= 0; i--)
    {
      JREInfo localJREInfo = (JREInfo)paramArrayList.get(i);
      if ((paramJREInfo.isOsInfoMatch(localJREInfo.getOSName(), localJREInfo.getOSArch())) && (paramJREInfo.isSystemJRE() == localJREInfo.isSystemJRE()) && (paramJREInfo.getPath() != null) && (Platform.get().samePaths(paramJREInfo.getPath(), localJREInfo.getPath())))
        break;
    }
    return i;
  }

  private static int findJREByProductVersion_int(JREInfo paramJREInfo, ArrayList paramArrayList)
  {
    String str1 = paramJREInfo.getLocation();
    if (str1 == null)
      return -1;
    for (int i = paramArrayList.size() - 1; i >= 0; i--)
    {
      JREInfo localJREInfo = (JREInfo)paramArrayList.get(i);
      String str2 = localJREInfo.getLocation();
      if ((paramJREInfo.isSystemJRE() == localJREInfo.isSystemJRE()) && (str2 != null) && (str1.equals(str2)) && (paramJREInfo.getProductVersion().equals(localJREInfo.getProductVersion())))
        break;
    }
    return i;
  }

  private static void addJRE_int(JREInfo paramJREInfo, boolean paramBoolean)
  {
    if ((paramJREInfo.getPath() != null) && (paramJREInfo.getPlatform() != null) && (paramJREInfo.getPlatform().compareTo("1.3") >= 0))
    {
      int i = findJREByPath_int(paramJREInfo, _jres);
      if ((i >= 0) && (!paramBoolean))
        return;
      JREInfo localJREInfo;
      if (i >= 0)
      {
        localJREInfo = (JREInfo)_jres.get(i);
        if (((paramJREInfo.getVmArgs() == null) || ("".equals(paramJREInfo.getVmArgs()))) && (null != localJREInfo.getVmArgs()))
          paramJREInfo.setVmArgs(localJREInfo.getVmArgs());
        _jres.remove(i);
      }
      for (i = 0; i < _jres.size(); i++)
      {
        localJREInfo = (JREInfo)_jres.get(i);
        if (!paramJREInfo.getProductVersion().isGreaterThanOrEqual(localJREInfo.getProductVersion()))
          continue;
        _jres.add(i, paramJREInfo);
        return;
      }
      _jres.add(paramJREInfo);
    }
  }

  private static void validateHomeJRE_int()
  {
    JREInfo localJREInfo1 = getHomeJRE_int();
    if (localJREInfo1 != null)
    {
      int i = findJREByPath_int(localJREInfo1, _jres);
      if (i < 0)
        addJRE_int(localJREInfo1, true);
      JREInfo localJREInfo2 = new JREInfo(localJREInfo1);
      localJREInfo2.setSystemJRE(true);
      i = findJREByPath_int(localJREInfo2, _jres);
      if (i < 0)
        addJRE_int(localJREInfo2, true);
      for (int j = 0; j < _jres.size(); j++)
      {
        JREInfo localJREInfo3 = (JREInfo)_jres.get(j);
        if ((localJREInfo3.isSystemJRE()) || (!localJREInfo3.getProduct().equals(localJREInfo1.getProduct())) || (!localJREInfo3.getOSName().equals(localJREInfo1.getOSName())) || (!localJREInfo3.getOSArch().equals(localJREInfo1.getOSArch())) || (Platform.get().samePaths(localJREInfo3.getPath(), localJREInfo1.getPath())))
          continue;
        _jres.remove(j);
      }
    }
  }

  private static void validateJREs_int()
  {
    for (int i = 0; i < _jres.size(); i++)
    {
      JREInfo localJREInfo = (JREInfo)_jres.get(i);
      if ((!localJREInfo.isOsInfoMatch()) || (isValidJREPath(localJREInfo.getPath())))
        continue;
      _jres.remove(i);
    }
  }

  private static int getJREIndex_int(String paramString)
  {
    int i = "deployment.javaws.jre.".length();
    int j = paramString.indexOf(".", i);
    if (j > i)
    {
      String str = paramString.substring(i, j);
      try
      {
        return Integer.parseInt(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    }
    return -1;
  }

  private static JREInfo getHomeJRE_int()
  {
    String str1 = Config.getJavaVersion();
    String str2 = Platform.get().getLongPathName(Config.getJREHome());
    Object localObject = new JREInfo((String)null, str1, "http://java.sun.com/products/autodl/j2se", str2, "", Config.getOSName(), Config.getOSArch(), true, false);
    int i = findJREByPath_int((JREInfo)localObject, _jres);
    if (i >= 0)
    {
      JREInfo localJREInfo = (JREInfo)_jres.get(i);
      if (!((JREInfo)localObject).getProduct().equals(localJREInfo.getProduct()))
        localJREInfo.setProduct(((JREInfo)localObject).getProduct());
      localObject = localJREInfo;
    }
    if (!((JREInfo)localObject).isOsInfoMatch())
    {
      ((JREInfo)localObject).setOSName(Config.getOSName());
      ((JREInfo)localObject).setOSArch(Config.getOSArch());
    }
    return (JREInfo)localObject;
  }

  static
  {
    validateHomeJRE_int();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.JREInfo
 * JD-Core Version:    0.6.0
 */