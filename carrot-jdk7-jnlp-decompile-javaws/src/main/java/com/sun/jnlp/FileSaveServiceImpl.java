package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import com.sun.deploy.util.DeploySysAction;
import com.sun.deploy.util.DeploySysRun;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.jnlp.FileContents;
import javax.jnlp.FileSaveService;

public final class FileSaveServiceImpl
  implements FileSaveService
{
  static FileSaveServiceImpl _sharedInstance = null;
  private ApiDialog _apiDialog = new ApiDialog();
  private String _lastPath;

  public static synchronized FileSaveService getInstance()
  {
    if (_sharedInstance == null)
      _sharedInstance = new FileSaveServiceImpl();
    return _sharedInstance;
  }

  String getLastPath()
  {
    return this._lastPath;
  }

  void setLastPath(String paramString)
  {
    this._lastPath = paramString;
  }

  public FileContents saveAsFileDialog(String paramString, String[] paramArrayOfString, FileContents paramFileContents)
    throws IOException
  {
    return saveFileDialog(paramString, paramArrayOfString, paramFileContents.getInputStream(), paramFileContents.getName());
  }

  public FileContents saveFileDialog(String paramString1, String[] paramArrayOfString, InputStream paramInputStream, String paramString2)
    throws IOException
  {
    if (!askUser())
      return null;
    Object localObject = DeploySysRun.executePrivileged(new DeploySysAction(paramString1, paramArrayOfString, paramInputStream)
    {
      private final String val$pathHint;
      private final String[] val$extensions;
      private final InputStream val$stream;

      public Object execute()
      {
        String str = this.val$pathHint;
        if (str == null)
          str = FileSaveServiceImpl.this.getLastPath();
        File[] arrayOfFile = ToolkitStore.getUI().showFileChooser(str, this.val$extensions, 9, false);
        if (arrayOfFile[0] != null)
        {
          if (!FileSaveServiceImpl.fileChk(arrayOfFile[0]))
            return null;
          try
          {
            byte[] arrayOfByte = new byte[8192];
            BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(arrayOfFile[0]));
            BufferedInputStream localBufferedInputStream = new BufferedInputStream(this.val$stream);
            for (int i = localBufferedInputStream.read(arrayOfByte); i != -1; i = localBufferedInputStream.read(arrayOfByte))
              localBufferedOutputStream.write(arrayOfByte, 0, i);
            localBufferedOutputStream.close();
            FileSaveServiceImpl.this.setLastPath(arrayOfFile[0].getPath());
            return new FileContentsImpl(arrayOfFile[0], FileSaveServiceImpl.computeMaxLength(arrayOfFile[0].length()));
          }
          catch (IOException localIOException)
          {
            Trace.ignored(localIOException);
            return localIOException;
          }
        }
        return null;
      }
    }
    , null);
    if ((localObject instanceof IOException))
      throw ((IOException)localObject);
    return (FileContents)localObject;
  }

  synchronized boolean askUser()
  {
    if (CheckServicePermission.hasFileAccessPermissions())
      return true;
    return this._apiDialog.askUser(ResourceManager.getString("api.file.save.title"), ResourceManager.getString("api.file.save.message"), ResourceManager.getString("api.file.save.always"));
  }

  static long computeMaxLength(long paramLong)
  {
    return paramLong * 3L;
  }

  static boolean fileChk(File paramFile)
  {
    if (paramFile.exists())
    {
      String str1 = ResourceManager.getString("api.file.save.fileExist", paramFile.getPath());
      String str2 = ResourceManager.getMessage("api.file.save.fileExistTitle");
      String str3 = ResourceManager.getString("common.ok_btn");
      String str4 = ResourceManager.getString("common.cancel_btn");
      ToolkitStore.getUI();
      int i = ToolkitStore.getUI().showMessageDialog(null, null, 3, str2, null, str1, null, str3, str4, null);
      ToolkitStore.getUI();
      return i == 0;
    }
    return true;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.FileSaveServiceImpl
 * JD-Core Version:    0.6.0
 */