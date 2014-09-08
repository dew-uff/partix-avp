package mediadorxml.engine.flworprocessor;

import globalqueryprocessor.subquerygenerator.svp.ExecucaoConsulta;
import globalqueryprocessor.subquerygenerator.svp.ExistsJoinOperation;
import globalqueryprocessor.subquerygenerator.svp.Query;
import globalqueryprocessor.subquerygenerator.svp.SimpleVirtualPartitioning;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;

import wrapper.sedna.XQueryResult;

import mediadorxml.algebra.basic.Predicate;
import mediadorxml.algebra.basic.TreeNode;
import mediadorxml.algebra.operators.AbstractOperator;
import mediadorxml.algebra.operators.JoinOperator;
import mediadorxml.algebra.operators.SelectOperator;
import mediadorxml.algebra.operators.SortOperator;
import mediadorxml.algebra.operators.functions.FunctionOperator;
import mediadorxml.catalog.CatalogManager;
import mediadorxml.engine.flworprocessor.util.ComparisonExpr;
import mediadorxml.engine.flworprocessor.util.OrderSpec;
import mediadorxml.engine.flworprocessor.util.Variable;
import mediadorxml.exceptions.AlgebraParserException;
import mediadorxml.exceptions.FragmentReductionException;
import mediadorxml.exceptions.OptimizerException;
import mediadorxml.javaccparser.SimpleNode;

public class FLWOR extends Clause{

	protected ArrayList<Variable> vars;
	protected ArrayList<String> debugStrings;
	protected ArrayList<Long> debugTimes;
	protected ArrayList<AbstractOperator> debugPlans;	
	
	private OrderByClause orderBy;
	protected XQueryResult xqResult;
	
	public FLWOR() {
		super();
		// Inicializa��o dos ArrayLists
		this.vars = new ArrayList<Variable>();		
		this.debugStrings = new ArrayList<String>();
		this.debugTimes = new ArrayList<Long>();	
		this.debugPlans = new ArrayList<AbstractOperator>();
	}
	
	public void compile(SimpleNode node) throws OptimizerException, FragmentReductionException, AlgebraParserException, IOException {
		this.compile(node, false);
	}

	public void compile(SimpleNode node, boolean debug) throws OptimizerException, FragmentReductionException, AlgebraParserException, IOException {
		// Cria��o do plano de execu��o do FLWOR
		this.generateExecutionPlan(node, debug);
		
	}
	
	/*
	 * Montagem do plano de execu��o da XQuery global atrav�s das etapas:
	 * 	1- Plano alg�brico sobre as views globais
	 *  2- Localiza��o das views globais
	 *  3- Redu��o dos fragmentos in�teis
	 *  4- Otimiza��o do plano de execu��o e localiza��o das opera��es
	 */
	protected void generateExecutionPlan(final SimpleNode node, final boolean debug) 
		throws OptimizerException, FragmentReductionException, AlgebraParserException, IOException{
		
		//Montagem do plano sobre as views globais
		this.processSimpleNode(node, debug);
		if (debug){
			addDebugInfo();
		}		
	}

	protected void addDebugInfo() {
		this.debugStrings.add(this.toString());
		this.debugTimes.add(new Long(System.nanoTime()));
		try{
			this.debugPlans.add(this.operator.clone());
		}
		catch (CloneNotSupportedException cloneExc) {
			
		}
	}		
		
	public String getDebugString(final int index){
		String returnStr = "";
		if ((this.debugStrings != null) && (index < this.debugStrings.size())){
			returnStr = (String)this.debugStrings.get(index);
		}
		
		return returnStr;
	}
	
	public long getDebugTime(final int index){
		long returnTime = 0;
		
		if ((this.debugTimes != null) && (index < this.debugTimes.size())){
			returnTime = this.debugTimes.get(index).longValue();
		}
		
		return returnTime;
	}
	
	public AbstractOperator getDebugPlan(final int index){
		if ((this.debugPlans != null) && (index < this.debugPlans.size())){
			return this.debugPlans.get(index);
		}
		else{
			return null;
		}
	}
	
	/*
	 * Execu��o do FLWOR atrav�s das suas sub-queries
	 */
	
	protected void processSimpleNode(SimpleNode node, boolean debug) throws AlgebraParserException, IOException{
		if (debug){
			this.debugTrace(node);
		}
				
		final String element = node.toString();
		boolean processChild = true;
		//------------------------------------------
		//FOR Clause:
		if ("ForClause".equals(element)){
			
			try {
				Query q = Query.getUniqueInstance(true);
				q.setElementConstructor(false); // Indica que n�o se trata da estrutura XML do resultado a ser mostrada para o usu�rio
				q.setOrderByClause(false); // Indica que n�o se trata da clausula orderBy
				q.setXpath(""); // Apagar o caminho do �ltimo FOR/LET, antes de come�ar a armazenar o caminho do FOR/LET seguinte. Cada sub-elemento XML � verificado para obter a cardinalidade deste em todo o documento.			
				q.setLastReadCardinality(-1); // Resetar a cardinalidade, pois se refere a um novo caminho Xpath para um novo FOR.
				//q.setAddedPredicate(false);		
				q.setAncestralPath("");
				
				final ForClause forClause = new ForClause();
				forClause.compileForLet(node, debug);				
				this.insertForLet(forClause);			
				processChild = false;				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
		}
		//------------------------------------------
		//LET Clause:
		else if ("LetClause".equals(element)){
			LetClause letClause = new LetClause();
			try {
				Query q = Query.getUniqueInstance(true);
				q.setElementConstructor(false); // Indica que n�o se trata da estrutura XML do resultado a ser mostrada para o usu�rio
				q.setOrderByClause(false); // Indica que n�o se trata da clausula orderBy
				q.setXpath(""); // Apagar o caminho do �ltimo FOR/LET, antes de come�ar a armazenar o caminho do FOR/LET seguinte.
				//q.setAddedPredicate(false);
				q.setAncestralPath("");
				q.setLastReadCardinality(-1); // Resetar a cardinalidade, pois se refere a um novo caminho Xpath para um novo LET.
				letClause.compileForLet(node, debug);
				this.insertForLet(letClause);					
					
				Hashtable<String, String> letClauses = (Hashtable<String, String>) q.getLetClauses();
				String lastReadLetVariable = q.getLastReadLetVariable();			
				q.setLastReadSimplePathExpr(q.getXpath());			
				
				if (!letClauses.containsKey(lastReadLetVariable)){ // verifica se a vari�vel deste LET j� existe na hashtable
					String lastReadForLetVariable = q.getLastReadForLetVariable(); // a vari�vel a qual o LET referencia, um n�vel acima				
					String letExpression = lastReadForLetVariable+"/"+q.getXpath();
					q.setLetClauses(lastReadLetVariable,letExpression);
				}			
									
				processChild = false;		
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
		}
		//------------------------------------------
		//WHERE Clause:
		else if ("WhereClause".equals(element)){
			WhereClause where = new WhereClause(node, debug);
			for (int i=0; i<where.getComparisons().size(); i++){
				this.insertComparison(where.getComparisons().get(i));

				try {
					Query q = Query.getUniqueInstance(true);
					q.setElementConstructor(true); // Evita consultas por cardinalidade para os predicados de sele��o especificados pelo usu�rio na consulta de entrada.
					q.setOrderByClause(false); // Indica que n�o se trata da clausula orderBy
					q.setXpath(""); // Apagar o caminho do �ltimo FOR, antes de come�ar a armazenar o caminho do FOR seguinte.				
					q.setLastReadCardinality(-1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}							
			}
			
			processChild = false;
		}
        //------------------------------------------
		//ORDER BY Clause:
		else if ("OrderByClause".equals(element)){
			orderBy = new OrderByClause(node, debug);			
			processChild = false;
			Query q = Query.getUniqueInstance(true);
			q.setOrderByClause(true);
			q.setElementConstructor(false); 
		}
		//------------------------------------------
		//RETURN Clause:
		else if ("ElmtConstructor".equals(element)){
			Query q;
			try{				
				q = Query.getUniqueInstance(true);
				q.setElementConstructor(true); // Evita consultas por cardinalidade para os caminhos indicados no resultado da xquery.				
				q.setOrderByClause(false); // Indica que n�o se trata da clausula orderBy
			}
			catch(Exception ex){
				//System.out.println(ex.getMessage() + "\r\n" + ex.getStackTrace());
			}			
			
			ReturnClause ret = new ReturnClause(node, debug);
			AbstractOperator constructReturn = ret.getOperator();		
			
			// Inclus�o do operador SORT abaixo do Construct, caso haja ORDER BY na consulta
			if (orderBy != null){
			
				q = Query.getUniqueInstance(true);
				q.setOrderByClause(true);
				q.setElementConstructor(false);
				
				SortOperator sort = new SortOperator();
				for (int i=0; i<orderBy.getOrderSpecList().size(); i++){
					int lcl = orderBy.getOrderSpecList().get(i).getPathLcl();
					String pred = lcl + " ";				
					
					if (orderBy.getOrderSpecList().get(i).isAscending())
						pred += "ASCENDING";
					else
						pred += "DESCENDING";
					sort.getPredicateList().add(pred);					
				}
				
				constructReturn.addChild(sort);
				constructReturn = sort;
			}
			
			// Processamento dos operadores de Fun��es de Agrega��o
			for (int i=0; i<ret.getFunctionOperatorsList().size(); i++){
				AbstractOperator sel = ret.getFunctionOperatorsList().get(i);
				
				// Substitui��o da vari�vel pelo NodeId (LCL)
				TreeNode rootNode = sel.getApt().getAptNode().getRootNode();
				int nodeId = this.getVarNodeId(rootNode.getLabel());
				if (nodeId == -1){
					throw new AlgebraParserException("Variable " + rootNode.getLabel() + " does not exist");
				}				
				
				rootNode.setLabel(nodeId);
				rootNode.setIsKeyNode(true);
				
				constructReturn.addChild(sel);
				constructReturn = sel;
			}
			
			// Processamento dos Selects para prepara��o do Construct/Sort
			for (int i=0; i<ret.getSelectOperatorsList().size(); i++){
				AbstractOperator sel = ret.getSelectOperatorsList().get(i);
				
				// Substitui��o da vari�vel pelo NodeId (LCL)
				TreeNode n = sel.getApt().getAptNode().getRootNode();
				int nodeId = this.getVarNodeId(n.getLabel());
				if (nodeId == -1){
					throw new AlgebraParserException("Variable " + n.getLabel() + " does not exist");
				}
				n.setLabel(nodeId);
				n.setIsKeyNode(true);
								
				constructReturn.addChild(sel);
				constructReturn = sel;
			}
			
			// Inclus�o dos Selects referentes ao ORDER BY, se houver
			if (orderBy != null){			
				q = Query.getUniqueInstance(true);
				q.setOrderByClause(true);
				q.setElementConstructor(false);
				
				for (int i=0; i<orderBy.getOrderSpecList().size(); i++){
					OrderSpec s = orderBy.getOrderSpecList().get(i);
					TreeNode n = s.getTreeNode().getRootNode();
					int nodeId = this.getVarNodeId(n.getLabel());
					n.setLabel(nodeId);
					n.setIsKeyNode(true);
					
					// 0- Buscar se j� existe operador de Select para a vari�vel do order by
					AbstractOperator op = constructReturn;
					AbstractOperator opEncontrado = null;
					while (op.getParentOperator() != null){
						TreeNode nn = op.getApt().getAptRootNode();
						if ((nn != null) && (nn.getLabelLCLid() == nodeId)){
							opEncontrado = op;
							break;
						}
						op = op.getParentOperator();
					}
									
					// 1- Se j� existir, faremos um merge das �rvores
					if (opEncontrado != null){
						opEncontrado.getApt().mergeTree(n);
					}
					else{
						// 2- Se n�o existir, criamos um novo operador Select para esta vari�vel
						SelectOperator sel = new SelectOperator();
						sel.getApt().setAptNode(n);
						constructReturn.addChild(sel);
						constructReturn = sel;				
					}
				}
			}
			
			
			//Inclus�o dos operadores de sele��o abaixo do �ltimo n�vel dos operadores de constru��o
			constructReturn.addChild(this.operator);
			
			
			this.operator = constructReturn.getRootOperator();
			
			processChild = false;
		}
			
		if (processChild & (node.jjtGetNumChildren()>0)){
			for (int i=0; i<node.jjtGetNumChildren(); i++){
				this.processSimpleNode((SimpleNode)node.jjtGetChild(i), debug);
			}
		}
	}
	
	protected AbstractOperator buildJoin(AbstractOperator op1, AbstractOperator op2) 
		throws AlgebraParserException{
		
		// Tratamento de mais de um FOR na XQuery (Join de Selects)
		JoinOperator join = new JoinOperator();
		join.addChild(op1);
		join.addChild(op2);
		join.generateApt();
		return join;
	}
	
	protected void insertForLet(ForLetClause forlet) throws AlgebraParserException{
		if (this.operator == null)
			this.operator = forlet.getOperator();  // Select Operator
		else{
			// Verifica��o se o nodo ra�z n�o � referente a uma outra vari�vel
			TreeNode n = forlet.getOperator().getApt().getAptRootNode();
			String label = n.getLabel();
			//Se come�ar com "$" faz refer�ncia a outra vari�vel
			if (label.charAt(0) == '$'){
				
				int varId = this.getVarNodeId(label.substring(1, label.length()));
				// Inclus�o da �rvore abaixo do nodo da vari�vel
				TreeNode varNode = this.operator.findNodeInPlanById(varId);				
				
				try {
					Query q = Query.getUniqueInstance(true);
					// Obt�m o nome do documento, o nome da cole��o e o caminho xpath da vari�vel a qual o LET faz refer�ncia.
					String documentName = q.getDocumentNameByVariableName(label);
					String collectionName = q.getCollectionNameByVariableName(label);
					String xpath = q.getXpathByVariableName(label);
					q.setLastReadForLetVariable(label);				
					
					String completePathLet = xpath + (!q.getXpath().equals("")?"/"+q.getXpath():q.getXpath());
					String subXpath = "";
					String LETPath = (!q.getXpath().equals("")?"/"+q.getXpath():q.getXpath()); // caminho da variavel LET desconsiderando o caminho a variavel de referencia
					
					CatalogManager cm = CatalogManager.getUniqueInstance();										
										
					// cardinalityFor: indica a cardinalidade do ultimo elemento do caminhoXpath indica no FOR ao qual este LET referencia.
					String cardinalityFor = ExecucaoConsulta.executeQuery(cm.getFormattedQuery(documentName, (collectionName!=null && !collectionName.equals("")?", '"+collectionName+"'":""), xpath));
					
					/* Se cardinalityFor n�o for maior que 1, o predicado de sele��o deve ser adicionado no LET, caso contr�rio, n�o adicione.
					 * Ex.1:    for $c in doc('loja','informacoesLoja')/Loja/Itens/Item
  							   let $a := $c/ResenhaClientes/Resenha
  						Supondo que /Loja/Itens/Item j� foi fragmentado como /Loja/Itens/Item[position >= vMin and position < vMax]
  						o LET n�o precisa de fragmenta��o, pois o caminho da vari�vel ao qual se refere j� foi fragmentado.
  						
  					   Ex.2:    for $c in doc('loja','informacoesLoja')/Loja/Itens
  							     let $a := $c/Item/ResenhaClientes/Resenha
  						Neste caso, o LET precisa ser fragmentado, pois o atributo de fragmenta��o, cuja cardinalidade � maior que 1 
  						est� especificado nele e n�o no FOR.  						
  							     
					 * */


					if ( Integer.parseInt(cardinalityFor.replace(".0", "")) <= 1 && !q.isAddedPredicate()) { 
						
						while ( q.getLastReadCardinality()<=1 ) {
													
							if (completePathLet.indexOf("/")!=-1) {
								subXpath = subXpath + (!subXpath.equals("")?"/":"") + completePathLet.substring(0,completePathLet.indexOf("/"));
								completePathLet = completePathLet.substring(completePathLet.indexOf("/")+1, completePathLet.length());
							}						
							else {
								subXpath = subXpath + (!subXpath.equals("")?"/":"") + completePathLet;
							}
														
							String cardinality = ExecucaoConsulta.executeQuery(cm.getFormattedQuery(documentName, (collectionName!=null && !collectionName.equals("")?", '"+collectionName+"'":""), subXpath));
													
							if (Integer.parseInt(cardinality.replace(".0", ""))>1){
								
								if (LETPath.indexOf(completePathLet)!=-1) {
									LETPath = LETPath.substring(0, LETPath.indexOf(completePathLet)-1); // obtem o caminho do let ate o ultimo elemento cuja cardinalidade foi verificada.
								}
						
								String OriginalPath = xpath + (!q.getXpath().equals("")?"/"+q.getXpath():q.getXpath());
	
								/* Se o caminho do let est� correto, adicione os predicados.
								 * Ex.:   for $order in doc('loja','informacoesLoja')/Loja/Itens
								 * 		  let $l := $order/Item/ResenhaClientes ...
								 * Adicione, pois OriginalPath � Loja/Itens/Item/ResenhaClientes, considerando o elemento Item como escolhido
								 * para a fragmentacao, o subcaminho Loja/Itens/Item ser� fragmentado. Pois 
								 * No entanto, se tiv�ssemos:
								 * 		  for $order in doc('loja','informacoesLoja')/Loja/Itens
								 * 		  let $l := $order/Item/ResenhaClientes/Cliente/Compra/Item/Preco
								 * O sistema reconhece que o Item para a fragmentacao � Loja/Itens/Item e n�o 
								 * Loja/Itens/Item/ResenhaClientes/Cliente/Compra/Item.  
							     */
								
								if ( OriginalPath.indexOf(xpath + LETPath) != -1 && !q.isAddedPredicate() ) { // Se ainda n�o adicionou nenhum predicado no caminho xpath da vari�vel em quest�o.								
								
									SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(true);								
									svp.setCardinalityOfElement(Integer.parseInt(cardinality.replace(".0", "")));								
									
									// cria os sub-intervalos para a vari�vel relacionada ao caminho xpath lido.								
									svp.getSelectionPredicate(-1,q.getFragmentationVariable());								
																									
									q.setAddedPredicate(true); // Evita a adi��o do mesmo predicado nos sub-elementos restantes com cardinalidade maior que 1.							
									 
									int posLetVariable = q.getInputQuery().indexOf(q.getLastReadLetVariable());
									String pathReplaceTo = q.getInputQuery().substring(posLetVariable,q.getInputQuery().length()); // substring da variavel Let ate o final
									pathReplaceTo = pathReplaceTo.substring(0, pathReplaceTo.indexOf(q.getXpath())-1); // substring da variavel Let ate a posicao do caminho xpath do let
									pathReplaceTo = pathReplaceTo + LETPath;
							
									// Acrescenta o predicado ao caminho xpath
									svp.addVirtualPredicates(pathReplaceTo, q.getFragmentationVariable(), Integer.parseInt(cardinality.replace(".0", "")));
															
								}	
							}	
							
							q.setLastReadCardinality(Integer.parseInt(cardinality.replace(".0", "")));
							
						}	// fim while
					
					} // fim if cardinalityFor
					
					
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				n.setLabel(varNode.getNodeId());
				AbstractOperator op = forlet.getOperator();
				op.addChild(this.operator);
				this.operator = op;
			}
			else{ // Outra view global = join
				this.operator = this.buildJoin(this.operator, forlet.getOperator());
			}
		}
		
		Variable var = forlet.getVariable();
		if (var != null){
			this.vars.add(var);
		}
	}
	
	protected void insertComparison(ComparisonExpr comp) throws IOException{
		
		// Verifica��o se a compara��o era predicado de jun��o
		if (comp.isJoinComparison()){
			// Inserir predicado de jun��o no operador Join
			this.insertJoinPredicate(comp);
		}
		// Se for compara��o utilizando uma fun��o tipo agrega�ao (count, avg)
		else if (comp.isFunctionComparison()){
			this.insertFunctionFilter(comp);
		}
		else{
			this.insertTreeNodeSimplePath(comp.getTreeNode().getRootNode());
		}
	}
	
	protected void insertTreeNodeSimplePath(TreeNode node){
		// Vari�vel da compara��o
		String varName = node.getLabel();
		
		// Busca do NodeId referente � vari�vel
		int nodeId = this.getVarNodeId(varName);
		
		// Busca do nodo onde a compara��o ser� inserida
		TreeNode nodePosition = this.operator.findNodeInPlanById(nodeId);
		
		// Remo��o do Root node com a vari�vel
		node = node.getChild(0);
		node.setParentNode(null);
		
		// Inclus�o do nodo de compara��o no plano
		nodePosition.addChild(node);
	}
	
	protected void insertJoinPredicate(ComparisonExpr comp) throws IOException{
		
		String pred = comp.getJoinPredicate();

		TreeNode n1 = comp.getTreeNode().getRootNode();
		TreeNode n2 = comp.getTreeNodeJoin().getRootNode();
		
		// Busca dos key nodes referentes aos LCLs encontrados
		int lclKN1 = this.getVarNodeId(n1.getLabel());
		int lclKN2 = this.getVarNodeId(n2.getLabel());	
				
		// Busca do operador Join que possui os LCLs dos Key Nodes encontrados
		ArrayList<AbstractOperator> joins = this.operator.getOperatorsListByType("Join");
		for (int i=0; i< joins.size(); i++){
			AbstractOperator join = joins.get(i);
			TreeNode joinNode = join.getApt().getAptRootNode();
			
			int inseridos = 0;
			// Inclus�o dos nodos do predicado de jun��o no operador Join
			if (joinNode.getChild(0).getLabelLCLid() == lclKN1){
				joinNode.getChild(0).addChild(n1.getChild(0));
				inseridos++;
			}
			else if (joinNode.getChild(0).getLabelLCLid() == lclKN2){
				joinNode.getChild(0).addChild(n2.getChild(0));				
				inseridos++;
			}
			if (joinNode.getChild(1).getLabelLCLid() == lclKN1){
				joinNode.getChild(1).addChild(n1.getChild(0));				
				inseridos++;
			}
			else if (joinNode.getChild(1).getLabelLCLid() == lclKN2){
				joinNode.getChild(1).addChild(n2.getChild(0));				
				inseridos++;
			}						
						
			// Inclus�o do predicado de jun��o
			if (inseridos == 2){
				join.getPredicateList().add(pred);
			}
			else if (inseridos == 1){
				// Verifica��o se um dos filhos aponta para outro Join
				TreeNode refNode0 = join.findNodeInChildrenById(joinNode.getChild(0).getLabelLCLid());
				TreeNode refNode1 = join.findNodeInChildrenById(joinNode.getChild(1).getLabelLCLid());
				if ((refNode0.getLabel().equals("Join_root")) || (refNode1.getLabel().equals("Join_root"))){
					// Como um dos filhos � outro Join, o predicado � deste Join
					join.getPredicateList().add(pred);
				}
			}
		}		
		
		Query q = Query.getUniqueInstance(true);
		if ( !q.isJoinCheckingFinished() ){ // Se n�o concluiu a verifica��o de qual jun��o possui menor cardinalidade.
			
			int idVar1 = Integer.parseInt(pred.substring(0, pred.indexOf("=")));
			int idVar2 = Integer.parseInt(pred.substring(pred.indexOf("=")+1, pred.length()));
			
			TreeNode nc1 = n1.findNode(idVar1);
			TreeNode nc2 = n2.findNode(idVar2);		
			
			//ej.verifyJoins(n1.getLabel(),( nc1!=null ? nc1.getLabel(): ""), n2.getLabel(), ( nc2!=null ? nc2.getLabel(): ""));
			String completePath1 = nc1.getLabel();
			String completePath2 = nc2.getLabel();
			String atr1 = nc1.getLabel();
			String atr2 = nc2.getLabel();
			
			while ( nc1.getParentNode() !=null && !nc1.getParentNode().getLabel().contains("(") && !nc1.getParentNode().getLabel().contains("Join_root")) {
				
				nc1 = nc1.getParentNode();
				completePath1 = nc1.getLabel() + "/" + completePath1;
			}
			
			while ( nc2.getParentNode() !=null && !nc2.getParentNode().getLabel().contains("(") && !nc2.getParentNode().getLabel().contains("Join_root")) {
				
				nc2 = nc2.getParentNode();
				completePath2 = nc2.getLabel() + "/" + completePath2;
			}		
			
			completePath1 = "$" + n1.getLabel() + "/" +completePath1;
			completePath2 = "$" + n2.getLabel() + "/" +completePath2;						
						
			ExistsJoinOperation ej= new ExistsJoinOperation(q.getInputQuery());
			ej.verifyJoins(completePath1, completePath2, n1.getLabel(), n2.getLabel(), atr1, atr2);
		}	
	}
	
	protected void insertFunctionFilter(ComparisonExpr comp){
		
		// Cria��o de um operador para a fun��o
		FunctionOperator funcOp = FunctionOperator.buildFunction(comp.getFunctionName());
		
		TreeNode node = comp.getTreeNode().getRootNode();		
		String label = node.getLabel();		
		
		// Atualiza��o do nodo ra�z para trocar o nome da vari�vel pelo seu LCL
		node.setLabel(this.getVarNodeId(node.getLabel()));
		node.setIsKeyNode(true);
		
		// Inclus�o do predicado do TreeNode no predicado do operador
		TreeNode lastNode = node;
		while(lastNode.hasChield()){
			lastNode = lastNode.getChild(0);
			lastNode.setMatchSpec(TreeNode.MatchSpecEnum.ZERO_MORE); 
		}
		Predicate pred = lastNode.getPredicate();
		String lastNodeLCL = lastNode.getLCL(); 
		lastNode.setPredicate(null); // remo��o do predicado do nodo
		
		funcOp.getApt().setAptNode(node);
		funcOp.getPredicateList().add(lastNodeLCL + pred.toString());  // inclus�o do predicado no operador		
		
		// Inclus�o do Operador da fun��o no plano alg�brico global
		funcOp.addChild(this.operator);
		this.operator = funcOp;
		
		try {
			Query q = Query.getUniqueInstance(true);					
			Hashtable<String, String> letClause = q.getLetClauses();
			Hashtable<String, String> forClause = q.getForClauses();
			
			if ( letClause != null && letClause.get("$"+label)!= null  ) {
				//String path = letClause.get("$"+label);
				q.setAggregateFunctions(comp.getFunctionName(), "$"+label, pred.toString());
			}
			else if ( forClause != null && forClause.get("$"+label)!= null  ) {
				//String path = forClause.get("$"+label);
				q.setAggregateFunctions(comp.getFunctionName() + pred.toString(), "$"+label, pred.toString());
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	protected int getVarNodeId(String varName){
		int returnInt = -1;
		if (this.vars != null){
			for(int i=0; i<this.vars.size();i++){
				Variable var = (Variable)this.vars.get(i);
				if (var.getVarName().equals(varName)){
					returnInt = var.getNodeId();
					break;
				}
			}
		}
		return returnInt;
	}

	public XQueryResult getXqResult() {
		return xqResult;
	}

	public void setXqResult(XQueryResult xqResult) {
		this.xqResult = xqResult;
	}
}
