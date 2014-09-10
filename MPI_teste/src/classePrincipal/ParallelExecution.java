package classePrincipal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

public class ParallelExecution implements Runnable{
        
        private static int cont = 0; // global
        private int identificacao;
        private int myrank;        
        static ThreadMXBean tmb = ManagementFactory.getThreadMXBean();

		private FileReader in = null;
        private BufferedReader buff = null;
        private String query = "";
        private String line = null;
        private XQConnection xqc = null;
        private XQExpression xqe = null;
        private XQResultSequence xqr = null;
        private SubQuery sbq = null;
        
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
        
        @SuppressWarnings("static-access")
		public void run() {
        	        	
        	//this.sbq.deleteCollection(Integer.toString(this.getIdentificacao()));            	
            
        	try {
        		
        		Query q = Query.getUniqueInstance(true);
        		long startTime; 
        		long delay;
        		long tmp;       
				in = new FileReader("/home/users/carlarod/SVP/fragmento_" + this.getIdentificacao() + ".txt");
        		buff = new BufferedReader(in);
				
				while((line = buff.readLine()) !=null){    
					if (!line.toUpperCase().contains("<ORDERBY>") && !line.toUpperCase().contains("<ORDERBYTYPE>") 
							&& !line.toUpperCase().contains("<AGRFUNC>")) {							
						query = query + " " + line;
					}
					else {
						// obter as cláusulas do orderby e de funçoes de agregaçao
						if (line.toUpperCase().contains("<ORDERBY>")){
							String orderByClause = line.substring(line.indexOf("<ORDERBY>")+"<ORDERBY>".length(), line.indexOf("</ORDERBY>"));
							q.setOrderBy(orderByClause);
						}
						
						if (line.toUpperCase().contains("<ORDERBYTYPE>")){
							String orderByType= line.substring(line.indexOf("<ORDERBYTYPE>")+"<ORDERBYTYPE>".length(), line.indexOf("</ORDERBYTYPE>"));							
							q.setOrderByType(orderByType);
						}
						
						if (line.toUpperCase().contains("<AGRFUNC>")){ // soma 1 para excluir a tralha contida apos a tag <AGRFUNC>
							
							String aggregateFunctions = line.substring(line.indexOf("<AGRFUNC>")+"<AGRFUNC>".length(), line.indexOf("</AGRFUNC>"));
														
							if (!aggregateFunctions.equals("") && !aggregateFunctions.equals("{}")) {
								String[] functions = aggregateFunctions.split(","); // separa todas as funções de agregação utilizadas no return statement.
							
								if (functions!=null) {
									
									for (String keyMap: functions) {
							
										String[] hashParts = keyMap.split("=");
										
										if (hashParts!=null) {
							
											q.setAggregateFunc(hashParts[0], hashParts[1]); // o par CHAVE, VALOR
										}
									}
									
								}
							}						
						}						
					}
				}
								
					startTime = System.nanoTime(); // inicializa o contador de tempo.	
					this.sbq.deleteCollection(Integer.toString(this.getIdentificacao()));
				    this.sbq.executeSubQuery(query, Integer.toString(this.getIdentificacao()));
				    
				    // tempo de leitura de arquivo + execução da consulta
				    delay = ((System.nanoTime() - startTime)/1000000);
				    
				    System.out.println("ParallelExecution.run(): - thread:" + identificacao + ", myrank:" + this.getMyrank() + " -- Execution Time:" + delay);
				    
				    // tempo das outras sub-consultas já executadas.
				    tmp = this.sbq.getStartTime(); 				    
   				    
				    // soma com o tempo gasto para execução da sub-consulta atual.
				    tmp = tmp + delay; 
				    
				    // atualiza variável
				    q.settotalExecutionTime(q.gettotalExecutionTime() + delay);
				    this.sbq.setStartTime(tmp);
					
					try {
		            	   Thread.sleep(2000);  // tempo especificado em milissegundos	            	   
		            	   
		               }
		               catch(InterruptedException e) {}
	        	
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				finally {
					try {
						if (xqr!=null) xqr.close();
						if (xqe!=null) xqe.close();
						if (xqc!=null) xqc.close();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					 
				}
                    
				
        }
        
        public ParallelExecution(int rank, int processorsNumberPerNode) throws XQException, IOException {               
                this.setMyrank(rank);
                // ALTERAR SEMPRE AQUI PARA MULTIPLICAR PELO NUMERO DE THREADS POR NÓ.
        		this.setIdentificacao((this.getMyrank()*processorsNumberPerNode) + cont); 
                
        		// variável global
        		cont++;
               
               //  Nao pode criar, pois os resultados sao apagados.
              this.sbq = SubQuery.getUniqueInstance(true);
   		   	  this.sbq.setRunningSubqueries(true);
        }
}

