package com.sun.jnlp;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import sun.awt.image.URLImageSource;

class ImageCache
{
  private static Map images = null;

  static synchronized Image getImage(URL paramURL)
  {
    Image localImage = (Image)images.get(paramURL);
    if (localImage == null)
    {
      localImage = Toolkit.getDefaultToolkit().createImage(new URLImageSource(paramURL));
      images.put(paramURL, localImage);
    }
    return localImage;
  }

  public static void initialize()
  {
    images = new HashMap();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.ImageCache
 * JD-Core Version:    0.6.0
 */