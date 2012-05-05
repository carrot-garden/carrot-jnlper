package com.sun.deploy.association.utility;

import java.net.URL;
import java.util.List;

public abstract interface AppAssociationReader
{
  public abstract String getDescriptionByMimeType(String paramString);

  public abstract String getDescriptionByFileExt(String paramString);

  public abstract String getMimeTypeByURL(URL paramURL);

  public abstract List getFileExtListByMimeType(String paramString);

  public abstract String getMimeTypeByFileExt(String paramString);

  public abstract String getIconFileNameByMimeType(String paramString);

  public abstract String getIconFileNameByFileExt(String paramString);

  public abstract List getActionListByFileExt(String paramString);

  public abstract List getActionListByMimeType(String paramString);

  public abstract boolean isMimeTypeExist(String paramString);

  public abstract boolean isFileExtExist(String paramString);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.AppAssociationReader
 * JD-Core Version:    0.6.0
 */