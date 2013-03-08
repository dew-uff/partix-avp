package mediadorxml.engine.flworprocessor;

import mediadorxml.algebra.operators.AbstractOperator;
import mediadorxml.javaccparser.SimpleNode;

public class Clause {

	protected AbstractOperator operator;
	
	// Construtor protected para impedir a cria��o de objetos de Clause
	protected Clause(){		
	}
		
	public AbstractOperator getOperator(){
		return this.operator;
	}
	
	public void setOperator(final AbstractOperator operator){
		this.operator = operator;
	}
	
	public String toString(){
		return this.operator.toString();
	}
	
	protected void debugTrace(SimpleNode node){
		StringBuffer strBuffer = new StringBuffer();
		//		 inclus�o do n�vel na hierarquia
		if (node.jjtGetParent() != null){
			SimpleNode n = (SimpleNode)node.jjtGetParent();
			while (n != null){
				strBuffer.append('-');
				n = (SimpleNode)n.jjtGetParent();
			}
		}
		strBuffer.append(node.toString());
		strBuffer.append(" - ");
		strBuffer.append(node.getText());
		strBuffer.append("\r\n");
		
		System.out.println(strBuffer.toString());
	}
}
