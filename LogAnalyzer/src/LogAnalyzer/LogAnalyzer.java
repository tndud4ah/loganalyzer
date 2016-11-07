package LogAnalyzer;

import org.apache.logging.log4j.*;

public class LogAnalyzer {
	static Logger logger = LogManager.getRootLogger();
	
	public static void main(String args[]) {
		logger.info("Deamon START and STOP Button");
		
		InitConf ic = new InitConf();
	}

}
