/*
 * LocalQueryTaskEngine_FGVP.java
 *
 * Created on 25 mai 2004, 14:45
 */

package localqueryprocessor.dynamicrangegenerator.avp.queryexecutor;

/**
 * 
 * @author lima
 */

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import connection.DBConnectionEngine;
import connection.DBConnectionPoolEngine;
import localqueryprocessor.localquerytask.LocalQueryTask;
import resultcomposer.ResultComposer;
import util.LocalQueryTaskStatistics;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range;


public class QueryExecutorFgvp extends QueryExecutor {

    private int nextRangeValue;

    /** Creates a new instance of LocalQueryTaskEngine_FGVP */
    public QueryExecutorFgvp(LocalQueryTask lqt, DBConnectionPoolEngine dbpool,
            ResultComposer resultComposer, String query, Range range,
            LocalQueryTaskStatistics lqtStatistics) throws RemoteException {
        super(lqt, dbpool, resultComposer, query, range, lqtStatistics);
    }

    protected boolean getQueryLimits(int[] limits) {
        if (nextRangeValue >= range.getOriginalLastValue()) {
            state = ST_RANGE_PROCESSED;
            return false;
        } else {
            switch (state) {
            case ST_STARTING_RANGE: {
                limits[0] = range.getFirstValue();
                state = ST_PROCESSING_RANGE;
                break;
            }
            case ST_PROCESSING_RANGE: {
                limits[0] = nextRangeValue;
                break;
            }
            default: {
                throw new IllegalThreadStateException(
                        "LocalQueryTaskEngine_AVP Exception: getQueryLimits() should not be called while in state "
                                + state + "!");
            }
            }
            limits[1] = limits[0] + range.getVPSize();
            if (limits[1] > range.getOriginalLastValue())
                limits[1] = range.getOriginalLastValue();
            nextRangeValue = limits[1];
            return true;
        }
    }

    protected ResultSet executeSubQuery(DBConnectionEngine dbconn,
            LocalQueryTaskStatistics statistics, String query, int[] limit) throws java.sql.SQLException,
            java.rmi.RemoteException {
        /*long queryStart = 0;
        ResultSet result;

        if (statistics != null)
            queryStart = System.currentTimeMillis();
        result = dbconn.executePreparedStatement();
        if (statistics != null)
            statistics.queryFinished(System.currentTimeMillis() - queryStart);
        return result;*/
    	throw new SQLException("Not implemented method!");
    }

	@Override
	protected void executeSubQuery(String query, int[] limit) {
		// TODO Auto-generated method stub
		
	}

}
