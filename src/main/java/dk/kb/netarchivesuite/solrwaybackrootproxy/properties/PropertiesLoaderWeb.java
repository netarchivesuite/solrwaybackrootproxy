package dk.kb.netarchivesuite.solrwaybackrootproxy.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesLoaderWeb {

	private static final Logger log = LoggerFactory.getLogger(PropertiesLoaderWeb.class);
	private static final String DEFAULT_PROPERTY_WEB_FILE = "solrwaybackweb.properties";

	public static final String WAYBACK_SERVER_PROPERTY="wayback.baseurl";
		
	public static String WAYBACK_SERVER_ROOT = null;

		
	private static Properties serviceProperties = null;
	//Default values.
 

	  public static void initProperties() {
	      initProperties(DEFAULT_PROPERTY_WEB_FILE);      
	    }
	    	
	public static void initProperties(String propertyFile) {
		try {

			log.info("Initializing solrwaybackweb-properties");
	        String user_home=System.getProperty("user.home");

			File f = new File(user_home,propertyFile);
            if (!f.exists()) {
              log.info("Could not find contextroot specific propertyfile:"+propertyFile +". Using default:"+DEFAULT_PROPERTY_WEB_FILE);
              propertyFile=DEFAULT_PROPERTY_WEB_FILE;                                 
            }                        
           log.info("Load web-properties: Using user.home folder:" + user_home +" and propertyFile:"+propertyFile);
			
			
			InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(user_home,propertyFile)), "ISO-8859-1");

			serviceProperties = new Properties();
			serviceProperties.load(isr);
			isr.close();

			String solrwaybackUrl = serviceProperties.getProperty(WAYBACK_SERVER_PROPERTY);
    		//Remove /solrwayback
			WAYBACK_SERVER_ROOT = solrwaybackUrl.substring(0,solrwaybackUrl.indexOf("/solrwayback"));
	    
			
			log.info("Property:"+ WAYBACK_SERVER_PROPERTY +" = " + WAYBACK_SERVER_ROOT);
		
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("Could not load property file:"+ propertyFile);
		}
	}
}
