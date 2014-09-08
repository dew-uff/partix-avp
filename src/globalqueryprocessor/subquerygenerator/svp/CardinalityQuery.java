package globalqueryprocessor.subquerygenerator.svp;

public class CardinalityQuery {
	
	protected String xquery; // Xquery a ser utilizada para buscar a cardinalidade de um elemento específico na base de dados.

	public String getXquery() {			
		return xquery;
	}

	public void setXquery(String xquery) {
		this.xquery = xquery;
	}

	public String getFormattedQuery(String documentName, String ElementName){
		String xqueryReturn = this.xquery;
		xqueryReturn = xqueryReturn.replace("#", documentName); // adiciona o nome do documento
		xqueryReturn = xqueryReturn.replace("?", ElementName);  // adiciona o nome do elemento	
		return xqueryReturn;
	}	
	
}
