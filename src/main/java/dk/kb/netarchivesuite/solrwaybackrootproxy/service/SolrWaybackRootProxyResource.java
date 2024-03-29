package dk.kb.netarchivesuite.solrwaybackrootproxy.service;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.netarchivesuite.solrwaybackrootproxy.properties.PropertiesLoaderWeb;

//No path except the context root+servletpath for the application. When deployed as root 

@Path("/")
public class SolrWaybackRootProxyResource {

  private static final Logger log = LoggerFactory.getLogger(SolrWaybackRootProxyResource.class);
 
  public static void main(String[] args) {
      String url="http:/test/cat.png";
      System.out.print(fixMissingProtocolSlash(url));
      
      
  }
  
  //In jersey you need this method to catch a rootcontext call. The proxy method needs at least 
  //one character more besides the root call.
   @GET
   public Response get(@Context UriInfo uriInfo, @Context HttpServletRequest httpRequest)
   {
     return proxy(uriInfo, httpRequest);      
   }
  
  /* 
   * Jersey syntax to match all
   * You can not just remove the ?, it will still need at least 1 character.
   */
  @GET
  @Path("/{var:.*?}")
  public Response proxy(@Context UriInfo uriInfo, @Context HttpServletRequest httpRequest) {
    try {
      //The log method is called before return so all information will be logged in same block in log-file.
      
      
      // Warning. You can not understand the code below, need to be refactored
      // and unittestet
      // For some reason the var regexp does not work with comma (;) and other
      // characters. So I have to grab the full url from uriInfo           

      
      
      // String
      // refererUrl="teg-desktop.sb.statsbiblioteket.dk:8080/solrwayback/services/web/20010704073938/http://opasia.dk/kultur/musik/anmeldelser/an_paradise_lost.shtml";
      // String leakUrlStr= "http://localhost:8080/images/leaked.png?test=123";
       String leakUrlStr = uriInfo.getRequestUri().toString();
          


      // Determine if the leak is relative (/images/test.png etc.) or
      // absolute(http://domain.com/blabla/test.png).
      // in first case find url from referer and combine it to the correct url,
      // in latter just use this URL.
      boolean relativeLeak = false;
      if (isSolrWaybackHostAndStartsWith(leakUrlStr, "")){ //just see if it is solwayback host (with or without port)         
        relativeLeak = true;        
      }      

      String refererUrl = httpRequest.getHeader("referer");


      if (refererUrl == null) {
        //This will open happen if user type a new URL in the browser.
        //Some OpenWayback users just type url in the browser field and it will open (some  harvested version) of
        //that file. The problem is crawltime is unknown. Just return latest harvest for that site.
        //The user can then open the calender and pick another harvest time.
        
        //Show toolbar. Year 2999 should be last harvest and will also tell user this is not a true harvest time.        
        String redirect = PropertiesLoaderWeb.WAYBACK_SERVER_ROOT+ "/solrwayback/services/web/29990101000000/" + leakUrlStr;
                        
        URI uri = new URI(redirect);
        logLeakRedirection( leakUrlStr, refererUrl, uri.toString(), "NO REFEFRER");
        return Response.seeOther(uri).build(); // Jersey way to forward response.       
      }
              
      //Fixing leak from style-sheets. Referer is Solrwayback service syntax
      if  ( (isSolrWaybackHostAndStartsWith(refererUrl, "/solrwayback/services/view"))          
         || (isSolrWaybackHostAndStartsWith(refererUrl, "/solrwayback/services/download")) ){

        //Special case.               
        URL refererURL = new URL(refererUrl);
        String leakAuth = refererURL.getAuthority();
        
        int index = leakUrlStr.indexOf(leakAuth);
        String leakUrlPart = leakUrlStr.substring(index + leakAuth.length()); //
        
        
        String query = refererURL.getQuery();
        if (query == null){
          
          logLeakRedirection( leakUrlStr, refererUrl, "NOT_FOUND(404)", "NOT FOUND");
          return Response.status(Response.Status.NOT_FOUND).build();
        }
        String leakUrlPartEncoded= URLEncoder.encode((leakUrlPart), "UTF-8");
        
        String queryAndUrlInfo=query+"&urlPart="+leakUrlPartEncoded;                        
        log.info("queryAndLeakInfo(missing params?):"+queryAndUrlInfo);
        String redirect = PropertiesLoaderWeb.WAYBACK_SERVER_ROOT+"/solrwayback/services/viewFromLeakedResource?"+queryAndUrlInfo;                      
        URI uri = new URI(redirect);
        logLeakRedirection( leakUrlStr, refererUrl, uri.toString(), "VIEW FROM LEAKED RESOURCE");
        return Response.seeOther(uri).build();
      }
      String solrwaybackProxyUrl = null;
      String solrwaybackBaseUrl = null;
      String waybackDataObject = null;
      if (refererUrl.indexOf("/web/") >0){

        int dataStart = refererUrl.indexOf("/web/");
        solrwaybackBaseUrl = refererUrl.substring(0, dataStart + 5);
        solrwaybackProxyUrl = solrwaybackBaseUrl.replaceFirst("/web/","/webProxy/");
        waybackDataObject = refererUrl.substring(dataStart + 5);
      }
      else if (refererUrl.indexOf("/webProxy/") >0) {          
              
              int dataStart = refererUrl.indexOf("/webProxy/");
              solrwaybackBaseUrl = refererUrl.substring(0, dataStart + 10);
              solrwaybackProxyUrl = solrwaybackBaseUrl;
              waybackDataObject = refererUrl.substring(dataStart + 10);
      }
      else {
          log.error("Could not find waybackDataObject from refererurl:"+refererUrl);
          return Response.noContent().build();
      }
      
      
      


      int indexFirstSlash = waybackDataObject.indexOf("/");

      String waybackDate = waybackDataObject.substring(0, indexFirstSlash);
      
      
      String waybackUrl = waybackDataObject.substring(indexFirstSlash + 1);
       
      //For some reason it can start with http:/test/ or https:/test/ ( the extra // is missing from protocol)
            
      waybackUrl=fixMissingProtocolSlash(waybackUrl);
            
      URL leakUrl = new URL(leakUrlStr);
      String leakAuth = leakUrl.getAuthority();
      
      
      
      int index = leakUrlStr.indexOf(leakAuth);

      // From http://localhost:8080/images/leaked.png the urlPart will be
      // /images/leaked.png?test=123);
      String leakUrlPart = leakUrlStr.substring(index + leakAuth.length()); //      

     // log.info("refererURL created:"+waybackUrl);
      URL refererURL = new URL(waybackUrl);
            
      String refererAuth = refererURL.getAuthority();
     //log.info("refererAiuth created:"+refererAuth);


      if (relativeLeak) {
        String redirect = solrwaybackProxyUrl + waybackDate + "/http://" + refererAuth + leakUrlPart;
                
        URI uri = new URI(redirect);
        logLeakRedirection( leakUrlStr, refererUrl, uri.toString(), "RELEATIVE LEAK");
        return Response.seeOther(uri).build(); // Jersey way to forward response.
      } else {
        String redirect = solrwaybackProxyUrl + waybackDate + "/" + leakUrlStr;       
        URI uri = new URI(redirect);
        logLeakRedirection( leakUrlStr, refererUrl, uri.toString(), "ABSOLUTE LEAK");
        return Response.seeOther(uri).build(); // Jersey way to forward response.
      }

    } catch (Exception e) {
      log.error("Error resolving leak:"+uriInfo.toString());
      e.printStackTrace();
      return Response.ok().build();
    }
  }

  //The tricky is to check with and without the port. using defaults ports 80 or 443 for htts wil have browsers strip the port, 
  //Is the referer url from Solrwayback itself and does it star with urlPart
  private static boolean isSolrWaybackHostAndStartsWith(String refererUrl, String urlPart) throws Exception{
    String solrWaybackBaseUrlFull =  PropertiesLoaderWeb.WAYBACK_SERVER_ROOT;
    String solrWaybackBaseWithOutPort =  removePortFromUrl(solrWaybackBaseUrlFull);
       
    if (refererUrl.startsWith(solrWaybackBaseUrlFull+urlPart)){
      return true;
    }
    else if (refererUrl.startsWith(solrWaybackBaseWithOutPort+urlPart)){
      return true;
    }
    return false;
  }
  
  
  /*
   * Syncronized so logs are not mixed. Performance overhead is neglible
   */
  
 public static String fixMissingProtocolSlash(String url) {
     
     if (url.startsWith("http:/") && !url.startsWith("http://")) {
         url ="http://"+url.substring(6);
         
     }
     else if (url.startsWith("https:/") && !url.startsWith("https://")) {
         url ="https://"+url.substring(7);
     }
     
     return url;
     
 }
  
  public static String removePortFromUrl(String url) throws Exception {

    URL aURL = new URL(url);    
   return aURL.getProtocol()+"://"+aURL.getHost()+aURL.getPath();
}
  
  private static synchronized void logLeakRedirection(String leakString, String referer, String redirectUrl,  String leakType  ){
    log.info("****************************************************************************************************************");
    log.info("LeakString:"+leakString);
    log.info("Referer:"+referer);
    log.info("Redirect Url:"+redirectUrl);    
    log.info("Leak type:"+leakType);
    log.info("****************************************************************************************************************");   
  }
  
}
