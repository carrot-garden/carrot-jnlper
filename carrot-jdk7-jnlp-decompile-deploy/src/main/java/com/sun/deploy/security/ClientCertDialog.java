package com.sun.deploy.security;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.JList;

final class ClientCertDialog
{
  static String showDialog(TreeMap paramTreeMap1, TreeMap paramTreeMap2)
  {
    Vector localVector = new Vector();
    Object localObject1 = paramTreeMap1.keySet().iterator();
    Object localObject3;
    while (((Iterator)localObject1).hasNext())
    {
      String str1 = (String)((Iterator)localObject1).next();
      localObject2 = (X509Certificate[])(X509Certificate[])paramTreeMap1.get(str1);
      localObject3 = paramTreeMap2.get(str1);
      String str2 = CertUtils.extractSubjectAliasName(localObject2[0]);
      String str3 = CertUtils.extractIssuerAliasName(localObject2[0]);
      String str4 = str2 + ":" + str3;
      String str5 = "clientauth.certlist.dialog.browserKS";
      if (localObject3.equals(CertType.PLUGIN))
        str5 = "clientauth.certlist.dialog.javaKS";
      MessageFormat localMessageFormat = new MessageFormat(getMessage(str5));
      Object[] arrayOfObject = { str4 };
      localVector.add(localMessageFormat.format(arrayOfObject));
    }
    localObject1 = new JList();
    ((JList)localObject1).setSelectionMode(0);
    ((JList)localObject1).setListData(localVector);
    if (localVector.size() > 0)
      ((JList)localObject1).setSelectedIndex(0);
    int i = ToolkitStore.getUI().showListDialog(null, getMessage("clientauth.certlist.dialog.caption"), getMessage("clientauth.certlist.dialog.text"), null, true, localVector, paramTreeMap1);
    Object localObject2 = null;
    ToolkitStore.getUI();
    if (i != -1)
    {
      localObject3 = paramTreeMap1.keySet().iterator();
      while ((i >= 0) && (((Iterator)localObject3).hasNext()))
      {
        localObject2 = (String)((Iterator)localObject3).next();
        i--;
      }
    }
    return (String)(String)(String)localObject2;
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  private static int getAcceleratorKey(String paramString)
  {
    return ResourceManager.getAcceleratorKey(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.ClientCertDialog
 * JD-Core Version:    0.6.0
 */