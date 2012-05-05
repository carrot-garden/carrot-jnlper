package com.sun.applet2.preloader.event;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.net.URL;

public class DownloadEvent extends PreloaderEvent
{
  public static final int DOWNLOADING = 0;
  public static final int VERIFYING = 1;
  public static final int PATCHING = 2;
  private long completed;
  private long total;
  private int percent;
  private URL url;
  private String version;
  private String resourceLabel;
  private int downloadtype;
  protected boolean isStart = false;
  protected boolean isComplete = false;
  protected boolean isExplicit = false;
  private final String[] types = { "load", "verify", "patch" };

  public DownloadEvent(int paramInt1, URL paramURL, String paramString1, String paramString2, long paramLong1, long paramLong2, int paramInt2)
  {
    super(3);
    this.downloadtype = paramInt1;
    this.url = paramURL;
    this.version = paramString1;
    this.resourceLabel = paramString2;
    this.completed = paramLong1;
    this.total = paramLong2;
    this.percent = paramInt2;
  }

  public void normalize(int paramInt)
  {
    if ((paramInt >= 100) || (this.percent >= 100))
    {
      this.percent = 100;
      return;
    }
    if (paramInt < 0)
    {
      Trace.println("warning: progress baseline could not be negative!", TraceLevel.PRELOADER);
      return;
    }
    if (this.percent < paramInt)
    {
      Trace.println("warning: progress baseline above the progress value! " + this.percent + " < " + paramInt, TraceLevel.PRELOADER);
      this.percent = 0;
      return;
    }
    this.percent = (100 * (this.percent - paramInt) / (100 - paramInt));
    if (this.percent == 0)
      this.percent = 1;
  }

  public URL getURL()
  {
    return this.url;
  }

  public String getVersion()
  {
    return this.version;
  }

  public String getResourceLabel()
  {
    return this.resourceLabel;
  }

  public int getDownloadType()
  {
    return this.downloadtype;
  }

  public long getCompletedCount()
  {
    return this.completed;
  }

  public long getTotalCount()
  {
    return this.total;
  }

  public int getOverallPercentage()
  {
    return this.percent;
  }

  public boolean isStart()
  {
    return this.isStart;
  }

  public boolean isComplete()
  {
    return this.isComplete;
  }

  public boolean isExplicit()
  {
    return this.isExplicit;
  }

  public void sendExplicitEvent(boolean paramBoolean)
  {
    this.isExplicit = paramBoolean;
  }

  public String toString()
  {
    return "DownloadEvent[type=" + this.types[this.downloadtype] + ",loaded=" + getCompletedCount() + ", total=" + getTotalCount() + ", percent=" + getOverallPercentage() + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.applet2.preloader.event.DownloadEvent
 * JD-Core Version:    0.6.0
 */