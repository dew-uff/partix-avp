package wrapper.sedna;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

public class ConfigWrapper {

	protected static Properties properties;
	
	public ConfigWrapper(){
	}
	
	public static Properties getProperties(Object obj){
		if (properties == null){
			properties = new Properties();
			try{
				final URL url = obj.getClass().getClassLoader().getResource("wrapper_exist.properties");

				if (null == url) {
					//logger.warn("URL was null");
				}
				else{

			        String fname   = url.getFile();			        
			        //System.out.println(fname);
			        properties.load(new FileInputStream(URLDecoder.decode(fname, "UTF-8")));
				}					
			}
			catch (IOException e){
				//logger.error(e.getLocalizedMessage());
			}
		}
		//logger.info(properties.size());
		return properties;
	}
	
	public static String getProperty(String label, Object obj){
		return ConfigWrapper.getProperties(obj).getProperty(label);
	}
}
