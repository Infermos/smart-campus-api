package smartcampus.config;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import smartcampus.filter.LoggingFilter;
import smartcampus.mapper.*;
import smartcampus.resource.*;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class AppConfig extends Application {
    
    static {
        System.err.println("AppConfig CLASS LOADED (static)");
    }
    
    public AppConfig() {
        System.err.println("AppConfig CONSTRUCTOR CALLED");
    }
    
    @Override
    public Set<Class<?>> getClasses() {
        System.err.println("getClasses() METHOD CALLED");
        
        Set<Class<?>> classes = new HashSet<>();
        
        // Explicitly register filter
        classes.add(LoggingFilter.class);
        System.err.println("Registered: LoggingFilter");
        
        // Explicitly register mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        System.err.println("Registered: RoomNotEmptyExceptionMapper");
        
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        System.err.println("Registered: LinkedResourceNotFoundExceptionMapper");
        
        classes.add(SensorUnavailableExceptionMapper.class);
        System.err.println("Registered: SensorUnavailableExceptionMapper");
        
        classes.add(GlobalExceptionMapper.class);
        System.err.println("Registered: GlobalExceptionMapper");
        
        // Explicitly register resources
        classes.add(DiscoveryResource.class);
        System.err.println("Registered: DiscoveryResource");
        
        classes.add(RoomResource.class);
        System.err.println("Registered: RoomResource");
        
        classes.add(SensorResource.class);
        System.err.println("Registered: SensorResource");
        
        classes.add(LogViewerResource.class);
        System.err.println("Registered: LogViewerResource");
        
        System.err.println("Total classes registered: " + classes.size());
        
        return classes;
    }
}