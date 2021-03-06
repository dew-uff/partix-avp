package uff.dew.svp.engine.flworprocessor.util;

import uff.dew.svp.algebra.basic.TreeNode;
import uff.dew.svp.engine.flworprocessor.Clause;
import uff.dew.svp.fragmentacaoVirtualSimples.Query;
import uff.dew.svp.javaccparser.SimpleNode;

public class OrderSpec extends Clause {

	protected TreeNode _node;
	protected boolean ascending;
	protected int pathLCL;

	public OrderSpec(SimpleNode node){
		this(node, false);
	}

	public OrderSpec(SimpleNode node, boolean debug){

		ascending = true;

		this.processSimpleNode(node, debug);
	}

	public TreeNode getTreeNode(){
		return this._node;
	}

	public boolean isAscending(){
		return this.ascending;
	}

	public int getPathLcl(){
		return this.pathLCL;
	}

	protected void processSimpleNode(SimpleNode node, boolean debug){
		if (debug)
			this.debugTrace(node);

		String element = node.toString();
		boolean processChild = true;

		if (element == "VarName"){
			// Criacao de node com o nome da variavel
			this._node = new TreeNode(node.getText(), TreeNode.RelationTypeEnum.ROOT);

			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true); 
				q.setElementConstructor(false);
				String orderBy = "";
				
				//Luiz Matos - adiciona fun��o de agrega��o utilizada no order by
				if (q.hasOrderByFunction()) {
					orderBy = q.getOrderBy() + (q.getOrderBy().equals("")? q.getOrderByFunction()+"($"+node.getText() + ")" : "/" + q.getOrderByFunction()+"($"+node.getText() + ")");				
				    q.setHasOrderByFunction(false);
				} else {
					orderBy = q.getOrderBy() + (q.getOrderBy().equals("")? "$"+node.getText() : "/" + "$"+node.getText());				
				}
				//System.out.println(orderBy);
				q.setOrderBy(orderBy); // elementos que serao utilizados para ordenacao do resultado final.		

			}
			catch(Exception ex){
				System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}
		}

		else if (element == "SimplePathExpr"){			

			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true);			 
			}
			catch(Exception ex){
				System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}

			SimplePathExpr sp = new SimplePathExpr(node, debug);
			this._node.addChild(sp.getTree());
			this._node = this._node.findNode(sp.getVarNodeId());
			this._node.setMatchSpec(TreeNode.MatchSpecEnum.ZERO_ONE);
			this.pathLCL = sp.getVarNodeId();
			processChild = false;		

		}
		else if (element == "OrderDescending"){
			this.ascending = false;

			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true); 
				q.setOrderByType("descending");			 
			}
			catch(Exception ex){
				System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}
		}
		else if (element == "OrderAscending"){
			this.ascending = true;

			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true); 
				q.setOrderByType("ascending");
				// Incluir os fragmentos virtuais 
			}
			catch(Exception ex){
				System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}
		}
		else if (element == "FuncAverage"){

			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true); 
				q.setHasOrderByFunction(true);
				q.setOrderByFunction("avg");
			}
			catch(Exception ex){
				System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}
		}
		else if (element == "FuncCount"){

			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true); 
				q.setHasOrderByFunction(true);
				q.setOrderByFunction("count");
			}
			catch(Exception ex){
				System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}
		}
		else if (element == "FuncSum"){

			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true); 
				q.setHasOrderByFunction(true);
				q.setOrderByFunction("sum");
			}
			catch(Exception ex){
				System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}
		}
		else if (element == "FuncMin"){

			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true); 
				q.setHasOrderByFunction(true);
				q.setOrderByFunction("min");
			}
			catch(Exception ex){
				System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}
		}
		else if (element == "FuncMax"){

			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true); 
				q.setHasOrderByFunction(true);
				q.setOrderByFunction("max");
			}
			catch(Exception ex){
				System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}
		}


		
		if (processChild & (node.jjtGetNumChildren()>0)){
			for (int i=0; i<node.jjtGetNumChildren(); i++){
				//System.out.println("bug - " + (SimpleNode)node.jjtGetChild(i));
				this.processSimpleNode((SimpleNode)node.jjtGetChild(i), debug);
			}
		}
	}

}
