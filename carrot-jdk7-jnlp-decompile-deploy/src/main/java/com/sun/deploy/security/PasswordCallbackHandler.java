package com.sun.deploy.security;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

final class PasswordCallbackHandler
  implements CallbackHandler
{
  private String msgText = null;
  private char[] keyPassphrase = null;

  PasswordCallbackHandler(String paramString)
  {
    this.msgText = paramString;
  }

  public void handle(Callback[] paramArrayOfCallback)
    throws IOException, UnsupportedCallbackException
  {
    for (int i = 0; i < paramArrayOfCallback.length; i++)
    {
      if (!(paramArrayOfCallback[i] instanceof PasswordCallback))
        continue;
      PasswordCallback localPasswordCallback = (PasswordCallback)paramArrayOfCallback[i];
      CredentialInfo localCredentialInfo = ToolkitStore.getUI().showPasswordDialog(null, getMessage("password.dialog.title"), getMessage(this.msgText), false, false, null, false, null);
      if (localCredentialInfo != null)
        localPasswordCallback.setPassword(localCredentialInfo.getPassword());
      else
        localPasswordCallback.setPassword(null);
    }
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.PasswordCallbackHandler
 * JD-Core Version:    0.6.0
 */