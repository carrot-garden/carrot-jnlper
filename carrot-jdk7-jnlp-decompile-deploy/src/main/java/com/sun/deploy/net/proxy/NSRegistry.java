package com.sun.deploy.net.proxy;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;

final class NSRegistry
{
  private static final int magic = 1986282561;
  private RandomAccessFile in = null;
  private int rootOffset;
  private int majorVersion;
  private int minorVersion;

  private Record readRecord(long paramLong)
    throws IOException
  {
    this.in.seek(paramLong);
    if (readInt() != paramLong)
      throw new IOException("invalid offset for record [" + paramLong + "]");
    Record localRecord = new Record(null);
    localRecord.name = readInt();
    localRecord.namelen = readUnsignedShort();
    localRecord.type = readUnsignedShort();
    localRecord.left = readInt();
    localRecord.down = readInt();
    localRecord.value = readInt();
    localRecord.valuelen = readUnsignedInt();
    localRecord.valuebuf = readUnsignedInt();
    return localRecord;
  }

  private String readString(long paramLong1, long paramLong2)
    throws IOException
  {
    this.in.seek(paramLong1);
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < paramLong2; i++)
      localStringBuffer.append((char)this.in.read());
    return localStringBuffer.toString();
  }

  private short readShort()
    throws IOException
  {
    int i = this.in.read();
    int j = this.in.read();
    if ((i | j) < 0)
      throw new EOFException();
    return (short)((j << 8) + (i << 0));
  }

  private int readUnsignedShort()
    throws IOException
  {
    int i = this.in.read();
    int j = this.in.read();
    if ((i | j) < 0)
      throw new EOFException();
    return (j << 8) + (i << 0);
  }

  private int readInt()
    throws IOException
  {
    int i = this.in.read();
    int j = this.in.read();
    int k = this.in.read();
    int m = this.in.read();
    if ((i | j | k | m) < 0)
      throw new EOFException();
    return (m << 24) + (k << 16) + (j << 8) + (i << 0);
  }

  private long readUnsignedInt()
    throws IOException
  {
    int i = this.in.read();
    int j = this.in.read();
    int k = this.in.read();
    int m = this.in.read();
    if ((i | j | k | m) < 0)
      throw new EOFException();
    return (m << 24) + (k << 16) + (j << 8) + (i << 0);
  }

  NSRegistry open(File paramFile)
  {
    if (this.in != null)
      return this;
    try
    {
      this.in = new RandomAccessFile(paramFile, "r");
      if (readInt() != 1986282561)
        throw new IOException("not a valid Netscape Registry File");
      this.majorVersion = readUnsignedShort();
      this.minorVersion = readUnsignedShort();
      this.in.skipBytes(4);
      this.rootOffset = readInt();
      return this;
    }
    catch (IOException localIOException)
    {
    }
    return null;
  }

  void close()
  {
    if (this.in != null)
    {
      try
      {
        this.in.close();
      }
      catch (IOException localIOException)
      {
      }
      this.in = null;
    }
  }

  private String get(Record paramRecord, StringTokenizer paramStringTokenizer)
    throws IOException
  {
    String str = paramStringTokenizer.nextToken();
    Record localRecord;
    if (paramStringTokenizer.hasMoreTokens())
    {
      for (int i = paramRecord.down; i != 0; j = localRecord.left)
      {
        localRecord = readRecord(i);
        if (str.equals(readString(localRecord.name, localRecord.namelen - 1)))
          return get(localRecord, paramStringTokenizer);
      }
      return null;
    }
    int k;
    for (int j = paramRecord.value; j != 0; k = localRecord.left)
    {
      localRecord = readRecord(j);
      if (str.equals(readString(localRecord.name, localRecord.namelen - 1)))
        return readString(localRecord.value, localRecord.valuelen - 1L);
    }
    return null;
  }

  String get(String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "/");
    try
    {
      return get(readRecord(this.rootOffset), localStringTokenizer);
    }
    catch (IOException localIOException)
    {
    }
    return null;
  }

  private void dump(String paramString, int paramInt)
    throws IOException
  {
    Record localRecord1 = readRecord(paramInt);
    System.out.println(paramString + readString(localRecord1.name, localRecord1.namelen - 1));
    int j;
    for (int i = localRecord1.down; i != 0; j = readRecord(i).left)
      dump(paramString + "    ", i);
    Record localRecord2;
    int m;
    for (int k = localRecord1.value; k != 0; m = localRecord2.left)
    {
      localRecord2 = readRecord(k);
      String str1 = readString(localRecord2.name, localRecord2.namelen - 1);
      String str2 = readString(localRecord2.value, localRecord2.valuelen - 1L);
      System.out.println(paramString + "[" + str1 + " = " + str2 + "]");
    }
  }

  void dump()
  {
    try
    {
      dump("", this.rootOffset);
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  public String toString()
  {
    String str = getClass().getName() + "@" + Integer.toHexString(hashCode());
    if (this.in != null)
      str = str + "[version " + this.majorVersion + "." + this.minorVersion + "]";
    else
      str = str + "[closed]";
    return str;
  }

  private static String getNS45PrefsFilePath(String paramString)
  {
    String str1 = null;
    try
    {
      File localFile = new File(paramString, "nsreg.dat");
      NSRegistry localNSRegistry = new NSRegistry().open(localFile);
      if (localNSRegistry != null)
      {
        String str2 = localNSRegistry.get("Common/Netscape/ProfileManager/LastNetscapeUser");
        if (str2 != null)
          str1 = localNSRegistry.get("Users/" + str2 + "/ProfileLocation");
        localNSRegistry.close();
      }
      if (str1 != null)
        str1 = str1 + File.separator + "prefs.js";
    }
    catch (Exception localException)
    {
      Trace.printException(localException, ResourceManager.getMessage("net.proxy.nsprefs.error"), ResourceManager.getMessage("net.proxy.error.caption"));
    }
    return str1;
  }

  private static void showProxyErrorDialog()
  {
    String str1 = ResourceManager.getString("common.ok_btn");
    String str2 = ResourceManager.getString("common.detail.button");
    ToolkitStore.getUI();
    ToolkitStore.getUI().showMessageDialog(null, null, 0, ResourceManager.getMessage("net.proxy.error.caption"), null, ResourceManager.getMessage("net.proxy.nsprefs.error"), null, str1, str2, null);
  }

  private static final class Record
  {
    int name;
    int namelen;
    int type;
    int left;
    int down;
    int value;
    long valuelen;
    long valuebuf;

    private Record()
    {
    }

    public boolean isKey()
    {
      return (this.type & 0x10) != 0;
    }

    public boolean isEntry()
    {
      return !isKey();
    }

    Record(NSRegistry.1 param1)
    {
      this();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.NSRegistry
 * JD-Core Version:    0.6.0
 */