/*
 * Created on 08/04/2005
 */
package globalqueryprocessor.clusterqueryprocessor;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.xquery.XQException;

import commons.Logger;
import commons.Messages;
import commons.JdbcUtil;
import commons.PartiXVPDatabaseMetaData;
import globalqueryprocessor.clusterqueryprocessor.ClusterQueryProcessorEngine;
import loadbalancer.LprfLoadBalancer;
import globalqueryprocessor.clusterqueryprocessor.querymanager.SelectQueryManager;
import globalqueryprocessor.clusterqueryprocessor.querymanager.UpdateQueryManager;
import globalqueryprocessor.clusterqueryprocessor.QueryScheduler;


/**
 * @author Bernardo
 */
public class ServerConnectionImpl {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3258408439326781744L;
	public static final String ADD_VP = "ADD VP";
	public static final String DROP_VP = "DROP VP";
	public static final String GET_VP_LIST = "GET VP LIST";
	public static final String GET_NODE_LIST = "GET NODE LIST";
	public static final String DROP_NODE = "DROP NODE";
	public static final String ADD_NODE = "ADD NODE";
	
	private Logger logger = Logger.getLogger(ServerConnectionImpl.class);
	
	private ConnectionManagerImpl connectionManager;
	private QueryScheduler queryScheduler;
	private ClusterQueryProcessorEngine clusterQueryProcessorEngine;
	private LprfLoadBalancer loadBalancer;
	private boolean autoCommit = false;

    /**
     * 
     */
    public ServerConnectionImpl(ConnectionManagerImpl connectionManager, ClusterQueryProcessorEngine clusterQueryProcessorEngine, QueryScheduler queryScheduler, LprfLoadBalancer loadBalancer) throws RemoteException {
		this.connectionManager = connectionManager;
    	this.clusterQueryProcessorEngine = clusterQueryProcessorEngine;
    	this.queryScheduler = queryScheduler;
		this.loadBalancer = loadBalancer;		
		logger.info(Messages.getString("serverconnection.newconnection"));
    }
	
	public ResultSet executeQuery(String query) throws RemoteException, XQException, SQLException {
		if(query.startsWith(GET_VP_LIST))
			return connectionManager.listVirtualPartitionedTable();
		else if(query.startsWith(GET_NODE_LIST))
			return connectionManager.getNodesList();
			
		if(query.toUpperCase().trim().equals("SELECT DUMP"))
			return queryScheduler.dump();
		ResultSet rs = null;	
		
		SelectQueryManager qm = new SelectQueryManager(query,connectionManager.getMetaData(),queryScheduler.getNextQueryNumber(),clusterQueryProcessorEngine,loadBalancer); 		
		rs = queryScheduler.executeQuery(qm);				
	
		return rs;
	}
	
	public int executeUpdate(String query) throws RemoteException, XQException {
		if(query.startsWith(ADD_VP)) {
			String[] params = query.substring(ADD_VP.length()+1).split(" ");
			connectionManager.addVirtualPartitionedTable(params[0],params[1]);
			return 0;
		} else if(query.startsWith(DROP_VP)) {
			connectionManager.dropVirtualPartitionedTable(query.substring(DROP_VP.length()+1));
			return 0;						
		} else if(query.startsWith(ADD_NODE)) {
			String[] params = query.substring(ADD_NODE.length()+1).split(" ");
			connectionManager.addNode(params[0],Integer.parseInt(params[1]));
			return 0;			
		} else if(query.startsWith(DROP_NODE)) {
			connectionManager.dropNode(Integer.parseInt(query.substring(DROP_NODE.length()+1)));
			return 0;			
		}			
		
		UpdateQueryManager qm = new UpdateQueryManager(query,queryScheduler.getNextQueryNumber(),clusterQueryProcessorEngine);				
		int count = queryScheduler.executeUpdate(qm);
								
		return count;
	}	
	
	public PartiXVPDatabaseMetaData getMetaData() throws RemoteException, XQException {
		return connectionManager.getMetaData();
	}

	public void setAutoCommit(boolean autoCommit) throws RemoteException, XQException {		
		if(this.autoCommit && !autoCommit)
			commit();
		this.autoCommit = autoCommit;		
		
		queryScheduler.beginTransaction();		
				
		if(!autoCommit)
			executeUpdate(JdbcUtil.BEGIN_TRANSACTION);					
	}
	
	public void commit() throws RemoteException, XQException {
		executeUpdate(JdbcUtil.COMMIT);
		queryScheduler.endTransaction();		
	}
	public void rollback() throws RemoteException, XQException {
		executeUpdate(JdbcUtil.ROLLBACK);		
		queryScheduler.endTransaction();
	}
	
	public void close() throws XQException, RemoteException {
		if(connectionManager != null)
			connectionManager.notifyClosedConnection(this);			
		connectionManager = null;		
		queryScheduler = null;
		clusterQueryProcessorEngine = null;
		loadBalancer = null;
		//UnicastRemoteObject.unexportObject(this,true);		
	}

	public boolean getAutoCommit() throws XQException, RemoteException {
		return autoCommit;
	}
}
