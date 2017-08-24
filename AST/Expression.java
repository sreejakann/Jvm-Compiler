package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public abstract class Expression extends ASTNode {

	TypeName typename;


	public TypeName getTypename() {
		return typename;
	}


	public void setTypename(TypeName typename) {
		this.typename = typename;
	}


	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
