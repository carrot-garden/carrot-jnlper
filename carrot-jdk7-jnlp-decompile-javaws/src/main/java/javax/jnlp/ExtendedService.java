package javax.jnlp;

import java.io.File;
import java.io.IOException;

public abstract interface ExtendedService
{
  public abstract FileContents openFile(File paramFile)
    throws IOException;

  public abstract FileContents[] openFiles(File[] paramArrayOfFile)
    throws IOException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.ExtendedService
 * JD-Core Version:    0.6.0
 */