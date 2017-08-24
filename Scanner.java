package cop5556sp17;

import java.util.ArrayList;
import java.util.Collections;

import cop5556sp17.Scanner.Kind;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	public static enum State {
		START, IN_DIGIT, IN_IDENT, AFTER_EQ, AFTER_MINUS, AFTER_NOT, AFTER_GT, AFTER_LT, AFTER_OR,IN_COMMENT, AFTER_COMMENT, AFTER_DIV;
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THIS
			if(kind == Kind.EOF)
				return Kind.EOF.getText();
			else
				return chars.substring(pos, pos + length);
		}
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }


		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//TODO IMPLEMENT THIS
			int line = Collections.binarySearch(linearr, pos);
			if(line < 0){
				line = -line - 2;
			}
			int posinline = pos - linearr.get(line);
			return new LinePos(line, posinline);
			
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			return Integer.parseInt(chars.substring(pos, pos + length));
		}

		public boolean isKind(Kind k) {
			// TODO Auto-generated method stub
			if(kind == k)
				return true;
			return false;
		}
		
	}

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		linearr = new ArrayList<Integer>();
		linearr.add(0);
	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		//TODO IMPLEMENT THIS!!!!
		int length = chars.length();
	    State state = State.START;
	    int startPos = 0;
	    int ch;
	    while (pos <= length) {
	        ch = pos < length ? chars.charAt(pos) : -1;
	        
	        switch (state) {
	            case START: {
	            	pos = skipWhiteSpace(pos);
	                ch = pos < length ? chars.charAt(pos) : -1;
	                startPos = pos;
	                switch (ch) {
	                	
	                    case -1: {
	                    	tokens.add(new Token(Kind.EOF, pos, 0)); 
	                    	pos++;
	                    	}  break;
	                    case '\n': {
	                		linearr.add(pos+1);
	                		pos++;
	                	}break;
	                    case '+': {
	                    	tokens.add(new Token(Kind.PLUS, startPos, 1));
	                    	pos++;
	                    	} break;
	                    case '*': {
	                    	tokens.add(new Token(Kind.TIMES, startPos, 1));
	                    	pos++;
	                    	} break;
	                    case '=': {
	                    	state = State.AFTER_EQ;pos++;
	                    	}break;
	                    case '-': {
	                    	state = State.AFTER_MINUS;
	                    	pos++;
	                    }break;
	                    case '>':{
	                    	state = State.AFTER_GT;
	                    	pos++;
	                    }break;
	                    case '<' :{
	                    	state = State.AFTER_LT;
	                    	pos++;
	                    }break;
	                    case '!': {
	                    	state = State.AFTER_NOT;
	                    	pos++;
	                    } break;
	                    case '|' :{
	                    	state = State.AFTER_OR;
	                    	pos++;
	                    }break;
	                    case '/' :{
	                    	state = State.AFTER_DIV;
	                    	pos++;
	                    }break;
	                    case '0': {
	                    	tokens.add(new Token(Kind.INT_LIT,startPos, 1));
	                    	pos++;
	                    	}break;
	                    case '(': {
	                    	tokens.add(new Token(Kind.LPAREN, startPos, 1));
	                    	pos++;
	                    } break;
	                    case ')': {
	                    	tokens.add(new Token(Kind.RPAREN, startPos, 1));
	                    	pos++;
	                    } break;
	                    case '{': {
	                    	tokens.add(new Token(Kind.LBRACE, startPos, 1));
	                    	pos++;
	                    } break;
	                    case '}': {
	                    	tokens.add(new Token(Kind.RBRACE, startPos, 1));
	                    	pos++;
	                    } break;
	                    case ';': {
	                    	tokens.add(new Token(Kind.SEMI, startPos, 1));
	                    	pos++;
	                    } break;
	                    case '&' :{
	                    	tokens.add(new Token(Kind.AND, startPos, 1));
	                    	pos++;
	                    } break;
	                    case ',' :{
	                    	tokens.add(new Token(Kind.COMMA, startPos, 1));
	                    	pos++;
	                    } break;
	                    case '%' :{
	                    	tokens.add(new Token(Kind.MOD, startPos, 1));
	                    	pos++;
	                    } break;
	                    
	                    default: {
	                        if (Character.isDigit(ch)) {
	                        	state = State.IN_DIGIT;
	                        	pos++;
	                        } 
	                        else if (Character.isJavaIdentifierStart(ch)) {
	                             state = State.IN_IDENT;pos++;
	                         } 
	                         else {
	                        	 throw new IllegalCharException("illegal char " +ch+" at pos "+pos);
	                         }
	                      }
	                } // switch (ch)

	            }  break;
	            case IN_DIGIT: {
	            	if(Character.isDigit(ch)) {
	                    pos++;
	            	} 
	            	else {
	            		String s = chars.substring(startPos, pos);
	            		try{
	            			Integer.parseInt(s);
	            			tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
		                    state = State.START;
	            		}
	                    catch(Exception e){
	                    	//Scanner.LinePos lp = getLinePos(t);
	                    	throw new IllegalNumberException("illegal number" + s);
	                    }
	            	}
	            }  break;
	            case IN_IDENT: {
	            	if(Character.isJavaIdentifierPart(ch)) {
	                    pos++;
	            	} 
	            	else {
	            		Kind k = ScannerHelper.isKeyword(chars.substring(startPos, pos));
	            		if(k!=null)
	            			tokens.add(new Token(k,startPos,pos - startPos));
	            		else
	            			tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
	                    state = State.START;
	            	}
	            }  break;
	            case AFTER_EQ: {
	            	if(ch == '='){
	            		pos++;
	            		tokens.add(new Token(Kind.EQUAL, startPos, pos - startPos));
	                    state = State.START;
	                    
	            	}
	            	else {throw new IllegalCharException(
                            "illegal char " +ch+" at pos "+pos);
                    }
	            		
	            }  break;
	            case AFTER_MINUS: {
	            	Kind k = Kind.MINUS;
	            	if(ch == '>'){
	            		k = Kind.ARROW;
	            		pos++;
	            	}
	            	tokens.add(new Token(k, startPos, pos - startPos));
            		state = State.START;
	            	
	            } break;
	            case AFTER_DIV: {
	            	if(ch == '*'){
	            		state = State.IN_COMMENT;
	            		pos++;
	            	}
	            	else{
	            		tokens.add(new Token(Kind.DIV, startPos, pos - startPos));
	            		state = State.START;
	            	}
	            		
	            }break;
	            case IN_COMMENT: {
	            	if(ch == '*'){
	            		state = State.AFTER_COMMENT;
	            	}
	            	else if(ch == '\n'){
	            		linearr.add(pos+1);
	            	}
	            	else if(ch == -1){
	            		tokens.add(new Token(Kind.EOF, startPos, 0));
	            		state = State.START;
	            	}
	            pos++;
	            }break;
	            
	            case AFTER_COMMENT: {
	            	if(ch == '/'){
	            		state = State.START;
	            	}
	            	else if(ch == '\n'){
	            		linearr.add(pos+1);
	            	}
	            	else if(ch == -1){
	            		tokens.add(new Token(Kind.EOF, startPos, 0));
	            		state = State.START;
	            	}
	            	else{
	            		if(ch != '*') state = State.IN_COMMENT;
	            	}
	            	pos++;
	            }break;
	            case AFTER_NOT: {
	            	Kind k = Kind.NOT;
	            	if(ch == '='){
	            		k = Kind.NOTEQUAL;
	            		pos++;
	            	}
	            	tokens.add(new Token(k, startPos, pos - startPos));
	            	state = State.START;
	            } break;
	            
	            case AFTER_GT: {
	            	Kind k = Kind.GT;
	            	if(ch == '='){
	            		k = Kind.GE;
	            		pos++;
	            	}
	            	tokens.add(new Token(k, startPos, pos - startPos));
	            	state = State.START;
	            } break;
	            
	            case AFTER_LT: {
	            	Kind k = Kind.LT;
	            	if(ch == '='){
	            		k = Kind.LE;
	            		pos++;
	            	}
	            	else if(ch == '-'){
	            		k = Kind.ASSIGN;
	            		pos++;
	            	}
	            	tokens.add(new Token(k, startPos, pos - startPos));
	            	state = State.START;
	            } break;
	            
	            case AFTER_OR: {
	            	Kind k = Kind.OR;
	            	if(ch == '-'){
	            		if(pos+1<length && chars.charAt(pos+1) == '>'){
	            			k = Kind.BARARROW;
	            			pos = pos+2;
	            		}
	            	}
	            	tokens.add(new Token(k, startPos, pos - startPos));
	            	state = State.START;
	            	
	            } break;
	            default:  assert false;
	        }// switch(state)
	    } // while
		//tokens.add(new Token(Kind.EOF,pos,0));
		return this;  
	}



	private int skipWhiteSpace(int pos) {
		// TODO Auto-generated method stub
		int len = chars.length();
		int n = pos;
			//while(n<len && chars.charAt(n) == ' '){
			while(n<len && Character.isWhitespace(chars.charAt(n))){
				if(chars.charAt(n) == '\n')
					break;
				n++;
			}

		return n;
	}


	final ArrayList<Integer> linearr;
	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	public Token getNextToken() {
		if(tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum + 1);
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		return t.getLinePos();
	}


}
