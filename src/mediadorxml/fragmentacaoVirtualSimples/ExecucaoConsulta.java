package mediadorxml.fragmentacaoVirtualSimples;

import java.io.IOException;

import javax.xml.xquery.XQException;

import mediadorxml.catalog.CatalogManager;
import mediadorxml.database.Database;

public class ExecucaoConsulta {

	public static String executeQuery(String xquery) throws IOException {
		try {
			Database db = CatalogManager.getUniqueInstance().getDatabase();
			return db.executeQueryAsString(xquery);
		} catch (XQException e) {
			System.out.println("ExecucaoConsulta class: Erro ao executar XQuery.");
			e.printStackTrace();
			return null;
		}
	}
}


