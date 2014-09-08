package mediadorxml.engine.flworprocessor.util;

import globalqueryprocessor.subquerygenerator.svp.ExecucaoConsulta;
import globalqueryprocessor.subquerygenerator.svp.ExistsJoinOperation;
import globalqueryprocessor.subquerygenerator.svp.Query;
import globalqueryprocessor.subquerygenerator.svp.SimpleVirtualPartitioning;
import globalqueryprocessor.subquerygenerator.svp.SubQuery;

import java.io.IOException;
import java.util.Hashtable;

import mediadorxml.algebra.basic.TreeNode;
import mediadorxml.engine.flworprocessor.Clause;
import mediadorxml.javaccparser.SimpleNode;

import mediadorxml.catalog.CatalogManager;

public class SimplePathExpr extends Clause {
	
	protected TreeNode _node;
	protected int _varNodeId;
	
	protected boolean _hasDefaultMSpec;
	protected TreeNode.MatchSpecEnum _defaultMSpec;

	protected String xpath="";
	
	public SimplePathExpr(SimpleNode node, boolean debug){
		_hasDefaultMSpec = false;
		this.processSimpleNode(node, debug);
	}
	
	public SimplePathExpr(SimpleNode node, TreeNode.MatchSpecEnum mSpec, boolean debug){
		_hasDefaultMSpec = true;
		_defaultMSpec = mSpec;
		this.processSimpleNode(node, debug);
	}
	
	public TreeNode getTree(){
		if (this._node != null)
			return this._node.getRootNode();
		else 
			return null;
	}
	
	public int getVarNodeId(){
		return this._varNodeId;
	}
	
	protected void processSimpleNode(SimpleNode node, boolean debug){
		if (debug)
			this.debugTrace(node);
		
		String element = node.toString();
		boolean processChild = true;		
	
		if (element == "Slash"){
			TreeNode newNode = new TreeNode();
			newNode.setRelationType(TreeNode.RelationTypeEnum.PARENT_CHILD);
			if (_hasDefaultMSpec)
				newNode.setMatchSpec(this._defaultMSpec);
			
			if (this._node == null)
				this._node = newNode;
			else{
				this._node.addChild(newNode);
				this._node = newNode;
			}
			this._varNodeId = this._node.getNodeId();
		}
		else if (element == "DoubleSlash"){
			TreeNode newNode = new TreeNode();
			newNode.setRelationType(TreeNode.RelationTypeEnum.ANCESTOR_DESCENDENT);
			if (_hasDefaultMSpec)
				newNode.setMatchSpec(this._defaultMSpec);
			
			if (this._node == null)
				this._node = newNode;
			else{
				this._node.addChild(newNode);
				this._node = newNode;
			}
			this._varNodeId = this._node.getNodeId();
			
			Query q;
			try {
				q = Query.getUniqueInstance(true);
				q.setLastReadCardinality(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
			
		}		
		else if (element == "QName"){			
			Query q;		
						
			try {
				q = Query.getUniqueInstance(true);
							
				if (!q.isElementConstructor() && !q.isAddedPredicate() && 
						(q.getqueryExprType()==null || 
						(q.getqueryExprType()!=null && !q.getqueryExprType().contains("collection")))) { // Se nao for o caminho xpath que representa a estrutura do resultado especificada pelo usuário, verifique a cardinalidade.
					CatalogManager cm = CatalogManager.getUniqueInstance();				
					this.xpath = q.getXpath();								
					this.xpath = this.xpath + (this.xpath.equals("")? node.getText():"/"+node.getText());
					q.setXpath(this.xpath);				
					
					if ( !q.isExistsJoin() 
							|| (q.isExistsJoin() && q.getVirtualPartitioningVariable()!=null && q.getVirtualPartitioningVariable().equals(q.getFragmentationVariable()))) {
						if ( q.getLastReadCardinality()==-1 || q.getLastReadCardinality()==0 || q.getLastReadCardinality()==1 ){
														
							if ( q.getLastReadCardinality()==-1 || q.getLastReadCardinality()==1 ) { // especificou o caminho completo
																								
								String cardinality = ExecucaoConsulta.executeQuery(cm.getFormattedQuery(q.getLastReadDocumentExpr(), (q.getLastReadCollectionExpr()!=null && !q.getLastReadCollectionExpr().equals("")?", '"+q.getLastReadCollectionExpr()+"'":""), this.xpath));
											
								// Condição para selecionar o atributo de fragmentação: Obter o primeiro elemento no caminho xpath da consulta, cuja cardinalidade no documento XML é maior que 1.
								if ( Integer.parseInt(cardinality.replace(".0", ""))>1 ) {
									
									if ( !q.isAddedPredicate() ) { // Se ainda não adicionou nenhum predicado no caminho xpath da variável em questão.
										
										SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(true);								
										svp.setCardinalityOfElement(Integer.parseInt(cardinality.replace(".0", "")));								
										
										// cria os sub-intervalos para a variável relacionada ao caminho xpath lido.								
										svp.getSelectionPredicate(-1,q.getFragmentationVariable());								
										
										// Acrescenta o predicado ao caminho xpath
										Hashtable<String,Hashtable<Integer,String>> predicates = svp.getSelectionPredicates();
										Hashtable<Integer,String> pred = predicates.get(q.getFragmentationVariable());								
										this._node.setLabel(node.getText()+pred.get(0));
										q.setAddedPredicate(true); // Evita a adição do mesmo predicado nos sub-elementos restantes
										
										int posForClause = q.getInputQuery().indexOf(q.getFragmentationVariable()); // posicao da variavel do FOR atual
										int posPath = q.getInputQuery().indexOf(this.xpath); // posicao onde começa a definiçao do caminho
										
										// se a posicao do caminho for menor que a posicao onde esta o FOR, indica que ha mais de um FOR para caminhos que estao contidos um no outro.
										if (posPath < posForClause){
											String substring = q.getInputQuery().substring(posForClause,q.getInputQuery().length()); // obtenho apenas a substring a partir da variavel do FOR que estou analisando.
											posPath = posForClause + substring.indexOf(this.xpath); // somo a posicao onde começao o caminho onde estou com os caracteres ate chegar no FOR que estou analisando para corrigir a contagem, considerando a consulta original.
										}
															
										String subpath = "";
										if ( posForClause != -1 &&  posPath != -1 ) {
											subpath = q.getInputQuery().substring(posForClause, posPath) + this.xpath;
										}
			
										System.out.println(subpath);
										System.out.println(q.getFragmentationVariable());
										System.out.println(Integer.parseInt(cardinality.replace(".0", "")));
										svp.addVirtualPredicates(subpath, q.getFragmentationVariable(), Integer.parseInt(cardinality.replace(".0", "")));
											
									}	
								}
								
								q.setLastReadCardinality(Integer.parseInt(cardinality.replace(".0", "")));		
							}
							else if ( q.getLastReadCardinality()==0 ) { // especificou como doc()//nomeElemento
								SubQuery sbq = SubQuery.getUniqueInstance(true);										
								if ( sbq.getSubQueries()!=null && sbq.getSubQueries().size() > 0 ){
									// já fragmentou a consulta. Não faça nada.
									q.setAddedPredicate(true);
								}
								else if (!q.isExistsJoin()){
									if ( !q.isAddedPredicate()) {
										analyzeAncestral(q.getLastReadCollectionExpr(), q.getLastReadDocumentExpr(), q.getFragmentationVariable(), node.getText());										
									}
								}
								else if (q.isExistsJoin()){		
									
									SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(true);									
									svp.setCardinalityOfElement(q.getLastJoinCardinality());
									svp.getSelectionPredicate(-1, q.getVirtualPartitioningVariable());
									svp.addVirtualPredicates(q.getPartitioningPath(), q.getVirtualPartitioningVariable(), q.getLastJoinCardinality());
										
								}
							}
	
						}
					}
				} // fim if
				
				else if (!q.isElementConstructor() && q.isOrderByClause()) {
					String orderBy = q.getOrderBy() + (q.getOrderBy().equals("")? node.getText():"/"+node.getText());
					q.setOrderBy(orderBy); // elementos que serao utilizados para ordenacao do resultado final.					
				}

				else if (!q.isElementConstructor() && q.isWaitingXpathAggregateFunction()) {					
					
					String xpathAggregateFunc = q.getXpathAggregateFunction();
					xpathAggregateFunc = xpathAggregateFunc + (xpathAggregateFunc!=null && !xpathAggregateFunc.equals("")?"/":"") + node.getText();
					q.setXpathAggregateFunction(xpathAggregateFunc);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			
			this._node.setLabel(node.getText());			
		}
		
		if (processChild & (node.jjtGetNumChildren()>0)){
			for (int i=0; i<node.jjtGetNumChildren(); i++){
				this.processSimpleNode((SimpleNode)node.jjtGetChild(i), debug);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void analyzeAncestral(String collectionName, String docName, String varName, String element) throws IOException{
		
		CatalogManager cm = CatalogManager.getUniqueInstance();
		Query q = Query.getUniqueInstance(true);
		
		String completePath = "";
		String completePathTmp = "";
		String cardinality = "0";		
		
		// A fragmentação somente será possível se algum ancestral imediato do elemento especificado tiver cardinalidade 1.
		String xquery = " for $n in doc('$schema_" + (collectionName!=null && !collectionName.equals("")?collectionName:docName) + "')//element"
					  + " where $n/element/@name = \"" + element +"\""
					  + " and sum($n/@total_nodes) = 1"
					  + " return substring($n/@name,1)";
		
		if (docName!=null && !docName.equals("")) { // se especificou o nome de um documento dentro de uma coleção. Ex.: doc('x.xml','y')
			xquery = " for $n in doc('$schema_" + (collectionName!=null && !collectionName.equals("")?collectionName:docName) + "')//element"
				   + " where $n/element/@name = \"" + element +"\""			  
				   + " return substring($n/@name,1)";			
		}		
				
		ExecucaoConsulta exc = new ExecucaoConsulta();
		String parentNode = exc.executeQuery(xquery);
		
		if (docName!=null && !docName.equals("")) { 
			cardinality = ExecucaoConsulta.executeQuery(cm.getFormattedQuery(docName, (collectionName!=null && !collectionName.equals("")?", '"+collectionName+"'":""), parentNode));
			
			if (Integer.parseInt(cardinality) == 1 ) { // elemento pai tem cardinalidade 1, pode executar a fragmentação. Setar a cardinalidade para 0 de forma a entrar no comando while seguinte.
				cardinality = "0"; 
			}
		}		
		
		completePath = element;
		
		if ( parentNode!= null && !parentNode.equals("") && !parentNode.contains("Erro") && !q.isAddedPredicate()) {
			
			while ( parentNode!= null && !parentNode.equals("") && !parentNode.contains("Erro") && Integer.parseInt(cardinality) == 0){				
				
				completePath = parentNode + "/" + completePath;
				completePathTmp = parentNode + "/" + completePathTmp;				
				q.setAncestralPath(completePath);
				
				cardinality = ExecucaoConsulta.executeQuery(cm.getFormattedQuery(docName, (collectionName!=null && !collectionName.equals("")?", '"+collectionName+"'":""), q.getAncestralPath()));
				
				xquery = " for $n in doc('$schema_" + (collectionName!=null && !collectionName.equals("")?collectionName:docName) + "')//element"
					   + " where $n/element/@name = \"" + parentNode +"\""
				 	   + " return substring($n/@name,1)";
				parentNode = exc.executeQuery(xquery);			
			}			
			
			if ( Integer.parseInt(cardinality) > 1 ) {
				
				// verificar se o elemento anterior tem cardinalidade 1.
				int posSlash = -1;
				completePath = q.getAncestralPath();
				
				while (Integer.parseInt(cardinality) > 1 && !completePath.equals("") && !completePath.equals("")) {				
					
					posSlash = completePath.lastIndexOf("/");
					if ( posSlash >= 0) {
						// Ex.:    for $it in doc('xmlDataBaseXmark.xml')/site/regions/australia/item/name
						completePathTmp = completePath.substring(posSlash+1, completePath.length()); // Ex.: name
						completePath = completePath.substring(0, posSlash); // Ex.: /site/regions/australia/item
						xquery = "let $elm := doc('" + docName + "'" 
						+ (collectionName!=null && !collectionName.equals("")?", '" + collectionName + "'":"") 
						+ ")/" + completePath + " return count($elm)";
						cardinality = ExecucaoConsulta.executeQuery(xquery);						
					}
					else {
			
						xquery = "let $elm := doc('" + docName + "'" 
						+ (collectionName!=null && !collectionName.equals("")?", '" + collectionName + "'":"") 
						+ ")/" + (!completePath.equals("")?completePath+"/":completePath) + completePathTmp + " return count($elm)";
						cardinality = ExecucaoConsulta.executeQuery(xquery);
						completePath = ""; 
					}							
					
				}
				
				xquery = "let $elm := doc('" + docName + "'" 
				+ (collectionName!=null && !collectionName.equals("")?", '" + collectionName + "'":"") 
				+ ")/" + (!completePath.equals("")?completePath+"/":completePath) + completePathTmp + " return count($elm)";
				cardinality = ExecucaoConsulta.executeQuery(xquery);				
				
				SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(true);
				
				svp.setCardinalityOfElement(Integer.parseInt(cardinality));
				svp.getSelectionPredicate(-1, varName);				
				svp.addVirtualPredicates("/"+completePathTmp, varName, Integer.parseInt(cardinality));
				q.setAncestralPath(""); // Quando a fragmentação já tiver sido efetuada, não será necessária analisar os elementos subsequentes.
				q.setAddedPredicate(true);
				q.setLastReadCardinality(Integer.parseInt(cardinality));
			}
			else {				
				
				if (!completePathTmp.equals("") && completePathTmp.charAt(completePathTmp.length()-1) == '/') {
					completePathTmp = completePathTmp.substring(0, completePathTmp.length()-1);
					q.setAncestralPath(completePathTmp);
				}				
			}
		}
	}
}
