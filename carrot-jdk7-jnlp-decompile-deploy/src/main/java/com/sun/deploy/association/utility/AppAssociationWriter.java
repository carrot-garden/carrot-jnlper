package com.sun.deploy.association.utility;

import com.sun.deploy.association.Association;
import com.sun.deploy.association.AssociationAlreadyRegisteredException;
import com.sun.deploy.association.AssociationNotRegisteredException;
import com.sun.deploy.association.RegisterFailedException;

public abstract interface AppAssociationWriter
{
  public static final int USER_LEVEL = 1;
  public static final int SYSTEM_LEVEL = 2;
  public static final int DEFAULT_LEVEL = 3;

  public abstract void checkAssociationValidForRegistration(Association paramAssociation)
    throws IllegalArgumentException;

  public abstract void checkAssociationValidForUnregistration(Association paramAssociation)
    throws IllegalArgumentException;

  public abstract boolean isAssociationExist(Association paramAssociation, int paramInt);

  public abstract void registerAssociation(Association paramAssociation, int paramInt)
    throws AssociationAlreadyRegisteredException, RegisterFailedException;

  public abstract void unregisterAssociation(Association paramAssociation, int paramInt)
    throws AssociationNotRegisteredException, RegisterFailedException;
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.AppAssociationWriter
 * JD-Core Version:    0.6.0
 */