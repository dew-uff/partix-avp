package mediadorxml.fragmentacaoVirtualAdaptativa;

import java.io.IOException;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import mediadorxml.catalog.CatalogManager;
import net.cfoster.sedna.xqj.SednaXQDataSource;

public class ConnectionSedna {
	
	public XQConnection establishSednaConnection() throws IOException {
						
		XQConnection xqc = null;
		CatalogManager cm = CatalogManager.getUniqueInstance();
		
		try {
			
			XQDataSource xqd = new SednaXQDataSource();				
			// Para acessar outra instância alterar o número da porta e o endereço IP			
			xqd.setProperty("port", cm.getportNumber());  
			xqd.setProperty("serverName", cm.getserverName()); 
			xqd.setProperty("databaseName", cm.getdatabaseName());
			
			xqc = xqd.getConnection(cm.getuserName(), cm.getuserPassword());
			return xqc;						
			
		} catch (XQException e) {
			System.out.println("ConnectionSedna: Erro ao estabelecer conexao com o host/banco indicados.");
			e.printStackTrace();
			return null;
		}
	}

}
