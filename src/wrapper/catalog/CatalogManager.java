package wrapper.catalog;

import globalqueryprocessor.subquerygenerator.svp.Collection;
import globalqueryprocessor.subquerygenerator.svp.Index;
import globalqueryprocessor.subquerygenerator.svp.Reference;

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
		
		// De-serializa��o do cat�logo de XML para o objeto da classe "Catalog"
		//String catalogFile = Config.getCatalogFile();
		XStream xstream = new XStream(new DomDriver());
		this._catalog = (Catalog)xstream.fromXML(new InputStreamReader(Config.getCatalogFileInputStream()));
	}
	
	public CatalogManager(Catalog catalog){
		this._catalog = catalog;
	}
	
	public void save() throws IOException{
		// serializa��o do cat�logo em XML
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
	
	public TreeNode getGlobalViewLocalization(String globalViewName) throws GlobalViewNotFoundException{
		
		// Busca a view global
		GlobalView gv = this._catalog.getGlobalView(globalViewName);
		
		if (gv != null){
			// Busca os fragmentos da view global
			ArrayList<LocalView> lv = gv.getLocalViews();
			return this.getLocalization(lv);
		}
		else
			throw new GlobalViewNotFoundException("Global view '" + globalViewName + "' does not exist.");
	}
	
	public GlobalView getGlobalView(String globalViewName){
		return this._catalog.getGlobalView(globalViewName);
	}
	
	public LocalView getLocalView(String localViewName){
		return this._catalog.getLocalView(localViewName);
	}
	
	protected TreeNode getLocalization(ArrayList<LocalView> localViews) throws UnsupportedOperationException{
		
		// R�plica (sem fragmenta��o)
		if (this.checkAllFragments(localViews, LocalView.FragmentType.FULL)){
			LocalView lv = localViews.get(0);
			return new TreeNode(lv.getViewName());
		}
		// Todos fragmentos horizontais
		else if (this.checkAllFragments(localViews, LocalView.FragmentType.HORIZONTAL)){
			return this.getHorizontalLocalization(localViews);
		}
		// Todos fragmentos Verticais
		else if (this.checkAllFragments(localViews, LocalView.FragmentType.VERTICAL)){
			return this.getVerticalLocalization(localViews);
		}
		// Fragmentos h�bridos
		else{
			return this.getHybridLocalization(localViews);
		}		
	}
	
	protected boolean checkAllFragments(ArrayList<LocalView> localViews, LocalView.FragmentType type){
		for (int i=0; i<localViews.size(); i++){
			LocalView lv = localViews.get(i);
			if (lv.getFragmentType() != type)
				return false;
		}		
		return true;
	}
	
	protected TreeNode getHorizontalLocalization(ArrayList<LocalView> localViews){
		// Uni�o de todos os fragmentos
		LocalView lv = localViews.get(0);
		TreeNode node = new TreeNode(lv.getViewName());
		
		for (int i=1; i<localViews.size(); i++){
			
			lv = localViews.get(i);
			
			// Cria��o de uma opera��o de Uni�o
			TreeNode unionNode = new TreeNode(UNION);
			unionNode.addChild(node);
			unionNode.addChild(new TreeNode(lv.getViewName()));
			
			node = unionNode;
		}
		
		return node.getRootNode();
	}
	
	protected TreeNode getVerticalLocalization(ArrayList<LocalView> localViews){
		// Uni�o de todos os fragmentos
		LocalView lv = localViews.get(0);
		TreeNode node = new TreeNode(lv.getViewName());
		
		for (int i=1; i<localViews.size(); i++){
			
			lv = localViews.get(i);
			
			// Cria��o de uma opera��o de Uni�o
			TreeNode unionNode = new TreeNode(JOIN);
			unionNode.addChild(node);
			unionNode.addChild(new TreeNode(lv.getViewName()));
			
			node = unionNode;
		}
		
		return node.getRootNode();
	}
	
	protected TreeNode getHybridLocalization(ArrayList<LocalView> localViews) throws UnsupportedOperationException{
		
		// 1. Descoberta da fragmenta��o prim�ria da h�brida
		boolean isUnionRoot = true;
		LocalView lvI = localViews.get(0);
		
		// Se h� um fragmento puramente horizontal, a prim�ria � horizontal (union no root)
		if (lvI.getProjectionPredicates() == null || lvI.getProjectionPredicates().size() == 0){
			isUnionRoot = true;
		}
		
		// Se h� um fragmento puramente vertical, a prim�ria � vertical (join no root)
		else if (lvI.getSelectionPredicates() == null || lvI.getSelectionPredicates().size() == 0){
			isUnionRoot = false;
		}
		
		// Se o fragmento � h�brido, procuramos o seu "par" para saber a prim�ria
		else {
			for (int j=1; j<localViews.size(); j++){
				LocalView lvJ = localViews.get(j);
				
				// Compara��o dos fragmentos verticais
				if (lvI.getProjectionPredicates().equals(lvJ.getProjectionPredicates())){
					isUnionRoot = false;
					break;
				}
				else if (lvI.getSelectionPredicates().equals(lvJ.getSelectionPredicates())){
					isUnionRoot = true;
					break;
				}
			}
		}
		
		if (isUnionRoot){
			return this.getHybridHorizontalLocalization(localViews);
		}
		else{
			return this.getHybridVerticalLocalization(localViews);
		}
		
	}
	
	protected TreeNode getHybridHorizontalLocalization(ArrayList<LocalView> localViews){
		
		// TreeNode root
		TreeNode localization = new TreeNode(UNION);

		ArrayList<LocalView> localViewsProcessed = new ArrayList<LocalView>();
		
		//Lista dos fragmentos puramente horizontais
		ArrayList<LocalView> pureHorizontalLocalViews = new ArrayList<LocalView>();
		for (int i=0; i<localViews.size(); i++){
			LocalView lv = localViews.get(i);
			if (lv.getProjectionPredicates() == null || lv.getProjectionPredicates().size() == 0){
				pureHorizontalLocalViews.add(lv);
				localViewsProcessed.add(lv);
			}
		}
		
		if (pureHorizontalLocalViews.size() > 0){
			localization.addChild(this.getHorizontalLocalization(pureHorizontalLocalViews));
		}
		
		// Fragmentos h�bridos
		ArrayList<TreeNode> treeNodesHorizontals = new ArrayList<TreeNode>();
		for (int i=0; i<localViews.size()-1; i++){
			LocalView lv = localViews.get(i);
			if (!localViewsProcessed.contains(lv)){
				// Busca dos pares deste fragmento
				ArrayList<LocalView> viewsHybr = new ArrayList<LocalView>();
				viewsHybr.add(lv);
				localViewsProcessed.add(lv);
				for (int j=i+1; j<localViews.size(); j++){
					LocalView lvJ = localViews.get(j);
					if (lv.getSelectionPredicates().equals(lvJ.getSelectionPredicates())){
						viewsHybr.add(lvJ);
						localViewsProcessed.add(lvJ);
					}
				}
				// Cria��o do TreeNode da fragmenta��o vertical do fragmento horizontal
				TreeNode n = this.getVerticalLocalization(viewsHybr);
				treeNodesHorizontals.add(n);
			}
		}
		
		// Inclus�o dos TreeNodes verticais no TreeNode principal (Horizontal)
		for (int i=0; i<treeNodesHorizontals.size(); i++){
			if (localization.getChildren().size() == 2){
				
				// Remo��o de um dos filhos de localization
				TreeNode localizationChild = localization.getChild(0);
				localization.getChildren().remove(localizationChild);
				
				TreeNode unionNode = new TreeNode(UNION);
				unionNode.addChild(treeNodesHorizontals.get(i));
				unionNode.addChild(localizationChild);
				
				localization.addChild(unionNode);
				
			}
			else{
				localization.addChild(treeNodesHorizontals.get(i));				
			}
		}
	
		return localization;		
	}
	
	protected TreeNode getHybridVerticalLocalization(ArrayList<LocalView> localViews) throws UnsupportedOperationException{
		
		// TreeNode root
		TreeNode localization = new TreeNode(JOIN);

		ArrayList<LocalView> localViewsProcessed = new ArrayList<LocalView>();
		
		//Lista dos fragmentos puramente verticais
		ArrayList<LocalView> pureVerticalLocalViews = new ArrayList<LocalView>();
		for (int i=0; i<localViews.size(); i++){
			LocalView lv = localViews.get(i);
			if (lv.getSelectionPredicates() == null || lv.getSelectionPredicates().size() == 0){
				pureVerticalLocalViews.add(lv);
				localViewsProcessed.add(lv);
			}
		}
		
		if (pureVerticalLocalViews.size() > 0){
			localization.addChild(this.getVerticalLocalization(pureVerticalLocalViews));
		}
		
		// Fragmentos h�bridos
		ArrayList<TreeNode> treeNodesHorizontals = new ArrayList<TreeNode>();
		for (int i=0; i<localViews.size()-1; i++){
			LocalView lv = localViews.get(i);
			if (!localViewsProcessed.contains(lv)){
				// Busca dos pares deste fragmento
				ArrayList<LocalView> viewsHybr = new ArrayList<LocalView>();
				viewsHybr.add(lv);
				localViewsProcessed.add(lv);
				for (int j=i+1; j<localViews.size(); j++){
					LocalView lvJ = localViews.get(j);
					if (lv.getProjectionPredicates().equals(lvJ.getProjectionPredicates())){
						viewsHybr.add(lvJ);
						localViewsProcessed.add(lvJ);
					}
				}
				// Cria��o do TreeNode da fragmenta��o vertical do fragmento horizontal
				TreeNode n = this.getHorizontalLocalization(viewsHybr);
				treeNodesHorizontals.add(n);
			}
		}
		
		// Inclus�o dos TreeNodes horizontais no TreeNode principal (Vertical)
		for (int i=0; i<treeNodesHorizontals.size(); i++){
			if (localization.getChildren().size() == 2){
				
				// Remo��o de um dos filhos de localization
				TreeNode localizationChild = localization.getChild(0);
				localization.getChildren().remove(localizationChild);
				
				TreeNode joinNode = new TreeNode(JOIN);
				joinNode.addChild(treeNodesHorizontals.get(i));
				joinNode.addChild(localizationChild);
				
				localization.addChild(joinNode);
				
			}
			else{
				localization.addChild(treeNodesHorizontals.get(i));				
			}
		}
	
		return localization;		
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
