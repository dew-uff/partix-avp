package wrapper.sedna;

import java.io.IOException;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import wrapper.catalog.CatalogManager;

//import mediadorxml.catalog.CatalogManager;
import net.cfoster.sedna.xqj.SednaXQDataSource;

public class ConnectionSedna {
	
	public XQConnection establishSednaConnection() throws IOException {
						
		XQConnection xqc = null;
		CatalogManager cm = CatalogManager.getUniqueInstance();
		
		try {
			
			XQDataSource xqd = new SednaXQDataSource();				
			// Para acessar outra inst�ncia alterar o n�mero da porta e o endere�o IP			
			xqd.setProperty("port", cm.getportNumber());  
			xqd.setProperty("serverName", cm.getserverName()); 
			xqd.setProperty("databaseName", cm.getdatabaseName());
			
			xqc = xqd.getConnection(cm.getuserName(), cm.getuserPassword());
			System.out.println("ConnectionSedna: successfully established connection ...");
			return xqc;						
			
		} catch (XQException e) {
			System.out.println("ConnectionSedna: error establishing database connection ...");
			e.printStackTrace();
			return null;
		}
	}

}

