package cop5556sp17;

import cop5556sp17.Scanner.Kind;

public class ScannerHelper {
	
	
	public static Kind isKeyword(String s){
		if(s.equals("eof"))
			return null;
		for(Kind k: Kind.values()){
			if(s.equals(k.getText())){
				return k;
			}
		}
		
		return null;
	}
}
