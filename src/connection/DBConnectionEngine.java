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

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

import org.xml.sax.SAXException;

import wrapper.sedna.ConnectionSedna;
import wrapper.sedna.XMLUtil;
import wrapper.sedna.XQueryResult;
import wrapper.sedna.XQueryWrapper;
import wrapper.sedna.XQueryWrapperBase;

import commons.Logger;
import commons.Messages;
import commons.JdbcUtil;
import commons.PargresDatabaseMetaData;
import commons.PargresRowSet;
import commons.DatabaseProperties;
import commons.PartiXVPDatabaseMetaData;


public class DBConnectionEngine extends XQueryWrapperBase {

	private static final long serialVersionUID = 3616727171272880692L;
	private Logger logger = Logger.getLogger(DBConnectionEngine.class);
	private static Object block = new Object();
	private static int connectionCount = 0;
	private int connectionId;
	private XQConnection dbConn = null;
	/* prepStat can't be global because of an lack of performance on how PostgreSQL treat 
       preparedStatement */
	//private PreparedStatement prepStat;
	//private String sql;
	//private int[] arguments;
	private DBConnectionPoolEngine dbPool;

	/** adapted by Luiz Matos */
	public DBConnectionEngine(DBConnectionPoolEngine dbPool, String hostName, String databaseName, String dbLogin, String dbPassword, int dbmsX)
			throws RemoteException, XQException, SQLException {
		super();
		this.dbPool = dbPool;
		connectDb(hostName, databaseName, dbLogin, dbPassword, dbmsX);
		synchronized (block) {
			connectionId = connectionCount;
			connectionCount++;
		}
	}

	/** Creates a new instance of DBGatewayEngine */
//	public DBConnectionEngine(DBConnectionPoolEngine dbPool, String jdbcDriver,
//			String dbUrl, String login, String password)
//					throws RemoteException, SQLException {
//		super();
//		this.dbPool = dbPool;
//		connectDb(jdbcDriver, dbUrl, login, password);
//		synchronized (block) {
//			connectionId = connectionCount;
//			connectionCount++;
//		}
//	}

	public XQConnection getConnection() {
		return dbConn;
	}

	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void disposeConnection() throws RemoteException, SQLException, XQException {
		try {
			dbPool.disposeConnection(this);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	/* adapated by Luiz Matos */
	private void connectDb(String hostName, String databaseName, String dbLogin, String dbPassword, int dbmsX) throws SQLException, XQException {
		try {

			ConnectionSedna con = new ConnectionSedna();
			this.dbConn = con.establishSednaConnection();

			//TODO: Ver como fica o autocommit
			this.dbConn.setAutoCommit(true);
			//checkAvailability();
			logger.debug(Messages.getString("dbconnection.newconnection", connectionId));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

//	private void connectDb(String jdbcDriver, String dbUrl, String dbLogin, String dbPassword) throws XQException {
//		try {
//			Class.forName(jdbcDriver);
//			//this.dbConn = DriverManager.getConnection(dbUrl, dbLogin, dbPassword);
//			//TODO: Ver como fica o autocommit
//			this.dbConn.setAutoCommit(true);
//			checkAvailability();
//			logger.debug(Messages.getString("dbconnection.newconnection",
//					connectionId));
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e);
//			throw new XQException(e.getMessage());
//		}
//	}

	public void close() throws XQException, RemoteException, SQLException {
		logger.debug(Messages.getString("dbconnection.closed", connectionId));
		if (!dbConn.isClosed()) {
			dbConn.close();
		}
		//UnicastRemoteObject.unexportObject(this, true);
	}

	/*	public void prepareStatement(String query) throws SQLException,
			RemoteException {
		//prepStat = dbConn.prepareStatement(query);
        sql = query;
        int numberOfArguments = 0;
        int i = 0;
        while (i < sql.lastIndexOf("?")){
            i = sql.indexOf("?", i + 1);
            numberOfArguments++;
        }
        arguments = new int[numberOfArguments + 1];

		logger.debug(Messages.getString("dbconnection.prepare",new Object[]{connectionId,query}));
	}
	 */
	/*public void setArgumentPreparedStatement(int argumentNumber, int value)
			throws SQLException, RemoteException {
		//prepStat.setInt(argumentNumber, value);
        arguments[argumentNumber] =  value;
	}
	 */
	/*public ResultSet executePreparedStatement() throws SQLException,
			RemoteException {
		ResultSet rs;

        //PreparedStatement prepStat;
        //prepStat = dbConn.prepareStatement(sql);
		Statement st = dbConn.createStatement();
		String mysql = sql;
        for (int i = 1; i <= arguments.length - 1; ++i)
        	mysql = mysql.replaceFirst("\\?",""+arguments[i]);
            //prepStat.setInt(i, arguments[i]);


        long begin = System.currentTimeMillis();
        //rs = prepStat.executeQuery();
        rs = st.executeQuery(mysql);

        logger.debug(Messages.getString("dbconnection.prepare",new Object[]{connectionId,(System.currentTimeMillis() - begin)}));
        PargresRowSet result = new PargresRowSet();
        result.populate(rs);
        rs.close();
		return result;
	}

	public void closePreparedStetement() throws SQLException, RemoteException {
		//if (prepStat != null) {
		//	prepStat.close();
		//	prepStat = null;
		//}
		//System.out.println("PreparedStatement closed");
	}*/

	public boolean isClosed() throws XQException, RemoteException, SQLException {
		return dbConn.isClosed();
	}

	public XQueryResult executeXQuery(String query) throws XQException, RemoteException, SQLException {
		XQResultSequence xqr = null;
		XQExpression xqe = null;

		String queryStr;
		String resultString = "";
		final XQueryResult result = new XQueryResult();

		//Iniciando a compilacao
		long startTime = 0;
		long compileTime = 0;
		long localTime =0;

		xqe = this.dbConn.createExpression();	
		System.out.println ("Query received: " + query);

		queryStr = this.updateViewLocation(query);
		System.out.println("Testing ..." + queryStr);
		//System.out.println("apos updateViewLocation - " + (System.nanoTime() - logTime)/1000000);

		// Inclusão de nodo root na query
		//queryStr = "<root>{ " + queryStr + " }</root>";
		//System.out.println( "query final" +queryStr);

		//Inicializando a execucao local da consulta
		startTime = System.currentTimeMillis();
		xqr = xqe.executeQuery(queryStr);
		localTime = System.currentTimeMillis() - startTime;
		//Finalizando a execucao local

		if (!xqr.next()){				
			result.setResult("");
		}			

		do {
			resultString += xqr.getItemAsString(null);	

		} while (xqr.next());

		System.out.println(resultString);			
		result.setResult(resultString);
		System.out.println("Imprimindo o tempo de execucaoo local" + localTime);

		result.setTimeMsLocal(localTime);
		result.setSuccess(true);
		//result.setTimeMsCompile(compileTime);
		result.setTimeMsRemote(0); // A query é executada totalmente local
		result.setTimeMsCommunicRemote(0);
		result.setTotalBytes(result.getResult().getBytes().length);
		result.setNumberQueriesExecuted(1);

		logger.debug(Messages.getString("dbconnection.queryexecuted", new String[] { connectionId + "", query }));

		xqe.close();
		xqr.close();
		return (XQueryResult) result;
	}

	private void checkAvailability() throws XQException {

		boolean ok = false;

			//TODO: DEFINIR CONSULTA DE TESTE QUE FUNCIONE EM QUALQUER BASE E EM QUALQUER DBMSX
			String testQuery;
			if(this.dbConn.getMetaData().getProductName().equals("Sedna"))
				testQuery = "fn:count(/)";
			else 
				testQuery = "fn:count(/)";			

			ok = this.dbConn.createExpression().executeQuery(testQuery).getBoolean(); 

		if(!ok)
			System.out.println("Database not connected.");;			
	}

	public void clear() throws RemoteException, XQException, SQLException {
		this.dbConn.rollback();
		//System.gc();
	}

	/* adapted by Luiz Matos */
	public int executeUpdate(String query) throws XQException, RemoteException {
		if (query.equals(JdbcUtil.BEGIN_TRANSACTION)) {
			this.dbConn.setAutoCommit(true);
			return 0;
		} else if (query.equals(JdbcUtil.COMMIT)) {
			this.dbConn.commit();
			return 0;
		} else if (query.equals(JdbcUtil.ROLLBACK)) {
			this.dbConn.rollback();
			return 0;
		} else {
			int count = -1;
			XQResultSequence r;
			try {
				r = this.dbConn.createExpression().executeQuery(query);
				count = r.count();
			} catch (XQException e) {
				logger.error(e);
				throw e;
			}
			logger.debug(Messages.getString("dbconnection.updateexecuted", new String[] { connectionId + "", query }));
			logger.debug(Messages.getString("dbconnection.rowsmodified", new String[] { connectionId + "", count + "" }));
			return count;
		}
	}

	/* adapted by Luiz Matos */
	public PartiXVPDatabaseMetaData getMetaData(DatabaseProperties prop, DatabaseProperties sort) throws XQException, RemoteException, SQLException {
		if (this.dbConn == null) {
			try {
				Thread.sleep(10000);
			} catch (Exception e) {

			}
		}
		if (this.dbConn == null)
			throw new XQException("DBConnection not created yet!");
		return new PartiXVPDatabaseMetaData(prop, sort, this.dbConn.getMetaData());
	}

	//	public PargresDatabaseMetaData getMetaData(DatabaseProperties prop, DatabaseProperties sort) throws SQLException,
	//	RemoteException {
	//		if (dbConn == null) {
	//			try {
	//				Thread.sleep(10000);
	//			} catch (Exception e) {
	//
	//			}
	//		}
	//		if (dbConn == null)
	//			throw new SQLException("DBConnection not created yet!");
	//		return new PargresDatabaseMetaData(prop, sort, dbConn.getMetaData());
	//	}

}
