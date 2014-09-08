package wrapper.sedna;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;  
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;  
import java.net.Socket; 
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

  
public class Client  {  
  
    public static void main(String[] args)  {  
          
        //Declaro o socket cliente  
        Socket s = null;  
        String b;
          
        //Declaro a Stream de saida de dados  
        PrintStream ps = null; 
        InputStream in = null;
        BufferedReader entrada=null; 
                  
        try{  
              
            //Cria o socket com o recurso desejado na porta especificada  
            s = new Socket("127.0.0.1",7000);
            // Canal para envio dos dados
            ps = new PrintStream(s.getOutputStream());
            //Le arquivos
            FileInputStream stream = new FileInputStream("C:/Users/Taty/workspace/WrapperSedna/src/wrapper/exist/consulta.txt");
            InputStreamReader streamReader = new InputStreamReader(stream); 
            // Coloca o conteudo do arquivo no buffer
            BufferedReader buffReader = new BufferedReader(streamReader); 
            String linha;  
            String linha2;  
            // Envia a consulta para o Server
            while ((linha = buffReader.readLine()) != null) 
            {  
                ps.println(linha); 
            	System.out.println(linha);  
            }  
            buffReader.close(); 
            entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
            while ((linha2 = entrada.readLine())!= null){
            	System.out.println(linha2);
            	
            }
           entrada.close(); 
            
            
            //Imprime uma linha para a stream de saída de dados  
            //Trata possíveis exceções  
        }catch(IOException e){  
              
            System.out.println("Algum problema ocorreu ao criar ou enviar dados pelo socket.");  
          
        }finally{  
              
            try{  
                  
                //Encerra o socket cliente  
                s.close();  
                  
            }catch(IOException e){}  
          
        }  
  
    }

	} 