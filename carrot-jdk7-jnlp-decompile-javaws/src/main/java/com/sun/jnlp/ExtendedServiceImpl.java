package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.JnlpxArgs;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import javax.jnlp.ExtendedService;
import javax.jnlp.FileContents;

public final class ExtendedServiceImpl
  implements ExtendedService
{
  private static ExtendedServiceImpl _sharedInstance = null;
  private static int DEFAULT_FILESIZE = 2147483647;

  public static synchronized ExtendedServiceImpl getInstance()
  {
    if (_sharedInstance == null)
      _sharedInstance = new ExtendedServiceImpl();
    return _sharedInstance;
  }

  public FileContents openFile(File paramFile)
    throws IOException
  {
    if (paramFile == null)
      return null;
    File localFile = new File(paramFile.getPath());
    if ((!CheckServicePermission.hasFileAccessPermissions(localFile.toString())) && (!JnlpxArgs.getFileReadWriteList().contains(localFile.toString())) && (!askUser(localFile.getPath())))
      return null;
    Object localObject = AccessController.doPrivileged(new PrivilegedAction(localFile)
    {
      private final File val$file;

      public Object run()
      {
        try
        {
          return new FileContentsImpl(this.val$file, ExtendedServiceImpl.DEFAULT_FILESIZE);
        }
        catch (IOException localIOException)
        {
        }
        return localIOException;
      }
    });
    if ((localObject instanceof IOException))
      throw ((IOException)localObject);
    return (FileContents)localObject;
  }

  synchronized boolean askUser(String paramString)
  {
    if (CheckServicePermission.hasFileAccessPermissions())
      return true;
    ApiDialog localApiDialog = new ApiDialog();
    String str1 = ResourceManager.getString("api.extended.open.title");
    String str2 = ResourceManager.getString("api.extended.open.message");
    String str3 = ResourceManager.getString("api.extended.open.label");
    return localApiDialog.askUser(str1, str2, null, str3, paramString, false);
  }

  public FileContents[] openFiles(File[] paramArrayOfFile)
    throws IOException
  {
    if ((paramArrayOfFile == null) || (paramArrayOfFile.length <= 0))
      return null;
    File[] arrayOfFile = new File[paramArrayOfFile.length];
    for (int i = 0; i < paramArrayOfFile.length; i++)
      arrayOfFile[i] = new File(paramArrayOfFile[i].getPath());
    i = 1;
    for (int j = 0; j < arrayOfFile.length; j++)
    {
      if (CheckServicePermission.hasFileAccessPermissions(arrayOfFile[j].toString()))
        continue;
      i = 0;
      break;
    }
    String str = "";
    for (int k = 0; k < arrayOfFile.length; k++)
      str = str + arrayOfFile[k].getPath() + "\n";
    if ((i == 0) && (!askUser(str)))
      return null;
    Object[] arrayOfObject = (Object[])(Object[])AccessController.doPrivileged(new PrivilegedAction(arrayOfFile)
    {
      private final File[] val$files;

      public Object run()
      {
        FileContents[] arrayOfFileContents = new FileContents[this.val$files.length];
        try
        {
          for (int i = 0; i < this.val$files.length; i++)
            arrayOfFileContents[i] = new FileContentsImpl(this.val$files[i], ExtendedServiceImpl.DEFAULT_FILESIZE);
        }
        catch (IOException localIOException)
        {
          arrayOfFileContents[0] = localIOException;
        }
        return arrayOfFileContents;
      }
    });
    if ((arrayOfObject[0] instanceof IOException))
      throw ((IOException)arrayOfObject[0]);
    return (FileContents[])(FileContents[])arrayOfObject;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.ExtendedServiceImpl
 * JD-Core Version:    0.6.0
 */