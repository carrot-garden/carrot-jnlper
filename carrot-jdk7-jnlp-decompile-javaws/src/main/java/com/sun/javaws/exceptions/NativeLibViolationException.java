package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;

public class NativeLibViolationException extends JNLPException
{
  public NativeLibViolationException()
  {
    super(ResourceManager.getString("launch.error.category.security"));
  }

  public String getRealMessage()
  {
    return ResourceManager.getString("launch.error.nativelibviolation");
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.exceptions.NativeLibViolationException
 * JD-Core Version:    0.6.0
 */