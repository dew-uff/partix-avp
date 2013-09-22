package classePrincipal;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import ru.ispras.sedna.driver.DatabaseManager;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;

import net.cfoster.sedna.xqj.SednaXQDataSource;

public class ConnectionSedna {
	
	public SednaConnection establishSednaConnection(String serverName, String databaseName) {
						
		SednaConnection con = null;
			
		try {
			con = DatabaseManager.getConnection(serverName, databaseName, "SYSTEM", "MANAGER");
			
		} catch (DriverException e) {
			System.out.println("ConnectionSedna: Erro ao estabelecer conexao com o host/banco indicados.");
			e.printStackTrace();
		}		
		return con;						
			
	
	}

}
