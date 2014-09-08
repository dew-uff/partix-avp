/*
 * Created on 08/04/2005
 */
package globalqueryprocessor.clusterqueryprocessor.querymanager;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import commons.Logger;
import commons.Messages;
import commons.JdbcUtil;
import commons.PartiXVPDatabaseMetaData;

import connection.DatabaseDisconnectionException;
import globalqueryprocessor.clusterqueryprocessor.ClusterQueryProcessorEngine;
import globalqueryprocessor.clusterqueryprocessor.HelloRowSet;
import loadbalancer.LprfLoadBalancer;
import globalqueryprocessor.clusterqueryprocessor.queryplanner.QueryInfo;
import globalqueryprocessor.clusterqueryprocessor.queryplanner.QueryPlanner;

/**
 * @author Bernardo
 */
public class SelectQueryManager extends AbstractQueryManager {
	private Logger logger = Logger.getLogger(SelectQueryManager.class);

	private QueryInfo queryInfo;

	private LprfLoadBalancer loadBalancer;

	public SelectQueryManager(String sql, PartiXVPDatabaseMetaData metadata,
			int queryNumber,
			ClusterQueryProcessorEngine clusterQueryProcessorEngine,
			LprfLoadBalancer loadBalancer) throws SQLException {
		super(sql,queryNumber,clusterQueryProcessorEngine);
		
		this.queryInfo = QueryPlanner.parser(sql,metadata, clusterSize);

		this.loadBalancer = loadBalancer;
		
		logger.debug(Messages.getString("serverconnection.queryCreate",
				new String[] { getQueryNumber() + "",
						queryInfo.getQueryTypeName(), queryInfo.getSql() }));
	}

	public ResultSet executeQuery() throws SQLException, RemoteException {
		schedullerWait();
		ResultSet resultSet = null;
		if (queryInfo.getQueryType() == QueryInfo.INTER_QUERY) {
			resultSet = executeInterQuery();
		} else if (queryInfo.getQueryType() == QueryInfo.INTRA_QUERY) {
			resultSet = executeIntraQuery();
		}
		return resultSet;
	}

	private ResultSet executeInterQueryDummy() throws SQLException,
			RemoteException {
		ResultSet resultSet = null;
		// *** UNIT TEST ***/
		if (queryInfo.getSql().equals(JdbcUtil.TEST_QUERY)) {
			resultSet = new HelloRowSet();
		} else if (queryInfo.getSql().equals(JdbcUtil.LAZY_INTER_QUERY)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			resultSet = new HelloRowSet();
		}
		// *******************/
		return resultSet;
	}

	private ResultSet executeInterQuery() throws SQLException, RemoteException {
		ResultSet resultSet = null;
		int nodeIndex = -1;
		try {

			while (resultSet == null) {
				nodeIndex = loadBalancer.next();
				loadBalancer.notifyStartInterQuery(nodeIndex);
				try {
					resultSet = executeInterQueryDummy();
					if (resultSet == null)
						resultSet = clusterQueryProcessorEngine.getNQP(
								nodeIndex).executeQuery(queryInfo.getSql());
					loadBalancer.notifyFinishInterQuery(nodeIndex);
				} catch (DatabaseDisconnectionException dde) {
					loadBalancer.notifyFinishInterQuery(nodeIndex);
					clusterQueryProcessorEngine.dropNode(nodeIndex);
				}
			}

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			loadBalancer.notifyFinishInterQuery(nodeIndex);
		}
		return resultSet;
	}
	
	private ResultSet executeIntraQueryDummy() throws SQLException, RemoteException {
		ResultSet resultSet = null;
		// *** UNIT TEST ***/
		if (queryInfo.getSql().equals(JdbcUtil.LAZY_INTRA_QUERY)) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();				
			}
			resultSet = new HelloRowSet();
		}
		// /**** *****////		
		return resultSet;
	}

	private ResultSet executeIntraQuery() throws SQLException, RemoteException {
		ResultSet resultSet = null;

		int nodeIndex = loadBalancer.next();
		loadBalancer.notifyStartIntraQuery(nodeIndex);
		try {
			resultSet = executeIntraQueryDummy();
			if(resultSet == null) {
				globalqueryprocessor.clusterqueryprocessor.queryplanner.QueryAvpDetail queryAvpDetail = queryInfo
						.getQueryAvpDetail();
				resultSet = clusterQueryProcessorEngine
						.executeQueryWithAVP(queryAvpDetail.getQuery(),
								queryAvpDetail.getRange(), queryAvpDetail
										.getNumNQPs(), queryAvpDetail
										.getPartitionSizes(), queryAvpDetail
										.isGetStatistics(), queryAvpDetail
										.isLocalResultComposition(),
								queryAvpDetail.isPerformDynamicLoadBalancing(),
								queryAvpDetail.isGetSystemResourceStatistics(),
								queryInfo);
			}
		} catch (SQLException e) {
			logger.error(e);
			loadBalancer.notifyFinishIntraQuery(nodeIndex);
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			loadBalancer.notifyFinishIntraQuery(nodeIndex);
		}

		loadBalancer.notifyFinishIntraQuery(nodeIndex);
		return resultSet;
	}

	public QueryInfo getQueryInfo() {
		return queryInfo;
	}

}
