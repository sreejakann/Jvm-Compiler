package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program p = null;
		p = program();
		matchEOF();
		return p;
	}

	Expression expression() throws SyntaxException {
		//TODO
		Expression e0 = null;
		Expression e1 = null;
		Token ftoken = t;
		e0 = term();
		while(t.isKind(LT) || t.isKind(LE) || t.isKind(GT) || t.isKind(GE) 
				|| t.isKind(EQUAL) || t.isKind(NOTEQUAL)){
			Token op = t;
			consume();
			e1 = term();
			e0 = new BinaryExpression(ftoken,e0,op,e1);
		}
		return e0;
	}

	Expression term() throws SyntaxException {
		//TODO
		Expression e0 = null;
		Expression e1 = null;
		Token ftoken = t;
		e0 = elem();
		while(t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR)){
			Token op = t;
			consume();
			e1 = elem();
			e0 = new BinaryExpression(ftoken,e0,op,e1);
		}
		return e0;
	}

	Expression elem() throws SyntaxException {
		//TODO
		Expression e0 = null;
		Expression e1 = null;
		Token ftoken = t;
		e0 = factor();
		while(t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)){
			Token op = t;
			consume();
			e1 = factor();
			e0 = new BinaryExpression(ftoken, e0, op, e1);
		}
		return e0;
		
	}

	Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		Expression e = null;
		switch (kind) {
		case IDENT: {
			e = new IdentExpression(t);
			consume();
		}
			break;
		case INT_LIT: {
			e = new IntLitExpression(t);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e = new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e = new ConstantExpression(t);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e = expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor at: "+ t.kind);
		}
		return e;
	}

	Block block() throws SyntaxException {
		//TODO
		Token ftoken = match(LBRACE);
		Dec e = null;
		Statement s = null;
		ArrayList<Dec> decs = new ArrayList<Dec>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		while(!t.isKind(RBRACE)){
			if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME)){
				e = dec();
				decs.add(e);
			}
			else{
				s = statement();
				statements.add(s);
			}
		}
		match(RBRACE);
		return new Block(ftoken,decs,statements);
		
	}

	Program program() throws SyntaxException {
		//TODO 
		Token ftoken = match(IDENT);
		ParamDec p = null;
		ArrayList<ParamDec> paramdecs = new ArrayList<ParamDec>();
		Block b = null;
		if(t.isKind(LBRACE)){
			b = block();
			return new Program(ftoken,paramdecs,b);
		}
		else{
			p = paramDec();
			paramdecs.add(p);
			while(t.isKind(COMMA)){
				match(COMMA);
				p = paramDec();
				paramdecs.add(p);
			}
			b = block();
			return new Program(ftoken,paramdecs,b);
		}
	}

	ParamDec paramDec() throws SyntaxException {
		//TODO
		Token ftoken = t;
		switch(t.kind){
		case KW_URL:{
			consume();
		}break;
		case KW_FILE: {
			consume();
		}break;
		case KW_INTEGER: {
			consume();
		}break;
		case KW_BOOLEAN: {
			consume();
		}break;
		default: 
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal paramDec at: "+t.kind);
		}
		Token id = match(IDENT);
		return new ParamDec(ftoken,id);
	}

	Dec dec() throws SyntaxException {
		//TODO
		Token ftoken = t;
		switch(t.kind){
		case KW_INTEGER:{
			consume();
		}break;
		case KW_BOOLEAN: {
			consume();
		}break;
		case KW_IMAGE: {
			consume();
		}break;
		case KW_FRAME: {
			consume();
		}break;
		default: 
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal dec at: "+t.kind);
		}
		Token id = match(IDENT);
		return new Dec(ftoken,id);
	}

	Statement statement() throws SyntaxException {
		//TODO 
		Statement s = null;
		Expression e = null;
		Block b = null;
		Token ftoken = null;
		switch(t.kind){
		case OP_SLEEP:{
			ftoken = consume();
			e = expression();
			match(SEMI);
			s = new SleepStatement(ftoken,e);
		}break;
		case KW_WHILE:
		case KW_IF:{
			ftoken = consume();
			match(LPAREN);
			e = expression();
			match(RPAREN);
			b = block();
			if(ftoken.isKind(KW_WHILE))
				s = new WhileStatement(ftoken,e,b);
			else
				s = new IfStatement(ftoken,e,b);
			
		}break;
		default:
			s = checkChainAssign();
		}
		return s;
	}
	Statement checkChainAssign() throws SyntaxException{
		Statement s = null;
		switch(t.kind){
		case IDENT:{
			Expression e = null;
			Token ftoken = t;
			IdentLValue var = new IdentLValue(ftoken);
			Token nt = scanner.peek();
			if(nt.isKind(ASSIGN)){
				consume();
				consume();
				e = expression();
				s = new AssignmentStatement(ftoken,var,e);
			}
			else{
				s = chain();
			}
		}break;
		default:
			s = chain();
		}
		match(SEMI);
		return s;
	}

	Chain chain() throws SyntaxException {
		//TODO
		Chain e0 = null;
		ChainElem e1 = null;
		Token ftoken = t;
		e0 = chainElem();
		Token op = arrowOp();
		e1 = chainElem();
		e0 = new BinaryChain(ftoken,e0,op,e1);
		while(t.isKind(ARROW) || t.isKind(BARARROW)){
			op = arrowOp();
			e1 = chainElem();
			e0 = new BinaryChain(ftoken,e0,op,e1);
		}
		return e0;
	}

	ChainElem chainElem() throws SyntaxException {
		//TODO
		ChainElem e = null;
		Token ftoken = null;
		Tuple tp = null;
		switch(t.kind){
		case IDENT:{
			e = new IdentChain(t);
			consume();
		}break;
		case OP_BLUR: case OP_GRAY: case OP_CONVOLVE:{
			ftoken = t;
			consume();
			tp = arg();
			e = new FilterOpChain(ftoken,tp);
		}break;
		case KW_SHOW: case KW_HIDE: case KW_MOVE: case KW_XLOC: case KW_YLOC:{
			ftoken = t;
			consume();
			tp = arg();
			e = new FrameOpChain(ftoken,tp);
		}break;
		case OP_WIDTH: case OP_HEIGHT: case KW_SCALE:{
			ftoken = t;
			consume();
			tp = arg();
			e = new ImageOpChain(ftoken,tp);
		}break;
		default:
			throw new SyntaxException("illegal chainElem at: "+t.kind);
		}
		return e;
	}

	Tuple arg() throws SyntaxException {
		Tuple tp = null;
		Expression e = null;
		Token ftoken = t;
		List<Expression> elist = new ArrayList<Expression>();
		if(!t.isKind(LPAREN))
			return new Tuple(ftoken,elist);
		match(LPAREN);
		e = expression();
		elist.add(e);
		while(t.isKind(COMMA)){
			consume();
			e = expression();
			elist.add(e);
		}
		match(RPAREN);
		tp = new Tuple(ftoken,elist);
		return tp;
	}
	
	
	Token arrowOp() throws SyntaxException {
		Token ftoken = null;
		switch(t.kind){
		case ARROW:{
			ftoken = consume();
		}break;
		case BARARROW: {
			ftoken = consume();
		}break;
		default:
			throw new SyntaxException("illegal arrowOp at: "+t.kind);
		}
		return ftoken;
	}
	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
