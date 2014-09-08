/*
 * ResultComposer.java
 *
 * Created on 17 décembre 2003, 11:32
 */

package resultcomposer;

/**
 *
 * @author  lima
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;

import globalqueryprocessor.clusterqueryprocessor.queryplanner.QueryInfo;
import commons.Grouper;
import commons.GrouperImpl;
import commons.ResultSetQueue;
import commons.ResultSetQueueImpl;
import commons.SortedResultSetQueue;


abstract public class ResultComposerEngine extends UnicastRemoteObject
		implements ResultComposer, Runnable {
    
	protected ResultSetQueue resultQueue;
	protected boolean finishingRequested;
	private boolean finished;
	protected Grouper grouper;
    protected boolean distributedComposition;
    protected boolean distributedSort;
    protected int id;
   // private QueryInfo qi;
	private QueryInfo qi;
    

	/** Creates a new instance of ResultComposer */
	public ResultComposerEngine(double id, QueryInfo qi, int numlqts, double grouperId, boolean distributedSort) throws RemoteException, SQLException {
		if (!distributedSort)
		    this.resultQueue = new ResultSetQueueImpl();
        else
            this.resultQueue = new SortedResultSetQueue();

        this.grouper = new GrouperImpl(qi, numlqts, grouperId);
		this.finishingRequested = false;
		this.finished = false;
        this.distributedComposition = qi.isDistributedComposition();
        this.distributedSort = distributedSort;
        this.qi = qi;
	}

	// To be implemented by subclasses.
	public abstract void run();

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	public synchronized void addResult(ResultSet q) {
		resultQueue.push(id, q);
		notifyAll();
	}

	public synchronized void finish() {
		finishingRequested = true;
		notifyAll();
	}

	protected synchronized void acceptResults() throws InterruptedException {
		// keep processing while there is no order to stop or while there are results to be processed
        if (qi.isDistributedSort()){
            while (!finishingRequested)
                wait();
        }    
        while (!finishingRequested || !resultQueue.isEmpty()) {
			if (resultQueue.isEmpty()) {
				wait();
			} else {
				ResultSet result = resultQueue.pop();
				grouper.insert(result);
			}
		}
		notifyAll();
	}

	protected synchronized void setFinished() {
		finished = true;
		notifyAll();
	}

	public boolean finished() {
		return finished;
	}
}
