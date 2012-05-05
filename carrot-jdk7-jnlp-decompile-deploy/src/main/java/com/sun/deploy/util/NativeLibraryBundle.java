package com.sun.deploy.util;

import com.sun.deploy.config.Platform;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class NativeLibraryBundle
{
  private static File rootDir;
  private static final String LOCK_SUFFIX = ".lck";
  private static Attributes.Name independentLibsAttribute = new Attributes.Name("IndependentLibraries");
  private static Set dirsCreatedByThisJVM = Collections.synchronizedSet(new HashSet());
  private File lockFile;
  private FileLockWrapper lock;
  private String dirName;
  private Map libNameMap = new HashMap();
  private File destination = null;
  static Map originFileLocks;

  public synchronized String get(String paramString)
  {
    return (String)this.libNameMap.get(paramString);
  }

  private synchronized void put(String paramString1, String paramString2)
  {
    this.libNameMap.put(paramString1, paramString2);
  }

  protected void finalize()
  {
    releaseLocksOfOriginals(this);
    if (deleteRecursively(new File(rootDir, this.dirName)))
    {
      this.lock.release();
      this.lockFile.delete();
    }
  }

  private synchronized File getDestinationDir()
    throws IOException
  {
    if (this.destination == null)
    {
      synchronized (dirsCreatedByThisJVM)
      {
        this.lockFile = File.createTempFile("tmp", ".lck", rootDir);
        String str = this.lockFile.getName();
        this.dirName = str.substring(0, str.lastIndexOf(".lck"));
        dirsCreatedByThisJVM.add(this.dirName);
      }
      this.lock = FileLockWrapper.lockFile(this.lockFile);
      this.destination = new File(rootDir, this.dirName);
      this.destination.mkdir();
    }
    return this.destination;
  }

  private static synchronized boolean tryLockOriginalCopy(File paramFile, boolean paramBoolean, NativeLibraryBundle paramNativeLibraryBundle)
  {
    String str;
    if (paramBoolean)
      str = paramFile.getParent();
    else
      str = paramFile.getAbsolutePath();
    Object localObject = originFileLocks.get(str);
    if (localObject == null)
    {
      originFileLocks.put(paramFile.getParent(), paramNativeLibraryBundle);
      return true;
    }
    return localObject == paramNativeLibraryBundle;
  }

  private static synchronized void releaseLocksOfOriginals(NativeLibraryBundle paramNativeLibraryBundle)
  {
    Iterator localIterator = originFileLocks.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      if (localEntry.getValue() != paramNativeLibraryBundle)
        localIterator.remove();
    }
  }

  private void processLib(Manifest paramManifest, File paramFile, boolean paramBoolean)
    throws IOException
  {
    String str = null;
    if (!tryLockOriginalCopy(paramFile, paramBoolean, this))
    {
      Trace.println("Failed to grab lock for " + paramFile, TraceLevel.CACHE);
      str = copyLib(paramFile);
    }
    else
    {
      str = paramFile.getAbsolutePath();
    }
    put(paramFile.getName(), str);
  }

  public void prepareLibrary(String paramString1, JarFile paramJarFile, String paramString2)
    throws IOException
  {
    Manifest localManifest = paramJarFile.getManifest();
    Object localObject = localManifest != null ? localManifest.getMainAttributes() : null;
    File localFile;
    if ((localObject != null) && ("true".equals(localObject.get(independentLibsAttribute))))
    {
      Trace.println("Lock individual library " + paramString1, TraceLevel.CACHE);
      localFile = new File(paramString2, paramString1);
      processLib(localManifest, localFile, false);
    }
    else
    {
      localFile = new File(paramString2);
      File[] arrayOfFile = localFile.listFiles();
      for (int i = 0; i < arrayOfFile.length; i++)
        processLib(localManifest, arrayOfFile[i], true);
    }
  }

  private String copyLib(File paramFile)
    throws IOException
  {
    if ((paramFile.isFile()) && (paramFile.getName().endsWith(Platform.get().getLibrarySufix())))
    {
      FileChannel localFileChannel1 = new FileInputStream(paramFile).getChannel();
      File localFile = new File(getDestinationDir(), paramFile.getName());
      FileChannel localFileChannel2 = new FileOutputStream(localFile).getChannel();
      localFileChannel1.transferTo(0L, localFileChannel1.size(), localFileChannel2);
      localFileChannel2.force(true);
      localFileChannel1.close();
      localFileChannel2.close();
      return localFile.getAbsolutePath();
    }
    return null;
  }

  private static void deleteOldDirs()
  {
    File[] arrayOfFile = rootDir.listFiles(new FileFilter()
    {
      public boolean accept(File paramFile)
      {
        return paramFile.isDirectory();
      }
    });
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      File localFile1 = arrayOfFile[i];
      if (dirsCreatedByThisJVM.contains(localFile1.getName()))
        continue;
      File localFile2 = new File(rootDir, localFile1.getName() + ".lck");
      try
      {
        FileLockWrapper localFileLockWrapper = FileLockWrapper.tryLockFile(localFile2);
        if (localFileLockWrapper != null)
        {
          boolean bool = deleteRecursively(localFile1);
          if (bool)
          {
            localFileLockWrapper.release();
            localFile2.delete();
          }
        }
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
  }

  private static boolean deleteRecursively(File paramFile)
  {
    File[] arrayOfFile = paramFile.listFiles();
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      File localFile = arrayOfFile[i];
      if (localFile.isDirectory())
        deleteRecursively(localFile);
      else
        localFile.delete();
    }
    return paramFile.delete();
  }

  static
  {
    String str = System.getProperty("java.io.tmpdir") + File.separator + ".java_jnlp_applet_nativelib_cache." + System.getProperty("user.name");
    rootDir = new File(str);
    if (!rootDir.exists())
      rootDir.mkdir();
    1 local1 = new Thread()
    {
      public void run()
      {
        NativeLibraryBundle.access$000();
      }
    };
    local1.setName("Native Library Cache Cleaner Thread");
    local1.setDaemon(true);
    local1.start();
    originFileLocks = new HashMap();
  }

  static class FileLockWrapper
  {
    private FileOutputStream str;
    private FileChannel chan;
    private FileLock lock;

    private FileLockWrapper(FileOutputStream paramFileOutputStream, FileChannel paramFileChannel, FileLock paramFileLock)
    {
      this.str = paramFileOutputStream;
      this.chan = paramFileChannel;
      this.lock = paramFileLock;
    }

    public void release()
    {
      try
      {
        this.lock.release();
      }
      catch (IOException localIOException1)
      {
        Trace.ignoredException(localIOException1);
      }
      try
      {
        this.chan.close();
      }
      catch (IOException localIOException2)
      {
        Trace.ignoredException(localIOException2);
      }
    }

    public static FileLockWrapper lockFile(File paramFile)
      throws IOException
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(paramFile);
      FileChannel localFileChannel = localFileOutputStream.getChannel();
      FileLock localFileLock = localFileChannel.lock();
      if (localFileLock == null)
      {
        localFileChannel.close();
        return null;
      }
      return new FileLockWrapper(localFileOutputStream, localFileChannel, localFileLock);
    }

    public static FileLockWrapper tryLockFile(File paramFile)
      throws IOException
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(paramFile);
      FileChannel localFileChannel = localFileOutputStream.getChannel();
      FileLock localFileLock = localFileChannel.tryLock();
      if (localFileLock == null)
      {
        localFileChannel.close();
        return null;
      }
      return new FileLockWrapper(localFileOutputStream, localFileChannel, localFileLock);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.NativeLibraryBundle
 * JD-Core Version:    0.6.0
 */