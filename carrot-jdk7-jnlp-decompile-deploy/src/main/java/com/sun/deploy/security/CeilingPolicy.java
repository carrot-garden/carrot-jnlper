package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Enumeration;
import sun.security.provider.PolicyFile;

public final class CeilingPolicy
{
  private static PermissionCollection _trustedPerms = null;
  private static boolean _initialized = false;

  public static void addTrustedPermissions(PermissionCollection paramPermissionCollection)
  {
    Object localObject;
    if (!_initialized)
    {
      _initialized = true;
      localObject = Config.getStringProperty("deployment.security.trusted.policy");
      if ((localObject != null) && (((String)localObject).length() > 0))
      {
        CodeSource localCodeSource = new CodeSource((URL)null, (Certificate[])null);
        for (int i = 1; Security.getProperty("policy.url." + i) != null; i++);
        String str = "policy.url." + i;
        Security.setProperty(str, (String)localObject);
        try
        {
          PolicyFile localPolicyFile = new PolicyFile();
          _trustedPerms = localPolicyFile.getPermissions(localCodeSource);
        }
        catch (Exception localException)
        {
          Trace.ignoredException(localException);
        }
        Security.setProperty(str, "");
      }
      else
      {
        _trustedPerms = new Permissions();
        _trustedPerms.add(new AllPermission());
      }
    }
    if (_trustedPerms != null)
    {
      localObject = _trustedPerms.elements();
      while (((Enumeration)localObject).hasMoreElements())
        paramPermissionCollection.add((Permission)((Enumeration)localObject).nextElement());
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.CeilingPolicy
 * JD-Core Version:    0.6.0
 */