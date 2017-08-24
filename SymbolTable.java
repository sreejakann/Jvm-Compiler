package cop5556sp17;



import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	
	//TODO  add fields
	Stack<Integer> st = new Stack<Integer>();
	Map<String, Map<Integer ,Dec>> omap = new HashMap<String, Map<Integer ,Dec>>();
	int scope = 0;
	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		
		st.push(scope++);
		
	}
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		st.pop();
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		int sc = st.peek();
		
		Map<Integer ,Dec> imap;
		if(!omap.containsKey(ident)){
			imap = new HashMap<Integer,Dec>();
			imap.put(sc, dec);
			omap.put(ident, imap);
			return true;
		}
		imap = omap.get(ident);
		
		if(imap.containsKey(sc)){
			return false;
		}
		imap.put(sc, dec);
		omap.put(ident, imap);
		return true;
	}
	
	public Dec lookup(String ident){
		int s=0;
		Dec dec = null;
		Map<Integer ,Dec> imap = omap.get(ident);
		if (imap == null) return null;
		
		for(int i=st.size()-1;i>=0;i--){
			s = st.get(i);
			if(imap.containsKey(s)){
				dec = imap.get(s);
				break;
			}
				
		}
		return dec;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		st.push(scope++);
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		int sc = st.peek();
		
		
		return "";
	}
	
	


}
