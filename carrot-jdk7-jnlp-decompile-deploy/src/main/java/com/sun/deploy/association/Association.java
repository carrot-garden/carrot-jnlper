package com.sun.deploy.association;

import com.sun.deploy.association.utility.AppUtility;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Association
{
  private String name;
  private String description;
  private String mimeType;
  private List fileExtensionList;
  private String iconFileName;
  private List actionList;
  private int hashcode;

  public String getName()
  {
    return this.name;
  }

  public void setName(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("The given mime file name is null.");
    this.name = paramString;
  }

  public String getDescription()
  {
    return this.description;
  }

  public void setDescription(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("The given description is null.");
    this.description = paramString;
  }

  public String getMimeType()
  {
    return this.mimeType;
  }

  public void setMimeType(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("The given MIME type is null.");
    this.mimeType = paramString;
  }

  public boolean addFileExtension(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("The given file extension is null.");
    paramString = AppUtility.addDotToFileExtension(paramString);
    if (this.fileExtensionList == null)
      this.fileExtensionList = new ArrayList();
    return this.fileExtensionList.add(paramString);
  }

  public boolean removeFileExtension(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("The given file extension is null.");
    paramString = AppUtility.addDotToFileExtension(paramString);
    if (this.fileExtensionList != null)
      return this.fileExtensionList.remove(paramString);
    return false;
  }

  public List getFileExtList()
  {
    if (this.fileExtensionList == null)
      return null;
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = this.fileExtensionList.iterator();
    while (localIterator.hasNext())
      localArrayList.add(localIterator.next());
    return localArrayList;
  }

  public String getIconFileName()
  {
    return this.iconFileName;
  }

  public void setIconFileName(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("The given icon file name is null.");
    this.iconFileName = paramString;
  }

  public boolean addAction(Action paramAction)
  {
    if (paramAction == null)
      throw new IllegalArgumentException("The given action is null.");
    if (paramAction.getVerb() == null)
      throw new IllegalArgumentException("the given action object has null verb field.");
    if (paramAction.getCommand() == null)
      throw new IllegalArgumentException("the given action object has null command field.");
    if (this.actionList == null)
      this.actionList = new ArrayList();
    return this.actionList.add(new Action(paramAction.getVerb(), paramAction.getCommand(), paramAction.getDescription()));
  }

  public boolean removeAction(Action paramAction)
  {
    if (paramAction == null)
      throw new IllegalArgumentException("The given action is null.");
    if ((paramAction.getVerb() == null) || (paramAction.getCommand() == null))
      throw new IllegalArgumentException("the given action object has null verb field or command field.");
    if (this.actionList != null)
      return this.actionList.remove(paramAction);
    return false;
  }

  public List getActionList()
  {
    if (this.actionList == null)
      return null;
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = this.actionList.iterator();
    while (localIterator.hasNext())
      localArrayList.add(localIterator.next());
    return localArrayList;
  }

  public Action getActionByVerb(String paramString)
  {
    if (this.actionList != null)
    {
      Iterator localIterator = this.actionList.iterator();
      if (localIterator != null)
        while (localIterator.hasNext())
        {
          Action localAction = (Action)localIterator.next();
          String str = localAction.getVerb();
          if (str.equalsIgnoreCase(paramString))
            return localAction;
        }
    }
    return null;
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof Association))
      return false;
    Association localAssociation = (Association)paramObject;
    String str1 = localAssociation.getDescription();
    String str2 = localAssociation.getIconFileName();
    String str3 = localAssociation.getMimeType();
    int i = (this.description == null ? str1 == null : this.description.equals(str1)) && (this.iconFileName == null ? str2 == null : this.iconFileName.equals(str2)) && (this.mimeType == null ? str3 == null : this.mimeType.equals(str3)) ? 1 : 0;
    if (i == 0)
      return false;
    List localList1 = localAssociation.getFileExtList();
    int k = 0;
    if ((this.fileExtensionList == null) && (localList1 == null))
      k = 1;
    else if ((this.fileExtensionList != null) && (localList1 != null) && (this.fileExtensionList.containsAll(localList1)) && (localList1.containsAll(this.fileExtensionList)))
      k = 1;
    if (k == 0)
      return false;
    List localList2 = localAssociation.getActionList();
    int j = 0;
    if ((this.actionList == null) && (localList2 != null))
      j = 1;
    else if ((this.actionList != null) && (localList2 != null) && (this.actionList.containsAll(localList2)) && (localList2.containsAll(this.actionList)))
      j = 1;
    return j;
  }

  public int hashCode()
  {
    if (this.hashcode != 0)
    {
      int i = 17;
      if (this.name != null)
        i = i * 37 + this.name.hashCode();
      if (this.description != null)
        i = i * 37 + this.description.hashCode();
      if (this.mimeType != null)
        i = i * 37 + this.mimeType.hashCode();
      if (this.iconFileName != null)
        i = i * 37 + this.iconFileName.hashCode();
      if (this.fileExtensionList != null)
        i = i * 37 + this.fileExtensionList.hashCode();
      if (this.actionList != null)
        i = i * 37 + this.actionList.hashCode();
      this.hashcode = i;
    }
    return this.hashcode;
  }

  public String toString()
  {
    String str1 = "\r\n";
    String str2 = "";
    str2 = str2.concat("MIME File Name: ");
    if (this.name != null)
      str2 = str2.concat(this.name);
    str2 = str2.concat(str1);
    str2 = str2.concat("Description: ");
    if (this.description != null)
      str2 = str2.concat(this.description);
    str2 = str2.concat(str1);
    str2 = str2.concat("MIME Type: ");
    if (this.mimeType != null)
      str2 = str2.concat(this.mimeType);
    str2 = str2.concat(str1);
    str2 = str2.concat("Icon File: ");
    if (this.iconFileName != null)
      str2 = str2.concat(this.iconFileName);
    str2 = str2.concat(str1);
    str2 = str2.concat("File Extension: ");
    Iterator localIterator;
    if (this.fileExtensionList != null)
    {
      localIterator = this.fileExtensionList.iterator();
      if (localIterator != null)
        while (localIterator.hasNext())
        {
          str2 = str2.concat((String)localIterator.next());
          if (!localIterator.hasNext())
            continue;
          str2 = str2.concat(" ");
        }
    }
    str2 = str2.concat(str1);
    str2 = str2.concat("Action List: ");
    if (this.actionList != null)
    {
      localIterator = this.actionList.iterator();
      if (localIterator != null)
      {
        Action localAction;
        for (str2 = str2.concat(str1); localIterator.hasNext(); str2 = str2.concat(localAction.toString()))
          localAction = (Action)localIterator.next();
      }
    }
    str2 = str2.concat(str1);
    return str2;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.Association
 * JD-Core Version:    0.6.0
 */