package mediadorxml.subquery;

import java.io.IOException;
import java.util.ArrayList;

import mediadorxml.algebra.basic.TreeNode;
import mediadorxml.algebra.operators.AbstractOperator;
import mediadorxml.algebra.operators.ConstructOperator;
import mediadorxml.algebra.operators.JoinOperator;
import mediadorxml.catalog.CatalogManager;
import mediadorxml.catalog.util.LocalView;
import mediadorxml.exceptions.OptimizerException;
import mediadorxml.optimizer.Optimizer;
import mediadorxml.subquery.util.ForClause;
import mediadorxml.subquery.util.ForLetClause;
import mediadorxml.subquery.util.LetClause;
import mediadorxml.subquery.util.OrderByClause;
import mediadorxml.subquery.util.ReturnClause;
import mediadorxml.subquery.util.WhereClause;

public class SubQuery {

	protected int queryId;
	protected String executionSite;
	protected AbstractOperator operator;
	
	protected boolean isInnerQuery = false;
	protected String innerQueryForLet;
	protected ArrayList<String> externalVariableList;
	protected ArrayList<SubQuery> innerQueryList;
	protected ArrayList<ForClause> forList;
	protected ArrayList<LetClause> letList;
	protected WhereClause where;
	protected OrderByClause orderBy;
	protected ReturnClause returncl;	
	
	public SubQuery(){
		this.externalVariableList = new ArrayList<String>();
		this.innerQueryList = new ArrayList<SubQuery>();
		this.forList = new ArrayList<ForClause>();
		this.letList = new ArrayList<LetClause>();
		this.where = new WhereClause();
		this.orderBy = new OrderByClause();
	}
	
	public SubQuery(ConstructOperator operator) throws IOException, OptimizerException{
		this();
		
		this.operator = operator;
		
		// A Query deverá ser executada no Site de execução do Construct
		this.executionSite = operator.getExecutionSite().getUri();
		
		// Clone do TreeNode da APT do Construct
		TreeNode nodeClone = operator.getApt().getAptRootNode().clone();
		this.queryId = nodeClone.getNodeId();
		this.returncl = new ReturnClause(nodeClone);
		
		// Busca dos LCLs no TreeNode do RETURN para construção da query
		this.processLCLs(this.returncl.getNode());
		
		// Tratamento do operador SORT
		AbstractOperator op = operator.getChildAt(0);
		if (op.getName().equals("Sort")){
			for(int p=0; p<op.getPredicateList().size(); p++){
				String pred = op.getPredicateList().get(p);
				int lcl = Integer.parseInt(pred.substring(0, pred.indexOf(" ")));
				TreeNode refNode = this.operator.findNodeInPlanById(lcl);
				String orderTerm = this.buildPath(refNode);
				if (pred.indexOf(" DESCENDING") > 0){
					orderTerm += " descending";
				}
				this.orderBy.addOrderTerm(orderTerm);
			}
		}
		
		// Tratamento de Joins para inserir os predicados de junção
		ArrayList<AbstractOperator> joins = this.operator.getOperatorsListByType("Join");
		for (int j=0; j<joins.size(); j++){
			JoinOperator join = (JoinOperator)joins.get(j);
			if (join.getExecutionSite().getUri().equals(this.executionSite)){
				// Verificação se o Join será uma innerQuery (se for uma variável de outros operadores)
				// - neste caso, não devemos incluir o predicado de junção nesta subquery. Ele será incluído
				// na própria innerQuery
				boolean include = true;
				String lclJoin = join.getApt().getAptRootNode().getLCL();
				AbstractOperator opp = join;
				while(opp.getParentOperator() != null){
					opp = opp.getParentOperator();
					if (opp.getApt().getAptRootNode() != null){
						TreeNode nodeLclJoin = opp.getApt().getAptRootNode().findNode(lclJoin);
						if (!(opp.getName().equals("Join") && (!nodeLclJoin.hasChield()))){
							if (nodeLclJoin != null){
								include = false;
								break;
							}
						}
					}
				}
				
				if (include)
					this.includeJoinPredicate(join);
			}
		}
		
		// Tratamento de funções com predicado de seleção
		ArrayList<AbstractOperator> funcs_count = this.operator.getOperatorsListByType("Aggregate_Count");
		ArrayList<AbstractOperator> funcs_max = this.operator.getOperatorsListByType("Aggregate_Max");
		ArrayList<AbstractOperator> funcs_min = this.operator.getOperatorsListByType("Aggregate_Min");
		ArrayList<AbstractOperator> funcs_avg = this.operator.getOperatorsListByType("Aggregate_Average");
		ArrayList<AbstractOperator> funcs_sum = this.operator.getOperatorsListByType("Aggregate_Sum");
		ArrayList<AbstractOperator> funcs = new ArrayList<AbstractOperator>();
		funcs.addAll(funcs_count);
		funcs.addAll(funcs_max);
		funcs.addAll(funcs_min);
		funcs.addAll(funcs_avg);
		funcs.addAll(funcs_sum);
		for (int i=0; i<funcs.size(); i++){
			AbstractOperator funcOp = funcs.get(i);
			if ((funcOp.getExecutionSite().getUri().equals(this.executionSite))
					&& (funcOp.getPredicateList().size() > 0)){
				
				String pred = funcOp.getPredicateList().get(0);
				int lclId = Integer.parseInt(pred.substring(1, pred.indexOf(")")));
				pred = pred.substring(pred.indexOf(")")+1, pred.length());
				
				TreeNode n = funcOp.findNodeInAptById(lclId);
				
				String whereAnd = this.buildPath(n) + pred;
				this.where.addAnd(whereAnd);
			}
		}
	}
	
	public SubQuery(JoinOperator operator, String forLet) throws OptimizerException, IOException{
		this();
		
		this.innerQueryForLet = forLet;
		
		this.isInnerQuery = true;
		
		this.operator = operator;
		
		// A Query deverá ser executada no Site de execução do Construct
		this.executionSite = operator.getExecutionSite().getUri();
		
		// Processamento dos LCLs filhos do join
		TreeNode joinAptNode = operator.getApt().getAptRootNode();
		this.queryId = joinAptNode.getNodeId();
		int var0 = joinAptNode.getChild(0).getLabelLCLid();
		int var1 = joinAptNode.getChild(1).getLabelLCLid();
		TreeNode ref0 = operator.findNodeInChildrenById(var0);
		TreeNode ref1 = operator.findNodeInChildrenById(var1);
		this.processVariable(var0);
		this.processVariable(var1);
		
		this.includeJoinPredicate(operator);
		
		// RETURN
		TreeNode nodeReturn = null;
		AbstractOperator op = operator;
		String searchLCL = joinAptNode.getLCL();
		nodeReturn = searchNodesToReturn(searchLCL, op);
		
		if (nodeReturn != null){
			// Processamento dos nodos do Return		
			// 1. Busca de uma lista com os nomes dos fragmentos abaixo deste join			
			ArrayList<String> fragmentNamesVar0 = searchFragmentNames(ref0.getRootNode().getRefAPT().getRefOperator());
			ArrayList<String> fragmentNamesVar1 = searchFragmentNames(ref1.getRootNode().getRefAPT().getRefOperator());		
					
			// 2. Atualização dos leaf-nodes com as variáveis
			this.updateReturnNodeVariables(nodeReturn, var0, fragmentNamesVar0, var1, fragmentNamesVar1, operator);
		}
		else{
			// Se nodeReturn for nulo, precisaremos inicializálo para retornar todos os nodos filhos das variáveis
			nodeReturn = new TreeNode("root");
			TreeNode c0 = new TreeNode("$v"+var0+"/*");
			TreeNode c1 = new TreeNode("$v"+var1+"/*");
			nodeReturn.addChild(c0);
			nodeReturn.addChild(c1);
		}
		
		// 3. Atualização do Root Node que será consultado na query principal
		String opname = ref0.getRootNode().getRefAPT().getRefOperator().getName();
		if (opname.equals("Select") | opname.equals("Construct")){
			nodeReturn.getRootNode().setLabel(ref0.getKeyNode().getLabel());
		}
		else{
			nodeReturn.getRootNode().setLabel(ref1.getKeyNode().getLabel());
		}				
		
		this.returncl = new ReturnClause(nodeReturn);
	}
	
	private static TreeNode searchNodesToReturn(String searchLCL, AbstractOperator op){
		TreeNode nodeReturn = null;
		while(op.getParentOperator() != null){
			op = op.getParentOperator();
			TreeNode n = op.getApt().getAptRootNode().findNode(searchLCL);
			if (n!=null){
				if (n.hasChield()){
					if (nodeReturn == null){
						nodeReturn = n.clone();
						nodeReturn.setParentNode(null);
					}
					else{
						nodeReturn.addChild(n.clone().getChildren());
					}
				}
				if (!n.isKeyNode()){
					TreeNode nk = searchNodesToReturn(n.getKeyNode().getLCL(), op);
					if (nk.hasChield()){
						if (nodeReturn == null){
							nodeReturn = nk;
							nodeReturn.setParentNode(null);
						}
						else
							nodeReturn.addChild(nk.getChildren());
					}
				}
			}
		}
		
		return nodeReturn;
	}
	
	private static ArrayList<String> searchFragmentNames(AbstractOperator op){

		ArrayList<String> fragmList = new ArrayList<String>();

		String opName = op.getName();
		if (opName.equals("Select")){
			fragmList.add(op.getApt().getAptRootNode().getLabel());
		}
		else if (opName.equals("Construct")){
			fragmList.addAll(searchFragmentNames(op.getChildAt(0).getChildAt(0)));
		}
		else if (opName.equals("Join") | opName.equals("Union")){
			fragmList.addAll(searchFragmentNames(op.getChildAt(0)));
			fragmList.addAll(searchFragmentNames(op.getChildAt(1)));
		}
		
		return fragmList;
	}
	
	private boolean updateReturnNodeVariables(TreeNode nodeReturn, int var0, ArrayList<String> fragListVar0, int var1, ArrayList<String> fragListVar1, AbstractOperator plan)
		throws OptimizerException, IOException{
	
		if (nodeReturn.hasChield()){
			for (int i=0; i<nodeReturn.getChildren().size(); i++)
				if (!this.updateReturnNodeVariables(nodeReturn.getChild(i), var0, fragListVar0, var1, fragListVar1, plan)){
					nodeReturn.getChildren().remove(i);
					i--;
				}
		}
		else{
			boolean found = false;
			// Tentamos com a primeira lista de fragmentos
			for (int i=0; i<fragListVar0.size(); i++){
				if (Optimizer.isTreeNodeInVerticalFragmentDefinition(nodeReturn, plan, fragListVar0.get(i))){
					
					String path = "";
					
					TreeNode p = nodeReturn;
					while (!p.isKeyNode()){
						path = p.getLabel() + path;
						if (p.getRelationType().equals(TreeNode.RelationTypeEnum.PARENT_CHILD)){
							path = "/" + path;
						}
						else if (p.getRelationType().equals(TreeNode.RelationTypeEnum.ANCESTOR_DESCENDENT)){
							path = "//" + path;
						}
						p = p.getParentNode();
					}
					
					nodeReturn.setLabel("$v" + var0 + path);
					
					found = true;
					break;
				}
			}
			if (!found){
				// Tentamos com a segunda lista de fragmentos
				for (int i=0; i<fragListVar1.size(); i++){
					if (Optimizer.isTreeNodeInVerticalFragmentDefinition(nodeReturn, plan, fragListVar1.get(i))){
						
						String path = "";
						
						TreeNode p = nodeReturn;
						while (!p.isKeyNode()){
							path = p.getLabel() + path;
							if (p.getRelationType().equals(TreeNode.RelationTypeEnum.PARENT_CHILD)){
								path = "/" + path;
							}
							else if (p.getRelationType().equals(TreeNode.RelationTypeEnum.ANCESTOR_DESCENDENT)){
								path = "//" + path;
							}
							p = p.getParentNode();
						}
						
						nodeReturn.setLabel("$v" + var1 + path);
						found = true;
						break;
					}
				}
			}
			return found;
		}		

		return true;
	}
	
	/*
	 * Processamento dos LCLs do TreeNode do RETURN (Construct) para montagem
	 * das outras cláusulas da query (FOR, WHERE)
	 */
	protected void processLCLs(TreeNode node) throws IOException, OptimizerException{
		
		if (node.isLabelLCL()){
			// Se é LCL, temos que buscar a sua referência no plano de execução e montar o novo label
			int refId = node.getLabelLCLid();
						
			TreeNode refNode = this.operator.findNodeInPlanById(refId);
			
			node.setLabel(this.buildPath(refNode));
			
		}
		else if (node.hasChield()){
			for (int i=0; i<node.getChildren().size(); i++){
				this.processLCLs(node.getChild(i));
			}
		}
	}
	
	protected String buildPath(TreeNode refNode) throws IOException, OptimizerException{
		
		String newLabel = "";
		
		boolean upToKeyNode = !refNode.isKeyNode();
		
		while (refNode.getParentNode() != null){
			String relation = "";
			if (refNode.getRelationType().equals(TreeNode.RelationTypeEnum.PARENT_CHILD))
				relation = "/";
			else if (refNode.getRelationType().equals(TreeNode.RelationTypeEnum.ANCESTOR_DESCENDENT))
				relation = "//";
			
			newLabel = relation + refNode.getLabel() + newLabel;
			
			// Inclusão do predicado do TreeNode (where)
			if (refNode.getPredicate() != null){
				newLabel += refNode.getPredicate().toString();
			}
			
			refNode = refNode.getParentNode();
			
			if ((upToKeyNode) && (refNode.isKeyNode()))
				break;
			
			if (refNode.isLabelLCL())
				break;
		}
		
		//Construção da variável se o nodo for LCL
		if (refNode.isLabelLCL()){
			String var = "$v" + refNode.getLabelLCLid();
			newLabel = var + newLabel;
			
			// Verificação de função de agregação
			String func = this.getFunctionName(refNode);
			if (func != null){
				newLabel = func + "(" + newLabel + ")";
			}
			
			//Processamento da variável
			this.processVariable(refNode.getLabelLCLid());
		}
		else if (refNode.getParentNode() == null){
			String viewName = refNode.getLabel();
			if (isViewVerticalFragmentRootChild(viewName)){
				
				String rootToRemove = getViewProjectionRoot(viewName);
				
				if (isThisQueryJoinedAfter(refNode) || (!newLabel.startsWith(rootToRemove))){
					newLabel = composeViewFromRoot(viewName) + newLabel;
				}
				else{					
					newLabel = "VIEW('" + viewName + "')" + newLabel.substring(rootToRemove.length());
				}
			}
			else{
				newLabel = "VIEW('" + viewName + "')" + newLabel;
			}
		}
		else {
			// 
		}
		
		return newLabel;
	}
	
	/**
	 * Verificação se a view é um fragmento vertical que possui raíz diferente
	 * da raíz da view global (ou seja, se é um nodo podado)
	 * @param viewName
	 * @return
	 */
	protected boolean isViewVerticalFragmentRootChild(String viewName){
		
		try{
			CatalogManager cm = CatalogManager.getUniqueInstance();
			LocalView lv = cm.getLocalView(viewName);
			
			if ((lv.getProjectionPredicates() != null) && (lv.getProjectionPredicates().size() > 0)){
				String projection = lv.getProjectionPredicates().get(0);
				
				// Se for = "/" é a raíz da visão global
				if (projection.indexOf("/") != projection.lastIndexOf("/")){
					return true;
				}
			}
		}
		catch (Exception ex){
			//TODO log
		}
		return false;
	}
	
	/**
	 * Composição de uma inner query para inclusão dos nodos pais 
	 * para que os fragmentos verticais tenham sempre a mesma raíz,
	 * que deverá ser a raíz da visão global
	 * @param viewName
	 * @return
	 */
	protected String composeViewFromRoot(String viewName){
		String strView = "(let $a := VIEW('" + viewName + "')\r\n return <root> ";
		try{
			CatalogManager cm = CatalogManager.getUniqueInstance();
			LocalView lv = cm.getLocalView(viewName);
			
			if ((lv.getProjectionPredicates() != null) && (lv.getProjectionPredicates().size() > 0)){
				String projection = lv.getProjectionPredicates().get(0);
				
				String[] listaNodos = projection.split("/");
				
				// Removeremos do loop o último elemento pois ele já é a raíz do fragmento				
				for (int i=1; i<listaNodos.length-1; i++){
					strView += "<" + listaNodos[i] + ">";
				}
				strView += " {$a} ";
				for (int i=listaNodos.length-2; i>=1; i--){
					strView += "</" + listaNodos[i] + ">";
				}
			}
		}
		catch (Exception ex){
			//TODO log
		}
		
		strView += " </root>)";
		return strView;
	}
	
	/**
	 * Verifica se esta sub-query será utilizada em algum join acima no plano para saber se precisamos
	 * normalizar as raízes ou não (não precisar = melhor desempenho)
	 * @param refNode
	 * @return
	 */
	protected boolean isThisQueryJoinedAfter(TreeNode refNode){
		AbstractOperator op = this.operator;
		while (op.getParentOperator() != null){
			op = op.getParentOperator();
			if (op.getName().equals("Join")){
				return true;
			}
		}
		return false;
	}
	
	protected String getViewProjectionRoot(String viewName){

		String root = "";
		
		try{
			CatalogManager cm = CatalogManager.getUniqueInstance();
			LocalView lv = cm.getLocalView(viewName);
			
			if ((lv.getProjectionPredicates() != null) && (lv.getProjectionPredicates().size() > 0)){
				String projection = lv.getProjectionPredicates().get(0);
				
				root = projection.substring(0, projection.lastIndexOf("/"));
			}
		}
		catch (Exception ex){
			//TODO log
		}
		
		return root;
	}
	
	protected String getFunctionName(TreeNode n){
		AbstractOperator op = n.getRootNode().getRefAPT().getRefOperator();
		if (op.getName().equals("Aggregate_Count")){
			return "count";
		}
		else if (op.getName().equals("Aggregate_Max")){
			return "max";
		}
		else if (op.getName().equals("Aggregate_Min")){
			return "min";
		}
		else if (op.getName().equals("Aggregate_Sum")){
			return "sum";
		}
		else if (op.getName().equals("Aggregate_Average")){
			return "avg";
		}
		else{
			return null;
		}
	}
	
	/*
	 * Processamento de uma variável da Query = construção do FOR/WHERE
	 */
	protected void processVariable(int LCLid) throws IOException, OptimizerException{
		
		String varId = "$v" + LCLid;
		boolean continuar = true; 
		
		// Verificação da existência de um FOR com a variável indicada
		for (int i=0; i<this.forList.size(); i++){
			ForClause f = this.forList.get(i);
			if (f.getVariable().equals(varId)){
				continuar = false;
				break;
			}
		}
		// Verificação da existência de um LET com a variável indicada
		for (int i=0; i<this.letList.size(); i++){
			LetClause f = this.letList.get(i);
			if (f.getVariable().equals(varId)){
				continuar = false;
				break;
			}
		}
		// Verificação da existência de uma innerQuery com a variável indicada
		for (int i=0; i<this.innerQueryList.size(); i++){
			if (this.innerQueryList.get(i).queryId == LCLid){
				continuar = false;
				break;
			}
		}
		
		if (continuar){
			
			TreeNode node = this.operator.findNodeInPlanById(LCLid);
			
			AbstractOperator opRef = node.getRootNode().getRefAPT().getRefOperator();
			
			// UNION
			if (opRef.getName().equals("Union")){
				// Inclusão de sources do Union no FOR ou LET
				ForLetClause forLetClause;
				// A definição se é LET ou FOR é feita de acordo com a especificação de um dos nodos filhos do
				// nodo raíz do Union
				if (node.getChild(0).getMatchSpec().equals(TreeNode.MatchSpecEnum.ZERO_MORE)){ // LET
					forLetClause = new LetClause(varId);
					this.processUnionSources(forLetClause, node);
					this.letList.add((LetClause)forLetClause);
				}
				else{  // FOR
					forLetClause = new ForClause(varId);
					this.processUnionSources(forLetClause, node);
					this.forList.add((ForClause)forLetClause);
				}
			}
			// JOIN (join de fragmentação vertical)
			else if (opRef.getName().equals("Join")){
				if (node.getChild(0).getMatchSpec().equals(TreeNode.MatchSpecEnum.ZERO_MORE)){ // LET
					this.innerQueryList.add(new SubQuery((JoinOperator)opRef, "let"));
				}
				else{ // FOR
					this.innerQueryList.add(new SubQuery((JoinOperator)opRef, "for"));
				}
			}
			// Construct
			else if (opRef.getName().equals("Construct")){
				// tratamento do Construct
				ForClause forClause = new ForClause(varId);
				this.processConstructSources(forClause, node);
				this.forList.add(forClause);
			}
			else{
				if (node.getMatchSpec().equals(TreeNode.MatchSpecEnum.ZERO_MORE)){ // LET
					LetClause letClause = new LetClause(varId);
					letClause.addSource(this.buildPath(node));
					this.letList.add(letClause);
				}
				else{  // FOR
					ForClause forClause = new ForClause(varId);
					forClause.addSource(this.buildPath(node));
					this.forList.add(forClause);
				}
				
				// Tratamento dos predicados Where
				this.searchAndClauses(varId, node);
			}
		}
	}
	
	/*
	 * Inclusão de sources no FOR provenientes de um operador UNION
	 */
	protected void processUnionSources(ForLetClause f, TreeNode unionRootNode) throws IOException, OptimizerException{
		
		for (int i=0; i<unionRootNode.getChildren().size(); i++){
			
			// Busca do nodo referente ao LCL da união
			int refId = unionRootNode.getChild(i).getLabelLCLid();
			TreeNode refNode = this.operator.findNodeInPlanById(refId);
			
			// Busca operador do TreeNode referente à união
			AbstractOperator op = refNode.getRootNode().getRefAPT().getRefOperator();
		
			// Se for uma outra união
			if (op.getName().equals("Union")){
				this.processUnionSources(f, refNode);
			}
			// JOIN (join de fragmentação vertical)
			else if (op.getName().equals("Join")){
				SubQuery innerQuery = null;
				//if (refNode.getChild(0).get_mSpec().equals(TreeNode.matchSpec.ZERO_MORE)){ // LET
					innerQuery = new SubQuery((JoinOperator)op, "let");
//				}
//				else{ // FOR
//					innerQuery = new SubQuery((JoinOperator)op, "for");
//				}
				this.innerQueryList.add(innerQuery);
				//f.addSource("$v" + innerQuery._id + "/" + innerQuery._return.getNode().getRootNode().getLabel());
				f.addSource("$v" + innerQuery.queryId);
			}
			// Se não for uma outra união, será uma fonte de dados Construct (query remota) ou Select (local)
			else{
				
				// Se o operador for um Construct (executado em outro Site)
				if (op.getName().equals("Construct")){
					this.processConstructSources(f, refNode);
				}
				else{
					// Se o operador for um Select (executado no mesmo Site do Union)
					f.addSource(this.buildPath(refNode));
					
					// Inclusão dos ANDs do Where do Select
					this.searchAndClauses(f.getVariable(), refNode);
				}
			}
		}
	}
	
	protected void processConstructSources(ForLetClause f, TreeNode constructNode) throws IOException{
 
		//Criação de uma variável externa
		String varName = "$SQ"+constructNode.getNodeId();
		this.externalVariableList.add(varName);
		// Inclusão de uma fonte de dados no FOR com o caminho para o nodo sendo analisado
		f.addSource(varName + "/SQ"+constructNode.getNodeId() + "/" + constructNode.getLabel());
	}
		
	
	protected void searchAndClauses(String varId, TreeNode refNode) throws IOException, OptimizerException{
		
		if (refNode.hasChield()){
			for (int i=0; i<refNode.getChildren().size(); i++){
				this.searchAndClauses(varId, refNode.getChild(i));
			}
		}
		else if (!refNode.isKeyNode()){
			String path = this.buildPath(refNode);
			path = varId + path;
			
			// Inclusão do AND
			this.where.addAnd(path);
		}
	}
	
	protected void includeJoinPredicate(JoinOperator join) throws IOException, OptimizerException{
		// Tratamento dos predicados do Join
		for (int p=0; p<join.getPredicateList().size(); p++){
			String pred = join.getPredicateList().get(p);
			
			// Recuperação dos dois LCLs da comparação
			int lcl1 = Integer.parseInt(pred.substring(0, pred.indexOf('=')));
			int lcl2 = Integer.parseInt(pred.substring(pred.indexOf('=')+1, pred.length()));
			
			// Busca dos nodes correspondente aos LCLs encontrados
			TreeNode node1 = join.findNodeInChildrenById(lcl1);
			TreeNode node2 = join.findNodeInChildrenById(lcl2);
			
			// Construção dos paths para os nodos - inclusão de novos FORs se for o caso
			String path1 = this.buildPath(node1);
			String path2 = this.buildPath(node2);
			
			// Inclusão do predicado do join na cláusula Where da sub-query
			this.where.addAnd(path1 + " = " + path2);
		}
	}

	public String getExecutionSite() {
		return executionSite;
	}

	public void setExecutionSite(String site) {
		executionSite = site;
	}
	
	public String getVarId(){
		return "SQ" + this.queryId;
	}
	
	public String getInnerQueryForLet(){
		if (this.innerQueryForLet.equals("for"))
			return "for $v" + this.queryId + " in ";
		else
			return "let $v" + this.queryId + " := ";
	}
	
	public String externalVariables(){
		String ret = "";
		
		for (int i=0; i<this.externalVariableList.size(); i++){
			ret += "declare variable " + (String)this.externalVariableList.get(i) + " as node() external; \r\n";
		}
		// InnerQueries external variables
		for (int i=0; i<this.innerQueryList.size(); i++){
			SubQuery iq = this.innerQueryList.get(i);
			ret += iq.externalVariables();
		}
		
		return ret;
	}
	
	public String toString(){
		String ret = "";
		
		// External Variables
		if (!this.isInnerQuery){
			ret += this.externalVariables();
		}		
		
		// InnerQueries
		for (int i=0; i<this.innerQueryList.size(); i++){
			SubQuery iq = this.innerQueryList.get(i);
			ret += iq.getInnerQueryForLet() + iq.toString();
		}
		
		// FORs
		for (int i=0; i<this.forList.size(); i++){
			ForClause myFor = this.forList.get(i);
			ret += myFor.toString();
		}
		
		// LETs
		for (int i=0; i<this.letList.size(); i++){
			LetClause myLet = this.letList.get(i);
			ret += myLet.toString();
		}
		
		// WHERE
		ret += this.where.toString();
		
		// ORDER BY
		ret += this.orderBy.toString();
		
		// RETURN
		ret += this.returncl.toString();
		
		return ret;
	}
}
