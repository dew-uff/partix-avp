package tests.execution;


import mediadorxml.catalog.CatalogManager;
import mediadorxml.engine.XQueryEngine;
import mediadorxml.javaccparser.ParseException;

import globalqueryprocessor.subquerygenerator.svp.ExistsJoinOperation;
import globalqueryprocessor.subquerygenerator.svp.Query;
import globalqueryprocessor.subquerygenerator.svp.SimpleVirtualPartitioning;
import globalqueryprocessor.subquerygenerator.svp.SubQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Main {
	private static String query_path;
	private static int quantity_distributed_nodes;
	private static int vp_strategy;

	private static String originalQuery = "";
	private static String xquery = null;

	// docQueries: recebe as sub-consultas com expressões doc(), geradas quando a consulta informada pelo usuário possui a expressão collection().
	// para cada documento existente na coleção informada, uma nova sub-consulta é gerada.
	private static ArrayList<String> docQueries = null;
	// docQueriesWithoutFragmentation: recebe as proprias consultas de entrada, quando nao ha relacao de 1 para n, dentro do documento XML consultado.
	private static ArrayList<String> docQueriesWithoutFragmentation = null;

	private static XQueryEngine engine = null;
	private static long startTime = 0;

	public static void main(String[] args) throws ParseException, Exception {  

		if	(args.length == 3) {

			setQuery_path("queries/"+args[0]);
			setVp_strategy(Integer.parseInt(args[1]));

			if (getVp_strategy() == 0) //se a estratégia escolhida for a centralizada, garante que não usará nenhum nó distribuído para o processamento da consulta, ficando isso a cargo do coordenador
				setQuantity_distributed_nodes(0);
			else
				setQuantity_distributed_nodes(Integer.parseInt(args[2]));

			System.out.println("parameters: " + getQuery_path() + " " + getVp_strategy() + " " + getQuantity_distributed_nodes());
			System.out.println("query: " + obtainQueryByPath(getQuery_path()));
			System.out.print("vp strategy: ");

			switch( getVp_strategy() ) {
			case 0: System.out.println("centralized");
			break;
			case 1: System.out.println("svp");
			break;

			case 2: 
				System.out.println("avp");

				SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(false);
				Query q = Query.getUniqueInstance(true);
				q.setInputQuery(obtainQueryByPath(getQuery_path()));
				if (q.getqueryExprType()!=null && q.getqueryExprType().equals("collection")) {

					if (docQueries!=null) {
						setOriginalQuery(obtainQueryByPath(getQuery_path()));
						for (String docQry : docQueries) {
							executeXQuery();											
						}
					}
				}
				else {

					svp.setNumberOfNodes(getQuantity_distributed_nodes());
					svp.setNewDocQuery(true);														
					executeXQuery();							
				}

				for (int i = 0; i<svp.getAddedPredicates().size(); i++)
					System.out.println(svp.getInitialFragments().toString());
				
				break;
			case 3:
				System.out.println("avp_wr");
				break;
			default:
				System.out.println("usage: java Main \"<query_filename>\" <vp_strategy> <qnt_distributed_nodes>. e.g.: java Main \"c3.xq\" 1 8 - being <vp_strategy> equals 0 -> centralized; 1 -> svp; 2 -> avp; 3 -> avp_wr");

			}

		} else { //se a quantidade de parâmetros informados for != 3
			System.out.println("usage: java Main \"<query_filename>\" <vp_strategy> <qnt_distributed_nodes>. e.g.: java Main \"c3.xq\" 1 8 - being <vp_strategy> equals 0 -> centralized; 1 -> svp; 2 -> avp; 3 -> avp_wr");
		}
	}

	private static void executeXQuery() throws ParseException, Exception {

		try{			
			ExistsJoinOperation ej = new ExistsJoinOperation(obtainQueryByPath(getQuery_path()));
			ej.verifyInputQuery();
			Query q = Query.getUniqueInstance(true);
			q.setLastReadCardinality(-1);
			q.setJoinCheckingFinished(false);				

			if ((xquery == null) || (!xquery.equals(obtainQueryByPath(getQuery_path())))){ 
				startTime = System.nanoTime();
				xquery = obtainQueryByPath(getQuery_path()); //  consulta de entrada	
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
			else {	// se contem collection			

				// Efetua o parser da consulta para identificar os elementos contidos em funções de agregação ou order by, caso existam.
				q.setOrderBy("");
				engine = new XQueryEngine();
				engine.execute(obtainQueryByPath(getQuery_path()), false);

				if (q.getPartitioningPath()!=null && !q.getPartitioningPath().equals("")) {
					SubQuery sbq = SubQuery.getUniqueInstance(false); 
					SimpleVirtualPartitioning svp = new SimpleVirtualPartitioning();
					svp.setCardinalityOfElement(q.getLastCollectionCardinality());
					svp.setNumberOfNodes(getQuantity_distributed_nodes());	
					svp.getSelectionPredicateToCollection(q.getVirtualPartitioningVariable(), q.getPartitioningPath(), xquery);											
					q.setAddedPredicate(true);
				}

				//engine = new XQueryEngine();
				//engine.execute(originalQuery, false);		

			}

			showInitialFragments();

			System.out.println("Starting Cluster Query Processor (CQP) ...");
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showInitialFragments() throws IOException {

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
					
					System.out.println(initialFragments);
					
					saida2.close();
					writer2.close();
					results = results + i + "#\r\n" + initialFragments + "\r\n";
					i++;					
				}				
			}
			else {

				if ( docQueriesWithoutFragmentation != null && docQueriesWithoutFragmentation.size() >0 ) { // para consulta que nao foram fragmentadas pois nao ha relacionamento de 1 para n.

					results = "<<<<  Não foram gerados fragmentos, pois os elementos dos documentos especificados na consulta \r\n não possuem relacionamento 1:N,"
							+" condição necessária para a fragmentação. \r\n Desta forma, as consultas a serem executadas são:  >>>> \r\n\r\n";
					int i = 1;

					for ( String initialFragments : docQueriesWithoutFragmentation ) {							
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

	//Retorna a consulta com base no path+arquivo informado
	public static String obtainQueryByPath(String path) throws IOException {
		FileInputStream stream;

		stream = new FileInputStream(path);

		InputStreamReader streamReader = new InputStreamReader(stream); 
		// Coloca o conteudo do arquivo no buffer
		BufferedReader buffReader = new BufferedReader(streamReader); 
		String linha, query = "";  

		// Envia a consulta para o Server
		while ((linha = buffReader.readLine()) != null) 
		{  
			query += " " + linha;  
		}  
		buffReader.close(); 

		return query;
	}

	public static String getQuery_path() {
		return query_path;
	}

	public static void setQuery_path(String query_path) {
		Main.query_path = query_path;
	}

	public static int getQuantity_distributed_nodes() {
		return quantity_distributed_nodes;
	}

	public static void setQuantity_distributed_nodes(int quantity_distributed_nodes) {
		Main.quantity_distributed_nodes = quantity_distributed_nodes;
	}

	public static int getVp_strategy() {
		return vp_strategy;
	}

	public static void setVp_strategy(int vp_strategy) {
		Main.vp_strategy = vp_strategy;
	}

	public static String getOriginalQuery() {
		return originalQuery;
	}

	public static void setOriginalQuery(String originalQuery) {
		Main.originalQuery = originalQuery;
	}
}
