package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.PrintService;

public final class PrintServiceImpl
  implements PrintService
{
  private static PrintServiceImpl _sharedInstance = null;
  private static ApiDialog _apiDialog = new ApiDialog();
  private PageFormat _pageFormat = null;

  public static synchronized PrintServiceImpl getInstance()
  {
    if (_sharedInstance == null)
      _sharedInstance = new PrintServiceImpl();
    return _sharedInstance;
  }

  public PageFormat getDefaultPage()
  {
    return (PageFormat)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        PrinterJob localPrinterJob = PrinterJob.getPrinterJob();
        if (localPrinterJob != null)
          return localPrinterJob.defaultPage();
        return null;
      }
    });
  }

  public PageFormat showPageFormatDialog(PageFormat paramPageFormat)
  {
    return (PageFormat)AccessController.doPrivileged(new PrivilegedAction(paramPageFormat)
    {
      private final PageFormat val$page;

      public Object run()
      {
        PrinterJob localPrinterJob = PrinterJob.getPrinterJob();
        if (localPrinterJob != null)
        {
          PrintServiceImpl.access$002(PrintServiceImpl.this, localPrinterJob.pageDialog(this.val$page));
          return PrintServiceImpl.this._pageFormat;
        }
        return null;
      }
    });
  }

  public synchronized boolean print(Pageable paramPageable)
  {
    return doPrinting(null, paramPageable);
  }

  public synchronized boolean print(Printable paramPrintable)
  {
    return doPrinting(paramPrintable, null);
  }

  private boolean doPrinting(Printable paramPrintable, Pageable paramPageable)
  {
    if (!askUser())
      return false;
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(paramPageable, paramPrintable)
    {
      private final Pageable val$document;
      private final Printable val$painter;

      public Object run()
      {
        PrinterJob localPrinterJob = PrinterJob.getPrinterJob();
        if (localPrinterJob == null)
          return Boolean.FALSE;
        if (this.val$document != null)
          localPrinterJob.setPageable(this.val$document);
        else if (PrintServiceImpl.this._pageFormat == null)
          localPrinterJob.setPrintable(this.val$painter);
        else
          localPrinterJob.setPrintable(this.val$painter, PrintServiceImpl.this._pageFormat);
        if (localPrinterJob.printDialog())
        {
          Thread localThread = new Thread(new Runnable(localPrinterJob)
          {
            private final PrinterJob val$sysPrinterJob;

            public void run()
            {
              try
              {
                this.val$sysPrinterJob.print();
              }
              catch (PrinterException localPrinterException)
              {
                Trace.ignoredException(localPrinterException);
              }
            }
          });
          localThread.start();
          return Boolean.TRUE;
        }
        return Boolean.FALSE;
      }
    });
    return localBoolean.booleanValue();
  }

  private synchronized boolean askUser()
  {
    if (CheckServicePermission.hasPrintAccessPermissions())
      return true;
    return requestPrintPermission();
  }

  public static boolean requestPrintPermission()
  {
    return _apiDialog.askUser(ResourceManager.getString("api.print.title"), ResourceManager.getString("api.print.message"), ResourceManager.getString("api.print.always"));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.PrintServiceImpl
 * JD-Core Version:    0.6.0
 */