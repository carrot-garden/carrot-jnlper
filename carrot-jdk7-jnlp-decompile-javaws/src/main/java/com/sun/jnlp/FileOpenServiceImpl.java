package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.deploy.util.DeploySysAction;
import com.sun.deploy.util.DeploySysRun;
import java.io.File;
import java.io.IOException;
import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;

public final class FileOpenServiceImpl
  implements FileOpenService
{
  static FileOpenServiceImpl _sharedInstance = null;
  static FileSaveServiceImpl _fileSaveServiceImpl;
  private ApiDialog _apiDialog;

  private FileOpenServiceImpl(FileSaveServiceImpl paramFileSaveServiceImpl)
  {
    _fileSaveServiceImpl = paramFileSaveServiceImpl;
    this._apiDialog = new ApiDialog();
  }

  public static synchronized FileOpenService getInstance()
  {
    if (_sharedInstance == null)
      _sharedInstance = new FileOpenServiceImpl((FileSaveServiceImpl)FileSaveServiceImpl.getInstance());
    return _sharedInstance;
  }

  public FileContents openFileDialog(String paramString, String[] paramArrayOfString)
    throws IOException
  {
    if (!askUser())
      return null;
    return (FileContents)DeploySysRun.executePrivileged(new DeploySysAction(paramString, paramArrayOfString)
    {
      private final String val$pathHint;
      private final String[] val$extensions;

      public Object execute()
      {
        String str = this.val$pathHint;
        if (str == null)
          str = FileOpenServiceImpl._fileSaveServiceImpl.getLastPath();
        File[] arrayOfFile = ToolkitStore.getUI().showFileChooser(str, this.val$extensions, 8, false);
        if (arrayOfFile[0] != null)
          try
          {
            FileOpenServiceImpl._fileSaveServiceImpl.setLastPath(arrayOfFile[0].getPath());
            return new FileContentsImpl(arrayOfFile[0], FileSaveServiceImpl.computeMaxLength(arrayOfFile[0].length()));
          }
          catch (IOException localIOException)
          {
          }
        return null;
      }
    }
    , null);
  }

  public FileContents[] openMultiFileDialog(String paramString, String[] paramArrayOfString)
    throws IOException
  {
    if (!askUser())
      return null;
    return (FileContents[])(FileContents[])DeploySysRun.executePrivileged(new DeploySysAction(paramString, paramArrayOfString)
    {
      private final String val$pathHint;
      private final String[] val$extentions;

      public Object execute()
      {
        String str = this.val$pathHint;
        if (str == null)
          str = FileOpenServiceImpl._fileSaveServiceImpl.getLastPath();
        File[] arrayOfFile = ToolkitStore.getUI().showFileChooser(str, this.val$extentions, 8, true);
        if ((arrayOfFile != null) && (arrayOfFile.length > 0))
        {
          FileContents[] arrayOfFileContents = new FileContents[arrayOfFile.length];
          for (int i = 0; i < arrayOfFile.length; i++)
            try
            {
              arrayOfFileContents[i] = new FileContentsImpl(arrayOfFile[i], FileSaveServiceImpl.computeMaxLength(arrayOfFile[i].length()));
              FileOpenServiceImpl._fileSaveServiceImpl.setLastPath(arrayOfFile[i].getPath());
            }
            catch (IOException localIOException)
            {
            }
          return arrayOfFileContents;
        }
        return null;
      }
    }
    , null);
  }

  synchronized boolean askUser()
  {
    if (CheckServicePermission.hasFileAccessPermissions())
      return true;
    return this._apiDialog.askUser(ResourceManager.getString("api.file.open.title"), ResourceManager.getString("api.file.open.message"), ResourceManager.getString("api.file.open.always"));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.FileOpenServiceImpl
 * JD-Core Version:    0.6.0
 */