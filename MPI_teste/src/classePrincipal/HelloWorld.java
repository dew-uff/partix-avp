package classePrincipal;

import java.io.File;
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
		
		final int THREADS_PER_NODE = 8;	
		final int TOTAL_NUMBER_THREADS = 32; //Numero total de thread considerando todos os n�s alocados
        SubQuery sbq;  
        SubQuery.deleteFilesFromDirectory();
                
		MPI.Init(args);		
		myrank = MPI.COMM_WORLD.Rank();
		
		Thread[] th = new Thread[THREADS_PER_NODE];		
		sbq = new SubQuery();
		sbq.setRunningSubqueries(true);				
		
		//System.out.println("HelloWorld.main(): Inicializando threads:" + THREADS_PER_NODE);
		for (int i = 0; i < THREADS_PER_NODE; i++) {
			// Cria uma thread para cada processador.
			th[i] = new Thread(new ParallelExecution(myrank,THREADS_PER_NODE));				
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
	    delay = (System.nanoTime() - init)/1000000; // Calcula o tempo de execu��o de todas as sub-consultas. Tempo retornado em milisegundos.		
		System.out.println("Execution time total:" + q.gettotalExecutionTime());

		if (myrank==0) { // N� 0 � o n� de controle, respons�vel pela consolida��o dos resultados.			
		
			FinalResult fr = new FinalResult();
			
			try {
				long wait = System.nanoTime();
				results = fr.getFinalResult(TOTAL_NUMBER_THREADS);				
				 
				// caminho onde ser� salvo o documento com a resposta final
				String completeFileName = "/home/users/carlarod/finalResult/xqueryAnswer.xml";		
					
			    File file = new File(completeFileName);			    
			    FileWriter fileWriter = new FileWriter(file);
			    PrintWriter output = new PrintWriter(fileWriter);			    
			    output.write(results);		    
			    
			    // Calcula o tempo de composi��o do resultado. Tempo retornado em milisegundos.
			    delay = ((System.nanoTime() - wait)/1000000);
				System.out.println("Composition time:" + delay);
				
			    if ( output!=null ){
			    	output.close();
			    }
			    
			    if ( fileWriter != null ){
			    	fileWriter.close();			    	
			    }
			
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