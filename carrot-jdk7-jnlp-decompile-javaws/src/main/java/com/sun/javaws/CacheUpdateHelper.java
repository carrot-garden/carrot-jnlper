package com.sun.javaws;

import com.sun.deploy.Environment;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.config.Platform;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.CacheUpdateProgressDialog;
import com.sun.deploy.ui.CacheUpdateProgressDialog.CanceledException;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

public class CacheUpdateHelper
{
  private static final char DIRECTORY_TYPE = 'D';
  private static final char TEMP_TYPE = 'X';
  private static final char VERSION_TYPE = 'V';
  private static final char INDIRECT_TYPE = 'I';
  private static final char RESOURCE_TYPE = 'R';
  private static final char APPLICATION_TYPE = 'A';
  private static final char EXTENSION_TYPE = 'E';
  private static final char MUFFIN_TYPE = 'P';
  private static final char MAIN_FILE_TAG = 'M';
  private static final char NATIVELIB_FILE_TAG = 'N';
  private static final char TIMESTAMP_FILE_TAG = 'T';
  private static final char CERTIFICATE_FILE_TAG = 'C';
  private static final char LAP_FILE_TAG = 'L';
  private static final char MAPPED_IMAGE_FILE_TAG = 'B';
  private static final char MUFFIN_ATTR_FILE_TAG = 'U';
  private static final String MUFFIN_PREFIX = "PM";
  private static final String MUFFIN_ATTRIBUTE_PREFIX = "PU";
  private static final String APP_PREFIX = "AM";
  private static final String EXT_PREFIX = "XM";
  private static final String LAP_PREFIX = "AL";
  private static final String JAR_PREFIX = "RM";
  private static final String NATIVE_PREFIX = "RN";
  private static final String DIR_PREFIX = "DM";
  private static final DateFormat _df = DateFormat.getDateTimeInstance();
  private static final LocalInstallHandler _lih = LocalInstallHandler.getInstance();

  public static boolean updateCache()
  {
    String str1 = Config.getOldJavawsCacheDir();
    File localFile1 = new File(str1);
    File localFile2 = Cache.getCacheDir();
    try
    {
      if ((localFile1.exists()) && (localFile1.isDirectory()) && (!localFile1.equals(localFile2)))
      {
        CacheUpdateProgressDialog.showProgress(0, 100);
        Cache.setCleanupEnabled(false);
        File localFile3 = new File(localFile1, "splash");
        File localFile4 = new File(localFile2, "splash");
        copyDir(localFile3, localFile4);
        File localFile5 = new File(localFile1, "muffins");
        if ((localFile5.exists()) && (localFile5.isDirectory()))
        {
          localObject1 = findFiles(localFile5, "PM");
          for (int i = 0; i < localObject1.length; i++)
            try
            {
              String str2 = localObject1[i].getName().substring(2);
              localObject3 = new File(localObject1[i].getParentFile(), "PU" + str2);
              if (((File)localObject3).exists())
              {
                localObject4 = getMuffinAttributes((File)localObject3);
                URL localURL = deriveURL(localFile5, localObject1[i], null);
                if (localURL != null)
                  Cache.insertMuffin(localURL, localObject1[i], (int)localObject4[0], localObject4[1]);
              }
            }
            catch (Exception localException)
            {
              Trace.ignored(localException);
            }
        }
        Object localObject1 = new File(localFile1, "removed.apps");
        if (((File)localObject1).exists())
        {
          localObject2 = new File(localFile2, "removed.apps");
          try
          {
            Cache.copyFile((File)localObject1, (File)localObject2);
          }
          catch (IOException localIOException)
          {
            Trace.ignored(localIOException);
          }
        }
        Object localObject2 = findFiles(localFile1, "AM");
        File[] arrayOfFile = findFiles(localFile1, "XM");
        Object localObject3 = findFiles(localFile1, "RM");
        Object localObject4 = findFiles(localFile1, "RN");
        int j = localObject2.length + arrayOfFile.length + localObject3.length + localObject4.length;
        int k = 0;
        for (int m = 0; m < localObject2.length; m++)
        {
          updateJnlpFile(localObject2[m], localFile1, localFile2, false);
          k++;
          CacheUpdateProgressDialog.showProgress(k, j);
        }
        for (m = 0; m < arrayOfFile.length; m++)
        {
          updateJnlpFile(arrayOfFile[m], localFile1, localFile2, true);
          k++;
          CacheUpdateProgressDialog.showProgress(k, j);
        }
        for (m = 0; m < localObject3.length; m++)
        {
          String str3 = localObject3[m].getParent();
          String str4 = localObject3[m].getName();
          String str5 = str4.replaceFirst("RM", "RN");
          boolean bool = new File(str3, str5).exists();
          updateJarFile(localObject3[m], localFile1, localFile2, bool);
          k++;
          CacheUpdateProgressDialog.showProgress(k, j);
        }
      }
    }
    catch (CacheUpdateProgressDialog.CanceledException localCanceledException)
    {
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
    finally
    {
      CacheUpdateProgressDialog.dismiss();
      Cache.setCleanupEnabled(true);
    }
    Config.setStringProperty("deployment.javaws.cachedir", null);
    return true;
  }

  private static void updateJnlpFile(File paramFile1, File paramFile2, File paramFile3, boolean paramBoolean)
  {
    String[] arrayOfString = new String[1];
    URL localURL = deriveURL(paramFile2, paramFile1, arrayOfString);
    if (localURL != null)
      try
      {
        long l = getTimeStamp(paramFile1);
        int i = 1;
        Cache.insertFile(paramFile1, i, localURL, arrayOfString[0], l, 0L);
        String str = paramFile1.getName().substring(2);
        File localFile = new File(paramFile1.getParentFile(), "AL" + str);
        if (localFile.exists())
        {
          Properties localProperties = new Properties();
          BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(localFile));
          localProperties.load(localBufferedInputStream);
          localBufferedInputStream.close();
          updateLapFile(paramFile1, localProperties, localURL, arrayOfString[0], paramBoolean);
        }
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
  }

  private static void updateJarFile(File paramFile1, File paramFile2, File paramFile3, boolean paramBoolean)
  {
    String[] arrayOfString = new String[1];
    URL localURL = deriveURL(paramFile2, paramFile1, arrayOfString);
    if (localURL != null)
      try
      {
        long l = getTimeStamp(paramFile1);
        int i = 256;
        if (paramBoolean)
          i |= 16;
        Cache.insertFile(paramFile1, i, localURL, arrayOfString[0], l, 0L);
      }
      catch (Exception localException)
      {
        Trace.ignored(localException);
      }
  }

  private static void updateLapFile(File paramFile, Properties paramProperties, URL paramURL, String paramString, boolean paramBoolean)
  {
    LocalApplicationProperties localLocalApplicationProperties = Cache.getLocalApplicationProperties(paramURL, paramString, !paramBoolean);
    String str1 = paramProperties.getProperty("_default.lastAccessed");
    Date localDate = new Date();
    if (str1 != null)
      try
      {
        localDate = _df.parse(str1);
      }
      catch (Exception localException1)
      {
      }
    localLocalApplicationProperties.setLastAccessed(localDate);
    String str2 = paramProperties.getProperty("_default.launchCount");
    if ((str2 != null) && (str2 != "0"))
      localLocalApplicationProperties.incrementLaunchCount();
    localLocalApplicationProperties.setAskedForInstall(true);
    String str3 = paramProperties.getProperty("_default.locallyInstalled");
    String str4 = paramProperties.getProperty("_default.title");
    if (str4 != null)
      Platform.get().addRemoveProgramsRemove(str4, false);
    String str5 = paramProperties.getProperty("_default.mime.types.");
    String str6 = paramProperties.getProperty("_default.extensions.");
    try
    {
      if (paramBoolean)
      {
        if (str3 == null);
      }
      else
      {
        LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(paramFile, URLUtil.getBase(paramURL), null, paramURL);
        if ((str3 != null) && (str3.equalsIgnoreCase("true")))
        {
          boolean bool1 = false;
          boolean bool2 = false;
          String str7 = paramProperties.getProperty("windows.installedDesktopShortcut");
          if (str7 != null)
            bool2 = _lih.removeShortcuts(str7);
          str7 = paramProperties.getProperty("windows.installedStartMenuShortcut");
          if (str7 != null)
            bool1 = _lih.removeShortcuts(str7);
          str7 = paramProperties.getProperty("windows.uninstalledStartMenuShortcut");
          if (str7 != null)
            _lih.removeShortcuts(str7);
          str7 = paramProperties.getProperty("windows.RContent.shortcuts");
          if (str7 != null)
            _lih.removeShortcuts(str7);
          str7 = paramProperties.getProperty("unix.installedDesktopShortcut");
          if (str7 != null)
            bool2 = _lih.removeShortcuts(str7);
          str7 = paramProperties.getProperty("unix.installedDirectoryFile");
          if (str7 != null)
            _lih.removeShortcuts(str7);
          str7 = paramProperties.getProperty("unix.gnome.installedStartMenuShortcut");
          if (str7 != null)
            bool1 = _lih.removeShortcuts(str7);
          str7 = paramProperties.getProperty("unix.gnome.installedUninstallShortcut");
          if (str7 != null)
            _lih.removeShortcuts(str7);
          str7 = paramProperties.getProperty("unix.gnome.installedRCShortcut");
          if (str7 != null)
            _lih.removeShortcuts(str7);
          if ((bool2) || (bool1))
            _lih.reinstallShortcuts(localLaunchDesc, localLocalApplicationProperties, bool2, bool1);
        }
        if ((str5 != null) || (str6 != null))
        {
          _lih.removeAssociations(str5, str6);
          _lih.reinstallAssociations(localLaunchDesc, localLocalApplicationProperties);
        }
        _lih.removeFromInstallPanel(localLaunchDesc, localLocalApplicationProperties, false);
        _lih.registerWithInstallPanel(localLaunchDesc, localLocalApplicationProperties);
      }
    }
    catch (Exception localIOException2)
    {
      Trace.ignored(localException2);
    }
    finally
    {
      try
      {
        localLocalApplicationProperties.store();
      }
      catch (IOException localIOException3)
      {
        Trace.ignoredException(localIOException3);
      }
    }
  }

  private static long getTimeStamp(File paramFile)
  {
    try
    {
      String str1 = paramFile.getName();
      if (str1.charAt(1) == 'M')
      {
        str1 = str1.replaceFirst("M", "T");
        File localFile = new File(paramFile.getParentFile(), str1);
        BufferedReader localBufferedReader = null;
        try
        {
          FileInputStream localFileInputStream = new FileInputStream(localFile);
          localBufferedReader = new BufferedReader(new InputStreamReader(localFileInputStream));
          String str2 = localBufferedReader.readLine();
          try
          {
            long l2 = Long.parseLong(str2);
            try
            {
              if (localBufferedReader != null)
                localBufferedReader.close();
            }
            catch (IOException localIOException3)
            {
              Trace.ignoredException(localIOException3);
            }
            return l2;
          }
          catch (NumberFormatException localNumberFormatException)
          {
            long l3 = 0L;
            try
            {
              if (localBufferedReader != null)
                localBufferedReader.close();
            }
            catch (IOException localIOException4)
            {
              Trace.ignoredException(localIOException4);
            }
            return l3;
          }
        }
        catch (IOException localIOException1)
        {
          long l1 = 0L;
          return l1;
        }
        finally
        {
          try
          {
            if (localBufferedReader != null)
              localBufferedReader.close();
          }
          catch (IOException localIOException5)
          {
            Trace.ignoredException(localIOException5);
          }
        }
      }
    }
    catch (Exception localException)
    {
    }
    return 0L;
  }

  private static URL deriveURL(File paramFile1, File paramFile2, String[] paramArrayOfString)
  {
    String str1 = paramFile2.toString().substring(paramFile1.toString().length() + 1);
    StringTokenizer localStringTokenizer = new StringTokenizer(str1, File.separator);
    try
    {
      String str2 = localStringTokenizer.nextToken();
      if ((str2.equals("http")) || (str2.equals("https")))
      {
        String str3 = "/";
        String str4 = localStringTokenizer.nextToken().substring(1);
        int i = new Integer(localStringTokenizer.nextToken().substring(1)).intValue();
        String str5 = localStringTokenizer.nextToken();
        if (str5.startsWith("V"))
          paramArrayOfString[0] = str5.substring(1);
        for (str5 = localStringTokenizer.nextToken(); str5.startsWith("DM"); str5 = localStringTokenizer.nextToken())
          str3 = str3 + str5.substring(2) + "/";
        str3 = str3 + str5.substring(2);
        if (i == 80)
          i = -1;
        return new URL(str2, str4, i, str3);
      }
    }
    catch (Exception localException)
    {
      Trace.ignored(localException);
    }
    return null;
  }

  private static void copyDir(File paramFile1, File paramFile2)
  {
    if ((paramFile1.exists()) && (paramFile1.isDirectory()))
    {
      paramFile2.mkdirs();
      File[] arrayOfFile = paramFile1.listFiles();
      for (int i = 0; i < arrayOfFile.length; i++)
      {
        File localFile = new File(paramFile2, arrayOfFile[i].getName());
        if (arrayOfFile[i].isDirectory())
        {
          copyDir(arrayOfFile[i], localFile);
        }
        else
        {
          if (localFile.exists())
            continue;
          try
          {
            Cache.copyFile(arrayOfFile[i], localFile);
          }
          catch (IOException localIOException)
          {
            Trace.ignored(localIOException);
          }
        }
      }
    }
  }

  private static File[] findFiles(File paramFile, String paramString)
  {
    ArrayList localArrayList = new ArrayList();
    String[] arrayOfString = paramFile.list(new FilenameFilter(paramString)
    {
      private final String val$prefix;

      public boolean accept(File paramFile, String paramString)
      {
        try
        {
          if (new File(paramFile, paramString).isDirectory())
            return !paramString.startsWith("RN");
        }
        catch (Exception localException)
        {
          return false;
        }
        return paramString.startsWith(this.val$prefix);
      }
    });
    for (int i = 0; i < arrayOfString.length; i++)
      if (arrayOfString[i].startsWith(paramString))
      {
        localArrayList.add(new File(paramFile, arrayOfString[i]));
      }
      else
      {
        File localFile = new File(paramFile, arrayOfString[i]);
        File[] arrayOfFile = findFiles(localFile, paramString);
        for (int j = 0; j < arrayOfFile.length; j++)
          localArrayList.add(arrayOfFile[j]);
      }
    return (File[])(File[])localArrayList.toArray(new File[0]);
  }

  private static long[] getMuffinAttributes(File paramFile)
    throws IOException
  {
    BufferedReader localBufferedReader = null;
    long l1 = -1L;
    long l2 = -1L;
    try
    {
      FileInputStream localFileInputStream = new FileInputStream(paramFile);
      localBufferedReader = new BufferedReader(new InputStreamReader(localFileInputStream));
      String str = localBufferedReader.readLine();
      try
      {
        l1 = Integer.parseInt(str);
      }
      catch (NumberFormatException localNumberFormatException1)
      {
        throw new IOException(localNumberFormatException1.getMessage());
      }
      str = localBufferedReader.readLine();
      try
      {
        l2 = Long.parseLong(str);
      }
      catch (NumberFormatException localNumberFormatException2)
      {
        throw new IOException(localNumberFormatException2.getMessage());
      }
    }
    finally
    {
      if (localBufferedReader != null)
        localBufferedReader.close();
    }
    return new long[] { l1, l2 };
  }

  public static boolean systemUpdateCheck()
  {
    if (!Environment.isSystemCacheMode())
      return false;
    long l = Cache.getLastAccessed(true);
    if (l > 0L)
      return false;
    String str1 = Config.getSystemCacheDirectory();
    File localFile1 = new File(str1, "javaws");
    File localFile2 = new File(str1, Cache.getCacheVersionString());
    if ((!localFile1.exists()) || (!localFile1.isDirectory()))
      return false;
    try
    {
      CacheUpdateProgressDialog.setSystemCache(true);
      CacheUpdateProgressDialog.showProgress(0, 100);
      Cache.setCleanupEnabled(false);
      File[] arrayOfFile1 = findFiles(localFile1, "AM");
      File[] arrayOfFile2 = findFiles(localFile1, "XM");
      File[] arrayOfFile3 = findFiles(localFile1, "RM");
      File[] arrayOfFile4 = findFiles(localFile1, "RN");
      int i = arrayOfFile1.length + arrayOfFile2.length + arrayOfFile3.length + arrayOfFile4.length;
      int j = 0;
      for (int k = 0; k < arrayOfFile1.length; k++)
      {
        updateJnlpFile(arrayOfFile1[k], localFile1, localFile2, false);
        j++;
        CacheUpdateProgressDialog.showProgress(j, i);
      }
      for (k = 0; k < arrayOfFile2.length; k++)
      {
        updateJnlpFile(arrayOfFile2[k], localFile1, localFile2, true);
        j++;
        CacheUpdateProgressDialog.showProgress(j, i);
      }
      for (k = 0; k < arrayOfFile3.length; k++)
      {
        String str2 = arrayOfFile3[k].getParent();
        String str3 = arrayOfFile3[k].getName();
        String str4 = str3.replaceFirst("RM", "RN");
        boolean bool = new File(str2, str4).exists();
        updateJarFile(arrayOfFile3[k], localFile1, localFile2, bool);
        j++;
        CacheUpdateProgressDialog.showProgress(j, i);
      }
    }
    catch (CacheUpdateProgressDialog.CanceledException localCanceledException)
    {
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
    finally
    {
      CacheUpdateProgressDialog.dismiss();
      Cache.setCleanupEnabled(true);
    }
    return true;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.CacheUpdateHelper
 * JD-Core Version:    0.6.0
 */