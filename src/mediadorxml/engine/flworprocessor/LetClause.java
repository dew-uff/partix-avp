package mediadorxml.engine.flworprocessor;

import java.io.IOException;
import java.util.Hashtable;

import mediadorxml.algebra.basic.TreeNode;
import mediadorxml.fragmentacaoVirtualSimples.Query;
import mediadorxml.javaccparser.SimpleNode;

public class LetClause extends ForLetClause {

	public LetClause() {
		super();
		this.changeSelectAptMatch();
	}
	
	public void compileForLet(SimpleNode node){
		this.compileForLet(node, false);
		this.changeSelectAptMatch();
	}
	
	public void compileForLet(SimpleNode node, boolean debug){	
		this.processSimpleNode(node, debug);	
		this.changeSelectAptMatch();
	}
	
	private void changeSelectAptMatch(){
		if (this.getOperator().getApt().getAptRootNode() != null){
			this.changeSelectAptMatchSpec(this.getOperator().getApt().getAptRootNode());
		}
	}
	
	/**
	 * Atualização do APT do operador Select criado para o Let para alterar
	 * todas as especificações de cardinalidade para zero ou mais (*), o que
	 * caracteriza a diferença do operador de Select de um FOR para um LET 
	 * @param node
	 */
	private void changeSelectAptMatchSpec(final TreeNode node){
		node.setMatchSpec(TreeNode.MatchSpecEnum.ZERO_MORE);
		for (int i=0; i<node.getChildren().size(); i++){
			this.changeSelectAptMatchSpec(node.getChild(i));
		}
	}
}
