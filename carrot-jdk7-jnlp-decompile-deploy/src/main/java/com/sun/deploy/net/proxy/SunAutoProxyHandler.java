package com.sun.deploy.net.proxy;

import com.sun.deploy.config.Config;
import com.sun.deploy.trace.Trace;
import java.net.URL;
import java.util.StringTokenizer;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class SunAutoProxyHandler extends AbstractAutoProxyHandler
{
  public ProxyInfo[] getProxyInfo(URL paramURL)
    throws ProxyUnavailableException
  {
    if (!Config.isJavaVersionAtLeast16())
      return fallbackGetProxyInfo(paramURL);
    ScriptEngineManager localScriptEngineManager = new ScriptEngineManager();
    ScriptEngine localScriptEngine = localScriptEngineManager.getEngineByName("js");
    ProxyInfo[] arrayOfProxyInfo;
    try
    {
      localScriptEngine.eval(this.autoProxyScript.toString());
      if ((localScriptEngine instanceof Invocable))
      {
        Invocable localInvocable = (Invocable)localScriptEngine;
        Object localObject = localInvocable.invokeFunction("FindProxyForURL", new Object[] { paramURL.toString(), paramURL.getHost() });
        arrayOfProxyInfo = extractAutoProxySetting((String)localObject);
      }
      else
      {
        Trace.netPrintln("JavaScript engine cannot invoke methods");
        arrayOfProxyInfo = new ProxyInfo[] { new ProxyInfo(null) };
      }
    }
    catch (Exception localException)
    {
      Trace.netPrintException(localException);
      arrayOfProxyInfo = fallbackGetProxyInfo(paramURL);
    }
    return arrayOfProxyInfo;
  }

  ProxyInfo[] fallbackGetProxyInfo(URL paramURL)
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore_2
    //   2: aload_0
    //   3: getfield 112	com/sun/deploy/net/proxy/SunAutoProxyHandler:jsPacScript	Ljava/lang/String;
    //   6: ifnull +123 -> 129
    //   9: new 71	java/util/StringTokenizer
    //   12: dup
    //   13: aload_0
    //   14: getfield 112	com/sun/deploy/net/proxy/SunAutoProxyHandler:jsPacScript	Ljava/lang/String;
    //   17: ldc 2
    //   19: iconst_0
    //   20: invokespecial 132	java/util/StringTokenizer:<init>	(Ljava/lang/String;Ljava/lang/String;Z)V
    //   23: astore_3
    //   24: aload_3
    //   25: invokevirtual 130	java/util/StringTokenizer:hasMoreTokens	()Z
    //   28: ifeq +101 -> 129
    //   31: aload_3
    //   32: invokevirtual 131	java/util/StringTokenizer:nextToken	()Ljava/lang/String;
    //   35: astore 4
    //   37: aload 4
    //   39: ldc 3
    //   41: invokevirtual 125	java/lang/String:indexOf	(Ljava/lang/String;)I
    //   44: istore 5
    //   46: aload 4
    //   48: ldc 6
    //   50: invokevirtual 125	java/lang/String:indexOf	(Ljava/lang/String;)I
    //   53: istore 6
    //   55: aload 4
    //   57: ldc 7
    //   59: invokevirtual 125	java/lang/String:indexOf	(Ljava/lang/String;)I
    //   62: istore 7
    //   64: aload_0
    //   65: iload 5
    //   67: aload_0
    //   68: iload 6
    //   70: iload 7
    //   72: invokespecial 117	com/sun/deploy/net/proxy/SunAutoProxyHandler:positiveMin	(II)I
    //   75: invokespecial 117	com/sun/deploy/net/proxy/SunAutoProxyHandler:positiveMin	(II)I
    //   78: istore 8
    //   80: aload 4
    //   82: ldc 1
    //   84: invokevirtual 126	java/lang/String:lastIndexOf	(Ljava/lang/String;)I
    //   87: istore 9
    //   89: iload 8
    //   91: iconst_m1
    //   92: if_icmpne +6 -> 98
    //   95: goto -71 -> 24
    //   98: iload 9
    //   100: iload 8
    //   102: if_icmpgt +14 -> 116
    //   105: aload 4
    //   107: iload 8
    //   109: invokevirtual 123	java/lang/String:substring	(I)Ljava/lang/String;
    //   112: astore_2
    //   113: goto +16 -> 129
    //   116: aload 4
    //   118: iload 8
    //   120: iload 9
    //   122: invokevirtual 124	java/lang/String:substring	(II)Ljava/lang/String;
    //   125: astore_2
    //   126: goto +3 -> 129
    //   129: aload_0
    //   130: aload_2
    //   131: invokevirtual 118	com/sun/deploy/net/proxy/SunAutoProxyHandler:extractAutoProxySetting	(Ljava/lang/String;)[Lcom/sun/deploy/net/proxy/ProxyInfo;
    //   134: areturn
    //   135: astore_2
    //   136: ldc 9
    //   138: invokestatic 120	com/sun/deploy/trace/Trace:msgNetPrintln	(Ljava/lang/String;)V
    //   141: iconst_1
    //   142: anewarray 61	com/sun/deploy/net/proxy/ProxyInfo
    //   145: dup
    //   146: iconst_0
    //   147: new 61	com/sun/deploy/net/proxy/ProxyInfo
    //   150: dup
    //   151: aconst_null
    //   152: invokespecial 116	com/sun/deploy/net/proxy/ProxyInfo:<init>	(Ljava/lang/String;)V
    //   155: aastore
    //   156: areturn
    //
    // Exception table:
    //   from	to	target	type
    //   0	134	135	java/lang/Throwable
  }

  private int positiveMin(int paramInt1, int paramInt2)
  {
    if (paramInt1 < 0)
      return paramInt2;
    if (paramInt2 < 0)
      return paramInt1;
    if (paramInt1 > paramInt2)
      return paramInt2;
    return paramInt1;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.net.proxy.SunAutoProxyHandler
 * JD-Core Version:    0.6.0
 */