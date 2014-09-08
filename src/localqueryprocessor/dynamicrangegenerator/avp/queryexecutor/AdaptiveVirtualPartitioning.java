package localqueryprocessor.dynamicrangegenerator.avp.queryexecutor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

//import br.uff.ic.dew.avp.simplevirtualpartitioning.Query;
//import br.uff.ic.dew.avp.simplevirtualpartitioning.SubQuery;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

public class AdaptiveVirtualPartitioning {

	private int lastSize = -1; // Tamanho utilizado para gerar o fragmento anterior 
	private int currentSize = -1; // Tamanho do fragmento atual
	private long lastDelay = -1; // indica o ultimo tempo de execucao obtido
	private long currentDelay = -1; // indica o tempo de execucao do fragmento atual
	private String initialFragment = ""; // indica a sub-consulta gerada na primeira fase da fragmentacao (FVS), passada como entrada para esta fase.
	private String adjustedFragment = ""; // indica a sub-consulta gerada apos a fase de ajuste (FVA)
	private int lastEnding = -1; // indica o fim do ultimo fragmento gerado na fase de ajuste
	private int beginningOfInterval; // inicio do intervalo analisado
	private int endOfInterval; // fim do intervalo analisado
	private int totalTimes = 0; // indica quantas vezes houve aumento no tempo de execução.
	protected ArrayList<String> adjustedFragments = null; // contem todos os fragmentos gerados apos a fase de ajuste de tamanho.

	//Variáveis by Lima
	private final int NUMBER_OF_EXECUTIONS = 1; // number of executions to test each different size
    private final int INITIAL_NUMBER_OF_ELEMENTS = 1024; // number of elements to the first size that will be tested

    private final float INITIAL_SIZE_GROWTH_TAX = (float) 1.0; //First tax used for partition size increase
    private final float RESTART_SIZE_GROWTH_TAX = INITIAL_SIZE_GROWTH_TAX * (float) 0.2; //Partition size increase tax used when restarts increasing partition size after performance deterioration detection
    private final float REDUCTION_IN_SIZE_GROWTH_TAX = (float) 0.75; //Tax used for partition size reduction when performance deterioration isdetected
    private final float MIN_SIZE_GROWTH_TAX = (float) 0.1;
    private final float SIZE_REDUCTION_WHILE_RESTARTING = (float) 0.05;

    private final int MAX_CHANCES_FOR_SET = 3;

    private final float MEANTIME_GROWTH_TOLERANCE = (float) 0.25; // to be applied over the "size increasing tax" being used
    private final float MEANTIME_SET_TOLERANCE = (float) 0.1; // to be used while evaluating current size

    private float sizeGrowthTax; // growth tax currently being used
    
	static int adjustCounter = 0;

	static String output = "C:\\Users\\lais\\Desktop\\Luiz\\doutorado\\experimento\\AVP\\", fileName = "results";
	static FileWriter file = null;
	static PrintWriter writeFile = null;
	static final int TOTAL_EXEC_QUERYS = 10;

//	public AdaptiveVirtualPartitioning(String initialSubQuery) throws IOException {
//
//		this.setInitialFragment(initialSubQuery);
//		System.out.println("initialSubQuery: " + initialSubQuery);
//		//Obtém as posições iniciais e finais do fragmento submetido via FVS
//		this.setIntervalDefinition();
//		System.out.println(getBeginningOfInterval() + "-" + getEndOfInterval());
//
//		// Inicia a fase de ajuste dos fragmentos
//		adjustFragmentSize();
//	}
//
//	/*
//	 * Metodo responsavel pelo ajuste dos fragmentos.
//	 */
////	private void adjustFragmentSize() throws IOException{
////
////		String oldBeginning; // inicio do intervalo na FVS - posição inicial informada na subquery recebida pelo nó		
////		String oldEnding; // fim do intervalo na FVS - posição final informada na subquery recebida pelo nó		
////
////		String newBeginning; // inicio do intervalo na FVA
////		String newEnding; // fim do intervalo na FVA		
////
////		System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():AJUSTANDO");
////		
////		// obtem o inicio e fim do intervalo para o fragmento recebido
////		if ( this.getInitialFragment().indexOf("[position() >= ") != -1 ) { // numero de fragmentos maior que numero de processadores.
////
////			System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():NUMERO DE FRAGMENTOS MAIOR QUE NUMERO DE PROCESSADORES.");			
////			oldBeginning = "[position() >= " + Integer.toString(this.getBeginningOfInterval());
////			oldEnding = "position() < " + Integer.toString(this.getEndOfInterval()) + "]";		
////		}
////		else {
////			System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():numero de processadores menor ou igual ao numero de elementos.");
////
////			oldBeginning = "[position() = " + Integer.toString(this.getBeginningOfInterval());			
////			oldEnding = "";
////
////		}			
////
////		while ( this.getLastEnding() < this.getEndOfInterval()) { // enquanto o final do ultimo fragmento processado for menor que o final do fragmento original gerado na FVS, prossiga com a fase de ajuste.
////
////			if ( this.getInitialFragment().indexOf("[position() >= ") != -1 ) { // numero de fragmentos maior que numero de processadores.							
////
////				newBeginning =  "[position() >= ";
////				newEnding = "position() < ";
////			}
////			else {				
////				newBeginning =  "[position() = ";
////				newEnding = "";
////			}
////
////			// obtem o inicio e fim do intervalo ajustado.
////			if ( this.getLastEnding() == -1) { // ainda nao ajustou fragmento algum.			
////
////				newBeginning =  newBeginning + Integer.toString(this.getBeginningOfInterval()); // o inicio do fragmento atual, é o próprio início do intervalo gerado na FVS.
////
////			}
////			else {
////
////				newBeginning = "[position() >= " + Integer.toString(this.getLastEnding()); // o inicio do fragmento atual é igual ao final do último fragmento ajustado.
////
////			}
////
////			if ( this.getCurrentSize() == -1 ){ // ainda nao processou nehum fragmento
////				this.setCurrentSize(INITIAL_NUMBER_OF_ELEMENTS); //First partition size (Luiz Matos)
////				sizeGrowthTax = INITIAL_SIZE_GROWTH_TAX; //First size growth tax = 100%
////			}
////			else { // ja processou algum fragmento, armazena o tamanho anterior.
////				this.setLastSize(this.getCurrentSize());
////			}
////
////			int end = this.getBeginningOfInterval();
////
////			if ( !newEnding.equals("") ) { // se fim diferente de inicio, especifique o final do intervalo.			
////
////				if ( this.getLastEnding() > 0 ) {
////					end = this.getLastEnding() + this.getCurrentSize();
////				}
////				else {
////					end = this.getBeginningOfInterval() + this.getCurrentSize();
////				}
////
////				if ( end > this.getEndOfInterval() ) {
////					end = this.getEndOfInterval(); 
////				}
////
////				newEnding = newEnding + Integer.toString(end) + "]";		
////			}						
////
////			// armazena o final do ultimo fragmento ajustado.
////			this.setLastEnding(end);
////
////			// cria novo fragmento, substituindo os intervalos antigos pelos novos.
////			String adjustedFrag = this.getInitialFragment().replace(oldBeginning, newBeginning);
////			adjustedFrag = adjustedFrag.replace(oldEnding, newEnding);	
////			this.setAdjustedFragment(adjustedFrag);
////
////			System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():fragmento ajustado:"+this.getAdjustedFragment());
////
////			this.addFragment(this.getAdjustedFragment());
////
////			executeAdjustedSubQuery();
////		}
////	} 
//
////	@SuppressWarnings("static-access")
////	private void executeAdjustedSubQuery() throws IOException{
////		//File to write results
////		file = new FileWriter(output+fileName+this.getEndOfInterval()+".txt");
////		writeFile = new PrintWriter(file);
////		System.out.println("Results file open: " + output+fileName+this.getEndOfInterval()+".txt");
////
////
////		XQExpression xqe = null;
////		XQResultSequence xqr = null;
////		String results = "";
////
////		long startTime;
////		long delay=0;
////		long totalTime=0, mediaTime=0;
////
////		try {
////
////			ConnectionSedna con = new ConnectionSedna();
////			XQConnection xqc = con.establishSednaConnection();
////
////			xqe = xqc.createExpression();			
////
////			for(int i = 0; i<TOTAL_EXEC_QUERYS; i++) {
////				//Execução da Query
////				startTime = System.nanoTime(); // inicializa o contador de tempo.
////				xqr = xqe.executeQuery(this.getAdjustedFragment());	
////				delay = ((System.nanoTime() - startTime)/1000000); // obtem o tempo gasto com o processamento desta sub-consulta em milisegundos.			
////				if (i!=0)
////					totalTime += delay;
////			}
////
////			mediaTime=totalTime/(TOTAL_EXEC_QUERYS-1);
////			//System.out.println("mediaTime: " + mediaTime);
////			writeFile.println(this.getBeginningOfInterval()+";"+this.getEndOfInterval()+";"+this.getCurrentSize()+";"+mediaTime);
////			System.out.println(this.getBeginningOfInterval()+";"+this.getEndOfInterval()+";"+this.getCurrentSize()+";"+mediaTime);
////
////			if ( this.getCurrentDelay() != -1) { // ja executou algum fragmento, armazenar o tempo de execução anterior antes de alterá-lo.				
////
////				this.setLastDelay(this.getCurrentDelay());
////			}
////
////			this.setCurrentDelay(delay);		
////
////			//Trata da definição dos novos fragmentos
////			updateFragmentSize();
////
////			while (xqr.next()) {				
////				results = results + xqr.getItemAsString(null);			
////			}
////
////			Query q = Query.getUniqueInstance(true);
////			SubQuery sbq = SubQuery.getUniqueInstance(true);
////
////			// Se nao tiver retornado resultado algum, o único elemento retornado será o constructorElement. Nao gerar XML, pois não há resultados.				
////			if ( results.trim().lastIndexOf("<") != 0 ) {
////
////				sbq.setConstructorElement(sbq.getConstructorElement(results)); // Usado para a composicao do resultado final.
////
////				String intervalBeginning = sbq.getIntervalBeginning(this.getAdjustedFragment());
////
////				if ( sbq.getElementAfterConstructor().equals("") ) {
////					sbq.setElementAfterConstructor(sbq.getElementAfterConstructorElement(results, sbq.getConstructorElement()));
////				}
////
////				if (sbq.isUpdateOrderClause()) {
////					sbq.getElementsAroundOrderByElement(this.getAdjustedFragment(), sbq.getElementAfterConstructor());
////				}
////
////				if (!q.isOrderByClause()) { // se a consulta original nao possui order by adicione o elemento idOrdem
////					sbq.storeResultInXMLDocument(SubQuery.addOrderId(results, intervalBeginning), intervalBeginning);
////				}
////				else { // se a consulta original possui order by apenas adicione o titulo do xml.
////					results = sbq.getTitle() + "<partialResult>\r\n" + results + "\r\n</partialResult>";
////					sbq.storeResultInXMLDocument(results, intervalBeginning);
////				}
////			}
////
////			xqc.close();		
////			writeFile.close();
////
////		} catch (Exception e) {
////			// TODO: handle exception
////			e.printStackTrace();	
////			System.out.println("executeAdjustedSubQuery(): Erro ao definir inicio e fim do intervalo.");
////		}
////	}
//
//	private void updateFragmentSize(){
//
//		long diff;
//
//		if ( this.getLastDelay() != -1) { // ja executou ao menos 1 fragmento, e foi armazenado o tempo anterior.
//
//			diff = this.getCurrentDelay() - this.getLastDelay(); // calcula a diferenca entre os tempos de execução.
//
//			if ( diff <= 0.00) { // tempo de execução do fragmento atual foi menor que tempo de execução do fragmento anterior.
//
//				// aumenta o tamanho do fragmento
//				//int newSize = this.getCurrentSize() + 1;
//				//int newSize = this.getCurrentSize() * (1.0 + sizeGrowthTax); //DOBRA O TAMANHO DO INTERVALO (Luiz Matos)
//				this.setCurrentSize((int)(this.getCurrentSize() * (1.0 + sizeGrowthTax)));
//				this.setTotalTimes(0); // reinicializa o contador.
//
//			}
//			else { // tempo de execução do fragmento atual foi maior que tempo de execução do fragmento anterior.				
//
//				//long proportionalTime = this.getLastDelay() + ( this.getLastDelay() * (this.getLastSize() / this.getCurrentSize()) );
//				long proportionalTime = this.getLastDelay()*2+((5*this.getLastDelay())/100); //dobro do ultimo tempo + 5% dele
//
//				// verifica se o aumento no tempo de execução é proporcionalmente menor que o tempo obtido com o fragmento anterior.
//				//if ( diff < proportionalTime) { 
//				if (this.getCurrentDelay() < proportionalTime) {
//					// aumenta o tamanho do fragmento
//					//int newSize = this.getCurrentSize() + 1;
//					//int newSize = this.getCurrentSize()*2; //DOBRA O TAMANHO DO INTERVALO (Luiz Matos)
//					this.setCurrentSize((int)(this.getCurrentSize() * (1.0 + sizeGrowthTax)));
//					this.setTotalTimes(0); // reinicializa o contador.
//
//					System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize(): TEMPO DE PROCESSAMENTO AUMENTOU, MAS O AUMENTO EH MENOR QUE O TEMPO DE EXECUCAO ANTERIOR. AUMENTE O TAMANHO DO FRAGMENTO!!");
//				}
//				else { 
//
//					this.setTotalTimes( this.getTotalTimes() + 1 ); // atualiza o numero de vezes em que houve aumento.
//
//					// verifica se houve aumento de tempo consecutivos. Em caso afirmativo, ocorreu deterioracao de desempenho.
//					if ( this.getTotalTimes() > 1 ) {
//
//						// diminui o tamanho do fragmento
//						// newSize = this.getCurrentSize() - 1;
//						int newSize = (this.getCurrentSize() / 2); //REDUZ O TAMANHO DO INTERVALO NA METADE (Luiz Matos)
//						this.setCurrentSize(newSize);						
//						System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize(): TEMPO DE PROCESSAMENTO AUMENTOU, O AUMENTO EH MAIOR QUE O TEMPO DE EXECUCAO POR VEZES CONSECUTIVAS. DETERIORACAO: DIMINUA O TAMANHO DO FRAGMENTO!!");
//					}
//					else {
//						System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize(): TEMPO DE PROCESSAMENTO AUMENTOU, O AUMENTO EH MAIOR QUE O TEMPO DE EXECUCAO MAS NAO POR VEZES CONSECUTIVAS. CONTINUAR MONITORAMENTO. MANTENHA O TAMANHO DO FRAGMENTO!!");
//						// Caso contrario, mantenha o tamanho do fragmento, pois um tamanho estavel foi encontrado.
//					}
//				}				
//			}
//		}
//		else {
//			// incrementa o tamanho do fragmento, para nao executar o segundo fragmento com o tamanho inicial com valor UM novamente.
//			//this.setCurrentSize( this.getCurrentSize() + 1 );
//			this.setCurrentSize( this.getCurrentSize() * 2 ); //DOBRA O TAMANHO DO INTERVALO (Luiz Matos)
//		}
//	}
//
//	@SuppressWarnings("static-access")
////	private void setIntervalDefinition(){
////
////		try {
////
////			SubQuery sbq = SubQuery.getUniqueInstance(true);
////			int begin = Integer.parseInt(sbq.getIntervalBeginning(this.getInitialFragment()));
////			this.setBeginningOfInterval(begin);
////			int end = Integer.parseInt(sbq.getIntervalEnding(this.getInitialFragment()));
////			this.setEndOfInterval(end);			
////		} catch (Exception e) {
////			// TODO: handle exception
////			System.out.println("AdaptativeVirtualPartitioning.setIntervalDefinition(): Erro ao definir inicio e fim do intervalo.");
////			e.printStackTrace();
////		}
////	}
//	
//	public ArrayList<String> getFinalFragments() {
//		return this.adjustedFragments;
//	}
//
//	public void addFragment(String fragment) {
//		if (this.adjustedFragments == null)
//			this.adjustedFragments = new ArrayList<String>();
//		this.adjustedFragments.add(fragment);
//	}
//
//	private int getTotalTimes() {
//		return totalTimes;
//	}
//
//	private void setTotalTimes(int totalTimes) {
//		this.totalTimes = totalTimes;
//	}
//
//	private long getLastDelay() {
//		return lastDelay;
//	}
//
//	private void setLastDelay(long lastDelay) {
//		this.lastDelay = lastDelay;
//	}
//
//	private long getCurrentDelay() {
//		return currentDelay;
//	}
//
//	private void setCurrentDelay(long currentDelay) {
//		this.currentDelay = currentDelay;
//	}
//
//	private int getLastEnding() {
//		return lastEnding;
//	}
//
//	private void setLastEnding(int lastEnding) {
//		this.lastEnding = lastEnding;
//	}
//
//	private String getAdjustedFragment() {
//		return adjustedFragment;
//	}
//
//	private void setAdjustedFragment(String adjustedFragment) {
//		this.adjustedFragment = adjustedFragment;
//	}
//
//	private String getInitialFragment() {
//		return initialFragment;
//	}
//
//	private void setInitialFragment(String initialFragment) {
//		this.initialFragment = initialFragment;
//	}	
//
//	private int getBeginningOfInterval() {
//		return beginningOfInterval;
//	}
//
//	private void setBeginningOfInterval(int beginningOfInterval) {
//		this.beginningOfInterval = beginningOfInterval;
//	}
//
//	private int getEndOfInterval() {
//		return endOfInterval;
//	}
//
//	private void setEndOfInterval(int endOfInterval) {
//		this.endOfInterval = endOfInterval;
//	}
//
//	private int getLastSize() {
//		return lastSize;
//	}
//
//	private void setLastSize(int lastSize) {
//		this.lastSize = lastSize;
//	}
//
//	private int getCurrentSize() {
//		return currentSize;
//	}
//
//	private void setCurrentSize(int currentSize) {
//		this.currentSize = currentSize;
//	}
//	public static int getAdjustCounter() {
//		return adjustCounter;
//	}
//
//	public static void setAdjustCounter(int adjustCounter) {
//		AdaptiveVirtualPartitioning.adjustCounter = adjustCounter;
//	}
}

