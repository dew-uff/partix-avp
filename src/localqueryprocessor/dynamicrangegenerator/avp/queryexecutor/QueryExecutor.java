package localqueryprocessor.dynamicrangegenerator.avp.queryexecutor;

import connection.DBConnectionPoolEngine;
import resultcomposer.ResultComposer;
import util.LocalQueryTaskStatistics;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range;
import localqueryprocessor.localquerytask.LocalQueryTask;

abstract public class QueryExecutor implements Runnable {

	// Valid States
	public static final int ST_STARTING_RANGE = 0;
	public static final int ST_PROCESSING_RANGE = 1;
	public static final int ST_RANGE_PROCESSED = 2;
	public static final int ST_WAITING_NEW_RANGE = 3;
	public static final int ST_FINISHING = 4;
	public static final int ST_FINISHED = 5;
	public static final int ST_ERROR = 5;
	
	// Protected attributes
	protected int state;
	protected Range range;
	protected LocalQueryTaskStatistics lqtStatistics;
	
	// Private attributes
	private String query;
	private Exception exception = null;
	private LocalQueryTask lqt;
	private DBConnectionPoolEngine dbpool;
	private ResultComposer resultComposer;


	/** adapted by Luiz Matos */
	public QueryExecutor(String query, Range range) {
		this.query = query;
		this.range = range;
		this.state = ST_STARTING_RANGE;
	}

	/** Creates a new instance of Job */
	public QueryExecutor(LocalQueryTask lqt, DBConnectionPoolEngine dbpool,
			ResultComposer resultComposer, String query, Range range,
			LocalQueryTaskStatistics lqtStatistics) {
		this.lqt = lqt;
		this.dbpool = dbpool;
		this.resultComposer = resultComposer;
		this.query = query;
		this.range = range;
		this.lqtStatistics = lqtStatistics;
		this.state = ST_STARTING_RANGE;
	}
	
	/* Abstract method: getQueryLimits( int []limits )
	 *  Determines next query limits.
	 *  If the interval to be processed is not finished, the method returns "true" and
	 *      "limits" vector stores new limits.
	 *  If the interval to be processed is finished, the method returns "false" and
	 *      "limits" vector contents are undetermined.
	 * "Limits" vector is allocated by the caller. Method must only modify its values.
	 */
	protected abstract boolean getQueryLimits(int[] limits);

	/* Abstract method: executeSubQuery();
	 * Each subclass can present different behavior while executing sub-query.
	 * When AVP is employed, for example, query execution time is always needed.
	 * Parameter dbconn must already be set and prepared statement must be with the appropriate arguments set
	 */
	protected abstract void executeSubQuery(String query, int[] limit);

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		try {
			
			int[] limit = new int[2];

			do {
				// Wait for new query interval arrival. Used for dynamic load balancing.
				waitForNewRange();

				// new interval arrived or a request to finish operations was received
				if (state != ST_FINISHING) {

					if (range == null) {
						throw new IllegalThreadStateException("LocalQueryTaskEngine exception: no interval to process and no request to finish received!");
					}

					while (state != ST_RANGE_PROCESSED) {						
						//int countArgs;

						System.out.println("Obtaining query limits ...");
						// Each algorithm can determine new limits in a different way
						getQueryLimits(limit);

						if (state != ST_RANGE_PROCESSED) {

							System.out.println("Executing subquery ...");
						     executeSubQuery(query, limit);
						}
					}

					synchronized (this) {
						// reset query interval
						range = null;
						state = ST_WAITING_NEW_RANGE;

						notifyAll();
					}

				} // if( a_state != ST_FINISHING )
			} while (state != ST_FINISHING);

			state = ST_FINISHED;

		} catch (Exception e) {
			System.err.println("Local Query Task Exception: " + e.getMessage());
			e.printStackTrace();
			exception = e;
			state = ST_ERROR;			
		}
	}

	// to be called by LocalQueryTask when assigning a new interval due to dynamic load balancing
	public synchronized void newRange(Range newRange) {
		// Sets a new interval to be processed. Used for dynamic load balancing.
		if (state != ST_WAITING_NEW_RANGE)
			throw new IllegalThreadStateException("QueryExecutor Exception: attempt to change query interval when state was "	+ state + " !");
		range = newRange;
		state = ST_STARTING_RANGE;
		notifyAll();
	}

	// to be called by LocalQueryTask to indicate LocalQueryTask must finish
	public synchronized void finish() {
		state = ST_FINISHING;
		notifyAll();
	}

	private synchronized void waitForNewRange() throws InterruptedException {
		while (state == ST_WAITING_NEW_RANGE) {
			wait();
		}
		notifyAll();
	}

	public Exception getException() {
		return exception;
	}
}


