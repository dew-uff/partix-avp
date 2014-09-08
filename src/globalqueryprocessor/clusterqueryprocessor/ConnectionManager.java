package globalqueryprocessor.clusterqueryprocessor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.xquery.XQException;

import commons.PartiXVPDatabaseMetaData;


public interface ConnectionManager extends Remote {
	public static String OBJECT_NAME = "ConnectionCqp";
	public static int DEFAULT_PORT = 8050;
	public int createConnection(String user, String password) throws RemoteException;
	public boolean forceNewPartitionLimits(String table, String field, long first, long last) throws RemoteException;
	public ResultSet executeQuery(int connectionId, String sql) throws RemoteException,XQException,SQLException;
	public int executeUpdate(int connectionId, String sql) throws RemoteException,XQException;
	public PartiXVPDatabaseMetaData getMetaData(int connectionId) throws XQException, RemoteException;
	public void setAutoCommit(int connectionId, boolean autoCommit) throws XQException, RemoteException;
	public void commit(int connectionId) throws XQException, RemoteException;
	public void rollback(int connectionId) throws XQException, RemoteException;
	public void close(int connectionId) throws XQException, RemoteException;
	public boolean getAutoCommit(int connectionId) throws XQException, RemoteException;
			
}
