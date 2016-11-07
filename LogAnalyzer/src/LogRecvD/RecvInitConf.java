package LogRecvD;

import java.io.File;

import org.apache.logging.log4j.*;

public class RecvInitConf {
	static Logger logger = LogManager.getRootLogger();
	
	void createPath(String s) {
		File f = new File(s);
		if ( ! f.isDirectory() ) {
			logger.info("Create directory path: ", s);
			f.mkdirs();
		}
	}

}
