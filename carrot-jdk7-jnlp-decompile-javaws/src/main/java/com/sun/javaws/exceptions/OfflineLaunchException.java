package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class OfflineLaunchException extends JNLPException
{
  private int type;
  public static final int MISSING_RESOURCE = 0;
  public static final int NO_OFFLINE_ALLOWED = 1;

  public OfflineLaunchException()
  {
    super(ResourceManager.getString("launch.error.category.download"));
  }

  public OfflineLaunchException(int paramInt)
  {
    super(ResourceManager.getString("launch.error.category.download"));
    this.type = paramInt;
  }

  public String getRealMessage()
  {
    String str = ResourceManager.getString("launch.error.offlinemissingresource");
    if (this.type == 0)
      str = ResourceManager.getString("launch.error.offlinemissingresource");
    else if (this.type == 1)
      str = ResourceManager.getString("launch.error.offline.noofflineallowed");
    return str;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.OfflineLaunchException
 * JD-Core Version:    0.6.0
 */