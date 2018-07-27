package dk.kb.netarchivesuite.solrwaybackrootproxy.service;

import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.netarchivesuite.solrwaybackrootproxy.properties.PropertiesLoaderWeb;

//No path except the context root+servletpath for the application. When deployed as root 

@Path("/")
public class SolrWaybackRootProxyResource {

  private static final Logger log = LoggerFactory.getLogger(SolrWaybackRootProxyResource.class);

  /*
   * 
   * Jersey syntax to match all
   */
  @GET
  @Path("/{var:.*?}")
  public Response proxy(@Context UriInfo uriInfo, @Context HttpServletRequest httpRequest) {
    try {

      // Warning. You can not understand the code below, need to be refactored
      // and unittestet

      // For some reason the var regexp does not work with comma (;) and other
      // characters. So I have to grab the full url from uriInfo
      log.info("/rootproxy called with uri:"+uriInfo.getRequestUri());

      // String
      // refererUrl="teg-desktop.sb.statsbiblioteket.dk:8080/solrwayback/services/web/20010704073938/http://opasia.dk/kultur/musik/anmeldelser/an_paradise_lost.shtml";
      // String leakUrlStr= "http://localhost:8080/images/leaked.png?test=123";
      String leakUrlStr = uriInfo.getRequestUri().toString();


      // Determine if the leak is relative (/images/test.png etc.) or
      // absolute(http://domain.com/blabla/test.png).
      // in first case find url from referer and combine it to the correct url,
      // in latter just use this URL.
      boolean relativeLeak = false;
      if (leakUrlStr.startsWith(PropertiesLoaderWeb.WAYBACK_SERVER_ROOT) ) {
        relativeLeak = true;        
      }      

      String refererUrl = httpRequest.getHeader("referer");
      if (refererUrl == null) {
        log.info("referer missing for url:" + leakUrlStr);
        return Response.status(Response.Status.NOT_FOUND).build();       
      }
  
            
      //Fixing leak from style-sheets. Referer is Solrwayback service syntax
      if (refererUrl.startsWith(PropertiesLoaderWeb.WAYBACK_SERVER_ROOT +"/solrwayback/services/view")
       || refererUrl.startsWith(PropertiesLoaderWeb.WAYBACK_SERVER_ROOT +"/solrwayback/services/download") ){
        //Special case.               
        URL refererURL = new URL(refererUrl);
        String leakAuth = refererURL.getAuthority();
        int index = leakUrlStr.indexOf(leakAuth);
        String leakUrlPart = leakUrlStr.substring(index + leakAuth.length()); //
        // System.out.println(leakAuth);        
        
        String query = refererURL.getQuery();
        if (query == null){
          log.warn("missing url solrwayback params from referer:"+refererUrl);
          return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        String queryAndUrlInfo=query+"&urlPart="+leakUrlPart;                        
        String redirect = PropertiesLoaderWeb.WAYBACK_SERVER_ROOT+"/solrwayback/services/viewFromLeakedResource?"+queryAndUrlInfo;
        log.info("Leak type:SolrwaybackResources view/download. Leak url:"+leakUrlStr +" Refererer:"+refererUrl +" -> Ŕedirect url:"+redirect);       
        URI uri = UriBuilder.fromUri(redirect).build();
        return Response.seeOther(uri).build();
      }
  
      int dataStart = refererUrl.indexOf("/web/");
      String solrwaybackBaseUrl = refererUrl.substring(0, dataStart + 5);
      String waybackDataObject = refererUrl.substring(dataStart + 5);
      // System.out.println(waybackDataObject);
      int indexFirstSlash = waybackDataObject.indexOf("/");

      String waybackDate = waybackDataObject.substring(0, indexFirstSlash);
      // System.out.println(waybackDate);
      String waybackUrl = waybackDataObject.substring(indexFirstSlash + 1);
      // System.out.println(waybackUrl);

      URL leakUrl = new URL(leakUrlStr);
      String leakAuth = leakUrl.getAuthority();

      // System.out.println(leakAuth);
      int index = leakUrlStr.indexOf(leakAuth);

      // From http://localhost:8080/images/leaked.png the urlPart will be
      // /images/leaked.png?test=123);
      String leakUrlPart = leakUrlStr.substring(index + leakAuth.length()); //
      // System.out.println(leakUrlPart);

      URL refererURL = new URL(waybackUrl);
      String refererAuth = refererURL.getAuthority();
      // System.out.println(refererAuth);

      // System.out.println(referleakUrlPart);

      if (relativeLeak) {
        String redirect = solrwaybackBaseUrl + waybackDate + "/http://" + refererAuth + leakUrlPart;
        log.info("Leak type:Relative url. Leak url:"+leakUrlStr +" Refererer:"+refererUrl +" -> Redirect url:"+redirect);
        
        URI uri = UriBuilder.fromUri(redirect).build();
        return Response.seeOther(uri).build(); // Jersey way to forward response.
      } else {
        String redirect = solrwaybackBaseUrl + waybackDate + "/" + leakUrlStr;
        log.info("Leak type:Absolute url. Leak url:"+leakUrlStr +" Refererer:"+refererUrl +" ->  Ŕedirect url:"+redirect);        
        URI uri = UriBuilder.fromUri(redirect).build();
        return Response.seeOther(uri).build(); // Jersey way to forward response.
      }

    } catch (Exception e) {
      e.printStackTrace();
      return Response.ok().build();
    }
  }

}
