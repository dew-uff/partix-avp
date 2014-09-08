package connection;

import javax.xml.xquery.XQException;

public class DatabaseDisconnectionException extends XQException {

	public DatabaseDisconnectionException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int disconnectedNode = -1;

	public int getDisconnectedNode() {
		return disconnectedNode;
	}

	public void setDisconnectedNode(int disconnectedNode) {
		this.disconnectedNode = disconnectedNode;
	}
}
