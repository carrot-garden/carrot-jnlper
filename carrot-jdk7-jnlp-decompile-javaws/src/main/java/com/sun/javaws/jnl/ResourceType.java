package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLable;

abstract interface ResourceType extends XMLable
{
  public abstract void visit(ResourceVisitor paramResourceVisitor);
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.jnl.ResourceType
 * JD-Core Version:    0.6.0
 */