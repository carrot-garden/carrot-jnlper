package com.sun.deploy.panel;

import com.sun.deploy.resources.ResourceManager;
import java.util.ArrayList;
import java.util.List;

class SimpleTreeNode
  implements ITreeNode
{
  private String desc;
  private List lChildNodes = new ArrayList();
  private List lProperties = new ArrayList();

  SimpleTreeNode(String paramString)
  {
    String str = ResourceManager.getMessage(paramString);
    this.desc = str;
  }

  public String getDescription()
  {
    return this.desc;
  }

  public int getChildNodeCount()
  {
    return this.lChildNodes.size();
  }

  public ITreeNode getChildNode(int paramInt)
  {
    return (ITreeNode)this.lChildNodes.get(paramInt);
  }

  public void addChildNode(ITreeNode paramITreeNode)
  {
    this.lChildNodes.add(paramITreeNode);
  }

  public int getPropertyCount()
  {
    return this.lProperties.size();
  }

  public IProperty getProperty(int paramInt)
  {
    return (IProperty)this.lProperties.get(paramInt);
  }

  public void addProperty(IProperty paramIProperty)
  {
    this.lProperties.add(paramIProperty);
  }

  public String toString()
  {
    return this.desc;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.SimpleTreeNode
 * JD-Core Version:    0.6.0
 */