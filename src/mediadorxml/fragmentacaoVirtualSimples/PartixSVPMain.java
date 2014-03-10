package mediadorxml.fragmentacaoVirtualSimples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import mediadorxml.catalog.CatalogManager;
import mediadorxml.engine.XQueryEngine;

public class PartixSVPMain {

	private String userInput = null; // indica a query original do usu�rio.
	
    private String originalQuery;
    private String inputQuery;
    private int nnodes = 30;
    private String xquery;
	
	// docQueries: recebe as sub-consultas com express�es doc(), geradas quando a consulta informada pelo usu�rio possui a express�o collection().
	// para cada documento existente na cole��o informada, uma nova sub-consulta � gerada.
	private ArrayList<String> docQueries = null;
	// docQueriesWithoutFragmentation: recebe as proprias consultas de entrada, quando nao ha relacao de 1 para n, dentro do documento XML consultado.
	private ArrayList<String> docQueriesWithoutFragmentation = null;
	private XQueryEngine engine = null;
	long startTime = 0;
	
	public PartixSVPMain(int numberOfNodes, String query) {
		nnodes = numberOfNodes;
		inputQuery = query;
	}
	

    public void svpPressed() throws Exception {
        
        xqueryPressed();
        
        SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(false);
        Query q = Query.getUniqueInstance(true);
        if (q.getqueryExprType()!=null && q.getqueryExprType().equals("collection")){
            
            if (docQueries!=null){
                
                originalQuery = inputQuery;
                for (String docQry : docQueries) {
                    
                    inputQuery = docQry;
                    
                    executeXQuery();                                            
                }
                
               inputQuery = originalQuery;
            }
        }
        else {
            svp.setNumberOfNodes(nnodes);
            svp.setNewDocQuery(true);                                                       
            executeXQuery();                            
        }
        
        saveFragments();
    }
    
    private void executeXQuery() throws IOException {
        
        ExistsJoinOperation ej = new ExistsJoinOperation(inputQuery);
        ej.verifyInputQuery();
        Query q = Query.getUniqueInstance(true);
        q.setLastReadCardinality(-1);
        q.setJoinCheckingFinished(false);               
        
        
        if ((xquery == null) || (!xquery.equals(inputQuery))){ 
            xquery = inputQuery; //  consulta de entrada                  
        }       
        
        
        if ( q.getqueryExprType()!= null && !q.getqueryExprType().contains("collection") ) { // se a consulta de entrada não contém collection, execute a fragmentação virtual.
        
            engine = new XQueryEngine();
            engine.execute(xquery, false); // Para debugar o parser, passe o segundo parâmetro como true.                
            
            q.setJoinCheckingFinished(true);
            
            if (q.isExistsJoin()){
                q.setOrderBy("");                       
                engine.execute(xquery, false); // Executa pela segunda vez, porém desta vez fragmenta apenas um dos joins
            }               
            
        }
        else {  // se contem collection         
                            
            // Efetua o parser da consulta para identificar os elementos contidos em funções de agregação ou order by, caso existam.
            q.setOrderBy("");
            engine = new XQueryEngine();
            engine.execute(originalQuery, false);
            
            if (q.getPartitioningPath()!=null && !q.getPartitioningPath().equals("")) {
                SimpleVirtualPartitioning svp = new SimpleVirtualPartitioning();
                svp.setCardinalityOfElement(q.getLastCollectionCardinality());
                svp.setNumberOfNodes(nnodes);                       
                svp.getSelectionPredicateToCollection(q.getVirtualPartitioningVariable(), q.getPartitioningPath(), xquery);                                         
                q.setAddedPredicate(true);
            }
        }
    }
    
    private void xqueryPressed() throws Exception {
        
        Query q = Query.getUniqueInstance(true);
        
        /* Define o tipo de consulta (collection() ou doc()) e, caso seja sobre uma coleção 
         * retorna as sub-consultas geradas, armazenando-as no objeto docQueries.
         */
        q.setInputQuery(inputQuery);
        docQueries = q.setqueryExprType(inputQuery);
        
        if ( docQueries!=null && docQueries.size() > 0 ){ // é diferente de null, quando consulta de entrada for sobre uma coleção
            
            docQueriesWithoutFragmentation = docQueries;                                
            
            String subQueries = "";
            int i = 1;
            
            // Exibe na tela a esquerda as sub-consultas geradas para o usuário.  
            for (String docQry : docQueries) {
                subQueries = subQueries + i + "=\r\n" + docQry + "\r\n";                                                                
                i++;
            }
        }
        else if (q.getqueryExprType()!=null && q.getqueryExprType().equals("document")) { // consulta de entrada sobre um documento. 
            q.setInputQuery(inputQuery);
        }
        else if (q.getqueryExprType()!=null && q.getqueryExprType().equals("collection")) { // consulta de entrada sobre uma coleção.
           throw new IOException("Erro ao gerar sub-consultas para a coleção indicada. Verifique a consulta de entrada.");
        }
    }
    
	private void saveFragments() throws IOException{
		
		String results = "";
		CatalogManager cm = CatalogManager.getUniqueInstance();
		Query q = Query.getUniqueInstance(true);
		
		try {
					
			String basePath = cm.getSVP_Directory() + "/fragmentos.txt";
			FileWriter writer = new FileWriter(basePath,false);
			PrintWriter saida = new PrintWriter(writer);
			
		    File f = new File(cm.getSVP_Directory());
		    File[] files = f.listFiles (new FileFilter() {   
		            public boolean accept(File pathname) {   
		                return pathname.getName().toLowerCase().endsWith(".txt");   
		            }   
		        });   
		    
		    for (int i = 0; i < files.length; ++i) {   
		            files[i].delete();   
		    }
		    
			SubQuery sbq = SubQuery.getUniqueInstance(true);		
					
			if ( sbq.getSubQueries()!=null && sbq.getSubQueries().size() > 0 ){
				
			    results = "<<<< Foram gerados " + sbq.getSubQueries().size() + 
								 " fragmentos na fragmentação virtual simples >>>> \r\n\r\n";
				int i = 1;
				
				for ( String initialFragments : sbq.getSubQueries() ) {

					String basePath2 = cm.getSVP_Directory() + "/fragmento_"+(i-1)+".txt";	
					FileWriter writer2 = new FileWriter(basePath2,false);
					PrintWriter saida2 = new PrintWriter(writer2);				

					saida2.print("<ORDERBY>" + q.getOrderBy() + "</ORDERBY>\r\n");
					saida2.print("<ORDERBYTYPE>" + q.getOrderByType() + "</ORDERBYTYPE>\r\n");
					saida2.print("<AGRFUNC>" + (q.getAggregateFunctions()!=null?q.getAggregateFunctions():"") + "</AGRFUNC>#\r\n");
					
					saida2.print(initialFragments);
					saida2.close();
					writer2.close();
					results = results + i + "#\r\n" + initialFragments + "\r\n";
					i++;					
				}				
			}
			else {
				
				if ( this.docQueriesWithoutFragmentation != null && this.docQueriesWithoutFragmentation.size() >0 ) { // para consulta que nao foram fragmentadas pois nao ha relacionamento de 1 para n.

					results = "<<<<  Não foram gerados fragmentos, pois os elementos dos documentos especificados na consulta \r\n não possuem relacionamento 1:N,"
					               	+" condição necessária para a fragmentação. \r\n Desta forma, as consultas a serem executadas são:  >>>> \r\n\r\n";
					int i = 1;
					
					for ( String initialFragments : this.docQueriesWithoutFragmentation ) {							
						results = results + i + "=\r\n" + initialFragments + "\r\n";
						i++;					
					}			
				}
				else { // nao gerou fragmentos e nao ha consultas de entrada. Ocorreu algum erro durante o parser da consulta. 
					results = "Erro ao gerar fragmentos. A consulta de entrada está incorreta ou não é uma consulta válida para o parser do sistema.";					
				}
			}
			
			saida.print(results);
			saida.close();
			writer.close();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
    
    public static void main(String[] args) {
    	if (args.length < 2) {
    		System.err.println("Arguments missing <nnodes> <queryfile>");
    		System.exit(1);
    	}
    	
    	int nodes = 0;
    	try {
    		nodes = Integer.parseInt(args[0]);
    	}
    	catch (NumberFormatException e) {
    		System.err.println("<nnodes> must be an integer!");
    		System.exit(1);
    	}
    	
    	String query = null;
    	try { 
    		query = readContentFromFile(args[1]);
    	} catch (Exception e) {
    		System.err.println("Something wrong reading the query file! " + e.getMessage());
    		System.exit(1);
    	}
    	
    	try {
            long start = System.currentTimeMillis();
    		PartixSVPMain main = new PartixSVPMain(nodes, query);
    		main.svpPressed();
            long partitionTime = System.currentTimeMillis() - start;
    		System.out.println("SVP done! Partitioning time: " + partitionTime + " ms.");
    	} catch (Exception e) {
    		System.err.println("Something wrong processing the SVP! " + e.getMessage());
    		e.printStackTrace();
    		System.exit(1);    		
    	}
    	

    }

    /**
     * Load the file content into a String object
     * 
     * @param filename The file
     * @return the content of the file in a string object
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static String readContentFromFile(String filename) throws FileNotFoundException, IOException {
        
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String everything = null;
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append('\n');
                line = br.readLine();
            }
            everything = sb.toString().trim();
        } finally {
            br.close();
        }
        
        return everything;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"



