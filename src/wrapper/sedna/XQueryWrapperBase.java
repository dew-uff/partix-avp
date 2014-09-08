package wrapper.sedna;

import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.xml.xquery.XQException;

public abstract class XQueryWrapperBase {

	/**
	 * Atualização das views locais na XQuery de entrada pelas localizações
	 * definidas no arquivo de propriedades
	 * @param xquery
	 * @return
	 */
	protected String updateViewLocation(String xquery){
		String newXQuery = xquery;
		System.out.print(xquery);
		int indexView = newXQuery.indexOf("VIEW(");
		System.out.print(indexView);
		while (indexView > 0){
			int indexClose = newXQuery.indexOf(")", indexView);
			String viewLabel = newXQuery.substring(indexView, indexClose+1);
			String newViewLabel = ConfigWrapper.getProperty(viewLabel, this);
			
			// Replace da view original pela correta segundo o arquivo de parâmetros
			newXQuery = newXQuery.replace((CharSequence)viewLabel, (CharSequence)newViewLabel);
			
			indexView = newXQuery.indexOf("VIEW(", indexView+1);
		}
		
		return newXQuery;
	}
	
	public abstract XQueryResult executeXQuery(final String query) throws XQException, RemoteException, SQLException;
}
