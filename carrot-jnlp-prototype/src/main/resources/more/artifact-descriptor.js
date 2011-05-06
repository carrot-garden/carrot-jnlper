/*
 *	build time: ${carrotTimeISO}
 */

/*
 * provides naming, version, build time & properties
 */

if (typeof artifactDescriptor == "undefined") {
	// make global
	artifactDescriptor = {};
}

// provides INDEX.LIST
artifactDescriptor.mainJar = '${mainJar}';

// extends java.awt.Applet
artifactDescriptor.mainClassApplet = '${mainClassApplet}';

// provides main(Strin ... args)
artifactDescriptor.mainClassApplication = '${mainClassApplication}';

// maven build properties
artifactDescriptor.groupId = '${project.groupId}';
artifactDescriptor.artifactId = '${project.artifactId}';
artifactDescriptor.version = '${project.version}';
artifactDescriptor.builTime = '${carrotTimeISO}';
artifactDescriptor.finalName = '${project.build.finalName}';

// final jnlp files
artifactDescriptor.jnlpFileApplet = '${jnlpFileApplet}';
artifactDescriptor.jnlpFileApplication = '${jnlpFileApplication}';

// version hacks
//used in javascript by deployJava.js
artifactDescriptor.initJavaVersion = '${initJavaVersion}';
// used in launch.jnlp by javaws.jar
artifactDescriptor.jnlpJavaVersion = '${jnlpJavaVersion}';
