//package mediadorxml.engine.flworprocessor.executor;

package mediadorxml.engine.flworprocessor.executor;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.io.*;

import org.apache.log4j.Logger;

//import mediadorxml.remotewrapper.XQueryResult;
import mediadorxml.remotewrapper.XQueryWrapperProxy;
import mediadorxml.subquery.SubQuery;
import wrapper.sedna.XQueryResult;

public class RemoteSubQueryExecutor extends Thread {
	
	private XQueryResult xqueryResult;
	private SubQuery remoteQuery;
	private int port;
	private String ip;
	private Socket connection = null;
    private DataInputStream  input;  
    private DataOutputStream  output;
	
	static final Logger logger = Logger.getLogger(RemoteSubQueryExecutor.class);

	public RemoteSubQueryExecutor(SubQuery remoteQuery,String ip, int port){
		this.remoteQuery = remoteQuery;
		this.ip = ip;
		this.port = port;
			}
	
	public SubQuery getRemoteQuery() {
		return remoteQuery;
	}

	public void setRemoteQuery(SubQuery remoteQuery) {
		this.remoteQuery = remoteQuery;
	}

	public XQueryResult getXqueryResult() {
		return xqueryResult;
	}

	public void setXqueryResult(XQueryResult xqueryResult) {
		this.xqueryResult = xqueryResult;
	}

	/**
	 * Execução da thread
	 */
	public void run(){
		
		
		try{
			this.xqueryResult = this.executeRemoteSubQuery();
		}
		catch(RemoteException remExc){
			logger.error(remExc);
		}
	}
	
	
	
	private XQueryResult executeRemoteSubQuery() throws RemoteException{
		//Estabelecendo conexao com o servidor
		XQueryResult resultadoLocal = new XQueryResult ();
		try {
			resultadoLocal = connection(remoteQuery,ip, port);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultadoLocal; 
		
		//final XQueryWrapperProxy proxy = new XQueryWrapperProxy();
		//proxy.setEndpoint(this.remoteQuery.getExecutionSite());
		//return proxy.executeXQuery(this.remoteQuery.toString());		
	}
	
	public void includeRootNodeInResult(){
		String sqResult = this.xqueryResult.getResult();
		String varId = this.remoteQuery.getVarId();
		this.xqueryResult.setResult(" <" + varId + "> " + sqResult + " </" + varId + "> ");
	}
	
	// Metodo que permite a conexao com o servidor que ira executar a consulta
	   public XQueryResult connection (SubQuery remoteQuery, String ip, int port) throws ClassNotFoundException{
		XQueryResult result = new XQueryResult();
		PrintStream ps = null;
		BufferedReader entrada=null; 
		try {
		
		//Cria o socket com o recurso desejado na porta especificada 
		Socket connection = new Socket(ip, port);
		
		String consulta = remoteQuery.toString();
					
		
		
		System.out.print("Imprimindo a consulta"+consulta);
		
		
		//byte [] consulta = remoteQuery.toString().getBytes();
		
		System.out.println("Imprimindo os bytes da consulta"+consulta);
				
		// Canal para envio dos dados
		ps = new PrintStream(connection.getOutputStream());
				
		//Submetendo a consulta ao servidor
		//ps.println(remoteQuery.toString());
		ps.println(consulta);
		//System.out.println("Verificando o valor de ps"+ps.toString());
		
		//Recebendo o retorno da consulta executada local
		ObjectInputStream  objIn  = new ObjectInputStream(connection.getInputStream()); 
		
		result = (XQueryResult) objIn.readObject(); 
		
			
		//Fechando a conexao do servidor
		ps.close();
		
		objIn.close();
		connection.close();
					
		} catch (UnknownHostException e) {
			System.out.println("Erro ao estabelecer a conexao com o ip" + ip);
			e.printStackTrace();
			
		} catch (IOException e) {
			System.out.println("Erro de IO:" +e);
			e.printStackTrace();
		}
		
		return result;
	}
	   

	   
}
