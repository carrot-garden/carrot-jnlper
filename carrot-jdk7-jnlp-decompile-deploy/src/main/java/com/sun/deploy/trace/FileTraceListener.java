package com.sun.deploy.trace;

import com.sun.deploy.config.Config;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class FileTraceListener
  implements TraceListener
{
  private static FileTraceListener sharedInstance;
  private File logFile;
  private PrintStream fileTraceStream;
  private boolean append;

  public static synchronized FileTraceListener getOrCreateSharedInstance(File paramFile1, File paramFile2, String paramString1, String paramString2, boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((sharedInstance == null) || (paramBoolean2))
    {
      paramFile1 = LoggerTraceListener.ensureLogFileAvailable(paramFile1, paramFile2, paramString1, paramString2);
      sharedInstance = new FileTraceListener(paramFile1, paramBoolean1);
    }
    return sharedInstance;
  }

  public FileTraceListener(File paramFile, boolean paramBoolean)
  {
    this.append = paramBoolean;
    this.logFile = paramFile;
    init();
  }

  private void init()
  {
    try
    {
      this.fileTraceStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(this.logFile.getPath(), this.append)));
      SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
      print("Log started: " + localSimpleDateFormat.format(new Date()) + "\n");
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  public void print(String paramString)
  {
    try
    {
      if (this.logFile.length() >= Config.getIntProperty("deployment.max.output.file.size") * 1048576)
      {
        this.fileTraceStream.close();
        File localFile = File.createTempFile("javaws", ".temp", this.logFile.getParentFile());
        long l = this.logFile.length() / 4L;
        BufferedReader localBufferedReader = new BufferedReader(new FileReader(this.logFile));
        BufferedWriter localBufferedWriter = new BufferedWriter(new FileWriter(localFile));
        localBufferedReader.skip(l * 3L);
        int i;
        while ((i = localBufferedReader.read()) != -1)
          localBufferedWriter.write(i);
        localBufferedReader.close();
        localBufferedWriter.close();
        if (this.logFile.delete())
        {
          localFile.renameTo(this.logFile);
          init();
        }
      }
      this.fileTraceStream.print(paramString);
      this.fileTraceStream.flush();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  public void flush()
  {
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.trace.FileTraceListener
 * JD-Core Version:    0.6.0
 */