package description;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 *  * jdt解析java文件，目前无法获得字段的行号  * *  
 */
public class JdtTest {
	public static void main(String[] args) throws Exception {
		String content = read(
				"D:\\work\\Java\\workspace\\paixu\\src\\paixu\\w.java"); // java源文件
//创建解析器  
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		
		parser.setKind(ASTParser.K_STATEMENTS);     
		parser.setResolveBindings(true);
//设定解析器的源代码字符  
		parser.setSource(content.toCharArray());
//使用解析器进行解析并返回AST上下文结果(CompilationUnit为根节点)  
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
//获取类型  
		List types = result.types();
//取得类型声明  
		TypeDeclaration typeDec = (TypeDeclaration) types.get(0);

//##############获取源代码结构信息#################  
//引用import  
//		List importList = result.imports();// 取得包名  
//		PackageDeclaration packetDec = result.getPackage();
//取得类名  
		String className = typeDec.getName().toString();// 取得函数(Method)声明列表  
		MethodDeclaration methodDec[] = typeDec.getMethods();// 取得函数(Field)声明列表  
		FieldDeclaration[] fields = typeDec.getFields();
		FieldDeclaration[] fieldDec = typeDec.getFields();
//输出包名  
//		System.out.println("包:");
//		System.out.println(packetDec.getName());
//		System.out.println(packetDec.getStartPosition());
//输出引用import  
//		System.out.println("引用import:");
//		for (Object obj : importList) {
//			ImportDeclaration importDec = (ImportDeclaration) obj;
//			System.out.println(importDec.getName());
//		}
//输出类名  
		System.out.println("类:");
		System.out.println(className);
//循环输出函数名称  
		System.out.println("========================");
		System.out.println("函数:");
		for (MethodDeclaration method : methodDec) {

			System.out.println("=============");
			System.out.println(method);
		}
//循环输出变量  
		System.out.println("变量:");
		for (FieldDeclaration fieldDecEle : fieldDec) {
//public static  
			for (Object modifiObj : fieldDecEle.modifiers()) {
				IExtendedModifier modify = (IExtendedModifier) modifiObj;
				System.out.println("注解" + modify);
			}
			System.out.println("类型：" + fieldDecEle.getType());
			for (Object obj : fieldDecEle.fragments()) {
				VariableDeclarationFragment frag = (VariableDeclarationFragment) obj;
				System.out.println("[FIELD_NAME:]" + frag.getName() + "");
			}
		}
	}

	private static String read(String filename) throws IOException {
		File file = new File(filename);
		byte[] b = new byte[(int) file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(b);
		return new String(b);
	}
}
