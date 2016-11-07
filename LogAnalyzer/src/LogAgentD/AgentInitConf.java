package LogAgentD;

import java.io.File;

import org.apache.logging.log4j.*;

public class AgentInitConf {
	static Logger logger = LogManager.getRootLogger();
	
	void createPath(String s) {
		File f = new File(s);
		if ( ! f.isDirectory() ) {
			logger.info("Create directory path: ", s);
			f.mkdirs();
		}
	}

}
