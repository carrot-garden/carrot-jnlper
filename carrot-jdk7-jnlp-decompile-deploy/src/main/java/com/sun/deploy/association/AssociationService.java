package com.sun.deploy.association;

import com.sun.deploy.association.utility.AppAssociationReader;
import com.sun.deploy.association.utility.AppAssociationReaderFactory;
import com.sun.deploy.association.utility.AppAssociationWriter;
import com.sun.deploy.association.utility.AppAssociationWriterFactory;
import com.sun.deploy.association.utility.AppUtility;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class AssociationService
{
  private AppAssociationReader appAssocReader = AppAssociationReaderFactory.newInstance();
  private AppAssociationWriter appAssocWriter = AppAssociationWriterFactory.newInstance();

  public Association getMimeTypeAssociation(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("The specified mime type is null");
    if (!this.appAssocReader.isMimeTypeExist(paramString))
      return null;
    Association localAssociation = new Association();
    List localList1 = this.appAssocReader.getFileExtListByMimeType(paramString);
    String str1 = this.appAssocReader.getIconFileNameByMimeType(paramString);
    String str2 = this.appAssocReader.getDescriptionByMimeType(paramString);
    List localList2 = this.appAssocReader.getActionListByMimeType(paramString);
    localAssociation.setMimeType(paramString);
    Iterator localIterator;
    if (localList1 != null)
    {
      localIterator = localList1.iterator();
      if (localIterator != null)
        while (localIterator.hasNext())
        {
          String str3 = (String)localIterator.next();
          if ((str3 != null) && (!"".equals(str3)))
            localAssociation.addFileExtension(str3);
        }
    }
    if (str1 != null)
      localAssociation.setIconFileName(str1);
    if (str2 != null)
      localAssociation.setDescription(str2);
    if (localList2 != null)
    {
      localIterator = localList2.iterator();
      if (localIterator != null)
        while (localIterator.hasNext())
          localAssociation.addAction((Action)localIterator.next());
    }
    return localAssociation;
  }

  public Association getFileExtensionAssociation(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("The specified file extension is null");
    paramString = AppUtility.addDotToFileExtension(paramString);
    if (!this.appAssocReader.isFileExtExist(paramString))
      return null;
    Association localAssociation = new Association();
    String str1 = this.appAssocReader.getMimeTypeByFileExt(paramString);
    String str2 = this.appAssocReader.getIconFileNameByFileExt(paramString);
    String str3 = this.appAssocReader.getDescriptionByFileExt(paramString);
    List localList = this.appAssocReader.getActionListByFileExt(paramString);
    localAssociation.addFileExtension(paramString);
    if (str2 != null)
      localAssociation.setIconFileName(str2);
    if (str1 != null)
      localAssociation.setMimeType(str1);
    if (str3 != null)
      localAssociation.setDescription(str3);
    if (localList != null)
    {
      Iterator localIterator = localList.iterator();
      if (localIterator != null)
        while (localIterator.hasNext())
          localAssociation.addAction((Action)localIterator.next());
    }
    return localAssociation;
  }

  public Association getAssociationByContent(URL paramURL)
  {
    if (paramURL == null)
      throw new IllegalArgumentException("The specified URL is null");
    Association localAssociation = null;
    String str1 = this.appAssocReader.getMimeTypeByURL(paramURL);
    if (str1 != null)
      localAssociation = getMimeTypeAssociation(str1);
    if (localAssociation == null)
    {
      String str2 = AppUtility.getFileExtensionByURL(paramURL);
      if (str2 != null)
        localAssociation = getFileExtensionAssociation(str2);
    }
    return localAssociation;
  }

  public void registerUserAssociation(Association paramAssociation)
    throws AssociationAlreadyRegisteredException, RegisterFailedException
  {
    if (paramAssociation == null)
      throw new IllegalArgumentException("The specified association is null");
    try
    {
      this.appAssocWriter.checkAssociationValidForRegistration(paramAssociation);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw localIllegalArgumentException;
    }
    if (this.appAssocWriter.isAssociationExist(paramAssociation, 1))
      throw new AssociationAlreadyRegisteredException("Assocation already exists!");
    this.appAssocWriter.registerAssociation(paramAssociation, 1);
  }

  public void unregisterUserAssociation(Association paramAssociation)
    throws AssociationNotRegisteredException, RegisterFailedException
  {
    if (paramAssociation == null)
      throw new IllegalArgumentException("The specified association is null");
    try
    {
      this.appAssocWriter.checkAssociationValidForUnregistration(paramAssociation);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw localIllegalArgumentException;
    }
    if (!this.appAssocWriter.isAssociationExist(paramAssociation, 1))
      throw new AssociationNotRegisteredException("Assocation not exists!");
    this.appAssocWriter.unregisterAssociation(paramAssociation, 1);
  }

  public void registerSystemAssociation(Association paramAssociation)
    throws AssociationAlreadyRegisteredException, RegisterFailedException
  {
    if (paramAssociation == null)
      throw new IllegalArgumentException("The specified association is null");
    try
    {
      this.appAssocWriter.checkAssociationValidForRegistration(paramAssociation);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw localIllegalArgumentException;
    }
    if (this.appAssocWriter.isAssociationExist(paramAssociation, 2))
      throw new AssociationAlreadyRegisteredException("Assocation already exists!");
    this.appAssocWriter.registerAssociation(paramAssociation, 2);
  }

  public void unregisterSystemAssociation(Association paramAssociation)
    throws AssociationNotRegisteredException, RegisterFailedException
  {
    if (paramAssociation == null)
      throw new IllegalArgumentException("The specified association is null");
    try
    {
      this.appAssocWriter.checkAssociationValidForUnregistration(paramAssociation);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw localIllegalArgumentException;
    }
    if (!this.appAssocWriter.isAssociationExist(paramAssociation, 2))
      throw new AssociationNotRegisteredException("Assocation not existed!");
    this.appAssocWriter.unregisterAssociation(paramAssociation, 2);
  }

  public boolean hasAssociation(Association paramAssociation)
  {
    return (this.appAssocWriter.isAssociationExist(paramAssociation, 2)) || (this.appAssocWriter.isAssociationExist(paramAssociation, 1));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.AssociationService
 * JD-Core Version:    0.6.0
 */