package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;


public abstract class Chain extends Statement {
	
	TypeName typename;
	
	
	public TypeName getTypename() {
		return typename;
	}


	public void setTypename(TypeName typename) {
		this.typename = typename;
	}


	public Chain(Token firstToken) {
		super(firstToken);
	}

}
