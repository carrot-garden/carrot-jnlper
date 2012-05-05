package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.ClipboardService;

public final class ClipboardServiceImpl
  implements ClipboardService
{
  private static ClipboardServiceImpl _sharedInstance = null;
  private Clipboard _sysClipboard = null;
  private ApiDialog _readDialog = null;
  private ApiDialog _writeDialog = null;

  private ClipboardServiceImpl()
  {
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    if (localToolkit != null)
      this._sysClipboard = localToolkit.getSystemClipboard();
  }

  public static synchronized ClipboardServiceImpl getInstance()
  {
    if (_sharedInstance == null)
      _sharedInstance = new ClipboardServiceImpl();
    return _sharedInstance;
  }

  public Transferable getContents()
  {
    if (!askUser(false))
      return null;
    return (Transferable)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return ClipboardServiceImpl.this._sysClipboard.getContents(null);
      }
    });
  }

  public void setContents(Transferable paramTransferable)
  {
    if (!askUser(true))
      return;
    AccessController.doPrivileged(new PrivilegedAction(paramTransferable)
    {
      private final Transferable val$contents;

      public Object run()
      {
        if (this.val$contents != null)
        {
          DataFlavor[] arrayOfDataFlavor = this.val$contents.getTransferDataFlavors();
          if ((arrayOfDataFlavor == null) || (arrayOfDataFlavor[0] == null))
            return null;
          try
          {
            if (this.val$contents.getTransferData(arrayOfDataFlavor[0]) == null)
              return null;
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
          catch (UnsupportedFlavorException localUnsupportedFlavorException)
          {
            Trace.ignoredException(localUnsupportedFlavorException);
          }
        }
        ClipboardServiceImpl.this._sysClipboard.setContents(this.val$contents, null);
        return null;
      }
    });
  }

  private synchronized boolean askUser(boolean paramBoolean)
  {
    if (!hasClipboard())
      return false;
    if (CheckServicePermission.hasClipboardPermissions())
      return true;
    String str3 = ResourceManager.getString("api.clipboard.title");
    if (paramBoolean)
    {
      str1 = ResourceManager.getString("api.clipboard.message.write");
      str2 = ResourceManager.getString("api.clipboard.write.always");
      return this._writeDialog.askUser(str3, str1, str2);
    }
    String str1 = ResourceManager.getString("api.clipboard.message.read");
    String str2 = ResourceManager.getString("api.clipboard.read.always");
    return this._readDialog.askUser(str3, str1, str2);
  }

  private boolean hasClipboard()
  {
    return this._sysClipboard != null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.ClipboardServiceImpl
 * JD-Core Version:    0.6.0
 */