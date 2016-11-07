package LogRecvD;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.*;

public class RecvFileConst {
	static Logger logger = LogManager.getRootLogger();
	
	private static int iMaxFileCount = 10;
	private static long iMaxTimeLimit = 20 * 1000;
	
	private static String EXT_ING = ".ing";
	private static String EXT_COM = ".dat";
	
	FileWriter fw;
	BufferedWriter bw;
	int iCount;
	long lOpenTime;
	String sFileName;
	
	RecvFileConst() {
		fw = null;
		bw = null;
		iCount = 0;
		lOpenTime = 0;
		sFileName = null;
	}
	
	void init() {
		fw = null;
		bw = null;
		iCount = 0;
		lOpenTime = 0;
		sFileName = null;
	}
	
	void FileOpen() {
		String today = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
		sFileName = LogAnalyzer.InitConf.sRecvPath + "recv." + today + EXT_ING;
		try {
			fw = new FileWriter(sFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bw = new BufferedWriter(fw);
		lOpenTime = System.currentTimeMillis();
	}
	
	void FileClose() {
		long lcurTime = System.currentTimeMillis();
		long ldiffTime = lcurTime - lOpenTime;
		
		if ( ldiffTime >= iMaxTimeLimit ) {
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			File fs = new File(sFileName);
			String sDestFileName = sFileName.replace(EXT_ING, EXT_COM);
			File fd = new File(sDestFileName);
			fs.renameTo(fd);
			
			init();
			
			
		}
	}
	
	void FileWrite(String s) {
		if (bw == null) {
			FileOpen();
		}
		
		try {
			bw.write(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( ++iCount >= iMaxFileCount) {
			FileClose();
		}
	}
	
}
