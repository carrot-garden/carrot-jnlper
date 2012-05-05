package com.sun.javaws.util;

import com.sun.deploy.util.DialogListener;
import com.sun.javaws.ui.SplashScreen;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class JavawsDialogListener
  implements DialogListener
{
  public void beforeShow()
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        SplashScreen.hide();
        return null;
      }
    });
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.util.JavawsDialogListener
 * JD-Core Version:    0.6.0
 */