package classePrincipal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.xquery.XQException;

import mediadorxml.database.Database;
import mediadorxml.database.DatabaseFactory;


public class SubQuery {

	protected static SubQuery sbq;
	protected ArrayList<String> subqueries = null;
	protected boolean sameQuery; // indica se as sub-consultas pertencem a mesma query original. Usada para as sub-consultas geradas a partir de cole��es.
	protected String constructorElement = ""; // define o elemento especificado pelo usu�rio para a estrutura da resposta. Ex.: <results><el1></el1>...</results>. Neste caso, o elemento � <results>.
	protected String elementAfterConstructor = ""; // define o elemento ap�s o construtor para a estrutura da resposta. Ex.: <results><order><el1></el1>...</order></results>. Neste caso, o elemento � <order>.
	protected boolean runningSubqueries;
	protected String docIdentifier = null; // usado para identificar o documento antes de armazena-lo na colecao de resultado para os casos em que as sub-consultas nao foram fragmentadas, e sao apenas consultas a documentos.
	private boolean updateOrderClause = true; // usado para identificar se h� elementos em torno do elemento do order by e acertar a cl�usula que ser� utilizada no retorno.
	protected long startTime = 0;
	
	protected synchronized long getStartTime() {
		return startTime;
	}

	protected synchronized void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	protected int finishedThreads = 0; 
	
	protected long getEndTime() {
		return endTime;
	}

	protected void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	protected long endTime = 0;
	
	protected synchronized int getFinishedThreads() {
		return finishedThreads;
	}

	private synchronized void setFinishedThreads(int finishedThreads) {
		this.finishedThreads = finishedThreads;
	}

	public boolean isUpdateOrderClause() {
		return updateOrderClause;
	}

	private void setUpdateOrderClause(boolean updateOrderClause) {
		this.updateOrderClause = updateOrderClause;
	}

	public String getElementAfterConstructor() {
		return elementAfterConstructor;
	}

	public void setElementAfterConstructor(String elementAfterConstructor) {
		this.elementAfterConstructor = elementAfterConstructor;
	}

	private String getDocIdentifier() {
		
		if ( docIdentifier == null ){
			docIdentifier = "0";
		}
		
		return docIdentifier;
	}

	public void setDocIdentifier(String docIdentifier) {
		this.docIdentifier = docIdentifier;
	}

	public boolean isRunningSubqueries() {
		return runningSubqueries;
	}

	public void setRunningSubqueries(boolean runningSubqueries) {
		this.runningSubqueries = runningSubqueries;
	}

	public String getConstructorElement() {
		return constructorElement;
	}

	public void setConstructorElement(String constructorElement) {
		this.constructorElement = constructorElement;
	}
	
	public boolean isSameQuery() {
		return sameQuery;
	}

	public void setSameQuery(boolean sameQuery) {
		this.sameQuery = sameQuery;
	}

	public static SubQuery getUniqueInstance(boolean getUnique) throws IOException{		
		
		if (sbq == null || !getUnique) {
			sbq = new SubQuery();
			sbq.setFinishedThreads(0);
		}
		
		return sbq;
	}
	
	public void addFragment(String fragment) {
		if (this.subqueries == null)
			this.subqueries = new ArrayList<String>();
		
		this.subqueries.add(fragment);
	}
	
	public ArrayList<String> getSubQueries(){
		return this.subqueries;
	}
	
	/* Metodo para execucao das sub-consultas geradas na fragmentacao virtual simples */
	public static void executeSubQuery(String xquery, String threadId) {
		
		String retorno = "";
		
		try {
	        Database db = DatabaseFactory.getLocalDatabase();

            long starttime = System.nanoTime();     

	        retorno = db.executeQueryAsString(xquery);
	        
			Query q = Query.getUniqueInstance(true);
			SubQuery sbq = SubQuery.getUniqueInstance(true);		
				
			// Se nao tiver retornado resultado algum, o �nico elemento retornado ser� o constructorElement. Nao gerar XML, pois n�o h� resultados.			
			if ( retorno.trim().lastIndexOf("<") != -1 ) {					
		
				sbq.setConstructorElement(getConstructorElement(retorno)); // Usado para a composicao do resultado final.
				
				String intervalBeginning = getIntervalBeginning(xquery);
				
				if ( sbq.getElementAfterConstructor().equals("") ) {
					sbq.setElementAfterConstructor(getElementAfterConstructorElement(retorno, sbq.getConstructorElement()));
				}
					
				if (sbq.isUpdateOrderClause()) {
					getElementsAroundOrderByElement(xquery, sbq.getElementAfterConstructor());
				}
					
				if (!q.isOrderByClause()) { // se a consulta original nao possui order by adicione o elemento idOrdem
					storeResultInXMLDocument(SubQuery.addOrderId(retorno, intervalBeginning), intervalBeginning, threadId);
				}
				else { // se a consulta original possui order by apenas adicione o titulo do xml.
					retorno = getTitle() + "<partialResult>\r\n" + retorno + "\r\n</partialResult>";
					storeResultInXMLDocument(retorno, intervalBeginning, threadId);
				}				
			}
            long delay = (System.nanoTime()-starttime) / 1000000;
				
		} catch (XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	public static String addOrderId(String originalPartialResult, String intervalBeginning){
		
		return getTitle() + " <partialResult> \r\n" 
			              + originalPartialResult + "\r\n"
			              + " <idOrdem>" + intervalBeginning + "</idOrdem> \r\n"
			              + " </partialResult>";
		
	    
	}
	
	public static String getTitle(){
		return "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n";
	}	
	
	public static synchronized void deleteFilesFromDirectory(){
		
		String basePath = StaticInfo.RESULTS_PATH;
		
		File folder = new File(basePath);
	    File[] listOfFiles = folder.listFiles();

	    for ( int i = 0; i < listOfFiles.length; i++ ) {
	      
	      if ( listOfFiles[i].isFile() ) {	        
	    	  // apagar o arquivo se existir
	    	  if (listOfFiles[i].exists()) {
	    		  listOfFiles[i].delete();
			    }
	      } 
	      
	    } // fim for

	}
	
	public static synchronized void deleteCollection(String threadId){
		
	    try {
            Database database = DatabaseFactory.getLocalDatabase();
            database.deleteCollection(StaticInfo.TEMP_DB_COLLECTION_NAME);
            // TODO verify this. shouldn't be recreating the collection
            database.createCollection(StaticInfo.TEMP_DB_COLLECTION_NAME);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XQException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	public static void storeResultInXMLDocument(String partialResult, String intervalBeginning, String threadId){
		
		//String absolutePathToXMLDocuments = "C:\\Users\\carla\\Desktop\\Desktop\\DissertacaoMestrado\\partialResults\\";
		String absolutePathToXMLDocuments = StaticInfo.PARTIAL_RESULTS_PATH;
		String fileName = "partialResult_intervalBeginning_"+ intervalBeginning + ".xml";
		String completeFileName = absolutePathToXMLDocuments + "/partialResult_intervalBeginning_"+ intervalBeginning + ".xml";
		
		try {
			
		    File file = new File(completeFileName);
		    
		    /*if (file.exists()) {
		    	file.delete();
		    }*/
		    
		    FileWriter fileWriter = new FileWriter(file);
		    PrintWriter output = new PrintWriter(fileWriter);
		    output.write(partialResult);		    
		    	    
		    if ( output!=null ){
		    	output.close();
		    }
		    
		    if ( fileWriter != null ){
		    	fileWriter.close();
		    	
		    	storeXMLDocumentIntoCollection(fileName, threadId);
		    }
		    
		} catch (IOException e) {			
			System.out.println("SubQuery.storeResultInXMLDocument: erro ao armazenar resultado parcial em documento XML.");
			e.printStackTrace();
		}		
	}
	
	/**
	 * Armazena os documentos que cont�m os resultados parciais na cole��o tempor�ria. 
	 * @throws IOException 
	 */
	private static synchronized void storeXMLDocumentIntoCollection(String fileName, String threadId) throws IOException{
				
		//String absolutePathToXMLDocuments = "C:\\Users\\carla\\Desktop\\Desktop\\DissertacaoMestrado\\partialResults\\";
		String absolutePathToXMLDocuments = StaticInfo.PARTIAL_RESULTS_PATH;
		//String absolutePathToXMLDocuments = "C:/Documents and Settings/Carla.UNIVERSI-C771D1/Meus documentos/UFRJ/Mestrado/PESC/DissertacaoMestrado/partialResults/";
		
		try {
			Database database = DatabaseFactory.getLocalDatabase();
			database.loadFileInCollection(StaticInfo.TEMP_DB_COLLECTION_NAME, absolutePathToXMLDocuments + "/" + fileName);
				
			int newFin= sbq.getFinishedThreads()+1;
			sbq.setFinishedThreads(newFin);
			
			//System.out.println("SubQuery.storeXMLDocumentIntoCollection(): finishedThreads" + sbq.getFinishedThreads());
				
		} catch (XQException e) {
			System.out.println("SubQuery.storeXMLDocumentIntoCollection: Erro ao efetuar upload do documento " 
					          + absolutePathToXMLDocuments.replace("\\", "/") + fileName + " para a cole��o tmpResultadosParciais.");
			e.printStackTrace();
		}
	}
	
	/***
	 * Utilizado para retornar os resultados das consultas com order by
	 * @param xqueryResult
	 * @param constructorElement
	 * @return
	 */
	public static String getElementAfterConstructorElement(String xqueryResult, String constructorElement) {
		
		String elementAfterConstructorElement = "";	
		
		constructorElement = constructorElement.replace("<", "</");
		int posConstructor = xqueryResult.indexOf(constructorElement);
				
		if ( posConstructor != -1 ) {
		
			String subpath = xqueryResult.substring(0, posConstructor);		
			int posLastEndTag = subpath.lastIndexOf("</");
			elementAfterConstructorElement = subpath.substring(posLastEndTag, subpath.lastIndexOf(">")+1);
			elementAfterConstructorElement = elementAfterConstructorElement.replace("</", "<");
		}
		
		//System.out.println("SubQuery.getElementAfterConstructorElement()"+elementAfterConstructorElement+".");
		return elementAfterConstructorElement;
	}
	
	/***
	 * Retorna os elementos em torno dos elementos especificados no order by.
	 * Ex.: <results> {
	 *      for $order ...
	 *      order by $order/ship_date
	 *      return <order>
	 *              <date>{ $order/ship_date }</date>
	 *             </order> }
	 *      </result>
	 * A fun��o retornaria date.   
	 * @param xqueryResult
	 * @param constructorElement
	 * @return
	 */
	public static void getElementsAroundOrderByElement(String xquery, String elementAfterConstructor) {
		
		String elementAroundOrderBy = "";		
		String completePath = "";
		
		Query q;
		SubQuery sbq;
		try {
			q = Query.getUniqueInstance(true);		
			sbq = SubQuery.getUniqueInstance(true);
			
			if (!q.getOrderBy().trim().equals("")) { // se a consulta original possui order by, acrescentar na consulta final o order by original.
				
				String originalClause = q.getOrderBy().trim();
				//System.out.println("getElementsAroundOrderByElement().inicio ="+originalClause+".");
				String[] orderElements = q.getOrderBy().trim().split("\\$");	
				
				String lastDeletedElement = "";
				for (int i = 0; i < orderElements.length; i++) {
					String subOrder = ""; // caminho apos a definicao da variavel. Ex.: $order/shipdate. subOrder recebe shipdate.
					int posSlash = orderElements[i].trim().indexOf("/");		
					
					subOrder = xquery.substring(xquery.indexOf(elementAfterConstructor),xquery.length()) ; // consome a string posterior ao primeiro elemento depois do construtor.
										
					if ( posSlash != -1 ) {
												
						if (orderElements[i].lastIndexOf('/') == orderElements[i].length()-1) { // se houver dois elementos na clausula order by, a string estar� como $order/ship_date/$order/@id, ao separar teremos $order/ship_date/, por isso, � necess�rio retirar a barra do final.
							orderElements[i] = orderElements[i].substring(0, orderElements[i].length()-1);
						}
						
						//System.out.println("getElementsAroundOrderByElement().orderElements[i]="+orderElements[i]+".");
						
						//System.out.println("getElementsAroundOrderByElement().subOrder -1="+subOrder+".");
						
						if (subOrder.trim().indexOf(elementAfterConstructor) !=-1 && subOrder.trim().indexOf(orderElements[i])!=-1) {
							subOrder = subOrder.trim().substring(subOrder.trim().indexOf(elementAfterConstructor), subOrder.trim().indexOf(orderElements[i])+1+orderElements[i].length()); // consome a parte posterior ao elemento do order by.
							
							//System.out.println("getElementsAroundOrderByElement().subOrder 0="+subOrder+".");
							if (subOrder.trim().indexOf(">")!=-1) {
								subOrder = subOrder.trim().substring(subOrder.trim().indexOf(">")+1, subOrder.trim().length()); // consome o elemento depois do construtor
							}
							
							//System.out.println("getElementsAroundOrderByElement().subOrder 1="+subOrder+".");
							if ( !lastDeletedElement.equals("") ) {
								
								if (lastDeletedElement.indexOf("/")!=-1){ // dois elementos. Ex.: date/sp
									
									String[] last = lastDeletedElement.split("/");
									lastDeletedElement = "";
									
									for (int j = last.length-1; j >=0; j--) {
										lastDeletedElement = lastDeletedElement + "</" + last[j] + ">";									
									}
								}
								else {
									lastDeletedElement = "</" + lastDeletedElement + ">";
								}
								//System.out.println("getElementsAroundOrderByElement().lastDeletedElement 1="+lastDeletedElement+".");
								
								if (subOrder.trim().indexOf(lastDeletedElement)!=-1 && subOrder.trim().indexOf(orderElements[i])!=-1) { 
									subOrder = subOrder.trim().substring(subOrder.trim().indexOf(lastDeletedElement)+lastDeletedElement.length(), subOrder.trim().indexOf(orderElements[i])+1); // consome a parte posterior ao elemento do order by.
								}
							}
							
							//System.out.println("getElementsAroundOrderByElement().subOrder 2="+subOrder+".");
							
							
							if ( subOrder.trim().indexOf("<")  != -1 ) { // se houver elementos entre o elemento depois do construtor e o elemento do order by, obtenha-o.
								elementAroundOrderBy = subOrder.trim().substring(subOrder.trim().indexOf("<")+1, subOrder.trim().indexOf("{")); // obt�m os elementos antes do elemento do order by.Ex.:<date><sp>{$order/ship_date}</sp></date>. Retornaria date><sp>.
								//System.out.println("getElementsAroundOrderByElement().elementAroundOrderBy 1="+elementAroundOrderBy+".");
								elementAroundOrderBy = elementAroundOrderBy.replaceAll("[\r\n\t' '>]", ""); // consome ENTER, TAB, espa�o em branco e o caracter >.
								//System.out.println("getElementsAroundOrderByElement().elementAroundOrderBy 2="+elementAroundOrderBy+".");
								elementAroundOrderBy = elementAroundOrderBy.replaceAll("[<]", "/"); // Substitui os caracteres que separam os elementos de < por /.
								//System.out.println("getElementsAroundOrderByElement().elementAroundOrderBy 3="+elementAroundOrderBy+".");
								lastDeletedElement = elementAroundOrderBy;
								completePath =  '$'+ orderElements[i].substring(0, posSlash) + "/" + (elementAroundOrderBy) + "/" + orderElements[i].substring(posSlash+1,orderElements[i].length());							
								String tmp = '$'+ orderElements[i];
								
								//System.out.println("getElementsAroundOrderByElement().tmp="+tmp+"."+"completepath="+completePath+",elaround:"+elementAroundOrderBy);
								originalClause = originalClause.replace(tmp, completePath);
							}	
							
							
							//System.out.println("getElementsAroundOrderByElement().originalClause="+originalClause+".");					
						
							q.setOrderBy(originalClause);
						}
						
			
					}				
				}
				
				sbq.setUpdateOrderClause(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}

	
	public static String getIntervalBeginning(String xquery){
		
		int posPositionFunction = xquery.indexOf("[position() ");
		String intervalBeginning = "";
		
		if ( posPositionFunction != -1 ) { // houve fragmentacao, pois ha cardinalidade 1:N entre os elementos.
			
			String subXquery = xquery.substring(posPositionFunction, xquery.length());			
			int posEqualsSymbol = subXquery.indexOf("=");
			int finalIntervalSpecification = ( subXquery.indexOf(" and") == -1? subXquery.indexOf("]"): subXquery.indexOf(" and") ); 
			intervalBeginning = subXquery.substring(posEqualsSymbol+2, finalIntervalSpecification); // soma dois para suprimir o caracter = e o espaco em branco
		}
		else { // nao houve fragmentacao
			
			SubQuery sbq;
			try {
				
				sbq = SubQuery.getUniqueInstance(true);
				int docId = Integer.parseInt(sbq.getDocIdentifier());
				docId++;
				intervalBeginning = Integer.toString(docId);
				sbq.setDocIdentifier(Integer.toString(docId));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		//System.out.println("SubQuery.getIntervalBeginning():retorna:"+intervalBeginning);
		return intervalBeginning;
	}
	
	public static String getIntervalEnding(String xquery){
		
		int posPositionFunction = xquery.lastIndexOf("position() ");
		String intervalEnding = "";
		
		if ( posPositionFunction != -1 ) { // houve fragmentacao, pois ha cardinalidade 1:N entre os elementos.		
			String subXquery = xquery.substring(posPositionFunction, xquery.length());
			subXquery = subXquery.substring(0, subXquery.indexOf("]")+1);
			
			// se possui simbolo <, o fragmento tem tamanho maior que 1,caso contrario, � um fragmento unit�rio.
			int posSymbol = ( subXquery.indexOf("<") != -1? subXquery.indexOf("<"): subXquery.indexOf("=") );
			
			//System.out.println("SubQuery.getIntervalEnding():subquery:"+subXquery);
			//System.out.println("SubQuery.getIntervalEnding():possymbol:"+posSymbol);
			int finalIntervalSpecification = subXquery.indexOf("]");
			//System.out.println("SubQuery.getIntervalEnding():finalIntervalSpecification:"+finalIntervalSpecification);
			intervalEnding = subXquery.substring(posSymbol+2, finalIntervalSpecification);
		}		
		
		//System.out.println("SubQuery.getIntervalEnding():retorna:"+intervalEnding);
		return intervalEnding;
	}
	
	public static String getConstructorElement(String xquery){
		
		int posPositionLessThan = xquery.trim().indexOf("<");	
		//System.out.println("SubQuery.getConstructorElement():xquery="+xquery);
		String subXquery = xquery.substring(posPositionLessThan, xquery.length());
		int posPositionGreaterThan = xquery.trim().indexOf(">");	 
		String constructorElement = subXquery.substring(posPositionLessThan, posPositionGreaterThan+2);
		
		// Isso ocorre quando a consulta nao retorna elementos para o intervalo especificado, retornando apenas a tag do 
		// elemento construtor. Ex.: <results/>
		if (constructorElement.indexOf("/>") != -1) {
			constructorElement = constructorElement.replace("/>", ">");
		}
	
		//System.out.println("subquery.getConstructorElement:"+constructorElement + ", constructorElement.indexOf(/>):"+constructorElement.indexOf("/>"));
		return constructorElement;
	}
}
