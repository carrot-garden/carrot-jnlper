package com.sun.javaws;

import com.sun.deploy.trace.Trace;
import java.io.File;

public class UnixOperaSupport extends OperaSupport
{
  private static final String USER_HOME = "user.home";
  private static final String OPERA_DIR = ".opera";

  public boolean isInstalled()
  {
    return getUserDir().exists();
  }

  public void enableJnlp(File paramFile, boolean paramBoolean)
  {
    File localFile1 = getUserDir();
    File localFile2 = null;
    if (localFile1.exists())
      try
      {
        localFile2 = new File(localFile1, "opera6.ini");
        if (!localFile2.exists())
          localFile2 = new File(localFile1, "opera.ini");
        enableJnlp(null, localFile2, paramFile, paramBoolean);
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
  }

  public UnixOperaSupport()
  {
    super(false);
  }

  private File getUserDir()
  {
    return new File(System.getProperty("user.home"), ".opera");
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.UnixOperaSupport
 * JD-Core Version:    0.6.0
 */