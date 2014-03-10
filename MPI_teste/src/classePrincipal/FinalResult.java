package classePrincipal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.Vector;

import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

public class FinalResult {

	public static String getFinalResult(int totalInstances, String basepath) throws IOException, DriverException{
		
		SubQuery sbq = SubQuery.getUniqueInstance(true);
		
		String finalResultXquery = "";
		String orderByClause = "";
		String variableName = "$ret";
		
		Query q = Query.getUniqueInstance(true);
		
		//System.out.println("FINAL RESULT -- ORDER BY::"+q.getOrderBy());
		
	if ( q.getAggregateFunctions() != null && q.getAggregateFunctions().size() > 0 ) { // possui funcoes de agregacao na clausula LET.
			
			finalResultXquery = sbq.getConstructorElement() + "{ \r\n"
			+	" let $c:= collection('tmpResultadosParciais')/partialResult/" + sbq.getConstructorElement().replaceAll("[</>]", "") + "\r\n"
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
			  + " for $ret in collection('tmpResultadosParciais')/partialResult/" 
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
							  +	" for $ret in collection('tmpResultadosParciais')/partialResult" 
					          + " let $c:= $ret/" + sbq.getConstructorElement().replaceAll("[</>]", "") + "/element()" // where $ret/element()/name()!='idOrdem'"
					          + " order by " + orderByClause + " ascending"
					          + " return $c" 
					          + " } "
					          + sbq.getConstructorElement().replace("<", "</"); 

			//System.out.println("finalresult.java:"+ finalResultXquery);
		}
		
		/*ExecucaoConsulta exec = new ExecucaoConsulta();
		String finalResult = exec.executeQuery(finalResultXquery);*/
	
	
	/* MODIFICADO PARA OBTER OS RESULTADOS PARCIAIS DE CADA INST�NCIA */
		
	String retorno = "";
	String path = basepath + "/finalResult/xqueryAnswer.xml";
	String retornoComplete = "";
		
	String[] allHosts = getAllHosts();

	for(String host: allHosts) {
		
			System.out.println("Getting partial from: " + host);
			try {
			ConnectionSedna con = new ConnectionSedna();
			SednaConnection scon = con.establishSednaConnection(host + ":5050", "xmark"); // con.establishSednaConnection("localhost", "examplesdb");
			scon.begin();
			
			SednaStatement st = scon.createStatement();
			scon.setDebugMode(true);
			
			//System.out.println("Finalresult.java xquery:"+ finalResultXquery);
			boolean res = st.execute(finalResultXquery);
						
			if ( res ) {
				SednaSerializedResult rs = st.getSerializedResult();				
				//System.out.println("Subquery.executeSubQuery: - rs.next():" + rs.next());
				//String resp = rs.next();
				//System.out.println("SubQuery.executeSubQuery():retorno 11="+retorno+".");
				String item;          
				retorno = new String();           
								
				while ((item = rs.next()) != null) {              
					retorno = retorno+"\n"+item;          
				}  
				
				retornoComplete = retornoComplete + retorno;
				FileWriter f = new FileWriter(path,true);			
		        f.write(retorno);
		        f.close();	        
		        
				/*while ( (resp = rs.next()) != null ) {				
					//retorno = retorno + rs.next();
					retorno = retorno + resp;
					System.out.println("SubQuery.executeSubQuery():retorno="+retorno+".");
				}*/
				
				//System.out.println("Subquery.executeSubQuery: - document:"  + "\r\n" + retorno + ".");
			}
		
			scon.commit();			
			scon.close();
			
			} catch (DriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		System.out.println("Adding all partial results to temp collection");
			if ( q.getAggregateFunctions() != null && q.getAggregateFunctions().size() > 0 ) { // Possui funcoes de agregacao no resultado.
							
				sbq.deleteCollection("00");
				
				ConnectionSedna con = new ConnectionSedna();
				SednaConnection scon = con.establishSednaConnection("localhost:5050", "xmark"); // con.establishSednaConnection("localhost", "examplesdb");
				SednaStatement st = scon.createStatement();
				
				// Verifica se a cole��o j� existe.
				ExecucaoConsulta exec = new ExecucaoConsulta();			
				//System.out.println("SubQuery.storeXMLDocumentIntoCollection(): runningSubqueries:" + sbq.isRunningSubqueries());
				
				
					if (exec.executeQuery("for $col in doc('$collections')/collections/collection/@name=\'tmpResultadosParciais' return $col", "32").equals("true")){				
						// Apagar a cole��o caso exista.				
						st.execute("DROP COLLECTION 'tmpResultadosParciais'");

						// Criar a cole��o
						st.execute("CREATE COLLECTION 'tmpResultadosParciais'");
					}
					else {
						System.out
								.println("SubQuery.storeXMLDocumentIntoCollection():criando cole��o");
						// Criar a cole��o
						st.execute("CREATE COLLECTION 'tmpResultadosParciais'");
					}
						
						
				
				// Armazenar documento na cole��o tempor�ria 
				st.execute("LOAD '" + path.replace("\\", "/") + "' 'xqueryAnswer.xml' 'tmpResultadosParciais'");
				
				scon.commit();
				
				boolean res = st.execute(finalResultXquery);
				if ( res ) {
					SednaSerializedResult rs = st.getSerializedResult();				
					
					String item;          
					retorno = new String();            
					
					while ((item = rs.next()) != null) {              
						retorno = retorno+"\n"+item;          
					}				
					
					//System.out.println("Subquery.executeSubQuery: - document:"  + "\r\n" + retorno + ".");
				}			
				
				scon.close();
				
				return retorno;
			}
			else {
				return retornoComplete;
			}		
		
	}
	
	private static String[] getAllHosts() {
		
		Vector<String> hosts = new Vector<String>();
        try {
        	BufferedReader br = new BufferedReader(new FileReader("machines"));

            String line = null;

            while ((line = br.readLine()) != null) {
                hosts.add(line.trim());
            }
            
            br.close();
        } 
        catch (Exception e) {
        	e.printStackTrace();
        }
		
		return hosts.toArray(new String[0]);
	}
}
