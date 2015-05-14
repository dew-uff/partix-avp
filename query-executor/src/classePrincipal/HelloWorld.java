package classePrincipal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mpi.MPI;
import mpi.MPIException;
import uff.dew.svp.ExecutionContext;
import uff.dew.svp.FinalResultComposer;
import uff.dew.svp.db.DatabaseException;

public class HelloWorld {
	
	public static void main(String[] args) throws MPIException, IOException {
	    
	    int myrank;		
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
		String svpHome = myargs[2];

		if (myrank == 0) {
			System.out.println("Threads per node: " + THREADS_PER_NODE);
			System.out.println("# of nodes: " + NUMBER_NODES);
			System.out.println("Total # of processors: " + TOTAL_NUMBER_THREADS);
			System.out.println("SVP Home: " + svpHome);
		}
		
		Thread[] th = new Thread[THREADS_PER_NODE];		
		
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
			// Aguarda até que todas as threads sejam finalizadas.
			try {
				th[i].join();
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		}			
		
        // <<<<<<<<<<<<<<<<<<<< Cada nó irá ler até 8 arquivos, pois cada um contém oito processadores. >>>>>>>>>>>>>>>>>>>>>>
        // Usar a lei de formação: (rank*8) a ((rank+1)*8)-1 para a leitura dos arquivos. 
         /* As threads do primeiro nodo devem ser identificadas de 0 a 7. 
         * As threads do segundo nodo devem ser identificadas de 8 a 15. 
         * As threads do segundo nodo devem ser identificadas de 16 a 23.
         * E, assim, sucessivamente. A cada criação de threads, passar para o construtor da runnable de onde deve começar o contador.
         */ 
       			
		MPI.COMM_WORLD.Barrier(); // Aguarda até que todos os nós tenham finalizado seus jobs. 
				
		if (myrank==0) { // Nó 0 é o nó de controle, responsável pela consolidação dos resultados.			

		    delay = (System.nanoTime() - init)/1000000; // Calcula o tempo de execução de todas as sub-consultas. Tempo retornado em milisegundos.		
		    System.out.println("Subquery phase execution time:" + delay);
			
			try {
				long t1 = System.nanoTime();
				// caminho onde será salvo o documento com a resposta final
                String completeFileName = svpHome + "/finalResult/xqueryAnswer.xml";

				File file = new File(completeFileName);		
				FileOutputStream out = new FileOutputStream(file);
				
				FinalResultComposer frc = new FinalResultComposer(out);
				frc.setDatabaseInfo("localhost", 1984, "admin", "admin", "expdb", "BASEX");
				
				// TODO hack. using a fragment as a way to restore context
				String fragmentFile = svpHome + "/SVP/fragmento_0.txt";
				FileInputStream contextStream = new FileInputStream(fragmentFile);
				frc.setExecutionContext(ExecutionContext.restoreFromStream(contextStream));
				contextStream.close();
				
				File partialsDir = new File(svpHome + "/partialResults");
				File[] partialFiles = partialsDir.listFiles();
				System.out.println("# of partial files: " + partialFiles.length);
				List<String> partialFilenames = new ArrayList<String>();
				for (File f : partialFiles) {
                    partialFilenames.add(f.getAbsolutePath());
                }
				Collections.sort(partialFilenames);
				
				for(String partial : partialFilenames) {
				    FileInputStream fis = new FileInputStream(partial);
				    frc.loadPartial(fis);
				    fis.close();
				}

				long t2 = System.nanoTime();
				
				System.out.println("Partials loading time: " + ((t2 - t1)/1000000) + " ms");
				
				frc.combinePartialResults();
				
			    // Calcula o tempo de composição do resultado. Tempo retornado em milisegundos.
			    delay = ((System.nanoTime() - t2)/1000000);
				System.out.println("Composition time:" + delay);
				
			    
			    long totalTime = ((System.nanoTime() - init)/1000000);
			    System.out.println("Total execution time:" + totalTime);
                if ( out!=null ){
                    out.close();
                }
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DatabaseException e) {
                e.printStackTrace();
            }
		}		
		MPI.Finalize();
   }
}
