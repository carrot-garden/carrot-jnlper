package javax.jnlp;

public abstract interface IntegrationService
{
  public abstract boolean requestShortcut(boolean paramBoolean1, boolean paramBoolean2, String paramString);

  public abstract boolean hasDesktopShortcut();

  public abstract boolean hasMenuShortcut();

  public abstract boolean removeShortcuts();

  public abstract boolean requestAssociation(String paramString, String[] paramArrayOfString);

  public abstract boolean hasAssociation(String paramString, String[] paramArrayOfString);

  public abstract boolean removeAssociation(String paramString, String[] paramArrayOfString);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.IntegrationService
 * JD-Core Version:    0.6.0
 */