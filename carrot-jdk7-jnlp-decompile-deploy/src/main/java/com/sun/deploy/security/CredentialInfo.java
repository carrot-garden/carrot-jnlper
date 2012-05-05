package com.sun.deploy.security;

import com.sun.deploy.trace.Trace;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.PasswordAuthentication;
import java.util.StringTokenizer;

public final class CredentialInfo
  implements Externalizable, Cloneable
{
  private String userName = "";
  private char[] password = new char[0];
  private byte[] encryptedPassword = new byte[0];
  private String domain = "";
  private long sessionId = -1L;
  private boolean isPasswordSaved = false;

  public Object clone()
  {
    CredentialInfo localCredentialInfo = new CredentialInfo();
    localCredentialInfo.userName = this.userName;
    localCredentialInfo.domain = this.domain;
    localCredentialInfo.sessionId = this.sessionId;
    localCredentialInfo.isPasswordSaved = this.isPasswordSaved;
    localCredentialInfo.password = new char[this.password.length];
    localCredentialInfo.encryptedPassword = new byte[this.encryptedPassword.length];
    System.arraycopy(this.password, 0, localCredentialInfo.password, 0, this.password.length);
    System.arraycopy(this.encryptedPassword, 0, localCredentialInfo.encryptedPassword, 0, this.encryptedPassword.length);
    return localCredentialInfo;
  }

  public void setSessionId(long paramLong)
  {
    this.sessionId = paramLong;
  }

  public long getSessionId()
  {
    return this.sessionId;
  }

  public boolean isPasswordSaveApproved()
  {
    return this.isPasswordSaved;
  }

  public void setPasswordSaveApproval(boolean paramBoolean)
  {
    this.isPasswordSaved = paramBoolean;
  }

  public void setUserName(String paramString)
  {
    if (paramString == null)
    {
      this.userName = "";
    }
    else if (paramString.indexOf("\\".toString()) > -1)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "\\");
      this.domain = localStringTokenizer.nextToken();
      this.userName = localStringTokenizer.nextToken();
    }
    else
    {
      this.userName = paramString;
    }
  }

  public String getUserName()
  {
    return this.userName;
  }

  public void setDomain(String paramString)
  {
    if (paramString == null)
      this.domain = "";
    else
      this.domain = paramString;
  }

  public String getDomain()
  {
    return this.domain;
  }

  public void setPassword(char[] paramArrayOfChar)
  {
    if (paramArrayOfChar == null)
    {
      paramArrayOfChar = new char[0];
      setEncryptedPassword(null);
    }
    this.password = new char[paramArrayOfChar.length];
    System.arraycopy(paramArrayOfChar, 0, this.password, 0, this.password.length);
  }

  public char[] getPassword()
  {
    char[] arrayOfChar = new char[this.password.length];
    System.arraycopy(this.password, 0, arrayOfChar, 0, this.password.length);
    return arrayOfChar;
  }

  public void writeExternal(ObjectOutput paramObjectOutput)
    throws IOException
  {
    try
    {
      paramObjectOutput.writeObject(this.userName);
      paramObjectOutput.writeLong(this.sessionId);
      paramObjectOutput.writeObject(this.domain);
      paramObjectOutput.writeInt(this.encryptedPassword.length);
      for (int i = 0; i < this.encryptedPassword.length; i++)
        paramObjectOutput.writeByte(this.encryptedPassword[i]);
    }
    catch (Exception localException)
    {
      Trace.securityPrintException(localException);
    }
  }

  public boolean isCredentialEmpty()
  {
    return (this.userName.length() <= 0) && (this.domain.length() <= 0);
  }

  public void readExternal(ObjectInput paramObjectInput)
    throws IOException, ClassNotFoundException
  {
    try
    {
      this.userName = ((String)paramObjectInput.readObject());
      this.sessionId = paramObjectInput.readLong();
      this.domain = ((String)paramObjectInput.readObject());
      this.encryptedPassword = new byte[paramObjectInput.readInt()];
      for (int i = 0; i < this.encryptedPassword.length; i++)
        this.encryptedPassword[i] = paramObjectInput.readByte();
    }
    catch (Exception localException)
    {
      Trace.securityPrintException(localException);
    }
  }

  protected byte[] getEncryptedPassword()
  {
    byte[] arrayOfByte = new byte[this.encryptedPassword.length];
    System.arraycopy(this.encryptedPassword, 0, arrayOfByte, 0, this.encryptedPassword.length);
    return arrayOfByte;
  }

  protected void setEncryptedPassword(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte == null)
      paramArrayOfByte = new byte[0];
    this.encryptedPassword = new byte[paramArrayOfByte.length];
    System.arraycopy(paramArrayOfByte, 0, this.encryptedPassword, 0, paramArrayOfByte.length);
  }

  public static CredentialInfo passAuthToCredentialInfo(PasswordAuthentication paramPasswordAuthentication)
  {
    CredentialInfo localCredentialInfo = new CredentialInfo();
    if (paramPasswordAuthentication != null)
    {
      if (paramPasswordAuthentication.getUserName().contains("\\"))
      {
        StringTokenizer localStringTokenizer = new StringTokenizer(paramPasswordAuthentication.getUserName(), "\\");
        localCredentialInfo.domain = localStringTokenizer.nextToken();
        localCredentialInfo.userName = localStringTokenizer.nextToken();
      }
      else
      {
        localCredentialInfo.userName = paramPasswordAuthentication.getUserName();
      }
      localCredentialInfo.password = paramPasswordAuthentication.getPassword();
    }
    return localCredentialInfo;
  }

  public PasswordAuthentication getPasswordAuthentication()
  {
    String str = this.userName;
    if ((this.domain != null) && (!this.domain.trim().equals("")))
      str = this.domain + '\\' + this.userName;
    return new PasswordAuthentication(str, this.password);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CredentialInfo
 * JD-Core Version:    0.6.0
 */