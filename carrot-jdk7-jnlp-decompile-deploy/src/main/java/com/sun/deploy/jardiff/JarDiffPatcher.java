package com.sun.deploy.jardiff;

import com.sun.applet2.preloader.CancelException;
import com.sun.deploy.resources.ResourceManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class JarDiffPatcher
  implements JarDiffConstants, Patcher
{
  private static final int DEFAULT_READ_SIZE = 2048;
  private static byte[] newBytes = new byte[2048];
  private static byte[] oldBytes = new byte[2048];

  public void applyPatch(Patcher.PatchDelegate paramPatchDelegate, String paramString1, String paramString2, OutputStream paramOutputStream)
    throws IOException
  {
    File localFile1 = new File(paramString1);
    File localFile2 = new File(paramString2);
    JarOutputStream localJarOutputStream = new JarOutputStream(paramOutputStream);
    JarFile localJarFile1 = new JarFile(localFile1);
    JarFile localJarFile2 = new JarFile(localFile2);
    HashSet localHashSet1 = new HashSet();
    HashMap localHashMap = new HashMap();
    determineNameMapping(localJarFile2, localHashSet1, localHashMap);
    Object[] arrayOfObject = localHashMap.keySet().toArray();
    HashSet localHashSet2 = new HashSet();
    Enumeration localEnumeration1 = localJarFile1.entries();
    if (localEnumeration1 != null)
      while (localEnumeration1.hasMoreElements())
        localHashSet2.add(((JarEntry)localEnumeration1.nextElement()).getName());
    double d1 = localHashSet2.size() + arrayOfObject.length + localJarFile2.size();
    double d2 = 0.0D;
    localHashSet2.removeAll(localHashSet1);
    d1 -= localHashSet1.size();
    Enumeration localEnumeration2 = localJarFile2.entries();
    if (localEnumeration2 != null)
      while (localEnumeration2.hasMoreElements())
      {
        JarEntry localJarEntry1 = (JarEntry)localEnumeration2.nextElement();
        if (!"META-INF/INDEX.JD".equals(localJarEntry1.getName()))
        {
          updateDelegate(paramPatchDelegate, d2, d1);
          d2 += 1.0D;
          writeEntry(localJarOutputStream, localJarEntry1, localJarFile2);
          boolean bool1 = localHashSet2.remove(localJarEntry1.getName());
          if (bool1)
            d1 -= 1.0D;
        }
        else
        {
          d1 -= 1.0D;
        }
      }
    String str;
    Object localObject1;
    for (int i = 0; i < arrayOfObject.length; i++)
    {
      str = (String)arrayOfObject[i];
      localObject1 = (String)localHashMap.get(str);
      JarEntry localJarEntry2 = localJarFile1.getJarEntry((String)localObject1);
      if (localJarEntry2 == null)
      {
        localObject2 = "move" + (String)localObject1 + " " + str;
        handleException("jardiff.error.badmove", (String)localObject2);
      }
      Object localObject2 = new JarEntry(str);
      ((JarEntry)localObject2).setTime(localJarEntry2.getTime());
      ((JarEntry)localObject2).setSize(localJarEntry2.getSize());
      ((JarEntry)localObject2).setCompressedSize(localJarEntry2.getCompressedSize());
      ((JarEntry)localObject2).setCrc(localJarEntry2.getCrc());
      ((JarEntry)localObject2).setMethod(localJarEntry2.getMethod());
      ((JarEntry)localObject2).setExtra(localJarEntry2.getExtra());
      ((JarEntry)localObject2).setComment(localJarEntry2.getComment());
      updateDelegate(paramPatchDelegate, d2, d1);
      d2 += 1.0D;
      writeEntry(localJarOutputStream, (JarEntry)localObject2, localJarFile1.getInputStream(localJarEntry2));
      boolean bool2 = localHashSet2.remove(localObject1);
      if (!bool2)
        continue;
      d1 -= 1.0D;
    }
    Iterator localIterator = localHashSet2.iterator();
    if (localIterator != null)
      while (localIterator.hasNext())
      {
        str = (String)localIterator.next();
        localObject1 = localJarFile1.getJarEntry(str);
        updateDelegate(paramPatchDelegate, d2, d1);
        d2 += 1.0D;
        writeEntry(localJarOutputStream, (JarEntry)localObject1, localJarFile1);
      }
    updateDelegate(paramPatchDelegate, d2, d1);
    localJarOutputStream.finish();
    localJarOutputStream.close();
    localJarFile1.close();
    localJarFile2.close();
  }

  private void updateDelegate(Patcher.PatchDelegate paramPatchDelegate, double paramDouble1, double paramDouble2)
    throws CancelException
  {
    if (paramPatchDelegate != null)
      paramPatchDelegate.patching((int)(100.0D * paramDouble1 / paramDouble2));
  }

  private void determineNameMapping(JarFile paramJarFile, Set paramSet, Map paramMap)
    throws IOException
  {
    InputStream localInputStream = paramJarFile.getInputStream(paramJarFile.getEntry("META-INF/INDEX.JD"));
    if (localInputStream == null)
      handleException("jardiff.error.noindex", null);
    LineNumberReader localLineNumberReader = new LineNumberReader(new InputStreamReader(localInputStream, "UTF-8"));
    String str = localLineNumberReader.readLine();
    if ((str == null) || (!str.equals("version 1.0")))
      handleException("jardiff.error.badheader", str);
    while ((str = localLineNumberReader.readLine()) != null)
    {
      List localList;
      if (str.startsWith("remove"))
      {
        localList = getSubpaths(str.substring("remove".length()));
        if (localList.size() != 1)
          handleException("jardiff.error.badremove", str);
        paramSet.add(localList.get(0));
        continue;
      }
      if (str.startsWith("move"))
      {
        localList = getSubpaths(str.substring("move".length()));
        if (localList.size() != 2)
          handleException("jardiff.error.badmove", str);
        if (paramMap.put(localList.get(1), localList.get(0)) != null)
          handleException("jardiff.error.badmove", str);
        continue;
      }
      if (str.length() <= 0)
        continue;
      handleException("jardiff.error.badcommand", str);
    }
    localLineNumberReader.close();
    localInputStream.close();
  }

  private void handleException(String paramString1, String paramString2)
    throws IOException
  {
    try
    {
      throw new IOException(ResourceManager.getString(paramString1, paramString2));
    }
    catch (MissingResourceException localMissingResourceException)
    {
      System.err.println("Fatal error: " + paramString1);
      new Throwable().printStackTrace(System.err);
      System.exit(-1);
    }
  }

  private List getSubpaths(String paramString)
  {
    int i = 0;
    int j = paramString.length();
    ArrayList localArrayList = new ArrayList();
    while (i < j)
    {
      while ((i < j) && (Character.isWhitespace(paramString.charAt(i))))
        i++;
      if (i >= j)
        continue;
      int k = i;
      int m = k;
      String str = null;
      while (i < j)
      {
        char c = paramString.charAt(i);
        if ((c == '\\') && (i + 1 < j) && (paramString.charAt(i + 1) == ' '))
        {
          if (str == null)
            str = paramString.substring(m, i);
          else
            str = str + paramString.substring(m, i);
          i++;
          m = i;
        }
        else
        {
          if (Character.isWhitespace(c))
            break;
        }
        i++;
      }
      if (m != i)
        if (str == null)
          str = paramString.substring(m, i);
        else
          str = str + paramString.substring(m, i);
      localArrayList.add(str);
    }
    return localArrayList;
  }

  private void writeEntry(JarOutputStream paramJarOutputStream, JarEntry paramJarEntry, JarFile paramJarFile)
    throws IOException
  {
    writeEntry(paramJarOutputStream, paramJarEntry, paramJarFile.getInputStream(paramJarEntry));
  }

  private void writeEntry(JarOutputStream paramJarOutputStream, JarEntry paramJarEntry, InputStream paramInputStream)
    throws IOException
  {
    paramJarOutputStream.putNextEntry(new ZipEntry(paramJarEntry.getName()));
    for (int i = paramInputStream.read(newBytes); i != -1; i = paramInputStream.read(newBytes))
      paramJarOutputStream.write(newBytes, 0, i);
    paramInputStream.close();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.jardiff.JarDiffPatcher
 * JD-Core Version:    0.6.0
 */