package classePrincipal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import javax.xml.xquery.XQException;

import mediadorxml.database.Database;
import mediadorxml.database.DatabaseFactory;

public class FinalResult {

	public static String getFinalResult(int totalInstances) throws IOException {
		
		SubQuery sbq = SubQuery.getUniqueInstance(true);
		
		String finalResultXquery = "";
		String orderByClause = "";
		String variableName = "$ret";
		
		Query q = Query.getUniqueInstance(true);
		
		//System.out.println("FINAL RESULT -- ORDER BY::"+q.getOrderBy());
		
		if ( q.getAggregateFunctions() != null && q.getAggregateFunctions().size() > 0 ) { // possui funcoes de agregacao na clausula LET.
			
			finalResultXquery = sbq.getConstructorElement() + "{ \r\n"
			+	" let $c:= collection('"+StaticInfo.TEMP_DB_COLLECTION_NAME+"')/partialResult/" + sbq.getConstructorElement().replaceAll("[</>]", "") + "\r\n"
			//+   " where $c/element()/name()!='idOrdem'" 
			+   " \r\n return \r\n\t" + "<" + sbq.getElementAfterConstructor().replaceAll("[</>]", "") + ">";

			Set<String> keys = q.getAggregateFunctions().keySet();
			for (String function : keys) {
				String expression = q.getAggregateFunctions().get(function);
				String elementsAroundFunction = "";
				
				if ( expression.indexOf(":") != -1 ){
					elementsAroundFunction = expression.substring( expression.indexOf(":")+1, expression.length());
					expression = expression.substring(0, expression.indexOf(":"));					
				}
				
				if ( elementsAroundFunction.indexOf("/") != -1 ) { // o elemento depois do return possui sub-elementos.
					//elementsAroundFunction = elementsAroundFunction.substring(elementsAroundFunction.indexOf("/")+1, elementsAroundFunction.length());
					String[] elm = elementsAroundFunction.split("/");			
					
					for (String openElement : elm) {
						//System.out.println("FinalResult.getFinalResult():"+elm.length+","+sbq.getElementAfterConstructor().replaceAll("[</>]", "") +",el:"+openElement);		
						
						if ( !openElement.equals("") && !openElement.equals(sbq.getElementAfterConstructor().replaceAll("[</>]", "")) )  {
							//System.out.println("FinalResult.getFinalResult(); armazenar o el:::"+openElement);
							finalResultXquery = finalResultXquery + "\r\n\t\t" + " <" + openElement + ">";
						}
					}
				
					elm = elementsAroundFunction.split("/");
					String subExpression = expression.substring(expression.indexOf("$"), expression.length());
					//System.out.println("FinalResult.getFinalResult(); subExpression o el:::"+subExpression);
					
					if (subExpression.indexOf("/")!=-1) { // agregacao com caminho xpath. Ex.: count($c/total)
						subExpression = subExpression.substring(subExpression.indexOf("/")+1, subExpression.length());
						//System.out.println("FinalResult.getFinalResult(); depois alterar o el:::"+subExpression+",el:"+elementsAroundFunction);
						expression = expression.replace("$c/"+subExpression, "$c/"+elementsAroundFunction+")");
						
						//System.out.println("FinalResult.getFinalResult(); depois alterar o expression:::"+expression);
						
					}
					else { // agregacao sem caminho xpath. Ex.: count($c)
						expression = expression.replace("$c", "$c/" + elementsAroundFunction);
					}
					
					if (expression.indexOf("count(") >=0){
						expression = expression.replace("count(", "sum("); // pois deve-se somar os valores j� previamente computados nos resultados parciais.
					}
					
					finalResultXquery = finalResultXquery + "{ " + expression + "}";
					
					for (int i = elm.length-1; i >= 0; i--) {
						String closeElement = elm[i];
						
						if ( !closeElement.equals("") && !closeElement.equals(sbq.getElementAfterConstructor().replaceAll("[</>]", "")) )  {
							//System.out.println("FinalResult.getFinalResult(); armazenar o el:::"+closeElement);
							finalResultXquery = finalResultXquery + "\r\n\t\t" + " </" + closeElement + ">";
						}						
					}					
					
				}			
				else { // apos o elemento depois do return estah a funcao de agregacao. ex.: return <resp> count($c) </resp> 
					elementsAroundFunction = "";
					expression = expression.replace("$c)", "$c/" + sbq.elementAfterConstructor.replaceAll("[</>]", "")+")");
					//System.out.println("FinalResult.getFinalResult(); entrei!!!!!!!!!!!"+expression+","+sbq.getElementAfterConstructor());
					
					String subExpression = expression.substring(expression.indexOf("$"), expression.length());
					
					if (subExpression.indexOf("/")!=-1) { // agregacao com caminho xpath. Ex.: count($c/total)
						subExpression = subExpression.substring(subExpression.indexOf("/")+1, subExpression.length());
						//System.out.println("FinalResult.getFinalResult(); depois alterar o el:::"+subExpression+",el:"+elementsAroundFunction);
						expression = expression.replace("$c/"+subExpression, "$c/"+sbq.elementAfterConstructor.replaceAll("[</>]", "")+")");
						
						//System.out.println("FinalResult.getFinalResult(); depois alterar o expression:::"+expression);
						
					}
					else { // agregacao sem caminho xpath. Ex.: count($c)
						expression = expression.replace("$c", "$c/" + sbq.elementAfterConstructor.replaceAll("[</>]", ""));
					}
					
					if (expression.indexOf("count(") >=0){
						expression = expression.replace("count(", "sum("); // pois deve-se somar os valores j� previamente computados nos resultados parciais.
					}
					
					finalResultXquery = finalResultXquery + "{ " + expression + "}";
				}
				
				/*System.out.println("FinalResult.getFinalResult(), EXPRESSION:"+expression + ", ELEROUND:"+elementsAroundFunction);
				finalResultXquery = finalResultXquery + "\r\n\t\t" + "{ <" + elementsAroundFunction + ">" 
				                    + "\r\n\t" + expression + "} </" + elementsAroundFunction + "> ";  */
				
			} // fim for
			
			finalResultXquery = finalResultXquery + "\r\n\t" + sbq.elementAfterConstructor.replace("<", "</")
            + " } " + sbq.getConstructorElement().replace("<", "</");
			
			//System.out.println("FinalResult.getFinalResult(): consulta final eh:"+finalResultXquery);
			
			
		}
				
		else if (!q.getOrderBy().trim().equals("")) { // se a consulta original possui order by, acrescentar na consulta final o order by original.
			
			String[] orderElements = q.getOrderBy().trim().split("\\$");			
			for (int i = 0; i < orderElements.length; i++) {
				String subOrder = ""; // caminho apos a definicao da variavel. Ex.: $order/shipdate. subOrder recebe shipdate.
				int posSlash = orderElements[i].trim().indexOf("/");
				
				if ( posSlash != -1 ) {
					subOrder = orderElements[i].trim().substring(posSlash+1, orderElements[i].length());					
					if (subOrder.charAt(subOrder.length()-1) == '/'){
						subOrder = subOrder.substring(0, subOrder.length()-1);
					}
				}
				
				if ( !subOrder.equals("") ) {
					orderByClause = orderByClause + (orderByClause.equals("")?"": ", ") + variableName + "/" + subOrder;
				}
			}
						
			finalResultXquery = sbq.getConstructorElement() + " {  " 
			  + " for $ret in collection('"+StaticInfo.TEMP_DB_COLLECTION_NAME+"')/partialResult/" 
			  + sbq.getConstructorElement().replaceAll("[</>]", "") + "/" + sbq.getElementAfterConstructor().replaceAll("[</>]", "")					          
	          + " order by " + orderByClause
	          + " return $ret"
	          + " } "
	          + sbq.getConstructorElement().replace("<", "</");  

			
			//System.out.println("finalresult.java:"+ finalResultXquery);
		}
		else { // se a consulta original nao possui order by, acrescentar na consulta final a ordenacao de acordo com a ordem dos elementos nos documentos pesquisados.
			orderByClause = "$ret/idOrdem";
			
			finalResultXquery = sbq.getConstructorElement() + " {  "
							  +	" for $ret in collection('"+StaticInfo.TEMP_DB_COLLECTION_NAME+"')/partialResult" 
					          + " let $c:= $ret/" + sbq.getConstructorElement().replaceAll("[</>]", "") + "/element()" // where $ret/element()/name()!='idOrdem'"
					          + " order by " + orderByClause + " ascending"
					          + " return $c" 
					          + " } "
					          + sbq.getConstructorElement().replace("<", "</"); 

			//System.out.println("finalresult.java:"+ finalResultXquery);
		}
		
		/*ExecucaoConsulta exec = new ExecucaoConsulta();
		String finalResult = exec.executeQuery(finalResultXquery);*/
	
		/* MODIFICADO PARA OBTER OS RESULTADOS PARCIAIS DE CADA INSTÂNCIA */
		Database[] databases = DatabaseFactory.getAllDatabases();

		try {
            StringBuilder sb = new StringBuilder();
            for (Database db: databases) {
                String partialResult = db.executeQueryAsString(finalResultXquery);
                sb.append(partialResult);
            }
            FileWriter fw = new FileWriter(StaticInfo.FINAL_RESULT_FILEPATH);
            fw.write(sb.toString());
            fw.close();
            
            if ( q.getAggregateFunctions() != null && q.getAggregateFunctions().size() > 0 ) { // Possui funcoes de agregacao no resultado.
            	
                Database localDb = DatabaseFactory.getLocalDatabase();
                localDb.deleteCollection(StaticInfo.TEMP_DB_COLLECTION_NAME);
                localDb.createCollection(StaticInfo.TEMP_DB_COLLECTION_NAME);
                
                localDb.loadFileInCollection(StaticInfo.TEMP_DB_COLLECTION_NAME, StaticInfo.FINAL_RESULT_FILEPATH);
                
                return localDb.executeQueryAsString(finalResultXquery);
            }
            else {
            	return sb.toString();
            }
        } catch (XQException e) {
            throw new IOException(e);
        }		
	}
}
