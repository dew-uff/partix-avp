package mediadorxml.optimizer;

import java.io.IOException;
import java.util.ArrayList;

import mediadorxml.algebra.basic.Predicate;
import mediadorxml.algebra.basic.TreeNode;
import mediadorxml.algebra.basic.Predicate.ComparisonOperator;
import mediadorxml.algebra.operators.AbstractOperator;
import mediadorxml.algebra.operators.JoinOperator;
import mediadorxml.algebra.operators.SelectOperator;
import mediadorxml.algebra.operators.UnionOperator;
import mediadorxml.catalog.CatalogManager;
import mediadorxml.catalog.util.GlobalView;
import mediadorxml.catalog.util.LocalView;
import mediadorxml.catalog.util.WrapperLocation;
import mediadorxml.exceptions.AlgebraParserException;
import mediadorxml.exceptions.FragmentReductionException;
import mediadorxml.exceptions.GlobalViewNotFoundException;
import mediadorxml.exceptions.OptimizerException;

public class Optimizer {

	public static AbstractOperator localizeGlobalViews(SelectOperator select)
			throws OptimizerException, AlgebraParserException {

		try {
			// Recupera��o do Singleton do gerenciador do Cat�logo
			CatalogManager cm = CatalogManager.getUniqueInstance();

			// Nome da vis�o global
			String globalViewName = select.getApt().getAptRootNode().getLabel();

			// Gera��o da opera��o de fragmentos que define a vis�o global
			TreeNode localization = cm.getGlobalViewLocalization(globalViewName);

			return Optimizer.getOperator(localization, select);

		} catch (IOException ex) {
			throw new OptimizerException(ex);
		} catch (GlobalViewNotFoundException ex) {
			throw new OptimizerException(ex);
		}
	}

	protected static AbstractOperator getOperator(TreeNode node,
			SelectOperator select) throws AlgebraParserException, IOException,
			OptimizerException {

		AbstractOperator op;
		String label = node.getLabel();

		// Uni�o de fragmentos
		if (label.equals("UNION")) {
			op = new UnionOperator();
			op.addChild(Optimizer.getOperator(node.getChild(0), select));
			op.addChild(Optimizer.getOperator(node.getChild(1), select));
			op.generateApt();
			op.setLocalizationOperator(true);
		}
		// Jun��o de fragmentos
		else if (label.equals("JOIN")) {
			op = new JoinOperator();
			op.addChild(Optimizer.getOperator(node.getChild(0), select));
			op.addChild(Optimizer.getOperator(node.getChild(1), select));
			op.generateApt();
			op.setLocalizationOperator(true);
			generateJoinPredicate(op);
		}
		// Fragmento
		else {
			try {
				op = select.clone();
				op.resetInternalIds();
				op.getApt().getAptRootNode().setLabel(label);
			} catch (CloneNotSupportedException exc) {
				exc.printStackTrace();
				op = null;
			}
		}

		return op;
	}

	public static void generateJoinPredicate(AbstractOperator join)
			throws IOException, OptimizerException {

		// Recupera��o do Singleton do gerenciador do Cat�logo
		CatalogManager cm = CatalogManager.getUniqueInstance();

		// Recuperamos a view local (fragmento) sobre o qual o operador �
		// aplicado
		TreeNode selectNode;
		if (join.getChildAt(0).getName().equals("Select")) {
			selectNode = join.getChildAt(0).getApt().getAptRootNode();
		} else {
			selectNode = join.getChildAt(1).getApt().getAptRootNode();
		}
		LocalView lv = cm.getLocalView(selectNode.getLabel());
		GlobalView gv = cm.getGlobalView(lv.getReferenceCollection());

		join.getPredicateList().clear();

		if (gv.getIndexNodes() != null) {
			for (int i = 0; i < gv.getIndexNodes().size(); i++) {

				boolean continuar = true;
				TreeNode predicate = Optimizer.buildTreeNodeFromPredicate(lv
						.getViewName(), gv.getIndexNodes().get(i));

				// 1. Navegar at� o keyNode do APT do Select seguindo o
				// predicado de proje��o
				while ((!selectNode.isKeyNode()) && (continuar)) {
					if (selectNode.getLabel().equals(predicate.getLabel())) {
						selectNode = selectNode.getChild(0);
						if (predicate.hasChield())
							predicate = predicate.getChild(0);
						else
							continuar = false;
					} else if (selectNode.getRelationType().equals(
							TreeNode.RelationTypeEnum.ANCESTOR_DESCENDENT)) {
						if (predicate.hasChield())
							predicate = predicate.getChild(0);
						else
							continuar = false;
					}
				}
				if (!selectNode.getLabel().equals(predicate.getLabel())) {
					if (selectNode.getRelationType().equals(
							TreeNode.RelationTypeEnum.ANCESTOR_DESCENDENT)) {
						while (continuar
								&& (!selectNode.getLabel().equals(
										predicate.getLabel()))) {
							if (predicate.hasChield())
								predicate = predicate.getChild(0);
							else
								continuar = false;
						}
					} else
						continuar = false;
				}

				if (continuar) {

					String joinPredicate = "";

					// Inclus�o do indice nos APTs do Join
					TreeNode select0LCL = join.getApt().getAptRootNode()
							.getChild(0);
					TreeNode select1LCL = join.getApt().getAptRootNode()
							.getChild(1);

					TreeNode ref0 = join.findNodeInChildrenById(select0LCL
							.getLabelLCLid());
					TreeNode ref1 = join.findNodeInChildrenById(select1LCL
							.getLabelLCLid());

					// if
					// (ref0.getRootNode().getRefAPT().getRefOperator().getName().equals("Select")){

					TreeNode indexNode = predicate.clone().getChild(0);
					indexNode.resetInternalIds();
					indexNode.setParentNode(null);

					TreeNode indexKeyNode = indexNode.getKeyNode();
					joinPredicate += indexKeyNode.getNodeId();
					indexKeyNode.setIsKeyNode(false);

					select0LCL.getChildren().clear();
					select0LCL.addChild(indexNode);
					// }
					// else{
					// AlgebraicOperator joinChild =
					// ref0.getRefAPT().getRefOperator();
					// String joinChildPred =
					// joinChild.getPredicateList().get(0);
					// joinPredicate += joinChildPred.substring(0,
					// joinChildPred.indexOf("="));
					// }

					joinPredicate += "=";

					// if
					// (ref1.getRootNode().getRefAPT().getRefOperator().getName().equals("Select")){

					indexNode = predicate.clone().getChild(0);
					indexNode.resetInternalIds();
					indexNode.setParentNode(null);

					indexKeyNode = indexNode.getKeyNode();
					joinPredicate += indexKeyNode.getNodeId();
					indexKeyNode.setIsKeyNode(false);

					select1LCL.getChildren().clear();
					select1LCL.addChild(indexNode);
					// }
					// else{
					// AlgebraicOperator joinChild =
					// ref1.getRefAPT().getRefOperator();
					// String joinChildPred =
					// joinChild.getPredicateList().get(0);
					// joinPredicate += joinChildPred.substring(0,
					// joinChildPred.indexOf("="));
					// }

					join.getPredicateList().add(joinPredicate);
				}
			}
		}
	}

	/**
	 * Verifica��o da utilizada de um Operador Select para o resultado da query
	 * a partir das defini��es de fragmenta��o horizontal e vertical do
	 * fragmento sobre o qual o operador � aplicado.
	 * 
	 * @param select:
	 *            Operador Select que ser� analisado
	 * @return boolean indicando a utilidade do operador
	 * @throws FragmentReductionException
	 * @throws OptimizerException
	 */
	public static boolean isSelectOperatorUseful(SelectOperator select)
			throws FragmentReductionException, OptimizerException {
		// valida��o da utilidade de um select sobre um fragmento

		try {
			// Recupera��o do Singleton do gerenciador do Cat�logo
			CatalogManager cm = CatalogManager.getUniqueInstance();

			// Recuperamos a view local (fragmento) sobre o qual o operador � aplicado
			LocalView lv = cm.getLocalView(select.getApt().getAptRootNode().getLabel());
			GlobalView gv = cm.getGlobalView(lv.getReferenceCollection());

			// ---------------------------------------------------------------
			// Tratamento dos predicados de sele��o (fragmenta��o horizontal)
			for (int i = 0; i < lv.getSelectionPredicates().size(); i++) {
				TreeNode predicate = Optimizer.buildTreeNodeFromPredicate(lv.getViewName(), lv.getSelectionPredicates().get(i));
				TreeNode apt = select.getApt().getAptRootNode();

				// compara��o dos dois TreeNodes para validar a utilidade do fragmento
				if (!apt.satisfies(predicate)){
					return false; // o predicado do fragmento n�o satisfaz o predicado da sele��o
				}
			}

			// ---------------------------------------------------------------
			// Tratamento dos predicados de proje��o (fragmenta��o vertical)
			if (lv.getProjectionPredicates().size() > 0) {
				int uso = countValidNodesInUse(select);
				int tolerancia = 0;
				// Toleramos os �ndices no caso de exist�ncia de jun��o de
				// fragmentos verticais
				// ( N indices X M jun��es acima do fragmento
				if (gv.getIndexNodes() != null){
					AbstractOperator op = select;
					while (op.getParentOperator() != null) {
						op = op.getParentOperator();
						if (op.getName().equals("Join")) {
							tolerancia += gv.getIndexNodes().size();
						}
					}
				}
				// Toleramos o pr�prio APT sobre o fragmento, pois ele ser�
				// sempre contado se n�o tiver nodos filhos
				if (!select.getApt().getKeyNode().hasChield())
					tolerancia++;

				if (uso <= tolerancia)
					return false;
			}
		} catch (IOException ex) {
			throw new FragmentReductionException(ex);
		}

		return true;
	}

	/**
	 * Contagem dos nodos v�lidos para o fragmento vertical no plano alg�brico
	 * para um operador Select sobre o pr�prio fragmento
	 * 
	 * @param select
	 * @param predicateNode
	 * @return
	 */
	private static int countValidNodesInUse(SelectOperator select)
			throws OptimizerException, IOException {

		int countUse = 0;
		TreeNode selectNode = select.getApt().getAptRootNode();
		String fragmentName = selectNode.getLabel();

		// 1. Verificar utilidade no pr�prio operador de Select
		if (selectNode.hasChield()){
			countUse += countValidNodesInUse(selectNode, select, fragmentName);
		}
		
		// Se n�o for encontrada utilidade no pr�prio operador de Select,
		// que pode acontecer em fragmentos Verticais podados, temos que 
		// adicionar 1 ao countUse se o KeyNode n�o tiver filhos, para
		// evitar a Toler�ncia posteriormente.
		if ((countUse == 0) && (!select.getApt().getKeyNode().hasChield())){
			countUse++;
		}

		// 2. Verificar utilidade nos demais operadores do plano alg�brico
		AbstractOperator op = select;
		countUse += countValidNodesInUse(op, selectNode, fragmentName);

		return countUse;
	}

	private static int countValidNodesInUse(AbstractOperator op,
			TreeNode nodeToSearch, String fragmentName)
			throws OptimizerException, IOException {
		int countUse = 0;
		String lclSearch = nodeToSearch.getKeyNode().getLCL();
		while (op.getParentOperator() != null) {
			op = op.getParentOperator();
			if (op.getApt().getAptRootNode() != null){
				TreeNode lclNode = op.getApt().getAptRootNode().findNode(lclSearch);
				if (lclNode != null) {
					if (lclNode.hasChield()) {
						countUse += countValidNodesInUse(lclNode, op, fragmentName);
					}
					// Caso o operador seja Select, deveremos tratar mesmo que n�o tenha
					// filhos (caso de "return $v ")
					else if (op.getName().equals("Select")){
						countUse += countValidNodesInUse(lclNode, op, fragmentName);
					}
					
					if (!lclNode.isKeyNode()) {
						TreeNode k = op.getApt().getAptRootNode().getKeyNode();
						countUse += countValidNodesInUse(op, k, fragmentName);
					}
				}
			}
		}
		return countUse;
	}

	/**
	 * Contagem dos nodos v�lidos em um TreeNode para um fragmento vertical
	 * 
	 * @param selectNode
	 * @param predicateNode
	 * @return
	 */
	private static int countValidNodesInUse(TreeNode node, AbstractOperator planoAlgebrico, String fragmentName)
			throws OptimizerException, IOException {
		int countUse = 0;

		if (node.hasChield()) {
			for (int i = 0; i < node.getChildren().size(); i++) {
				countUse += countValidNodesInUse(node.getChild(i), planoAlgebrico, fragmentName);
			}
		} else {
			// Se for leaf-node
			if (Optimizer.isTreeNodeInVerticalFragmentDefinition(node, planoAlgebrico, fragmentName)) {
				countUse++;
			}
		}

		return countUse;
	}

	protected static TreeNode buildTreeNodeFromPredicate(String fragmentName,
			String selectionPredicate) throws OptimizerException {

		TreeNode pred = new TreeNode(fragmentName,
				TreeNode.RelationTypeEnum.ROOT);

		String[] ss = selectionPredicate.split("/");

		for (int i = 0; i < ss.length; i++) {
			String name = ss[i];
			if (name.length() > 0) {
				TreeNode.RelationTypeEnum relType = TreeNode.RelationTypeEnum.PARENT_CHILD;
				Predicate predicate = null;
				if (ss[i].startsWith("/")) {
					relType = TreeNode.RelationTypeEnum.ANCESTOR_DESCENDENT;
					name = ss[i].substring(1);
				}
				if (name.indexOf(" ") > 0) {
					String p = name.substring(name.indexOf(" ") + 1, name
							.length());
					name = name.substring(0, name.indexOf(" "));
					if (p.length() > 0) {
						ComparisonOperator op = null;
						if (p.indexOf(">= ") >= 0)
							op = Predicate.ComparisonOperator.GreaterThanOrEqualsTo;
						else if (p.indexOf("> ") >= 0)
							op = Predicate.ComparisonOperator.GreaterThan;
						else if (p.indexOf("<= ") >= 0)
							op = Predicate.ComparisonOperator.LessThanOrEqualsTo;
						else if (p.indexOf("< ") >= 0)
							op = Predicate.ComparisonOperator.LessThan;
						else if (p.indexOf("= ") >= 0)
							op = Predicate.ComparisonOperator.EqualsTo;
						else
							throw new OptimizerException(
									"Selection predicate Comparison Operator not detected.");

						p = p.substring(p.indexOf(" "));

						predicate = new Predicate(op, p);
					}
				}
				TreeNode n = new TreeNode(name, relType);
				n.setPredicate(predicate);
				pred.addChild(n);
				pred = n;
			}
		}

		pred.setIsKeyNode(true);

		return pred.getRootNode();
	}

	/*
	 * M�todo simplificado de otimiza��o do plano de execu��o localizado
	 */
	public static AbstractOperator optimizePlan(AbstractOperator operator)
			throws OptimizerException, IOException {

		ArrayList planosExecucao;

		// 1: montar os N planos de execu��o poss�veis
		planosExecucao = Optimizer.generateAllPlans(operator);

		// 2: Estimar os custos de execu��o dos planos
		double[] custos = new double[planosExecucao.size()];
		CostEstimator ce = new CostEstimator();
		for (int i = 0; i < planosExecucao.size(); i++) {
			AbstractOperator op = (AbstractOperator) planosExecucao.get(i);
			custos[i] = ce.estimatePlanExecutionCost(op);
		}

		// 3: Obter o plano com menor custo
		double menor = custos[0];
		int indexMenor = 0;
		for (int c = 1; c < custos.length; c++) {
			if (menor > custos[c]) {
				menor = custos[c];
				indexMenor = c;
			}
		}
		return (AbstractOperator) planosExecucao.get(indexMenor);
	}

	/*
	 * Montagem de todos os planos poss�veis
	 */
	protected static ArrayList<AbstractOperator> generateAllPlans(
			AbstractOperator plan) throws OptimizerException, IOException {

		ArrayList<AbstractOperator> planos = new ArrayList<AbstractOperator>();

		// Gera��o de todos os planos de execu��o poss�veis

		// 1: Busca dos fragmentos utilizados pela consulta
		ArrayList listaFragmentos = Optimizer.getFragmentsList(plan);

		// 2: Montagem das combina��es poss�veis fragmento/site
		int numCombinacoes = 1;
		for (int i = 0; i < listaFragmentos.size(); i++) {
			LocalView lv = (LocalView) listaFragmentos.get(i);
			numCombinacoes = numCombinacoes * lv.getWrapperLocation().size();
		}

		WrapperLocation[][] matrix = new WrapperLocation[numCombinacoes][listaFragmentos
				.size()];
		for (int i = 0; i < listaFragmentos.size(); i++) {

			LocalView lvi = (LocalView) listaFragmentos.get(i);
			int repeticaoGrupo = 1;
			int repeticaoIndividuo = 1;

			// C�lculo do repeticaoIndividuo (depois)
			for (int j = i + 1; j < listaFragmentos.size(); j++) {
				LocalView lvj = (LocalView) listaFragmentos.get(j);
				repeticaoIndividuo = repeticaoIndividuo
						* lvj.getWrapperLocation().size();
			}

			// C�lculo do repeticaoGrupo (antes)
			for (int k = 0; k < i; k++) {
				LocalView lvk = (LocalView) listaFragmentos.get(k);
				repeticaoGrupo = repeticaoGrupo
						* lvk.getWrapperLocation().size();
			}

			if (repeticaoIndividuo * repeticaoGrupo
					* lvi.getWrapperLocation().size() != numCombinacoes)
				throw new OptimizerException(
						"Erro na gera��o das combina��es para otimiza��o dos planos de execu��o");

			// Montagem do grupo
			WrapperLocation[] grupo = new WrapperLocation[lvi
					.getWrapperLocation().size()
					* repeticaoIndividuo];
			int c = 0;
			for (int x = 0; x < lvi.getWrapperLocation().size(); x++) {
				for (int y = 0; y < repeticaoIndividuo; y++) {
					grupo[c] = (WrapperLocation) lvi.getWrapperLocation()
							.get(x);
					c++;
				}
			}

			// Inclus�o na matriz de combina��es
			c = 0;
			for (int z = 0; z < repeticaoGrupo; z++) {
				for (int w = 0; w < grupo.length; w++) {
					matrix[c][i] = grupo[w];
					c++;
				}
			}
		}

		// 3: Cria��o dos planos de execu��o para cada combina��o fragmento/site
		for (int i = 0; i < numCombinacoes; i++) {
			planos.add(Optimizer.localizeAndClonePlan(plan, listaFragmentos, matrix[i]));
		}

		return planos;

	}

	/*
	 * Busca lista de LocalViews com todos os fragmentos utilizados pelo plano
	 * alg�brico
	 */
	protected static ArrayList<LocalView> getFragmentsList(AbstractOperator plan)
			throws OptimizerException {
		ArrayList<LocalView> listLocalViews = new ArrayList<LocalView>();

		// busca da lista de fragmentos (localViews) utilizados no plano de
		// execu��o
		try {
			CatalogManager cm = CatalogManager.getUniqueInstance();

			// Busca dos operadores Select para localiza��o das views globais
			ArrayList opsSelect = plan.getOperatorsListByType("Select");

			// Para cada operador Select, faremos a localiza��o da view global
			for (int i = 0; i < opsSelect.size(); i++) {
				AbstractOperator select = (AbstractOperator) opsSelect.get(i);
				// Valida��o se o operador � sobre um Fragmento
				String viewName = select.getApt().getAptRootNode().getLabel();
				if (!viewName.startsWith("(")) {
					LocalView lv = cm.getLocalView(viewName);
					listLocalViews.add(lv);
				}
			}
		} catch (Exception e) {
			throw new OptimizerException("Erro na recupera��o do Cat�logo", e);
		}

		return listLocalViews;
	}

	protected static AbstractOperator localizeAndClonePlan(
			AbstractOperator plan, ArrayList listaFragmentos,
			WrapperLocation[] listaLocalizacao) throws OptimizerException,
			IOException {

		AbstractOperator planClone = null;

		try {
			planClone = plan.clone();
		} catch (CloneNotSupportedException exc) {
			exc.printStackTrace();
		}

		// 1: Inclus�o da localiza��o dos Fragmentos no plano
		for (int i = 0; i < listaFragmentos.size(); i++) {
			planClone = planClone.getRootOperator();
			LocalView lv = (LocalView) listaFragmentos.get(i);
			planClone.setWrapperLocation(lv.getViewName(), listaLocalizacao[i]);
		}

		// 2: Localiza��o das opera��es que n�o s�o efetuadas diretamente sobre
		// um fragmento
		Optimizer.completeWrapperLocation(planClone);

		return planClone;
	}

	/*
	 * Localiza��o dos operadores n�o-localizados e montagem dos Construct de
	 * operadores filho que forem executados em Sites remotos
	 */
	protected static void completeWrapperLocation(AbstractOperator operator)
			throws OptimizerException, IOException {

		WrapperLocation wl = null;

		// Se todos os operadores filho forem executados no mesmo Site,
		// este operador poder� ser executado no mesmo Site dos filhos.
		// Caso contr�rio ser� executado no pr�prio Mediador
		if (operator.hasChild()) {
			for (int i = 0; i < operator.getChildOperators().size(); i++) {
				AbstractOperator child = operator.getChildAt(i);
				if (child.getExecutionSite() == null) {
					Optimizer.completeWrapperLocation(child);
				}
				if (wl == null)
					wl = child.getExecutionSite();
				else {
					if (!wl.equals(child.getExecutionSite())) {
						wl = WrapperLocation.getMediatorLocation();
					}
				}
			}
		}

		operator.setExecutionSite(wl);

		// Cria��o dos Construct dos operadores filho que forem executados em
		// sites diferentes do operador atual
		if (operator.hasChild()) {
			for (int i = 0; i < operator.getChildOperators().size(); i++) {
				AbstractOperator child = operator.getChildAt(i);
				if (!operator.getExecutionSite().equals(child.getExecutionSite())) {
					child.addConstructOperator();
				}
			}
		}
	}

	/**
	 * Poda (prune) dos nodos filhos do node que n�o fazem parte do fragmento
	 * correspondente - para casos de fragmento com predicados de proje��o
	 * (fragmenta��o vertical)
	 * 
	 * @param node
	 */
	public static void pruneNodesNotInFragment(TreeNode node,
			AbstractOperator operator) throws IOException, OptimizerException {

		// 1. Buscar o nome da view local (fragmento)
		TreeNode k = node;
		while (k.getRootNode().isLabelLCL()) {
			int lcl = k.getRootNode().getLabelLCLid();
			k = operator.findNodeInPlanById(lcl);
		}
		String viewName = k.getRootNode().getLabel();

		// 2. Buscar a lista de predicados de proje��o
		CatalogManager cm = CatalogManager.getUniqueInstance();
		LocalView lv = cm.getLocalView(viewName);
		if (lv != null) {
			ArrayList<String> projPreds = lv.getProjectionPredicates();

			// 3. Se tiver predicado de proje��o, identificar se os nodos filhos
			// atendem aos predicados de proje��o,
			// removendo aqueles que n�o atenderem
			if (projPreds.size() > 0) {
				for (int i = 0; i < node.getChildren().size(); i++) {
					pruneNodesNotInFragment(node.getChild(i), operator,
							viewName);
				}
			}
		}
	}	
	
	/**
	 * Poda (prune) dos nodos pais do node que n�o fazem parte da proje��o 
	 * do fragmento correspondente - para casos de fragmento com predicados
	 * de proje��o (fragmenta��o vertical)
	 * 
	 * @param node
	 */
	public static void pruneNodesNotInProjection(TreeNode node,	AbstractOperator operator) throws IOException, OptimizerException {

		// 1. Buscar o nome da view local (fragmento)
		TreeNode k = node;
		while (k.getRootNode().isLabelLCL()) {
			int lcl = k.getRootNode().getLabelLCLid();
			k = operator.findNodeInPlanById(lcl);
		}
		String viewName = k.getRootNode().getLabel();

		// 2. Buscar a lista de predicados de proje��o
		CatalogManager cm = CatalogManager.getUniqueInstance();
		LocalView lv = cm.getLocalView(viewName);
		if ((lv != null) && (lv.getProjectionPredicates() != null) && (lv.getProjectionPredicates().size() > 0)) {
			
			// 3. Buscar o primeiro predicado de proje��o, que corresponde
			// � proje��o propriamente dita (sem exclus�o)
			String projecao = lv.getProjectionPredicates().get(0);
			
			TreeNode predicado = Optimizer.buildTreeNodeFromPredicate(viewName, projecao);
			
			TreeNode kNodeParent = node.getParentNode();
			TreeNode kNode = node;
			
			if (kNodeParent != null){
				// Navegamos at� o leaf-node do predicado
				while (predicado.hasChield()) {
					predicado = predicado.getChild(0);
				}
				
				while(kNode.getParentNode().getParentNode() != null){
					if (kNodeParent.getLabel().equals(predicado.getLabel())){
						TreeNode toKill = kNodeParent;
						kNodeParent = kNodeParent.getParentNode();
						kNodeParent.getChildren().clear();
						kNodeParent.addChild(kNode);
						toKill.getChildren().clear();
						toKill = null;
						predicado = predicado.getParentNode();
					}
					else {
						kNodeParent = kNodeParent.getParentNode();
						kNode = kNode.getParentNode();
					}
				}
			}
		}
	}

	private static void pruneNodesNotInFragment(TreeNode node,
			AbstractOperator operator, String fragmentName) throws IOException,
			OptimizerException {

		// S� podemos podar os leaf nodes, por isso temos que navegar at� eles
		if (node.hasChield()) {
			for (int i = 0; i < node.getChildren().size(); i++) {
				pruneNodesNotInFragment(node.getChild(i), operator,
						fragmentName);
			}
		} else { // Leaf-node

			if (!Optimizer.isTreeNodeInVerticalFragmentDefinition(node,
					operator, fragmentName)) {
				// Remo��o do nodo
				TreeNode nodeParent = node.getParentNode();
				nodeParent.getChildren().remove(node);
			}
		}
	}

	public static boolean isTreeNodeInVerticalFragmentDefinition(TreeNode node,
			AbstractOperator planoAlgebrico, String fragmentName)
			throws IOException, OptimizerException {

		// **************************************************************
		// TODO Reimplementar este m�todo utilizando o schema das views..
		// funcionar� para casos de relacionamento
		// Ancestor-descendant que atualmente n�o est� funcionando.
		// **************************************************************

		// 1. Buscar a lista de predicados de proje��o
		CatalogManager cm = CatalogManager.getUniqueInstance();
		LocalView lv = cm.getLocalView(fragmentName);
		GlobalView gv = cm.getGlobalView(lv.getReferenceCollection());
		ArrayList<String> projPreds = lv.getProjectionPredicates();

		// 2. Se tiver predicado de proje��o, identificar se os nodos filhos
		// atendem aos predicados de proje��o,
		// removendo aqueles que n�o atenderem
		if (projPreds.size() > 0) {

			// Adicionamos � lista de predicados de proje��o do fragmento os
			// predicados dos �ndices da fragmenta��o
			// vertical, se houver
			if ((gv.getIndexNodes() != null) && (gv.getIndexNodes().size() > 0)) {
				projPreds.addAll(gv.getIndexNodes());
			}

			int predsOk = 0;

			// Iremos validar o nodo para cada predicado de proje��o
			for (int p = 0; p < projPreds.size(); p++) {

				int inc = 1;
				String projecaoPathExpr = projPreds.get(p);
				String nodePathExpr = "";
				if (projecaoPathExpr.startsWith("{")) {
					inc = -1;
					projecaoPathExpr = projecaoPathExpr.substring(1, projecaoPathExpr.length() - 1);
				}

				boolean isOk = true;

				TreeNode nodeX = node;
				TreeNode predicado = Optimizer.buildTreeNodeFromPredicate(fragmentName, projecaoPathExpr);

				// Navegamos at� o leaf-node do predicado
				while (predicado.hasChield()) {
					predicado = predicado.getChild(0);
				}

				boolean encontrou = false;
				boolean isParent = true;
				while (isOk && (nodeX != null) && (predicado != null)) {

					while (nodeX.isLabelLCL()) {
						nodeX = planoAlgebrico.findNodeInPlanById(nodeX.getLabelLCLid());
						while (nodeX.getLabel().equals("Join_root")	|| nodeX.getLabel().equals("Union_root")) {
							nodeX = planoAlgebrico.findNodeInPlanById(nodeX.getChild(0).getLabelLCLid());
							AbstractOperator refOp = nodeX.getRootNode().getRefAPT().getRefOperator();
							if (refOp.getName().equals("Select")) {
								nodeX = refOp.getApt().getKeyNode();
							} else if (refOp.getName().equals("Construct")) {
								nodeX = refOp.getChildAt(0).getChildAt(0).getApt().getKeyNode();
							}
						}
					}
					
					nodePathExpr = "/" + nodeX.getLabel() + nodePathExpr;

					if ((nodeX.getLabel().equals(predicado.getLabel()))
							|| (predicado.getLabel().equals(fragmentName) 
							&& (nodeX.getParentNode() == null))) {
						encontrou = true;
						predicado = predicado.getParentNode();
						isParent = nodeX.getRelationType().equals(TreeNode.RelationTypeEnum.PARENT_CHILD);
						nodeX = nodeX.getParentNode();
					} else if (encontrou && isParent) {
						isOk = false;
					} else if (!isParent) {
						predicado = predicado.getParentNode();
					} else if (!encontrou) {
						isParent = nodeX.getRelationType().equals(TreeNode.RelationTypeEnum.PARENT_CHILD);
						nodeX = nodeX.getParentNode();
					}
				}

				if (isOk && encontrou) {
					predsOk += inc;
				}
				else{
					// Se o nodo n�o foi validado como filho do predicado de proje��o,
					// iremos verificar se ele � pai deste predicado de proje��o (caso "return $v ")
					
					if (nodePathExpr.indexOf("/",1) > 0){
						nodePathExpr = nodePathExpr.substring(nodePathExpr.indexOf("/",1), nodePathExpr.length());
						if (projecaoPathExpr.startsWith(nodePathExpr) && (inc>0)){
							predsOk += inc;
						}
					}
				}
			}
			
			if (predsOk <= 0) {
				return false;
			}
		}

		return true;
	}
}
