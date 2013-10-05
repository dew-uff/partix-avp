package mediadorxml.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DatabaseFactory {
    
    private static final String CONFIG_FILE_DEFAULT_FILE_PATH = "configuration.xml";
    
    private static final String CONFIG_FILE_HOST_ELEMENT = "host";
    private static final String CONFIG_FILE_PORT_ELEMENT = "port";
    private static final String CONFIG_FILE_USERNAME_ELEMENT = "user";
    private static final String CONFIG_FILE_PASSWORD_ELEMENT = "password";
    private static final String CONFIG_FILE_TYPE_ELEMENT = "type";
    private static final String CONFIG_FILE_DATABASE_NAME_ELEMENT = "databaseName";
    private static final String CONFIG_FILE_DATABASE_ELEMENT = "database";
    
    private static final String TYPE_BASEX = "BASEX";
    private static final String TYPE_SEDNA = "SEDNA";
    
    public static Database getLocalDatabase() throws IOException {
        return getLocalDatabase(new FileInputStream(CONFIG_FILE_DEFAULT_FILE_PATH));
    }
    
    public static Database getDatabase(String host) throws IOException {
        return getDatabase(new FileInputStream(CONFIG_FILE_DEFAULT_FILE_PATH),host);
    }
    
    public static Database getLocalDatabase(InputStream fileStream) throws IOException {

        String thisHostname = InetAddress.getLocalHost().getHostName();
            
        return getDatabase(fileStream, thisHostname);
    }
    
    public static Database getDatabase(InputStream fileStream, String host) throws IOException {

        try
        {
            Element frag = getDatabaseElement(fileStream, host);
            
            if (frag == null) {
                return null;
            }
            
            return extractDatabase(frag);
        }
        catch(Exception e) {
            // TODO enhance exception handling. converting all is not the answer.
            throw new IOException(e);
        }
    }
    
    public static Database[] getAllDatabases() throws IOException {
        File file = new File(CONFIG_FILE_DEFAULT_FILE_PATH);
        System.out.println("Configuration filepath: " + file.getAbsolutePath());
        return getAllDatabases(new FileInputStream(file));
    }

    public static Database[] getAllDatabases(InputStream fileStream) throws IOException {
        List<Database> databases = new ArrayList<Database>();
        try {
            DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = b.newDocumentBuilder();
            Document doc = builder.parse(fileStream);
            
            NodeList allDbElements = doc.getElementsByTagName(CONFIG_FILE_DATABASE_ELEMENT);
            for (int i = 0; i < allDbElements.getLength(); i++) {
                Element dbElement = (Element) allDbElements.item(i);
                databases.add(extractDatabase(dbElement));
            }
            return databases.toArray(new Database[0]);
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    private static Element getDatabaseElement(InputStream fileStream,
            String host) throws ParserConfigurationException, SAXException, IOException {
        
        DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = b.newDocumentBuilder();
        Document doc = builder.parse(fileStream);
        NodeList allIds = doc.getElementsByTagName(CONFIG_FILE_HOST_ELEMENT);
        for (int i = 0; i < allIds.getLength(); i++) {
            Node node = allIds.item(i);
            if (node.getTextContent().equals(host)) {
                return (Element) node.getParentNode();
            }
        }
        
        return null;
    }

    private static Database extractDatabase(Element fragment) throws IOException {
        if (fragment.getElementsByTagName(CONFIG_FILE_TYPE_ELEMENT).item(0)
                .getTextContent().equals(TYPE_BASEX)) {
            return getBaseXDatabase(fragment);
        } 
        else if (fragment.getElementsByTagName(CONFIG_FILE_TYPE_ELEMENT).item(0)
                .getTextContent().equals(TYPE_SEDNA)) {
            return getSednaDatabase(fragment);
        } 
        else  {
            throw new IOException("Database type not supported");
        }
        
    }
    
    private static Database getBaseXDatabase(Element fragment) throws IOException {
        String host = fragment.getElementsByTagName(CONFIG_FILE_HOST_ELEMENT).item(0)
                .getTextContent();
        
        String port = fragment.getElementsByTagName(CONFIG_FILE_PORT_ELEMENT).item(0)
                .getTextContent();
        
        String user = fragment.getElementsByTagName(CONFIG_FILE_USERNAME_ELEMENT).item(0)
                .getTextContent();
        
        String pass = fragment.getElementsByTagName(CONFIG_FILE_PASSWORD_ELEMENT).item(0)
                .getTextContent();
        
        return new BaseXDatabase(host, Integer.parseInt(port), user, pass);
    }
    
    private static Database getSednaDatabase(Element fragment) throws IOException {
        String host = fragment.getElementsByTagName(CONFIG_FILE_HOST_ELEMENT).item(0)
                .getTextContent();
        
        String port = fragment.getElementsByTagName(CONFIG_FILE_PORT_ELEMENT).item(0)
                .getTextContent();
        
        String user = fragment.getElementsByTagName(CONFIG_FILE_USERNAME_ELEMENT).item(0)
                .getTextContent();
        
        String pass = fragment.getElementsByTagName(CONFIG_FILE_PASSWORD_ELEMENT).item(0)
                .getTextContent();
        
        String database = fragment.getElementsByTagName(CONFIG_FILE_DATABASE_NAME_ELEMENT).item(0)
                .getTextContent();
        
        return new SednaDatabase(host, Integer.parseInt(port), user, pass, database);
    }
}
