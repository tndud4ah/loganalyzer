package LogParseD;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
			while (buf != null)
			{
				rdata = buf;
				rdata.trim();
				if (rdata.isEmpty()) continue;
				if (rdata.indexOf(0) == '#') continue; /* comment line */
				idx = rdata.indexOf("=");
				if (idx == -1) continue; /* strange line */
				value = rdata.substring(idx+1);
				if (value.isEmpty())
				{
					br.close();
					logger.error("rvalue is empty");
					return 0;
				}
				if (rdata.indexOf("LOG_ID=") != -1)
				{
					if (!node.functionName.isEmpty())
					{
						br.close();
						logger.error("endl=end seems to be omitted!!!\n");
						return 0;					
					}
					node.functionName = value;
				}
				else if (rdata.indexOf("syntax=") != -1 || rdata.indexOf("SYNTAX=") != -1)
				{
					if (value.charAt(0) != '(' || value.charAt(value.length()-1) != ')')
					{
						br.close();
						logger.error("syntax (...) abnormal\n");
						return 0;
					}
					value = value.substring(1);							/* first char'(' remove */
					value = value.substring(0, value.length()-1);		/* last	char')' remove */
					if (value.isEmpty())
					{
						br.close();
						logger.error("syntax is empty.\n");
						return 0;
					}
					value.replace("/LRB/", "(");
					value.replace("/RRB/", ")");
					node.syntax.add(value);
				}
				else if (rdata.indexOf("endl=") != -1 || rdata.indexOf("ENDL=") != -1)
				{
					if (value.isEmpty())
					{
						br.close();
						logger.error("endl's value is empty.\n");
						return 0;
					}
					maketokens(node);
					vnode.add(node);
					node.clearNode();
				}
				else
				{
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
			idx = syntax.indexOf(Sign, init);
			if (idx != -1) {
				if (idx > init)
					node.tokens.add(syntax.substring(init, idx));
				node.tokens.add(Sign);
				syntax.substring(init,idx+Sign.length());
			} else {
				 node.tokens.add(syntax);
				 break;
			}
		}
		return 1;
	}
	
	public int doParse(String inputstr, String resultstr, String hostip) {
		Vector<String> pardata = new Vector<String>(); /* parsed data */
		Boolean islasttoken;
		String index;
		resultstr = new String();
		int i, j;
		String ptr, ptr2, pend = new String();

		pend = inputstr.substring(inputstr.indexOf(inputstr.length()));
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
						pardata.add(string(ptr,ptr2-ptr));
						ptr = ptr2 + vnode[i].tokens[j+1].size();
						++j; /* because next token is also processed */
					}
				} 
				else /* constant string */
				{
					ptr2 = strstr(ptr,vnode[i].tokens[j].c_str());
					if (!ptr2) break;
					else
					{
						if (ptr2 != ptr) break;
						ptr += vnode[i].tokens[j].size();
					}
				}
			}/* inner for loop */
			if (ptr == pend && j == vnode[i].tokens.size()) break;
		}/* out for loop */
		if (i == vnode.size()) return -1;
		resultstr = "<FC>" + vnode[i].functionName + "</FC><ARG>"; 
		for(unsigned k=0;k < vnode[i].fields.size();++k)
		{
			if (k>0) resultstr += "~";
			if (vnode[i].fields[k] == "hostip")
				resultstr += hostip;
			else if (vnode[i].fields[k].at(0) == '#')
			{
				snprintf(index,sizeof index,"%s",vnode[i].fields[k].c_str());
				if ((unsigned)atoi(index+1) < pardata.size())
					resultstr += pardata[atoi(index+1)];
				else
				{
					return -1;
				}
			}
			else if (vnode[i].fields[k].at(0) == '(' && vnode[i].fields[k].at(vnode[i].fields[k].size()-1) == ')') /* 20110519 add */
			{ /* complex string process */
				string::size_type idx,idx2;
				string cmplxstr = vnode[i].fields[k].substr(1);
				cmplxstr.erase(cmplxstr.size()-1); /* last ')' remove */
				for(;;)
				{
					idx = cmplxstr.find("/#");
					if (idx == string::npos) break;
					idx2 = cmplxstr.find("/",idx+1);
					if (idx2 == string::npos) return -1;
					snprintf(index,sizeof index,"%s",cmplxstr.substr(idx+2,idx2-(idx+2)).c_str());
					if ((unsigned)atoi(index) < pardata.size())
						cmplxstr.replace(idx,idx2+1-idx,pardata[atoi(index)]);
					else
						return -1;
				}
				resultstr += cmplxstr;
			}
			else
				resultstr += vnode[i].fields[k];
		}
		resultstr += "</ARG>";
		return 0;
	}
	public int getLogId(String inputstr, String LogId) {
		return 0;
	}
	public int doParse2(String inputstr, String resultstr, String hostip) {
		return 0;
	}
	public int printIniInfo() {
		return 0;
	}
}

public class ParseRule {
	static Logger logger = LogManager.getRootLogger();

	
}
