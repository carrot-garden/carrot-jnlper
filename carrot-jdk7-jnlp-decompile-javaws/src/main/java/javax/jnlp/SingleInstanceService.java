package javax.jnlp;

public abstract interface SingleInstanceService
{
  public abstract void addSingleInstanceListener(SingleInstanceListener paramSingleInstanceListener);

  public abstract void removeSingleInstanceListener(SingleInstanceListener paramSingleInstanceListener);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.SingleInstanceService
 * JD-Core Version:    0.6.0
 */