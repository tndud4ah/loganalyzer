package LogAnalyzer;

import java.io.File;

import org.apache.logging.log4j.*;

public class InitConf {
	static Logger logger = LogManager.getRootLogger();

	public static String sRecvPath = ".\\recv\\";
	public static String sParsePath = ".\\parse\\";
	public static String sAgentPath = ".\\info\\";
	
	InitConf() {
		createPath(sRecvPath);
		createPath(sParsePath);
		createPath(sAgentPath);
	}

	void createPath(String s) {
		File f = new File(s);
		if ( ! f.isDirectory() ) {
			logger.info("Create directory path: ", s);
			f.mkdirs();
		}
	}

}
