package LogParseD;

import java.io.File;

import org.apache.logging.log4j.*;

public class ParseInitConf {
	static Logger logger = LogManager.getRootLogger();
	
	void createPath(String s) {
		File f = new File(s);
		if ( ! f.isDirectory() ) {
			logger.info("Create directory path: ", s);
			f.mkdirs();
		}
	}
	
	public String getType(int type) {
		if (type == 1) {
			return "fw";
		} else if (type == 2) {
			return "ips";
		} else if (type == 3) {
			return "waf";
		} else if (type == 4) {
			return "nac";
		}
		return "unknown";
	}

}
