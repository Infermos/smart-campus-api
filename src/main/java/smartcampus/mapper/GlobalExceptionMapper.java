package smartcampus.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        
        // Log the full stack trace for debugging purposes
        logger.severe("Unexpected error occurred: " + ex.getMessage());
        ex.printStackTrace();
        
        // Return a generic error message without exposing internal details
        Map<String, Object> error = new HashMap<>();
        error.put("error", "An unexpected error occurred. Please contact support.");
        error.put("status", 500);
        
        return Response.status(500)
                .entity(error)
                .build();
    }
}