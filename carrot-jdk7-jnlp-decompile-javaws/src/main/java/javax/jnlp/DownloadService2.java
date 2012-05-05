package javax.jnlp;

import com.sun.jnlp.DownloadService2Impl;
import com.sun.jnlp.DownloadService2Impl.ResourceSpecAccess;
import java.io.IOException;

public abstract interface DownloadService2
{
  public static final int ALL = 0;
  public static final int APPLICATION = 1;
  public static final int APPLET = 2;
  public static final int EXTENSION = 3;
  public static final int JAR = 4;
  public static final int IMAGE = 5;
  public static final int CLASS = 6;

  public abstract ResourceSpec[] getCachedResources(ResourceSpec paramResourceSpec);

  public abstract ResourceSpec[] getUpdateAvailableResources(ResourceSpec paramResourceSpec)
    throws IOException;

  public static class ResourceSpec
  {
    private String url;
    private String version;
    private int type;
    private long size;
    private long lastModified;
    private long expirationDate;

    public ResourceSpec(String paramString1, String paramString2, int paramInt)
    {
      this.url = paramString1;
      this.version = paramString2;
      this.type = paramInt;
      this.size = -1L;
    }

    public String getUrl()
    {
      return this.url;
    }

    public String getVersion()
    {
      return this.version;
    }

    public int getType()
    {
      return this.type;
    }

    public long getSize()
    {
      return this.size;
    }

    public long getLastModified()
    {
      return this.lastModified;
    }

    public long getExpirationDate()
    {
      return this.expirationDate;
    }

    static
    {
      DownloadService2Impl.setResourceSpecAccess(new DownloadService2Impl.ResourceSpecAccess()
      {
        public void setSize(DownloadService2.ResourceSpec paramResourceSpec, long paramLong)
        {
          DownloadService2.ResourceSpec.access$002(paramResourceSpec, paramLong);
        }

        public void setLastModified(DownloadService2.ResourceSpec paramResourceSpec, long paramLong)
        {
          DownloadService2.ResourceSpec.access$102(paramResourceSpec, paramLong);
        }

        public void setExpirationDate(DownloadService2.ResourceSpec paramResourceSpec, long paramLong)
        {
          DownloadService2.ResourceSpec.access$202(paramResourceSpec, paramLong);
        }
      });
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     javax.jnlp.DownloadService2
 * JD-Core Version:    0.6.0
 */