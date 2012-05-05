package javax.jnlp;

import java.awt.datatransfer.Transferable;

public abstract interface ClipboardService
{
  public abstract Transferable getContents();

  public abstract void setContents(Transferable paramTransferable);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.ClipboardService
 * JD-Core Version:    0.6.0
 */