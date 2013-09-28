package classePrincipal;

import java.util.ArrayList;

import classePrincipal.Query;
import classePrincipal.SubQuery;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

public class AdaptativeVirtualPartitioning {
 
	private int lastSize = -1; // Tamanho utilizado para gerar o fragmento anterior 
	private int currentSize = -1; // Tamanho do fragmento atual
	private long lastDelay = -1; // indica o ultimo tempo de execucao obtido
	private long currentDelay = -1; // indica o tempo de execucao do fragmento atual
	private String initialFragment = ""; // indica a sub-consulta gerada na primeira fase da fragmentacao (FVS), passada como entrada para esta fase.
	private String adjustedFragment = ""; // indica a sub-consulta gerada apos a fase de ajuste (FVA)
	private int lastEnding = -1; // indica o fim do ultimo fragmento gerado na fase de ajuste
	private int beginningOfInterval; // inicio do intervalo analisado
	private int endOfInterval; // fim do intervalo analisado
	private int totalTimes = 0; // indica quantas vezes houve aumento no tempo de execu��o.
	protected ArrayList<String> adjustedFragments = null; // contem todos os fragmentos gerados apos a fase de ajuste de tamanho.
	
	public ArrayList<String> getFinalFragments() {
		return this.adjustedFragments;
	}

	public void addFragment(String fragment) {
		if (this.adjustedFragments == null)
			this.adjustedFragments = new ArrayList<String>();
		
		this.adjustedFragments.add(fragment);
	}
	
	private int getTotalTimes() {
		return totalTimes;
	}

	private void setTotalTimes(int totalTimes) {
		this.totalTimes = totalTimes;
	}

	private long getLastDelay() {
		return lastDelay;
	}

	private void setLastDelay(long lastDelay) {
		this.lastDelay = lastDelay;
	}

	private long getCurrentDelay() {
		return currentDelay;
	}

	private void setCurrentDelay(long currentDelay) {
		this.currentDelay = currentDelay;
	}
	
	private int getLastEnding() {
		return lastEnding;
	}

	private void setLastEnding(int lastEnding) {
		this.lastEnding = lastEnding;
	}

	private String getAdjustedFragment() {
		return adjustedFragment;
	}

	private void setAdjustedFragment(String adjustedFragment) {
		this.adjustedFragment = adjustedFragment;
	}

	public AdaptativeVirtualPartitioning(String initialSubQuery, int rank){
		//System.out.println("AdaptativeVirtualPartitioning.AdaptativeVirtualPartitioning()::initialSubQuery::"+initialSubQuery+", no nodo " + rank);
		this.setInitialFragment(initialSubQuery);
		this.setIntervalDefinition();
		
		// Inicia a fase de ajuste dos fragmentos
		adjustFragmentSize();
	}

	private String getInitialFragment() {
		return initialFragment;
	}

	private void setInitialFragment(String initialFragment) {
		this.initialFragment = initialFragment;
	}	
	
	private int getBeginningOfInterval() {
		return beginningOfInterval;
	}

	private void setBeginningOfInterval(int beginningOfInterval) {
		this.beginningOfInterval = beginningOfInterval;
	}

	private int getEndOfInterval() {
		return endOfInterval;
	}

	private void setEndOfInterval(int endOfInterval) {
		this.endOfInterval = endOfInterval;
	}
	
	private int getLastSize() {
		return lastSize;
	}

	private void setLastSize(int lastSize) {
		this.lastSize = lastSize;
	}
	
	private int getCurrentSize() {
		return currentSize;
	}

	private void setCurrentSize(int currentSize) {
		this.currentSize = currentSize;
	}
	
	/*
	 * Metodo responsavel pelo ajuste dos fragmentos.
	 */
	
	private void adjustFragmentSize(){
		
		String oldBeginning; // inicio do intervalo na FVS		
		String oldEnding; // inicio do intervalo na FVA
		
		String newBeginning; // fim do intervalo na FVS
		String newEnding; // fim do intervalo na FVA		
		
		//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():AJUSTANDO");
		// obtem o inicio e fim do intervalo para o fragmento recebido
		if ( this.getInitialFragment().indexOf("[position() >= ") != -1 ) { // numero de fragmentos maior que numero de processadores.
		
			//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():NUMERO DE FRAGMENTOS MAIOR QUE NUMERO DE PROCESSADORES.");			
			oldBeginning = "[position() >= " + Integer.toString(this.getBeginningOfInterval());
			//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():setando oldbeginning."+oldBeginning);
			oldEnding = "position() < " + Integer.toString(this.getEndOfInterval()) + "]";
			//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():setando oldending."+oldEnding);
			//newBeginning =  "[position() >= ";
			//newEnding = "position() < ";
		}
		else {
			//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():numero de processadores menor ou igual ao numero de elementos.");
			
			oldBeginning = "[position() = " + Integer.toString(this.getBeginningOfInterval());			
			oldEnding = "";
			
		}			
		
		while ( this.getLastEnding() < this.getEndOfInterval()) { // enquanto o final do ultimo fragmento processado for menor que o final do fragmento original gerado na FVS, prossiga com a fase de ajuste.
			
			if ( this.getInitialFragment().indexOf("[position() >= ") != -1 ) { // numero de fragmentos maior que numero de processadores.							
				
				newBeginning =  "[position() >= ";
				newEnding = "position() < ";
			}
			else {				
				newBeginning =  "[position() = ";
				newEnding = "";
			}
			
			//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():dentro do while e valor last ending eh."+this.getLastEnding());
			// obtem o inicio e fim do intervalo ajustado.
			if ( this.getLastEnding() == -1) { // ainda nao ajustou fragmento algum.			
				
				newBeginning =  newBeginning + Integer.toString(this.getBeginningOfInterval()); // o inicio do fragmento atual, � o pr�prio in�cio do intervalo gerado na FVS.
				
				//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():dentro do while."+newBeginning);
			}
			else {
				
				newBeginning = "[position() >= " + Integer.toString(this.getLastEnding()); // o inicio do fragmento atual � igual ao final do �ltimo fragmento ajustado.
				
				//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():dentro do while usando last ending."+newBeginning);
			}
			
			if ( this.getCurrentSize() == -1 ){ // ainda nao processou nehum fragmento
				this.setCurrentSize(1);
			}
			else { // ja processou algum fragmento, armazena o tamanho anterior.
				this.setLastSize(this.getCurrentSize());
			}
			
			int end = this.getBeginningOfInterval();
			
			if ( !newEnding.equals("") ) { // se fim diferente de inicio, especifique o final do intervalo.			
				
				if ( this.getLastEnding() > 0 ) {
					end = this.getLastEnding() + this.getCurrentSize();
				}
				else {
					end = this.getBeginningOfInterval() + this.getCurrentSize();
				}
				
				if ( end > this.getEndOfInterval() ) {
					end = this.getEndOfInterval(); 
				}
				
				//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():dentro do while obtendo currentsize."+this.getCurrentSize());
				
				newEnding = newEnding + Integer.toString(end) + "]";		
			}						
			
			// armazena o final do ultimo fragmento ajustado.
			this.setLastEnding(end);
			
			//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():oldbeg:"+oldBeginning+", newbeg:"+newBeginning);
			//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():oldend:"+oldEnding+", newend:"+newEnding);
			//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():lastend:"+this.getLastEnding()+", lastsize:"+this.getLastSize()+",cursize:"
				//	+ this.getCurrentSize());
			
			// cria novo fragmento, substituindo os intervalos antigos pelos novos.
			String adjustedFrag = this.getInitialFragment().replace(oldBeginning, newBeginning);
			adjustedFrag = adjustedFrag.replace(oldEnding, newEnding);	
			this.setAdjustedFragment(adjustedFrag);
			
			//System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():fragmento ajustado:"+this.getAdjustedFragment());
			
			this.addFragment(this.getAdjustedFragment());
			
			executeAdjustedSubQuery();
		}
	} 
	
	@SuppressWarnings("static-access")
	private void setIntervalDefinition(){
	
		try {
			
			SubQuery sbq = SubQuery.getUniqueInstance(true);
			//System.out.println("AdaptativeVirtualPartitioning.setIntervalDefinition():initialfrag:"+this.getInitialFragment());
			int begin = Integer.parseInt(sbq.getIntervalBeginning(this.getInitialFragment()));
			//System.out.println("AdaptativeVirtualPartitioning.setIntervalDefinition():begin:"+begin);
			this.setBeginningOfInterval(begin);
			int end = Integer.parseInt(sbq.getIntervalEnding(this.getInitialFragment()));
			//System.out.println("AdaptativeVirtualPartitioning.setIntervalDefinition():end:"+end);
			this.setEndOfInterval(end);			
			//System.out.println("AdaptativeVirtualPartitioning.setIntervalDefinition():FINALIZOU!");
		} catch (Exception e) {
			// TODO: handle exception
			//System.out.println("AdaptativeVirtualPartitioning.setIntervalDefinition(): Erro ao definir inicio e fim do intervalo.");
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("static-access")
	private void executeAdjustedSubQuery(){
		
		XQExpression xqe = null;
		XQResultSequence xqr = null;
		String results = "";
		
		long startTime;
		long delay;
		
		//System.out.println("AdaptativeVirtualPartitioning.executeAdjustedSubQuery(): before try");
		/*try {
			//System.out.println("AdaptativeVirtualPartitioning.executeAdjustedSubQuery(): before instantiate connection.");
			ConnectionSedna con = new ConnectionSedna();			
			//System.out.println("AdaptativeVirtualPartitioning.executeAdjustedSubQuery(): before establish connection");
			XQConnection xqc = con.establishSednaConnection("146.164.31.140", "examplesdb"); // 146.164.31.140
			//System.out.println("AdaptativeVirtualPartitioning.executeAdjustedSubQuery(): after establish connection");
			xqe = xqc.createExpression();			
			
			startTime = System.nanoTime(); // inicializa o contador de tempo.
			xqr = xqe.executeQuery(this.getAdjustedFragment());	
			delay = ((System.nanoTime() - startTime)/1000); // obtem o tempo gasto com o processamento desta sub-consulta.			
			
			if ( this.getCurrentDelay() != -1) { // ja executou algum fragmento, armazenar o tempo de execu��o anterior antes de alter�-lo.				
				
				this.setLastDelay(this.getCurrentDelay());
			}
			
			this.setCurrentDelay(delay);
			
			//System.out.println("AdaptativeVirtualPartitioning.executeAdjustedSubQuery(): lastdelay="+this.getLastDelay()+",curdelay:"+this.getCurrentDelay());
			updateFragmentSize();
			
			while (xqr.next()) {				
				results = results + xqr.getItemAsString(null);			
			}
			
			//System.out.println("AdaptativeVirtualPartitioning.executeAdjustedSubQuery(): results="+results);
			Query q = Query.getUniqueInstance(true);
			SubQuery sbq = SubQuery.getUniqueInstance(true);
			
			// Se nao tiver retornado resultado algum, o �nico elemento retornado ser� o constructorElement. Nao gerar XML, pois n�o h� resultados.				
			if ( results.trim().lastIndexOf("<") != 0 ) {
				
				//System.out.println("entrei -------- ");
				sbq.setConstructorElement(sbq.getConstructorElement(results)); // Usado para a composicao do resultado final.
				
				String intervalBeginning = sbq.getIntervalBeginning(this.getAdjustedFragment());
				
				if ( sbq.getElementAfterConstructor().equals("") ) {
					sbq.setElementAfterConstructor(sbq.getElementAfterConstructorElement(results, sbq.getConstructorElement()));
				}
				
				if (sbq.isUpdateOrderClause()) {
					sbq.getElementsAroundOrderByElement(this.getAdjustedFragment(), sbq.getElementAfterConstructor());
				}
				
				if (!q.isOrderByClause()) { // se a consulta original nao possui order by adicione o elemento idOrdem
					sbq.storeResultInXMLDocument(SubQuery.addOrderId(results, intervalBeginning), intervalBeginning);
				}
				else { // se a consulta original possui order by apenas adicione o titulo do xml.
					results = sbq.getTitle() + "<partialResult>\r\n" + results + "\r\n</partialResult>";
					sbq.storeResultInXMLDocument(results, intervalBeginning);
				}
			}
						
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("executeAdjustedSubQuery(): Erro ao executar a sub-consulta gerada.");
		}*/
	}
	
	private void updateFragmentSize(){
		
		long diff;
		
		if ( this.getLastDelay() != -1) { // ja executou ao menos 1 fragmento, e foi armazenado o tempo anterior.
			
			diff = this.getCurrentDelay() - this.getLastDelay(); // calcula a diferenca entre os tempos de execu��o.
			
			System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize():diff="+diff);
			
			if ( diff <= 0.00) { // tempo de execu��o do fragmento atual foi menor que tempo de execu��o do fragmento anterior.
				
				// aumenta o tamanho do fragmento
				int newSize = this.getCurrentSize() + 1;
				this.setCurrentSize(newSize);
				this.setTotalTimes(0); // reinicializa o contador.
				
				//System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize(): TEMPO DE PROCESSAMENTO DIMINUIU! AUMENTE O TAMANHO DO FRAGMENTO!!");
			}
			else { // tempo de execu��o do fragmento atual foi maior que tempo de execu��o do fragmento anterior.				
												
				long proportionalTime = this.getLastDelay() + ( this.getLastDelay() * (this.getLastSize() / this.getCurrentSize()) );
				
				//System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize():proportionalTime="+this.getLastDelay()+", lastsize:"+this.getLastSize()+", cursize:"+this.getCurrentSize());
				//System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize():proportionalTime="+proportionalTime);
				
				// verifica se o aumento no tempo de execu��o � proporcionalmente menor que o tempo obtido com o fragmento anterior.
				if ( diff < proportionalTime) { 
					
					// aumenta o tamanho do fragmento
					int newSize = this.getCurrentSize() + 1;
					this.setCurrentSize(newSize);
					this.setTotalTimes(0); // reinicializa o contador.
					
					//System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize(): TEMPO DE PROCESSAMENTO AUMENTOU, MAIS O AUMENTO EH MENOR QUE O TEMPO DE EXECUCAO ANTERIOR. AUMENTE O TAMANHO DO FRAGMENTO!!");
				}
				else { 
					
					this.setTotalTimes( this.getTotalTimes() + 1 ); // atualiza o numero de vezes em que houve aumento.
					
					// verifica se houve aumento de tempo consecutivos. Em caso afirmativo, ocorreu deterioracao de desempenho.
					if ( this.getTotalTimes() > 1 ) {
						
						// diminui o tamanho do fragmento
						int newSize = this.getCurrentSize() - 1;
						this.setCurrentSize(newSize);						
						//System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize(): TEMPO DE PROCESSAMENTO AUMENTOU, O AUMENTO EH MAIOR QUE O TEMPO DE EXECUCAO POR VEZES CONSECUTIVAS. DETERIORACAO: DIMINUA O TAMANHO DO FRAGMENTO!!");
					}
					else {
						//System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize(): TEMPO DE PROCESSAMENTO AUMENTOU, O AUMENTO EH MAIOR QUE O TEMPO DE EXECUCAO MAIS NAO POR VEZES CONSECUTIVAS. CONTINUAR MONITORAMENTO. MANTENHA O TAMANHO DO FRAGMENTO!!");
						// Caso contrario, mantenha o tamanho do fragmento, pois um tamanho estavel foi encontrado.
					}
				}
				
				//System.out.println("AdaptativeVirtualPartitioning.updateFragmentSize():newsize="+this.getCurrentSize()+", totaltimes deterioracao:"+this.getTotalTimes());;
			}
		}
		else {
			// incrementa o tamanho do fragmento, para nao executar o segundo fragmento com o tamanho inicial com valor UM novamente.
			this.setCurrentSize( this.getCurrentSize() + 1 );
		}
	}
	
}






