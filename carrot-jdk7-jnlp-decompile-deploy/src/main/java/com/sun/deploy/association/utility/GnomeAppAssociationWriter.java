package com.sun.deploy.association.utility;

import com.sun.deploy.association.Action;
import com.sun.deploy.association.Association;
import com.sun.deploy.association.RegisterFailedException;
import com.sun.deploy.trace.Trace;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class GnomeAppAssociationWriter
  implements AppAssociationWriter
{
  static String GNOMEDIR_VALUE = GnomeAssociationUtil.getEnv("GNOMEDIR");
  static String GNOME_LINUX_SYSTEM_SHARE_DIR = GNOMEDIR_VALUE + "/share/";
  static String GNOME_SOLARIS_SYSTEM_SHARE_DIR = "/usr/share/gnome/";
  static String GNOME_LINUX_SYSTEM_MIME_INFO_DIR = GNOME_LINUX_SYSTEM_SHARE_DIR + "mime-info/";
  static String GNOME_SOLARIS_SYSTEM_MIME_INFO_DIR = GNOME_SOLARIS_SYSTEM_SHARE_DIR + "mime-info/";
  static String GNOME_LINUX_SYSTEM_APPLICATION_REGISTRY_DIR = GNOME_LINUX_SYSTEM_SHARE_DIR + "application-registry/";
  static String GNOME_SOLARIS_SYSTEM_APPLICATION_REGISTRY_DIR = GNOME_SOLARIS_SYSTEM_SHARE_DIR + "application-registry/";
  static String OSNAME = System.getProperty("os.name").toLowerCase();
  static String GNOME_SYSTEM_MIME_INFO_DIR = OSNAME.equals("linux") ? GNOME_LINUX_SYSTEM_MIME_INFO_DIR : GNOME_SOLARIS_SYSTEM_MIME_INFO_DIR;
  static String GNOME_SYSTEM_APPLICATION_REGISTRY_DIR = OSNAME.equals("linux") ? GNOME_LINUX_SYSTEM_APPLICATION_REGISTRY_DIR : GNOME_SOLARIS_SYSTEM_APPLICATION_REGISTRY_DIR;
  static String GNOME_USER_MIME_INFO_DIR = System.getProperty("user.home") + "/.gnome/mime-info/";
  static String GNOME_USER_APPLICATION_INFO_DIR = System.getProperty("user.home") + "/.gnome/application-info/";
  static String MIME_SUFFIX = ".mime";
  static String KEYS_SUFFIX = ".keys";
  static String APPLICATIONS_SUFFIX = ".applications";
  private String defaultAppID = null;
  private String defaultAppCommand = null;

  private String fileExtListToString(List paramList)
  {
    String str1 = "";
    Iterator localIterator = paramList.iterator();
    if (localIterator != null)
      while (localIterator.hasNext())
      {
        String str2 = (String)localIterator.next();
        if (str2 == null)
          continue;
        str2 = AppUtility.removeDotFromFileExtension(str2);
        if (str1.length() == 0)
        {
          str1 = str1.concat(str2);
          continue;
        }
        str1 = str1.concat(' ' + str2);
      }
    str1 = null;
    return str1;
  }

  private String getSystemDotMimeFilePath(Association paramAssociation)
  {
    return GNOME_SYSTEM_MIME_INFO_DIR + paramAssociation.getName() + MIME_SUFFIX;
  }

  private String getSystemDotKeysFilePath(Association paramAssociation)
  {
    return GNOME_SYSTEM_MIME_INFO_DIR + paramAssociation.getName() + KEYS_SUFFIX;
  }

  private String getSystemDotApplicationsFilePath(Association paramAssociation)
  {
    return GNOME_SYSTEM_APPLICATION_REGISTRY_DIR + paramAssociation.getName() + APPLICATIONS_SUFFIX;
  }

  private String getUserDotMimeFilePath(Association paramAssociation)
  {
    return GNOME_USER_MIME_INFO_DIR + paramAssociation.getName() + MIME_SUFFIX;
  }

  private String getUserDotKeysFilePath(Association paramAssociation)
  {
    return GNOME_USER_MIME_INFO_DIR + paramAssociation.getName() + KEYS_SUFFIX;
  }

  private String getUserDotApplicationsFilePath(Association paramAssociation)
  {
    return GNOME_USER_APPLICATION_INFO_DIR + paramAssociation.getName() + APPLICATIONS_SUFFIX;
  }

  private void checkSystemMIMEDatabase()
    throws IOException
  {
    File localFile = null;
    localFile = new File(GNOME_SYSTEM_MIME_INFO_DIR);
    if (!localFile.exists())
      throw new IOException("The system MIME info directory doesn't exist: " + GNOME_SYSTEM_MIME_INFO_DIR + ". Make sure Gnome 2.0+ is installed and env GNOMEDIR is set properly.");
    localFile = new File(GNOME_SYSTEM_APPLICATION_REGISTRY_DIR);
    if (!localFile.exists())
      throw new IOException("The system MIME info directory doesn't exist: " + GNOME_SYSTEM_APPLICATION_REGISTRY_DIR + ". Make sure Gnome 2.0+ is installed and env GNOMEDIR is set properly.");
    localFile = new File(GNOME_SYSTEM_MIME_INFO_DIR);
    if (!localFile.canWrite())
      throw new IOException("No write permission to the system MIME info directory: " + GNOME_SYSTEM_MIME_INFO_DIR);
    localFile = new File(GNOME_SYSTEM_APPLICATION_REGISTRY_DIR);
    if (!localFile.canWrite())
      throw new IOException("No write permission to the system MIME info directory: " + GNOME_SYSTEM_APPLICATION_REGISTRY_DIR);
  }

  private void checkUserMIMEDatabase()
    throws IOException
  {
    File localFile = null;
    localFile = new File(GNOME_USER_MIME_INFO_DIR);
    boolean bool;
    if (!localFile.exists())
    {
      bool = localFile.mkdirs();
      if (!bool)
        throw new IOException("The user MIME info directory doesn't exist, and fails to be created: " + GNOME_USER_MIME_INFO_DIR);
    }
    localFile = new File(GNOME_USER_APPLICATION_INFO_DIR);
    if (!localFile.exists())
    {
      bool = localFile.mkdirs();
      if (!bool)
        throw new IOException("The user MIME info directory doesn't exist, and fails to be created: " + GNOME_USER_APPLICATION_INFO_DIR);
    }
    localFile = new File(GNOME_USER_MIME_INFO_DIR);
    if (!localFile.canWrite())
      throw new IOException("No write permission to the user MIME info directory: " + GNOME_USER_MIME_INFO_DIR);
    localFile = new File(GNOME_USER_APPLICATION_INFO_DIR);
    if (!localFile.canWrite())
      throw new IOException("No write permission to the user MIME info directory: " + GNOME_USER_MIME_INFO_DIR);
  }

  private void createFile(String paramString)
    throws IOException
  {
    boolean bool = false;
    File localFile = new File(paramString);
    if (!localFile.exists())
    {
      bool = localFile.createNewFile();
      if (!bool)
        throw new IOException("Create MIME file: " + paramString + " failed.");
    }
  }

  private void parseOpenAction(Association paramAssociation)
  {
    List localList = paramAssociation.getActionList();
    if (localList == null)
      return;
    String str = null;
    Iterator localIterator = localList.iterator();
    while ((localIterator.hasNext()) && (this.defaultAppCommand == null))
    {
      Action localAction = (Action)localIterator.next();
      str = localAction.getVerb();
      if (str.equalsIgnoreCase("open"))
        this.defaultAppCommand = localAction.getCommand().trim();
    }
    if (this.defaultAppCommand != null)
    {
      int i = this.defaultAppCommand.lastIndexOf(File.separator);
      if ((i == -1) || (i == this.defaultAppCommand.length() - 1))
        this.defaultAppID = this.defaultAppCommand;
      else
        this.defaultAppID = this.defaultAppCommand.substring(i + 1, this.defaultAppCommand.length());
    }
  }

  private void writeDotMimeFile(Association paramAssociation, String paramString)
    throws IOException
  {
    createFile(paramString);
    String str1 = paramAssociation.getMimeType();
    List localList = paramAssociation.getFileExtList();
    BufferedWriter localBufferedWriter = null;
    try
    {
      localBufferedWriter = new BufferedWriter(new FileWriter(paramString, true));
      localBufferedWriter.write(str1 + "\n");
      String str2 = null;
      if (localList == null)
        str2 = "";
      else
        str2 = fileExtListToString(localList);
      localBufferedWriter.write("\text: " + str2 + "\n");
      localBufferedWriter.write("\n");
    }
    catch (IOException localIOException1)
    {
      throw new IOException("Write mime info to " + paramString + " failed.");
    }
    finally
    {
      if (localBufferedWriter != null)
        try
        {
          localBufferedWriter.close();
        }
        catch (IOException localIOException2)
        {
        }
    }
  }

  private void writeDotKeysFile(Association paramAssociation, String paramString)
    throws IOException
  {
    createFile(paramString);
    String str1 = paramAssociation.getMimeType();
    String str2 = paramAssociation.getDescription();
    String str3 = paramAssociation.getIconFileName();
    BufferedWriter localBufferedWriter = null;
    try
    {
      localBufferedWriter = new BufferedWriter(new FileWriter(paramString, true));
      localBufferedWriter.write(str1 + "\n");
      if (str2 != null)
        localBufferedWriter.write("\tdescription=" + str2 + "\n");
      if (str3 != null)
        localBufferedWriter.write("\ticon_filename=" + str3 + "\n");
      parseOpenAction(paramAssociation);
      if (this.defaultAppID != null)
      {
        localBufferedWriter.write("\tdefault_action_type=application\n");
        localBufferedWriter.write("\tdefault_application_id=" + this.defaultAppID + "\n");
        localBufferedWriter.write("\tshort_list_application_user_additions=" + this.defaultAppID + "\n");
      }
      localBufferedWriter.write("\n");
    }
    catch (IOException localIOException1)
    {
      throw new IOException("Write mime info to " + paramString + " failed.");
    }
    finally
    {
      if (localBufferedWriter != null)
        try
        {
          localBufferedWriter.close();
        }
        catch (IOException localIOException2)
        {
        }
    }
  }

  private void writeDotApplicationsFile(Association paramAssociation, String paramString)
    throws IOException
  {
    createFile(paramString);
    BufferedWriter localBufferedWriter = null;
    try
    {
      parseOpenAction(paramAssociation);
      if ((this.defaultAppID != null) && (this.defaultAppCommand != null))
      {
        localBufferedWriter = new BufferedWriter(new FileWriter(paramString, true));
        localBufferedWriter.write(this.defaultAppID + "\n");
        localBufferedWriter.write("\tcommand=" + this.defaultAppCommand + "\n");
        localBufferedWriter.write("\tname=" + this.defaultAppID + "\n");
        localBufferedWriter.write("\tcan_open_multiple_files=false\n");
        localBufferedWriter.write("\trequires_terminal=false\n");
        String str = paramAssociation.getMimeType();
        localBufferedWriter.write("\tmime_types=" + str + "\n");
        localBufferedWriter.write("\n");
      }
    }
    catch (IOException localIOException1)
    {
      throw new IOException("Write mime info to " + paramString + " failed.");
    }
    finally
    {
      if (localBufferedWriter != null)
        try
        {
          localBufferedWriter.close();
        }
        catch (IOException localIOException2)
        {
        }
    }
  }

  private boolean dotMimeFileContains(File paramFile, String paramString1, String paramString2)
  {
    int i = 0;
    int j = paramString2 == null ? 1 : 0;
    try
    {
      BufferedReader localBufferedReader = new BufferedReader(new FileReader(paramFile));
      String str;
      while ((str = localBufferedReader.readLine()) != null)
        if (paramString1.equals(str))
        {
          i = 1;
          if (j == 0)
            continue;
        }
        else
        {
          if ((j != 0) || (!str.contains("ext:")) || (!str.contains(paramString2)))
            continue;
          j = 1;
          if (i == 0)
            continue;
        }
      localBufferedReader.close();
      return (i != 0) && (j != 0);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
    return false;
  }

  public void checkAssociationValidForRegistration(Association paramAssociation)
    throws IllegalArgumentException
  {
    if ((paramAssociation.getName() == null) || (paramAssociation.getMimeType() == null))
      throw new IllegalArgumentException("The given association is invalid. It should specify both the name and mimeType fields to perform this operation.");
  }

  public void checkAssociationValidForUnregistration(Association paramAssociation)
    throws IllegalArgumentException
  {
    if (paramAssociation.getName() == null)
      throw new IllegalArgumentException("The given association is invalid. It should specify the name field to perform this operation.");
  }

  public boolean isAssociationExist(Association paramAssociation, int paramInt)
  {
    File localFile = null;
    if (paramInt == 2)
      localFile = new File(getSystemDotMimeFilePath(paramAssociation));
    else
      localFile = new File(getUserDotMimeFilePath(paramAssociation));
    List localList = paramAssociation.getFileExtList();
    String str = null;
    if (localList != null)
      str = fileExtListToString(localList);
    if (localFile.exists())
    {
      if (paramAssociation.getMimeType() == null)
        return true;
      return dotMimeFileContains(localFile, paramAssociation.getMimeType(), str);
    }
    return false;
  }

  public void registerAssociation(Association paramAssociation, int paramInt)
    throws RegisterFailedException
  {
    String str1 = null;
    String str2 = null;
    String str3 = null;
    try
    {
      if (paramInt == 2)
      {
        checkSystemMIMEDatabase();
        str1 = getSystemDotMimeFilePath(paramAssociation);
        str2 = getSystemDotKeysFilePath(paramAssociation);
        str3 = getSystemDotApplicationsFilePath(paramAssociation);
      }
      else
      {
        checkUserMIMEDatabase();
        str1 = getUserDotMimeFilePath(paramAssociation);
        str2 = getUserDotKeysFilePath(paramAssociation);
        str3 = getUserDotApplicationsFilePath(paramAssociation);
      }
      writeDotMimeFile(paramAssociation, str1);
      writeDotKeysFile(paramAssociation, str2);
      writeDotApplicationsFile(paramAssociation, str3);
    }
    catch (IOException localIOException)
    {
      if (str1 != null)
        new File(str1).delete();
      if (str2 != null)
        new File(str2).delete();
      if (str3 != null)
        new File(str3).delete();
      throw new RegisterFailedException(localIOException.getMessage());
    }
  }

  public void unregisterAssociation(Association paramAssociation, int paramInt)
    throws RegisterFailedException
  {
    String str1 = null;
    String str2 = null;
    String str3 = null;
    try
    {
      if (paramInt == 2)
      {
        checkSystemMIMEDatabase();
        str1 = getSystemDotMimeFilePath(paramAssociation);
        str2 = getSystemDotKeysFilePath(paramAssociation);
        str3 = getSystemDotApplicationsFilePath(paramAssociation);
      }
      else
      {
        checkUserMIMEDatabase();
        str1 = getUserDotMimeFilePath(paramAssociation);
        str2 = getUserDotKeysFilePath(paramAssociation);
        str3 = getUserDotApplicationsFilePath(paramAssociation);
      }
      new File(str1).delete();
      new File(str2).delete();
      new File(str3).delete();
    }
    catch (IOException localIOException)
    {
      throw new RegisterFailedException(localIOException.getMessage());
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.GnomeAppAssociationWriter
 * JD-Core Version:    0.6.0
 */