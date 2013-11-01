package mediadorxml.tests;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import mediadorxml.algebra.operators.AbstractOperator;
import mediadorxml.catalog.CatalogManager;
import mediadorxml.engine.XQueryEngine;
import mediadorxml.graphics.PlanPanel;
import mediadorxml.javaccparser.ParseException;
import mediadorxml.tests.graph.ImageSelection;

import mediadorxml.fragmentacaoVirtualAdaptativa.AdaptativeVirtualPartitioning;
import mediadorxml.fragmentacaoVirtualSimples.DecomposeQuery;
import mediadorxml.fragmentacaoVirtualSimples.ExistsJoinOperation;
import mediadorxml.fragmentacaoVirtualSimples.FinalResult;
import mediadorxml.fragmentacaoVirtualSimples.Query;
import mediadorxml.fragmentacaoVirtualSimples.SimpleVirtualPartitioning;
import mediadorxml.fragmentacaoVirtualSimples.SubQuery;

public class TesteXQueryPlan extends JFrame implements ClipboardOwner {

	private static final long serialVersionUID = 3699229248000609422L;
	private JPanel jContentPane = null;
	private JButton jButtonRun = null;
	private JTextArea jTextAreaInput = null;
	private JScrollPane scrollPaneInput = null;
	private JTextArea jTextAreaOutput = null;
	private JScrollPane scrollPaneOutput = null;
	
	private JPanel jPanelRadioButtons = null;
	private JPanel jPanelTextField = null;
	private ButtonGroup buttonGroup = null;
	private JRadioButton jRadioButtonXQuery = null;
	private JRadioButton jRadioButtonFragVirtualSimples = null;

	private JLabel jLabel = null;
	private JTextField jTextField = null;
	
	private boolean executouFVS = false;
	private String originalQuery = "";
	
	private JRadioButton jRadioButtonFragVirtualAdaptativa = null;
	private JRadioButton jRadioButtonResultado = null;	
	
	private String xquery = null;
	private String userInput = null; // indica a query original do usu�rio.
	
	// docQueries: recebe as sub-consultas com express�es doc(), geradas quando a consulta informada pelo usu�rio possui a express�o collection().
	// para cada documento existente na cole��o informada, uma nova sub-consulta � gerada.
	private ArrayList<String> docQueries = null;
	// docQueriesWithoutFragmentation: recebe as proprias consultas de entrada, quando nao ha relacao de 1 para n, dentro do documento XML consultado.
	private ArrayList<String> docQueriesWithoutFragmentation = null;
	private XQueryEngine engine = null;
	long startTime = 0;
	
	private Clipboard clipboard;

	/**
	 * This method initializes jButtonRun	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonRun() {
		if (jButtonRun == null) {
			jButtonRun = new JButton();			
			jButtonRun.setText("Copiar");
			jButtonRun.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try{
						
						ImageSelection selection = new ImageSelection(new Robot().createScreenCapture(TesteXQueryPlan.this.getBounds())); 
							
						clipboard.setContents(selection, TesteXQueryPlan.this);
					}
					catch(Exception ex){
						jTextAreaOutput.setText(ex.getMessage() + "\r\n" + ex.getStackTrace());
					}
				}
			});		   

		}
		return jButtonRun;
	}
	
	private void showInitialFragments() throws IOException{
		
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
								 " fragmentos na fragmenta��o virtual simples >>>> \r\n\r\n";
				int i = 1;
				
				for ( String initialFragments : sbq.getSubQueries() ) {

					String basePath2 = cm.getSVP_Directory() + "/fragmento_"+(i-1)+".txt";	
					FileWriter writer2 = new FileWriter(basePath2,false);
					PrintWriter saida2 = new PrintWriter(writer2);				

					saida2.print("<ORDERBY>" + q.getOrderBy() + "</ORDERBY>\r\n");
					saida2.print("<ORDERBYTYPE>" + q.getOrderByType() + "</ORDERBYTYPE>\r\n");
					saida2.print("<AGRFUNC>" + (q.getAggregateFunctions()!=null?q.getAggregateFunctions():"") + "</AGRFUNC>#\r\n");
					
					saida2.print(initialFragments);
					saida2.close();
					writer2.close();
					results = results + i + "#\r\n" + initialFragments + "\r\n";
					i++;					
				}				
			}
			else {
				
				if ( this.docQueriesWithoutFragmentation != null && this.docQueriesWithoutFragmentation.size() >0 ) { // para consulta que nao foram fragmentadas pois nao ha relacionamento de 1 para n.

					results = "<<<<  N�o foram gerados fragmentos, pois os elementos dos documentos especificados na consulta \r\n n�o possuem relacionamento 1:N,"
					               	+" condi��o necess�ria para a fragmenta��o. \r\n Desta forma, as consultas a serem executadas s�o:  >>>> \r\n\r\n";
					int i = 1;
					
					for ( String initialFragments : this.docQueriesWithoutFragmentation ) {							
						results = results + i + "=\r\n" + initialFragments + "\r\n";
						i++;					
					}			
				}
				else { // nao gerou fragmentos e nao ha consultas de entrada. Ocorreu algum erro durante o parser da consulta. 
					results = "Erro ao gerar fragmentos. A consulta de entrada est� incorreta ou n�o � uma consulta v�lida para o parser do sistema.";					
				}
			}
			
			setOutputText(results);
			saida.print(results);
			saida.close();
			writer.close();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	private void executeXQuery() throws ParseException, Exception {
				
		
		if (jTextField==null || jTextField.getText().trim().equals("")) {
			JOptionPane.showMessageDialog(null,"Por favor, informe o n�mero de processadores.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);					
		}
		else {
		
			try{			
				
				Integer.parseInt(jTextField.getText());
				
				ExistsJoinOperation ej = new ExistsJoinOperation(jTextAreaInput.getText());
				ej.verifyInputQuery();
				Query q = Query.getUniqueInstance(true);
				q.setLastReadCardinality(-1);
				q.setJoinCheckingFinished(false);				
				
				
				if ((xquery == null) || (!xquery.equals(jTextAreaInput.getText()))){ 
					startTime = System.nanoTime();
					xquery = jTextAreaInput.getText(); //  consulta de entrada					
				}		
				
				
				if ( q.getqueryExprType()!= null && !q.getqueryExprType().contains("collection") ) { // se a consulta de entrada n�o cont�m collection, execute a fragmenta��o virtual.
				
					engine = new XQueryEngine();
					engine.execute(xquery, true); // Para debugar o parser, passe o segundo par�metro como true.				
					
					q.setJoinCheckingFinished(true);
					
					if (q.isExistsJoin()){
						q.setOrderBy("");						
						engine.execute(xquery, true); // Executa pela segunda vez, por�m desta vez fragmenta apenas um dos joins
					}				
					
				}
				else {	// se contem collection			
									
					// Efetua o parser da consulta para identificar os elementos contidos em fun��es de agrega��o ou order by, caso existam.
					q.setOrderBy("");
					engine = new XQueryEngine();
					engine.execute(originalQuery, true);
					
					if (q.getPartitioningPath()!=null && !q.getPartitioningPath().equals("")) {
						SubQuery sbq = SubQuery.getUniqueInstance(false); 
						SimpleVirtualPartitioning svp = new SimpleVirtualPartitioning();
						svp.setCardinalityOfElement(q.getLastCollectionCardinality());
						svp.setNumberOfNodes(Integer.parseInt(jTextField.getText()));						
						svp.getSelectionPredicateToCollection(q.getVirtualPartitioningVariable(), q.getPartitioningPath(), xquery);											
						q.setAddedPredicate(true);
					}
					
					//engine = new XQueryEngine();
					//engine.execute(originalQuery, false);				
				}
				
				showInitialFragments();
				
				jTextAreaInput.setText(userInput);
			} catch (NumberFormatException n) {
				JOptionPane.showMessageDialog(null,"O n�mero de processadores deve ser um n�mero inteiro.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
				n.printStackTrace();
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
		}
				
	}
	
	private JPanel getJtextField(){
		
		jLabel = new JLabel();
		jLabel.setText("N. de processadores:");
		jTextField = new JTextField();
		jTextField.setText("");
		jTextField.setPreferredSize(new Dimension(45,25));
		
		jPanelTextField = new JPanel();
		jPanelTextField.add(jLabel);
		jPanelTextField.add(jTextField);		
		
		return jPanelTextField;
	}
		
	private JPanel getRadioButtonGroup(){		
		
		if (buttonGroup == null){
			jRadioButtonXQuery = new JRadioButton("Parser da consulta"); //XQuery
			jRadioButtonFragVirtualSimples = new JRadioButton("Frag. Virtual Simples (FVS)"); 			
			jRadioButtonFragVirtualAdaptativa = new JRadioButton("Frag. Virtual Adaptativa (FVA)");
			jRadioButtonResultado = new JRadioButton("Resultado");
			
			buttonGroup = new ButtonGroup();
			buttonGroup.add(jRadioButtonXQuery);
			buttonGroup.add(jRadioButtonFragVirtualSimples);
			buttonGroup.add(jRadioButtonFragVirtualAdaptativa);
			buttonGroup.add(jRadioButtonResultado);
			
			jPanelRadioButtons = new JPanel();
			
			jPanelRadioButtons.add(getJtextField(), java.awt.BorderLayout.EAST);
			
			jPanelRadioButtons.add(jRadioButtonXQuery);
			jPanelRadioButtons.add(jRadioButtonFragVirtualSimples);
			jPanelRadioButtons.add(jRadioButtonFragVirtualAdaptativa);
			jPanelRadioButtons.add(jRadioButtonResultado);			
			
			jRadioButtonXQuery.addActionListener(new java.awt.event.ActionListener() {
				
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					JProgressBar jp = new JProgressBar();
					jContentPane.add(jp);
					jp.setIndeterminate(true);
					jContentPane.repaint();
					setSize(1280, 995); //(width, height)
										
					JOptionPane.showMessageDialog(null, "Est� opera��o pode levar alguns segundos.\nA barra central desaparecer� ap�s a conclus�o.\n\nClique OK para continuar.");										
					
					String returnClause = jTextAreaInput.getText().trim();
					returnClause = returnClause.substring(returnClause.indexOf("return")+6, returnClause.length()); // obtem a string apos a clausula return.
					
					if ( jTextAreaInput.getText().trim().indexOf("<" ) > 0 
							|| jTextAreaInput.getText().trim().lastIndexOf(">") != jTextAreaInput.getText().trim().length()-1) {
						
						JOptionPane.showMessageDialog(null,"A consulta de entrada deve iniciar e terminar com um elemento construtor. Exemplo: <resultado> { for $var in ... } </resultado>.\r\nPor favor, corrija a consulta antes de continuar.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
					}
					
					else if (jTextAreaInput.getText().trim().toUpperCase().indexOf("/TEXT()") != -1) {
						
						JOptionPane.showMessageDialog(null,"O parser deste programa n�o aceita a fun��o text(). Especifique somente os caminhos xpath para acessar os elementos nos documentos XML.\r\nPor favor, corrija a consulta antes de continuar.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
					}
					
					else if (returnClause.trim().charAt(0) != '<') {
						
						JOptionPane.showMessageDialog(null,"� obrigat�ria a especifica��o de um elemento XML ap�s a cl�usula return.\r\nEx.: <results> { for $var ... return <elemName> ... </elemName> } </results>","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
					}
					
					else {
					
						try{
							String inputStr = jTextAreaInput.getText();							
							setOutputText(inputStr);					
							
							Query q = Query.getUniqueInstance(true);
							
							/* Define o tipo de consulta (collection() ou doc()) e, caso seja sobre uma cole��o 
							 * retorna as sub-consultas geradas, armazenando-as no objeto docQueries.
							 */
							q.setInputQuery(inputStr);
							docQueries = q.setqueryExprType(inputStr);
							
							if ( docQueries!=null && docQueries.size() > 0 ){ // � diferente de null, quando consulta de entrada for sobre uma cole��o
								
								docQueriesWithoutFragmentation = docQueries;								
								
								String subQueries = "";
								int i = 1;
								
								// Exibe na tela a esquerda as sub-consultas geradas para o usu�rio.  
								for (String docQry : docQueries) {
									subQueries = subQueries + i + "=\r\n" + docQry + "\r\n";																
									setOutputText(docQry);	
									i++;
								}
								
								setOutputText(" <<<< Foram geradas " + (i-1) + " sub-consulta(s) para a(s) cole��o(�es) de entrada. >>>> \r\n" + subQueries);
							}
							
							else if (q.getqueryExprType()!=null && q.getqueryExprType().equals("document")) { // consulta de entrada sobre um documento. 
								q.setInputQuery(inputStr);
							}
							
							else if (q.getqueryExprType()!=null && q.getqueryExprType().equals("collection")) { // consulta de entrada sobre uma cole��o.
								setOutputText("Erro ao gerar sub-consultas para a cole��o indicada. Verifique a consulta de entrada.");
							}
						} 							
						catch(Exception ex){
							setOutputText(ex.getMessage() + "\r\n" + ex.getStackTrace());
						}
							
						} // fim else que verifica se o constructor foi especificado na consulta de entrada.
					
					jContentPane.remove(jp);
				}
			});
			
			
			jRadioButtonResultado.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {					
					
					String returnClause = jTextAreaInput.getText().trim();
					returnClause = returnClause.substring(returnClause.indexOf("return")+6, returnClause.length()); // obtem a string apos a clausula return.
					
					if (jTextAreaInput.getText().trim().indexOf("<") > 0 
							|| jTextAreaInput.getText().trim().lastIndexOf(">") != jTextAreaInput.getText().trim().length()-1) {
						JOptionPane.showMessageDialog(null,"A consulta de entrada deve iniciar e terminar com um elemento construtor. Exemplo: <resultado> { for $var in ... } </resultado>.\r\nPor favor, corrija a consulta antes de continuar.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
					}
					
					else if (jTextAreaInput.getText().trim().toUpperCase().indexOf("/TEXT()") != -1) {
						
						JOptionPane.showMessageDialog(null,"O parser deste programa n�o aceita a fun��o text(). Especifique somente os caminhos xpath para acessar os elementos nos documentos XML.\r\nPor favor, corrija a consulta antes de continuar.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
					}
					
					else if (returnClause.trim().charAt(0) != '<') {
						
						JOptionPane.showMessageDialog(null,"� obrigat�ria a especifica��o de um elemento XML ap�s a cl�usula return.\r\nEx.: <results> { for $var ... return <elemName> ... </elemName> } </results>","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
					}
					
					else {
					
						try{					
							
							Query q;
							AdaptativeVirtualPartitioning adp; 
							Integer.parseInt(jTextField.getText());
													
							if (!executouFVS) {
					
								q = Query.getUniqueInstance(true);						
								jRadioButtonFragVirtualSimples.doClick();
								q = Query.getUniqueInstance(true);
							}
							else {
								q = Query.getUniqueInstance(true);
								jRadioButtonFragVirtualSimples.doClick();
								q = Query.getUniqueInstance(true);
							}						
							
							SubQuery sbq = SubQuery.getUniqueInstance(true);
							sbq.deleteFilesFromDirectory();
							sbq.deleteCollection();
							FinalResult fr = new FinalResult();
							
							if ( sbq.getSubQueries()!=null && sbq.getSubQueries().size() > 0 ){
								
								for ( String initialFragments : sbq.getSubQueries() ) {							
									
									sbq.setRunningSubqueries(true);					
									q = Query.getUniqueInstance(true);
									
									if ( initialFragments.indexOf("[position() = ") == -1) { // A especificacao do inicio do intervalo nao eh uma igualdade.
										// executa a fase de ajuste do fragmento apenas se o fragmento tiver tamanho maior que 1.
										adp = new AdaptativeVirtualPartitioning(initialFragments);			
									}
									else {
										sbq.executeSubQuery(initialFragments); // executa diretamente o fragmento.
									}
									
									q = Query.getUniqueInstance(true);
									sbq.setRunningSubqueries(false);
									q = Query.getUniqueInstance(true);
								}
							}
							else {
								// subqueries de consultas que nao sofreram fragmentacao por nao ter elementos com cardinalidade maior que 1.
								if ( docQueriesWithoutFragmentation != null && docQueriesWithoutFragmentation.size() > 0 ) {
									
									sbq.setDocIdentifier("0"); // para identificar os resultados de cada sub-consulta doc() gerada, evitando conflito de insercao de documentos com mesmo nome na base de dados.
									for ( String initialFragments : docQueriesWithoutFragmentation ) {							
										
										sbq.setRunningSubqueries(true);							
										q = Query.getUniqueInstance(true);						
										sbq.executeSubQuery(initialFragments);
										q = Query.getUniqueInstance(true);
										sbq.setRunningSubqueries(false);
										q = Query.getUniqueInstance(true);
									}
								}							
							}
							
							String result = fr.getFinalResult();
							
							if ( result != null && result.trim().length() > 0 ){
							
								setOutputText(" <<<< Resultado >>>> \r\n" + result);
							}
							else {
								setOutputText(" <<<< Erro ao recuperar resultados >>>>");
							}
						
						} catch (NumberFormatException n) {
							JOptionPane.showMessageDialog(null,"O n�mero de processadores deve ser um n�mero inteiro.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
							n.printStackTrace();
						}
						catch(Exception ex){
							setOutputText(ex.getMessage() + "\r\n" + ex.getStackTrace());
						}
						
					} // fim else que verifica se o elemento construtor foi especificado.
					
				}
					
			});
			
			jRadioButtonFragVirtualAdaptativa.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
										
					String returnClause = jTextAreaInput.getText().trim();
					returnClause = returnClause.substring(returnClause.indexOf("return")+6, returnClause.length()); // obtem a string apos a clausula return.
					
					if (jTextAreaInput.getText().trim().indexOf("<") > 0 
							|| jTextAreaInput.getText().trim().lastIndexOf(">") != jTextAreaInput.getText().trim().length()-1) {
						JOptionPane.showMessageDialog(null,"A consulta de entrada deve iniciar e terminar com um elemento construtor. Exemplo: <resultado> { for $var in ... } </resultado>.\r\nPor favor, corrija a consulta antes de continuar.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
					}
					
					else if (jTextAreaInput.getText().trim().toUpperCase().indexOf("/TEXT()") != -1) {
						
						JOptionPane.showMessageDialog(null,"O parser deste programa n�o aceita a fun��o text(). Especifique somente os caminhos xpath para acessar os elementos nos documentos XML.\r\nPor favor, corrija a consulta antes de continuar.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
					}
					
					else if (returnClause.trim().charAt(0) != '<') {
						
						JOptionPane.showMessageDialog(null,"� obrigat�ria a especifica��o de um elemento XML ap�s a cl�usula return.\r\nEx.: <results> { for $var ... return <elemName> ... </elemName> } </results>","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
					}
					
					else {
					
						try{					
							CatalogManager cm = CatalogManager.getUniqueInstance();
							Query q;
							AdaptativeVirtualPartitioning adp = null;	
							Integer.parseInt(jTextField.getText());
							
							String basePath = cm.getAVP_Directory() + "/fragmentos.txt";
							FileWriter writer = new FileWriter(basePath,false);
							PrintWriter saida = new PrintWriter(writer);
							
						    File f = new File(cm.getAVP_Directory());	
						    File[] files = f.listFiles (new FileFilter() {   
						            public boolean accept(File pathname) {   
						                return pathname.getName().toLowerCase().endsWith(".txt");   
						            }   
						        });   
						    
						    for (int i = 0; i < files.length; ++i) {   
						            files[i].delete();   
						    }
						    
													
							if (!executouFVS) {
								q = Query.getUniqueInstance(true);						
								jRadioButtonFragVirtualSimples.doClick();
								q = Query.getUniqueInstance(true);
							}
							else {
								q = Query.getUniqueInstance(true);
								jRadioButtonFragVirtualSimples.doClick();
								q = Query.getUniqueInstance(true);
							}							
							
							SubQuery sbq = SubQuery.getUniqueInstance(true);
							sbq.deleteFilesFromDirectory();
							sbq.deleteCollection();							
							
							if ( sbq.getSubQueries()!=null && sbq.getSubQueries().size() > 0 ){
								
								int j = 0;								
								
								for ( String initialFragments : sbq.getSubQueries() ) {							
							
									sbq.setRunningSubqueries(true);					
									q = Query.getUniqueInstance(true);
									
									if ( initialFragments.indexOf("[position() = ") == -1) { // A especificacao do inicio do intervalo nao eh uma igualdade.
										// executa a fase de ajuste do fragmento apenas se o fragmento tiver tamanho maior que 1.
										adp = new AdaptativeVirtualPartitioning(initialFragments);										
									}
									
									q = Query.getUniqueInstance(true);
									sbq.setRunningSubqueries(false);
									q = Query.getUniqueInstance(true);
									
									if ( adp != null && adp.getFinalFragments() != null && adp.getFinalFragments().size() > 0 ){
										
										String results = "";
										int i = 1;
										
										for ( String finalFragment : adp.getFinalFragments() ) {											
											
											String basePath2 = cm.getAVP_Directory() + "/fragmento_"+(i+j)+".txt";
											FileWriter writer2 = new FileWriter(basePath2,false);
											PrintWriter saida2 = new PrintWriter(writer2);

											saida2.print(finalFragment);
											saida2.close();
											writer2.close();
											
											results = results + Integer.toString(i+j) + "#\r\n" + finalFragment + "\r\n";
											i++;					
										}	
										
										j = j + i - 1;
										saida.print(results);
									}
									
								} // fim for
								

								saida.close();
								writer.close();
								
								setOutputText(" <<<< Fragmenta��o virtual adaptativa conclu�da. >>>> \r\n Verifique no diret�rio: \r\n " 
										+ cm.getAVP_Directory() + "/fragmentos.txt");
							}
							else {
								// subqueries de consultas que nao sofreram fragmentacao por nao ter elementos com cardinalidade maior que 1.
								if ( docQueriesWithoutFragmentation != null && docQueriesWithoutFragmentation.size() > 0 ) {
									
									setOutputText(" <<<< A consulta de entrada n�o gerou fragmentos virtuais simples, logo n�o � poss�vel realizar a fragmenta��o virtual adaptativa. >>>>");
								}
															
							}					
						
						} catch (NumberFormatException n) {
							JOptionPane.showMessageDialog(null,"O n�mero de processadores deve ser um n�mero inteiro.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
							n.printStackTrace();
						}
						catch(Exception ex){
							setOutputText(ex.getMessage() + "\r\n" + ex.getStackTrace());
						}
						
					} // fim else que verifica se o elemento construtor foi especificado.
					
				}
					
			});
			
			
			jRadioButtonFragVirtualSimples.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {					
										
					String returnClause = jTextAreaInput.getText().trim();
					returnClause = returnClause.substring(returnClause.indexOf("return")+6, returnClause.length()); // obtem a string apos a clausula return.
					
					if (jTextField==null || jTextField.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(null,"Por favor, informe o n�mero de processadores.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);					
					}
					else {					
					
						if (jTextAreaInput.getText().trim().indexOf("<") > 0 
								|| jTextAreaInput.getText().trim().lastIndexOf(">") != jTextAreaInput.getText().trim().length()-1) {
						
							JOptionPane.showMessageDialog(null,"A consulta de entrada deve iniciar e terminar com um elemento construtor. Exemplo: <resultado> { for $var in ... } </resultado>.\r\nPor favor, corrija a consulta antes de continuar.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
						}
						
						else if (jTextAreaInput.getText().trim().toUpperCase().indexOf("/TEXT()") != -1) {
							
							JOptionPane.showMessageDialog(null,"O parser deste programa n�o aceita a fun��o text(). Especifique somente os caminhos xpath para acessar os elementos nos documentos XML.\r\nPor favor, corrija a consulta antes de continuar.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
						}
						
						else if (jTextAreaInput.getText().trim().toUpperCase().indexOf("/TEXT()") != -1) {
	
							JOptionPane.showMessageDialog(null,"O parser deste programa n�o aceita a fun��o text(). Especifique somente os caminhos xpath para acessar os elementos nos documentos XML.\r\nPor favor, corrija a consulta antes de continuar.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
						}
						
						else if (returnClause.trim().charAt(0) != '<') {
							
							JOptionPane.showMessageDialog(null,"� obrigat�ria a especifica��o de um elemento XML ap�s a cl�usula return.\r\nEx.: <results> { for $var ... return <elemName> ... </elemName> } </results>","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
						}
						
						else {
							jTextAreaInput.requestFocus();
							jRadioButtonXQuery.doClick();
														
							try{
								Integer.parseInt(jTextField.getText());
								SimpleVirtualPartitioning svp = SimpleVirtualPartitioning.getUniqueInstance(false);
								Query q = Query.getUniqueInstance(true);
								if (q.getqueryExprType()!=null && q.getqueryExprType().equals("collection")){
									
									if (docQueries!=null){
										
										originalQuery = jTextAreaInput.getText();
										for (String docQry : docQueries) {
											
											jTextAreaInput.setText(docQry);
											executeXQuery();											
										}
										
										jTextAreaInput.setText(originalQuery);
									}
								}
								else {
									
									svp.setNumberOfNodes(Integer.parseInt(jTextField.getText().trim()));
									svp.setNewDocQuery(true);														
									executeXQuery();							
								}

							} catch (NumberFormatException n) {
								JOptionPane.showMessageDialog(null,"O n�mero de processadores deve ser um n�mero inteiro.","Fragmenta��o Virtual Adaptativa", JOptionPane.INFORMATION_MESSAGE);
								n.printStackTrace();
							}
							catch(Exception ex){
								setOutputText(ex.getMessage() + "\r\n" + ex.getStackTrace() + "\r\n\r\n--\r\n");
							}
							
							executouFVS = true; // indica que a fragmenta��o virtual simples j� foi executada uma vez.
						} // fim else que verifica se o elemento contrutor foi especificado na consulta de entrada.
					
					}							
				}
			});		
			
		}
		return jPanelRadioButtons;
	}

	/**
	 * This method initializes jTextAreaInput	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JScrollPane getJScrollPaneInput() {
		if (jTextAreaInput == null) {		
						
			userInput = "" + 
			" <results> \r\n" +
			" { \r\n"+
			"   for $article in doc('dblp.xml')/dblp/article \r\n"+
			"   where $article/journal = \"ACM SIGMOD Digital Review\" \r\n"+
			" return \r\n"+
			"  <ACM_articles> \r\n"+
			"    {$article/title} \r\n"+
			"    {$article/year} \r\n" + 
			"    {$article/volume} \r\n"+		
			"  </ACM_articles> \r\n"+
			" } \r\n"+ 
			" </results>";
						
			jTextAreaInput = new JTextArea(userInput,10,30);
			scrollPaneInput = new JScrollPane(jTextAreaInput);
		}
				
		/**
		 * Para evitar que o usu�rio altere algo na consulta de entrada e esta altera��o n�o seja capturada ao gerar os fragmentos,
		 * o m�todo abaixo reseta todos os objetos que armazenam resultados e vari�veis da consulta de entrada a cada vez que o 
		 * usu�rio retira o foco da textarea de entrada, ou seja, a cada vez que ocorre o evento de perda de foco (focusLost).
		 */
		jTextAreaInput.addFocusListener(new java.awt.event.FocusAdapter() {   
            public void focusLost(java.awt.event.FocusEvent e) {
            	
            	Query.getUniqueInstance(false);
                SimpleVirtualPartitioning.getUniqueInstance(false);
                SubQuery.getUniqueInstance(false);	            	
                DecomposeQuery.getUniqueInstance(false);                  
            }   
        });
		
		jTextAreaInput.addKeyListener(new java.awt.event.KeyAdapter() {
			
			public void keyReleased(java.awt.event.KeyEvent e) {			

				userInput = jTextAreaInput.getText();				
			}
			
		});
		
		jTextField.addFocusListener(new java.awt.event.FocusAdapter() {   
            public void focusLost(java.awt.event.FocusEvent e) {
            	
            	Query.getUniqueInstance(false);
                SimpleVirtualPartitioning.getUniqueInstance(false);
                SubQuery.getUniqueInstance(false);	            	
                DecomposeQuery.getUniqueInstance(false);                  
            }   
        });
		
		return scrollPaneInput;
	}

	/**
	 * This method initializes jTextAreaOutput	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JScrollPane getJScrollPaneOutput() {
		if (jTextAreaOutput == null) {
			jTextAreaOutput = new JTextArea(10,60);
			scrollPaneOutput = new JScrollPane(jTextAreaOutput);
		}
		return scrollPaneOutput;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TesteXQueryPlan application = new TesteXQueryPlan();
		application.show();
	}

	/**
	 * This is the default constructor
	 */
	public TesteXQueryPlan() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(810, 399);
		this.setContentPane(getJContentPane());
		this.setTitle("Fragmenta��o Virtual sobre Bases de Dados XML");
		
		clipboard = getToolkit().getSystemClipboard();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		
		if (jContentPane == null) {
			jContentPane = new JPanel();			
			jContentPane.setLayout(new BorderLayout());			
			jContentPane.add(getJButtonRun(), java.awt.BorderLayout.SOUTH);			
			jContentPane.add(getRadioButtonGroup(), java.awt.BorderLayout.NORTH);			
			jContentPane.add(getJScrollPaneInput(), java.awt.BorderLayout.WEST);
			jContentPane.add(getJScrollPaneOutput(), java.awt.BorderLayout.EAST);
		}
		
		return jContentPane;
	}
	
	private void setEastComponent(Component view){
		jContentPane.remove(scrollPaneOutput);
		scrollPaneOutput = new JScrollPane(view);	
		jContentPane.add(scrollPaneOutput, java.awt.BorderLayout.EAST);
		this.show();
		this.repaint();
	}
	
	private void setOutputText(String str){
		jTextAreaOutput.setText(str);
		setEastComponent(jTextAreaOutput);
	}
	
	private void setOutputPlanGraph(AbstractOperator plan){
		PlanPanel planPanel = new PlanPanel(plan);
		planPanel.setPreferredSize(new Dimension(900, 1500));
		planPanel.setSize(new Dimension(2500, 1500));
		setEastComponent(planPanel);
	}
	
	private String printDelay(long startTime, long endTime){
		long diff = (endTime - startTime)/1000000;
		return "----------- \r\n Tempo: " + diff + " ms. \r\n----------- \r\n\r\n";
	}
	
	public void lostOwnership(Clipboard clipboard, Transferable contents) 
	{}

}  //  @jve:decl-index=0:visual-constraint="10,10"



