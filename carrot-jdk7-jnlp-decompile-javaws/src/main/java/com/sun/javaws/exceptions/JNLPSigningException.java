package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import com.sun.javaws.jnl.LaunchDesc;

public class JNLPSigningException extends LaunchDescException
{
  String _signedSource = null;

  public JNLPSigningException(LaunchDesc paramLaunchDesc, String paramString)
  {
    super(paramLaunchDesc, null);
    this._signedSource = paramString;
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.badsignedjnlp");
  }

  public String getSignedSource()
  {
    return this._signedSource;
  }

  public String toString()
  {
    return "JNLPSigningException[" + getMessage() + "]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.JNLPSigningException
 * JD-Core Version:    0.6.0
 */