package classePrincipal;

import mediadorxml.database.Database;
import mediadorxml.database.DatabaseFactory;

public class ExecucaoConsulta {

	public static String executeQuery(String xquery, String threadId) {
		
		try {
		    Database db = DatabaseFactory.getLocalDatabase();
            //long startTime = System.nanoTime();
		    return db.executeQueryAsString(xquery);
            //long delay = ((System.nanoTime() - startTime)/1000);    		
		
		} catch (Exception e) {
			System.out.println("ExecucaoConsulta class: Erro ao executar XQuery.");
			e.printStackTrace();
			return null;
		}
	}
}


