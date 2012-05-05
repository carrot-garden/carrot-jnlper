package com.sun.deploy.trace;

import com.sun.deploy.config.Config;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggerTraceListener
  implements TraceListener
{
  private static LoggerTraceListener sharedInstance;
  private final Logger logger;

  public static synchronized LoggerTraceListener getOrCreateSharedInstance(String paramString1, File paramFile1, File paramFile2, String paramString2, String paramString3, boolean paramBoolean)
  {
    if ((sharedInstance == null) || (paramBoolean))
    {
      paramFile1 = ensureLogFileAvailable(paramFile1, paramFile2, paramString2, paramString3);
      sharedInstance = new LoggerTraceListener(paramString1, paramFile1.getPath());
    }
    return sharedInstance;
  }

  public LoggerTraceListener(String paramString1, String paramString2)
  {
    FileHandler localFileHandler = null;
    this.logger = Logger.getLogger(paramString1);
    this.logger.setUseParentHandlers(false);
    try
    {
      localFileHandler = new FileHandler(paramString2, Config.getIntProperty("deployment.max.output.file.size") * 1048576, 1);
      this.logger.addHandler(localFileHandler);
      this.logger.setLevel(Level.OFF);
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  public Logger getLogger()
  {
    return this.logger;
  }

  public void print(String paramString)
  {
    this.logger.log(Level.FINE, paramString);
  }

  public void flush()
  {
  }

  static File ensureLogFileAvailable(File paramFile1, File paramFile2, String paramString1, String paramString2)
  {
    if (!ensureExists(paramFile1))
      paramFile1 = Trace.createTempFile(paramString1, paramString2, paramFile2);
    return paramFile1;
  }

  private static boolean ensureExists(File paramFile)
  {
    if (paramFile == null)
      return false;
    try
    {
      if (!paramFile.exists())
      {
        File localFile = paramFile.getParentFile();
        if (!localFile.exists())
          localFile.mkdirs();
        paramFile.createNewFile();
      }
      return true;
    }
    catch (IOException localIOException)
    {
      Trace.println("Cannot write to file: " + paramFile, TraceLevel.BASIC);
    }
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.trace.LoggerTraceListener
 * JD-Core Version:    0.6.0
 */