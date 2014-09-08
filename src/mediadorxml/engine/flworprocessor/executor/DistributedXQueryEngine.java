package mediadorxml.engine.flworprocessor.executor;



import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import mediadorxml.algebra.basic.TreeNode;
//import mediadorxml.remotewrapper.XQueryResult;
import wrapper.sedna.XQueryResult;
import mediadorxml.remotewrapper.XQueryWrapperProxy;
import mediadorxml.subquery.SubQuery;
//import net.sf.saxon.Configuration;
//import net.sf.saxon.query.DynamicQueryContext;
//import net.sf.saxon.query.StaticQueryContext;
//import net.sf.saxon.query.XQueryExpression;
//import net.sf.saxon.trans.XPathException;

public class DistributedXQueryEngine {
	
	private ArrayList<SubQuery> subQueryList;
	private ArrayList<XQueryResult> resultList;
	private ArrayList<RemoteSubQueryExecutor> remoteExecutorList;
	
	static private final Logger logger = Logger.getLogger(DistributedXQueryEngine.class);
	
	public DistributedXQueryEngine(ArrayList<SubQuery> subQueryList){
		this.subQueryList = subQueryList;
		this.resultList = new ArrayList<XQueryResult>();
		this.remoteExecutorList = new ArrayList<RemoteSubQueryExecutor>();
	}

	public ArrayList<SubQuery> getSubQueryList() {
		return subQueryList;
	}
	
	public ArrayList<XQueryResult> getResultList() {
		return resultList;
	}

	public XQueryResult executeAll() throws RemoteException{
		
		final long startTime = System.nanoTime();
		//final long startTime = System.currentTimeMillis();
		
		long endRemoteTime = 0;
		long maxRemoteExecTime = 0;
		
		XQueryResult resultAll = new XQueryResult();
		
		if (this.subQueryList.size() == 0){
			resultAll.setResult("");
			resultAll.setSuccess(false);
			resultAll.setTotalBytes(0);
		}
		
		// Se existir apenas uma subQuery, o resultado é imediato
		else if (this.subQueryList.size() == 1){
			try{
				
				final SubQuery subQuery = this.subQueryList.get(0);
				int pos = subQuery.getExecutionSite().indexOf(":");
				String ip  = subQuery.getExecutionSite().substring(0,pos);
				//System.out.println("O ip da maquina eh:"+ip);
				int port = Integer.parseInt(subQuery.getExecutionSite().substring(pos+1));
				//System.out.println("A porta da maquina eh:"+port);
				RemoteSubQueryExecutor remoteExecutor = new RemoteSubQueryExecutor(subQuery,ip, port);
				remoteExecutor.start();
				remoteExecutor.join();
				XQueryResult remoteResult = remoteExecutor.getXqueryResult();
				this.resultList.add(remoteResult);
				resultAll = this.resultList.get(0);
				endRemoteTime = System.nanoTime();
				//endRemoteTime = System.currentTimeMillis();
				maxRemoteExecTime = remoteResult.getTimeMsLocal() + remoteResult.getTimeMsCompile();
			}
			catch(InterruptedException iexc){
				logger.error(iexc);
			}
		}
		
		// Existindo mais de uma subQuery, temos que executar primeiro as remotas e depois a local
		else{
			SubQuery sqMediator = null;
			
			// Execução das consultas remotas em paralelo
			for (int i=0; i<this.subQueryList.size(); i++){
				final SubQuery subQuery = this.subQueryList.get(i);
				if (subQuery.getExecutionSite().equals("MEDIATOR")){
					sqMediator = subQuery;
					this.resultList.add(null);
				}
				else{
					// Criação de uma thread do executor para a consulta remota
					int pos = subQuery.getExecutionSite().indexOf(":");
					String ip  = subQuery.getExecutionSite().substring(0,pos);
					
					int port = Integer.parseInt(subQuery.getExecutionSite().substring(pos+1));
					RemoteSubQueryExecutor remoteExecutor = new RemoteSubQueryExecutor(subQuery, ip, port);
					remoteExecutor.start();
					this.remoteExecutorList.add(remoteExecutor);					
				}
			}
			
			// Join das threads na thread atual e recuperação dos resultados obtidos
			for (int i=0; i<this.remoteExecutorList.size(); i++){
				
				RemoteSubQueryExecutor remoteExecutor = this.remoteExecutorList.get(i);
				
				try{
					remoteExecutor.join();
					
					// Recuperação do resultado da subQuery remota
					remoteExecutor.includeRootNodeInResult();
					final XQueryResult sqResult = remoteExecutor.getXqueryResult();			
					this.resultList.add(sqResult);
					if ((sqResult.getTimeMsLocal() + sqResult.getTimeMsCompile()) > maxRemoteExecTime){
						maxRemoteExecTime = sqResult.getTimeMsLocal() + sqResult.getTimeMsCompile();
					}
				}
				catch(InterruptedException iexc){
					logger.error(iexc);
				}				
			}
			endRemoteTime = System.nanoTime();
			//endRemoteTime = System.currentTimeMillis();
			
			// Execução da subQuery do Mediador (local)
			resultAll = this.executeMediatorSubQuery(sqMediator);
			
		}
		long totalRemoteTime = (endRemoteTime - startTime)/1000000;
		//long totalRemoteTime = (endRemoteTime - startTime);
		resultAll.setTimeMsCommunicRemote(totalRemoteTime - maxRemoteExecTime);
		resultAll.setTimeMsRemote(maxRemoteExecTime);
		resultAll.setTimeMsLocal((System.nanoTime() - endRemoteTime)/1000000);
		//resultAll.setTimeMsLocal(System.nanoTime() - endRemoteTime);
		//resultAll.setTimeMsLocal(System.currentTimeMillis() - endRemoteTime);
		resultAll.setNumberQueriesExecuted(this.subQueryList.size());
		
		
		return resultAll;
	}
	
	protected XQueryResult executeMediatorSubQuery(final SubQuery subQuery) {
		
		final XQueryResult xqueryResult = new XQueryResult();
		
		try{
			final Configuration config = new Configuration();
			
			// Contexto estático para compilação da XQuery
	        final StaticQueryContext staticContext = new StaticQueryContext(config);
	        String xqueryStr = subQuery.toString();
	        final XQueryExpression exp = staticContext.compileQuery(xqueryStr);
	        
	        // Contexto dinâmico para execução da XQuery
	        final DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
	        
	        // Carga das variáveis com os resultados das queries remotas em memória
	        for (int i=0; i<this.subQueryList.size(); i++){
	        	SubQuery remoteSubQuery = this.subQueryList.get(i);
				if (!remoteSubQuery.getExecutionSite().equals("MEDIATOR")){
					
					String varName = remoteSubQuery.getVarId();
					String xml = this.resultList.get(i).getResult();
					
					dynamicContext.setParameter(varName, staticContext.buildDocument(new StreamSource(new StringReader(xml))));
				}
	        }
	       
	        Properties props = new Properties();
	        props.setProperty(OutputKeys.METHOD, "html");
	        props.setProperty(OutputKeys.INDENT, "yes");
	        
	        StringWriter swriter = new StringWriter();
	        exp.run(dynamicContext, new StreamResult(swriter), props);
	        
	        xqueryResult.setResult(swriter.toString());
	        xqueryResult.setSuccess(true);
	        xqueryResult.setTotalBytes(xqueryResult.getResult().getBytes().length);
		}
	    catch (XPathException x){
	    	xqueryResult.setSuccess(false);
	    	xqueryResult.setResult(x.getMessage());
	    }	
        
        return xqueryResult;
	}
}
