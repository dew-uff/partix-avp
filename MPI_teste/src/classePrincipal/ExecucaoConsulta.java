package classePrincipal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

import net.cfoster.sedna.xqj.SednaXQDataSource;

public class ExecucaoConsulta {

	public static String executeQuery(String xquery, String threadId, OutputStream out) {
		
		XQResultSequence xqr = null;
		XQExpression xqe = null;
		XQConnection xqc = null;
		//String retorno = "";
		
		try {
			
			XQDataSource xqd = new SednaXQDataSource();				
			// Para acessar outra inst�ncia alterar o n�mero da porta e o endere�o IP			
			xqd.setProperty("port", "5050");  
			//System.out.println("ExecucaoConsulta class: 50"+threadId);
			xqd.setProperty("serverName", "127.0.0.1"); 
			xqd.setProperty("databaseName", "xmark");
			
			xqc = xqd.getConnection("SYSTEM", "MANAGER");
			xqe = xqc.createExpression();			
			
			long startTime = System.nanoTime();
			//System.out.println("ExecucaoConsulta class: xquery::"+xquery);

			xqr = xqe.executeQuery(xquery);	
			long delay = ((System.nanoTime() - startTime)/1000);			
			
			if (!xqr.next()){
				System.out.println("ExecucaoConsulta class: Nenhum resultado retornado. Verifique o banco de dados ao qual est� conectado.");
				return null;
			}			
			
			do {				
				out.write(xqr.getItemAsString(null).getBytes());
				//retorno = retorno + xqr.getItemAsString(null);														
			} while (xqr.next());
			
		} catch (XQException e) {
			System.out.println("ExecucaoConsulta class: Erro ao executar XQuery.");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.out.println("ExecucaoConsulta class: Erro ao ler resultado da consulta.");
			e.printStackTrace();
		}
		
		finally {
			try {
				if (xqr!=null) xqr.close();			
				if (xqe!=null) xqe.close();			
				if (xqc!=null) xqc.close();				
			} catch (Exception e2) {
				System.out.println("ExecucaoConsulta class: Erro ao fechar conex�o.");
				e2.printStackTrace();
				return null;
			}
		}		

		
		return null;
	}
	
	public static String executeQueryAsString(String xquery, String threadId) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		executeQuery(xquery, threadId, out);
		return out.toString();
	}
}


