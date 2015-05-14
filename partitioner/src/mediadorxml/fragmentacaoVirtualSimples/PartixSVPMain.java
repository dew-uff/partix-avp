package mediadorxml.fragmentacaoVirtualSimples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import mediadorxml.catalog.CatalogManager;
import uff.dew.svp.Partitioner;
import uff.dew.svp.db.DatabaseException;
import uff.dew.svp.exceptions.PartitioningException;

public class PartixSVPMain {

    private String inputQuery;
    private int nfragments = 30;
	
	public PartixSVPMain(int numberOfNodes, String query) {
		nfragments = numberOfNodes;
		inputQuery = query;
	}
	
	public void execute() throws DatabaseException, PartitioningException, IOException {
        Partitioner partitioner = new Partitioner("localhost", 1984, "admin", "admin", "expdb", "BASEX");
        List<String> fragments = partitioner.executePartitioning(inputQuery, nfragments);
        saveFragments(fragments);
	}
	
	private void saveFragments(List<String> fragments) throws IOException{
		
		String results = "";
		CatalogManager cm = CatalogManager.getUniqueInstance();
		
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
		    
		    results = "<<<< Foram gerados " + fragments.size() + 
								 " fragmentos na fragmentação virtual simples >>>> \r\n\r\n";

		    int i = 1;
			
			for ( String initialFragments : fragments ) {

				String basePath2 = cm.getSVP_Directory() + "/fragmento_"+(i-1)+".txt";	
				FileWriter writer2 = new FileWriter(basePath2,false);
				PrintWriter saida2 = new PrintWriter(writer2);				
				saida2.print(initialFragments);
				saida2.close();
				writer2.close();
				results = results + i + "#\r\n" + initialFragments + "\r\n";
				i++;					
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
    		System.err.println("Arguments missing <nfragments> <queryfile>");
    		System.exit(1);
    	}
    	
    	int nodes = 0;
    	try {
    		nodes = Integer.parseInt(args[0]);
    	}
    	catch (NumberFormatException e) {
    		System.err.println("<nfragments> must be an integer!");
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
    		main.execute();
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

}



