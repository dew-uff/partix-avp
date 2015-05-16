package classePrincipal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import uff.dew.svp.SubQueryExecutionException;
import uff.dew.svp.SubQueryExecutor;
import uff.dew.svp.db.DatabaseException;
import uff.dew.svp.fragmentacaoVirtualSimples.Query;
import uff.dew.svp.fragmentacaoVirtualSimples.SubQuery;

public class ParallelExecution implements Runnable {
        
        private static int cont = 0; // global
        private int identificacao;
        private int myrank;        
        static ThreadMXBean tmb = ManagementFactory.getThreadMXBean();

		private FileReader in = null;
        private BufferedReader buff = null;
        private String line = null;
		private String svpHome;
        
        private int getMyrank() {
			return myrank;
		}

		private void setMyrank(int myrank) {
			this.myrank = myrank;
		}
		
		private int getIdentificacao() {
			return identificacao;
		}

		private void setIdentificacao(int identificacao) {
			this.identificacao = identificacao;
		}
        
		public void run() {
            
			try{
                long startTime; 
                long delay;
                String fragmentFilename = svpHome + "/SVP/fragmento_" + this.getIdentificacao() + ".txt";
                in = new FileReader(fragmentFilename);
                buff = new BufferedReader(in);
                StringBuilder fragmento = new StringBuilder();
                
                while((line = buff.readLine()) !=null){
                    fragmento.append(line);
                    fragmento.append("\n");
                }
                in.close();

			    startTime = System.nanoTime(); // inicializa o contador de tempo.	
			    
			    SubQueryExecutor sqe = new SubQueryExecutor(fragmento.toString());
				sqe.setDatabaseInfo("localhost", 1984, "admin", "admin", "expdb", "BASEX");
				
                String query = sqe.getExecutionContext().getQueryObj().getInputQuery();
                SubQuery sbq = sqe.getExecutionContext().getSubQueryObj();
                String filePath = "partialResult_intervalBeginning_"+ String.format("%1$020d", Integer.parseInt(SubQuery.getIntervalBeginning(query,sbq))) + ".xml";
		        String completePath = svpHome + "/partialResults/" + filePath;
		        FileOutputStream fos = new FileOutputStream(completePath);
				
				sqe.executeQuery(fos);
				fos.flush();
				fos.close();
			    
			    // tempo de leitura de arquivo + execução da consulta
			    delay = ((System.nanoTime() - startTime)/1000000);
			     
			    System.out.println("ParallelExecution.run(): - thread: " + identificacao + ", myrank: " + this.getMyrank() + " -- Execution Time: " + delay);
			    
				try {
            	   Thread.sleep(2000);  // tempo especificado em milissegundos	            	   
                }
                catch(InterruptedException e) {}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SubQueryExecutionException e1) {
                e1.printStackTrace();
            } catch (DatabaseException e1) {
                e1.printStackTrace();
            } 
        }
        
        public ParallelExecution(int rank, int processorsPerNode, String svpHome) {               
                this.setMyrank(rank);
        		this.setIdentificacao((this.getMyrank()*processorsPerNode) + cont); 
        		this.svpHome = svpHome;
                
        		// variável global
        		cont++;
        }
}

