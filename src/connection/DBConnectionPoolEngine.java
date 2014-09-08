/*
 * DBGatewayEngine.java
 *
 * Created on 12 novembre 2003, 16:57
 */

package connection;

/**
 *
 * @author  lima
 */

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.xml.xquery.XQException;

import commons.Logger;
import commons.Messages;
 
public class DBConnectionPoolEngine {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3258415044935824178L;

	private Logger logger = Logger.getLogger(DBConnectionPoolEngine.class);
	
    private String jdbcUrl;
    private String jdbcDriver;
    private String hostName;
    private String databaseName;
    private String dbLogin;
    private String dbPassword;
    private LinkedList<DBConnectionEngine> connPool;
    private int iniConnectionPoolSize;
    private int dbmsX;
    

    /** adapted by Luiz Matos */
    public DBConnectionPoolEngine(String hostName, String databaseName, String dbLogin, String dbPassword, int iniPoolSize, int dbmsX) throws RemoteException, SQLException, XQException {
        super();
        this.hostName = hostName;
        this.databaseName = databaseName;
        this.dbLogin = dbLogin;
        this.dbPassword = dbPassword;
        this.iniConnectionPoolSize = iniPoolSize;
        this.dbmsX = dbmsX;
        createConnectionPool();
		logger.info("DBConnection Pool created!");		
    }
    
    /** Creates a new instance of DBGatewayEngine */
//    public DBConnectionPoolEngine(String jdbcUrl, String jdbcDriver, String dbLogin, String dbPassword, int iniPoolSize) throws RemoteException, SQLException {
//        super();
//        this.jdbcUrl = jdbcUrl;
//        this.jdbcDriver = jdbcDriver;
//        this.dbLogin = dbLogin;
//        this.dbPassword = dbPassword;
//        this.iniConnectionPoolSize = iniPoolSize;
//        createConnectionPool();
//		logger.info("DBConnection Pool created!");		
//    }

	public void shutdown() throws Throwable {
		//UnicastRemoteObject.unexportObject(this,true);
		finalize();
	}
	
    protected void finalize() throws Throwable {
        closeConnectionPool();
        super.finalize();		
    }
	   
    public synchronized void createConnectionPool() throws RemoteException, XQException, SQLException {
        connPool = new LinkedList<DBConnectionEngine>();
        if( iniConnectionPoolSize > 0 ) {
            for( int i = 0; i < iniConnectionPoolSize; i++ ) {
            	DBConnectionEngine conn = new DBConnectionEngine(this, hostName, databaseName, dbLogin, dbPassword, dbmsX);
                connPool.addLast( conn );
            }
        }
        notifyAll();
    }
    
    public synchronized void closeConnectionPool() throws RemoteException, XQException, SQLException {
        DBConnectionEngine conn;		
        while( connPool.size() > 0 ) {
            conn = (DBConnectionEngine) connPool.removeFirst();
            conn.close();
            conn = null;
        }
		logger.debug(Messages.getString("dbconnectionpool.closed"));		
        notifyAll();
    }

    public synchronized DBConnectionEngine reserveConnection() throws XQException, RemoteException, SQLException {
        DBConnectionEngine conn;
        if( connPool.size() > 0 )
            conn = (DBConnectionEngine) connPool.removeFirst();
        else
            conn = new DBConnectionEngine(this, hostName, databaseName, dbLogin, dbPassword, dbmsX);
        notifyAll();
        return conn;
    }
    
    public synchronized void disposeConnection(DBConnectionEngine conn) throws RemoteException, XQException, SQLException {
        if( !conn.isClosed() ) {
            conn.clear();
            connPool.addFirst( conn );
        }
        notifyAll();
    }
    
}
