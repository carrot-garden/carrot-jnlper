package com.sun.deploy.panel;

import com.sun.deploy.config.Platform;
import com.sun.deploy.resources.ResourceManager;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JTree;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

final class XMLDocumentHandler extends DefaultHandler
{
  private ArrayList elementStack = new ArrayList();
  private boolean addNode = true;

  public void startDocument()
    throws SAXException
  {
    SimpleTreeNode localSimpleTreeNode = new SimpleTreeNode(getMessage("common.settings"));
    this.elementStack.add(0, localSimpleTreeNode);
  }

  public void startElement(String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
    throws SAXException
  {
    Object localObject1;
    Object localObject2;
    if (paramString3.equalsIgnoreCase("label"))
    {
      if (getShouldAdd())
      {
        localObject1 = new SimpleTreeNode(paramAttributes.getValue("text"));
        localObject2 = (ITreeNode)this.elementStack.get(0);
        ((ITreeNode)localObject2).addChildNode((ITreeNode)localObject1);
        this.elementStack.add(0, localObject1);
      }
    }
    else if (paramString3.equalsIgnoreCase("checkbox"))
    {
      if (getShouldAdd())
      {
        localObject1 = new ToggleProperty(paramAttributes.getValue("propertyName"), paramAttributes.getValue("checked"));
        localObject2 = (ITreeNode)this.elementStack.get(0);
        ((ITreeNode)localObject2).addProperty((IProperty)localObject1);
      }
    }
    else
    {
      Object localObject3;
      if (paramString3.equalsIgnoreCase("radiogroup"))
      {
        if (getShouldAdd())
        {
          localObject1 = paramAttributes.getValue("propertyName");
          localObject2 = paramAttributes.getValue("checked");
          localObject3 = new RadioPropertyGroup((String)localObject1, (String)localObject2);
          this.elementStack.add(0, localObject3);
        }
      }
      else
      {
        Object localObject4;
        if (paramString3.equalsIgnoreCase("rButton"))
        {
          if (getShouldAdd())
          {
            localObject1 = paramAttributes.getValue("text");
            localObject2 = (RadioPropertyGroup)this.elementStack.remove(0);
            localObject3 = ((RadioPropertyGroup)localObject2).getPropertyName();
            localObject4 = new RadioProperty((String)localObject3, (String)localObject1);
            ((RadioProperty)localObject4).setGroup((RadioPropertyGroup)localObject2);
            ITreeNode localITreeNode = (ITreeNode)this.elementStack.get(0);
            localITreeNode.addProperty((IProperty)localObject4);
            this.elementStack.add(0, localObject2);
          }
        }
        else if (paramString3.equalsIgnoreCase("TextField"))
        {
          if (getShouldAdd())
          {
            localObject1 = paramAttributes.getValue("propertyName");
            localObject2 = new TextFieldProperty((String)localObject1, "");
            localObject3 = (ITreeNode)this.elementStack.get(0);
            ((ITreeNode)localObject3).addProperty((IProperty)localObject2);
          }
        }
        else if (paramString3.equalsIgnoreCase("platform"))
        {
          localObject1 = paramAttributes.getValue("text");
          localObject2 = new Vector();
          if (((String)localObject1).indexOf(",") != -1)
          {
            localObject3 = new StringTokenizer((String)localObject1, ",");
            while (((StringTokenizer)localObject3).hasMoreTokens())
            {
              localObject4 = ((StringTokenizer)localObject3).nextToken();
              ((Vector)localObject2).add(((String)localObject4).trim());
            }
          }
          else
          {
            ((Vector)localObject2).add(((String)localObject1).trim());
          }
          if (!setShouldAdd((Vector)localObject2))
            this.elementStack.add(0, localObject1);
        }
        else if (paramString3.equalsIgnoreCase("permission"))
        {
          localObject1 = paramAttributes.getValue("text");
          if ((((String)localObject1).indexOf("admin") != -1) && (!Platform.get().hasAdminPrivileges()))
            this.addNode = false;
          if (!this.addNode)
            this.elementStack.add(0, localObject1);
        }
      }
    }
  }

  public void endElement(String paramString1, String paramString2, String paramString3)
    throws SAXException
  {
    if (paramString3.equalsIgnoreCase("label"))
    {
      if (getShouldAdd())
        this.elementStack.remove(0);
    }
    else if (paramString3.equalsIgnoreCase("radiogroup"))
    {
      if (getShouldAdd())
        this.elementStack.remove(0);
    }
    else if (paramString3.equalsIgnoreCase("platform"))
    {
      if (!getShouldAdd())
        this.elementStack.remove(0);
      this.addNode = true;
    }
    else if (paramString3.equalsIgnoreCase("permission"))
    {
      if (!getShouldAdd())
        this.elementStack.remove(0);
      this.addNode = true;
    }
  }

  private boolean getShouldAdd()
  {
    return this.addNode;
  }

  private boolean setShouldAdd(Vector paramVector)
  {
    String str1 = System.getProperty("os.name").toLowerCase();
    for (int i = 0; i < paramVector.size(); i++)
    {
      String str2 = ((String)paramVector.get(i)).toLowerCase();
      if ((str2.indexOf("gnome") != -1) && (Platform.get().isLocalInstallSupported()))
      {
        this.addNode = true;
        break;
      }
      if (str1.indexOf(str2) != -1)
      {
        this.addNode = true;
        break;
      }
      this.addNode = false;
    }
    return this.addNode;
  }

  public void endDocument()
    throws SAXException
  {
    ITreeNode localITreeNode = (ITreeNode)this.elementStack.get(0);
  }

  public void error(SAXParseException paramSAXParseException)
  {
    System.err.println("Error: " + paramSAXParseException);
  }

  public void fatalError(SAXParseException paramSAXParseException)
  {
    System.err.println("FatalError: " + paramSAXParseException);
  }

  public void warning(SAXParseException paramSAXParseException)
  {
    System.err.println("Warning: " + paramSAXParseException);
  }

  JTree getJTree()
  {
    ITreeNode localITreeNode = (ITreeNode)this.elementStack.get(0);
    JTree localJTree = TreeBuilder.createTree(new PropertyTreeModel(localITreeNode));
    localJTree.setFont(ResourceManager.getUIFont());
    return localJTree;
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.XMLDocumentHandler
 * JD-Core Version:    0.6.0
 */