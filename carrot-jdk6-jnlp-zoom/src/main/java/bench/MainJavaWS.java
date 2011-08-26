package bench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.javaws.LocalInstallHandler;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchSelection;

public class MainJavaWS {

	static final Logger log = LoggerFactory.getLogger(MainJavaWS.class);

	public static void main(String[] args) {

		log.debug("init");

		LocalInstallHandler install = LocalInstallHandler.getInstance();

		log.debug("install : {}", install);

		log.debug("isAssociationSupported : {}",
				install.isAssociationSupported());

		log.debug("isLocalInstallSupported : {}",
				install.isLocalInstallSupported());

		InformationDesc info = new InformationDesc(null, null, null, null,
				null, null, null, null, false);

		LaunchDesc desc = new LaunchDesc("0.1", null, null, null, info,
				LaunchDesc.ALLPERMISSIONS_SECURITY, null, null,
				LaunchDesc.INTERNAL_TYPE, null, null, null, null, null, null,
				null, LaunchSelection.createDefaultMatchJRE());

		String path = "";

		LocalApplicationProperties props = Cache
				.getLocalApplicationProperties(path);

		install.installShortcuts(desc, props);

	}

}
