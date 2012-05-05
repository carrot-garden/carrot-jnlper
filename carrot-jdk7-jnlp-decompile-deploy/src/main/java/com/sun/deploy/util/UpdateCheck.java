package com.sun.deploy.util;

import com.sun.deploy.config.Platform;
import com.sun.deploy.uitoolkit.ToolkitStore;

public class UpdateCheck
{
  public static void showDialog()
  {
    if (!Platform.get().shouldPromptForAutoCheck())
      return;
    ToolkitStore.getUI();
    Platform.get().handleUserResponse(0);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.UpdateCheck
 * JD-Core Version:    0.6.0
 */