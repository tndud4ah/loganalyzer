package LogParseD;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import org.apache.logging.log4j.*;

class Inode {
	public String functionName = new String();
	public Vector<String> syntax = new Vector<String>();
	public Vector<String> tokens = new Vector<String>();
	public Vector<String> fields = new Vector<String>();
	
	public void clearNode() {
		functionName = new String();
		syntax = new Vector<String>();
		tokens = new Vector<String>();
		fields = new Vector<String>();
	}
}

class Iniproc {
	private Vector<Inode> vnode;
    private String rdata; /* read data = buffer */
    private Vector<String> pdata; /* parsed data */
	private String Sign;
	private String SignSign;
	
    public Iniproc() {
    	vnode.clear();
    	rdata = null;
    	pdata.clear();
    	Sign = "$s";
    	SignSign= "$s$s";
    	
    }
    
	public int readini(final String filename) {
		String buf;
		String value;
		int idx;
		
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String sLine = br.readLine();
		
		int nline = 0;
		Inode node;
		node.clearNode();
		while (sLine != null)
		{
			++nline;
			rdata = sLine;
			rdata.trim();
			if (rdata.isEmpty()) continue;
			if (rdata.indexOf(0) == '#') continue; /* comment line */
			idx = rdata.indexOf("=");
			if (idx == -1) continue; /* strange line */
			value = rdata.substring(idx+1);
			if (value.isEmpty())
			{
				br.close();
				logger.error("%s", "rvalue is empty");
				return 0;
			}
			if (rdata.indexOf("LOG_ID=") != -1)
			{
				if (!node.functionName.isEmpty())
				{
					br.close();
					logger.error("%s","endl=end seems to be omitted!!!\n");
					return 0;					
				}
				node.functionName = value;
			}
			else if (rdata.indexOf("syntax=") != -1 || rdata.indexOf("SYNTAX=") != -1)
			{
				if (value.charAt(0) != '(' || value.charAt(value.length()-1) != ')')
				{
					br.close();
					logger.error("%s","syntax (...) abnormal\n");
					return 0;
				}
				value..erase(0,1);							/* first char'(' remove */
				value.erase(value.size()-1,1); /* last	char')' remove */
				if (value.empty())
				{
					fin.close();
					fprintf(stderr,"%s","syntax is empty.\n");
					return -1;
				}
				while(1)
				{
					idx = value.find("/LRB/");
					if (idx == string::npos) break;
					value.replace(idx,5,"(");
				}
				while(1)
				{
					idx = value.find("/RRB/");
					if (idx == string::npos) break;
					value.replace(idx,5,")");
				}
				node.syntax.push_back(value);
			}
			else if (rdata.find("endl=") != string::npos || rdata.find("ENDL=") != string::npos)
			{
				if (value.empty())
				{
					fin.close();
					fprintf(stderr,"%s","endl's value is empty.\n");
					return -1;
				}
				maketokens(node);
				vnode.push_back(node);
				node.clearnode();
			}
			else
			{
				node.fields.push_back(value);
			}
			sLine = br.readLine();
		}
		
		fin.close();
		
		if (!node.functionName.empty())
		{
			fprintf(stderr,"%s","endl=end seems to be omitted!!!\n");
			return -1;					
		}
 		return 0;
	}
}
public int maketokens(Inode& node);
public int doParse(const char *inputstr,string& resultstr,const char *hostip);
public int getLogId(const char *inputstr,string& LogId);
public int doParse2(const char *inputstr,string& resultstr,const char *hostip);
public int printIniInfo();

public class ParseRule {
	static Logger logger = LogManager.getRootLogger();

	
}
