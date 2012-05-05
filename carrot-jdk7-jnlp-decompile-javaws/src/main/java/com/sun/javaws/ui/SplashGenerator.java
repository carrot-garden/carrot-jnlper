package com.sun.javaws.ui;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.ImageLoader;
import com.sun.deploy.ui.ImageLoaderCallback;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

class SplashGenerator extends Thread
  implements ImageLoaderCallback
{
  private File _index;
  private File _dir;
  private final String _key;
  private final LaunchDesc _ld;
  private Properties _props = new Properties();

  public SplashGenerator(LaunchDesc paramLaunchDesc)
  {
    this._ld = paramLaunchDesc;
    this._dir = new File(Config.getSplashDir());
    this._key = this._ld.getSplashCanonicalHome().toString();
    String str = Config.getSplashIndex();
    this._index = new File(str);
    Config.setSplashCache();
    Config.get().storeIfNeeded();
    if (this._index.exists())
      try
      {
        FileInputStream localFileInputStream = new FileInputStream(this._index);
        if (localFileInputStream != null)
        {
          this._props.load(localFileInputStream);
          localFileInputStream.close();
        }
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
  }

  public boolean needsCustomSplash()
  {
    return !this._props.containsKey(this._key);
  }

  public void remove()
  {
    addSplashToCacheIndex(this._key, null);
  }

  public void run()
  {
    InformationDesc localInformationDesc = this._ld.getInformation();
    IconDesc[] arrayOfIconDesc = localInformationDesc.getIcons();
    if ((!this._dir.getParentFile().canWrite()) || ((this._dir.exists()) && (!this._dir.canWrite())) || ((this._index.exists()) && (!this._index.canWrite())))
      return;
    try
    {
      this._dir.mkdirs();
    }
    catch (Throwable localThrowable1)
    {
      splashError(localThrowable1);
    }
    try
    {
      this._index.createNewFile();
    }
    catch (Throwable localThrowable2)
    {
      splashError(localThrowable2);
    }
    IconDesc localIconDesc = localInformationDesc.getIconLocation(48, 4);
    if (localIconDesc == null)
      return;
    ImageLoader.getInstance().loadImage(localIconDesc.getLocation(), localIconDesc.getVersion(), this);
  }

  public void imageAvailable(URL paramURL, String paramString, Image paramImage, File paramFile)
  {
  }

  public void finalImageAvailable(URL paramURL, String paramString, Image paramImage, File paramFile)
  {
    try
    {
      create(paramImage, paramFile);
    }
    catch (Throwable localThrowable)
    {
      if ((localThrowable instanceof OutOfMemoryError))
        splashError(localThrowable);
      else
        Trace.ignored(localThrowable);
    }
  }

  public void create(Image paramImage, File paramFile)
  {
    InformationDesc localInformationDesc = this._ld.getInformation();
    int j = paramImage.getHeight(null);
    int i = paramImage.getWidth(null);
    if (paramFile != null)
      try
      {
        String str = paramFile.getCanonicalPath();
        addSplashToCacheIndex(this._key, str);
      }
      catch (Throwable localThrowable)
      {
        Trace.ignored(localThrowable);
      }
  }

  private void addSplashToCacheIndex(String paramString1, String paramString2)
  {
    if (paramString2 != null)
      this._props.setProperty(paramString1, paramString2);
    else if (this._props.containsKey(paramString1))
      this._props.remove(paramString1);
    File[] arrayOfFile = this._dir.listFiles();
    if (arrayOfFile == null)
      return;
    for (int i = 0; i < arrayOfFile.length; i++)
    {
      if (arrayOfFile[i].equals(this._index))
        continue;
      try
      {
        String str = arrayOfFile[i].getCanonicalPath();
        if (!this._props.containsValue(str))
          arrayOfFile[i].delete();
      }
      catch (IOException localIOException2)
      {
        splashError(localIOException2);
      }
    }
    try
    {
      FileOutputStream localFileOutputStream = new FileOutputStream(this._index);
      this._props.store(localFileOutputStream, "");
      localFileOutputStream.flush();
      localFileOutputStream.close();
    }
    catch (IOException localIOException1)
    {
      splashError(localIOException1);
    }
  }

  private void splashError(Throwable paramThrowable)
  {
    LaunchErrorDialog.show(null, paramThrowable, false);
    throw new Error(paramThrowable.toString());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.SplashGenerator
 * JD-Core Version:    0.6.0
 */