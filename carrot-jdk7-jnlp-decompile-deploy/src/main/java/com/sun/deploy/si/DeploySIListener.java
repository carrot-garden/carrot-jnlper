package com.sun.deploy.si;

public abstract interface DeploySIListener
{
  public abstract void newActivation(String[] paramArrayOfString);

  public abstract Object getSingleInstanceListener();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.si.DeploySIListener
 * JD-Core Version:    0.6.0
 */