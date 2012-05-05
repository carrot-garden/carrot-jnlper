package com.sun.deploy.config;

import com.sun.deploy.Environment;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.util.DeploymentHooks;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract class Config extends Properties
{
  private static final String SYSPROP_OS_NAME = "os.name";
  protected static final String PROPERTIES_FILE = "deployment.properties";
  protected static final String CONFIG_FILE = "deployment.config";
  protected static final String BASE = "deployment.";
  protected static final String USER = "deployment.user.";
  protected static final String SYSTEM = "deployment.system.";
  protected static final String SEC = "deployment.security.";
  protected static final String USEC = "deployment.user.security.";
  protected static final String SSEC = "deployment.system.security.";
  protected static final String PROX = "deployment.proxy.";
  protected static final String JAVAPI = "deployment.javapi.";
  protected static final String JAVAWS = "deployment.javaws.";
  public static final String VERSION_UPDATED_KEY = "deployment.version";
  public static final String VERSION_UPDATE_THIS = "6.0";
  public static final String SYSTEM_CACHEDIR_NAME = "SystemCache";
  public static final String CACHEDIR_NAME = "cache";
  public static final String CACHEDIR_KEY = "deployment.user.cachedir";
  public static final String SYSCACHE_KEY = "deployment.system.cachedir";
  public static final String SEC_TLSv1_KEY = "deployment.security.TLSv1";
  public static final boolean SEC_TLSv1_DEF = true;
  public static final String SEC_TLSv11_KEY = "deployment.security.TLSv1.1";
  public static final boolean SEC_TLSv11_DEF = false;
  public static final String SEC_TLSv12_KEY = "deployment.security.TLSv1.2";
  public static final boolean SEC_TLSv12_DEF = false;
  public static final String SEC_SSLv2_KEY = "deployment.security.SSLv2Hello";
  public static final boolean SEC_SSLv2_DEF = false;
  public static final String SEC_SSLv3_KEY = "deployment.security.SSLv3";
  public static final boolean SEC_SSLv3_DEF = true;
  public static final String BASELINE_DEBUG_KEY = "deployment.baseline.debug";
  public static final String BASELINE_URL_KEY = "deployment.baseline.url";
  public static final String BASELINE_URL_DEF = "";
  public static final String JAVAWS_CACHE_KEY = "deployment.javaws.cachedir";
  public static final String JAVAPI_CACHE_KEY = "deployment.javapi.cachedir";
  public static final String JAVAWS_UPDATE_KEY = "deployment.javaws.cache.update";
  public static final boolean JAVAWS_UPDATE_DEF = false;
  public static final String JAVAPI_UPDATE_KEY = "deployment.javapi.cache.update";
  public static final boolean JAVAPI_UPDATE_DEF = false;
  public static final String LOGDIR_KEY = "deployment.user.logdir";
  public static final String LOGDIR_DEF = "$USER_HOME" + File.separator + "log";
  public static final String PLUGIN_OUTPUTFILE_PREFIX = "plugin";
  public static final String JAVAWS_OUTPUTFILE_PREFIX = "javaws";
  public static final String OUTPUTFILE_LOG_SUFFIX = ".log";
  public static final String OUTPUTFILE_TRACE_SUFFIX = ".trace";
  public static final String JAVAWS_TRACEFILE_KEY = "deployment.javaws.traceFileName";
  public static final String JAVAWS_TRACEFILE_DEF = "";
  public static final String JAVAWS_LOGFILE_KEY = "deployment.javaws.logFileName";
  public static final String JAVAWS_LOGFILE_DEF = "";
  public static final String TMPDIR_KEY = "deployment.user.tmp";
  public static final String TMPDIR_DEF = "$USER_HOME" + File.separator + "tmp";
  public static final String USR_EXTDIR_KEY = "deployment.user.extdir";
  public static final String USR_EXTDIR_DEF = "$USER_HOME" + File.separator + "ext";
  public static final String SYS_EXTDIR_KEY = "deployment.system.extdir";
  public static final String SYS_MAX_APPLET_THREAD_COUNT_KEY = "deployment.system.maxAppletThreadCount";
  public static final String USEC_POLICY_KEY = "deployment.user.security.policy";
  public static final String USEC_CACERTS_KEY = "deployment.user.security.trusted.cacerts";
  public static final String USEC_CACERTS_DEF = "$USER_HOME" + File.separator + "security" + File.separator + "trusted.cacerts";
  public static final String USEC_JSSECERTS_KEY = "deployment.user.security.trusted.jssecacerts";
  public static final String USEC_JSSECERTS_DEF = "$USER_HOME" + File.separator + "security" + File.separator + "trusted.jssecacerts";
  public static final String USEC_TRUSTED_CERTS_KEY = "deployment.user.security.trusted.certs";
  public static final String USEC_TRUSTED_CERTS_DEF = "$USER_HOME" + File.separator + "security" + File.separator + "trusted.certs";
  public static final String USEC_TRUSTED_JSSE_CERTS_KEY = "deployment.user.security.trusted.jssecerts";
  public static final String USEC_TRUSTED_JSSE_CERTS_DEF = "$USER_HOME" + File.separator + "security" + File.separator + "trusted.jssecerts";
  public static final String USEC_TRUSTED_CLIENT_CERTS_KEY = "deployment.user.security.trusted.clientauthcerts";
  public static final String USEC_TRUSTED_CLIENT_CERTS_DEF = "$USER_HOME" + File.separator + "security" + File.separator + "trusted.clientcerts";
  public static final String USEC_PRETRUST_KEY = "deployment.user.security.trusted.publishers";
  public static final String USEC_PRETRUST_DEF = "$USER_HOME" + File.separator + "security" + File.separator + "trusted.publishers";
  public static final String USEC_BLACKLIST_KEY = "deployment.user.security.blacklist";
  public static final String USEC_BLACKLIST_DEF = "$USER_HOME" + File.separator + "security" + File.separator + "blacklist";
  public static final String USEC_TRUSTED_LIBRARIES_KEY = "deployment.user.security.trusted.libraries";
  public static final String USEC_TRUSTED_LIBRARIES_DEF = "$USER_HOME" + File.separator + "security" + File.separator + "trusted.libraries";
  public static final String USEC_CREDENTIAL_KEY = "deployment.user.security.saved.credentials";
  public static final String USEC_CREDENTIAL_DEF = "$USER_HOME" + File.separator + "security" + File.separator + "auth.dat";
  public static final String SSEC_POLICY_KEY = "deployment.system.security.policy";
  public static final String SSEC_CACERTS_KEY = "deployment.system.security.cacerts";
  public static final String SSEC_CACERTS_DEF = "$LATEST_JRE_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts";
  public static final String SSEC_OLD_CACERTS_KEY = "deployment.system.security.oldcacerts";
  public static final String SSEC_OLD_CACERTS_DEF = "$JRE_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts";
  public static final String SSEC_JSSECERTS_KEY = "deployment.system.security.jssecacerts";
  public static final String SSEC_JSSECERTS_DEF = "$LATEST_JRE_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "jssecacerts";
  public static final String SSEC_OLD_JSSECERTS_KEY = "deployment.system.security.oldjssecacerts";
  public static final String SSEC_OLD_JSSECERTS_DEF = "$JRE_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "jssecacerts";
  public static final String SSEC_TRUSTED_CERTS_KEY = "deployment.system.security.trusted.certs";
  public static final String SSEC_TRUSTED_CERTS_DEF = "$JAVA_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "trusted.certs";
  public static final String SSEC_TRUSTED_JSSE_CERTS_KEY = "deployment.system.security.trusted.jssecerts";
  public static final String SSEC_TRUSTED_JSSE_CERTS_DEF = "$JAVA_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "trusted.jssecerts";
  public static final String SSEC_TRUSTED_CLIENT_CERTS_KEY = "deployment.system.security.trusted.clientauthcerts";
  public static final String SSEC_TRUSTED_CLIENT_CERTS_DEF = "$JAVA_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "trusted.clientcerts";
  public static final String SSEC_PRETRUST_KEY = "deployment.system.security.trusted.publishers";
  public static final String SSEC_PRETRUST_DEF = "$LATEST_JRE_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "trusted.publishers";
  public static final String SSEC_BLACKLIST_KEY = "deployment.system.security.blacklist";
  public static final String SSEC_BLACKLIST_DEF = "$LATEST_JRE_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "blacklist";
  public static final String SSEC_TRUSTED_LIBRARIES_KEY = "deployment.system.security.trusted.libraries";
  public static final String SSEC_TRUSTED_LIBRARIES_DEF = "$LATEST_JRE_HOME" + File.separator + "lib" + File.separator + "security" + File.separator + "trusted.libraries";
  public static final String APPCONTEXT_APP_NAME_KEY = "deploy.trust.decider.app.name";
  public static final String SEC_ASKGRANT_SHOW_KEY = "deployment.security.askgrantdialog.show";
  public static final boolean SEC_ASKGRANT_SHOW_DEF = true;
  public static final String SEC_ASKGRANT_NOTCA_KEY = "deployment.security.askgrantdialog.notinca";
  public static final boolean SEC_ASKGRANT_NOTCA_DEF = true;
  public static final String SEC_USE_BROWSER_KEYSTORE_KEY = "deployment.security.browser.keystore.use";
  public static final boolean SEC_USE_BROWSER_KEYSTORE_DEF = true;
  public static final String SEC_USE_CLIENTAUTH_AUTO_KEY = "deployment.security.clientauth.keystore.auto";
  public static final boolean SEC_USE_CLIENTAUTH_AUTO_DEF = true;
  public static final String SEC_NOTINCA_WARN_KEY = "deployment.security.notinca.warning";
  public static final boolean SEC_NOTINCA_WARN_DEF = true;
  public static final String SEC_EXPIRED_WARN_KEY = "deployment.security.expired.warning";
  public static final boolean SEC_EXPIRED_WARN_DEF = true;
  public static final String SEC_JSSE_HOST_WARN_KEY = "deployment.security.jsse.hostmismatch.warning";
  public static final boolean SEC_JSSE_HOST_WARN_DEF = true;
  public static final String SEC_HTTPS_DIALOG_WARN_KEY = "deployment.security.https.warning.show";
  public static final boolean SEC_HTTPS_DIALOG_WARN_DEF = false;
  public static final String SEC_TRUSTED_POLICY_KEY = "deployment.security.trusted.policy";
  public static final String SEC_TRUSTED_POLICY_DEF = "";
  public static final String SEC_AWT_WARN_WINDOW_KEY = "deployment.security.sandbox.awtwarningwindow";
  public static final boolean SEC_AWT_WARN_WINDOW_DEF = true;
  public static final String SEC_SANDBOX_JNLP_ENHANCED_KEY = "deployment.security.sandbox.jnlp.enhanced";
  public static final boolean SEC_SANDBOX_JNLP_ENHANCED_DEF = true;
  public static final String SEC_USE_VALIDATION_CRL_KEY = "deployment.security.validation.crl";
  public static final boolean SEC_USE_VALIDATION_CRL_DEF = false;
  public static final String SEC_USE_VALIDATION_CRL_URL_KEY = "deployment.security.validation.crl.url";
  public static final String SEC_USE_VALIDATION_OCSP_KEY = "deployment.security.validation.ocsp";
  public static final boolean SEC_USE_VALIDATION_OCSP_DEF = false;
  public static final String SEC_USE_VALIDATION_OCSP_EE_KEY = "deployment.security.validation.ocsp.publisher";
  public static final boolean SEC_USE_VALIDATION_OCSP_EE_DEF = false;
  public static final String SEC_USE_VALIDATION_OCSP_SIGNER_KEY = "deployment.security.validation.ocsp.signer";
  public static final String SEC_USE_VALIDATION_OCSP_URL_KEY = "deployment.security.validation.ocsp.url";
  public static final String SEC_AUTHENTICATOR_KEY = "deployment.security.authenticator";
  public static final boolean SEC_AUTHENTICATOR_DEF = true;
  public static final String SEC_USE_PRETRUST_LIST_KEY = "deployment.security.pretrust.list";
  public static final boolean SEC_USE_PRETRUST_LIST_DEF = true;
  public static final String SEC_USE_BLACKLIST_CHECK_KEY = "deployment.security.blacklist.check";
  public static final boolean SEC_USE_BLACKLIST_CHECK_DEF = true;
  public static final String SEC_USE_PASSWORD_CACHE_KEY = "deployment.security.password.cache";
  public static final boolean SEC_USE_PASSWORD_CACHE_DEF = true;
  public static final String REMOTE_JNLP_DOCS_KEY = "deployment.javaws.home.jnlp.url";
  public static final String REMOTE_JNLP_DOCS_DEF = "http://java.sun.com/products/javawebstart";
  public static final String SSV_MODE_ALWAYS = "ALWAYS";
  public static final String SSV_MODE_PROMPT = "PROMPT";
  public static final String SSV_MODE_NEVER = "NEVER";
  public static final String SSV_MODE_KEY = "deployment.insecure.jres";
  public static final String SSV_MODE_DEF = "PROMPT";
  public static final String SSV_UPDATE_KEY = "deployment.java.update.check";
  public static final boolean SSV_UPDATE_DEF = false;
  public static final int PROX_TYPE_UNKNOWN = -1;
  public static final int PROX_TYPE_NONE = 0;
  public static final int PROX_TYPE_MANUAL = 1;
  public static final int PROX_TYPE_AUTO = 2;
  public static final int PROX_TYPE_BROWSER = 3;
  public static final int PROX_TYPE_SYSTEM = 4;
  public static final String PROX_TYPE_KEY = "deployment.proxy.type";
  public static final int PROX_TYPE_DEF = 3;
  public static final String PROX_SAME_KEY = "deployment.proxy.same";
  public static final boolean PROX_SAME_DEF = false;
  public static final String PROX_LOCAL_KEY = "deployment.proxy.bypass.local";
  public static final boolean PROX_LOCAL_DEF = false;
  public static final String PROX_AUTOCFG_KEY = "deployment.proxy.auto.config.url";
  public static final String PROX_BYPASS_KEY = "deployment.proxy.bypass.list";
  public static final String PROX_HTTP_HOST_KEY = "deployment.proxy.http.host";
  public static final String PROX_HTTP_PORT_KEY = "deployment.proxy.http.port";
  public static final String PROX_HTTPS_HOST_KEY = "deployment.proxy.https.host";
  public static final String PROX_HTTPS_PORT_KEY = "deployment.proxy.https.port";
  public static final String PROX_FTP_HOST_KEY = "deployment.proxy.ftp.host";
  public static final String PROX_FTP_PORT_KEY = "deployment.proxy.ftp.port";
  public static final String PROX_SOX_HOST_KEY = "deployment.proxy.socks.host";
  public static final String PROX_SOX_PORT_KEY = "deployment.proxy.socks.port";
  public static final String PROX_OVERRIDE_KEY = "deployment.proxy.override.hosts";
  public static final String PROX_OVERRIDE_DEF = "";
  protected static final String ACTIVE_PREFIX = "active.";
  protected static final String[] PROXY_KEYS = { "deployment.proxy.type", "deployment.proxy.same", "deployment.proxy.bypass.local", "deployment.proxy.auto.config.url", "deployment.proxy.bypass.list", "deployment.proxy.http.host", "deployment.proxy.http.port", "deployment.proxy.https.host", "deployment.proxy.https.port", "deployment.proxy.ftp.host", "deployment.proxy.ftp.port", "deployment.proxy.socks.host", "deployment.proxy.socks.port", "deployment.proxy.override.hosts" };
  public static final String CACHE_MAX_KEY = "deployment.cache.max.size";
  public static final String CACHE_MAX_DEF = "-1";
  public static final String CACHE_COMPRESSION_KEY = "deployment.cache.jarcompression";
  public static final int CACHE_COMPRESSION_DEF = 0;
  public static final String CACHE_ENABLED_KEY = "deployment.cache.enabled";
  public static final boolean CACHE_ENABLED_DEF = true;
  public static final String OPR_ENABLED_KEY = "deployment.repository.enabled";
  public static final boolean OPR_ENABLED_DEF = true;
  public static final String OPR_ASK_SHOW_KEY = "deployment.repository.askdownloaddialog.show";
  public static final boolean OPR_ASK_SHOW_DEF = true;
  public static final String SPLASH_CACHE_INDEX_KEY = "deployment.javaws.splash.index";
  public static final String CONSOLE_MODE_HIDE = "HIDE";
  public static final String CONSOLE_MODE_SHOW = "SHOW";
  public static final String CONSOLE_MODE_DISABLED = "DISABLE";
  public static final String CONSOLE_MODE_KEY = "deployment.console.startup.mode";
  public static final String CONSOLE_MODE_DEF = "HIDE";
  public static final String TRACE_MODE_KEY = "deployment.trace";
  public static final boolean TRACE_MODE_DEF = false;
  public static final String TRACE_LEVEL_KEY = "deployment.trace.level";
  public static final String MAX_NUM_FILES_KEY = "deployment.max.output.files";
  public static final int MAX_NUM_FILES_DEF = 5;
  public static final String MAX_SIZE_FILE_KEY = "deployment.max.output.file.size";
  public static final int MAX_SIZE_FILE_DEF = 10;
  public static final String LOG_MODE_KEY = "deployment.log";
  public static final boolean LOG_MODE_DEF = false;
  public static final String LOG_CP_KEY = "deployment.control.panel.log";
  public static final boolean LOG_CP_DEF = false;
  public static final String USE_SYSTEM_LF_KEY = "deployment.system.lookandfeel";
  public static final String JPI_TRACE_FILE_KEY = "deployment.javapi.trace.filename";
  public static final String JPI_TRACE_FILE_DEF = "";
  public static final String JPI_LOG_FILE_KEY = "deployment.javapi.log.filename";
  public static final String JPI_LOG_FILE_DEF = "";
  public static final String SHOW_EXCEPTIONS_KEY = "deployment.javapi.lifecycle.exception";
  public static final boolean SHOW_EXCEPTIONS_DEF = false;
  public static final int JPI_RUNTIME_TYPE_JRE = 0;
  public static final int JPI_RUNTIME_TYPE_JDK = 1;
  public static final String BROWSER_VM_IEXPLORER_KEY = "deployment.browser.vm.iexplorer";
  public static final boolean BROWSER_VM_IEXPLORER_DEF = true;
  public static final String BROWSER_VM_MOZILLA_KEY = "deployment.browser.vm.mozilla";
  public static final boolean BROWSER_VM_MOZILLA_DEF = true;
  public static final String SYSTEM_TRAY_ICON_KEY = "deployment.system.tray.icon";
  public static final boolean SYSTEM_TRAY_ICON_DEF = false;
  public static final String JPI_RUNTIME_VER_KEY = "deployment.javapi.runtime.version";
  public static final String JPI_RUNTIME_VER_DEF = "";
  public static final String JPI_RUNTIME_TYPE_KEY = "deployment.javapi.runtime.type";
  public static final int JPI_RUNTIME_TYPE_DEF = 0;
  public static final String JAVAPI_STOP_TIMEOUT_KEY = "deployment.javapi.stop.timeout";
  public static final int JAVAPI_STOP_TIMEOUT_DEF = 200;
  public static final int JAVAPI_STOP_TIMEOUT_MAX = 3000;
  public static final String JAVAWS_CONCURRENT_DOWNLOADS_KEY = "deployment.javaws.concurrentDownloads";
  public static final int JAVAWS_CONCURRENT_DOWNLOADS_DEF = 4;
  public static final int JAVAWS_CONCURRENT_DOWNLOADS_MAX = 10;
  public static final String JPI_JAVA_PATH = ".path";
  public static final String JPI_JAVA_ARGS = ".args";
  public static final String JPI_JAVA_OSNAME = ".osname";
  public static final String JPI_JAVA_OSARCH = ".osarch";
  public static final String JPI_JAVA_ENABLED = ".enabled";
  public static final String JPI_JRE_KEY = "deployment.javapi.jre.";
  public static final String JPI_JDK_KEY = "deployment.javapi.jdk.";
  public static final String ASSOCIATION_MODE_KEY = "deployment.javaws.associations";
  public static final String ASSOCIATION_MODE_ALWAYS = "ALWAYS";
  public static final String ASSOCIATION_MODE_NEVER = "NEVER";
  public static final String ASSOCIATION_MODE_NEW_ONLY = "NEW_ONLY";
  public static final String ASSOCIATION_MODE_ASK_USER = "ASK_USER";
  public static final String ASSOCIATION_MODE_REPLACE_ASK = "REPLACE_ASK";
  public static final String ASSOCIATION_MODE_DEF = "ASK_USER";
  public static final int ASSOCIATION_NEVER = 0;
  public static final int ASSOCIATION_NEW_ONLY = 1;
  public static final int ASSOCIATION_ASK_USER = 2;
  public static final int ASSOCIATION_REPLACE_ASK = 3;
  public static final int ASSOCIATION_ALWAYS = 4;
  public static final int SHORTCUT_NEVER = 0;
  public static final int SHORTCUT_NO = 0;
  public static final int SHORTCUT_ALWAYS = 1;
  public static final int SHORTCUT_YES = 1;
  public static final int SHORTCUT_ASK_USER = 2;
  public static final int SHORTCUT_ASK_IF_HINTED = 3;
  public static final int SHORTCUT_ALWAYS_IF_HINTED = 4;
  public static final String SHORTCUT_MODE_NEVER = "NEVER";
  public static final String SHORTCUT_MODE_ALWAYS = "ALWAYS";
  public static final String SHORTCUT_MODE_ASK_USER = "ASK_USER";
  public static final String SHORTCUT_MODE_ASK_IF_HINTED = "ASK_IF_HINTED";
  public static final String SHORTCUT_MODE_ALWAYS_IF_HINTED = "ALWAYS_IF_HINTED";
  public static final String SHORTCUT_MODE_KEY = "deployment.javaws.shortcut";
  public static final String SHORTCUT_MODE_DEF = "ASK_IF_HINTED";
  public static final int INSTALL_NEVER = 0;
  public static final int INSTALL_IF_SHORTCUT = 1;
  public static final int INSTALL_IF_HINT_AND_SHORTCUT = 2;
  public static final int INSTALL_IF_HINT = 3;
  public static final String INSTALL_MODE_NEVER = "NEVER";
  public static final String INSTALL_MODE_IF_SHORTCUT = "IF_SHORTCUT";
  public static final String INSTALL_MODE_IF_HINT_AND_SHORTCUT = "IF_HINT_AND_SHORTCUT";
  public static final String INSTALL_MODE_IF_HINT = "IF_HINT";
  public static final String INSTALL_MODE_KEY = "deployment.javaws.install";
  public static final String INSTALL_MODE_DEF = "IF_HINT";
  public static final String SHORTCUT_UNINSTALL_KEY = "deployment.javaws.uninstall.shortcut";
  public static final boolean SHORTCUT_UNINSTALL_DEF = false;
  public static final String JAVAWS_JRE_PLATFORM_ID = ".platform";
  public static final String JAVAWS_JRE_PRODUCT_ID = ".product";
  public static final String JAVAWS_JRE_LOCATION = ".location";
  public static final String JAVAWS_JRE_PATH = ".path";
  public static final String JAVAWS_JRE_ARGS = ".args";
  public static final String JAVAWS_JRE_OS_ARCH = ".osarch";
  public static final String JAVAWS_JRE_OS_NAME = ".osname";
  public static final String JAVAWS_JRE_ISENABLED = ".enabled";
  public static final String JAVAWS_JRE_ISREGISTERED = ".registered";
  public static final String JAVAWS_JRE_KEY = "deployment.javaws.jre.";
  public static final String JAVAWS_JRE_INSTALL_KEY = "deployment.javaws.installURL";
  public static final String JAVAWS_JRE_INSTALL_DEF = "http://java.sun.com/products/autodl/j2se";
  public static final String JAVAFX_INSTALLER_URL = "http://javaweb.sfbay.sun.com/~hj156752/awtless/fx/installer/fxinstaller.jnlp";
  public static final String AUTODOWNLOAD_MODE_ALWAYS = "ALWAYS";
  public static final String AUTODOWNLOAD_MODE_PROMPT = "PROMPT";
  public static final String AUTODOWNLOAD_MODE_NEVER = "NEVER";
  public static final String JAVAWS_JRE_AUTODOWNLOAD_KEY = "deployment.javaws.autodownload";
  public static final String JAVAWS_JRE_AUTODOWNLOAD_DEF = "ALWAYS";
  public static final int MIXCODE_ENABLE = 0;
  public static final int MIXCODE_HIDE_RUN = 1;
  public static final int MIXCODE_HIDE_CANCEL = 2;
  public static final int MIXCODE_DISABLE = 3;
  public static final String MIXCODE_MODE_ENABLE = "ENABLE";
  public static final String MIXCODE_MODE_HIDE_RUN = "HIDE_RUN";
  public static final String MIXCODE_MODE_HIDE_CANCEL = "HIDE_CANCEL";
  public static final String MIXCODE_MODE_DISABLE = "DISABLE";
  public static final String MIXCODE_MODE_KEY = "deployment.security.mixcode";
  public static final String MIXCODE_MODE_DEF = "ENABLE";
  public static final String JAUTHENTICATOR_SYSTEM_PROP = "javaws.cfg.jauthenticator";
  public static final String BROWSER_PATH_KEY = "deployment.browser.path";
  public static final String BROWSER_PATH_DEF = "";
  public static final String EXTENDED_BROWSER_ARGS_KEY = "deployment.browser.args";
  public static final String EXTENDED_BROWSER_ARGS_DEF = "-remote openURL(%u,new-window)";
  public static final String CAPTURE_MIME_KEY = "deployment.capture.mime.types";
  public static final boolean CAPTURE_MIME_DEF = false;
  public static final String UPDATE_MIME_KEY = "deployment.update.mime.types";
  public static final boolean UPDATE_MIME_DEF = true;
  public static final String MIME_DEFAULTS_KEY = "deployment.mime.types.use.default";
  public static final boolean MIME_DEFAULTS_MIME_DEF = true;
  public static final String JAVAWS_MUFFIN_LIMIT_KEY = "deployment.javaws.muffin.max";
  public static final int JAVAWS_MUFFIN_LIMIT_DEF = 256;
  public static final String JAVAWS_UPDATE_TIMEOUT_KEY = "deployment.javaws.update.timeout";
  public static final int JAVAWS_UPDATE_TIMEOUT_DEF = 1500;
  public static final String SECURE_PROPS_KEY = "deployment.javaws.secure.properties";
  public static final String JQS_KEY = "java.quick.starter";
  public static final boolean JQS_DEF = false;
  public static final String ENABLE_JAVAFX_KEY = "deployment.javafx.mode.enabled";
  public static final boolean ENABLE_JAVAFX_DEF = true;
  public static final String USE_NEW_PLUGIN_KEY = "deployment.jpi.mode.new";
  public static final boolean USE_NEW_PLUGIN_DEF = true;
  public static final String APPCONTEXT_KEY_PREFIX = "deploy-";
  private static final String[] DefaultSecureProperties = { "sun.java2d.noddraw", "javaws.cfg.jauthenticator", "swing.useSystemFontSettings", "swing.metalTheme", "http.agent", "http.keepAlive", "sun.awt.noerasebackground", "sun.java2d.opengl", "sun.java2d.d3d", "java.awt.syncLWRequests", "java.awt.Window.locationByPlatform", "sun.awt.erasebackgroundonresize", "sun.awt.keepWorkingSetOnMinimize", "swing.noxp", "swing.boldMetal", "awt.useSystemAAFontSettings", "sun.java2d.dpiaware", "sun.awt.disableMixing", "sun.lang.ClassLoader.allowArraySyntax", "java.awt.smartInvalidate" };
  private static String[] secureVmArgs = { "-d32", "-client", "-server", "-verbose", "-version", "-showversion", "-help", "-X", "-ea", "-enableassertions", "-da", "-disableassertions", "-esa", "-enablesystemassertions", "-dsa", "-disablesystemassertions", "-Xmixed", "-Xint", "-Xnoclassgc", "-Xincgc", "-Xbatch", "-Xprof", "-Xdebug", "-Xfuture", "-Xrs", "-XX:+ForceTimeHighResolution", "-XX:-ForceTimeHighResolution", "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps", "-XX:+PrintHeapAtGC", "-XX:PrintCMSStatistics", "-XX:+PrintTenuringDistribution", "-XX:+TraceClassUnloading", "-XX:SurvivorRatio", "-XX:MaxTenuringThreshol", "-XX:CMSMarkStackSize", "-XX:CMSMarkStackSizeMax", "-XX:+CMSClassUnloadingEnabled", "-XX:+CMSIncrementalMode", "-XX:CMSIncrementalDutyCycleMin", "-XX:+CMSIncrementalPacing", "-XX:CMSInitiatingOccupancyFraction", "-XX:+UseConcMarkSweepGC", "-XX:-ParallelRefProcEnabled", "-XX:ParallelGCThreads", "-XX:ParallelCMSThreads", "-XX:+DisableExplicitGC", "-XX:+UseCompressedOops", "-XX:+UseG1GC", "-XX:GCPauseIntervalMillis", "-XX:MaxGCPauseMillis", "-XX:+HeapDumpOnOutOfMemoryError" };
  private static String[] secureVmPrefixes = { "-ea:", "-enableassertions:", "-da:", "-disableassertions:", "-verbose:", "-Xmn", "-Xms", "-Xmx", "-Xss", "-XX:NewRatio", "-XX:NewSize", "-XX:MaxNewSize", "-XX:PermSize", "-XX:MaxPermSize", "-XX:MaxHeapFreeRatio", "-XX:MinHeapFreeRatio", "-XX:UseSerialGC", "-XX:ThreadStackSize", "-XX:MaxInlineSize", "-XX:ReservedCodeCacheSize", "-XX:MaxDirectMemorySize" };
  private static DeploymentHooks deploymentHooks = new DeploymentHooks();
  private static final String _javaVersionProperty = System.getProperty("java.version");
  private static final String _javaRuntimeNameProperty = System.getProperty("java.runtime.name");
  private static final boolean _atLeast13 = !_javaVersionProperty.startsWith("1.2");
  private static final boolean _atLeast14 = (_atLeast13) && (!_javaVersionProperty.startsWith("1.3"));
  private static final boolean _atLeast15 = (_atLeast14) && (!_javaVersionProperty.startsWith("1.4"));
  private static final boolean _atLeast16 = (_atLeast15) && (!_javaVersionProperty.startsWith("1.5"));
  private static final boolean _atLeast17 = (_atLeast16) && (!_javaVersionProperty.startsWith("1.6"));
  private static Config _config = null;
  private static String _jreHome = System.getProperty("java.home");
  private static String _latestJREHome = null;
  public static final String SKIP_CACHE_UPGRADE = "deployment.cache.upgrade.skip";
  private String _osFullName;
  private static final String _os;
  private static final String _arch;
  private static final String _platform;
  private static boolean _debugSet;
  private static boolean _debugPlugin;
  private static boolean _debugDeploy;

  public static DeploymentHooks getHooks()
  {
    return deploymentHooks;
  }

  public static void setupPackageAccessRestriction()
  {
    addToSecurityProperty("package.access", "com.sun.javaws");
    addToSecurityProperty("package.access", "com.sun.deploy");
    addToSecurityProperty("package.access", "com.sun.jnlp");
    addToSecurityProperty("package.definition", "com.sun.javaws");
    addToSecurityProperty("package.definition", "com.sun.deploy");
    addToSecurityProperty("package.definition", "com.sun.jnlp");
    addToSecurityProperty("package.access", "org.mozilla.jss");
    addToSecurityProperty("package.definition", "org.mozilla.jss");
  }

  private static void addToSecurityProperty(String paramString1, String paramString2)
  {
    String str = Security.getProperty(paramString1);
    Trace.securityPrintln("property " + paramString1 + " value " + str);
    if (str != null)
      str = str + "," + paramString2;
    else
      str = paramString2;
    Security.setProperty(paramString1, str);
    Trace.securityPrintln("property " + paramString1 + " new value " + str);
  }

  public static String getJavaVersion()
  {
    return _javaVersionProperty;
  }

  public static String getJavaRuntimeNameProperty()
  {
    return _javaRuntimeNameProperty;
  }

  public static boolean isJavaVersionAtLeast17()
  {
    return _atLeast17;
  }

  public static boolean isJavaVersionAtLeast16()
  {
    return _atLeast16;
  }

  public static boolean isJavaVersionAtLeast15()
  {
    return _atLeast15;
  }

  public static boolean isJavaVersionAtLeast14()
  {
    return _atLeast14;
  }

  public static boolean isJavaVersionAtLeast13()
  {
    return _atLeast13;
  }

  public static boolean isJFB()
  {
    return _javaRuntimeNameProperty.endsWith("Business");
  }

  public static boolean installDeployRMIClassLoaderSpi()
  {
    return !Boolean.getBoolean("jnlp.noDeployRMIClassLoaderSpi");
  }

  public static synchronized Config get()
  {
    if (_config != null)
      return _config;
    return DefaultConfig.getDefaultConfig();
  }

  public static void setInstance(Config paramConfig)
  {
    if ((_config != null) && (paramConfig != null) && (!(paramConfig instanceof JCPConfig)))
      Trace.println("Unexpected second call to Config.setInstance() !", TraceLevel.BASIC);
    _config = paramConfig;
    if (getDeployDebug())
    {
      Trace.println("Config.setInstance called from:");
      new Throwable().printStackTrace();
    }
  }

  protected static synchronized DefaultConfig getDefaults()
  {
    return DefaultConfig.getDefaultConfig();
  }

  public static Properties getDefaultProperties()
  {
    return getDefaults();
  }

  public abstract boolean init(String paramString1, String paramString2);

  public abstract boolean isPropertyLocked(String paramString);

  public abstract void storeIfNeeded();

  public abstract void refreshIfNeeded();

  public abstract void storeInstalledJREs(Vector paramVector);

  public abstract String getEnterpriseString();

  public abstract boolean isValid();

  public abstract boolean isConfigDirty();

  public abstract Properties getSystemProps();

  public abstract boolean getJqs();

  public abstract boolean getJavaPlugin();

  public static String getPropertiesFilename()
  {
    return "deployment.properties";
  }

  public static int getMaxCommandLineLength()
  {
    return Platform.get().getPlatformMaxCommandLineLength();
  }

  public static String booleanToString(boolean paramBoolean)
  {
    if (paramBoolean == true)
      return "true";
    return "false";
  }

  public static String getJREHome()
  {
    return _jreHome;
  }

  public static String getLatestJREHome()
  {
    if (_latestJREHome == null)
    {
      Vector localVector = Platform.get().getInstalledJREList();
      if (localVector != null)
        _latestJREHome = findLatestJREPath(localVector);
      else
        _latestJREHome = getJREHome();
    }
    return _latestJREHome;
  }

  static String findLatestJREPath(Vector paramVector)
  {
    Object[] arrayOfObject = paramVector.toArray();
    String str1 = "";
    String str2 = getJREHome();
    int i = 0;
    while (i < paramVector.size() - 1)
    {
      if ((str1.compareTo((String)arrayOfObject[i]) < 0) && (((String)arrayOfObject[(i + 1)]).length() > 0))
      {
        str1 = (String)arrayOfObject[i];
        str2 = (String)arrayOfObject[(i + 1)];
      }
      i += 2;
    }
    return str2;
  }

  public static String getOSHome()
  {
    return Platform.get().getOSHome();
  }

  public static String getSystemHome()
  {
    return Platform.get().getSystemHome();
  }

  public static String getUserHome()
  {
    return Platform.get().getUserHome();
  }

  public static String getLocalStorageDir()
  {
    return Platform.get().getLocalStorageDir();
  }

  public static String getStringProperty(String paramString)
  {
    String str = get().getProperty(paramString);
    if (str != null)
      return replaceVariables(str).trim();
    return null;
  }

  public static void setStringProperty(String paramString1, String paramString2)
  {
    if ((paramString2 == null) || (paramString2.length() == 0))
      get().setProperty(paramString1, null);
    else
      get().setProperty(paramString1, restoreVariables(paramString2));
  }

  public static int getIntProperty(String paramString)
  {
    String str = getStringProperty(paramString);
    if (str == null)
      return -1;
    try
    {
      return Integer.parseInt(str);
    }
    catch (NumberFormatException localNumberFormatException)
    {
    }
    return -1;
  }

  public static void setIntProperty(String paramString, int paramInt)
  {
    setStringProperty(paramString, Integer.toString(paramInt));
  }

  public static boolean getBooleanProperty(String paramString)
  {
    String str = getStringProperty(paramString);
    if (str == null)
      return false;
    return Boolean.valueOf(str).booleanValue();
  }

  public static void setBooleanProperty(String paramString, boolean paramBoolean)
  {
    setStringProperty(paramString, booleanToString(paramBoolean));
  }

  private static void setProperties(Properties paramProperties)
  {
    Enumeration localEnumeration = paramProperties.keys();
    while (localEnumeration.hasMoreElements())
    {
      String str = (String)localEnumeration.nextElement();
      setStringProperty(str, paramProperties.getProperty(str));
    }
  }

  protected static String restoreVariables(String paramString)
  {
    if (paramString.indexOf(Environment.getJavaHome()) >= 0)
      paramString = substitute(paramString, "$JAVA_HOME", Environment.getJavaHome());
    if (paramString.indexOf(getJREHome()) >= 0)
      paramString = substitute(paramString, "$JRE_HOME", getJREHome());
    if (paramString.indexOf(getLatestJREHome()) >= 0)
      paramString = substitute(paramString, "$LATEST_JRE_HOME", getLatestJREHome());
    if (paramString.indexOf(getUserHome()) >= 0)
      paramString = substitute(paramString, "$USER_HOME", getUserHome());
    if (paramString.indexOf(getSystemHome()) >= 0)
      paramString = substitute(paramString, "$SYSTEM_HOME", getSystemHome());
    return paramString;
  }

  protected static String replaceVariables(String paramString)
  {
    if (paramString.indexOf("$JAVA_HOME") >= 0)
      paramString = substitute(paramString, Environment.getJavaHome(), "$JAVA_HOME");
    if (paramString.indexOf("$JRE_HOME") >= 0)
      paramString = substitute(paramString, getJREHome(), "$JRE_HOME");
    if (paramString.indexOf("$LATEST_JRE_HOME") >= 0)
      paramString = substitute(paramString, getLatestJREHome(), "$LATEST_JRE_HOME");
    if (paramString.indexOf("$USER_HOME") >= 0)
      paramString = substitute(paramString, getUserHome(), "$USER_HOME");
    if (paramString.indexOf("SYSTEM_HOME") >= 0)
      paramString = substitute(paramString, getSystemHome(), "$SYSTEM_HOME");
    return paramString;
  }

  private static String substitute(String paramString1, String paramString2, String paramString3)
  {
    int i = paramString1.indexOf(paramString3);
    return paramString1.substring(0, i) + paramString2 + paramString1.substring(i + paramString3.length());
  }

  public static String getSystemProperty(String paramString)
  {
    return (String)AccessController.doPrivileged(new PrivilegedAction(paramString)
    {
      private final String val$property;

      public Object run()
      {
        return System.getProperty(this.val$property);
      }
    });
  }

  public static void printProps()
  {
    Trace.println("\n_defaultProps:");
    printP(getDefaults());
    Trace.println("\n_props:");
    printP(get());
  }

  private static void printP(Properties paramProperties)
  {
    Enumeration localEnumeration = paramProperties.keys();
    while (localEnumeration.hasMoreElements())
    {
      String str1 = (String)localEnumeration.nextElement();
      String str2 = paramProperties.getProperty(str1);
      Trace.println("  " + str1 + " : " + str2);
    }
    Trace.println("");
  }

  public static String getUserTrustedCertificateFile()
  {
    return getStringProperty("deployment.user.security.trusted.certs");
  }

  public static String getSystemTrustedCertificateFile()
  {
    return getStringProperty("deployment.system.security.trusted.certs");
  }

  public static String getUserTrustedHttpsCertificateFile()
  {
    return getStringProperty("deployment.user.security.trusted.jssecerts");
  }

  public static String getSystemTrustedHttpsCertificateFile()
  {
    return getStringProperty("deployment.system.security.trusted.jssecerts");
  }

  public static String getUserRootCertificateFile()
  {
    return getStringProperty("deployment.user.security.trusted.cacerts");
  }

  public static String getSystemRootCertificateFile()
  {
    return getStringProperty("deployment.system.security.cacerts");
  }

  public static String getOldSystemRootCertificateFile()
  {
    return getStringProperty("deployment.system.security.oldcacerts");
  }

  public static String getUserSSLRootCertificateFile()
  {
    return getStringProperty("deployment.user.security.trusted.jssecacerts");
  }

  public static String getSystemSSLRootCertificateFile()
  {
    return getStringProperty("deployment.system.security.jssecacerts");
  }

  public static String getOldSystemSSLRootCertificateFile()
  {
    return getStringProperty("deployment.system.security.oldjssecacerts");
  }

  public static String getUserClientAuthCertFile()
  {
    return getStringProperty("deployment.user.security.trusted.clientauthcerts");
  }

  public static String getSystemClientAuthCertFile()
  {
    return getStringProperty("deployment.system.security.trusted.clientauthcerts");
  }

  public static String getUserPretrustFile()
  {
    return getStringProperty("deployment.user.security.trusted.publishers");
  }

  public static String getSystemPretrustFile()
  {
    return getStringProperty("deployment.system.security.trusted.publishers");
  }

  public static String getUserBlacklistFile()
  {
    return getStringProperty("deployment.user.security.blacklist");
  }

  public static String getSystemBlacklistFile()
  {
    return getStringProperty("deployment.system.security.blacklist");
  }

  public static String getUserTrustedLibrariesFile()
  {
    return getStringProperty("deployment.user.security.trusted.libraries");
  }

  public static String getSystemTrustedLibrariesFile()
  {
    return getStringProperty("deployment.system.security.trusted.libraries");
  }

  public static String getUserAuthFile()
  {
    return getStringProperty("deployment.user.security.saved.credentials");
  }

  public static String getUserCookieFile()
  {
    return getUserHome() + File.separator + "security" + File.separator + "cookie.txt";
  }

  public static String getCacheDirectory()
  {
    String str = get().getProperty("deployment.user.cachedir");
    if (str == null)
      str = getDefaultCacheDirectory();
    return str;
  }

  public static String getDefaultCacheDirectory()
  {
    return getLocalStorageDir() + File.separator + "cache";
  }

  public String getDefaultCacheVersionDirectory()
  {
    return getDefaultCacheDirectory() + File.separator + "6.0";
  }

  public boolean isCacheUpgradeSkipped()
  {
    return getBooleanProperty("deployment.cache.upgrade.skip");
  }

  public static String getPluginCacheDir()
  {
    String str = getStringProperty("deployment.javapi.cachedir");
    if (str != null)
      return str;
    return getCacheDirectory() + File.separator + "javapi";
  }

  public static String getTempCacheDir()
  {
    return getCacheDirectory() + File.separator + "tmp";
  }

  public static String getSystemCacheDirectory()
  {
    return getStringProperty("deployment.system.cachedir");
  }

  public String getDefaultSystemCacheVersionDirectory()
  {
    String str = Platform.get().getDefaultSystemCache();
    if (str != null)
      return str + File.separator + "6.0";
    return null;
  }

  public static void setCacheDirectory(String paramString)
  {
    get().setProperty("deployment.user.cachedir", paramString);
  }

  public static String getLogDirectory()
  {
    return getStringProperty("deployment.user.logdir");
  }

  public static String getTempDirectory()
  {
    return getStringProperty("deployment.user.tmp");
  }

  public static String getUserExtensionDirectory()
  {
    return getStringProperty("deployment.user.extdir");
  }

  public static String getSystemExtensionDirectory()
  {
    return getStringProperty("deployment.system.extdir");
  }

  public static int getMaxAppletThreadCount()
  {
    int i = getIntProperty("deployment.system.maxAppletThreadCount");
    if (i == -1)
      i = 50;
    return i;
  }

  public static String getUserSecurityPolicyURL()
  {
    return getStringProperty("deployment.user.security.policy");
  }

  public static String getSystemSecurityPolicyURL()
  {
    return getStringProperty("deployment.system.security.policy");
  }

  public static int getProxyType()
  {
    return getIntProperty("active.deployment.proxy.type");
  }

  public static boolean isProxySame()
  {
    return getBooleanProperty("active.deployment.proxy.same");
  }

  public static boolean isProxyBypassLocal()
  {
    return getBooleanProperty("active.deployment.proxy.bypass.local");
  }

  public static String getProxyAutoConfig()
  {
    return getStringProperty("active.deployment.proxy.auto.config.url");
  }

  public static String getProxyBypass()
  {
    return getStringProperty("active.deployment.proxy.bypass.list");
  }

  public static String getProxyOverride()
  {
    return getStringProperty("active.deployment.proxy.override.hosts");
  }

  public static String getProxyHttpHost()
  {
    return getStringProperty("active.deployment.proxy.http.host");
  }

  public static String getProxyHttpsHost()
  {
    return getStringProperty("active.deployment.proxy.https.host");
  }

  public static String getProxyFtpHost()
  {
    return getStringProperty("active.deployment.proxy.ftp.host");
  }

  public static String getProxySocksHost()
  {
    return getStringProperty("active.deployment.proxy.socks.host");
  }

  public static int getProxyHttpPort()
  {
    return getIntProperty("active.deployment.proxy.http.port");
  }

  public static int getProxyHttpsPort()
  {
    return getIntProperty("active.deployment.proxy.https.port");
  }

  public static int getProxyFtpPort()
  {
    return getIntProperty("active.deployment.proxy.ftp.port");
  }

  public static int getProxySocksPort()
  {
    return getIntProperty("active.deployment.proxy.socks.port");
  }

  public static void validateSystemCacheDirectory()
  {
    if ((getSystemCacheDirectory() != null) && (getSystemCacheDirectory().equalsIgnoreCase(getCacheDirectory())))
    {
      Trace.println(ResourceManager.getMessage("launch.warning.cachedir"), TraceLevel.BASIC);
      Environment.setSystemCacheMode(false);
      Cache.setSystemCacheDir(null);
    }
  }

  public static boolean useSystemLookAndFeel()
  {
    String str = getStringProperty("deployment.system.lookandfeel");
    if (str != null)
      return Boolean.valueOf(str).booleanValue();
    return Platform.get().systemLookAndFeelDefault();
  }

  public static String getJavaCommand()
  {
    String str = System.getProperty("java.home");
    return getJavaCommand(str);
  }

  public static String getJavaCommand(String paramString)
  {
    if (null == paramString)
      return null;
    if (paramString.endsWith(Platform.get().getPlatformSpecificJavaName()))
      return paramString;
    if (!paramString.endsWith(File.separator))
      paramString = paramString + File.separator;
    return paramString + "bin" + File.separator + Platform.get().getPlatformSpecificJavaName();
  }

  public static String getJavaHome(String paramString)
  {
    if (null == paramString)
      return null;
    String str = "bin" + File.separator + Platform.get().getPlatformSpecificJavaName();
    if (paramString.endsWith(str));
    for (paramString = paramString.substring(0, paramString.length() - str.length()); paramString.endsWith(File.separator); paramString = paramString.substring(0, paramString.length() - 1));
    return paramString;
  }

  public static String getOldJavawsCacheDir()
  {
    String str = getStringProperty("deployment.javaws.cachedir");
    if (str != null)
      return str;
    return getCacheDirectory() + File.separator + "javaws";
  }

  public static String getSplashDir()
  {
    return Cache.getCacheDir().getPath() + File.separator + "splash";
  }

  public static String getSplashIndex()
  {
    return getSplashDir() + File.separator + "splash.xml";
  }

  public static void setSplashCache()
  {
    get().setProperty("deployment.javaws.splash.index", getSplashIndex());
  }

  public static String[] getSecureProperties()
  {
    ArrayList localArrayList = new ArrayList(4);
    int i = 0;
    while (i < DefaultSecureProperties.length)
      localArrayList.add(DefaultSecureProperties[(i++)]);
    String str = getStringProperty("deployment.javaws.secure.properties");
    if (str != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(str, ",");
      while (localStringTokenizer.hasMoreTokens())
        localArrayList.add(localStringTokenizer.nextToken());
    }
    return (String[])(String[])localArrayList.toArray(new String[0]);
  }

  public static boolean isSecureVmArg(String paramString)
  {
    for (int i = 0; i < secureVmArgs.length; i++)
      if (paramString.equals(secureVmArgs[i]))
        return true;
    for (i = 0; i < secureVmPrefixes.length; i++)
      if (paramString.startsWith(secureVmPrefixes[i]))
        return !containsUnsupportedCharacters(paramString);
    if ((paramString.startsWith("-D")) || (paramString.startsWith("\"-D")))
      return isSecureSystemProperty(paramString);
    return false;
  }

  public static boolean isSecureSystemProperty(String paramString)
  {
    String str = "";
    if (paramString.startsWith("-D"))
      paramString = paramString.substring(2);
    else if (paramString.startsWith("\"-D"))
      paramString = paramString.substring(3);
    int i = paramString.indexOf('=');
    if (i != -1)
    {
      if (i + 1 < paramString.length())
        str = paramString.substring(i + 1);
      paramString = paramString.substring(0, i);
    }
    return isSecureProperty(paramString, str);
  }

  private static boolean isSecurePropertyKey(String paramString)
  {
    if (containsUnsupportedCharacters(paramString))
      return false;
    if ((paramString.startsWith("jnlp.")) || (paramString.startsWith("javaws.")) || (paramString.startsWith("javapi.")))
      return true;
    for (int i = 0; i < DefaultSecureProperties.length; i++)
      if (paramString.equals(DefaultSecureProperties[i]))
        return true;
    return false;
  }

  public static boolean isSecureProperty(String paramString1, String paramString2)
  {
    return (isSecurePropertyKey(paramString1)) && (!containsUnsupportedCharacters(paramString2));
  }

  private static boolean containsUnsupportedCharacters(String paramString)
  {
    for (int i = 0; i < paramString.length(); i++)
    {
      int j = paramString.charAt(i);
      if ((j < 32) || (j > 126) || (j == 37))
        return true;
      if (j != 34)
        continue;
      int k = paramString.length() - 1;
      if (((i != 0) && (i != k)) || (paramString.charAt(0) != '"') || (paramString.charAt(k) != '"'))
        return true;
    }
    return false;
  }

  public static void addSecureSystemPropertiesTo(Properties paramProperties)
  {
    String[] arrayOfString = getSecureProperties();
    for (int i = 0; i < arrayOfString.length; i++)
    {
      String str1 = arrayOfString[i];
      String str2 = System.getProperty(str1);
      if (str2 == null)
        continue;
      paramProperties.setProperty(str1, str2);
    }
  }

  public static void addAllSystemPropertiesTo(Properties paramProperties)
  {
    Properties localProperties = System.getProperties();
    Enumeration localEnumeration = localProperties.propertyNames();
    while (localEnumeration.hasMoreElements())
    {
      String str1 = (String)localEnumeration.nextElement();
      String str2 = localProperties.getProperty(str1);
      paramProperties.setProperty(str1, str2);
    }
  }

  public static List getProxyOverrideList()
  {
    return null;
  }

  public static void setMixcodeValue(int paramInt)
  {
    String str;
    switch (paramInt)
    {
    case 0:
      str = "ENABLE";
      break;
    case 1:
    default:
      str = "HIDE_RUN";
      break;
    case 2:
      str = "HIDE_CANCEL";
      break;
    case 3:
      str = "DISABLE";
    }
    setStringProperty("deployment.security.mixcode", str);
  }

  public static int getMixcodeValue()
  {
    String str = getStringProperty("deployment.security.mixcode");
    if (str.equals("ENABLE"))
      return 0;
    if (str.equals("HIDE_RUN"))
      return 1;
    if (str.equals("HIDE_CANCEL"))
      return 2;
    if (str.equals("DISABLE"))
      return 3;
    return 0;
  }

  public static int getShortcutValue()
  {
    String str = getStringProperty("deployment.javaws.shortcut");
    if (str.equals("NEVER"))
      return 0;
    if (str.equals("ALWAYS"))
      return 1;
    if (str.equals("ASK_USER"))
      return 2;
    if (str.equals("ASK_IF_HINTED"))
      return 3;
    if (str.equals("ALWAYS_IF_HINTED"))
      return 4;
    return 0;
  }

  public static int getInstallMode()
  {
    String str = getStringProperty("deployment.javaws.install");
    if (str.equals("IF_HINT"))
      return 3;
    if (str.equals("IF_SHORTCUT"))
      return 1;
    if (str.equals("IF_HINT_AND_SHORTCUT"))
      return 2;
    return 0;
  }

  public static int getAssociationValue()
  {
    String str = getStringProperty("deployment.javaws.associations");
    if (str.equals("ALWAYS"))
      return 4;
    if (str.equals("NEVER"))
      return 0;
    if (str.equals("NEW_ONLY"))
      return 1;
    if (str.equals("ASK_USER"))
      return 2;
    if (str.equals("REPLACE_ASK"))
      return 3;
    return 0;
  }

  public static long getCacheSizeMax()
  {
    long l1 = -1L;
    String str = getStringProperty("deployment.cache.max.size");
    if ((str != null) && (str.length() > 0))
    {
      long l2;
      if ((str.endsWith("M")) || (str.endsWith("m")))
      {
        l2 = 1048576L;
        str = str.substring(0, str.length() - 1);
      }
      else if ((str.endsWith("K")) || (str.endsWith("k")))
      {
        l2 = 1024L;
        str = str.substring(0, str.length() - 1);
      }
      else
      {
        l2 = 1048576L;
      }
      try
      {
        long l3 = Long.valueOf(str).longValue();
        if (l3 > 0L)
          l1 = l2 * l3;
      }
      catch (NumberFormatException localNumberFormatException)
      {
        l1 = -1L;
      }
    }
    return l1;
  }

  public static boolean isDebugMode()
  {
    String str = System.getProperty("deploy.debugMode");
    return (str != null) && (str.equalsIgnoreCase("true"));
  }

  public static boolean isDebugVMMode()
  {
    String str = System.getProperty("deploy.useDebugJavaVM");
    return (str != null) && (str.equalsIgnoreCase("true"));
  }

  public static boolean checkClassName(String paramString)
  {
    try
    {
      Class localClass = Class.forName(paramString);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      return false;
    }
    return true;
  }

  public static String getOSName()
  {
    return _os;
  }

  public static String getOSFullName()
  {
    return get()._getOSFullName();
  }

  protected String _getOSFullName()
  {
    if (this._osFullName == null)
      this._osFullName = System.getProperty("os.name");
    return this._osFullName;
  }

  public static String getOSArch()
  {
    return _arch;
  }

  public static String getOSPlatform()
  {
    return _platform;
  }

  public static void setDebug()
  {
    if (Environment.getenv("JPI_PLUGIN2_DEBUG") != null)
      _debugPlugin = true;
    if (Environment.getenv("DEPLOY_DEBUG") != null)
      _debugDeploy = true;
    _debugSet = true;
  }

  public static boolean getPluginDebug()
  {
    if (!_debugSet)
      setDebug();
    return _debugPlugin;
  }

  public static boolean getDeployDebug()
  {
    if (!_debugSet)
      setDebug();
    return _debugDeploy;
  }

  static
  {
    String str = System.getProperty("os.name");
    if (str.startsWith("Win"))
      _os = "Windows";
    else
      _os = str;
    _arch = System.getProperty("os.arch");
    _platform = System.getProperty("os.platform");
    _debugSet = false;
    _debugPlugin = false;
    _debugDeploy = false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.config.Config
 * JD-Core Version:    0.6.0
 */