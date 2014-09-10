package mediadorxml.engine.flworprocessor.util;

<<<<<<< HEAD
import globalqueryprocessor.subquerygenerator.svp.Query;
import mediadorxml.algebra.basic.TreeNode;
import mediadorxml.engine.flworprocessor.Clause;
=======
import mediadorxml.algebra.basic.TreeNode;
import mediadorxml.engine.flworprocessor.Clause;
import mediadorxml.fragmentacaoVirtualSimples.Query;
>>>>>>> e5fbc1cea5dfd6fc988da05b836ff82d02601a48
import mediadorxml.javaccparser.SimpleNode;

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
			// Cria��o de node com o nome da vari�vel
			this._node = new TreeNode(node.getText(), TreeNode.RelationTypeEnum.ROOT);
			
			try{				
				Query q = Query.getUniqueInstance(true);						
				q.setOrderByClause(true); 
				q.setElementConstructor(false);
				
				String orderBy = q.getOrderBy() + (q.getOrderBy().equals("")? "$"+node.getText() : "/" + "$"+node.getText());				
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
		
		if (processChild & (node.jjtGetNumChildren()>0)){
			for (int i=0; i<node.jjtGetNumChildren(); i++){
				this.processSimpleNode((SimpleNode)node.jjtGetChild(i), debug);
			}
		}
	}

}
