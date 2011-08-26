package bench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javaws.LocalInstallHandler;

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

	}

}
