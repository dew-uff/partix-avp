package wrapper.sedna;

import java.io.BufferedReader;  
import java.io.FileReader;
import java.io.IOException;  
import java.io.InputStream;
import java.io.InputStreamReader;  
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;  
import java.net.Socket;  

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
  
public class Server {  
  
    public static void main(String[] args) {  
          
        //Declaro o ServerSocket  
        ServerSocket serv=null;   
          
        //Declaro o Socket de comunicação  
        Socket s= null;  
          
        //Declaro o leitor para a entrada de dados  
        BufferedReader entrada=null; 
        // Declaro a saida de dados
        PrintStream ps = null;
        String resp="";  
        
                  
        try{  
              
            //Cria o ServerSocket na porta 7000 se estiver disponível  
            serv = new ServerSocket(7000);  
          
            //Aguarda uma conexão na porta especificada e cria retorna o socket que irá comunicar com o cliente  
            s = serv.accept();  

            
            entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
                      
            System.out.println("retornando para o Server"); 

            XQueryWrapper wrapper = new XQueryWrapper();
            XQueryResult result = new XQueryResult();
            //Cria a Stream de saida de dados 
            
           
            System.out.println ("executa a consulta no banco local");

            //Lendo a consulta submetida no mediador         
            String consulta = "";
            String linha;
            String resultadoConsulta = "";
            boolean flag = true;
            while (flag){
            	linha = entrada.readLine();
            	if (linha == null || linha.equals(""))
            	{
            		flag = false;
            	}
            	consulta = consulta + " " + linha;
            	System.out.print("Linha: "+linha);
            }
            System.out.print("Consulta: "+consulta);
       
            //Executa a consulta no banco local
            result = wrapper.executeXQuery(consulta);
            
            //Formatando o retorno da consulta para o formato XML
            System.out.println("Verificando o retorno da variavel result:"+result.getResult());
            try {
				resultadoConsulta = XMLUtil.format(result.getResult());
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
            System.out.println("O retorno da consulta identada fica assim:"+resultadoConsulta);
            
            result.setResult(resultadoConsulta);
			
            
            ObjectOutputStream objOut = new ObjectOutputStream(s.getOutputStream());
            
            objOut.writeObject(result);
         
            objOut.flush();
            objOut.close();
            entrada.close();
        
        //trata possíveis excessões de input/output. Note que as excessões são as mesmas utilizadas para as classes de java.io    
        }catch(IOException e){  
          
            //Imprime uma notificação na saída padrão caso haja algo errado.  
            System.out.println("Algum problema ocorreu para criar ou receber o socket.");  
          
        }finally{  
              
        	 
             
            try{  
                  
                //Encerro o socket de comunicação  
                s.close();  
                  
                
                  
            }catch(IOException e){  
            	
            	//Encerro o ServerSocket  
                
            }  
        }  
      
          
          
          
          
    }  
} 
