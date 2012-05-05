package com.sun.deploy.jardiff;

import com.sun.applet2.preloader.CancelException;
import java.io.IOException;
import java.io.OutputStream;

public abstract interface Patcher
{
  public abstract void applyPatch(PatchDelegate paramPatchDelegate, String paramString1, String paramString2, OutputStream paramOutputStream)
    throws IOException;

  public static abstract interface PatchDelegate
  {
    public abstract void patching(int paramInt)
      throws CancelException;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.jardiff.Patcher
 * JD-Core Version:    0.6.0
 */