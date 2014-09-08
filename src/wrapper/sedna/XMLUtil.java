package wrapper.sedna;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.xml.internal.txw2.Document;

public class XMLUtil {  
	   public static org.w3c.dom.Document stringToDom(String xmlSource) throws SAXException, ParserConfigurationException,  
	         IOException {  
	      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	      DocumentBuilder builder = factory.newDocumentBuilder();  
	      return builder.parse(new InputSource(new StringReader(xmlSource)));  
	   }  
	  
	   public static String format(String xmlSource) throws IOException, ParserConfigurationException, SAXException {  
	      org.w3c.dom.Document document = stringToDom(xmlSource);  
	      OutputFormat of = new OutputFormat();  
	      of.setIndent(2);  
	      of.setOmitXMLDeclaration(true);  
	  
	      Writer writer = new StringWriter();  
	      XMLSerializer serializer = new XMLSerializer(writer, of);  
	      serializer.serialize(document);  
	  
	      return writer.toString();  
	   }  
	   
	}