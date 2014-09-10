package mediadorxml.catalog.util;

<<<<<<< HEAD
import globalqueryprocessor.subquerygenerator.svp.CardinalityQuery;
import globalqueryprocessor.subquerygenerator.svp.Collection;
import globalqueryprocessor.subquerygenerator.svp.Index;
import globalqueryprocessor.subquerygenerator.svp.Reference;

import java.util.ArrayList;

=======
import java.util.ArrayList;

import mediadorxml.fragmentacaoVirtualSimples.CardinalityQuery;
import mediadorxml.fragmentacaoVirtualSimples.Collection;
import mediadorxml.fragmentacaoVirtualSimples.Index;
import mediadorxml.fragmentacaoVirtualSimples.Reference;
>>>>>>> e5fbc1cea5dfd6fc988da05b836ff82d02601a48

public class Catalog {
	
	protected String catalogName;	
		
	/*Carla -27/11/2010*/	
	protected ArrayList<Reference> relationships;
	protected String cardinalityQuery;
	protected String documentsQuery;
	
	// Diretórios onde os arquivo com os fragmentos e os resultados são gerados.
	// O usuário deve especificar no catálogo o caminho do arquivo no computador local, onde o sistema está sendo executado.
	protected String svpDirectory;	
	protected String avpDirectory;
	protected String partialResultDirectory;
	
	/* Parâmetros utilizados para conexão com o banco de dados Sedna*/
	protected String serverName;
	protected String databaseName;
	protected String userName;
	protected String userPassword;
<<<<<<< HEAD

	protected ArrayList<GlobalView> globalViews;
=======
>>>>>>> e5fbc1cea5dfd6fc988da05b836ff82d02601a48
	
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(String portNumber) {
		this.portNumber = portNumber;
	}

	protected String portNumber;
	
	public String getSVP_Directory() {
		return this.svpDirectory;
	}

	public void setSVP_Directory(String sVPDirectory) {
		this.svpDirectory = sVPDirectory;
	}

	public String getAVP_Directory() {
		return this.avpDirectory;
	}

	public void setAVP_Directory(String aVPDirectory) {
		this.avpDirectory = aVPDirectory;
	}

	public String getPartialResult_Directory() {
		return this.partialResultDirectory;
	}

	public void setPartialResult_Directory(String partialResultDirectory) {
		this.partialResultDirectory = partialResultDirectory;
	}

	public String getCardinalityQuery() {		
		return this.cardinalityQuery;
	}
	
	public String getFormattedQuery(String documentName, String collectionName, String path){
		String xqueryReturn = this.cardinalityQuery;		
		xqueryReturn = xqueryReturn.replace("#", documentName); // adiciona o nome do documento
		xqueryReturn = xqueryReturn.replace("%", collectionName);  // adiciona o nome do elemento
		xqueryReturn = xqueryReturn.replace("?", path);  // adiciona o caminho até o elemento
		return xqueryReturn;
	}
	
	public String getDocumentsQuery() {		
		return this.documentsQuery;
	}
	
	public String getFormattedDocumentsQuery(String collectionName){
		String xqueryReturn = this.documentsQuery;
		xqueryReturn = xqueryReturn.replace("?", collectionName); // adiciona o nome da coleção		
		return xqueryReturn;
	}

	public void setCardinalityQuery(String xquery) {
		this.cardinalityQuery = xquery;
	}
	
	public void setDocumentsQuery(String xquery) {
		this.documentsQuery = xquery;
	}

	public Catalog(){
	}
	
	public Catalog(String catalogName){
		this.catalogName = catalogName;
	}
	
	public String getCatalogName(){
		return this.catalogName;
	}
	
	public ArrayList<Reference> getRelationships() {		
		return this.relationships;
	}

	public void setRelationships(Reference ref) {
		this.relationships = new ArrayList<Reference>();
		this.relationships.add(ref);
<<<<<<< HEAD
	}
	
	public ArrayList getGlobalViews(){
		return this.globalViews;
	}
	
	public void setGlobalViews(ArrayList<GlobalView> globalViews){
		this.globalViews = globalViews;
	}
	
	public void setGlobalViews(GlobalView globalViews){
		this.globalViews = new ArrayList<GlobalView>();
		this.globalViews.add(globalViews);
	}
	
	public GlobalView getGlobalView(String globalViewName){
		for (int i=0; i<this.globalViews.size(); i++){
			if (((GlobalView)this.globalViews.get(i)).getViewName().equals(globalViewName))
				return (GlobalView)this.globalViews.get(i);
		}
		return null;
	}
	
	public LocalView getLocalView(String localViewName){
		for (int g=0; g<this.getGlobalViews().size(); g++){
			for (int l=0; l<((GlobalView)this.getGlobalViews().get(g)).getLocalViews().size(); l++){
				LocalView lv = (LocalView)((GlobalView)this.getGlobalViews().get(g)).getLocalViews().get(l);
				if (lv.getViewName().equals(localViewName))
					return lv;
			}
		}
		return null;
	}
=======
	}	
>>>>>>> e5fbc1cea5dfd6fc988da05b836ff82d02601a48
}
