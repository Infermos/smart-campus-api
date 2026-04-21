package smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String timestamp = dateFormat.format(new Date());
        
        String message = String.format("[%s] REQUEST  - Method: %s, URI: %s", 
                                       timestamp, method, path);
        
        // Output to console (goes to server logs)
        System.out.println(message);
        
        // Logger output (meets specification requirement)
        logger.info("Incoming Request - Method: " + method + ", URI: " + path);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, 
                       ContainerResponseContext responseContext) throws IOException {
        
        int status = responseContext.getStatus();
        String timestamp = dateFormat.format(new Date());
        
        String message = String.format("[%s] RESPONSE - Status: %d", 
                                       timestamp, status);
        
        // Output to console (goes to server logs)
        System.out.println(message);
        
        // Logger output (meets specification requirement)
        logger.info("Outgoing Response - Status: " + status);
    }
}