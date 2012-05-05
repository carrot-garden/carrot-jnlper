package com.sun.deploy.association.utility;

public class AppAssociationReaderFactory
{
  public static AppAssociationReader newInstance()
  {
    return new GnomeAppAssociationReader();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.AppAssociationReaderFactory
 * JD-Core Version:    0.6.0
 */