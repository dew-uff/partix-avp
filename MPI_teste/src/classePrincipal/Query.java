package classePrincipal;

import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class Query {

	protected String queryExprType; // Indica se a consulta informada pelo usuário utiliza a cláusula doc() ou a cláusula collection();
	protected String fragmentationAttribute; // Indica o caminho xpath do atributo de fragmentação virtual
	protected long totalExecutionTime;
	
	protected synchronized long gettotalExecutionTime() {
		return totalExecutionTime;
	}

	protected synchronized void settotalExecutionTime(long totalExecTime) {
		this.totalExecutionTime = totalExecTime;
	}
		
	protected String xpath="";
	protected String xpathAggregateFunction = "";
	
	public String getXpathAggregateFunction() {
		return xpathAggregateFunction;
	}

	public void setXpathAggregateFunction(String xpathAggregateFunction) {
		this.xpathAggregateFunction = xpathAggregateFunction;
	}

	protected String inputQuery=""; // Armazena a consulta de entrada
	protected String orderBy = "";
	protected String orderByType = ""; // indica se a ordenacao é ascending ou descending.
	protected String lastReadForLetVariable = "";
	protected String lastReturnVariable = "";
	protected String lastReadFunction = ""; // indica a ultima funcao de agregacao lida
	protected boolean isWaitingXpathAggregateFunction = false; // indica se esta esperando a leitura do caminho xpath referente a funcao de agregacao. Ex.: count($l/order_line). Estaria aguardando a leitura de order_line.
	protected String elementsAroundFunction = ""; // indica os elementos em torno da funcao de agregacao. Ex.: ... return <resp><total><arrecadacao>{sum($pagto)}</arrecadacao></total><resp>. ElementsAroundFunction = resp|total|arrecacadao.
	
	public String getElementsAroundFunction() {
		return elementsAroundFunction;
	}

	public void setElementsAroundFunction(String elementsAroundFunction) {
		this.elementsAroundFunction = elementsAroundFunction;
	}

	public String getLastReadFunction() {
		return lastReadFunction;
	}

	public void setLastReadFunction(String lastReadFunction) {
		this.lastReadFunction = lastReadFunction;
	}
	
	public void setAggregateFunc(String variableName, String aggregateFunction) {
		if ( this.aggregateFunctions == null ){
			this.aggregateFunctions = new Hashtable<String, String>();
		}
		
		this.aggregateFunctions.put(variableName, aggregateFunction);
	}

	
	public boolean isWaitingXpathAggregateFunction() {
		return isWaitingXpathAggregateFunction;
	}

	public void setWaitingXpathAggregateFunction(boolean isWaitingXpathAggregateFunction) {
		this.isWaitingXpathAggregateFunction = isWaitingXpathAggregateFunction;
	}

	public String getLastReturnVariable() {
		return lastReturnVariable;
	}

	public void setLastReturnVariable(String lastReturnVariable) {
		this.lastReturnVariable = lastReturnVariable;
	}

    protected Hashtable<String, String> aggregateFunctions; // indica as funçoes de agregação que devem ser acrescentadas no final.
	
	public Hashtable<String, String> getAggregateFunctions() {
		return aggregateFunctions;
	}

	public void setAggregateFunctions(String aggregateFunction, String variableName, String comparisonOp) {
		
		if (this.aggregateFunctions == null) {
			this.aggregateFunctions = new Hashtable<String, String>();
		}
		
		// verifica qual a funcao utilizada
		String functionPredicate = "";
		
		/*if ( aggregateFunction.toUpperCase().indexOf("COUNT") != -1 ){
			functionPredicate = "count";
		}
		else if ( aggregateFunction.toUpperCase().indexOf("MAX") != -1 ){
			functionPredicate = "max";			
		}
		else if ( aggregateFunction.toUpperCase().indexOf("MIN") != -1 ){
			functionPredicate = "min";
		}
		else if ( aggregateFunction.toUpperCase().indexOf("SUM") != -1 ){
			functionPredicate = "sum";
		}
		else if ( aggregateFunction.toUpperCase().indexOf("AVERAGE") != -1 ){
			functionPredicate = "average";
		}*/
		
		functionPredicate = aggregateFunction + "(" + variableName + ")" + comparisonOp;
		
		if ( comparisonOp!=null && !comparisonOp.equals("") ) { // count de claúsulas Where em consultas com FOR									
			this.getAggregateReturn().put(variableName, functionPredicate);
		}
		else { // count em consultas com LET.			
			this.getAggregateFunctions().put(functionPredicate, functionPredicate);
		}
	}

	public String getLastReadForLetVariable() {
		return lastReadForLetVariable;
	}

	public void setLastReadForLetVariable(String lastReadForLetVariable) {
		this.lastReadForLetVariable = lastReadForLetVariable;
	}

	public String getOrderByType() {
		return orderByType;
	}

	public void setOrderByType(String orderByType) {
		this.orderByType = orderByType;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getInputQuery() {
		return inputQuery;
	}

	public void setInputQuery(String inputQuery) {
		this.inputQuery = inputQuery;
	}
	
	protected String fragmentationVariable= ""; // Setada na classe ForLetClause.java ao iniciar o parser de um FOR.
	protected String lastReadLetVariable = "";
	protected String lastReadWhereVariable = "";
	protected String lastReadSimplePathExpr = "";
	protected String lastReadDocumentExpr = "";
	protected String lastReadCollectionExpr = "";
	protected int lastReadCardinality = 0;
	protected boolean addedPredicate;
	protected boolean elementConstructor;
	protected boolean orderByClause;
	
	public boolean isOrderByClause() {
		return orderByClause;
	}

	public void setOrderByClause(boolean orderByClause) {
		this.orderByClause = orderByClause;
	}

	public boolean isElementConstructor() {
		return elementConstructor;
	}

	public void setElementConstructor(boolean elementConstructor) {
		this.elementConstructor = elementConstructor;
	}

	public boolean isAddedPredicate() {
		return addedPredicate;
	}

	public void setAddedPredicate(boolean addedPredicate) {
		this.addedPredicate = addedPredicate;
	}

	public int getLastReadCardinality() {
		return lastReadCardinality;
	}

	public void setLastReadCardinality(int lastReadCardinality) {
		this.lastReadCardinality = lastReadCardinality;
	}

	public String getLastReadCollectionExpr() {
		return lastReadCollectionExpr;
	}

	public void setLastReadCollectionExpr(String lastReadCollectionExpr) {
		this.lastReadCollectionExpr = lastReadCollectionExpr;
	}

	public String getLastReadDocumentExpr() {
		return lastReadDocumentExpr;
	}

	public void setLastReadDocumentExpr(String lastReadDocumentExpr) {
		this.lastReadDocumentExpr = lastReadDocumentExpr;
	}

	protected String operatorComparision = "";	

	protected static Query _inputQuery;	
	
	protected Hashtable<String,String> forClauses = new Hashtable<String,String>();
	protected Hashtable<String,String> letClauses = new Hashtable<String,String>();
	protected Hashtable<String,String> selectionPredicates = new Hashtable<String,String>();
	protected Hashtable<String,String> aggregateReturn = new Hashtable<String,String>(); // indica os nomes do elementos em torno das funcoes de agregacao no resultado.
	
	public Hashtable<String, String> getAggregateReturn() {
		return aggregateReturn;
	}

	public void setAggregateReturn(String variableName, String aggregateFunction) {
		if ( this.aggregateReturn == null ){
			this.aggregateReturn = new Hashtable<String, String>();
		}
		
		this.aggregateReturn.put(variableName, aggregateFunction);
	}

	protected Hashtable<String,Hashtable<String,String>> indexes = new Hashtable<String,Hashtable<String,String>>();
	protected Hashtable<String,String> indexesToBeUsed = new Hashtable<String,String>();
	
	public String getqueryExprType(){
		return queryExprType;
	}
	
	public static Query getUniqueInstance(boolean flag) throws IOException{		
		if (!flag || _inputQuery == null)
			_inputQuery = new Query();
				
		return _inputQuery;
	}	

	public String getFragmentationAttribute() {
		return fragmentationAttribute;
	}

	public void setFragmentationAttribute(String attributeName) {
		this.fragmentationAttribute = attributeName;
	}
	
	public String getPathVariable(String variableName){
		
		String path = this.forClauses.get(variableName); // caminho da variável
		int posComma = path.indexOf(":");		
		String subPath = path.substring(posComma+1,path.length());		
		
		if (subPath.indexOf(":")!=-1){			
			subPath = subPath.substring(subPath.indexOf(":")+1,subPath.length());
		}	
		
		//System.out.println("Query.java.pathVariable:::"+subPath);
		return subPath;
	}
			
	
	public String getDocumentNameByVariableName(String variableName) {

		/* this.forClauses é uma hashtable que possuem a seguinte estrutura:
		<chave, conteudo> onde a chave é o nome da variável XML, incluindo o caracter $ (ex.: $order)
		e conteudo é o caminho completo sobre o qual a variável está definida.
		
		Este caminho é expresso segundo o seguinte padrão: 
			nomeDocumento:nomeColecao:caminhoXpath
			Ex.: loja:informacoesLojas:Loja/Itens/Item/Pedido
		*/
		
		String path = this.forClauses.get(variableName); // definicao da variavel. Indica o documento, a colecao e o caminho xpath ao qual a variavel se refere.
		int posComma = path.indexOf(":"); // posição do primeiro caracter (:)
		String documentName = path.substring(0, posComma); // Nome do documento
		
		return documentName;		
	}
	
    public String getCollectionNameByVariableName(String variableName) throws IOException{
		
		String path = this.forClauses.get(variableName); // definicao da variavel. Indica o documento, a colecao e o caminho xpath ao qual a variavel se refere.
		int posComma = path.indexOf(":"); // posição do primeiro caracter (:)
		String subPath = path.substring(posComma+1,path.length());
		String collectionName = subPath.substring(0,subPath.indexOf(":"));

		return collectionName;	
		
	}

    public String getXpathByVariableName(String variableName) {
	
		String path = this.forClauses.get(variableName); // definicao da variavel. Indica o documento, a colecao e o caminho xpath ao qual a variavel se refere.		
		int posComma = path.indexOf(":");		
		String subPath = path.substring(posComma+1,path.length());				
		subPath = subPath.substring(subPath.indexOf(":")+1,subPath.length());
		
		return subPath;
	
    }
	
	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public String getFragmentationVariable() {
		return fragmentationVariable;
	}

	public void setFragmentationVariable(String fragmentationVariable) {
		this.fragmentationVariable = fragmentationVariable;
	}

	public Hashtable<String, String> getForClauses() {
		return forClauses;
	}

	public void setForClauses(String varName, String pathName) {
		this.forClauses.put(varName,pathName);
	}
	
	public Hashtable<String, String> getSelectionPredicates() {
		return selectionPredicates;
	}

	public void setSelectionPredicates(String whereClause, String wherePath) {
		this.selectionPredicates.put(whereClause, wherePath);
	}
	
	public Hashtable<String, String> getLetClauses() {
		return letClauses;
	}

	public void setLetClauses(String varName, String referenceVarName) {
		this.letClauses.put(varName,referenceVarName);
	}

	public String getLastReadLetVariable() {
		return lastReadLetVariable;
	}

	public void setLastReadLetVariable(String lastReadLetVariable) {
		this.lastReadLetVariable = lastReadLetVariable;
	}

	public String getLastReadWhereVariable() {
		return lastReadWhereVariable;
	}

	public void setLastReadWhereVariable(String lastReadWhereVariable) {
		this.lastReadWhereVariable = lastReadWhereVariable;
	}

	public String getOperatorComparision() {
		return operatorComparision;
	}

	public void setOperatorComparision(String operatorComparision) {
		this.operatorComparision = operatorComparision;
	}

	public String getLastReadSimplePathExpr() {
		return lastReadSimplePathExpr;
	}

	public void setLastReadSimplePathExpr(String lastReadSimplePathExpr) {
		this.lastReadSimplePathExpr = lastReadSimplePathExpr;
	}

	public Hashtable<String, Hashtable<String, String>> getIndexes() {
		return indexes;
	}

	public void setIndexes(String path, Hashtable<String, String> indexIdentifier) {
		this.indexes.put(path,indexIdentifier);
	}

	public Hashtable<String, String> getIndexesToBeUsed() {
		return indexesToBeUsed;
	}

	public void setIndexesToBeUsed(String indexName, String operator) {
		this.indexesToBeUsed.put(indexName,operator);
	}

	
}
