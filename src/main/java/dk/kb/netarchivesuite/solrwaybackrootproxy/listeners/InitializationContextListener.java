package dk.kb.netarchivesuite.solrwaybackrootproxy.listeners;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.netarchivesuite.solrwaybackrootproxy.properties.PropertiesLoaderWeb;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitializationContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(InitializationContextListener.class);
    private static String version;
    

    
    // this is called by the web-container before opening up for requests.(defined in web.xml)
    public void contextInitialized(ServletContextEvent event) {

        log.info("SolrWaybackRootProxy starting up...");
        Properties props = new Properties();
        try {
          
            String webbAppContext = event.getServletContext().getContextPath();                    
            props.load(InitializationContextListener.class.getResourceAsStream("/build.properties"));
            version = props.getProperty("APPLICATION.VERSION");                                          
            PropertiesLoaderWeb.initProperties(webbAppContext+"web.properties"); //frontend
                        
            log.info("SolrWaybackRootProxy version " + version + " started successfully");

        } catch (Exception e) {
            log.error("failed to initialize service", e);
            e.printStackTrace();
            throw new RuntimeException("failed to initialize service", e);
        }
    }



    public void contextDestroyed(ServletContextEvent sce) {
      // TODO Auto-generated method stub
      
    }

}
