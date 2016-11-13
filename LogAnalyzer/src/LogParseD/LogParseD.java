package LogParseD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.logging.log4j.*;

public class LogParseD {
	static Logger logger = LogManager.getRootLogger();
	static String sAllowLog = "<206>[LOG_ADMITTED] id=firewall time=\"2015-05-26 10:00:29\" fw=YD-INTFW-1 pri=7 rule=441 proto=123/udp src=10.90.1.193 src_port=123 dst=10.90.53.151 dst_port=123 sent=94 rcvd=94 duration=64 msg=\"source interface = External\"";	
	static Vector<cAgentInfo> vAgentInfo = new Vector<cAgentInfo> ();

	static class cAgentInfo {
		private int lv1;
		private int lv2;
		private int lv3;
		private int type;
		private String name;
		private InetAddress ip;
		
		public cAgentInfo() {
			lv1 = 0;
			lv2 = 0;
			lv3 = 0;
			type = 0;
			name = null;
			ip = null;
		}
		
		public cAgentInfo(int lv1, int lv2, int lv3, int type, String name, InetAddress ip) {
			this.lv1 = lv1;
			this.lv2 = lv2;
			this.lv3 = lv3;
			this.type = type;
			this.name = name;
			this.ip = ip;
		}
		
		void printAgentInfo() {
			System.out.println(this.lv1 + this.lv2 + this.lv3 + this.type + this.name + this.ip);
		}
		
		
	}
	
	public static void main(String args[]) {
		ReadAgentInfo();
		for(int i=0; i < vAgentInfo.size(); i++) {
			logger.debug(vAgentInfo.get(i).ip);
		}
		
		Vector<String> vs = new Vector<String>();
		vs = getFileNames(LogAnalyzer.InitConf.sRecvPath, "dat");
		for(int i=0; i < vs.size(); i++) {
			logger.debug(vs.elementAt(i));
		}
		
		String solution = new String();
		solution = "fw;ips;";
		
		
		Map<String, Iniproc> mInifile = new HashMap<String, Iniproc>();
		StringTokenizer sKey = new StringTokenizer(solution, ";");
		while (sKey.hasMoreElements()) {
			Iniproc iniproc = new Iniproc();
			mInifile.put(sKey.nextToken(), iniproc);
		}

		for (Map.Entry<String, Iniproc> entry: mInifile.entrySet()) {
			logger.debug(entry.getKey() + entry.getValue());
			String szTemp = String.format(".\\rule\\%s.ini", entry.getKey());
			File f = new File(szTemp);
			if (!f.exists()) {
				logger.info(szTemp + " is not found.");
				System.exit(0);
			}
			entry.getValue().readini(szTemp);
		}
		
		mInifile.get("fw").printIniInfo();
		
		String resultstr = new String();
		mInifile.get("fw").doParse(sAllowLog, resultstr, vAgentInfo.get(0).ip);
		
		Set<Entry<String, Iniproc>> mitr = mInifile.entrySet();
		logger.debug(mitr.size());
/*		for (mitr = mInifile..begin(); mitr != g_stConfig.mInifile.end(); ++mitr) {
			snprintf(szTemp, sizeof(szTemp), "%s/ini/%s.ini", getwienv("ESM_HOME"), mitr->first.c_str());
			if (access(szTemp, F_OK) != 0) {	// ini file does not exist.
				Thr_Log(LOG_PRINT, 0, FL, "[%s] is not found!!!\n", szTemp);
				exit(2);
			}
			//mitr.second.readini(szTemp);
		}
*/
		
		
	}
	
	private static void ReadAgentInfo() {
		FileReader fr;
		try {
			fr = new FileReader(".\\info\\agent.info");
			BufferedReader br = new BufferedReader(fr);
			String sLine = br.readLine();
			while(sLine != null) {
				StringTokenizer st = new StringTokenizer(sLine, ",");
				int iLv1 = Integer.parseInt(st.nextToken());
				int iLv2 = Integer.parseInt(st.nextToken());
				int iLv3 = Integer.parseInt(st.nextToken());
				int iType = Integer.parseInt(st.nextToken());
				String sName = st.nextToken();
				InetAddress iIp = InetAddress.getByName(st.nextToken());
				
				cAgentInfo cai = new cAgentInfo(iLv1, iLv2, iLv3, iType, sName, iIp);
				vAgentInfo.add(cai);
				sLine = br.readLine();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Vector<String> getFileNames(String targetDirName, String fileExt) {
		Vector<String> fileNames = new Vector<String>();
		File dir = new File(targetDirName);
		fileExt = fileExt.toLowerCase();
		
		if(dir.isDirectory()) {
			String dirName = dir.getPath();
			String[] filenames = dir.list(null);
			int cntFiles = filenames.length;
			
			for(int iFile=0; iFile<cntFiles; ++iFile) {
				String filename = filenames[iFile];
				String fullFileName = dirName + "/" + filename;
				File file = new File(fullFileName);
				
				boolean isDirectory = file.isDirectory();
				if(!isDirectory && filename.toLowerCase().endsWith(fileExt)) {
					fileNames.add(fullFileName);
				}
			}
		}
		
		return fileNames;
	}
	
	
}
