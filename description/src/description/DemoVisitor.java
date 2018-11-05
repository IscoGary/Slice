package description;

import java.util.Hashtable;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;

import org.eclipse.jdt.core.dom.FieldDeclaration;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class DemoVisitor extends ASTVisitor {

	@Override

	public boolean visit(FieldDeclaration node) {

		for (Object obj : node.fragments()) {

			VariableDeclarationFragment v = (VariableDeclarationFragment) obj;

			System.out.println("Field:\t" + v.getName());

		}

		return true;

	}

	@Override

	public boolean visit(MethodDeclaration node) {

		System.out.println("Method:\t" + node.getName());

		return true;

	}

	@Override

	public boolean visit(TypeDeclaration node) {

		System.out.println("Class:\t" + node.getName());

		return true;

	}

	public final void accept(ASTVisitor visitor) {
		if (visitor == null) { throw new IllegalArgumentException(); }
		//visitor.preVisit(this);
		// 执行与节点类型无关的 preVisit 方法
		accept0(visitor); // 调用 accept0,执行与节点类型相关的 visit/endVisit 方法
		//visitor.postVisit(this); // 执行与节点类型无关的 postVisit 方法
		}
	
	public void accept0(ASTVisitor visitor){
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
		// 调用 visit( )访问本节点
		// 如果 visit( )返回 true,则访问子节点acceptChild(visitor, getProperty1()); // 访问非序列型属性
		acceptChildren(visitor, rawListProperty); // 访问序列型属性
		acceptChild(visitor, getProperty2());
		}
		visitor.endVisit(this);// 调用 endVisit( )执行一些节点访问后的操作
	}

	Hashtable<String, Integer> symTable = new Hashtable<String, Integer>();
	Stack<Integer> stack = new Stack<Integer>();

	public boolean visit(IfStatement n) {
		n.getExpression().accept(this);
		Statement thenstatement = n.getThenStatement();
		Statement elsestatement = n.getElseStatement();
		int result = stack.pop();
		if (result == 1) {
			thenstatement.accept(this);
		} else {
			if (null != elsestatement)
				elsestatement.accept(this);
		}
		return false;
	}

	public boolean visit(WhileStatement n) {
		n.getExpression().accept(this);
		int result = stack.pop();
		while (result == 1) {
			n.getBody().accept(this);
			n.getExpression().accept(this);
			result = stack.pop();
		}
		return false;
	}

	public void endVisit(Assignment n) {

		Assignment.Operator operator = n.getOperator();
		String varName = n.getLeftHandSide().toString();
		int value = stack.pop();
		stack.pop();
		if (operator == Assignment.Operator.ASSIGN) {
			symTable.put(varName, value);
		} /*
			 * else if (operator == Assignment.Operator.PLUS_ASSIGN) {
			 * 
			 * } else if (operator == Assignment.Operator.MINUS_ASSIGN) {
			 * 
			 * } else if (operator == Assignment.Operator.TIMES_ASSIGN) {
			 * 
			 * } else if (operator == Assignment.Operator.DIVIDE_ASSIGN) {
			 * 
			 * } else if (operator == Assignment.Operator.REMAINDER_ASSIGN) {
			 * 
			 * }
			 */
		System.out.println(varName + "=" + value);

	}

	public void endVisit(InfixExpression n) {

		InfixExpression.Operator operator = n.getOperator();
		int leftValue, rightValue;
		rightValue = stack.pop();
		leftValue = stack.pop();

		if (operator == InfixExpression.Operator.PLUS) {
			stack.push(leftValue + rightValue);
		} else if (operator == InfixExpression.Operator.MINUS) {
			stack.push(leftValue - rightValue);
		} else if (operator == InfixExpression.Operator.TIMES) {
			stack.push(leftValue * rightValue);
		} else if (operator == InfixExpression.Operator.DIVIDE) {
			if (rightValue == 0) {
				System.out.println("divided by zero");
				System.exit(1);
			} else {
				stack.push(leftValue / rightValue);
			}
		} else if (operator == InfixExpression.Operator.REMAINDER) {
			if (rightValue == 0) {
				System.out.println("divided by zero");
				System.exit(1);
			} else {
				stack.push(leftValue % rightValue);
			}
		} else if (operator == InfixExpression.Operator.GREATER) {
			stack.push(leftValue > rightValue ? 1 : 0);

		} else if (operator == InfixExpression.Operator.GREATER_EQUALS) {
			stack.push(leftValue >= rightValue ? 1 : 0);
		} else if (operator == InfixExpression.Operator.LESS) {
			stack.push(leftValue < rightValue ? 1 : 0);

		} else if (operator == InfixExpression.Operator.LESS_EQUALS) {
			stack.push(leftValue <= rightValue ? 1 : 0);
		} else if (operator == InfixExpression.Operator.EQUALS) {
			stack.push(leftValue == rightValue ? 1 : 0);
		} else if (operator == InfixExpression.Operator.NOT_EQUALS) {
			stack.push(leftValue != rightValue ? 1 : 0);
		}
	}

	public void endVisit(SimpleName n) {
		String name = n.getIdentifier();
		if (!symTable.containsKey(name)) {
			symTable.put(name, 0);
		}
		stack.push(symTable.get(name));
	}

	public void endVisit(NumberLiteral n) {
		stack.push(Integer.parseInt(n.getToken()));
	}

}
