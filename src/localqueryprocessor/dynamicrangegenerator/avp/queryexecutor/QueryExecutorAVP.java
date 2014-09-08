package localqueryprocessor.dynamicrangegenerator.avp.queryexecutor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

import connection.DBConnectionPoolEngine;

import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.PartitionSize;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.PartitionTuner;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.PartitionTunerMT_NonUniform;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.RangeStatistics;
import localqueryprocessor.dynamicrangegenerator.avp.ConnectionSedna;
import localqueryprocessor.localquerytask.LocalQueryTask;

import resultcomposer.ResultComposer;
import util.LocalQueryTaskStatistics;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;


public class QueryExecutorAVP extends QueryExecutor {

	private PartitionTuner partitionTuner;
	private PartitionSize currentPartitionSize;
	private int nextRangeValue;
	private Preview preview;

	private PartitionTunerMT_NonUniform pTuner = null;

	private int beginningOfInterval; // inicio do intervalo analisado
	private int endOfInterval; // fim do intervalo analisado

	static FileWriter file = null;
	static PrintWriter writeFile = null;
	static final int TOTAL_EXECUTIONS = 10;

	/*
	 * adapted by Luiz Matos
	 */
	public QueryExecutorAVP(String query, Range range, RangeStatistics statistics) throws IOException {
		super(query, range);

		this.partitionTuner = new PartitionTunerMT_NonUniform(range.getVPSize(), statistics);
		this.currentPartitionSize = null;
		this.preview = new Preview(range.getFirstValue(), range.getOriginalLastValue());
		setBeginningOfInterval(range.getFirstValue());
		setEndOfInterval(range.getOriginalLastValue());
		try {
			file = new FileWriter("C:\\Users\\lais\\Desktop\\Luiz\\doutorado\\EQ\\experimento\\results\\resultsAVP-" + range.getNumVPs()+"nodes-initial-" + range.getFirstValue() + ".txt");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		writeFile = new PrintWriter(file);

		// Inicia a fase de ajuste dos fragmentos
		//adjustFragmentSize();
		//pTuner = new PartitionTunerMT_NonUniform(range.getOriginalLastValue()-range.getFirstValue());
		//pTuner.getPartitionSize();
		System.out.println("Estou no construtor adaptado da classe QueryExecutorAVP ...");
	}

	/** Creates a new instance of QueryExecutor_AVP */
	public QueryExecutorAVP(LocalQueryTask lqt, DBConnectionPoolEngine dbpool,
			ResultComposer resultComposer, String query, Range range,
			LocalQueryTaskStatistics lqtStatistics) throws RemoteException {
		super(lqt, dbpool, resultComposer, query, range, lqtStatistics);
		// a_partitionTuner = new PartitionTunerMeanTime(
		// a_range.getStatistics() );
		this.partitionTuner = new PartitionTunerMT_NonUniform(this.range.getStatistics());
		this.currentPartitionSize = null;
		if (this.lqtStatistics != null)
			this.lqtStatistics.setPartitionTuner(this.partitionTuner);

		preview = new Preview(range.getFirstValue(),range.getOriginalLastValue());
		System.out.println("Estou no construtor original da classe QueryExecutorAVP ...");
	}

	protected boolean getQueryLimits(int[] limits) {
		switch (state) {
		case ST_STARTING_RANGE: {
			limits[0] = range.getFirstValue();
			state = ST_PROCESSING_RANGE;
			currentPartitionSize = partitionTuner.getPartitionSize();
			break;
		}
		case ST_PROCESSING_RANGE: {
			limits[0] = nextRangeValue;
			if (partitionTuner.stillTuning()) {
				if (currentPartitionSize.getNumPerformedExecutions() >= currentPartitionSize.getNumExpectedExecutions()) {
					// Number of expected executions was reached.
					// Send feedback to partition tuner.
					partitionTuner.setSizeResults(currentPartitionSize);
					// Ask a new partition size.
					currentPartitionSize = partitionTuner.getPartitionSize();
				}
			}
			break;
		}
		default: {
			throw new IllegalThreadStateException("LocalQueryTaskEngine_AVP Exception: getQueryLimits() should not be called while in state " + state + "!");
		}
		}
		limits[1] = range.getNextValue(currentPartitionSize.numberOfKeys());
		nextRangeValue = limits[1];
		if (limits[0] == limits[1]) {
			state = ST_RANGE_PROCESSED;
			partitionTuner.reset();
			return false;
		} else if (limits[0] < limits[1])
			return true;
		else
			throw new IllegalThreadStateException("LocalQueryTaskEngine_AVP Exception: lower limit superior to upper!");


	}

	@Override
	protected void executeSubQuery(String query, int[] limit) {

		System.out.println("limit[0] = " + limit[0]);
		System.out.println("limit[1] = " + limit[1]);

		String xquery = query;	

		while(xquery.indexOf("?") > -1) {
			xquery = xquery.replaceFirst("\\?",limit[0]+"");
			xquery = xquery.replaceFirst("\\?",limit[1]+"");
		}
		System.out.println("query = " + xquery);

		XQExpression xqe = null;
		XQResultSequence xqr = null;
		String results = "";


		long startTime;
		long delay=0;
		long totalTime=0, mediaTime=0;

		try {

			ConnectionSedna con = new ConnectionSedna();
			//XQConnection xqc = con.establishSednaConnection();
			SednaConnection conn = con.establishSednaConnection("5050");

			SednaStatement st = conn.createStatement();

			//xqe = xqc.createExpression();			

			for(int i = 0; i<TOTAL_EXECUTIONS; i++) {
				//Execução da Query
				startTime = System.nanoTime(); // inicializa o contador de tempo.
				//xqr = xqe.executeQuery(this.getAdjustedFragment());	
				st.execute(xquery);
				delay = (System.nanoTime() - startTime); // obtem o tempo gasto com o processamento desta sub-consulta em nanosegundos.			
				if (i!=0)
					totalTime += delay;
			}

			mediaTime=totalTime/(TOTAL_EXECUTIONS-1);
			//			writeFile.printf(limit[0]+";"+limit[1]+";"+currentPartitionSize.numberOfKeys()+";"+convertNano2Seconds(mediaTime));
			//			
			//			//writeFile.println(limit[0]+";"+limit[1]+";"+currentPartitionSize.numberOfKeys()+";"+convertNano2Seconds(mediaTime));
			//			System.out.println(limit[0]+";"+limit[1]+";"+currentPartitionSize.numberOfKeys()+";"+convertNano2Seconds(mediaTime));
			//			
			//			System.out.println("\n");

			//			SednaSerializedResult pr = st.getSerializedResult();
			//			String item;
			//			while ((item = pr.next()) != null)
			//				results = results + item;

			//System.out.println(results);
			//			while (xqr.next()) {				
			//				results = results + xqr.getItemAsString(null);			
			//			}

			currentPartitionSize.setExecTime(mediaTime);
			preview.setRange(nextRangeValue);

			conn.close();
			//writeFile.close();
			//xqc.close();		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();	
			System.out.println("executeAdjustedSubQuery(): Erro ao definir inicio e fim do intervalo.");
		}
		finally {
			//this.finish();

			//			PrintStream out = new PrintStream(System.out);
			//			partitionTuner.printTuningStatistics(out );
		}

	}

	/*
	 * Metodo responsavel pelo ajuste dos fragmentos.
	 */
	//	private void adjustFragmentSize() throws IOException {
	//
	//		String oldBeginning; // inicio do intervalo na FVS - posição inicial informada na subquery recebida pelo nó		
	//		String oldEnding; // fim do intervalo na FVS - posição final informada na subquery recebida pelo nó		
	//
	//		String newBeginning; // inicio do intervalo na FVA
	//		String newEnding; // fim do intervalo na FVA		
	//
	//		System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():AJUSTANDO");
	//		
	//		// obtem o inicio e fim do intervalo para o fragmento recebido
	//		if ( this.getInitialFragment().indexOf("[position() >= ") != -1 ) { // numero de fragmentos maior que numero de processadores.
	//
	//			System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():NUMERO DE FRAGMENTOS MAIOR QUE NUMERO DE PROCESSADORES.");			
	//			oldBeginning = "[position() >= " + Integer.toString(this.getBeginningOfInterval());
	//			oldEnding = "position() < " + Integer.toString(this.getEndOfInterval()) + "]";		
	//		}
	//		else {
	//			System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():numero de processadores menor ou igual ao numero de elementos.");
	//
	//			oldBeginning = "[position() = " + Integer.toString(this.getBeginningOfInterval());			
	//			oldEnding = "";
	//
	//		}			
	//
	//		while ( this.getLastEnding() < this.getEndOfInterval()) { // enquanto o final do ultimo fragmento processado for menor que o final do fragmento original gerado na FVS, prossiga com a fase de ajuste.
	//
	//			if ( this.getInitialFragment().indexOf("[position() >= ") != -1 ) { // numero de fragmentos maior que numero de processadores.							
	//
	//				newBeginning =  "[position() >= ";
	//				newEnding = "position() < ";
	//			}
	//			else {				
	//				newBeginning =  "[position() = ";
	//				newEnding = "";
	//			}
	//
	//			// obtem o inicio e fim do intervalo ajustado.
	//			if ( this.getLastEnding() == -1) { // ainda nao ajustou fragmento algum.			
	//
	//				newBeginning =  newBeginning + Integer.toString(this.getBeginningOfInterval()); // o inicio do fragmento atual, é o próprio início do intervalo gerado na FVS.
	//
	//			}
	//			else {
	//
	//				newBeginning = "[position() >= " + Integer.toString(this.getLastEnding()); // o inicio do fragmento atual é igual ao final do último fragmento ajustado.
	//
	//			}
	//
	//			if ( this.getCurrentSize() == -1 ){ // ainda nao processou nehum fragmento
	//				this.setCurrentSize(INITIAL_NUMBER_OF_ELEMENTS); //First partition size (Luiz Matos)
	//				sizeGrowthTax = INITIAL_SIZE_GROWTH_TAX; //First size growth tax = 100%
	//			}
	//			else { // ja processou algum fragmento, armazena o tamanho anterior.
	//				this.setLastSize(this.getCurrentSize());
	//			}
	//
	//			int end = this.getBeginningOfInterval();
	//
	//			if ( !newEnding.equals("") ) { // se fim diferente de inicio, especifique o final do intervalo.			
	//
	//				if ( this.getLastEnding() > 0 ) {
	//					end = this.getLastEnding() + this.getCurrentSize();
	//				}
	//				else {
	//					end = this.getBeginningOfInterval() + this.getCurrentSize();
	//				}
	//
	//				if ( end > this.getEndOfInterval() ) {
	//					end = this.getEndOfInterval(); 
	//				}
	//
	//				newEnding = newEnding + Integer.toString(end) + "]";		
	//			}						
	//
	//			// armazena o final do ultimo fragmento ajustado.
	//			this.setLastEnding(end);
	//
	//			// cria novo fragmento, substituindo os intervalos antigos pelos novos.
	//			String adjustedFrag = this.getInitialFragment().replace(oldBeginning, newBeginning);
	//			adjustedFrag = adjustedFrag.replace(oldEnding, newEnding);	
	//			this.setAdjustedFragment(adjustedFrag);
	//
	//			System.out.println("AdaptativeVirtualPartitioning.adjustFragmentSize():fragmento ajustado:"+this.getAdjustedFragment());
	//
	//			this.addFragment(this.getAdjustedFragment());
	//
	//			executeAdjustedSubQuery();
	//		}
	//	} 

	//	@SuppressWarnings("static-access")
	//	private void executeAdjustedSubQuery() throws IOException{
	//		//File to write results
	//		file = new FileWriter(output+fileName+this.getEndOfInterval()+".txt");
	//		writeFile = new PrintWriter(file);
	//		System.out.println("Results file open: " + output+fileName+this.getEndOfInterval()+".txt");
	//
	//
	//		XQExpression xqe = null;
	//		XQResultSequence xqr = null;
	//		String results = "";
	//
	//		long startTime;
	//		long delay=0;
	//		long totalTime=0, mediaTime=0;
	//
	//		try {
	//
	//			ConnectionSedna con = new ConnectionSedna();
	//			XQConnection xqc = con.establishSednaConnection();
	//
	//			xqe = xqc.createExpression();			
	//
	//			for(int i = 0; i<TOTAL_EXEC_QUERYS; i++) {
	//				//Execução da Query
	//				startTime = System.nanoTime(); // inicializa o contador de tempo.
	//				xqr = xqe.executeQuery(this.getAdjustedFragment());	
	//				delay = ((System.nanoTime() - startTime)/1000000); // obtem o tempo gasto com o processamento desta sub-consulta em milisegundos.			
	//				if (i!=0)
	//					totalTime += delay;
	//			}
	//
	//			mediaTime=totalTime/(TOTAL_EXEC_QUERYS-1);
	//			//System.out.println("mediaTime: " + mediaTime);
	//			writeFile.println(this.getBeginningOfInterval()+";"+this.getEndOfInterval()+";"+this.getCurrentSize()+";"+mediaTime);
	//			System.out.println(this.getBeginningOfInterval()+";"+this.getEndOfInterval()+";"+this.getCurrentSize()+";"+mediaTime);
	//
	//			if ( this.getCurrentDelay() != -1) { // ja executou algum fragmento, armazenar o tempo de execução anterior antes de alterá-lo.				
	//
	//				this.setLastDelay(this.getCurrentDelay());
	//			}
	//
	//			this.setCurrentDelay(delay);		
	//
	//			//Trata da definição dos novos fragmentos
	//			updateFragmentSize();
	//
	//			while (xqr.next()) {				
	//				results = results + xqr.getItemAsString(null);			
	//			}
	//
	//			Query q = Query.getUniqueInstance(true);
	//			SubQuery sbq = SubQuery.getUniqueInstance(true);
	//
	//			// Se nao tiver retornado resultado algum, o único elemento retornado será o constructorElement. Nao gerar XML, pois não há resultados.				
	//			if ( results.trim().lastIndexOf("<") != 0 ) {
	//
	//				sbq.setConstructorElement(sbq.getConstructorElement(results)); // Usado para a composicao do resultado final.
	//
	//				String intervalBeginning = sbq.getIntervalBeginning(this.getAdjustedFragment());
	//
	//				if ( sbq.getElementAfterConstructor().equals("") ) {
	//					sbq.setElementAfterConstructor(sbq.getElementAfterConstructorElement(results, sbq.getConstructorElement()));
	//				}
	//
	//				if (sbq.isUpdateOrderClause()) {
	//					sbq.getElementsAroundOrderByElement(this.getAdjustedFragment(), sbq.getElementAfterConstructor());
	//				}
	//
	//				if (!q.isOrderByClause()) { // se a consulta original nao possui order by adicione o elemento idOrdem
	//					sbq.storeResultInXMLDocument(SubQuery.addOrderId(results, intervalBeginning), intervalBeginning);
	//				}
	//				else { // se a consulta original possui order by apenas adicione o titulo do xml.
	//					results = sbq.getTitle() + "<partialResult>\r\n" + results + "\r\n</partialResult>";
	//					sbq.storeResultInXMLDocument(results, intervalBeginning);
	//				}
	//			}
	//
	//			xqc.close();		
	//			writeFile.close();
	//
	//		} catch (Exception e) {
	//			// TODO: handle exception
	//			e.printStackTrace();	
	//			System.out.println("executeAdjustedSubQuery(): Erro ao definir inicio e fim do intervalo.");
	//		}
	//	}

	//	protected void executeSubQuery(String query, int[] limit) {
	//
	//		long queryStart = System.currentTimeMillis();
	//		String mysql = query;		
	//		while(mysql.indexOf("?") > -1) {
	//			mysql = mysql.replaceFirst("\\?",limit[0]+"");
	//			mysql = mysql.replaceFirst("\\?",limit[1]+"");
	//		}              
	//
	//		//ResultSet result = dbconn.getConnection().createStatement().executeQuery(mysql);         
	//
	//		long queryElapsedTime = System.currentTimeMillis() - queryStart;
	//		currentPartitionSize.setExecTime(queryElapsedTime);
	//		preview.setRange(nextRangeValue);
	//
	//	}

	private static double convertNano2Seconds(double nanoseconds) {
		return nanoseconds/1000000000;
	}

	public int getBeginningOfInterval() {
		return beginningOfInterval;
	}

	public void setBeginningOfInterval(int beginningOfInterval) {
		this.beginningOfInterval = beginningOfInterval;
	}

	public int getEndOfInterval() {
		return endOfInterval;
	}

	public void setEndOfInterval(int endOfInterval) {
		this.endOfInterval = endOfInterval;
	}

	class Preview {
		private long init;
		private int rangeBegin;
		private int rangeEnd;
		private int actual;

		public Preview(int rangeBegin, int rangeEnd) {
			this.rangeBegin = rangeBegin;
			this.rangeEnd = rangeEnd;
			actual = rangeBegin;
			init = System.currentTimeMillis();
		}

		public void setRange(int range) {
			actual = range;
		}

		public String toString() {
			float tr = (rangeEnd-rangeBegin+1);
			float at = System.currentTimeMillis()-init;
			float ar = actual-rangeBegin+1;
			long total = (long)((tr * at)/ar);
			return "Estimated time: "+at+"/"+total+" ms";
		}
	}
}
