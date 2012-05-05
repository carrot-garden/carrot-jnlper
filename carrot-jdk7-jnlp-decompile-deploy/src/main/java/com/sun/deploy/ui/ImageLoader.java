package com.sun.deploy.ui;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.URLUtil;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

public class ImageLoader
  implements Runnable
{
  private Component _component;
  private static ImageLoader _instance = null;
  private final Object _imageLoadingLock = new Object();
  private boolean _running = false;
  private ArrayList _toLoad = new ArrayList();

  public static ImageLoader getInstance()
  {
    if (_instance == null)
      _instance = new ImageLoader();
    return _instance;
  }

  private Component getComponent()
  {
    if (this._component == null)
      synchronized (this)
      {
        if (this._component == null)
          this._component = new Component()
          {
          };
      }
    return this._component;
  }

  public Image loadImage(String paramString)
    throws IOException
  {
    Image localImage = Toolkit.getDefaultToolkit().createImage(paramString);
    if (localImage != null)
    {
      Component localComponent = getComponent();
      MediaTracker localMediaTracker = new MediaTracker(localComponent);
      localMediaTracker.addImage(localImage, 0);
      try
      {
        localMediaTracker.waitForID(0, 5000L);
      }
      catch (InterruptedException localInterruptedException)
      {
        throw new IOException("Failed to load");
      }
      return localImage;
    }
    return null;
  }

  public Image loadImage(URL paramURL)
    throws IOException
  {
    Image localImage = Toolkit.getDefaultToolkit().createImage(paramURL);
    if (localImage != null)
    {
      Component localComponent = getComponent();
      MediaTracker localMediaTracker = new MediaTracker(localComponent);
      localMediaTracker.addImage(localImage, 0);
      try
      {
        localMediaTracker.waitForID(0, 5000L);
      }
      catch (InterruptedException localInterruptedException)
      {
        throw new IOException("Failed to load");
      }
      return localImage;
    }
    return null;
  }

  public void loadImage(URL paramURL, String paramString, ImageLoaderCallback paramImageLoaderCallback)
  {
    loadImage(paramURL, paramString, paramImageLoaderCallback, false);
  }

  public void loadImage(URL paramURL, String paramString, ImageLoaderCallback paramImageLoaderCallback, boolean paramBoolean)
  {
    int i = 0;
    synchronized (this._imageLoadingLock)
    {
      if (!this._running)
      {
        this._running = true;
        i = 1;
      }
      this._toLoad.add(new LoadEntry(paramURL, paramString, paramImageLoaderCallback, paramBoolean));
    }
    if (i != 0)
      new Thread(this).start();
  }

  public void loadImage(URL paramURL, ImageLoaderCallback paramImageLoaderCallback)
  {
    loadImage(paramURL, paramImageLoaderCallback, false);
  }

  public void loadImage(URL paramURL, ImageLoaderCallback paramImageLoaderCallback, boolean paramBoolean)
  {
    int i = 0;
    synchronized (this._imageLoadingLock)
    {
      if (!this._running)
      {
        this._running = true;
        i = 1;
      }
      this._toLoad.add(new LoadEntry(paramURL, paramImageLoaderCallback, paramBoolean));
    }
    if (i != 0)
      new Thread(this).start();
  }

  public void run()
  {
    int i = 0;
    while (i == 0)
    {
      LoadEntry localLoadEntry = null;
      synchronized (this._imageLoadingLock)
      {
        if (this._toLoad.size() > 0)
        {
          localLoadEntry = (LoadEntry)this._toLoad.remove(0);
        }
        else
        {
          i = 1;
          this._running = false;
        }
      }
      if (i == 0)
        try
        {
          ??? = null;
          File localFile = null;
          URL localURL = localLoadEntry.url;
          if (localURL == null)
          {
            localFile = DownloadEngine.getCachedFile(localLoadEntry.iconRef, localLoadEntry.iconVer);
            if (localFile != null)
              localURL = URLUtil.fileToURL(localFile);
          }
          if (localURL != null)
            ??? = loadImage(localURL);
          if (??? != null)
          {
            if (localLoadEntry.useCached)
            {
              publish(localLoadEntry, (Image)???, localFile, true);
            }
            else
            {
              publish(localLoadEntry, (Image)???, localFile, false);
              if (localLoadEntry.iconRef != null)
                new DelayedImageLoader(localLoadEntry, (Image)???).start();
            }
          }
          else if (localLoadEntry.iconRef != null)
            new DelayedImageLoader(localLoadEntry, (Image)???).start();
        }
        catch (MalformedURLException localMalformedURLException)
        {
          Trace.ignoredException(localMalformedURLException);
        }
        catch (IOException localIOException)
        {
          Trace.ignoredException(localIOException);
        }
    }
  }

  private static void publish(LoadEntry paramLoadEntry, Image paramImage, File paramFile, boolean paramBoolean)
  {
    URL localURL = paramLoadEntry.iconRef;
    String str = paramLoadEntry.iconVer;
    ImageLoaderCallback localImageLoaderCallback = paramLoadEntry.cb;
    SwingUtilities.invokeLater(new Runnable(paramBoolean, localImageLoaderCallback, localURL, str, paramImage, paramFile)
    {
      private final boolean val$isComplete;
      private final ImageLoaderCallback val$cb;
      private final URL val$url;
      private final String val$version;
      private final Image val$image;
      private final File val$file;

      public void run()
      {
        if (this.val$isComplete)
          this.val$cb.finalImageAvailable(this.val$url, this.val$version, this.val$image, this.val$file);
        else
          this.val$cb.imageAvailable(this.val$url, this.val$version, this.val$image, this.val$file);
      }
    });
  }

  private class DelayedImageLoader extends Thread
  {
    private ImageLoader.LoadEntry _entry;
    private Image _image;

    public DelayedImageLoader(ImageLoader.LoadEntry paramImage, Image arg3)
    {
      this._entry = paramImage;
      Object localObject;
      this._image = localObject;
    }

    public void run()
    {
      try
      {
        if (Cache.isCacheEnabled())
        {
          File localFile = DownloadEngine.getUpdatedFile(this._entry.iconRef, this._entry.iconVer);
          if (localFile != null)
          {
            this._image = ImageLoader.this.loadImage(localFile.getPath());
            ImageLoader.access$000(this._entry, this._image, localFile, true);
          }
        }
        else
        {
          this._image = ImageLoader.this.loadImage(this._entry.iconRef);
          ImageLoader.access$000(this._entry, this._image, null, true);
        }
      }
      catch (MalformedURLException localMalformedURLException)
      {
        Trace.ignoredException(localMalformedURLException);
      }
      catch (IOException localIOException)
      {
        Trace.ignoredException(localIOException);
      }
    }
  }

  private class LoadEntry
  {
    public URL url;
    public URL iconRef;
    public String iconVer;
    public ImageLoaderCallback cb;
    public boolean useCached;

    public LoadEntry(URL paramString, String paramImageLoaderCallback, ImageLoaderCallback paramBoolean, boolean arg5)
    {
      this.cb = paramBoolean;
      this.url = null;
      this.iconRef = paramString;
      this.iconVer = paramImageLoaderCallback;
      boolean bool;
      this.useCached = bool;
    }

    public LoadEntry(URL paramImageLoaderCallback, ImageLoaderCallback paramBoolean, boolean arg4)
    {
      this.url = paramImageLoaderCallback;
      this.cb = paramBoolean;
      this.iconRef = null;
      this.iconVer = null;
      boolean bool;
      this.useCached = bool;
    }

    public String toString()
    {
      return "LoadEntry:\n  url: " + this.url + "\n" + "  iconRef " + this.iconRef + "\n" + "  iconVer: " + this.iconVer + "  useCached: " + this.useCached;
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.ImageLoader
 * JD-Core Version:    0.6.0
 */