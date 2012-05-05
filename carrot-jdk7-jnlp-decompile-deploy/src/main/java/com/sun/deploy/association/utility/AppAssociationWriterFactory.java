package com.sun.deploy.association.utility;

public class AppAssociationWriterFactory
{
  public static AppAssociationWriter newInstance()
  {
    return new GnomeAppAssociationWriter();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.utility.AppAssociationWriterFactory
 * JD-Core Version:    0.6.0
 */