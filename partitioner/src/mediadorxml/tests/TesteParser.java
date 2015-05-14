package mediadorxml.tests;

import java.io.StringReader;

import uff.dew.svp.javaccparser.ParseException;
import uff.dew.svp.javaccparser.SimpleNode;
import uff.dew.svp.javaccparser.TokenMgrError;
import uff.dew.svp.javaccparser.XQueryParser;


public class TesteParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String queryStr = "<bib>\r\n {\r\n  for $b in doc('bib.xml')/bib/book\r\n  where $b/publisher = \"Addison-Wesley\" and $b/@year > 1991\r\n  return\r\n   <book year='{ $b/@year }'>\r\n     { $b/title }\r\n    </book>\r\n }\r\n</bib>\r\n";
		//String queryStr = "<results>\r\n  {\r\n    for $b in doc('bib.xml')/bib/book,\r\n        $t in $b/title,\r\n        $a in $b/author\r\n    return\r\n        <result>\r\n            { $t }    \r\n            { $a }\r\n        </result>\r\n  }\r\n</results>";
		
		String queryStr = "<results>\r\n  {\r\n    for $b in doc('bib.xml')/bib/book\r\n    return\r\n        <result>\r\n            { $b/title }    \r\n            { $b/author }\r\n        </result>\r\n  }\r\n</results>";
		
		XQueryParser p = new XQueryParser(new StringReader(queryStr));
		
		try 
        {
            System.out.println( "\nquery = " + queryStr + "\n" );

            SimpleNode root = p.Start();
            root.dump( "" );
        }
        catch( ParseException e ) 
        { 
            System.out.print( e.getMessage() ); 
            System.out.println();
        }   
		// I'm not sure what distinguishes a ParseException 
		// from a TokenMgrError -- they appear quite similar

        catch( TokenMgrError tke )
        {
            System.out.print( tke.getMessage() );
            System.out.println();
        }  
		
		catch (Exception e){
			System.out.print( e.getMessage() );
            System.out.println();
		}

	}
}
