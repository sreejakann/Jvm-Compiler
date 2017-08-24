package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public abstract class Statement extends ASTNode {
	
	TypeName typename;
	

	public TypeName getTypename() {
		return typename;
	}

	public void setTypename(TypeName typename) {
		this.typename = typename;
	}

	public Statement(Token firstToken) {
		super(firstToken);
	}

	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
