package com.sun.deploy.net;

import com.sun.applet2.preloader.CancelException;

public abstract interface HttpDownloadListener
{
  public abstract boolean downloadProgress(int paramInt1, int paramInt2)
    throws CancelException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.HttpDownloadListener
 * JD-Core Version:    0.6.0
 */