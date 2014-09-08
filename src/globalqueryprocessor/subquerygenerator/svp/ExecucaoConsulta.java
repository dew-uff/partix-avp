package globalqueryprocessor.subquerygenerator.svp;

import java.io.IOException;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

import localqueryprocessor.dynamicrangegenerator.avp.ConnectionSedna;
import mediadorxml.catalog.CatalogManager;
import net.cfoster.sedna.xqj.*;

public class ExecucaoConsulta {

	public static String executeQuery(String xquery) throws IOException {
		
		XQResultSequence xqr = null;
		XQExpression xqe = null;
		XQConnection xqc = null;		
		String retorno = "";		
		
		try {
			
			ConnectionSedna con = new ConnectionSedna();
			xqc = con.establishSednaConnection();			
			xqe = xqc.createExpression();
			xqr = xqe.executeQuery(xquery);	
			
			if (!xqr.next()){				
				return "";
			}			
			
			do {				
				retorno = retorno + xqr.getItemAsString(null);														
			} while (xqr.next());
						
			xqc.close();
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


