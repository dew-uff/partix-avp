/*
 * OlapClusterServer.java
 *
 * Created on 15 décembre 2003, 17:27
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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xquery.XQException;


import globalqueryprocessor.clusterqueryprocessor.queryplanner.QueryInfo;
import globalqueryprocessor.globalquerytask.GlobalQueryTask;
import commons.DatabaseProperties;
import commons.PartiXVPDatabaseMetaData;
import localqueryprocessor.localquerytask.LocalQueryTask;
import resultcomposer.ResultComposerGlobal;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range;
import util.SystemResourceStatistics;
import wrapper.sedna.XQueryResult;


public interface NodeQueryProcessor extends Remote {

	public GlobalQueryTask newGlobalQueryTask(NodeQueryProcessor localnqp,
			ArrayList<NodeQueryProcessor> nqp, String query, Range range,
			int[] partitionSizes, boolean getStatistics,
			boolean localResultComposition, 
			int queryExecutionStrategy, boolean performDynamicLoadBalancing,
			boolean getSystemResourceStatistics, QueryInfo qi) throws RemoteException;

	public LocalQueryTask newLocalQueryTask(int id, GlobalQueryTask globalTask,
			ResultComposerGlobal globalResultComposer, String query,
			Range range, int queryExecutionStrategy, int numlqts,
			boolean localResultComposition,
			boolean performDynamicLoadBalancing, boolean getStatistics,
			boolean getSystemResourceStatistics, QueryInfo qi) throws RemoteException;

	public ResultComposerGlobal newGlobalResultComposer(int numlqts, QueryInfo qi) throws RemoteException, SQLException;

	public void turnSystemMonitorOn() throws IOException, RemoteException;

	public void turnSystemMonitorOff() throws RemoteException;

	public void resetSystemMonitorCounters() throws RemoteException;

	public SystemResourceStatistics getSystemResourceStatistics()
			throws RemoteException;

	public boolean quotedDateIntervals() throws RemoteException;

	public void shutdown() throws RemoteException, NotBoundException,
	MalformedURLException;

	public PartiXVPDatabaseMetaData getDatabaseMetaData(DatabaseProperties prop, DatabaseProperties sort) throws RemoteException, XQException, SQLException;
	
	public XQueryResult executeQuery(String query) throws RemoteException, XQException, SQLException;
	public int executeUpdate(String query) throws RemoteException, XQException, SQLException;

	public int getNodeId() throws RemoteException;
}
