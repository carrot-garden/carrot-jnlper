package com.sun.deploy.panel;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.filechooser.FileFilter;

final class CertFileFilter extends FileFilter
{
  private Hashtable filters = null;
  private String description = null;
  private String fullDescription = null;
  private boolean useExtensionsInDescription = true;

  public boolean accept(File paramFile)
  {
    if (paramFile != null)
    {
      if (paramFile.isDirectory())
        return true;
      String str = getExtension(paramFile);
      if ((str != null) && (this.filters.get(getExtension(paramFile)) != null))
        return true;
    }
    return false;
  }

  String getExtension(File paramFile)
  {
    if (paramFile != null)
    {
      String str = paramFile.getName();
      int i = str.lastIndexOf('.');
      if ((i > 0) && (i < str.length() - 1))
        return str.substring(i + 1).toLowerCase();
    }
    return null;
  }

  void addExtension(String paramString)
  {
    if (this.filters == null)
      this.filters = new Hashtable(5);
    this.filters.put(paramString.toLowerCase(), this);
    this.fullDescription = null;
  }

  public String getDescription()
  {
    if (this.fullDescription == null)
      if ((this.description == null) || (isExtensionListInDescription()))
      {
        this.fullDescription = (this.description + " (");
        Enumeration localEnumeration = this.filters.keys();
        if (localEnumeration != null)
          for (this.fullDescription = (this.fullDescription + "." + (String)localEnumeration.nextElement()); localEnumeration.hasMoreElements(); this.fullDescription = (this.fullDescription + ", ." + (String)localEnumeration.nextElement()));
        this.fullDescription += ")";
      }
      else
      {
        this.fullDescription = this.description;
      }
    return this.fullDescription;
  }

  void setDescription(String paramString)
  {
    this.description = paramString;
    this.fullDescription = null;
  }

  public boolean isExtensionListInDescription()
  {
    return this.useExtensionsInDescription;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.CertFileFilter
 * JD-Core Version:    0.6.0
 */