package com.sun.deploy.config;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.util.URLUtil;
import java.io.File;

public class DefaultConfig extends EmptyConfig
{
  private static DefaultConfig _defaultConfigInstance;
  private static boolean _inInit = false;

  public static synchronized DefaultConfig getDefaultConfig()
  {
    if (_defaultConfigInstance == null)
      _defaultConfigInstance = new DefaultConfig();
    return _defaultConfigInstance;
  }

  public DefaultConfig()
  {
    init(null, null);
  }

  public Object setProperty(String paramString1, String paramString2)
  {
    if (_inInit)
      return super.setProperty(paramString1, paramString2);
    RuntimeException localRuntimeException = new RuntimeException("Attempt changing default config properties");
    Trace.ignored(localRuntimeException);
    return getProperty(paramString1);
  }

  public boolean init(String paramString1, String paramString2)
  {
    _inInit = true;
    clear();
    setProperty("deployment.javapi.trace.filename", "");
    setProperty("deployment.javapi.log.filename", "");
    setProperty("deployment.javaws.associations", "ASK_USER");
    setProperty("deployment.javaws.traceFileName", "");
    setProperty("deployment.javaws.logFileName", "");
    setProperty("deployment.security.TLSv1", "true");
    setProperty("deployment.security.TLSv1.1", "false");
    setProperty("deployment.security.TLSv1.2", "false");
    setProperty("deployment.security.SSLv2Hello", "false");
    setProperty("deployment.security.SSLv3", "true");
    setProperty("deployment.user.cachedir", Config.getDefaultCacheDirectory());
    String str1 = Platform.get().getDefaultSystemCache();
    if (str1 != null)
      setProperty("deployment.system.cachedir", str1);
    setProperty("deployment.user.logdir", LOGDIR_DEF);
    setProperty("deployment.user.tmp", TMPDIR_DEF);
    setProperty("deployment.user.extdir", USR_EXTDIR_DEF);
    String str2 = Platform.get().getUserHome() + File.separator + "security" + File.separator + "java.policy";
    setProperty("deployment.user.security.policy", "file:/" + URLUtil.encodePath(str2));
    setProperty("deployment.user.security.trusted.cacerts", USEC_CACERTS_DEF);
    setProperty("deployment.user.security.trusted.jssecacerts", USEC_JSSECERTS_DEF);
    setProperty("deployment.user.security.trusted.certs", USEC_TRUSTED_CERTS_DEF);
    setProperty("deployment.user.security.trusted.jssecerts", USEC_TRUSTED_JSSE_CERTS_DEF);
    setProperty("deployment.user.security.trusted.clientauthcerts", USEC_TRUSTED_CLIENT_CERTS_DEF);
    setProperty("deployment.user.security.trusted.publishers", USEC_PRETRUST_DEF);
    setProperty("deployment.user.security.blacklist", USEC_BLACKLIST_DEF);
    setProperty("deployment.user.security.trusted.libraries", USEC_TRUSTED_LIBRARIES_DEF);
    setProperty("deployment.user.security.saved.credentials", USEC_CREDENTIAL_DEF);
    setProperty("deployment.system.security.cacerts", SSEC_CACERTS_DEF);
    setProperty("deployment.system.security.oldcacerts", SSEC_OLD_CACERTS_DEF);
    setProperty("deployment.system.security.jssecacerts", SSEC_JSSECERTS_DEF);
    setProperty("deployment.system.security.oldjssecacerts", SSEC_OLD_JSSECERTS_DEF);
    setProperty("deployment.system.security.trusted.certs", SSEC_TRUSTED_CERTS_DEF);
    setProperty("deployment.system.security.trusted.jssecerts", SSEC_TRUSTED_JSSE_CERTS_DEF);
    setProperty("deployment.system.security.trusted.clientauthcerts", SSEC_TRUSTED_CLIENT_CERTS_DEF);
    setProperty("deployment.system.security.trusted.publishers", SSEC_PRETRUST_DEF);
    setProperty("deployment.system.security.blacklist", SSEC_BLACKLIST_DEF);
    setProperty("deployment.system.security.trusted.libraries", SSEC_TRUSTED_LIBRARIES_DEF);
    setProperty("deployment.security.askgrantdialog.show", "true");
    setProperty("deployment.security.askgrantdialog.notinca", "true");
    setProperty("deployment.security.browser.keystore.use", "true");
    setProperty("deployment.security.clientauth.keystore.auto", "true");
    setProperty("deployment.security.pretrust.list", "true");
    setProperty("deployment.security.blacklist.check", "true");
    setProperty("deployment.security.password.cache", "true");
    setProperty("deployment.security.notinca.warning", "true");
    setProperty("deployment.security.expired.warning", "true");
    setProperty("deployment.security.jsse.hostmismatch.warning", "true");
    setProperty("deployment.security.https.warning.show", "false");
    setProperty("deployment.security.trusted.policy", "");
    setProperty("deployment.security.sandbox.awtwarningwindow", "true");
    setProperty("deployment.security.sandbox.jnlp.enhanced", "true");
    setProperty("deployment.security.validation.crl", "false");
    setProperty("deployment.security.validation.ocsp", "false");
    setProperty("deployment.security.validation.ocsp.publisher", "false");
    setProperty("deployment.security.authenticator", "true");
    setProperty("deployment.proxy.type", "3");
    setProperty("deployment.proxy.same", "false");
    setProperty("deployment.proxy.bypass.local", "false");
    setProperty("deployment.proxy.override.hosts", "");
    setProperty("deployment.cache.max.size", "-1");
    setProperty("deployment.cache.jarcompression", "0");
    setProperty("deployment.cache.enabled", "true");
    setProperty("deployment.repository.enabled", "true");
    setProperty("deployment.repository.askdownloaddialog.show", "true");
    setProperty("deployment.console.startup.mode", "HIDE");
    setProperty("deployment.trace", "false");
    setProperty("deployment.log", "false");
    setProperty("deployment.max.output.files", "5");
    setProperty("deployment.max.output.file.size", "10");
    setProperty("deployment.control.panel.log", "false");
    setProperty("deployment.javapi.lifecycle.exception", "false");
    setProperty("deployment.javapi.runtime.type", "0");
    setProperty("deployment.javaws.shortcut", "ASK_IF_HINTED");
    setProperty("deployment.javaws.install", "IF_HINT");
    setProperty("deployment.javaws.uninstall.shortcut", "false");
    setProperty("deployment.capture.mime.types", "false");
    setProperty("deployment.update.mime.types", "true");
    setProperty("deployment.mime.types.use.default", "true");
    setProperty("deployment.javaws.installURL", "http://java.sun.com/products/autodl/j2se");
    setProperty("deployment.security.mixcode", "ENABLE");
    if (Platform.get().canAutoDownloadJRE())
      setProperty("deployment.javaws.autodownload", "ALWAYS");
    else
      setProperty("deployment.javaws.autodownload", "NEVER");
    setProperty("deployment.javaws.muffin.max", "256");
    setProperty("deployment.javaws.update.timeout", "1500");
    setProperty("deployment.javaws.cache.update", "false");
    setProperty("deployment.javapi.cache.update", "false");
    setProperty("deployment.javapi.stop.timeout", "200");
    setProperty("deployment.javaws.concurrentDownloads", "4");
    setProperty("deployment.javaws.home.jnlp.url", "http://java.sun.com/products/javawebstart");
    setProperty("deployment.javafx.mode.enabled", "true");
    setProperty("deployment.baseline.url", "");
    if (getOSName().equals("Windows"))
    {
      setProperty("deployment.browser.vm.iexplorer", "true");
      setProperty("deployment.browser.vm.mozilla", "true");
      setProperty("deployment.system.tray.icon", "false");
    }
    else
    {
      setProperty("deployment.browser.path", "");
      setProperty("deployment.browser.args", "-remote openURL(%u,new-window)");
    }
    setProperty("deployment.insecure.jres", "PROMPT");
    setProperty("deployment.java.update.check", "false");
    String str3 = System.getProperty("javaws.cfg.jauthenticator");
    if (str3 != null)
    {
      String str4 = "" + ((!str3.equalsIgnoreCase("all")) && (!str3.equalsIgnoreCase("true")));
      setProperty("deployment.security.authenticator", str4);
    }
    _inInit = false;
    return true;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.DefaultConfig
 * JD-Core Version:    0.6.0
 */