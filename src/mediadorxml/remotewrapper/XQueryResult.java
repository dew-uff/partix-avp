package mediadorxml.remotewrapper;

import java.io.*;

public class XQueryResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 759535829075193252L;
	private String result;
	private boolean success;
	private long timeMsCompile;
	private long timeMsLocal;
	private long timeMsCommunicRemote;
	private long timeMsRemote;
	private long totalBytes; 
	private int numberQueriesExecuted;
	
	// Default constructor
	public XQueryResult(){
	}
	
	public XQueryResult(String result, boolean success){
		this.result = result;
		this.success = success;
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(final String result) {
		this.result = result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(final boolean success) {
		this.success = success;
	}

	public long getTimeMsLocal() {
		return timeMsLocal;
	}

	public void setTimeMsLocal(final long timems) {
		this.timeMsLocal = timems;
	}

	public long getTimeMsRemote() {
		return timeMsRemote;
	}

	public void setTimeMsRemote(final long timeMsRemote) {
		this.timeMsRemote = timeMsRemote;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public void setTotalBytes(final long totalNodes) {
		this.totalBytes = totalNodes;
	}

	public int getNumberQueriesExecuted() {
		return numberQueriesExecuted;
	}

	public void setNumberQueriesExecuted(int numberQueriesExecuted) {
		this.numberQueriesExecuted = numberQueriesExecuted;
	}

	public long getTimeMsCompile() {
		return timeMsCompile;
	}

	public void setTimeMsCompile(long timeMsCompile) {
		this.timeMsCompile = timeMsCompile;
	}

	public long getTimeMsCommunicRemote() {
		return timeMsCommunicRemote;
	}

	public void setTimeMsCommunicRemote(long timeMsCommunicRemote) {
		this.timeMsCommunicRemote = timeMsCommunicRemote;
	}
	
}

