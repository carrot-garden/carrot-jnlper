package com.sun.deploy.association.utility;

import com.sun.deploy.trace.Trace;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class DesktopEntry
{
  public static final String DEFAULT_GROUP = "Desktop Entry";
  private String group = null;
  private Properties entries = null;
  private static String[] DESKTOP_ENTRY_KEYS = { "Type", "Version", "Encoding", "Name", "GenericName", "NoDisplay", "Comment", "Icon", "Hidden", "FilePattern", "TryExec", "Exec", "Path", "Terminal", "SwallowTitle", "SwallowExec", "Actions", "MimeType", "SortOrder", "Dev", "FSType", "MountPoint", "ReadOnly", "UnmountIcon", "URL", "Categories ", "OnlyShowIn", "NotShowIn", "StartupNotify", "StartupWMClass" };

  public DesktopEntry()
  {
    this("Desktop Entry");
  }

  public DesktopEntry(String paramString)
  {
    this.group = paramString;
    this.entries = new Properties();
    set("Version", "1.0");
  }

  public String getGroup()
  {
    return this.group;
  }

  public void setGroup(String paramString)
  {
    this.group = paramString;
  }

  public String getType()
  {
    return get("Type");
  }

  public void setType(String paramString)
  {
    set("Type", paramString);
  }

  public void setEncoding(String paramString)
  {
    set("Encoding", paramString);
  }

  public String getEncoding()
  {
    return get("Encoding");
  }

  public void setName(String paramString)
  {
    set("Name", paramString);
  }

  public String getName()
  {
    return get("Name");
  }

  public String getGenericName()
  {
    return get("GenericName");
  }

  public void setGenericName(String paramString)
  {
    set("GenericName", paramString);
  }

  public String getExec()
  {
    return get("Exec");
  }

  public void setExec(String paramString)
  {
    set("Exec", paramString);
  }

  public String getIcon()
  {
    return get("Icon");
  }

  public void setIcon(String paramString)
  {
    set("Icon", paramString);
  }

  public boolean getTerminal()
  {
    return Boolean.parseBoolean(get("Terminal"));
  }

  public void setTerminal(boolean paramBoolean)
  {
    set("Terminal", String.valueOf(paramBoolean));
  }

  public String getCategories()
  {
    return get("Categories");
  }

  public void setCategories(String paramString)
  {
    set("Categories", paramString);
  }

  public String getComment()
  {
    return get("Comment");
  }

  public void setComment(String paramString)
  {
    set("Comment", paramString);
  }

  public String getPath()
  {
    return get("Path");
  }

  public void setPath(String paramString)
  {
    set("Path", paramString);
  }

  public void set(String paramString1, String paramString2)
  {
    if (paramString2 == null)
      this.entries.remove(paramString1);
    else
      this.entries.setProperty(paramString1, paramString2);
  }

  public void set(String paramString1, String paramString2, String paramString3)
  {
    set(paramString1 + "[" + paramString2 + "]", paramString3);
  }

  public String get(String paramString1, String paramString2)
  {
    return get(paramString1 + "[" + paramString2 + "]");
  }

  public String get(String paramString)
  {
    return this.entries.getProperty(paramString);
  }

  public void load(String paramString)
  {
    try
    {
      this.entries.load(new ByteArrayInputStream(paramString.getBytes()));
    }
    catch (IOException localIOException)
    {
      Trace.ignoredException(localIOException);
    }
  }

  public static DesktopEntry create(String paramString)
  {
    return create("Desktop Entry", paramString);
  }

  public static DesktopEntry create(String paramString1, String paramString2)
  {
    if ((null == paramString1) || (paramString1.trim().equals("")))
      paramString1 = "Desktop Entry";
    DesktopEntry localDesktopEntry = new DesktopEntry(paramString1);
    localDesktopEntry.load(paramString2);
    return localDesktopEntry;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    if (null == this.group)
      localStringBuffer.append("[Desktop Entry]\n");
    else
      localStringBuffer.append("[" + this.group + "]\n");
    Iterator localIterator = this.entries.keySet().iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      localStringBuffer.append(str);
      localStringBuffer.append("=");
      localStringBuffer.append(this.entries.getProperty(str));
      localStringBuffer.append("\n");
    }
    return localStringBuffer.toString();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.DesktopEntry
 * JD-Core Version:    0.6.0
 */