package globalqueryprocessor.globalquerytask;

/**
 * 
 * @author lima
 * @author lzomatos
 * 
 */

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.ResultSet;

import localqueryprocessor.localquerytask.LocalQueryTask;
import util.LocalQueryTaskStatistics;
import util.SystemResourceStatistics;

public interface GlobalQueryTask extends Remote {
    static public final int QE_STRATEGY_FGVP = 0;

    static public final int QE_STRATEGY_AVP = 1;

    public ResultSet start() throws RemoteException, InterruptedException,
            Exception;

    public void localIntervalFinished(int localTaskId) throws RemoteException;

    public void localQueryTaskFinished(int localTaskId,
            LocalQueryTaskStatistics statistics,
            SystemResourceStatistics resourceStatistics, Exception exception) throws RemoteException;

    public void getLQTReferences(int[] id, LocalQueryTask[] reference)
            throws RemoteException;

    public LocalQueryTask getLQTReference(int id) throws RemoteException;
}
