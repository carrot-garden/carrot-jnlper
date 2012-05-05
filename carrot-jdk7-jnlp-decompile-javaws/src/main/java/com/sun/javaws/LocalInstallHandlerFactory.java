package com.sun.javaws;

public class LocalInstallHandlerFactory
{
  public static LocalInstallHandler newInstance()
  {
    return new UnixInstallHandler();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.LocalInstallHandlerFactory
 * JD-Core Version:    0.6.0
 */