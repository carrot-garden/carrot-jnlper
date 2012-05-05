package com.sun.deploy.config;

import com.sun.deploy.Environment;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.VersionID;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

public class JfxRuntime
{
  private final String version;
  private final VersionID versionId;
  private final String path;
  public static final String RT_JAR = "lib" + File.separator + "jfxrt.jar";

  public static JfxRuntime runtimeForPath(String paramString)
  {
    File localFile = new File(paramString + File.separator + RT_JAR);
    if (localFile.canRead())
      try
      {
        URLClassLoader localURLClassLoader = new URLClassLoader(new URL[] { localFile.toURL() }, null);
        Class localClass = localURLClassLoader.loadClass("com.sun.javafx.runtime.VersionInfo");
        if (localClass != null)
        {
          Field localField = localClass.getDeclaredField("RAW_VERSION");
          if (localField != null)
          {
            localField.setAccessible(true);
            return new JfxRuntime((String)localField.get(null), paramString);
          }
        }
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
    Trace.println("No valid JFX runtime at [" + paramString + "]", TraceLevel.BASIC);
    return null;
  }

  public JfxRuntime(String paramString1, String paramString2)
  {
    this.version = paramString1;
    this.path = (paramString2 + File.separator);
    this.versionId = new VersionID(paramString1);
  }

  public boolean isValid()
  {
    File localFile = new File(this.path + RT_JAR);
    return localFile.canRead();
  }

  public String toString()
  {
    return "JavaFX " + this.version + " found at " + this.path;
  }

  public String getClassPath()
  {
    String str = Environment.getenv("FORCED_FX_ROOT");
    if (str != null)
      return str + File.separator + RT_JAR;
    return this.path + RT_JAR;
  }

  public URL[] getURLs()
  {
    URL[] arrayOfURL = new URL[1];
    File localFile = new File(getClassPath());
    try
    {
      arrayOfURL[0] = localFile.getCanonicalFile().toURI().toURL();
    }
    catch (IOException localIOException)
    {
      Trace.printException(localIOException);
      return null;
    }
    return arrayOfURL;
  }

  public String getNativeLibPath()
  {
    return this.path + "bin";
  }

  public VersionID getProductVersion()
  {
    return this.versionId;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.JfxRuntime
 * JD-Core Version:    0.6.0
 */