package com.sun.javaws.exceptions;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;

public class CacheAccessException extends JNLPException
{
  private String _message;

  public CacheAccessException(boolean paramBoolean1, boolean paramBoolean2)
  {
    super(ResourceManager.getString("launch.error.category.config"));
    if (paramBoolean2)
    {
      if (paramBoolean1)
        this._message = ResourceManager.getString("launch.error.disabled.system.cache");
      else
        this._message = ResourceManager.getString("launch.error.disabled.user.cache");
    }
    else if (Cache.exists())
    {
      if (paramBoolean1)
        this._message = ResourceManager.getString("launch.error.cant.access.system.cache");
      else
        this._message = ResourceManager.getString("launch.error.cant.access.user.cache");
    }
    else
    {
      String str = ResourceManager.getString(paramBoolean1 ? "cert.dialog.system.level" : "cert.dialog.user.level");
      if ((paramBoolean1) && (Config.getSystemCacheDirectory() == null))
        this._message = ResourceManager.getString("launch.error.nocache.config");
      else
        this._message = ResourceManager.getString("launch.error.nocache", str);
    }
  }

  public CacheAccessException(boolean paramBoolean)
  {
    this(paramBoolean, false);
  }

  public String getRealMessage()
  {
    return this._message;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.CacheAccessException
 * JD-Core Version:    0.6.0
 */