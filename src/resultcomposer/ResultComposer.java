/*
 * OlapClusterServer.java
 *
 * Created on 15 décembre 2003, 17:27
 */

package resultcomposer;

/**
 *
 * @author lima
 * @author lzomatos
 * 
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.ResultSet;


public interface ResultComposer {
	public void start();

	public void addResult(ResultSet q);

	public void finish();

	public boolean finished();
}
