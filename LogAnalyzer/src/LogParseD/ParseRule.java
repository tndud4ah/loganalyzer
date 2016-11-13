package LogParseD;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
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
	static Logger logger = LogManager.getRootLogger();

	private Vector<Inode> vnode = new Vector<Inode>();
    private String rdata = new String(); /* read data = buffer */
    private Vector<String> pdata = new Vector<String>(); /* parsed data */
	private String Sign = "$s";
	private String SignSign = "$s$s";
	
    public Iniproc() {
    	vnode = new Vector<Inode>();
    	rdata = new String();
    	pdata = new Vector<String>();
    	Sign = "$s";
    	SignSign = "$s$s";
    	
    }
    
	public int readini(final String filename) {
		String buf = new String();
		String value = new String();
		int idx;
		
		Inode node = new Inode();
		node.clearNode();
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filename));
			buf = br.readLine();
			logger.debug(buf);
			while (buf != null) {
				logger.debug("readini()" + buf);
				rdata = buf;
				rdata.trim();
				
				if (rdata.isEmpty()) {
					buf = br.readLine();
					continue;
				}
				
				if (rdata.indexOf(0) == '#') { // comment line
					buf = br.readLine();
					continue;
				}
				
				idx = rdata.indexOf("=");
				if (idx == -1) { // strange line
					buf = br.readLine();
					continue;
				}
				
				value = rdata.substring(idx+1);
				if (value.isEmpty()) {
					br.close();
					logger.error("rvalue is empty");
					return 0;
				}
				
				if (rdata.indexOf("LOG_ID=") != -1) {
					if (!node.functionName.isEmpty()) {
						br.close();
						logger.error("endl=end seems to be omitted!!!\n");
						return 0;
					}
					node.functionName = value;
				} else if (rdata.indexOf("syntax=") != -1 || rdata.indexOf("SYNTAX=") != -1) {
					if (value.charAt(0) != '(' || value.charAt(value.length()-1) != ')') {
						br.close();
						logger.error("syntax (...) abnormal\n");
						return 0;
					}
					
					value = value.substring(1);							/* first char'(' remove */
					value = value.substring(0, value.length()-1);		/* last	char')' remove */
					if (value.isEmpty()) {
						br.close();
						logger.error("syntax is empty.\n");
						return 0;
					}
					value.replace("/LRB/", "(");
					value.replace("/RRB/", ")");
					node.syntax.add(value);
				} else if (rdata.indexOf("endl=") != -1 || rdata.indexOf("ENDL=") != -1) {
					if (value.isEmpty()) {
						br.close();
						logger.error("endl's value is empty.\n");
						return 0;
					}
					maketokens(node);
					vnode.add(node);
					node.clearNode();
				} else {
					node.fields.add(value);
				}
				buf = br.readLine();
			}
			
			br.close();
			
			if (!node.functionName.isEmpty())
			{
				logger.error("endl=end seems to be omitted!!!\n");
				return 0;
			}
 		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		
		return 1;
	}
	
	public int maketokens(Inode node) {
		String syntax;
		int idx;
		int init = 0;

		node.tokens = new Vector<String>();
		syntax = node.syntax.get(0);
		if (syntax.indexOf(SignSign, init) != -1)
		{
			logger.error("continued double Sign[$s$s] is not allowed!!!\n");
			return 0;
		}
		
		for(; !syntax.isEmpty(); ) {
			logger.debug("maketoknes()");
			idx = syntax.indexOf(Sign, init);
			if (idx != -1) {
				if (idx > init) {
					String tok = syntax.substring(init, idx);
					logger.debug(tok);
					node.tokens.add(tok);
				}
				node.tokens.add(Sign);
				syntax = syntax.substring(idx+Sign.length());
			} else {
				 node.tokens.add(syntax);
				 break;
			}
		}
		return 1;
	}
	
	//public int doParse(String inputstr, String resultstr, String hostip) {
	public int doParse(String inputstr, String resultstr, InetAddress hostip) {
		Vector<String> pardata = new Vector<String>(); /* parsed data */
		Boolean islasttoken;
		String index;
		resultstr = new String();
		int i, j;
		String ptr, ptr2, pend = new String();

		pend = inputstr.substring(inputstr.indexOf(inputstr.length()-1));
		for (i = 0; i < vnode.size(); ++i)
		{
			ptr = inputstr;
			pardata = new Vector<String>();
			for (j = 0; j < vnode.get(i).tokens.size(); ++j)
			{
				islasttoken = (j+1 == vnode.get(i).tokens.size()) ? true : false;
				if (vnode.get(i).tokens.get(j) == Sign)
				{
					if (islasttoken)
					{
						pardata.add(ptr);
						ptr = inputstr.substring(inputstr.indexOf(inputstr.length()));
					}
					else
					{
						ptr2 = ptr.substring(ptr.indexOf(vnode.get(i).tokens.get(j+1)));
						if (ptr2.isEmpty()) break;
						pardata.add(ptr.substring(ptr.indexOf(ptr2)));
						ptr = ptr2 + vnode.get(i).tokens.get(j+1).length();
						++j; /* because next token is also processed */
					}
				} 
				else /* constant string */
				{
					ptr2 = ptr.substring(ptr.indexOf(vnode.get(i).tokens.get(j+1)));
					if (ptr2.isEmpty()) break;
					else
					{
						if (ptr2 != ptr) break;
						ptr += vnode.get(i).tokens.get(j).length();
					}
				}
			}/* inner for loop */
			if (ptr == pend && j == vnode.get(i).tokens.size()) break;
		}/* out for loop */
		if (i == vnode.size()) return 0;
		resultstr = "<FC>" + vnode.get(i).functionName + "</FC><ARG>"; 
		for(int k=0;k < vnode.get(i).fields.size();++k)
		{
			if (k>0) resultstr += "~";
			if (vnode.get(i).fields.get(k) == "hostip")
				resultstr += hostip;
			else if (vnode.get(i).fields.get(k).charAt(0) == '#')
			{
				index = String.format("%s", vnode.get(i).fields.get(k));
				if (Integer.parseInt(index+1) < pardata.size())
					resultstr += pardata.get(Integer.parseInt(index+1));
				else
				{
					return 0;
				}
			}
			else if (vnode.get(i).fields.get(k).charAt(0) == '(' 
					&& vnode.get(i).fields.get(k).charAt(vnode.get(i).fields.get(k).length()-1) == ')')
			{
				int idx, idx2;
				String cmplxstr = new String();
				cmplxstr = vnode.get(i).fields.get(k).substring(1);
				cmplxstr = cmplxstr.substring(0, cmplxstr.length()-1); /* last ')' remove */
				for(;;)
				{
					idx = cmplxstr.indexOf("/#");
					if (idx == -1) break;
					idx2 = cmplxstr.indexOf("/", idx+1);
					if (idx2 == -1) return 0;
					index = String.format("%s", cmplxstr.substring(idx+2,idx2-(idx+2)));
					if (Integer.parseInt(index) < pardata.size())
						cmplxstr.replace(cmplxstr.substring(idx, idx2), pardata.get(Integer.parseInt(index)));
					else
						return 0;
				}
				resultstr += cmplxstr;
			}
			else
				resultstr += vnode.get(i).fields.get(k);
		}
		resultstr += "</ARG>";
		return 1;
	}
	
	public int getLogId(String inputstr, String LogId) {
		Boolean islasttoken;
		
		LogId = new String();
		int i,j;
		String ptr, ptr2, pend = new String();

		pend = inputstr.substring(inputstr.indexOf(inputstr.length()));
		for (i=0; i < vnode.size(); ++i) {
			ptr = inputstr;
			pdata.clear();
			for (j=0; j < vnode.get(i).tokens.size(); ++j) {
				islasttoken = (j+1 == vnode.get(i).tokens.size()) ? true : false;
				if (vnode.get(i).tokens.get(j) == Sign) {
					if (islasttoken) {
						pdata.add(ptr);
						ptr = inputstr.substring(inputstr.indexOf(inputstr.length()));
					} 
					else {
						ptr2 = ptr.substring(ptr.indexOf(vnode.get(i).tokens.get(j+1)));
						if (ptr2.isEmpty())
							break;
						pdata.add(ptr.substring(ptr.indexOf(ptr2)));
						ptr = ptr2 + vnode.get(i).tokens.get(j+1).length();
						++j; /* because next token is also processed */
					}
				} 
				else {	/* constant string */
					ptr2 = ptr.substring(ptr.indexOf(vnode.get(i).tokens.get(j)));
					if (ptr2.isEmpty())
						break;
					else {
						if (ptr2 != ptr)
							break;
						ptr += vnode.get(i).tokens.get(j).length();
					}
				}
			}	/* inner for loop */
			if (ptr == pend && j == vnode.get(i).tokens.size())
				break;
		}	/* out for loop */
		if (i == vnode.size()) {
			logger.error("i=[%d], vnode.size()=[%d]\n", i, vnode.size());
			return 0;
		}
		
		LogId = vnode.get(i).functionName;
		
		return 1;
	}
	
	public int printIniInfo() {
		logger.info("REPORT--------------");
		for (int j=0; j < vnode.size(); ++j) {
			logger.info("(" + j+1 + ")function:" + vnode.get(j).functionName + "---------------");
			logger.info("syntax size:" + vnode.get(j).syntax.size());
			logger.info("fields size:" + vnode.get(j).fields.size());

			//for (unsigned i=0; i < vnode[j].syntax.size(); ++i)
				logger.info("syntax:" + vnode.get(j).syntax.get(0));
			for (int i=0; i < vnode.get(j).tokens.size(); ++i)
				logger.info("->tokens(" + i+1 + "):" + vnode.get(j).tokens.get(i));
			for (int i=0; i < vnode.get(j).fields.size(); ++i)
				logger.info("->fields(" + i+1 + "):" + vnode.get(j).fields.get(i));
		}
		
		return 1;
	}
}

public class ParseRule {
	static Logger logger = LogManager.getRootLogger();

	
}
