package mediadorxml.catalog;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import mediadorxml.algebra.basic.TreeNode;
import mediadorxml.catalog.util.Catalog;
import mediadorxml.catalog.util.GlobalView;
import mediadorxml.catalog.util.LocalView;
import mediadorxml.config.Config;
import mediadorxml.exceptions.GlobalViewNotFoundException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import mediadorxml.fragmentacaoVirtualSimples.Collection;
import mediadorxml.fragmentacaoVirtualSimples.Index;
import mediadorxml.fragmentacaoVirtualSimples.Reference;

public class CatalogManager {
	
	protected Catalog _catalog;
	protected static CatalogManager _catalogManager;
	
	protected static String UNION = "UNION";
	protected static String JOIN = "JOIN";
	
	public static CatalogManager getUniqueInstance() throws IOException{
		if (_catalogManager == null)
			_catalogManager = new CatalogManager();
		return _catalogManager;
	}
	
	public CatalogManager() throws FileNotFoundException, IOException{
		
		// De-serialização do catálogo de XML para o objeto da classe "Catalog"
		//String catalogFile = Config.getCatalogFile();
		XStream xstream = new XStream(new DomDriver());
		this._catalog = (Catalog)xstream.fromXML(new InputStreamReader(Config.getCatalogFileInputStream()));
	}
	
	public CatalogManager(Catalog catalog){
		this._catalog = catalog;
	}
	
	public void save() throws IOException{
		// serialização do catálogo em XML
		String catalogFile = Config.getCatalogFile();
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this._catalog);
		try{
			FileWriter fw = new FileWriter(catalogFile);
			fw.write(xml);
			fw.flush();
			fw.close();
			fw = null;
		}
		catch(IOException e){
			// TODO Log
			throw(e);
		}
	}
	public ArrayList<Reference> getRelationships(){
		return this._catalog.getRelationships();
	}
	
	public String getCardinalityQuery(){
		return this._catalog.getCardinalityQuery();
	}
	
	public String getFormattedQuery(String documentName, String collectionName, String path){
		return this._catalog.getFormattedQuery(documentName, collectionName, path);
	}
	
	public String getFormattedDocumentsQuery(String collectionName){
		return this._catalog.getFormattedDocumentsQuery(collectionName);
	}
		
	public String getSVP_Directory(){
		return this._catalog.getSVP_Directory();
	}
	
	public String getAVP_Directory(){
		return this._catalog.getAVP_Directory();
	}
	
	public String getpartialResults_Directory(){
		return this._catalog.getPartialResult_Directory();
	}
	
	public String getserverName(){
		return this._catalog.getServerName();
	}
	
	public String getdatabaseName(){
		return this._catalog.getDatabaseName();
	}
	
	public String getuserName(){
		return this._catalog.getUserName();
	}
	
	public String getuserPassword(){
		return this._catalog.getUserPassword();
	}
	
	public String getportNumber(){
		return this._catalog.getPortNumber();
	}
}
