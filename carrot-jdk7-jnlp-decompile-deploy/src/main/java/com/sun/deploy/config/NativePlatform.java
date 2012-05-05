package com.sun.deploy.config;

import com.sun.deploy.trace.Trace;
import java.io.PrintStream;

public class NativePlatform
{
  String _osname;
  String _osarch;
  String _unifiedOsArch;
  boolean _is32Bit;
  boolean _is64Bit;
  private static NativePlatform _currentNativePlatform = null;

  public String getOSName()
  {
    return this._osname;
  }

  public String getOSArch()
  {
    return this._osarch;
  }

  public String getOsArchUnified()
  {
    return this._unifiedOsArch;
  }

  public boolean is32Bit()
  {
    return this._is32Bit;
  }

  public boolean is64Bit()
  {
    return this._is64Bit;
  }

  public static NativePlatform getCurrentNativePlatform()
  {
    if (null == _currentNativePlatform)
      _currentNativePlatform = new NativePlatform(null, null);
    return _currentNativePlatform;
  }

  public NativePlatform(String paramString1, String paramString2)
  {
    if (paramString1 == null)
      paramString1 = Config.getOSName();
    if (paramString2 == null)
      paramString2 = Config.getOSArch();
    this._osname = paramString1;
    this._osarch = paramString2;
    if ((paramString2.equals("x86")) || (paramString2.equals("i386")) || (paramString2.equals("i486")) || (paramString2.equals("i586")) || (paramString2.equals("i686")))
    {
      this._is32Bit = true;
      this._is64Bit = false;
      this._unifiedOsArch = "x86";
    }
    else if ((paramString2.equals("ppc")) || (paramString2.equals("arm")) || (paramString2.equals("sparc")) || (paramString2.equals("pa_risc2.0")))
    {
      this._is32Bit = true;
      this._is64Bit = false;
      this._unifiedOsArch = paramString2;
    }
    else if ((paramString2.equals("x86_64")) || (paramString2.equals("amd64")))
    {
      this._is32Bit = false;
      this._is64Bit = true;
      this._unifiedOsArch = "x86_64";
    }
    else if ((paramString2.equals("ia64")) || (paramString2.equals("sparcv9")))
    {
      this._is32Bit = false;
      this._is64Bit = true;
      this._unifiedOsArch = paramString2;
    }
    else
    {
      String str = "JREInfo: unknown osArch: <" + paramString2 + ">, considering 32bit";
      Trace.println(str);
      System.out.println(str);
      this._is32Bit = true;
      this._is64Bit = false;
      this._unifiedOsArch = paramString2;
    }
  }

  public boolean compatible(NativePlatform paramNativePlatform)
  {
    return (getOSName().equals(paramNativePlatform.getOSName())) && (getOsArchUnified().equals(paramNativePlatform.getOsArchUnified()));
  }

  public String toString()
  {
    String str;
    if (is32Bit())
      str = "32bit";
    else if (is64Bit())
      str = "64bit";
    else
      str = "??bit";
    return this._osname + ", " + this._osarch + " [ " + this._unifiedOsArch + ", " + str + " ]";
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.NativePlatform
 * JD-Core Version:    0.6.0
 */