/*
 * Created on 08/04/2005
 */
package globalqueryprocessor.clusterqueryprocessor;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Random;

import javax.sql.rowset.RowSetMetaDataImpl;
import javax.xml.xquery.XQException;

import globalqueryprocessor.clusterqueryprocessor.ConnectionManager;
import commons.Logger;
import commons.Messages;
import config.Configurator;
import globalqueryprocessor.clusterqueryprocessor.ClusterQueryProcessorEngine;
import globalqueryprocessor.clusterqueryprocessor.UserManager;
import loadbalancer.LprfLoadBalancer;
import globalqueryprocessor.clusterqueryprocessor.QueryScheduler;
import commons.PargresRowSet;
import commons.DatabaseProperties;
import commons.PartiXVPDatabaseMetaData;
import localqueryprocessor.nodequeryprocessor.NodeQueryProcessor;
import util.MyRMIRegistry;


/**
 * @author Bernardo
 */
public class ConnectionManagerImpl implements ConnectionManager {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4051324561100124982L;
	private Logger logger = Logger.getLogger(ConnectionManagerImpl.class);
	private ClusterQueryProcessorEngine clusterQueryProcessorEngine;
	private QueryScheduler queryScheduler;
	private LprfLoadBalancer loadBalancer;
	private int port;
	private PartiXVPDatabaseMetaData meta;	
	private HashMap<Integer,ServerConnectionImpl> opennedConnections = new HashMap<Integer,ServerConnectionImpl>();
	private UserManager userManager;
	private DatabaseProperties databaseProperties;
    private DatabaseProperties sortProperties;
	private Random keyGen = new Random(System.currentTimeMillis());
	
    public ConnectionManagerImpl(int port, String configFileName) throws RemoteException {
    	try {
    		this.clusterQueryProcessorEngine = new ClusterQueryProcessorEngine();
    		Configurator configurator = new Configurator(configFileName,this);
	    	logger.info(Messages.getString("connectionManagerImpl.init"));
	    	loadBalancer = new LprfLoadBalancer(0);	    	
	     	configurator.config();
	    	userManager = configurator.getUserManager();
	    	databaseProperties = configurator.getDatabaseProperties();
            sortProperties = configurator.getSortProperties();
	    	
	    	logger.info(Messages.getString("connectionManagerImpl.step1"));
    		//RMISocketFactory
			this.port = port;
    		queryScheduler = new QueryScheduler(this);
    		logger.info(Messages.getString("connectionManagerImpl.step2"));
			logger.info(Messages.getString("connectionManagerImpl.step3"));

			reloadMetaData();
			if(meta == null)
				throw new Exception("PartiXVPDatabaseMetaData not loaded!");
			logger.info(Messages.getString("connectionManagerImpl.step4"));
			meta.dump();
    		register();
    		logger.info(Messages.getString("connectionManagerImpl.partixvpReady"));	
        } catch (Exception e) {
			e.printStackTrace();
        } 
    }
    /* (non-Javadoc)
     * @see org.pargres.cqp.ConnectionManager#createConnection()
     */    
    public int createConnection(String user, String password) throws RemoteException {
    	userManager.verify(user,password);
    	ServerConnectionImpl serverConnection = new ServerConnectionImpl(this,clusterQueryProcessorEngine,queryScheduler,loadBalancer);
    	Integer key = new Integer(keyGen.nextInt());    	
    	opennedConnections.put(key,serverConnection);    	    	
    	return key;
    }
    
    public void notifyClosedConnection(ServerConnectionImpl serverConnection) {
    	opennedConnections.remove(serverConnection);
    }
    
	public void invalidMetaData() {
		meta = null;
	}    
	
	public PartiXVPDatabaseMetaData getMetaData() throws RemoteException, XQException, SQLException {
		if(meta == null)
			reloadMetaData();
		return meta;
	}
	
	public synchronized void reloadMetaData() throws RemoteException, XQException, SQLException {
			logger.info(Messages.getString("connectionManagerImpl.metadataReloading"));
			NodeQueryProcessor nqp = clusterQueryProcessorEngine.getNQP(0);
			System.out.println("Node Id: " + nqp.getNodeId());

			meta = nqp.getDatabaseMetaData(databaseProperties, sortProperties);
			logger.info(Messages.getString("connectionManagerImpl.metadataReloaded"));
	}	

    private void register() {
        try {
			//A comunicação via RMI é quase tão rápida quanto via socket
			//http://martin.nobilitas.com/java/thruput.html			
	    	MyRMIRegistry.bind(port,getRmiAddress(),this);
			logger.info(Messages.getString("connectionManagerImpl.register",new Object[]{port,getRmiAddress()}));
        } catch (Exception e) {
			e.printStackTrace();
        } 
	}
	
	private String getRmiAddress() {
		return "rmi://localhost:"+port+"/"+ConnectionManager.OBJECT_NAME;
	}
	
    private void unregister() {
        try {
			MyRMIRegistry.unbind(port,getRmiAddress(),this);
        } catch (Exception e) {
			e.printStackTrace();
        }
    }	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		logger.debug(Messages.getString("connectionManagerImpl.finalize"));
	}
	
	public void destroy() {
		queryScheduler.shutdown();
		try {
			clusterQueryProcessorEngine.shutdown();
			clusterQueryProcessorEngine = null;			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Object[] array = opennedConnections.values().toArray();
		for(int i = 0; i < array.length; i++) {
			try {
				((ServerConnectionImpl)array[i]).close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		unregister();
	}
	
    public static void main(String[] args) {
        int portNumber;
        String configFileName;

        if (args.length < 2) {
            System.out
                    .println("usage: java ConnectionManagerImpl "
                            + "query_processor_port_number ConfigFileName");
            return;
        }

        portNumber = Integer.parseInt(args[0]);
        configFileName = args[1].trim();

        try {
        	new ConnectionManagerImpl(portNumber,configFileName);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
	
	public boolean forceNewPartitionLimits(String table, String field, long first, long last) throws RemoteException {
		return meta.forceNewPartitionLimits(table,field,first,last);
	}	
	
	public ResultSet executeQuery(int connectionId, String query) throws RemoteException, XQException, SQLException {
		return opennedConnections.get(connectionId).executeQuery(query);
	}
	
	public int executeUpdate(int connectionId, String query) throws RemoteException, XQException {
		return opennedConnections.get(connectionId).executeUpdate(query);
	}	
	
	public PartiXVPDatabaseMetaData getMetaData(int connectionId) throws RemoteException, XQException {
		return opennedConnections.get(connectionId).getMetaData();
	}

	public void setAutoCommit(int connectionId, boolean autoCommit) throws RemoteException, XQException {		
		opennedConnections.get(connectionId).setAutoCommit(autoCommit);				
	}
	
	public void commit(int connectionId) throws RemoteException, XQException {
		opennedConnections.get(connectionId).commit();		
	}
	public void rollback(int connectionId) throws RemoteException, XQException {
		opennedConnections.get(connectionId).rollback();
	}
	
	public void close(int connectionId) throws RemoteException, XQException {
		opennedConnections.get(connectionId).close();
		//UnicastRemoteObject.unexportObject(this,true);		
	}

	public boolean getAutoCommit(int connectionId) throws XQException, RemoteException {
		return opennedConnections.get(connectionId).getAutoCommit();
	}

	public ResultSet getNodesList() throws XQException, RemoteException, SQLException {
		return clusterQueryProcessorEngine.getNodesList();
	}
	
	public void addVirtualPartitionedTable(String table, String field) throws RemoteException {
		databaseProperties.addProperties(table,field);
		try {			
			reloadMetaData();
		} catch (Exception e) {
			//databaseProperties.remove(table);
			e.printStackTrace();
		}
	}
	
	public boolean dropVirtualPartitionedTable(String table) throws RemoteException, XQException, SQLException {
		boolean ok = databaseProperties.remove(table);
		if(ok)
			reloadMetaData();
		return ok;
	}	
	
	public void addNode(String host, int port) throws RemoteException {
		clusterQueryProcessorEngine.addNode(host,port);
		loadBalancer.addNode();
	}
	public void dropNode(int nodeId) throws RemoteException {
		clusterQueryProcessorEngine.dropNode(nodeId);		
		loadBalancer.dropNode(nodeId);
	}	
	
	public ResultSet listVirtualPartitionedTable() throws XQException, SQLException, RemoteException {
		int i = 0;		
		PargresRowSet rs = new PargresRowSet();
		
		RowSetMetaDataImpl meta = new RowSetMetaDataImpl();
		meta.setColumnCount(1);
		meta.setColumnName(1, "DUMMY");
		meta.setColumnType(1, Types.VARCHAR);
		rs.setMetaData(meta);

		for(localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range range : getMetaData().getRangeList()) {
			String list = i+" - "+(String)range.getTableName()+"\t: "+(String)range.getField()+
			       "\t[cardinality = "+range.getCardinality()+", " +
			       "range init = "+range.getRangeInit()+", " +
			       "range end = "+range.getRangeEnd()+"]\n";
			i++;
			rs.moveToInsertRow();
			rs.updateString(1, list);
			rs.insertRow();
			rs.moveToCurrentRow();			
		}
		return rs;
	}	
}
