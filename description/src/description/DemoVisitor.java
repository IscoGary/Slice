package description;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.eclipse.jdt.core.dom.*;

public class DemoVisitor extends ASTVisitor {

	private static String des = "———des:";


	Hashtable<String, Integer> symTable = new Hashtable<String, Integer>();
	// Stack<Integer> stack = new Stack<Integer>();

	public boolean visit(Block body) {
		List statements = body.statements();// get the statements of the method body  
		Iterator iter = statements.iterator();
		while (iter.hasNext()) {
			((ASTNode) iter.next()).accept(this);
		}

		return false;
	}

	public boolean visit(IfStatement n) {
		setDes(getDes() + "if ");
		n.getExpression().accept(this);

		Statement thenstatement = n.getThenStatement();
		Statement elsestatement = n.getElseStatement();
		if (null != thenstatement) {
			thenstatement.accept(this);
		}
		if (null != elsestatement)
			elsestatement.accept(this);

		return false;
	}

	public boolean visit(ReturnStatement n) {
		setDes(getDes() + " return ");
		n.getExpression().accept(this);

		return false;
	}

	public boolean visit(InfixExpression iex) {
		if (iex.getParent() instanceof ReturnStatement)
			setDes(getDes() + "whether ");
		iex.getLeftOperand().accept(this);
		explain(iex);
		iex.getRightOperand().accept(this);

		return false;
	}

	public boolean visit(ConditionalExpression cex) {
//		setDes(getDes() + "whether ");
//		cex.getExpression().accept(this);
//		Expression thenExpression = cex.getThenExpression();
//		Expression elseExpression = cex.getElseExpression();
//		if (null != thenExpression) {
//			thenExpression.accept(this);
//		}
//		if (null != elseExpression)
//			elseExpression.accept(this);
		return false;
	}

//	public boolean visit(WhileStatement n) {
//		n.getExpression().accept(this);
//
//		n.getBody().accept(this);
//		n.getExpression().accept(this);
//
//		return false;
//	}

	public boolean visit(ExpressionStatement n) {
		n.getExpression().accept(this);

		return false;
	}

	public boolean visit(Assignment n) {
		setDes(getDes() + "set ");
		n.getLeftHandSide().accept(this);
		explain(n);
		n.getRightHandSide().accept(this);
		return false;
	}

	public boolean visit(ThrowStatement n) {
		setDes(getDes() + "throw ");
		n.getExpression().accept(this);
		return false;
	}

	public boolean visit(VariableDeclarationStatement n) {
//		System.out.println("Type of variable:" + n.getType());
//		System.out.println("Name of variable:" + n.fragments());
		return false;
	}

	public void endVisit(Assignment n) {

	}

	public void endVisit(InfixExpression n) {

	}

//	public void endVisit(ThrowStatement n) {
//		explain(n);
//	}

	public void endVisit(SimpleName n) {
		explain(n);
	}

	public void endVisit(NumberLiteral n) {
		explain(n);
	}

	public void endVisit(NullLiteral n) {
		explain(n);
	}

//	public void endVisit(NumberLiteral n) {
//		System.out.println("   "+n);
//		stack.push(Integer.parseInt(n.getToken()));
//	}

	public void endVisit(ArrayAccess n) {
		explain(n);
	}

	private static void explain(ASTNode stmt) {
		if (stmt instanceof IfStatement) {
//			IfStatement ifstmt = (IfStatement) stmt;
//			InfixExpression wex = (InfixExpression) ifstmt.getExpression();
//			ExpressionStatement expressStmt = (ExpressionStatement) stmt;
//			Expression express = expressStmt.getExpression();
		} else if (stmt instanceof Assignment) {
			Assignment assign = (Assignment) stmt;
			setDes(getDes() + " " + assign.getOperator() + " ");
		} else if (stmt instanceof MethodInvocation) {
//				MethodInvocation mi = (MethodInvocation) express;
//				System.out.println("invocation name:" + mi.getName());
//				System.out.println("invocation exp:" + mi.getExpression());
//				System.out.println("invocation arg:" + mi.arguments());
		}

		else if (stmt instanceof VariableDeclarationStatement) {
//			VariableDeclarationStatement var = (VariableDeclarationStatement) stmt;
//			System.out.println("Type of variable:" + var.getType());
//			System.out.println("Name of variable:" + var.fragments());
//			System.out.println();
		} else if (stmt instanceof ReturnStatement) {
			// ReturnStatement rtstmt = (ReturnStatement) stmt;
		} else if (stmt instanceof InfixExpression) {
			InfixExpression n = (InfixExpression) stmt;
			InfixExpression.Operator operator = n.getOperator();

			// if(n.getLeftOperand().)
//			if (operator == InfixExpression.Operator.PLUS) {
//				des+="is "+n.getRightOperand()+" ";
//			} else if (operator == InfixExpression.Operator.MINUS) {
//				des+="is "+n.getRightOperand()+" ";
//			} else if (operator == InfixExpression.Operator.TIMES) {
//				des+="is "+n.getRightOperand()+" ";
//			} else if (operator == InfixExpression.Operator.DIVIDE) {
//				if (rightValue == 0) {
//					System.out.println("divided by zero");
//					System.exit(1);
//				} else {
//					des+="is "+n.getRightOperand()+" ";
//				}
//			} else if (operator == InfixExpression.Operator.REMAINDER) {
//				if (rightValue == 0) {
//					System.out.println("divided by zero");
//					System.exit(1);
//				} else {
//					des+="is "+n.getRightOperand()+" ";
//				}
//			} else 
			if (operator == InfixExpression.Operator.GREATER) {
				setDes(getDes() + " is greater than  ");

			} else if (operator == InfixExpression.Operator.GREATER_EQUALS) {
				setDes(getDes() + " is greater than or equals ");
			} else if (operator == InfixExpression.Operator.LESS) {
				setDes(getDes() + " is less than  ");

			} else if (operator == InfixExpression.Operator.LESS_EQUALS) {
				setDes(getDes() + " is less than or equals ");
			} else if (operator == InfixExpression.Operator.EQUALS) {
				setDes(getDes() + " is ");
			} else if (operator == InfixExpression.Operator.NOT_EQUALS) {
				setDes(getDes() + " is not  ");
			} else if (operator == InfixExpression.Operator.CONDITIONAL_AND) {
				setDes(getDes() + " and  ");
			}
		} else if (stmt instanceof ThrowStatement) {
			ThrowStatement thstmt = (ThrowStatement) stmt;
			setDes(getDes() + " throw  ");
		} else if (stmt instanceof SimpleName) {
			if (!(stmt.getParent() instanceof ArrayAccess)) {

				String name = ((SimpleName) stmt).getIdentifier();
				setDes(getDes() + name);
			}
		} else if (stmt instanceof Assignment) {
			Assignment as = (Assignment) stmt;
			Assignment.Operator operator = as.getOperator();
			String varName = as.getLeftHandSide().toString();
			if (operator == Assignment.Operator.ASSIGN) {
				setDes(getDes() + "set " + varName);
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

		} else if (stmt instanceof NumberLiteral) {
			if (!(stmt.getParent() instanceof ArrayAccess)) {
				NumberLiteral nl = (NumberLiteral) stmt;
				setDes(getDes() + Integer.parseInt(nl.getToken()) + " ");
			}
		} else if (stmt instanceof ArrayAccess) {
			ArrayAccess aa = (ArrayAccess) stmt;
			setDes(getDes() + "the element at the index of " + aa.getIndex() + " of the array " + aa.getArray());
		} else if (stmt instanceof NullLiteral) {
			NullLiteral nl = (NullLiteral) stmt;
			setDes(getDes() + nl.toString());
		}

	}

	public static String getDes() {
		return des;
	}

	public static void setDes(String des) {
		DemoVisitor.des = des;
	}
}
