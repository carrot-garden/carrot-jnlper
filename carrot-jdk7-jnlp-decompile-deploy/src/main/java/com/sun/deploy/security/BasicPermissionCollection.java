package com.sun.deploy.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.net.URL;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

final class BasicPermissionCollection extends PermissionCollection
  implements Serializable
{
  private static final long serialVersionUID = 739301742472979399L;
  private transient Map perms = new HashMap(11);
  private boolean all_allowed = false;
  private Class permClass;
  private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("permissions", Hashtable.class), new ObjectStreamField("all_allowed", Boolean.TYPE), new ObjectStreamField("permClass", Class.class) };

  public void add(Permission paramPermission)
  {
    if (!(paramPermission instanceof BasicPermission))
      throw new IllegalArgumentException("invalid permission: " + paramPermission);
    if (isReadOnly())
      throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
    BasicPermission localBasicPermission = (BasicPermission)paramPermission;
    if (this.perms.size() == 0)
      this.permClass = localBasicPermission.getClass();
    else if (localBasicPermission.getClass() != this.permClass)
      throw new IllegalArgumentException("invalid permission: " + paramPermission);
    this.perms.put(localBasicPermission.getName(), paramPermission);
    if ((!this.all_allowed) && (localBasicPermission.getName().equals("*")))
      this.all_allowed = true;
  }

  public boolean implies(Permission paramPermission)
  {
    if (!(paramPermission instanceof BasicPermission))
      return false;
    BasicPermission localBasicPermission = (BasicPermission)paramPermission;
    if (localBasicPermission.getClass() != this.permClass)
      return false;
    if (this.all_allowed)
      return true;
    String str = localBasicPermission.getName();
    Permission localPermission1 = (Permission)this.perms.get(str);
    if (localPermission1 != null)
      return localPermission1.implies(paramPermission);
    int i;
    for (int j = str.length() - 1; (i = str.lastIndexOf(".", j)) != -1; j = i - 1)
    {
      str = str.substring(0, i + 1) + "*";
      localPermission1 = (Permission)this.perms.get(str);
      if (localPermission1 != null)
        return localPermission1.implies(paramPermission);
    }
    if (!paramPermission.getName().toLowerCase().startsWith(SecureCookiePermission.ORIGIN_PREFIX + "https:"))
      return true;
    Iterator localIterator = this.perms.values().iterator();
    while (localIterator.hasNext())
    {
      Permission localPermission2 = (Permission)localIterator.next();
      if ((localPermission2.getName().startsWith(SecureCookiePermission.ORIGIN_PREFIX)) && (paramPermission.getName().startsWith(SecureCookiePermission.ORIGIN_PREFIX)))
        try
        {
          URL localURL1 = new URL(localPermission2.getName().substring(SecureCookiePermission.ORIGIN_PREFIX.length()));
          URL localURL2 = new URL(paramPermission.getName().substring(SecureCookiePermission.ORIGIN_PREFIX.length()));
          if ((localURL1.getProtocol().toLowerCase().equals(localURL2.getProtocol().toLowerCase())) && (localURL1.getHost().equals(localURL2.getHost())))
            return true;
        }
        catch (Exception localException)
        {
        }
    }
    return false;
  }

  public Enumeration elements()
  {
    return Collections.enumeration(this.perms.values());
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    Hashtable localHashtable = new Hashtable(this.perms.size() * 2);
    localHashtable.putAll(this.perms);
    ObjectOutputStream.PutField localPutField = paramObjectOutputStream.putFields();
    localPutField.put("all_allowed", this.all_allowed);
    localPutField.put("permissions", localHashtable);
    localPutField.put("permClass", this.permClass);
    paramObjectOutputStream.writeFields();
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    ObjectInputStream.GetField localGetField = paramObjectInputStream.readFields();
    Hashtable localHashtable = (Hashtable)localGetField.get("permissions", null);
    this.perms = new HashMap(localHashtable.size() * 2);
    this.perms.putAll(localHashtable);
    this.all_allowed = localGetField.get("all_allowed", false);
    this.permClass = ((Class)localGetField.get("permClass", null));
    if (this.permClass == null)
    {
      Enumeration localEnumeration = localHashtable.elements();
      if (localEnumeration.hasMoreElements())
      {
        Permission localPermission = (Permission)localEnumeration.nextElement();
        this.permClass = localPermission.getClass();
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.BasicPermissionCollection
 * JD-Core Version:    0.6.0
 */