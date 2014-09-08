package globalqueryprocessor.subquerygenerator.svp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class SimpleVirtualPartitioning {

	protected static SimpleVirtualPartitioning svp;	
	protected int numberOfNodes; // número de nós disponíveis para o processamento da consulta
	protected ArrayList<String> initialFragments = null;
	protected ArrayList<String> virtualPartitioningResult = null;
	protected boolean newDocQuery;	// indica que está iniciando o processamento de uma nova sub-consulta doc() a partir de uma consulta de entrada collection()
	
	public boolean isNewDocQuery() {
		return newDocQuery;
	}

	public void setNewDocQuery(boolean newDocQuery) {
		this.newDocQuery = newDocQuery;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	public int getCardinalityOfElement() {
		return cardinalityOfElement;
	}

	public void setCardinalityOfElement(int cardinalityOfElement) {
		this.cardinalityOfElement = cardinalityOfElement;
	}
	
	public ArrayList<String> getInitialFragments() {
		return this.initialFragments;
	}
	
	public ArrayList<String> getvirtualPartitioningResult() {
		return this.virtualPartitioningResult;
	}

	public void addFragment(String fragment) {
		if (this.initialFragments == null)
			this.initialFragments = new ArrayList<String>();
		
		this.initialFragments.add(fragment);
	}

	protected int cardinalityOfElement; // cardinalidade do elemento utilizado para a fragmentação
	protected Hashtable<String,Hashtable<Integer,String>> selectionPredicates = new Hashtable<String,Hashtable<Integer,String>>(); ; // vetor com todos os predicados a serem adicionados para gerar os fragmentos virtuais.
	
	/* Vetor com todos os predicados já adicionados a consulta original. Utilizado quando há mais de um FOR sobre caminhos diferentes, 
	 * mas que contém o subcaminho utilizado para a fragmentação, a fim de evitar intervalos conflitantes e, consequentemente, 
	 * fragmentos cujo resultado é um conjunto vazio.*/	 
	protected Hashtable<String,Integer> addedPredicates = new Hashtable<String,Integer>(); ;  
	
	public Hashtable<String, Integer> getAddedPredicates() {
		return addedPredicates;
	}

	public void setAddedPredicates(String completePath, Integer elementCardinality) {
		if (this.addedPredicates==null)
			this.addedPredicates = new Hashtable<String, Integer>();
		
		this.addedPredicates.put(completePath,elementCardinality); //caminho completo que define a variável XML e cardinalidade do elemento usada para criar os fragmentos.
	}

	public Hashtable<String, Hashtable<Integer, String>> getSelectionPredicates() {
		return selectionPredicates;
	}
	
	public void setSubqueries(String varName, Hashtable<Integer,String> subqueries) {
		this.selectionPredicates.put(varName,subqueries);
	}

	public static SimpleVirtualPartitioning getUniqueInstance(boolean getUnique) throws IOException{		
		
		if (svp == null || !getUnique)
			svp = new SimpleVirtualPartitioning();
		
		return svp;
	}
	
	public String getSelectionPredicate(int index, String fragmentationVariable) {
				
		int fragmentSize = this.cardinalityOfElement / this.numberOfNodes;
		int resto = this.cardinalityOfElement % this.numberOfNodes;		
		String newPredicate = "";		
		Hashtable<Integer,String> predicates = new Hashtable<Integer, String>();		
				
		if ( index == - 1 && this.selectionPredicates.get(fragmentationVariable) == null ) { // Se índice igual a -1, sistema irá gerar os intervalos pela primeira vez.				
			
			if ( fragmentSize > 0 ) { // cardinalidade do elemento é maior que a quantidade de nós de processamento.
				
				for (int i = 1, j = 1; i < ( resto == 0 ? this.numberOfNodes + 1 : this.numberOfNodes ); i++, j = j + fragmentSize) {			
						
						if (fragmentSize > 1){
							newPredicate = "[position() >= " + j + " and position() < " + ((i * fragmentSize) + 1) + "]";							
						}
						else {
							newPredicate = "[position() = " + i * fragmentSize + "]";			
						}
						
						Integer ind = new Integer(i-1); 
						predicates.put(ind, newPredicate);
						
				} // fim for
				
				int lastPosition = ( fragmentSize * this.numberOfNodes ) + resto + 1;
				// se resto for diferente de zero, o último fragmento possui um intervalo maior que os demais 
				newPredicate = "[position() >= " + ( ( fragmentSize * (this.numberOfNodes - 1) ) + 1) 
			  	             + " and position() < " + lastPosition + "]";
				
				Integer ind = new Integer(this.numberOfNodes-1);
				predicates.put(ind, newPredicate);
				
			} // fim if
			
			else { // número de elementos é menor que o número de processadores. 
				
				for (int k = 1; k <= cardinalityOfElement; k++) {
					
					newPredicate = "[position() = " + k + "]";
					
					Integer ind = new Integer(k-1); 
					predicates.put(ind, newPredicate);
				} // fim for										
			}									
						
			selectionPredicates.put(fragmentationVariable, predicates);	
			return "";
		
		}
		else { // Sistema já conhece todos os intervalos, devolve apenas os predicados à medida que as sub-consultas são geradas
			
			predicates = selectionPredicates.get(fragmentationVariable);
			String IntervalPosition = predicates.get(index);
			return IntervalPosition;
		}		
		
	} // fim procedure
	
	public void addVirtualPredicates(String subPath, String variableName, Integer elementCardinality) throws IOException {
				
		Query q = Query.getUniqueInstance(true);
		ArrayList<String> fragments = svp.getInitialFragments();
		ArrayList<String> fragmentsBkp = new ArrayList<String>();				
		
		/** Predicados que definem os fragmentos **/
		SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(true);
		Hashtable<String,Hashtable<Integer,String>> predicates = svp.getSelectionPredicates();
		Hashtable<Integer,String> pred = predicates.get(variableName);		
		
		if (svp.isNewDocQuery()){
			fragments = null; // reinicializa o arrayList para não utilizar os predicados já adicionados da sub-consulta anterior.
		}

		for ( int j = 0; j < pred.size(); j++ ) {
			
			String predicate = pred.get(j);
			
			// se ainda nao adicionou fragmentos referentes a nenhum FOR anterior.						
			if ( fragments == null ) {								
				
				String virtualFragment = q.getInputQuery();		
				String completePath = virtualFragment.substring(virtualFragment.indexOf(variableName), virtualFragment.length());

				if (completePath.indexOf(subPath)>=0){
					
					completePath = completePath.substring(0, completePath.indexOf(subPath));
					completePath = completePath + subPath;
									
					virtualFragment = virtualFragment.replace(completePath, completePath + predicate);
					virtualFragment = (j) + virtualFragment;
					svp.addFragment(virtualFragment);
				}
				
			}
			else {													
				
				for ( String frag : fragments ) {
					String virtualFragment = frag;			
									
					String completePath = virtualFragment.substring(virtualFragment.indexOf(variableName), virtualFragment.length());					
					
					completePath = completePath.substring(0, completePath.indexOf(subPath));
					completePath = completePath + subPath;
					
					// Verifica se o caminho até o atributo de fragmentação já foi usado para geração de algum predicado de seleção.
					// Em caso afirmativo retorna a cardinalidade do atributo de fragmentação no documento, caso contrário retorna zero.
					Integer cardinality = pathAlreadyExists(virtualFragment);								
					
					// se nenhum predicado de seleção foi adicionado para o mesmo caminho xpath, adicione todos os intervalos
					if ( cardinality == 0 ) { 									
						
						virtualFragment = virtualFragment.replace(completePath, completePath + predicate);
						virtualFragment = (j) + virtualFragment;
						fragmentsBkp.add(virtualFragment);
					}
					else { // adicionar apenas os intervalos cujas sub-consultas geradas não produzem resultados vazios.
						/*ex.: for $x in doc('loja','informacoesLoja')/Loja/Itens/Item[position >= 1 and position < 18]
							   for $a in doc('loja','informacoesLoja')/Loja/Itens/Item[position >= 52 and position < 71]/ResenhaClientes
							   O caminho /Loja/Itens/Item já teve um predicado de seleção adicionado, logo o intervalo da variável
							   $a subsequente, deve adicionar os mesmo intervalos do predicado anterior e não predicados disjuntos,
							   caso a cardinalidade do elemento /Loja/Itens/Item seja a mesma para os dois FOR, o que indica que 
							   ambos se referem ao mesmo documento, ou geram fragmentos iguais.
					    */
						if ( Integer.toString(j).equals(virtualFragment.substring(0, 1)) ) {

							for (int m = 0; m < this.virtualPartitioningResult.size(); m++) {
								this.virtualPartitioningResult.remove(m);
							}
							
							SubQuery sbq = SubQuery.getUniqueInstance(true);
							for (int n = 0; n < sbq.getSubQueries().size(); n++) {
								sbq.getSubQueries().remove(n);
							}							
						}
															
						if ( cardinality == elementCardinality ) { // mesmo caminho e mesmo elemento para FORs diferentes na consulta																													
							
							if (virtualFragment.substring(0, 1).equals(Integer.toString(j))) {						
								virtualFragment = virtualFragment.replace(completePath, completePath + predicate);
								virtualFragment = (j) + virtualFragment;
								// Adicionar o predicado na posição correta. Como explicado no comentário acima.
								fragmentsBkp.add(virtualFragment);
							}
						}
						else {
							
							virtualFragment = virtualFragment.replace(completePath, completePath + predicate);
							virtualFragment = (j) + virtualFragment;						
							fragmentsBkp.add(virtualFragment);
						}
						
					}
				}	
			
			}
			
		} // fim for
			
		svp.setAddedPredicates(subPath, elementCardinality);
		fragments = fragmentsBkp;					
		
		if ( fragmentsBkp!=null && fragmentsBkp.size()>0 ) { // é diferente de null quando há mais de um FOR na consulta informada pelo usuário.
									
			this.RemoveIdentifiersFromFragments(fragmentsBkp);
				
		}
		else { // quando a consulta informada possui apenas um FOR
			
			this.RemoveIdentifiersFromFragments(this.getInitialFragments());
		
		}							
				
	}
	
	/**
	 * Função que retorna o caminho que caracteriza o elemento escolhido como atributo de fragmentação
	 * @param completePath String do FOR até o término do caminho Xpath do FOR que está sendo analisado.
	 * @return O caminho até o atributo de fragmentação
	 */
	public String getPathPredicate(String completePath) {
				
		int posParenthese = completePath.indexOf(")"); // Fim da definição do documento/coleção		
		String subPath = completePath.substring(posParenthese+1,completePath.length()); // caminho xpath	
		
		return subPath;
	
	}
	
	/**
	 * Função que retorna a cardinalidade do elemento que já teve o predicado adicionado, ou caso não encontre, retorna zero.
	 * @param forClausePath String do variável do FOR até o término do caminho Xpath do FOR que está sendo analisado.
	 * @return A cardinalidade do atributo de fragmentação ou zero, caso este atributo ainda não tenha sido usado em predicados anteriores.
	 */
	public Integer pathAlreadyExists(String forClausePath){
		
		Hashtable<String, Integer> addePredicates = this.getAddedPredicates(); // Obtém os predicados já adicionados a consulta original.
		Set<String> variableSet = addePredicates.keySet();		
		Iterator<String> iter = variableSet.iterator();	
			
		String pathAnalysed = getPathPredicate(forClausePath);		
		Integer cardinality = 0;
		
		while ( iter.hasNext() && cardinality == 0 ){ // enquanto houver predicados adicionados e nenhum deles for semelhante ao predicado atual, continue.	
			
			String completePath = iter.next().toString();  // CompletePath = $c in doc(doc,coll)/xpath
			String pathCompareTo = getPathPredicate(completePath); // GetPathPredicate retorna apenas a parte xpath de completePath passado como parâmetro.
			
			if ( pathCompareTo.indexOf("[") != -1 ) 
				pathCompareTo = pathCompareTo.substring(0, pathCompareTo.indexOf("["));		
			
			if ( pathCompareTo.indexOf(pathAnalysed) != -1 || pathAnalysed.indexOf(pathCompareTo) != -1 ) {				
				cardinality = addePredicates.get(completePath);
			}	
			
		}	
		
		return cardinality;
		
	}
	
	private void RemoveIdentifiersFromFragments(ArrayList<String> fragmentsList) throws IOException{
		
		SubQuery sbq = SubQuery.getUniqueInstance(true);
		
		if (fragmentsList != null) {
			
			for ( String frag: fragmentsList ){ 
				
				String strBeforeFor = "";
				if ( frag.toUpperCase().indexOf("FOR ") != -1 ) {
					// Obtém a string antes do primeiro FOR
					strBeforeFor = frag.substring(0, frag.toUpperCase().indexOf("FOR "));
				}	
				else if ( frag.toUpperCase().indexOf("LET ") != -1 ) {								
					// Obtém a string antes do primeiro LET
					strBeforeFor = frag.substring(0, frag.toUpperCase().indexOf("FOR "));							
				}
				
				if ( strBeforeFor.length() > 0 ) {
					// Retira todos os dígitos que indicam o índice do predicado, adicionado na consulta para excluir os fragmentos que geram resultado vazio.
					String strWithoutIndex = strBeforeFor.replaceAll("[0-9]+", "");
					// Substitui no fragmento inicial final
					frag = frag.replace(strBeforeFor, strWithoutIndex);
				}
				
				if (this.virtualPartitioningResult == null)
					this.virtualPartitioningResult = new ArrayList<String>();
				
				this.virtualPartitioningResult.add(frag);
				
				sbq.addFragment(frag);
			}
		}
		
	}
	
	public String getSelectionPredicateToCollection(String fragmentationVariable, String virtualAttribute, String mainQuery) throws IOException {
		
		int fragmentSize = this.cardinalityOfElement / this.numberOfNodes;
		int resto = this.cardinalityOfElement % this.numberOfNodes;		
		String newPredicate = "";		
		Hashtable<Integer,String> predicates = new Hashtable<Integer, String>();					
		String subQuery = "";
		String subQueryTmp = "";
		String endUnion = "";
		int posVar = -1;
		String replaceTo = "";
		String finalQuery = "";		
		
		if (!mainQuery.toUpperCase().contains("FOR $")) {
			if (fragmentationVariable.charAt(fragmentationVariable.length()-1) != ' ') { // se último caracter não for espaço, o usuário usou uma cláusula LET, cuja variável está imediatamente antes dos caracteres := 
				fragmentationVariable = fragmentationVariable + ":=";
				//fragmentationVariable = fragmentationVariable + " ";
			}
		}
		else {
			if (fragmentationVariable.charAt(fragmentationVariable.length()-1) != ' ') {
				fragmentationVariable = fragmentationVariable + " ";
			}
		}
		
		SubQuery sbq = SubQuery.getUniqueInstance(true);
		// Obtém o caminho até onde o elemento de fragmentação.
		posVar = mainQuery.indexOf(fragmentationVariable);
				
		subQuery = mainQuery.substring(posVar, mainQuery.length());
		
		endUnion = subQuery.substring(subQuery.indexOf(")"),subQuery.length());
		
		if ( fragmentSize > 0 ) { // cardinalidade do elemento é maior que a quantidade de nós de processamento.
			
			for (int i = 1, j = 1; i < ( resto == 0 ? this.numberOfNodes + 1 : this.numberOfNodes ); i++, j = j + fragmentSize) {			
					
					subQueryTmp = subQuery;
					if (fragmentSize > 1){
						newPredicate = "[position() >= " + j + " and position() < " + ((i * fragmentSize) + 1) + "]";							
					}
					else {
						newPredicate = "[position() = " + i * fragmentSize + "]";			
					}
					
					Integer ind = new Integer(i-1); 
					predicates.put(ind, newPredicate);
			
					replaceTo = ")" + newPredicate + " " + endUnion.substring(1,endUnion.length());
					subQueryTmp = subQueryTmp.replace(endUnion, replaceTo);					
					
					finalQuery = mainQuery;
					finalQuery = finalQuery.replace(subQuery, subQueryTmp) ;
					
					this.addFragment(finalQuery);	
					sbq.addFragment(finalQuery);
					
			} // fim for
			
			if ( resto > 0) {
				
				subQueryTmp = subQuery;
				int lastPosition = ( fragmentSize * this.numberOfNodes ) + resto + 1;
				// se resto for diferente de zero, o último fragmento possui um intervalo maior que os demais 
				newPredicate = "[position() >= " + ( ( fragmentSize * (this.numberOfNodes - 1) ) + 1) 
			  	             + " and position() < " + lastPosition + "]";
				
				Integer ind = new Integer(this.numberOfNodes-1);
				predicates.put(ind, newPredicate);
				
				replaceTo = ")" + newPredicate + " " + endUnion.substring(1,endUnion.length());
				subQueryTmp = subQueryTmp.replace(endUnion, replaceTo);					
									
				finalQuery = mainQuery;
				finalQuery = finalQuery.replace(subQuery, subQueryTmp) ;
				
				this.addFragment(finalQuery);	
				sbq.addFragment(finalQuery);
			}
			
		} // fim if
		
		else { // número de elementos é menor que o número de processadores. 
			
			for (int k = 1; k <= cardinalityOfElement; k++) {
				
				subQueryTmp = subQuery;
				newPredicate = "[position() = " + k + "]";
				
				Integer ind = new Integer(k-1); 
				predicates.put(ind, newPredicate);
				
				replaceTo = ")" + newPredicate + " " + endUnion.substring(1,endUnion.length());
				subQueryTmp = subQueryTmp.replace(endUnion, replaceTo);					
									
				finalQuery = mainQuery;
				finalQuery = finalQuery.replace(subQuery, subQueryTmp) ;
				
				this.addFragment(finalQuery);
				sbq.addFragment(finalQuery);

			} // fim for										
		}	
					
		selectionPredicates.put(fragmentationVariable, predicates);			
		return "";
		
				
		
	} // fim procedure	
	
}
