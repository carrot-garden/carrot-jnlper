package com.sun.javaws;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

public abstract class OperaSupport
{
  protected static final String OPERA_PREFERENCES = "opera.ini";
  protected static final String OPERA_6_PREFERENCES = "opera6.ini";
  protected boolean useDefault;
  private static final String INSTALL_SECTION = "INSTALL";
  private static final String VERSION_KEY = "OVER";
  private static final float OPERA_2_PREFERENCE_VERSION = 5.0F;
  private static final float LAST_TESTED_OPERA_PREFERENCE_VERSION = 7.11F;
  private static final String FILE_TYPES_SECTION_INFO = "File Types Section Info";
  private static final String FILE_TYPES_VERSION_KEY = "Version";
  private static final String FILE_TYPES = "File Types";
  private static final String FILE_TYPES_KEY = "application/x-java-jnlp-file";
  private static final String FILE_TYPES_VALUE = "{0},{1},,,jnlp,|";
  private static final String EXPLICIT_PATH = "3";
  private static final String IMPLICIT_PATH = "4";
  private static final String FILE_TYPES_EXTENSION = "File Types Extension";
  private static final String FILE_TYPES_EXTENSION_KEY = "application/x-java-jnlp-file";
  private static final String FILE_TYPES_EXTENSION_VALUE = ",0";

  public abstract boolean isInstalled();

  public abstract void enableJnlp(File paramFile, boolean paramBoolean);

  protected void enableJnlp(OperaPreferences paramOperaPreferences, File paramFile1, File paramFile2, boolean paramBoolean)
    throws IOException
  {
    if (paramOperaPreferences == null)
      paramOperaPreferences = getPreferences(paramFile1);
    if (paramOperaPreferences != null)
    {
      float f = 5.0F;
      String str = paramOperaPreferences.get("INSTALL", "OVER");
      if (str != null)
        try
        {
          f = Float.parseFloat(str.trim());
        }
        catch (NumberFormatException localNumberFormatException)
        {
          Trace.println("Unable to determine Opera version from the preference file; assuming 5.0 or higher.", TraceLevel.BASIC);
        }
      if (f < 5.0F)
      {
        paramOperaPreferences.put("File Types Section Info", "Version", "1");
      }
      else if (!paramOperaPreferences.containsKey("File Types Section Info", "Version"))
      {
        if (f > 7.11F)
          Trace.println("Setting '[File Types Section Info]Version=2' in the Opera preference file.", TraceLevel.BASIC);
        paramOperaPreferences.put("File Types Section Info", "Version", "2");
      }
      if ((paramBoolean == true) || (!paramOperaPreferences.containsKey("File Types", "application/x-java-jnlp-file")))
      {
        Object[] arrayOfObject = { null, null };
        if ((f < 5.0F) || (!this.useDefault))
        {
          arrayOfObject[0] = "3";
          try
          {
            arrayOfObject[1] = paramFile2.getCanonicalPath();
          }
          catch (IOException localIOException)
          {
            arrayOfObject[1] = paramFile2.getAbsolutePath();
          }
        }
        else
        {
          arrayOfObject[0] = "4";
          arrayOfObject[1] = "";
        }
        paramOperaPreferences.put("File Types", "application/x-java-jnlp-file", MessageFormat.format("{0},{1},,,jnlp,|", arrayOfObject));
      }
      if ((f >= 5.0F) && (!paramOperaPreferences.containsKey("File Types Extension", "application/x-java-jnlp-file")))
        paramOperaPreferences.put("File Types Extension", "application/x-java-jnlp-file", ",0");
      paramOperaPreferences.store(new FileOutputStream(paramFile1));
    }
  }

  protected OperaPreferences getPreferences(File paramFile)
    throws IOException
  {
    OperaPreferences localOperaPreferences = null;
    if (paramFile.exists())
    {
      if (paramFile.canRead())
      {
        if (paramFile.canWrite())
        {
          localOperaPreferences = new OperaPreferences();
          localOperaPreferences.load(new FileInputStream(paramFile));
        }
        else
        {
          Trace.println("No write access to the Opera preference file (" + paramFile.getAbsolutePath() + ").", TraceLevel.BASIC);
        }
      }
      else
        Trace.println("No read access to the Opera preference file (" + paramFile.getAbsolutePath() + ").", TraceLevel.BASIC);
    }
    else
      Trace.println("The Opera preference file (" + paramFile.getAbsolutePath() + ") does not exist.", TraceLevel.BASIC);
    return localOperaPreferences;
  }

  protected OperaSupport(boolean paramBoolean)
  {
    this.useDefault = paramBoolean;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.OperaSupport
 * JD-Core Version:    0.6.0
 */