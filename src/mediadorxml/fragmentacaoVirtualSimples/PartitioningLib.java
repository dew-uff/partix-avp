package mediadorxml.fragmentacaoVirtualSimples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import mediadorxml.config.Config;
import mediadorxml.engine.XQueryEngine;

public class PartitioningLib {

    public static void main(String[] args) {
        
        if (args.length < 3) {
            System.out.println("Usage: java -jar vpart <dbConfiguration.xml> <query.xq> <output_file>");
            System.exit(0);
        }

        File f = new File(args[0]);
        if (!f.exists()) {
            System.err.println(args[0] + " DB configuration file not found!");
            System.exit(1);
        }
        
        Config.setCatalogFile(args[0]);
        
        String query = null;
        
        try {
            query = readQueryFromFile(args[1]);
            verifyQuery(query);
        } catch (FileNotFoundException e) {
            System.out.println("Query file was not found!");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Something went wrong while reading query file!");
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
       
        try {
            processQuery(query);
        } catch (IOException e) {
            System.out.println("Something went wrong while processing the query: " + e.getMessage());
            System.exit(1);
        }
        
        try {
            saveResult(args[2]);
        } catch (IOException e) {
            System.out.println("Something went wrong while saving the result: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void verifyQuery(String query) throws Exception {
        String returnClause = query.substring(query.indexOf("return")+6, query.length());
        
        if (query.indexOf("<") > 0 || query.lastIndexOf(">") != query.length()-1) {
            throw new Exception("A consulta de entrada deve iniciar e terminar com um elemento construtor. Exemplo: <resultado> { for $var in ... } </resultado>.");
        }
        else if (query.toUpperCase().indexOf("/TEXT()") != -1) {
            throw new Exception("O parser deste programa não aceita a função text(). Especifique somente os caminhos xpath para acessar os elementos nos documentos XML.");
        }
        else if (returnClause.trim().charAt(0) != '<') {
            
            throw new Exception("É obrigatória a especificação de um elemento XML após a cláusula return. Ex.: <results> { for $var ... return <elemName> ... </elemName> } </results>");
        }
    }

    private static String readQueryFromFile(String filename) throws FileNotFoundException, IOException {
        
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
    
    private static void processQuery(String query) throws IOException {

        Query q = Query.getUniqueInstance(true);
        q.setInputQuery(query);
        
        List<String> docQueries = q.setqueryExprType(query);

        SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(false);
        
        if (q.getqueryExprType()!=null && q.getqueryExprType().equals("collection")){
            
            if (docQueries!=null){
                 
                for (String docQry : docQueries) {
                    
                    executeXQuery(docQry,query);                                            
                }
            }
        }
        else {
            
            svp.setNumberOfNodes(1);
            svp.setNewDocQuery(true);                                                       
            executeXQuery(query,query);                            
        }
    }

    private static void executeXQuery(String xquery, String originalQuery) {
        
        ExistsJoinOperation ej = new ExistsJoinOperation(xquery);
        ej.verifyInputQuery();
        Query q = Query.getUniqueInstance(true);
        q.setLastReadCardinality(-1);
        q.setJoinCheckingFinished(false);               
        
        if ( q.getqueryExprType()!= null && !q.getqueryExprType().contains("collection") ) { // se a consulta de entrada não contém collection, execute a fragmentação virtual.
        
            XQueryEngine engine = new XQueryEngine();
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
            XQueryEngine engine = new XQueryEngine();
            engine.execute(originalQuery, false);
            
            if (q.getPartitioningPath()!=null && !q.getPartitioningPath().equals("")) {
                SimpleVirtualPartitioning svp = new SimpleVirtualPartitioning();
                svp.setCardinalityOfElement(q.getLastCollectionCardinality());
                svp.setNumberOfNodes(1);                       
                svp.getSelectionPredicateToCollection(q.getVirtualPartitioningVariable(), q.getPartitioningPath(), xquery);                                         
                q.setAddedPredicate(true);
            }
            
            //engine = new XQueryEngine();
            //engine.execute(originalQuery, false);             
        }
    }
    
    private static void saveResult(String outputFile) throws IOException {
        
        SubQuery sbq = SubQuery.getUniqueInstance(true);        
                
        if (sbq.getSubQueries()!= null && sbq.getSubQueries().size() > 0){
            FileWriter writer = new FileWriter(outputFile,false);
            PrintWriter saida = new PrintWriter(writer);
            Query q = Query.getUniqueInstance(true);
            SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(true);
            int cardinality = svp.getCardinalityOfElement();
            
            String fragment = sbq.getSubQueries().get(0);
            
            int selInit = fragment.indexOf("[position()");
            int selEnd = fragment.indexOf(']', selInit);
            String selection = fragment.substring(selInit, selEnd+1);
            fragment = fragment.replace(selection, "[#"+cardinality+"#]");
            
            saida.print("<ORDERBY>" + q.getOrderBy() + "</ORDERBY>\r\n");
            saida.print("<ORDERBYTYPE>" + q.getOrderByType() + "</ORDERBYTYPE>\r\n");
            saida.print("<AGRFUNC>" + (q.getAggregateFunctions()!=null?q.getAggregateFunctions():"") + "</AGRFUNC>#\r\n");
                
            saida.print(fragment);
            saida.close();
            writer.close();
        }
    }
}
