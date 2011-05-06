/*
 *	build time: ${carrotTimeISO}
 */
/*
 * provides applet html tag configuration parameters
 * http://download.oracle.com/javase/6/docs/technotes/guides/jweb/deployment_advice.html
 * by default include values for both plugin V1 and plugin V2 of java browser plugin
 */

//##############################
//
if (typeof appletAttributes == "undefined") {
	appletAttributes = {}; // make global
}

/* used by plugin v1 */
appletAttributes.codebase = "."; // url to class path jars
appletAttributes.code = "${mainClassApplet}"; // extends java.awt.Applet
appletAttributes.archive = "${mainJar}"; // provides INDEX.LIST

/* used by plugin v1 & v2 */
appletAttributes.width = "100%"; // default applet size
appletAttributes.height = "100%"; // default applet size

// ##############################

if (typeof appletParameters == "undefined") {
	appletParameters = {}; // make global
}

/* used by plugin v1 */
appletParameters.separate_jvm = "${jnlpJavaUseArgs}"; //
if (appletParameters.separate_jvm == "true") {
	appletParameters.java_arguments = "${jnlpJavaArgsString} -Xms${jnlpJavaHeapMinimum} -Xmsx${jnlpJavaHeapMaximum} "; //
}

/* used by plugin v1 */
appletParameters.java_version = "${jnlpJavaVersion}";

/* used by plugin v2 */
appletParameters.jnlp_href = "${jnlpFileApplet}";

// ##############################

function usePluginV1() {
	//
	delete appletParameters.jnlp_href;
	//
}

function usePluginV2() {
	//
	delete appletAttributes.codebase;
	delete appletAttributes.code;
	delete appletAttributes.archive;
	//
	delete appletParameters.separate_jvm;
	delete appletParameters.java_arguments;
	delete appletParameters.java_version;
	//
}

// ##############################
