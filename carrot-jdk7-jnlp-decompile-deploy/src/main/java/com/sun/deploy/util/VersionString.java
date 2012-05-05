package com.sun.deploy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class VersionString
{
  private ArrayList _versionIds = new ArrayList();

  public VersionString(String paramString)
  {
    if (paramString != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, " ", false);
      while (localStringTokenizer.hasMoreElements())
        this._versionIds.add(new VersionID(localStringTokenizer.nextToken()));
    }
  }

  public VersionString(VersionID paramVersionID)
  {
    if (paramVersionID != null)
      this._versionIds.add(paramVersionID);
  }

  public boolean isSimpleVersion()
  {
    if (this._versionIds.size() == 1)
      return ((VersionID)this._versionIds.get(0)).isSimpleVersion();
    return false;
  }

  public boolean contains(VersionID paramVersionID)
  {
    for (int i = 0; i < this._versionIds.size(); i++)
    {
      VersionID localVersionID = (VersionID)this._versionIds.get(i);
      boolean bool = localVersionID.match(paramVersionID);
      if (bool)
        return true;
    }
    return false;
  }

  public boolean contains(String paramString)
  {
    return contains(new VersionID(paramString));
  }

  public boolean containsGreaterThan(VersionID paramVersionID)
  {
    for (int i = 0; i < this._versionIds.size(); i++)
    {
      VersionID localVersionID = (VersionID)this._versionIds.get(i);
      boolean bool = localVersionID.isGreaterThan(paramVersionID);
      if (bool)
        return true;
    }
    return false;
  }

  public boolean containsGreaterThan(String paramString)
  {
    return containsGreaterThan(new VersionID(paramString));
  }

  public static boolean contains(String paramString1, String paramString2)
  {
    return new VersionString(paramString1).contains(paramString2);
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < this._versionIds.size(); i++)
    {
      localStringBuffer.append(this._versionIds.get(i).toString());
      localStringBuffer.append(' ');
    }
    return localStringBuffer.toString();
  }

  public List getAllVersionIDs()
  {
    return this._versionIds;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.VersionString
 * JD-Core Version:    0.6.0
 */