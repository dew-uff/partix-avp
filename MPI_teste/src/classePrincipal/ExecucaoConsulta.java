package classePrincipal;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import net.cfoster.sedna.xqj.*;

public class ExecucaoConsulta {

	public static String executeQuery(String xquery, String threadId) {
		
		XQResultSequence xqr = null;
		XQExpression xqe = null;
		XQConnection xqc = null;
		String retorno = "";
		
		try {
			
			XQDataSource xqd = new SednaXQDataSource();				
			// Para acessar outra instância alterar o número da porta e o endereço IP			
			xqd.setProperty("port", "50"+threadId);  
			//System.out.println("ExecucaoConsulta class: 50"+threadId);
			xqd.setProperty("serverName", "146.164.31.140"); 
			xqd.setProperty("databaseName", "experiments_db");
			
			xqc = xqd.getConnection("SYSTEM", "MANAGER");
			xqe = xqc.createExpression();			
			
			long startTime = System.nanoTime();
			//System.out.println("ExecucaoConsulta class: xquery::"+xquery);

			xqr = xqe.executeQuery(xquery);	
			long delay = ((System.nanoTime() - startTime)/1000);			
			
			if (!xqr.next()){
				System.out.println("ExecucaoConsulta class: Nenhum resultado retornado. Verifique o banco de dados ao qual está conectado.");
				return null;
			}			
			
			do {				
				retorno = retorno + xqr.getItemAsString(null);														
			} while (xqr.next());
						
			return retorno;
			
		} catch (XQException e) {
			System.out.println("ExecucaoConsulta class: Erro ao executar XQuery.");
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if (xqr!=null) xqr.close();			
				if (xqe!=null) xqe.close();			
				if (xqc!=null) xqc.close();				
			} catch (Exception e2) {
				System.out.println("ExecucaoConsulta class: Erro ao fechar conexão.");
				e2.printStackTrace();
				return null;
			}
		}		
		
	}
}


