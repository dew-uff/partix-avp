/*
 * OlapClusterServerEngine.java
 *
 * Created on 15 décembre 2003, 17:35
 */

package localqueryprocessor.nodequeryprocessor;

/**
 * 
 * @author lima
 * @author lzomatos
 * 
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xquery.XQException;

import commons.Logger;
import commons.Messages;
import commons.PartiXVPDatabaseMetaData;
import connection.DBConnectionEngine;
import connection.DBConnectionPoolEngine;
import globalqueryprocessor.clusterqueryprocessor.queryplanner.QueryInfo;
import globalqueryprocessor.globalquerytask.GlobalQueryTask;
import globalqueryprocessor.globalquerytask.GlobalQueryTaskEngine;
import commons.DatabaseProperties;
import localqueryprocessor.localquerytask.LocalQueryTask;
import localqueryprocessor.localquerytask.LocalQueryTaskEngine;
import resultcomposer.ResultComposerEngineGlobal;
import resultcomposer.ResultComposerGlobal;
import util.MyRMIRegistry;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range;
import util.SystemResourceStatistics;
import util.SystemResourcesMonitor;
import wrapper.sedna.XQueryResult;


public class NodeQueryProcessorEngine implements NodeQueryProcessor {

	//private static final long serialVersionUID = 3257288049812322103L;
	private boolean started = false;
	private Logger logger = Logger.getLogger(NodeQueryProcessorEngine.class);
	private String objectName;
	//private boolean shutdownRequested;
	private DBConnectionPoolEngine localDbPool;
	private boolean quotedDateIntervals;
	private SystemResourcesMonitor monitor;
	private int port;
	//private static NodeQueryProcessor nqpDummy;

	/** adapted by Luiz Matos 
	 * @throws XQException 
	 * @throws SQLException */
	public NodeQueryProcessorEngine(String hostName, int port, String databaseName, String dbLogin, String dbPassword, boolean quotedDateIntervals, int dbmsX)
			throws RemoteException, XQException, SQLException {
		super();
		this.port = port;

		//this.shutdownRequested = false;
		this.localDbPool = new DBConnectionPoolEngine(hostName, databaseName, dbLogin, dbPassword, 1, dbmsX);
		this.quotedDateIntervals = quotedDateIntervals;
		this.monitor = null;
		this.started = true;

		objectName = "rmi://localhost:" + port + "/NodeQueryProcessor";
		try {
			//this.turnSystemMonitorOn();
 			MyRMIRegistry.bind(port, objectName, this);
			//nqpDummy = (NodeQueryProcessor) 
			MyRMIRegistry.lookup("localhost",port,objectName);

		} catch (Exception e) {
			logger.error("NodeQueryProcessorEngine Exception: "	+ e.getMessage());
			e.printStackTrace();
		} 		
		logger.info(Messages.getString("nodeQueryProcessorEngine.running",new Object[]{port,objectName}));		
	}
	
	/** Creates a new instance of OlapClusterServerEngine */
//	public NodeQueryProcessorEngine(int port, String jdbcDriver,
//			String jdbcUrl, String jdbcLogin, String jdbcPassword,
//			boolean quotedDateIntervals) throws RemoteException, SQLException {
//		super();
//		this.port = port;
//
//		//this.shutdownRequested = false;
//		this.localDbPool = new DBConnectionPoolEngine(jdbcUrl, jdbcDriver, jdbcLogin, jdbcPassword, 1);
//		this.quotedDateIntervals = quotedDateIntervals;
//		this.monitor = null;
//		this.started = true;
//
//		objectName = "//localhost:" + port + "/NodeQueryProcessor";
//		try {
//			//this.turnSystemMonitorOn();
//
//			MyRMIRegistry.bind(port, objectName, this);
//			//nqpDummy = (NodeQueryProcessor) 
//			MyRMIRegistry.lookup("localhost",port,objectName);
//
//		} catch (Exception e) {
//			logger.error("NodeQueryProcessorEngine Exception: "
//					+ e.getMessage());
//			e.printStackTrace();
//		} 		
//		logger.info(Messages.getString("nodeQueryProcessorEngine.running",new Object[]{port,objectName}));		
//	}

	protected void finalize() throws Throwable {
		turnSystemMonitorOff();
		((DBConnectionPoolEngine) localDbPool).shutdown();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		int portNumber, dbmsX;
		String hostName, jdbcDriverName, jdbcURL, databaseName, dbLogin, dbPwd;
		boolean quotedDateIntervals;
		//SystemResourcesMonitor monitor = null;

		if (args.length != 7) {
			System.out.println("usage: java NodeQueryProcessorEngine <hostName_or_IP> <port_number> <databaseName> <dbLogin> <dbPassword> <quotedDateInterval[0|1]> <dbmsX_used>");
			System.out.println("e.g.: java NodeQueryProcessorEngine localhost 3050 dblp SYSTEM MANAGER 0 1 - being <dbmsX_used> equals 1 -> Sedna; 2 -> BaseX; 3 -> undefined; 4 -> undefined");
			return;
		}
		hostName = args[0];
		portNumber = (new Integer(args[1])).intValue();
		databaseName = args[2];
		dbLogin = args[3];
		dbPwd = args[4];
		if (Integer.parseInt(args[5]) == 0)
			quotedDateIntervals = false;
		else
			quotedDateIntervals = true;
		dbmsX = (new Integer(args[6])).intValue();

		try {
			new NodeQueryProcessorEngine(hostName, portNumber, databaseName, dbLogin, dbPwd, quotedDateIntervals, dbmsX);
		} catch (Exception e) {
			//logger.error(e);
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public GlobalQueryTask newGlobalQueryTask(NodeQueryProcessor localnqp,
			ArrayList<NodeQueryProcessor> nqp, String query, Range range,
			int[] partitionSizes, boolean getStatistics,
			boolean localResultComposition, 
			int queryExecutionStrategy, boolean performDynamicLoadBalancing,
			boolean getSystemResourceStatistics, QueryInfo qi) throws RemoteException {
		GlobalQueryTask globalTask = new GlobalQueryTaskEngine(localnqp, nqp,
				query, range, getStatistics, localResultComposition,
				queryExecutionStrategy,
				performDynamicLoadBalancing, partitionSizes,
				getSystemResourceStatistics, qi);
		return globalTask;
	}

	public LocalQueryTask newLocalQueryTask(int id, GlobalQueryTask globalTask,
			ResultComposerGlobal globalResultComposer, String query,
			Range range, int queryExecutionStrategy, int numlqts,
			boolean localResultComposition,
			boolean performDynamicLoadBalancing, boolean getStatistics,
			boolean getSystemResourceStatistics, QueryInfo qi) throws RemoteException {
		LocalQueryTask localTask = new LocalQueryTaskEngine(id, globalTask,
				getDBConnectionPool(), globalResultComposer, query, range,
				queryExecutionStrategy, numlqts, localResultComposition,
				performDynamicLoadBalancing, getStatistics,
				(getSystemResourceStatistics ? monitor : null), qi);
		return localTask;
	}

	public ResultComposerGlobal newGlobalResultComposer(int numlqts, QueryInfo qi) throws RemoteException, SQLException {
		ResultComposerGlobal rc = new ResultComposerEngineGlobal(numlqts, qi);
		return rc;
	}

	public synchronized void shutdown() throws RemoteException,
	NotBoundException, MalformedURLException {
		logger.debug(Messages.getString("nodeQueryProcessorEngine.unbinding"));
		MyRMIRegistry.unbind(port,objectName,this);
		try {			
			finalize();
		} catch (Throwable t) {
			t.printStackTrace();
			logger.error(t);
		}
	}

	public DBConnectionPoolEngine getDBConnectionPool() throws RemoteException {
		return localDbPool;
	}

	public boolean quotedDateIntervals() throws RemoteException {
		return quotedDateIntervals;
	}

	public void turnSystemMonitorOn() throws IOException, RemoteException {
		if (monitor == null) {
			monitor = new SystemResourcesMonitor();
			monitor.start();
		}
	}

	public void turnSystemMonitorOff() throws RemoteException {
		if (monitor != null) {
			monitor.finish();
			monitor = null;
		}
	}

	public void resetSystemMonitorCounters() throws RemoteException {
		if (monitor != null)
			monitor.resetCounters();
	}

	public SystemResourceStatistics getSystemResourceStatistics()
			throws RemoteException {
		if (monitor != null)
			return monitor.getStatistics();
		else
			throw new RuntimeException("NodeQueryProcessorEngine Exception: monitor was off but the number of misses was demanded");
	}

	/**
	 * @return Returns the started.
	 */
	public boolean isStarted() {
		return started;
	}

	public PartiXVPDatabaseMetaData getDatabaseMetaData(DatabaseProperties prop, DatabaseProperties sort) throws RemoteException, XQException, SQLException {
		DBConnectionEngine con = null;
		PartiXVPDatabaseMetaData meta = null;
		try {
			con = this.localDbPool.reserveConnection();
			meta = con.getMetaData(prop, sort);
			this.localDbPool.disposeConnection(con);
		} catch (XQException e) {
			e.getMessage();
		}
		return meta;
	}

	public XQueryResult executeQuery(String query) throws RemoteException, XQException, SQLException {		
		DBConnectionEngine con = this.localDbPool.reserveConnection();
		XQueryResult result = con.executeXQuery(query);
		this.localDbPool.disposeConnection(con);
		return result;
	}

	public int executeUpdate(String query) throws RemoteException, XQException, SQLException {		
		DBConnectionEngine con = this.localDbPool.reserveConnection();
		int count = con.executeUpdate(query);
		this.localDbPool.disposeConnection(con);
		return count;
	}

	public int getNodeId() throws RemoteException {
		return this.hashCode();
	}
}
