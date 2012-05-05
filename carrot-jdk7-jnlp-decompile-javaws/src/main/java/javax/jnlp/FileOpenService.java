package javax.jnlp;

import java.io.IOException;

public abstract interface FileOpenService
{
  public abstract FileContents openFileDialog(String paramString, String[] paramArrayOfString)
    throws IOException;

  public abstract FileContents[] openMultiFileDialog(String paramString, String[] paramArrayOfString)
    throws IOException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.FileOpenService
 * JD-Core Version:    0.6.0
 */