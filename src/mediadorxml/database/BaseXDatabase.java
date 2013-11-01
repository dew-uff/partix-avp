package mediadorxml.database;

import java.io.IOException;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;

import net.xqj.basex.BaseXXQDataSource;

public class BaseXDatabase extends BaseDatabase {

    public BaseXDatabase(String host, int port, String username, String password) throws IOException {
        BaseXXQDataSource basexDataSource = new BaseXXQDataSource();

        basexDataSource.setServerName(host);
        basexDataSource.setPort(port);
        basexDataSource.setUser(username);
        basexDataSource.setPassword(password);
        
        dataSource = basexDataSource;
    }
    
    @Override
    public void deleteCollection(String collectionName) throws XQException {
        executeCommand("DROP DB " + collectionName);
    }

    @Override
    public void createCollection(String collectionName) throws XQException {
        executeCommand("CREATE DB " + collectionName);
    }
    
    @Override
    public void createCollectionWithContent(String collectionName, String dirPath) 
            throws XQException {
        executeCommand("CREATE DB " + collectionName + " " + dirPath);
        
    }
    
    private void executeCommand(String command) throws XQException {
        XQConnection conn = getConnection();
        XQExpression exp = conn.createExpression();
        exp.executeCommand(command);
        exp.close();
        conn.close();
    }

    @Override
    public int getCardinality(String xpath, String document, String collection)
            throws XQException {
        String query = "let $elm := doc('" + document + "')/" + xpath + " return count($elm)";
        
        return Integer.parseInt(executeQueryAsString(query));
    }
    
    @Override
    public String getHost() {
        BaseXXQDataSource ds = (BaseXXQDataSource) dataSource;
        return ds.getServerName();
    }

    @Override
    public int getPort() {
        BaseXXQDataSource ds = (BaseXXQDataSource) dataSource;
        return Integer.parseInt(ds.getProperty("port"));
    }

    @Override
    public String getUsername() {
        BaseXXQDataSource ds = (BaseXXQDataSource) dataSource;
        return ds.getUser();
    }

    @Override
    public String getPassword() {
        BaseXXQDataSource ds = (BaseXXQDataSource) dataSource;
        return ds.getPassword();
    }

    @Override
    public void loadFileInCollection(String collectionName, String filePath)
            throws XQException {
        // TODO Auto-generated method stub
        
    }

}
