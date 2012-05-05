package javax.jnlp;

import java.io.IOException;
import java.io.InputStream;

public abstract interface FileSaveService
{
  public abstract FileContents saveFileDialog(String paramString1, String[] paramArrayOfString, InputStream paramInputStream, String paramString2)
    throws IOException;

  public abstract FileContents saveAsFileDialog(String paramString, String[] paramArrayOfString, FileContents paramFileContents)
    throws IOException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.FileSaveService
 * JD-Core Version:    0.6.0
 */