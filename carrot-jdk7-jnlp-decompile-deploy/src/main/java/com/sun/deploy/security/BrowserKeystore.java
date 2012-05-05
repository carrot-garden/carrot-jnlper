package com.sun.deploy.security;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.Platform;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Provider;
import java.security.Security;
import java.text.MessageFormat;
import java.util.HashMap;

public class BrowserKeystore
{
  private static Object cryptoManager = null;
  private static boolean initializeJSS = false;

  public static void registerSecurityProviders()
  {
    if (Config.isJavaVersionAtLeast15())
    {
      Service localService = ServiceManager.getService();
      Provider localProvider;
      if (localService.isIExplorer())
        try
        {
          Class localClass1 = Class.forName("com.sun.deploy.security.MSCryptoProvider", true, ClassLoader.getSystemClassLoader());
          if (localClass1 != null)
          {
            localProvider = (Provider)localClass1.newInstance();
            Security.insertProviderAt(localProvider, Security.getProviders().length + 1);
          }
        }
        catch (Throwable localThrowable1)
        {
        }
      if (localService.isNetscape())
        if (isJSSCryptoConfigured())
        {
          Trace.msgSecurityPrintln("browserkeystore.jss.config");
          try
          {
            Class localClass2 = Class.forName("com.sun.deploy.security.MozillaJSSProvider", true, ClassLoader.getSystemClassLoader());
            if (localClass2 != null)
            {
              localProvider = (Provider)localClass2.newInstance();
              Security.insertProviderAt(localProvider, Security.getProviders().length + 1);
            }
          }
          catch (Throwable localThrowable2)
          {
            Trace.msgSecurityPrintln("browserkeystore.jss.notconfig");
          }
        }
        else
        {
          Trace.msgSecurityPrintln("browserkeystore.jss.notconfig");
        }
    }
  }

  public static synchronized Object getJSSCryptoManager()
  {
    if ((cryptoManager == null) && (!initializeJSS))
    {
      initializeJSS = true;
      String str = null;
      if (Platform.get().isBrowserFireFox())
        str = Platform.get().getFireFoxUserProfileDirectory();
      else
        str = Platform.get().getMozillaUserProfileDirectory();
      Trace.msgSecurityPrintln("browserkeystore.mozilla.dir", new Object[] { str });
      if (str != null)
        try
        {
          Class localClass1 = Class.forName("org.mozilla.jss.CryptoManager$InitializationValues", true, ClassLoader.getSystemClassLoader());
          Constructor localConstructor = localClass1.getConstructor(new Class[] { String.class });
          Object localObject1 = localConstructor.newInstance(new Object[] { str });
          Field localField = localClass1.getField("installJSSProvider");
          localField.setBoolean(localObject1, false);
          Class localClass2 = Class.forName("org.mozilla.jss.CryptoManager", true, ClassLoader.getSystemClassLoader());
          Method localMethod1 = localClass2.getMethod("initialize", new Class[] { localClass1 });
          Object localObject2 = localMethod1.invoke(null, new Object[] { localObject1 });
          Method localMethod2 = localClass2.getMethod("getInstance", null);
          Object localObject3 = localMethod2.invoke(null, null);
          Class localClass3 = Class.forName("org.mozilla.jss.util.PasswordCallback", true, ClassLoader.getSystemClassLoader());
          Method localMethod3 = localClass2.getMethod("setPasswordCallback", new Class[] { localClass3 });
          JSSPasswordCallbackInvocationHandler localJSSPasswordCallbackInvocationHandler = new JSSPasswordCallbackInvocationHandler(null);
          Class localClass4 = Proxy.getProxyClass(ClassLoader.getSystemClassLoader(), new Class[] { localClass3 });
          Object localObject4 = localClass4.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { localJSSPasswordCallbackInvocationHandler });
          localMethod3.invoke(localObject3, new Object[] { localObject4 });
          cryptoManager = localObject3;
          Trace.msgSecurityPrintln("browserkeystore.jss.yes");
        }
        catch (ClassNotFoundException localClassNotFoundException)
        {
          Trace.msgSecurityPrintln("browserkeystore.jss.no");
          return null;
        }
        catch (Throwable localThrowable)
        {
          localThrowable.printStackTrace();
          Trace.msgSecurityPrintln("browserkeystore.jss.no");
          return null;
        }
    }
    return cryptoManager;
  }

  public static boolean isJSSCryptoConfigured()
  {
    return getJSSCryptoManager() != null;
  }

  private static class JSSPasswordCallbackInvocationHandler
    implements InvocationHandler
  {
    private HashMap passwordAttempts = new HashMap();

    private JSSPasswordCallbackInvocationHandler()
    {
    }

    public Object invoke(Object paramObject, Method paramMethod, Object[] paramArrayOfObject)
      throws Throwable
    {
      if ((paramArrayOfObject != null) && (paramArrayOfObject[0] != null))
      {
        localObject = paramArrayOfObject[0];
        Class localClass1 = localObject.getClass();
        Method localMethod = localClass1.getMethod("getName", null);
        String str1 = (String)localMethod.invoke(localObject, null);
        String str2 = paramMethod.getName();
        Integer localInteger = (Integer)this.passwordAttempts.get(str1);
        if ((localInteger == null) || (localInteger.intValue() < 2))
        {
          if (localInteger == null)
            this.passwordAttempts.put(str1, new Integer(1));
          else
            this.passwordAttempts.put(str1, new Integer(localInteger.intValue() + 1));
          char[] arrayOfChar = getPasswordDialog(str1);
          Class localClass2 = Class.forName("org.mozilla.jss.util.Password", true, ClassLoader.getSystemClassLoader());
          Class[] arrayOfClass = { new char[0].getClass() };
          Constructor localConstructor = localClass2.getConstructor(arrayOfClass);
          if (arrayOfChar != null)
          {
            Object[] arrayOfObject = { arrayOfChar };
            return localConstructor.newInstance(arrayOfObject);
          }
        }
      }
      Object localObject = Class.forName("org.mozilla.jss.util.PasswordCallback$GiveUpException", true, ClassLoader.getSystemClassLoader());
      throw ((Throwable)((Class)localObject).newInstance());
    }

    private char[] getPasswordDialog(String paramString)
    {
      MessageFormat localMessageFormat = new MessageFormat(ResourceManager.getMessage("browserkeystore.password.dialog.text"));
      Object[] arrayOfObject = { paramString };
      String str = localMessageFormat.format(arrayOfObject);
      CredentialInfo localCredentialInfo = ToolkitStore.getUI().showPasswordDialog(null, ResourceManager.getMessage("password.dialog.title"), str, false, false, null, false, null);
      if (localCredentialInfo == null)
        return null;
      return localCredentialInfo.getPassword();
    }

    JSSPasswordCallbackInvocationHandler(BrowserKeystore.1 param1)
    {
      this();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.BrowserKeystore
 * JD-Core Version:    0.6.0
 */