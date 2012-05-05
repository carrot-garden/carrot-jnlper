package com.sun.deploy.association.utility;

import com.sun.deploy.config.Platform;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class DesktopEntryFile
{
  private String uri = null;
  private ArrayList desktopEntries = null;

  public DesktopEntryFile(String paramString)
  {
    if (null == paramString)
      throw new NullPointerException("uri is null!");
    if (paramString.indexOf("://") == 0)
      throw new IllegalArgumentException("Invalid URI[" + paramString + "]!");
    this.uri = paramString;
    this.desktopEntries = new ArrayList();
    Trace.println("new DesktopEntryFile uri = [" + paramString + "]", TraceLevel.BASIC);
  }

  public DesktopEntryFile(File paramFile)
  {
    this(paramFile.toURI().toString());
  }

  public String getParent()
  {
    String str = this.uri.toString();
    int i = str.lastIndexOf(File.separator);
    int j = str.indexOf("://") + 3;
    if (i < j)
    {
      if ((j > 0) && (str.length() > j))
        return str.substring(0, j);
      return "/";
    }
    return str.substring(0, i);
  }

  private DesktopEntryFile getParentDesktopEntryFile()
  {
    String str = getParent();
    return str == null ? null : new DesktopEntryFile(str);
  }

  public Collection readEntries()
    throws IOException
  {
    readEntryInternal();
    return this.desktopEntries;
  }

  public void writeEntry(DesktopEntry paramDesktopEntry)
    throws IOException
  {
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(paramDesktopEntry);
    writeEntries(localArrayList);
  }

  public void writeEntries(Collection paramCollection)
    throws IOException
  {
    this.desktopEntries = new ArrayList(paramCollection);
    writeEntryInternal();
  }

  public boolean exists()
  {
    return gnome_vfs_file_exists(this.uri.toString());
  }

  public boolean delete()
  {
    return gnome_vfs_delete_file(this.uri.toString());
  }

  public boolean deleteToNonEmptyParent()
  {
    for (DesktopEntryFile localDesktopEntryFile = this; (null != localDesktopEntryFile) && (localDesktopEntryFile.exists()) && (!localDesktopEntryFile.getURI().matches(".*://")) && (localDesktopEntryFile.delete()); localDesktopEntryFile = localDesktopEntryFile.getParentDesktopEntryFile())
      Trace.println("file deleted " + localDesktopEntryFile.toString(), TraceLevel.BASIC);
    return true;
  }

  public boolean mkdir()
    throws IOException
  {
    return gnome_vfs_mkdir(this.uri.toString());
  }

  private static native String gnome_vfs_read_file(String paramString);

  private static native boolean gnome_vfs_mkdir(String paramString);

  private static native boolean gnome_vfs_file_exists(String paramString);

  private static native boolean gnome_vfs_write_file(String paramString1, String paramString2);

  private static native boolean gnome_vfs_delete_file(String paramString);

  private static native void ensure_load_gnome_vfs_lib();

  private void readEntryInternal()
    throws IOException
  {
    BufferedReader localBufferedReader = new BufferedReader(new StringReader(gnome_vfs_read_file(this.uri.toString())));
    String str = null;
    DesktopEntry localDesktopEntry = null;
    int i = 0;
    while (true)
    {
      str = localBufferedReader.readLine();
      if (null == str)
        break;
      if (str.matches("^\\s*\\[.*\\]\\s*"))
      {
        if (null != localDesktopEntry)
          continue;
        localDesktopEntry = new DesktopEntry(trimBracket(str));
        this.desktopEntries.add(localDesktopEntry);
        continue;
      }
      if (null == localDesktopEntry)
        continue;
      localDesktopEntry.load(str);
    }
  }

  private static String trimBracket(String paramString)
  {
    if (null == paramString)
      return "";
    int i = paramString.length();
    if (i > 1)
      return paramString.trim().substring(1, i - 1);
    return "";
  }

  private void writeEntryInternal()
    throws IOException
  {
    DesktopEntryFile localDesktopEntryFile = getParentDesktopEntryFile();
    ArrayList localArrayList = new ArrayList();
    while ((null != localDesktopEntryFile) && (!localDesktopEntryFile.exists()))
    {
      localArrayList.add(localDesktopEntryFile);
      localDesktopEntryFile = localDesktopEntryFile.getParentDesktopEntryFile();
    }
    for (int i = localArrayList.size() - 1; i >= 0; i--)
    {
      localDesktopEntryFile = (DesktopEntryFile)localArrayList.get(i);
      Trace.println("writeEntryInternal mkdir " + localDesktopEntryFile.toString(), TraceLevel.BASIC);
      localDesktopEntryFile.mkdir();
    }
    StringBuffer localStringBuffer = new StringBuffer();
    Iterator localIterator = this.desktopEntries.iterator();
    while (localIterator.hasNext())
    {
      localStringBuffer.append(localIterator.next());
      localStringBuffer.append("\n");
    }
    Trace.println("gnome_vfs_write_file [" + this.uri.toString() + "]", TraceLevel.BASIC);
    boolean bool = gnome_vfs_write_file(this.uri.toString(), localStringBuffer.toString());
  }

  public String getURI()
  {
    return this.uri;
  }

  public String toString()
  {
    return "DesktopEntryFile[" + this.uri + "]";
  }

  static
  {
    Platform.get().loadDeployNativeLib();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.DesktopEntryFile
 * JD-Core Version:    0.6.0
 */