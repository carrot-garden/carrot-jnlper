package com.sun.deploy.util;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.util.ArrayList;
import java.util.Arrays;

public class VersionID
  implements Comparable
{
  private final String[] _tuple;
  private final boolean _usePrefixMatch;
  private final boolean _useGreaterThan;
  private final boolean _isCompound;
  private final VersionID _rest;

  public VersionID(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0))
    {
      this._tuple = new String[0];
      this._useGreaterThan = false;
      this._usePrefixMatch = false;
      this._isCompound = false;
      this._rest = null;
      return;
    }
    int i = 0;
    int j = 0;
    int k = 0;
    int m = paramString.indexOf("&");
    Object localObject;
    if (m >= 0)
    {
      this._isCompound = true;
      localObject = new VersionID(paramString.substring(0, m));
      this._rest = new VersionID(paramString.substring(m + 1));
      this._tuple = ((VersionID)localObject)._tuple;
      this._usePrefixMatch = ((VersionID)localObject)._usePrefixMatch;
      this._useGreaterThan = ((VersionID)localObject)._useGreaterThan;
    }
    else
    {
      this._isCompound = false;
      this._rest = null;
      if (paramString.endsWith("+"))
      {
        this._useGreaterThan = true;
        this._usePrefixMatch = false;
        paramString = paramString.substring(0, paramString.length() - 1);
      }
      else if (paramString.endsWith("*"))
      {
        this._useGreaterThan = false;
        this._usePrefixMatch = true;
        paramString = paramString.substring(0, paramString.length() - 1);
      }
      else
      {
        this._useGreaterThan = false;
        this._usePrefixMatch = false;
      }
      localObject = new ArrayList();
      int n = 0;
      for (int i1 = 0; i1 < paramString.length(); i1++)
      {
        if (".-_".indexOf(paramString.charAt(i1)) == -1)
          continue;
        if (n < i1)
        {
          String str = paramString.substring(n, i1);
          ((ArrayList)localObject).add(str);
        }
        n = i1 + 1;
      }
      if (n < paramString.length())
        ((ArrayList)localObject).add(paramString.substring(n, paramString.length()));
      this._tuple = ((String[])(String[])((ArrayList)localObject).toArray(new String[0]));
    }
    Trace.println("Created version ID: " + this, TraceLevel.NETWORK);
  }

  public boolean isSimpleVersion()
  {
    return (!this._useGreaterThan) && (!this._usePrefixMatch) && (!this._isCompound);
  }

  public boolean match(VersionID paramVersionID)
  {
    if ((this._isCompound) && (!this._rest.match(paramVersionID)))
      return false;
    return this._useGreaterThan ? paramVersionID.isGreaterThanOrEqualTuple(this) : this._usePrefixMatch ? isPrefixMatchTuple(paramVersionID) : matchTuple(paramVersionID);
  }

  public boolean equals(Object paramObject)
  {
    if (matchTuple(paramObject))
    {
      VersionID localVersionID = (VersionID)paramObject;
      if (((this._rest == null) || (this._rest.equals(localVersionID._rest))) && (this._useGreaterThan == localVersionID._useGreaterThan) && (this._usePrefixMatch == localVersionID._usePrefixMatch))
        return true;
    }
    return false;
  }

  public int hashCode()
  {
    int i = 1;
    int j = 0;
    for (int k = 0; k < this._tuple.length; k++)
      if (i != 0)
      {
        i = 0;
        j = this._tuple[k].hashCode();
      }
      else
      {
        j ^= this._tuple[k].hashCode();
      }
    return j;
  }

  private boolean matchTuple(Object paramObject)
  {
    if ((paramObject == null) || (!(paramObject instanceof VersionID)))
      return false;
    VersionID localVersionID = (VersionID)paramObject;
    String[] arrayOfString1 = normalize(this._tuple, localVersionID._tuple.length);
    String[] arrayOfString2 = normalize(localVersionID._tuple, this._tuple.length);
    for (int i = 0; i < arrayOfString1.length; i++)
    {
      Object localObject1 = getValueAsObject(arrayOfString1[i]);
      Object localObject2 = getValueAsObject(arrayOfString2[i]);
      if (!localObject1.equals(localObject2))
        return false;
    }
    return true;
  }

  private Object getValueAsObject(String paramString)
  {
    if ((paramString.length() > 0) && (paramString.charAt(0) != '-'))
      try
      {
        return Integer.valueOf(paramString);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    return paramString;
  }

  public boolean isGreaterThan(VersionID paramVersionID)
  {
    return isGreaterThanOrEqualHelper(paramVersionID, false, true);
  }

  public boolean isGreaterThanOrEqual(VersionID paramVersionID)
  {
    return isGreaterThanOrEqualHelper(paramVersionID, true, true);
  }

  boolean isGreaterThanOrEqualTuple(VersionID paramVersionID)
  {
    return isGreaterThanOrEqualHelper(paramVersionID, true, false);
  }

  private boolean isGreaterThanOrEqualHelper(VersionID paramVersionID, boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean2) && (this._isCompound) && (!this._rest.isGreaterThanOrEqualHelper(paramVersionID, paramBoolean1, true)))
      return false;
    String[] arrayOfString1 = normalize(this._tuple, paramVersionID._tuple.length);
    String[] arrayOfString2 = normalize(paramVersionID._tuple, this._tuple.length);
    for (int i = 0; i < arrayOfString1.length; i++)
    {
      Object localObject1 = getValueAsObject(arrayOfString1[i]);
      Object localObject2 = getValueAsObject(arrayOfString2[i]);
      if (localObject1.equals(localObject2))
        continue;
      if (((localObject1 instanceof Integer)) && ((localObject2 instanceof Integer)))
        return ((Integer)localObject1).intValue() > ((Integer)localObject2).intValue();
      String str1 = arrayOfString1[i].toString();
      String str2 = arrayOfString2[i].toString();
      return str1.compareTo(str2) > 0;
    }
    return paramBoolean1;
  }

  private boolean isPrefixMatchTuple(VersionID paramVersionID)
  {
    String[] arrayOfString = normalize(paramVersionID._tuple, this._tuple.length);
    for (int i = 0; i < this._tuple.length; i++)
    {
      String str1 = this._tuple[i];
      String str2 = arrayOfString[i];
      if (!str1.equals(str2))
        return false;
    }
    return true;
  }

  private String[] normalize(String[] paramArrayOfString, int paramInt)
  {
    if (paramArrayOfString.length < paramInt)
    {
      String[] arrayOfString = new String[paramInt];
      System.arraycopy(paramArrayOfString, 0, arrayOfString, 0, paramArrayOfString.length);
      Arrays.fill(arrayOfString, paramArrayOfString.length, arrayOfString.length, "0");
      return arrayOfString;
    }
    return paramArrayOfString;
  }

  public int compareTo(Object paramObject)
  {
    if ((paramObject == null) || (!(paramObject instanceof VersionID)))
      return -1;
    VersionID localVersionID = (VersionID)paramObject;
    return isGreaterThanOrEqual(localVersionID) ? 1 : equals(localVersionID) ? 0 : -1;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < this._tuple.length - 1; i++)
    {
      localStringBuffer.append(this._tuple[i]);
      localStringBuffer.append('.');
    }
    if (this._tuple.length > 0)
      localStringBuffer.append(this._tuple[(this._tuple.length - 1)]);
    if (this._useGreaterThan)
      localStringBuffer.append('+');
    if (this._usePrefixMatch)
      localStringBuffer.append('*');
    if (this._isCompound)
    {
      localStringBuffer.append("&");
      localStringBuffer.append(this._rest);
    }
    return localStringBuffer.toString();
  }

  public VersionID getFamilyVersionID()
  {
    if (this._tuple.length < 3)
      return null;
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < 3; i++)
    {
      localStringBuffer.append(this._tuple[i]);
      localStringBuffer.append('.');
    }
    localStringBuffer.append('*');
    return new VersionID(localStringBuffer.toString());
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.VersionID
 * JD-Core Version:    0.6.0
 */