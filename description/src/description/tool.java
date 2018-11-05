package description;

import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

public class tool extends ASTVisitor {
	static DemoVisitor demovisitor = new DemoVisitor();

	private static String read(String filename) throws IOException {
		File file = new File(filename);
		byte[] b = new byte[(int) file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(b);
		return new String(b);
	}

	private static void description(ASTNode an) {
		if (an == null)
			System.out.println("end");
		else {
			an.accept(demovisitor);
		}
	}

	private static Statement explain(Statement stmt) {
		if (stmt instanceof IfStatement) {
			IfStatement ifstmt = (IfStatement) stmt;
			InfixExpression wex = (InfixExpression) ifstmt.getExpression();
			System.out.println(wex.getLeftOperand());
			System.out.println(wex.getOperator());
			System.out.println(wex.getRightOperand());
			Statement thstmt = ifstmt.getThenStatement();
		} else if (stmt instanceof ExpressionStatement) {
			ExpressionStatement expressStmt = (ExpressionStatement) stmt;
			Expression express = expressStmt.getExpression();
			System.out.println("assignbnbnnnnnnnnnnnnnnn");
			if (express instanceof Assignment) {
				Assignment assign = (Assignment) express;
				System.out.println("LHS:" + assign.getLeftHandSide() + "; ");
				System.out.println("Op:" + assign.getOperator() + "; ");
				System.out.println("RHS:" + assign.getRightHandSide());
			} else if (express instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) express;
				System.out.println("invocation name:" + mi.getName());
				System.out.println("invocation exp:" + mi.getExpression());
				System.out.println("invocation arg:" + mi.arguments());
			}
			System.out.println();
		} else if (stmt instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement var = (VariableDeclarationStatement) stmt;
			System.out.println("Type of variable:" + var.getType());
			System.out.println("Name of variable:" + var.fragments());
			System.out.println();
		} else if (stmt instanceof ReturnStatement) {
			ReturnStatement rtstmt = (ReturnStatement) stmt;
			System.out.println("return:" + rtstmt.getExpression());
			System.out.println();
		}
		return stmt;
	}

	public static void main(String[] args) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS4); // 设置Java语言规范版本
		// parser.setKind(ASTParser.K_STATEMENTS);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		// char[] src = "class A { void method1(int b){int a=0;if(b>0) {a=1;};}
		// }".toCharArray();
		String src = read("C:\\Users\\IscoGary\\git\\repository\\description\\src\\description\\A.java");
		parser.setSource(src.toCharArray());
		// CompilationUnit result = (CompilationUnit) parser.createAST(null);
		// cu.recordModifications();
		// AST ast = cu.getAST();
		CompilationUnit result = (CompilationUnit) parser.createAST(null); // 这个参数是IProgessMonitor,用于GUI的进度显示,我们不需要，填个null.
																			// 返回值是AST的根结点
		// Statement st = (Statement) parser.createAST(null);
		// System.out.println(st); // 把AST直接输出看看啥样

		int offset = result.getPosition(7, 3);
		NodeFinder finder = new NodeFinder(result, offset, 0);
		ASTNode node = finder.getCoveringNode();
		while (node != null && !(node instanceof Statement)) {
			node = node.getParent();
		}
		System.out.println(node);
		
		// show class name  
		List types = result.types();
		TypeDeclaration typeDec = (TypeDeclaration) types.get(0);
		//System.out.println("className:" + typeDec.getName());
		
		// show methods  
		MethodDeclaration methodDec[] = typeDec.getMethods();
		//System.out.println("Method:");
		for (MethodDeclaration method : methodDec) {
			// get method name  
			SimpleName methodName = method.getName();
			//System.out.println("method name:" + methodName);
			// get method parameters  
			List param = method.parameters();
			//System.out.println("method parameters:" + param);

			// get method return type  
			Type returnType = method.getReturnType2();
			//System.out.println("method return type:" + returnType);
			
			
			
			// get method body  
			Block body = method.getBody();
//			List statements = body.statements();// get the statements of the method body  
//			Iterator iter = statements.iterator();
			
//			while (iter.hasNext()) {
//				// get each  statement
//				Statement stmt = (Statement) iter.next();
//				System.out.println("===");
//				// stmt=explain(stmt);
////				if (stmt instanceof IfStatement)
////					dv.visit((IfStatement)stmt);
//				stmt.accept(dv);
//			}
			
			DemoVisitor dv = new DemoVisitor();
			body.accept(dv);
			System.out.println(dv.getDes());
		}
	}
}