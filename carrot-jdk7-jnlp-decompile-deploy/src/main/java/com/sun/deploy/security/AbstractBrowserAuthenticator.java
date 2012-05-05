package com.sun.deploy.security;

import java.net.PasswordAuthentication;
import java.util.Arrays;

public abstract class AbstractBrowserAuthenticator
  implements BrowserAuthenticator
{
  protected PasswordAuthentication getPAFromCharArray(char[] paramArrayOfChar)
  {
    if (paramArrayOfChar == null)
      return null;
    for (int i = 0; (i < paramArrayOfChar.length) && (':' != paramArrayOfChar[i]); i++);
    PasswordAuthentication localPasswordAuthentication = null;
    if (i < paramArrayOfChar.length)
    {
      String str = new String(paramArrayOfChar, 0, i);
      char[] arrayOfChar = extractArray(paramArrayOfChar, i + 1);
      localPasswordAuthentication = new PasswordAuthentication(str, arrayOfChar);
      resetArray(arrayOfChar);
    }
    resetArray(paramArrayOfChar);
    return localPasswordAuthentication;
  }

  private void resetArray(char[] paramArrayOfChar)
  {
    Arrays.fill(paramArrayOfChar, ' ');
  }

  private char[] extractArray(char[] paramArrayOfChar, int paramInt)
  {
    char[] arrayOfChar = new char[paramArrayOfChar.length - paramInt];
    for (int i = 0; i < arrayOfChar.length; i++)
      arrayOfChar[i] = paramArrayOfChar[(i + paramInt)];
    return arrayOfChar;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.AbstractBrowserAuthenticator
 * JD-Core Version:    0.6.0
 */