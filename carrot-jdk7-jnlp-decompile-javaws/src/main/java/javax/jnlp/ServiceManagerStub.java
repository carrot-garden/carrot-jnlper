package javax.jnlp;

public abstract interface ServiceManagerStub
{
  public abstract Object lookup(String paramString)
    throws UnavailableServiceException;

  public abstract String[] getServiceNames();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.ServiceManagerStub
 * JD-Core Version:    0.6.0
 */