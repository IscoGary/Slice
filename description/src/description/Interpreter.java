package description;

import java.util.Hashtable;
import java.util.Stack;

import org.eclipse.jdt.core.dom.*;
import edu.ustc.cs.compile.platform.interfaces.InterRepresent;
import edu.ustc.cs.compile.platform.interfaces.InterpreterException;
import edu.ustc.cs.compile.platform.interfaces.InterpreterInterface;

public class Interpreter implements InterpreterInterface {
	public static void main(String[] args) throws InterpreterException {		
//        String srcFileName = "test/expr5.txt"; 
//        StaticCheck checker=new StaticCheck();
//        InterRepresent ir =checker.doParse(srcFileName);
//         ir.showIR();
//        Interpreter it = new Interpreter();
//        it.interpret(ir);            	   
    }


	public void interpret(InterRepresent ir) throws InterpreterException {
		
		InterpVisitor visitor = new InterpVisitor();
		try {
			// 调用实际解释执行SimpleMiniJOOL程序的代码

			((Block) ir.getIR()).accept(visitor);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new InterpreterException();
		}
	}
}

/**
 * 
 * 封装实际解释执行SimpleMiniJOOL程序的代码。 InterpVisitor 创建人:xrzhang
 * 时间：2018年5月31日-上午8:46:06
 * 
 * @version 1.0.0
 *
 */
class InterpVisitor extends ASTVisitor {
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