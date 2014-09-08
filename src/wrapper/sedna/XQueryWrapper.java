package wrapper.sedna;

import java.io.IOException;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
//
//import org.exist.xmldb.DatabaseImpl;
//import org.exist.xmldb.XQueryService;
//import org.xmldb.api.DatabaseManager;
//import org.xmldb.api.base.Collection;
//import org.xmldb.api.base.CompiledExpression;
//import org.xmldb.api.base.Database;
//import org.xmldb.api.base.Resource;
//import org.xmldb.api.base.ResourceIterator;
//import org.xmldb.api.base.ResourceSet;
//import org.xmldb.api.base.XMLDBException;

public class XQueryWrapper extends XQueryWrapperBase {
	
	//static private final Logger logger = Logger.getLogger(XQueryWrapper.class);
	public XQueryWrapper(){}
	
	public XQueryResult executeXQuery(final String query) {
		
		System.out.println("Entrou em executeXQuery");
		//long logTime = System.nanoTime();
		long logTime = System.currentTimeMillis();
				
		XQResultSequence xqr = null;
		XQExpression xqe = null;
		XQConnection xqc = null;
		
		String queryStr;
		String retorno = "";
		final XQueryResult result = new XQueryResult();
		//Iniciando a compilacao
		long startTime = System.currentTimeMillis();
		long compileTime = 0;
		long localTime =0;
		
		try{
		    ConnectionSedna con = new ConnectionSedna();
		    try {
				xqc = con.establishSednaConnection();
				System.out.println("conexao estabelecida");
			} catch (IOException e) {
				System.out.println("conexao nao estabelecida");
				e.printStackTrace();
			}			
		    xqe = xqc.createExpression();	
		    System.out.println ("Consulta testada" +query);
		    
		    queryStr = this.updateViewLocation(query);
		    System.out.println( "testando....." +queryStr);
			//System.out.println("apos updateViewLocation - " + (System.nanoTime() - logTime)/1000000);
			
			// Inclusão de nodo root na query
			queryStr = "<root>{ " + queryStr + " }</root>";
			
			//Finalizando o tempo de compilacao da consulta
			compileTime = (System.currentTimeMillis() - startTime);
			System.out.println("Imprimindo o tempo de compilacao local"+compileTime);
			
			
			System.out.println( "query final" +queryStr);
			
			//Inicializando a execucao local da consulta
			startTime = System.currentTimeMillis();
	        xqr = xqe.executeQuery(queryStr);
			//System.out.println("validando o retorno do xqr"+xqr);
	        
	        if (!xqr.next()){				
	        	result.setResult("");
			}			
			
			do {
				retorno += xqr.getItemAsString(null);	
				
			} while (xqr.next());
			
					
			System.out.println(retorno);			
			
			xqc.close();
			result.setResult(retorno);
			//Finalizando a execucao local
			localTime = System.currentTimeMillis() - startTime;
			System.out.println("Imprimindo o tempo de execucaoo local"+localTime);
			result.setTimeMsLocal(localTime);
			result.setSuccess(true);
	        result.setTimeMsCompile(compileTime);
			result.setTimeMsRemote(0); // A query é executada totalmente local
	        result.setTimeMsCommunicRemote(0);
	        result.setTotalBytes(result.getResult().getBytes().length);
	        result.setNumberQueriesExecuted(1);
			
		} catch (XQException e) {
			System.out.println("Erro ao executar XQuery.");
			result.setResult(e.getMessage());
	        result.setSuccess(false);
	        result.setTimeMsCompile(compileTime);
	        result.setTimeMsLocal(localTime);
	        result.setTimeMsRemote(0);
	        result.setTimeMsCommunicRemote(0);
	        result.setTotalBytes(result.getResult().getBytes().length);
	        result.setNumberQueriesExecuted(1);
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if (xqr!=null) xqr.close();			
				if (xqe!=null) xqe.close();			
				if (xqc!=null) xqc.close();
				
			} catch (Exception e2) {
				System.out.println("Erro ao fechar conexão");
				e2.printStackTrace();
				return null;
			}
		}
	
		return result;
	}

}
