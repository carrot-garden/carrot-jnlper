package com.sun.deploy.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract interface HttpDownload
{
  public abstract MessageHeader download(int paramInt1, URL paramURL, InputStream paramInputStream, String paramString, File paramFile, HttpDownloadListener paramHttpDownloadListener, int paramInt2, boolean paramBoolean)
    throws CanceledDownloadException, IOException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.HttpDownload
 * JD-Core Version:    0.6.0
 */