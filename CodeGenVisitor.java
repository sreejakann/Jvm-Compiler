package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
//import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;

	int param_counter = 0;
	int dec_counter = 1;
	ArrayList<Dec> dec_list = new ArrayList<Dec>();


	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		for(Dec dec: dec_list){
			mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypename().getJVMTypeDesc(), null, startRun, endRun, dec.getSlotNum());
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypename());
		assignStatement.getVar().visit(this, arg);
		Token t = assignStatement.getVar().getDec().getType();
		//if(t.isKind(KW_IMAGE)){
			//mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
		//}
		return assignStatement;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {

		Token op = binaryChain.getArrow();
		Chain E0 = binaryChain.getE0();
		ChainElem E1 = binaryChain.getE1();
		Token t0 = E0.getFirstToken();
		Token t1 = E1.getFirstToken();
		if(t0.isKind(OP_BLUR) || t0.isKind(OP_GRAY) || t0.isKind(OP_CONVOLVE)){
			if(op.isKind(ARROW)){
				mv.visitInsn(ACONST_NULL);
			}
			else if(op.isKind(BARARROW)){
				if((t0.isKind(OP_CONVOLVE)))
					mv.visitInsn(ACONST_NULL);
				else
					mv.visitInsn(DUP);
			}
			E0.visit(this, null);
		}

		else if(E0 instanceof IdentChain)
			E0.visit(this, true);
		else
			E0.visit(this, null);


		if(t1.isKind(OP_BLUR) || t1.isKind(OP_GRAY) || t1.isKind(OP_CONVOLVE)){
			if(op.isKind(ARROW)){
				mv.visitInsn(ACONST_NULL);
			}
			else if(op.isKind(BARARROW)){
				if((t1.isKind(OP_CONVOLVE)))
					mv.visitInsn(ACONST_NULL);
				else
					mv.visitInsn(DUP);
			}
			E1.visit(this, null);
		}

		else if(E1 instanceof IdentChain)
			E1.visit(this, false);
		else
			E1.visit(this, null);


		return binaryChain;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();
		Token op = binaryExpression.getOp();

		e0.visit(this, null);
		e1.visit(this, null);

		if(e0.getTypename().getJVMTypeDesc().equals("I") && e1.getTypename().getJVMTypeDesc().equals("I")){
			switch(op.kind){
				case PLUS:{
					mv.visitInsn(IADD);
				} break;
				case MINUS:{
					mv.visitInsn(ISUB);
				}break;
				case TIMES:{
					mv.visitInsn(IMUL);
				}break;
				case DIV:{
					mv.visitInsn(IDIV);
				}break;
				case MOD:{
					mv.visitInsn(IREM);
				}break;
				case LT:
				case GT:
				case LE:
				case GE:
				case EQUAL:
				case NOTEQUAL:{
					Label l1 = new Label();
					if(op.isKind(LT))
						mv.visitJumpInsn(IF_ICMPGE, l1);
				    if(op.isKind(GT))
				    	mv.visitJumpInsn(IF_ICMPLE, l1);
				    if(op.isKind(LE))
				    	mv.visitJumpInsn(IF_ICMPGT, l1);
				    if(op.isKind(GE))
				    	mv.visitJumpInsn(IF_ICMPLT, l1);
				    if(op.isKind(EQUAL))
				    	mv.visitJumpInsn(IF_ICMPNE, l1);
				    if(op.isKind(NOTEQUAL))
				    	mv.visitJumpInsn(IF_ICMPEQ, l1);
					mv.visitInsn(ICONST_1);
					Label l2 = new Label();
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
				}break;
				default:
			}
		}
		else if(e0.getTypename().getJVMTypeDesc().equals("Z") && e1.getTypename().getJVMTypeDesc().equals("Z")){
			switch(op.kind){
			case AND:{
				mv.visitInsn(IAND);
			}break;
			case OR:{
				mv.visitInsn(IOR);
			}break;
			case LT:
			case GT:
			case LE:
			case GE:
			case EQUAL:
			case NOTEQUAL:{
				Label l1 = new Label();
				if(op.isKind(LT))
					mv.visitJumpInsn(IF_ICMPGE, l1);
			    if(op.isKind(GT))
			    	mv.visitJumpInsn(IF_ICMPLE, l1);
			    if(op.isKind(LE))
			    	mv.visitJumpInsn(IF_ICMPGT, l1);
			    if(op.isKind(GE))
			    	mv.visitJumpInsn(IF_ICMPLT, l1);
			    if(op.isKind(EQUAL))
			    	mv.visitJumpInsn(IF_ICMPNE, l1);
			    if(op.isKind(NOTEQUAL))
			    	mv.visitJumpInsn(IF_ICMPEQ, l1);
				mv.visitInsn(ICONST_1);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}break;
			default:
			}
		}

		else if(e0.getTypename().getJVMTypeDesc().equals("Ljava/awt/image/BufferedImage;") && e1.getTypename().getJVMTypeDesc().equals("Ljava/awt/image/BufferedImage;")){
			switch(op.kind){
			case PLUS:{
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
			}break;
			case MINUS:{
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			}break;
			case EQUAL:
			case NOTEQUAL:{
				Label l1 = new Label();
			    if(op.isKind(EQUAL))
			    	mv.visitJumpInsn(IF_ACMPNE, l1);
			    if(op.isKind(NOTEQUAL))
			    	mv.visitJumpInsn(IF_ACMPEQ, l1);
				mv.visitInsn(ICONST_1);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}break;
			default:
			}
		}

		else if(e0.getTypename().getJVMTypeDesc().equals("Ljava/awt/image/BufferedImage;") && e1.getTypename().getJVMTypeDesc().equals("I")){
			switch(op.kind){
			case TIMES:{
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			}break;
			case DIV:{
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
			}break;
			case MOD:{
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			}break;
			default:
			}
		}
		else if(e0.getTypename().getJVMTypeDesc().equals("I") && e1.getTypename().getJVMTypeDesc().equals("Ljava/awt/image/BufferedImage;")){
			if(op.kind == TIMES){
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			}
		}

		return binaryExpression;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		Label ls = new Label();
		mv.visitLabel(ls);

		List<Dec> dl = block.getDecs();
		List<Statement> sl = block.getStatements();
		for(Dec dec: dl){
			dec.visit(this, null);
			dec_list.add(dec);
		}
		for(Statement statement: sl){
			statement.visit(this, null);
			if(statement instanceof BinaryChain){
				mv.visitInsn(POP);
			}
		}
		Label le = new Label();
		mv.visitLabel(le);
		return block;
	}


	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		Boolean var = booleanLitExpression.getValue();
		if(var)
			mv.visitInsn(ICONST_1);
		else
			mv.visitInsn(ICONST_0);
		return booleanLitExpression;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		Token t = constantExpression.firstToken;
		if(t.isKind(KW_SCREENWIDTH)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		}
		else if(t.isKind(KW_SCREENHEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName , "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
		}

		return constantExpression;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		TypeName t = declaration.getTypename();
		declaration.setSlotNum(dec_counter);
		dec_counter++;

		if(t.isType(IMAGE) || t.isType(FRAME)){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlotNum());
		}


		return declaration;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {

		Token t = filterOpChain.getFirstToken();
		if(t.isKind(OP_BLUR)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if(t.isKind(OP_CONVOLVE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if(t.isKind(OP_GRAY)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
		}

		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		//mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
		frameOpChain.getArg().visit(this, arg);
		Token t = frameOpChain.getFirstToken();
		if(t.isKind(KW_SHOW)){

			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
		}
		else if(t.isKind(KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		}
		else if(t.isKind(KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		}
		else if(t.isKind(KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		}
		else if(t.isKind(KW_YLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
		}
		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Boolean left = (Boolean)arg;
		Dec dec = identChain.getDec();
		String str = identChain.getFirstToken().getText();
		TypeName t = dec.getTypename();
		if(left){

			if(dec instanceof ParamDec){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, str, dec.getTypename().getJVMTypeDesc());
				if(dec.getTypename().isType(URL))
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
				else if(dec.getTypename().equals(TypeName.FILE))
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
			}
			else{
				if(t.equals(TypeName.INTEGER) || t.equals(TypeName.BOOLEAN)){
					mv.visitVarInsn(ILOAD,dec.getSlotNum());
				}
				else if(t.equals(TypeName.IMAGE) || t.equals(TypeName.FRAME)){
					mv.visitVarInsn(ALOAD,dec.getSlotNum());
				}
			}
		}
		else{
			if(t.equals(TypeName.INTEGER)){
				if(dec instanceof ParamDec){
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, str, dec.getTypename().getJVMTypeDesc());
				}
				else{
					mv.visitInsn(DUP);
					mv.visitVarInsn(ISTORE,dec.getSlotNum());
				}
			}
			else if(t.equals(TypeName.IMAGE)){
				//mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE,dec.getSlotNum());
			}

			else if(t.equals(TypeName.FRAME)){
				mv.visitVarInsn(ALOAD, dec.getSlotNum());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, dec.getSlotNum());
			}
			else if(t.equals(TypeName.URL)){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, str, dec.getTypename().getJVMTypeDesc());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
			}

			else if(t.equals(TypeName.FILE)){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, str, dec.getTypename().getJVMTypeDesc());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
			}

		}
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec dec = identExpression.getDec();
		TypeName t = dec.getTypename();
		String str = identExpression.getFirstToken().getText();
		if(dec instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, str, dec.getTypename().getJVMTypeDesc());
		}
		else
			if(t.equals(TypeName.INTEGER) || t.equals(TypeName.BOOLEAN)){
				mv.visitVarInsn(ILOAD,dec.getSlotNum());
			}
			else if(t.equals(TypeName.IMAGE) || t.equals(TypeName.FRAME)){
				mv.visitVarInsn(ALOAD,dec.getSlotNum());
			}
		return identExpression;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec dec = identX.getDec();
		String str = identX.getText();
		TypeName t = dec.getTypename();
		if(t.equals(TypeName.IMAGE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
			mv.visitVarInsn(ASTORE,dec.getSlotNum());
		}

		else if(t.equals(TypeName.INTEGER) || t.equals(TypeName.BOOLEAN)){
			if(dec instanceof ParamDec){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, str, dec.getTypename().getJVMTypeDesc());
			}
			else{
				mv.visitVarInsn(ISTORE,dec.getSlotNum());
			}
		}
		else
			mv.visitVarInsn(ASTORE,dec.getSlotNum());


		return identX;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {

		ifStatement.getE().visit(this, arg);
		Label l3 = new Label();
		mv.visitJumpInsn(IFEQ, l3);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(l3);
		return ifStatement;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		Token t = imageOpChain.getFirstToken();
		if(t.isKind(OP_WIDTH)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getWidth", PLPRuntimeImageOps.getWidthSig, false);
		}
		else if(t.isKind(OP_HEIGHT)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getHeight", PLPRuntimeImageOps.getHeightSig , false);
		}
		else if(t.isKind(KW_SCALE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		}
		return imageOpChain;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		int val = intLitExpression.value;
		mv.visitLdcInsn(val);

		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		paramDec.setSlotNum(-1);
		if(paramDec.getTypename().getJVMTypeDesc().equals("I")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "I", null, new Integer(0));
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(param_counter);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}
		else if(paramDec.getTypename().getJVMTypeDesc().equals("Z")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Z", null, new Boolean(false));
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(param_counter);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}

		else if(paramDec.getTypename().getJVMTypeDesc().equals("Ljava/net/URL;")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/net/URL;", null,null );
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(param_counter);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		}
		else if(paramDec.getTypename().getJVMTypeDesc().equals("Ljava/io/File;")){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/io/File;", null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(param_counter);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
		}

		fv.visitEnd();
		param_counter++;

		return paramDec;


	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return sleepStatement;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> exp = tuple.getExprList();
		for(Expression e : exp){
			e.visit(this, arg);
		}
		return tuple;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {

		Label l4 = new Label();
		mv.visitJumpInsn(GOTO, l4);
		Label l3 = new Label();
		mv.visitLabel(l3);
		whileStatement.getB().visit(this, arg);

		mv.visitLabel(l4);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, l3);

		return null;
	}

}
