package com.sun.jnlp;

import com.sun.applet2.preloader.Preloader;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.LaunchDesc;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;
import javax.jnlp.BasicService;
import javax.jnlp.ClipboardService;
import javax.jnlp.DownloadService;
import javax.jnlp.DownloadService2;
import javax.jnlp.ExtendedService;
import javax.jnlp.ExtensionInstallerService;
import javax.jnlp.FileOpenService;
import javax.jnlp.FileSaveService;
import javax.jnlp.IntegrationService;
import javax.jnlp.PersistenceService;
import javax.jnlp.PrintService;
import javax.jnlp.SingleInstanceService;

public abstract interface JNLPClassLoaderIf
{
  public abstract void quiescenceRequested(Thread paramThread, boolean paramBoolean);

  public abstract void quiescenceCancelled(boolean paramBoolean);

  public abstract URL getResource(String paramString);

  public abstract URL findResource(String paramString);

  public abstract LaunchDesc getLaunchDesc();

  public abstract JARDesc getJarDescFromURL(URL paramURL);

  public abstract int getDefaultSecurityModel();

  public abstract JarFile getJarFile(URL paramURL)
    throws IOException;

  public abstract void addResource(URL paramURL, String paramString1, String paramString2);

  public abstract BasicService getBasicService();

  public abstract FileOpenService getFileOpenService();

  public abstract FileSaveService getFileSaveService();

  public abstract ExtensionInstallerService getExtensionInstallerService();

  public abstract DownloadService getDownloadService();

  public abstract ClipboardService getClipboardService();

  public abstract PrintService getPrintService();

  public abstract PersistenceService getPersistenceService();

  public abstract ExtendedService getExtendedService();

  public abstract SingleInstanceService getSingleInstanceService();

  public abstract IntegrationService getIntegrationService();

  public abstract DownloadService2 getDownloadService2();

  public abstract Preloader getPreloader();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.JNLPClassLoaderIf
 * JD-Core Version:    0.6.0
 */