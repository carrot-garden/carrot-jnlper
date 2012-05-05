package com.sun.deploy.util;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import sun.misc.JavaUtilJarAccess;
import sun.misc.SharedSecrets;
import sun.security.action.OpenFileInputStreamAction;

public final class TrustedLibraries
{
  private static TrustedLibraries INSTANCE = null;
  private static final String DIGEST_MANIFEST = "-DIGEST-MANIFEST";
  private final HashMap entries = new HashMap();
  private static long lastModified = 0L;

  public static TrustedLibraries getInstance()
  {
    return INSTANCE;
  }

  public static TrustedLibraries getInstance(File paramFile)
  {
    return new TrustedLibraries(paramFile);
  }

  private TrustedLibraries()
  {
    try
    {
      String str1 = Config.getUserTrustedLibrariesFile();
      String str2 = Config.getSystemTrustedLibrariesFile();
      File localFile1 = new File(str1);
      File localFile2 = new File(str2);
      if (localFile1.exists())
      {
        setup(localFile1);
        lastModified = localFile1.lastModified();
      }
      if (localFile2.exists())
      {
        setup(localFile2);
        long l = localFile2.lastModified();
        if (l > lastModified)
          lastModified = l;
      }
    }
    catch (IOException localIOException)
    {
      throw new TrustedLibrariesSyntaxException(localIOException);
    }
  }

  private TrustedLibraries(File paramFile)
  {
    try
    {
      setup(paramFile);
    }
    catch (IOException localIOException)
    {
      throw new TrustedLibrariesSyntaxException(localIOException);
    }
  }

  private void setup(File paramFile)
    throws IOException
  {
    BufferedReader localBufferedReader = null;
    try
    {
      FileInputStream localFileInputStream = (FileInputStream)AccessController.doPrivileged(new OpenFileInputStreamAction(paramFile));
      localBufferedReader = new BufferedReader(new InputStreamReader(localFileInputStream));
      parse(localBufferedReader);
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
    finally
    {
      if (localBufferedReader != null)
        localBufferedReader.close();
    }
  }

  public boolean isEmpty()
  {
    return this.entries.isEmpty();
  }

  private void setupTokenizer(StreamTokenizer paramStreamTokenizer)
  {
    paramStreamTokenizer.resetSyntax();
    paramStreamTokenizer.wordChars(97, 122);
    paramStreamTokenizer.wordChars(65, 90);
    paramStreamTokenizer.wordChars(48, 57);
    paramStreamTokenizer.wordChars(46, 46);
    paramStreamTokenizer.wordChars(45, 45);
    paramStreamTokenizer.wordChars(95, 95);
    paramStreamTokenizer.wordChars(43, 43);
    paramStreamTokenizer.wordChars(47, 47);
    paramStreamTokenizer.whitespaceChars(0, 32);
    paramStreamTokenizer.commentChar(35);
    paramStreamTokenizer.eolIsSignificant(true);
  }

  private void parse(Reader paramReader)
    throws IOException
  {
    StreamTokenizer localStreamTokenizer = new StreamTokenizer(paramReader);
    setupTokenizer(localStreamTokenizer);
    while (true)
    {
      int i = localStreamTokenizer.nextToken();
      if (i == -1)
        break;
      if (i == 10)
        continue;
      if (i != -3)
        throw new IOException("Unexpected token: " + localStreamTokenizer);
      String str = localStreamTokenizer.sval;
      if (str.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST-MANIFEST"))
        parseJarEntry(localStreamTokenizer);
      else
        throw new IOException("Unknown attribute `" + str + "', line " + localStreamTokenizer.lineno());
    }
  }

  private void parseColon(StreamTokenizer paramStreamTokenizer)
    throws IOException
  {
    int i = paramStreamTokenizer.nextToken();
    if (i != 58)
      throw new IOException("Expected ':', read " + paramStreamTokenizer);
  }

  private void parseJarEntry(StreamTokenizer paramStreamTokenizer)
    throws IOException
  {
    String str1 = paramStreamTokenizer.sval;
    parseColon(paramStreamTokenizer);
    String str2 = null;
    paramStreamTokenizer.wordChars(61, 61);
    int i = paramStreamTokenizer.nextToken();
    if (i != -3)
      throw new IOException("Unexpected value: " + paramStreamTokenizer);
    paramStreamTokenizer.ordinaryChar(61);
    str2 = paramStreamTokenizer.sval;
    if (str2 == null)
      throw new IOException("hash must be specified");
    this.entries.put(str1.toUpperCase(Locale.ENGLISH) + str2, null);
  }

  public boolean contains(String paramString1, String paramString2)
  {
    return this.entries.containsKey(paramString1.toUpperCase(Locale.ENGLISH) + paramString2);
  }

  private static Attributes readAttributes(JarFile paramJarFile, JarEntry paramJarEntry)
    throws IOException
  {
    InputStream localInputStream = paramJarFile.getInputStream(paramJarEntry);
    try
    {
      Object localObject1 = AccessController.doPrivileged(new PrivilegedExceptionAction(localInputStream)
      {
        private final InputStream val$is;

        public Object run()
          throws Exception
        {
          Attributes localAttributes = new Attributes();
          Class localClass = Class.forName("java.util.jar.Manifest$FastInputStream");
          Constructor[] arrayOfConstructor = localClass.getDeclaredConstructors();
          Constructor localConstructor = null;
          for (int i = 0; i < arrayOfConstructor.length; i++)
          {
            localObject = arrayOfConstructor[i].getParameterTypes();
            if ((localObject.length != 1) || (localObject[0] != InputStream.class))
              continue;
            localConstructor = arrayOfConstructor[i];
            break;
          }
          if (localConstructor == null)
            throw new Exception("Failed to find stream constructor");
          localConstructor.setAccessible(true);
          Object[] arrayOfObject1 = { this.val$is };
          Object localObject = localConstructor.newInstance(arrayOfObject1);
          byte[] arrayOfByte = new byte[512];
          Class[] arrayOfClass = { localClass, arrayOfByte.getClass() };
          Method localMethod = Attributes.class.getDeclaredMethod("read", arrayOfClass);
          if (localMethod != null)
          {
            localMethod.setAccessible(true);
            Object[] arrayOfObject2 = { localObject, arrayOfByte };
            localMethod.invoke(localAttributes, arrayOfObject2);
          }
          return localAttributes;
        }
      });
      localAttributes = (Attributes)localObject1;
    }
    catch (Exception localException)
    {
      Attributes localAttributes = paramJarEntry.getAttributes();
      return localAttributes;
    }
    finally
    {
      localInputStream.close();
    }
  }

  public static boolean checkJarEntry(JarFile paramJarFile, JarEntry paramJarEntry)
    throws IOException, GeneralSecurityException
  {
    if ((INSTANCE == null) || (INSTANCE.isEmpty()))
      return true;
    if (!paramJarEntry.getName().toUpperCase(Locale.ENGLISH).endsWith(".SF"))
      return false;
    Attributes localAttributes = readAttributes(paramJarFile, paramJarEntry);
    if (localAttributes == null)
      return false;
    Iterator localIterator = localAttributes.keySet().iterator();
    while (localIterator.hasNext())
    {
      String str1 = localIterator.next().toString();
      if (str1.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST-MANIFEST"))
      {
        Attributes.Name localName = new Attributes.Name(str1);
        String str2 = localAttributes.getValue(localName);
        if (INSTANCE.contains(str1, str2))
        {
          Trace.msgSecurityPrintln("downloadengine.check.trustedlibraries.found");
          throw new GeneralSecurityException("trusted libraries list entry!");
        }
      }
    }
    Trace.msgSecurityPrintln("downloadengine.check.trustedlibraries.notfound");
    return false;
  }

  public static boolean checkJarFile(JarFile paramJarFile)
    throws IOException
  {
    if ((INSTANCE == null) || (INSTANCE.isEmpty()))
    {
      Trace.msgSecurityPrintln("downloadengine.check.trustedlibraries.notexist");
      return false;
    }
    List localList = getManifestDigests(paramJarFile);
    Object localObject;
    String str;
    if ((localList != null) && (localList.size() > 0))
      try
      {
        Iterator localIterator = localList.iterator();
        while (localIterator.hasNext())
        {
          localObject = (String)localIterator.next();
          str = (String)localIterator.next();
          if (INSTANCE.contains((String)localObject, str))
          {
            Trace.msgSecurityPrintln("downloadengine.check.trustedlibraries.found");
            return true;
          }
        }
        return false;
      }
      catch (NoSuchElementException localNoSuchElementException)
      {
      }
    Enumeration localEnumeration = paramJarFile.entries();
    while (localEnumeration.hasMoreElements())
    {
      localObject = (JarEntry)localEnumeration.nextElement();
      str = ((JarEntry)localObject).getName().toUpperCase(Locale.ENGLISH);
      if ((str.startsWith("META-INF/")) || (str.startsWith("/META-INF/")))
        try
        {
          if (checkJarEntry(paramJarFile, (JarEntry)localObject))
            return false;
        }
        catch (GeneralSecurityException localGeneralSecurityException)
        {
          return true;
        }
    }
    Trace.msgSecurityPrintln("downloadengine.check.trustedlibraries.notsigned");
    return false;
  }

  public static boolean hasBeenModifiedSince(long paramLong)
  {
    return (INSTANCE != null) && (lastModified >= paramLong);
  }

  private static List getManifestDigests(JarFile paramJarFile)
  {
    try
    {
      JavaUtilJarAccess localJavaUtilJarAccess = SharedSecrets.javaUtilJarAccess();
      return localJavaUtilJarAccess.getManifestDigests(paramJarFile);
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
      return null;
    }
    catch (NoClassDefFoundError localNoClassDefFoundError)
    {
    }
    return null;
  }

  static
  {
    if ((Config.getBooleanProperty("deployment.security.blacklist.check")) && (Config.checkClassName("sun.security.action.OpenFileInputStreamAction")))
    {
      Trace.msgSecurityPrintln("downloadengine.check.trustedlibraries.enabled");
      INSTANCE = new TrustedLibraries();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.TrustedLibraries
 * JD-Core Version:    0.6.0
 */