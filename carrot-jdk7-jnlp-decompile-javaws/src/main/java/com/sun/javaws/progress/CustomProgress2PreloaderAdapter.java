package com.sun.javaws.progress;

import com.sun.applet2.Applet2Context;
import com.sun.applet2.Applet2Host;
import com.sun.applet2.preloader.CancelException;
import com.sun.applet2.preloader.Preloader;
import com.sun.applet2.preloader.event.AppletInitEvent;
import com.sun.applet2.preloader.event.DownloadErrorEvent;
import com.sun.applet2.preloader.event.DownloadEvent;
import com.sun.applet2.preloader.event.ErrorEvent;
import com.sun.applet2.preloader.event.PreloaderEvent;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import javax.jnlp.DownloadServiceListener;

public class CustomProgress2PreloaderAdapter extends Preloader
{
  private DownloadServiceListener _ds = null;
  private boolean seenUserDeclined = false;

  public CustomProgress2PreloaderAdapter(DownloadServiceListener paramDownloadServiceListener)
  {
    Trace.println("Adapt DownloadServiceListener [" + paramDownloadServiceListener.getClass() + "] to Preloader ", TraceLevel.PRELOADER);
    this._ds = paramDownloadServiceListener;
  }

  public CustomProgress2PreloaderAdapter(DownloadServiceListener paramDownloadServiceListener, Applet2Context paramApplet2Context)
  {
    super(paramApplet2Context);
    Trace.println("Wrap DownloadServiceListener [" + paramDownloadServiceListener.getClass() + "] as Preloader with appletcontext", TraceLevel.PRELOADER);
    this._ds = paramDownloadServiceListener;
  }

  public Object getOwner()
  {
    return null;
  }

  public boolean handleEvent(PreloaderEvent paramPreloaderEvent)
    throws CancelException
  {
    try
    {
      switch (paramPreloaderEvent.getType())
      {
      case 3:
        DownloadEvent localDownloadEvent = (DownloadEvent)paramPreloaderEvent;
        if (this._ds == null)
          break;
        switch (localDownloadEvent.getDownloadType())
        {
        case 0:
          this._ds.progress(localDownloadEvent.getURL(), localDownloadEvent.getVersion(), localDownloadEvent.getCompletedCount(), localDownloadEvent.getTotalCount(), localDownloadEvent.getOverallPercentage());
          break;
        case 1:
          this._ds.validating(localDownloadEvent.getURL(), localDownloadEvent.getVersion(), localDownloadEvent.getCompletedCount(), localDownloadEvent.getTotalCount(), localDownloadEvent.getOverallPercentage());
          break;
        case 2:
          this._ds.upgradingArchive(localDownloadEvent.getURL(), localDownloadEvent.getVersion(), localDownloadEvent.getOverallPercentage(), localDownloadEvent.getOverallPercentage());
          break;
        default:
          return false;
        }
        return true;
      case 6:
        ErrorEvent localErrorEvent = (ErrorEvent)paramPreloaderEvent;
        if ((this._ds != null) && ((localErrorEvent instanceof DownloadErrorEvent)))
          this._ds.downloadFailed(localErrorEvent.getLocation(), ((DownloadErrorEvent)localErrorEvent).getVersion());
        if ((this._ds == null) && (this.ctx != null) && (this.ctx.getHost() != null))
          this.ctx.getHost().showError(localErrorEvent.getValue(), localErrorEvent.getException(), this.seenUserDeclined);
        return true;
      case 7:
        this.seenUserDeclined = true;
        break;
      case 5:
        AppletInitEvent localAppletInitEvent = (AppletInitEvent)paramPreloaderEvent;
        if ((this.ctx == null) || (this.ctx.getHost() == null))
          break;
        switch (localAppletInitEvent.getSubtype())
        {
        case 3:
        case 6:
          this.ctx.getHost().showApplet();
        }
        break;
      case 4:
      default:
        Trace.println("Preloader Adapter skips event " + paramPreloaderEvent, TraceLevel.PRELOADER);
        return false;
      }
    }
    catch (Throwable localThrowable)
    {
      throw new CancelException("Got " + localThrowable + " from download service listener. Treat as Cancel.");
    }
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.progress.CustomProgress2PreloaderAdapter
 * JD-Core Version:    0.6.0
 */