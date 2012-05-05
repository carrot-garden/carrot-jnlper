package com.sun.jnlp;

import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.jnlp.ServiceManagerStub;
import javax.jnlp.UnavailableServiceException;

public final class JnlpLookupStub
  implements ServiceManagerStub
{
  public Object lookup(String paramString)
    throws UnavailableServiceException
  {
    Object localObject = AccessController.doPrivileged(new PrivilegedAction(paramString)
    {
      private final String val$name;

      public Object run()
      {
        return JnlpLookupStub.this.findService(this.val$name);
      }
    });
    if (localObject == null)
      throw new UnavailableServiceException(paramString);
    return localObject;
  }

  private Object findService(String paramString)
  {
    if (paramString != null)
    {
      JNLPClassLoaderIf localJNLPClassLoaderIf = JNLPClassLoaderUtil.getInstance();
      if (localJNLPClassLoaderIf == null)
        return null;
      if (paramString.equals("javax.jnlp.BasicService"))
        return localJNLPClassLoaderIf.getBasicService();
      if (paramString.equals("javax.jnlp.FileOpenService"))
        return localJNLPClassLoaderIf.getFileOpenService();
      if (paramString.equals("javax.jnlp.FileSaveService"))
        return localJNLPClassLoaderIf.getFileSaveService();
      if (paramString.equals("javax.jnlp.ExtensionInstallerService"))
        return localJNLPClassLoaderIf.getExtensionInstallerService();
      if (paramString.equals("javax.jnlp.DownloadService"))
        return localJNLPClassLoaderIf.getDownloadService();
      if (paramString.equals("javax.jnlp.ClipboardService"))
        return localJNLPClassLoaderIf.getClipboardService();
      if (paramString.equals("javax.jnlp.PrintService"))
        return localJNLPClassLoaderIf.getPrintService();
      if (paramString.equals("javax.jnlp.PersistenceService"))
        return localJNLPClassLoaderIf.getPersistenceService();
      if (paramString.equals("javax.jnlp.ExtendedService"))
        return localJNLPClassLoaderIf.getExtendedService();
      if (paramString.equals("javax.jnlp.SingleInstanceService"))
        return localJNLPClassLoaderIf.getSingleInstanceService();
      if (paramString.equals("javax.jnlp.IntegrationService"))
        return localJNLPClassLoaderIf.getIntegrationService();
      if (paramString.equals("javax.jnlp.DownloadService2"))
        return localJNLPClassLoaderIf.getDownloadService2();
    }
    return null;
  }

  public String[] getServiceNames()
  {
    if (ExtensionInstallerServiceImpl.getInstance() != null)
      return new String[] { "javax.jnlp.BasicService", "javax.jnlp.FileOpenService", "javax.jnlp.FileSaveService", "javax.jnlp.ExtensionInstallerService", "javax.jnlp.DownloadService", "javax.jnlp.ClipboardService", "javax.jnlp.PersistenceService", "javax.jnlp.PrintService", "javax.jnlp.ExtendedService", "javax.jnlp.SingleInstanceService", "com.sun.jnlp.IntegrationService" };
    return new String[] { "javax.jnlp.BasicService", "javax.jnlp.FileOpenService", "javax.jnlp.FileSaveService", "javax.jnlp.DownloadService", "javax.jnlp.ClipboardService", "javax.jnlp.PersistenceService", "javax.jnlp.PrintService", "javax.jnlp.ExtendedService", "javax.jnlp.SingleInstanceService" };
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.JnlpLookupStub
 * JD-Core Version:    0.6.0
 */