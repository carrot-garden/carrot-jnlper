package com.sun.jnlp;

import java.awt.AWTPermission;
import java.io.FilePermission;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permission;

public final class CheckServicePermission
{
  private static boolean checkPermission(Permission paramPermission)
  {
    try
    {
      AccessController.checkPermission(paramPermission);
      return true;
    }
    catch (AccessControlException localAccessControlException)
    {
    }
    return false;
  }

  static boolean hasFileAccessPermissions(String paramString)
  {
    return checkPermission(new FilePermission(paramString, "read,write"));
  }

  static boolean hasFileAccessPermissions()
  {
    return checkPermission(new FilePermission("*", "read,write"));
  }

  static boolean hasPrintAccessPermissions()
  {
    return checkPermission(new RuntimePermission("queuePrintJob"));
  }

  static boolean hasClipboardPermissions()
  {
    return checkPermission(new AWTPermission("accessClipboard"));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.CheckServicePermission
 * JD-Core Version:    0.6.0
 */