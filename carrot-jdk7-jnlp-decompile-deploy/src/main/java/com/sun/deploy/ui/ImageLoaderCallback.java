package com.sun.deploy.ui;

import java.awt.Image;
import java.io.File;
import java.net.URL;

public abstract interface ImageLoaderCallback
{
  public abstract void imageAvailable(URL paramURL, String paramString, Image paramImage, File paramFile);

  public abstract void finalImageAvailable(URL paramURL, String paramString, Image paramImage, File paramFile);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.ImageLoaderCallback
 * JD-Core Version:    0.6.0
 */