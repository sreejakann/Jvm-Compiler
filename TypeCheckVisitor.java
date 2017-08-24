package cop5556sp17;

import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.List;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Chain e0 = binaryChain.getE0();
		ChainElem e1 = binaryChain.getE1();
		e0.visit(this, arg);
		e1.visit(this, arg);
		Token op = binaryChain.getArrow();
		Token ft = e1.getFirstToken();
		switch(op.kind){
			case ARROW:{
				if(e0.getTypename().isType(URL) && e1.getTypename().isType(IMAGE)){
					binaryChain.setTypename(IMAGE);
					return binaryChain;
				}
				else if(e0.getTypename().isType(FILE) && e1.getTypename().isType(IMAGE)){
					binaryChain.setTypename(IMAGE);
					return binaryChain;
				}
				else if(e0.getTypename().isType(FRAME) && (e1 instanceof FrameOpChain) && (ft.isKind(KW_XLOC) || ft.isKind(KW_YLOC))){
					binaryChain.setTypename(INTEGER);
					return binaryChain;
				}
				else if(e0.getTypename().isType(FRAME) && (e1 instanceof FrameOpChain) && (ft.isKind(KW_SHOW) || ft.isKind(KW_MOVE) || ft.isKind(KW_HIDE))){
					binaryChain.setTypename(FRAME);
					return binaryChain;
				}
				else if(e0.getTypename().isType(IMAGE) && (e1 instanceof ImageOpChain) && (ft.isKind(OP_WIDTH) || ft.isKind(OP_HEIGHT))){
					binaryChain.setTypename(INTEGER);
					return binaryChain;
				}
				else if(e0.getTypename().isType(IMAGE) && e1.getTypename().isType(FRAME)){
					binaryChain.setTypename(FRAME);
					return binaryChain;
				}
				else if(e0.getTypename().isType(IMAGE) && e1.getTypename().isType(FILE)){
					binaryChain.setTypename(NONE);
					return binaryChain;
				}
				else if(e0.getTypename().isType(IMAGE) && (e1 instanceof FilterOpChain) && (ft.isKind(OP_GRAY) || ft.isKind(OP_BLUR) || ft.isKind(OP_CONVOLVE))){
					binaryChain.setTypename(IMAGE);
					return binaryChain;
				}
				else if(e0.getTypename().isType(IMAGE) && (e1 instanceof ImageOpChain) && (ft.isKind(KW_SCALE))){
					binaryChain.setTypename(IMAGE);
					return binaryChain;
				}
//				else if(e0.getTypename().isType(IMAGE) && (e1 instanceof FrameOpChain)){
//					binaryChain.setTypename(IMAGE);
//					return binaryChain;
//				}
				else if (e0.getTypename().isType(IMAGE) && (e1 instanceof IdentChain) && e1.getTypename().isType(IMAGE)) {
					binaryChain.setTypename(IMAGE);
					return binaryChain;
				}
				else if (e0.getTypename().isType(IMAGE) && (e1 instanceof IdentChain) && e1.getTypename().isType(INTEGER)) {
					binaryChain.setTypename(IMAGE);
					return binaryChain;
				}
				else if (e0.getTypename().isType(INTEGER) && (e1 instanceof IdentChain) && e1.getTypename().isType(INTEGER)){
					binaryChain.setTypename(INTEGER);
					return binaryChain;
				}
				else
					throw new TypeCheckException("not a valid binary chain for arrow op");
			}
			case BARARROW:{
				if(e0.getTypename().isType(IMAGE) && (e1 instanceof FilterOpChain) && (ft.isKind(OP_GRAY) || ft.isKind(OP_BLUR) || ft.isKind(OP_CONVOLVE))){
					binaryChain.setTypename(IMAGE);
					return binaryChain;
				}
				else
					throw new TypeCheckException("not a valid binary chain for bararrow op");
			}
			default:{
				throw new TypeCheckException("op is neither bararrow nor arrow");
			}
		}
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();
		e0.visit(this, arg);
		e1.visit(this, arg);
		Token op = binaryExpression.getOp();
		switch(op.kind){
			case PLUS:
			case MINUS: {
				if(e0.getTypename().isType(INTEGER) && e1.getTypename().isType(INTEGER)){
					binaryExpression.setTypename(INTEGER);
					return binaryExpression;
				}
				else if(e0.getTypename().isType(IMAGE) && e1.getTypename().isType(IMAGE)){
					binaryExpression.setTypename(IMAGE);
					return binaryExpression;
				}
				else
					throw new TypeCheckException("expression types are not integer or image");
			}
			case TIMES:{
				if(e0.getTypename().isType(INTEGER) && e1.getTypename().isType(INTEGER)){
					binaryExpression.setTypename(INTEGER);
					return binaryExpression;
				}
				else if(e0.getTypename().isType(INTEGER) && e1.getTypename().isType(IMAGE)){
					binaryExpression.setTypename(IMAGE);
					return binaryExpression;
				}
				else if(e0.getTypename().isType(IMAGE) && e1.getTypename().isType(INTEGER)){
					binaryExpression.setTypename(IMAGE);
					return binaryExpression;
				}
				else
					throw new TypeCheckException("expression types are not integer or image");
			}
			case DIV:{
				if(e0.getTypename().isType(INTEGER) && e1.getTypename().isType(INTEGER)){
					binaryExpression.setTypename(INTEGER);
					return binaryExpression;
				}
				else if(e0.getTypename().isType(IMAGE) && e1.getTypename().isType(INTEGER)){
					binaryExpression.setTypename(IMAGE);
					return binaryExpression;
				}
				else
					throw new TypeCheckException("expression types for div op are not integers");
			}
			case MOD:{
				if(e0.getTypename().isType(INTEGER) && e1.getTypename().isType(INTEGER)){
					binaryExpression.setTypename(INTEGER);
					return binaryExpression;
				}
				else if(e0.getTypename().isType(IMAGE) && e1.getTypename().isType(INTEGER)){
					binaryExpression.setTypename(IMAGE);
					return binaryExpression;
				}
				else
					throw new TypeCheckException("expression types are not integer or image");
			}
			case AND:
			case OR:{
				if(e0.getTypename().isType(BOOLEAN) && e1.getTypename().isType(BOOLEAN)){
					binaryExpression.setTypename(BOOLEAN);
					return binaryExpression;
				}
				else
					throw new TypeCheckException("expression types are not integer or boolean for lt,gt,le and ge operands");
			}
			case LT:
			case GT:
			case LE:
			case GE:{
				if(e0.getTypename().isType(INTEGER) && e1.getTypename().isType(INTEGER)){
					binaryExpression.setTypename(BOOLEAN);
					return binaryExpression;
				}
				else if(e0.getTypename().isType(BOOLEAN) && e1.getTypename().isType(BOOLEAN)){
					binaryExpression.setTypename(BOOLEAN);
					return binaryExpression;
				}
				else
					throw new TypeCheckException("expression types are not integer or boolean for lt,gt,le and ge operands");
			}
			case EQUAL:
			case NOTEQUAL:{
				if(e0.getTypename() == e1.getTypename()){
					binaryExpression.setTypename(BOOLEAN);
					return binaryExpression;
				}
				else
					throw new TypeCheckException("expression types are not equal for operands equal and notequal");
			}
			default:{
				throw new TypeCheckException("operand is not legal");
			}
		}

	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub

		symtab.enterScope();
		List<Dec> dl = block.getDecs();
		List<Statement> sl = block.getStatements();
		int i =0;
		int j=0;
		Dec d1;
		Statement s1;
		int d_line = 0;
		int d_pos = 0;
		int s_line = 0;
		int s_pos = 0;
		while(i<dl.size() && j<sl.size()){
			d1 = dl.get(i);
			s1 = sl.get(j);
			d_line = d1.getFirstToken().getLinePos().line;
			s_line = s1.getFirstToken().getLinePos().line;
			if(d_line == s_line){
				d_pos = d1.getFirstToken().getLinePos().posInLine;
				s_pos = s1.getFirstToken().getLinePos().posInLine;
				if(d_pos < s_pos){
					d1.visit(this, arg);
					i++;
				}
				else{
					s1.visit(this, arg);
					j++;
				}
			}else if(d_line < s_line){
				d1.visit(this, arg);
				i++;
			}
			else{
				s1.visit(this, arg);
				j++;
			}

		}

		if(i == dl.size()){
			while(j<sl.size()){
				s1 = sl.get(j);
				s1.visit(this, arg);
				j++;
			}
		}
		if(j == sl.size()){
			while(i<dl.size()){
				d1 = dl.get(i);
				d1.visit(this, arg);
				i++;
			}
		}

		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setTypename(TypeName.BOOLEAN);
		return booleanLitExpression;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tp = filterOpChain.getArg();
		tp.visit(this, arg);
		if(tp.getExprList().size() != 0){
			throw new TypeCheckException("Tuple length is not zero");
		}
		filterOpChain.setTypename(IMAGE);
		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tp = frameOpChain.getArg();
		tp.visit(this, arg);
		Token t = frameOpChain.getFirstToken();
		if(t.isKind(KW_SHOW) || t.isKind(KW_HIDE)){
			if(tp.getExprList().size() != 0){
				throw new TypeCheckException("Tuple length is not zero");
			}
			frameOpChain.setTypename(NONE);
		}
		else if(t.isKind(KW_XLOC) || t.isKind(KW_YLOC)){
			if(frameOpChain.getArg().getExprList().size() != 0){
				throw new TypeCheckException("Tuple length is not zero");
			}
			frameOpChain.setTypename(INTEGER);
		}
		else if(t.isKind(KW_MOVE)){
			if(frameOpChain.getArg().getExprList().size() != 2){
				throw new TypeCheckException("Tuple length is not zero");
			}
			frameOpChain.setTypename(NONE);
		}
		else
			throw new TypeCheckException("Bug in parser");

		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String ident = identChain.getFirstToken().getText();
		Dec dec = symtab.lookup(ident);
		if(dec == null){
			throw new TypeCheckException("IdentChain:ident is not declared in current scope");
		}
		//int scope = symtab.scope;
		//if(!symtab.omap.get(ident).containsKey(scope)){
		//	throw new TypeCheckException("ident is not visibe in current scope");
		//}

		TypeName tn = dec.getTypename();
		identChain.setTypename(tn);
		identChain.setDec(dec);
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t = identExpression.getFirstToken();
		String ident = t.getText();
		Dec dec = symtab.lookup(ident);
		if(dec == null){
			throw new TypeCheckException("IdentExpr:ident is not declared in current scope");
		}
//		int scope = symtab.scope;
//		if(!symtab.omap.get(ident).containsKey(scope)){
//			throw new TypeCheckException("ident is not visibe in current scope");
//		}
//		TypeName tn = Type.getTypeName(t);
		identExpression.setTypename(dec.getTypename());
		identExpression.setDec(dec);

		return identExpression;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = ifStatement.getE();
		Block b = ifStatement.getB();
		e.visit(this, arg);
		b.visit(this, arg);
		if(!(e.getTypename().isType(BOOLEAN)))
			throw new TypeCheckException("expression type is not boolean for if statement");
		return ifStatement;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTypename(TypeName.INTEGER);
		return intLitExpression;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = sleepStatement.getE();
		e.visit(this, arg);
		if(!(e.getTypename().isType(INTEGER)))
			throw new TypeCheckException("expression type is not integer for sleep statement");
		return sleepStatement;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = whileStatement.getE();
		Block b = whileStatement.getB();
		e.visit(this, arg);
		b.visit(this, arg);
		if(!(e.getTypename().isType(BOOLEAN)))
			throw new TypeCheckException("expression type is not boolean for while statement");
		return whileStatement;

	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t = declaration.getFirstToken();
		TypeName typename = Type.getTypeName(t);
		declaration.setTypename(typename);

		String ident = declaration.getIdent().getText();
		Boolean check = symtab.insert(ident, declaration);
		if(!check){
			throw new TypeCheckException("Inserting duplicate variables under same scope is not allowed");
		}
		return declaration;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<ParamDec> pdl = program.getParams();
		for(ParamDec p : pdl){
			p.visit(this, arg);
		}
		Block b = program.getB();
		b.visit(this, arg);
		return program;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = assignStatement.getE();
		IdentLValue idl = assignStatement.getVar();
		e.visit(this, arg);
		idl.visit(this, arg);
		if(e.getTypename() != idl.getDec().getTypename())
			throw new TypeCheckException("type names for identlvalue and expression is not equal for assignmentstatement");
		return assignStatement;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String ident = identX.getFirstToken().getText();
		Dec dec = symtab.lookup(ident);
		if(dec == null){
			throw new TypeCheckException("IdentLval:ident is not declared in current scope");
		}

		identX.setDec(dec);
		return identX;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub

		Token t = paramDec.getFirstToken();
		TypeName typename = Type.getTypeName(t);
		paramDec.setTypename(typename);
		String ident = paramDec.getIdent().getText();
		Boolean check = symtab.insert(ident, paramDec);
		if(!check){
			throw new TypeCheckException("Inserting duplicate variables under same scope is not allowed");
		}
		return paramDec;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setTypename(TypeName.INTEGER);
		return constantExpression;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		imageOpChain.getArg().visit(this, arg);
		Token t = imageOpChain.getFirstToken();
		if(t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT)){
			if(imageOpChain.getArg().getExprList().size() != 0){
				throw new TypeCheckException("Tuple length is not zero");
			}
			imageOpChain.setTypename(INTEGER);
		}
		else if(t.isKind(KW_SCALE)){
			if(imageOpChain.getArg().getExprList().size() != 1){
				throw new TypeCheckException("Tuple length is not one");
			}
			imageOpChain.setTypename(IMAGE);
		}
		return imageOpChain;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<Expression> elist = tuple.getExprList();
		for(Expression e : elist){
			e.visit(this, arg);
			if(!(e.getTypename().isType(INTEGER)))
				throw new TypeCheckException("expression type is not integer for the given tuple");
		}

		return tuple;
	}


}
