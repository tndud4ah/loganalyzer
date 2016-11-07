package LogAgentD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import org.apache.logging.log4j.*;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
/* How do insert inet type
import org.postgresql.util.PGobject;

PGobject PGobj = new PGobject();
PGobj.setType("inet");
PGobj.setValue("192.168.0.1/24");
pst.setObject(1, PGobj);
*/
public class LogAgentD {
	static Logger logger = LogManager.getRootLogger();
	
	static String sAgentFile = LogAnalyzer.InitConf.sAgentPath + "agent.info";
	static int iPollingTime = 5 * 1000;

	public static void main(String args[]) {
		Connection conn = null;
		String url = "jdbc:postgresql://localhost:5432/postgres";
		String user = "postgres";
		String password = "postgres";

		try {
			conn = DriverManager.getConnection(url, user, password);
			if ( conn != null ) {
				logger.info("Connect postgresql successfully");
			}
			
			CopyManager cm = new CopyManager((BaseConnection) conn);
			
			while(true) {
				PrintWriter pw = new PrintWriter(sAgentFile, "UTF-8");
				cm.copyOut("COPY \"agent_info\" TO STDOUT WITH DELIMITER AS ','", pw);
				if (pw != null) pw.close();
	            Thread.sleep(iPollingTime);
			}

		} catch (SQLException | IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
