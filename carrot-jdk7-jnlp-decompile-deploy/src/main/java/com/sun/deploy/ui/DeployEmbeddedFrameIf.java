package com.sun.deploy.ui;

import java.awt.Component;

public abstract interface DeployEmbeddedFrameIf
{
  public abstract void push(Component paramComponent);

  public abstract Component pop();
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.ui.DeployEmbeddedFrameIf
 * JD-Core Version:    0.6.0
 */