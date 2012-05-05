package com.sun.deploy.jardiff;

import com.sun.deploy.resources.ResourceManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class JarDiff
  implements JarDiffConstants
{
  private static final int DEFAULT_READ_SIZE = 2048;
  private static byte[] newBytes = new byte[2048];
  private static byte[] oldBytes = new byte[2048];
  private static boolean _debug;

  public static void createPatch(String paramString1, String paramString2, OutputStream paramOutputStream, boolean paramBoolean)
    throws IOException
  {
    JarFile2 localJarFile21 = new JarFile2(paramString1);
    JarFile2 localJarFile22 = new JarFile2(paramString2);
    try
    {
      HashMap localHashMap = new HashMap();
      HashSet localHashSet1 = new HashSet();
      HashSet localHashSet2 = new HashSet();
      HashSet localHashSet3 = new HashSet();
      HashSet localHashSet4 = new HashSet();
      Iterator localIterator = localJarFile22.getJarEntries();
      String str;
      if (localIterator != null)
        while (localIterator.hasNext())
        {
          localObject1 = (JarEntry)localIterator.next();
          localObject2 = ((JarEntry)localObject1).getName();
          str = localJarFile21.getBestMatch(localJarFile22, (JarEntry)localObject1);
          if (str == null)
          {
            if (_debug)
              System.out.println("NEW: " + (String)localObject2);
            localHashSet4.add(localObject2);
          }
          else if ((str.equals(localObject2)) && (!localHashSet3.contains(str)))
          {
            if (_debug)
              System.out.println((String)localObject2 + " added to implicit set!");
            localHashSet2.add(localObject2);
          }
          else
          {
            if ((!paramBoolean) && ((localHashSet2.contains(str)) || (localHashSet3.contains(str))))
            {
              if (_debug)
                System.out.println("NEW: " + (String)localObject2);
              localHashSet4.add(localObject2);
            }
            else
            {
              if (_debug)
                System.err.println("moved.put " + (String)localObject2 + " " + str);
              localHashMap.put(localObject2, str);
              localHashSet3.add(str);
            }
            if ((localHashSet2.contains(str)) && (paramBoolean))
            {
              if (_debug)
              {
                System.err.println("implicit.remove " + str);
                System.err.println("moved.put " + str + " " + str);
              }
              localHashSet2.remove(str);
              localHashMap.put(str, str);
              localHashSet3.add(str);
            }
          }
        }
      Object localObject1 = new ArrayList();
      localIterator = localJarFile21.getJarEntries();
      if (localIterator != null)
        while (localIterator.hasNext())
        {
          localObject2 = (JarEntry)localIterator.next();
          str = ((JarEntry)localObject2).getName();
          if ((!localHashSet2.contains(str)) && (!localHashSet3.contains(str)) && (!localHashSet4.contains(str)))
          {
            if (_debug)
              System.err.println("deleted.add " + str);
            ((ArrayList)localObject1).add(str);
          }
        }
      if (_debug)
      {
        localIterator = localHashMap.keySet().iterator();
        if (localIterator != null)
        {
          System.out.println("MOVED MAP!!!");
          while (localIterator.hasNext())
          {
            localObject2 = (String)localIterator.next();
            str = (String)localHashMap.get(localObject2);
            System.out.println("key is " + (String)localObject2 + " value is " + str);
          }
        }
        localIterator = localHashSet2.iterator();
        if (localIterator != null)
        {
          System.out.println("IMOVE MAP!!!");
          while (localIterator.hasNext())
          {
            localObject2 = (String)localIterator.next();
            System.out.println("key is " + (String)localObject2);
          }
        }
      }
      Object localObject2 = new JarOutputStream(paramOutputStream);
      createIndex((JarOutputStream)localObject2, (List)localObject1, localHashMap);
      localIterator = localHashSet4.iterator();
      if (localIterator != null)
        while (localIterator.hasNext())
        {
          str = (String)localIterator.next();
          if (_debug)
            System.out.println("New File: " + str);
          writeEntry((JarOutputStream)localObject2, localJarFile22.getEntryByName(str), localJarFile22);
        }
      ((JarOutputStream)localObject2).finish();
      ((JarOutputStream)localObject2).close();
    }
    catch (IOException localIOException1)
    {
      throw localIOException1;
    }
    finally
    {
      try
      {
        localJarFile21.getJarFile().close();
      }
      catch (IOException localIOException2)
      {
      }
      try
      {
        localJarFile22.getJarFile().close();
      }
      catch (IOException localIOException3)
      {
      }
    }
  }

  private static void createIndex(JarOutputStream paramJarOutputStream, List paramList, Map paramMap)
    throws IOException
  {
    StringWriter localStringWriter = new StringWriter();
    localStringWriter.write("version 1.0");
    localStringWriter.write("\r\n");
    for (int i = 0; i < paramList.size(); i++)
    {
      localObject1 = (String)paramList.get(i);
      localStringWriter.write("remove");
      localStringWriter.write(" ");
      writeEscapedString(localStringWriter, (String)localObject1);
      localStringWriter.write("\r\n");
    }
    Iterator localIterator = paramMap.keySet().iterator();
    if (localIterator != null)
      while (localIterator.hasNext())
      {
        localObject1 = (String)localIterator.next();
        localObject2 = (String)paramMap.get(localObject1);
        localStringWriter.write("move");
        localStringWriter.write(" ");
        writeEscapedString(localStringWriter, (String)localObject2);
        localStringWriter.write(" ");
        writeEscapedString(localStringWriter, (String)localObject1);
        localStringWriter.write("\r\n");
      }
    Object localObject1 = new JarEntry("META-INF/INDEX.JD");
    Object localObject2 = localStringWriter.toString().getBytes("UTF-8");
    localStringWriter.close();
    paramJarOutputStream.putNextEntry((ZipEntry)localObject1);
    paramJarOutputStream.write(localObject2, 0, localObject2.length);
  }

  private static void writeEscapedString(Writer paramWriter, String paramString)
    throws IOException
  {
    int i = 0;
    int j = 0;
    char[] arrayOfChar = null;
    while ((i = paramString.indexOf(' ', i)) != -1)
    {
      if (j != i)
      {
        if (arrayOfChar == null)
          arrayOfChar = paramString.toCharArray();
        paramWriter.write(arrayOfChar, j, i - j);
      }
      j = i;
      i++;
      paramWriter.write(92);
    }
    if (j != 0)
      paramWriter.write(arrayOfChar, j, arrayOfChar.length - j);
    else
      paramWriter.write(paramString);
  }

  private static void writeEntry(JarOutputStream paramJarOutputStream, JarEntry paramJarEntry, JarFile2 paramJarFile2)
    throws IOException
  {
    writeEntry(paramJarOutputStream, paramJarEntry, paramJarFile2.getJarFile().getInputStream(paramJarEntry));
  }

  private static void writeEntry(JarOutputStream paramJarOutputStream, JarEntry paramJarEntry, InputStream paramInputStream)
    throws IOException
  {
    paramJarOutputStream.putNextEntry(paramJarEntry);
    try
    {
      for (int i = paramInputStream.read(newBytes); i != -1; i = paramInputStream.read(newBytes))
        paramJarOutputStream.write(newBytes, 0, i);
    }
    catch (IOException localIOException1)
    {
      throw localIOException1;
    }
    finally
    {
      try
      {
        paramInputStream.close();
      }
      catch (IOException localIOException2)
      {
      }
    }
  }

  private static void showHelp()
  {
    System.out.println("JarDiff: [-nonminimal (for backward compatibility with 1.0.1/1.0] [-creatediff | -applydiff] [-output file] old.jar new.jar");
  }

  public static void main(String[] paramArrayOfString)
    throws IOException
  {
    int i = 1;
    boolean bool = true;
    String str = "out.jardiff";
    for (int j = 0; j < paramArrayOfString.length; j++)
      if ((paramArrayOfString[j].equals("-nonminimal")) || (paramArrayOfString[j].equals("-n")))
      {
        bool = false;
      }
      else if ((paramArrayOfString[j].equals("-creatediff")) || (paramArrayOfString[j].equals("-c")))
      {
        i = 1;
      }
      else if ((paramArrayOfString[j].equals("-applydiff")) || (paramArrayOfString[j].equals("-a")))
      {
        i = 0;
      }
      else if ((paramArrayOfString[j].equals("-debug")) || (paramArrayOfString[j].equals("-d")))
      {
        _debug = true;
      }
      else if ((paramArrayOfString[j].equals("-output")) || (paramArrayOfString[j].equals("-o")))
      {
        j++;
        if (j >= paramArrayOfString.length)
          continue;
        str = paramArrayOfString[j];
      }
      else if ((paramArrayOfString[j].equals("-applydiff")) || (paramArrayOfString[j].equals("-a")))
      {
        i = 0;
      }
      else
      {
        if (j + 2 != paramArrayOfString.length)
        {
          showHelp();
          System.exit(0);
        }
        if (i != 0)
          try
          {
            FileOutputStream localFileOutputStream1 = new FileOutputStream(str);
            createPatch(paramArrayOfString[j], paramArrayOfString[(j + 1)], localFileOutputStream1, bool);
            localFileOutputStream1.close();
          }
          catch (IOException localIOException1)
          {
            try
            {
              System.out.println(ResourceManager.getString("jardiff.error.create", localIOException1.toString()));
            }
            catch (MissingResourceException localMissingResourceException1)
            {
            }
          }
        else
          try
          {
            FileOutputStream localFileOutputStream2 = new FileOutputStream(str);
            new JarDiffPatcher().applyPatch(null, paramArrayOfString[j], paramArrayOfString[(j + 1)], localFileOutputStream2);
            localFileOutputStream2.close();
          }
          catch (IOException localIOException2)
          {
            try
            {
              System.out.println(ResourceManager.getString("jardiff.error.apply", localIOException2.toString()));
            }
            catch (MissingResourceException localMissingResourceException2)
            {
            }
          }
        System.exit(0);
      }
    showHelp();
  }

  private static class JarFile2
  {
    private JarFile _jar;
    private List _entries;
    private HashMap _nameToEntryMap;
    private HashMap _crcToEntryMap;

    public JarFile2(String paramString)
      throws IOException
    {
      this._jar = new JarFile(new File(paramString));
      index();
    }

    public JarFile getJarFile()
    {
      return this._jar;
    }

    public Iterator getJarEntries()
    {
      return this._entries.iterator();
    }

    public JarEntry getEntryByName(String paramString)
    {
      return (JarEntry)this._nameToEntryMap.get(paramString);
    }

    private static boolean differs(InputStream paramInputStream1, InputStream paramInputStream2)
      throws IOException
    {
      int i = 0;
      int k = 0;
      int m = 0;
      try
      {
        while (i != -1)
        {
          i = paramInputStream2.read(JarDiff.newBytes);
          int j = paramInputStream1.read(JarDiff.oldBytes);
          if (i != j)
          {
            if (JarDiff._debug)
              System.out.println("\tread sizes differ: " + i + " " + j + " total " + k);
            m = 1;
            break;
          }
          if (i <= 0)
            continue;
          while (true)
          {
            i--;
            if (i < 0)
              break;
            k++;
            if (JarDiff.newBytes[i] == JarDiff.oldBytes[i])
              continue;
            if (JarDiff._debug)
              System.out.println("\tbytes differ at " + k);
            m = 1;
          }
          if (m != 0)
            break;
          i = 0;
        }
      }
      catch (IOException localIOException1)
      {
        throw localIOException1;
      }
      finally
      {
        try
        {
          paramInputStream1.close();
        }
        catch (IOException localIOException2)
        {
        }
        try
        {
          paramInputStream2.close();
        }
        catch (IOException localIOException3)
        {
        }
      }
      return m;
    }

    public String getBestMatch(JarFile2 paramJarFile2, JarEntry paramJarEntry)
      throws IOException
    {
      if (contains(paramJarFile2, paramJarEntry))
        return paramJarEntry.getName();
      return hasSameContent(paramJarFile2, paramJarEntry);
    }

    public boolean contains(JarFile2 paramJarFile2, JarEntry paramJarEntry)
      throws IOException
    {
      JarEntry localJarEntry = getEntryByName(paramJarEntry.getName());
      if (localJarEntry == null)
        return false;
      if (localJarEntry.getCrc() != paramJarEntry.getCrc())
        return false;
      boolean bool = false;
      try
      {
        InputStream localInputStream1 = getJarFile().getInputStream(localJarEntry);
        InputStream localInputStream2 = paramJarFile2.getJarFile().getInputStream(paramJarEntry);
        bool = differs(localInputStream1, localInputStream2);
      }
      catch (IOException localIOException1)
      {
        throw localIOException1;
      }
      finally
      {
        try
        {
          getJarFile().close();
        }
        catch (IOException localIOException2)
        {
        }
        try
        {
          paramJarFile2.getJarFile().close();
        }
        catch (IOException localIOException3)
        {
        }
      }
      return !bool;
    }

    public String hasSameContent(JarFile2 paramJarFile2, JarEntry paramJarEntry)
      throws IOException
    {
      String str1 = null;
      Long localLong = new Long(paramJarEntry.getCrc());
      if (this._crcToEntryMap.containsKey(localLong))
      {
        LinkedList localLinkedList = (LinkedList)this._crcToEntryMap.get(localLong);
        ListIterator localListIterator = localLinkedList.listIterator(0);
        if (localListIterator != null)
          try
          {
            String str2;
            while (localListIterator.hasNext())
            {
              JarEntry localJarEntry = (JarEntry)localListIterator.next();
              InputStream localInputStream1 = getJarFile().getInputStream(localJarEntry);
              InputStream localInputStream2 = paramJarFile2.getJarFile().getInputStream(paramJarEntry);
              if (!differs(localInputStream1, localInputStream2))
              {
                str1 = localJarEntry.getName();
                str2 = str1;
                jsr 28;
              }
            }
          }
          catch (IOException localIOException1)
          {
            throw localIOException1;
          }
          finally
          {
            try
            {
              getJarFile().close();
            }
            catch (IOException localIOException2)
            {
            }
            try
            {
              paramJarFile2.getJarFile().close();
            }
            catch (IOException localIOException3)
            {
            }
          }
      }
      return str1;
    }

    private void index()
      throws IOException
    {
      Enumeration localEnumeration = this._jar.entries();
      this._nameToEntryMap = new HashMap();
      this._crcToEntryMap = new HashMap();
      this._entries = new ArrayList();
      if (JarDiff._debug)
        System.out.println("indexing: " + this._jar.getName());
      if (localEnumeration != null)
        while (localEnumeration.hasMoreElements())
        {
          JarEntry localJarEntry = (JarEntry)localEnumeration.nextElement();
          long l = localJarEntry.getCrc();
          Long localLong = new Long(l);
          if (JarDiff._debug)
            System.out.println("\t" + localJarEntry.getName() + " CRC " + l);
          this._nameToEntryMap.put(localJarEntry.getName(), localJarEntry);
          this._entries.add(localJarEntry);
          LinkedList localLinkedList;
          if (this._crcToEntryMap.containsKey(localLong))
          {
            localLinkedList = (LinkedList)this._crcToEntryMap.get(localLong);
            localLinkedList.add(localJarEntry);
            this._crcToEntryMap.put(localLong, localLinkedList);
          }
          else
          {
            localLinkedList = new LinkedList();
            localLinkedList.add(localJarEntry);
            this._crcToEntryMap.put(localLong, localLinkedList);
          }
        }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.jardiff.JarDiff
 * JD-Core Version:    0.6.0
 */