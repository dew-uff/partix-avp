package classePrincipal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.xquery.XQException;

import ru.ispras.sedna.driver.DriverException;
import mpi.*;

public class HelloWorld {
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws MPIException, IOException, XQException{
	    
	    int myrank;		
		String results = "";
		long delay;
		
		String[] myargs = MPI.Init(args);		
		
		if (myargs.length < 3) {
			System.err.println("Not enough arguments!");
			MPI.Finalize();
			System.exit(1);
		}
		myrank = MPI.COMM_WORLD.Rank();
		
		final int THREADS_PER_NODE = Integer.parseInt(myargs[0]);	
		final int NUMBER_NODES = Integer.parseInt(myargs[1]);
		final int TOTAL_NUMBER_THREADS = THREADS_PER_NODE*NUMBER_NODES; //Numero total de thread considerando todos os n�s alocados
		
		System.out.println("SVP Home: " + myargs[2]);
		String svpHome = myargs[2];
		
		SubQuery sbq;  
        SubQuery.setBasepath(svpHome);
		SubQuery.deleteFilesFromDirectory();

		
		Thread[] th = new Thread[THREADS_PER_NODE];		
		sbq = new SubQuery();
		sbq.setRunningSubqueries(true);				
		
		//System.out.println("HelloWorld.main(): Inicializando threads:" + THREADS_PER_NODE);
		for (int i = 0; i < THREADS_PER_NODE; i++) {
			// Cria uma thread para cada processador.
			th[i] = new Thread(new ParallelExecution(myrank,THREADS_PER_NODE, svpHome));				
		}
		
		long init = System.nanoTime();	
		for (int i = 0; i < THREADS_PER_NODE; i++) {
			// Inicializa a thread em cada processador.
			th[i].start();			
		}
			
		for (int i = 0; i < THREADS_PER_NODE; i++) {
			// Aguarda at� que todas as threads sejam finalizadas.
			try {
				th[i].join();
			} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			
		}			
		
        // <<<<<<<<<<<<<<<<<<<< Cada n� ir� ler at� 8 arquivos, pois cada um cont�m oito processadores. >>>>>>>>>>>>>>>>>>>>>>
        // Usar a lei de forma��o: (rank*8) a ((rank+1)*8)-1 para a leitura dos arquivos. 
         /* As threads do primeiro nodo devem ser identificadas de 0 a 7. 
         * As threads do segundo nodo devem ser identificadas de 8 a 15. 
         * As threads do segundo nodo devem ser identificadas de 16 a 23.
         * E, assim, sucessivamente. A cada cria��o de threads, passar para o construtor da runnable de onde deve come�ar o contador.
         */ 
       			
		MPI.COMM_WORLD.Barrier(); // Aguarda at� que todos os n�s tenham finalizado seus jobs. 
				
		Query q = Query.getUniqueInstance(true);

		if (myrank==0) { // N� 0 � o n� de controle, respons�vel pela consolida��o dos resultados.			

		    delay = (System.nanoTime() - init)/1000000; // Calcula o tempo de execu��o de todas as sub-consultas. Tempo retornado em milisegundos.		
			System.out.println("Subquery phase execution time:" + delay);
			
			FinalResult fr = new FinalResult();
			
			try {
				long wait = System.nanoTime();
				// caminho onde ser� salvo o documento com a resposta final
				//String completeFileName = svpHome + "/finalResult/xqueryAnswer.xml";
//				String completeFileName = "/usr/local/gabriel/partix-files/finalResult/xqueryAnswer.xml";
                String completeFileName = svpHome + "/finalResult/xqueryAnswer.xml";

				File file = new File(completeFileName);		
				FileOutputStream out = new FileOutputStream(file);
				results = fr.getFinalResult(TOTAL_NUMBER_THREADS, svpHome, out);				
				 
			    // Calcula o tempo de composi��o do resultado. Tempo retornado em milisegundos.
			    delay = ((System.nanoTime() - wait)/1000000);
				System.out.println("Composition time:" + delay);
				
			    if ( out!=null ){
			    	out.close();
			    }
			    
			    long totalTime = ((System.nanoTime() - init)/1000000);
			    System.out.println("Total execution time:" + totalTime);
//			    if ( fileWriter != null ){
//			    	fileWriter.close();			    	
//			    }
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		    
		
		}		
		MPI.Finalize();
	
   }
}
